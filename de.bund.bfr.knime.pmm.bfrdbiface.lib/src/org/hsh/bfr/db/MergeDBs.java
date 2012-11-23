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

import java.awt.Cursor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.hsh.bfr.db.gui.dbtable.MyDBTable;

/**
 * @author Weiser
 *
 */
public class MergeDBs {

	private Hashtable<String, Integer> lastInsertedID;
	private Hashtable<String, Integer> idConverter;
	private Hashtable<String, Integer> idConverterReverse;
	private Hashtable<MyTable, MyTable> usedTs;
	private Hashtable<String, String> usedIDs;
	private HashMap<String, Integer> checkIfOthersAlreadyEditedUpdates;
	private String untersuchteDB = "";
	private boolean isFalenski = false;
	private boolean isMertens = false;
	private boolean isWese = false;
	private boolean isHammerl = false;
	private boolean isBoehnlein = false;
	private String DBVersion = "1.4.4"; // im Code mitunter als "oldVersion" angewendet...
	
	/*
	 * 
// NICHT vergessen:
 * Einsammeln von: Mertens, B�hnlein, Falenski, Wese, Hammerl, Niederberger, Buschulte, Burchardi, Analytik Jena
 * 		0. DBVersion �ndern!!!
 * 		1. Vorher Backup von allen DBs machen!!!!
 * 		2: DatumAb und Pfad anpassen!
 * 		3. Passwort�nderungen kontrollieren!
 * 		4. Ausnahmen in SourceCode definieren wegen evtl. DB �nderungen (suche nach "Ausnahmen", auch in doFinalizingThings schauen, da ist ein guter Ort f�r finale �nderungen!)
 * 			- Welche Tabellen gilt es besonders zu �berpr�fen bzgl. unabsichtlicher Ver�nderung (z.B. L�schung)?
 * 		5. Aufpassen auf:
 * 			- not merged: es kam schon vor, dass jemand einen Matrixeintrag ver�ndert hat, diesen dann ausgew�hlt hat. Nach dem mergen, war der falsche Eintrag ausgew�hlt, da die Matrixnamens�nderung nicht �bernommen wurde, Beispiel: Matrix ID 54 wurde von Frucht... ge�ndert in Rindfleischsosse...
 * 			. Konfliktgefahr entsteht dadurch, dass leere Daterns�tze erzeugt werden. Diese werden dann von verschiedenen Nutzern verwendet...
 * 		6. "Leere" Datens�tze beobachten und l�schen!
 * 		7. Literatur ID >= 587 manuell einf�gen!!! Genauso: GeschaetzteModelle etc. und zwar f�r DBVersion = 1.3.9!!!
	 * 
	 */
	public MergeDBs() {
		if (DBKernel.isKNIME) {
			return;
		}
	    int retVal = JOptionPane.showConfirmDialog(DBKernel.mainFrame, "Sicher?",
	    		"DBs zusammenf�hren?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (retVal == JOptionPane.YES_OPTION && DBKernel.getTempSA().equals("defad")) {
			try {
				DBKernel.myList.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				usedTs = new Hashtable<MyTable, MyTable>();
				usedIDs = new Hashtable<String, String>();
				checkIfOthersAlreadyEditedUpdates = new HashMap<String, Integer>();
				String folder = "Q:/BfR/DBs/";
				String dateFrom = "2012-07-10 00:00:00"; // 20120403
				//String dateFrom = "2012-08-22 12:00:00"; // Lars
				//folder = "C:/Dokumente und Einstellungen/Weiser/Desktop/144_lars/";

				//idConverter = new Hashtable<String, Integer>(); idConverterReverse = new Hashtable<String, Integer>();  lastInsertedID = new Hashtable<String, Integer>();
				//isBoehnlein = true; go4It(folder + "141_boehnlein/", dateFrom); isBoehnlein = false;
				//idConverter = new Hashtable<String, Integer>(); idConverterReverse = new Hashtable<String, Integer>();   lastInsertedID = new Hashtable<String, Integer>();
				//isHammerl = true; go4It(folder + "141_hammerl/", dateFrom); isHammerl = false;					
				//idConverter = new Hashtable<String, Integer>(); idConverterReverse = new Hashtable<String, Integer>();   lastInsertedID = new Hashtable<String, Integer>();
				//isWese = true; go4It(folder + "141_wese/", dateFrom); isWese = false;	
				idConverter = new Hashtable<String, Integer>(); idConverterReverse = new Hashtable<String, Integer>();   lastInsertedID = new Hashtable<String, Integer>();
				isMertens = true; go4It(folder + "143_mertens/", dateFrom); isMertens = false;

				for (Enumeration<MyTable> e=usedTs.keys(); e.hasMoreElements();) {
					DBKernel.doMNs(e.nextElement());
				}

				MyDBTable myDB = DBKernel.myList.getMyDBTable();
				myDB.setTable(myDB.getActualTable());
				
				JOptionPane.showMessageDialog(DBKernel.mainFrame, "Fertig!", "DBs zusammenf�hren", JOptionPane.INFORMATION_MESSAGE);		
			}
			finally {
				DBKernel.myList.setCursor(Cursor.getDefaultCursor());
			}
		}
	}
	// Haben sich Feldertypen ver�ndert? M�ssen Daten von einer Tabelle in eine andere Tabelle verschoben werden? Neu verlinkt?
	// Dinge, die schon vor dem Stichtag (dateFrom) in der DB waren und jetzt verschoben/ver�ndert werden m�ssen, aber vorher nicht konnten
	// Unbedingt Beispiel einf�gen!!!!
	private void doFinalizingThings(final Statement anfrage) {
	}
	private void go4It(final String dbPath, final String datumAb) {
		System.out.println(dbPath);
		untersuchteDB = dbPath;
		boolean dl = DBKernel.dontLog;
		DBKernel.dontLog = true;
		try {
			Connection conn = DBKernel.getDefaultAdminConn(dbPath, true);
		    Statement anfrage = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		    checkeDoppeltVergebeneDKZs(anfrage);
		    if (DBVersion.equals("1.3.9")) {
				//System.err.println("ok, hier ist die TriggerScheisse passiert, wo wegen Statup (data-in-motion) der Trigger abgeschaltet worden ist");
				// bei den gesch�tzten Modellen d�rfte das nicht passiert sein, oder???? Checken nicht vergessen!!!
				//  || tablename.equals("Literatur") && tID >= 587)
		    	lastInsertedID.put("Literatur", 586);
		    	go4Probleme(DBVersion, anfrage, "Literatur", 587, false); // sicherheitshalber, denn: Dieser Fehler kann aber auch �bersehen werden, wenn es kein Update gibt, sonder nur ein INSERT, z.B. beim ris-Importer!
		    	lastInsertedID.put("GeschaetzteModelle", 0);
		    	lastInsertedID.put("GeschaetzteParameter", 0);
		    	lastInsertedID.put("GeschaetztesModell_Referenz", 0);
		    	lastInsertedID.put("GeschaetzteParameterCovCor", 0);
		    	lastInsertedID.put("Sekundaermodelle_Primaermodelle", 0);
		    }
		    //go4Probleme(DBVersion, anfrage, "Literatur", 587, true);
		    System.out.println("go4ChangeLog");
		    go4ChangeLog(anfrage, datumAb);
		    /*
		    go4Probleme(DBVersion, anfrage, "GeschaetzteModelle", 0, false);
		    go4Probleme(DBVersion, anfrage, "GeschaetzteParameter", 0, false);
		    go4Probleme(DBVersion, anfrage, "GeschaetztesModell_Referenz", 0, false);
		    go4Probleme(DBVersion, anfrage, "GeschaetzteParameterCovCor", 0, false);
		    go4Probleme(DBVersion, anfrage, "Sekundaermodelle_Primaermodelle", 0, false);
		    */
		    System.out.println("go4Dateispeicher");
			go4Dateispeicher(anfrage, datumAb);
		    System.out.println("doFinalizingThings");
			doFinalizingThings(anfrage);
		    System.out.println("SHUTDOWN");
			anfrage.execute("SHUTDOWN");
			anfrage.close();
			conn.close();
		}
		catch (Exception e) {
			MyLogger.handleException(e);
		}		
		DBKernel.dontLog = dl;		
	}

	private boolean doAlteintraege(final Object[] alteintraege, final String tablename, final Integer tID, final MyTable myT, final Statement anfrage, final Timestamp ts) {
		PreparedStatement ps = null;
		try {
		    if (alteintraege != null) {
		    	Object[] eintragAlt = null, eintragNeu = null;
		    	if (alteintraege.length == 0) {
		    		MyLogger.handleMessage("doAlteintraege - Fehler?!?");
		    		return false;
		    	}
		    	else if (alteintraege.length > 1) {
		    		eintragNeu = (Object[]) alteintraege[1];
		    	}
		    	else if (alteintraege.length == 1) {
		    		if (getID(anfrage, tablename, "ID", ""+tID) == null) {
						if (tablename.equals("Versuchsbedingungen") && (tID >= 1847 && tID <= 2264 && isBoehnlein && DBVersion.equals("1.4.2"))) {
							// hat B�hnlein gel�scht, nicht gut....
							System.err.println("Versuchsbedingungen von B�hnlein gel�scht, nicht ok... " + tID);
						}
						else {
			    			Integer cid = convertID(tablename, tID, true);
			    			String sql = "DELETE FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("ID") + "=" + cid;			    	
				    		DBKernel.sendRequest(sql, false);
					    	if (!tablename.equals("Kennzahlen")) {
					    		String tt= "";
					    		if (isMertens && DBVersion.equals("1.3.7")) {
					    			// in der MERTENS DB 1.3.6/7 ist  irgendwas faul... lieber nicht in ChangeLog nachschauen, sonst Absturz...
					    		}
					    		else {
									LinkedHashMap<Integer, Vector<String>> v = DBKernel.getUsersFromChangeLog(anfrage, tablename, tID, null, true);
								  	for (Map.Entry<Integer, Vector<String>> entry : v.entrySet()) {
										for (String entr : entry.getValue()) {
											tt += entry.getKey() + "\t" + entr + "\n"; 
										}
								  	}			
						    	}			    			
					    		System.err.println("not Kennzahlen, ist das auch kein Versehen? " + sql + "\t" + tID + "\n" + tt);
				    		}
						}
		    			return true;
		    		}
		    		ResultSet rs = getResultSet(anfrage, "SELECT * FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("ID") + "=" + tID, false);
		    		if (rs != null && rs.first()) {
		    			eintragNeu = new Object[rs.getMetaData().getColumnCount()]; 
		    			for (int i=0;i<eintragNeu.length;i++) {
		    				eintragNeu[i] = rs.getObject(i+1);
		    			}
		    			rs.close();
		    		}
		    	}

		    	eintragAlt = (Object[]) alteintraege[0];
	    		if (eintragAlt == null) { // INSERT
					ps = DBKernel.getDBConnection().prepareStatement(myT.getInsertSQL1(), Statement.RETURN_GENERATED_KEYS);
					Object[] newF = doFields(ps, myT, eintragNeu); 
		    		if (newF != null) {
		    			//System.err.println(newF[1] + "\t" + ps.toString());
						if (ps.executeUpdate() > 0) {
							Integer lastID = DBKernel.getLastInsertedID(ps);
							if (lastInsertedID.get(tablename) == null || lastInsertedID.get(tablename) < tID) {
								lastInsertedID.put(tablename, tID);
							}				    	
							if (idConverter.containsKey(tablename + "_" + tID)) {
								System.err.println("Ups... idConverter contains " + tablename + "_" + tID + " already..." + idConverter.get(tablename + "_" + tID) + "\t" + lastID);
							}
							if (idConverterReverse.containsKey(tablename + "_" + lastID)) {
								System.err.println("Ups... idConverterReverse contains " + tablename + "_" + lastID + " already..." + idConverterReverse.get(tablename + "_" + lastID) + "\t" + tID);
							}
							idConverter.put(tablename + "_" + tID, lastID);			
							idConverterReverse.put(tablename + "_" + lastID, tID);			
							checkUsedIDs(tablename, lastID);
							/*
				    		if (tablename.equals("Literatur")) { // 174864 175072 //  && (cid == 583 || cid == 584 || cid == 585)
				    			System.err.println("WEb\t" + tID + "\t" + lastID);
				    		}
				    		*/				    		
						} 
		    		}
	    		}
				else { // UPDATE
						if (isMertens && tablename.equals("Nachweisverfahren") &&
								(tID == 180 || tID == 181 || tID == 182) &&
								(ts.getTime() == 1326881481188L || ts.getTime() == 1326881855383L || ts.getTime() == 1326883213735L)) { // hier gab es einen Konflikt mit Hammerl, die beide dieselben leeren Datens�tze benutzt haben!!!
							Integer neueID = insertNewEmptyDatensatz(tablename, tID);
							System.err.println("isMertens\t" + ts + "\t" + ts.getTime() + "\t" + tID + "\t" + neueID);
							System.err.println("Alteintrag: " + eintragAlt2String(eintragAlt));
						}
						if (DBVersion.equals("1.4.2")) {
							if (tablename.equals("SonstigeParameter") && tID == 121
									&& (ts.getTime() == 1333629019339L && isBoehnlein ||
									ts.getTime() == 1334221337693L && isWese ||
									ts.getTime() == 1334159961219L && isMertens)) { // hier gab es einen Konflikt mit einem leeren Datensatz von Wese, b�hnlein und mertens und wese haben diesen leeren Datensatz benutzt!!!
								Integer neueID = insertNewEmptyDatensatz(tablename, tID);
								System.err.println("insertNewEmptyDatensatz\t" + ts + "\t" + ts.getTime() + "\t" + tID + "\t" + neueID);
								System.err.println("Alteintrag: " + eintragAlt2String(eintragAlt));
							}
							/*
							if (tablename.equals("Einheiten") && tID == 92
									&& (ts.getTime() == 1334144911590L && isBoehnlein ||
											ts.getTime() == 1334652660305L && isWese ||
											ts.getTime() == 1337007884664L && isMertens)) {
								Integer neueID = insertNewEmptyDatensatz(tablename, tID);
								System.err.println("insertNewEmptyDatensatz\t" + ts + "\t" + ts.getTime() + "\t" + tID + "\t" + neueID);
								System.err.println("Alteintrag: " + eintragAlt2String(eintragAlt));
							}
							*/
						}
						if (isMertens && DBVersion.equals("1.3.9") && tablename.equals("Einheiten") && tID == 88 && eintragAlt[1] == null) {
							insertNewEmptyDatensatz(tablename, tID);						
						}
						
		    			Integer cid = convertID(tablename, tID, true);
						if (checkIfOthersAlreadyEditedUpdates(anfrage, tablename, tID, false)) {
					  		//System.err.println("vorher nachher:"); 
					    	//System.err.println(eintragAlt2String(eintragAlt));
					    	//System.err.println(eintragAlt2String(eintragNeu));
						}
		    			// compareAltEintragMitDB, mal checken, ob ok, sonst k�nnte es sein, dass
		    			// 1. Konflikt
		    			// 2. Fehler in ChangeLog, bei Mertens in Version 1.3.6 passiert
		    			// 3. ???
		    			compareAltEintragMitDB(eintragAlt, tablename, cid, myT);
						if (lastInsertedID.containsKey(tablename) && lastInsertedID.get(tablename) < tID) { // dann ist hier wohl was schiefgelaufen... der INSERT hat nicht stattgefunden... wieso eigentlich ???
							System.err.println("checkencheckenchekcenwassweew\t" + tablename + "\t" + tID + "\t" + cid);	
							// dann f�gen wir halt mal nen leeren Datensatz ein... hier k�nnte was verloren gehen... bitte Alteintrag �berpr�fen!!!!!
							//System.err.println("checkencheckenchekcen!!! ist das korrekt hier?? was los mit ChaNGElOG; WIESO IST DAS KAPUTT????");
							System.err.println("Alteintrag: " + eintragAlt2String(eintragAlt));
							if (tablename.equals("GeschaetzteParameter")) {
								insertRowDeNovo(tablename, eintragAlt, tID); // sonst Fehlermeldung wegen "NOT NULL"
							} else {
								insertNewEmptyDatensatz(tablename, tID);
							}
						}
						if (isMertens && DBVersion.equals("1.3.7") && tablename.equals("Kontakte") && tID == 129 && DBKernel.getValue(tablename, "ID", ""+tID, "ID") == null) {
							insertNewEmptyDatensatz(tablename, tID);
						}
						cid = convertID(tablename, tID, true);
						if (DBKernel.getValue(tablename, "ID", ""+cid, "ID") == null) {
							System.err.println("ok, hier ist die TriggerScheisse passiert, wo wegen Statup (data-in-motion) der Trigger abgeschaltet worden ist: " + "\t" + tablename + "\t" + tID + "\t" + cid);
						}

						ps = DBKernel.getDBConnection().prepareStatement(myT.getUpdateSQL1());
		    			Object[] ddt = doFields(ps, myT, eintragNeu); 
			    		if (ddt != null) {		    			
			    			cid = convertID(tablename, tID, true);
					    	ps.setInt(myT.getNumFields(), cid); // WHERE ID = ... (Integer) eintragNeu[0]
					    	ps.execute();
							if (ps.getUpdateCount() != 1) {
								System.err.println("H����h, was los huier?\t" + ps.getUpdateCount() + "\t" + tablename + "\t" + tID + "\t" + cid + "\t" + DBKernel.getValue(tablename, "ID", ""+cid, "ID") + "\n" + ps.getWarnings() + "\n" + ps.toString());
							}
							else if (tablename.equals("Einheiten") || tablename.equals("SonstigeParameter")) {
								System.err.println(tablename + " - Updated - ID: " + cid + "\t" + eintragAlt2String(ddt));
								checkIfOthersAlreadyEditedUpdates(anfrage, tablename, tID, true);
							}
			    		}
				  	

				}
	    		
	    		return true;
		    }
		    else {
		    	MyLogger.handleMessage("Keine Eintr�ge f�r alteintraege?!?");					    							    	
		    }
		}
		catch (Exception e) {
			MyLogger.handleMessage(tablename);
			MyLogger.handleMessage(ps.toString());
			MyLogger.handleException(e);
		}
		return false;
	}
	private Integer insertNewEmptyDatensatz(final String tablename, final Integer tID) {
		Integer result = null;
		try {
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL(tablename) + " (" + DBKernel.delimitL("ID") + ") VALUES (NULL)", Statement.RETURN_GENERATED_KEYS);
			if (ps.executeUpdate() > 0) {
				result = DBKernel.getLastInsertedID(ps);
				String username = getUsername();//isMertens ? "mertens" : isBoehnlein ? "b�hnlein" : isHammerl ? "hammerl" : isWese ? "wese" : "";
				/*
		    	DBKernel.sendRequest("INSERT INTO " + DBKernel.delimitL("ChangeLog") + " (" + DBKernel.delimitL("Zeitstempel") + ", " + DBKernel.delimitL("Username") + ", " +
			      		DBKernel.delimitL("Tabelle") + ", " + DBKernel.delimitL("TabellenID") + ", " +
			      		DBKernel.delimitL("Alteintrag") + ") VALUES (" + System.currentTimeMillis() + ",'" + username + "','" + tablename + "'," + result + ",NULL)", false);
			      		*/
				try {
			    	PreparedStatement ps2 = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("ChangeLog") +
				      		" (" + DBKernel.delimitL("ID") + ", " + DBKernel.delimitL("Zeitstempel") + ", " + DBKernel.delimitL("Username") + ", " +
				      		DBKernel.delimitL("Tabelle") + ", " + DBKernel.delimitL("TabellenID") + ", " +
				      		DBKernel.delimitL("Alteintrag") + ") VALUES (NEXT VALUE FOR " + DBKernel.delimitL("ChangeLogSEQ") + ", ?, ?, '" + tablename + "'," + result + ", NULL)");
				    	ps2.setTimestamp(1, new Timestamp(0L));
				    	ps2.setString(2, username);
				    	ps2.execute();					
				}
				catch (Exception e) {e.printStackTrace();}
				if (lastInsertedID.get(tablename) == null || lastInsertedID.get(tablename) < tID) {
					lastInsertedID.put(tablename, tID);
				}		
				if (idConverter.containsKey(tablename + "_" + tID)) {
					System.err.println("Ups... idConverter contains " + tablename + "_" + tID + " already..." + idConverter.get(tablename + "_" + tID) + "\t" + result);
				}
				if (idConverterReverse.containsKey(tablename + "_" + result)) {
					System.err.println("Ups... idConverterReverse contains " + tablename + "_" + result + " already..." + idConverterReverse.get(tablename + "_" + result) + "\t" + tID);
				}
				idConverter.put(tablename + "_" + tID, result);			
				idConverterReverse.put(tablename + "_" + result, tID);			
				checkUsedIDs(tablename, result);								
			}
			else {
				System.err.println("hat nicht geklappt...");
			}		
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	private String eintragAlt2String(final Object[] eintragAlt) {
		if (eintragAlt == null) {
			return null;
		}
		String result = eintragAlt[0].toString();
		for (int i=1;i<eintragAlt.length;i++) {
			result += "\t" + eintragAlt[i];
		}
		return result;
	}
	private boolean compareAltEintragMitDB(final Object[] eintragAlt, final String tablename, final int id, final MyTable myT) {
		boolean result = false;
		ResultSet rs = DBKernel.getResultSet("SELECT * FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("ID") + " = " + id, false);
		Object[] modO = doFields(null, myT, eintragAlt);
		try {
			int i=0;
			if (rs != null && rs.first()) {
				for (i=1;i<eintragAlt.length;i++) { // i startet bei 1, die IDs d�rfen nicht verglichen werden, da in den DBs m�glichrweise verschieden...
					Object o1 = modO[i];
					Object o2 = rs.getObject(i+1);
					if (o1 == null && o2 == null || o1 != null && o2 != null && o1.toString().equals(o2.toString())) {
						continue;
					} else if (o2 == null && o1 instanceof Integer && myT.getForeignFields() != null && myT.getForeignFields()[i-1] != null &&
							!DBKernel.hasID(myT.getForeignFields()[i-1].getTablename(), (Integer) o1)) {
						continue;
					} else if (o2 == null && o1 instanceof Double && myT.getForeignFields() != null && myT.getForeignFields()[i-1] != null &&
							!DBKernel.hasID(myT.getForeignFields()[i-1].getTablename(), ((Double) o1).intValue())) {
						continue;
					} else if (o1 instanceof Long && o2 instanceof Date && o2.toString().equals((new Date(1000 * (Long) o1)).toString())) {
						continue;
					} else {
						System.err.println("verschieden:\t" + o1 + "\t" + o2 + "\t" + id + "\t" + tablename + "\t" + myT.getTablename() + "\t" + rs.getMetaData().getColumnName(i+1));
						break;
					}
				}			
			}
			if (i == eintragAlt.length) {
				result = true;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	private Object[] doFields(final PreparedStatement ps, final MyTable myT, final Object[] eintragNeu) {
		if (eintragNeu == null) {
			return null;
		}
		Object[] result = new Object[myT.getNumFields()];
		try {
			String[] ft = myT.getFieldTypes();
			MyTable[] foreigns = myT.getForeignFields();
			String[] mnTable = myT.getMNTable();
			result[0] = convertID(myT.getTablename(), (Integer) eintragNeu[0], false); // ID
			int i=1;
			for (;i<=ft.length;i++) { // ID wird hier nicht ben�tigt
				result[i] = eintragNeu[i];

				if (myT.getTablename().equals("Kennzahlen")) {
					if (i == 2) { // 1: Tabelle; 2: TabellenID
						result[2] = convertID(result[1].toString(), (Integer) result[2], false);
					}
				}

				if (ft[i-1].equals("DOUBLE")) {
					if (result[i] != null && foreigns[i-1] != null && (mnTable == null || mnTable[i - 1] == null)) {
						result[i] = convertID(foreigns[i-1].getTablename(), ((Double) result[i]).intValue(), false).doubleValue();														
					}
					if (ps != null) {
						if (result[i] == null) {
							ps.setNull(i, java.sql.Types.DOUBLE);
						} else {
							ps.setDouble(i, (Double) result[i]);
						}																	
					}
				}
				else if (ps != null && (ft[i-1].startsWith("VARCHAR(") || ft[i-1].startsWith("CHAR(") || ft[i-1].startsWith("BLOB("))) {
					if (result[i] == null) {
						ps.setNull(i, java.sql.Types.VARCHAR);
					} else {
						ps.setString(i, (String) result[i]);
					}											
				}
				else if (ps != null && ft[i-1].equals("BOOLEAN")) {
					if (result[i] == null) {
						ps.setNull(i, java.sql.Types.BOOLEAN);
					} else {
						ps.setBoolean(i, (Boolean) result[i]);
					}											
				}
				else if (ps != null && ft[i-1].equals("DATE")) {
					if (result[i] == null) {
						ps.setNull(i, java.sql.Types.DATE);
					} else {
						if (result[i] instanceof Long) {
							ps.setDate(i, new Date(1000*(Long)result[i]));		// das musste in insertIntoChangeLog mal ge�ndert werden, da es Fehler produziert hat in ein Array in die DB ein TimeStamp Objekt zu legen, weil es nicht serialisiert werden konnte 
						} else {
							ps.setDate(i, (Date) result[i]);
						}				
					}
				}
				else if (ft[i-1].equals("INTEGER")) {
					if (result[i] != null && foreigns[i-1] != null && (mnTable == null || mnTable[i - 1] == null)) {
						if (result[i] instanceof Double) {
							result[i] = convertID(foreigns[i-1].getTablename(), ((Double)result[i]).intValue(), false); // Es wurde mal ein Double zu einem Integer konvertiert: 		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Prozessdaten") + " ALTER COLUMN " + DBKernel.delimitL("Tenazitaet") + " SET DATA TYPE INTEGER", false);
						} else {
							result[i] = convertID(foreigns[i-1].getTablename(), (Integer) result[i], false);
						}														
					}
					if (ps != null) {
						if (result[i] == null) {
							ps.setNull(i, java.sql.Types.INTEGER);
						} else {
							if (result[i] instanceof Double) {
								ps.setInt(i, ((Double)result[i]).intValue()); // Es wurde mal ein Double zu einem Integer konvertiert: 		DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Prozessdaten") + " ALTER COLUMN " + DBKernel.delimitL("Tenazitaet") + " SET DATA TYPE INTEGER", false);
							} else {
								ps.setInt(i, (Integer)result[i]);
							}																	
						}
					}
				}
				else if (ps != null && ft[i-1].equals("BIGINT")) {
					if (result[i] == null) {
						ps.setNull(i, java.sql.Types.BIGINT);
					} else {
						ps.setLong(i, (Long)result[i]);
					}											
				}
				else if (ps != null) {
					MyLogger.handleMessage("Parameter not dwefined... " + myT.getTablename() + "\t" + i);
				}
			}
			if (!myT.getHideScore()) { // INTEGER
				result[i] = eintragNeu[i];
				if (ps != null) {
					if (result[i] == null) {
						ps.setNull(i, java.sql.Types.INTEGER);
					} else {
						ps.setInt(i, (Integer) result[i]);
					}						
				}
				i++;
			}
			if (!myT.getHideKommentar()) { // VARCHAR(1023)
				result[i] = eintragNeu[i];
				if (ps != null) {
					if (result[i] == null) {
						ps.setNull(i, java.sql.Types.VARCHAR);
					} else {
						ps.setString(i, (String) result[i]);
					}
				}
				i++;
			}
			if (!myT.getHideTested()) { // BOOLEAN
				result[i] = eintragNeu[i];
				if (ps != null) {
					if (result[i] == null) {
						ps.setNull(i, java.sql.Types.BOOLEAN);
					} else {
						ps.setBoolean(i, (Boolean) result[i]);
					}			
				}
				i++;
			}		
			
			return result;
		}
		catch (Exception e) {
			MyLogger.handleMessage(myT.getTablename());
			MyLogger.handleException(e);
		}
		return null;
	}
	
	
	private Integer getID(final Statement anfrage, final String tablename, final String feldname, final String feldVal) {
		Integer result = null;
		try {
			ResultSet rs = anfrage.executeQuery("SELECT " + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL(tablename) +
					" WHERE " + DBKernel.delimitL(feldname) + "='" + feldVal + "'");
			if (rs != null && rs.last() && rs.getRow() == 1) {
				result = rs.getInt(1);
			}
		}
		catch (Exception e) {MyLogger.handleException(e);}
		return result;
	}
	private ResultSet getResultSet(final Statement anfrage, final String sql, final boolean suppressWarnings) {
		    ResultSet ergebnis = null;
		    try {
		      ergebnis = anfrage.executeQuery(sql);
		      ergebnis.first();
		    }
		    catch (Exception e) {
		      if (!suppressWarnings) {
		    	  MyLogger.handleMessage(sql);
		        MyLogger.handleException(e);
		      }
		    }
		    return ergebnis;
	}

	private void go4Dateispeicher(final Statement anfrage, final String datumAb) {
	    try {	    	
		    String sql = "SELECT * FROM " + DBKernel.delimitL("DateiSpeicher") +
				" WHERE " + DBKernel.delimitL("Zeitstempel") + " > '" + datumAb + "' ORDER BY " + DBKernel.delimitL("Zeitstempel") + " ASC";
	        ResultSet rs = getResultSet(anfrage, sql, false);
				if (rs.first()) {
				      sql = "INSERT INTO " + DBKernel.delimitL("DateiSpeicher") +
				      " (" + DBKernel.delimitL("Zeitstempel") + "," + DBKernel.delimitL("Tabelle") + "," + DBKernel.delimitL("Feld") + "," +
				      DBKernel.delimitL("TabellenID") + "," +
				      DBKernel.delimitL("Dateiname") + "," + DBKernel.delimitL("Dateigroesse") + "," + DBKernel.delimitL("Datei") + ")" +
				      " VALUES (?,?,?,?,?,?,?);";
			    	PreparedStatement psmt = DBKernel.getDBConnection().prepareStatement(sql);
					do {
						String tablename = rs.getString("Tabelle");
						Integer tID = rs.getInt("TabellenID");
						int blobSize = rs.getInt("Dateigroesse");
				  	    psmt.clearParameters();
				  	    psmt.setTimestamp(1, rs.getTimestamp("Zeitstempel"));
				  	    psmt.setString(2, tablename);
				  	    psmt.setString(3, rs.getString("Feld"));
				    	psmt.setInt(4, convertID(tablename, tID, false));
				  	    psmt.setString(5, rs.getString("Dateiname"));
				  	    psmt.setInt(6, blobSize);
			            // Get as a BLOB
			            Blob aBlob = rs.getBlob("Datei");
			            byte[] b = aBlob.getBytes(1, (int) aBlob.length());
				  	    InputStream bais = new ByteArrayInputStream(b);
				  	    psmt.setBinaryStream(7, bais, b.length);
				  	    psmt.executeUpdate();
				  	    bais.close();
					} while (rs.next());
			  	    psmt.close();
				}  	
	    }
	    catch (Exception e) {
	    	MyLogger.handleException(e);
	    }
	}
	private void go4ChangeLog(final Statement anfrage, final String datumAb) {
	    String sql = "SELECT * FROM " + DBKernel.delimitL("ChangeLog") +
		" WHERE " +  (isMertens && DBVersion.equals("1.3.7") ? DBKernel.delimitL("ID") + " > 169239 AND " : "") + DBKernel.delimitL("Zeitstempel") + " > '" + datumAb + "' ORDER BY " + DBKernel.delimitL("ID") + " ASC"; // Zeitstempel
	    //System.out.println(sql);
	    ResultSet rs = getResultSet(anfrage, sql, false);
	    try {
			if (rs != null && rs.first()) {
				do {
					String tablename = rs.getString("Tabelle");
					Integer tID = rs.getInt("TabellenID");
					Integer clID = rs.getInt("ID");
					Timestamp ts = rs.getTimestamp("Zeitstempel");
					/*
					if (isMertens && DBVersion.equals("1.3.7") && tablename.equals("Literatur")) {
			    		Object o = rs.getObject("Alteintrag");
			    		System.out.println(clID + "\t" + ts + "\t" + rs.getString("Username") + "\t" + tablename + "\t" + tID + "\t" + o);
					}
					*/
					// Ausnahmen, aber mal schauen. In jedem Fall auf Agenzien und Matrices achten!
					// die anderen d�rften noch keine Daten enthalten...
					MyTable myT = DBKernel.myList.getTable(tablename);
					if (myT == null
							|| tablename.equals("Matrices")
							|| tablename.equals("Verpackungsmaterial")
							|| tablename.equals("Agenzien")
							|| tablename.equals("Methoden")
							|| tablename.equals("MatrixEigenschaften") // gibts ja gar nicht mehr, oder?
						//	|| tablename.equals("Prozess_Workflow")
						//	|| tablename.equals("Zutatendaten")
							|| tablename.equals("Krankheitsbilder")
							|| tablename.equals("Literatur") && tID <= 239
						//	|| tablename.equals("ComBaseImport")
							|| isHammerl && tablename.equals("Aufbereitungs_Nachweisverfahren") && tID == 24 && DBVersion.equals("1.4.2") // Hammerl hatte nicht die zuletzt gemergte DB eingespielt... ausserdem ohnehin keine relevante �nderung
							|| isBoehnlein && tablename.equals("Einheiten") && tID == 88 && DBVersion.equals("1.3.9")
						//	|| tablename.equals("SonstigeParameter")
							|| (tablename.equals("Versuchsbedingungen") &&	tID == 926 && isFalenski)
							|| (tablename.equals("Versuchsbedingungen") && (tID < 4 || (tID >= 1847 && tID <= 2264 && !(isBoehnlein && DBVersion.equals("1.4.2"))))) // das sind Combase Eintr�ge, da sollte nix ge�ndert werden... B�hnlein hat aber rumgfummelt und G�tescore vergeben und Agensdetail eingetragen
							|| (tablename.equals("Messwerte") && (tID < 19 || tID >= 11249 && tID <= 12244)) // das sind Combase Eintr�ge, da sollte nix ge�ndert werden...
							|| (isWese && tablename.equals("Kontakte") && tID == 129 && (DBVersion.equals("1.3.6") || DBVersion.equals("1.3.7"))) // nicht l�schen, der leere Datensatz wird von Mertens benutzt beim Merge von 1.3.6 nach 1.3.7
							|| (isWese && tablename.equals("Versuchsbedingungen") && tID == 1107 && (DBVersion.equals("1.3.6") || DBVersion.equals("1.3.7"))) // nicht �ndern, versehentlich editiert von Wese
							) {
						MyLogger.handleMessage("not merged: " + untersuchteDB + "\t" + tablename + "\t" + tID);
					}
					else {
						if (!usedTs.containsKey(myT)) {
							usedTs.put(myT, myT);
						}
					    sql = "SELECT ARRAY_AGG(" + DBKernel.delimitL("Alteintrag") + " ORDER BY " + DBKernel.delimitL("Zeitstempel") + " ASC)," +
					    "ARRAY_AGG(" + DBKernel.delimitL("Zeitstempel") + " ORDER BY " + DBKernel.delimitL("Zeitstempel") + " ASC)," + 
					    "ARRAY_AGG(" + DBKernel.delimitL("Username") + " ORDER BY " + DBKernel.delimitL("Zeitstempel") + " ASC)" + 
					    " FROM " + DBKernel.delimitL("ChangeLog") +
					    	" WHERE " + DBKernel.delimitL("TabellenID") + " = " + tID +
					    	" AND " + DBKernel.delimitL("Tabelle") + " = '" + tablename + "'" +
					    	//" AND " + DBKernel.delimitL("Zeitstempel") + " >= '" + ts + "'" + // Das hier ist irgendwie bl�d... da gibts tats�chlich manchmal identische Zeiten... wieso auch immer...Import von Exceltabellen??? Konsequenz ist jedenfalls: Ups... idConverter contains Versuchsbedingungen_Sonstiges_1188 already...1191	1192! Der Algorithmus macht �fter hintereinander INSERT INTO, weil der NULL Alteintrag (=Ersteintrag in die DB) denselben Zeitstempel hat wie der bereits zum erstenmal editierte... 
					    	" AND " + DBKernel.delimitL("ID") + " >= '" + clID + "'" + // mit den IDs das kann nicht klappen f�r bereits zusammengef�hrte DB-Eintr�ge! Stichwort: Konflikt! Der Konflikt w�rde aber auch drohen bei der Zeitstempel Variante, oder? Glaub schon! 
					    	" GROUP BY " + DBKernel.delimitL("Tabelle") + "," + DBKernel.delimitL("TabellenID");
					    ResultSet rs2 = getResultSet(anfrage, sql, false);
					    if (rs2 != null && rs2.first()) {
						    Array a = rs2.getArray(1);
						    if (a != null) {
							    Object[] alteintraege = (Object[])a.getArray();
							    if (!doAlteintraege(alteintraege, tablename, tID, myT, anfrage, ts)) {
							    	MyLogger.handleMessage("doAlteintraege: Wasn nu los???");
							    }
						    }
						    else {
						    	MyLogger.handleMessage("Keine Eintr�ge f�r a?!?");					    	
						    }
					    }
					    else {
					    	MyLogger.handleMessage("Keine Eintr�ge?!?");
					    }

					    	PreparedStatement ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("ChangeLog") +
						      		" (" + DBKernel.delimitL("ID") + ", " + DBKernel.delimitL("Zeitstempel") + ", " + DBKernel.delimitL("Username") + ", " +
						      		DBKernel.delimitL("Tabelle") + ", " + DBKernel.delimitL("TabellenID") + ", " +
						      		DBKernel.delimitL("Alteintrag") + ") VALUES (NEXT VALUE FOR " + DBKernel.delimitL("ChangeLogSEQ") + ", ?,?,?,?,?)");
						    	ps.setTimestamp(1, ts);
						    	ps.setString(2, rs.getString("Username"));
						    	ps.setString(3, tablename);
						    	ps.setInt(4, convertID(tablename, tID, false));
						    	if (tablename.equals("SonstigeParameter") && tID == 121) {
									System.err.println("ps.setInt(4,\t" + tablename + "\t" + tID + "\t" + convertID(tablename, tID, false));
								}
						    	
					    		Object[] modO = doFields(null, myT, (Object[]) rs.getObject("Alteintrag"));

						    	if (modO == null) {
									ps.setNull(5, java.sql.Types.OTHER);
								} else {
									ps.setObject(5, modO);
								}
						    	ps.execute();		
								if (ps.getUpdateCount() != 1) {
									System.err.println("H�2�2��h, was los huier?" + ps.getUpdateCount());
								}
					}
									    	
				} while (rs.next());
			}		
	    }
	    catch (Exception e) {
	    	MyLogger.handleException(e);
	    }
	}
	private Integer convertID(final String tablename, final Integer id, final boolean pkBetroffen) {
		Integer result = id;
		if (id != null) {
	    	if (idConverter.containsKey(tablename + "_" + id)) {
				result = idConverter.get(tablename + "_" + id);
			}
	    	//else System.err.println("idConverter containsKey " + tablename + "_" + id + " nicht");
	    	if (pkBetroffen) {
					checkUsedIDs(tablename, result);    		
	    	}
		}
		return result;
	}
	private void checkUsedIDs(final String tablename, final Integer lastPrimaryID) {
	  	if (!usedIDs.containsKey(tablename + "_" + lastPrimaryID)) {
			usedIDs.put(tablename + "_" + lastPrimaryID, untersuchteDB);
		} else if (!usedIDs.get(tablename + "_" + lastPrimaryID).equals(untersuchteDB)) {
	  		System.err.println("ID von verschiedenen DBs benutzt, Achtung: Konfliktgefahr!!!!" + untersuchteDB + "\t" + tablename + "\t" + lastPrimaryID + "\t" + usedIDs.get(tablename + "_" + lastPrimaryID));
	  	}    				
	
	}	
    private void checkeDoppeltVergebeneDKZs(final Statement anfrage) {
    	System.err.println("checkeDoppeltVergebeneDKZs - Start");
		LinkedHashMap<String, MyTable> myTables = DBKernel.myList.getAllTables();
		Hashtable<Integer, String> hash = new Hashtable<Integer, String>();
		for(String key : myTables.keySet()) {
			MyTable myT = myTables.get(key);
			String tn = myT.getTablename();
			System.out.println(tn);
			MyTable[] foreignFields = myT.getForeignFields();
			if (foreignFields != null) {
				for (int i=0; i<foreignFields.length; i++) {
					if (foreignFields[i] != null && foreignFields[i].getTablename().equals("DoubleKennzahlen")) {
						ResultSet rs = DBKernel.getResultSet(anfrage, "SELECT " + DBKernel.delimitL(myT.getFieldNames()[i]) +
								"," + DBKernel.delimitL("ID") + " FROM " + DBKernel.delimitL(myT.getTablename()), false);
						try {
						    if (rs != null && rs.first()) {
						    	do {
							    	if (rs.getObject(1) != null) {
							    		Integer dkzID = rs.getInt(1);
							    		if (hash.containsKey(dkzID)) {
											System.err.println("DKZ doppelt vergeben: " + dkzID + "\t" + tn + "_" + rs.getString(2) + "\t" + hash.get(dkzID));
										} else {
											hash.put(dkzID, tn + "_" + rs.getString(2));
										}
							    	}
						    	} while(rs.next());
						    	rs.close();
						    }
						}
						catch (Exception e) {e.printStackTrace();}
					}
				}
			}
		}
    	System.err.println("checkeDoppeltVergebeneDKZs - Fin");
    }
    private void go4Probleme(final String theDBVersion, final Statement anfrage, final String tablename, final int idAb, final boolean insertEmpty) {
		if (theDBVersion.equals("1.3.9")) {
			// bei den gesch�tzten Modellen d�rfte das nicht passiert sein, oder???? Checken nicht vergessen!!!
			//  || tablename.equals("Literatur") && tID >= 587)
			ResultSet rs = DBKernel.getResultSet(anfrage, "SELECT * FROM " + DBKernel.delimitL(tablename) + " WHERE " + DBKernel.delimitL("ID") + " >= " + idAb, false);
			try {
			    if (rs != null && rs.first()) {
			    	do {
						//System.err.println("ok, hier ist die TriggerScheisse passiert, wo wegen Statup (data-in-motion) der Trigger abgeschaltet worden ist: " + rs.getObject("ID"));
				    	if (rs.getObject("ID") != null) {
					    	if (insertEmpty) {
					    		insertNewEmptyDatensatz(tablename, rs.getInt("ID"));		
					    	}
					    	else {
								Object[] eintragNeu = new Object[rs.getMetaData().getColumnCount()]; 
								for (int i=0;i<eintragNeu.length;i++) {
									eintragNeu[i] = rs.getObject(i+1);
								}			
								insertRowDeNovo(tablename, eintragNeu, rs.getInt("ID"));
					    	}
				    	}
			    	} while(rs.next());
			    	rs.close();
			    }
			}
			catch (Exception e) {e.printStackTrace();}
		}
    }
    private void insertRowDeNovo(final String tablename, final Object[] eintragNeu, final Integer tID) {
    	MyTable myT = DBKernel.myList.getTable(tablename);
		PreparedStatement ps;
		try {
			ps = DBKernel.getDBConnection().prepareStatement(myT.getInsertSQL1(), Statement.RETURN_GENERATED_KEYS);
			Object[] newF = doFields(ps, myT, eintragNeu); 
			if (newF != null) {
				//System.err.println(newF[1] + "\t" + ps.toString());
				if (ps.executeUpdate() > 0) {
					Integer lastID = DBKernel.getLastInsertedID(ps);
					String username = isMertens ? "mertens" : isBoehnlein ? "b�hnlein" : isHammerl ? "hammerl" : isWese ? "wese" : "";
					/*
			    	DBKernel.sendRequest("INSERT INTO " + DBKernel.delimitL("ChangeLog") + " (" + DBKernel.delimitL("Zeitstempel") + ", " + DBKernel.delimitL("Username") + ", " +
				      		DBKernel.delimitL("Tabelle") + ", " + DBKernel.delimitL("TabellenID") + ", " +
				      		DBKernel.delimitL("Alteintrag") + ") VALUES (" + System.currentTimeMillis() + ",'" + username + "','" + tablename + "'," + lastID + ",NULL)", false);
				      		*/
			    	PreparedStatement ps2 = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("ChangeLog") +
				      		" (" + DBKernel.delimitL("ID") + ", " + DBKernel.delimitL("Zeitstempel") + ", " + DBKernel.delimitL("Username") + ", " +
				      		DBKernel.delimitL("Tabelle") + ", " + DBKernel.delimitL("TabellenID") + ", " +
				      		DBKernel.delimitL("Alteintrag") + ") VALUES (NEXT VALUE FOR " + DBKernel.delimitL("ChangeLogSEQ") + ", ?, ?, '" + tablename + "'," + lastID + ", NULL)");
				    	ps2.setTimestamp(1, new Timestamp(0L)); // System.currentTimeMillis()
				    	ps2.setString(2, username);
				    	ps2.execute();
				    if (lastInsertedID.get(tablename) == null || lastInsertedID.get(tablename) < tID) {
						lastInsertedID.put(tablename, tID);
					}				    	
					if (idConverter.containsKey(tablename + "_" + tID)) {
						System.err.println("Ups... idConverter contains " + tablename + "_" + tID + " already..." + idConverter.get(tablename + "_" + tID) + "\t" + lastID);
					}
					if (idConverterReverse.containsKey(tablename + "_" + lastID)) {
						System.err.println("Ups... idConverterReverse contains " + tablename + "_" + lastID + " already..." + idConverterReverse.get(tablename + "_" + lastID) + "\t" + tID);
					}
					idConverter.put(tablename + "_" + tID, lastID);			
					idConverterReverse.put(tablename + "_" + lastID, tID);			
					checkUsedIDs(tablename, lastID);
					/*
		    		if (tablename.equals("Literatur")) { // 174864 175072 //  && (cid == 583 || cid == 584 || cid == 585)
		    			System.err.println("WEb\t" + tID + "\t" + lastID);
		    		}
		    		*/				    		
				} 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }
    private String getUsername() {
    	if (isFalenski) {
			return "falenski";
		} else if (isMertens) {
			return "mertens";
		} else if (isWese) {
			return "wese";
		} else if (isHammerl) {
			return "hammerl";
		} else if (isBoehnlein) {
			return "b�hnlein";
		} else {
			return "";
		}
    }
    private boolean checkIfOthersAlreadyEditedUpdates(final Statement anfrage, final String tablename, final Integer id, final boolean anyway) {
    	String username = getUsername();
		String tt = "";
		boolean otherUsers = false;
		LinkedHashMap<Integer, Vector<String>> v = DBKernel.getUsersFromChangeLog(anfrage, tablename, id, null, true);
	  	for (Map.Entry<Integer, Vector<String>> entry : v.entrySet()) {
			for (String entr : entry.getValue()) {
				tt += entr + "\n"; 
				if (!entr.startsWith(username)) {
					otherUsers = true;
				}
			}
	  	}			
	  	boolean result = false;
	  	if ((tt.length() > 0 || v.size() == 0) && (otherUsers || anyway)) { // v.size() == 0: falls nix in den Logs steht, trotzdem checken! K�nnten ja Ur-defad-Daten ver�ndert worden sein.
	  		if (!checkIfOthersAlreadyEditedUpdates.containsKey(tablename + "_" + id)) {
		  		System.err.print("checkIfOthersAlreadyEditedUpdates...  " + tablename + "\t" + id + "\n" + tt);	 
		  		checkIfOthersAlreadyEditedUpdates.put(tablename + "_" + id, id);
	  		}
	  		result = true;
	  	}
	  	return result;
    }
}
