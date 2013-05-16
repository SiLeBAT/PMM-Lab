package org.hsh.bfr.db;

import java.util.LinkedHashMap;

import org.hsh.bfr.db.gui.MyList;

public class MyDBTables {

	private static LinkedHashMap<String, MyTable> myTables = new LinkedHashMap<String, MyTable>();
	private static LinkedHashMap<Object, String> hashZeit = new LinkedHashMap<Object, String>();
	private static LinkedHashMap<Object, String> hashGeld = new LinkedHashMap<Object, String>();
	private static LinkedHashMap<Object, String> hashGewicht = new LinkedHashMap<Object, String>();
	private static LinkedHashMap<Object, String> hashSpeed = new LinkedHashMap<Object, String>();
	private static LinkedHashMap<Object, String> hashDosis = new LinkedHashMap<Object, String>();
	private static LinkedHashMap<Object, String> hashFreigabe = new LinkedHashMap<Object, String>();

	public static LinkedHashMap<String, MyTable> getAllTables() {
		return myTables;
	}
	public static MyTable getTable(final String tableName) {
		if (myTables.containsKey(tableName)) return myTables.get(tableName);
		else return null;
	}
	public static void recreateTriggers() {
		for(String key : myTables.keySet()) {
				String tableName = myTables.get(key).getTablename();
				DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_" + tableName + "_U"), false);
				DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_" + tableName + "_D"), false);
				DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_" + tableName + "_I"), false);
				DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("A_" + tableName + "_U"), false);
				DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("A_" + tableName + "_D"), false);
				DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("A_" + tableName + "_I"), false);
				if (!tableName.equals("ChangeLog") && !tableName.equals("DateiSpeicher") && !tableName.equals("Infotabelle")) {
					DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("A_" + tableName + "_D") + " AFTER DELETE ON " +
							DBKernel.delimitL(tableName) + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false); // (oneThread ? "QUEUE 0" : "") +    
					DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("A_" + tableName + "_I") + " AFTER INSERT ON " +
							DBKernel.delimitL(tableName) + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false); // (oneThread ? "QUEUE 0" : "") +
					DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("A_" + tableName + "_U") + " AFTER UPDATE ON " +
							DBKernel.delimitL(tableName) + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false); // (oneThread ? "QUEUE 0" : "") +
				}
		}
		DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_USERS_U"), false);
		DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_USERS_D"), false);
		DBKernel.sendRequest("DROP TRIGGER " + DBKernel.delimitL("B_USERS_I"), false);
		DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("B_Users_I") + " BEFORE INSERT ON " +
	        		DBKernel.delimitL("Users") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false);    	
	        // Zur �berwachung, damit immer mindestens ein Admin �brig bleibt; dasselbe gibts im MyDataChangeListener f�r Delete Operations!
	        // Au�erdem zur �berwachung, da� der eingeloggte User seine Kennung nicht �ndert
		DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("B_Users_U") + " BEFORE UPDATE ON " +
	        		DBKernel.delimitL("Users") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false);   
	        // Zur �berwachung, damit eine importierte xml Datei nicht gel�scht werden kann!
		DBKernel.sendRequest("CREATE TRIGGER " + DBKernel.delimitL("B_ProzessWorkflow_U") + " BEFORE UPDATE ON " +
	        		DBKernel.delimitL("ProzessWorkflow") + " FOR EACH ROW " + " CALL " + DBKernel.delimitL(new MyTrigger().getClass().getName()), false);    	
	}
	@SuppressWarnings("unchecked")
	public static void loadMyTables() {
		fillHashes();

		DBKernel.changeLog = new MyTable("ChangeLog",
				new String[]{"Zeitstempel","Username","Tabelle","TabellenID","Alteintrag"},
				new String[]{"DATETIME","VARCHAR(60)","VARCHAR(100)","INTEGER","OTHER"},
				new String[]{null,null,null,null,null},
				new MyTable[]{null,null,null,null,null});
		addTable(DBKernel.changeLog, MyList.SystemTabellen_LIST);
		DBKernel.blobSpeicher = new MyTable("DateiSpeicher",
				new String[]{"Zeitstempel","Tabelle","TabellenID","Feld","Dateiname","Dateigroesse","Datei"},
				new String[]{"DATETIME","VARCHAR(100)","INTEGER","VARCHAR(100)","VARCHAR(255)","INTEGER","BLOB(10M)"},
				new String[]{null,null,null,null,null,null,null},
				new MyTable[]{null,null,null,null,null,null,null});
		addTable(DBKernel.blobSpeicher, MyList.SystemTabellen_LIST);
		DBKernel.users = new MyTable("Users",
				new String[]{"Username","Vorname","Name","Zugriffsrecht"},
				new String[]{"VARCHAR(60)","VARCHAR(30)","VARCHAR(30)","INTEGER"},
				new String[]{null,null,null,null},
				new MyTable[]{null,null,null,null},
				new String[][]{{"Username"}},
				new LinkedHashMap[]{null,null,null,Users.getUserTypesHash()});
		addTable(DBKernel.users, MyList.SystemTabellen_LIST); // m�sste jetzt doch gehen, oder?...  lieber die Users ganz weg, weil das Editieren auf dem HSQLServer nicht korrekt funktioniert - siehe im Trigger removeAccRight usw., da m�sste man erst die sendRequests umstellen auf defaultconnection...		

		MyTable infoTable = new MyTable("Infotabelle",
				new String[]{"Parameter","Wert"},
				new String[]{"VARCHAR(255)","VARCHAR(255)"},
				new String[]{null, null},
				new MyTable[]{null,null},
				new String[][]{{"Parameter"}},
				new LinkedHashMap[]{null,null});
		addTable(infoTable, -1);

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
		addTable(literatur, DBKernel.isKrise ? -1 : (DBKernel.isKNIME ? MyList.BasisTabellen_LIST : 66));

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
		addTable(newDoubleTable, -1);

		// Katalogtabellen
		MyTable matrix = new MyTable("Matrices", new String[]{"Matrixname","Leitsatznummer","pH","aw","Dichte","Katalogcodes"},
				new String[]{"VARCHAR(255)","VARCHAR(20)","DOUBLE","DOUBLE","DOUBLE","INTEGER"},
				new String[]{"Kulturmedium / Futtermittel / Lebensmittel / Serum / Kot / Gewebe","Leitsatznummer - falls bekannt","pH-Wert �ber alle Produkte der Warengruppe - falls absch�tzbar","aw-Wert �ber alle Produkte der Warengruppe - falls absch�tzbar","Dichte der Matrix �ber alle Produkte der Warengruppe - falls absch�tzbar","Matrixkatalog - Codes"},
				new MyTable[]{null,null,newDoubleTable,newDoubleTable,newDoubleTable,null},
				null,
				null,
				new String[]{null,null,null,null,null,"INT"});
		addTable(matrix, MyList.BasisTabellen_LIST);
		
		MyTable toxinUrsprung = new MyTable("ToxinUrsprung", new String[]{"Ursprung"},
				new String[]{"VARCHAR(255)"},
				new String[]{null},
				new MyTable[]{null});
		addTable(toxinUrsprung, -1);
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
		addTable(agenzien, MyList.BasisTabellen_LIST);
		MyTable normen = new MyTable("Methodennormen", new String[]{"Name","Beschreibung"},
				new String[]{"VARCHAR(255)","VARCHAR(1023)"},
				new String[]{"Name der Norm","Beschreibung der Norm"},
				new MyTable[]{null,null});
		addTable(normen, -1);
		MyTable methoden = new MyTable("Methoden", new String[]{"Name","Beschreibung","Referenz","Norm","Katalogcodes"},
				new String[]{"VARCHAR(1023)","VARCHAR(1023)","INTEGER","INTEGER","INTEGER"},
				new String[]{"Name des Nachweisverfahrens","Beschreibung des Nachweisverfahrens","Verweis auf Literaturstelle","Zugeh�rige Normen, z.B. ISO, DIN, CEN, etc.","Methodenkatalog - Codes"}, // ,"Angabe, ob Testreagenzien auch inhouse produziert werden k�nnen"
				new MyTable[]{null,null,literatur,normen,null},
				null,
				null,
				new String[]{null,null,null,"Methoden_Normen","INT"});
		addTable(methoden, DBKernel.getUsername().equals("buschulte") ? 66 : -1);
		MyTable methoden_Normen = new MyTable("Methoden_Normen",
				new String[]{"Methoden","Normen","Norm_Nummer"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{methoden,normen,null},
				new LinkedHashMap[]{null,null,null});
		addTable(methoden_Normen, -1);
		
		MyTable matrix_OG = new MyTable("Codes_Matrices", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(20)","INTEGER"},
				new String[]{"Codebezeichnung, sofern vorhanden (z.B. TOP, GS1, BLS, ADV oder auch selfmade)","Hierarchischer Code","Zugeh�rige Matrix"},
				new MyTable[]{null,null,matrix},
				new String[][]{{"CodeSystem","Code"}});
		addTable(matrix_OG, -1); // -1
		matrix.setForeignField(matrix_OG, 5);
		MyTable agenzienkategorie = new MyTable("Codes_Agenzien", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(20)","INTEGER"},
				new String[]{"Codebezeichnung, sofern vorhanden (z.B. TOP, ADV oder auch selfmade)","Hierarchischer Code","Zugeh�riges Agens"},
				new MyTable[]{null,null,agenzien},
				new String[][]{{"CodeSystem","Code"}});
		addTable(agenzienkategorie, -1);
		agenzien.setForeignField(agenzienkategorie, 15);
		MyTable methoden_OG = new MyTable("Codes_Methoden", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(40)","INTEGER"},
				new String[]{"Codebezeichnung, sofern vorhanden (z.B. TOP, BLV oder auch selfmade)","Hierarchischer Code","Zugeh�rige Methode"},
				new MyTable[]{null,null,methoden},
				new String[][]{{"CodeSystem","Code"}});
		addTable(methoden_OG, -1); // -1
		methoden.setForeignField(methoden_OG, 4);

		MyTable ComBaseImport = new MyTable("ComBaseImport", new String[]{"Referenz","Agensname","Agenskatalog","b_f","Matrixname","Matrixkatalog"},
				new String[]{"INTEGER","VARCHAR(255)","INTEGER","VARCHAR(255)","VARCHAR(255)","INTEGER"},
				new String[]{null,null,null,null,null,null},
				new MyTable[]{literatur,null,agenzien,null,null,matrix});
		addTable(ComBaseImport, DBKernel.isKNIME ? MyList.BasisTabellen_LIST : -1); // 66
		MyTable adressen = new MyTable("Kontakte",
				new String[]{"Name","Strasse","Hausnummer","Postfach","PLZ","Ort","Bundesland","Land","Ansprechpartner","Telefon","Fax","EMail","Webseite"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(10)","VARCHAR(20)","VARCHAR(10)","VARCHAR(60)","VARCHAR(30)","VARCHAR(100)","VARCHAR(100)","VARCHAR(30)","VARCHAR(30)","VARCHAR(100)","VARCHAR(255)"},
				new String[]{"Name der Firma / Labor / Einrichtung", null,null,null,null,null,null,null,"Ansprechpartner inkl. Vor und Zunahme",null,null,null,null},
				new MyTable[]{null,null,null,null,null,null,null,null,null,null,null,null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,DBKernel.hashBundesland,null,null,null,null,null,null});
		addTable(adressen, DBKernel.isKrise ? MyList.Lieferketten_LIST : (DBKernel.isKNIME ? -1 : MyList.BasisTabellen_LIST));
		
		MyTable symptome = new MyTable("Symptome", new String[]{"Bezeichnung","Beschreibung","Bezeichnung_engl","Beschreibung_engl"},
				new String[]{"VARCHAR(50)","VARCHAR(255)","VARCHAR(50)","VARCHAR(255)"},
				new String[]{"Kurzform auf deutsch","Ausf�hrliche Beschreibung auf deutsch","Kurzform auf englisch","Ausf�hrliche Beschreibung auf englisch"},
				new MyTable[]{null,null,null,null});
		addTable(symptome, -1);

		MyTable risikogruppen = new MyTable("Risikogruppen", new String[]{"Bezeichnung","Beschreibung"},
				new String[]{"VARCHAR(50)","VARCHAR(255)"},
				new String[]{"Kurzform","Ausf�hrliche Beschreibung"},
				new MyTable[]{null,null});
		addTable(risikogruppen, -1);

		MyTable tierkrankheiten = new MyTable("Tierkrankheiten", new String[]{"VET_Code","Kurzbezeichnung","Krankheitsart"},
				new String[]{"VARCHAR(255)","VARCHAR(50)","VARCHAR(255)"},
				new String[]{null,"Kurzform","Ausf�hrliche Beschreibung"},
				new MyTable[]{null,null,null});
		addTable(tierkrankheiten, -1);
		
		MyTable krankheiten = generateICD10Tabellen();
		
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
		addTable(diagnostik, MyList.Krankheitsbilder_LIST);
		MyTable krankheitsbildersymptome = new MyTable("Krankheitsbilder_Symptome",
				new String[]{"Krankheitsbilder","Symptome"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{diagnostik,symptome},
				new LinkedHashMap[]{null,null});
		addTable(krankheitsbildersymptome, -1);
		MyTable krankheitsbilderrisikogruppen = new MyTable("Krankheitsbilder_Risikogruppen",
				new String[]{"Krankheitsbilder","Risikogruppen"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{diagnostik,risikogruppen},
				new LinkedHashMap[]{null,null});
		addTable(krankheitsbilderrisikogruppen, -1);
		MyTable agensmatrices = new MyTable("Agenzien_Matrices", // ,"nat�rliches Vorkommen in Lebensmitteln in D"
				new String[]{"Agens","Matrix","Referenz"},
				new String[]{"INTEGER","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{agenzien,matrix,literatur},
				new LinkedHashMap[]{null,null,null});
		addTable(agensmatrices, MyList.Krankheitsbilder_LIST);
		
		MyTable zertifikate = new MyTable("Zertifizierungssysteme", new String[]{"Bezeichnung","Abkuerzung","Anbieter"},
				new String[]{"VARCHAR(255)","VARCHAR(20)","INTEGER"},
				new String[]{"Vollst�ndiger Name zum Zertifizierungssystem","Abk�rzung f�r Zertifizierungssystem","Anbieter des Zertifizierungssystems - Verweis auf die Kontakttabelle"},
				new MyTable[]{null,null,adressen});
		addTable(zertifikate, DBKernel.isKNIME ? -1 : MyList.BasisTabellen_LIST);
		
		MyTable methodiken = new MyTable("Methodiken", new String[]{"Name","Beschreibung","Kurzbezeichnung","WissenschaftlicheBezeichnung","Katalogcodes"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(30)","VARCHAR(255)","INTEGER"},
				new String[]{"Name der Methodik","Beschreibung der Methodik",null,null,"Methodenkatalog - Codes"},
				new MyTable[]{null,null,null,null,null},
				null,
				null,
				new String[]{null,null,null,null,"INT"});
		addTable(methodiken, -1);
		MyTable methodiken_OG = new MyTable("Codes_Methodiken", new String[]{"CodeSystem","Code","Basis"},
				new String[]{"VARCHAR(20)","VARCHAR(20)","INTEGER"},
				new String[]{"Codebezeichnung","Hierarchischer Code","Zugeh�rige Methode"},
				new MyTable[]{null,null,methodiken},
				new String[][]{{"CodeSystem","Code"}});
		addTable(methodiken_OG, -1); // -1
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
		addTable(labore, DBKernel.isKNIME ? -1 : MyList.BasisTabellen_LIST);
		MyTable labore_Methodiken = new MyTable("Labore_Methodiken",
				new String[]{"Labore","Methodiken"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{labore,methodiken},
				new LinkedHashMap[]{null,null});
		addTable(labore_Methodiken, -1);
		MyTable labore_Matrices = new MyTable("Labore_Matrices",
				new String[]{"Labore","Matrices"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{labore,matrix},
				new LinkedHashMap[]{null,null});
		addTable(labore_Matrices, -1);
		MyTable labore_Agenzien = new MyTable("Labore_Agenzien",
				new String[]{"Labore","Agenzien","Methodiken"},
				new String[]{"INTEGER","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{labore,agenzien,methodiken},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,"Labore_Agenzien_Methodiken"});
		addTable(labore_Agenzien, -1);
		MyTable labore_Agenzien_Methodiken = new MyTable("Labore_Agenzien_Methodiken",
				new String[]{"Labore_Agenzien","Methodiken"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{labore_Agenzien,methodiken},
				new LinkedHashMap[]{null,null});
		addTable(labore_Agenzien_Methodiken, -1);

		
		MyTable Konzentrationseinheiten = new MyTable("Einheiten", new String[]{"Einheit","Beschreibung"},
				new String[]{"VARCHAR(255)","VARCHAR(255)"},
				new String[]{null,null},
				new MyTable[]{null,null},
				null,
				new LinkedHashMap[]{null,null},
				new String[]{null,null});
		addTable(Konzentrationseinheiten, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);
		MyTable SonstigeParameter = new MyTable("SonstigeParameter", new String[]{"Parameter","Beschreibung","Kategorie"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(255)"},
				new String[]{null,null,null},
				new MyTable[]{null,null,null},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null});
		addTable(SonstigeParameter, -1);
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
		addTable(kits, MyList.Nachweissysteme_LIST);

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
		addTable(aufbereitungsverfahren, MyList.Nachweissysteme_LIST);
		MyTable aufbereitungsverfahren_Kits = new MyTable("Aufbereitungsverfahren_Kits",
				new String[]{"Aufbereitungsverfahren","Kits"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{aufbereitungsverfahren,kits},
				new LinkedHashMap[]{null,null});
		addTable(aufbereitungsverfahren_Kits, -1);
		MyTable aufbereitungsverfahren_Normen = new MyTable("Aufbereitungsverfahren_Normen",
				new String[]{"Aufbereitungsverfahren","Normen","Norm_Nummer"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{aufbereitungsverfahren,normen,null},
				new LinkedHashMap[]{null,null,null});
		addTable(aufbereitungsverfahren_Normen, -1);

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
		addTable(nachweisverfahren, MyList.Nachweissysteme_LIST);
		MyTable nachweisverfahren_Kits = new MyTable("Nachweisverfahren_Kits",
				new String[]{"Nachweisverfahren","Kits"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{nachweisverfahren,kits},
				new LinkedHashMap[]{null,null});
		addTable(nachweisverfahren_Kits, -1);
		MyTable nachweisverfahren_Normen = new MyTable("Nachweisverfahren_Normen",
				new String[]{"Nachweisverfahren","Normen","Norm_Nummer"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{nachweisverfahren,normen,null},
				new LinkedHashMap[]{null,null,null});
		addTable(nachweisverfahren_Normen, -1);

		MyTable aufbereitungs_nachweisverfahren = new MyTable("Aufbereitungs_Nachweisverfahren",
				new String[]{"Aufbereitungsverfahren","Nachweisverfahren","Nachweisgrenze","NG_Einheit","Sensitivitaet","Spezifitaet","Effizienz","Wiederfindungsrate","Referenz"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","DOUBLE","DOUBLE","DOUBLE","DOUBLE","INTEGER"},
				new String[]{null,null,"Nachweisgrenze des Verfahrens bezogen auf die Konzentration des Agens auf/in der Ausgangsmatrix","Einheit der Konzentration der Nachweisgrenze - Auswahlbox","Mittlere zu erwartende Sensitivit�t (Angabe als Wert im Bereich 0 - 1) (95%= 0.95)\nDefinition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Mittlere zu erwartende Spezifit�t (Angabe als Wert im Bereich 0 - 1) (95%= 0.95)\nDefinition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Mittlere zu erwartende Effizienz (Angabe als Wert im Bereich 0 - 1) (95%= 0.95)\nDefinition siehe z.B. http://www.bb-sbl.de/tutorial/zusammenhangsanalyse/sensitivitaetspezifitaeteffizienz.html","Wiederfindungsrate","Referenz f�r alle Angaben im Datensatz"},
				new MyTable[]{aufbereitungsverfahren,nachweisverfahren,newDoubleTable,Konzentrationseinheiten,null,null,null,null,literatur});
		addTable(aufbereitungs_nachweisverfahren, MyList.Nachweissysteme_LIST);

		MyTable labor_aufbereitungs_nachweisverfahren = new MyTable("Labor_Aufbereitungs_Nachweisverfahren",
				new String[]{"Labor","Aufbereitungs_Nachweisverfahren","ZertifikatNr","Gueltigkeit","Zertifizierungssystem","Durchsatz","DurchsatzEinheit","Kosten","KostenEinheit","FreigabeModus","AuftragsAnnahme","SOP","LaborAngebot"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)","DATE","INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","VARCHAR(50)","INTEGER","BOOLEAN","BOOLEAN","BLOB(10M)"},
				new String[]{"Verweis zum Eintrag in Labor-Tabelle","Verweis zum Eintrag in Kombi-Tabelle Aufbereitungs_Nachweisverfahren","Zertifikatnummer - falls vorhanden","G�ltigkeitsdatum des Zertifikats - falls vorhanden","Zertifizierungsanbieter - Verweis auf Tabelle Zertifizierungssysteme","Angaben zum Durchsatz des Labors f�r das Verfahren - sollte im LaborAngebot angegeben sein","Einheit des Durchsatzes - Auswahlbox","Kosten pro Probe/Einzelansatz - ohne Rabatte - sollte im LaborAngebot angegeben sein","W�hrung f�r die Kosten - Auswahlbox","Auswahl ob diese Information �ffentlich zug�nglich sein soll: nie, nur in der Krise, immer - Auswahlbox","Nimmt das Labor auch externe Auftr�ge an?","Existiert eine SOP zu dem Verfahren bei dem Labor?","Das Angebot kann ein individuelles Angebot, ein Katalogeintrag, eine E-Mail oder auch ein anderes Dokument des Labors sein, m�glicherweise auch mit Angabe der G�ltigkeit des Angebots"},
				new MyTable[]{labore,aufbereitungs_nachweisverfahren,null,null,zertifikate,null,null,null,null,null,null,null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,hashSpeed,null,hashGeld,hashFreigabe,null,null,null});
		addTable(labor_aufbereitungs_nachweisverfahren, MyList.Nachweissysteme_LIST);

	
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
		addTable(tenazity_raw_data, MyList.Tenazitaet_LIST);
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
		addTable(tenazity_measured_vals, DBKernel.isKNIME ? MyList.Tenazitaet_LIST : -1);
		tenazity_raw_data.setForeignField(tenazity_measured_vals, 6);

		MyTable Versuchsbedingungen_Sonstiges = new MyTable("Versuchsbedingungen_Sonstiges",
				new String[]{"Versuchsbedingungen","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{tenazity_raw_data,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null});
		addTable(Versuchsbedingungen_Sonstiges, -1);
		MyTable Messwerte_Sonstiges = new MyTable("Messwerte_Sonstiges",
				new String[]{"Messwerte","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{tenazity_measured_vals,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null});
		addTable(Messwerte_Sonstiges, -1);

		MyTable importedCombaseData = new MyTable("ImportedCombaseData",
				new String[]{"CombaseID","Literatur","Versuchsbedingung"},
				new String[]{"VARCHAR(100)","INTEGER","INTEGER"},
				new String[]{null,null,null},
				new MyTable[]{null,literatur,tenazity_raw_data},
				new String[][]{{"CombaseID","Literatur","Versuchsbedingung"}},
				new LinkedHashMap[]{null,null,null});
		addTable(importedCombaseData, -1);

		// Prozessdaten:
		MyTable betriebe = new MyTable("Produzent", new String[]{"Kontaktadresse","Betriebsnummer"},
				new String[]{"INTEGER","VARCHAR(50)"},
				new String[]{"Verweis auf eintr�ge in Tabelle Kontakte mit Lebensmittel-Betrieben, Landwirten etc","Betriebsnummer aus BALVI-System sofern vorhanden"},
				new MyTable[]{adressen,null});
		addTable(betriebe, -1);
		MyTable betrieb_matrix_produktion = new MyTable("Betrieb_Matrix_Produktion", new String[]{"Betrieb","Matrix","EAN","Produktionsmenge","Einheit","Referenz","Anteil","lose"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","DOUBLE","VARCHAR(50)","INTEGER","DOUBLE","BOOLEAN"},
				new String[]{"Verweis auf die Basistabelle der Betriebe","Verweis auf die Matricestabelle","EAN-Nummer aus SA2-Datenbank - falls bekannt","Produktionsmenge des Lebensmittels","Verweis auf Basistabelle Ma�einheiten","Verweis auf Literaturstelle","Anteil in %",null},
				new MyTable[]{betriebe,matrix,null,null,null,literatur,null,null},
				new LinkedHashMap[]{null,null,null,null,hashGewicht,null,null,null});
		addTable(betrieb_matrix_produktion, -1);
		MyTable prozessElemente = new MyTable("ProzessElemente",
				new String[]{"Prozess_ID","ProzessElement","ProzessElementKategorie","ProzessElementSubKategorie","ProzessElement_engl","ProzessElementKategorie_engl","ProzessElementSubKategorie_engl"},
				new String[]{"INTEGER","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)","VARCHAR(60)"},
				new String[]{"Prozess_ID in CARVER","Bezeichnung des Vorgangs bei der Prozessierung","Bezeichnung f�r die Kategorie, in die der Prozess einzuordnen ist","Bezeichnung der Unterkategorie f�r eine genauere Spezifizierung des Vorgangs","Wie ProzessElement, aber englische Bezeichnung","Wie ProzessElementKategorie, aber englische Bezeichnung","Wie ProzessElementSubKategorie, aber englische Bezeichnung"},
				new MyTable[]{null,null,null,null,null,null,null},
				new LinkedHashMap[]{null,null,null,null,null,null,null});
		addTable(prozessElemente, MyList.Prozessdaten_LIST);
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
		addTable(prozessFlow, MyList.Prozessdaten_LIST);
		MyTable prozessFlowReferenzen = new MyTable("ProzessWorkflow_Literatur",
				new String[]{"ProzessWorkflow","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{prozessFlow,literatur},
				new LinkedHashMap[]{null,null});
		addTable(prozessFlowReferenzen, -1);		
		
		MyTable Kostenkatalog = new MyTable("Kostenkatalog",
				new String[]{"Kostenart","Kostenunterart","Beschreibung","Einheit"},
				new String[]{"VARCHAR(255)","VARCHAR(255)","VARCHAR(255)","VARCHAR(25)"},
				new String[]{null,null,null,"Einheit pro Bezugseinheit (pro Liter Endprodukt)"},
				new MyTable[]{null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null},
				new String[]{null,null,null,null});
		addTable(Kostenkatalog, -1);
		MyTable Kostenkatalogpreise = new MyTable("Kostenkatalogpreise",
				new String[]{"Kostenkatalog","Betrieb","Datum","Preis","Waehrung"},
				new String[]{"INTEGER","INTEGER","DATE","DOUBLE","VARCHAR(50)"},
				new String[]{null,null,"Preis wurde erhoben am...",null,null},
				new MyTable[]{Kostenkatalog,betriebe,null,newDoubleTable,null},
				null,
				new LinkedHashMap[]{null,null,null,null,hashGeld},
				new String[]{null,null,null,null,null});
		addTable(Kostenkatalogpreise, DBKernel.getUsername().equals("burchardi") || DBKernel.getUsername().equals("defad") ? 66 : -1);

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
		addTable(prozessdaten, DBKernel.isKNIME ? MyList.Prozessdaten_LIST : -1); // MyList.Prozessdaten_LIST
		prozessFlow.setForeignField(prozessdaten, 7);
		MyTable prozessReferenzen = new MyTable("Prozessdaten_Literatur",
				new String[]{"Prozessdaten","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{prozessdaten,literatur},
				new LinkedHashMap[]{null,null});
		addTable(prozessReferenzen, -1);		
		MyTable Prozessdaten_Sonstiges = new MyTable("Prozessdaten_Sonstiges",
				new String[]{"Prozessdaten","SonstigeParameter","Wert","Einheit","Ja_Nein"},
				new String[]{"INTEGER","INTEGER","DOUBLE","INTEGER","BOOLEAN"},
				new String[]{null,null,null,null,"Falls der Parameter ein 'Ja/Nein' erwartet, wie z.B. Vakuum-verpackt, dann muss der Wert hier angegeben werden"},
				new MyTable[]{prozessdaten,SonstigeParameter,newDoubleTable,Konzentrationseinheiten,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				new String[]{null,null,null,null,null});
		addTable(Prozessdaten_Sonstiges, -1);
		MyTable Prozessdaten_Messwerte = new MyTable("Prozessdaten_Messwerte",
				new String[]{"Prozessdaten","ExperimentID","Agens","Zeit","ZeitEinheit","Konzentration","Einheit","Konzentration_GKZ","Einheit_GKZ"},
				new String[]{"INTEGER","INTEGER","INTEGER","DOUBLE","VARCHAR(50)","DOUBLE","INTEGER","DOUBLE","INTEGER"},
				new String[]{null,null,null,"Zeitpunkt der Messung relativ zum Prozessschritt,\nd.h. falls die Messung z.B. gleich zu Beginn des Prozessschrittes gemacht wird,\ndann ist hier 0 einzutragen!\nUnabh�ngig davon wie lange der gesamte Prozess schon l�uft!",null,"Konzentration des Agens","Konzentration - Einheit","Gesamtkeimzahl","Gesamtkeimzahl-Einheit"},
				new MyTable[]{prozessdaten,null,agenzien,newDoubleTable,null,newDoubleTable,Konzentrationseinheiten,newDoubleTable,Konzentrationseinheiten},
				null,
				new LinkedHashMap[]{null,null,null,null,hashZeit,null,null,null,null},
				new String[]{null,null,null,null,null,null,null,null,null});
		addTable(Prozessdaten_Messwerte, -1);
		MyTable Prozessdaten_Kosten = new MyTable("Prozessdaten_Kosten",
				new String[]{"Prozessdaten","Kostenkatalog","Menge"},
				new String[]{"INTEGER","INTEGER","DOUBLE"},
				new String[]{null,null,null,},
				new MyTable[]{prozessdaten,Kostenkatalog,newDoubleTable},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null});
		addTable(Prozessdaten_Kosten, -1);

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
		addTable(Verpackungen, -1);
		addTable(prozessLinks, -1);
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
		addTable(zutatendaten, -1);
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
		addTable(Zutatendaten_Sonstiges, -1);
		MyTable Zutatendaten_Kosten = new MyTable("Zutatendaten_Kosten",
				new String[]{"Zutatendaten","Kostenkatalog","Menge"},
				new String[]{"INTEGER","INTEGER","DOUBLE"},
				new String[]{null,null,null,},
				new MyTable[]{zutatendaten,Kostenkatalog,newDoubleTable},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null});
		addTable(Zutatendaten_Kosten, -1);
		

		MyTable LinkedTestConditions = new MyTable("LinkedTestConditions", new String[]{"CondID","LinkedCondID"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{null,null},
				null,
				new LinkedHashMap[]{null,null},
				null);
		addTable(LinkedTestConditions, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);	

		generateStatUpModellTables(literatur, tenazity_raw_data, hashZeit, Konzentrationseinheiten);

		doLieferkettenTabellen(adressen, agenzien, matrix, h4);

	}
	@SuppressWarnings("unchecked")
	private static void doLieferkettenTabellen(final MyTable adressen, final MyTable agenzien, final MyTable matrix, final LinkedHashMap<Object, String> h4) {
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
		addTable(Knoten, MyList.Lieferketten_LIST);

		MyTable Agensnachweis = new MyTable("Station_Agenzien", new String[]{"Station","Erreger","Labornachweis","AnzahlLabornachweise"},
				new String[]{"INTEGER","INTEGER","BOOLEAN","INTEGER"},
				new String[]{null,null,"Labornachweise vorhanden?",null},
				new MyTable[]{Knoten,agenzien,null,null},
				null,
				new LinkedHashMap[]{null,null,hYNB,null});
		addTable(Agensnachweis, -1);
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
		addTable(Produzent_Artikel, MyList.Lieferketten_LIST);
		Knoten.setForeignField(Produzent_Artikel, 13);
		MyTable Produktmatrices = new MyTable("Produktkatalog_Matrices", new String[]{"Produktkatalog","Matrix"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{Produzent_Artikel,matrix},
				null,
				new LinkedHashMap[]{null,null});
		addTable(Produktmatrices, -1);
		
		MyTable Chargen = new MyTable("Chargen",
				new String[]{"Artikel","Zutaten","ChargenNr","MHD","Herstellungsdatum","Menge","Einheit","Lieferungen"},
				new String[]{"INTEGER","INTEGER","VARCHAR(255)","DATE","DATE","DOUBLE","VARCHAR(50)","INTEGER"},
				new String[]{null,null,null,null,null,null,null,null},
				new MyTable[]{Produzent_Artikel,null,null,null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null,null,null,null},
				new String[]{null,"INT",null,null,null,null,null,"INT"});
		addTable(Chargen, MyList.Lieferketten_LIST);
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
		addTable(Artikel_Lieferung, MyList.Lieferketten_LIST);
		Chargen.setForeignField(Artikel_Lieferung, 7);
		
		MyTable ChargenVerbindungen = new MyTable("ChargenVerbindungen",
				new String[]{"Zutat","Produkt","MixtureRatio"}, // man k�nnte hier sowas machen wie: ,"#Units","Unitmenge","UnitEinheit", um zu notieren wieviel der vorgelieferten Menge in das Produkt gegangen sind
				new String[]{"INTEGER","INTEGER","DOUBLE"}, // ,"VARCHAR(50)","VARCHAR(50)","VARCHAR(50)"
				new String[]{null,null,"Mixture Ratio (prozentualer Anteil von der Zutat im Zielprodukt,\nz.B. Zielprodukt = Sprout mixture, Zutat = alfalfa sprouts => z.B. 0.33 (33%))"},
				new MyTable[]{Artikel_Lieferung,Chargen,null},
				null,
				new LinkedHashMap[]{null,null,null},
				new String[]{null,null,null});
		addTable(ChargenVerbindungen, DBKernel.debug ? MyList.Lieferketten_LIST : -1);
		Chargen.setForeignField(ChargenVerbindungen, 1);

		//check4Updates_129_130(myList);

		//DBKernel.sendRequest("UPDATE " + DBKernel.delimitL("Kontakte") + " SET " + DBKernel.delimitL("Bundesland") + " = 'NI' WHERE " + DBKernel.delimitL("ID") + " = 167", false);
	}
	private static MyTable generateICD10Tabellen() {
		MyTable ICD10_Kapitel = new MyTable("ICD10_Kapitel", new String[]{"KapNr","KapTi"},
				new String[]{"VARCHAR(2)","VARCHAR(110)"},
				new String[]{"Kapitelnummer, 2 Zeichen","Kapiteltitel, bis zu 110 Zeichen"},
				new MyTable[]{null,null},
				new String[][]{{"KapNr"}},
				null,
				new String[]{null,null});
		addTable(ICD10_Kapitel, -1);		
		MyTable ICD10_Gruppen = new MyTable("ICD10_Gruppen", new String[]{"GrVon","GrBis","KapNr","GrTi"},
				new String[]{"VARCHAR(3)","VARCHAR(3)","INTEGER","VARCHAR(210)"},
				new String[]{"erster Dreisteller der Gruppe, 3 Zeichen","letzter Dreisteller der Gruppe, 3 Zeichen","Kapitelnummer, 2 Zeichen","Gruppentitel, bis zu 210 Zeichen"},
				new MyTable[]{null,null,ICD10_Kapitel,null},
				new String[][]{{"GrVon"}},
				null,
				new String[]{null,null,null,null});
		addTable(ICD10_Gruppen, -1);		
		MyTable ICD10_MorbL = new MyTable("ICD10_MorbL", new String[]{"MorbLCode","MorbLTi"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Titel"},
				new MyTable[]{null,null},
				new String[][]{{"MorbLCode"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MorbL, -1);		
		MyTable ICD10_MortL1Grp = new MyTable("ICD10_MortL1Grp", new String[]{"MortL1GrpCode","MortL1GrpTi"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Gruppenschl�sselnummer","Gruppentitel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL1GrpCode"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MortL1Grp, -1);		
		MyTable ICD10_MortL1 = new MyTable("ICD10_MortL1", new String[]{"MortL1Code","MortL1GrpCode","MortL1Ti"},
				new String[]{"VARCHAR(5)","INTEGER","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Gruppenschl�sselnummer","Titel"},
				new MyTable[]{null,ICD10_MortL1Grp,null},
				new String[][]{{"MortL1Code"}},
				null,
				new String[]{null,null,null});
		addTable(ICD10_MortL1, -1);		
		MyTable ICD10_MortL2 = new MyTable("ICD10_MortL2", new String[]{"MortL2Code","MortL2Ti"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Titel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL2Code"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MortL2, -1);		
		MyTable ICD10_MortL3Grp = new MyTable("ICD10_MortL3Grp", new String[]{"MortL3GrpCode","MortL3GrpTi"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Gruppenschl�sselnummer","Gruppentitel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL3GrpCode"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MortL3Grp, -1);		
		MyTable ICD10_MortL3 = new MyTable("ICD10_MortL3", new String[]{"MortL3Code","MortL3GrpCode","MortL3Ti"},
				new String[]{"VARCHAR(5)","INTEGER","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Gruppenschl�sselnummer","Titel"},
				new MyTable[]{null,ICD10_MortL3Grp,null},
				new String[][]{{"MortL3Code"}},
				null,
				new String[]{null,null,null});
		addTable(ICD10_MortL3, -1);		
		MyTable ICD10_MortL4 = new MyTable("ICD10_MortL4", new String[]{"MortL4Code","MortL4Ti"},
				new String[]{"VARCHAR(5)","VARCHAR(255)"},
				new String[]{"Schl�sselnummer","Titel"},
				new MyTable[]{null,null},
				new String[][]{{"MortL4Code"}},
				null,
				new String[]{null,null});
		addTable(ICD10_MortL4, -1);		
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
		addTable(ICD10_Kodes, MyList.Krankheitsbilder_LIST);
		return ICD10_Kodes;
	}	
	private static void fillHashModelTypes() {
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
	private static void generateStatUpModellTables(final MyTable literatur, final MyTable tenazity_raw_data, final LinkedHashMap<Object, String> hashZeit, final MyTable Konzentrationseinheiten) {
		MyTable PMMLabWorkflows = new MyTable("PMMLabWorkflows", new String[]{"Workflow"},
				new String[]{"BLOB(100M)"},
				new String[]{null},
				new MyTable[]{null},
				null,
				new LinkedHashMap[]{null},
				null);
		addTable(PMMLabWorkflows, -1);	
		MyTable DataSource = new MyTable("DataSource", new String[]{"Table","TableID","SourceDBUUID","SourceID"},
				new String[]{"VARCHAR(255)","INTEGER","VARCHAR(255)","INTEGER"},
				new String[]{null,null,null,null},
				new MyTable[]{null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null},
				null);
		addTable(DataSource, -1);	

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
		addTable(Parametertyp, -1);
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
		addTable(Modellkatalog, MyList.PMModelle_LIST);		
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
		addTable(ModellkatalogParameter, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);	
		MyTable Modell_Referenz = new MyTable("Modell_Referenz", new String[]{"Modell","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{Modellkatalog,literatur},
				null,
				new LinkedHashMap[]{null,null},
				null,
				new String[] {"not null","not null"});
		addTable(Modell_Referenz, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);		
		
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
		addTable(GeschaetzteModelle, MyList.PMModelle_LIST);		
		MyTable GeschaetztesModell_Referenz = new MyTable("GeschaetztesModell_Referenz", new String[]{"GeschaetztesModell","Literatur"},
				new String[]{"INTEGER","INTEGER"},
				new String[]{null,null},
				new MyTable[]{GeschaetzteModelle,literatur},
				null,
				new LinkedHashMap[]{null,null},
				null,
				new String[] {"not null","not null"});
		addTable(GeschaetztesModell_Referenz, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);	
		MyTable GeschaetzteParameter = new MyTable("GeschaetzteParameter", new String[]{"GeschaetztesModell","Parameter",
				"Wert","ZeitEinheit","Einheit","KI.unten","KI.oben","SD","StandardError","t","p"},
				new String[]{"INTEGER","INTEGER",
				"DOUBLE","VARCHAR(50)","INTEGER","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE","DOUBLE"},
				new String[]{null,null,null,null,null,null,null,null,null,null,null},
				new MyTable[]{GeschaetzteModelle,ModellkatalogParameter,null,null,Konzentrationseinheiten,null,null,null,null,null,null},
				null,
				new LinkedHashMap[]{null,null,null,hashZeit,null,null,null,null,null,null,null},
				null,
				new String[] {"not null","not null",null,null,null,null,null,null,null,null,null});
		addTable(GeschaetzteParameter, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);	
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
		addTable(GueltigkeitsBereiche, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);
		MyTable VarParMaps = new MyTable("VarParMaps", new String[]{"GeschaetztesModell","VarPar","VarParMap"},
				new String[]{"INTEGER","INTEGER","VARCHAR(50)"},
				new String[]{null,null,null},
				new MyTable[]{GeschaetzteModelle,ModellkatalogParameter,null},
				null,
				new LinkedHashMap[]{null,null,null},
				null,
				new String[] {null,null,null});
		addTable(VarParMaps, -1);	
		MyTable GeschaetzteParameterCovCor = new MyTable("GeschaetzteParameterCovCor", new String[]{"param1","param2",
				"GeschaetztesModell","cor","Wert"},
				new String[]{"INTEGER","INTEGER","INTEGER","BOOLEAN","DOUBLE"},
				new String[]{null,null,null,null,null},
				new MyTable[]{GeschaetzteParameter,GeschaetzteParameter,GeschaetzteModelle,null,null},
				null,
				new LinkedHashMap[]{null,null,null,null,null},
				null,
				null);//new String[] {"not null","not null","not null","not null",null});
		addTable(GeschaetzteParameterCovCor, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);		
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
		addTable(Sekundaermodelle_Primaermodelle, DBKernel.isKNIME ? MyList.PMModelle_LIST : -1);		
	}
  
  private static void fillHashes() {
		hashZeit.put("Sekunde", DBKernel.getLanguage().equals("en") ? "Second(s)" : "Sekunde(n) [s][sec]");					
		hashZeit.put("Minute", DBKernel.getLanguage().equals("en") ? "Minute(s)" : "Minute(n)");					
		hashZeit.put("Stunde", DBKernel.getLanguage().equals("en") ? "Hour(s)" : "Stunde(n)");		
		hashZeit.put("Tag", DBKernel.getLanguage().equals("en") ? "Day(s)" : "Tag(e)");		
		hashZeit.put("Woche", DBKernel.getLanguage().equals("en") ? "Week(s)" : "Woche(n)");		
		hashZeit.put("Monat", DBKernel.getLanguage().equals("en") ? "Month(s)" : "Monat(e)");		
		hashZeit.put("Jahr", DBKernel.getLanguage().equals("en") ? "Year(s)" : "Jahr(e)");			  

		hashGeld.put("Dollar", "Dollar ($)");					
		hashGeld.put("Euro", "Euro (�)");					

		hashGewicht.put("Milligramm", DBKernel.getLanguage().equals("en") ? "Milligrams (mg)" : "Milligramm (mg)");					
		hashGewicht.put("Gramm", DBKernel.getLanguage().equals("en") ? "Grams (g)" : "Gramm (g)");					
		hashGewicht.put("Kilogramm", DBKernel.getLanguage().equals("en") ? "Kilograms (kg)" : "Kilogramm (kg)");					
		hashGewicht.put("Tonne", DBKernel.getLanguage().equals("en") ? "Tons (t)" : "Tonne (t)");					

		hashSpeed.put("pro Stunde", DBKernel.getLanguage().equals("en") ? "per hour (1/h)" : "pro Stunde (1/h)");					
		hashSpeed.put("pro Tag", DBKernel.getLanguage().equals("en") ? "per day (1/d)" : "pro Tag (1/d)");							

		hashDosis.put("Sporenzahl", "Sporenzahl");					
		hashDosis.put("KBE pro g", "KBE (cfu) pro Gramm (KBE/g)");					
		hashDosis.put("KBE pro ml", "KBE (cfu) pro Milliliter (KBE/ml)");					
		hashDosis.put("PBE pro g", "PBE (pfu) pro Gramm (PBE/g)");					
		hashDosis.put("PBE pro ml", "PBE (pfu) pro Milliliter (PBE/ml)");					
		hashDosis.put("Milligramm", "Milligramm (mg)");							
		hashDosis.put("Mikrogramm", "Mikrogramm (\u00B5g)");							
		hashDosis.put("\u00B5g/kg/KG", "\u00B5g/kg/KG");							
		hashDosis.put("Anzahl", "Anzahl (Viren, Bakterien, Parasiten, Organismen, ...)");	

		hashFreigabe.put(0, DBKernel.getLanguage().equals("en") ? "never" : "gar nicht");					
		hashFreigabe.put(1, DBKernel.getLanguage().equals("en") ? "crisis" : "Krise");					
		hashFreigabe.put(2, DBKernel.getLanguage().equals("en") ? "always" : "immer");					
		
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
  }
	private static void addTable(MyTable myT, int child) {
		myT.setChild(child);
		myTables.put(myT.getTablename(), myT);
	}
  /*
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
			
			DBKernel.doMNs(getTable("Matrices"));
			DBKernel.doMNs(getTable("Agenzien"));
	
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
	*/


	/*
	public void createAllTablesInDB() {
		//if (myDB != null) {
			createTables();				
			if (!DBKernel.isKrise) {
				fillWithDataAndGrants();
				UpdateChecker.doStatUpGrants();
			}
		//}				
	}
	private void createTables() {
		for(String key : myTables.keySet()) {
			if (!DBKernel.isKrise || key.equals("Produzent_Artikel") || key.equals("Artikel_Lieferung")
					 || key.equals("Lieferung_Lieferungen") || key.equals("Produzent")
					 || key.equals("Kontakte")) {
				myTables.get(key).createTable();
			}	
		}
	}
  private void fillWithDataAndGrants() {
	
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

		for(String key : myTables.keySet()) {
			String tableName = myTables.get(key).getTablename();
			if (!tableName.equals("Users") && !tableName.equals("ChangeLog") && !tableName.equals("DateiSpeicher")) {
				DBKernel.grantDefaults(tableName);
			}
		}
    DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL("ChangeLog") + " TO " + DBKernel.delimitL("WRITE_ACCESS"), false);				
    DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL("ChangeLog") + " TO " + DBKernel.delimitL("SUPER_WRITE_ACCESS"), false);				
    DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL("DateiSpeicher") + " TO " + DBKernel.delimitL("WRITE_ACCESS"), false);				
    DBKernel.sendRequest("GRANT SELECT, INSERT, UPDATE ON TABLE " + DBKernel.delimitL("DateiSpeicher") + " TO " + DBKernel.delimitL("SUPER_WRITE_ACCESS"), false);				

    try {
	      DBKernel.getDBConnection().createStatement().execute("CREATE USER " + DBKernel.delimitL(DBKernel.getTempSA(DBKernel.HSHDB_PATH)) + " PASSWORD '" + DBKernel.getTempSAPass(DBKernel.HSHDB_PATH) + "' ADMIN"); // MD5.encode("de6!�5ddy", "UTF-8")
    }
    catch (Exception e) {MyLogger.handleException(e);}
  }
	*/
}
