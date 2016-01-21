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
package de.bund.bfr.knime.pmm.js.common;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class LiteratureList {
	private static final String NUM_LITERATURE = "numLiterature";
	private static final String LITERATURE = "literature";

	private int numLiterature;
	private Literature[] literature;

	public Literature[] getLiterature() {
		return literature;
	}

	public void setLiterature(final Literature[] literature) {
		numLiterature = literature.length;
		this.literature = literature;
	}

	public void saveToNodeSettings(NodeSettingsWO settings) {
		SettingsHelper.addInt(NUM_LITERATURE, numLiterature, settings);
		for (int i = 0; i < numLiterature; i++) {
			literature[i].saveToNodeSettings(settings.addNodeSettings(LITERATURE + i));
		}
	}

	public void loadFromNodeSettings(NodeSettingsRO settings) {
		try {
		numLiterature = settings.getInt(NUM_LITERATURE);
		literature = new Literature[numLiterature];
		for (int i = 0; i < numLiterature; i++) {
			literature[i] = new Literature();
			literature[i].loadFromNodeSettings(settings.getNodeSettings(LITERATURE + i));
		}
		} catch (InvalidSettingsException e) {}
	}
}
