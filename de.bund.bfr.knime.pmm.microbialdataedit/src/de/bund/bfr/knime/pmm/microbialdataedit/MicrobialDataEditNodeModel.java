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
package de.bund.bfr.knime.pmm.microbialdataedit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MdInfoXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.XmlConverter;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeRelationReader;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * This is the model implementation of MicrobialDataEdit.
 * 
 * 
 * @author Christian Thoens
 */
public class MicrobialDataEditNodeModel extends NodeModel {

	protected static final String CFGKEY_ADDEDCONDITIONS = "AddedConditions";
	protected static final String CFGKEY_ADDEDCONDITIONVALUES = "AddedConditionValues";
	protected static final String CFGKEY_ADDEDCONDITIONUNITS = "AddedConditionUnits";
	protected static final String CFGKEY_CONDITIONS = "Conditions";
	protected static final String CFGKEY_CONDITIONVALUES = "ConditionValues";
	protected static final String CFGKEY_CONDITIONUNITS = "ConditionUnits";
	protected static final String CFGKEY_AGENTS = "Agents";
	protected static final String CFGKEY_AGENTDETAILS = "AgentDetails";
	protected static final String CFGKEY_MATRICES = "Matrices";
	protected static final String CFGKEY_MATRIXDETAILS = "MatrixDetails";
	protected static final String CFGKEY_COMMENTS = "Comments";
	protected static final String CFGKEY_QUALITYSCORES = "QualityScores";
	protected static final String CFGKEY_CHECKS = "Checks";
	protected static final String CFGKEY_TIMESERIES = "TimeSeries";

	private Map<Integer, MiscXml> addedConditions;
	private Map<Integer, Map<String, Double>> addedConditionValues;
	private Map<Integer, Map<String, String>> addedConditionUnits;
	private Map<Integer, MiscXml> conditions;
	private Map<Integer, Map<String, Double>> conditionValues;
	private Map<Integer, Map<String, String>> conditionUnits;
	private Map<String, AgentXml> agents;
	private Map<String, String> agentDetails;
	private Map<String, MatrixXml> matrices;
	private Map<String, String> matrixDetails;
	private Map<String, String> comments;
	private Map<String, Integer> qualityScores;
	private Map<String, Boolean> checks;
	private Map<String, List<TimeSeriesXml>> timeSeries;

	/**
	 * Constructor for the node model.
	 */
	protected MicrobialDataEditNodeModel() {
		super(1, 1);
		addedConditions = new LinkedHashMap<>();
		addedConditionValues = new LinkedHashMap<>();
		addedConditionUnits = new LinkedHashMap<>();
		conditions = new LinkedHashMap<>();
		conditionValues = new LinkedHashMap<>();
		conditionUnits = new LinkedHashMap<>();
		agents = new LinkedHashMap<>();
		agentDetails = new LinkedHashMap<>();
		matrices = new LinkedHashMap<>();
		matrixDetails = new LinkedHashMap<>();
		comments = new LinkedHashMap<>();
		qualityScores = new LinkedHashMap<>();
		checks = new LinkedHashMap<>();
		timeSeries = new LinkedHashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		KnimeRelationReader reader = new KnimeRelationReader(
				SchemaFactory.createDataSchema(), inData[0]);
		List<KnimeTuple> tuples = new ArrayList<>();

		while (reader.hasMoreElements()) {
			tuples.add(reader.nextElement());
		}

		BufferedDataContainer container = exec
				.createDataContainer(SchemaFactory.createDataSchema()
						.createSpec());

		for (KnimeTuple tuple : tuples) {
			String combaseID = tuple.getString(TimeSeriesSchema.ATT_COMBASEID);
			int condID = tuple.getInt(TimeSeriesSchema.ATT_CONDID);
			String id;

			if (combaseID != null) {
				id = combaseID + " (" + condID + ")";
			} else {
				id = condID + "";
			}

			if (agents.containsKey(id)) {
				PmmXmlDoc agentXml = new PmmXmlDoc();
				AgentXml agent = agents.get(id);

				if (agent != null) {
					agentXml.add(agent);
				} else {
					agentXml.add(new AgentXml());
				}

				tuple.setValue(TimeSeriesSchema.ATT_AGENT, agentXml);
			}

			if (agentDetails.containsKey(id)) {
				PmmXmlDoc agentXml = tuple
						.getPmmXml(TimeSeriesSchema.ATT_AGENT);

				((AgentXml) agentXml.get(0)).setDetail(agentDetails.get(id));

				tuple.setValue(TimeSeriesSchema.ATT_AGENT, agentXml);
			}

			if (matrices.containsKey(id)) {
				PmmXmlDoc matrixXml = new PmmXmlDoc();
				MatrixXml matrix = matrices.get(id);

				if (matrix != null) {
					matrixXml.add(matrix);
				} else {
					matrixXml.add(new MatrixXml());
				}

				tuple.setValue(TimeSeriesSchema.ATT_MATRIX, matrixXml);
			}

			if (matrixDetails.containsKey(id)) {
				PmmXmlDoc matrixXml = tuple
						.getPmmXml(TimeSeriesSchema.ATT_MATRIX);

				((MatrixXml) matrixXml.get(0)).setDetail(matrixDetails.get(id));

				tuple.setValue(TimeSeriesSchema.ATT_MATRIX, matrixXml);
			}

			if (comments.containsKey(id)) {
				PmmXmlDoc infoXml = tuple
						.getPmmXml(TimeSeriesSchema.ATT_MDINFO);

				((MdInfoXml) infoXml.get(0)).setComment(comments.get(id));

				tuple.setValue(TimeSeriesSchema.ATT_MDINFO, infoXml);
			}

			if (qualityScores.containsKey(id)) {
				PmmXmlDoc infoXml = tuple
						.getPmmXml(TimeSeriesSchema.ATT_MDINFO);

				((MdInfoXml) infoXml.get(0)).setQualityScore(qualityScores
						.get(id));

				tuple.setValue(TimeSeriesSchema.ATT_MDINFO, infoXml);
			}

			if (checks.containsKey(id)) {
				PmmXmlDoc infoXml = tuple
						.getPmmXml(TimeSeriesSchema.ATT_MDINFO);

				((MdInfoXml) infoXml.get(0)).setChecked(checks.get(id));

				tuple.setValue(TimeSeriesSchema.ATT_MDINFO, infoXml);
			}

			if (timeSeries.containsKey(id)) {
				PmmXmlDoc timeSeriesXml = new PmmXmlDoc();

				timeSeriesXml.getElementSet().addAll(timeSeries.get(id));
				tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES, timeSeriesXml);
			}

			PmmXmlDoc miscXml = new PmmXmlDoc();

			for (int miscID : conditions.keySet()) {
				MiscXml misc = new MiscXml(conditions.get(miscID));

				misc.setValue(conditionValues.get(miscID).get(id));
				misc.setUnit(conditionUnits.get(miscID).get(id));
				miscXml.add(misc);
			}

			for (int miscID : addedConditions.keySet()) {
				if (conditions.containsKey(miscID)) {
					continue;
				}

				MiscXml misc = new MiscXml(conditions.get(miscID));

				misc.setValue(addedConditionValues.get(miscID).get(id));
				misc.setUnit(addedConditionUnits.get(miscID).get(id));
				miscXml.add(misc);
			}

			tuple.setValue(TimeSeriesSchema.ATT_MISC, miscXml);
			container.addRowToTable(tuple);
		}

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
		if (!SchemaFactory.createDataSchema().conforms(inSpecs[0])) {
			throw new InvalidSettingsException("Wrong input");
		}

		return new DataTableSpec[] { SchemaFactory.createDataSchema()
				.createSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(CFGKEY_ADDEDCONDITIONS,
				XmlConverter.objectToXml(addedConditions));
		settings.addString(CFGKEY_ADDEDCONDITIONVALUES,
				XmlConverter.objectToXml(addedConditionValues));
		settings.addString(CFGKEY_ADDEDCONDITIONUNITS,
				XmlConverter.objectToXml(addedConditionUnits));
		settings.addString(CFGKEY_CONDITIONS,
				XmlConverter.objectToXml(conditions));
		settings.addString(CFGKEY_CONDITIONVALUES,
				XmlConverter.objectToXml(conditionValues));
		settings.addString(CFGKEY_CONDITIONUNITS,
				XmlConverter.objectToXml(conditionUnits));
		settings.addString(CFGKEY_AGENTS, XmlConverter.objectToXml(agents));
		settings.addString(CFGKEY_AGENTDETAILS,
				XmlConverter.objectToXml(agentDetails));
		settings.addString(CFGKEY_MATRICES, XmlConverter.objectToXml(matrices));
		settings.addString(CFGKEY_MATRIXDETAILS,
				XmlConverter.objectToXml(matrixDetails));
		settings.addString(CFGKEY_COMMENTS, XmlConverter.objectToXml(comments));
		settings.addString(CFGKEY_QUALITYSCORES,
				XmlConverter.objectToXml(qualityScores));
		settings.addString(CFGKEY_CHECKS, XmlConverter.objectToXml(checks));
		settings.addString(CFGKEY_TIMESERIES,
				XmlConverter.objectToXml(timeSeries));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		addedConditions = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_ADDEDCONDITIONS),
				new LinkedHashMap<Integer, MiscXml>());
		addedConditionValues = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_ADDEDCONDITIONVALUES),
				new LinkedHashMap<Integer, Map<String, Double>>());
		addedConditionUnits = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_ADDEDCONDITIONUNITS),
				new LinkedHashMap<Integer, Map<String, String>>());
		conditions = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_CONDITIONS),
				new LinkedHashMap<Integer, MiscXml>());
		conditionValues = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_CONDITIONVALUES),
				new LinkedHashMap<Integer, Map<String, Double>>());
		conditionUnits = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_CONDITIONUNITS),
				new LinkedHashMap<Integer, Map<String, String>>());
		agents = XmlConverter.xmlToObject(settings.getString(CFGKEY_AGENTS),
				new LinkedHashMap<String, AgentXml>());
		agentDetails = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_AGENTDETAILS),
				new LinkedHashMap<String, String>());
		matrices = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_MATRICES),
				new LinkedHashMap<String, MatrixXml>());
		matrixDetails = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_MATRIXDETAILS),
				new LinkedHashMap<String, String>());
		comments = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_COMMENTS),
				new LinkedHashMap<String, String>());
		qualityScores = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_QUALITYSCORES),
				new LinkedHashMap<String, Integer>());
		checks = XmlConverter.xmlToObject(settings.getString(CFGKEY_CHECKS),
				new LinkedHashMap<String, Boolean>());
		timeSeries = XmlConverter.xmlToObject(
				settings.getString(CFGKEY_TIMESERIES),
				new LinkedHashMap<String, List<TimeSeriesXml>>());
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
