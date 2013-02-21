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
/*
 * Created by JFormDesigner on Thu Aug 12 23:40:52 CEST 2010
 */

package org.hsh.bfr.db.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.hsh.bfr.db.Backup;
import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.MyTable;
import org.hsh.bfr.db.MyTrigger;
import org.hsh.bfr.db.UpdateChecker;
import org.hsh.bfr.db.Users;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.gui.dbtree.MyDBTree;
import org.hsh.bfr.db.imports.GeneralXLSImporter;
import org.hsh.bfr.db.imports.ICD10Importer;
import org.hsh.bfr.db.imports.InfoBox;
import org.hsh.bfr.db.imports.KrankheitenFPAImporter;
import org.hsh.bfr.db.imports.KrankheitenVETImporter;
import org.hsh.bfr.db.imports.MyRisImporter;
import org.hsh.bfr.db.imports.MySQLImporter;
import org.hsh.bfr.db.imports.SQLScriptImporter;
import org.hsh.bfr.db.imports.SymptomeFPAImporter;
import org.hsqldb.lib.MD5;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Armin Weiser
 */
public class Login extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean firstRun;
	
	public Login() {
		//DBKernel.prefs = Preferences.userNodeForPackage(this.getClass());		
	}
	public Login(final boolean firstRun) {
		this.firstRun = firstRun;
		initComponents();	
		DBKernel.login = this;
		//DBKernel.prefs = Preferences.userNodeForPackage(this.getClass());
		String lastUser = DBKernel.prefs.get("LAST_USER_LOGIN", "");
		String lastDBPath = DBKernel.prefs.get("LAST_DB_PATH", DBKernel.HSHDB_PATH);
		textField1.setText(lastUser);
		textField2.setText(lastDBPath);
		if (DBKernel.debug && lastUser.equals(DBKernel.getTempSA(lastDBPath))) {
			passwordField1.setText(DBKernel.getTempSAPass(lastDBPath));
			//this.setTitle(textField1.getFont().getName() + " - " + textField1.getFont().getSize());
		}
	}

	private void okButtonActionPerformed(final ActionEvent e) {
		DBKernel.HSHDB_PATH = textField2.getText();
		  if (DBKernel.isHsqlServer(DBKernel.HSHDB_PATH)) {
			DBKernel.isServerConnection = true;
		} else {
			  DBKernel.isServerConnection = false;
			  if (!DBKernel.HSHDB_PATH.endsWith(System.getProperty("file.separator"))) {
				DBKernel.HSHDB_PATH += System.getProperty("file.separator");
			}
		  }
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			MyList myList = loadDB();
		  	if (myList != null) {
		  		DBKernel.saveUP2PrefsTEMP(DBKernel.HSHDB_PATH);
		  		/*
			  	DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Infotabelle") + " WHERE " + DBKernel.delimitL("Parameter") + " = 'DBuuid'", false);
			  	DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ChangeLog"), false);
			  	*/
		  	}

			//UpdateChecker.check4Updates_148_149(null);
			/*

			DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ModellkatalogParameter") + " WHERE " + DBKernel.delimitL("Modell") + " >= 47 AND " + DBKernel.delimitL("Modell") + " <= 49", false);
			DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Modell_Referenz") + " WHERE " + DBKernel.delimitL("Modell") + " >= 47 AND " + DBKernel.delimitL("Modell") + " <= 49", false);
		  	DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Modellkatalog") + " WHERE " + DBKernel.delimitL("ID") + " >= 47 AND " + DBKernel.delimitL("ID") + " <= 49", false);
		  	DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Literatur") + " WHERE " + DBKernel.delimitL("ID") + " <= 239", false);
			*/
			//MyList myList = loadDB(); UpdateChecker.temporarily(myList);
			/*
			DBKernel.sendRequest("CREATE USER " + DBKernel.delimitL(DBKernel.getTempSA()) +
					" PASSWORD '" + DBKernel.getTempSAPass() + "' ADMIN", false);
			DBKernel.sendRequest("DROP USER " + DBKernel.delimitL("SA"), false);
			*/
			/*
			DBKernel.mergeIDs("Station", 786, 769);
			DBKernel.mergeIDs("Station", 770, 763);
			DBKernel.mergeIDs("Station", 766, 11);
			DBKernel.mergeIDs("Station", 473, 484);
			DBKernel.mergeIDs("Station", 783, 28);
			DBKernel.mergeIDs("Station", 784, 30);
			*/
		}
		finally {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void cancelButtonActionPerformed(final ActionEvent e) {
		this.dispose();
	}
	private void changePasswort(final MyDBTable myDB, String username, final String newPassword) throws Exception {
    	boolean isAdmin = DBKernel.isAdmin();
    	if (isAdmin) {
			DBKernel.sendRequest("SET PASSWORD '" + newPassword + "';", false); // MD5.encode(newPassword, "UTF-8")
    	}
		else {
    		DBKernel.closeDBConnections(false);
    		DBKernel.getDefaultAdminConn();
    		DBKernel.sendRequest("ALTER USER " + DBKernel.delimitL(username) + " SET PASSWORD '" + newPassword + "';", false); // MD5.encode(newPassword, "UTF-8")
    		DBKernel.closeDBConnections(false);
			myDB.initConn(username, newPassword); // MD5.encode(newPassword, "UTF-8")
    	}		
	}
	private MyList loadDB() {
	    MyDBTable myDB = null;
	    MyDBTree myDBTree = null;
		MyList myList = null;
		boolean doUpdates = false;
		try {
			// Datenbank schon vorhanden?
			boolean noDBThere = !DBKernel.isServerConnection && !DBKernel.DBFilesDa();

			myDB = new MyDBTable();
			// Login fehlgeschlagen
			String username = textField1.getText();
			String password = new String(passwordField1.getPassword()); // DBKernel.isStatUp ? new String(passwordField1.getPassword()) : MD5.encode(new String(passwordField1.getPassword()), "UTF-8");
			//MD5.encode(password, "UTF-8");
			if (!myDB.initConn(username, password)) {
				if (DBKernel.passFalse) {
					String md5Password = MD5.encode(password, "UTF-8");
					if (!myDB.initConn(username, md5Password)) {
						if (DBKernel.passFalse) {
							passwordField1.setBackground(Color.RED);
							passwordField2.setBackground(Color.WHITE);
							passwordField3.setBackground(Color.WHITE);
							passwordField1.requestFocus();																				
						}
						return null;
					}
					else {
						changePasswort(myDB, username, password);
					}
				}
			}
						
			//DBKernel.sendRequest("ALTER USER " + DBKernel.delimitL(textField1.getText()) + " SET PASSWORD ''", false);
			//DBKernel.sendRequest("SET DATABASE TRANSACTION CONTROL MVCC", false);
			
			// Login succeeded
			if (!DBKernel.isServerConnection) {
				long fs = DBKernel.getFileSize(DBKernel.HSHDB_PATH + "DB.data");
		    	if (fs > 200000000) {
					InfoBox ib = new InfoBox(this, "big data file (" + fs / 1000000 + ")!!! Bitte mal bei Armin melden!\n(Tel.: 030-18412 2118, E-Mail: armin.weiser@bfr.bund.de)", true, new Dimension(750, 300), null, true);
					ib.setVisible(true);    				  										        									    		
		    	}
				MyLogger.handleMessage(username + " logged in!" + "\nDB.data (size): " + fs);
				if (fs >= 500*1024*1024) { // 500MB
			    	MyLogger.handleMessage("vor CHECKPOINT DEFRAG: " + fs);
			    	DBKernel.sendRequest("CHECKPOINT DEFRAG", false);
			    	System.gc();
			    	MyLogger.handleMessage("nach CHECKPOINT DEFRAG: " + DBKernel.getFileSize(DBKernel.HSHDB_PATH + "DB.data"));
				}				
			}
			DBKernel.prefs.put("LAST_USER_LOGIN", username);
			DBKernel.prefs.put("LAST_DB_PATH", DBKernel.HSHDB_PATH);
			MyLogger.handleMessage("HSHDB_PATH: " + DBKernel.HSHDB_PATH);
			// Datenbank erstellen
			if (noDBThere) { // soll erstmal nicht mehr erlaubt sein, UPDATE Funktionalit�t ist jetzt angesagt
			}
			else if (!DBKernel.isServerConnection) { // !DBKernel.isKrise && 
				int dbAlt = DBKernel.isDBVeraltet(this);
				if (dbAlt == JOptionPane.YES_OPTION) {// UPDATE Funktionalit�t ist jetzt angesagt
					doUpdates = true;
				}
				else if (dbAlt == JOptionPane.CANCEL_OPTION) {
					DBKernel.closeDBConnections(false);
					return null;
				}
			}
			
			// Passwort �ndern
			if (checkBox1.isSelected()) {
				if (passwordField2.getPassword().length >= 0) {
					String newPassword = new String(passwordField2.getPassword());
					if (newPassword.length() == 0) { // Passw�rter d�rfen nicht leer sein!
						passwordField1.setBackground(Color.WHITE);
						passwordField2.setBackground(Color.RED);
						passwordField3.setBackground(Color.RED);
						passwordField2.requestFocus();
						return null;
					}
					if (newPassword.equals(new String(passwordField3.getPassword()))) {
						changePasswort(myDB, username, newPassword);
					}
					else {
						passwordField1.setBackground(Color.WHITE);
						passwordField2.setBackground(Color.WHITE);
						passwordField3.setBackground(Color.RED);
						passwordField3.requestFocus();
						return null;
					}
				}
				else {
					passwordField1.setBackground(Color.WHITE);
					passwordField2.setBackground(Color.RED);
					passwordField3.setBackground(Color.WHITE);
					passwordField2.requestFocus();
					return null;
				}
			}
			
			// Login succeeded: GUI aufbauen
			myDBTree = new MyDBTree();
			myList = new MyList(myDB, myDBTree);
			DBKernel.myList = myList;
			MainFrame mf = new MainFrame(myList);
			DBKernel.mainFrame = mf;
			
			// Datenbank f�llen			
			if (noDBThere) {
				DBKernel.importing = true;
				if (!DBKernel.debug || !DBKernel.createNewFirstDB) { // das komplette neu erstellen der DB sollte abgel�st werden durch "UPDATE"-Funktionalit�t (�nderungen einbauen in die vorhandene DB...)
					File temp = DBKernel.getCopyOfInternalDB();
					if (!Backup.doRestore(myDB, temp, true)) { // Passwort hat sich ver�ndert innerhalb der 2 beteiligten Datenbanken...
						passwordField1.setBackground(Color.RED);
						passwordField2.setBackground(Color.WHITE);
						passwordField3.setBackground(Color.WHITE);
						passwordField1.requestFocus();					
						return null;
					}

					loadMyTables(myList, null);
				}
				else {
					loadMyTables(myList, myDB);
					String folder = "/org/hsh/bfr/db/res/"; //"I:/SourceCode/Data/SiLeBAT_DB/InitDB/";
					if (!DBKernel.isKrise) {
						DBKernel.sendRequest("INSERT INTO " + DBKernel.delimitL("Infotabelle") +
								" (" + DBKernel.delimitL("Parameter") + "," + DBKernel.delimitL("Wert") + ") VALUES ('DBVersion','" + DBKernel.DBVersion + "')", false);

						new KrankheitenVETImporter().doImport(folder + "VET_Krankheitsarten.xls", mf.getProgressBar(), false);
						new SymptomeFPAImporter().doImport(folder + "Katalog-Symptome-FPAdvisor.xls", mf.getProgressBar(), false);
						new KrankheitenFPAImporter().doImport(folder + "Krankheiten_FPAdvisor.xls", mf.getProgressBar(), false);
						
						new GeneralXLSImporter().doImport(folder + "Untersuchungsaemter.xls", mf.getProgressBar(), false);
						//new GeneralXLSImporter().doImport(folder + "Adressen-DB43-1.xls", mf.getProgressBar(), false);
						//new GeneralXLSImporter().doImport(folder + "Adressen-DB43-2.xls", mf.getProgressBar(), false);
						
						new GeneralXLSImporter().doImport(folder + "Basis-Agenzien_new-20111209.xls", mf.getProgressBar(), false);						
						new GeneralXLSImporter().doImport(folder + "Nachweisverfahren.xls", mf.getProgressBar(), false);
						//new GeneralXLSImporter().doImport(folder + "Matrices_BLS-Liste.xls", mf.getProgressBar(), false);
						new GeneralXLSImporter().doImport(folder + "Matrices_BLS-Liste_inkl_FA.xls", mf.getProgressBar(), false);

						//new GeneralXLSImporter().doImport(folder + "Labore.xls", mf.getProgressBar(), false);

						new GeneralXLSImporter().doImport(folder + "ProzessID_Cat_Subcat_Process.xls", mf.getProgressBar(), false);

						new MyRisImporter().doImport(folder + "risMS.txt", mf.getProgressBar(), false);

						new ICD10Importer().doImport(folder + "x1gma2011/Klassifikationsdateien/", mf.getProgressBar(), false);

						new GeneralXLSImporter().doImport("/org/hsh/bfr/db/res/Methodiken.xls", mf.getProgressBar(), false);
						//new GeneralXLSImporter().doImport("/org/hsh/bfr/db/res/Modell_Tabellen.xls", mf.getProgressBar(), false);

						try {    	
					    	importEinheiten();
					    	importSP();			    
					    					    
							new SQLScriptImporter().doImport("/org/hsh/bfr/db/res/StatupInserts.sql", null, false);
							importKataloge();
							
							new GeneralXLSImporter().doImport(folder + "ComBaseImport.xls", mf.getProgressBar(), false);						
							new MySQLImporter().doImport("", null, false);	
							new GeneralXLSImporter().doImport(folder + "Versuchsbedingungen.xls", mf.getProgressBar(), false);	
					    } 
					    catch (Exception e) {
					    	MyLogger.handleException(e);
						}
					    
						new GeneralXLSImporter().doImport("/org/hsh/bfr/db/res/Verpackungsmaterial.xls", mf.getProgressBar(), false);
						UpdateChecker.dropJansTabellen();
						
						DBKernel.doMNs(DBKernel.myList.getTable("Methoden"));
				        DBKernel.doMNs(DBKernel.myList.getTable("Labore"));
					    DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ChangeLog"), false); //  + " WHERE " + DBKernel.delimitL("ID") + " < 45000"	
					    
						DBKernel.setDBVersion(DBKernel.DBVersion);
					}

				    MyLogger.handleMessage("Fertig!");
				}
				DBKernel.importing = false;
			}
			else {
				/*
				DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Zutatendaten") +
						" ALTER COLUMN " + DBKernel.delimitL("#Units") + " DOUBLE", false);
				DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("ProzessWorkflow") +
						" DROP COLUMN " + DBKernel.delimitL("#Chargenunits"), false);
				DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("ProzessWorkflow") +
						" DROP COLUMN " + DBKernel.delimitL("Unitmenge"), false);
				DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("ProzessWorkflow") +
						" DROP COLUMN " + DBKernel.delimitL("UnitEinheit"), false);
						*/
				loadMyTables(myList, null);		
				/*
				// speziell f�r Wese, die schon eine Teil-1.3.9 DB hatte
				boolean dl1 = DBKernel.dontLog;
				DBKernel.dontLog = true;
		  		UpdateChecker.check4Updates_138_139(myList, true); 
				DBKernel.dontLog = dl1;
				*/
				//new MySQLImporter().doImport("", null, false);	
				if (doUpdates) {
					boolean dl = DBKernel.dontLog;
					DBKernel.dontLog = true;
					try {
					  	boolean isAdmin = DBKernel.isAdmin();
					  	if (!isAdmin) {
					  		DBKernel.closeDBConnections(false);
					  		DBKernel.getDefaultAdminConn();
					  	}
					  	String dbVersion = DBKernel.getDBVersion();
					  	if (dbVersion.equals("1.3.5")) {
					  		UpdateChecker.check4Updates_135_136(myList); 
					  		DBKernel.setDBVersion("1.3.6");
					  	}
					  	else if (dbVersion.equals("1.3.6")) {
					  		UpdateChecker.check4Updates_136_137(myList); 
					  		DBKernel.setDBVersion("1.3.7");
					  	}
					  	else if (dbVersion.equals("1.3.7")) {
					  		UpdateChecker.check4Updates_137_138(myList); 
					  		DBKernel.setDBVersion("1.3.8");
					  	}
					  	else if (dbVersion.equals("1.3.8")) {
					  		UpdateChecker.check4Updates_138_139(myList); 
					  		DBKernel.setDBVersion("1.3.9");
					  	}
					  	else if (dbVersion.equals("1.3.9")) {
					  		UpdateChecker.check4Updates_139_140(myList); 
					  		DBKernel.setDBVersion("1.4.0");
					  	}
					  	else if (dbVersion.equals("1.4.0")) {
					  		UpdateChecker.check4Updates_140_141(myList); 
					  		DBKernel.setDBVersion("1.4.1");
					  	}
					  	else if (dbVersion.equals("1.4.1")) {
					  		UpdateChecker.check4Updates_141_142(myList); 
					  		DBKernel.setDBVersion("1.4.2");
					  	}
					  	else if (dbVersion.equals("1.4.2")) {
					  		UpdateChecker.check4Updates_142_143(myList); 
					  		DBKernel.setDBVersion("1.4.3");
					  	}
					  	else if (dbVersion.equals("1.4.3")) {
					  		UpdateChecker.check4Updates_143_144(myList); 
					  		DBKernel.setDBVersion("1.4.4");
					  	}
					  	else if (dbVersion.equals("1.4.4")) {
					  		UpdateChecker.check4Updates_144_145(myList); 
					  		DBKernel.setDBVersion("1.4.5");
					  	}
					  	/*
					  	else { // dbVersion == null || dbVersion == unbekannt
							InfoBox ib = new InfoBox(this, "Deine DB ist zu alt...\nBitte mal bei Armin melden!\n(Tel.: 030-18412 2118, E-Mail: armin.weiser@bfr.bund.de)", true, new Dimension(750, 300), null, true);
							ib.setVisible(true); 
							mf.dispose();
							return myList;
					  	}
					  	*/
					  	if (DBKernel.getDBVersion().equals("1.4.5")) {
					  		UpdateChecker.check4Updates_145_146(myList); 
					  		DBKernel.setDBVersion("1.4.6");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.4.6")) {
					  		UpdateChecker.check4Updates_146_147(myList); 
					  		DBKernel.setDBVersion("1.4.7");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.4.7")) {
					  		UpdateChecker.check4Updates_147_148(myList); 
					  		DBKernel.setDBVersion("1.4.8");
					  	}					  	
					  	if (DBKernel.getDBVersion().equals("1.4.8")) {
					  		UpdateChecker.check4Updates_148_149(myList);
					  		DBKernel.setDBVersion("1.4.9");
					  	}					  	
					  	if (DBKernel.getDBVersion().equals("1.4.9")) {
					  		UpdateChecker.check4Updates_149_150(myList); 
					  		DBKernel.setDBVersion("1.5.0");
					  	}
					  	
					  	if (DBKernel.getDBVersion().equals("1.5.0")) {
					  		UpdateChecker.check4Updates_150_151(myList); 
					  		DBKernel.setDBVersion("1.5.1");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.1")) {
					  		UpdateChecker.check4Updates_151_152(myList); 
					  		DBKernel.setDBVersion("1.5.2");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.2")) {
					  		UpdateChecker.check4Updates_152_153(myList); 
					  		DBKernel.setDBVersion("1.5.3");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.3")) {
					  		UpdateChecker.check4Updates_153_154(myList); 
					  		DBKernel.setDBVersion("1.5.4");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.4")) {
					  		UpdateChecker.check4Updates_154_155(myList); 
					  		DBKernel.setDBVersion("1.5.5");
					  	}
					  	
						DBKernel.closeDBConnections(false);
					}
					catch (Exception e) {e.printStackTrace();DBKernel.dontLog = dl;return myList;}
					DBKernel.dontLog = dl;
					loadDB();		
					return myList;
				}
				/*
				else if (DBKernel.isKrise) {
					boolean doAnonymize = false;
					DBKernel.sendRequest("CREATE USER SA PASSWORD '' ADMIN", true);
					DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Kontakte") +
							" ALTER COLUMN " + DBKernel.delimitL("Stra�e") + " RENAME TO " + DBKernel.delimitL("Strasse"), true);
					DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Kontakte") +
							" ALTER COLUMN " + DBKernel.delimitL("E-Mail") + " RENAME TO " + DBKernel.delimitL("EMail"), true);
					DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Kontakte") +
							" ALTER COLUMN " + DBKernel.delimitL("Web-Site") + " RENAME TO " + DBKernel.delimitL("Webseite"), true);
					DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Literatur") + " ALTER COLUMN " + DBKernel.delimitL("Titel") + " SET DATA TYPE VARCHAR(1023)", true);
					DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Literatur") + " ALTER COLUMN " + DBKernel.delimitL("Volume") + " SET DATA TYPE VARCHAR(50)", true);
					DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Literatur") + " ALTER COLUMN " + DBKernel.delimitL("Issue") + " SET DATA TYPE VARCHAR(50)", true);
					DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Literatur") +
							" ADD COLUMN " + DBKernel.delimitL("Webseite") + " VARCHAR(255) BEFORE " + DBKernel.delimitL("Paper"), true);
					DBKernel.sendRequest("ALTER TABLE " + DBKernel.delimitL("Literatur") +
							" ADD COLUMN " + DBKernel.delimitL("Literaturtyp") + " INTEGER BEFORE " + DBKernel.delimitL("Paper"), true);
					if (doAnonymize) {
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Name") + " = ''", false);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Strasse") + " = ''", false);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Hausnummer") + " = ''", false);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Telefon") + " = ''", false);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Fax") + " = ''", false);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("EMail") + " = ''", false);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Webseite") + " = ''", false);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Ansprechpartner") + " = ''", false);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Postfach") + " = ''", false);
						DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Produzent") + " SET " + DBKernel.delimitL("Betriebsnummer") + " = ''", false);
					}
				}
				*/
			}
		    //DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ChangeLog"), false); //  + " WHERE " + DBKernel.delimitL("ID") + " < 45000"
			if (!myList.setSelection(DBKernel.prefs.get("LAST_SELECTED_TABLE", "Versuchsbedingungen"))) {  // Agens_Nachweisverfahren  Agenzien
				myList.setSelection(null);
			}

/*
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_DoubleKennzahlen_U") + " AFTER UPDATE ON " + DBKernel.delimitL("DoubleKennzahlen") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_ToxinUrsprung_U") + " AFTER UPDATE ON " + DBKernel.delimitL("ToxinUrsprung") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_DoubleKennzahlen_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Agenzien") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Codes_Agenzien_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Codes_Agenzien") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Matrices_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Matrices") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Codes_Matrices_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Codes_Matrices") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_ComBaseImport_U") + " AFTER UPDATE ON " + DBKernel.delimitL("ComBaseImport") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Literatur_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Literatur") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Einheiten_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Einheiten") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Methodiken_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Methodiken") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Aufbereitungsverfahren_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Aufbereitungsverfahren") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Nachweisverfahren_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Nachweisverfahren") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Aufbereitungs_Nachweisverfahren_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Aufbereitungs_Nachweisverfahren") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Versuchsbedingungen_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Versuchsbedingungen") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_SonstigeParameter_U") + " AFTER UPDATE ON " + DBKernel.delimitL("SonstigeParameter") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Versuchsbedingungen_Sonstiges_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Versuchsbedingungen_Sonstiges") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Messwerte_Sonstiges_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Messwerte_Sonstiges") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Messwerte_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Messwerte") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Modellkatalog_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Modellkatalog") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_ModellkatalogParameter_U") + " AFTER UPDATE ON " + DBKernel.delimitL("ModellkatalogParameter") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_GeschaetztesModell_Referenz_U") + " AFTER UPDATE ON " + DBKernel.delimitL("GeschaetztesModell_Referenz") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Modell_Referenz_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Modell_Referenz") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_GeschaetzteModelle_U") + " AFTER UPDATE ON " + DBKernel.delimitL("GeschaetzteModelle") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_GeschaetzteParameter_U") + " AFTER UPDATE ON " + DBKernel.delimitL("GeschaetzteParameter") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_GeschaetzteParameterCovCor_U") + " AFTER UPDATE ON " + DBKernel.delimitL("GeschaetzteParameterCovCor") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Sekundaermodelle_Primaermodelle_U") + " AFTER UPDATE ON " + DBKernel.delimitL("Sekundaermodelle_Primaermodelle") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));

		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_DoubleKennzahlen_I") + " AFTER INSERT ON " + DBKernel.delimitL("DoubleKennzahlen") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_ToxinUrsprung_I") + " AFTER INSERT ON " + DBKernel.delimitL("ToxinUrsprung") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_DoubleKennzahlen_I") + " AFTER INSERT ON " + DBKernel.delimitL("Agenzien") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Codes_Agenzien_I") + " AFTER INSERT ON " + DBKernel.delimitL("Codes_Agenzien") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Matrices_I") + " AFTER INSERT ON " + DBKernel.delimitL("Matrices") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Codes_Matrices_I") + " AFTER INSERT ON " + DBKernel.delimitL("Codes_Matrices") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_ComBaseImport_I") + " AFTER INSERT ON " + DBKernel.delimitL("ComBaseImport") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Literatur_I") + " AFTER INSERT ON " + DBKernel.delimitL("Literatur") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Einheiten_I") + " AFTER INSERT ON " + DBKernel.delimitL("Einheiten") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Methodiken_I") + " AFTER INSERT ON " + DBKernel.delimitL("Methodiken") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Aufbereitungsverfahren_I") + " AFTER INSERT ON " + DBKernel.delimitL("Aufbereitungsverfahren") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Nachweisverfahren_I") + " AFTER INSERT ON " + DBKernel.delimitL("Nachweisverfahren") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Aufbereitungs_Nachweisverfahren_I") + " AFTER INSERT ON " + DBKernel.delimitL("Aufbereitungs_Nachweisverfahren") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Versuchsbedingungen_I") + " AFTER INSERT ON " + DBKernel.delimitL("Versuchsbedingungen") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_SonstigeParameter_I") + " AFTER INSERT ON " + DBKernel.delimitL("SonstigeParameter") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Versuchsbedingungen_Sonstiges_I") + " AFTER INSERT ON " + DBKernel.delimitL("Versuchsbedingungen_Sonstiges") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Messwerte_Sonstiges_I") + " AFTER INSERT ON " + DBKernel.delimitL("Messwerte_Sonstiges") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Messwerte_I") + " AFTER INSERT ON " + DBKernel.delimitL("Messwerte") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Modellkatalog_I") + " AFTER INSERT ON " + DBKernel.delimitL("Modellkatalog") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_ModellkatalogParameter_I") + " AFTER INSERT ON " + DBKernel.delimitL("ModellkatalogParameter") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_GeschaetztesModell_Referenz_I") + " AFTER INSERT ON " + DBKernel.delimitL("GeschaetztesModell_Referenz") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Modell_Referenz_I") + " AFTER INSERT ON " + DBKernel.delimitL("Modell_Referenz") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_GeschaetzteModelle_I") + " AFTER INSERT ON " + DBKernel.delimitL("GeschaetzteModelle") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_GeschaetzteParameter_I") + " AFTER INSERT ON " + DBKernel.delimitL("GeschaetzteParameter") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_GeschaetzteParameterCovCor_I") + " AFTER INSERT ON " + DBKernel.delimitL("GeschaetzteParameterCovCor") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
		    System.out.println("CREATE TRIGGER " + DBKernel.delimitL("A_Sekundaermodelle_Primaermodelle_I") + " AFTER INSERT ON " + DBKernel.delimitL("Sekundaermodelle_Primaermodelle") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyDIMTrigger().getClass().getName()));
*/
			this.dispose();
			mf.pack();
			mf.setExtendedState(JFrame.MAXIMIZED_BOTH);
			mf.setVisible(true);
			mf.toFront();
			myDB.grabFocus();//myDB.selectCell(0, 0);
			//getAllMetaData(myList);			
		}
		catch (Exception e) {
			MyLogger.handleException(e);
		}    
		return myList;
	}
	private void importKataloge() {
		try {
	    	PreparedStatement ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Codes_Matrices") +
					" (" + DBKernel.delimitL("CodeSystem") + "," + DBKernel.delimitL("Code") + "," + DBKernel.delimitL("Basis") + ") VALUES (?,?,?)");
			ps.setString(1, "TOP"); ps.setString(2, "01"); ps.setInt(3, 17024); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "02"); ps.setInt(3, 17033); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "03"); ps.setInt(3, 17042); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "04"); ps.setInt(3, 17075); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "05"); ps.setInt(3, 17084); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "06"); ps.setInt(3, 17093); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "07"); ps.setInt(3, 17346); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "08"); ps.setInt(3, 17495); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "09"); ps.setInt(3, 17501); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "10"); ps.setInt(3, 17507); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "11"); ps.setInt(3, 17513); ps.execute();
			ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Agenzien") +
					" (" + DBKernel.delimitL("Agensname") + ") VALUES (?)");
			ps.setString(1, "Bakterien"); ps.execute(); // Bakterien
			ps.setString(1, "Toxine"); ps.execute(); // Toxine
			ps.setString(1, "Hepatitis E-Virus"); ps.execute(); // Hepatitis E
			ps.setString(1, "Krim-Kongo-H�morraghisches-Fieber-Virus"); ps.execute(); // Krim-Kongo-H�morraghisches-Fieber-Virus
			ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Codes_Agenzien") +
					" (" + DBKernel.delimitL("CodeSystem") + "," + DBKernel.delimitL("Code") + "," + DBKernel.delimitL("Basis") + ") VALUES (?,?,?)");
			ps.setString(1, "TOP"); ps.setString(2, "00"); ps.setInt(3, DBKernel.getID("Agenzien", "Agensname", "Bakterien")); ps.execute(); // Bakterien
			ps.setString(1, "TOP"); ps.setString(2, "0001"); ps.setInt(3, 90); ps.execute(); // Bacillus Anthracis
			ps.setString(1, "TOP"); ps.setString(2, "0002"); ps.setInt(3, 161); ps.execute(); // Salmonella Typhi
			ps.setString(1, "TOP"); ps.setString(2, "0003"); ps.setInt(3, 31); ps.execute(); // Genus Brucella
			ps.setString(1, "TOP"); ps.setString(2, "0004"); ps.setInt(3, 273); ps.execute(); // Vibrio Cholerae
			ps.setString(1, "TOP"); ps.setString(2, "0005"); ps.setInt(3, 14); ps.execute(); // Enteroh�morrhagische E. coli
			ps.setString(1, "TOP"); ps.setString(2, "0006"); ps.setInt(3, 2973); ps.execute(); // Francisella tularensis
			ps.setString(1, "TOP"); ps.setString(2, "0007"); ps.setInt(3, 156); ps.execute(); // Shigella dysenteriae
			ps.setString(1, "TOP"); ps.setString(2, "01"); ps.setInt(3, DBKernel.getID("Agenzien", "Agensname", "Toxine")); ps.execute(); // Toxine
			ps.setString(1, "TOP"); ps.setString(2, "0101"); ps.setInt(3, 3328); ps.execute(); // Botulinumtoxin
			ps.setString(1, "TOP"); ps.setString(2, "0102"); ps.setInt(3, 3329); ps.execute(); // Botulinumtoxin A
			ps.setString(1, "TOP"); ps.setString(2, "0103"); ps.setInt(3, 3330); ps.execute(); // Botulinumtoxin B
			ps.setString(1, "TOP"); ps.setString(2, "0104"); ps.setInt(3, 3331); ps.execute(); // Botulinumtoxin C
			ps.setString(1, "TOP"); ps.setString(2, "0105"); ps.setInt(3, 3332); ps.execute(); // Botulinumtoxin D
			ps.setString(1, "TOP"); ps.setString(2, "0106"); ps.setInt(3, 3333); ps.execute(); // Botulinumtoxin E
			ps.setString(1, "TOP"); ps.setString(2, "0107"); ps.setInt(3, 3334); ps.execute(); // Botulinumtoxin F
			ps.setString(1, "TOP"); ps.setString(2, "0108"); ps.setInt(3, 3335); ps.execute(); // Botulinumtoxin G
			ps.setString(1, "TOP"); ps.setString(2, "0109"); ps.setInt(3, 3253); ps.execute(); // Rizin
			ps.setString(1, "TOP"); ps.setString(2, "02"); ps.setInt(3, 62); ps.execute(); // Viren
			ps.setString(1, "TOP"); ps.setString(2, "0201"); ps.setInt(3, DBKernel.getID("Agenzien", "Agensname", "Hepatitis E-Virus")); ps.execute();
			ps.setString(1, "TOP"); ps.setString(2, "0202"); ps.setInt(3, DBKernel.getID("Agenzien", "Agensname", "Krim-Kongo-H�morraghisches-Fieber-Virus")); ps.execute();
	    	
			
			ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Matrices") +
					" (" + DBKernel.delimitL("Matrixname") + ") VALUES (?)");
			ps.setString(1, "Brucella-Bouillon"); ps.execute();
			ps.setString(1, "feste N�hrmedien"); ps.execute();
			ps.setString(1, "Butterfield's Phosphate Buffer"); ps.execute();
			ps.setString(1, "fl�ssige N�hrmedien"); ps.execute();
			ps.setString(1, "Brucella-Agar"); ps.execute();
			ps.setString(1, "Brucella-Selektiv-Agar"); ps.execute();
			
	    	ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Codes_Matrices") +
					" (" + DBKernel.delimitL("CodeSystem") + "," + DBKernel.delimitL("Code") + "," + DBKernel.delimitL("Basis") + ") VALUES (?,?,?)");
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0100"); ps.setInt(3, 19986); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "00"); ps.setInt(3, 19987); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0101"); ps.setInt(3, 19988); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "01"); ps.setInt(3, 19989); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0000"); ps.setInt(3, 19990); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0001"); ps.setInt(3, 19991); ps.execute();
			ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Agenzien") +
					" (" + DBKernel.delimitL("Agensname") + ") VALUES (?)");
			ps.setString(1, "Gruppe fakultativ anaerober gramnegativer St�bchen"); ps.execute();
			ps.setString(1, "Genus Escherichia"); ps.execute();
			ps.setString(1, "Escherichia coli O104:H4"); ps.execute();
	    	ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Codes_Agenzien") +
					" (" + DBKernel.delimitL("CodeSystem") + "," + DBKernel.delimitL("Code") + "," + DBKernel.delimitL("Basis") + ") VALUES (?,?,?)");
			ps.setString(1, "SiLeBAT"); ps.setString(2, "00"); ps.setInt(3, 3603); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0000"); ps.setInt(3, 3604); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "000000"); ps.setInt(3, 3605); ps.execute();
			
			DBKernel.doMNs(DBKernel.myList.getTable("Matrices"));
			DBKernel.doMNs(DBKernel.myList.getTable("Agenzien"));
	
			ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Codes_Methoden") +
					" (" + DBKernel.delimitL("CodeSystem") + "," + DBKernel.delimitL("Code") + "," + DBKernel.delimitL("Basis") + ") VALUES (?,?,?)");
			ps.setString(1, "TOP"); ps.setString(2, "01"); ps.setInt(3, 697); ps.execute();
	
	
	    	ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Methodennormen") +
					" (" + DBKernel.delimitL("Name") + ") VALUES (?)");
	    	ps.setString(1, "WHO"); ps.execute(); 
	    	ps.setString(1, "ISO"); ps.execute(); 
	    	ps.setString(1, "DIN"); ps.execute(); 
	    	ps.setString(1, "CEN"); ps.execute(); 
	    	ps.setString(1, "OIE"); ps.execute(); 
	    	ps.setString(1, "IDF"); ps.execute(); 

			ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Risikogruppen") +
					" (" + DBKernel.delimitL("Bezeichnung") + ") VALUES (?)");
			ps.setString(1, "Senioren"); ps.execute();
			ps.setString(1, "Kinder"); ps.execute();
			ps.setString(1, "Jugendliche"); ps.execute();
			ps.setString(1, "Immunsupprimierte Menschen"); ps.execute();
			ps.setString(1, "Schwangere"); ps.execute();
			ps.setString(1, "Kleinkinder/S�uglinge"); ps.execute();		
			
			// neue Daten ab 1.3.7
			ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Matrices") +
					" (" + DBKernel.delimitL("Matrixname") + ") VALUES (?)");
			ps.setString(1, "Sprossensamen"); ps.execute();
			ps.setString(1, "Bockshornkleesamen"); ps.execute();
			ps.setString(1, "Alfalfasamen"); ps.execute();
			ps.setString(1, "Mungobohnensamen"); ps.execute();
			ps.setString(1, "Rettichsamen"); ps.execute();
			ps.setString(1, "Linsensamen"); ps.execute();
			ps.setString(1, "Zwiebelsamen"); ps.execute();

			ps.setString(1, "Frischgem�se"); ps.execute();
			ps.setString(1, "Sprossgem�se"); ps.execute();
			ps.setString(1, "Bockshornkleesprossen"); ps.execute();
			ps.setString(1, "Alfalfasprossen"); ps.execute();
			ps.setString(1, "Mungobohnensprossen"); ps.execute();
			ps.setString(1, "Rettichsprossen"); ps.execute();
			ps.setString(1, "Linsensprossen"); ps.execute();
			ps.setString(1, "Zwiebelsprossen"); ps.execute();
			
	    	ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Codes_Matrices") +
					" (" + DBKernel.delimitL("CodeSystem") + "," + DBKernel.delimitL("Code") + "," + DBKernel.delimitL("Basis") + ") VALUES (?,?,?)");
			ps.setString(1, "SiLeBAT"); ps.setString(2, "02"); ps.setInt(3, 19992); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0200"); ps.setInt(3, 19993); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0201"); ps.setInt(3, 19994); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0202"); ps.setInt(3, 19995); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0203"); ps.setInt(3, 19996); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0204"); ps.setInt(3, 19997); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0205"); ps.setInt(3, 19998); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "03"); ps.setInt(3, 19999); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "0301"); ps.setInt(3, 20000); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "030100"); ps.setInt(3, 20001); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "030101"); ps.setInt(3, 20002); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "030102"); ps.setInt(3, 20003); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "030103"); ps.setInt(3, 20004); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "030104"); ps.setInt(3, 20005); ps.execute();
			ps.setString(1, "SiLeBAT"); ps.setString(2, "030105"); ps.setInt(3, 20006); ps.execute();
			
			ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Modellkatalog") +
					" (" + DBKernel.delimitL("Name") + "," + DBKernel.delimitL("Notation") + "," +
					DBKernel.delimitL("Level") + "," + DBKernel.delimitL("Klasse") + "," +
					DBKernel.delimitL("Formel") + "," + DBKernel.delimitL("Eingabedatum") + "," +
					DBKernel.delimitL("Software") + ") VALUES (?,?,?,?,?,?,?)");
			ps.setString(1, "D-Wert (Bigelow)"); 
			ps.setString(2, "d_wert");
			ps.setInt(3, 1);
			ps.setInt(4, 2);
			ps.setString(5, "LOG10N ~ LOG10N0 - t / D");
			ps.setDate(6, new java.sql.Date(System.currentTimeMillis()));
			ps.setString(7, "R");
			ps.execute();

			ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("ModellkatalogParameter") +
					" (" + DBKernel.delimitL("Modell") + "," + DBKernel.delimitL("Parametername") + "," +
					DBKernel.delimitL("Parametertyp") + "," + DBKernel.delimitL("ganzzahl") + ") VALUES (?,?,?,?)");
			ps.setInt(1, 44); 
			ps.setString(2, "D");
			ps.setInt(3, 2);
			ps.setBoolean(4, false);
			ps.execute();
			ps.setInt(1, 44); 
			ps.setString(2, "LOG10N0");
			ps.setInt(3, 2);
			ps.setBoolean(4, false);
			ps.execute();
			ps.setInt(1, 44); 
			ps.setString(2, "t");
			ps.setInt(3, 1);
			ps.setBoolean(4, false);
			ps.execute();
			ps.setInt(1, 44); 
			ps.setString(2, "LOG10N");
			ps.setInt(3, 3);
			ps.setBoolean(4, false);
			ps.execute();
			
			// neue Tabelle seit 1.4.1
			try {
				ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Parametertyp") +
						" (" + DBKernel.delimitL("Parametertyp") + ") VALUES (?)");
				ps.setInt(1, 1); ps.execute();
				ps.setInt(1, 2); ps.execute();
				ps.setInt(1, 3); ps.execute();
				// neu seit 1.4.4
				ps.setInt(1, 4); ps.execute();
			}
			catch (Exception e) {
				e.printStackTrace();
			}			
			
		}
		catch (Exception e) {e.printStackTrace();}
		
	}
	private void importEinheiten() {
		try {
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Einheiten") +
					" (" + DBKernel.delimitL("Beschreibung") + "," + DBKernel.delimitL("Einheit") + ") VALUES (?,?)");
	    	ps.setString(1, "log Anzahl (Zellen, Partikel, ...) pro Gramm (log Anzahl/g)"); ps.setString(2, "log Anzahl pro g"); ps.execute(); 	// Viren, Bakterien
	    	ps.setString(1, "log Anzahl (Zellen, Partikel, ...) pro 25 Gramm (log Anzahl/25g)"); ps.setString(2, "log Anzahl pro 25g"); ps.execute(); 	// Viren, Bakterien
	    	ps.setString(1, "log Anzahl (Zellen, Partikel, ...) pro 100 Gramm (log Anzahl/100g)"); ps.setString(2, "log Anzahl pro 100g"); ps.execute(); 	// Viren, Bakterien
	    	ps.setString(1, "log Anzahl (Zellen, Partikel, ...) pro Milliliter (log Anzahl/ml)"); ps.setString(2, "log Anzahl pro ml"); ps.execute(); 	// Viren, Bakterien
	    	ps.setString(1, "log KBE (cfu) pro Gramm (log KBE/g)"); ps.setString(2, "log KBE pro g"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "log KBE (cfu) pro 25 Gramm (log KBE/25g)"); ps.setString(2, "log KBE pro 25g"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "log KBE (cfu) pro 100 Gramm (log KBE/100g)"); ps.setString(2, "log KBE pro 100g"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "log KBE (cfu) pro Milliliter (log KBE/ml)"); ps.setString(2, "log KBE pro ml"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "log KBE (cfu) pro Quadratzentimeter (log KBE/cm^2)"); ps.setString(2, "log KBE pro cm^2"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "log PBE (pfu) pro Gramm (log PBE/g)"); ps.setString(2, "log PBE pro g"); ps.execute(); 	// Viren
	    	ps.setString(1, "log PBE (pfu) pro 25 Gramm (log PBE/25g)"); ps.setString(2, "log PBE pro 25g"); ps.execute(); 	// Viren
	    	ps.setString(1, "log PBE (pfu) pro 100 Gramm (log PBE/100g)"); ps.setString(2, "log PBE pro 100g"); ps.execute(); 	// Viren
	    	ps.setString(1, "log PBE (pfu) pro Milliliter (log PBE/ml)"); ps.setString(2, "log PBE pro ml"); ps.execute(); 	// Viren
	    	ps.setString(1, "log Nanogramm pro Gramm (log ng/g)"); ps.setString(2, "log ng pro g"); ps.execute(); 	// Toxine
	    	ps.setString(1, "log Nanogramm pro Milliliter (log ng/ml)"); ps.setString(2, "log ng pro ml"); ps.execute(); 	// Toxine
	    	ps.setString(1, "Anzahl (Zellen, Partikel, ...) pro Gramm (Anzahl/g)"); ps.setString(2, "Anzahl pro g"); ps.execute(); 	// Viren, Bakterien
	    	ps.setString(1, "Anzahl (Zellen, Partikel, ...) pro 25 Gramm (Anzahl/25g)"); ps.setString(2, "Anzahl pro 25g"); ps.execute(); 	// Viren, Bakterien
	    	ps.setString(1, "Anzahl (Zellen, Partikel, ...) pro 100 Gramm (Anzahl/100g)"); ps.setString(2, "Anzahl pro 100g"); ps.execute(); 	// Viren, Bakterien
	    	ps.setString(1, "Anzahl (Zellen, Partikel, ...) pro Milliliter (Anzahl/ml)"); ps.setString(2, "Anzahl pro ml"); ps.execute(); 	// Viren, Bakterien
	    	ps.setString(1, "KBE (cfu) pro Gramm (KBE/g)"); ps.setString(2, "KBE pro g"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "KBE (cfu) pro 25 Gramm (KBE/25g)"); ps.setString(2, "KBE pro 25g"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "KBE (cfu) pro 100 Gramm (KBE/100g)"); ps.setString(2, "KBE pro 100g"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "KBE (cfu) pro Milliliter (KBE/ml)"); ps.setString(2, "KBE pro ml"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "KBE (cfu) pro Quadratzentimeter (KBE/cm^2)"); ps.setString(2, "KBE pro cm^2"); ps.execute(); 	// Bakterien
	    	ps.setString(1, "PBE (pfu) pro Gramm (PBE/g)"); ps.setString(2, "PBE pro g"); ps.execute(); 	// Viren
	    	ps.setString(1, "PBE (pfu) pro 25 Gramm (PBE/25g)"); ps.setString(2, "PBE pro 25g"); ps.execute(); 	// Viren
	    	ps.setString(1, "PBE (pfu) pro 100 Gramm (PBE/100g)"); ps.setString(2, "PBE pro 100g"); ps.execute(); 	// Viren
	    	ps.setString(1, "PBE (pfu) pro Milliliter (PBE/ml)"); ps.setString(2, "PBE pro ml"); ps.execute(); 	// Viren
	    	ps.setString(1, "Nanogramm pro Gramm (ng/g)"); ps.setString(2, "ng pro g"); ps.execute(); 	// Toxine
	    	ps.setString(1, "Nanogramm pro Milliliter (ng/ml)"); ps.setString(2, "ng pro ml"); ps.execute(); 	// Toxine		
		}
		catch (Exception e) {e.printStackTrace();}
	}
	private void importSP() {
		try {
			PreparedStatement ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("SonstigeParameter") +
					" (" + DBKernel.delimitL("Parameter") + "," + DBKernel.delimitL("Beschreibung") + ") VALUES (?,?)");
	    	ps.setString(1, "ALTA"); ps.setString(2, "alta fermentation product in the environment"); ps.execute();
	    	ps.setString(1, "acetic_acid"); ps.setString(2, "acetic acid (possibly as salt) in the environment"); ps.execute();
	    	ps.setString(1, "anaerobic"); ps.setString(2, "anaerobic environment"); ps.execute();
	    	ps.setString(1, "ascorbic_acid"); ps.setString(2, "ascorbic acid (possibly as salt) in the environment"); ps.execute();
	    	ps.setString(1, "benzoic_acid"); ps.setString(2, "benzoic acid (possibly as salt) in the environment"); ps.execute();
	    	ps.setString(1, "citric_acid"); ps.setString(2, "citric acid (possibly as salt) in the environment"); ps.execute();
	    	ps.setString(1, "CO_2"); ps.setString(2, "carbon-dioxide in the environment"); ps.execute();
	    	ps.setString(1, "competition"); ps.setString(2, "other species in the environment"); ps.execute();
	    	ps.setString(1, "cut"); ps.setString(2, "cut (minced, chopped, ground, etc)"); ps.execute();
	    	ps.setString(1, "dried"); ps.setString(2, "dried food"); ps.execute();
	    	ps.setString(1, "EDTA"); ps.setString(2, "ethylenenediaminetetraacetic acid in the environment"); ps.execute();
	    	ps.setString(1, "ethanol"); ps.setString(2, "ethanol in the environment"); ps.execute();
	    	ps.setString(1, "fat"); ps.setString(2, "fat in the environment"); ps.execute();
	    	ps.setString(1, "frozen"); ps.setString(2, "frozen food"); ps.execute();
	    	ps.setString(1, "fructose"); ps.setString(2, "fructose in the environment"); ps.execute();
	    	ps.setString(1, "glucose"); ps.setString(2, "glucose in the environment"); ps.execute();
	    	ps.setString(1, "glycerol"); ps.setString(2, "glycerol in the environment"); ps.execute();
	    	ps.setString(1, "HCl"); ps.setString(2, "hydrochloric acid in the environment"); ps.execute();
	    	ps.setString(1, "heated"); ps.setString(2, "inoculation in/on previously heated (cooked, baked, pasteurized, etc) but not sterilised food/medium"); ps.execute();
	    	ps.setString(1, "irradiated"); ps.setString(2, "in an environment that has been irradiated"); ps.execute();
	    	ps.setString(1, "irradiation"); ps.setString(2, "irradiation at constant rate during the observation time"); ps.execute();
	    	ps.setString(1, "lactic_acid"); ps.setString(2, "lactic acid (possibly as salt) in the environment"); ps.execute();
	    	ps.setString(1, "lactic_bacteria_fermented"); ps.setString(2, "food fermented by lactic acid bacteria"); ps.execute();
	    	ps.setString(1, "Modified_Atmosphere"); ps.setString(2, "modified atmosphere environment"); ps.execute();
	    	ps.setString(1, "malic_acid"); ps.setString(2, "malic acid in the environment"); ps.execute();
	    	ps.setString(1, "moisture"); ps.setString(2, "moisture in the environment"); ps.execute();
	    	ps.setString(1, "monolaurin"); ps.setString(2, "glycerol monolaurate (emulsifier) in the environment"); ps.execute();
	    	ps.setString(1, "N_2"); ps.setString(2, "nitrogen in the environment"); ps.execute();
	    	ps.setString(1, "NaCl"); ps.setString(2, "sodium chloride in the environment"); ps.execute();
	    	ps.setString(1, "nisin"); ps.setString(2, "nisin in the environment"); ps.execute();
	    	ps.setString(1, "nitrite"); ps.setString(2, "sodium or potassium nitrite in the environment"); ps.execute();
	    	ps.setString(1, "O_2"); ps.setString(2, "oxygen (aerobic conditions) in the environment"); ps.execute();
	    	ps.setString(1, "propionic_acid"); ps.setString(2, "propionic acid (possibly as salt) in the environment"); ps.execute();
	    	ps.setString(1, "raw"); ps.setString(2, "raw"); ps.execute();
	    	ps.setString(1, "shaken"); ps.setString(2, "shaken (agitated, stirred)"); ps.execute();
	    	ps.setString(1, "smoked"); ps.setString(2, "smoked food"); ps.execute();
	    	ps.setString(1, "sorbic_acid"); ps.setString(2, "sorbic acid (possibly as salt) in the environment"); ps.execute();
	    	ps.setString(1, "sterile"); ps.setString(2, "sterilised before inoculation"); ps.execute();
	    	ps.setString(1, "sucrose"); ps.setString(2, "sucrose in the environment"); ps.execute();
	    	ps.setString(1, "sugar"); ps.setString(2, "sugar in the environment"); ps.execute();
	    	ps.setString(1, "vacuum"); ps.setString(2, "vacuum-packed"); ps.execute();
	    	ps.setString(1, "oregano"); ps.setString(2, "oregano essential oil in the environment"); ps.execute();
	    	ps.setString(1, "indigenous_flora"); ps.setString(2, "with the indigenous flora in the environment (but not counted)"); ps.execute();
	    	ps.setString(1, "pressure"); ps.setString(2, "pressure controlled"); ps.execute();
	    	ps.setString(1, "diacetic_acid"); ps.setString(2, "in presence of diacetic acid (possibly as salt)"); ps.execute();
	    	ps.setString(1, "betaine"); ps.setString(2, "in presence of betaine"); ps.execute();
		}
		catch (Exception e) {e.printStackTrace();}
	}
  public void dropDatabase() {
	  DBKernel.closeDBConnections(false);
    File f = new File(DBKernel.HSHDB_PATH);
    File[] files = f.listFiles();
    if (files != null) {
      for (int i=0;i<files.length;i++) {
        if (files[i].isFile() && files[i].getName().startsWith("DB.")) {
          files[i].delete();
        }
      }
    	System.gc();    	
    }
  }
	@SuppressWarnings("unchecked")
	public void loadMyTables(final MyList myList, final MyDBTable myDB) {
		LinkedHashMap<Object, String> hashZeit = new LinkedHashMap<Object, String>();
		hashZeit.put("Sekunde", "Sekunde(n) [s][sec]");					
		hashZeit.put("Minute", "Minute(n)");					
		hashZeit.put("Stunde", "Stunde(n)");		
		hashZeit.put("Tag", "Tag(e)");		
		hashZeit.put("Woche", "Woche(n)");		
		hashZeit.put("Monat", "Monat(e)");		
		hashZeit.put("Jahr", "Jahr(e)");		

		LinkedHashMap<Object, String> hashGeld = new LinkedHashMap<Object, String>();
		hashGeld.put("Dollar", "Dollar ($)");					
		hashGeld.put("Euro", "Euro (�)");					

		LinkedHashMap<Object, String> hashGewicht = new LinkedHashMap<Object, String>();
		hashGewicht.put("Milligramm", "Milligramm (mg)");					
		hashGewicht.put("Gramm", "Gramm (g)");					
		hashGewicht.put("Kilogramm", "Kilogramm (kg)");					
		hashGewicht.put("Tonne", "Tonne (t)");					

		LinkedHashMap<Object, String> hashSpeed = new LinkedHashMap<Object, String>();
		hashSpeed.put("pro Stunde", "pro Stunde (1/h)");					
		hashSpeed.put("pro Tag", "pro Tag (1/d)");							

		LinkedHashMap<Object, String> hashDosis = new LinkedHashMap<Object, String>();
		hashDosis.put("Sporenzahl", "Sporenzahl");					
		hashDosis.put("KBE pro g", "KBE (cfu) pro Gramm (KBE/g)");					
		hashDosis.put("KBE pro ml", "KBE (cfu) pro Milliliter (KBE/ml)");					
		hashDosis.put("PBE pro g", "PBE (pfu) pro Gramm (PBE/g)");					
		hashDosis.put("PBE pro ml", "PBE (pfu) pro Milliliter (PBE/ml)");					
		hashDosis.put("Milligramm", "Milligramm (mg)");							
		hashDosis.put("Mikrogramm", "Mikrogramm (\u00B5g)");							
		hashDosis.put("\u00B5g/kg/KG", "\u00B5g/kg/KG");							
		hashDosis.put("Anzahl", "Anzahl (Viren, Bakterien, Parasiten, Organismen, ...)");	
/*
		LinkedHashMap<Object, String> hashProG = new LinkedHashMap<Object, String>();
		hashProG.put("log Anzahl pro g", "log Anzahl (Zellen, Partikel, ...) pro Gramm (log Anzahl/g)");		// Viren, Bakterien
		hashProG.put("log Anzahl pro 25g", "log Anzahl (Zellen, Partikel, ...) pro 25 Gramm (log Anzahl/25g)");		// Viren, Bakterien
		hashProG.put("log Anzahl pro 100g", "log Anzahl (Zellen, Partikel, ...) pro 100 Gramm (log Anzahl/100g)");		// Viren, Bakterien
		hashProG.put("log Anzahl pro ml", "log Anzahl (Zellen, Partikel, ...) pro Milliliter (log Anzahl/ml)");		// Viren, Bakterien
		hashProG.put("log KBE pro g", "log KBE (cfu) pro Gramm (log KBE/g)");		// Bakterien
		hashProG.put("log KBE pro 25g", "log KBE (cfu) pro 25 Gramm (log KBE/25g)");// Bakterien					
		hashProG.put("log KBE pro 100g", "log KBE (cfu) pro 100 Gramm (log KBE/100g)");// Bakterien					
		hashProG.put("log KBE pro ml", "log KBE (cfu) pro Milliliter (log KBE/ml)");// Bakterien	
		hashProG.put("log KBE pro cm^2", "log KBE (cfu) pro Quadratzentimeter (log KBE/cm^2)");// Bakterien	
		hashProG.put("log PBE pro g", "log PBE (pfu) pro Gramm (log PBE/g)");		// Viren
		hashProG.put("log PBE pro 25g", "log PBE (pfu) pro 25 Gramm (log PBE/25g)");// Viren					
		hashProG.put("log PBE pro 100g", "log PBE (pfu) pro 100 Gramm (log PBE/100g)");// Viren					
		hashProG.put("log PBE pro ml", "log PBE (pfu) pro Milliliter (log PBE/ml)");// Viren	
		hashProG.put("log ng pro g", "log Nanogramm pro Gramm (log ng/g)");		// Toxine			
		hashProG.put("log ng pro ml", "log Nanogramm pro Milliliter (log ng/ml)");		// Toxine
		hashProG.put("Anzahl pro g", "Anzahl (Zellen, Partikel, ...) pro Gramm (Anzahl/g)");		// Viren, Bakterien
		hashProG.put("Anzahl pro 25g", "Anzahl (Zellen, Partikel, ...) pro 25 Gramm (Anzahl/25g)");		// Viren, Bakterien
		hashProG.put("Anzahl pro 100g", "Anzahl (Zellen, Partikel, ...) pro 100 Gramm (Anzahl/100g)");		// Viren, Bakterien
		hashProG.put("Anzahl pro ml", "Anzahl (Zellen, Partikel, ...) pro Milliliter (Anzahl/ml)");		// Viren, Bakterien
		hashProG.put("KBE pro g", "KBE (cfu) pro Gramm (KBE/g)");		// Bakterien
		hashProG.put("KBE pro 25g", "KBE (cfu) pro 25 Gramm (KBE/25g)");// Bakterien					
		hashProG.put("KBE pro 100g", "KBE (cfu) pro 100 Gramm (KBE/100g)");// Bakterien					
		hashProG.put("KBE pro ml", "KBE (cfu) pro Milliliter (KBE/ml)");// Bakterien	
		hashProG.put("KBE pro cm^2", "KBE (cfu) pro Quadratzentimeter (KBE/cm^2)");// Bakterien	
		hashProG.put("PBE pro g", "PBE (pfu) pro Gramm (PBE/g)");		// Viren
		hashProG.put("PBE pro 25g", "PBE (pfu) pro 25 Gramm (PBE/25g)");// Viren					
		hashProG.put("PBE pro 100g", "PBE (pfu) pro 100 Gramm (PBE/100g)");// Viren					
		hashProG.put("PBE pro ml", "PBE (pfu) pro Milliliter (PBE/ml)");// Viren	
		hashProG.put("ng pro g", "Nanogramm pro Gramm (ng/g)");		// Toxine			
		hashProG.put("ng pro ml", "Nanogramm pro Milliliter (ng/ml)");		// Toxine
*/
		LinkedHashMap<Object, String> hashFreigabe = new LinkedHashMap<Object, String>();
		hashFreigabe.put(0, "gar nicht");					
		hashFreigabe.put(1, "Krise");					
		hashFreigabe.put(2, "immer");					
		
		DBKernel.hashBundesland.put("Baden-W�rttemberg", "Baden-W�rttemberg");
		DBKernel.hashBundesland.put("Bayern", "Bayern");
		DBKernel.hashBundesland.put("Berlin", "Berlin");
		DBKernel.hashBundesland.put("Brandenburg", "Brandenburg");
		DBKernel.hashBundesland.put("Bremen", "Bremen");
		DBKernel.hashBundesland.put("Hamburg", "Hamburg");
		DBKernel.hashBundesland.put("Hessen", "Hessen");
		DBKernel.hashBundesland.put("Mecklenburg-Vorpommern", "Mecklenburg-Vorpommern");
		DBKernel.hashBundesland.put("Niedersachsen", "Niedersachsen");
		DBKernel.hashBundesland.put("Nordrhein-Westfalen", "Nordrhein-Westfalen");
		DBKernel.hashBundesland.put("Rheinland-Pfalz", "Rheinland-Pfalz");
		DBKernel.hashBundesland.put("Saarland", "Saarland");
		DBKernel.hashBundesland.put("Sachsen", "Sachsen");
		DBKernel.hashBundesland.put("Sachsen-Anhalt", "Sachsen-Anhalt");
		DBKernel.hashBundesland.put("Schleswig-Holstein", "Schleswig-Holstein");
		DBKernel.hashBundesland.put("Th�ringen", "Th�ringen");

		// HSQLDB-Doku: "The OTHER type is for storage of Java objects."
		DBKernel.changeLog = new MyTable("ChangeLog",
				new String[]{"Zeitstempel","Username","Tabelle","TabellenID","Alteintrag"},
				new String[]{"DATETIME","VARCHAR(60)","VARCHAR(100)","INTEGER","OTHER"},
				new String[]{null,null,null,null,null},
				new MyTable[]{null,null,null,null,null});
		myList.addTable(DBKernel.changeLog, MyList.SystemTabellen_LIST);
		DBKernel.blobSpeicher = new MyTable("DateiSpeicher",
				new String[]{"Zeitstempel","Tabelle","TabellenID","Feld","Dateiname","Dateigroesse","Datei"},
				new String[]{"DATETIME","VARCHAR(100)","INTEGER","VARCHAR(100)","VARCHAR(255)","INTEGER","BLOB(10M)"},
				new String[]{null,null,null,null,null,null,null},
				new MyTable[]{null,null,null,null,null,null,null});
		myList.addTable(DBKernel.blobSpeicher, MyList.SystemTabellen_LIST);
		DBKernel.users = new MyTable("Users",
				new String[]{"Username","Vorname","Name","Zugriffsrecht"},
				new String[]{"VARCHAR(60)","VARCHAR(30)","VARCHAR(30)","INTEGER"},
				new String[]{null,null,null,null},
				new MyTable[]{null,null,null,null},
				new String[][]{{"Username"}},
				new LinkedHashMap[]{null,null,null,Users.getUserTypesHash()});
		myList.addTable(DBKernel.users, MyList.SystemTabellen_LIST); // m�sste jetzt doch gehen, oder?...  lieber die Users ganz weg, weil das Editieren auf dem HSQLServer nicht korrekt funktioniert - siehe im Trigger removeAccRight usw., da m�sste man erst die sendRequests umstellen auf defaultconnection...		

		MyTable infoTable = new MyTable("Infotabelle",
				new String[]{"Parameter","Wert"},
				new String[]{"VARCHAR(255)","VARCHAR(255)"},
				new String[]{null, null},
				new MyTable[]{null,null},
				new String[][]{{"Parameter"}},
				new LinkedHashMap[]{null,null});
		myList.addTable(infoTable, -1);

		// Paper, SOP, LA, Manual/Handbuch, Laborbuch
		LinkedHashMap<Integer, String> lt = new LinkedHashMap<Integer, String>();
	    lt.put(new Integer(1), "Paper");
	    lt.put(new Integer(2), "SOP");
	    lt.put(new Integer(3), "LA");
	    lt.put(new Integer(4), "Handbuch");
	    lt.put(new Integer(5), "Laborbuch");
	    lt.put(new Integer(6), "Buch");
	    lt.put(new Integer(7), "Webseite");
	    lt.put(new Integer(8), "Bericht");
		MyTable literatur = new MyTable("Literatur", new String[]{"Erstautor","Jahr","Titel","Abstract","Journal","Volume","Issue","Seite","FreigabeModus","Webseite","Literaturtyp","Paper"},
				new String[]{"VARCHAR(100)","INTEGER","VARCHAR(1023)","VARCHAR(16383)","VARCHAR(255)","VARCHAR(50)","VARCHAR(50)","INTEGER","INTEGER","VARCHAR(255)","INTEGER","BLOB(10M)"},
				new String[]{"Erstautor der Publikation","Ver�ffentlichungsjahr","Titel des Artikels","Abstract/Zusammenfassung des Artikels","Journal / Buchtitel / ggf. Webseite / Veranstaltung etc.",null,null,"Seitenzahl_Beginn","Auswahl ob diese Information �ffentlich zug�nglich sein soll: nie, nur in der Krise, immer - Auswahlbox",null,"Auswahl zwischen Paper, SOP, LA, Handbuch/Manual, Laborbuch","Originaldatei"},
				new MyTable[]{null,null,null,null,null,null,null,null,null,null,null,null},
				new String[][]{{"Erstautor","Jahr","Titel"}},
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,hashFreigabe,null,lt,null});
		myList.addTable(literatur, DBKernel.isKrise ? -1 : (DBKernel.isKNIME ? MyList.BasisTabellen_LIST : 66));

		LinkedHashMap<Integer, String> wt = new LinkedHashMap<Integer, String>();
		wt.put(new Integer(1), "Einzelwert");
		wt.put(new Integer(2), "Mittelwert");
		wt.put(new Integer(3), "Median");
		MyTable newDoubleTable = new MyTable("DoubleKennzahlen",
				new String[]{"Wert","Exponent","Wert_typ","Wert_g","Wiederholungen","Wiederholungen_g","Standardabweichung","Standardabweichung_exp","Standardabweichung_g",
				"Minimum","Minimum_exp","Minimum_g","Maximum","Maximum_exp","Maximum_g",
				"LCL95","LCL95_exp","LCL95_g","UCL95","UCL95_exp","UCL95_g",
				"Verteilung","Verteilung_g","Funktion (Zeit)","Funktion (Zeit)_g","Funktion (x)","x","Funktion (x)_g",
				"Undefiniert (n.d.)","Referenz"},
				new String[]{"DOUBLE","DOUBLE","INTEGER","BOOLEAN","DOUBLE","BOOLEAN","DOUBLE","DOUBLE","BOOLEAN",
				"DOUBLE","DOUBLE","BOOLEAN","DOUBLE","DOUBLE","BOOLEAN",
				"DOUBLE","DOUBLE","BOOLEAN","DOUBLE","DOUBLE","BOOLEAN",
				"VARCHAR(1023)","BOOLEAN","VARCHAR(1023)","BOOLEAN","VARCHAR(1023)","VARCHAR(25)","BOOLEAN",
				"BOOLEAN","INTEGER"},
				new String[]{"Wert","Exponent zur Basis 10, falls vorhanden\nBeispiel 1.3*10^-4 : 1.3 wird in der Spalte 'Wert' eingetragen und -4 in dieser Spalte","Wert_typ ist entweder Einzelwert, Mittelwert oder Median","Der Einzelwert wurde nicht wirklich gemessen, sondern gesch�tzt (ja=gesch�tzt, nein=gemessen)","Anzahl der Wiederholungsmessungen/technischen Replikate f�r diesen Wert","gesch�tzt","Standardabweichung des gemessenen Wertes - Eintrag nur bei Mehrfachmessungen m�glich","Exponent zur Basis 10 f�r die Standardabweichung, falls vorhanden","gesch�tzt",
				"Minimum","Exponent zur Basis 10 f�r das Minimum, falls vorhanden","gesch�tzt","Maximum","Exponent zur Basis 10 f�r das Maximum, falls vorhanden","gesch�tzt",
				"Untere Konfidenzgrenze des gemessenen Wertes (95%-KI) - Eintrag nur bei Mehrfachmessungen m�glich","Exponent zur Basis 10 f�r LCL95, falls vorhanden","gesch�tzt","Obere Konfidenzgrenze des gemessenen Wertes (95%-KI) - Eintrag nur bei Mehrfachmessungen m�glich","Exponent zur Basis 10 f�r UCL95, falls vorhanden","gesch�tzt",
				"Verteilung der Werte bei Mehrfachmessungen, z.B. Normalverteilung. Anzugeben ist die entsprechende Funktion in R, z.B. rnorm(n, mean = 0, sd = 1)","gesch�tzt","'Parameter'/Zeit-Profil. Funktion des Parameters in Abh�ngigkeit von der Zeit.\nF�r das Parsen wird die Klasse http://math.hws.edu/javamath/javadoc/edu/hws/jcm/data/Parser.html benutzt.","gesch�tzt","'Parameter'/x-Profil. Funktion des Parameters in Abh�ngigkeit des anzugebenden x-Parameters.\nF�r das Parsen wird die Klasse http://math.hws.edu/javamath/javadoc/edu/hws/jcm/data/Parser.html benutzt.","Der zugeh�rige x-Parameter: Bezugsgr��e f�r eine Funktion, z.B. Temperatur in Abh�ngigkeit von pH, dann ist die Bezugsgr��e pH.","gesch�tzt",
				"Undefiniert (n.d.)","Referenz zu diesen Kennzahlen"},
				new MyTable[]{null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null,null,null,null,
				null,literatur},
				new LinkedHashMap[]{null,null,wt,null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null,null,null,null,
				null,null});
		myList.addTable(newDoubleTable, -1);

		// Katalogtabellen
		MyTable matrix = new MyTable("Matrices", new String[]{"Matrixname","Leitsatznummer","pH","aw","Dichte","Katalogcodes"},
				new String[]{"VARCHAR(255)","VARCHAR(20)","DOUBLE","DOUBLE","DOUBLE","INTEGER"},
				new String[]{"Kulturmedium / Futtermittel / Lebensmittel / Serum / Kot / Gewebe","Leitsatznummer - falls bekannt","pH-Wert �ber alle Produkte der Warengruppe - falls absch�tzbar","aw-Wert �ber alle Produkte der Warengruppe - falls absch�tzbar","Dichte der Matrix �ber alle Produkte der Warengruppe - falls absch�tzbar","Matrixkatalog - Codes"},
				new MyTable[]{null,null,newDoubleTable,newDoubleTable,newDoubleTable,null},
				null,
				null,
				new String[]{null,null,null,null,null,"INT"});
		myList.addTable(matrix, MyList.BasisTabellen_LIST);
		
		MyTable toxinUrsprung = new MyTable("ToxinUrsprung", new String[]{"Ursprung"},
				new String[]{"VARCHAR(255)"},
				new String[]{null},
				new MyTable[]{null});
		myList.addTable(toxinUrsprung, -1);
		LinkedHashMap<Integer, String> btv = new LinkedHashMap<Integer, String>();
	  btv.put(new Integer(1), "Bakterium");	btv.put(new Integer(2), "Toxin"); btv.put(new Integer(3), "Virus");
		LinkedHashMap<Integer, String> h1234 = new LinkedHashMap<Integer, String>();
	  h1234.put(new Integer(1), "eins");	h1234.put(new Integer(2), "zwei");
	  h1234.put(new Integer(3), "drei");	h1234.put(new Integer(4), "vier");					
		LinkedHashMap<Integer, String> hPM = new LinkedHashMap<Integer, String>();
	  hPM.put(new Integer(1), "+");	hPM.put(new Integer(2), "-");
		LinkedHashMap<Integer, String> hYN = new LinkedHashMap<Integer, String>();
		hYN.put(new Integer(1), "ja");	hYN.put(new Integer(0), "nein");
		LinkedHashMap<Boolean, String> hYNB = new LinkedHashMap<Boolean, String>();
		hYNB.put(new Boolean(true), "ja");	hYNB.put(new Boolean(false), "nein");
		LinkedHashMap<Integer, String> hYNT = new LinkedHashMap<Integer, String>();
		hYNT.put(new Integer(1), "mit Therapie");hYNT.put(new Integer(0), "ohne Therapie");hYNT.put(new Integer(2), "Keine Angabe");
		MyTable agenzien = new MyTable("Agenzien",
				new String[]{"Agensname","Kurzbezeichnung","WissenschaftlicheBezeichnung",
				"Klassifizierung","Familie","Gattung","Spezies","Subspezies_Subtyp",
				"Risikogruppe","Humanpathogen","Ursprung","Gramfaerbung",
				"CAS_Nummer","Carver_Nummer","FaktSheet","Katalogcodes"}, // Weitere Differenzierungsmerkmale [Stamm; Biovar, Serovar etc.]	lieber als AgensDetail
				new String[]{"VARCHAR(255)","VARCHAR(30)","VARCHAR(255)",
				"INTEGER","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)",
				"INTEGER","INTEGER","INTEGER","INTEGER",
				"VARCHAR(20)","VARCHAR(20)","BLOB(10M)","INTEGER"},
				new String[]{null,null,null,
				null,null,null,null,null,
				null,null,"Ursprung - nur bei Toxinen, z.B. Bakterium, Pflanzensamen","Gramf�rbung - nur bei Bakterien",
				null,null,"Datenblatt","Agenskatalog - Codes"},
				new MyTable[]{null,null,null,
				null,null,null,null,null,
				null,null,toxinUrsprung,null,
				null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,
				btv,null,null,null,null,
				h1234,hYN,null,hPM,
				null,null,null,null},
				new String[]{null,null,null,
				null,null,null,null,null,
				null,null,null,null,
				null,null,null,"INT"});
		myList.addTable(agenzien, MyList.BasisTabellen_LIST);
		MyTable normen = new MyTable("Methodennormen", new String[]{"Name","Beschreibung"},
				new String[]{"VARCHAR(255)","VARCHAR(1023)"},
				new String[]{"Name der Norm","Beschreibung der Norm"},
				new MyTable[]{null,null});
		myList.addTable(normen, -1);
		MyTable methoden = new MyTable("Methoden", new String[]{"Name","Beschreibung","Referenz","Norm","Katalogcodes"},
				new String[]{"VARCHAR(1023)","VARCHAR(1023)","INTEGER","INTEGER","INTEGER"},
				new String[]{"Name des Nachweisverfahrens","Beschreibung des Nachweisverfahrens","Verweis auf Literaturstelle","Zugeh�rige Normen, z.B. ISO, DIN, CEN, etc.","Methodenkatalog - Codes"}, // ,"Angabe, ob Testreagenzien auch inhouse produziert werden k�nnen"
				new MyTable[]{null,null,literatur,normen,null},
				null,
				null,
				new String[]{null,null,null,"Methoden_Normen","INT"});
		myList.addTable(methoden, DBKernel.getUsername().equals("buschulte") ? 66 : -1);
		MyTable methoden_Normen = new MyTable("Methoden_Normen",
				new String[]{"Methoden","Normen","Norm_Nummer"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{methoden,normen,null},
				new LinkedHashMap[]{null,null,null});
		myList.addTable(methoden_Normen, -1);
		
		MyTable matrix_OG = new MyTable("Codes_Matrices", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(20)","INTEGER"},
				new String[]{"Codebezeichnung, sofern vorhanden (z.B. TOP, GS1, BLS, ADV oder auch selfmade)","Hierarchischer Code","Zugeh�rige Matrix"},
				new MyTable[]{null,null,matrix},
				new String[][]{{"CodeSystem","Code"}});
		myList.addTable(matrix_OG, -1); // -1
		matrix.setForeignField(matrix_OG, 5);
		MyTable agenzienkategorie = new MyTable("Codes_Agenzien", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(20)","INTEGER"},
				new String[]{"Codebezeichnung, sofern vorhanden (z.B. TOP, ADV oder auch selfmade)","Hierarchischer Code","Zugeh�riges Agens"},
				new MyTable[]{null,null,agenzien},
				new String[][]{{"CodeSystem","Code"}});
		myList.addTable(agenzienkategorie, -1);
		agenzien.setForeignField(agenzienkategorie, 15);
		MyTable methoden_OG = new MyTable("Codes_Methoden", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(40)","INTEGER"},
				new String[]{"Codebezeichnung, sofern vorhanden (z.B. TOP, BLV oder auch selfmade)","Hierarchischer Code","Zugeh�rige Methode"},
				new MyTable[]{null,null,methoden},
				new String[][]{{"CodeSystem","Code"}});
		myList.addTable(methoden_OG, -1); // -1
		methoden.setForeignField(methoden_OG, 4);

		MyTable ComBaseImport = new MyTable("ComBaseImport", new String[]{"Referenz","Agensname","Agenskatalog","b_f","Matrixname","Matrixkatalog"},
				new String[]{"INTEGER","VARCHAR(255)","INTEGER","VARCHAR(255)","VARCHAR(255)","INTEGER"},
				new String[]{null,null,null,null,null,null},
				new MyTable[]{literatur,null,agenzien,null,null,matrix});
		myList.addTable(ComBaseImport, DBKernel.isKNIME ? MyList.BasisTabellen_LIST : -1); // 66
		MyTable adressen = new MyTable("Kontakte",
				new String[]{"Name","Strasse","Hausnummer","Postfach","PLZ","Ort","Bundesland","Land","Ansprechpartner","Telefon","Fax","EMail","Webseite"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(10)","VARCHAR(20)","VARCHAR(10)","VARCHAR(60)","VARCHAR(30)","VARCHAR(100)","VARCHAR(100)","VARCHAR(30)","VARCHAR(30)","VARCHAR(100)","VARCHAR(255)"},
				new String[]{"Name der Firma / Labor / Einrichtung", null,null,null,null,null,null,null,"Ansprechpartner inkl. Vor und Zunahme",null,null,null,null},
				new MyTable[]{null,null,null,null,null,null,null,null,null,null,null,null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,DBKernel.hashBundesland,null,null,null,null,null,null});
		myList.addTable(adressen, DBKernel.isKNIME ? -1 : MyList.BasisTabellen_LIST);
		
		MyTable symptome = new MyTable("Symptome", new String[]{"Bezeichnung","Beschreibung","Bezeichnung_engl","Beschreibung_engl"},
				new String[]{"VARCHAR(50)","VARCHAR(255)","VARCHAR(50)","VARCHAR(255)"},
				new String[]{"Kurzform auf deutsch","Ausf�hrliche Beschreibung auf deutsch","Kurzform auf englisch","Ausf�hrliche Beschreibung auf englisch"},
				new MyTable[]{null,null,null,null});
		myList.addTable(symptome, -1);

		MyTable risikogruppen = new MyTable("Risikogruppen", new String[]{"Bezeichnung","Beschreibung"},
				new String[]{"VARCHAR(50)","VARCHAR(255)"},
				new String[]{"Kurzform","Ausf�hrliche Beschreibung"},
				new MyTable[]{null,null});
		myList.addTable(risikogruppen, -1);

		MyTable tierkrankheiten = new MyTable("Tierkrankheiten", new String[]{"VET_Code","Kurzbezeichnung","Krankheitsart"},
				new String[]{"VARCHAR(255)","VARCHAR(50)","VARCHAR(255)"},
				new String[]{null,"Kurzform","Ausf�hrliche Beschreibung"},
				new MyTable[]{null,null,null});
		myList.addTable(tierkrankheiten, -1);
		
		MyTable krankheiten = generateICD10Tabellen(myList);
		
		LinkedHashMap<Object, String> h1 = new LinkedHashMap<Object, String>();
		h1.put("Human", "Human");h1.put("Kaninchen", "Kaninchen");h1.put("Maus", "Maus");h1.put("Ratte", "Ratte");
		h1.put("Meerschweinchen", "Meerschweinchen");h1.put("Primaten", "Primaten");h1.put("sonst. S�ugetier", "sonst. S�ugetier");
		LinkedHashMap<Object, String> h2 = new LinkedHashMap<Object, String>();
		h2.put("inhalativ", "inhalativ");					
		h2.put("oral", "oral");					
		h2.put("dermal", "dermal");		
		h2.put("Blut/Serum/K�rperfl�ssigkeit", "Blut/Serum/K�rperfl�ssigkeit");		
		h2.put("h�matogen", "h�matogen");							
		h2.put("transplazental", "transplazental");							
		h2.put("kutan", "kutan");					
		h2.put("venerisch", "venerisch");							
		h2.put("transkutan", "transkutan");							
		h2.put("intraperitoneal", "intraperitoneal");							
		h2.put("intraven�s", "intraven�s");							
		h2.put("subkutan", "subkutan");							
		h2.put("intramuskul�r", "intramuskul�r");							
		h2.put("Injektion", "Injektion");							
		LinkedHashMap<Object, String> h3 = new LinkedHashMap<Object, String>();
		h3.put("akut", "akut");					
		h3.put("chronisch", "chronisch");					
		h3.put("perkaut", "perkaut");		
		h3.put("subakut", "subakut");		
		LinkedHashMap<Object, String> k1 = new LinkedHashMap<Object, String>();
		k1.put("A", "A");k1.put("B", "B");k1.put("C", "C");					
		LinkedHashMap<Object, String> k2 = new LinkedHashMap<Object, String>();
		k2.put("1", "1");k2.put("1*", "1*");k2.put("1**", "1**");
		k2.put("2", "2");k2.put("2*", "2*");k2.put("2**", "2**");
		k2.put("3", "3");k2.put("3*", "3*");k2.put("3**", "3**");
		k2.put("4", "4");k2.put("4*", "4*");k2.put("4**", "4**");
		MyTable diagnostik = new MyTable("Krankheitsbilder", new String[]{"Referenz","Agens","AgensDetail","Risikokategorie_CDC","BioStoffV",
				"Krankheit","Symptome",
				"Zielpopulation","Aufnahmeroute","Krankheitsverlauf",
				"Risikogruppen",
				"Inzidenz","Inzidenz_Alter",
				"Inkubationszeit","IZ_Einheit",
				"Symptomdauer","SD_Einheit",
				"Infektionsdosis","ID_Einheit",
				"Letalitaetsdosis50","LD50_Einheit","LD50_Organismus","LD50_Aufnahmeroute",
				"Letalitaetsdosis100","LD100_Einheit","LD100_Organismus","LD100_Aufnahmeroute",
				"Meldepflicht",
				"Morbiditaet","Mortalitaet",
				"Letalitaet","Therapie_Letal","Ausscheidungsdauer",
				"ansteckend","Therapie","Antidot","Impfung","Todeseintritt",
				"Spaetschaeden","Komplikationen"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","VARCHAR(10)","VARCHAR(10)",
				"INTEGER","INTEGER",
				"VARCHAR(100)","VARCHAR(255)","VARCHAR(50)",
				"INTEGER",
				"DOUBLE","DOUBLE",
				"DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)","VARCHAR(100)","VARCHAR(255)",
				"DOUBLE","VARCHAR(50)","VARCHAR(100)","VARCHAR(255)",
				"BOOLEAN",
				"DOUBLE","DOUBLE",
				"DOUBLE","INTEGER","VARCHAR(50)",
				"BOOLEAN","BOOLEAN","BOOLEAN","BOOLEAN","VARCHAR(50)",
				"VARCHAR(255)","VARCHAR(255)"
				},
				new String[]{"Referenz - Verweis auf Tabelle Literatur","Agens - Verweis auf Tabelle Agenzien","Details zum Erreger, die durch den Katalog nicht abgebildet werden, z.B. Serovartyp","Risikokategorie laut Einstufung des Centers for Disease Control and Prevention (CDC)","Schutzstufen gem�� Verordnung �ber Sicherheit und Gesundheitsschutz bei T�tigkeiten mit biologischen Arbeitsstoffen (Biostoffverordnung - BioStoffV)",
				"Bezeichnung der Krankheit, wenn m�glich Verweis auf die Internationale Klassifikation der Krankheiten 10. Revision (ICD10-Kodes)","Auswahl aus hinterlegtem Katalog mit der M�glichkeit, neue Symptome einzuf�gen; Mehrfachnennungen m�glich",
				"Zielpopulation","Aufnahmeroute","Art des Krankheitsverlaufs",
				"Risikogruppen - Mehrfachnennungen m�glich",
				"Anzahl der Neuerkrankungsf�lle/100.000/Jahr in Deutschland","Angabe der Altersgruppe, falls die Inzidenz altersbezogen angegeben ist",
				"Zeitraum von Aufnahme des Agens bis zum Auftreten des/r ersten Symptome/s","Inkubationszeit-Einheit",
				"Symptomdauer","Symptomdauer-Einheit",
				"Infektionsdosis oraler Aufnahme","Infektionsdosis-Einheit",
				"Letalit�tsdosis LD50: mittlere Dosis einer Substanz, die bei 50 % der Exponierten zum Tode f�hrt","Letalit�tsdosis50-Einheit","Bei welchem Organismus wurden die Untersuchungen zu LD50 gemacht?","Welche Aufnahmeroute wurde gew�hlt?",
				"Letalit�tsdosis LD100: mittlere Dosis einer Substanz, die bei 100 % der Exponierten zum Tode f�hrt","Letalit�tsdosis100-Einheit","Bei welchem Organismus wurden die Untersuchungen zu LD100 gemacht?","Welche Aufnahmeroute wurde gew�hlt?",
				"Meldepflicht nach Infektionsschutzgesetz",
				"Krankheitsh�ufigkeit pro Jahr in Deutschland, falls nicht im Kommentarfeld anders vermerkt - Angabe in Prozent","Prozentualer Anteil der Todesf�lle, bezogen auf die Gesamtzahl der Bev�lkerung in Deutschland, falls nicht im Kommentarfeld anders vermerkt",
				"Verh�ltnis der Todesf�lle zur Anzahl der Erkrankten, angegeben in Prozent","Letalit�t: mit bzw. ohne Therapie","Ungef�hre, m�gliche Dauer des Ausscheidens des Erregers",
				"Krankheit ist von Mensch zu Mensch �bertragbar","Therapiem�glichkeit besteht","Antidotgabe m�glich","Schutzimpfung verf�gbar","M�glicher Zeitraum vom Symptombeginn bis zum Eintritt des Todes",
				"M�gliche Sp�tsch�den","M�gliche ung�nstige Beeinflussung oder Verschlimmerung des Krankheitszustandes"},
				new MyTable[]{literatur,agenzien,null,null,null,
				krankheiten,symptome,
				null,null,null,
				risikogruppen,
				newDoubleTable,newDoubleTable,
				newDoubleTable,null,
				newDoubleTable,null,
				newDoubleTable,null,
				newDoubleTable,null,null,null,
				newDoubleTable,null,null,null,
				null,
				newDoubleTable,newDoubleTable,
				newDoubleTable,null,null,
				null,null,null,null,null,
				null,null},
				null,
				new LinkedHashMap[]{null,null,null,k1,k2,
				null,null,
				h1,h2,h3,
				null,
				null,null,
				null,hashZeit,
				null,hashZeit,
				null,hashDosis,
				null,hashDosis,h1,h2,
				null,hashDosis,h1,h2,
				hYNB,
				null,null,
				null,hYNT,hashZeit,
				hYNB,hYNB,hYNB,hYNB,hashZeit,
				null,null},
				new String[]{null,null,null,null,null,
				null,"Krankheitsbilder_Symptome",
				null,null,null,
				"Krankheitsbilder_Risikogruppen",
				null,null,
				null,null,
				null,null,
				null,null,
				null,null,null,null,
				null,null,null,null,
				null,
				null,null,
				null,null,null,
				null,null,null,null,null,
				null,null});
		myList.addTable(diagnostik, MyList.Krankheitsbilder_LIST);
		MyTable krankheitsbildersymptome = new MyTable("Krankheitsbilder_Symptome",
				new String[]{"Krankheitsbilder","Symptome"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{diagnostik,symptome},
				new LinkedHashMap[]{null,null});
		myList.addTable(krankheitsbildersymptome, -1);
		MyTable krankheitsbilderrisikogruppen = new MyTable("Krankheitsbilder_Risikogruppen",
				new String[]{"Krankheitsbilder","Risikogruppen"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{diagnostik,risikogruppen},
				new LinkedHashMap[]{null,null});
		myList.addTable(krankheitsbilderrisikogruppen, -1);
		MyTable agensmatrices = new MyTable("Agenzien_Matrices", // ,"nat�rliches Vorkommen in Lebensmitteln in D"
				new String[]{"Agens","Matrix","Referenz"},
				new String[]{"INTEGER","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{agenzien,matrix,literatur},
				new LinkedHashMap[]{null,null,null});
		myList.addTable(agensmatrices, MyList.Krankheitsbilder_LIST);
		
		MyTable zertifikate = new MyTable("Zertifizierungssysteme", new String[]{"Bezeichnung","Abkuerzung","Anbieter"},
				new String[]{"VARCHAR(255)","VARCHAR(20)","INTEGER"},
				new String[]{"Vollst�ndiger Name zum Zertifizierungssystem","Abk�rzung f�r Zertifizierungssystem","Anbieter des Zertifizierungssystems - Verweis auf die Kontakttabelle"},
				new MyTable[]{null,null,adressen});
		myList.addTable(zertifikate, DBKernel.isKNIME ? -1 : MyList.BasisTabellen_LIST);
		
		MyTable methodiken = new MyTable("Methodiken", new String[]{"Name","Beschreibung","Kurzbezeichnung","WissenschaftlicheBezeichnung","Katalogcodes"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(30)","VARCHAR(255)","INTEGER"},
				new String[]{"Name der Methodik","Beschreibung der Methodik",null,null,"Methodenkatalog - Codes"},
				new MyTable[]{null,null,null,null,null},
				null,
				null,
				new String[]{null,null,null,null,"INT"});
		myList.addTable(methodiken, -1);
		MyTable methodiken_OG = new MyTable("Codes_Methodiken", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(20)","INTEGER"},
				new String[]{"Codebezeichnung","Hierarchischer Code","Zugeh�rige Methode"},
				new MyTable[]{null,null,methodiken},
				new String[][]{{"CodeSystem","Code"}});
		myList.addTable(methodiken_OG, -1); // -1
		methodiken.setForeignField(methodiken_OG, 4);
		h1 = new LinkedHashMap<Object, String>();
		h1.put("NRL", "NRL"); h1.put("Konsiliarlabor", "Konsiliarlabor"); h1.put("staatlich", "staatlich"); h1.put("GPV", "GPV"); h1.put("privat", "privat"); h1.put("sonstiges", "sonstiges");	// GPV = Gegenprobensachverst�ndiger	
		MyTable labore = new MyTable("Labore", new String[]{"Kontakt","HIT_Nummer","ADV_Nummer",
				"privat_staatlich","Matrices","Untersuchungsart","Agenzien"},
				new String[]{"INTEGER","BIGINT","VARCHAR(10)",
				"VARCHAR(20)","INTEGER","INTEGER","INTEGER"},
				new String[]{"Verweis auf die Kontakttabelle - Tabelle enth�lt auch Betriebslabore","HIT-Nummer","ADV-Nummer",
				"Ist das Labor privat, staatlich, oder sogar ein NRL (Nationales Referenz Labor) oder ein GPV (Gegenprobensachverst�ndigen Labor) oder etwas anderes?","Matrices, auf die das Labor spezialisiert ist. Mehrfachnennungen m�glich.","Art der Untersuchung, Methoden. Mehrfachnennungen m�glich.","Agenzien, die das Labor untersucht und f�r die die genutzten Methodiken bekannt sind. Mehrfachnennungen m�glich."},
				new MyTable[]{adressen,null,null,null,matrix,methodiken,agenzien},
				new String[][]{{"HIT_Nummer"},{"ADV_Nummer"}},
				new LinkedHashMap[]{null,null,null,h1,null,null,null},
				new String[]{null,null,null,null,"Labore_Matrices","Labore_Methodiken","Labore_Agenzien"});
		myList.addTable(labore, DBKernel.isKNIME ? -1 : MyList.BasisTabellen_LIST);
		MyTable labore_Methodiken = new MyTable("Labore_Methodiken",
				new String[]{"Labore","Methodiken"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{labore,methodiken},
				new LinkedHashMap[]{null,null});
		myList.addTable(labore_Methodiken, -1);
		MyTable labore_Matrices = new MyTable("Labore_Matrices",
				new String[]{"Labore","Matrices"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{labore,matrix},
				new LinkedHashMap[]{null,null});
		myList.addTable(labore_Matrices, -1);
		MyTable labore_Agenzien = new MyTable("Labore_Agenzien",
				new String[]{"Labore","Agenzien","Methodiken"},
				new String[]{"INTEGER","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{labore,agenzien,methodiken},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,"Labore_Agenzien_Methodiken"});
		myList.addTable(labore_Agenzien, -1);
		MyTable labore_Agenzien_Methodiken = new MyTable("Labore_Agenzien_Methodiken",
				new String[]{"Labore_Agenzien","Methodiken"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{labore_Agenzien,methodiken},
				new LinkedHashMap[]{null,null});
		myList.addTable(labore_Agenzien_Methodiken, -1);

		
		MyTable Konzentrationseinheiten = new MyTable("Einheiten", new String[]{"Einheit","Beschreibung"},
				new String[]{"VARCHAR(255)","VARCHAR(255)"},
				new String[]{null,null},
				new MyTable[]{null,null},
				null,
				new LinkedHashMap[]{null,null},
				new String[]{null,null});
		myList.addTable(Konzentrationseinheiten, -1);
		MyTable SonstigeParameter = new MyTable("SonstigeParameter", new String[]{"Parameter","Beschreibung"},
				new String[]{"VARCHAR(255)","VARCHAR(255)"},
				new String[]{null,null},
				new MyTable[]{null,null},
				null,
				new LinkedHashMap[]{null,null},
				new String[]{null,null});
		myList.addTable(SonstigeParameter, -1);
		h1 = new LinkedHashMap<Object, String>();
	    h1.put("Fest", "Fest"); h1.put("Fl�ssig", "Fl�ssig"); h1.put("Gasf�rmig", "Gasf�rmig");		
		//min, avg, max
		

		MyTable kits = new MyTable("Kits", new String[]{"Bezeichnung","Testanbieter","ZertifikatNr","Gueltigkeit","Zertifizierungssystem","AnbieterAngebot","Kosten","KostenEinheit",
				"Einheiten","Probenmaterial","Aufbereitungsverfahren","Nachweisverfahren",
				"Extraktionssystem_Bezeichnung","DNA_Extraktion","RNA_Extraktion","Protein_Extraktion","Extraktionstechnologie",
				"Quantitativ","Identifizierung","Typisierung",
				"Methoden","Matrix","MatrixDetail","Agens","AgensDetail",
				"Spezialequipment","Laienpersonal",
				"Format","Katalognummer"},
				new String[]{"VARCHAR(50)","INTEGER","VARCHAR(50)","DATE","INTEGER","BLOB(10M)","DOUBLE","VARCHAR(50)",
				"INTEGER","VARCHAR(255)","BOOLEAN","BOOLEAN",
				"VARCHAR(255)","BOOLEAN","BOOLEAN","BOOLEAN","VARCHAR(255)",
				"BOOLEAN","BOOLEAN","BOOLEAN",
				"INTEGER","INTEGER","VARCHAR(255)","INTEGER","VARCHAR(255)",
				"BOOLEAN","BOOLEAN",
				"VARCHAR(255)","VARCHAR(255)"},
				new String[]{"Bezeichnung des Kits","Verweis auf Eintrag in Kontakttabelle - falls Testanbieter vorhanden","Zertifikatnummer - falls vorhanden","G�ltigkeitsdatum des Zertifikats - falls vorhanden","Zertifizierungsanbieter - Verweis auf Tabelle Zertifizierungssysteme","Das Angebot kann ein individuelles Angebot, ein Katalogeintrag, eine E-Mail oder auch ein anderes Dokument des Testanbieters sein, m�glicherweise auch mit Angabe der G�ltigkeit des Angebots","Kosten f�r das Kit - Angabe ohne Mengenrabbatte. Gesch�tzte Materialkosten, falls inhouse","W�hrung f�r die Kosten - Auswahlbox",
				"Anzahl der Kits pro Bestellung",null,null,null,
				null,null,null,null,null,
				"Quantitativ",null,null,
				null,null,null,null,null,
				null,null,
				null,null},
				new MyTable[]{null,adressen,null,null,zertifikate,null,null,null,
				null,null,null,null,
				null,null,null,null,null,
				null,null,null,
				methodiken,matrix,null,agenzien,null,
				null,null,
				null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,null,hashGeld,
				null,null,hYNB,hYNB,
				null,null,null,null,null,
				hYNB,hYNB,hYNB,
				null,null,null,null,null,
				hYNB,hYNB,
				null,null});
		myList.addTable(kits, MyList.Nachweissysteme_LIST);
/*		
		MyTable aufbereitungsverfahren = new MyTable("Aufbereitungsverfahren", new String[]{"Methode","SOP/LA","Aufkonzentrierung","DNA Extraktion","RNA Extraktion","Protein Extraktion"},
				new String[]{"INTEGER","BLOB(10M)","BOOLEAN","BOOLEAN","BOOLEAN","BOOLEAN"},
				new String[]{"Verweis auf Eintrag in Methoden","Standard Operating Procedure oder Laboranweisung - falls vorhanden",null,null,null,null},
				new MyTable[]{methoden,null,null,null,null,null});
		myList.addTable(aufbereitungsverfahren, 5);
		MyTable matrix_aufbereitungsverfahren = new MyTable("Matrix_Aufbereitungsverfahren", new String[]{"Matrix","MatrixDetail","Aufbereitungsverfahren","Kits","Dauer","DauerEinheit","DauerReferenz","Personalressourcen","ZeitEinheit","ValidierungReferenz","Kosten","KostenEinheit"},
				new String[]{"INTEGER","VARCHAR(255)","INTEGER","INTEGER","DOUBLE","VARCHAR(50)","INTEGER","DOUBLE","VARCHAR(50)","INTEGER","DOUBLE","VARCHAR(50)"},
				new String[]{"Verweis auf Matrix in Matrixtabelle","Details zur Matrix, die durch den Katalog nicht abgebildet werden","Verweis auf Eintrag in Aufbereitungsverfahren Tabelle","Notwendige Kits - hier k�nnen mehrere Kits ausgew�hlt werden","Dauer des Verfahrens (von Beginn bis Vorliegen des Ergebnisses)","Zeiteinheit der Dauer","Verweis auf Eintrag in Literaturtabelle - falls inhouse gemessen, muss das Laborbuch o.�. in der Literaturtabelle eingetragen werden. Falls keine Angabe gilt der Wert als gesch�tzt!","Wie gro� ist der durchschnittliche TA-Zeitaufwand zur Durchf�hrung des Nachweisverfahrens - gesch�tzt?","Zeiteinheit f�r Zeitaufwand f�r Personal - Auswahlbox","Angabe ob unabh�ngige Validierung durchgef�hrt wurde, Verweis auf die Literaturstelle - falls inhouse validiert, muss das Laborbuch, der Validierungsbericht o.�. in der Literaturtabelle eingetragen werden","Gesch�tzte Materialkosten, zus�tzlich zu den Kitkosten","W�hrung f�r die Kosten - Auswahlbox"},
				new MyTable[]{matrix,null,aufbereitungsverfahren,kits,null,null,literatur,null,null,literatur,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,hashZeit,null,null,hashZeit,null,null,hashGeld},
				new String[]{null,null,null,"Aufbereitungsverfahren_Kits",null,null,null,null,null,null,null,null});
		myList.addTable(matrix_aufbereitungsverfahren, 5);
		*/
		MyTable aufbereitungsverfahren = new MyTable("Aufbereitungsverfahren",
				new String[]{"Bezeichnung","Kurzbezeichnung","WissenschaftlicheBezeichnung",
				"Aufkonzentrierung","DNA_Extraktion","RNA_Extraktion","Protein_Extraktion",
				"Homogenisierung","Zelllyse",
				"Matrix","MatrixDetail","Agens","AgensDetail",
				"Kits","Dauer","DauerEinheit","Personalressourcen","ZeitEinheit",
				"Kosten","KostenEinheit",
				"Normen","SOP_LA","Spezialequipment","Laienpersonal","Referenz"},
				new String[]{"VARCHAR(255)","VARCHAR(30)","VARCHAR(255)",
				"BOOLEAN","BOOLEAN","BOOLEAN","BOOLEAN",
				"BOOLEAN","BOOLEAN",
				"INTEGER","VARCHAR(255)","INTEGER","VARCHAR(255)",
				"INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"INTEGER","BLOB(10M)","BOOLEAN","BOOLEAN","INTEGER"},
				new String[]{null,null,null,null,null,null,null,null,null,
				"Verweis auf Matrix in Matrixtabelle","Details zur Matrix, die durch den Katalog nicht abgebildet werden",
				"Verweis auf Eintrag in Agens-Tabelle","Details zum Erreger, die durch den Katalog nicht abgebildet werden, z.B. welches Serovar",
				"Notwendige Kits - hier k�nnen mehrere Kits ausgew�hlt werden","Dauer des Verfahrens (von Beginn bis Vorliegen des Ergebnisses)","Zeiteinheit der Dauer","Wie gro� ist der durchschnittliche TA-Zeitaufwand zur Durchf�hrung des Nachweisverfahrens - gesch�tzt?","Zeiteinheit f�r Zeitaufwand f�r Personal - Auswahlbox",
				"Gesch�tzte Materialkosten, zus�tzlich zu den Kitkosten","W�hrung f�r die Kosten - Auswahlbox",
				"Zugeh�rige Normen, z.B. ISO, DIN, CEN, etc.","Standard Operating Procedure oder Laboranweisung - falls vorhanden","Wird f�r das Verfahren Spezialequipment ben�tigt? Details bitte ins Kommentarfeld eintragen","Kann das Verfahren ohne Fachpersonal durchgef�hrt werden? Details bitte ins Kommentarfeld eintragen","Referenz, gegebenenfalls Laborbuch"},
				new MyTable[]{null,null,null,null,null,null,null,null,null,
				matrix,null,agenzien,null,
				kits,null,null,null,null,
				null,null,
				normen,null,null,null,literatur},
				null,
				new LinkedHashMap[]{null,null,null,
				null,null,null,null,
				null,null,
				null,null,null,null,
				null,null,hashZeit,null,hashZeit,
				null,hashGeld,
				null,null,null,null,null},
				new String[]{null,null,null,
				null,null,null,null,
				null,null,
				null,null,null,null,
				"Aufbereitungsverfahren_Kits",null,null,null,null,
				null,null,
				"Aufbereitungsverfahren_Normen",null,null,null,null});
		myList.addTable(aufbereitungsverfahren, MyList.Nachweissysteme_LIST);
		MyTable aufbereitungsverfahren_Kits = new MyTable("Aufbereitungsverfahren_Kits",
				new String[]{"Aufbereitungsverfahren","Kits"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{aufbereitungsverfahren,kits},
				new LinkedHashMap[]{null,null});
		myList.addTable(aufbereitungsverfahren_Kits, -1);
		MyTable aufbereitungsverfahren_Normen = new MyTable("Aufbereitungsverfahren_Normen",
				new String[]{"Aufbereitungsverfahren","Normen","Norm_Nummer"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{aufbereitungsverfahren,normen,null},
				new LinkedHashMap[]{null,null,null});
		myList.addTable(aufbereitungsverfahren_Normen, -1);
/*		
		MyTable nachweisverfahren = new MyTable("Nachweisverfahren", new String[]{"Methode","SOP/LA","Quantitativ","Identifizierung","Typisierung"},
				new String[]{"INTEGER","BLOB(10M)","BOOLEAN","BOOLEAN","BOOLEAN"},
				new String[]{"Verweis auf Eintrag in Methodentabelle","Standard Operating Procedure oder Laboranweisung - falls vorhanden","Handelt es sich um eine quantitative Methode? Falls nein, ist es automatisch eine qualitative Methode!","Handelt es sich um eine Methode zur Identifizierung des Agens?","Handelt es sich um eine Methode zur Typisierung des Agens?"},
				new MyTable[]{methoden,null,null,null,null});
		myList.addTable(nachweisverfahren, 5);
		MyTable agens_nachweisverfahren = new MyTable("Agens_Nachweisverfahren", new String[]{"Agens","AgensDetail","Nachweisverfahren","Kits","Kosten","KostenEinheit"},
				new String[]{"INTEGER","VARCHAR(255)","INTEGER","INTEGER","DOUBLE","VARCHAR(50)"},
				new String[]{"Verweis auf Eintrag in Agens-Tabelle","Details zum Erreger, die durch den Katalog nicht abgebildet werden, z.B. welches Serovar","Verweis auf Eintrag in Nachweisverfahren Tabelle","Notwendige Kits - hier k�nnen mehrere Kits ausgew�hlt werden","Gesch�tzte Materialkosten, zus�tzlich zu den Kitkosten","W�hrung f�r die Kosten - Auswahlbox"},
				new MyTable[]{agenzien,null,nachweisverfahren,kits,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,hashGeld},
				new String[]{null,null,null,"Nachweisverfahren_Kits",null,null});
		myList.addTable(agens_nachweisverfahren, 5);
		*/
		MyTable nachweisverfahren = new MyTable("Nachweisverfahren",
				new String[]{"Bezeichnung",
				"Quantitativ","Identifizierung","Typisierung",
				"Methoden",
				"Matrix","MatrixDetail",
				"Agens","AgensDetail",
				"Kits","Dauer","DauerEinheit","Personalressourcen","ZeitEinheit",
				"Kosten","KostenEinheit",
				"Normen","SOP_LA","Spezialequipment","Laienpersonal","Referenz"},
				new String[]{"VARCHAR(255)",
				"BOOLEAN","BOOLEAN","BOOLEAN",
				"INTEGER",
				"INTEGER","VARCHAR(255)",
				"INTEGER","VARCHAR(255)",
				"INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"INTEGER","BLOB(10M)","BOOLEAN","BOOLEAN","INTEGER"},
				new String[]{null,"Handelt es sich um eine quantitative Methode? Falls nein, ist es automatisch eine qualitative Methode!","Handelt es sich um eine Methode zur Identifizierung des Agens?","Handelt es sich um eine Methode zur Typisierung des Agens?",
				"Methoden. Verweis auf Tabelle Methodiken",
				"Verweis auf Matrix in Matrixtabelle","Details zur Matrix, die durch den Katalog nicht abgebildet werden",
				"Verweis auf Eintrag in Agens-Tabelle","Details zum Erreger, die durch den Katalog nicht abgebildet werden, z.B. welches Serovar",
				"Notwendige Kits - hier k�nnen mehrere Kits ausgew�hlt werden","Dauer des Verfahrens (von Beginn bis Vorliegen des Ergebnisses)","Zeiteinheit der Dauer","Wie gro� ist der durchschnittliche TA-Zeitaufwand zur Durchf�hrung des Nachweisverfahrens - gesch�tzt?","Zeiteinheit f�r Zeitaufwand f�r Personal - Auswahlbox",
				"Gesch�tzte Materialkosten, zus�tzlich zu den Kitkosten","W�hrung f�r die Kosten - Auswahlbox",
				"Zugeh�rige Normen, z.B. ISO, DIN, CEN, etc.","Standard Operating Procedure oder Laboranweisung - falls vorhanden","Wird f�r das Verfahren Spezialequipment ben�tigt? Details bitte ins Kommentarfeld eintragen","Kann das Verfahren ohne Fachpersonal durchgef�hrt werden? Details bitte ins Kommentarfeld eintragen","Referenz, gegebenenfalls Laborbuch"},
				new MyTable[]{null,
				null,null,null,
				methodiken,
				matrix,null,
				agenzien,null,
				kits,null,null,null,null,
				null,null,
				normen,null,null,null,literatur},
				null,
				new LinkedHashMap[]{null,
				null,null,null,
				null,
				null,null,
				null,null,
				null,null,hashZeit,null,hashZeit,
				null,hashGeld,null,null,null},
				new String[]{null,
				null,null,null,
				null,
				null,null,
				null,null,
				"Nachweisverfahren_Kits",null,null,null,null,
				null,null,
				"Nachweisverfahren_Normen",null,null,null,null});
		myList.addTable(nachweisverfahren, MyList.Nachweissysteme_LIST);
		MyTable nachweisverfahren_Kits = new MyTable("Nachweisverfahren_Kits",
				new String[]{"Nachweisverfahren","Kits"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{nachweisverfahren,kits},
				new LinkedHashMap[]{null,null});
		myList.addTable(nachweisverfahren_Kits, -1);
		MyTable nachweisverfahren_Normen = new MyTable("Nachweisverfahren_Normen",
				new String[]{"Nachweisverfahren","Normen","Norm_Nummer"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{nachweisverfahren,normen,null},
				new LinkedHashMap[]{null,null,null});
		myList.addTable(nachweisverfahren_Normen, -1);
/*
		MyTable agens_nachweisverfahren_matrix_aufbereitungsverfahren = new MyTable("Agens_Nachweisverfahren_Matrix_Aufbereitungsverfahren",
				new String[]{"Referenz","Matrix_Aufbereitungsverfahren","Agens_Nachweisverfahren","Reale Nachweisgrenze","*10^n","NG-Einheit","Sensitivit�t","Spezifit�t","Effizienz","Dauer","DauerEinheit","Personalressourcen","ZeitEinheit","SOP/LA"},
				new String[]{"INTEGER","INTEGER","INTEGER","DOUBLE","DOUBLE","INTEGER","DOUBLE","DOUBLE","DOUBLE","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)","BLOB(10M)"},
				new String[]{"Referenz f�r alle Angaben im Datensatz","Verweis auf Matrix in Kombi-Tabelle Matrix * Aufbereitung","Verweis auf Eintrag in Kombi-Tabelle Nachweisverfahren * Agens","Reale Nachweisgrenze des Verfahrens bezogen auf die Konzentration des Agens auf/in der Ausgangsmatrix","Exponent zur Basis 10, falls vorhanden; Beispiel 1.3*10^-4 : 1.3 wird in der Spalte der Nachweisgrenze eingetragen und -4 in dieser Spalte","Einheit der Konzentration der Nachweisgrenze - Auswahlbox","Mittlere zu erwartende Sensitivit�t (Angabe als Wert im Bereich 0 - 1) (95%= 0.95); Definition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Mittlere zu erwartende Spezifit�t (Angabe als Wert im Bereich 0 - 1) (95%= 0.95); Definition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Mittlere zu erwartende Effizienz (Angabe als Wert im Bereich 0 - 1) (95%= 0.95); Definition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Dauer des Nachweisverfahrens ohne Ber�cksichtigung des Aufbereitungsverfahrens","Zeiteinheit f�r Dauer des Nachweisverfahrens - Auswahlbox","Wie gro� ist der durchschnittliche TA-Zeitaufwand zur Durchf�hrung des Nachweisverfahrens - gesch�tzt?","Zeiteinheit f�r Zeitaufwand f�r Personal - Auswahlbox","Standard Operating Procedure oder Laboranweisung - falls vorhanden"},
				new MyTable[]{literatur,matrix_aufbereitungsverfahren,agens_nachweisverfahren,null,null,Konzentrationseinheiten,null,null,null,null,null,null,null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,hashZeit,null,hashZeit,null});
		myList.addTable(agens_nachweisverfahren_matrix_aufbereitungsverfahren, 5);
		MyTable labor_agens_nachweisverfahren_matrix_aufbereitungsverfahren = new MyTable("Labor_Agens_Nachweisverfahren_Matrix_Aufbereitungsverfahren",
				new String[]{"Labor","Agens_Nachweisverfahren_Matrix_Aufbereitungsverfahren","ZertifikatNr","G�ltigkeit","Zertifizierungssystem","Durchsatz","DurchsatzEinheit","Kosten","KostenEinheit","FreigabeModus","AuftragsAnnahme","SOP","LaborAngebot"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)","DATE","INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)","INTEGER","BOOLEAN","BOOLEAN","BLOB(10M)"},
				new String[]{"Verweis zum Eintrag in Labor-Tabelle","Verweis zum Eintrag in Kombi-Tabelle Nachweis * Agens * Matrix * Aufbereit","Zertifikatnummer - falls vorhanden","G�ltigkeitsdatum des Zertifikats - falls vorhanden","Zertifizierungsanbieter - Verweis auf Tabelle Zertifizierungssysteme","Angaben zum Durchsatz des Labors f�r das Verfahren - sollte im LaborAngebot angegeben sein","Einheit des Durchsatzes - Auswahlbox","Kosten pro Probe/Einzelansatz - ohne Rabatte - sollte im LaborAngebot angegeben sein","W�hrung f�r die Kosten - Auswahlbox","Auswahl ob diese Information �ffentlich zug�nglich sein soll: nie, nur in der Krise, immer - Auswahlbox","Nimmt das Labor auch externe Auftr�ge an?","Existiert eine SOP zu dem Verfahren bei dem Labor?","Das Angebot kann ein individuelles Angebot, ein Katalogeintrag, eine E-Mail oder auch ein anderes Dokument des Labors sein, m�glicherweise auch mit Angabe der G�ltigkeit des Angebots"},
				new MyTable[]{labore,agens_nachweisverfahren_matrix_aufbereitungsverfahren,null,null,zertifikate,null,null,null,null,null,null,null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,hashSpeed,null,hashGeld,hashFreigabe,null,null,null});
		myList.addTable(labor_agens_nachweisverfahren_matrix_aufbereitungsverfahren, 5);
*/
		MyTable aufbereitungs_nachweisverfahren = new MyTable("Aufbereitungs_Nachweisverfahren",
				new String[]{"Aufbereitungsverfahren","Nachweisverfahren","Nachweisgrenze","NG_Einheit","Sensitivitaet","Spezifitaet","Effizienz","Wiederfindungsrate","Referenz"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","DOUBLE","DOUBLE","DOUBLE","DOUBLE","INTEGER"},
				new String[]{null,null,"Nachweisgrenze des Verfahrens bezogen auf die Konzentration des Agens auf/in der Ausgangsmatrix","Einheit der Konzentration der Nachweisgrenze - Auswahlbox","Mittlere zu erwartende Sensitivit�t (Angabe als Wert im Bereich 0 - 1) (95%= 0.95)\nDefinition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Mittlere zu erwartende Spezifit�t (Angabe als Wert im Bereich 0 - 1) (95%= 0.95)\nDefinition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Mittlere zu erwartende Effizienz (Angabe als Wert im Bereich 0 - 1) (95%= 0.95)\nDefinition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Wiederfindungsrate","Referenz f�r alle Angaben im Datensatz"},
				new MyTable[]{aufbereitungsverfahren,nachweisverfahren,newDoubleTable,Konzentrationseinheiten,null,null,null,null,literatur});
		myList.addTable(aufbereitungs_nachweisverfahren, MyList.Nachweissysteme_LIST);

		MyTable labor_aufbereitungs_nachweisverfahren = new MyTable("Labor_Aufbereitungs_Nachweisverfahren",
				new String[]{"Labor","Aufbereitungs_Nachweisverfahren","ZertifikatNr","Gueltigkeit","Zertifizierungssystem","Durchsatz","DurchsatzEinheit","Kosten","KostenEinheit","FreigabeModus","AuftragsAnnahme","SOP","LaborAngebot"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)","DATE","INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)","INTEGER","BOOLEAN","BOOLEAN","BLOB(10M)"},
				new String[]{"Verweis zum Eintrag in Labor-Tabelle","Verweis zum Eintrag in Kombi-Tabelle Aufbereitungs_Nachweisverfahren","Zertifikatnummer - falls vorhanden","G�ltigkeitsdatum des Zertifikats - falls vorhanden","Zertifizierungsanbieter - Verweis auf Tabelle Zertifizierungssysteme","Angaben zum Durchsatz des Labors f�r das Verfahren - sollte im LaborAngebot angegeben sein","Einheit des Durchsatzes - Auswahlbox","Kosten pro Probe/Einzelansatz - ohne Rabatte - sollte im LaborAngebot angegeben sein","W�hrung f�r die Kosten - Auswahlbox","Auswahl ob diese Information �ffentlich zug�nglich sein soll: nie, nur in der Krise, immer - Auswahlbox","Nimmt das Labor auch externe Auftr�ge an?","Existiert eine SOP zu dem Verfahren bei dem Labor?","Das Angebot kann ein individuelles Angebot, ein Katalogeintrag, eine E-Mail oder auch ein anderes Dokument des Labors sein, m�glicherweise auch mit Angabe der G�ltigkeit des Angebots"},
				new MyTable[]{labore,aufbereitungs_nachweisverfahren,null,null,zertifikate,null,null,null,null,null,null,null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,hashSpeed,null,hashGeld,hashFreigabe,null,null,null});
		myList.addTable(labor_aufbereitungs_nachweisverfahren, MyList.Nachweissysteme_LIST);

	
		h1 = new LinkedHashMap<Object, String>();
	    h1.put("in", "in"); h1.put("on", "on");	
	    /*
		h2 = new LinkedHashMap<Object, String>();
	    h2.put("BfR", "BfR");	
	    h2.put("ComBase", "ComBase");
	    h2.put("FLI", "FLI");	
	    h2.put("MRI", "MRI");	
	    h2.put("Andere", "Andere");	
	    */
		MyTable tenazity_raw_data = new MyTable("Versuchsbedingungen", new String[]{"Referenz","Agens","AgensDetail","Matrix","EAN","MatrixDetail",
				"Messwerte",
				"Temperatur","pH","aw","CO2","Druck","Luftfeuchtigkeit",
				"in_on","Sonstiges","Nachweisverfahren","FreigabeModus",
				"ID_CB","Organismus_CB","environment_CB","b_f_CB","b_f_details_CB"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","INTEGER","VARCHAR(255)","VARCHAR(255)",
				"INTEGER",
				"DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE",
				"CHAR(2)","INTEGER","INTEGER","INTEGER",
				"VARCHAR(50)","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(255)"},
				new String[]{"Verweis auf die zugeh�rige Literatur","Verweis auf den Erregerkatalog","Details zum Erreger, die durch den Katalog nicht abgebildet werden, z.B. Stamm, Serovar","Auswahl der Matrix","EAN-Nummer aus SA2-Datenbank - falls bekannt","Details zur Matrix, die durch den Katalog nicht abgebildet werden",
				"zugeh�rige Messwerte",
				"Experimentelle Bedingung: Temperatur in Grad Celcius","Experimentelle Bedingung: pH-Wert","Experimentelle Bedingung: aw-Wert","Experimentelle Bedingung: CO2 [ppm]","Experimentelle Bedingung: Druck [bar]","Experimentelle Bedingung: Luftfeuchtigkeit [%]",
				"Auf der Oberfl�che oder in der Matrix drin gemessen bzw. entnommen? - Auswahlbox (auf / innen)","Sonstige experimentelle Rahmenbedingungen in der Umgebung. Aber auch Facetten der Matrix.\nEs �ffnet sich ein Fenster, in dem an die Combase angelehnte Parameter eingetragen werden k�nnen, vgl. Feld condition in der Combase:\nhttp://www.combase.cc/CB_TechnDescription.pdf","Das benutzte Nachweisverfahren","Auswahl ob diese Information �ffentlich zug�nglich sein soll: nie, nur in der Krise, immer - Auswahlbox",
				"Eindeutige ID aus der Combase Datenbank - bei eigenen Eintr�gen bleibt das Feld leer","Eintrag aus der Combase Datenbank - bei eigenen Eintr�gen bleibt das Feld leer","Eintrag aus der Combase Datenbank - bei eigenen Eintr�gen bleibt das Feld leer","Eintrag aus der Combase Datenbank - bei eigenen Eintr�gen bleibt das Feld leer","Eintrag aus der Combase Datenbank - bei eigenen Eintr�gen bleibt das Feld leer"},
				new MyTable[]{literatur,agenzien,null,matrix,null,null,
				null,
				newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,
				null,SonstigeParameter,aufbereitungs_nachweisverfahren,null,
				null,null,null,null,null},
				new String[][]{{"ID_CB"}},
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,null,null,
					null,h1,null,null,hashFreigabe,null,null,null,null,null},
				new String[]{null,null,null,null,null,null,"INT",null,null,null,null,null,null,
					null,"Versuchsbedingungen_Sonstiges",null,null,null,null,null,null,null});
		myList.addTable(tenazity_raw_data, MyList.Tenazitaet_LIST);
		MyTable tenazity_measured_vals = new MyTable("Messwerte", new String[]{"Versuchsbedingungen","Zeit","ZeitEinheit",
				"Delta","Konzentration","Konz_Einheit",
				"Temperatur","pH","aw","CO2","Druck","Luftfeuchtigkeit",
				"Sonstiges"},
				new String[]{"INTEGER","DOUBLE","VARCHAR(50)",
				"BOOLEAN","DOUBLE","INTEGER",
				"DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE",
				"INTEGER"},
				new String[]{"Verweis auf die Tabelle mit den experimentellen Versuchsbedingungen","Vergangene Zeit nach Versuchsbeginn","Ma�einheit der Zeit - Auswahlbox",
				"Falls angehakt:\nin den folgenden Feldern sind die Ver�nderungen der Konzentration des Erregers im Vergleich zum Startzeitpunkt eingetragen.\nDabei bedeutet eine positive Zahl im Feld 'Konzentration' eine Konzentrationserh�hung, eine negative Zahl eine Konzentrationsreduzierung.","Konzentration des Erregers - Entweder ist die absolute Konzentration bzw. der Mittelwert bei Mehrfachmessungen hier einzutragen ODER die Konzentrations�nderung, falls das Delta-Feld angehakt ist","Einheit zu den Konzentrationsangaben, auch der Logarithmus ist hier ausw�hlbar - Auswahlbox",
				"Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: Temperatur in Grad Celcius","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: pH-Wert","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: aw-Wert","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: CO2 [ppm]","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: Druck [bar]","Experimentelle Bedingung, falls abweichend von den festen Versuchsbedingungen: Luftfeuchtigkeit [%]",
				"Sonstige experimentelle Rahmenbedingungen in der Umgebung, aber auch Facetten der Matrix, falls abweichend von den festen Versuchsbedingungen.\nEs �ffnet sich ein Fenster, in dem an die Combase angelehnte Parameter eingetragen werden k�nnen, vgl. Feld condition in der Combase:\nhttp://www.combase.cc/CB_TechnDescription.pdf"},
				new MyTable[]{tenazity_raw_data,newDoubleTable,null,null,newDoubleTable,Konzentrationseinheiten,
				newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,
				SonstigeParameter},
				null,
				new LinkedHashMap[]{null,null,hashZeit,null,null,null,null,null,null,null,
					null,null,null},
				new String[]{null,null,null,null,null,null,null,null,null,null,null,null,
					"Messwerte_Sonstiges"});
		myList.addTable(tenazity_measured_vals, MyList.Tenazitaet_LIST); // MyList.Tenazitaet_LIST
		tenazity_raw_data.setForeignField(tenazity_measured_vals, 6);

		MyTable Versuchsbedingungen_Sonstiges = new MyTable("Versuchsbedingungen_Sonstiges",
				new String[]{"Versuchsbedingungen","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{tenazity_raw_data,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null});
		myList.addTable(Versuchsbedingungen_Sonstiges, -1);
		MyTable Messwerte_Sonstiges = new MyTable("Messwerte_Sonstiges",
				new String[]{"Messwerte","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{tenazity_measured_vals,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null});
		myList.addTable(Messwerte_Sonstiges, -1);

		MyTable importedCombaseData = new MyTable("ImportedCombaseData",
				new String[]{"CombaseID","Literatur","Versuchsbedingung"},
				new String[]{"VARCHAR(100)","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{null,literatur,tenazity_raw_data},
				new String[][]{{"CombaseID","Literatur","Versuchsbedingung"}},
				new LinkedHashMap[]{null,null,null});
		myList.addTable(importedCombaseData, -1);

		// Prozessdaten:
		MyTable betriebe = new MyTable("Produzent", new String[]{"Kontaktadresse","Betriebsnummer"},
				new String[]{"INTEGER","VARCHAR(50)"},
				new String[]{"Verweis auf eintr�ge in Tabelle Kontakte mit Lebensmittel-Betrieben, Landwirten etc","Betriebsnummer aus BALVI-System sofern vorhanden"},
				new MyTable[]{adressen,null});
		myList.addTable(betriebe, -1);
		MyTable betrieb_matrix_produktion = new MyTable("Betrieb_Matrix_Produktion", new String[]{"Betrieb","Matrix","EAN","Produktionsmenge","Einheit","Referenz","Anteil","lose"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","DOUBLE","VARCHAR(50)","INTEGER","DOUBLE","BOOLEAN"},
				new String[]{"Verweis auf die Basistabelle der Betriebe","Verweis auf die Matricestabelle","EAN-Nummer aus SA2-Datenbank - falls bekannt","Produktionsmenge des Lebensmittels","Verweis auf Basistabelle Ma�einheiten","Verweis auf Literaturstelle","Anteil in %",null},
				new MyTable[]{betriebe,matrix,null,null,null,literatur,null,null},
				new LinkedHashMap[]{null,null,null,null,hashGewicht,null,null,null});
		myList.addTable(betrieb_matrix_produktion, -1);
		MyTable prozessElemente = new MyTable("ProzessElemente",
				new String[]{"Prozess_ID","ProzessElement","ProzessElementKategorie","ProzessElementSubKategorie","ProzessElement_engl","ProzessElementKategorie_engl","ProzessElementSubKategorie_engl"},
				new String[]{"INTEGER","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)"},
				new String[]{"Prozess_ID in CARVER","Bezeichnung des Vorgangs bei der Prozessierung","Bezeichnung f�r die Kategorie, in die der Prozess einzuordnen ist","Bezeichnung der Unterkategorie f�r eine genauere Spezifizierung des Vorgangs","Wie ProzessElement, aber englische Bezeichnung","Wie ProzessElementKategorie, aber englische Bezeichnung","Wie ProzessElementSubKategorie, aber englische Bezeichnung"},
				new MyTable[]{null,null,null,null,null,null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,null});
		myList.addTable(prozessElemente, MyList.Prozessdaten_LIST);
		h1 = new LinkedHashMap<Object, String>();
	    h1.put("EAN (betriebsspezifisch)", "EAN (betriebsspezifisch)");					
	    h1.put("Produktklasse (�berbetrieblich)", "Produktklasse (�berbetrieblich)");					
	    h1.put("Produktgruppe (�berbetrieblich und produkt�bergreifen)", "Produktgruppe (�berbetrieblich und produkt�bergreifen)");		
	    LinkedHashMap<Object, String> h4 = new LinkedHashMap<Object, String>();
	    h4.put(1, "Kilogramm");					
	    h4.put(2, "Gramm");					
	    h4.put(7, "Liter");					
	    h4.put(24, "Prozent (%)");					
	    h4.put(25, "Promille (�)");					
	    h4.put(35, "St�ck");					
	    
		MyTable prozessFlow = new MyTable("ProzessWorkflow",
				new String[]{"Name","Autor","Datum","Beschreibung","Firma","Produktmatrix","EAN","Prozessdaten","XML","Referenz"}, // ,"#Chargenunits","Unitmenge","UnitEinheit"
				new String[]{"VARCHAR(60)","VARCHAR(60)","DATE","VARCHAR(1023)","INTEGER","INTEGER","VARCHAR(255)","INTEGER","BLOB(10M)","INTEGER"}, // ,"DOUBLE","DOUBLE","INTEGER"
				new String[]{"Eigene Bezeichnung f�r den Workflow","Name des Eintragenden",null,"Beschreibung des Workflows in Prosa-Text","Verweis auf den Betrieb aus der Tabelle Produzent","Verweis auf Matrixkatalog","EAN-Nummer aus SA2-Datenbank - falls bekannt",null,"Ablage der CARVER XML Datei, die den Workflow abbildet",null}, // ,null,null,null
				new MyTable[]{null,null,null,null,betriebe,matrix,null,null,null,literatur}, // null,null,null,
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null}, // ,null,null,h4
				new String[]{null,null,null,null,null,null,null,"INT",null,"ProzessWorkflow_Literatur"}); // ,"DBL","DBL",null
		myList.addTable(prozessFlow, MyList.Prozessdaten_LIST);
		MyTable prozessFlowReferenzen = new MyTable("ProzessWorkflow_Literatur",
				new String[]{"ProzessWorkflow","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{prozessFlow,literatur},
				new LinkedHashMap[]{null,null});
		myList.addTable(prozessFlowReferenzen, -1);		
		
		MyTable Kostenkatalog = new MyTable("Kostenkatalog",
				new String[]{"Kostenart","Kostenunterart","Beschreibung","Einheit"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(25)"},
				new String[]{null,null,null,"Einheit pro Bezugseinheit (pro Liter Endprodukt)"},
				new MyTable[]{null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null},
				new String[]{null,null,null,null});
		myList.addTable(Kostenkatalog, -1);
		MyTable Kostenkatalogpreise = new MyTable("Kostenkatalogpreise",
				new String[]{"Kostenkatalog","Betrieb","Datum","Preis","Waehrung"},
				new String[]{"INTEGER","INTEGER","DATE","DOUBLE","VARCHAR(50)"},
				new String[]{null,null,"Preis wurde erhoben am...",null,null},
				new MyTable[]{Kostenkatalog,betriebe,null,newDoubleTable,null},
				null,
				new LinkedHashMap[]{null,null,null,null,hashGeld},
				new String[]{null,null,null,null,null});
		myList.addTable(Kostenkatalogpreise, DBKernel.getUsername().equals("burchardi") ? 66 : -1);

		MyTable prozessdaten = new MyTable("Prozessdaten",
				new String[]{"Referenz","Workflow","Bezugsgruppe","Prozess_CARVER","ProzessDetail",
				"Kapazitaet","KapazitaetEinheit","KapazitaetEinheitBezug",
				"Dauer","DauerEinheit",
				"Zutaten",
				"Temperatur","pH","aw","CO2","Druck","Luftfeuchtigkeit",
				"Sonstiges","Tenazitaet","Kosten"}, // "Luftfeuchtigkeit [%]","Kochsalzgehalt [%]",
				new String[]{"INTEGER","INTEGER","VARCHAR(60)","INTEGER","VARCHAR(255)",
				"DOUBLE","INTEGER","VARCHAR(50)",
				"DOUBLE","VARCHAR(50)",
				"INTEGER",
				"DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE",
				"INTEGER","INTEGER","INTEGER"},
				new String[]{"Verweise auf Eintr�ge aus der Tabelle Literatur, die diesen Prozessschritt beschreiben","Verweis auf einen Eintrag aus der Tabelle Workflow, zu dem dieser Prozessschritt geh�rt","Auswahlbox: EAN (betriebsspezifisch), Produktgruppe (�berbetrieblich), Produktklasse (�berbetrieblich)","Verweis auf einen Eintrag aus der Tabelle ProzessElemente, der den Prozesschritt benennt","DetailInformation zu diesem Prozessschritt",
				"Fassungsverm�gen des Prozesselements, z.B. Volumen, Gewicht",null,"Bei einem kontinuierlichen Prozess muss die zeitliche Bezugsgr��e angegeben werden. Bei einem abgeschotteten Prozess bleibt das Feld leer",
				"Dauer des Prozessschritts","Einheit der Dauer",
				"Verweis auf Eintrag aus der Tabelle Zutatendaten, der Menge und Art der Zutat spezifiziert",
				"Temperatur - in �C!!!",null,null,null,"Druck - in [bar]!!!","Luftfeuchtigkeit - Einheit bitte in [%]",
				"Sonstige experimentelle Rahmenbedingungen in der Umgebung. Aber auch Facetten der Matrix.\nEs �ffnet sich ein Fenster, in dem an die Combase angelehnte Parameter eingetragen werden k�nnen, vgl. Feld condition in der Combase:\nhttp://www.combase.cc/CB_TechnDescription.pdf",
				"Tenazit�tsdaten, falls vorliegend",null},
				new MyTable[]{literatur,prozessFlow,null,prozessElemente,null,
				newDoubleTable,null,null,
				newDoubleTable,null,
				null,
				newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,
				SonstigeParameter, agenzien, Kostenkatalog},
				null,
				new LinkedHashMap[]{null,null,h1,null,null,
				null,h4,hashZeit,
				null,hashZeit,
				null,
				null,null,null,null,null,null,
				null,null,null},
				new String[]{"Prozessdaten_Literatur",null,null,null,null,
				null,null,null,
				null,null,
				"INT",
				null,null,null,null,null,null,
				"Prozessdaten_Sonstiges","Prozessdaten_Messwerte","Prozessdaten_Kosten"});
		myList.addTable(prozessdaten, MyList.Prozessdaten_LIST); // MyList.Prozessdaten_LIST
		prozessFlow.setForeignField(prozessdaten, 7);
		MyTable prozessReferenzen = new MyTable("Prozessdaten_Literatur",
				new String[]{"Prozessdaten","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{prozessdaten,literatur},
				new LinkedHashMap[]{null,null});
		myList.addTable(prozessReferenzen, -1);		
		MyTable Prozessdaten_Sonstiges = new MyTable("Prozessdaten_Sonstiges",
				new String[]{"Prozessdaten","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{prozessdaten,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null});
		myList.addTable(Prozessdaten_Sonstiges, -1);
		MyTable Prozessdaten_Messwerte = new MyTable("Prozessdaten_Messwerte",
				new String[]{"Prozessdaten","ExperimentID","Agens","Zeit","ZeitEinheit","Konzentration","Einheit","Konzentration_GKZ","Einheit_GKZ"},
				new String[]{"INTEGER","INTEGER","INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","INTEGER","DOUBLE","INTEGER"},
				new String[]{null,null,null,"Zeitpunkt der Messung relativ zum Prozessschritt,\nd.h. falls die Messung z.B. gleich zu Beginn des Prozessschrittes gemacht wird,\ndann ist hier 0 einzutragen!\nUnabh�ngig davon wie lange der gesamte Prozess schon l�uft!",null,"Konzentration des Agens","Konzentration - Einheit","Gesamtkeimzahl","Gesamtkeimzahl-Einheit"},
				new MyTable[]{prozessdaten,null,agenzien,newDoubleTable,null,newDoubleTable,Konzentrationseinheiten,newDoubleTable,Konzentrationseinheiten},
				null,
				new LinkedHashMap[]{null,null,null,null,hashZeit,null,null,null,null},
				new String[]{null,null,null,null,null,null,null,null,null});
		myList.addTable(Prozessdaten_Messwerte, -1);
		MyTable Prozessdaten_Kosten = new MyTable("Prozessdaten_Kosten",
				new String[]{"Prozessdaten","Kostenkatalog","Menge"},
				new String[]{"INTEGER","INTEGER","DOUBLE"},
				new String[]{null,null,null,},
				new MyTable[]{prozessdaten,Kostenkatalog,newDoubleTable},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null});
		myList.addTable(Prozessdaten_Kosten, -1);

		MyTable prozessLinks = new MyTable("Prozess_Verbindungen",
				new String[]{"Ausgangsprozess","Zielprozess"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{prozessdaten,prozessdaten},
				new LinkedHashMap[]{null,null});
		MyTable Verpackungen = new MyTable("Verpackungsmaterial", new String[]{"Kode","Verpackung"},
				new String[]{"VARCHAR(10)","VARCHAR(100)"},
				new String[]{null,null},
				new MyTable[]{null,null},
				null,
				new LinkedHashMap[]{null,null},
				new String[]{null,null});
		myList.addTable(Verpackungen, -1);
		myList.addTable(prozessLinks, -1);
		h1 = new LinkedHashMap<Object, String>();
	    h1.put("Zutat", "Zutat");					
	    h1.put("Produkt", "Produkt");					
		MyTable zutatendaten = new MyTable("Zutatendaten",
				new String[]{"Prozessdaten","Zutat_Produkt","Units","Unitmenge","UnitEinheit","Vorprozess",
				"Matrix","EAN","MatrixDetail","Verpackung","Temperatur","pH","aw","CO2","Druck","Luftfeuchtigkeit","Sonstiges","Kosten"},
				new String[]{"INTEGER","VARCHAR(10)","DOUBLE","DOUBLE","INTEGER","INTEGER",
				"INTEGER","VARCHAR(255)","VARCHAR(255)","INTEGER","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","INTEGER","INTEGER"},
				new String[]{"Verweis auf Eintr�ge aus der Tabelle Zutatendaten (Bedeutung nur f�r interne Verarbeitung)","Auswahl ob es sich um eine Zutat oder ein Produkt","Gr��e einer Charge","Mengengr��e pro Chargenelement","Einheit eines Chargenelements","Produkt des Vorprozesses",
				null,"EAN-Nummer aus SA2-Datenbank - falls bekannt","Details zur Matrix, die durch den Katalog nicht abgebildet werden",null,"Temperatur in Grad Celcius","pH-Wert","aw-Wert","CO2 [ppm]","Druck [bar]","Luftfeuchtigkeit - Einheit bitte in [%]","Sonstige Rahmenbedingungen in der Umgebung. Aber auch Facetten der Matrix.\nEs �ffnet sich ein Fenster, in dem an die Combase angelehnte Parameter eingetragen werden k�nnen, vgl. Feld condition in der Combase:\nhttp://www.combase.cc/CB_TechnDescription.pdf",null},
				new MyTable[]{prozessdaten,null,newDoubleTable,newDoubleTable,null,null,
				matrix,null,null,Verpackungen,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,newDoubleTable,SonstigeParameter,Kostenkatalog}, // prozessLinks
				null,
				new LinkedHashMap[]{null,h1,null,null,h4,null,
				null,null,null,null,null,null,null,null,null,null,null,null},
				new String[]{null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,null,"Zutatendaten_Sonstiges","Zutatendaten_Kosten"});
		myList.addTable(zutatendaten, -1);
		prozessdaten.setForeignField(zutatendaten, 10);
		zutatendaten.setForeignField(zutatendaten, 5);
		
		MyTable Zutatendaten_Sonstiges = new MyTable("Zutatendaten_Sonstiges",
				new String[]{"Zutatendaten","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{zutatendaten,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null});
		myList.addTable(Zutatendaten_Sonstiges, -1);
		MyTable Zutatendaten_Kosten = new MyTable("Zutatendaten_Kosten",
				new String[]{"Zutatendaten","Kostenkatalog","Menge"},
				new String[]{"INTEGER","INTEGER","DOUBLE"},
				new String[]{null,null,null,},
				new MyTable[]{zutatendaten,Kostenkatalog,newDoubleTable},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null});
		myList.addTable(Zutatendaten_Kosten, -1);
		

		MyTable LinkedTestConditions = new MyTable("LinkedTestConditions", new String[]{"CondID","LinkedCondID"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{null,null},
				null,
				new LinkedHashMap[]{null,null},
				null);
		myList.addTable(LinkedTestConditions, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);	

		generateStatUpModellTables(myList, literatur, tenazity_raw_data, hashZeit, Konzentrationseinheiten);

		//doJansTabellen(myList, prozessFlow, literatur, agenzien);
		
		doLieferkettenTabellen(myList, adressen, agenzien, matrix, h4);

		if (myDB != null) {
			myList.createTables();				
			if (!DBKernel.isKrise) {
				fillWithDataAndGrants(myList, myDB);
			    
				UpdateChecker.doStatUpGrants();
				//UpdateChecker.doJansGrants();				
			}
		}		
	}
	@SuppressWarnings("unchecked")
	private void doLieferkettenTabellen(final MyList myList, final MyTable adressen, final MyTable agenzien, final MyTable matrix, final LinkedHashMap<Object, String> h4) {
		LinkedHashMap<Boolean, String> hYNB = new LinkedHashMap<Boolean, String>();
		hYNB.put(new Boolean(true), "ja");	hYNB.put(new Boolean(false), "nein");
		MyTable Knoten = new MyTable("Station", new String[]{"Kontaktadresse","Betriebsnummer","Betriebsart","VATnumber","Code",
				"FallErfuellt","AnzahlFaelle","AlterMin","AlterMax","DatumBeginn","DatumHoehepunkt","DatumEnde","Erregernachweis","Produktkatalog"},
				new String[]{"INTEGER","VARCHAR(50)","VARCHAR(255)","VARCHAR(255)","VARCHAR(25)",
				"BOOLEAN","INTEGER","INTEGER","INTEGER","DATE","DATE","DATE","INTEGER","INTEGER"},
				new String[]{"Verweis auf Eintr�ge in Tabelle Kontakte mit Lebensmittel-Betrieben, Landwirten etc","Betriebsnummer aus BALVI-System sofern vorhanden",
				"z.B. Endverbraucher, Erzeuger, Einzelh�ndler, Gro�h�ndler, Gastronomie, Mensch. Siehe weitere Beispiele ADV Katalog", "Steuernummer", "interner Code, z.B. NI00",
				"Falldefinition erf�llt (z.B. laut RKI)",null,null,null,"Datum fr�hester Erkrankungsbeginn","Datum des H�hepunkt an Neuerkrankungen","Datum letzter Erkrankungsbeginn",null,null},
				new MyTable[]{adressen,null,null,null,null,null,null,null,null,null,null,null,agenzien,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,hYNB,null,null,null,null,null,null,null,null},
				new String[]{null,null,null,null,null,null,null,null,null,null,null,null,"Station_Agenzien","INT"});
		myList.addTable(Knoten, MyList.Lieferketten_LIST);
		MyTable Agensnachweis = new MyTable("Station_Agenzien", new String[]{"Station","Erreger","Labornachweis","AnzahlLabornachweise"},
				new String[]{"INTEGER","INTEGER","BOOLEAN","INTEGER"},
				new String[]{null,null,"Labornachweise vorhanden?",null},
				new MyTable[]{Knoten,agenzien,null,null},
				null,
				new LinkedHashMap[]{null,null,hYNB,null});
		myList.addTable(Agensnachweis, -1);
		LinkedHashMap<String, String> proce = new LinkedHashMap<String, String>();
		proce.put("nicht erhitzt und verzehrsfertig (Salate, rohe Produkte)", "nicht erhitzt und verzehrsfertig (Salate, rohe Produkte)");
		proce.put("erhitzt und verzehrsfertig (fast alles)", "erhitzt und verzehrsfertig (fast alles)");
		proce.put("erhitzt und nicht verzehrsf�hig (Vorprodukte wie eingefrorene Kuchen)", "erhitzt und nicht verzehrsf�hig (Vorprodukte wie eingefrorene Kuchen)");
		proce.put("nicht erhitzt und nicht verzehrsf�hig (Rohwaren, die nicht zum Rohverzehr bestimmt sind wie Fleisch oder Eier)", "nicht erhitzt und nicht verzehrsf�hig (Rohwaren, die nicht zum Rohverzehr bestimmt sind wie Fleisch oder Eier)");
		MyTable Produzent_Artikel = new MyTable("Produktkatalog", // Produzent_Artikel
				new String[]{"Station","Artikelnummer","Bezeichnung","Prozessierung","IntendedUse","Code","Matrices","Chargen"},
				new String[]{"INTEGER","VARCHAR(255)","VARCHAR(1023)","VARCHAR(255)","VARCHAR(255)","VARCHAR(25)","INTEGER","INTEGER"},
				new String[]{null,null,null,"gekocht? gesch�ttelt? ger�hrt?","wozu ist der Artikel gedacht? Was soll damit geschehen?","interner Code",null,null},
				new MyTable[]{Knoten,null,null,null,null,null,matrix,null},
				null,
				new LinkedHashMap[]{null,null,null,proce,null,null,null,null},
				new String[]{null,null,null,null,null,null,"Produktkatalog_Matrices","INT"});
		myList.addTable(Produzent_Artikel, MyList.Lieferketten_LIST);
		Knoten.setForeignField(Produzent_Artikel, 13);
		MyTable Produktmatrices = new MyTable("Produktkatalog_Matrices", new String[]{"Produktkatalog","Matrix"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{Produzent_Artikel,matrix},
				null,
				new LinkedHashMap[]{null,null});
		myList.addTable(Produktmatrices, -1);
		
		MyTable Chargen = new MyTable("Chargen",
				new String[]{"Artikel","Zutaten","ChargenNr","MHD","Herstellungsdatum","Menge","Einheit","Lieferungen"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","DATE","DATE","DOUBLE","VARCHAR(50)","INTEGER"},
				new String[]{null,null,null,null,null,null,null,null},
				new MyTable[]{Produzent_Artikel,null,null,null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null},
				new String[]{null,"INT",null,null,null,null,null,"INT"});
		myList.addTable(Chargen, MyList.Lieferketten_LIST);
		Produzent_Artikel.setForeignField(Chargen, 7);
		
		MyTable Artikel_Lieferung = new MyTable("Lieferungen", // Artikel_Lieferung
				new String[]{"Charge","Lieferdatum","#Units1","BezUnits1","#Units2","BezUnits2", // "Artikel","ChargenNr","MHD",
					"Unitmenge","UnitEinheit","Empf�nger"}, // ,"Vorprodukt","Zielprodukt"
				new String[]{"INTEGER","DATE","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)",
					"DOUBLE","VARCHAR(50)","INTEGER"}, // ,"INTEGER","INTEGER"
				new String[]{null,"Lieferdatum (arrival)",null,null,null,null,null,null,null}, // ,null,null
				new MyTable[]{Chargen,null,null,null,null,null,null,null,Knoten}, // ,null,null
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null}, // ,null,null
				new String[]{null,null,null,null,null,null,null,null,null}); // ,"INT","INT"
		myList.addTable(Artikel_Lieferung, MyList.Lieferketten_LIST);
		Chargen.setForeignField(Artikel_Lieferung, 7);
		
		MyTable ChargenVerbindungen = new MyTable("ChargenVerbindungen",
				new String[]{"Zutat","Produkt","MixtureRatio"}, // man k�nnte hier sowas machen wie: ,"#Units","Unitmenge","UnitEinheit", um zu notieren wieviel der vorgelieferten Menge in das Produkt gegangen sind
				new String[]{"INTEGER","INTEGER","DOUBLE"}, // ,"VARCHAR(50)","VARCHAR(50)","VARCHAR(50)"
				new String[]{null,null,"Mixture Ratio (prozentualer Anteil von der Zutat im Zielprodukt,\nz.B. Zielprodukt = Sprout mixture, Zutat = alfalfa sprouts => z.B. 0.33 (33%))"},
				new MyTable[]{Artikel_Lieferung,Chargen,null},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null});
		myList.addTable(ChargenVerbindungen, DBKernel.debug ? MyList.Lieferketten_LIST : -1);
		Chargen.setForeignField(ChargenVerbindungen, 1);
		/*
		MyTable Lieferung_Lieferungen = new MyTable("LieferungVerbindungen",
				new String[]{"Vorprodukt","Zielprodukt","MixtureRatio"}, // man k�nnte hier sowas machen wie: ,"#Units","Unitmenge","UnitEinheit", um zu notieren wieviel der vorgelieferten Menge in das Produkt gegangen sind
				new String[]{"INTEGER","INTEGER","DOUBLE"}, // ,"VARCHAR(50)","VARCHAR(50)","VARCHAR(50)"
				new String[]{null,null,"Mixture Ratio (prozentualer Anteil vom Vorprodukt im Zielprodukt,\nz.B. Zielprodukt = Sprout mixture, Vorprodukt = alfalfa sprouts => z.B. 0.33 (33%))"},
				new MyTable[]{Chargen,Chargen,null},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null});
		myList.addTable(Lieferung_Lieferungen, MyList.Lieferketten_LIST);
		Artikel_Lieferung.setForeignField(Lieferung_Lieferungen, 12);
		Artikel_Lieferung.setForeignField(Lieferung_Lieferungen, 13);
		*/

		//check4Updates_129_130(myList);

		//DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Bundesland") + " = 'NI' WHERE " + DBKernel.delimitL("ID") + " = 167", false);
	}
	private MyTable generateICD10Tabellen(final MyList myList) {
		MyTable ICD10_Kapitel = new MyTable("ICD10_Kapitel", new String[]{"KapNr","KapTi"},
				new String[]{"VARCHAR(2)","VARCHAR(110)"},
				new String[]{"Kapitelnummer, 2 Zeichen","Kapiteltitel, bis zu 110 Zeichen"},
				new MyTable[]{null,null},
				new String[][]{{"KapNr"}},
				null,
				new String[]{null,null});
		myList.addTable(ICD10_Kapitel, -1);		
		MyTable ICD10_Gruppen = new MyTable("ICD10_Gruppen", new String[]{"GrVon","GrBis","KapNr","GrTi"},
				new String[]{"VARCHAR(3)","VARCHAR(3)","INTEGER","VARCHAR(210)"},
				new String[]{"erster Dreisteller der Gruppe, 3 Zeichen","letzter Dreisteller der Gruppe, 3 Zeichen","Kapitelnummer, 2 Zeichen","Gruppentitel, bis zu 210 Zeichen"},
				new MyTable[]{null,null,ICD10_Kapitel,null},
				new String[][]{{"GrVon"}},
				null,
				new String[]{null,null,null,null});
		myList.addTable(ICD10_Gruppen, -1);		
		MyTable ICD10_MorbL = new MyTable("ICD10_MorbL", new String[]{"MorbLCode","MorbLTi"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Titel"},
				new MyTable[]{null,null},
				new String[][]{{"MorbLCode"}},
				null,
				new String[]{null,null});
		myList.addTable(ICD10_MorbL, -1);		
		MyTable ICD10_MortL1Grp = new MyTable("ICD10_MortL1Grp", new String[]{"MortL1GrpCode","MortL1GrpTi"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Gruppenschl�sselnummer","Gruppentitel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL1GrpCode"}},
				null,
				new String[]{null,null});
		myList.addTable(ICD10_MortL1Grp, -1);		
		MyTable ICD10_MortL1 = new MyTable("ICD10_MortL1", new String[]{"MortL1Code","MortL1GrpCode","MortL1Ti"},
				new String[]{"VARCHAR(5)","INTEGER","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Gruppenschl�sselnummer","Titel"},
				new MyTable[]{null,ICD10_MortL1Grp,null},
				new String[][]{{"MortL1Code"}},
				null,
				new String[]{null,null,null});
		myList.addTable(ICD10_MortL1, -1);		
		MyTable ICD10_MortL2 = new MyTable("ICD10_MortL2", new String[]{"MortL2Code","MortL2Ti"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Titel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL2Code"}},
				null,
				new String[]{null,null});
		myList.addTable(ICD10_MortL2, -1);		
		MyTable ICD10_MortL3Grp = new MyTable("ICD10_MortL3Grp", new String[]{"MortL3GrpCode","MortL3GrpTi"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Gruppenschl�sselnummer","Gruppentitel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL3GrpCode"}},
				null,
				new String[]{null,null});
		myList.addTable(ICD10_MortL3Grp, -1);		
		MyTable ICD10_MortL3 = new MyTable("ICD10_MortL3", new String[]{"MortL3Code","MortL3GrpCode","MortL3Ti"},
				new String[]{"VARCHAR(5)","INTEGER","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Gruppenschl�sselnummer","Titel"},
				new MyTable[]{null,ICD10_MortL3Grp,null},
				new String[][]{{"MortL3Code"}},
				null,
				new String[]{null,null,null});
		myList.addTable(ICD10_MortL3, -1);		
		MyTable ICD10_MortL4 = new MyTable("ICD10_MortL4", new String[]{"MortL4Code","MortL4Ti"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Titel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL4Code"}},
				null,
				new String[]{null,null});
		myList.addTable(ICD10_MortL4, -1);		
		MyTable ICD10_Kodes = new MyTable("ICD10_Kodes", new String[]{"Ebene","Ort","Art",
				"KapNr","GrVon","Code","NormCode","CodeOhnePunkt",
				"Titel","P295","P301",
				"MortL1Code","MortL2Code","MortL3Code","MortL4Code","MorbLCode",
				"SexCode","SexFehlerTyp",
				"AltUnt",
				"AltUntNeu",
				"AltOb","AltObNeu",
				"AltFehlerTyp","Exot","Belegt",
				"IfSGMeldung","IfSGLabor"},
				new String[]{"VARCHAR(1)","VARCHAR(1)","VARCHAR(1)","INTEGER","INTEGER","VARCHAR(7)","VARCHAR(6)","VARCHAR(5)",
				"VARCHAR(255)","VARCHAR(1)","VARCHAR(1)","INTEGER","INTEGER","INTEGER","INTEGER","INTEGER","VARCHAR(1)","VARCHAR(1)","VARCHAR(3)",
				"VARCHAR(4)","VARCHAR(3)","VARCHAR(4)","VARCHAR(1)","VARCHAR(1)","VARCHAR(1)","VARCHAR(1)","VARCHAR(1)"},
				new String[]{"Klassifikationsebene, 1 Zeichen: 3 = Dreisteller; 4 = Viersteller; 5 = F�nfsteller","Ort der Schl�sselnummer im Klassifikationsbaum, 1 Zeichen: T = terminale Schl�sselnummer (kodierbarer Endpunkt); N = nichtterminale Schl�sselnummer (kein kodierbarer Endpunkt)","Art der Vier- und F�nfsteller: X = explizit aufgef�hrt (pr�kombiniert); S = per Subklassifikation (postkombiniert)",
				"Kapitelnummer","erster Dreisteller der Gruppe","Schl�sselnummer ohne eventuelles Kreuz, bis zu 7 Zeichen","Schl�sselnummer ohne Strich, Stern und  Ausrufezeichen, bis zu 6 Zeichen","Schl�sselnummer ohne Punkt, Strich, Stern und Ausrufezeichen, bis zu 5 Zeichen",
				"Klassentitel, bis zu 255 Zeichen","Verwendung der Schl�sselnummer nach Paragraph 295: P = zur Prim�rverschl�sselung zugelassene Schl�sselnummer; O = nur als Sternschl�sselnummer zugelassen; Z = nur als Ausrufezeichenschl�sselnummer zugelassen; V = nicht zur Verschl�sselung zugelassen","Verwendung der Schl�sselnummer nach Paragraph 301: P = zur Prim�rverschl�sselung zugelassen; O = nur als Sternschl�sselnummer zugelassen; Z = nur als Ausrufezeichenschl�sselnummer zugelassen; V = nicht zur Verschl�sselung zugelassen",
				"Bezug zur Mortalit�tsliste 1","Bezug zur Mortalit�tsliste 2","Bezug zur Mortalit�tsliste 3","Bezug zur Mortalit�tsliste 4","Bezug zur Morbidit�tsliste",
				"Geschlechtsbezug der Schl�sselnummer: 9 = kein Geschlechtsbezug; M = m�nnlich; W = weiblich", "Art des Fehlers bei Geschlechtsbezug: 9 = irrelevant; M = Muss-Fehler; K = Kann-Fehler",
				"untere Altersgrenze f�r eine Schl�sselnummer: 999     = irrelevant; 000     = unter 1 vollendeten Tag; 001-006 = 1 Tag bis unter 7 Tage; 011-013 = 7 Tage bis unter 28 Tage; also 011 =  7-13 Tage (1 Woche bis unter 2 Wochen); 012 = 14-20 Tage (2 Wochen bis unter 3 Wochen); 013 = 21-27 Tage (3 Wochen bis unter einem Monat); 101-111 = 28 Tage bis unter 1 Jahr; also 101 = 28 Tage bis Ende des 2. Lebensmonats; 102 = Anfang bis Ende des 3. Lebensmonats; 103 = Anfang bis Ende des 4. Lebensmonats; usw. bis; 111 = Anfang des 12. Lebensmonats bis unter 1 Jahr; 201-299 = 1 Jahr bis unter 100 Jahre; 300-324 = 100 Jahre bis unter 125 Jahre",
				"untere Altersgrenze f�r eine Schl�sselnummer, alternatives Format: 9999    = irrelevant; t000 - t365 = 0 Tage bis unter 1 Jahr; j001 - j124 = 1 Jahr bis unter 124 Jahre",
				"obere Altersgrenze f�r eine Schl�sselnummer, wie bei Feld 'AltUnt'","obere Altersgrenze f�r eine Schl�sselnummer,alternatives Format wie bei Feld 'AltUntNeu'",
				"Art des Fehlers bei Altersbezug: 9 = irrelevant; M = Muss-Fehler; K = Kann-Fehler","Krankheit in Mitteleuropa sehr selten? J = Ja; N = Nein","Schl�sselnummer mit Inhalt belegt? J = Ja; N = Nein (--> Kann-Fehler ausl�sen!)",
				"IfSG-Meldung, kennzeichnet, dass bei Diagnosen,die mit dieser Schl�sselnummer kodiert sind, besonders auf die Arzt-Meldepflicht nach dem Infektionsschutzgesetz IfSG) hinzuweisen ist: J = Ja; N = Nein","IfSG-Labor, kennzeichnet, dass bei Laboruntersuchungen zu diesen Diagnosen die Laborausschlussziffer des EBM (32006) gew�hlt werden kann: J = Ja; N = Nein"},
				new MyTable[]{null,null,null,ICD10_Kapitel,ICD10_Gruppen,null,null,null,null,null,null,ICD10_MortL1,ICD10_MortL2,ICD10_MortL3,ICD10_MortL4,ICD10_MorbL,
				null,null,null,null,null,null,null,null,null,null,null},
				new String[][]{{"Code"},{"NormCode"},{"CodeOhnePunkt"}});
		myList.addTable(ICD10_Kodes, MyList.Krankheitsbilder_LIST);
		return ICD10_Kodes;
	}
	private void fillHashModelTypes() {
		DBKernel.hashModelType.put(0, "unknown");					
		DBKernel.hashModelType.put(1, "growth");					
		DBKernel.hashModelType.put(2, "inactivation");	
		DBKernel.hashModelType.put(3, "survival");					
		DBKernel.hashModelType.put(4, "growth/inactivation");	
		DBKernel.hashModelType.put(5, "inactivation/survival");					
		DBKernel.hashModelType.put(6, "growth/survival");	
		DBKernel.hashModelType.put(7, "growth/inactivation/survival");					
		DBKernel.hashModelType.put(8, "T");	
		DBKernel.hashModelType.put(9, "pH");	
		DBKernel.hashModelType.put(10, "aw");	
		DBKernel.hashModelType.put(11, "T/pH");	
		DBKernel.hashModelType.put(12, "T/aw");	
		DBKernel.hashModelType.put(13, "pH/aw");	
		DBKernel.hashModelType.put(14, "T/pH/aw");	
	}
	@SuppressWarnings("unchecked")
	private void generateStatUpModellTables(final MyList myList, final MyTable literatur, final MyTable tenazity_raw_data, final LinkedHashMap<Object, String> hashZeit, final MyTable Konzentrationseinheiten) {
		MyTable PMMLabWorkflows = new MyTable("PMMLabWorkflows", new String[]{"Workflow"},
				new String[]{"BLOB(100M)"},
				new String[]{null},
				new MyTable[]{null},
				null,
				new LinkedHashMap[]{null},
				null);
		myList.addTable(PMMLabWorkflows, -1);	
		MyTable DataSource = new MyTable("DataSource", new String[]{"Table","TableID","SourceDBUUID","SourceID"},
				new String[]{"VARCHAR(255)","INTEGER","VARCHAR(255)","INTEGER"},
				new String[]{null,null,null,null},
				new MyTable[]{null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null},
				null);
		myList.addTable(DataSource, -1);	

		LinkedHashMap<Object, String> hashLevel = new LinkedHashMap<Object, String>();
		hashLevel.put(1, "primary");					
		hashLevel.put(2, "secondary");	
		fillHashModelTypes();
		LinkedHashMap<Object, String> hashTyp = new LinkedHashMap<Object, String>();
		hashTyp.put(1, "Kovariable");			// independent ?		
		hashTyp.put(2, "Parameter");	
		hashTyp.put(3, "Response");	// dependent ?	
		hashTyp.put(4, "StartParameter");	
		MyTable Parametertyp = new MyTable("Parametertyp", new String[]{"Parametertyp"},
				new String[]{"INTEGER"},
				new String[]{null},
				new MyTable[]{null},
				null,
				new LinkedHashMap[]{hashTyp},
				new String[]{null});
		myList.addTable(Parametertyp, -1);
		MyTable Modellkatalog = new MyTable("Modellkatalog", new String[]{"Name","Notation","Level","Klasse","Typ","Eingabedatum",
				"eingegeben_von","Beschreibung","Formel","Ableitung","Software",
				"Parameter","Referenzen"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","INTEGER","INTEGER","VARCHAR(255)","DATE",
				"VARCHAR(255)","VARCHAR(1023)","VARCHAR(511)","INTEGER","VARCHAR(255)",
				"INTEGER","INTEGER"},
				new String[]{null,null,"1: primary, 2:secondary","1:growth, 2:inactivation, 3:survival,\n4:growth/inactivation, 5:inactivation/survival, 6: growth/survival,\n7:growth/inactivation/survival\n8: T, 9: pH, 10:aw, 11:T/pH, 12:T/aw, 13:pH/aw, 14:T/pH/aw",null,null,"Ersteller des Datensatzes","Beschreibung des Modells","zugrundeliegende Formel f�r das Modell","Ableitung","schreibt den Schaetzknoten vor",
				"Parameterdefinitionen, die dem Modell zugrunde liegen: abh�ngige Variable, unabh�ngige Variable, Parameter","Referenzen, die dem Modell zugrunde liegen"},
				new MyTable[]{null,null,null,null,null,null,null,null,null,null,null,
				Parametertyp,literatur},
				null,
				new LinkedHashMap[]{null,null,hashLevel,DBKernel.hashModelType,null,null,null,null,null,null,null,
				null,null},
				new String[] {null,null,null,null,null,null,null,null,null,null,null,
						"ModellkatalogParameter","Modell_Referenz"},
				//new String[] {"not null","not null",null,null,null,"not null",
				new String[] {null,null,null,null,null,null,null,
				null,null,null,null,
				null,null});
		myList.addTable(Modellkatalog, MyList.PMModelle_LIST);		
		MyTable ModellkatalogParameter = new MyTable("ModellkatalogParameter", new String[]{"Modell","Parametername","Parametertyp",
				"ganzzahl","min","max","Beschreibung"},
				new String[]{"INTEGER","VARCHAR(127)","INTEGER",
				"BOOLEAN","DOUBLE","DOUBLE","VARCHAR(1023)"},
				new String[]{null,null,"1: Kovariable, 2: Parameter, 3: Response, 4: StartParameter",
				"TRUE, falls der Parameter ganzzahlig sein muss",null,null,null},
				new MyTable[]{Modellkatalog,null,null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,hashTyp,null,null,null,null},
				null,
				//new String[] {"not null","not null","default 1","default FALSE","default null","default null",null});
				new String[] {"not null",null,"default 1","default FALSE","default null","default null",null});
		myList.addTable(ModellkatalogParameter, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);	
		MyTable Modell_Referenz = new MyTable("Modell_Referenz", new String[]{"Modell","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{Modellkatalog,literatur},
				null,
				new LinkedHashMap[]{null,null},
				null,
				new String[] {"not null","not null"});
		myList.addTable(Modell_Referenz, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);		
		
		MyTable GeschaetzteModelle = new MyTable("GeschaetzteModelle", new String[]{"Versuchsbedingung","Modell",
				"Response","manuellEingetragen","Rsquared","RSS","RMS","AIC","BIC","Score",
				"Referenzen","GeschaetzteParameter","GeschaetzteParameterCovCor","GueltigkeitsBereiche","PMML","PMMLabWF"},
				new String[]{"INTEGER","INTEGER","INTEGER","BOOLEAN","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","INTEGER",
				"INTEGER","INTEGER","INTEGER","INTEGER","BLOB(10M)","INTEGER"},
				new String[]{null,null,"Response, verweist auf die Tabelle ModellkatalogParameter","wurde das Modell manuell eingetragen oder ist es eine eigene Sch�tzung basierend auf den internen Algorithmen und den in den Messwerten hinterlegten Rohdaten","r^2 oder Bestimmtheitsma� der Sch�tzung","Variation der Residuen",null,null,null,"subjektiver Score zur Bewertung der Sch�tzung",
				"Referenzen, aus denen diese Modellsch�tzung entnommen wurde","Verweis auf die Tabelle ModellkatalogParameter mit den gesch�tzten Parametern","Verweis auf die Tabelle ModellkatalogParameterCovCor mit den Korrelationen der gesch�tzten Parameter","G�ltigkeitsbereiche f�r Sekund�rmodelle",null,null},
				new MyTable[]{tenazity_raw_data,Modellkatalog,
				ModellkatalogParameter,null,null,null,null,null,null,null,
				literatur,ModellkatalogParameter,null,ModellkatalogParameter,null,PMMLabWorkflows},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null},
				new String[] {null,null,
				null,null,null,null,null,null,null,null,
				"GeschaetztesModell_Referenz","GeschaetzteParameter","INT","GueltigkeitsBereiche",null,null},
				new String[] {null,null,null,"default FALSE",null,null,null,null,null,null,
				null,null,null,null,null,null});
				//new String[] {null,"not null",null,"default FALSE",null,null,null,
				//null,null,null});
		myList.addTable(GeschaetzteModelle, MyList.PMModelle_LIST);		
		MyTable GeschaetztesModell_Referenz = new MyTable("GeschaetztesModell_Referenz", new String[]{"GeschaetztesModell","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{GeschaetzteModelle,literatur},
				null,
				new LinkedHashMap[]{null,null},
				null,
				new String[] {"not null","not null"});
		myList.addTable(GeschaetztesModell_Referenz, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);	
		MyTable GeschaetzteParameter = new MyTable("GeschaetzteParameter", new String[]{"GeschaetztesModell","Parameter",
				"Wert","ZeitEinheit","Konz_Einheit","KI.unten","KI.oben","SD","StandardError","t","p"},
				new String[]{"INTEGER","INTEGER",
				"DOUBLE","VARCHAR(50)","INTEGER","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE"},
				new String[]{null,null,null,null,null,null,null,null,null,null,null},
				new MyTable[]{GeschaetzteModelle,ModellkatalogParameter,null,null,Konzentrationseinheiten,null,null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,hashZeit,null,null,null,null,null,null,null},
				null,
				new String[] {"not null","not null",null,null,null,null,null,null,null,null,null});
		myList.addTable(GeschaetzteParameter, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);	
		MyTable GueltigkeitsBereiche = new MyTable("GueltigkeitsBereiche", new String[]{"GeschaetztesModell","Parameter",
				"Gueltig_von","Gueltig_bis"},
				new String[]{"INTEGER","INTEGER",
				"DOUBLE","DOUBLE"},
				new String[]{null,null,null,null},
				new MyTable[]{GeschaetzteModelle,ModellkatalogParameter,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null},
				null,
				new String[] {"not null","not null",null,null});
		myList.addTable(GueltigkeitsBereiche, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);
		MyTable VarParMaps = new MyTable("VarParMaps", new String[]{"GeschaetztesModell","VarPar","VarParMap"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{GeschaetzteModelle,ModellkatalogParameter,null},
				null,
				new LinkedHashMap[]{null,null,null},
				null,
				new String[] {null,null,null});
		myList.addTable(VarParMaps, -1);	
		MyTable GeschaetzteParameterCovCor = new MyTable("GeschaetzteParameterCovCor", new String[]{"param1","param2",
				"GeschaetztesModell","cor","Wert"},
				new String[]{"INTEGER","INTEGER","INTEGER","BOOLEAN","DOUBLE"},
				new String[]{null,null,null,null,null},
				new MyTable[]{GeschaetzteParameter,GeschaetzteParameter,GeschaetzteModelle,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				null,
				null);//new String[] {"not null","not null","not null","not null",null});
		myList.addTable(GeschaetzteParameterCovCor, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);		
		GeschaetzteModelle.setForeignField(GeschaetzteParameterCovCor, 12);
		MyTable Sekundaermodelle_Primaermodelle = new MyTable("Sekundaermodelle_Primaermodelle",
				new String[]{"GeschaetztesPrimaermodell","GeschaetztesSekundaermodell"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{GeschaetzteModelle,GeschaetzteModelle},
				null,
				new LinkedHashMap[]{null,null},
				null,
				new String[] {"not null","not null"});
		myList.addTable(Sekundaermodelle_Primaermodelle, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);		
	}
  private void fillWithDataAndGrants(final MyList myList, final MyDBTable myDB) {
	
    try {
      // f�r den Defaultwert bei Zugriffsrecht
      DBKernel.getDBConnection().createStatement().execute("CREATE TRIGGER " + DBKernel.delimitL("B_Users_I") + " BEFORE INSERT ON " +
      		DBKernel.delimitL("Users") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()));    	
      // Zur �berwachung, damit immer mindestens ein Admin �brig bleibt; dasselbe gibts im MyDataChangeListener f�r Delete Operations!
      // Au�erdem zur �berwachung, da� der eingeloggte User seine Kennung nicht �ndert
      DBKernel.getDBConnection().createStatement().execute("CREATE TRIGGER " + DBKernel.delimitL("B_Users_U") + " BEFORE UPDATE ON " +
      		DBKernel.delimitL("Users") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()));    	
      // Zur �berwachung, damit eine importierte xml Datei nicht gel�scht werden kann!
      DBKernel.getDBConnection().createStatement().execute("CREATE TRIGGER " + DBKernel.delimitL("B_ProzessWorkflow_U") + " BEFORE UPDATE ON " +
      		DBKernel.delimitL("ProzessWorkflow") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()));    	
    }
    catch (Exception e) {MyLogger.handleException(e);}
    
    DBKernel.sendRequest("CREATE ROLE " + DBKernel.delimitL("WRITE_ACCESS"), true);
    DBKernel.sendRequest("CREATE ROLE " + DBKernel.delimitL("SUPER_WRITE_ACCESS"), true);
    
    try {    	
    	PreparedStatement ps = DBKernel.getDBConnection().prepareStatement("INSERT INTO " + DBKernel.delimitL("Users") +
					" (" + DBKernel.delimitL("Username") + "," + DBKernel.delimitL("Vorname") + "," + DBKernel.delimitL("Name") + "," + DBKernel.delimitL("Zugriffsrecht") + ") VALUES (?,?,?,?)");
		//ps.setString(1, "SA"); ps.setString(2, "Super"); ps.setString(3, "Admin"); ps.setInt(4, Users.ADMIN); ps.execute();
		ps.setString(1, "pichner"); ps.setString(2, "Rohtraud"); ps.setString(3, "Pichner"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "elschner"); ps.setString(2, "Mandy"); ps.setString(3, "Elschner"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "b�hnlein"); ps.setString(2, "Christina"); ps.setString(3, "B�hnlein"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "hammerl"); ps.setString(2, "Jens-Andre"); ps.setString(3, "Hammerl"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "buschulte"); ps.setString(2, "Anja"); ps.setString(3, "Buschulte"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "meyer-scholl"); ps.setString(2, "Anne"); ps.setString(3, "Meyer-Scholl"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "weber"); ps.setString(2, "J�rg"); ps.setString(3, "Weber"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "balvi"); ps.setString(2, "BALVI"); ps.setString(3, "GmbH"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "niederberger"); ps.setString(2, "Almut"); ps.setString(3, "Niederberger"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "mader"); ps.setString(2, "Anneluise"); ps.setString(3, "Mader"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "filter"); ps.setString(2, "Matthias"); ps.setString(3, "Filter"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "falenski"); ps.setString(2, "Alexander"); ps.setString(3, "Falenski"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "mertens"); ps.setString(2, "Katja"); ps.setString(3, "Mertens"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "schneider"); ps.setString(2, "Daniela"); ps.setString(3, "Schneider"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "wese"); ps.setString(2, "Anne-Kathrin"); ps.setString(3, "Wese"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "schielke"); ps.setString(2, "Anika"); ps.setString(3, "Schielke"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "thiele"); ps.setString(2, "Holger"); ps.setString(3, "Thiele"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
		ps.setString(1, "burchardi"); ps.setString(2, "Henrike"); ps.setString(3, "Burchardi"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
	    ps.setString(1, "frentzel"); ps.setString(2, "Hendrik"); ps.setString(3, "Frentzel"); ps.setInt(4, Users.SUPER_WRITE_ACCESS); ps.execute();
	    
      } 
    catch (Exception e) {
    	MyLogger.handleException(e);
		}


    LinkedHashMap<String, MyTable> myTables = myList.getAllTables();
		for(String key : myTables.keySet()) {
			String tableName = myTables.get(key).getTablename();
			if (!tableName.equals("Users") && !tableName.equals("ChangeLog") && !tableName.equals("DateiSpeicher")) {
				DBKernel.grantDefaults(tableName);
				/*
				DBKernel.sendRequest("GRANT SELECT ON TABLE " + DBKernel.delimitL(tableName) + " TO " + DBKernel.delimitL("PUBLIC"), false);				
				if (tableName.startsWith("Codes_")) {
					DBKernel.sendRequest("GRANT SELECT ON TABLE " + DBKernel.delimitL(tableName) + " TO " + DBKernel.delimitL("WRITE_ACCESS"), false);				
				}
				else {
					DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL(tableName) + " TO " + DBKernel.delimitL("WRITE_ACCESS"), false);				
				}
				DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE " + DBKernel.delimitL(tableName) + " TO " + DBKernel.delimitL("SUPER_WRITE_ACCESS"), false);
				*/				
			}
		}
    DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL("ChangeLog") + " TO " + DBKernel.delimitL("WRITE_ACCESS"), false);				
    DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL("ChangeLog") + " TO " + DBKernel.delimitL("SUPER_WRITE_ACCESS"), false);				
    DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL("DateiSpeicher") + " TO " + DBKernel.delimitL("WRITE_ACCESS"), false);				
    DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL("DateiSpeicher") + " TO " + DBKernel.delimitL("SUPER_WRITE_ACCESS"), false);				

/*
	DBKernel.sendRequest(
    		"CREATE FUNCTION LD(x VARCHAR(255), y VARCHAR(255))\n" +
    		"RETURNS INT\n" + 
    		"NO SQL\n" +
    		"LANGUAGE JAVA\n" +
    		"PARAMETER STYLE JAVA\n" +
    		"EXTERNAL NAME 'CLASSPATH:org.hsh.bfr.db.Levenshtein.LD'"
    		, false);
*/
    try {
	      DBKernel.getDBConnection().createStatement().execute("CREATE USER " + DBKernel.delimitL(DBKernel.getTempSA(DBKernel.HSHDB_PATH)) + " PASSWORD '" + DBKernel.getTempSAPass(DBKernel.HSHDB_PATH) + "' ADMIN"); // MD5.encode("de6!�5ddy", "UTF-8")
    }
    catch (Exception e) {MyLogger.handleException(e);}
  }
  /*
  private void getAllMetaData(MyList myList) {
	  List<String> allTerms = new ArrayList<String>();
	    LinkedHashMap<String, MyTable> myTables = myList.getAllTables();
		for(String key : myTables.keySet()) {
			MyTable myT = myTables.get(key);
			String tableName = myT.getTablename();
			if (tableName != null) {
				String t = escapeStuff(tableName);
				if (!allTerms.contains(t)) {
					allTerms.add(t);
				}
			}
			for (String fn : myT.getFieldNames()) {
				if (fn != null) {
					String t = escapeStuff(fn);
					if (!allTerms.contains(t)) {
						allTerms.add(t);
					}
				}
			}
			for (String fc : myT.getFieldComments()) {
				if (fc != null) {
					String t = escapeStuff(fc);
					if (!allTerms.contains(t)) {
						allTerms.add(t);
					}
				}
			}
		}	  
		System.out.println("System-Tabellen = ");
		System.out.println("Basis-Tabellen = ");
		System.out.println("Tenazit�t = tenacity");
		System.out.println("PMModelle = ");
		System.out.println("Krankheitsbilder = ");
		System.out.println("Prozessdaten = ");
		System.out.println("Nachweissysteme = ");
		System.out.println("Guetescore = ");
		System.out.println("Kommentar = ");
		System.out.println("Geprueft = ");
		System.out.println("Hier\\ kann\\ eine\\ SUBJEKTIVE\\ Einsch�tzung\\ der\\ G�te\\ des\\ Datensatzes\\ (des\\ Experiments,\\ der\\ Methode,\\ ...)\\ abgegeben\\ werden\\nACHTUNG\\:\\ nicht\\ vergessen\\ diese\\ Einsch�tzung\\ zu\\ kommentieren\\ im\\ Feld\\ Kommentar = ");
		System.out.println("Datens�tze\\ k�nnen\\ von\\ einem\\ anderen\\ Benutzer\\ auf\\ Richtigkeit\\ hin\\ gepr�ft\\ werden.\\nDies\\ erh�ht\\ die\\ G�te\\ des\\ Eintrages. = ");
			for (String t : allTerms) {
			System.out.println(t + " = ");			
		}
  }
  private String escapeStuff(String term) {
	  return term.replace("#", "\\#").replace(" ", "\\ ").replace("=", "\\=").replace(":", "\\:").replace("\n", "\\n");
  }
  */

  private void thisWindowClosing(final WindowEvent e) {
	    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
	    	this.setVisible(false);
	    	if (firstRun) {
	    		DBKernel.closeDBConnections(false);
	    		this.dispose();
	    		System.exit(0);
	    	}
	    }
	}

	private void textField1KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void passwordField1KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}
  private void this_keyReleased(final KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
    	okButtonActionPerformed(null);
    }
  }

	private void checkBox1ActionPerformed(final ActionEvent e) {
		passwordField2.setEnabled(checkBox1.isSelected());
		passwordField3.setEnabled(checkBox1.isSelected());
	}

	private void passwordField2KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void passwordField3KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void checkBox1KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void thisWindowOpened(final WindowEvent e) {
		passwordField1.requestFocus();
	}

	private void button1ActionPerformed(final ActionEvent e) {
	    JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File(textField2.getText()));
	    chooser.setDialogTitle("W�hle Ordner der Datenbank");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
	      textField2.setText(chooser.getSelectedFile().getAbsolutePath());
	      }
	    else {
	    	MyLogger.handleMessage("No Selection ");
	    }
	}

	private void textField2KeyReleased(final KeyEvent e) {
		this_keyReleased(e);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		ResourceBundle bundle = ResourceBundle.getBundle("org.hsh.bfr.db.gui.PanelProps");
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		textField1 = new JTextField();
		label2 = new JLabel();
		passwordField1 = new JPasswordField();
		button1 = new JButton();
		textField2 = new JTextField();
		checkBox1 = new JCheckBox();
		label3 = new JLabel();
		passwordField2 = new JPasswordField();
		label4 = new JLabel();
		passwordField3 = new JPasswordField();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle(bundle.getString("Login.this.title"));
		setAlwaysOnTop(true);
		setIconImage(new ImageIcon(getClass().getResource("/org/hsh/bfr/db/gui/res/Database.gif")).getImage());
		setFont(new Font("Tahoma", Font.PLAIN, 13));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				thisWindowClosing(e);
			}
			@Override
			public void windowOpened(final WindowEvent e) {
				thisWindowOpened(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG);
			dialogPane.setFont(new Font("Tahoma", Font.PLAIN, 13));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.setLayout(new FormLayout(
					"default, 10dlu, default:grow",
					"5*(default, $lgap), default"));

				//---- label1 ----
				label1.setText(bundle.getString("Login.label1.text"));
				label1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.add(label1, CC.xy(1, 1));

				//---- textField1 ----
				textField1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				textField1.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(final KeyEvent e) {
						textField1KeyReleased(e);
					}
				});
				contentPanel.add(textField1, CC.xy(3, 1));

				//---- label2 ----
				label2.setText(bundle.getString("Login.label2.text"));
				label2.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.add(label2, CC.xy(1, 3));

				//---- passwordField1 ----
				passwordField1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				passwordField1.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(final KeyEvent e) {
						passwordField1KeyReleased(e);
					}
				});
				contentPanel.add(passwordField1, CC.xy(3, 3));

				//---- button1 ----
				button1.setText(bundle.getString("Login.button1.text"));
				button1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						button1ActionPerformed(e);
					}
				});
				contentPanel.add(button1, CC.xy(1, 5));

				//---- textField2 ----
				textField2.setFont(new Font("Tahoma", Font.PLAIN, 13));
				textField2.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(final KeyEvent e) {
						textField2KeyReleased(e);
					}
				});
				contentPanel.add(textField2, CC.xy(3, 5));

				//---- checkBox1 ----
				checkBox1.setText(bundle.getString("Login.checkBox1.text"));
				checkBox1.setFont(new Font("Tahoma", Font.PLAIN, 13));
				checkBox1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						checkBox1ActionPerformed(e);
					}
				});
				checkBox1.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(final KeyEvent e) {
						checkBox1KeyReleased(e);
					}
				});
				contentPanel.add(checkBox1, CC.xy(3, 7));

				//---- label3 ----
				label3.setText(bundle.getString("Login.label3.text"));
				label3.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.add(label3, CC.xy(1, 9));

				//---- passwordField2 ----
				passwordField2.setEnabled(false);
				passwordField2.setFont(new Font("Tahoma", Font.PLAIN, 13));
				passwordField2.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(final KeyEvent e) {
						passwordField2KeyReleased(e);
					}
				});
				contentPanel.add(passwordField2, CC.xy(3, 9));

				//---- label4 ----
				label4.setText(bundle.getString("Login.label4.text"));
				label4.setFont(new Font("Tahoma", Font.PLAIN, 13));
				contentPanel.add(label4, CC.xy(1, 11));

				//---- passwordField3 ----
				passwordField3.setEnabled(false);
				passwordField3.setFont(new Font("Tahoma", Font.PLAIN, 13));
				passwordField3.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(final KeyEvent e) {
						passwordField3KeyReleased(e);
					}
				});
				contentPanel.add(passwordField3, CC.xy(3, 11));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_PAD); // BUTTON_BAR_GAP_BORDER
				buttonBar.setFont(new Font("Tahoma", Font.PLAIN, 13));
				buttonBar.setLayout(new FormLayout(
					"$glue, $button, $rgap, $button",
					"pref"));

				//---- okButton ----
				okButton.setText("OK");
				okButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, CC.xy(2, 1));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.setIcon(null);
				cancelButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				buttonBar.add(cancelButton, CC.xy(4, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(435, 245);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label1;
	private JTextField textField1;
	private JLabel label2;
	private JPasswordField passwordField1;
	private JButton button1;
	private JTextField textField2;
	private JCheckBox checkBox1;
	private JLabel label3;
	private JPasswordField passwordField2;
	private JLabel label4;
	private JPasswordField passwordField3;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}



