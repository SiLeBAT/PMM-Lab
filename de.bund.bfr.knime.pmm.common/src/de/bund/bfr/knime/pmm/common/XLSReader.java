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
import de.bund.bfr.knime.pmm.common.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

public class XLSReader {

	public static String ID_COLUMN = "ID";
	public static String AGENT_DETAILS_COLUMN = TimeSeriesSchema.ATT_AGENT
			+ " Details";
	public static String MATRIX_DETAILS_COLUMN = TimeSeriesSchema.ATT_MATRIX
			+ " Details";

	private List<String> warnings;

	public XLSReader() {
		warnings = new ArrayList<>();
	}

	public Map<String, KnimeTuple> getTimeSeriesTuples(File file, String sheet,
			Map<String, Object> columnMappings, String agentColumnName,
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
		Integer agentDetailsColumn = null;
		Integer matrixDetailsColumn = null;
		Integer agentColumn = null;
		Integer matrixColumn = null;
		String timeColumnName = null;
		String logcColumnName = null;

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
				} else if (mapping.equals(AttributeUtilities.ATT_COMMENT)) {
					commentColumn = columns.get(column);
				} else if (mapping.equals(AttributeUtilities.TIME)) {
					timeColumn = columns.get(column);
					timeColumnName = column;
				} else if (mapping.equals(AttributeUtilities.LOGC)) {
					logcColumn = columns.get(column);
					logcColumnName = column;
				} else if (mapping.equals(AGENT_DETAILS_COLUMN)) {
					agentDetailsColumn = columns.get(column);
				} else if (mapping.equals(MATRIX_DETAILS_COLUMN)) {
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

			if (idCell != null && !idCell.toString().trim().isEmpty()
					&& !idCell.toString().trim().equals(id)) {
				if (tuple != null) {
					tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES,
							timeSeriesXml);
					tuples.put(id, tuple);
				}

				id = idCell.toString().trim();
				tuple = new KnimeTuple(SchemaFactory.createDataSchema());
				tuple.setValue(TimeSeriesSchema.ATT_CONDID,
						MathUtilities.getRandomNegativeInt());
				timeSeriesXml = new PmmXmlDoc();

				PmmXmlDoc dataInfo = new PmmXmlDoc();
				PmmXmlDoc agentXml = new PmmXmlDoc();
				PmmXmlDoc matrixXml = new PmmXmlDoc();

				if (commentCell != null) {
					dataInfo.add(new MdInfoXml(null, null, commentCell
							.toString().trim(), null, null));
				} else {
					dataInfo.add(new MdInfoXml(null, null, null, null, null));
				}

				if (agentCell != null
						&& agentMappings.get(agentCell.toString().trim()) != null) {
					agentXml.add(agentMappings.get(agentCell.toString().trim()));
				} else {
					agentXml.add(new AgentXml(null, null, null));
				}

				if (matrixCell != null
						&& matrixMappings.get(matrixCell.toString().trim()) != null) {
					matrixXml.add(matrixMappings.get(matrixCell.toString()
							.trim()));
				} else {
					matrixXml.add(new MatrixXml(null, null, null));
				}

				if (agentDetailsCell != null) {
					((AgentXml) agentXml.get(0)).setDetail(agentDetailsCell
							.toString().trim());
				}

				if (matrixDetailsCell != null) {
					((MatrixXml) matrixXml.get(0)).setDetail(matrixDetailsCell
							.toString().trim());
				}

				tuple.setValue(TimeSeriesSchema.ATT_MDINFO, dataInfo);
				tuple.setValue(TimeSeriesSchema.ATT_AGENT, agentXml);
				tuple.setValue(TimeSeriesSchema.ATT_MATRIX, matrixXml);

				PmmXmlDoc miscXML = new PmmXmlDoc();

				for (String column : miscColumns.keySet()) {
					MiscXml misc = (MiscXml) columnMappings.get(column);
					Cell cell = row.getCell(miscColumns.get(column));

					if (cell != null && !cell.toString().trim().isEmpty()) {
						try {
							misc.setValue(Double.parseDouble(cell.toString()
									.replace(",", ".")));
						} catch (NumberFormatException e) {
							warnings.add(column + " value in row " + (i + 1)
									+ " is not valid ("
									+ cell.toString().trim() + ")");
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

				if (timeCell != null && !timeCell.toString().trim().isEmpty()) {
					try {
						time = Double.parseDouble(timeCell.toString().replace(
								",", "."));
					} catch (NumberFormatException e) {
						warnings.add(timeColumnName + " value in row "
								+ (i + 1) + " is not valid ("
								+ timeCell.toString().trim() + ")");
					}
				}

				if (logcCell != null && !logcCell.toString().trim().isEmpty()) {
					try {
						logc = Double.parseDouble(logcCell.toString().replace(
								",", "."));
					} catch (NumberFormatException e) {
						warnings.add(logcColumnName + " value in row "
								+ (i + 1) + " is not valid ("
								+ logcCell.toString().trim() + ")");
					}
				}

				timeSeriesXml.add(new TimeSeriesXml(null, time, logc));
			}
		}

		return tuples;
	}

	public Map<String, KnimeTuple> getPrimaryModelTuples(File file,
			String sheet, Map<String, Object> columnMappings,
			String agentColumnName, Map<String, AgentXml> agentMappings,
			String matrixColumnName, Map<String, MatrixXml> matrixMappings,
			KnimeTuple modelTuple, Map<String, String> modelMappings)
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
				} else if (mapping.equals(AttributeUtilities.ATT_COMMENT)) {
					commentColumn = columns.get(column);
				} else if (mapping.equals(AGENT_DETAILS_COLUMN)) {
					agentDetailsColumn = columns.get(column);
				} else if (mapping.equals(MATRIX_DETAILS_COLUMN)) {
					matrixDetailsColumn = columns.get(column);
				}
			}
		}

		for (int i = 1;; i++) {
			if (isEndOfFile(s, i)) {
				break;
			}

			KnimeTuple dataTuple = new KnimeTuple(
					SchemaFactory.createDataSchema());
			Row row = s.getRow(i);
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

			if (commentCell != null) {
				dataInfo.add(new MdInfoXml(null, null, commentCell.toString()
						.trim(), null, null));
			} else {
				dataInfo.add(new MdInfoXml(null, null, null, null, null));
			}

			if (agentCell != null
					&& agentMappings.get(agentCell.toString().trim()) != null) {
				agentXml.add(agentMappings.get(agentCell.toString().trim()));
			} else {
				agentXml.add(new AgentXml(null, null, null));
			}

			if (matrixCell != null
					&& matrixMappings.get(matrixCell.toString().trim()) != null) {
				matrixXml.add(matrixMappings.get(matrixCell.toString().trim()));
			} else {
				matrixXml.add(new MatrixXml(null, null, null));
			}

			if (agentDetailsCell != null) {
				((AgentXml) agentXml.get(0)).setDetail(agentDetailsCell
						.toString().trim());
			}

			if (matrixDetailsCell != null) {
				((MatrixXml) matrixXml.get(0)).setDetail(matrixDetailsCell
						.toString().trim());
			}

			dataTuple.setValue(TimeSeriesSchema.ATT_MDINFO, dataInfo);
			dataTuple.setValue(TimeSeriesSchema.ATT_AGENT, agentXml);
			dataTuple.setValue(TimeSeriesSchema.ATT_MATRIX, matrixXml);

			PmmXmlDoc miscXML = new PmmXmlDoc();

			for (String column : miscColumns.keySet()) {
				MiscXml misc = (MiscXml) columnMappings.get(column);
				Cell cell = row.getCell(miscColumns.get(column));

				if (cell != null && !cell.toString().trim().isEmpty()) {
					try {
						misc.setValue(Double.parseDouble(cell.toString()
								.replace(",", ".")));
					} catch (NumberFormatException e) {
						warnings.add(column + " value in row " + (i + 1)
								+ " is not valid (" + cell.toString().trim()
								+ ")");
						misc.setValue(null);
					}
				} else {
					misc.setValue(null);
				}

				miscXML.add(misc);
			}

			dataTuple.setValue(TimeSeriesSchema.ATT_MISC, miscXML);

			PmmXmlDoc paramXml = modelTuple
					.getPmmXml(Model1Schema.ATT_PARAMETER);

			for (PmmXmlElementConvertable el : paramXml.getElementSet()) {
				ParamXml element = (ParamXml) el;
				String columnName = modelMappings.get(element.getName());

				if (columnName != null) {
					int column = columns.get(columnName);
					Cell cell = row.getCell(column);

					if (cell != null && !cell.toString().trim().isEmpty()) {
						try {
							element.setValue(Double.parseDouble(cell.toString()
									.replace(",", ".")));
						} catch (NumberFormatException e) {
							warnings.add(columnName + " value in row "
									+ (i + 1) + " is not valid ("
									+ cell.toString().trim() + ")");
							element.setValue(null);
						}
					}
				} else {
					element.setValue(null);
				}
			}

			modelTuple.setValue(Model1Schema.ATT_PARAMETER, paramXml);

			PmmXmlDoc estXml = modelTuple.getPmmXml(Model1Schema.ATT_ESTMODEL);

			((EstModelXml) estXml.get(0)).setID(MathUtilities
					.getRandomNegativeInt());

			modelTuple.setValue(Model1Schema.ATT_ESTMODEL, estXml);

			KnimeTuple tuple = new KnimeTuple(
					SchemaFactory.createM1DataSchema(), modelTuple, dataTuple);

			tuples.put((i + 1) + "", tuple);
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

			if (cell != null && !cell.toString().trim().isEmpty()) {
				valueSet.add(cell.toString().trim());
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

			if (cell == null || cell.toString().trim().isEmpty()) {
				break;
			}

			columns.put(cell.toString().trim(), i);
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

			if (headerCell == null || headerCell.toString().trim().isEmpty()) {
				return true;
			}

			if (cell != null && !cell.toString().trim().isEmpty()) {
				return false;
			}
		}
	}

}
