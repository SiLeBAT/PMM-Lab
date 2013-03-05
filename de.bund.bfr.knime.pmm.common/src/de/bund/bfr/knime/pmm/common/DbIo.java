package de.bund.bfr.knime.pmm.common;

import java.sql.Array;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public class DbIo {

    public static String convertArray2String(Array array) {
    	String result = null;
	    if (array != null) {
		    try {
				Object[] o = (Object[])array.getArray();
				if (o != null && o.length > 0) {
					result = convertO(o[0]);
					for (int i=1;i<o.length;i++) {
						result += "," + convertO(o[i]);
					}					
				}
			}
		    catch (SQLException e) {
				e.printStackTrace();
			}
	    }
    	return result;
    }
    public static PmmXmlDoc convertStringLists2TSXmlDoc(String t, String l) {
		PmmXmlDoc tsDoc = new PmmXmlDoc();
		if (t != null && l != null && !t.isEmpty() && !l.isEmpty()) {
			String[] toksT = t.split(",");
			String[] toksL = l.split(",");
			if (toksT.length > 0) {
				int i=0;
				for (String time : toksT) {
					try {
						TimeSeriesXml tsx = new TimeSeriesXml("t"+i,
								time.equals("?") ? null : Double.parseDouble(time),
										toksL[i].equals("?") ? null : Double.parseDouble(toksL[i]));
						tsDoc.add(tsx);
					}
					catch (Exception e) {
					}
					i++;
				}
			}
		}
		return tsDoc;    	
    }
    public static PmmXmlDoc convertArrays2ParamXmlDoc(LinkedHashMap<String, String> varMap, Array name,
    		Array value, Array timeUnit, Array units, Array error, Array min, Array max) {
		PmmXmlDoc paramDoc = new PmmXmlDoc();
	    if (name != null) {
		    try {
				Object[] na = (Object[])name.getArray();
				Object[] va = (value == null) ? null : (Object[])value.getArray();
				Object[] tu = (timeUnit == null) ? null : (Object[])timeUnit.getArray();
				Object[] cu = (units == null) ? null : (Object[])units.getArray();
				Object[] er = (error == null) ? null : (Object[])error.getArray();
				Object[] mi = (Object[])min.getArray();
				Object[] ma = (Object[])max.getArray();
				if (na != null && na.length > 0) {
					for (int i=0;i<na.length;i++) {
						String nas = na[i].toString();
						Double vad = (va == null || va[i] == null) ? Double.NaN : Double.parseDouble(va[i].toString());
						if (!Double.isNaN(vad)) {
							if (tu != null && tu[i] != null) {
								if (tu[i].toString().equalsIgnoreCase("Sekunde")) vad = vad / 3600;
								else if (tu[i].toString().equalsIgnoreCase("Minute")) vad = vad / 60;
								else if (tu[i].toString().equalsIgnoreCase("Stunde")) ;
								else if (tu[i].toString().equalsIgnoreCase("Tag")) vad = vad * 24;
								else if (tu[i].toString().equalsIgnoreCase("Woche")) vad = vad * 24 * 7;
								else if (tu[i].toString().equalsIgnoreCase("Monat")) vad = vad * 24 * 30;
								else if (tu[i].toString().equalsIgnoreCase("Jahr")) vad = vad * 24 * 365;
								else System.err.println("convertArrays2ParamXmlDoc - Unconsidered Time Unit used... Please Check!!!! ->" + tu[i]);
							}
							if (cu != null && cu[i] != null) {
								System.err.println("convertArrays2ParamXmlDoc - Unconsidered concentration Unit used... Please Check!!!! ->" + cu[i]);
							}
						}
						Double erd = (er == null || er[i] == null) ? Double.NaN : Double.parseDouble(er[i].toString());
						Double mid = (mi[i] == null) ? Double.NaN : Double.parseDouble(mi[i].toString());
						Double mad = (ma[i] == null) ? Double.NaN : Double.parseDouble(ma[i].toString());
						String onas = nas;
			    		if (varMap != null && varMap.containsKey(nas)) onas = varMap.get(nas);
						ParamXml px = new ParamXml(onas,vad,erd,mid,mad,null,null,cu==null?null:(String) cu[i]);
						px.setName(nas);
						paramDoc.add(px);
					}					
				}
			}
		    catch (SQLException e) {
				e.printStackTrace();
			}
	    }
		return paramDoc;
    }
    public static PmmXmlDoc convertArrays2IndepXmlDoc(LinkedHashMap<String, String> varMap, Array name, Array min, Array max, Array units) {
		PmmXmlDoc indepDoc = new PmmXmlDoc();
	    if (name != null) {
		    try {
				Object[] na = (Object[])name.getArray();
				Object[] mi = (min == null) ? null : (Object[])min.getArray();
				Object[] ma = (max == null) ? null : (Object[])max.getArray();
				Object[] cu = (units == null) ? null : (Object[])units.getArray();
				if (na != null && na.length > 0) {
					for (int i=0;i<na.length;i++) {
						String nas = na[i].toString();
						Double mid = (mi == null || mi[i] == null) ? Double.NaN : Double.parseDouble(mi[i].toString());
						Double mad = (ma == null || ma[i] == null) ? Double.NaN : Double.parseDouble(ma[i].toString());
						String onas = nas;
			    		if (varMap != null && varMap.containsKey(nas)) onas = varMap.get(nas);
						IndepXml ix = new IndepXml(onas,mid,mad,cu==null?null:(String) cu[i]);
						ix.setName(nas);
						indepDoc.add(ix);
					}					
				}
			}
		    catch (SQLException e) {
				e.printStackTrace();
			}
	    }
		return indepDoc;
    }
    
    private static String convertO(Object o) {
		if (o == null) {
			return "?";
		} else {
			return o.toString();
		}
    }

    public static LinkedHashMap<String, String> getVarParMap(String varparStr) {
    	LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
    	if (varparStr != null) {
    		String[] t1 = varparStr.split(",");

    		for (String map : t1) {
    			String[] t2 = map.split("=");
    			if (t2.length == 2) ret.put(t2[0], t2[1]);
    		}
    	}
		return ret;    	
    }
}
