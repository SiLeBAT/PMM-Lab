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
public class TimeSeriesList {
	private static final String NUM_TIMESERIES = "numTimeSeries";
	private static final String TIMESERIES = "timeSeries";

	private int numTimeSeries;
	private TimeSeries[] timeSeries;

	public TimeSeries[] getTimeSeries() {
		return timeSeries;
	}

	public void setTimeSeries(final TimeSeries[] timeSeries) {
		numTimeSeries = timeSeries.length;
		this.timeSeries = timeSeries;
	}

	public void saveToNodeSettings(NodeSettingsWO settings) {
		settings.addInt(NUM_TIMESERIES, numTimeSeries);
		for (int i = 0; i < numTimeSeries; i++) {
			timeSeries[i].saveToNodeSettings(settings.addNodeSettings(TIMESERIES + i));
		}
	}

	public void loadFromNodeSettings(NodeSettingsRO settings) {
		try {
			numTimeSeries = settings.getInt(NUM_TIMESERIES);
			timeSeries = new TimeSeries[numTimeSeries];
			for (int i = 0; i < numTimeSeries; i++) {
				timeSeries[i] = new TimeSeries();
				timeSeries[i].loadFromNodeSettings(settings.getNodeSettings(TIMESERIES + i));
			}
		} catch (InvalidSettingsException e) {
		}
	}
}
