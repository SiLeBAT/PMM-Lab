/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.pmm.jsonutil;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.bund.bfr.knime.pmm.common.ParamXml;

public class JSONParamList {
	JSONArray obj;
	
	public JSONParamList(JSONArray obj) {
		this.obj = obj;
	}
	
	@SuppressWarnings("unchecked")
	public JSONParamList(List<ParamXml> params) {
		obj = new JSONArray();
		
		for (ParamXml param : params) {
			obj.add(new JSONParam(param).getObj());
		}
	}

	public JSONArray getObj() {
		return obj;
	}
	
	public List<ParamXml> toParamXml() {
		List<ParamXml> params = new ArrayList<>();
		
		for (int i = 0; i < obj.size(); i++) {
			JSONObject jp = (JSONObject) obj.get(i);
			params.add(new JSONParam(jp).toParamXml());
		}
		
		return params;
	}
}
