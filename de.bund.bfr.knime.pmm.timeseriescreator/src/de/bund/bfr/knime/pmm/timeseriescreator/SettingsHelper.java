/*******************************************************************************
 * PMM-Lab � 2013, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2013, Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.pmm.timeseriescreator;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmm.common.AgentXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.XmlConverter;
import de.bund.bfr.knime.pmm.common.units.NumberContent;
import de.bund.bfr.knime.pmm.common.units.Time;

public class SettingsHelper {

	protected static final String CFGKEY_LITERATURE = "Literature";
	protected static final String CFGKEY_AGENT = "Agent";
	protected static final String CFGKEY_MATRIX = "Matrix";
	protected static final String CFGKEY_COMMENT = "Comment";
	protected static final String CFGKEY_MISC = "Misc";
	protected static final String CFGKEY_TIMESERIES = "TimeSeries";
	protected static final String CFGKEY_TIMEUNIT = "TimeUnit";
	protected static final String CFGKEY_LOGCUNIT = "LogcUnit";

	protected static final String DEFAULT_TIMEUNIT = new Time()
			.getStandardUnit();
	protected static final String DEFAULT_LOGCUNIT = new NumberContent()
			.getStandardUnit();

	private List<LiteratureItem> literature;
	private AgentXml agent;
	private MatrixXml matrix;
	private String comment;
	private List<TimeSeriesXml> timeSeries;
	private String timeUnit;
	private String logcUnit;
	private List<MiscXml> misc;

	public SettingsHelper() {
		literature = new ArrayList<>();
		agent = null;
		matrix = null;
		comment = null;
		timeSeries = new ArrayList<>();
		timeUnit = DEFAULT_TIMEUNIT;
		logcUnit = DEFAULT_LOGCUNIT;
		misc = new ArrayList<>();
	}

	public void loadSettings(NodeSettingsRO settings) {
		try {
			literature = XmlConverter.xmlToObject(
					settings.getString(CFGKEY_LITERATURE),
					new ArrayList<LiteratureItem>());
		} catch (InvalidSettingsException e) {
			literature = new ArrayList<>();

		}

		try {
			agent = XmlConverter.xmlToObject(settings.getString(CFGKEY_AGENT),
					null);
		} catch (InvalidSettingsException e) {
			agent = null;
		}

		try {
			matrix = XmlConverter.xmlToObject(
					settings.getString(CFGKEY_MATRIX), null);
		} catch (InvalidSettingsException e) {
			matrix = null;
		}

		try {
			comment = settings.getString(CFGKEY_COMMENT);
		} catch (InvalidSettingsException e) {
			comment = null;
		}

		try {
			timeSeries = XmlConverter.xmlToObject(
					settings.getString(CFGKEY_TIMESERIES),
					new ArrayList<TimeSeriesXml>());
		} catch (InvalidSettingsException e) {
			timeSeries = new ArrayList<>();
		}

		try {
			timeUnit = settings.getString(CFGKEY_TIMEUNIT);
		} catch (InvalidSettingsException e) {
			timeUnit = DEFAULT_TIMEUNIT;
		}

		try {
			logcUnit = settings.getString(CFGKEY_LOGCUNIT);
		} catch (InvalidSettingsException e) {
			logcUnit = DEFAULT_LOGCUNIT;
		}

		try {
			misc = XmlConverter.xmlToObject(settings.getString(CFGKEY_MISC),
					new ArrayList<MiscXml>());
		} catch (InvalidSettingsException e) {
			misc = new ArrayList<>();
		}
	}

	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(CFGKEY_LITERATURE,
				XmlConverter.objectToXml(literature));
		settings.addString(CFGKEY_AGENT, XmlConverter.objectToXml(agent));
		settings.addString(CFGKEY_MATRIX, XmlConverter.objectToXml(matrix));
		settings.addString(CFGKEY_COMMENT, comment);
		settings.addString(CFGKEY_TIMESERIES,
				XmlConverter.objectToXml(timeSeries));
		settings.addString(CFGKEY_TIMEUNIT, timeUnit);
		settings.addString(CFGKEY_LOGCUNIT, logcUnit);
		settings.addString(CFGKEY_MISC, XmlConverter.objectToXml(misc));
	}

	public List<LiteratureItem> getLiterature() {
		return literature;
	}

	public void setLiterature(List<LiteratureItem> literature) {
		this.literature = literature;
	}

	public AgentXml getAgent() {
		return agent;
	}

	public void setAgent(AgentXml agent) {
		this.agent = agent;
	}

	public MatrixXml getMatrix() {
		return matrix;
	}

	public void setMatrix(MatrixXml matrix) {
		this.matrix = matrix;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<TimeSeriesXml> getTimeSeries() {
		return timeSeries;
	}

	public void setTimeSeries(List<TimeSeriesXml> timeSeries) {
		this.timeSeries = timeSeries;
	}

	public String getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}

	public String getLogcUnit() {
		return logcUnit;
	}

	public void setLogcUnit(String logcUnit) {
		this.logcUnit = logcUnit;
	}

	public List<MiscXml> getMisc() {
		return misc;
	}

	public void setMisc(List<MiscXml> misc) {
		this.misc = misc;
	}
}
