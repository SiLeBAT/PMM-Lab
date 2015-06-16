/**
 * Code and decode a ParamXml into/from JSON.
 * @author Miguel Alba
 */
package de.bund.bfr.knime.pmm.jsonutil;

import java.util.HashMap;

import org.json.simple.JSONObject;

import de.bund.bfr.knime.pmm.common.ParamXml;

public class JSONParam {

	JSONObject obj; // Json object
	
	// attribute keys
	static final String ATT_NAME = "name";
	static final String ATT_ORIGNAME = "origname";
	static final String ATT_VALUE = "value";
	static final String ATT_ERROR = "error";
	static final String ATT_MIN = "min";
	static final String ATT_MAX = "max";
	static final String ATT_P = "P";
	static final String ATT_T = "t";
	static final String ATT_MINGUESS = "minGuess";
	static final String ATT_MAXGUESS = "maxGuess";
	static final String ATT_CATEGORY = "category";
	static final String ATT_UNIT = "unit";
	static final String ATT_DESCRIPTION = "description";
	static final String ATT_CORRELATION = "correlation";


	public JSONParam(JSONObject obj) {
		this.obj = obj;
	}

	@SuppressWarnings("unchecked")
	public JSONParam(ParamXml param) {
		obj = new JSONObject();

		obj.put(ATT_NAME, param.getName());
		obj.put(ATT_ORIGNAME, param.getOrigName());
		obj.put(ATT_VALUE, param.getValue());
		obj.put(ATT_ERROR, param.getError());
		obj.put(ATT_MIN, param.getMin());
		obj.put(ATT_MAX, param.getMax());
		obj.put(ATT_P, param.getP());
		obj.put(ATT_T, param.getT());
		obj.put(ATT_MINGUESS, param.getMinGuess());
		obj.put(ATT_MAXGUESS, param.getMaxGuess());
		obj.put(ATT_CATEGORY, param.getCategory());
		obj.put(ATT_UNIT, param.getUnit());
		obj.put(ATT_DESCRIPTION, param.getDescription());
		obj.put(ATT_CORRELATION, param.getAllCorrelations());
	}

	public JSONObject getObj() {
		return obj;
	}

	public ParamXml toParamXml() {
		String name = (String) obj.get(ATT_NAME);
		String origName = (String) obj.get(ATT_ORIGNAME);
		Double value = (Double) obj.get(ATT_VALUE);
		Double error = (Double) obj.get(ATT_ERROR);
		Double min = (Double) obj.get(ATT_MIN);
		Double max = (Double) obj.get(ATT_MAX);
		Double P = (Double) obj.get(ATT_P);
		Double t = (Double) obj.get(ATT_T);
		Double minGuess = (Double) obj.get(ATT_MINGUESS);
		Double maxGuess = (Double) obj.get(ATT_MAXGUESS);
		String category = (String) obj.get(ATT_CATEGORY);
		String unit = (String) obj.get(ATT_UNIT);
		String description = (String) obj.get(ATT_DESCRIPTION);
		HashMap<String, Double> correlations = (HashMap<String, Double>) obj
				.get(ATT_CORRELATION);

		return new ParamXml(name, origName, value, error, min, max, P, t,
				minGuess, maxGuess, category, unit, description, correlations);
	}
}
