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
package de.bund.bfr.knime.pmm.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.knime.pmm.common.units.Categories;

public class XLSReader {

	public static String ID_COLUMN = "ID";
	public static String CONCENTRATION_STDDEV_COLUMN = "Concentration StdDev";

	private List<String> warnings;

	public XLSReader() {
		warnings = new ArrayList<>();
	}

	public Map<String, KnimeTuple> getTimeSeriesTuples(File file, String sheet,
			Map<String, Object> columnMappings, String timeUnit,
			String concentrationUnit, String agentColumnName,
			Map<String, AgentXml> agentMappings, String matrixColumnName,
			Map<String, MatrixXml> matrixMappings) throws Exception {
		warnings.clear();

		Sheet s = getWorkbook(file).getSheet(sheet);

		if (s == null) {
			throw new Exception("Sheet not found");
		}

		Map<String, KnimeTuple> tuples = new LinkedHashMap<String, KnimeTuple>();
		Map<String, Integer> columns = getColumns(s);
		Map<String, Integer> miscColumns = new LinkedHashMap<>();
		Integer idColumn = null;
		Integer commentColumn = null;
		Integer timeColumn = null;
		Integer logcColumn = null;
		Integer stdDevColumn = null;
		Integer agentDetailsColumn = null;
		Integer matrixDetailsColumn = null;
		Integer agentColumn = null;
		Integer matrixColumn = null;
		String timeColumnName = null;
		String logcColumnName = null;
		String stdDevColumnName = null;

		if (agentColumnName != null) {
			agentColumn = columns.get(agentColumnName);
		}

		if (matrixColumnName != null) {
			matrixColumn = columns.get(matrixColumnName);
		}

		for (String column : columns.keySet()) {
			if (columnMappings.containsKey(column)) {
				Object mapping = columnMappings.get(column);

				if (mapping instanceof MiscXml) {
					miscColumns.put(column, columns.get(column));
				} else if (mapping.equals(ID_COLUMN)) {
					idColumn = columns.get(column);
				} else if (mapping.equals(MdInfoXml.ATT_COMMENT)) {
					commentColumn = columns.get(column);
				} else if (mapping.equals(AttributeUtilities.TIME)) {
					timeColumn = columns.get(column);
					timeColumnName = column;
				} else if (mapping.equals(AttributeUtilities.CONCENTRATION)) {
					logcColumn = columns.get(column);
					logcColumnName = column;
				} else if (mapping
						.equals(XLSReader.CONCENTRATION_STDDEV_COLUMN)) {
					stdDevColumn = columns.get(column);
					stdDevColumnName = column;
				} else if (mapping.equals(AttributeUtilities.AGENT_DETAILS)) {
					agentDetailsColumn = columns.get(column);
				} else if (mapping.equals(AttributeUtilities.MATRIX_DETAILS)) {
					matrixDetailsColumn = columns.get(column);
				}
			}
		}

		KnimeTuple tuple = null;
		PmmXmlDoc timeSeriesXml = null;
		String id = null;

		for (int i = 1;; i++) {
			if (isEndOfFile(s, i)) {
				if (tuple != null) {
					tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES,
							timeSeriesXml);
					tuples.put(id, tuple);
				}

				break;
			}

			Row row = s.getRow(i);
			Cell idCell = null;
			Cell commentCell = null;
			Cell timeCell = null;
			Cell logcCell = null;
			Cell stdDevCell = null;
			Cell agentDetailsCell = null;
			Cell matrixDetailsCell = null;
			Cell agentCell = null;
			Cell matrixCell = null;

			if (idColumn != null) {
				idCell = row.getCell(idColumn);
			}

			if (commentColumn != null) {
				commentCell = row.getCell(commentColumn);
			}

			if (timeColumn != null) {
				timeCell = row.getCell(timeColumn);
			}

			if (logcColumn != null) {
				logcCell = row.getCell(logcColumn);
			}

			if (stdDevColumn != null) {
				stdDevCell = row.getCell(stdDevColumn);
			}

			if (agentDetailsColumn != null) {
				agentDetailsCell = row.getCell(agentDetailsColumn);
			}

			if (matrixDetailsColumn != null) {
				matrixDetailsCell = row.getCell(matrixDetailsColumn);
			}

			if (agentColumn != null) {
				agentCell = row.getCell(agentColumn);
			}

			if (matrixColumn != null) {
				matrixCell = row.getCell(matrixColumn);
			}

			if (hasData(idCell) && !getData(idCell).equals(id)) {
				if (tuple != null) {
					tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES,
							timeSeriesXml);
					tuples.put(id, tuple);
				}

				id = getData(idCell);
				tuple = new KnimeTuple(SchemaFactory.createDataSchema());
				tuple.setValue(TimeSeriesSchema.ATT_CONDID,
						MathUtilities.getRandomNegativeInt());
				timeSeriesXml = new PmmXmlDoc();

				PmmXmlDoc dataInfo = new PmmXmlDoc();
				PmmXmlDoc agentXml = new PmmXmlDoc();
				PmmXmlDoc matrixXml = new PmmXmlDoc();

				if (commentCell != null) {
					dataInfo.add(new MdInfoXml(null, null,
							getData(commentCell), null, null));
				} else {
					dataInfo.add(new MdInfoXml(null, null, null, null, null));
				}

				if (hasData(agentCell)
						&& agentMappings.get(getData(agentCell)) != null) {
					agentXml.add(agentMappings.get(getData(agentCell)));
				} else {
					agentXml.add(new AgentXml());
				}

				if (hasData(matrixCell)
						&& matrixMappings.get(getData(matrixCell)) != null) {
					matrixXml.add(matrixMappings.get(getData(matrixCell)));
				} else {
					matrixXml.add(new MatrixXml());
				}

				if (hasData(agentDetailsCell)) {
					((AgentXml) agentXml.get(0))
							.setDetail(getData(agentDetailsCell));
				}

				if (hasData(matrixDetailsCell)) {
					((MatrixXml) matrixXml.get(0))
							.setDetail(getData(matrixDetailsCell));
				}

				tuple.setValue(TimeSeriesSchema.ATT_MDINFO, dataInfo);
				tuple.setValue(TimeSeriesSchema.ATT_AGENT, agentXml);
				tuple.setValue(TimeSeriesSchema.ATT_MATRIX, matrixXml);

				PmmXmlDoc miscXML = new PmmXmlDoc();

				for (String column : miscColumns.keySet()) {
					MiscXml misc = (MiscXml) columnMappings.get(column);
					Cell cell = row.getCell(miscColumns.get(column));

					if (hasData(cell)) {
						try {
							misc.setValue(Double.parseDouble(getData(cell)
									.replace(",", ".")));
						} catch (NumberFormatException e) {
							warnings.add(column + " value in row " + (i + 1)
									+ " is not valid (" + getData(cell) + ")");
							misc.setValue(null);
						}
					} else {
						misc.setValue(null);
					}

					miscXML.add(misc);
				}

				tuple.setValue(TimeSeriesSchema.ATT_MISC, miscXML);
			}

			if (tuple != null) {
				Double time = null;
				Double logc = null;
				Double stdDev = null;

				if (hasData(timeCell)) {
					try {
						time = Double.parseDouble(getData(timeCell).replace(
								",", "."));
					} catch (NumberFormatException e) {
						warnings.add(timeColumnName + " value in row "
								+ (i + 1) + " is not valid ("
								+ getData(timeCell) + ")");
					}
				}

				if (hasData(logcCell)) {
					try {
						logc = Double.parseDouble(getData(logcCell).replace(
								",", "."));
					} catch (NumberFormatException e) {
						warnings.add(logcColumnName + " value in row "
								+ (i + 1) + " is not valid ("
								+ getData(logcCell) + ")");
					}
				}

				if (hasData(stdDevCell)) {
					try {
						stdDev = Double.parseDouble(getData(stdDevCell)
								.replace(",", "."));
					} catch (NumberFormatException e) {
						warnings.add(stdDevColumnName + " value in row "
								+ (i + 1) + " is not valid ("
								+ getData(stdDevCell) + ")");
					}
				}

				timeSeriesXml.add(new TimeSeriesXml(null, time, timeUnit, logc,
						concentrationUnit, stdDev));
			}
		}

		return tuples;
	}

	public Map<String, KnimeTuple> getModelTuples(File file, String sheet,
			Map<String, Object> columnMappings, String agentColumnName,
			Map<String, AgentXml> agentMappings, String matrixColumnName,
			Map<String, MatrixXml> matrixMappings, KnimeTuple modelTuple,
			Map<String, String> modelMappings,
			Map<String, KnimeTuple> secModelTuples,
			Map<String, Map<String, String>> secModelMappings,
			Map<String, Map<String, String>> secModelIndepMins,
			Map<String, Map<String, String>> secModelIndepMaxs,
			Map<String, Map<String, String>> secModelIndepUnits)
			throws Exception {
		warnings.clear();

		Sheet s = getWorkbook(file).getSheet(sheet);

		if (s == null) {
			throw new Exception("Sheet not found");
		}

		Map<String, KnimeTuple> tuples = new LinkedHashMap<String, KnimeTuple>();
		Map<String, Integer> columns = getColumns(s);
		Map<String, Integer> miscColumns = new LinkedHashMap<>();
		Integer commentColumn = null;
		Integer agentDetailsColumn = null;
		Integer matrixDetailsColumn = null;
		Integer agentColumn = null;
		Integer matrixColumn = null;

		if (agentColumnName != null) {
			agentColumn = columns.get(agentColumnName);
		}

		if (matrixColumnName != null) {
			matrixColumn = columns.get(matrixColumnName);
		}

		for (String column : columns.keySet()) {
			if (columnMappings.containsKey(column)) {
				Object mapping = columnMappings.get(column);

				if (mapping instanceof MiscXml) {
					miscColumns.put(column, columns.get(column));
				} else if (mapping.equals(MdInfoXml.ATT_COMMENT)) {
					commentColumn = columns.get(column);
				} else if (mapping.equals(AttributeUtilities.AGENT_DETAILS)) {
					agentDetailsColumn = columns.get(column);
				} else if (mapping.equals(AttributeUtilities.MATRIX_DETAILS)) {
					matrixDetailsColumn = columns.get(column);
				}
			}
		}

		int index = 0;

		for (int rowNumber = 1;; rowNumber++) {
			if (isEndOfFile(s, rowNumber)) {
				break;
			}

			KnimeTuple dataTuple = new KnimeTuple(
					SchemaFactory.createDataSchema());
			Row row = s.getRow(rowNumber);
			Cell commentCell = null;
			Cell agentDetailsCell = null;
			Cell matrixDetailsCell = null;
			Cell agentCell = null;
			Cell matrixCell = null;

			if (commentColumn != null) {
				commentCell = row.getCell(commentColumn);
			}

			if (agentDetailsColumn != null) {
				agentDetailsCell = row.getCell(agentDetailsColumn);
			}

			if (matrixDetailsColumn != null) {
				matrixDetailsCell = row.getCell(matrixDetailsColumn);
			}

			if (agentColumn != null) {
				agentCell = row.getCell(agentColumn);
			}

			if (matrixColumn != null) {
				matrixCell = row.getCell(matrixColumn);
			}

			dataTuple.setValue(TimeSeriesSchema.ATT_CONDID,
					MathUtilities.getRandomNegativeInt());

			PmmXmlDoc dataInfo = new PmmXmlDoc();
			PmmXmlDoc agentXml = new PmmXmlDoc();
			PmmXmlDoc matrixXml = new PmmXmlDoc();

			if (hasData(commentCell)) {
				dataInfo.add(new MdInfoXml(null, null, getData(commentCell),
						null, null));
			} else {
				dataInfo.add(new MdInfoXml(null, null, null, null, null));
			}

			if (hasData(agentCell)
					&& agentMappings.get(getData(agentCell)) != null) {
				agentXml.add(new AgentXml(agentMappings.get(getData(agentCell))));
			} else {
				agentXml.add(new AgentXml());
			}

			if (hasData(matrixCell)
					&& matrixMappings.get(getData(matrixCell)) != null) {
				matrixXml.add(new MatrixXml(matrixMappings
						.get(getData(matrixCell))));
			} else {
				matrixXml.add(new MatrixXml());
			}

			if (hasData(agentDetailsCell)) {
				((AgentXml) agentXml.get(0))
						.setDetail(getData(agentDetailsCell));
			}

			if (hasData(matrixDetailsCell)) {
				((MatrixXml) matrixXml.get(0))
						.setDetail(getData(matrixDetailsCell));
			}

			dataTuple.setValue(TimeSeriesSchema.ATT_MDINFO, dataInfo);
			dataTuple.setValue(TimeSeriesSchema.ATT_AGENT, agentXml);
			dataTuple.setValue(TimeSeriesSchema.ATT_MATRIX, matrixXml);

			PmmXmlDoc miscXML = new PmmXmlDoc();

			for (String column : miscColumns.keySet()) {
				MiscXml misc = new MiscXml((MiscXml) columnMappings.get(column));
				Cell cell = row.getCell(miscColumns.get(column));

				if (hasData(cell)) {
					try {
						misc.setValue(Double.parseDouble(getData(cell).replace(
								",", ".")));
					} catch (NumberFormatException e) {
						warnings.add(column + " value in row "
								+ (rowNumber + 1) + " is not valid ("
								+ getData(cell) + ")");
					}
				}

				miscXML.add(misc);
			}

			dataTuple.setValue(TimeSeriesSchema.ATT_MISC, miscXML);

			PmmXmlDoc paramXml = modelTuple
					.getPmmXml(Model1Schema.ATT_PARAMETER);
			PmmXmlDoc estXml = modelTuple.getPmmXml(Model1Schema.ATT_ESTMODEL);

			((EstModelXml) estXml.get(0)).setID(MathUtilities
					.getRandomNegativeInt());

			for (PmmXmlElementConvertable el : paramXml.getElementSet()) {
				ParamXml element = (ParamXml) el;
				String mapping = modelMappings.get(element.getName());

				if (mapping != null) {
					int column = columns.get(mapping);
					Cell cell = row.getCell(column);

					if (hasData(cell)) {
						try {
							element.setValue(Double.parseDouble(getData(cell)
									.replace(",", ".")));
						} catch (NumberFormatException e) {
							warnings.add(mapping + " value in row "
									+ (rowNumber + 1) + " is not valid ("
									+ getData(cell) + ")");
						}
					}
				}
			}

			modelTuple.setValue(Model1Schema.ATT_PARAMETER, paramXml);
			modelTuple.setValue(Model1Schema.ATT_ESTMODEL, estXml);

			if (secModelTuples.isEmpty()) {
				tuples.put(index + "",
						new KnimeTuple(SchemaFactory.createM1DataSchema(),
								modelTuple, dataTuple));
				index++;
			} else {
				for (String param : secModelTuples.keySet()) {
					KnimeTuple secTuple = secModelTuples.get(param);
					PmmXmlDoc secParamXml = secTuple
							.getPmmXml(Model2Schema.ATT_PARAMETER);
					PmmXmlDoc secDepXml = secTuple
							.getPmmXml(Model2Schema.ATT_DEPENDENT);
					PmmXmlDoc secEstXml = secTuple
							.getPmmXml(Model2Schema.ATT_ESTMODEL);
					PmmXmlDoc secModelXml = secTuple
							.getPmmXml(Model2Schema.ATT_MODELCATALOG);
					PmmXmlDoc secIndepXml = secTuple
							.getPmmXml(Model2Schema.ATT_INDEPENDENT);
					String formula = ((CatalogModelXml) secModelXml.get(0))
							.getFormula();

					formula = MathUtilities.replaceVariable(formula,
							((DepXml) secDepXml.get(0)).getName(), param);
					((CatalogModelXml) secModelXml.get(0)).setFormula(formula);
					((DepXml) secDepXml.get(0)).setName(param);
					((EstModelXml) secEstXml.get(0)).setID(MathUtilities
							.getRandomNegativeInt());

					for (PmmXmlElementConvertable el : secParamXml
							.getElementSet()) {
						ParamXml element = (ParamXml) el;
						String mapping = secModelMappings.get(param).get(
								element.getName());

						if (mapping != null) {
							Cell cell = row.getCell(columns.get(mapping));

							if (hasData(cell)) {
								try {
									element.setValue(Double
											.parseDouble(getData(cell).replace(
													",", ".")));
								} catch (NumberFormatException e) {
									warnings.add(mapping + " value in row "
											+ (rowNumber + 1)
											+ " is not valid (" + getData(cell)
											+ ")");
								}
							}
						}
					}

					for (PmmXmlElementConvertable el : secIndepXml
							.getElementSet()) {
						IndepXml element = (IndepXml) el;
						String unit = secModelIndepUnits.get(param).get(
								element.getName());

						if (unit != null) {
							String minMapping = secModelIndepMins.get(param)
									.get(element.getName());
							String maxMapping = secModelIndepMaxs.get(param)
									.get(element.getName());

							if (minMapping != null) {
								Cell minCell = row.getCell(columns
										.get(minMapping));

								if (hasData(minCell)) {
									try {
										double value = Double
												.parseDouble(getData(minCell)
														.replace(",", "."));

										element.setMin(Categories.getCategory(
												element.getCategory()).convert(
												value, unit, element.getUnit()));
									} catch (NumberFormatException e) {
										warnings.add(minMapping
												+ " value in row "
												+ (rowNumber + 1)
												+ " is not valid ("
												+ getData(minCell) + ")");
									}
								}
							}

							if (maxMapping != null) {
								Cell maxCell = row.getCell(columns
										.get(maxMapping));

								if (hasData(maxCell)) {
									try {
										double value = Double
												.parseDouble(getData(maxCell)
														.replace(",", "."));

										element.setMax(Categories.getCategory(
												element.getCategory()).convert(
												value, unit, element.getUnit()));
									} catch (NumberFormatException e) {
										warnings.add(maxMapping
												+ " value in row "
												+ (rowNumber + 1)
												+ " is not valid ("
												+ getData(maxCell) + ")");
									}
								}
							}
						}
					}

					secTuple.setValue(Model2Schema.ATT_MODELCATALOG,
							secModelXml);
					secTuple.setValue(Model2Schema.ATT_PARAMETER, secParamXml);
					secTuple.setValue(Model2Schema.ATT_DEPENDENT, secDepXml);
					secTuple.setValue(Model2Schema.ATT_ESTMODEL, secEstXml);
					secTuple.setValue(Model2Schema.ATT_INDEPENDENT, secIndepXml);

					tuples.put(
							index + "",
							new KnimeTuple(SchemaFactory.createM12DataSchema(),
									new KnimeTuple(SchemaFactory
											.createM1DataSchema(), modelTuple,
											dataTuple), secTuple));
					index++;
				}
			}
		}

		return tuples;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public List<String> getSheets(File file) throws Exception {
		List<String> sheets = new ArrayList<>();
		Workbook workbook = getWorkbook(file);

		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			sheets.add(workbook.getSheetName(i));
		}

		return sheets;
	}

	public List<String> getColumns(File file, String sheet) throws Exception {
		Sheet s = getWorkbook(file).getSheet(sheet);

		if (s == null) {
			throw new Exception("Sheet not found");
		}

		return new ArrayList<>(getColumns(s).keySet());
	}

	public Set<String> getValuesInColumn(File file, String sheet, String column)
			throws Exception {
		Set<String> valueSet = new LinkedHashSet<>();
		Sheet s = getWorkbook(file).getSheet(sheet);

		if (s == null) {
			throw new Exception("Sheet not found");
		}

		Map<String, Integer> columns = getColumns(s);
		int columnId = columns.get(column);

		for (int i = 1; i < s.getLastRowNum(); i++) {
			Cell cell = s.getRow(i).getCell(columnId);

			if (hasData(cell)) {
				valueSet.add(getData(cell));
			}
		}

		return valueSet;
	}

	private Workbook getWorkbook(File file) throws Exception {
		InputStream inputStream = null;

		if (file.exists()) {
			inputStream = new FileInputStream(file);
		} else {
			try {
				URL url = new URL(file.getPath());

				inputStream = url.openStream();
			} catch (Exception e) {
				throw new FileNotFoundException("File not found");
			}
		}

		return WorkbookFactory.create(inputStream);
	}

	private Map<String, Integer> getColumns(Sheet sheet) {
		Map<String, Integer> columns = new LinkedHashMap<String, Integer>();

		for (int i = 0;; i++) {
			Cell cell = sheet.getRow(0).getCell(i);

			if (!hasData(cell)) {
				break;
			}

			columns.put(getData(cell), i);
		}

		return columns;
	}

	private boolean isEndOfFile(Sheet sheet, int i) {
		Row row = sheet.getRow(i);

		if (row == null) {
			return true;
		}

		for (int j = 0;; j++) {
			Cell headerCell = sheet.getRow(0).getCell(j);
			Cell cell = sheet.getRow(i).getCell(j);

			if (!hasData(headerCell)) {
				return true;
			}

			if (hasData(cell)) {
				return false;
			}
		}
	}

	private boolean hasData(Cell cell) {
		return cell != null && !cell.toString().trim().isEmpty();
	}

	public String getData(Cell cell) {
		if (cell != null) {
			return cell.toString().trim();
		}

		return null;
	}

}
