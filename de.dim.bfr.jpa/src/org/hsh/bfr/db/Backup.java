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
package org.hsh.bfr.db;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.hsh.bfr.db.gui.dbtable.MyDBTable;

import de.dim.bfr.jpa.internal.BFRJPAActivator;

/**
 * @author Armin
 *
 */
public class Backup extends FileFilter {
	
	  public static boolean dbBackup() {
		  return dbBackup(DBKernel.mainFrame);
	  }
	public static boolean dbBackup(final JFrame frame) {
  	String lastOutDir = DBKernel.prefs.get("LAST_OUTPUT_DIR", "");
	  JFileChooser fc = new JFileChooser(lastOutDir);
	  Backup bkp = new Backup();
	  fc.setFileFilter(bkp);
	  fc.setAcceptAllFileFilterUsed(false);
	  fc.setMultiSelectionEnabled(false);	  
	  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	  Calendar c1 = Calendar.getInstance();
	  fc.setSelectedFile(new File(DBKernel.getUsername() + "_" + DBKernel.DBVersion + "_" + sdf.format(c1.getTime()) + ".tar.gz")); // "AP1-2-DB_" + System.currentTimeMillis() + ".tar.gz"
	  fc.setDialogTitle("Backup");
	  int returnVal = fc.showSaveDialog(frame);
	  if (returnVal == JFileChooser.APPROVE_OPTION) {
	  	File selectedFile = fc.getSelectedFile();
	  	if (selectedFile != null) {
	  		DBKernel.prefs.put("LAST_OUTPUT_DIR", selectedFile.getParent());
	    	// siehe BUG: bugs.sun.com/bugdatabase/view_bug.do?bug_id=4847375
	    	try {
	      	//System.out.println(fc.getCurrentDirectory());
	    		Runtime.getRuntime().exec("attrib -r \"" + fc.getCurrentDirectory() + "\"");
	    	}
	    	catch (Exception e) {MyLogger.handleException(e);}
	  		if (selectedFile.exists()) {
	  			returnVal = JOptionPane.showConfirmDialog(frame, "Soll die Datei ersetzt werden?", "Backup Datei bereits vorhanden", JOptionPane.YES_NO_CANCEL_OPTION);
	  			if (returnVal == JOptionPane.NO_OPTION) {return dbBackup(frame);}
	  			else if (returnVal == JOptionPane.YES_OPTION) {
					;
				} else {
					return false;
				}
	  		}
	  		dbBackup(frame, selectedFile, false);
	  	}
	  }
	  else if (returnVal == JFileChooser.CANCEL_OPTION) {
		  return false;
	  }
	  return true;
  }
  private static void dbBackup(final JFrame frame, final File backupFile, final boolean silent) {
  	if (backupFile != null && backupFile.getParentFile().exists() && DBKernel.DBFilesDa()) {
	    try {
	    	if (backupFile.exists()) {
				backupFile.delete();
			}
	    	System.gc();
	    	String filename = backupFile.getAbsolutePath();
	    	if (!filename.endsWith(".tar.gz")) {
				filename += ".tar.gz";
			}

	    	boolean isAdmin = DBKernel.isAdmin();
	    	if (!isAdmin) {
	    		MyDBTable myDB = (DBKernel.myList == null ? null :  DBKernel.myList.getMyDBTable());
		    	if (myDB != null) {
					myDB.checkUnsavedStuff();
				}
	    		DBKernel.closeDBConnections(false);
	    		DBKernel.getDefaultAdminConn();
	    	}

	    	if (!DBKernel.isServerConnection) {
	    		MyLogger.handleMessage("vor CHECKPOINT DEFRAG: " + DBKernel.getFileSize(DBKernel.HSHDB_PATH + "DB.data"));
		    	DBKernel.sendRequest("CHECKPOINT DEFRAG", false);
		    	MyLogger.handleMessage("nach CHECKPOINT DEFRAG: " + DBKernel.getFileSize(DBKernel.HSHDB_PATH + "DB.data"));
	    	}
	    	
	    	String answerErr = DBKernel.sendRequestGetErr("BACKUP DATABASE TO '" + filename + "' BLOCKING");
	    	if (!silent) {
	  	      if (answerErr.length() == 0) {
	  	      	JOptionPane.showMessageDialog(frame, "In '" + filename + "' wurde erfolgreich ein Backup der Datenbank erstellt!", //  + (DBKernel.isKNIME ? "\nDas Fenster schliesst sich jetzt, bitte neu �ffnen!" : "")
	  	      			"Backup", JOptionPane.INFORMATION_MESSAGE);
	  	      }
	  	      else {
	  	      	JOptionPane.showMessageDialog(frame,
	  	      			"Das Backup der Datenbank ist fehlgeschlagen!\n" +
	  	      			"Die Fehlermeldung lautet:\n" + answerErr,
	  	      			"Backup", JOptionPane.ERROR_MESSAGE);	      	
	  	      }
	    	}
	    	System.gc();
	    	
	    	if (!isAdmin) {
	    		DBKernel.closeDBConnections(false);
				if (!DBKernel.isKNIME) {
		    		MyDBTable myDB = (DBKernel.myList == null ? null :  DBKernel.myList.getMyDBTable());
			    	if (myDB != null) {
			    		myDB.initConn(DBKernel.getDBConnection());
			    		if (myDB.getActualTable() != null) {
							myDB.getActualTable().restoreProperties(myDB);
						}
			    		myDB.syncTableRowHeights();		    		
			    	}
				}
	    	}
	    	
	    }
	    catch (Exception e) {
	    	MyLogger.handleException(e);
	    }
  	}
  	if (DBKernel.isKNIME) {
  		DBKernel.mainFrame.dispose();
		BFRJPAActivator def = BFRJPAActivator.getDefault();
  		def.getUIService().openDBGUI(true);
  	}
  }
  
  public static void doRestore(final MyDBTable myDB) {
  	String lastOutDir = DBKernel.prefs.get("LAST_OUTPUT_DIR", "");
	  JFileChooser fc = new JFileChooser(lastOutDir);
	  Backup bkp = new Backup();
	  fc.setFileFilter(bkp);
	  fc.setAcceptAllFileFilterUsed(false);
	  fc.setMultiSelectionEnabled(false);
	  fc.setDialogTitle("Restore");
	  int returnVal = fc.showOpenDialog(DBKernel.mainFrame);
	  if(returnVal == JFileChooser.APPROVE_OPTION) {
		  	File selectedFile = fc.getSelectedFile();
	  		if (selectedFile != null) {
				DBKernel.prefs.put("LAST_OUTPUT_DIR", selectedFile.getParent());
			}
		  	doRestore(myDB, selectedFile, false);
	  }	  
  }
  public static boolean doRestore(final MyDBTable myDB, final File scriptFile, final boolean silent) {
	  boolean result = true;
  	if (scriptFile != null && scriptFile.exists()) {
  		if (!silent) {
			int returnVal = JOptionPane.showConfirmDialog(DBKernel.mainFrame,
					"Die Datenbank wird gel�scht!\n" +
					"Vielleicht sollten Sie vorher nochmal ein Backup machen...\n" +
					"Soll das Backup wirklich eingespielt werden?",
					"Datenbank l�schen", JOptionPane.YES_NO_OPTION);
			if (returnVal != JOptionPane.YES_OPTION) {
				return result;
			}  			
  		}
			
			// Also los!
			String answerErr = "";
			if (DBKernel.DBFilesDa()) {
		    	boolean isAdmin = DBKernel.isAdmin();
		    	if (!isAdmin) {
			    	if (myDB != null) {
						myDB.checkUnsavedStuff();
					}
			    	if (!DBKernel.closeDBConnections(false)) {
			    		try {DBKernel.getDefaultAdminConn();}
			    		catch (Exception e) {MyLogger.handleException(e);}
			    	}
		    	}
		    	DBKernel.closeDBConnections(false);	    		
				System.gc();
			}
			deleteOldFiles();
			System.gc();
			//org.hsqldb.lib.tar.DbBackup dbb = new org.hsqldb.lib.tar.DbBackup(scriptFile, DBKernel.HSHDB_PATH + "DB");
			try {
				org.hsqldb.lib.tar.DbBackup.main(new String[]{
						"--extract", scriptFile.getAbsolutePath(), DBKernel.HSHDB_PATH});
			}
			catch (Exception e) {
				answerErr += e.getMessage();
				MyLogger.handleException(e);
			}
			System.gc();
			
			try {
				if (!DBKernel.isKNIME) {
					Connection conn = DBKernel.getDBConnection();
					if (conn != null) {
						myDB.initConn(conn);
						myDB.setTable();					
					}
					else {
						result = false;
					}
				}
				if (!silent && answerErr.length() == 0) {
					JOptionPane.showMessageDialog(DBKernel.mainFrame, "Fertig!", //  + (DBKernel.isKNIME ? "\nDas Fenster schliesst sich jetzt, bitte neu �ffnen!" : "")
							"Restore", JOptionPane.INFORMATION_MESSAGE);
					if (!DBKernel.isKNIME) {
						myDB.myRefresh();
					}
				}
			}
			catch (Exception e) {
				if (answerErr.length() > 0) {
					answerErr += "\n";
				}
				answerErr += e.getMessage();
				MyLogger.handleException(e);
			}
			if (!silent && answerErr.length() > 0) {
      	JOptionPane.showMessageDialog(DBKernel.mainFrame,
      			"Das Wiederherstellen der Datenbank ist fehlgeschlagen!\n" +
      			"Die Fehlermeldung lautet:\n" + answerErr,
      			"Restore", JOptionPane.ERROR_MESSAGE);	      					
			}
      System.gc();
  	}
  	if (DBKernel.isKNIME) {
  		DBKernel.mainFrame.dispose();
		BFRJPAActivator def = BFRJPAActivator.getDefault();
  		def.getUIService().openDBGUI(true);
  	}
  	return result;
  }
  private static void deleteOldFiles() {
    java.io.File f = new java.io.File(DBKernel.HSHDB_PATH);
    String fileKennung = "DB.";
    java.io.File[] files = f.listFiles();
    for (int i=0;i<files.length;i++) {
      if (files[i].isFile() && files[i].getName().startsWith(fileKennung)) { //  && !files[i].getName().endsWith(".properties")
        System.gc();
        files[i].delete();
      }
    }
  }


	@Override
	public boolean accept(final File f) {
	  if (f.isDirectory()) {
		return true;
	}
	
	  String extension = getExtension(f);
	  if ((extension.equals("tar.gz"))) {
		return true;
	} 
	  return false;
	}
	  
	@Override
	public String getDescription() {
	    return "Backup Datei (*.tar.gz)";
	}
	
	private String getExtension(final File f) {
	  String s = f.getName();
	  int i = s.lastIndexOf('.');
	  int j = s.lastIndexOf('.', i-1);
	  if (j > 0 &&  j < s.length() - 1) {
		return s.substring(j+1).toLowerCase();
	} else if (i > 0 &&  i < s.length() - 1) {
		return s.substring(i+1).toLowerCase();
	}
	  return "";
	}
  
}
