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
/**
 * 
 */
package org.hsh.bfr.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.hsh.bfr.db.gui.dbtable.MyDBForm;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;

/**
 * @author Armin
 *
 */
public class MyTable {
	
	private String tableName = null;
	private String[] fieldNames = null;
	private String[] fieldTypes = null;
	private String[] fieldComments = null;
	private MyTable[] foreignFields = null;
	private String[] mnTable = null;
	private Vector<String> listMNs = null;
	private String[][] uniqueFields = null;
	private String[] defaults = null;
	private LinkedHashMap<Object, String>[] foreignHashs = null;
	private boolean hideScore = false;
	private boolean hideTested = false;
	private boolean hideKommentar = false;

	// Parameter zum Abspeichern
	private LinkedHashMap<Integer, Integer> rowHeights = new LinkedHashMap<Integer, Integer>();
	private int[] colWidths = null;
	private List<? extends SortKey> sortKeyList = null;
	private String searchString = "";
	private int selectedRow = -1;
	private int selectedCol = 0;
	private int verticalScrollerPosition = 0;
	private int horizontalScrollerPosition = 0;
	private int form_SelectedID = 0;
	
	public MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, null, null);
	}
	public MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, uniqueFields, null);
	}
	public MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, LinkedHashMap<Object, String>[] foreignHashs) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, null, foreignHashs);
	}
	public MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields, LinkedHashMap<Object, String>[] foreignHashs) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, uniqueFields, foreignHashs, null);		
	}
	public MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields, LinkedHashMap<Object, String>[] foreignHashs, String[] mnTable) {
		this(tableName, fieldNames, fieldTypes, fieldComments, foreignFields, uniqueFields, foreignHashs, mnTable, null);		
	}
	public MyTable(String tableName, String[] fieldNames, String[] fieldTypes, String[] fieldComments, MyTable[] foreignFields, String[][] uniqueFields, LinkedHashMap<Object, String>[] foreignHashs, String[] mnTable, String[] defaults) {
		this.tableName = tableName;
		this.fieldNames = fieldNames;
		this.fieldTypes = fieldTypes;
		this.fieldComments = fieldComments;
		this.foreignFields = foreignFields;
		this.uniqueFields = uniqueFields;
		this.foreignHashs = foreignHashs;
		this.mnTable = mnTable;
		this.defaults = defaults;
		try {
			if (mnTable != null) {
				for (int i=0;i<mnTable.length;i++) {
					if (mnTable[i] != null && mnTable[i].length() > 0) {
						if (listMNs == null) listMNs = new Vector<String>();
						listMNs.add(fieldNames[i]);
					}
				}			
			}			
		}
		catch (Exception e) {MyLogger.handleException(e);}
		hideKommentar = tableName.equals("ChangeLog") || tableName.equals("DateiSpeicher") || tableName.equals("ComBaseImport")
		 || tableName.equals("Nachweisverfahren_Kits") || tableName.equals("Aufbereitungsverfahren_Kits") || tableName.equals("Methoden_Normen")
		 || tableName.equals("Methodennormen") || tableName.equals("Labore_Methodiken") || tableName.equals("Labore_Matrices")
		 || tableName.equals("Labore_Agenzien") || tableName.equals("Labore_Agenzien_Methodiken")
		 || tableName.startsWith("ICD10_") || tableName.equals("DoubleKennzahlen")
		 || tableName.equals("SonstigeParameter") || tableName.equals("Einheiten")
		 || tableName.equals("Infotabelle") || tableName.equals("ToxinUrsprung")
		   || tableName.equals("Prozessdaten_Messwerte")
		   || tableName.equals("Verpackungsmaterial")
		   || tableName.equals("ImportedCombaseData")
		   || tableName.equals("Parametertyp")
		   || tableName.equals("Prozessdaten_Literatur") || tableName.equals("ProzessWorkflow_Literatur")
		 || tableName.equals("Produzent_Artikel") || tableName.equals("Artikel_Lieferung") || tableName.equals("Lieferung_Lieferungen")
		 // StatUp 
		  || tableName.equals("ModellkatalogParameter") || tableName.equals("Modell_Referenz") || tableName.equals("GeschaetztesModell_Referenz")
		  || tableName.equals("GeschaetzteParameter") || tableName.equals("GeschaetzteParameterCovCor") || tableName.equals("Sekundaermodelle_Primaermodelle")
		  || tableName.equals("VarParMaps") || tableName.equals("DataSource");

		 hideTested = hideKommentar || tableName.equals("Users") || tableName.equals("Prozess_Verbindungen")
		 || tableName.equals("Zutatendaten_Sonstiges") || tableName.equals("Versuchsbedingungen_Sonstiges") || tableName.equals("Messwerte_Sonstiges")
		  || tableName.equals("Prozessdaten_Sonstiges") || tableName.equals("Krankheitsbilder_Symptome") || tableName.equals("Krankheitsbilder_Risikogruppen") || tableName.equals("Agens_Matrices")
		 || tableName.equals("Kontakte") || tableName.equals("Codes_Agenzien") || tableName.equals("Literatur")
		 || tableName.equals("Codes_Matrices") || tableName.equals("Methoden") || tableName.equals("Codes_Methoden")
		 || tableName.equals("Methodiken") || tableName.equals("Codes_Methodiken")|| tableName.equals("Nachweisverfahren_Testanbieter")
		 || tableName.equals("Produzent") || tableName.equals("Labore") || tableName.equals("Testanbieter")
		 || tableName.equals("Matrices") || tableName.equals("Agenzien") || tableName.equals("Einheiten")
		 || tableName.equals("Symptome") || tableName.equals("Risikogruppen") || tableName.equals("Tierkrankheiten") || tableName.equals("Zertifizierungssysteme")
		 || tableName.equals("ProzessElemente") //|| tableName.equals("Prozessdaten_Workflow")
		 || tableName.equals("GueltigkeitsBereiche")
		 || tableName.equals("Kostenkatalog") || tableName.equals("Kostenkatalogpreise")
		 || tableName.equals("Prozessdaten_Kosten") || tableName.equals("Zutatendaten_Kosten")
		 // StatUp
		  || tableName.equals("Modellkatalog")
		  // Jans Tabellen
		 || tableName.equals("Exposition") || tableName.equals("Risikocharakterisierung") || tableName.equals("Verwendung") 
		 || tableName.equals("Transport")  || tableName.equals("Methoden_Software") || tableName.equals("Produkt")
		 // Krise
		 || tableName.equals("LieferungVerbindungen") || tableName.equals("ChargenVerbindungen") || tableName.equals("Lieferungen") || tableName.equals("Produktkatalog")
		 || tableName.equals("Station") || tableName.equals("Chargen") || tableName.equals("Station_Agenzien") || tableName.equals("Produktkatalog_Matrices");

		 hideScore = hideTested
			 || tableName.equals("Messwerte") || tableName.equals("Kits") || tableName.equals("Zutatendaten");
	}
	public void saveProperties(MyDBForm myForm) {
		form_SelectedID = myForm.getSelectedID();
	}
	public void restoreProperties(MyDBForm myForm) {
		myForm.setSelectedID(form_SelectedID);
	}
	public void saveProperties(MyDBTable myDB) {
		JTable bigTable = myDB.getTable();
		JScrollPane scroller = myDB.getScroller();
		if (scroller != null) {
			verticalScrollerPosition = scroller.getVerticalScrollBar().getValue();
			horizontalScrollerPosition = scroller.getHorizontalScrollBar().getValue();
		}
		else {
			verticalScrollerPosition = 0; horizontalScrollerPosition = 0;
		}
		
		rowHeights.clear();
		for (int i=0;i<bigTable.getRowCount();i++) {
			Object o = bigTable.getValueAt(i, 0);
	  	if (o != null && o instanceof Integer) rowHeights.put((Integer)bigTable.getValueAt(i, 0), bigTable.getRowHeight(i));
		}							
		colWidths = new int[bigTable.getColumnCount()];
		for (int i=0;i<colWidths.length;i++) {
			colWidths[i] = bigTable.getColumnModel().getColumn(i).getWidth();
		}				
		if (bigTable.getRowSorter() != null && bigTable.getRowSorter().getSortKeys().size() > 0) {
			sortKeyList = bigTable.getRowSorter().getSortKeys();
		}
		searchString = "";
		try{searchString = myDB.getMyDBPanel().getSuchfeld().getText();}catch (Exception e) {}
		if (bigTable.getRowCount() > 0) {
			selectedRow = bigTable.getSelectedRow();
			selectedCol = bigTable.getSelectedColumn();
			//System.out.println("saveProperties\t" + selectedRow);
		}
	}
	public void restoreProperties(MyDBTable myDB) {
		JTable bigTable = myDB.getTable();
		try{myDB.getMyDBPanel().getSuchfeld().setText(searchString);myDB.getMyDBPanel().handleSuchfeldChange(null);}catch (Exception e) {}
		if (sortKeyList != null && bigTable.getRowSorter() != null) {
			bigTable.getRowSorter().setSortKeys(sortKeyList);
			@SuppressWarnings("unchecked")
			TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) bigTable.getRowSorter();
			sorter.sort();
			
		}
		if (rowHeights != null) {
			for (int i=0;i<bigTable.getRowCount();i++) {
			  	if (bigTable.getRowCount() > i && rowHeights.containsKey(bigTable.getValueAt(i, 0))) {
			  		Integer rh = rowHeights.get(bigTable.getValueAt(i, 0));
			  		bigTable.setRowHeight(i, rh);
			  	}
			}							
		}
		if (colWidths != null) {
			for (int i=0;i<colWidths.length;i++) {
				if (bigTable.getColumnCount() > i) bigTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
			}							
		}
		if (selectedRow >= 0) {
			myDB.setSelectedRowCol(selectedRow, selectedCol, verticalScrollerPosition, horizontalScrollerPosition, true);
		}
		else if (tableName.equals("Agenzien") || tableName.equals("Matrices") || tableName.equals("Methoden")) {
			if (myDB.getMyDBPanel() != null && myDB.getMyDBPanel().getMyDBTree() != null) {
				if (tableName.equals("Agenzien")) myDB.setSelectedID(90);			
				if (tableName.equals("Matrices")) myDB.setSelectedID(17024);			
				if (tableName.equals("Methoden")) myDB.setSelectedID(697);			
			}
		}
		else {
			myDB.selectCell(0, 0);
		}
	}
	public boolean isFirstTime() {
		return colWidths == null;
	}
	public String getTablename() {
		return tableName;
	}
	public boolean getHideScore() {
		return hideScore;
	}
	public boolean getHideTested() {
		return hideTested;
	}
	public boolean getHideKommentar() {
		return hideKommentar;
	}
	public boolean isReadOnly() {
		return tableName.equals("ChangeLog") || tableName.equals("DateiSpeicher") ||
				DBKernel.isKNIME && DBKernel.prefs.getBoolean("PMM_LAB_SETTINGS_DB_RO", true);
	}
	public Vector<Integer> getMyBLOBs() {
		Vector<Integer> myBLOBs = new Vector<Integer>();
    for (int i=0;i<fieldTypes.length;i++) {
    	if (fieldTypes[i].startsWith("BLOB(")) {    	
    		myBLOBs.add(i);
    	}
    }
		return myBLOBs;
	}
	public String toString() {
		return getTablename();
	}
	public Vector<String> getListMNs() {
		return listMNs;
	}
	public String[] getMNTable() {
		return mnTable;
	}
	public MyTable[] getForeignFields() {
		return foreignFields;
	}
	public void setForeignField(MyTable myT, int pos) {
		foreignFields[pos] = myT;
	}
	public LinkedHashMap<Object, String>[] getForeignHashs() {
		return foreignHashs;
	}
	public String[] getFieldTypes() {
		return fieldTypes;
	}
	public String[] getFieldNames() {
		return fieldNames;
	}
	public String[] getFieldComments() {
		return fieldComments;
	}
	public int getNumFields() {
		int add = 1; // ID
		if (!hideScore) add++;
		if (!hideKommentar) add++;
		if (!hideTested) add++;
		return fieldNames.length + add; // + ID + Kommentar + Guetescore + Geprueft
	}
	
	public String getRowCountSQL() {
		return "SELECT COUNT(*) FROM " + DBKernel.delimitL(tableName);
	}

	public String getSelectSQL() {
		String fieldDefs = DBKernel.delimitL("ID");
	    for (int i=0;i<fieldNames.length;i++) {
	    	fieldDefs += "," + DBKernel.delimitL(fieldNames[i]);    		
	    }
	    if (!hideScore) fieldDefs += "," + DBKernel.delimitL("Guetescore");
	    if (!hideKommentar) fieldDefs += "," + DBKernel.delimitL("Kommentar");
	    if (!hideTested) fieldDefs += "," + DBKernel.delimitL("Geprueft");
		return "SELECT " + fieldDefs + " FROM " + DBKernel.delimitL(tableName);
	}
	
	public String getInsertSQL1() {
		return "INSERT INTO " + DBKernel.delimitL(tableName) + " " + getInsertSql();
	}
	public String getInsertSQL2() {
		return getInsertSql2();
	}
	
	public String getUpdateSQL1() {
		return "UPDATE " + DBKernel.delimitL(tableName) + " SET " + getUpdateSql();
	}
	public String getUpdateSQL2() {
		return getUpdateSql2();
	}
	
	public String getDeleteSQL1() {
		return "DELETE FROM " + DBKernel.delimitL(tableName) + " WHERE " + DBKernel.delimitL("ID") + " = ?";
	}
	public String getDeleteSQL2() {
		return "1";
	}
	
	public List<String> getIndexSQL() {
		List<String> indexSQL = new ArrayList<String>();	
    for (int i=0;i<fieldNames.length;i++) {
    	if (foreignFields[i] != null) {
    		if (mnTable == null || mnTable[i] == null || mnTable[i].length() == 0) {
    			boolean odsn = true;
    			if (tableName.equals("Modellkatalog") || tableName.equals("ModellkatalogParameter")
    					|| tableName.equals("Modell_Referenz") || tableName.equals("GeschaetzteModelle")
    					|| tableName.equals("GeschaetztesModell_Referenz") || tableName.equals("GeschaetzteParameter")
    					|| tableName.equals("GeschaetzteParameterCovCor") || tableName.equals("Sekundaermodelle_Primaermodelle")
    					 || tableName.equals("GueltigkeitsBereiche")) odsn = false;
    			indexSQL.add("ALTER TABLE " + DBKernel.delimitL(tableName) + " ADD CONSTRAINT " + DBKernel.delimitL(tableName + "_fk_" + fieldNames[i] + "_" + i) +
    	        		" FOREIGN KEY (" + DBKernel.delimitL(fieldNames[i]) + ")" +
    	        		" REFERENCES " + DBKernel.delimitL(foreignFields[i].getTablename()) + " (" + DBKernel.delimitL("ID") + ") " + (odsn ? "ON DELETE SET NULL;" : ";"));
    		}
    	}
    }
  	if (uniqueFields != null) {
  		for (int i=0;i<uniqueFields.length;i++) {
  			String uFs = "";
  			for (int j=0;j<uniqueFields[i].length;j++) {
  				uFs += ","+DBKernel.delimitL(uniqueFields[i][j]);
  			}
  			indexSQL.add("ALTER TABLE " + DBKernel.delimitL(tableName) + " ADD CONSTRAINT " + DBKernel.delimitL(tableName + "_uni_" + i) +
        		" UNIQUE (" + uFs.substring(1) + ");");
      }
  	}
		return indexSQL;
	}
	public void createTable() {
		createTable(false);
	}
	public void createTable(final boolean suppressWarnings) {		
	    String fieldDefs = DBKernel.delimitL("ID") + " INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY";
		if (tableName.equals("ChangeLog")) {
		    fieldDefs = DBKernel.delimitL("ID") + " INTEGER GENERATED BY DEFAULT AS SEQUENCE " + DBKernel.delimitL("ChangeLogSEQ") + " PRIMARY KEY";			
		}
	    //String uFsAll = "";
	    for (int i=0;i<fieldNames.length;i++) {
	    	if (fieldTypes[i].startsWith("BLOB(") && !tableName.equals("DateiSpeicher")) {
	    		fieldDefs += "," + DBKernel.delimitL(fieldNames[i]) + " " + "VARCHAR(255)";
	    	}
	    	else {
	    		String defolt = null;
	    		if (defaults != null && defaults[i] != null) defolt = defaults[i]; 
	    		fieldDefs += "," + DBKernel.delimitL(fieldNames[i]) + " " + fieldTypes[i] + (defolt == null ? "" : " " + defolt);    
		    	if (!tableName.equals("DateiSpeicher") && !tableName.equals("ChangeLog")) {
		    		//uFsAll += "," + DBKernel.delimitL(fieldNames[i]);
		    	}
	    	}
	    }
	  	// "alles" UNIQUE machen
	  	/*
	  	if (uFsAll.length() > 0) {
			indexSQL.add("ALTER TABLE " + DBKernel.delimitL(tableName) + " ADD CONSTRAINT " + DBKernel.delimitL(tableName + "_uni_all") +
	        		" UNIQUE (" + uFsAll.substring(1) + ");");	  		
	  	}
	  	*/
	  	if (!hideScore) fieldDefs += "," + DBKernel.delimitL("Guetescore") + " " + "INTEGER";
	  	if (!hideKommentar) fieldDefs += "," + DBKernel.delimitL("Kommentar") + " " + "VARCHAR(1023)";
	  	if (!hideTested) fieldDefs += "," + DBKernel.delimitL("Geprueft") + " " + "BOOLEAN";
	    DBKernel.createTable(tableName, fieldDefs, getIndexSQL(), true, true);		
	}

  private String getUpdateSql() {
    String result = "";
    for (int i=0;i<fieldNames.length;i++) {
    	result += DBKernel.delimitL(fieldNames[i]) + "=?,";
    }
    if (!hideScore) result += DBKernel.delimitL("Guetescore") + "=?,";
    if (!hideKommentar) result += DBKernel.delimitL("Kommentar") + "=?,";
    if (!hideTested) result += DBKernel.delimitL("Geprueft") + "=?,";
    if (result.length() > 0) result = result.substring(0, result.length() - 1); // letztes Komma weg!
    result += " WHERE " + DBKernel.delimitL("ID") + "=?";
    return result;
  }
  private String getUpdateSql2() {
    String result = "";
    for (int i=2;i<=getNumFields();i++) {// fieldNames.length+4 //  + Kommentar + Guetescore
    	result += i + ",";
    }
    result += "1";
    return result;
  }

  private String getInsertSql() {
    String result = "";
    String qms="";
    String columnName;
    for (int i=0;i<fieldNames.length;i++) {
    	columnName = fieldNames[i];
      result += DBKernel.delimitL(columnName) + ",";
      qms += "?,";
    }
    if (!hideScore) {result += DBKernel.delimitL("Guetescore") + ","; qms += "?,";}
    if (!hideKommentar) {result += DBKernel.delimitL("Kommentar") + ","; qms += "?,";}
    if (!hideTested) {result += DBKernel.delimitL("Geprueft") + ","; qms += "?,";}
    if (result.length() > 0) result = result.substring(0, result.length() - 1); // letztes Komma weg!
    if (qms.length() > 0) qms = qms.substring(0, qms.length() - 1); // letztes Komma weg!
    if (result.length() > 0) result = "(" + result + ") VALUES (" + qms + ")";
    return result;
  }
  private String getInsertSql2() {
    String result = "";
    for (int i=2;i<=getNumFields();i++) {// fieldNames.length+4 // + Kommentar + Guetescore
    	result += i + ",";
    }
    if (result.length() > 0) result = result.substring(0, result.length() - 1);
    return result;
  }
  
  public String getMetadata() {
	  String result = "--------------  " + tableName + "  --------------\n";
	    for (int i=0;i<fieldNames.length;i++) {
	    	result += fieldNames[i] + "\t" + (fieldComments[i] == null ? fieldNames[i] : fieldComments[i]) + "\t" + fieldTypes[i] + "\n";
	    }
	  	if (uniqueFields != null) {
	  		for (int i=0;i<uniqueFields.length;i++) {
	  			String uFs = "";
	  			for (int j=0;j<uniqueFields[i].length;j++) {
	  				uFs += ","+uniqueFields[i][j];
	  			}
	  			result += uFs.substring(1) + "\t";
	      }
	  	}
	  return result;
  }
}
