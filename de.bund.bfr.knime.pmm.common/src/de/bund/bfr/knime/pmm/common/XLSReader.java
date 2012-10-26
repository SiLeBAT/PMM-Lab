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
import java.util.Arrays;
import java.util.LinkedHashMap;
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

	private static String ID = "ID";

	private static String[] STANDARD_COLUMNS = { ID,
			TimeSeriesSchema.ATT_AGENTNAME, TimeSeriesSchema.ATT_MATRIXNAME,
			TimeSeriesSchema.ATT_COMMENT, TimeSeriesSchema.ATT_TIME,
			TimeSeriesSchema.ATT_LOGC, TimeSeriesSchema.ATT_TEMPERATURE,
			TimeSeriesSchema.ATT_PH, TimeSeriesSchema.ATT_WATERACTIVITY };

	private XLSReader() {
	}

	public static Map<String, KnimeTuple> getTuples(File file) throws Exception {
		Map<String, KnimeTuple> tuples = new LinkedHashMap<String, KnimeTuple>();
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
		Sheet sheet = wb.getSheetAt(0);
		Map<String, Integer> standardColumns = getStandardColumns(sheet);
		Map<String, Integer> miscColumns = getMiscColumns(sheet);

		KnimeTuple tuple = null;
		String id = null;

		for (int i = 1;; i++) {
			if (isEndOfFile(sheet, i)) {
				if (tuple != null) {
					tuples.put(id, tuple);
				}

				break;
			}

			Row row = sheet.getRow(i);
			Cell idCell = row.getCell(standardColumns.get(ID));
			Cell agentCell = row.getCell(standardColumns
					.get(TimeSeriesSchema.ATT_AGENTNAME));
			Cell matrixCell = row.getCell(standardColumns
					.get(TimeSeriesSchema.ATT_MATRIXNAME));
			Cell commentCell = row.getCell(standardColumns
					.get(TimeSeriesSchema.ATT_COMMENT));
			Cell tempCell = row.getCell(standardColumns
					.get(TimeSeriesSchema.ATT_TEMPERATURE));
			Cell phCell = row.getCell(standardColumns
					.get(TimeSeriesSchema.ATT_PH));
			Cell awCell = row.getCell(standardColumns
					.get(TimeSeriesSchema.ATT_WATERACTIVITY));
			Cell timeCell = row.getCell(standardColumns
					.get(TimeSeriesSchema.ATT_TIME));
			Cell logcCell = row.getCell(standardColumns
					.get(TimeSeriesSchema.ATT_LOGC));

			if (idCell != null && !idCell.toString().trim().isEmpty()
					&& !idCell.toString().trim().equals(id)) {
				if (tuple != null) {
					tuples.put(id, tuple);
				}

				id = idCell.toString().trim();
				tuple = new KnimeTuple(new TimeSeriesSchema());
				tuple.setValue(TimeSeriesSchema.ATT_CONDID,
						MathUtilities.getRandomNegativeInt());

				if (agentCell != null) {
					tuple.setValue(TimeSeriesSchema.ATT_AGENTDETAIL, agentCell
							.toString().trim());
				}

				if (matrixCell != null) {
					tuple.setValue(TimeSeriesSchema.ATT_MATRIXDETAIL,
							matrixCell.toString().trim());
				}

				if (commentCell != null) {
					tuple.setValue(TimeSeriesSchema.ATT_COMMENT, commentCell
							.toString().trim());
				}

				if (tempCell != null && !tempCell.toString().trim().isEmpty()) {
					try {
						tuple.setValue(
								TimeSeriesSchema.ATT_TEMPERATURE,
								Double.parseDouble(tempCell.toString().replace(
										",", ".")));
					} catch (NumberFormatException e) {
						throw new Exception(TimeSeriesSchema.ATT_TEMPERATURE
								+ " value in row " + (i + 1) + " is not valid");
					}
				}

				if (phCell != null && !phCell.toString().trim().isEmpty()) {
					try {
						tuple.setValue(
								TimeSeriesSchema.ATT_PH,
								Double.parseDouble(phCell.toString().replace(
										",", ".")));
					} catch (NumberFormatException e) {
						throw new Exception(TimeSeriesSchema.ATT_PH
								+ " value in row " + (i + 1) + " is not valid");
					}
				}

				if (awCell != null && !awCell.toString().trim().isEmpty()) {
					try {
						tuple.setValue(
								TimeSeriesSchema.ATT_WATERACTIVITY,
								Double.parseDouble(awCell.toString().replace(
										",", ".")));
					} catch (NumberFormatException e) {
						throw new Exception(TimeSeriesSchema.ATT_WATERACTIVITY
								+ " value in row " + (i + 1) + " is not valid");
					}
				}

				PmmXmlDoc miscXML = new PmmXmlDoc();

				for (String miscName : miscColumns.keySet()) {
					Cell cell = row.getCell(miscColumns.get(miscName));
					Double value = null;

					try {
						value = Double.parseDouble(cell.toString().replace(",",
								"."));
					} catch (Exception e) {
					}

					miscXML.add(new MiscXml(MathUtilities
							.getRandomNegativeInt(), miscName, "", value, ""));
				}

				tuple.setValue(TimeSeriesSchema.ATT_MISC, miscXML);
			}

			if (tuple != null) {
				if (timeCell != null && !timeCell.toString().trim().isEmpty()) {
					try {
						tuple.addValue(
								TimeSeriesSchema.ATT_TIME,
								Double.parseDouble(timeCell.toString().replace(
										",", ".")));
					} catch (NumberFormatException e) {
						throw new Exception(TimeSeriesSchema.ATT_TIME
								+ " value in row " + (i + 1) + " is not valid");
					}
				}

				if (logcCell != null && !logcCell.toString().trim().isEmpty()) {
					try {
						tuple.addValue(
								TimeSeriesSchema.ATT_LOGC,
								Double.parseDouble(logcCell.toString().replace(
										",", ".")));
					} catch (NumberFormatException e) {
						throw new Exception(TimeSeriesSchema.ATT_LOGC
								+ " value in row " + (i + 1) + " is not valid");
					}
				}
			}
		}

		return tuples;
	}

	private static Map<String, Integer> getStandardColumns(Sheet sheet)
			throws Exception {
		Map<String, Integer> standardColumns = new LinkedHashMap<String, Integer>();

		for (int i = 0;; i++) {
			Cell cell = sheet.getRow(0).getCell(i);

			if (cell == null || cell.toString().trim().isEmpty()) {
				break;
			}

			String columnName = cell.toString().trim();

			if (Arrays.asList(STANDARD_COLUMNS).contains(columnName)) {
				standardColumns.put(columnName, i);
			}
		}

		for (String columnName : STANDARD_COLUMNS) {
			if (!standardColumns.containsKey(columnName)) {
				throw new Exception("Column " + columnName + " is missing");
			}
		}

		return standardColumns;
	}

	private static Map<String, Integer> getMiscColumns(Sheet sheet) {
		Map<String, Integer> miscColumns = new LinkedHashMap<String, Integer>();

		for (int i = 0;; i++) {
			Cell cell = sheet.getRow(0).getCell(i);

			if (cell == null || cell.toString().trim().isEmpty()) {
				break;
			}

			String columnName = cell.toString().trim();

			if (!Arrays.asList(STANDARD_COLUMNS).contains(columnName)) {
				miscColumns.put(columnName, i);
			}
		}

		return miscColumns;
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
				break;
			}

			if (cell != null && !cell.toString().trim().isEmpty()) {
				return false;
			}
		}

		return true;
	}
}
