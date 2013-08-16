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
import org.hsh.bfr.db.MyDBTables;
import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.UpdateChecker;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.gui.dbtree.MyDBTree;
import org.hsh.bfr.db.imports.InfoBox;
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
		/*
		if (DBKernel.debug && lastUser.equals(DBKernel.getTempSA(lastDBPath))) {
			passwordField1.setText(DBKernel.getTempSAPass(lastDBPath));
			//this.setTitle(textField1.getFont().getName() + " - " + textField1.getFont().getSize());
		}
		*/
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
		  		//DBKernel.saveUP2PrefsTEMP(DBKernel.HSHDB_PATH);
		  		/*
			  	DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("Infotabelle") + " WHERE " + DBKernel.delimitL("Parameter") + " = 'DBuuid'", false);
			  	DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("DateiSpeicher"), false);
			  	DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ChangeLog"), false);
			  	DBKernel.sendRequest("CHECKPOINT DEFRAG", false);
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
		    	if (fs > 300000000) {
					InfoBox ib = new InfoBox(this, "big data file (" + fs / 1000000 + ")!!! Bitte mal bei Armin melden!\n(Tel.: 030-18412 2118, E-Mail: armin.weiser@bfr.bund.de)", true, new Dimension(750, 300), null, true);
					ib.setVisible(true);    				  										        									    		
		    	}
				MyLogger.handleMessage(username + " logged in!" + "\nDB.data (size): " + fs);
				/*
				if (fs >= 500*1024*1024) { // 500MB
			    	MyLogger.handleMessage("vor CHECKPOINT DEFRAG: " + fs);
			    	DBKernel.sendRequest("CHECKPOINT DEFRAG", false);
			    	System.gc();
			    	MyLogger.handleMessage("nach CHECKPOINT DEFRAG: " + DBKernel.getFileSize(DBKernel.HSHDB_PATH + "DB.data"));
				}				
				*/
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
					File temp = DBKernel.getCopyOfInternalDB();
					if (!Backup.doRestore(myDB, temp, true)) { // Passwort hat sich ver�ndert innerhalb der 2 beteiligten Datenbanken...
						passwordField1.setBackground(Color.RED);
						passwordField2.setBackground(Color.WHITE);
						passwordField3.setBackground(Color.WHITE);
						passwordField1.requestFocus();					
						return null;
					}

					MyDBTables.loadMyTables();
					myList.addAllTables();

				DBKernel.importing = false;
			}
			else {
				MyDBTables.loadMyTables();		
				myList.addAllTables();

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
					  	if (dbVersion.equals("1.3.6")) {
					  		UpdateChecker.check4Updates_136_137(); 
					  		DBKernel.setDBVersion("1.3.7");
					  	}
					  	else if (dbVersion.equals("1.3.7")) {
					  		UpdateChecker.check4Updates_137_138(); 
					  		DBKernel.setDBVersion("1.3.8");
					  	}
					  	else if (dbVersion.equals("1.3.8")) {
					  		UpdateChecker.check4Updates_138_139(); 
					  		DBKernel.setDBVersion("1.3.9");
					  	}
					  	else if (dbVersion.equals("1.3.9")) {
					  		UpdateChecker.check4Updates_139_140(); 
					  		DBKernel.setDBVersion("1.4.0");
					  	}
					  	else if (dbVersion.equals("1.4.0")) {
					  		UpdateChecker.check4Updates_140_141(); 
					  		DBKernel.setDBVersion("1.4.1");
					  	}
					  	else if (dbVersion.equals("1.4.1")) {
					  		UpdateChecker.check4Updates_141_142(); 
					  		DBKernel.setDBVersion("1.4.2");
					  	}
					  	else if (dbVersion.equals("1.4.2")) {
					  		UpdateChecker.check4Updates_142_143(); 
					  		DBKernel.setDBVersion("1.4.3");
					  	}
					  	else if (dbVersion.equals("1.4.3")) {
					  		UpdateChecker.check4Updates_143_144(); 
					  		DBKernel.setDBVersion("1.4.4");
					  	}
					  	else if (dbVersion.equals("1.4.4")) {
					  		UpdateChecker.check4Updates_144_145(); 
					  		DBKernel.setDBVersion("1.4.5");
					  	}

					  	if (DBKernel.getDBVersion().equals("1.4.5")) {
					  		UpdateChecker.check4Updates_145_146(); 
					  		DBKernel.setDBVersion("1.4.6");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.4.6")) {
					  		UpdateChecker.check4Updates_146_147(); 
					  		DBKernel.setDBVersion("1.4.7");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.4.7")) {
					  		UpdateChecker.check4Updates_147_148(); 
					  		DBKernel.setDBVersion("1.4.8");
					  	}					  	
					  	if (DBKernel.getDBVersion().equals("1.4.8")) {
					  		UpdateChecker.check4Updates_148_149();
					  		DBKernel.setDBVersion("1.4.9");
					  	}					  	
					  	if (DBKernel.getDBVersion().equals("1.4.9")) {
					  		UpdateChecker.check4Updates_149_150(); 
					  		DBKernel.setDBVersion("1.5.0");
					  	}
					  	
					  	if (DBKernel.getDBVersion().equals("1.5.0")) {
					  		UpdateChecker.check4Updates_150_151(); 
					  		DBKernel.setDBVersion("1.5.1");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.1")) {
					  		UpdateChecker.check4Updates_151_152(); 
					  		DBKernel.setDBVersion("1.5.2");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.2")) {
					  		UpdateChecker.check4Updates_152_153(); 
					  		DBKernel.setDBVersion("1.5.3");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.3")) {
					  		UpdateChecker.check4Updates_153_154(); 
					  		DBKernel.setDBVersion("1.5.4");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.4")) {
					  		UpdateChecker.check4Updates_154_155(); 
					  		DBKernel.setDBVersion("1.5.5");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.5")) {
					  		UpdateChecker.check4Updates_155_156(); 
					  		DBKernel.setDBVersion("1.5.6");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.6")) {
					  		UpdateChecker.check4Updates_156_157(); 
					  		DBKernel.setDBVersion("1.5.7");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.7")) {
					  		UpdateChecker.check4Updates_157_158(); 
					  		DBKernel.setDBVersion("1.5.8");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.8")) {
					  		UpdateChecker.check4Updates_158_159(); 
					  		DBKernel.setDBVersion("1.5.9");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.5.9")) {
					  		UpdateChecker.check4Updates_159_160(); 
					  		DBKernel.setDBVersion("1.6.0");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.6.0")) {
					  		UpdateChecker.check4Updates_160_161(); 
					  		DBKernel.setDBVersion("1.6.1");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.6.1")) {
					  		UpdateChecker.check4Updates_161_162(); 
					  		DBKernel.setDBVersion("1.6.2");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.6.2")) {
					  		UpdateChecker.check4Updates_162_163(); 
					  		DBKernel.setDBVersion("1.6.3");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.6.3")) {
					  		UpdateChecker.check4Updates_163_164(); 
					  		DBKernel.setDBVersion("1.6.4");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.6.4")) {
					  		UpdateChecker.check4Updates_164_165(); 
					  		DBKernel.setDBVersion("1.6.5");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.6.5")) {
					  		UpdateChecker.check4Updates_165_166(); 
					  		DBKernel.setDBVersion("1.6.6");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.6.6")) {
					  		UpdateChecker.check4Updates_166_167(); 
					  		DBKernel.setDBVersion("1.6.7");
					  	}
					  	if (DBKernel.getDBVersion().equals("1.6.7")) {
					  		UpdateChecker.check4Updates_167_168(); 
					  		DBKernel.setDBVersion("1.6.8");
					  	}

						DBKernel.closeDBConnections(false);
					}
					catch (Exception e) {e.printStackTrace();DBKernel.dontLog = dl;return myList;}
					DBKernel.dontLog = dl;
					loadDB();		
					return myList;
				}
			}

			//DBKernel.sendRequest("DELETE FROM " + DBKernel.delimitL("ChangeLog"), false); //  + " WHERE " + DBKernel.delimitL("ID") + " < 45000"
			if (!myList.setSelection(DBKernel.prefs.get("LAST_SELECTED_TABLE", "Versuchsbedingungen"))) {  // Agens_Nachweisverfahren  Agenzien
				myList.setSelection(null);
			}

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



