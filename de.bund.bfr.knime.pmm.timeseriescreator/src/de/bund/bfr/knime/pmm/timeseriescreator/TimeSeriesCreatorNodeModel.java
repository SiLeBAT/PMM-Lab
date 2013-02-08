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
package de.bund.bfr.knime.pmm.timeseriescreator;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hsh.bfr.db.DBKernel;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmm.common.AgentXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.XmlConverter;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * This is the model implementation of TimeSeriesCreator.
 * 
 * 
 * @author Christian Thoens
 */
public class TimeSeriesCreatorNodeModel extends NodeModel {

	protected static final String CFGKEY_LITERATUREIDS = "LiteratureIDs";
	protected static final String CFGKEY_AGENTID = "AgentID";
	protected static final String CFGKEY_MATRIXID = "MatrixID";
	protected static final String CFGKEY_COMMENT = "Comment";
	protected static final String CFGKEY_MISCVALUES = "MiscValues";
	protected static final String CFGKEY_TIMESERIES = "TimeSeries";
	protected static final String CFGKEY_TIMEUNIT = "TimeUnit";
	protected static final String CFGKEY_LOGCUNIT = "LogcUnit";
	protected static final String CFGKEY_TEMPUNIT = "TempUnit";

	protected static final int DEFAULT_AGENTID = -1;
	protected static final int DEFAULT_MATRIXID = -1;

	private KnimeSchema schema;

	private List<Integer> literatureIDs;
	private int agentID;
	private int matrixID;
	private String comment;
	private List<Point2D.Double> timeSeries;
	private String timeUnit;
	private String logcUnit;
	private String tempUnit;
	private Map<Integer, Double> miscValues;

	/**
	 * Constructor for the node model.
	 */
	protected TimeSeriesCreatorNodeModel() {
		super(0, 1);
		literatureIDs = new ArrayList<>();
		agentID = DEFAULT_AGENTID;
		matrixID = DEFAULT_MATRIXID;
		comment = "";
		timeSeries = new ArrayList<>();
		timeUnit = AttributeUtilities.getStandardUnit(TimeSeriesSchema.TIME);
		logcUnit = AttributeUtilities.getStandardUnit(TimeSeriesSchema.LOGC);
		tempUnit = AttributeUtilities
				.getStandardUnit(AttributeUtilities.ATT_TEMPERATURE);
		miscValues = new LinkedHashMap<>();
		schema = new TimeSeriesSchema();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataContainer container = exec.createDataContainer(schema
				.createSpec());
		int id = MathUtilities.getRandomNegativeInt();
		PmmXmlDoc timeSeriesXml = new PmmXmlDoc();
		PmmXmlDoc miscXML = new PmmXmlDoc();
		PmmXmlDoc agentXml = new PmmXmlDoc();
		PmmXmlDoc matrixXml = new PmmXmlDoc();
		PmmXmlDoc literatureXML = new PmmXmlDoc();

		for (int litID : literatureIDs) {
			String author = DBKernel.getValue("Literatur", "ID", litID + "",
					"Erstautor") + "";
			String year = DBKernel.getValue("Literatur", "ID", litID + "",
					"Jahr") + "";
			String title = DBKernel.getValue("Literatur", "ID", litID + "",
					"Titel") + "";
			String mAbstract = DBKernel.getValue("Literatur", "ID", litID + "",
					"Abstract") + "";

			literatureXML.add(new LiteratureItem(author,
					Integer.parseInt(year), title, mAbstract, litID));
		}

		if (agentID != DEFAULT_AGENTID) {
			String agentName = DBKernel.getValue("Agenzien", "ID",
					agentID + "", "Agensname") + "";

			agentXml.add(new AgentXml(agentID, agentName, null));
		} else {
			agentXml.add(new AgentXml(null, null, null));
		}

		if (matrixID != DEFAULT_MATRIXID) {
			String matrixName = DBKernel.getValue("Matrices", "ID", matrixID
					+ "", "Matrixname")
					+ "";

			matrixXml.add(new MatrixXml(matrixID, matrixName, null));
		} else {
			matrixXml.add(new MatrixXml(null, null, null));
		}

		for (int miscID : miscValues.keySet()) {
			String miscName = null;
			Double value = miscValues.get(miscID);

			if (miscID == AttributeUtilities.ATT_TEMPERATURE_ID) {
				miscName = AttributeUtilities.ATT_TEMPERATURE;
				value = AttributeUtilities.convertToStandardUnit(
						AttributeUtilities.ATT_TEMPERATURE, value, tempUnit);
			} else if (miscID == AttributeUtilities.ATT_PH_ID) {
				miscName = AttributeUtilities.ATT_PH;
			} else if (miscID == AttributeUtilities.ATT_AW_ID) {
				miscName = AttributeUtilities.ATT_WATERACTIVITY;
			} else {
				miscName = DBKernel.getValue("SonstigeParameter", "ID", miscID
						+ "", "Parameter")
						+ "";
			}

			miscXML.add(new MiscXml(miscID, miscName, null, miscValues
					.get(miscID), null));
		}

		for (Point2D.Double p : timeSeries) {
			timeSeriesXml.add(new TimeSeriesXml(null,
					AttributeUtilities.convertToStandardUnit(
							TimeSeriesSchema.TIME, p.x, timeUnit),
					AttributeUtilities.convertToStandardUnit(
							TimeSeriesSchema.LOGC, p.y, logcUnit)));
		}

		KnimeTuple tuple = new KnimeTuple(schema);

		tuple.setValue(TimeSeriesSchema.ATT_CONDID, id);
		tuple.setValue(TimeSeriesSchema.ATT_AGENT, agentXml);
		tuple.setValue(TimeSeriesSchema.ATT_MATRIX, matrixXml);
		tuple.setValue(TimeSeriesSchema.ATT_COMMENT, comment);
		tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES, timeSeriesXml);
		tuple.setValue(TimeSeriesSchema.ATT_MISC, miscXML);
		tuple.setValue(TimeSeriesSchema.ATT_LITMD, literatureXML);

		container.addRowToTable(tuple);
		exec.setProgress(1, "Adding row 0");
		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		return new DataTableSpec[] { schema.createSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(CFGKEY_LITERATUREIDS,
				XmlConverter.listToXml(literatureIDs));
		settings.addInt(CFGKEY_AGENTID, agentID);
		settings.addInt(CFGKEY_MATRIXID, matrixID);
		settings.addString(CFGKEY_COMMENT, comment);
		settings.addString(CFGKEY_TIMESERIES,
				XmlConverter.listToXml(timeSeries));
		settings.addString(CFGKEY_TIMEUNIT, timeUnit);
		settings.addString(CFGKEY_LOGCUNIT, logcUnit);
		settings.addString(CFGKEY_TEMPUNIT, tempUnit);
		settings.addString(CFGKEY_MISCVALUES, XmlConverter.mapToXml(miscValues));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		literatureIDs = XmlConverter.xmlToIntList(settings
				.getString(CFGKEY_LITERATUREIDS));
		agentID = settings.getInt(CFGKEY_AGENTID);
		matrixID = settings.getInt(CFGKEY_MATRIXID);
		comment = settings.getString(CFGKEY_COMMENT);
		timeSeries = XmlConverter.xmlToPointDoubleList(settings
				.getString(CFGKEY_TIMESERIES));
		timeUnit = settings.getString(CFGKEY_TIMEUNIT);
		logcUnit = settings.getString(CFGKEY_LOGCUNIT);
		tempUnit = settings.getString(CFGKEY_TEMPUNIT);
		miscValues = XmlConverter.xmlToIntDoubleMap(settings
				.getString(CFGKEY_MISCVALUES));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

}
