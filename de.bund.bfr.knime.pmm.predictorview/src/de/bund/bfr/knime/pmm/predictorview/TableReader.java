package de.bund.bfr.knime.pmm.predictorview;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bund.bfr.knime.pmm.common.AgentXml;
import de.bund.bfr.knime.pmm.common.CatalogModelXml;
import de.bund.bfr.knime.pmm.common.DepXml;
import de.bund.bfr.knime.pmm.common.EstModelXml;
import de.bund.bfr.knime.pmm.common.IndepXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.ModelCombiner;
import de.bund.bfr.knime.pmm.common.ParamXml;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.QualityMeasurementComputation;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.chart.ChartConstants;
import de.bund.bfr.knime.pmm.common.chart.ChartSelectionPanel;
import de.bund.bfr.knime.pmm.common.chart.Plotable;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.PmmUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.knime.pmm.common.units.Categories;

public class TableReader {

	private static final String IDENTIFIER = "Identifier";

	private List<String> ids;
	private Map<String, KnimeTuple> tupleMap;
	private List<String> stringColumns;
	private List<List<String>> stringColumnValues;
	private List<String> doubleColumns;
	private List<List<Double>> doubleColumnValues;
	private List<String> formulas;
	private List<Map<String, Double>> parameterData;
	private List<String> conditions;
	private List<List<Double>> conditionValues;
	private List<List<Double>> conditionMinValues;
	private List<List<Double>> conditionMaxValues;
	private List<List<String>> conditionUnits;
	private List<String> standardVisibleColumns;
	private List<String> filterableStringColumns;
	private Map<String, String> newInitParams;
	private Map<String, String> newLagParams;
	private Map<KnimeTuple, List<KnimeTuple>> combinedTuples;

	private Map<String, Plotable> plotables;
	private Map<String, String> shortLegend;
	private Map<String, String> longLegend;
	private Map<String, String> shortIds;

	private Map<String, String> tempParam;
	private Map<String, String> phParam;
	private Map<String, String> awParam;

	public TableReader(List<KnimeTuple> tuples, Map<String, String> initParams,
			Map<String, String> lagParams, boolean defaultBehaviour) {
		Set<String> idSet = new LinkedHashSet<String>();
		boolean isTertiaryModel = tuples.get(0).getSchema()
				.conforms(SchemaFactory.createM12Schema());
		boolean containsData = tuples.get(0).getSchema()
				.conforms(SchemaFactory.createDataSchema());
		List<String> miscParams = null;
		Map<KnimeTuple, List<KnimeTuple>> combined = new LinkedHashMap<KnimeTuple, List<KnimeTuple>>();
		List<KnimeTuple> rawTuples = tuples;

		newInitParams = new LinkedHashMap<String, String>();
		newLagParams = new LinkedHashMap<String, String>();

		if (isTertiaryModel) {
			combined = new ModelCombiner(tuples, containsData, initParams,
					lagParams).getTupleCombinations();

			tuples = new ArrayList<KnimeTuple>(combined.keySet());

			try {
				List<KnimeTuple> newTuples = QualityMeasurementComputation
						.computePrimary(tuples, false);

				for (int i = 0; i < tuples.size(); i++) {
					combined.put(newTuples.get(i), combined.get(tuples.get(i)));
					combined.remove(tuples.get(i));
				}

				tuples = newTuples;
			} catch (Exception e) {
			}

			if (!defaultBehaviour) {
				combinedTuples = new LinkedHashMap<KnimeTuple, List<KnimeTuple>>();

				for (KnimeTuple t1 : combined.keySet()) {
					combinedTuples.put(t1, new ArrayList<KnimeTuple>());

					for (KnimeTuple t2 : combined.get(t1)) {
						combinedTuples.get(t1).addAll(
								getAllDataTuples(t2, rawTuples));
					}
				}

				for (KnimeTuple tuple : tuples) {
					List<KnimeTuple> usedTuples = combinedTuples.get(tuple);

					if (!usedTuples.isEmpty()) {
						String oldID = ((CatalogModelXml) usedTuples.get(0)
								.getPmmXml(Model1Schema.ATT_MODELCATALOG)
								.get(0)).getId()
								+ "";
						String newID = ((CatalogModelXml) tuple.getPmmXml(
								Model1Schema.ATT_MODELCATALOG).get(0)).getId()
								+ "";

						if (initParams.containsKey(oldID)) {
							newInitParams.put(newID, initParams.get(oldID));
						}

						if (lagParams.containsKey(oldID)) {
							newLagParams.put(newID, lagParams.get(oldID));
						}
					}
				}
			}
		} else {
			newInitParams.putAll(initParams);
			newLagParams.putAll(lagParams);

			if (!tuples.isEmpty()) {
				if (tuples.get(0).getPmmXml(Model1Schema.ATT_INDEPENDENT)
						.size() > 1) {
					containsData = false;
				}
			}
		}

		ids = new ArrayList<String>();
		tupleMap = new LinkedHashMap<String, KnimeTuple>();
		plotables = new LinkedHashMap<String, Plotable>();
		shortLegend = new LinkedHashMap<String, String>();
		longLegend = new LinkedHashMap<String, String>();
		shortIds = new LinkedHashMap<String, String>();
		tempParam = new LinkedHashMap<String, String>();
		phParam = new LinkedHashMap<String, String>();
		awParam = new LinkedHashMap<String, String>();
		formulas = new ArrayList<String>();
		parameterData = new ArrayList<Map<String, Double>>();
		doubleColumns = Arrays.asList(Model1Schema.SSE, Model1Schema.MSE,
				Model1Schema.RMSE, Model1Schema.RSQUARED, Model1Schema.AIC);
		doubleColumnValues = new ArrayList<List<Double>>();
		doubleColumnValues.add(new ArrayList<Double>());
		doubleColumnValues.add(new ArrayList<Double>());
		doubleColumnValues.add(new ArrayList<Double>());
		doubleColumnValues.add(new ArrayList<Double>());
		doubleColumnValues.add(new ArrayList<Double>());
		conditions = null;
		conditionValues = null;
		conditionMinValues = null;
		conditionMaxValues = null;
		conditionUnits = null;

		if (isTertiaryModel) {
			stringColumns = Arrays.asList(IDENTIFIER, ChartConstants.STATUS,
					Model1Schema.FORMULA, Model1Schema.ATT_EMLIT,
					Model1Schema.FITTEDMODELNAME, Model2Schema.FORMULA,
					TimeSeriesSchema.ATT_AGENT, TimeSeriesSchema.ATT_MATRIX);
			stringColumnValues = new ArrayList<List<String>>();
			stringColumnValues.add(new ArrayList<String>());
			stringColumnValues.add(new ArrayList<String>());
			stringColumnValues.add(new ArrayList<String>());
			stringColumnValues.add(new ArrayList<String>());
			stringColumnValues.add(new ArrayList<String>());
			stringColumnValues.add(new ArrayList<String>());
			stringColumnValues.add(new ArrayList<String>());
			stringColumnValues.add(new ArrayList<String>());
			standardVisibleColumns = new ArrayList<String>(
					Arrays.asList(ChartSelectionPanel.FORMULA,
							ChartSelectionPanel.PARAMETERS));
			standardVisibleColumns.addAll(stringColumns);
			standardVisibleColumns.addAll(doubleColumns);
			filterableStringColumns = Arrays.asList(ChartConstants.STATUS);

			miscParams = PmmUtilities.getIndeps(tuples);
			miscParams.remove(AttributeUtilities.TIME);
			conditions = new ArrayList<String>();
			conditionMinValues = new ArrayList<List<Double>>();
			conditionMaxValues = new ArrayList<List<Double>>();
			conditionUnits = new ArrayList<List<String>>();

			for (String param : miscParams) {
				conditions.add(param);
				conditionMinValues.add(new ArrayList<Double>());
				conditionMaxValues.add(new ArrayList<Double>());
				conditionUnits.add(new ArrayList<String>());
				standardVisibleColumns.add(param);
			}
		} else {
			if (containsData) {
				stringColumns = Arrays.asList(IDENTIFIER,
						ChartConstants.STATUS, Model1Schema.FORMULA,
						Model1Schema.ATT_EMLIT, Model1Schema.FITTEDMODELNAME,
						AttributeUtilities.DATAID);
				stringColumnValues = new ArrayList<List<String>>();
				stringColumnValues.add(new ArrayList<String>());
				stringColumnValues.add(new ArrayList<String>());
				stringColumnValues.add(new ArrayList<String>());
				stringColumnValues.add(new ArrayList<String>());
				stringColumnValues.add(new ArrayList<String>());
				stringColumnValues.add(new ArrayList<String>());
				standardVisibleColumns = new ArrayList<String>(Arrays.asList(
						ChartSelectionPanel.FORMULA,
						ChartSelectionPanel.PARAMETERS));
				standardVisibleColumns.addAll(stringColumns);
				standardVisibleColumns.addAll(doubleColumns);
				filterableStringColumns = Arrays.asList(ChartConstants.STATUS);

				miscParams = PmmUtilities.getMiscParams(tuples);
				conditions = new ArrayList<String>();
				conditionValues = new ArrayList<List<Double>>();
				conditionUnits = new ArrayList<List<String>>();

				for (String param : miscParams) {
					conditions.add(param);
					conditionValues.add(new ArrayList<Double>());
					conditionUnits.add(new ArrayList<String>());
					standardVisibleColumns.add(param);
				}
			} else {
				stringColumns = Arrays.asList(IDENTIFIER,
						ChartConstants.STATUS, Model1Schema.FORMULA,
						Model1Schema.ATT_EMLIT, Model1Schema.FITTEDMODELNAME);
				stringColumnValues = new ArrayList<List<String>>();
				stringColumnValues.add(new ArrayList<String>());
				stringColumnValues.add(new ArrayList<String>());
				stringColumnValues.add(new ArrayList<String>());
				stringColumnValues.add(new ArrayList<String>());
				stringColumnValues.add(new ArrayList<String>());
				standardVisibleColumns = new ArrayList<String>(Arrays.asList(
						ChartSelectionPanel.FORMULA,
						ChartSelectionPanel.PARAMETERS));
				standardVisibleColumns.addAll(stringColumns);
				standardVisibleColumns.addAll(doubleColumns);
				filterableStringColumns = Arrays.asList(Model1Schema.FORMULA,
						ChartConstants.STATUS);
			}
		}

		Map<String, List<KnimeTuple>> dataTuples = new LinkedHashMap<String, List<KnimeTuple>>();

		if (isTertiaryModel && containsData) {
			for (KnimeTuple tuple : tuples) {
				String id = ((EstModelXml) tuple.getPmmXml(
						Model1Schema.ATT_ESTMODEL).get(0)).getId()
						+ "";

				if (!dataTuples.containsKey(id)) {
					dataTuples.put(id, new ArrayList<KnimeTuple>());
				}

				dataTuples.get(id).add(tuple);
			}
		}

		int index = 1;

		for (KnimeTuple tuple : tuples) {
			String id = ((EstModelXml) tuple.getPmmXml(
					Model1Schema.ATT_ESTMODEL).get(0)).getId()
					+ "";

			if (!isTertiaryModel && containsData) {
				id += "(" + tuple.getInt(TimeSeriesSchema.ATT_CONDID) + ")";
			}

			if (!idSet.add(id)) {
				continue;
			}

			String primId;

			if (isTertiaryModel) {
				primId = ((CatalogModelXml) combined.get(tuple).get(0)
						.getPmmXml(Model1Schema.ATT_MODELCATALOG).get(0))
						.getId() + "";
			} else {
				primId = ((CatalogModelXml) tuple.getPmmXml(
						Model1Schema.ATT_MODELCATALOG).get(0)).getId()
						+ "";
			}

			ids.add(id);
			tupleMap.put(id, tuple);

			CatalogModelXml modelXml = (CatalogModelXml) tuple.getPmmXml(
					Model1Schema.ATT_MODELCATALOG).get(0);
			DepXml depXml = (DepXml) tuple
					.getPmmXml(Model1Schema.ATT_DEPENDENT).get(0);
			String modelName = modelXml.getName();
			String formula = MathUtilities.getAllButBoundaryCondition(modelXml
					.getFormula());
			String depVar = depXml.getName();
			PmmXmlDoc indepXml = tuple.getPmmXml(Model1Schema.ATT_INDEPENDENT);
			PmmXmlDoc paramXml = tuple.getPmmXml(Model1Schema.ATT_PARAMETER);
			Map<String, List<Double>> variables = new LinkedHashMap<String, List<Double>>();
			Map<String, Double> varMin = new LinkedHashMap<String, Double>();
			Map<String, Double> varMax = new LinkedHashMap<String, Double>();
			Map<String, Double> parameters = new LinkedHashMap<String, Double>();
			Map<String, Double> paramData = new LinkedHashMap<String, Double>();
			Map<String, Map<String, Double>> covariances = new LinkedHashMap<String, Map<String, Double>>();
			String initParam = initParams.get(primId);
			String lagParam = lagParams.get(primId);
			Map<String, List<String>> categories = new LinkedHashMap<String, List<String>>();
			Map<String, String> units = new LinkedHashMap<String, String>();
			Plotable plotable = new Plotable(Plotable.FUNCTION_SAMPLE);

			categories.put(depXml.getName(),
					Arrays.asList(depXml.getCategory()));
			units.put(depXml.getName(), depXml.getUnit());

			for (PmmXmlElementConvertable el : indepXml.getElementSet()) {
				IndepXml element = (IndepXml) el;

				variables.put(element.getName(), new ArrayList<Double>());
				varMin.put(element.getName(), element.getMin());
				varMax.put(element.getName(), element.getMax());

				categories.put(element.getName(),
						Arrays.asList(element.getCategory()));
				units.put(element.getName(), element.getUnit());

				if (Categories.getTempCategory().equals(
						Categories.getCategoryByUnit(element.getUnit()))) {
					tempParam.put(id, element.getName());
				}

				if (Categories.getPhUnit().equals(element.getUnit())) {
					phParam.put(id, element.getName());
				}

				if (Categories.getAwUnit().equals(element.getUnit())) {
					awParam.put(id, element.getName());
				}
			}

			Double minConcentration = null;
			Double maxConcentration = null;

			if (isTertiaryModel && containsData) {
				Point2D range = getConcentrationRange(dataTuples.get(id));

				if (range != null) {
					minConcentration = range.getX();
					maxConcentration = range.getY();
				}
			}

			for (PmmXmlElementConvertable el : paramXml.getElementSet()) {
				ParamXml element = (ParamXml) el;

				if (element.getName().equals(initParam)
						|| element.getName().equals(lagParam)) {
					variables.put(element.getName(), new ArrayList<Double>());
					units.put(element.getName(), element.getUnit());
					categories.put(element.getName(),
							Arrays.asList(element.getCategory()));

					if (element.getName().equals(initParam)
							&& minConcentration != null
							&& maxConcentration != null) {
						varMin.put(element.getName(), minConcentration);
						varMax.put(element.getName(), maxConcentration);
					} else {
						varMin.put(element.getName(), element.getMin());
						varMax.put(element.getName(), element.getMax());
					}

					if (element.getValue() != null) {
						plotable.addValueList(
								element.getName(),
								new ArrayList<Double>(Arrays.asList(element
										.getValue())));
					} else {
						plotable.addValueList(element.getName(),
								new ArrayList<Double>());
					}
				} else {
					parameters.put(element.getName(), element.getValue());
					paramData.put(element.getName(), element.getValue());
					paramData.put(element.getName() + ": SE",
							element.getError());
					paramData.put(element.getName() + ": t", element.getT());
					paramData.put(element.getName() + ": Pr > |t|",
							element.getP());
				}

				if (initParam == null && lagParam == null) {
					Map<String, Double> cov = new LinkedHashMap<String, Double>();

					for (PmmXmlElementConvertable el2 : paramXml
							.getElementSet()) {
						cov.put(((ParamXml) el2).getName(), element
								.getCorrelation(((ParamXml) el2).getOrigName()));
					}

					covariances.put(element.getName(), cov);
				}
			}

			formulas.add(formula);
			parameterData.add(paramData);

			PmmXmlDoc estModelXml = tuple.getPmmXml(Model1Schema.ATT_ESTMODEL);
			String literature = "";

			for (PmmXmlElementConvertable el : tuple.getPmmXml(
					Model1Schema.ATT_EMLIT).getElementSet()) {
				literature += "," + (LiteratureItem) el;
			}

			if (!literature.isEmpty()) {
				literature = literature.substring(1);
			}

			shortLegend.put(id, index + "");
			longLegend.put(id, index + "");
			shortIds.put(id, index + "");
			stringColumnValues.get(0).add(index + "");
			stringColumnValues.get(2).add(modelName);
			stringColumnValues.get(3).add(literature);
			stringColumnValues.get(4).add(
					((EstModelXml) estModelXml.get(0)).getName());
			index++;

			if (isTertiaryModel) {
				AgentXml agent = (AgentXml) tuple.getPmmXml(
						TimeSeriesSchema.ATT_AGENT).get(0);
				MatrixXml matrix = (MatrixXml) tuple.getPmmXml(
						TimeSeriesSchema.ATT_MATRIX).get(0);
				Set<String> secModels = new LinkedHashSet<String>();

				for (KnimeTuple t : combined.get(tuple)) {
					secModels.add(((CatalogModelXml) t.getPmmXml(
							Model2Schema.ATT_MODELCATALOG).get(0)).getName());
				}

				String secString = "";

				for (String s : secModels) {
					secString += "," + s;
				}

				stringColumnValues.get(5).add(secString.substring(1));
				stringColumnValues.get(6).add(agent.getName());
				stringColumnValues.get(7).add(matrix.getName());
			}

			doubleColumnValues.get(0).add(
					MathUtilities.getSSE(
							((EstModelXml) estModelXml.get(0)).getRms(),
							((EstModelXml) estModelXml.get(0)).getDof()));
			doubleColumnValues.get(1).add(
					MathUtilities.getMSE(((EstModelXml) estModelXml.get(0))
							.getRms()));
			doubleColumnValues.get(2).add(
					((EstModelXml) estModelXml.get(0)).getRms());
			doubleColumnValues.get(3).add(
					((EstModelXml) estModelXml.get(0)).getR2());
			doubleColumnValues.get(4).add(
					((EstModelXml) estModelXml.get(0)).getAic());

			plotable.setFunction(modelXml.getFormula());
			plotable.setFunctionValue(depVar);
			plotable.setFunctionArguments(variables);
			plotable.setMinArguments(varMin);
			plotable.setMaxArguments(varMax);
			plotable.setFunctionParameters(parameters);
			plotable.setCovariances(covariances);
			plotable.setDegreesOfFreedom(((EstModelXml) estModelXml.get(0))
					.getDof());
			plotable.setCategories(categories);
			plotable.setUnits(units);

			if (isTertiaryModel) {
				if (containsData) {
					for (int i = 0; i < miscParams.size(); i++) {
						String unit = null;

						for (PmmXmlElementConvertable el : tuple.getPmmXml(
								TimeSeriesSchema.ATT_MISC).getElementSet()) {
							MiscXml element = (MiscXml) el;

							if (miscParams.get(i).equals(element.getName())) {
								unit = element.getUnit();
								break;
							}
						}

						conditionUnits.get(i).add(unit);

						if (unit != null) {
							units.put(miscParams.get(i), unit);
							categories.put(
									miscParams.get(i),
									Arrays.asList(Categories.getCategoryByUnit(
											unit).getName()));
						}
					}
				}

				for (int i = 0; i < miscParams.size(); i++) {
					Double min = null;
					Double max = null;
					String unit = null;

					for (PmmXmlElementConvertable el : tuple.getPmmXml(
							Model1Schema.ATT_INDEPENDENT).getElementSet()) {
						IndepXml element = (IndepXml) el;

						if (miscParams.get(i).equals(element.getName())) {
							min = element.getMin();
							max = element.getMax();
							unit = element.getUnit();
							break;
						}
					}

					conditionMinValues.get(i).add(min);
					conditionMaxValues.get(i).add(max);

					if (!containsData) {
						conditionUnits.get(i).add(unit);
					} else {
						List<String> cu = conditionUnits.get(i);

						if (!cu.isEmpty() && cu.get(cu.size() - 1) == null) {
							cu.set(cu.size() - 1, unit);
						}
					}
				}
			} else if (containsData) {
				String dataName;

				if (tuple.getString(TimeSeriesSchema.ATT_COMBASEID) != null) {
					dataName = tuple.getString(TimeSeriesSchema.ATT_COMBASEID);
				} else {
					dataName = "" + tuple.getInt(TimeSeriesSchema.ATT_CONDID);
				}

				stringColumnValues.get(5).add(dataName);

				for (int i = 0; i < miscParams.size(); i++) {
					Double value = null;
					String unit = null;

					for (PmmXmlElementConvertable el : tuple.getPmmXml(
							TimeSeriesSchema.ATT_MISC).getElementSet()) {
						MiscXml element = (MiscXml) el;

						if (miscParams.get(i).equals(element.getName())) {
							value = element.getValue();
							unit = element.getUnit();
							break;
						}
					}

					conditionValues.get(i).add(value);
					conditionUnits.get(i).add(unit);
				}
			}

			if (!plotable.isPlotable()) {
				stringColumnValues.get(1).add(ChartConstants.FAILED);
			} else if (PmmUtilities.isOutOfRange(paramXml)) {
				stringColumnValues.get(1).add(ChartConstants.OUT_OF_LIMITS);
			} else if (PmmUtilities.covarianceMatrixMissing(paramXml)) {
				stringColumnValues.get(1).add(ChartConstants.NO_COVARIANCE);
			} else {
				stringColumnValues.get(1).add(ChartConstants.OK);
			}

			plotables.put(id, plotable);
		}
	}

	public List<String> getIds() {
		return ids;
	}

	public Map<String, KnimeTuple> getTupleMap() {
		return tupleMap;
	}

	public Map<String, Plotable> getPlotables() {
		return plotables;
	}

	public Map<String, String> getShortIds() {
		return shortIds;
	}

	public List<String> getStringColumns() {
		return stringColumns;
	}

	public List<List<String>> getStringColumnValues() {
		return stringColumnValues;
	}

	public List<String> getDoubleColumns() {
		return doubleColumns;
	}

	public List<List<Double>> getDoubleColumnValues() {
		return doubleColumnValues;
	}

	public List<String> getFormulas() {
		return formulas;
	}

	public List<Map<String, Double>> getParameterData() {
		return parameterData;
	}

	public List<String> getConditions() {
		return conditions;
	}

	public List<List<Double>> getConditionValues() {
		return conditionValues;
	}

	public List<List<Double>> getConditionMinValues() {
		return conditionMinValues;
	}

	public List<List<Double>> getConditionMaxValues() {
		return conditionMaxValues;
	}

	public List<List<String>> getConditionUnits() {
		return conditionUnits;
	}

	public List<String> getStandardVisibleColumns() {
		return standardVisibleColumns;
	}

	public List<String> getFilterableStringColumns() {
		return filterableStringColumns;
	}

	public Map<String, String> getShortLegend() {
		return shortLegend;
	}

	public Map<String, String> getLongLegend() {
		return longLegend;
	}

	public Map<String, String> getNewInitParams() {
		return newInitParams;
	}

	public Map<String, String> getNewLagParams() {
		return newLagParams;
	}

	public Map<KnimeTuple, List<KnimeTuple>> getCombinedTuples() {
		return combinedTuples;
	}

	public Map<String, String> getTempParam() {
		return tempParam;
	}

	public Map<String, String> getPhParam() {
		return phParam;
	}

	public Map<String, String> getAwParam() {
		return awParam;
	}

	private List<KnimeTuple> getAllDataTuples(KnimeTuple current,
			List<KnimeTuple> all) {
		List<KnimeTuple> tuples = new ArrayList<KnimeTuple>();
		Integer primId = ((CatalogModelXml) current.getPmmXml(
				Model1Schema.ATT_MODELCATALOG).get(0)).getId();
		Integer secEstId = ((EstModelXml) current.getPmmXml(
				Model2Schema.ATT_ESTMODEL).get(0)).getId();

		for (KnimeTuple tuple : all) {
			Integer pId = ((CatalogModelXml) tuple.getPmmXml(
					Model1Schema.ATT_MODELCATALOG).get(0)).getId();
			Integer sId = ((EstModelXml) tuple.getPmmXml(
					Model2Schema.ATT_ESTMODEL).get(0)).getId();

			if (primId.equals(pId) && secEstId.equals(sId)) {
				tuples.add(tuple);
			}
		}

		return tuples;
	}

	private static Point2D getConcentrationRange(List<KnimeTuple> tuples) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (KnimeTuple tuple : tuples) {
			for (PmmXmlElementConvertable el : tuple.getPmmXml(
					TimeSeriesSchema.ATT_TIMESERIES).getElementSet()) {
				Double value = ((TimeSeriesXml) el).getConcentration();

				if (value != null) {
					min = Math.min(value, min);
					max = Math.max(value, max);
				}
			}
		}

		if (MathUtilities.isValid(min) && MathUtilities.isValid(max)) {
			return new Point2D.Double(min, max);
		}

		return null;
	}

}
