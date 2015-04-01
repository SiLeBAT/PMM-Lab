/**
 * Code and decode an IndepXml into/from JSON.
 * @author Miguel Alba
 */
package de.bund.bfr.knime.pmm.jsonutil;

import org.json.simple.JSONObject;

import de.bund.bfr.knime.pmm.common.IndepXml;

public class JSONIndep {
	JSONObject obj; // json object

	// attribute keys
	private static final String ATT_NAME = "name";
	private static final String ATT_ORIGNAME = "origname";
	private static final String ATT_MIN = "min";
	private static final String ATT_MAX = "max";
	private static final String ATT_CATEGORY = "category";
	private static final String ATT_UNIT = "unit";
	private static final String ATT_DESCRIPTION = "description";

	public JSONIndep(JSONObject obj) {
		this.obj = obj;
	}

	@SuppressWarnings("unchecked")
	public JSONIndep(IndepXml indep) {
		obj = new JSONObject();

		obj.put(ATT_NAME, indep.getName());
		obj.put(ATT_ORIGNAME, indep.getOrigName());
		obj.put(ATT_MIN, indep.getMin());
		obj.put(ATT_MAX, indep.getMax());
		obj.put(ATT_CATEGORY, indep.getCategory());
		obj.put(ATT_UNIT, indep.getUnit());
		obj.put(ATT_DESCRIPTION, indep.getDescription());
	}

	public JSONObject getObj() {
		return obj;
	}
	
	public IndepXml toIndepXml() {
		String name = (String) obj.get(ATT_NAME);
		String origName = (String) obj.get(ATT_ORIGNAME);
		double min = (double) obj.get(ATT_MIN);
		double max = (double) obj.get(ATT_MAX);
		String category = (String) obj.get(ATT_CATEGORY);
		String unit = (String) obj.get(ATT_UNIT);
		String description = (String) obj.get(ATT_DESCRIPTION);
		
		return new IndepXml(name, origName, min, max, category, unit, description);
	}
}
