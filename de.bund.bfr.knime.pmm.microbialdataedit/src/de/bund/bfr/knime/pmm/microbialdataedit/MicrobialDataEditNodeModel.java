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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
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
	protected static final String CFGKEY_AGENTS = "Agents";
	protected static final String CFGKEY_MATRICES = "Matrices";
	protected static final String CFGKEY_COMMENTS = "Comments";
	protected static final String CFGKEY_QUALITYSCORES = "QualityScores";
	protected static final String CFGKEY_CHECKS = "Checks";

	private Map<MiscXml, Map<String, Double>> addedConditions;
	private Map<String, AgentXml> agents;
	private Map<String, MatrixXml> matrices;
	private Map<String, String> comments;
	private Map<String, Integer> qualityScores;
	private Map<String, Boolean> checks;

	/**
	 * Constructor for the node model.
	 */
	protected MicrobialDataEditNodeModel() {
		super(1, 1);
		addedConditions = new LinkedHashMap<>();
		agents = new LinkedHashMap<>();
		matrices = new LinkedHashMap<>();
		comments = new LinkedHashMap<>();
		qualityScores = new LinkedHashMap<>();
		checks = new LinkedHashMap<>();
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
					agentXml.add(new AgentXml(null, null, null));
				}

				tuple.setValue(TimeSeriesSchema.ATT_AGENT, agentXml);
			}

			if (matrices.containsKey(id)) {
				PmmXmlDoc matrixXml = new PmmXmlDoc();
				MatrixXml matrix = matrices.get(id);

				if (matrix != null) {
					matrixXml.add(matrix);
				} else {
					matrixXml.add(new MatrixXml(null, null, null));
				}

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

			PmmXmlDoc miscXml = tuple.getPmmXml(TimeSeriesSchema.ATT_MISC);
			Set<Integer> usedMiscIDs = new LinkedHashSet<>();

			for (PmmXmlElementConvertable el : miscXml.getElementSet()) {
				usedMiscIDs.add(((MiscXml) el).getID());
			}

			for (MiscXml misc : addedConditions.keySet()) {
				if (usedMiscIDs.contains(misc.getID())) {
					continue;
				}

				miscXml.add(new MiscXml(misc.getID(), misc.getName(), null,
						addedConditions.get(misc).get(id), null, misc
								.getDbuuid()));
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
				XmlConverter.mapToXml(addedConditions));
		settings.addString(CFGKEY_AGENTS, XmlConverter.mapToXml(agents));
		settings.addString(CFGKEY_MATRICES, XmlConverter.mapToXml(matrices));
		settings.addString(CFGKEY_COMMENTS, XmlConverter.mapToXml(comments));
		settings.addString(CFGKEY_QUALITYSCORES,
				XmlConverter.mapToXml(qualityScores));
		settings.addString(CFGKEY_CHECKS, XmlConverter.mapToXml(checks));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		addedConditions = XmlConverter.xmlToMiscStringDoubleMap(settings
				.getString(CFGKEY_ADDEDCONDITIONS));
		agents = XmlConverter.xmlToAgentMap(settings.getString(CFGKEY_AGENTS));
		matrices = XmlConverter.xmlToMatrixMap(settings
				.getString(CFGKEY_MATRICES));
		comments = XmlConverter.xmlToStringMap(settings
				.getString(CFGKEY_COMMENTS));
		qualityScores = XmlConverter.xmlToIntMap(settings
				.getString(CFGKEY_QUALITYSCORES));
		checks = XmlConverter.xmlToBoolMap(settings.getString(CFGKEY_CHECKS));
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
