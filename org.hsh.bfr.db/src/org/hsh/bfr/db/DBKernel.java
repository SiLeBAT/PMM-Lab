/*******************************************************************************
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Joergen Brandt (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Christian Thoens (BfR)
 * Annemarie Kaesbohrer (BfR)
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

import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.hsh.bfr.db.db.XmlLoader;
import org.hsh.bfr.db.gui.Login;
import org.hsh.bfr.db.gui.MainFrame;
import org.hsh.bfr.db.gui.MyList;
import org.hsh.bfr.db.gui.dbtable.MyDBTable;
import org.hsh.bfr.db.gui.dbtable.editoren.MyStringFilter;
import org.hsh.bfr.db.gui.dbtree.MyDBTree;
import org.hsh.bfr.db.imports.InfoBox;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Armin
 * 
 */
public class DBKernel {

	/**
	 * @param args
	 */

	private static HashMap<String, String> adminU = new HashMap<String, String>();
	private static HashMap<String, String> adminP = new HashMap<String, String>();
	private static LinkedHashMap<Object, LinkedHashMap<Object, String>> filledHashtables = new LinkedHashMap<Object, LinkedHashMap<Object, String>>();

	private static Connection localConn = null;
	private static String m_Username = "";
	private static String m_Password = "";

	public final static String HSH_PATH = System.getProperty("user.home")
			+ System.getProperty("file.separator") + ".localHSH"
			+ System.getProperty("file.separator") + "BfR"
			+ System.getProperty("file.separator");
	public static String HSHDB_PATH = HSH_PATH + "DBs"
			+ System.getProperty("file.separator");

	public static boolean importing = false;
	public static boolean dontLog = false;

	public static MyPreferences prefs = new MyPreferences();

	public static MyDBI myDBi = null;
	public static MyList myList = null;
	public static MyDBTable topTable = null;
	public static MainFrame mainFrame = null;
	public static Login login = null;

	public static boolean passFalse = false;

	public static long triggerFired = System.currentTimeMillis();
	public static boolean scrolling = false;
	public static boolean isServerConnection = false;
	public static boolean isKNIME = false;

	public static String DBVersion = "1.7.9";
	public static boolean debug = true;
	public static boolean isKrise = false;

	public static String getTempSA(String dbPath) {
		// String sa = DBKernel.prefs.get("DBADMINUSER" +
		// getCRC32(dbPath),"00");
		// if (sa.equals("00")) {
		if (!adminU.containsKey(dbPath))
			getUP(dbPath);
		return adminU.get(dbPath);
	}

	public static String getTempSAPass(String dbPath) {
		// String pass = DBKernel.prefs.get("DBADMINPASS" +
		// getCRC32(dbPath),"00");
		// if (pass.equals("00")) {
		if (isServerConnection && isKrise)
			return "de6!�5ddy";
		if (!adminP.containsKey(dbPath))
			getUP(dbPath);
		return adminP.get(dbPath);
	}

	private static String getDefaultSA() {
		return getDefaultSA(false);
	}

	private static String getDefaultSAPass() {
		return getDefaultSAPass(false);
	}

	private static String getDefaultSA(boolean other) {
		String sa = "";
		// if (debug) return "SA";
		if (other)
			sa = isKNIME || isKrise ? "defad" : "SA";
		else
			sa = isKNIME || isKrise ? "SA" : "defad";
		return sa;
	}

	private static String getDefaultSAPass(boolean other) {
		String pass = "";
		// if (debug) return "";
		if (other)
			pass = isKNIME || isKrise ? "de6!�5ddy" : "";
		else
			pass = isKNIME || isKrise ? "" : "de6!�5ddy";
		return pass;
	}

	public static void removeAdminInfo(String dbPath) {
		if (adminU.containsKey(dbPath))
			adminU.remove(dbPath);
		if (adminP.containsKey(dbPath))
			adminP.remove(dbPath);
	}

	public static String getLanguage() {
		return !isKNIME && !isKrise ? "de" : "en"; // isKrise ||
	}

	public static boolean getUP(String dbPath) {
		boolean result = false;
		DBKernel.closeDBConnections(false);

		String sa = getDefaultSA();
		String pass = getDefaultSAPass();
		Connection conn = null;
		try {
			conn = getDBConnection(dbPath, sa, pass, false, true);
		} catch (Exception e) {
		}
		if (conn != null && !isAdmin(conn, sa)) {
			try {
				conn.close();
			} catch (Exception e) {
			}
			conn = null;
		}
		if (conn == null) {
			sa = getDefaultSA(true);
			try {
				conn = getDBConnection(dbPath, sa, pass, false, true);
			} catch (Exception e) {
			}
			if (conn != null && !isAdmin(conn, sa)) {
				try {
					conn.close();
				} catch (Exception e) {
				}
				conn = null;
			}
		}
		if (conn == null) {
			pass = getDefaultSAPass(true);
			try {
				conn = getDBConnection(dbPath, sa, pass, false, true);
			} catch (Exception e) {
			}
			if (conn != null && !isAdmin(conn, sa)) {
				try {
					conn.close();
				} catch (Exception e) {
				}
				conn = null;
			}
		}
		if (conn == null) {
			sa = getDefaultSA(false);
			try {
				conn = getDBConnection(dbPath, sa, pass, false, true);
			} catch (Exception e) {
			}
			if (conn != null && !isAdmin(conn, sa)) {
				try {
					conn.close();
				} catch (Exception e) {
				}
				conn = null;
			}
		}

		if (conn == null)
			System.err.println("Admin not found...");
		else {
			result = true;
			adminU.put(dbPath, sa);
			adminP.put(dbPath, pass);
			// System.err.println("pass combi is: " + sa + "\t" + pass);
		}

		try {
			DBKernel.closeDBConnections(false);
			DBKernel.getDBConnection(true);
			if (DBKernel.myList != null
					&& DBKernel.myList.getMyDBTable() != null) {
				DBKernel.myList.getMyDBTable().setConnection(
						DBKernel.getDBConnection(true));
			}
		} catch (Exception e) {
		}

		return result;
	}

	private static boolean different(final Object[] rowBefore,
			final Object[] rowAfter) {
		if (rowBefore == null && rowAfter == null) {
			return false;
		}
		if (rowBefore == null && rowAfter != null || rowBefore != null
				&& rowAfter == null) {
			return true;
		}
		if (rowBefore.equals(rowAfter)) {
			return false;
		}
		for (int i = 0; i < rowBefore.length; i++) {
			if (rowBefore[i] == null && rowAfter[i] == null) {
				;
			} else if (rowBefore[i] == null && rowAfter[i] != null
					|| rowAfter[i] == null && rowBefore[i] != null
					|| !rowBefore[i].toString().equals(rowAfter[i].toString())) {
				return true;
			}
		}
		return false;
	}

	/*
	 * private static Integer getNextChangeLogID(final Connection conn) {
	 * Integer result = null; try { Statement stmt =
	 * conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
	 * ResultSet.CONCUR_READ_ONLY); ResultSet rs =
	 * stmt.executeQuery("SELECT MAX(" + DBKernel.delimitL("ID") + ") FROM " +
	 * DBKernel.delimitL("ChangeLog")); if (rs != null && rs.first()) { result =
	 * rs.getInt(1) + 1; rs.close(); } } catch (Exception e) {
	 * e.printStackTrace(); } return result; }
	 */
	private static Integer callIdentity(final Connection conn) {
		Integer result = null;
		try {
			Statement stmt = conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery("CALL IDENTITY()");
			if (rs != null && rs.first()) {
				result = rs.getInt(1);
				rs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	protected static boolean insertIntoChangeLog(final String tablename,
			final Object[] rowBefore, final Object[] rowAfter) {
		return insertIntoChangeLog(tablename, rowBefore, rowAfter, localConn,
				false);
	}

	protected static boolean insertIntoChangeLog(final String tablename,
			final Object[] rowBefore, final Object[] rowAfter,
			final Connection conn, final boolean suppressWarnings) {
		if (dontLog) {
			return true;
		} else {
			boolean diff = different(rowBefore, rowAfter);
			if (debug && !DBKernel.importing) {
				MyLogger.handleMessage("different: " + diff);
				if (diff) {
					System.err.println(eintragAlt2String(rowBefore));
					System.err.println(eintragAlt2String(rowAfter));
				}
			}
			if (!diff) {
				return true;
			}
			boolean result = false;
			try {
				// getDBConnection();
				String username = DBKernel.getUsername();
				PreparedStatement ps = conn.prepareStatement("INSERT INTO "
						+ DBKernel.delimitL("ChangeLog") + " ("
						+ DBKernel.delimitL("ID") + ", "
						+ DBKernel.delimitL("Zeitstempel") + ", "
						+ DBKernel.delimitL("Username") + ", "
						+ DBKernel.delimitL("Tabelle") + ", "
						+ DBKernel.delimitL("TabellenID") + ", "
						+ DBKernel.delimitL("Alteintrag")
						+ ") VALUES (NEXT VALUE FOR "
						+ DBKernel.delimitL("ChangeLogSEQ")
						+ ", ?, ?, ?, ?, ?)");

				ps.setTimestamp(1, new Timestamp(new Date().getTime()));
				ps.setString(2, username);
				ps.setString(3, tablename);
				int tableID;
				if (rowBefore != null && rowBefore.length > 0
						&& rowBefore[0] != null
						&& rowBefore[0] instanceof Integer) {
					tableID = (Integer) rowBefore[0];
				} else if (rowAfter != null && rowAfter.length > 0
						&& rowAfter[0] != null
						&& rowAfter[0] instanceof Integer) {
					tableID = (Integer) rowAfter[0];
				} else {
					tableID = -1;
				}
				ps.setInt(4, tableID);
				// System.err.println(eintragAlt2String(rowBefore));
				check4SerializationProblems(rowBefore);
				ps.setObject(5, rowBefore);
				triggerFired = System.currentTimeMillis();
				ps.execute();

				if (debug && !DBKernel.importing) {
					System.err.println("callIdentity: " + callIdentity(conn));
					// System.err.println("getLastInsertedID: " +
					// getLastInsertedID(ps));
				}

				result = true;
			} catch (Exception e) {
				if (!suppressWarnings) {
					MyLogger.handleMessage(tablename + ": "
							+ eintragAlt2String(rowBefore) + "\t"
							+ eintragAlt2String(rowAfter));
					MyLogger.handleException(e, true);
				}
			}
			return result;
		}
	}

	private static void check4SerializationProblems(final Object[] rowBefore) {
		if (rowBefore == null) {
			return;
		}
		for (int i = 0; i < rowBefore.length; i++) {
			if (rowBefore[i] instanceof org.hsqldb.types.TimestampData) {
				rowBefore[i] = ((org.hsqldb.types.TimestampData) rowBefore[i])
						.getSeconds();
				// Long d = (Long) rowBefore[i];
				// System.err.println(d + "\t" + rowBefore[i]);
			}
		}
	}

	private static String eintragAlt2String(final Object[] eintragAlt) {
		if (eintragAlt == null) {
			return null;
		}
		String result = eintragAlt[0].toString();
		for (int i = 1; i < eintragAlt.length; i++) {
			result += "," + eintragAlt[i];
		}
		return result;
	}

	public static Integer getLastInsertedID(final PreparedStatement psmt) {
		Integer lastInsertedID = null;
		try {
			ResultSet rs = psmt.getGeneratedKeys();
			if (rs.next()) {
				lastInsertedID = rs.getInt(1);
			} else {
				System.err.println("getGeneratedKeys failed!\n" + psmt);
			}
			rs.close();
		} catch (Exception e) {
		}
		return lastInsertedID;
	}

	protected static void createTable(final String tableName,
			final String fieldDefs, final List<String> indexSQL) {
		createTable(tableName, fieldDefs, indexSQL, true, false);
	}

	protected static void createTable(final String tableName,
			final String fieldDefs, final List<String> indexSQL,
			final boolean cached, final boolean suppressWarnings) {
		try {
			getDBConnection();
			if (tableName.equals("ChangeLog")) { // ||
													// tableName.equals("DateiSpeicher")
													// ||
													// tableName.equals("Infotabelle")
				DBKernel.sendRequest(
						"CREATE SEQUENCE "
								+ DBKernel.delimitL(tableName + "SEQ")
								+ " AS INTEGER START WITH 1 INCREMENT BY 1",
						false);
				DBKernel.sendRequest(
						"GRANT USAGE ON SEQUENCE "
								+ DBKernel.delimitL("ChangeLogSEQ") + " TO "
								+ DBKernel.delimitL("PUBLIC"), false);
			}

			Statement stmt = localConn.createStatement(); // ResultSet.TYPE_SCROLL_INSENSITIVE,
															// ResultSet.CONCUR_READ_ONLY
															// ResultSet.CONCUR_UPDATABLE
			// stmt.execute("DROP TABLE " + delimitL(tableName) +
			// " IF EXISTS;");
			String sqlc = "CREATE " + (cached ? "CACHED" : "MEMORY")
					+ " TABLE " + delimitL(tableName) + " (" + fieldDefs + ");";
			stmt.execute(sqlc);
			// System.out.println(sqlc);
			for (String sql : indexSQL) {
				if (sql.length() > 0) {
					// System.out.println(sql);
					stmt.execute(sql);
				}
			}
			if (!tableName.equals("ChangeLog")
					&& !tableName.equals("DateiSpeicher")
					&& !tableName.equals("Infotabelle")) {
				stmt.execute("CREATE TRIGGER "
						+ delimitL("A_" + tableName + "_U")
						+ " AFTER UPDATE ON " + delimitL(tableName)
						+ " FOR EACH ROW " + " CALL "
						+ delimitL(new MyTrigger().getClass().getName())); // (oneThread
																			// ?
																			// "QUEUE 0"
																			// :
																			// "")
																			// +
				stmt.execute("CREATE TRIGGER "
						+ delimitL("A_" + tableName + "_D")
						+ " AFTER DELETE ON " + delimitL(tableName)
						+ " FOR EACH ROW " + " CALL "
						+ delimitL(new MyTrigger().getClass().getName())); // (oneThread
																			// ?
																			// "QUEUE 0"
																			// :
																			// "")
																			// +
				stmt.execute("CREATE TRIGGER "
						+ delimitL("A_" + tableName + "_I")
						+ " AFTER INSERT ON " + delimitL(tableName)
						+ " FOR EACH ROW " + " CALL "
						+ delimitL(new MyTrigger().getClass().getName())); // (oneThread
																			// ?
																			// "QUEUE 0"
																			// :
																			// "")
																			// +
				if (tableName.equals("Modellkatalog")
						|| tableName.equals("ModellkatalogParameter")
						|| tableName.equals("Modell_Referenz")) {
					/*
					 * || tableName.equals("GeschaetzteModelle") ||
					 * tableName.equals("GeschaetztesModell_Referenz") ||
					 * tableName.equals("GeschaetzteParameter") ||
					 * tableName.equals("GeschaetzteParameterCovCor") ||
					 * tableName.equals("Sekundaermodelle_Primaermodelle") ||
					 * tableName.equals("Literatur")
					 */
					// der INSERT TRIGGER verursacht leider Probleme, weil
					// data-in-motion die neu generierte ID mittels CALL
					// IDENTITY() abruft
					// das liefert aber die in ChangeLog eingetragene ID zur�ck
					// und nicht die in der gew�nschten Tabelle
					// muss also erst mal ohne gehen...
					// bei Literatur auch problematisch... vielleicht sollten
					// wir den Literaturview verstecken?
				} else {
					/*
					 * stmt.execute("CREATE TRIGGER " + delimitL("A_" +
					 * tableName + "_I") + " AFTER INSERT ON " +
					 * delimitL(tableName) + " FOR EACH ROW " + " CALL " +
					 * delimitL(new MyTrigger().getClass().getName())); //
					 * (oneThread ? "QUEUE 0" : "") +
					 */
				}
			}
			stmt.close();
		} catch (Exception e) {
			if (!suppressWarnings)
				MyLogger.handleException(e);
		}
	}

	public static String getPassword() {
		return m_Password;
	}

	public static String getUsername() {
		String username = DBKernel.m_Username;
		try { // im Servermodus muss ich schon abchecken, welcher User
				// eingeloggt ist!
			Connection lconn = getDefaultConnection();
			if (lconn == null) {
				username = DBKernel.m_Username; // lokale Variante
			} else {
				// System.out.println(lconn.getMetaData());
				username = lconn.getMetaData().getUserName(); // Server (hoffe
																// ich klappt
																// immer ...?!?)
			}
		} catch (SQLException e) {
			// MyLogger.handleException(e);
		}
		return username;
	}

	public static void setForeignNullAt(final String tableName,
			final String fieldName, final Object id) {
		try {
			Statement anfrage = getDBConnection().createStatement();
			String sql = "UPDATE " + delimitL(tableName) + " SET "
					+ delimitL(fieldName) + " = NULL WHERE " + delimitL("ID")
					+ " = " + id;
			// System.out.println(sql);
			anfrage.execute(sql);
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
	}

	public static boolean deleteBLOB(final String tableName,
			final String fieldName, final int id) {
		String sql = "DELETE FROM " + delimitL("DateiSpeicher") + " WHERE "
				+ delimitL("TabellenID") + "=" + id + " AND"
				+ delimitL("Tabelle") + "='" + tableName + "' AND "
				+ delimitL("Feld") + "='" + fieldName + "'";
		return sendRequest(sql, false);
	}

	public static boolean insertBLOB(final String tableName,
			final String fieldName, final File fl, final int id) {
		boolean result = false;
		try {
			if (fl.exists()) {
				String sql = "INSERT INTO " + delimitL("DateiSpeicher") + " ("
						+ delimitL("Zeitstempel") + "," + delimitL("Tabelle")
						+ "," + delimitL("Feld") + "," + delimitL("TabellenID")
						+ "," + delimitL("Dateiname") + ","
						+ delimitL("Dateigroesse") + "," + delimitL("Datei")
						+ ")" + " VALUES (?,?,?,?,?,?,?);";

				PreparedStatement psmt = getDBConnection()
						.prepareStatement(sql);
				psmt.clearParameters();
				psmt.setTimestamp(1, new Timestamp(new Date().getTime()));
				psmt.setString(2, tableName);
				psmt.setString(3, fieldName);
				psmt.setInt(4, id);
				psmt.setString(5, fl.getName());
				psmt.setInt(6, (int) fl.length());
				FileInputStream fis = new FileInputStream(fl);
				psmt.setBinaryStream(7, fis, (int) fl.length());
				result = (psmt.executeUpdate() > 0);
				psmt.close();
				fis.close();
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static boolean insertBLOB(final String tableName,
			final String fieldName, final String content,
			final String filename, final int id) {
		boolean result = false;
		try {
			String sql = "INSERT INTO " + delimitL("DateiSpeicher") + " ("
					+ delimitL("Zeitstempel") + "," + delimitL("Tabelle") + ","
					+ delimitL("Feld") + "," + delimitL("TabellenID") + ","
					+ delimitL("Dateiname") + "," + delimitL("Dateigroesse")
					+ "," + delimitL("Datei") + ")"
					+ " VALUES (?,?,?,?,?,?,?);";

			PreparedStatement psmt = getDBConnection().prepareStatement(sql);
			psmt.clearParameters();
			psmt.setTimestamp(1, new Timestamp(new Date().getTime()));
			psmt.setString(2, tableName);
			psmt.setString(3, fieldName);
			psmt.setInt(4, id);
			psmt.setString(5, filename);
			byte[] b = content.getBytes();
			psmt.setInt(6, b.length);
			InputStream bais = new ByteArrayInputStream(b);
			psmt.setBinaryStream(7, bais, b.length);
			result = (psmt.executeUpdate() > 0);
			psmt.close();
			bais.close();
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static List<Integer> getLastChangeLogEntries(final String tablename,
			int fromID) {
		List<Integer> result = new ArrayList<Integer>();
		String sql = "SELECT " + delimitL("TabellenID") + " FROM "
				+ delimitL("ChangeLog") + " WHERE " + delimitL("Tabelle")
				+ " = '" + tablename + "' AND " + delimitL("ID") + " >= "
				+ fromID;
		ResultSet rs = getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				do {
					result.add(rs.getInt(1));
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static LinkedHashMap<String, Timestamp> getFirstUserFromChangeLog(
			final String tablename, final Integer tableID) {
		LinkedHashMap<String, Timestamp> result = new LinkedHashMap<String, Timestamp>();
		String sql = "SELECT " + delimitL("Username") + ","
				+ delimitL("Zeitstempel") + " FROM " + delimitL("ChangeLog")
				+ " WHERE " + delimitL("Tabelle") + " = '" + tablename
				+ "' AND " + delimitL("TabellenID") + " = " + tableID
				+ " ORDER BY " + delimitL("Zeitstempel") + " ASC";
		ResultSet rs = getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				result.put(rs.getString(1), rs.getTimestamp(2));
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static LinkedHashMap<Integer, Vector<String>> getUsersFromChangeLog(
			final String tablename, final Integer tableID) {
		return getUsersFromChangeLog(tablename, tableID, null);
	}

	public static LinkedHashMap<Integer, Vector<String>> getUsersFromChangeLog(
			final String tablename, final String username) {
		return getUsersFromChangeLog(tablename, null, username);
	}

	public static LinkedHashMap<Integer, Vector<String>> getUsersFromChangeLog(
			final String tablename, final Integer tableID, final String username) {
		return getUsersFromChangeLog(null, tablename, tableID, username, false);
	}

	public static LinkedHashMap<Integer, Vector<String>> getUsersFromChangeLog(
			final Statement anfrage, final String tablename,
			final Integer tableID, final String username,
			final boolean showDeletedAsWell) {
		LinkedHashMap<Integer, Vector<String>> result = new LinkedHashMap<Integer, Vector<String>>();
		Vector<String> entries = new Vector<String>();
		String sql = "SELECT "
				+ delimitL("TabellenID")
				+ ","
				+ delimitL("Username")
				+ ","
				+ delimitL("Zeitstempel")
				+ // DISTINCT
				","
				+ delimitL(tablename)
				+ "."
				+ delimitL("ID")
				+ " AS "
				+ delimitL("ID")
				+ ","
				+ delimitL("ChangeLog")
				+ "."
				+ delimitL("ID")
				+ ","
				+ delimitL("Alteintrag")
				+ ","
				+ delimitL(tablename)
				+ ".*"
				+ " FROM "
				+ delimitL("ChangeLog")
				+ " LEFT JOIN "
				+ delimitL(tablename)
				+ " ON "
				+ delimitL("ChangeLog")
				+ "."
				+ delimitL("TabellenID")
				+ "="
				+ delimitL(tablename)
				+ "."
				+ delimitL("ID")
				+ " WHERE "
				+ delimitL("ChangeLog")
				+ "."
				+ delimitL("Tabelle")
				+ " = '"
				+ tablename
				+ "'"
				+ (tableID != null ? " AND " + delimitL("ChangeLog") + "."
						+ delimitL("TabellenID") + " = " + tableID : "")
				+ (username != null ? " AND " + delimitL("ChangeLog") + "."
						+ delimitL("Username") + " = '" + username + "'" : "")
				+ " ORDER BY " + delimitL("ChangeLog") + "." + delimitL("ID")
				+ " ASC"; // Zeitstempel DESC
		// System.out.println(sql);
		ResultSet rs = anfrage == null ? getResultSet(sql, false)
				: getResultSet(anfrage, sql, false);
		try {
			if (rs != null && rs.first()) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"dd.MM.yyyy HH:mm:ss");
				String actualRow = "";
				for (int j = 8; j <= rs.getMetaData().getColumnCount(); j++) {
					actualRow += "\t" + rs.getString(j);
				}
				do {
					if (showDeletedAsWell || rs.getObject("ID") != null) { // wurde
																			// die
																			// ID
																			// in
																			// der
																			// Zwischenzeit
																			// gel�scht?
																			// Dann
																			// muss
																			// sie
																			// auch
																			// nicht
																			// gelistet
																			// werden!
						Integer id = rs.getInt("TabellenID");
						if (result.containsKey(id)) {
							entries = result.get(id);
						} else {
							entries = new Vector<String>();
						}
						String newEntry = rs.getString("Username") + "\t"
								+ sdf.format(rs.getTimestamp("Zeitstempel"));
						Object o = rs.getObject("Alteintrag");
						if (o != null && o instanceof Object[]) {
							Object[] oo = (Object[]) o;
							String ae = "";
							for (int i = 1; i < oo.length; i++) {
								ae += "\t" + oo[i];
							}
							if (entries.size() > 0) {
								String oldEntry = entries
										.get(entries.size() - 1);
								entries.remove(entries.size() - 1);
								int oe = oldEntry.indexOf("\n\t");
								if (oldEntry.startsWith("Unknown\n\t")) {
									oe = oldEntry.indexOf("\n\t", oe + 1);
								}
								if (oe > 0) {
									oldEntry = oldEntry.substring(0, oe) + "\n"
											+ ae;
								} else {
									oldEntry = oldEntry + "\n" + ae;
								}
								entries.add(oldEntry);
							} else {
								// kann passieren, wenn erster Eintrag von
								// defad, z.B. bei Katalogen
								entries.add("Unknown\n\t" + ae.substring(1));
								// newEntry = "Unknown\n\t" + ae.substring(1) +
								// "\n" + newEntry;
							}
						}
						entries.add(newEntry + "\n" + actualRow);
						result.put(id, entries);
					} else {
						// System.err.println(rs.getInt("TabellenID") +
						// " wurde bereits gel�scht!");
					}
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static String delimitL(final String name) {
		String newName = name.replace("\"", "\"\"");
		return "\"" + newName + "\"";
	}

	public static boolean closeDBConnections(final boolean kompakt) {
		boolean result = true;
		try {
			if (localConn != null && !localConn.isClosed()) {
				if (!DBKernel.isServerConnection) {
					try {
						if (kompakt && !isAdmin()) { // kompakt ist nur beim
														// Programm schliessen
														// true
							closeDBConnections(false);
							try {
								localConn = getDefaultAdminConn(HSHDB_PATH,
										false, true);
							} catch (Exception e) {
								e.printStackTrace();
							}
							if (localConn == null) {
								getUP(HSHDB_PATH);
								if (localConn != null)
									localConn.close();
								try {
									localConn = getDefaultAdminConn(HSHDB_PATH,
											false, true);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						Statement stmt = localConn.createStatement(); // ResultSet.TYPE_SCROLL_INSENSITIVE,
																		// ResultSet.CONCUR_READ_ONLY
						MyLogger.handleMessage("vor SHUTDOWN");
						stmt.execute("SHUTDOWN"); // Hier kanns es eine
													// Exception geben, weil nur
													// der Admin SHUTDOWN machen
													// darf!
					} catch (SQLException e) {
						result = false;
						if (kompakt)
							e.printStackTrace();
					} // e.printStackTrace();
				}
				MyLogger.handleMessage("vor close");
				localConn.close();
				MyLogger.handleMessage("vor gc");
				System.gc();
				System.runFinalization();
				try {
					if (myList != null && myList.getMyDBTable() != null
							&& myList.getMyDBTable().getActualTable() != null) {
						DBKernel.prefs
								.put("LAST_SELECTED_TABLE", myList
										.getMyDBTable().getActualTable()
										.getTablename());

						DBKernel.prefs
								.put("LAST_MainFrame_FULL",
										DBKernel.mainFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH ? "TRUE"
												: "FALSE");
						// DBKernel.mainFrame.setExtendedState(JFrame.NORMAL);

						DBKernel.prefs.put("LAST_MainFrame_WIDTH",
								DBKernel.mainFrame.getWidth() + "");
						DBKernel.prefs.put("LAST_MainFrame_HEIGHT",
								DBKernel.mainFrame.getHeight() + "");
						DBKernel.prefs.put("LAST_MainFrame_X",
								DBKernel.mainFrame.getX() + "");
						DBKernel.prefs.put("LAST_MainFrame_Y",
								DBKernel.mainFrame.getY() + "");

						DBKernel.prefs.prefsFlush();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			result = false;
			MyLogger.handleException(e);
		}
		return result;
	}

	public static void getPaper(final int tableID, final String tablename,
			final String feldname, final int blobID) {
		try {
			ResultSet rs = getResultSet(
					"SELECT "
							+ DBKernel.delimitL("Dateiname")
							+ ","
							+ DBKernel.delimitL("Datei")
							+ " FROM "
							+ delimitL("DateiSpeicher")
							+ (blobID > 0 ? " WHERE " + DBKernel.delimitL("ID")
									+ "=" + blobID : " WHERE "
									+ DBKernel.delimitL("Tabelle") + "='"
									+ tablename + "' AND "
									+ DBKernel.delimitL("Feld") + "='"
									+ feldname + "' AND "
									+ DBKernel.delimitL("TabellenID") + "="
									+ tableID + " " + " ORDER BY "
									+ delimitL("ID") + " DESC"), true);
			if (rs.first()) {
				do {
					try {
						final String filename = rs.getString("Dateiname");
						// final InputStream is = rs.getBinaryStream("Datei");
						final byte[] b = rs.getBytes("Datei");
						if (b != null) { // is
							Runnable runnable = new Runnable() {
								@Override
								public void run() {
									try {
										String tmpFolder = System
												.getProperty("java.io.tmpdir");
										String pathname = "";
										if (tmpFolder != null
												&& tmpFolder.length() > 0) {
											// ByteArrayOutputStream out = null;
											FileOutputStream out = null;
											try {
												// out = new
												// ByteArrayOutputStream();
												if (!tmpFolder
														.endsWith(System
																.getProperty("file.separator"))) {
													tmpFolder += System
															.getProperty("file.separator");
												}
												pathname = tmpFolder + filename;
												out = new FileOutputStream(
														pathname);
												// int c;
												// while ((c = is.read()) != -1)
												// out.write(c);
												// int availableLength =
												// is.available();
												// byte[] totalBytes = new
												// byte[availableLength];
												// int bytedata =
												// is.read(totalBytes);
												out.write(b); // totalBytes
												// byte[] ba =
												// out.toByteArray();
												// System.out.println("InputStreamLen = "
												// + ba.length + "\tfeldname = "
												// + feldname + "\ttableID = " +
												// tableID + "\tfilename = " +
												// filename);
											} finally {
												/*
												 * if (is != null) { is.close();
												 * }
												 */
												if (out != null) {
													out.close();
												}
											}
											if (pathname.length() > 0) {
												// Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler \""
												// + new File(pathname) + "\"");
												Runtime.getRuntime()
														.exec(new String[] {
																"rundll32",
																"url.dll,FileProtocolHandler",
																new File(
																		pathname)
																		.getAbsolutePath() });
											}
										}
									} catch (Exception e) {
										MyLogger.handleException(e);
									}
								}
							};
							Thread thread = new Thread(runnable);
							thread.start();
						} else {
							MyLogger.handleMessage("InputStream = null\tfeldname = "
									+ feldname
									+ "\ttableID = "
									+ tableID
									+ "\tfilename = " + filename);
						}
					} catch (Exception e) {
						MyLogger.handleException(e);
					}
					break; // nur das zuletzt abgespeicherte soll ge�ffnet
							// werden!
				} while (rs.next());
			}
		} catch (SQLException e) {
			MyLogger.handleException(e);
		}
	}

	public static Connection getDefaultConnection() {
		Connection result = null;
		String connStr = "jdbc:default:connection";
		try {
			result = DriverManager.getConnection(connStr);
		} catch (Exception e) {
			// MyLogger.handleException(e);
		}
		return result;
	}

	public static Connection getDBConnection(boolean suppressWarnings)
			throws Exception {
		return getDBConnection(HSHDB_PATH, DBKernel.m_Username,
				DBKernel.m_Password, false, suppressWarnings);
	}

	public static Connection getDBConnection() throws Exception {
		return getDBConnection(false);
	}

	public static Connection getDBConnection(final String username,
			final String password) throws Exception {
		DBKernel.m_Username = username;
		DBKernel.m_Password = password;
		return getDBConnection(HSHDB_PATH, username, password, false);
	}

	public static void setLocalConn(final Connection conn, String path,
			String username, String password) {
		localConn = conn;
		DBKernel.HSHDB_PATH = path;
		DBKernel.m_Username = username;
		DBKernel.m_Password = password;
	}

	public static Connection getLocalConn(boolean try2Boot) {
		return getLocalConn(try2Boot, true);
	}

	public static Connection getLocalConn(boolean try2Boot, boolean autoUpdate) {
		try {
			if ((localConn == null || localConn.isClosed()) && try2Boot
					&& isKNIME)
				localConn = getInternalKNIMEDB_LoadGui(autoUpdate);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return localConn;
	}

	public static void setCaller4Trigger(String tableName,
			Callable<Void> caller4Trigger) {
		XmlLoader.doTest();
		MyTable myT = DBKernel.myDBi.getTable(tableName);
		if (myT != null)
			myT.setCaller4Trigger(caller4Trigger);
	}

	// newConn wird nur von MergeDBs ben�tigt
	public static Connection getDBConnection(final String dbPath,
			final String theUsername, final String thePassword,
			final boolean newConn) throws Exception {
		return getDBConnection(dbPath, theUsername, thePassword, newConn, false);
	}

	private static Connection getDBConnection(final String dbPath,
			final String theUsername, final String thePassword,
			final boolean newConn, final boolean suppressWarnings)
			throws Exception {
		if (newConn) {
			return getNewConnection(theUsername, thePassword, dbPath,
					suppressWarnings);
		} else if (localConn == null || localConn.isClosed()) {
			localConn = getNewConnection(theUsername, thePassword, dbPath,
					suppressWarnings);
		}
		return localConn;
	}

	public static Connection getDefaultAdminConn(final String dbPath,
			final boolean newConn, final boolean suppressWarnings)
			throws Exception {
		Connection result = getDBConnection(dbPath, getTempSA(dbPath),
				getTempSAPass(dbPath), newConn, suppressWarnings);
		return result;
	}

	// newConn wird nur von MergeDBs und Bfrdb ben�tigt
	public static Connection getDefaultAdminConn(final String dbPath,
			final boolean newConn) throws Exception {
		Connection result = getDBConnection(dbPath, getTempSA(dbPath),
				getTempSAPass(dbPath), newConn);
		return result;
	}

	public static Connection getDefaultAdminConn() throws Exception {
		return getDefaultAdminConn(DBKernel.HSHDB_PATH, false);
	}

	private static Connection getNewConnection(final String dbUsername,
			final String dbPassword, final String path,
			final boolean suppressWarnings) throws Exception {
		// Sicherheitshalber erstmal alles wieder auf Read/Write Access setzen!
		DBKernel.prefs.putBoolean("PMM_LAB_SETTINGS_DB_RO", false);
		DBKernel.prefs.prefsFlush();
		if (isServerConnection) {
			return getNewServerConnection(dbUsername, dbPassword, path,
					suppressWarnings);
		} else {
			return getNewLocalConnection(dbUsername, dbPassword, path + "DB",
					suppressWarnings);
		}
	}

	public static Connection getNewServerConnection(final String dbUsername,
			final String dbPassword, final String serverPath) throws Exception {
		return getNewServerConnection(dbUsername, dbPassword, serverPath, false);
	}

	private static Connection getNewServerConnection(final String dbUsername,
			final String dbPassword, final String serverPath,
			final boolean suppressWarnings) throws Exception {
		// serverPath = "192.168.212.54/silebat";
		Connection result = null;
		passFalse = false;
		Class.forName("org.hsqldb.jdbc.JDBCDriver").newInstance();
		// System.out.println(dbFile);
		String connStr = "jdbc:hsqldb:hsql://" + serverPath;// + (isKNIME ?
															// ";readonly=true"
															// : "");// +
															// ";hsqldb.cache_rows=1000000;hsqldb.cache_size=1000000";
		try {
			result = DriverManager.getConnection(connStr, dbUsername,
					dbPassword);
			result.setReadOnly(DBKernel.isReadOnly());
		} catch (Exception e) {
			passFalse = e.getMessage().startsWith(
					"invalid authorization specification");
			if (!suppressWarnings)
				MyLogger.handleException(e);
		}
		return result;
	}

	public static boolean isHsqlServer(String checkURL) {
		boolean result = false; // checkURL.startsWith("192") ||
								// checkURL.startsWith("localhost");
		String host = "";
		try {
			if (!checkURL.startsWith("http")) {
				checkURL = "http://" + checkURL;
			}
			URL url = new URL(checkURL); // "192.168.212.54/silebat"
			host = url.getHost();
			if (!host.isEmpty()) {
				InetSocketAddress isa = new InetSocketAddress(host, 9001);// new
																			// URL(checkURL).openConnection();
				result = !isa.isUnresolved();
			}
		} catch (MalformedURLException e) {
			// e.printStackTrace();
		}
		// System.err.println(checkURL + "\t" + result + "\t" + host);
		return result;
	}

	public static Connection getNewLocalConnection(final String dbUsername,
			final String dbPassword, final String dbFile) throws Exception {
		return getNewLocalConnection(dbUsername, dbPassword, dbFile, false);
	}

	public static Connection getNewLocalConnection(final String dbUsername,
			final String dbPassword, final String dbFile,
			final boolean suppressWarnings) throws Exception {
		// startHsqldbServer("c:/tmp/DB", "DB");
		Connection result = null;
		passFalse = false;
		Class.forName("org.hsqldb.jdbc.JDBCDriver").newInstance();
		// System.out.println(dbFile);
		String connStr = "jdbc:hsqldb:file:" + dbFile;// +
														// ";hsqldb.cache_rows=1000000;hsqldb.cache_size=1000000";//
														// + (isKNIME ?
														// ";readonly=true" :
														// "");
		// connStr =
		// "jdbc:hsqldb:hsql://localhost/DB;hsqldb.cache_rows=1000000;hsqldb.cache_size=1000000;hsqldb.tx=mvcc";
		// //
		try {
			result = DriverManager.getConnection(connStr
			// +
			// ";crypt_key=65898eaeb54a0bc34097cae57259e8f9;crypt_type=blowfish"
					, dbUsername, dbPassword);
			result.setReadOnly(DBKernel.isReadOnly());
		} catch (Exception e) {
			// Database lock acquisition failure: lockFile:
			// org.hsqldb.persist.LockFile@137939d4[file =C:\Dokumente und
			// Einstellungen\Weiser\.localHSH\BfR\DBs\DB.lck, exists=true,
			// locked=false, valid=false, ] method: checkHeartbeat read:
			// 2010-12-08 09:08:12 heartbeat - read: -4406 ms.
			//
			passFalse = e.getMessage().startsWith(
					"invalid authorization specification");
			// MyLogger.handleMessage(e.getMessage());
			if (e.getMessage().startsWith("Database lock acquisition failure:")) {
				result = getLocalCopyROConnection(dbFile, dbUsername,
						dbPassword);
				if (result == null) {
					InfoBox ib = new InfoBox(
							login,
							"Die Datenbank wird zur Zeit von\neinem anderen Benutzer verwendet!",
							true, new Dimension(300, 150), null, true);
					ib.setVisible(true);
				}
			} else {
				if (!suppressWarnings)
					MyLogger.handleException(e);
				// if (!suppressWarnings || !isKNIME &&
				// adminU.containsKey(dbFile.substring(0, dbFile.length() - 2)))
				// MyLogger.handleException(e);
			}
			// LOGGER.log(Level.INFO, dbUsername + " - " + dbPassword + " - " +
			// dbFile, e);
		}
		return result;
	}

	private static Connection getLocalCopyROConnection(final String dbFile,
			final String dbUsername, final String dbPassword) {
		Connection result = null;
		try {
			File tempDir = File.createTempFile("DBdirectory", ".db");
			tempDir.delete();
			copyDirectory(new File(dbFile).getParentFile(), tempDir);

			if (debug) {
				MyLogger.handleMessage(tempDir.getAbsolutePath());
			}
			result = DriverManager.getConnection(
					"jdbc:hsqldb:file:" + tempDir.getAbsolutePath()
							+ System.getProperty("file.separator") + "DB"// ;hsqldb.cache_rows=1000000;hsqldb.cache_size=1000000"
																			// //
																			// ;readonly=true
							// result =
							// DriverManager.getConnection("jdbc:hsqldb:file:" +
							// "C:\\Dokumente und Einstellungen\\Weiser\\Lokale Einstellungen\\Temp\\DBdirectory3007129564907469644.db\\DB"
							// +
							// ";hsqldb.cache_rows=1000000;hsqldb.cache_size=1000000"
							// + ";readonly=true"
							// +
							// ";crypt_key=65898eaeb54a0bc34097cae57259e8f9;crypt_type=blowfish"
					, dbUsername, dbPassword);
			result.setReadOnly(true);
		} catch (Exception e1) {
			MyLogger.handleException(e1);
		}
		return result;
	}

	private static void copyDirectory(final File sourceLocation,
			final File targetLocation) {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				if (children[i].startsWith("DB.")) {
					copyDirectory(new File(sourceLocation, children[i]),
							new File(targetLocation, children[i]));
				}
			}
		} else {
			FileChannel inChannel = null, outChannel = null;
			try {
				inChannel = new FileInputStream(sourceLocation).getChannel();
				outChannel = new FileOutputStream(targetLocation).getChannel();
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} catch (IOException e) {
				MyLogger.handleException(e);
			} finally {

				try {
					if (inChannel != null) {
						inChannel.close();
					}
					if (outChannel != null) {
						outChannel.close();
					}
				} catch (IOException e) {
					MyLogger.handleException(e);
				}
			}
		}
	}

	public static Integer getMaxID(final String tablename) {
		Integer result = null;
		String sql = "SELECT TOP 1 " + delimitL("ID") + " FROM "
				+ delimitL(tablename) + " ORDER BY " + delimitL("ID") + " DESC";
		ResultSet rs = getResultSet(sql, false);
		try {
			if (rs != null && rs.first()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static Integer getID(final String tablename,
			final String[] feldname, final String[] feldVal) {
		Integer result = null;
		String sql = "SELECT " + delimitL("ID") + " FROM "
				+ delimitL(tablename) + " WHERE ";
		String where = " ";
		for (int i = 0; i < feldname.length; i++) {
			if (i < feldVal.length) {
				if (!where.trim().isEmpty())
					where += " AND ";
				where += delimitL(feldname[i]);
				if (feldVal[i] == null) {
					where += " IS NULL";
				} else {
					where += " = '" + feldVal[i].replace("'", "''") + "'";
				}
			}
		}
		ResultSet rs = getResultSet(sql + where, true);
		try {
			if (rs != null && rs.last()) {
				result = rs.getInt(1);
				if (rs.getRow() > 1) {
					System.err.println("Attention! Entry occurs " + rs.getRow()
							+ "x in table " + tablename + ", please check: '"
							+ where + "'!!!");
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static Integer getID(final String tablename, final String feldname,
			final String feldVal) {
		Integer result = null;
		String sql = "SELECT " + delimitL("ID") + " FROM "
				+ delimitL(tablename) + " WHERE " + delimitL(feldname);
		if (feldVal == null) {
			sql += " IS NULL";
		} else {
			sql += " = '" + feldVal.replace("'", "''") + "'";
		}
		ResultSet rs = getResultSet(sql, true);
		try {
			if (rs != null && rs.last()) {
				result = rs.getInt(1);
				if (rs.getRow() > 1) {
					System.err.println("Attention! Entry " + feldVal
							+ " occurs " + rs.getRow() + "x in column "
							+ feldname + " of table " + tablename
							+ ", please check!!!");
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static Integer getLastID(final String tablename) {
		Integer result = null;
		String sql = "SELECT MAX(" + delimitL("ID") + ") FROM "
				+ delimitL(tablename);
		ResultSet rs = getResultSet(sql, true);
		try {
			if (rs != null && rs.last()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static Object getValue(final String tablename,
			final String feldname, final String feldVal,
			final String desiredColumn) {
		return getValue(null, tablename, feldname, feldVal, desiredColumn);
	}

	public static Object getValue(Connection conn, final String tablename,
			final String feldname, final String feldVal,
			final String desiredColumn) {
		return getValue(conn, tablename, new String[] { feldname },
				new String[] { feldVal }, desiredColumn, true);
	}

	public static Object getValue(Connection conn, final String tablename,
			final String[] feldname, final String[] feldVal,
			final String desiredColumn) {
		return getValue(conn, tablename, feldname, feldVal, desiredColumn,
				false);
	}

	public static Object getValue(Connection conn, final String tablename,
			final String[] feldname, final String[] feldVal,
			final String desiredColumn, boolean suppressWarnings) {
		Object result = null;
		String sql = "SELECT " + delimitL(desiredColumn) + " FROM "
				+ delimitL(tablename) + " WHERE ";
		String where = " ";
		for (int i = 0; i < feldname.length; i++) {
			if (i < feldVal.length) {
				if (!where.trim().isEmpty())
					where += " AND ";
				where += delimitL(feldname[i]);
				if (feldVal[i] == null) {
					where += " IS NULL";
				} else {
					where += " = '" + feldVal[i].replace("'", "''") + "'";
				}
			}
		}
		ResultSet rs = getResultSet(conn, sql + where, true);
		try {
			if (rs != null && rs.last()) { // && rs.getRow() == 1
				result = rs.getObject(1);
				if (!suppressWarnings && rs.getRow() > 1) {
					System.err.println("Attention! '" + where + "' results in "
							+ rs.getRow() + " entries in table " + tablename
							+ ", please check (getValue)!!!");
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static boolean isDouble(final String textValue) {
		boolean result = true;
		try {
			// System.out.println(textValue);
			if (textValue.equals("-")) {
				return true;
			}
			Double.parseDouble(textValue);
		} catch (NumberFormatException e) {
			result = false;
		}
		return result;
	}

	public static boolean hasID(final String tablename, final int id) {
		boolean result = false;
		ResultSet rs = getResultSet("SELECT " + delimitL("ID") + " FROM "
				+ delimitL(tablename) + " WHERE " + delimitL("ID") + "=" + id,
				true);
		try {
			if (rs != null && rs.last() && rs.getRow() == 1) {
				result = true;
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	private static String handleField(final Object id,
			final MyTable[] foreignFields, final String[] mnTable, final int i,
			final boolean goDeeper, final String startDelim,
			final String delimiter, final String endDelim,
			final boolean newRow, HashSet<MyTable> alreadyUsed) {
		String result = "";
		if (id == null) {
			;
		} else if (foreignFields != null && i > 1
				&& foreignFields.length > i - 2 && foreignFields[i - 2] != null) {
			if (goDeeper) {
				LinkedHashMap<Object, String> hashBox = fillHashtable(
						foreignFields[i - 2],
						startDelim,
						delimiter,
						endDelim,
						goDeeper && !alreadyUsed.contains(foreignFields[i - 2]),
						false, alreadyUsed); // " | " " ; "
				if (hashBox != null && hashBox.get(id) != null) {
					String ssttrr = hashBox.get(id).toString();
					result = ssttrr.trim().length() == 0 ? "" : ssttrr; // ft +
																		// ":\n"
																		// +
				} else if (mnTable != null && i > 1 && i - 2 < mnTable.length
						&& mnTable[i - 2] != null
						&& mnTable[i - 2].length() > 0) {
					result = "";
					// System.err.println("isMN..." + ft);
				} else {
					System.err.println("hashBox �berpr�fen...\t" + id);
					result = "";// ft + ": leer\n";
				}
			} else {
				String ft = foreignFields[i - 2].getTablename();
				result = ft + "-ID: " + id + "\n";
			}
		} else {
			result = (id instanceof Double ? DBKernel.getDoubleStr(id) : id
					.toString());
		}
		if (result.length() > 0) {
			if (mnTable != null && i > 1 && i - 2 < mnTable.length
					&& mnTable[i - 2] != null && mnTable[i - 2].length() > 0) { // MN-Tabellen,
																				// wie
																				// z.B.
																				// INT
																				// oder
																				// DBL
																				// sollten
																				// hier
																				// unsichtbar
																				// bleiben!
			} else {
				result += (newRow ? "\n" : ""); // rs.getMetaData().getColumnName(i)
												// + ": " +
			}
		}
		return result;
	}

	public static void refreshHashTables() {
		filledHashtables.clear();
	}

	public static LinkedHashMap<Object, String> fillHashtable(
			final MyTable theTable, final String startDelim,
			final String delimiter, final String endDelim,
			final boolean goDeeper) {
		return fillHashtable(theTable, startDelim, delimiter, endDelim,
				goDeeper, false);
	}

	public static LinkedHashMap<Object, String> fillHashtable(
			final MyTable theTable, final String startDelim,
			final String delimiter, final String endDelim,
			final boolean goDeeper, final boolean forceUpdate) {
		return fillHashtable(theTable, startDelim, delimiter, endDelim,
				goDeeper, forceUpdate, null);
	}

	public static LinkedHashMap<Object, String> fillHashtable(
			final MyTable theTable, final String startDelim,
			final String delimiter, final String endDelim,
			final boolean goDeeper, final boolean forceUpdate,
			HashSet<MyTable> alreadyUsed) {
		if (theTable == null) {
			return null;
		}
		String foreignTable = theTable.getTablename();
		if (forceUpdate && filledHashtables.containsKey(foreignTable)) {
			filledHashtables.remove(foreignTable);
		}
		if (filledHashtables.containsKey(foreignTable)) {
			return filledHashtables.get(foreignTable);
		}

		LinkedHashMap<Object, String> h = new LinkedHashMap<Object, String>();
		String selectSQL = theTable.getSelectSQL();
		String sql = selectSQL;
		ResultSet rs = getResultSet(sql, true);
		String value;
		int i;
		Object o = null;
		Object val = null;
		try {
			if (rs != null && rs.first()) {
				MyTable[] foreignFields = theTable.getForeignFields();
				String[] mnTable = theTable.getMNTable();
				if (alreadyUsed == null)
					alreadyUsed = new HashSet<MyTable>();
				alreadyUsed.add(theTable);
				do {
					value = "";
					if (theTable.getFields2ViewInGui() != null) {
						for (String s : theTable.getFields2ViewInGui()) {
							for (i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
								if (rs.getMetaData().getColumnName(i).equals(s)) {
									value += handleField(rs.getObject(i),
											foreignFields, mnTable, i,
											goDeeper, startDelim, delimiter,
											endDelim, true, alreadyUsed);
									break;
								}
							}
						}
					} else {
						for (i = 2; i <= rs.getMetaData().getColumnCount(); i++) { // bei
																					// 2
																					// beginnen,
																					// damit
																					// die
																					// Spalte
																					// ID
																					// nicht
																					// zu
																					// sehen
																					// ist!
							String v = handleField(rs.getObject(i),
									foreignFields, mnTable, i, goDeeper,
									startDelim, delimiter, endDelim, true,
									alreadyUsed);
							if (!v.isEmpty()) {
								String cn = rs.getMetaData().getColumnName(i);
								if (foreignTable.equals("DoubleKennzahlen")
										&& (cn.equals("Exponent") || cn
												.endsWith("_exp"))) {
									if (value.endsWith("\n"))
										value = value.substring(0,
												value.length() - 1)
												+ " * 10^" + v;
									else {
										value += (cn.equals("Exponent") ? "Wert"
												: (cn.endsWith("_exp") ? cn
														.substring(0,
																cn.length() - 4)
														: cn))
												+ ": " + "1 * 10^" + v;
									}
								} else {
									value += cn + ": " + v;
								}
							}
						}
					}
					/*
					 * if (foreignTable.equals("DoubleKennzahlen") &&
					 * value.isEmpty()) { value = "..."; }
					 */
					o = rs.getObject(1);
					val = value;
					if (theTable.getTablename().equals("DoubleKennzahlen")) {
						h.put(new Double((Integer) rs.getObject(1)), value);
					} else {
						h.put(rs.getObject(1), value);
					}
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
			MyLogger.handleMessage(theTable.getTablename() + "\t" + o + "\t"
					+ val + "\t" + selectSQL);
		}
		if (!filledHashtables.containsKey(foreignTable)) {
			filledHashtables.put(foreignTable, h);
		}
		return h;
	}

	public static String getDoubleStr(final Object dbl) {
		if (dbl == null) {
			return null;
		}
		NumberFormat f = NumberFormat.getInstance(Locale.US);
		f.setGroupingUsed(false);
		return f.format(dbl);
	}

	public static boolean kzIsString(final String kennzahl) {
		return kennzahl.equals("Verteilung")
				|| kennzahl.equals("Funktion (Zeit)") || kennzahl.equals("x")
				|| kennzahl.equals("Funktion (x)");
	}

	public static boolean kzIsBoolean(final String kennzahl) {
		return kennzahl.endsWith("_g") || kennzahl.equals("Undefiniert (n.d.)");
	}

	public static Object insertDBL(final String tablename,
			final String fieldname, final Integer tableID, Object kzID,
			String kz, Object value) {
		try {
			if (kzID == null) {
				kzID = DBKernel.getValue(tablename, "ID", tableID + "",
						fieldname);
				if (kzID == null) {
					PreparedStatement psmt = DBKernel
							.getDBConnection()
							.prepareStatement(
									"INSERT INTO "
											+ DBKernel.delimitL("DoubleKennzahlen")
											+ " (" + DBKernel.delimitL("Wert")
											+ ") VALUES (NULL)",
									Statement.RETURN_GENERATED_KEYS);
					if (psmt.executeUpdate() > 0) {
						kzID = DBKernel.getLastInsertedID(psmt);
						DBKernel.sendRequest(
								"UPDATE " + DBKernel.delimitL(tablename)
										+ " SET "
										+ DBKernel.delimitL(fieldname) + "="
										+ kzID + " WHERE "
										+ DBKernel.delimitL("ID") + "="
										+ tableID, false);
					}
				}
			}
			if (kzID == null) {
				System.err.println("eeeeeSHIIETEW...");
			} else { // UPDATE
				if (kz.indexOf("(?)") >= 0) {
					kz = kz.replace("(?)", "(x)");
				}
				if (value == null) {
					value = "NULL";
				}
				if (DBKernel.kzIsString(kz)) {
					DBKernel.sendRequest(
							"UPDATE " + DBKernel.delimitL("DoubleKennzahlen")
									+ " SET " + DBKernel.delimitL(kz) + "='"
									+ value + "'" + " WHERE "
									+ DBKernel.delimitL("ID") + "=" + kzID,
							false);
				} else if (DBKernel.kzIsBoolean(kz)) {
					DBKernel.sendRequest(
							"UPDATE " + DBKernel.delimitL("DoubleKennzahlen")
									+ " SET " + DBKernel.delimitL(kz) + "="
									+ value + "" + " WHERE "
									+ DBKernel.delimitL("ID") + "=" + kzID,
							false);
				} else {
					DBKernel.sendRequest(
							"UPDATE " + DBKernel.delimitL("DoubleKennzahlen")
									+ " SET " + DBKernel.delimitL(kz) + "="
									+ value + " WHERE "
									+ DBKernel.delimitL("ID") + "=" + kzID,
							false);
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return kzID;
	}

	public static ResultSet getResultSet(final String sql,
			final boolean suppressWarnings) {
		ResultSet ergebnis = null;
		try {
			getDBConnection();
			Statement anfrage = localConn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ergebnis = anfrage.executeQuery(sql);
			ergebnis.first();
		} catch (Exception e) {
			if (!suppressWarnings) {
				MyLogger.handleMessage(sql);
				MyLogger.handleException(e);
			}
		}
		return ergebnis;
	}

	public static ResultSet getResultSet(final Connection conn,
			final String sql, final boolean suppressWarnings) {
		if (conn == null) {
			return getResultSet(sql, suppressWarnings);
		} else {
			try {
				return getResultSet(conn.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY), sql, suppressWarnings);
			} catch (SQLException e) {
				// e.printStackTrace();
			}
		}
		return null;
	}

	public static ResultSet getResultSet(final Statement anfrage,
			final String sql, final boolean suppressWarnings) {
		ResultSet ergebnis = null;
		try {
			ergebnis = anfrage.executeQuery(sql);
			ergebnis.first();
		} catch (Exception e) {
			if (!suppressWarnings) {
				MyLogger.handleMessage(sql);
				MyLogger.handleException(e);
			}
		}
		return ergebnis;
	}

	public static boolean sendRequest(final String sql,
			final boolean suppressWarnings) {
		return sendRequest(sql, suppressWarnings, false);
	}

	public static boolean sendRequest(final String sql,
			final boolean suppressWarnings, final boolean fetchAdminInCase) {
		try {
			Connection conn = getDBConnection();
			return sendRequest(conn, sql, suppressWarnings, fetchAdminInCase);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean sendRequest(Connection conn, final String sql,
			final boolean suppressWarnings, final boolean fetchAdminInCase) {
		boolean result = false;
		boolean adminGathered = false;
		try {
			if (conn == null || conn.isClosed())
				conn = getDBConnection();
			if (fetchAdminInCase && !DBKernel.isAdmin()) { // @Todo: eigentlich:
															// isAdmin(conn,
															// conn.getMetaData().getUserName())
				DBKernel.closeDBConnections(false);
				conn = DBKernel.getDefaultAdminConn();
				adminGathered = true;
			}
			Statement anfrage = conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			anfrage.execute(sql);
			result = true;
		} catch (Exception e) {
			if (!suppressWarnings) {
				if (!DBKernel.isKNIME
						|| (!e.getMessage().equals(
								"The table data is read only") && !e
								.getMessage()
								.equals("invalid transaction state: read-only SQL-transaction")))
					MyLogger.handleMessage(sql);
				MyLogger.handleException(e);
			}
		}
		if (adminGathered) {
			DBKernel.closeDBConnections(false);
			try {
				conn = DBKernel.getDBConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static Integer sendRequestGetAffectedRowNumber(Connection conn,
			final String sql, final boolean suppressWarnings,
			final boolean fetchAdminInCase) {
		Integer result = null;
		boolean adminGathered = false;
		try {
			if (fetchAdminInCase && !DBKernel.isAdmin()) {
				DBKernel.closeDBConnections(false);
				conn = DBKernel.getDefaultAdminConn();
				adminGathered = true;
			}
			Statement anfrage = conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			result = anfrage.executeUpdate(sql);
		} catch (Exception e) {
			if (!suppressWarnings) {
				if (!DBKernel.isKNIME
						|| (!e.getMessage().equals(
								"The table data is read only") && !e
								.getMessage()
								.equals("invalid transaction state: read-only SQL-transaction")))
					MyLogger.handleMessage(sql);
				MyLogger.handleException(e);
			}
		}
		if (adminGathered) {
			DBKernel.closeDBConnections(false);
			try {
				DBKernel.getDBConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static String sendRequestGetErr(final String sql) {
		String result = "";
		try {
			getDBConnection();
			Statement anfrage = localConn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			anfrage.execute(sql);
		} catch (Exception e) {
			result = e.getMessage();
			MyLogger.handleException(e);
		}
		return result;
	}

	public static boolean showHierarchic(final String tableName) {
		return tableName.equals("Matrices") || tableName.equals("Methoden")
				|| tableName.equals("Agenzien")
				|| tableName.equals("Methodiken");
	}

	public static int countUsers(boolean adminsOnly) {
		return countUsers(localConn, adminsOnly);
	}

	public static int countUsers(Connection conn, boolean adminsOnly) {
		int result = -1;
		ResultSet rs = getResultSet(conn,
				"SELECT COUNT(*) FROM "
						+ delimitL("Users")
						+ " WHERE "
						+ (adminsOnly ? delimitL("Zugriffsrecht") + " = "
								+ Users.ADMIN + " AND " : "")
						+ delimitL("Username") + " IS NOT NULL", true);
		try {
			if (rs != null && rs.first()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
			result = -1;
		}
		// System.out.println(result);
		return result;
	}

	public static int getRowCount(final String tableName, final String where) {
		Connection conn = null;
		try {
			conn = getDBConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getRowCount(conn, tableName, where);
	}

	public static int getRowCount(Connection conn, final String tableName,
			final String where) {
		int result = 0;
		String sql = "SELECT COUNT(*) FROM "
				+ DBKernel.delimitL(tableName)
				+ (where != null && where.trim().length() > 0 ? " " + where
						: "");
		ResultSet rs = DBKernel.getResultSet(conn, sql, true);
		try {
			if (rs != null && rs.first()) {
				result = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean isAdmin() {
		String un = getUsername();
		return isAdmin(null, un);
	}

	public static boolean isAdmin(Connection conn, String un) { // nur der Admin
																// kann
																// �berhaupt die
																// Users Tabelle
																// abfragen,
																// daher ist ein
																// Wert <> -1
																// ein Zeichen
																// f�r
																// Adminrechte,
																// das kann auch
																// defad sein
		if (conn == null) {
			if (un.equals(getTempSA(HSHDB_PATH))) {
				return true;
			}
		}
		boolean result = false;
		ResultSet rs = getResultSet(conn, "SELECT COUNT(*) FROM "
				+ delimitL("Users") + " WHERE " + delimitL("Zugriffsrecht")
				+ " = " + Users.ADMIN + " AND " + delimitL("Username") + " = '"
				+ un + "'", true);
		try {
			if (rs != null && rs.first()) {
				result = (rs.getInt(1) > (conn == null ? 0 : -1));
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static String getCodesName(final String tablename) {
		int index = tablename.indexOf("_");
		if (index >= 0) {
			return "Codes" + tablename.substring(index);
		} else {
			return "Codes_" + tablename;
		}
	}

	public static boolean DBFilesDa() {
		return DBFilesDa(DBKernel.HSHDB_PATH);
	}

	public static boolean DBFilesDa(String path) {
		boolean result = false;
		File f = new File(path + "DB.script");
		if (!f.exists()) {
			f = new File(path + "DB.data");
		}
		result = f.exists();
		return result;
	}

	public static void doMNs(final MyDBTable table) {
		doMNs(table.getActualTable());
	}

	public static void doMNs(final MyTable table) {
		boolean dl = DBKernel.dontLog;
		boolean dlmk = MainKernel.dontLog;
		DBKernel.dontLog = true;
		MainKernel.dontLog = true;
		Vector<String> listMNs = table.getListMNs();
		if (listMNs != null) {
			String tableName = table.getTablename();
			// hier soll immer die ID drin stehen, die wird dann zur Darstellung
			// der M:N Beziehung ausgelesen.
			// Mach einfach f�r alle Zeilen, dauert ja nicht lange, oder?
			for (int i = 0; i < listMNs.size(); i++) {
				String feldname = listMNs.get(i);
				DBKernel.sendRequest(
						"UPDATE " + DBKernel.delimitL(tableName) + " SET "
								+ DBKernel.delimitL(feldname) + "="
								+ DBKernel.delimitL("ID") + " WHERE "
								+ DBKernel.delimitL(feldname) + " IS NULL OR "
								+ DBKernel.delimitL(feldname) + "!="
								+ DBKernel.delimitL("ID"), false);
			}
		}
		DBKernel.dontLog = dl;
		MainKernel.dontLog = dlmk;
	}

	public static int isDBVeraltet(final Login login) {
		// if (true) return JOptionPane.NO_OPTION;
		int result = JOptionPane.NO_OPTION;

		String dbVersion = getDBVersion();
		MyLogger.handleMessage("DBVersion: " + dbVersion);
		if (dbVersion == null || !dbVersion.equals(DBKernel.DBVersion)) {
			result = askVeraltetDBBackup(login);
		}
		/*
		 * ResultSet rs = getResultSet("SELECT " + delimitL("Katalogcodes") +
		 * " FROM " + delimitL("Methoden"), true); try { ResultSetMetaData rsmd
		 * = rs.getMetaData(); } catch (Exception e) {
		 * MyLogger.handleMessage("DB veraltet... mach ne neue!"); result =
		 * askVeraltetDBBackup(login); }
		 */
		return result;
	}

	private static int askVeraltetDBBackup(final Login login) {
		int result = JOptionPane.YES_OPTION;
		int retVal = JOptionPane
				.showConfirmDialog(
						login,
						"Die Datenbank ist veraltet und muss ersetzt werden.\nSoll zuvor ein Backup der alten Datenbank erstellt werden?",
						"Backup erstellen?", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
		if (retVal == JOptionPane.YES_OPTION) {
			if (!Backup.dbBackup(login)) {
				result = JOptionPane.CANCEL_OPTION;
			}
		} else if (retVal == JOptionPane.NO_OPTION) {
			retVal = JOptionPane
					.showConfirmDialog(
							login,
							"Die Datenbank wirklich ohne Backup �berschreiben?? Sicher?",
							"Sicher?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
			if (retVal == JOptionPane.YES_OPTION) {
				;
			} else {
				return askVeraltetDBBackup(login);
			}
		} else {
			result = JOptionPane.CANCEL_OPTION;
		}
		return result;
	}

	public static long getLastCache(Connection conn, String tablename) {
		long result = 0;
		ResultSet rs = getResultSet(conn, "SELECT " + delimitL("Wert")
				+ " FROM " + delimitL("Infotabelle") + " WHERE "
				+ delimitL("Parameter") + " = 'lastCache_" + tablename + "'",
				true);
		try {
			if (rs != null && rs.first()) {
				String strVal = rs.getString(1);
				result = Long.parseLong(strVal);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		// System.out.println(result);
		return result;
	}

	public static long getLastRelevantChange(Connection conn,
			String[] relevantTables) {
		long result = 0;
		if (relevantTables.length > 0) {
			String where = delimitL("Tabelle") + " = '" + relevantTables[0]
					+ "'";
			for (int i = 1; i < relevantTables.length; i++) {
				where += " OR " + delimitL("Tabelle") + " = '"
						+ relevantTables[i] + "'";
			}
			String sql = "SELECT TOP 1 " + delimitL("Zeitstempel") + " FROM "
					+ delimitL("ChangeLog") + " WHERE " + where + " ORDER BY "
					+ delimitL("Zeitstempel") + " DESC";
			ResultSet rs = getResultSet(conn, sql, true);
			try {
				if (rs != null && rs.first()) {
					result = rs.getTimestamp(1).getTime();
				}
			} catch (Exception e) {
				MyLogger.handleException(e);
			}
		}
		// System.out.println(result);
		return result;
	}

	public static void setLastCache(Connection conn, String tablename,
			long newCacheTime) {
		try {
			boolean ro = conn.isReadOnly();
			if (ro)
				conn.setReadOnly(false);
			if (!sendRequest(conn,
					"INSERT INTO \"Infotabelle\" (\"Parameter\",\"Wert\") VALUES ('lastCache_"
							+ tablename + "','" + newCacheTime + "')", true,
					false)) {
				sendRequest(conn, "UPDATE \"Infotabelle\" SET \"Wert\" = '"
						+ newCacheTime + "' WHERE \"Parameter\" = 'lastCache_"
						+ tablename + "'", false, false);
			}
			if (ro)
				conn.setReadOnly(ro);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getDBVersion() {
		return getDBVersion(null);
	}

	public static String getDBVersion(Connection conn) {
		String result = null;
		ResultSet rs = getResultSet(conn, "SELECT " + delimitL("Wert")
				+ " FROM " + delimitL("Infotabelle") + " WHERE "
				+ delimitL("Parameter") + " = 'DBVersion'", true);
		try {
			if (rs != null && rs.first()) {
				result = rs.getString(1);
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		// System.out.println(result);
		return result;
	}

	public static void setDBVersion(final String dbVersion) {
		setDBVersion(null, dbVersion);
	}

	public static void setDBVersion(Connection conn, final String dbVersion) {
		DBKernel.sendRequest(conn, "UPDATE " + DBKernel.delimitL("Infotabelle")
				+ " SET " + DBKernel.delimitL("Wert") + " = '" + dbVersion
				+ "'" + " WHERE " + DBKernel.delimitL("Parameter")
				+ " = 'DBVersion'", false, false);
	}

	public static long getFileSize(final String filename) {
		File file = new File(filename);
		if (file == null || !file.exists() || !file.isFile()) {
			System.out.println("File doesn\'t exist");
			return -1;
		}
		return file.length();
	}

	/*
	 * public static boolean isDBL(MyTable myT, int column) { boolean result =
	 * false; String[] mnTable = myT.getMNTable(); if (column > 0 && mnTable !=
	 * null && column-1 < mnTable.length && mnTable[column - 1] != null &&
	 * mnTable[column - 1].equals("DBL")) result = true; return result; }
	 */
	public static boolean isNewDBL(final MyTable myT, final int column) {
		boolean result = false;
		MyTable[] myFs = myT.getForeignFields();
		if (column > 0 && myFs != null && column - 1 < myFs.length
				&& myFs[column - 1] != null
				&& myFs[column - 1].getTablename().equals("DoubleKennzahlen")) {
			result = true;
		}
		return result;
	}

	public static void grantDefaults(final String tableName) {
		DBKernel.sendRequest(
				"GRANT SELECT ON TABLE " + DBKernel.delimitL(tableName)
						+ " TO " + DBKernel.delimitL("PUBLIC"), false);
		if (tableName.startsWith("Codes_")) {
			DBKernel.sendRequest(
					"GRANT SELECT ON TABLE " + DBKernel.delimitL(tableName)
							+ " TO " + DBKernel.delimitL("WRITE_ACCESS"), false);
		} else {
			DBKernel.sendRequest(
					"GRANT SELECT, INSERT, UPDATE ON TABLE "
							+ DBKernel.delimitL(tableName) + " TO "
							+ DBKernel.delimitL("WRITE_ACCESS"), false);
		}
		DBKernel.sendRequest(
				"GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE "
						+ DBKernel.delimitL(tableName) + " TO "
						+ DBKernel.delimitL("SUPER_WRITE_ACCESS"), false);
	}

	public static void openDBGUI() {
		final Connection connection = getLocalConn(true);
		try {
			connection.setReadOnly(DBKernel.isReadOnly());
			if (DBKernel.myList != null
					&& DBKernel.myList.getMyDBTable() != null) {
				DBKernel.myList.getMyDBTable().setConnection(connection);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		StartApp.go(connection);
	}

	public static String getInternalDefaultDBPath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toString().replace("/", System.getProperty("file.separator"))
				+ System.getProperty("file.separator")
				+ ".pmmlabDB"
				+ System.getProperty("file.separator");
	}

	private static Connection getInternalKNIMEDB_LoadGui(boolean autoUpdate) {
		Connection result = null;
		try {
			// Create a file object from the URL
			String internalPath = DBKernel.prefs.get(
					"PMM_LAB_SETTINGS_DB_PATH", getInternalDefaultDBPath());
			DBKernel.isServerConnection = DBKernel.isHsqlServer(internalPath);
			if (DBKernel.isServerConnection) {
				HSHDB_PATH = internalPath;
				try {
					// DBKernel.getNewServerConnection(login, pw, filename);
					result = DBKernel.getDBConnection(DBKernel.prefs.get(
							"PMM_LAB_SETTINGS_DB_USERNAME", ""), DBKernel.prefs
							.get("PMM_LAB_SETTINGS_DB_PASSWORD", ""));
					createGui(result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				File incFileInternalDBFolder = new File(internalPath);
				if (!incFileInternalDBFolder.exists()) {
					if (!incFileInternalDBFolder.mkdirs()) {
						System.err
								.println("Creation of folder for internal database not succeeded.");
						return null;// throw new
									// IllegalStateException("Creation of folder for internal database not succeeded.",
									// null);//return null;
					}
				}
				if (incFileInternalDBFolder.list() == null) {
					System.err
							.println("Creation of folderlist for internal database not succeeded.");
					return null;// throw new
								// IllegalStateException("Creation of folderlist for internal database not succeeded.",
								// null);//return null;
				}
				// folder is empty? Create database!
				String[] fl = incFileInternalDBFolder.list();
				boolean folderEmpty = (fl.length == 0);
				if (!folderEmpty) {
					folderEmpty = true;
					for (String f : fl) {
						if (f.startsWith("DB.")) {
							folderEmpty = false;
							break;
						}
					}
				}
				if (folderEmpty) {
					// Get the bundle this class belongs to.
					Bundle bundle = FrameworkUtil.getBundle(DBKernel.class);
					URL incURLfirstDB = bundle
							.getResource("org/hsh/bfr/db/res/firstDB.tar.gz");
					if (incURLfirstDB == null) { // incURLInternalDBFolder ==
													// null ||
						return null;
					}
					File incFilefirstDB = new File(FileLocator.toFileURL(
							incURLfirstDB).getPath());
					try {
						org.hsqldb.lib.tar.DbBackupMain.main(new String[] {
								"--extract", incFilefirstDB.getAbsolutePath(),
								incFileInternalDBFolder.getAbsolutePath() });
						JOptionPane pane = new JOptionPane(
								"Internal database created in folder '"
										+ incFileInternalDBFolder.getAbsolutePath()
										+ "'", JOptionPane.INFORMATION_MESSAGE);
						JDialog dialog = pane
								.createDialog("Internal database created");
						dialog.setAlwaysOnTop(true);
						dialog.setVisible(true);
					} catch (Exception e) {
						throw new IllegalStateException(
								"Creation of internal database not succeeded.",
								e);
					}
				}

				try {
					HSHDB_PATH = internalPath;
					String un = DBKernel.prefs.get(
							"PMM_LAB_SETTINGS_DB_USERNAME", null);
					String pw = DBKernel.prefs.get(
							"PMM_LAB_SETTINGS_DB_PASSWORD", null);
					result = getDBConnection(un != null ? un
							: getTempSA(HSHDB_PATH), pw != null ? pw
							: getTempSAPass(HSHDB_PATH));

					createGui(result);
					if (autoUpdate) {
						checkUpdate();
					} else {
						Thread queryThread = new Thread() {
							public void run() {
								try {
									checkUpdate();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						};
						queryThread.start();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// DBKernel.saveUP2PrefsTEMP(HSHDB_PATH);
			DBKernel.getTempSA(HSHDB_PATH);
		} catch (IOException e) {
			throw new IllegalStateException(
					"Cannot locate necessary internal database path.", e);
		}
		return result;
	}

	private static void checkUpdate() throws Exception {
		// UpdateChecker
		String dbVersion = DBKernel.getDBVersion();
		if (!DBKernel.isServerConnection
				&& (dbVersion == null || !dbVersion.equals(DBKernel.DBVersion))) {
			boolean dl = MainKernel.dontLog;
			MainKernel.dontLog = true;
			boolean isAdmin = DBKernel.isAdmin();
			if (!isAdmin) {
				DBKernel.closeDBConnections(false);
				DBKernel.getDefaultAdminConn();
			}

			if (DBKernel.getDBVersion() == null
					|| DBKernel.getDBVersion().equals("1.4.3")) {
				UpdateChecker.check4Updates_143_144();
				DBKernel.setDBVersion("1.4.4");
			}
			if (DBKernel.getDBVersion().equals("1.4.4")) {
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
			if (DBKernel.getDBVersion().equals("1.6.8")) {
				UpdateChecker.check4Updates_168_169();
				DBKernel.setDBVersion("1.6.9");
			}
			if (DBKernel.getDBVersion().equals("1.6.9")) {
				UpdateChecker.check4Updates_169_170();
				DBKernel.setDBVersion("1.7.0");
			}
			if (DBKernel.getDBVersion().equals("1.7.0")) {
				UpdateChecker.check4Updates_170_171();
				DBKernel.setDBVersion("1.7.1");
			}
			if (DBKernel.getDBVersion().equals("1.7.1")) {
				UpdateChecker.check4Updates_171_172();
				DBKernel.setDBVersion("1.7.2");
			}
			if (DBKernel.getDBVersion().equals("1.7.2")) {
				UpdateChecker.check4Updates_172_173();
				DBKernel.setDBVersion("1.7.3");
			}
			if (DBKernel.getDBVersion().equals("1.7.3")) {
				UpdateChecker.check4Updates_173_174();
				DBKernel.setDBVersion("1.7.4");
			}
			if (DBKernel.getDBVersion().equals("1.7.4")) {
				UpdateChecker.check4Updates_174_175();
				DBKernel.setDBVersion("1.7.5");
			}
			if (DBKernel.getDBVersion().equals("1.7.5")) {
				UpdateChecker.check4Updates_175_176();
				DBKernel.setDBVersion("1.7.6");
			}
			if (DBKernel.getDBVersion().equals("1.7.6")) {
				UpdateChecker.check4Updates_176_177();
				DBKernel.setDBVersion("1.7.7");
			}
			if (DBKernel.getDBVersion().equals("1.7.7")) {
				UpdateChecker.check4Updates_177_178();
				DBKernel.setDBVersion("1.7.8");
			}
			if (DBKernel.getDBVersion().equals("1.7.8")) {
				UpdateChecker.check4Updates_178_179();
				DBKernel.setDBVersion("1.7.9");
			}
			DBKernel.sendRequest("DROP TABLE " + DBKernel.delimitL("CACHE_TS")
					+ " IF EXISTS", false, true);
			DBKernel.sendRequest(
					"DROP TABLE " + DBKernel.delimitL("CACHE_selectEstModel")
							+ " IF EXISTS", false, true);
			DBKernel.sendRequest(
					"DROP TABLE " + DBKernel.delimitL("CACHE_selectEstModel1")
							+ " IF EXISTS", false, true);
			DBKernel.sendRequest(
					"DROP TABLE " + DBKernel.delimitL("CACHE_selectEstModel2")
							+ " IF EXISTS", false, true);

			if (!isAdmin) {
				DBKernel.closeDBConnections(false);
				DBKernel.getDBConnection();
				if (DBKernel.myList != null
						&& DBKernel.myList.getMyDBTable() != null) {
					DBKernel.myList.getMyDBTable().setConnection(
							DBKernel.getDBConnection());
				}
			}
			MainKernel.dontLog = dl;

		}
	}

	public static void createGui(Connection conn) {
		// MyDBTables.loadMyTables();
		DBKernel.myDBi = new MyDBTablesNew();
		try {
			if (DBKernel.myList == null && conn != null) {
				// Login login = new Login();
				MyDBTable myDB = new MyDBTable();
				myDB.initConn(conn);
				MyDBTree myDBTree = new MyDBTree();
				MyList myList = new MyList(myDB, myDBTree);
				DBKernel.myList = myList;
				if (DBKernel.myList != null
						&& DBKernel.myList.getMyDBTable() != null) {
					if (myDB.getConnection() == null
							|| myDB.getConnection().isClosed()) {
						DBKernel.myList.getMyDBTable().setConnection(
								DBKernel.getDBConnection());
					}
				}
				myList.addAllTables();
				// login.loadMyTables(myList, null);

				MainFrame mf = new MainFrame(myList);
				DBKernel.mainFrame = mf;
				myList.setSelection(DBKernel.prefs.get("LAST_SELECTED_TABLE",
						"Versuchsbedingungen"));
				try {
					boolean full = Boolean.parseBoolean(DBKernel.prefs.get(
							"LAST_MainFrame_FULL", "FALSE"));

					int w = Integer.parseInt(DBKernel.prefs.get(
							"LAST_MainFrame_WIDTH", "1020"));
					int h = Integer.parseInt(DBKernel.prefs.get(
							"LAST_MainFrame_HEIGHT", "700"));
					int x = Integer.parseInt(DBKernel.prefs.get(
							"LAST_MainFrame_X", "0"));
					int y = Integer.parseInt(DBKernel.prefs.get(
							"LAST_MainFrame_Y", "0"));
					DBKernel.mainFrame.setPreferredSize(new Dimension(w, h));
					DBKernel.mainFrame.setBounds(x, y, w, h);

					DBKernel.mainFrame.pack();
					DBKernel.mainFrame.setLocationRelativeTo(null);
					if (full)
						DBKernel.mainFrame
								.setExtendedState(JFrame.MAXIMIZED_BOTH);
				} catch (Exception e) {
				}
			}
		} catch (Exception he) {
		} // HeadlessException
	}

	public static String[] getItemListMisc(Connection conn) {
		HashSet<String> hs = new HashSet<String>();
		try {
			ResultSet rs = null;
			String sql = "SELECT " + DBKernel.delimitL("Parameter") + " FROM "
					+ DBKernel.delimitL("SonstigeParameter");
			rs = DBKernel.getResultSet(conn, sql, false);
			do {
				if (rs.getObject("Parameter") != null)
					hs.add(rs.getString("Parameter"));
			} while (rs.next());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hs.toArray(new String[] {});
	}

	public static boolean mergeIDs(Connection conn, final String tableName,
			int oldID, int newID) {
		ResultSet rs = null;
		String sql = "SELECT FKTABLE_NAME, FKCOLUMN_NAME FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE "
				+ " WHERE PKTABLE_NAME = '" + tableName + "'";
		try {
			rs = DBKernel.getResultSet(conn, sql, false);
			if (rs != null && rs.first()) {
				do {
					String fkt = rs.getObject("FKTABLE_NAME") != null ? rs
							.getString("FKTABLE_NAME") : "";
					String fkc = rs.getObject("FKCOLUMN_NAME") != null ? rs
							.getString("FKCOLUMN_NAME") : "";
					// System.err.println(tableName + " wird in " + fkt + "->" +
					// fkc + " referenziert");
					if (!DBKernel.sendRequest(conn,
							"UPDATE " + DBKernel.delimitL(fkt) + " SET "
									+ DBKernel.delimitL(fkc) + "=" + newID
									+ " WHERE " + DBKernel.delimitL(fkc) + "="
									+ oldID, false, false))
						return false;
				} while (rs.next());
				if (DBKernel.sendRequest(conn,
						"DELETE FROM " + DBKernel.delimitL(tableName)
								+ " WHERE " + DBKernel.delimitL("ID") + "="
								+ oldID, false, false)) {
					return true;
				}
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return false;
	}

	public static int getUsagecountOfID(final String tableName, int id) {
		int result = 0;
		ResultSet rs = DBKernel
				.getResultSet(
						"SELECT FKTABLE_NAME, FKCOLUMN_NAME FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE "
								+ " WHERE PKTABLE_NAME = '" + tableName + "'",
						false);
		try {
			if (rs != null && rs.first()) {
				do {
					String fkt = rs.getObject("FKTABLE_NAME") != null ? rs
							.getString("FKTABLE_NAME") : "";
					String fkc = rs.getObject("FKCOLUMN_NAME") != null ? rs
							.getString("FKCOLUMN_NAME") : "";
					// System.err.println(tableName + " wird in " + fkt + "->" +
					// fkc + " referenziert");
					ResultSet rs2 = DBKernel.getResultSet(
							"SELECT " + DBKernel.delimitL("ID") + " FROM "
									+ DBKernel.delimitL(fkt) + " WHERE "
									+ DBKernel.delimitL(fkc) + "=" + id, false);
					if (rs2 != null && rs2.last()) {
						result += rs2.getRow();
					}
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static List<String> getUsageListOfID(final String tableName, int id) {
		List<String> result = new ArrayList<String>();
		ResultSet rs = DBKernel
				.getResultSet(
						"SELECT FKTABLE_NAME, FKCOLUMN_NAME FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE "
								+ " WHERE PKTABLE_NAME = '" + tableName + "'",
						false);
		try {
			if (rs != null && rs.first()) {
				do {
					String fkt = rs.getObject("FKTABLE_NAME") != null ? rs
							.getString("FKTABLE_NAME") : "";
					String fkc = rs.getObject("FKCOLUMN_NAME") != null ? rs
							.getString("FKCOLUMN_NAME") : "";
					// System.err.println(tableName + " wird in " + fkt + "->" +
					// fkc + " referenziert");
					ResultSet rs2 = DBKernel.getResultSet(
							"SELECT " + DBKernel.delimitL("ID") + " FROM "
									+ DBKernel.delimitL(fkt) + " WHERE "
									+ DBKernel.delimitL(fkc) + "=" + id, false);
					if (rs2 != null && rs2.first()) {
						do {
							result.add(fkt + ": " + rs2.getInt("ID"));
						} while (rs.next());
					}
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
		return result;
	}

	public static File getCopyOfInternalDB() {
		File temp = null;
		try {
			temp = File.createTempFile("firstDB", ".tar.gz");
			InputStream in = DBKernel.class
					.getResourceAsStream("/org/hsh/bfr/db/res/firstDB.tar.gz");
			BufferedInputStream bufIn = new BufferedInputStream(in);
			BufferedOutputStream bufOut = null;
			try {
				bufOut = new BufferedOutputStream(new FileOutputStream(temp));
			} catch (FileNotFoundException e1) {
				MyLogger.handleException(e1);
			}

			byte[] inByte = new byte[4096];
			int count = -1;
			try {
				while ((count = bufIn.read(inByte)) != -1) {
					bufOut.write(inByte, 0, count);
				}
			} catch (IOException e) {
				MyLogger.handleException(e);
			}

			try {
				bufOut.close();
			} catch (IOException e) {
				MyLogger.handleException(e);
			}
			try {
				bufIn.close();
			} catch (IOException e) {
				MyLogger.handleException(e);
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		return temp;
	}

	public static Integer openPrimModelDBWindow(Component parent, Integer id) {
		MyTable myT = DBKernel.myList.getTable("Modellkatalog");
		MyStringFilter mf = new MyStringFilter(myT, "Level", "1");
		Object newVal = DBKernel.myList.openNewWindow(myT, id, "Modellkatalog",
				null, null, null, null, true, mf, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openSecModelDBWindow(Component parent, Integer id) {
		MyTable myT = DBKernel.myList.getTable("Modellkatalog");
		MyStringFilter mf = new MyStringFilter(myT, "Level", "2");
		Object newVal = DBKernel.myList.openNewWindow(myT, id, "Modellkatalog",
				null, null, null, null, true, mf, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openMiscDBWindow(Component parent, Integer id) {
		MyTable myT = myList.getTable("SonstigeParameter");
		Object newVal = myList.openNewWindow(myT, id, "SonstigeParameter",
				null, null, null, null, true, null, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openAgentDBWindow(Component parent, Integer id) {
		MyTable myT = myList.getTable("Agenzien");
		Object newVal = myList.openNewWindow(myT, id, "Agenzien", null, null,
				null, null, true, null, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openMatrixDBWindow(Component parent, Integer id) {
		MyTable myT = myList.getTable("Matrices");
		Object newVal = myList.openNewWindow(myT, id, "Matrices", null, null,
				null, null, true, null, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static Integer openLiteratureDBWindow(Component parent, Integer id) {
		MyTable myT = myList.getTable("Literatur");
		Object newVal = myList.openNewWindow(myT, id, "Literatur", null, null,
				null, null, true, null, parent);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	public static String getLocalDBUUID() {
		try {
			return getDBUUID(getLocalConn(true), true);
		} catch (SQLException e) {
			return null;
		}
	}

	public static String getDBUUID(Connection conn, boolean tryOnceAgain)
			throws SQLException {
		String result = null;
		ResultSet rs = getResultSet(
				conn,
				"SELECT \"Wert\" FROM \"Infotabelle\" WHERE \"Parameter\" = 'DBuuid'",
				false);
		if (rs != null && rs.next()) {
			result = rs.getString(1);
		}
		if (tryOnceAgain && result == null) {
			setDBUUID(conn, UUID.randomUUID().toString());
			result = getDBUUID(conn, false);
		}
		return result;
	}

	public static boolean isReadOnly() {
		return DBKernel.isKNIME
				&& DBKernel.prefs.getBoolean("PMM_LAB_SETTINGS_DB_RO", true)
				|| !DBKernel.isKNIME
				&& DBKernel.prefs.getBoolean("DB_READONLY", true);
	}

	private static void setDBUUID(Connection conn, final String uuid)
			throws SQLException {
		conn.setReadOnly(false);
		sendRequest(conn,
				"INSERT INTO \"Infotabelle\" (\"Parameter\",\"Wert\") VALUES ('DBuuid','"
						+ uuid + "')", true, false);
		conn.setReadOnly(DBKernel.isReadOnly());
	}

	public static void getKnownIDs4PMM(Connection conn,
			HashMap<Integer, Integer> foreignDbIds, String tablename,
			String rowuuid) {
		String sql = "SELECT " + DBKernel.delimitL("TableID") + ","
				+ DBKernel.delimitL("SourceID") + " FROM "
				+ DBKernel.delimitL("DataSource") + " WHERE ";
		sql += DBKernel.delimitL("Table") + "=" + "'" + tablename + "' AND";
		sql += DBKernel.delimitL("SourceDBUUID") + "=" + "'" + rowuuid + "';";

		ResultSet rs = DBKernel.getResultSet(conn, sql, true);
		try {
			if (rs != null && rs.first()) {
				do {
					if (rs.getObject("SourceID") != null
							&& rs.getObject("TableID") != null) {
						foreignDbIds.put(rs.getInt("SourceID"),
								rs.getInt("TableID"));
					}
				} while (rs.next());
			}
		} catch (Exception e) {
			MyLogger.handleException(e);
		}
	}

	public static void setKnownIDs4PMM(Connection conn,
			HashMap<Integer, Integer> foreignDbIds, String tablename,
			String rowuuid) {
		for (Integer sID : foreignDbIds.keySet()) {
			Object id = DBKernel.getValue(conn, "DataSource", new String[] {
					"Table", "SourceDBUUID", "SourceID" }, new String[] {
					tablename, rowuuid, sID + "" }, "TableID");
			if (id == null) {
				String sql = "INSERT INTO " + DBKernel.delimitL("DataSource")
						+ " (" + DBKernel.delimitL("Table") + ","
						+ DBKernel.delimitL("TableID") + ","
						+ DBKernel.delimitL("SourceDBUUID") + ","
						+ DBKernel.delimitL("SourceID") + ") VALUES ('"
						+ tablename + "'," + foreignDbIds.get(sID) + ",'"
						+ rowuuid + "'," + sID + ");";
				DBKernel.sendRequest(conn, sql, false, false);
			}
		}
	}
}
