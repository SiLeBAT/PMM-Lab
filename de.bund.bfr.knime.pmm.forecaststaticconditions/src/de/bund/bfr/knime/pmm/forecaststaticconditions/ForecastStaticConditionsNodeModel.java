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
package de.bund.bfr.knime.pmm.forecaststaticconditions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;

import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.combine.ModelCombiner;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeRelationReader;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * This is the model implementation of ForecastStaticConditions.
 * 
 * 
 * @author Christian Thoens
 */
public class ForecastStaticConditionsNodeModel extends NodeModel {

	static final String CFGKEY_CONCENTRATION = "Concentration";
	static final String CFGKEY_CONCENTRATIONPARAMETERS = "ConcentrationParameters";

	static final double DEFAULT_CONCENTRATION = 3.0;

	private double concentration;
	private List<String> concentrationParameters;

	private KnimeSchema peiSchema;
	private KnimeSchema seiSchema;
	private KnimeSchema schema;

	/**
	 * Constructor for the node model.
	 */
	protected ForecastStaticConditionsNodeModel() {
		super(1, 1);
		concentration = DEFAULT_CONCENTRATION;
		concentrationParameters = new ArrayList<String>();

		try {
			peiSchema = new KnimeSchema(new Model1Schema(),
					new TimeSeriesSchema());
			seiSchema = new KnimeSchema(new KnimeSchema(new Model1Schema(),
					new Model2Schema()), new TimeSeriesSchema());
		} catch (PmmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable output = null;

		if (schema == seiSchema) {
			output = performSecondaryForecast(inData[0], exec);
		} else if (schema == peiSchema) {
			output = performPrimaryForecast(inData[0], exec);
		}

		return new BufferedDataTable[] { output };
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
		try {
			if (seiSchema.conforms(inSpecs[0])) {
				schema = seiSchema;
			} else if (peiSchema.conforms(inSpecs[0])) {
				schema = peiSchema;
			} else {
				throw new InvalidSettingsException("Wrong input!");
			}

			return new DataTableSpec[] { schema.createSpec() };
		} catch (PmmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addDouble(CFGKEY_CONCENTRATION, concentration);

		if (concentrationParameters != null) {
			settings.addStringArray(CFGKEY_CONCENTRATIONPARAMETERS,
					concentrationParameters.toArray(new String[0]));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			concentration = settings.getDouble(CFGKEY_CONCENTRATION);
		} catch (InvalidSettingsException e) {
			concentration = DEFAULT_CONCENTRATION;
		}

		try {
			concentrationParameters = new ArrayList<String>(
					Arrays.asList(settings
							.getStringArray(CFGKEY_CONCENTRATIONPARAMETERS)));
		} catch (InvalidSettingsException e) {
			concentrationParameters = new ArrayList<String>();
		}
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

	private BufferedDataTable performSecondaryForecast(BufferedDataTable table,
			ExecutionContext exec) throws Exception {
		KnimeRelationReader reader = new KnimeRelationReader(schema, table);
		List<KnimeTuple> tuples = new ArrayList<KnimeTuple>();
		Map<String, String> paramMap = new HashMap<String, String>();

		for (String assign : concentrationParameters) {
			int i = assign.indexOf(":");
			String id = assign.substring(0, i);
			String param = assign.substring(i + 1);

			paramMap.put(id, param);
		}

		while (reader.hasMoreElements()) {
			tuples.add(reader.nextElement());
		}

		Map<KnimeTuple, List<KnimeTuple>> combinedTuples = ModelCombiner
				.combine(tuples, schema, true, paramMap);
		Set<String> idSet = new HashSet<String>();
		BufferedDataContainer container = exec.createDataContainer(schema
				.createSpec());
		int index = 0;

		for (KnimeTuple newTuple : combinedTuples.keySet()) {
			String id = newTuple.getInt(Model1Schema.ATT_MODELID) + "";
			String oldID = combinedTuples.get(newTuple).get(0)
					.getInt(Model1Schema.ATT_MODELID)
					+ "";

			if (!idSet.add(id)) {
				continue;
			}

			List<Double> times = newTuple
					.getDoubleList(TimeSeriesSchema.ATT_TIME);
			List<Double> logcs;

			if (paramMap.containsKey(oldID)) {
				Double temp = newTuple
						.getDouble(TimeSeriesSchema.ATT_TEMPERATURE);
				Double ph = newTuple.getDouble(TimeSeriesSchema.ATT_PH);
				Double aw = newTuple
						.getDouble(TimeSeriesSchema.ATT_WATERACTIVITY);
				String formula = newTuple.getString(Model1Schema.ATT_FORMULA);
				List<String> params = newTuple
						.getStringList(Model1Schema.ATT_PARAMNAME);
				List<Double> values = newTuple
						.getDoubleList(Model1Schema.ATT_VALUE);
				Map<String, Double> constants = new HashMap<String, Double>();
				String initialParameter = paramMap.get(oldID);

				values.set(params.indexOf(initialParameter), concentration);
				checkPrimaryModel(combinedTuples.get(newTuple).get(0),
						initialParameter);
				checkSecondaryModels(combinedTuples.get(newTuple));

				for (int i = 0; i < params.size(); i++) {
					constants.put(params.get(i), values.get(i));
				}

				constants.put(TimeSeriesSchema.ATT_PH, ph);
				constants.put(TimeSeriesSchema.ATT_WATERACTIVITY, aw);
				constants.put(TimeSeriesSchema.ATT_TEMPERATURE, temp);
				logcs = new ArrayList<Double>();

				for (double t : times) {
					constants.put(TimeSeriesSchema.ATT_TIME, t);
					logcs.add(computeLogc(formula, constants));
				}
			} else {
				logcs = Collections.nCopies(times.size(), null);
			}

			for (KnimeTuple tuple : combinedTuples.get(newTuple)) {
				List<String> params = tuple
						.getStringList(Model1Schema.ATT_PARAMNAME);
				List<Double> values = tuple
						.getDoubleList(Model1Schema.ATT_VALUE);

				if (paramMap.containsKey(oldID)) {
					values.set(params.indexOf(paramMap.get(oldID)),
							concentration);
				} else {
					values = Collections.nCopies(values.size(), null);
				}

				tuple.setValue(TimeSeriesSchema.ATT_LOGC, logcs);
				tuple.setValue(Model1Schema.ATT_VALUE, values);
				container.addRowToTable(tuple);
			}

			exec.setProgress((double) index / (double) combinedTuples.size(),
					"");
			exec.checkCanceled();
			index++;
		}

		container.close();

		return container.getTable();
	}

	private BufferedDataTable performPrimaryForecast(BufferedDataTable table,
			ExecutionContext exec) throws Exception {
		KnimeRelationReader reader = new KnimeRelationReader(schema, table);
		List<KnimeTuple> tuples = new ArrayList<KnimeTuple>();
		Map<String, String> paramMap = new HashMap<String, String>();

		for (String assign : concentrationParameters) {
			int i = assign.indexOf(":");
			String id = assign.substring(0, i);
			String param = assign.substring(i + 1);

			paramMap.put(id, param);
		}

		while (reader.hasMoreElements()) {
			tuples.add(reader.nextElement());
		}

		BufferedDataContainer container = exec.createDataContainer(schema
				.createSpec());
		int index = 0;

		for (KnimeTuple tuple : tuples) {
			String id = tuple.getInt(Model1Schema.ATT_MODELID) + "";
			List<Double> values = tuple.getDoubleList(Model1Schema.ATT_VALUE);
			List<Double> times = tuple.getDoubleList(TimeSeriesSchema.ATT_TIME);
			List<Double> logcs;

			if (paramMap.containsKey(id)) {
				Double temp = tuple.getDouble(TimeSeriesSchema.ATT_TEMPERATURE);
				Double ph = tuple.getDouble(TimeSeriesSchema.ATT_PH);
				Double aw = tuple.getDouble(TimeSeriesSchema.ATT_WATERACTIVITY);
				String formula = tuple.getString(Model1Schema.ATT_FORMULA);
				List<String> params = tuple
						.getStringList(Model1Schema.ATT_PARAMNAME);

				Map<String, Double> constants = new HashMap<String, Double>();

				values.set(params.indexOf(paramMap.get(id)), concentration);

				for (int i = 0; i < params.size(); i++) {
					constants.put(params.get(i), values.get(i));
				}

				constants.put(TimeSeriesSchema.ATT_PH, ph);
				constants.put(TimeSeriesSchema.ATT_WATERACTIVITY, aw);
				constants.put(TimeSeriesSchema.ATT_TEMPERATURE, temp);
				logcs = new ArrayList<Double>();

				for (double t : times) {
					constants.put(TimeSeriesSchema.ATT_TIME, t);
					logcs.add(computeLogc(formula, constants));
				}
			} else {
				logcs = Collections.nCopies(times.size(), null);
			}

			tuple.setValue(Model1Schema.ATT_VALUE, values);
			tuple.setValue(TimeSeriesSchema.ATT_LOGC, logcs);
			container.addRowToTable(tuple);
			exec.setProgress((double) index / (double) tuples.size(), "");
			exec.checkCanceled();
			index++;
		}

		container.close();

		return container.getTable();
	}

	private Double computeLogc(String formula, Map<String, Double> constants) {
		DJep parser = MathUtilities.createParser();

		for (String constant : constants.keySet()) {
			Double value = constants.get(constant);

			if (value != null) {
				parser.addConstant(constant, value);
			}
		}

		try {
			Node f = parser.parse(formula.replaceAll(TimeSeriesSchema.ATT_LOGC
					+ "=", ""));
			double value = (Double) parser.evaluate(f);

			return value;
		} catch (Exception e) {
			return null;
		}
	}

	private void checkPrimaryModel(KnimeTuple tuple, String initialParameter)
			throws PmmException {
		String modelName = tuple.getString(Model1Schema.ATT_MODELNAME);
		List<String> params = tuple.getStringList(Model1Schema.ATT_PARAMNAME);
		List<Double> values = tuple.getDoubleList(Model1Schema.ATT_VALUE);

		for (int i = 0; i < params.size(); i++) {
			if (!params.get(i).equals(initialParameter)
					&& values.get(i) == null) {
				setWarningMessage(params.get(i) + " in " + modelName
						+ " is not specified");
			}
		}
	}

	private void checkSecondaryModels(List<KnimeTuple> tuples)
			throws PmmException {
		for (KnimeTuple tuple : tuples) {
			String depVar = tuple.getString(Model2Schema.ATT_DEPVAR);
			List<String> params = tuple
					.getStringList(Model2Schema.ATT_PARAMNAME);
			List<Double> values = tuple.getDoubleList(Model2Schema.ATT_VALUE);

			for (int i = 0; i < params.size(); i++) {
				if (values.get(i) == null) {
					setWarningMessage(params.get(i) + " in " + depVar
							+ "-model is not specified");
				}
			}
		}
	}

}
