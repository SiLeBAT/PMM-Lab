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
package de.bund.bfr.knime.pmm.modelanddatajoiner;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeRelationReader;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

public class CombinedJoiner implements Joiner {

	private static final String PRIMARY = "Primary";

	private KnimeSchema modelSchema;
	private KnimeSchema dataSchema;
	private KnimeSchema seiSchema;

	private BufferedDataTable modelTable;
	private BufferedDataTable dataTable;

	private List<JComboBox> primaryVariableBoxes;
	private Map<String, List<JComboBox>> secondaryVariableBoxes;

	private List<String> primaryVariables;
	private Map<String, List<String>> secondaryVariables;
	private List<String> primaryParameters;
	private List<String> secondaryParameters;

	private List<KnimeTuple> tuples;

	public CombinedJoiner(BufferedDataTable modelTable,
			BufferedDataTable dataTable) throws PmmException {
		this.modelTable = modelTable;
		this.dataTable = dataTable;

		modelSchema = new KnimeSchema(new Model1Schema(), new Model2Schema());
		dataSchema = new TimeSeriesSchema();
		seiSchema = new KnimeSchema(new KnimeSchema(new Model1Schema(),
				new Model2Schema()), new TimeSeriesSchema());
		primaryParameters = Arrays.asList("", TimeSeriesSchema.ATT_LOGC,
				TimeSeriesSchema.ATT_TIME);
		secondaryParameters = Arrays.asList("", TimeSeriesSchema.ATT_PH,
				TimeSeriesSchema.ATT_TEMPERATURE,
				TimeSeriesSchema.ATT_WATERACTIVITY);
		readTable();
		getVariables();
	}

	@Override
	public JComponent createPanel(List<String> assignments) {
		Map<String, Map<String, String>> replacements = getAssignmentsMap(assignments);

		primaryVariableBoxes = new ArrayList<JComboBox>(primaryVariables.size());
		secondaryVariableBoxes = new LinkedHashMap<String, List<JComboBox>>();

		JPanel panel = new JPanel();
		JPanel topPanel = new JPanel();

		panel.setLayout(new BorderLayout());
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		JPanel primaryPanel = new JPanel();

		primaryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		primaryPanel.setBorder(BorderFactory
				.createTitledBorder("Primary Variables"));

		for (String var : primaryVariables) {
			JComboBox box = new JComboBox(primaryParameters.toArray());

			if (replacements.containsKey(PRIMARY)
					&& replacements.get(PRIMARY).containsKey(var)) {
				box.setSelectedItem(replacements.get(PRIMARY).get(var));
			}

			primaryVariableBoxes.add(box);
			primaryPanel.add(new JLabel(var + ":"));
			primaryPanel.add(box);
		}

		topPanel.add(primaryPanel);

		for (String depVarSec : secondaryVariables.keySet()) {
			JPanel secondaryPanel = new JPanel();
			List<JComboBox> boxes = new ArrayList<JComboBox>();

			secondaryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			secondaryPanel.setBorder(BorderFactory.createTitledBorder(depVarSec
					+ "-Model Variables"));

			for (String var : secondaryVariables.get(depVarSec)) {
				JComboBox box = new JComboBox(secondaryParameters.toArray());

				if (replacements.containsKey(depVarSec)
						&& replacements.get(depVarSec).containsKey(var)) {
					box.setSelectedItem(replacements.get(depVarSec).get(var));
				}

				boxes.add(box);
				secondaryPanel.add(new JLabel(var + ":"));
				secondaryPanel.add(box);
			}

			secondaryVariableBoxes.put(depVarSec, boxes);
			topPanel.add(secondaryPanel);
		}

		panel.add(topPanel, BorderLayout.NORTH);

		return new JScrollPane(panel);
	}

	@Override
	public List<String> getAssignments() {
		List<String> assignments = new ArrayList<String>();
		StringBuilder primaryAssignments = new StringBuilder(PRIMARY + ":");

		for (int i = 0; i < primaryVariables.size(); i++) {
			String replacement = (String) primaryVariableBoxes.get(i)
					.getSelectedItem();

			if (!replacement.equals("")) {
				primaryAssignments.append(primaryVariables.get(i) + "="
						+ replacement + ",");
			}
		}

		primaryAssignments.deleteCharAt(primaryAssignments.length() - 1);
		assignments.add(primaryAssignments.toString());

		for (String depVarSec : secondaryVariables.keySet()) {
			StringBuilder secondaryAssignments = new StringBuilder(depVarSec
					+ ":");

			for (int i = 0; i < secondaryVariables.get(depVarSec).size(); i++) {
				String replacement = (String) secondaryVariableBoxes
						.get(depVarSec).get(i).getSelectedItem();

				if (!replacement.equals("")) {
					secondaryAssignments.append(secondaryVariables.get(
							depVarSec).get(i)
							+ "=" + replacement + ",");
				}
			}

			assignments.add(secondaryAssignments.toString());
		}

		return assignments;
	}

	@Override
	public BufferedDataTable getOutputTable(List<String> assignments,
			ExecutionContext exec) throws InvalidSettingsException,
			CanceledExecutionException, PmmException, InterruptedException {
		BufferedDataContainer container = exec.createDataContainer(seiSchema
				.createSpec());
		Map<String, Map<String, String>> replacements = getAssignmentsMap(assignments);
		int rowCount = tuples.size() * dataTable.getRowCount();
		int index = 0;

		if (!replacements.containsKey(PRIMARY)) {
			container.close();

			return container.getTable();
		}

		for (int i = 0; i < tuples.size(); i++) {
			KnimeTuple modelTuple = tuples.get(i);
			String formula = modelTuple.getString(Model1Schema.ATT_FORMULA);
			String depVar = modelTuple.getString(Model1Schema.ATT_DEPVAR);
			List<String> indepVar = modelTuple
					.getStringList(Model1Schema.ATT_INDEPVAR);
			Map<String, String> varMap = modelTuple
					.getMap(Model1Schema.ATT_VARPARMAP);
			List<String> newIndepVar = new ArrayList<String>();
			Map<String, String> newVarMap = new HashMap<String, String>();
			String formulaSec = modelTuple.getString(Model2Schema.ATT_FORMULA);
			String depVarSec = modelTuple.getString(Model2Schema.ATT_DEPVAR);
			List<String> indepVarSec = modelTuple
					.getStringList(Model2Schema.ATT_INDEPVAR);
			Map<String, String> varMapSec = modelTuple
					.getMap(Model2Schema.ATT_VARPARMAP);
			List<String> newIndepVarSec = new ArrayList<String>();
			Map<String, String> newVarMapSec = new HashMap<String, String>();
			boolean allVarsReplaced = true;

			if (replacements.get(PRIMARY).containsKey(depVar)) {
				if (varMap.containsKey(depVar)) {
					newVarMap.put(replacements.get(PRIMARY).get(depVar),
							varMap.get(depVar));
				} else {
					newVarMap
							.put(replacements.get(PRIMARY).get(depVar), depVar);
				}

				depVar = replacements.get(PRIMARY).get(depVar);
			} else {
				allVarsReplaced = false;
			}

			for (String var : replacements.get(PRIMARY).keySet()) {
				String newVar = replacements.get(PRIMARY).get(var);

				formula = MathUtilities.replaceVariable(formula, var, newVar);
			}

			for (String iv : indepVar) {
				if (replacements.get(PRIMARY).containsKey(iv)) {
					if (varMap.containsKey(iv)) {
						newVarMap.put(replacements.get(PRIMARY).get(iv),
								varMap.get(iv));
					} else {
						newVarMap.put(replacements.get(PRIMARY).get(iv), iv);
					}

					newIndepVar.add(replacements.get(PRIMARY).get(iv));
				} else {
					allVarsReplaced = false;
					break;
				}
			}

			for (String var : replacements.get(depVarSec).keySet()) {
				String newVar = replacements.get(depVarSec).get(var);

				formulaSec = MathUtilities.replaceVariable(formulaSec, var,
						newVar);
			}

			for (String iv : indepVarSec) {
				if (replacements.containsKey(depVarSec)
						&& replacements.get(depVarSec).containsKey(iv)) {
					if (varMapSec.containsKey(iv)) {
						newVarMapSec.put(replacements.get(depVarSec).get(iv),
								varMapSec.get(iv));
					} else {
						newVarMapSec.put(replacements.get(depVarSec).get(iv),
								iv);
					}

					newIndepVarSec.add(replacements.get(depVarSec).get(iv));
				} else {
					allVarsReplaced = false;
					break;
				}
			}

			if (!allVarsReplaced) {
				continue;
			}

			modelTuple.setValue(Model1Schema.ATT_FORMULA, formula);
			modelTuple.setValue(Model1Schema.ATT_DEPVAR, depVar);
			modelTuple.setValue(Model1Schema.ATT_INDEPVAR, newIndepVar);
			modelTuple.setValue(Model1Schema.ATT_VARPARMAP, newVarMap);
			modelTuple.setValue(Model1Schema.ATT_DATABASEWRITABLE,
					Model1Schema.NOTWRITABLE);
			modelTuple.setValue(Model2Schema.ATT_FORMULA, formulaSec);
			modelTuple.setValue(Model2Schema.ATT_INDEPVAR, newIndepVarSec);
			modelTuple.setValue(Model2Schema.ATT_VARPARMAP, newVarMapSec);
			modelTuple.setValue(Model2Schema.ATT_DATABASEWRITABLE,
					Model1Schema.NOTWRITABLE);

			KnimeRelationReader reader = new KnimeRelationReader(dataSchema,
					dataTable);

			while (reader.hasMoreElements()) {
				KnimeTuple dataTuple = reader.nextElement();
				KnimeTuple tuple = new KnimeTuple(seiSchema, modelTuple,
						dataTuple);

				container.addRowToTable(tuple);
				exec.checkCanceled();
				exec.setProgress((double) index / (double) rowCount,
						"Adding row " + index);
				index++;
			}
		}

		container.close();

		return container.getTable();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	private void readTable() throws PmmException {
		KnimeRelationReader reader = new KnimeRelationReader(modelSchema,
				modelTable);
		Set<String> ids = new HashSet<String>();

		tuples = new ArrayList<KnimeTuple>();

		while (reader.hasMoreElements()) {
			KnimeTuple modelRow = reader.nextElement();

			if (ids.add(modelRow.getInt(Model1Schema.ATT_ESTMODELID) + "("
					+ modelRow.getString(Model2Schema.ATT_DEPVAR) + ")")) {
				tuples.add(modelRow);
			}
		}
	}

	private void getVariables() throws PmmException {
		primaryVariables = new ArrayList<String>();
		secondaryVariables = new LinkedHashMap<String, List<String>>();

		for (KnimeTuple tuple : tuples) {
			if (!primaryVariables.contains(tuple
					.getString(Model1Schema.ATT_DEPVAR))) {
				primaryVariables.add(tuple.getString(Model1Schema.ATT_DEPVAR));
			}

			for (String indep : tuple.getStringList(Model1Schema.ATT_INDEPVAR)) {
				if (!primaryVariables.contains(indep)) {
					primaryVariables.add(indep);
				}
			}

			if (!secondaryVariables.containsKey(tuple
					.getString(Model2Schema.ATT_DEPVAR))) {
				List<String> secVars = new ArrayList<String>();

				for (String indepVarSec : tuple
						.getStringList(Model2Schema.ATT_INDEPVAR)) {
					secVars.add(indepVarSec);
				}

				secondaryVariables.put(
						tuple.getString(Model2Schema.ATT_DEPVAR), secVars);
			}
		}
	}

	private Map<String, Map<String, String>> getAssignmentsMap(
			List<String> assignments) {
		Map<String, Map<String, String>> assignmentsMap = new LinkedHashMap<String, Map<String, String>>();

		for (String s : assignments) {
			String[] elements = s.split(":");

			if (elements.length == 2) {
				Map<String, String> modelMap = new LinkedHashMap<String, String>();
				String model = elements[0];
				String assigns = elements[1];

				for (String assign : assigns.split(",")) {
					String[] assignElements = assign.split("=");

					modelMap.put(assignElements[0], assignElements[1]);
				}

				assignmentsMap.put(model, modelMap);
			}
		}

		return assignmentsMap;
	}
}
