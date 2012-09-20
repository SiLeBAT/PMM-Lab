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
package de.bund.bfr.knime.pmm.modelestimation;

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
import java.util.concurrent.atomic.AtomicInteger;

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
import org.nfunk.jep.ParseException;

import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeRelationReader;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.math.ParameterOptimizer;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * This is the model implementation of ModelEstimation.
 * 
 * 
 * @author Christian Thoens
 */
public class ModelEstimationNodeModel extends NodeModel {

	private static final int MAX_THREADS = 8;

	private KnimeSchema peiSchema;
	private KnimeSchema seiSchema;
	private KnimeSchema schema;

	private List<KnimeTuple> tuples;
	private AtomicInteger runningThreads;
	private AtomicInteger finishedThreads;

	/**
	 * Constructor for the node model.
	 */
	protected ModelEstimationNodeModel() {
		super(1, 1);

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
		BufferedDataTable table = inData[0];
		BufferedDataContainer container = exec.createDataContainer(schema
				.createSpec());
		KnimeRelationReader reader = new KnimeRelationReader(schema, table);
		int n = table.getRowCount();

		tuples = new ArrayList<KnimeTuple>(n);
		runningThreads = new AtomicInteger(0);
		finishedThreads = new AtomicInteger(0);

		if (schema == peiSchema) {
			for (int i = 0; i < n; i++) {
				tuples.add(reader.nextElement());
			}

			for (KnimeTuple tuple : tuples) {
				while (true) {
					exec.checkCanceled();
					exec.setProgress((double) finishedThreads.get()
							/ (double) n, "");

					if (runningThreads.get() < MAX_THREADS) {
						break;
					}

					Thread.sleep(100);
				}

				Thread thread = new Thread(new PrimaryEstimationThread(tuple));

				runningThreads.incrementAndGet();
				thread.start();
			}

			while (true) {
				exec.setProgress((double) finishedThreads.get() / (double) n,
						"");

				if (runningThreads.get() == 0) {
					break;
				}

				Thread.sleep(100);
			}

			for (KnimeTuple tuple : tuples) {
				tuple.setValue(Model1Schema.ATT_DATABASEWRITABLE,
						Model1Schema.WRITABLE);
				container.addRowToTable(tuple);
			}
		} else if (schema == seiSchema) {
			Map<String, List<Double>> depVarMap = new HashMap<String, List<Double>>();
			Map<String, List<Double>> temperatureMap = new HashMap<String, List<Double>>();
			Map<String, List<Double>> phMap = new HashMap<String, List<Double>>();
			Map<String, List<Double>> waterActivityMap = new HashMap<String, List<Double>>();
			Set<String> ids = new HashSet<String>();

			while (reader.hasMoreElements()) {
				KnimeTuple tuple = reader.nextElement();
				String id = tuple.getString(Model2Schema.ATT_DEPVAR);

				tuples.add(tuple);

				if (ids.add(id)) {
					depVarMap.put(id, new ArrayList<Double>());
					temperatureMap.put(id, new ArrayList<Double>());
					phMap.put(id, new ArrayList<Double>());
					waterActivityMap.put(id, new ArrayList<Double>());
				}

				List<String> keys = tuple
						.getStringList(Model1Schema.ATT_PARAMNAME);
				List<Double> values = tuple
						.getDoubleList(Model1Schema.ATT_VALUE);
				List<Double> minValues = tuple
						.getDoubleList(Model1Schema.ATT_MINVALUE);
				List<Double> maxValues = tuple
						.getDoubleList(Model1Schema.ATT_MAXVALUE);

				if (values.contains(null)) {
					continue;
				}

				double value = values.get(keys.indexOf(tuple
						.getString(Model2Schema.ATT_DEPVAR)));
				Double minValue = minValues.get(keys.indexOf(tuple
						.getString(Model2Schema.ATT_DEPVAR)));
				Double maxValue = maxValues.get(keys.indexOf(tuple
						.getString(Model2Schema.ATT_DEPVAR)));

				if ((minValue != null && value < minValue)
						|| (maxValue != null && value > maxValue)) {
					setWarningMessage("Some primary parameters are out of their range of values");
				}

				depVarMap.get(id).add(value);
				temperatureMap.get(id).add(
						tuple.getDouble(TimeSeriesSchema.ATT_TEMPERATURE));
				phMap.get(id).add(tuple.getDouble(TimeSeriesSchema.ATT_PH));
				waterActivityMap.get(id).add(
						tuple.getDouble(TimeSeriesSchema.ATT_WATERACTIVITY));
			}

			Map<String, List<Double>> paramValueMap = new HashMap<String, List<Double>>();
			Map<String, List<Double>> paramErrorMap = new HashMap<String, List<Double>>();
			Map<String, Double> errorMap = new HashMap<String, Double>();
			Map<String, Double> rSquaredMap = new HashMap<String, Double>();
			Map<String, List<Double>> minIndepMap = new HashMap<String, List<Double>>();
			Map<String, List<Double>> maxIndepMap = new HashMap<String, List<Double>>();
			Map<String, Integer> estIDMap = new HashMap<String, Integer>();

			for (int i = 0; i < n; i++) {
				KnimeTuple tuple = tuples.get(i);
				String id = tuple.getString(Model2Schema.ATT_DEPVAR);

				tuple.setValue(Model2Schema.ATT_DATABASEWRITABLE,
						Model2Schema.WRITABLE);

				if (paramValueMap.containsKey(id)) {
					tuple.setValue(Model2Schema.ATT_VALUE,
							paramValueMap.get(id));
					tuple.setValue(Model2Schema.ATT_RMS, errorMap.get(id));
					tuple.setValue(Model2Schema.ATT_RSQUARED,
							rSquaredMap.get(id));
					tuple.setValue(Model2Schema.ATT_PARAMERR,
							paramErrorMap.get(id));
					tuple.setValue(Model2Schema.ATT_MININDEP,
							minIndepMap.get(id));
					tuple.setValue(Model2Schema.ATT_MAXINDEP,
							maxIndepMap.get(id));
					tuple.setValue(Model2Schema.ATT_ESTMODELID,
							estIDMap.get(id));
				} else {
					String formula = tuple.getString(Model2Schema.ATT_FORMULA);
					List<String> parameters = tuple
							.getStringList(Model2Schema.ATT_PARAMNAME);
					List<Double> minParameterValues = tuple
							.getDoubleList(Model2Schema.ATT_MINVALUE);
					List<Double> maxParameterValues = tuple
							.getDoubleList(Model2Schema.ATT_MAXVALUE);
					List<Double> targetValues = depVarMap.get(id);
					List<String> arguments = tuple
							.getStringList(Model2Schema.ATT_INDEPVAR);
					List<List<Double>> argumentValues = new ArrayList<List<Double>>();

					for (String arg : arguments) {
						if (arg.equals(TimeSeriesSchema.ATT_TEMPERATURE)) {
							argumentValues.add(temperatureMap.get(id));
						} else if (arg.equals(TimeSeriesSchema.ATT_PH)) {
							argumentValues.add(phMap.get(id));
						} else if (arg
								.equals(TimeSeriesSchema.ATT_WATERACTIVITY)) {
							argumentValues.add(waterActivityMap.get(id));
						}
					}

					MathUtilities
							.removeNullValues(targetValues, argumentValues);

					List<Double> parameterValues;
					List<Double> parameterErrors;
					Double error;
					Double rSquared;
					Integer estID;
					ParameterOptimizer optimizer = new ParameterOptimizer(
							formula, parameters, minParameterValues,
							maxParameterValues, targetValues, arguments,
							argumentValues);

					optimizer.optimize();

					if (optimizer.isSuccessful()) {
						parameterValues = optimizer.getParameterValues();
						parameterErrors = optimizer
								.getParameterStandardErrors();
						error = optimizer.getStandardError();
						rSquared = optimizer.getRSquare();
						estID = MathUtilities.getRandomNegativeInt();
					} else {
						parameterValues = Collections.nCopies(
								parameters.size(), null);
						parameterErrors = Collections.nCopies(
								parameters.size(), null);
						error = null;
						rSquared = null;
						estID = null;
					}

					List<Double> minValues = new ArrayList<Double>();
					List<Double> maxValues = new ArrayList<Double>();

					for (List<Double> values : argumentValues) {
						minValues.add(Collections.min(values));
						maxValues.add(Collections.max(values));
					}

					tuple.setValue(Model2Schema.ATT_VALUE, parameterValues);
					tuple.setValue(Model2Schema.ATT_RMS, error);
					tuple.setValue(Model2Schema.ATT_RSQUARED, rSquared);
					tuple.setValue(Model2Schema.ATT_PARAMERR, parameterErrors);
					tuple.setValue(Model2Schema.ATT_MININDEP, minValues);
					tuple.setValue(Model2Schema.ATT_MAXINDEP, maxValues);
					tuple.setValue(Model2Schema.ATT_ESTMODELID, estID);
					paramValueMap.put(id, parameterValues);
					errorMap.put(id, error);
					rSquaredMap.put(id, rSquared);
					paramErrorMap.put(id, parameterErrors);
					minIndepMap.put(id, minValues);
					maxIndepMap.put(id, maxValues);
					estIDMap.put(id, estID);
				}

				container.addRowToTable(tuple);
				exec.checkCanceled();
				exec.setProgress((double) i / (double) n, "");
			}
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
		try {
			if (seiSchema.conforms((DataTableSpec) inSpecs[0])) {
				schema = seiSchema;
			} else if (peiSchema.conforms((DataTableSpec) inSpecs[0])) {
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
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

	private class PrimaryEstimationThread implements Runnable {

		private KnimeTuple tuple;

		public PrimaryEstimationThread(KnimeTuple tuple) {
			this.tuple = tuple;
		}

		@Override
		public void run() {
			try {
				String formula = tuple.getString(Model1Schema.ATT_FORMULA);
				List<String> parameters = tuple
						.getStringList(Model1Schema.ATT_PARAMNAME);
				List<Double> minParameterValues = tuple
						.getDoubleList(Model1Schema.ATT_MINVALUE);
				List<Double> maxParameterValues = tuple
						.getDoubleList(Model1Schema.ATT_MAXVALUE);
				List<Double> targetValues = tuple
						.getDoubleList(TimeSeriesSchema.ATT_LOGC);
				List<Double> timeValues = tuple
						.getDoubleList(TimeSeriesSchema.ATT_TIME);
				List<String> arguments = Arrays
						.asList(TimeSeriesSchema.ATT_TIME);
				List<List<Double>> argumentValues = new ArrayList<List<Double>>();
				List<Double> parameterValues;
				List<Double> parameterErrors;
				Double error;
				Double rSquare;
				Integer estID;
				boolean successful = false;
				ParameterOptimizer optimizer = null;

				if (targetValues != null && timeValues != null) {
					argumentValues.add(timeValues);
					MathUtilities
							.removeNullValues(targetValues, argumentValues);

					optimizer = new ParameterOptimizer(formula, parameters,
							minParameterValues, maxParameterValues,
							targetValues, arguments, argumentValues);
					optimizer.optimize();
					successful = optimizer.isSuccessful();
				}

				if (successful) {
					parameterValues = optimizer.getParameterValues();
					parameterErrors = optimizer.getParameterStandardErrors();
					error = optimizer.getStandardError();
					rSquare = optimizer.getRSquare();
					estID = MathUtilities.getRandomNegativeInt();
				} else {
					parameterValues = Collections.nCopies(parameters.size(),
							null);
					parameterErrors = Collections.nCopies(parameters.size(),
							null);
					error = null;
					rSquare = null;
					estID = null;
				}

				tuple.setValue(Model1Schema.ATT_VALUE, parameterValues);
				tuple.setValue(Model1Schema.ATT_RMS, error);
				tuple.setValue(Model1Schema.ATT_RSQUARED, rSquare);
				tuple.setValue(Model1Schema.ATT_PARAMERR, parameterErrors);
				tuple.setValue(Model1Schema.ATT_MININDEP,
						Arrays.asList(Collections.min(argumentValues.get(0))));
				tuple.setValue(Model1Schema.ATT_MAXINDEP,
						Arrays.asList(Collections.max(argumentValues.get(0))));
				tuple.setValue(Model1Schema.ATT_ESTMODELID, estID);
				runningThreads.decrementAndGet();
				finishedThreads.incrementAndGet();
			} catch (PmmException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

}
