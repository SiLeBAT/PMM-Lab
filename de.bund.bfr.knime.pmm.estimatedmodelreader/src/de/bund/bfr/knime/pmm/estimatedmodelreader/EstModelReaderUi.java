/*******************************************************************************
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * J�rgen Brandt (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Christian Th�ns (BfR)
 * Annemarie K�sbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.bund.bfr.knime.pmm.estimatedmodelreader;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyDBTables;
import org.hsh.bfr.db.MyTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.Config;

import quick.dbtable.DBTable;

import de.bund.bfr.knime.pmm.bfrdbiface.lib.Bfrdb;
import de.bund.bfr.knime.pmm.common.EstModelXml;
import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.ui.DoubleTextField;
import de.bund.bfr.knime.pmm.common.ui.ModelReaderUi;
import de.bund.bfr.knime.pmm.timeseriesreader.MdReaderUi;

public class EstModelReaderUi extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 20120828;

	public static final String PARAM_PARAMETERS = "parameters";
	public static final String PARAM_PARAMETERNAME = "parameterName";
	public static final String PARAM_PARAMETERMIN = "parameterMin";
	public static final String PARAM_PARAMETERMAX = "parameterMax";

	public static final String PARAM_QUALITYMODE = "qualityFilterMode";
	public static final String PARAM_QUALITYTHRESH = "qualityThreshold";

	public static final String PARAM_CHOSENMODEL = "chosenModel";

	private JRadioButton qualityButtonNone;
	private JRadioButton qualityButtonRms;
	private JRadioButton qualityButtonR2;
	private DoubleTextField qualityField;
	private MdReaderUi tsReaderUi;
	private ModelReaderUi modelReaderUi;
	private Integer chosenModel = 0;
	private JButton doFilter;
	
	private boolean showDbTable;
	private Bfrdb db;
	private JPanel southSouthPanel;
	
	public static final int MODE_OFF = 0;
	public static final int MODE_R2 = 1;
	public static final int MODE_RMS = 2;
	
	public EstModelReaderUi(Bfrdb db) {
		this(db,null);
	}
	
	public EstModelReaderUi(Bfrdb db, String[] itemListMisc) {								
		this(db,itemListMisc, true, true, true, false);
	}
	public EstModelReaderUi(Bfrdb db, String[] itemListMisc,
			boolean showModelOptions, boolean showQualityOptions, boolean showMDOptions, boolean showDbTable) {		
		this.showDbTable = showDbTable;
		this.db = db;
		modelReaderUi = new ModelReaderUi();
		modelReaderUi.addLevelListener( this );
		qualityButtonNone = new JRadioButton( "Do not filter" );
		qualityButtonNone.setSelected( true );
		qualityButtonNone.addActionListener( this );
		qualityButtonRms = new JRadioButton( "Filter by RMS" );
		qualityButtonRms.addActionListener( this );
		qualityButtonR2 = new JRadioButton( "Filter by R squared" );
		qualityButtonR2.addActionListener( this );
		qualityField = new DoubleTextField( false );
		qualityField.setText( "0.8" );
		qualityField.setEnabled( false );
		tsReaderUi = new MdReaderUi(db.getConnection(),itemListMisc);
						
		JPanel buttonPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();	

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPanel.add(qualityButtonNone);
		buttonPanel.add(Box.createHorizontalGlue());		
		buttonPanel.add(qualityButtonRms);		
		buttonPanel.add(Box.createHorizontalGlue());		
		buttonPanel.add(qualityButtonR2);
		
		group.add(qualityButtonNone);		
		group.add(qualityButtonRms);
		group.add(qualityButtonR2);		
		
		JPanel panel = new JPanel();		
		panel.setBorder( BorderFactory.createTitledBorder( "Estimation quality" ) );
		panel.setLayout( new BorderLayout() );
		panel.setPreferredSize(new Dimension(550, 75));
		panel.add( buttonPanel, BorderLayout.NORTH );
		panel.add( new JLabel( "Quality threshold   " ), BorderLayout.WEST );
		panel.add( qualityField, BorderLayout.CENTER );
		
		JPanel southPanel = new JPanel();
		
		southPanel.setLayout(new BorderLayout());
		if (showQualityOptions) southPanel.add(panel, BorderLayout.NORTH);
		if (showMDOptions) southPanel.add(tsReaderUi, BorderLayout.CENTER);
		
		southSouthPanel = new JPanel();
		southSouthPanel.setLayout(new BorderLayout());
		doFilter = new JButton("ApplyAndShowFilterResults");
		doFilter.addActionListener(this);
		southSouthPanel.add(doFilter, BorderLayout.NORTH);
		
		southPanel.add(southSouthPanel, BorderLayout.SOUTH);
		
		setPreferredSize(new Dimension(550, showModelOptions ? 500 : 300));
		setLayout(new BorderLayout());
		if (showModelOptions) {
			add(modelReaderUi, BorderLayout.CENTER);
			add(southPanel, BorderLayout.SOUTH);		
		}
		else {
			add(southPanel, BorderLayout.CENTER);					
		}
		
		updateTsReaderUi();
	}
	
	private DBTable getDataTable(Bfrdb db) {
		final DBTable dbTable = new DBTable();
		try {
			String sql = " TRUE " +
					(tsReaderUi.getAgentID() > 0 ? " AND \"Agens\" = " + tsReaderUi.getAgentID() : "") +
					(tsReaderUi.getMatrixID() > 0 ? " AND \"Matrix\" = " + tsReaderUi.getMatrixID() : "");
			LinkedHashMap<String, DoubleTextField[]> params = tsReaderUi.getParameter();
			for (String key : params.keySet()) {
				DoubleTextField[] dtf = params.get(key);
				if (key.equals(AttributeUtilities.ATT_TEMPERATURE)) {
					sql +=
							(dtf[0].getValue() != null ? " AND \"Temperatur\" >= " + dtf[0].getValue() : "") +
							(dtf[1].getValue() != null ? " AND \"Temperatur\" <= " + dtf[1].getValue() : "");
				}
				else if (key.equals(AttributeUtilities.ATT_PH)) {
					sql +=
							(dtf[0].getValue() != null ? " AND \"pH\" >= " + dtf[0].getValue() : "") +
							(dtf[1].getValue() != null ? " AND \"pH\" <= " + dtf[1].getValue() : "");
				}
				else if (key.equals(AttributeUtilities.ATT_WATERACTIVITY)) {
					sql +=
							(dtf[0].getValue() != null ? " AND \"aw\" >= " + dtf[0].getValue() : "") +
							(dtf[1].getValue() != null ? " AND \"aw\" <= " + dtf[1].getValue() : "");
				}
			}
			ResultSet rs = db.selectEstModel(1, sql, "", false);
			dbTable.refresh(rs);
			final JTable table = dbTable.getTable(); 
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			    public void valueChanged(ListSelectionEvent e) {
			    	if (!e.getValueIsAdjusting()) {
			    		int selRow = table.getSelectedRow();
			    		if (selRow >= 0) {
				    		for (int i=0;i<table.getColumnCount();i++) {
				    			if (dbTable.getColumn(i).getColumnName().equals("GeschaetztesModell")) {
				    				Object o = dbTable.getValueAt(table.getSelectedRow(), 31);
				    				if (o != null && o instanceof Integer) {
								        chosenModel = (Integer) o;
				    				}
							        break;
				    			}
				    		}
			    		}
			    	}
			    }
			});		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return dbTable;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object src = arg0.getSource();
		if (src instanceof JRadioButton) {			
			if (qualityButtonNone.isSelected()) qualityField.setEnabled(false);
			else qualityField.setEnabled(true);
		}		
		else if (src instanceof JComboBox) {			
			updateTsReaderUi();			
		}		
		else if (src instanceof JButton) { // doFilter
			if (showDbTable) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				southSouthPanel.setVisible(false);
				southSouthPanel.removeAll();
				southSouthPanel.add(doFilter, BorderLayout.NORTH);
				southSouthPanel.add(getDataTable(db), BorderLayout.CENTER);
				southSouthPanel.setPreferredSize(new Dimension(550, 150));
				southSouthPanel.setVisible(true);
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			else {
				MyTable gm = MyDBTables.getTable("GeschaetzteModelle");
				// MyIDFilter mf = new MyIDFilter(filterIDs);
				Object newGmId = DBKernel.myList.openNewWindow(
						gm,
						chosenModel > 0 ? chosenModel : null,
						(Object) "GeschaetzteModelle",
						null,
						1,
						1,
						null,
						true, null, this);
				if (newGmId != null && newGmId instanceof Integer) {
					chosenModel = (Integer) newGmId;
					doFilter.setText("ApplyAndShowFilterResults [" + chosenModel + "]");
				}
				else {
					chosenModel = 0;
					doFilter.setText("ApplyAndShowFilterResults");
				}
			}
		}		
	}
	
	public void addModelPrim(final int id, final String name, final String modelType) throws PmmException {
		modelReaderUi.addModelPrim(id, name, modelType);
	}
	
	public void addModelSec(final int id, final String name, final String modelType) throws PmmException {
		modelReaderUi.addModelSec(id, name, modelType);
	}
	
	public void setMatrixString( final String str ) throws InvalidSettingsException {
		tsReaderUi.setMatrixString(str);
	}
	public void setAgentString( final String str ) throws InvalidSettingsException {
		tsReaderUi.setAgentString(str);
	}
	public void setLiteratureString( final String str ) throws InvalidSettingsException {
		tsReaderUi.setLiteratureString(str);
	}
	public void setParameter(LinkedHashMap<String, DoubleTextField[]> params) {
		tsReaderUi.setParameter(params);
	}
	public void clearModelSet() { modelReaderUi.clearModelSet(); }
	public void enableModelList( String idList ) { modelReaderUi.enableModelList( idList ); }
	public String getAgentString() { return tsReaderUi.getAgentString(); }
	public String getLiteratureString() { return tsReaderUi.getLiteratureString(); }
	public LinkedHashMap<String, DoubleTextField[]> getParameter() { return tsReaderUi.getParameter(); }
	public int getLevel() { return modelReaderUi.getLevel(); }
	public String getModelClass() { return modelReaderUi.getModelClass(); }
	public String getMatrixString() { return tsReaderUi.getMatrixString(); }
	public String getModelList() { return modelReaderUi.getModelList(); }
	
	public void setMiscItems(String[] itemListMisc) {
		tsReaderUi.setMiscItems(itemListMisc);
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
    
    private void updateTsReaderUi() {
    	/*
    	if( modelReaderUi.getLevel() == 1 )
    		tsReaderUi.setActive();
    	else
    		tsReaderUi.setInactive();
    		*/
    }
    
    public void saveSettingsTo(Config c) {
     	modelReaderUi.saveSettingsTo(c.addConfig("ModelReaderUi"));
     	tsReaderUi.saveSettingsTo(c.addConfig("MdReaderUi"));
    	
    	c.addInt( EstModelReaderUi.PARAM_QUALITYMODE, this.getQualityMode() );
    	c.addDouble( EstModelReaderUi.PARAM_QUALITYTHRESH, qualityField.getValue());
    	
    	c.addInt(PARAM_CHOSENMODEL, chosenModel);

    	LinkedHashMap<String, DoubleTextField[]> params = this.getParameter();
		Config c2 = c.addConfig(EstimatedModelReaderNodeModel.PARAM_PARAMETERS);
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
	public void setSettings(Config c) throws InvalidSettingsException {		
		setSettings(c, null, null, null, null, null);
	}
	public void setSettings(Config c, Integer defAgent, Integer defMatrix, Double defTemp, Double defPh, Double defAw) throws InvalidSettingsException {		
     	modelReaderUi.setSettings(c.getConfig("ModelReaderUi"));
     	tsReaderUi.setSettings(c.getConfig("MdReaderUi"));

		this.setQualityMode( c.getInt( EstModelReaderUi.PARAM_QUALITYMODE ) );
		this.setQualityThresh( c.getDouble( EstModelReaderUi.PARAM_QUALITYTHRESH ) );

		chosenModel = c.getInt(PARAM_CHOSENMODEL);
		doFilter.setText("ApplyAndShowFilterResults" + (chosenModel > 0 ? "[" + chosenModel + "]" : ""));
				
		Config c2 = c.getConfig(EstModelReaderUi.PARAM_PARAMETERS);
		String[] pars = c2.getStringArray(EstModelReaderUi.PARAM_PARAMETERNAME);
		String[] mins = c2.getStringArray(EstModelReaderUi.PARAM_PARAMETERMIN);
		String[] maxs = c2.getStringArray(EstModelReaderUi.PARAM_PARAMETERMAX);

		LinkedHashMap<String, DoubleTextField[]> params = new LinkedHashMap<String, DoubleTextField[]>();
		for (int i=0;i<pars.length;i++) {
			DoubleTextField[] dbl = new DoubleTextField[2];
			dbl[0] = new DoubleTextField(true);
			dbl[1] = new DoubleTextField(true);
			if (!mins[i].equals("null")) dbl[0].setValue(Double.parseDouble(mins[i]));
			if (!maxs[i].equals("null")) dbl[1].setValue(Double.parseDouble(maxs[i]));
			params.put(pars[i], dbl);
		}
		this.setParameter(params);
		
		fillWithDefaults(c, defAgent, defMatrix, defTemp, defPh, defAw, params);
	}
	private void fillWithDefaults(Config c, Integer defAgent, Integer defMatrix, Double defTemp, Double defPh, Double defAw, LinkedHashMap<String, DoubleTextField[]> params) throws InvalidSettingsException {
		if (defAgent != null) {
			//c.getConfig("MdReaderUi").addInt(MdReaderUi.PARAM_AGENTID, defAgent);
			tsReaderUi.setAgensID(defAgent);
			tsReaderUi.setAgentString(""+DBKernel.getValue(db.getConnection(), "Agenzien", "ID", defAgent+"", "Agensname"));
		}
		if (defMatrix != null) {
			//c.getConfig("MdReaderUi").addInt(MdReaderUi.PARAM_MATRIXID, defMatrix);
			tsReaderUi.setMatrixID(defMatrix);
			tsReaderUi.setMatrixString(""+DBKernel.getValue(db.getConnection(), "Matrices", "ID", defMatrix+"", "Matrixname"));
		}
		if (defTemp != null) {
			DoubleTextField[] dtf = params.get(AttributeUtilities.ATT_TEMPERATURE);
			dtf[0].setValue(defTemp - 10);
			dtf[1].setValue(defTemp + 10);
		}
		if (defPh != null) {
			DoubleTextField[] dtf = params.get(AttributeUtilities.ATT_PH);
			dtf[0].setValue(defPh - 1);
			dtf[1].setValue(defPh + 1);
		}
		if (defAw != null) {
			DoubleTextField[] dtf = params.get(AttributeUtilities.ATT_WATERACTIVITY);
			dtf[0].setValue(defAw - 0.1);
			dtf[1].setValue(defAw + 0.1);
		}
	}
}