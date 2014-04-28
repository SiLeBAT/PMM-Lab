package de.bund.bfr.knime.pmm.dbdelete;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmm.bfrdbiface.lib.Bfrdb;
import de.bund.bfr.knime.pmm.common.CellIO;
import de.bund.bfr.knime.pmm.common.EstModelXml;
import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * This is the model implementation of DatabaseDelete.
 * 
 *
 * @author Armin A. Weiser
 */
public class DatabaseDeleteNodeModel extends NodeModel {
    /*
	static final String PARAM_FILENAME = "filename";
	static final String PARAM_LOGIN = "login";
	static final String PARAM_PASSWD = "passwd";
	static final String PARAM_OVERRIDE = "override";
	*/
	static final String PARAM_DELTESTCOND = "deleteTestConditions";
	static final String PARAM_DELPRIMARYMODELS = "deletePrimaryModels";
	static final String PARAM_DELSECONDARYMODELS = "deleteSecondaryModels";
/*
	private String filename;
	private String login;
	private String passwd;
	private boolean override;
	*/
	private boolean delTS, delPM, delSM;
	private String warnings;

	/**
     * Constructor for the node model.
     */
    protected DatabaseDeleteNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	Bfrdb db = null;
    	/*
    	if( override ) {
			db = new Bfrdb( filename, login, passwd );
		} else {
			db = new Bfrdb(DBKernel.getLocalConn(true));
		}
		*/
    	try {
			db = new Bfrdb(DBKernel.getLocalConn(true));
		} catch (Exception e1) {}
    	String dbuuid = db.getDBUUID();
    	Connection conn = db.getConnection();
    	conn.setReadOnly(false);

		DataTableSpec outSpec = getOutSpec(inData[0].getDataTableSpec());
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		DataCell[] cells = new DataCell[outSpec.getNumColumns()];
		
    	warnings = "";
    	for (int level = 2;level>=0;level--) {
    		boolean conform =
    				level == 0 ? new TimeSeriesSchema().conforms(inData[0].getDataTableSpec()) :
    					(level == 1 ? new Model1Schema().conforms(inData[0].getDataTableSpec()) :
    						new Model2Schema().conforms(inData[0].getDataTableSpec()));
        	for (DataRow row : inData[0]) {
    			for (int ii=0;ii<row.getNumCells();ii++) {
    				cells[outSpec.findColumnIndex(outSpec.getColumnNames()[ii])] = row.getCell(outSpec.findColumnIndex(outSpec.getColumnNames()[ii]));
    			}

    			int numDBSuccesses = 0;
    			numDBSuccesses += getNumDBSuccesses(conform, level, dbuuid, row, outSpec, conn);
    			
    			cells[cells.length-1] = new IntCell(numDBSuccesses);
    			container.addRowToTable(new DefaultRow(row.getKey().toString() + "_" + level, cells));
    		}
    	}
    			    			
    	if (!warnings.isEmpty()) {
			this.setWarningMessage(warnings.trim());
		}			
    	conn.setReadOnly(DBKernel.prefs.getBoolean("PMM_LAB_SETTINGS_DB_RO", true));
    	db.close();

		container.close();
		return new BufferedDataTable[] {container.getTable()};
    }
    private int getNumDBSuccesses(boolean conform, int level, String dbuuid, DataRow row, DataTableSpec outSpec, Connection conn) {
    	int numDBSuccesses = 0;
		if (conform && (delPM && level == 1 || delSM && level == 2)) {
			try {
				if (dbuuid.equals(row.getCell(outSpec.findColumnIndex(level == 1 ? Model1Schema.ATT_DBUUID : Model2Schema.ATT_DBUUID)).toString())) {
					DataCell dc = row.getCell(outSpec.findColumnIndex(level == 1 ? Model1Schema.ATT_ESTMODEL : Model2Schema.ATT_ESTMODEL));
					if (dc instanceof StringValue || dc.isMissing()) {
						PmmXmlDoc estModel = CellIO.getPmmXml(dc);
						if (estModel != null) {
							EstModelXml emx = null;
							for (PmmXmlElementConvertable el : estModel.getElementSet()) {
								if (el instanceof EstModelXml) {
									emx = (EstModelXml) el;
									break;
								}
							}
							if (level == 2 || checkPrimaryDeletion(conn, emx.getId())) numDBSuccesses += deleteFMID(conn, level, emx.getId());
						}
					}
				}					
			}
			catch (PmmException e) {e.printStackTrace();}
		}    	
		if (conform && level == 0 && delTS) {
			DataCell dc = row.getCell(outSpec.findColumnIndex(TimeSeriesSchema.ATT_CONDID));
			if (!dc.isMissing()) {
				Integer tsID = CellIO.getInt(dc);
				if (checkTimeSeriesDeletion(conn, tsID)) numDBSuccesses += deleteTSID(conn, tsID);							
			}
		}
		return numDBSuccesses;
    }
    private boolean checkTimeSeriesDeletion(Connection conn, Integer tsID) {
    	boolean result = true;
    	if (tsID == null) return false;
		String sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("GeschaetzteModelle") +
				" WHERE " + DBKernel.delimitL("Versuchsbedingung") + "=" + tsID;
		ResultSet rs = DBKernel.getResultSet(conn, sql, false);
		try {
			if (rs != null && rs.first()) {
				result = false;
				warnings += "Unable to delete test condition with ID " + tsID + ". Please delete primary models first:\n";
				do {
					Object o = rs.getObject("ID");
					warnings += "ID: " + o + "\n";
				} while (rs.next());
			}
		}
		catch (Exception e) {MyLogger.handleException(e);}   
    	return result;
    }
    private boolean checkPrimaryDeletion(Connection conn, Integer estID) {
    	boolean result = true;
    	if (estID == null) return false;
		String sql = "SELECT " + DBKernel.delimitL("GeschaetztesSekundaermodell") + " FROM " + DBKernel.delimitL("Sekundaermodelle_Primaermodelle") +
				" WHERE " + DBKernel.delimitL("GeschaetztesPrimaermodell") + "=" + estID;
		ResultSet rs = DBKernel.getResultSet(conn, sql, false);
		try {
			if (rs != null && rs.first()) {
				result = false;
				warnings += "Unable to delete primary model with ID " + estID + ". Please delete secondary models first:\n";
				do {
					Object o = rs.getObject("GeschaetztesSekundaermodell");
					warnings += "ID: " + o + "\n";
				} while (rs.next());
			}
		}
		catch (Exception e) {MyLogger.handleException(e);}   
		return result;
    }
    private int deleteFMID(Connection conn, int level, Object rowEstMID) {
    	int numDBSuccesses = 0;
    	Integer firstCLID = DBKernel.getMaxID("ChangeLog");
    	if (firstCLID == null) firstCLID = 0; else firstCLID += 1;
		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("VarParMaps") + " WHERE " + DBKernel.delimitL("GeschaetztesModell") + "=" + rowEstMID, false, false);
		List<Integer> lid = DBKernel.getLastChangeLogEntries("VarParMaps", firstCLID);
		for (int id : lid) DBKernel.sendRequest(conn, "DELETE FROM " + DBKernel.delimitL("DataSource") + " WHERE " + DBKernel.delimitL("Table") + "='VarParMaps' AND " + DBKernel.delimitL("TableID") + "=" + id, false, false);
		
		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("GeschaetztesModell_Referenz") + " WHERE " + DBKernel.delimitL("GeschaetztesModell") + "=" + rowEstMID, false, false);
		lid = DBKernel.getLastChangeLogEntries("GeschaetztesModell_Referenz", firstCLID);
		for (int id : lid) DBKernel.sendRequest(conn, "DELETE FROM " + DBKernel.delimitL("DataSource") + " WHERE " + DBKernel.delimitL("Table") + "='GeschaetztesModell_Referenz' AND " + DBKernel.delimitL("TableID") + "=" + id, false, false);
		
		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("GeschaetzteParameterCovCor") + " WHERE " + DBKernel.delimitL("GeschaetztesModell") + "=" + rowEstMID, false, false);
		lid = DBKernel.getLastChangeLogEntries("GeschaetzteParameterCovCor", firstCLID);
		for (int id : lid) DBKernel.sendRequest(conn, "DELETE FROM " + DBKernel.delimitL("DataSource") + " WHERE " + DBKernel.delimitL("Table") + "='GeschaetzteParameterCovCor' AND " + DBKernel.delimitL("TableID") + "=" + id, false, false);
		
		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("GueltigkeitsBereiche") + " WHERE " + DBKernel.delimitL("GeschaetztesModell") + "=" + rowEstMID, false, false);
		lid = DBKernel.getLastChangeLogEntries("GueltigkeitsBereiche", firstCLID);
		for (int id : lid) DBKernel.sendRequest(conn, "DELETE FROM " + DBKernel.delimitL("DataSource") + " WHERE " + DBKernel.delimitL("Table") + "='GueltigkeitsBereiche' AND " + DBKernel.delimitL("TableID") + "=" + id, false, false);
		
		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("GeschaetzteParameter") + " WHERE " + DBKernel.delimitL("GeschaetztesModell") + "=" + rowEstMID, false, false);
		lid = DBKernel.getLastChangeLogEntries("GeschaetzteParameter", firstCLID);
		for (int id : lid) DBKernel.sendRequest(conn, "DELETE FROM " + DBKernel.delimitL("DataSource") + " WHERE " + DBKernel.delimitL("Table") + "='GeschaetzteParameter' AND " + DBKernel.delimitL("TableID") + "=" + id, false, false);

		if (level == 2) {
			numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("Sekundaermodelle_Primaermodelle") + " WHERE " + DBKernel.delimitL("GeschaetztesSekundaermodell") + "=" + rowEstMID, false, false);
			lid = DBKernel.getLastChangeLogEntries("Sekundaermodelle_Primaermodelle", firstCLID);
			for (int id : lid) DBKernel.sendRequest(conn, "DELETE FROM " + DBKernel.delimitL("DataSource") + " WHERE " + DBKernel.delimitL("Table") + "='Sekundaermodelle_Primaermodelle' AND " + DBKernel.delimitL("TableID") + "=" + id, false, false);
		}

		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("GeschaetzteModelle") + " WHERE " + DBKernel.delimitL("ID") + "=" + rowEstMID, false, false);
		lid = DBKernel.getLastChangeLogEntries("GeschaetzteModelle", firstCLID);
		for (int id : lid) DBKernel.sendRequest(conn, "DELETE FROM " + DBKernel.delimitL("DataSource") + " WHERE " + DBKernel.delimitL("Table") + "='GeschaetzteModelle' AND " + DBKernel.delimitL("TableID") + "=" + id, false, false);
		
		return numDBSuccesses;
    }
    private int deleteTSID(Connection conn, Object tsID) {
    	int numDBSuccesses = 0;
		String sql = "SELECT " + DBKernel.delimitL("Referenz") + " FROM " + DBKernel.delimitL("Versuchsbedingungen") +
				" WHERE " + DBKernel.delimitL("ID") + "=" + tsID;
		ResultSet rs = DBKernel.getResultSet(conn, sql, false);
		try {
			if (rs != null && rs.first()) {
				do {
					Object o = rs.getObject("Referenz");
					if (o != null) {
						int numForeignCounts = DBKernel.getUsagecountOfID("Literatur", (Integer) o);
						if (numForeignCounts == 1) {
							numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("Literatur") + " WHERE " + DBKernel.delimitL("ID") + "=" + o, false, false);							
						}
					}
				} while (rs.next());
			}
		}
		catch (Exception e) {MyLogger.handleException(e);}

    	sql = "SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL("Messwerte") +
				" WHERE " + DBKernel.delimitL("Versuchsbedingungen") + "=" + tsID;
    	rs = DBKernel.getResultSet(conn, sql, false);
		try {
			if (rs != null && rs.first()) {
				do {
					Object o = rs.getObject("ID");
					if (o != null) {
						numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("Messwerte_Sonstiges") + " WHERE " + DBKernel.delimitL("Messwerte") + "=" + o, false, false);
					}
				} while (rs.next());
			}
		}
		catch (Exception e) {MyLogger.handleException(e);}
		
		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("Messwerte") + " WHERE " + DBKernel.delimitL("Versuchsbedingungen") + "=" + tsID, false, false);
		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("Versuchsbedingungen_Sonstiges") + " WHERE " + DBKernel.delimitL("Versuchsbedingungen") + "=" + tsID, false, false);
		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("ImportedCombaseData") + " WHERE " + DBKernel.delimitL("Versuchsbedingung") + "=" + tsID, false, false);
		numDBSuccesses += DBKernel.sendRequestGetAffectedRowNumber(conn, "DELETE FROM " + DBKernel.delimitL("Versuchsbedingungen") + " WHERE " + DBKernel.delimitL("ID") + "=" + tsID, false, false);
		
		return numDBSuccesses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[]{getOutSpec(inSpecs[0])};
    }
    private DataTableSpec getOutSpec(DataTableSpec inSpec) {
    	DataColumnSpec[] oldSpecs = new DataColumnSpec[inSpec.getNumColumns() + 1];
		for (int i=0;i<oldSpecs.length-1;i++) {
			oldSpecs[i] = inSpec.getColumnSpec(i);
		}
		DataColumnSpec resultSpec = new DataColumnSpecCreator("DBResult", IntCell.TYPE).createSpec();
		oldSpecs[oldSpecs.length-1] = resultSpec;
		return new DataTableSpec(oldSpecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	/*
    	settings.addString(PARAM_FILENAME, filename);
    	settings.addString(PARAM_LOGIN, login);
    	settings.addString(PARAM_PASSWD, passwd);
    	settings.addBoolean(PARAM_OVERRIDE, override);
    	*/
    	settings.addBoolean(PARAM_DELTESTCOND, delTS);
    	settings.addBoolean(PARAM_DELPRIMARYMODELS, delPM);
    	settings.addBoolean(PARAM_DELSECONDARYMODELS, delSM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	/*
    	filename = settings.getString(PARAM_FILENAME);
    	login = settings.getString(PARAM_LOGIN);
    	passwd = settings.getString(PARAM_PASSWD);
    	override = settings.getBoolean(PARAM_OVERRIDE);
    	*/
    	delTS = settings.getBoolean(PARAM_DELTESTCOND);
    	if (settings.containsKey(PARAM_DELPRIMARYMODELS)) delPM = settings.getBoolean(PARAM_DELPRIMARYMODELS); else delPM = false;;
    	if (settings.containsKey(PARAM_DELSECONDARYMODELS)) delSM = settings.getBoolean(PARAM_DELSECONDARYMODELS); else delSM = false;;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

}

