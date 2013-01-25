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
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

public class XLSReader {

	public static String ID_COLUMN = "ID";

	private XLSReader() {
	}

	public static Map<String, KnimeTuple> getTimeSeriesTuples(File file,
			Map<String, Object> columnMappings) throws Exception {
		Sheet sheet = getSheet(file);
		Map<String, KnimeTuple> tuples = new LinkedHashMap<String, KnimeTuple>();
		Map<String, Integer> columns = getColumns(sheet);
		Map<String, Integer> miscColumns = new LinkedHashMap<>();
		Integer idColumn = null;
		Integer commentColumn = null;
		Integer timeColumn = null;
		Integer logcColumn = null;

		for (String column : columns.keySet()) {
			Object mapping = columnMappings.get(column);

			if (mapping instanceof MiscXml) {
				miscColumns.put(column, columns.get(column));
			} else if (mapping.equals(ID_COLUMN)) {
				idColumn = columns.get(column);
			} else if (mapping.equals(TimeSeriesSchema.ATT_COMMENT)) {
				commentColumn = columns.get(column);
			} else if (mapping.equals(TimeSeriesSchema.TIME)) {
				timeColumn = columns.get(column);
			} else if (mapping.equals(TimeSeriesSchema.LOGC)) {
				logcColumn = columns.get(column);
			}
		}

		KnimeTuple tuple = null;
		PmmXmlDoc timeSeriesXml = null;
		String id = null;

		for (int i = 1;; i++) {
			if (isEndOfFile(sheet, i)) {
				if (tuple != null) {
					tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES,
							timeSeriesXml);
					tuples.put(id, tuple);
				}

				break;
			}

			Row row = sheet.getRow(i);
			Cell idCell = row.getCell(idColumn);
			Cell commentCell = row.getCell(commentColumn);
			Cell timeCell = row.getCell(timeColumn);
			Cell logcCell = row.getCell(logcColumn);

			if (idCell != null && !idCell.toString().trim().isEmpty()
					&& !idCell.toString().trim().equals(id)) {
				if (tuple != null) {
					tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES,
							timeSeriesXml);
					tuples.put(id, tuple);
				}

				id = idCell.toString().trim();
				tuple = new KnimeTuple(new TimeSeriesSchema());
				tuple.setValue(TimeSeriesSchema.ATT_CONDID,
						MathUtilities.getRandomNegativeInt());
				timeSeriesXml = new PmmXmlDoc();

				if (commentCell != null) {
					tuple.setValue(TimeSeriesSchema.ATT_COMMENT, commentCell
							.toString().trim());
				}

				PmmXmlDoc miscXML = new PmmXmlDoc();

				for (String column : miscColumns.keySet()) {
					MiscXml misc = (MiscXml) columnMappings.get(column);
					Cell cell = row.getCell(miscColumns.get(column));

					try {
						misc.setValue(Double.parseDouble(cell.toString()
								.replace(",", ".")));
					} catch (Exception e) {
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
						throw new Exception(TimeSeriesSchema.TIME
								+ " value in row " + (i + 1) + " is not valid");
					}
				}

				if (logcCell != null && !logcCell.toString().trim().isEmpty()) {
					try {
						logc = Double.parseDouble(logcCell.toString().replace(
								",", "."));
					} catch (NumberFormatException e) {
						throw new Exception(TimeSeriesSchema.LOGC
								+ " value in row " + (i + 1) + " is not valid");
					}
				}

				timeSeriesXml.add(new TimeSeriesXml(null, time, logc));
			}
		}

		return tuples;
	}

	// public static Map<String, KnimeTuple> getDValueTuples(File file)
	// throws Exception {
	// Sheet sheet = getSheet(file);
	// Map<String, KnimeTuple> tuples = new LinkedHashMap<String, KnimeTuple>();
	// Map<String, Integer> standardColumns = getColumns(sheet,
	// DVALUE_STANDARD_COLUMNS);
	// Map<String, Integer> miscColumns = getOtherColumns(sheet,
	// DVALUE_STANDARD_COLUMNS);
	//
	// for (int i = 1;; i++) {
	// if (!hasID(sheet, i)) {
	// break;
	// }
	//
	// Row row = sheet.getRow(i);
	// String id = row.getCell(standardColumns.get(ID)).toString();
	// Cell agentCell = row.getCell(standardColumns
	// .get(TimeSeriesSchema.ATT_AGENTNAME));
	// Cell matrixCell = row.getCell(standardColumns
	// .get(TimeSeriesSchema.ATT_MATRIXNAME));
	// Cell commentCell = row.getCell(standardColumns
	// .get(TimeSeriesSchema.ATT_COMMENT));
	// Cell dValueCell = row.getCell(standardColumns.get(DVALUE));
	// KnimeTuple tuple = new KnimeTuple(new KnimeSchema(
	// new Model1Schema(), new TimeSeriesSchema()));
	//
	// tuple.setValue(TimeSeriesSchema.ATT_CONDID,
	// MathUtilities.getRandomNegativeInt());
	//
	// if (agentCell != null) {
	// tuple.setValue(TimeSeriesSchema.ATT_AGENTDETAIL, agentCell
	// .toString().trim());
	// }
	//
	// if (matrixCell != null) {
	// tuple.setValue(TimeSeriesSchema.ATT_MATRIXDETAIL, matrixCell
	// .toString().trim());
	// }
	//
	// if (commentCell != null) {
	// tuple.setValue(TimeSeriesSchema.ATT_COMMENT, commentCell
	// .toString().trim());
	// }
	//
	// PmmXmlDoc miscXML = new PmmXmlDoc();
	//
	// for (String miscName : miscColumns.keySet()) {
	// Cell cell = row.getCell(miscColumns.get(miscName));
	// Double value = null;
	//
	// try {
	// value = Double.parseDouble(cell.toString()
	// .replace(",", "."));
	// } catch (Exception e) {
	// }
	//
	// miscXML.add(new MiscXml(MathUtilities.getRandomNegativeInt(),
	// miscName, "", value, ""));
	// }
	//
	// tuple.setValue(TimeSeriesSchema.ATT_MISC, miscXML);
	//
	// Double dValue = null;
	// Double log10N0 = null;
	//
	// if (dValueCell != null && !dValueCell.toString().trim().isEmpty()) {
	// try {
	// dValue = Double.parseDouble(dValueCell.toString().replace(
	// ",", "."));
	//
	// if (dValue <= 0.0) {
	// log10N0 = 10.0;
	// } else {
	// log10N0 = 0.0;
	// }
	// } catch (NumberFormatException e) {
	// throw new Exception(DVALUE + " value in row " + (i + 1)
	// + " is not valid");
	// }
	// }
	//
	// PmmXmlDoc modelXML = new PmmXmlDoc();
	// PmmXmlDoc estModelXML = new PmmXmlDoc();
	// PmmXmlDoc depXML = new PmmXmlDoc();
	// PmmXmlDoc indepXML = new PmmXmlDoc();
	// PmmXmlDoc paramXML = new PmmXmlDoc();
	//
	// modelXML.add(new CatalogModelXml(MathUtilities
	// .getRandomNegativeInt(), "", TimeSeriesSchema.LOGC + "="
	// + LOG10N0 + "+1/" + DVALUE + "*" + TimeSeriesSchema.TIME));
	// estModelXML.add(new EstModelXml(MathUtilities
	// .getRandomNegativeInt(), "", null, null, null, null, null));
	// depXML.add(new DepXml(TimeSeriesSchema.LOGC));
	// indepXML.add(new IndepXml(TimeSeriesSchema.TIME, null, null));
	// paramXML.add(new ParamXml(LOG10N0, log10N0, null, null, null, null,
	// null));
	// paramXML.add(new ParamXml(DVALUE, dValue, null, null, null, null,
	// null));
	//
	// tuple.setValue(Model1Schema.ATT_MODELCATALOG, modelXML);
	// tuple.setValue(Model1Schema.ATT_ESTMODEL, estModelXML);
	// tuple.setValue(Model1Schema.ATT_DEPENDENT, depXML);
	// tuple.setValue(Model1Schema.ATT_INDEPENDENT, indepXML);
	// tuple.setValue(Model1Schema.ATT_PARAMETER, paramXML);
	//
	// tuples.put(id, tuple);
	// }
	//
	// return tuples;
	// }

	public static List<String> getTimeSeriesMiscColumns(File file)
			throws Exception {
		Sheet sheet = getSheet(file);

		return new ArrayList<>(getColumns(sheet).keySet());
	}

	// public static List<String> getDValueMiscColumns(File file) throws
	// Exception {
	// Sheet sheet = getSheet(file);
	//
	// return new ArrayList<>(getOtherColumns(sheet, DVALUE_STANDARD_COLUMNS)
	// .keySet());
	// }

	private static Sheet getSheet(File file) throws Exception {
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

		Workbook wb = WorkbookFactory.create(inputStream);

		return wb.getSheetAt(0);
	}

	private static Map<String, Integer> getColumns(Sheet sheet) {
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

	private static boolean isEndOfFile(Sheet sheet, int i) {
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

	// private static boolean hasID(Sheet sheet, int i) {
	// Row row = sheet.getRow(i);
	//
	// if (row == null) {
	// return true;
	// }
	//
	// for (int j = 0;; j++) {
	// Cell headerCell = sheet.getRow(0).getCell(j);
	// Cell cell = sheet.getRow(i).getCell(j);
	//
	// if (headerCell == null || headerCell.toString().trim().isEmpty()) {
	// return false;
	// }
	//
	// if (headerCell.toString().trim().equals(ID)) {
	// if (cell != null && !cell.toString().trim().isEmpty()) {
	// return true;
	// } else {
	// return false;
	// }
	// }
	// }
	// }
}
