/*
 * Created by JFormDesigner on Fri Mar 15 10:38:51 CET 2013
 */

package de.bund.bfr.knime.pmm.estimatedmodelreader;

import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import org.hsh.bfr.db.DBKernel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.Config;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import de.bund.bfr.knime.pmm.bfrdbiface.lib.Bfrdb;
import de.bund.bfr.knime.pmm.common.EstModelXml;
import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.ui.*;
import de.bund.bfr.knime.pmm.predictorview.PredictorViewNodeDialog;
import de.bund.bfr.knime.pmm.predictorview.SettingsHelper;
import de.bund.bfr.knime.pmm.predictorview.TableReader;
import de.bund.bfr.knime.pmm.timeseriesreader.*;

/**
 * @author Armin Weiser
 */
public class EmReaderUi extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7838365860944621209L;
	
	public static final String PARAM_PARAMETERS = "parameters";
	public static final String PARAM_PARAMETERNAME = "parameterName";
	public static final String PARAM_PARAMETERMIN = "parameterMin";
	public static final String PARAM_PARAMETERMAX = "parameterMax";
	public static final String PARAM_QUALITYMODE = "qualityFilterMode";
	public static final String PARAM_QUALITYTHRESH = "qualityThreshold";
	public static final String PARAM_TABLECOLInternalID = "TableInternalID"; // Col1
	public static final String PARAM_TABLECOLModelID = "TableModelID"; // Col2
	public static final String PARAM_TABLECOLModelName = "TableModelName"; // Col3
	public static final String PARAM_TABLECOLInitParam = "TableInitParam"; // Col4
	public static final String PARAM_TABLECOLInitParamValue = "TableInitParamValue"; // Col5
	public static final String PARAM_TABLECOLIsInitParam = "IsInitParam"; // Col6
	public static final String PARAM_TABLECOLFormulaID = "FormulaID"; // Col7

	public static final String PARAM_NOMDDATA = "withoutMdData";
	
	private Bfrdb db;
	
	public static final int MODE_OFF = 0;
	public static final int MODE_R2 = 1;
	public static final int MODE_RMS = 2;
	
	private SettingsHelper set = null;
	
	public EmReaderUi() {
		this(null);
	}
	public EmReaderUi(Bfrdb db) {
		this(db,null);
	}	
	public EmReaderUi(Bfrdb db, String[] itemListMisc) {								
		this(db,itemListMisc, true, true, true, false);
	}
	public EmReaderUi(Bfrdb db, String[] itemListMisc,
			boolean showModelOptions, boolean showQualityOptions, boolean showMDOptions, boolean showFilterResults) {		
		this.db = db;
		initComponents();		
		
		if (!showModelOptions) modelReaderUi.setVisible(false);
		if (!showQualityOptions) qualityPanel.setVisible(false);
		if (!showMDOptions) mdReaderUi.setVisible(false);
		if (!showFilterResults) {
			filterResults.getTableHeader().setVisible(false);
			filterResults.setVisible(false);
			scrollPane1.setVisible(false);
		}
		filterResults.getColumnModel().getColumn(1).setMinWidth(0);
		filterResults.getColumnModel().getColumn(1).setMaxWidth(0);
		filterResults.getColumnModel().getColumn(1).setWidth(0);
		filterResults.getColumnModel().getColumn(6).setMinWidth(0);
		filterResults.getColumnModel().getColumn(6).setMaxWidth(0);
		filterResults.getColumnModel().getColumn(6).setWidth(0);
		//if (!showDbTable) dbTable.setVisible(false);
	}

	private LinkedHashMap<String, Double[]> getParams(LinkedHashMap<String, DoubleTextField[]> params) {
		LinkedHashMap<String, Double[]> parameter = new LinkedHashMap<String, Double[]>();
		if (params != null && params.size() > 0) {
    		for (String par : params.keySet()) {
    			DoubleTextField[] dbl = params.get(par);
    			parameter.put(par, new Double[]{dbl[0].getValue(), dbl[1].getValue()});
    		}
    	}
		return parameter;
	}
	private String getWhereCondition(int level, String param, String param2, Double min, Double max) {
		String result =
				(min != null ? " AND (\"" + param + "\" >= " + min + " OR \"" + param + "\" IS NULL" +
						(level == 2 ? " OR POSITION_ARRAY('" + param2 + "' IN \"Independent2\") > 0 AND \"maxIndep2\"[POSITION_ARRAY('" + param2 + "' IN \"Independent2\")] >= " + min : "") +
						")" : "") +
		
				(max != null ? " AND (\"" + param + "\" <= " + max + " OR \"" + param + "\" IS NULL" +
						(level == 2 ? " OR POSITION_ARRAY('" + param2 + "' IN \"Independent2\") > 0 AND \"minIndep2\"[POSITION_ARRAY('" + param2 + "' IN \"Independent2\")] <= " + max : "") +
						")" : "");		
		return result;
	}
	private void getDataTable(Bfrdb db) {
		try {
			//create table TTEST ("ID" INTEGER, "Referenz" INTEGER, "Agens" INTEGER, "AgensDetail" VARCHAR(255), "Matrix" INTEGER, "MatrixDetail" VARCHAR(255), "Temperatur" Double, "pH" Double, "aw" Double, "CO2" Double, "Druck" Double, "Luftfeuchtigkeit" Double, "Sonstiges" INTEGER, "Kommentar" VARCHAR(1023), "Guetescore" INTEGER, "Geprueft" BOOLEAN);
			HashMap<String, Integer> codeLength = new HashMap<String, Integer>();
			codeLength.put("ADV", 4);
			codeLength.put("BLS", 3);
			codeLength.put("TOP", 4);
			codeLength.put("GS1", 3);
			codeLength.put("Combase", 5);
			codeLength.put("FA", 4);
			codeLength.put("SiLeBAT", 4);
			codeLength.put("N�hrmedien", 13);
			codeLength.put("VET", 5);
			String where = " TRUE ";
			if (mdReaderUi.getAgentID() > 0) {
				String matchingIDs = "" + mdReaderUi.getAgentID();
				ResultSet rs = DBKernel.getResultSet(db.getConnection(), "SELECT \"CodeSystem\",\"Code\" FROM " + DBKernel.delimitL("Codes_Agenzien") +
						" WHERE \"Basis\" = " + mdReaderUi.getAgentID(), false);
				if (rs != null && rs.first()) {
					do {
						String cs = rs.getString("CodeSystem");
						if (cs != null && codeLength.containsKey(cs)) {
							int codeLen = codeLength.get(cs);
							String aCode = rs.getString("Code");
							if (aCode == null) aCode = "12345678901234";
							if (aCode.length() >= codeLen) {
								ResultSet rs2 = DBKernel.getResultSet(db.getConnection(), "SELECT \"Basis\" FROM " + DBKernel.delimitL("Codes_Agenzien") +
										" WHERE " + DBKernel.delimitL("CodeSystem") + "='" + rs.getString("CodeSystem") +
										"' AND LEFT(" + DBKernel.delimitL("Code") + "," + codeLen + ") = '" + aCode.substring(0, codeLen) + "'", false);
								if (rs2 != null && rs2.first()) {
									do {
										matchingIDs += "," + rs2.getInt("Basis");
									} while (rs2.next());
								}							
							}
						}
					} while (rs.next());
				}

				where += " AND (\"Agens\" IS NULL OR \"Agens\" IN (" + matchingIDs + "))";
			}

			codeLength.put("ADV", 5);
			if (mdReaderUi.getMatrixID() > 0) {
				String matchingIDs = "" + mdReaderUi.getMatrixID();
				ResultSet rs = DBKernel.getResultSet(db.getConnection(), "SELECT \"CodeSystem\",\"Code\" FROM " + DBKernel.delimitL("Codes_Matrices") +
						" WHERE \"Basis\" = " + mdReaderUi.getMatrixID(), false);
				if (rs != null && rs.first()) {
					do {
						String cs = rs.getString("CodeSystem");
						if (cs != null && codeLength.containsKey(cs)) {
							int codeLen = codeLength.get(cs);
							String mCode = rs.getString("Code");
							if (mCode == null) mCode = "12345678901234";
							if (mCode.length() >= codeLen) {
								ResultSet rs2 = DBKernel.getResultSet(db.getConnection(), "SELECT \"Basis\" FROM " + DBKernel.delimitL("Codes_Matrices") +
										" WHERE " + DBKernel.delimitL("CodeSystem") + "='" + rs.getString("CodeSystem") +
										"' AND LEFT(" + DBKernel.delimitL("Code") + "," + codeLen + ") = '" + mCode.substring(0, codeLen) + "'", false);
								if (rs2 != null && rs2.first()) {
									do {
										matchingIDs += "," + rs2.getInt("Basis");
									} while (rs2.next());
								}							
							}
						}
					} while (rs.next());
				}

				where += " AND (\"Matrix\" IS NULL OR \"Matrix\" IN (" + matchingIDs + "))";
			}
			
    		int level = modelReaderUi.getLevel();
			LinkedHashMap<String, DoubleTextField[]> params = mdReaderUi.getParameter();
			for (String key : params.keySet()) {
				DoubleTextField[] dtf = params.get(key);
				if (key.equals(AttributeUtilities.ATT_TEMPERATURE)) {
					where += getWhereCondition(level, "Temperatur", "Temperature", dtf[0].getValue(), dtf[1].getValue());							
				}
				else if (key.equals(AttributeUtilities.ATT_PH)) {
					where += getWhereCondition(level, "pH", "pH", dtf[0].getValue(), dtf[1].getValue());
				}
				else if (key.equals(AttributeUtilities.ATT_AW)) {
					where += getWhereCondition(level, "aw", "aw", dtf[0].getValue(), dtf[1].getValue());
				}
			}
			
	    	try {
	    		boolean withoutMdData = withoutData.isSelected();
	    		List<KnimeTuple> hs = null;
				try {
					hs = EstimatedModelReaderNodeModel.getKnimeTuples(db, db.getConnection(),
							EstimatedModelReaderNodeModel.createSchema(withoutMdData, level), null, level, withoutMdData,
							getQualityMode(), getQualityThresh(), mdReaderUi.getMatrixString(), mdReaderUi.getAgentString(), mdReaderUi.getLiteratureString(),
							mdReaderUi.getMatrixID(), mdReaderUi.getAgentID(), mdReaderUi.getLiteratureID(), getParams(mdReaderUi.getParameter()),
							modelReaderUi.isModelFilterEnabled(), modelReaderUi.getModelList(), where);
				} catch (PmmException e) {
					e.printStackTrace();
				} catch (InvalidSettingsException e) {
					e.printStackTrace();
				}
	    		//System.err.println(where);
				//List<KnimeTuple> hs = EstimatedModelReaderNodeModel.getKnimeTuples(db, db.getConnection(), SchemaFactory.createM12DataSchema(), null, 2, false, where);
		    	if (hs != null && hs.size() > 0) {
		    		MyTableModel mtm = (MyTableModel) filterResults.getModel();
		    		PredictorViewNodeDialog pvnd = new PredictorViewNodeDialog(hs, set, true);
		    		//Map<String, Double> gpv0 = pvnd.getParamValues();
		    		//Map<String, String> gip0 = pvnd.getInitParams();
		    		JPanel mainComponent = pvnd.getMainComponent();
/*
		    		List<String> selectedIDs = new ArrayList<String>();
		    		Map<String, String> gip = new LinkedHashMap<String, String>();
		    		Map<String, Double> gpv = new LinkedHashMap<String, Double>();
		    		String selID = "";
		    		for (int i=0; i<mtm.getRowCount(); i++) {
		    			if (!selID.equals(mtm.getValueAt(i, 0).toString())) {
			    			selID = mtm.getValueAt(i, 0).toString();
			    			selectedIDs.add(selID);
			    			gpv = new LinkedHashMap<String, Double>();
			    			gip = new LinkedHashMap<String, String>();
		    			}
		    			gpv.put(mtm.getValueAt(i, 3).toString(), Double.parseDouble(mtm.getValueAt(i, 4).toString()));
		    			if (Boolean.parseBoolean(mtm.getValueAt(i, 5).toString())) gip.put(mtm.getValueAt(i, 6).toString(), mtm.getValueAt(i, 3).toString());
		    		}

		    		PredictorViewNodeDialog pvnd = new PredictorViewNodeDialog(hs, gip);
		    		JPanel mainComponent = pvnd.getMainComponent();
		    		ChartAllPanel chartPanel = (ChartAllPanel)mainComponent.getComponent(0);
			    	//addTab("Predictor view", mainComponent);

		    		chartPanel.getSelectionPanel().setSelectedIDs(selectedIDs);
		    		chartPanel.getConfigPanel().setParamXValues(gpv);
		    		*/

		    		Window parentWindow = SwingUtilities.windowForComponent(this); 
		    		Frame parentFrame = null;
		    		if (parentWindow instanceof Frame) {
		    		    parentFrame = (Frame)parentWindow;
		    		}		    		
		    		JDialog dialog = new JDialog(parentFrame);
		    		dialog.setModal(true);
		    		dialog.add(mainComponent);
		    		dialog.pack();
		    		centerOnScreen(dialog, true);
		    				    		
		    		dialog.setVisible(true);
		    		
		    		//List<String> ls = chartPanel.getSelectionPanel().getSelectedIDs();
		    		set = pvnd.getSettings();
		    		
		    		//SettingsHelper set = new SettingsHelper();
		    		TableReader reader = new TableReader(hs, set.getConcentrationParameters());
		    		Map<String, KnimeTuple> tm = reader.getTupleMap();
	    			mtm.removeAll();
		    		for (String id : set.getSelectedIDs()) {
		    			KnimeTuple tuple = tm.get(id);
		    			PmmXmlDoc estModelXml = tuple.getPmmXml(Model1Schema.ATT_ESTMODEL);
		    			EstModelXml emx = (EstModelXml) estModelXml.get(0);
		    			Map<String, Double> gpv = pvnd.getParamValues();
		    			Map<String, String> gip = pvnd.getInitParams();
	    				for (String param : gpv.keySet()) {
	    					boolean hasInit = gip.containsValue(param);
	    					String formulaID = "";
	    					if (hasInit) {
	    						for (String fid : gip.keySet()) {
	    							if (gip.get(fid).equals(param)) {
	    								formulaID = fid;
	    								break;
	    							}
	    						}
	    					}
			    			mtm.addRegister(id, emx.getID(), emx.getName(), param, gpv.get(param), hasInit, formulaID);
	    				}
		    		}
		    	}
			}
	    	catch (SQLException e) {
				e.printStackTrace();
			}
			/*
			ResultSet rs = null;
			if (modelReaderUi.isVisible()) {
				rs = db.selectEstModel(modelReaderUi.getLevel(), -1, where, false);
			}
			else {
				rs = db.selectEstModel(1, -1, where);
			}
			dbTable.refresh(rs);
			final JTable table = dbTable.getTable(); 
    		for (int i=0;i<table.getColumnCount();i++) {
    			Column c = dbTable.getColumn(i);
    			String cn = c.getColumnName(); 
    			if (cn.equals("GeschaetztesModell2") || cn.equals("GeschaetztesModell") || cn.equals("Temperatur") || cn.equals("pH") || cn.equals("aw")
    					 || cn.equals("Agensname") || cn.equals("AgensDetail") || cn.equals("Matrixname")
    					 || cn.equals("MatrixDetail") || cn.equals("Kommentar") || cn.equals("MDGeprueft") || cn.equals("MDGuetescore")
    					 || cn.equals("Parameter") || cn.equals("Parametername")  || cn.equals("Parametername2") || cn.equals("Wert") || cn.equals("Wert2")
    					 || cn.equals("ZeitEinheit") || cn.equals("ZeitEinheit") || cn.equals("Rsquared") || cn.equals("Rsquared2")
    					 || cn.equals("Geprueft") || cn.equals("Geprueft2") || cn.equals("Guetescore") || cn.equals("Guetescore2")) {
    				c.setVisible(true);
    			}
    			else c.setVisible(false);
    		}
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			    public void valueChanged(ListSelectionEvent e) {
			    	if (!e.getValueIsAdjusting()) {
			    		chosenModel = null; chosenModel2 = null;
			    		java.util.List<Integer> c1 = new ArrayList<Integer>();
			    		java.util.List<Integer> c2 = new ArrayList<Integer>();
			    		int[] selRows = table.getSelectedRows();
			    		if (dbTable.getRowCount() > 0) {
			    			for (int ii=0;ii<selRows.length;ii++) {
			    				if (selRows[ii] >= 0) {
						    		for (int i=0;i<table.getColumnCount();i++) {
						    			if (dbTable.getColumn(i).getColumnName().equals("GeschaetztesModell")) {
						    				Object o = dbTable.getValueAt(selRows[ii], i);
						    				if (o != null && o instanceof Integer) {
										        c1.add((Integer) o);
						    				}
									        if (getLevel() != 2) break;
						    			}
						    			if (dbTable.getColumn(i).getColumnName().equals("GeschaetztesModell2")) {
						    				Object o = dbTable.getValueAt(selRows[ii], i);
						    				if (o != null && o instanceof Integer) {
										        c2.add((Integer) o);
						    				}
									        break;
						    			}
						    		}
			    				}
			    			}
			    		}
			    		chosenModel = new int[c1.size()];
			    		chosenModel2 = new int[c2.size()];
			    		int i=0; for (int c : c1) {chosenModel[i] = c;i++;}
			    		i=0; for (int c : c2) {chosenModel2[i] = c;i++;}
			    	}
			    }
			});
			*/
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private void centerOnScreen(final Component c, final boolean absolute) {
	    final int width = c.getWidth();
	    final int height = c.getHeight();
	    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (screenSize.width / 2) - (width / 2);
	    int y = (screenSize.height / 2) - (height / 2);
	    if (!absolute) {
	        x /= 2;
	        y /= 2;
	    }
	    c.setLocation(x, y);
	}
	private void qualityButtonActionPerformed(ActionEvent e) {
		if (qualityButtonNone.isSelected()) qualityField.setEnabled(false);
		else qualityField.setEnabled(true);
	}

	private void doFilterActionPerformed(ActionEvent e) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			getDataTable(db);
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	public void addModelPrim(final int id, final String name, final String modelType) throws PmmException {
		modelReaderUi.addModelPrim(id, name, modelType);
	}
	
	public void addModelSec(final int id, final String name, final String modelType) throws PmmException {
		modelReaderUi.addModelSec(id, name, modelType);
	}
	
	public void setMatrixString( final String str ) throws InvalidSettingsException {
		mdReaderUi.setMatrixString(str);
	}
	public void setAgentString( final String str ) throws InvalidSettingsException {
		mdReaderUi.setAgentString(str);
	}
	public void setLiteratureString( final String str ) throws InvalidSettingsException {
		mdReaderUi.setLiteratureString(str);
	}
	public void setParameter(LinkedHashMap<String, DoubleTextField[]> params) {
		mdReaderUi.setParameter(params);
	}
	public void clearModelSet() { modelReaderUi.clearModelSet(); }
	public void enableModelList( String idList ) { modelReaderUi.enableModelList( idList ); }
	public String getAgentString() { return mdReaderUi.getAgentString(); }
	public String getLiteratureString() { return mdReaderUi.getLiteratureString(); }
	public LinkedHashMap<String, DoubleTextField[]> getParameter() { return mdReaderUi.getParameter(); }
	public int getLevel() { return modelReaderUi.getLevel(); }
	public String getModelClass() { return modelReaderUi.getModelClass(); }
	public String getMatrixString() { return mdReaderUi.getMatrixString(); }
	public String getModelList() { return modelReaderUi.getModelList(); }
	
	public void setMiscItems(String[] itemListMisc) {
		mdReaderUi.setMiscItems(itemListMisc);
	}
	public double getQualityThresh() throws InvalidSettingsException {
		
		if( !qualityField.isValueValid() )
			throw new InvalidSettingsException( "Threshold quality invalid." );
		
		return qualityField.getValue();
	}
	
	public int getQualityMode() {
		
		if( qualityButtonNone.isSelected() )
			return MODE_OFF;
		
		if( qualityButtonRms.isSelected() )
			return MODE_RMS;
		
		return MODE_R2;
	}
	
	public boolean isModelFilterEnabled() { return modelReaderUi.isModelFilterEnabled(); }
	
	public void setLevel( int level ) throws PmmException { modelReaderUi.setLevel( level ); }
	public void setModelClass( String modelClass ) throws PmmException { modelReaderUi.setModelClass( modelClass ); }
	public void setModelFilterEnabled( boolean en ) { modelReaderUi.setModelFilterEnabled( en ); }
	
	public void setQualityMode( final int mode ) throws PmmException {
		
		
		switch( mode ) {
		
			case MODE_OFF :
				qualityButtonNone.setSelected( true );
				qualityField.setEnabled( false );
				break;
				
			case MODE_R2 :
				qualityButtonR2.setSelected( true );
				qualityField.setEnabled( true );
				break;
				
			case MODE_RMS :
				qualityButtonRms.setSelected( true );
				qualityField.setEnabled( true );
				break;
		
			default :
				throw new PmmException( "Invalid quality filter mode." );
		}
	}
	
	public void setQualityThresh( final double thresh ) {
		qualityField.setText( String.valueOf( thresh ) );
	}
	
    public static boolean passesFilter(
    		final int level,
    		final int qualityMode,
    		final double qualityThresh,
    		final String matrixString,
    		final String agentString,
    		final String literatureString,
    		int matrixID, int agentID, int literatureID,
    		final LinkedHashMap<String, Double[]> parameter,
    		boolean modelFilterEnabled,
    		final String modelList,
    		final KnimeTuple tuple )
    throws PmmException {

    	if( level == 1 )
    		if( !MdReaderUi.passesFilter( matrixString,
				agentString, literatureString, matrixID, agentID, literatureID, parameter, tuple ) )
    			return false;
    	
    	if (modelFilterEnabled && !ModelReaderUi.passesFilter(modelList, tuple ) )
    		return false;
    		
    		
        	
		PmmXmlDoc x = tuple.getPmmXml(Model1Schema.getAttribute(Model1Schema.ATT_ESTMODEL, level));
		EstModelXml emx = null;
		if (x != null) {
			for (PmmXmlElementConvertable el : x.getElementSet()) {
				if (el instanceof EstModelXml) {
					emx = (EstModelXml) el;
					break;
				}
			}
		}

		switch( qualityMode ) {
    	
    		case MODE_OFF :
    			return true;
    			
    		case MODE_RMS :    			
    			if (emx != null && emx.getRMS() <= qualityThresh) return true;    			
    			else return false;
    			
    		case MODE_R2 :    			
    			if (emx != null && emx.getR2() != null && emx.getR2() >= qualityThresh) return true;    			
    			else return false;
    			
    		default :
    			throw new PmmException( "Unrecognized Quality Filter mode." );
    	}
    }
    
    public void saveSettingsTo(Config c) {
    	if (set != null) set.saveSettings(c.addConfig("PredictorSettings"));
    	
     	modelReaderUi.saveSettingsTo(c.addConfig("ModelReaderUi"));
     	mdReaderUi.saveSettingsTo(c.addConfig("MdReaderUi"));
    	
    	c.addInt( EmReaderUi.PARAM_QUALITYMODE, this.getQualityMode() );
    	c.addDouble( EmReaderUi.PARAM_QUALITYTHRESH, qualityField.getValue());
    	
    	c.addBoolean(PARAM_NOMDDATA, withoutData.isSelected());
    	
    	MyTableModel mtm = (MyTableModel) filterResults.getModel();
    	c.addStringArray(PARAM_TABLECOLInternalID, mtm.getColumnData(0));
    	c.addStringArray(PARAM_TABLECOLModelID, mtm.getColumnData(1));
    	c.addStringArray(PARAM_TABLECOLModelName, mtm.getColumnData(2));
    	c.addStringArray(PARAM_TABLECOLInitParam, mtm.getColumnData(3));
    	c.addStringArray(PARAM_TABLECOLInitParamValue, mtm.getColumnData(4));
    	c.addStringArray(PARAM_TABLECOLIsInitParam, mtm.getColumnData(5));
    	c.addStringArray(PARAM_TABLECOLFormulaID, mtm.getColumnData(6));
    	    	
		Config c2 = c.addConfig(EstimatedModelReaderNodeModel.PARAM_PARAMETERS);
    	LinkedHashMap<String, DoubleTextField[]> params = this.getParameter();
    	if (params != null && params.size() > 0) {
    		String[] pars = new String[params.size()];
    		String[] mins = new String[params.size()];
    		String[] maxs = new String[params.size()];
    		int i=0;
    		for (String par : params.keySet()) {
    			DoubleTextField[] dbl = params.get(par);
    			pars[i] = par;
    			mins[i] = ""+dbl[0].getValue();
    			maxs[i] = ""+dbl[1].getValue();
    			i++;
    		}
    		c2.addStringArray(EstimatedModelReaderNodeModel.PARAM_PARAMETERNAME, pars);
    		c2.addStringArray(EstimatedModelReaderNodeModel.PARAM_PARAMETERMIN, mins);
    		c2.addStringArray(EstimatedModelReaderNodeModel.PARAM_PARAMETERMAX, maxs);
    	}
    }	
	public void setSettings(Config c) throws InvalidSettingsException {		
		setSettings(c, null, null, null, null, null);
	}
	public void setSettings(Config c, Integer defAgent, Integer defMatrix, Double defTemp, Double defPh, Double defAw) throws InvalidSettingsException {		
		if (c.containsKey("PredictorSettings")) {
			set = new SettingsHelper();
			set.loadSettings(c.getConfig("PredictorSettings"));
		}

    	LinkedHashMap<String, DoubleTextField[]> params = new LinkedHashMap<String, DoubleTextField[]>();
		modelReaderUi.setLevel(1);
     	if (c.containsKey("ModelReaderUi")) modelReaderUi.setSettings(c.getConfig("ModelReaderUi"));
     	if (c.containsKey("MdReaderUi")) mdReaderUi.setSettings(c.getConfig("MdReaderUi"));

    		this.setQualityMode( c.getInt( EmReaderUi.PARAM_QUALITYMODE ) );
    		this.setQualityThresh( c.getDouble( EmReaderUi.PARAM_QUALITYTHRESH ) );

    		if (c.containsKey(PARAM_NOMDDATA)) withoutData.setSelected(c.getBoolean(PARAM_NOMDDATA));
    		
    		if (c.containsKey(PARAM_TABLECOLInternalID)) {
    			String[] c1 = c.getStringArray(PARAM_TABLECOLInternalID);
    			if (c1 != null) {
        			String[] c2 = c.getStringArray(PARAM_TABLECOLModelID);
        			String[] c3 = c.getStringArray(PARAM_TABLECOLModelName);
        			String[] c4 = c.getStringArray(PARAM_TABLECOLInitParam);
        			String[] c5 = c.getStringArray(PARAM_TABLECOLInitParamValue);
        			String[] c6 = c.getStringArray(PARAM_TABLECOLIsInitParam);
        			String[] c7 = c.getStringArray(PARAM_TABLECOLFormulaID);
                	MyTableModel mtm = (MyTableModel) filterResults.getModel();
        			mtm.removeAll();
            		for (int i=0;i<c1.length;i++) {
        	    		mtm.addRegister(c1[i], Integer.parseInt(c2[i]), c3[i], c4[i], Double.parseDouble(c5[i]), Boolean.parseBoolean(c6[i]), c7[i]);
            		}
    			}
    		}
    				
    		Config c2 = c.getConfig(EmReaderUi.PARAM_PARAMETERS);
    		if (c2.containsKey(EmReaderUi.PARAM_PARAMETERNAME)) {
        		String[] pars = c2.getStringArray(EmReaderUi.PARAM_PARAMETERNAME);
        		String[] mins = c2.getStringArray(EmReaderUi.PARAM_PARAMETERMIN);
        		String[] maxs = c2.getStringArray(EmReaderUi.PARAM_PARAMETERMAX);

        		for (int i=0;i<pars.length;i++) {
        			DoubleTextField[] dbl = new DoubleTextField[2];
        			dbl[0] = new DoubleTextField(true);
        			dbl[1] = new DoubleTextField(true);
        			if (!mins[i].equals("null")) dbl[0].setValue(Double.parseDouble(mins[i]));
        			if (!maxs[i].equals("null")) dbl[1].setValue(Double.parseDouble(maxs[i]));
        			params.put(pars[i], dbl);
        		}
    		}
    		if (params.size() == 0) fillWithDefaults(c, defAgent, defMatrix, defTemp, defPh, defAw, params);     		
    		this.setParameter(params);     		
	}
	private void fillWithDefaults(Config c, Integer defAgent, Integer defMatrix, Double defTemp, Double defPh, Double defAw, LinkedHashMap<String, DoubleTextField[]> params) throws InvalidSettingsException {
		if (defAgent != null) {
			//c.getConfig("MdReaderUi").addInt(MdReaderUi.PARAM_AGENTID, defAgent);
			mdReaderUi.setAgensID(defAgent);
			mdReaderUi.setAgentString(""+DBKernel.getValue(db.getConnection(), "Agenzien", "ID", defAgent+"", "Agensname"));
		}
		if (defMatrix != null) {
			//c.getConfig("MdReaderUi").addInt(MdReaderUi.PARAM_MATRIXID, defMatrix);
			mdReaderUi.setMatrixID(defMatrix);
			mdReaderUi.setMatrixString(""+DBKernel.getValue(db.getConnection(), "Matrices", "ID", defMatrix+"", "Matrixname"));
		}
		if (defTemp != null) {
			DoubleTextField[] dtf = params.get(AttributeUtilities.ATT_TEMPERATURE);
			if (dtf == null) {
				dtf = new DoubleTextField[2];
				dtf[0] = new DoubleTextField(true); dtf[1] = new DoubleTextField(true);
				params.put(AttributeUtilities.ATT_TEMPERATURE, dtf);
			}
			dtf[0].setValue(defTemp - 10);
			dtf[1].setValue(defTemp + 10);
		}
		if (defPh != null) {
			DoubleTextField[] dtf = params.get(AttributeUtilities.ATT_PH);
			if (dtf == null) {
				dtf = new DoubleTextField[2];
				dtf[0] = new DoubleTextField(true); dtf[1] = new DoubleTextField(true);
				params.put(AttributeUtilities.ATT_PH, dtf);
			}
			dtf[0].setValue(defPh - 1);
			dtf[1].setValue(defPh + 1);
		}
		if (defAw != null) {
			DoubleTextField[] dtf = params.get(AttributeUtilities.ATT_AW);
			if (dtf == null) {
				dtf = new DoubleTextField[2];
				dtf[0] = new DoubleTextField(true); dtf[1] = new DoubleTextField(true);
				params.put(AttributeUtilities.ATT_AW, dtf);
			}
			dtf[0].setValue(defAw - 0.1);
			dtf[1].setValue(defAw + 0.1);
		}
	}

	class MyTableModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6358436149095581889L;
		private String[] columns = new String[]{"InternalID", "ModelID", "ModelName", "InitParam", "InitParamValue","isInitParam","FormulaID"};
		private Class<?>[] columnTypes = new Class<?>[] {String.class, Integer.class, String.class, String.class, Double.class, Boolean.class, String.class};
		private ArrayList<Register> list = new ArrayList<Register>();

	    @Override
	    public int getColumnCount() {
	        return columns.length;
	    }

	    @Override
	    public int getRowCount() {
	        return list.size();
	    }
	    
	    public String[] getColumnData(int columnIndex) {
	    	if (columnIndex >= getColumnCount()) return null;
	        int nRow = getRowCount();
	        String[] tableData = new String[nRow];
	        for (int i = 0 ; i < nRow ; i++) {
	        	tableData[i] = getValueAt(i, columnIndex).toString();
	        }
	        return tableData;
	    }

		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return false;
		}

        public Class<?> getColumnClass(int columnIndex) {
        	return columnTypes[columnIndex];
        }

        public String getColumnName(int columnIndex) {
        	return columns[columnIndex];
        }

        @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	        Register r = list.get(rowIndex);
	        switch (columnIndex) {
	        	case 0: return r.InternalID; 
	        	case 1: return r.ModelID; 
		        case 2: return r.ModelName;
		        case 3: return r.InitParam; 
		        case 4: return r.InitParamValue;
		        case 5: return r.isInitParam;
		        case 6: return r.FormulaID;
	        }
	            return null;
	    }

	    public void addRegister(String InternalID, Integer ModelID, String ModelName, String InitParam, Double InitParamValue, Boolean isInitParam, String FormulaID) {
	        list.add(new Register(InternalID, ModelID, ModelName, InitParam, InitParamValue, isInitParam, FormulaID));
	        this.fireTableDataChanged();
	    }
	    public void removeAll() {
	    	list.clear();
	    	this.fireTableDataChanged();
	    }

	    class Register{
	    	String InternalID;
	        Integer ModelID;
	        String ModelName;
	        String InitParam;
	        Double InitParamValue;
	        Boolean isInitParam;
	        String FormulaID;

	        public Register(String InternalID, Integer ModelID, String ModelName, String InitParam, Double InitParamValue, Boolean isInitParam, String FormulaID) {
	            this.InternalID = InternalID;
	            this.ModelID = ModelID;
	            this.ModelName = ModelName;
	            this.InitParam = InitParam;
	            this.InitParamValue = InitParamValue;
	            this.isInitParam = isInitParam;
	            this.FormulaID = FormulaID;
	        }
	    }

	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		withoutData = new JCheckBox();
		modelReaderUi = new ModelReaderUi(true);
		qualityPanel = new JPanel();
		qualityButtonNone = new JRadioButton();
		qualityButtonRms = new JRadioButton();
		qualityButtonR2 = new JRadioButton();
		label3 = new JLabel();
		qualityField = new DoubleTextField();
		mdReaderUi = new MdReaderUi();
		panel6 = new JPanel();
		doFilter = new JButton();
		scrollPane1 = new JScrollPane();
		filterResults = new JTable();

		//======== this ========
		setLayout(new FormLayout(
			"default:grow",
			"default, 3*($lgap, fill:default), $lgap, fill:default:grow"));

		//---- withoutData ----
		withoutData.setText("Load models without associated microbial data");
		add(withoutData, CC.xy(1, 1));
		add(modelReaderUi, CC.xy(1, 3));

		//======== qualityPanel ========
		{
			qualityPanel.setBorder(new TitledBorder("Estimation Quality"));
			qualityPanel.setLayout(new FormLayout(
				"4*(default, $lcgap), default:grow",
				"default"));

			//---- qualityButtonNone ----
			qualityButtonNone.setText("Do not filter");
			qualityButtonNone.setSelected(true);
			qualityButtonNone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					qualityButtonActionPerformed(e);
				}
			});
			qualityPanel.add(qualityButtonNone, CC.xy(1, 1));

			//---- qualityButtonRms ----
			qualityButtonRms.setText("Filter by RMS");
			qualityButtonRms.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					qualityButtonActionPerformed(e);
				}
			});
			qualityPanel.add(qualityButtonRms, CC.xy(3, 1));

			//---- qualityButtonR2 ----
			qualityButtonR2.setText("Filter by R squared");
			qualityButtonR2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					qualityButtonActionPerformed(e);
				}
			});
			qualityPanel.add(qualityButtonR2, CC.xy(5, 1));

			//---- label3 ----
			label3.setText("Quality threshold:");
			qualityPanel.add(label3, CC.xy(7, 1));

			//---- qualityField ----
			qualityField.setValue(0.8);
			qualityField.setEnabled(false);
			qualityPanel.add(qualityField, CC.xy(9, 1));
		}
		add(qualityPanel, CC.xy(1, 5));
		add(mdReaderUi, CC.xy(1, 7));

		//======== panel6 ========
		{
			panel6.setBorder(new TitledBorder("Results"));
			panel6.setLayout(new FormLayout(
				"default:grow",
				"default, $lgap, default"));

			//---- doFilter ----
			doFilter.setText("ApplyAndShowFilterResults");
			doFilter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doFilterActionPerformed(e);
				}
			});
			panel6.add(doFilter, CC.xy(1, 1));

			//======== scrollPane1 ========
			{

				//---- filterResults ----
				filterResults.setFillsViewportHeight(true);
				filterResults.setModel(new MyTableModel());
				scrollPane1.setViewportView(filterResults);
			}
			panel6.add(scrollPane1, CC.xy(1, 3));
		}
		add(panel6, CC.xy(1, 9));

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(qualityButtonNone);
		buttonGroup1.add(qualityButtonRms);
		buttonGroup1.add(qualityButtonR2);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JCheckBox withoutData;
	private ModelReaderUi modelReaderUi;
	private JPanel qualityPanel;
	private JRadioButton qualityButtonNone;
	private JRadioButton qualityButtonRms;
	private JRadioButton qualityButtonR2;
	private JLabel label3;
	private DoubleTextField qualityField;
	private MdReaderUi mdReaderUi;
	private JPanel panel6;
	private JButton doFilter;
	private JScrollPane scrollPane1;
	private JTable filterResults;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
