package de.bund.bfr.knime.pmm.fittedparameterview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataTable;

import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.ParamXml;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.chart.Plotable;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeRelationReader;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.PmmUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

public class TableReader {

	private List<String> ids;
	private List<Integer> colorCounts;
	private Map<String, Plotable> plotables;
	private List<String> stringColumns;
	private List<List<String>> stringColumnValues;
	private List<String> doubleColumns;
	private List<List<Double>> doubleColumnValues;
	private List<String> standardVisibleColumns;
	private List<String> filterableStringColumns;
	private List<List<String>> infoParameters;
	private List<List<?>> infoParameterValues;
	private Map<String, String> shortLegend;
	private Map<String, String> longLegend;

	public TableReader(DataTable table, List<String> usedConditions) {
		Set<String> idSet = new LinkedHashSet<String>();
		KnimeRelationReader reader = new KnimeRelationReader(
				SchemaFactory.createM1DataSchema(), table);
		Map<String, List<Double>> paramDataMap = new LinkedHashMap<>();
		Map<String, Map<String, List<Double>>> miscDataMaps = new LinkedHashMap<>();
		List<String> miscParams = null;

		ids = new ArrayList<String>();
		plotables = new LinkedHashMap<String, Plotable>();
		infoParameters = new ArrayList<List<String>>();
		infoParameterValues = new ArrayList<List<?>>();
		shortLegend = new LinkedHashMap<String, String>();
		longLegend = new LinkedHashMap<String, String>();
		stringColumns = Arrays.asList(Model1Schema.ATT_PARAMETER);
		filterableStringColumns = new ArrayList<>();
		stringColumnValues = new ArrayList<List<String>>();
		stringColumnValues.add(new ArrayList<String>());
		standardVisibleColumns = new ArrayList<>(
				Arrays.asList(Model1Schema.ATT_PARAMETER));

		miscParams = PmmUtilities.getAllMiscParams(table);
		doubleColumns = new ArrayList<>();
		doubleColumnValues = new ArrayList<>();
		colorCounts = new ArrayList<Integer>();

		for (String param : miscParams) {
			doubleColumns.add("Min " + param);
			doubleColumns.add("Max " + param);
			doubleColumnValues.add(new ArrayList<Double>());
			doubleColumnValues.add(new ArrayList<Double>());
			standardVisibleColumns.add("Min " + param);
			standardVisibleColumns.add("Max " + param);
		}

		while (reader.hasMoreElements()) {
			KnimeTuple row = reader.nextElement();
			PmmXmlDoc paramXml = row.getPmmXml(Model1Schema.ATT_PARAMETER);

			for (PmmXmlElementConvertable el1 : paramXml.getElementSet()) {
				ParamXml element1 = (ParamXml) el1;
				String id = element1.getName();

				if (!idSet.contains(id)) {
					idSet.add(id);
					ids.add(id);
					stringColumnValues.get(0).add(id);
					infoParameters.add(new ArrayList<String>());
					infoParameterValues.add(new ArrayList<>());
					shortLegend.put(id, id);
					longLegend.put(id, id);

					paramDataMap.put(id, new ArrayList<Double>());
					miscDataMaps.put(id,
							new LinkedHashMap<String, List<Double>>());

					for (String param : miscParams) {
						miscDataMaps.get(id)
								.put(param, new ArrayList<Double>());
					}
				}

				paramDataMap.get(id).add(element1.getValue());

				PmmXmlDoc misc = row.getPmmXml(TimeSeriesSchema.ATT_MISC);

				for (String param : miscParams) {
					Double paramValue = null;

					for (PmmXmlElementConvertable el2 : misc.getElementSet()) {
						MiscXml element2 = (MiscXml) el2;

						if (param.equals(element2.getName())) {
							paramValue = element2.getValue();
							break;
						}
					}

					miscDataMaps.get(id).get(param).add(paramValue);
				}
			}
		}

		for (String id : ids) {
			Plotable plotable = new Plotable(Plotable.DATASET_STRICT);
			Map<String, List<Double>> arguments = new LinkedHashMap<String, List<Double>>();

			for (String param : usedConditions) {
				arguments.put(param, new ArrayList<Double>(Arrays.asList(0.0)));
			}

			plotable.setFunctionValue(id);
			plotable.setFunctionArguments(arguments);
			plotable.addValueList(id, paramDataMap.get(id));

			Map<String, List<Double>> miscs = miscDataMaps.get(id);

			for (String param : miscParams) {
				plotable.addValueList(param, miscs.get(param));
			}

			for (int i = 0; i < miscParams.size(); i++) {
				List<Double> nonNullValues = new ArrayList<Double>(
						miscs.get(miscParams.get(i)));

				nonNullValues.removeAll(Arrays.asList((Double) null));

				if (!nonNullValues.isEmpty()) {
					doubleColumnValues.get(2 * i).add(
							Collections.min(nonNullValues));
					doubleColumnValues.get(2 * i + 1).add(
							Collections.max(nonNullValues));
				} else {
					doubleColumnValues.get(2 * i).add(null);
					doubleColumnValues.get(2 * i + 1).add(null);
				}
			}

			colorCounts.add(plotable.getNumberOfCombinations());
			plotables.put(id, plotable);
		}
	}

	public List<String> getIds() {
		return ids;
	}

	public List<Integer> getColorCounts() {
		return colorCounts;
	}

	public Map<String, Plotable> getPlotables() {
		return plotables;
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

	public List<String> getStandardVisibleColumns() {
		return standardVisibleColumns;
	}

	public List<String> getFilterableStringColumns() {
		return filterableStringColumns;
	}

	public List<List<String>> getInfoParameters() {
		return infoParameters;
	}

	public List<List<?>> getInfoParameterValues() {
		return infoParameterValues;
	}

	public Map<String, String> getShortLegend() {
		return shortLegend;
	}

	public Map<String, String> getLongLegend() {
		return longLegend;
	}
}
