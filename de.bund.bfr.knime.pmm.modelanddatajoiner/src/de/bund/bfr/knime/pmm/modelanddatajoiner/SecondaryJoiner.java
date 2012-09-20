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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

public class SecondaryJoiner implements Joiner, ActionListener {

	private KnimeSchema modelSchema;
	private KnimeSchema dataSchema;
	private KnimeSchema seiSchema;

	private BufferedDataTable modelTable;
	private BufferedDataTable dataTable;

	private List<String> usedModels;
	private List<Map<String, String>> replacements;

	private List<String> models;
	private Map<String, String> modelNames;
	private Map<String, String> modelFormulas;
	private Map<String, String> dependentVariables;
	private Map<String, List<String>> independentVariables;
	private List<String> dependentParameters;
	private List<String> independentParameters;

	private Map<String, JPanel> boxPanels;
	private Map<String, JPanel> buttonPanels;
	private Map<String, List<Map<String, JComboBox>>> comboBoxes;
	private Map<String, List<JButton>> addButtons;
	private Map<String, List<JButton>> removeButtons;

	private boolean isValid;

	public SecondaryJoiner(BufferedDataTable modelTable,
			BufferedDataTable dataTable) throws PmmException {
		this.modelTable = modelTable;
		this.dataTable = dataTable;

		modelSchema = new Model2Schema();
		dataSchema = new KnimeSchema(new Model1Schema(), new TimeSeriesSchema());
		seiSchema = new KnimeSchema(new KnimeSchema(new Model1Schema(),
				new Model2Schema()), new TimeSeriesSchema());
		readModelTable();
		readDataTable();
	}

	public JPanel createPanel(List<String> assignments) {
		JPanel panel = new JPanel();
		JPanel topPanel = new JPanel();

		boxPanels = new HashMap<String, JPanel>();
		buttonPanels = new HashMap<String, JPanel>();
		comboBoxes = new HashMap<String, List<Map<String, JComboBox>>>();
		addButtons = new HashMap<String, List<JButton>>();
		removeButtons = new HashMap<String, List<JButton>>();
		panel.setLayout(new BorderLayout());
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		getReplacementsFromNodeAssignments(assignments);

		for (String modelID : models) {
			List<Map<String, String>> modelAssignments = new ArrayList<Map<String, String>>();
			List<Map<String, JComboBox>> modelBoxes = new ArrayList<Map<String, JComboBox>>();
			List<JButton> modelAddButtons = new ArrayList<JButton>();
			List<JButton> modelRemoveButtons = new ArrayList<JButton>();

			for (int i = 0; i < usedModels.size(); i++) {
				if (usedModels.get(i).equals(modelID)) {
					modelAssignments.add(replacements.get(i));
				}
			}

			JPanel modelPanel = new JPanel();
			JPanel leftPanel = new JPanel();
			JPanel rightPanel = new JPanel();

			leftPanel.setLayout(new GridLayout(0, 1));
			rightPanel.setLayout(new GridLayout(0, 1));

			for (Map<String, String> assignment : modelAssignments) {
				Map<String, JComboBox> boxes = new HashMap<String, JComboBox>();
				JPanel assignmentPanel = new JPanel();

				assignmentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

				JComboBox depBox = new JComboBox(dependentParameters.toArray());

				depBox.setSelectedItem(assignment.get(dependentVariables
						.get(modelID)));
				depBox.addActionListener(this);
				boxes.put(dependentVariables.get(modelID), depBox);
				assignmentPanel.add(new JLabel(dependentVariables.get(modelID)
						+ ":"));
				assignmentPanel.add(depBox);

				for (String indepVar : independentVariables.get(modelID)) {
					JComboBox indepBox = new JComboBox(
							independentParameters.toArray());

					indepBox.setSelectedItem(assignment.get(indepVar));
					indepBox.addActionListener(this);
					boxes.put(indepVar, indepBox);
					assignmentPanel.add(new JLabel(indepVar + ":"));
					assignmentPanel.add(indepBox);
				}

				modelBoxes.add(boxes);
				leftPanel.add(assignmentPanel);

				JPanel buttonPanel = new JPanel();
				JButton addButton = new JButton("+");
				JButton removeButton = new JButton("-");

				addButton.addActionListener(this);
				removeButton.addActionListener(this);
				modelAddButtons.add(addButton);
				modelRemoveButtons.add(removeButton);
				buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
				buttonPanel.add(removeButton);
				buttonPanel.add(addButton);
				rightPanel.add(buttonPanel);
			}

			JPanel buttonPanel = new JPanel();
			JButton addButton = new JButton("+");

			addButton.addActionListener(this);
			modelAddButtons.add(addButton);
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(addButton);
			leftPanel.add(new JPanel());
			rightPanel.add(buttonPanel);

			boxPanels.put(modelID, leftPanel);
			buttonPanels.put(modelID, rightPanel);
			comboBoxes.put(modelID, modelBoxes);
			addButtons.put(modelID, modelAddButtons);
			removeButtons.put(modelID, modelRemoveButtons);
			modelPanel.setBorder(BorderFactory.createTitledBorder(modelNames
					.get(modelID)));
			modelPanel.setLayout(new BorderLayout());
			modelPanel.setToolTipText(modelFormulas.get(modelID));
			modelPanel.add(leftPanel, BorderLayout.CENTER);
			modelPanel.add(rightPanel, BorderLayout.EAST);
			topPanel.add(modelPanel);
		}

		panel.add(topPanel, BorderLayout.NORTH);
		checkIfInputIsValid();

		JPanel scrollPanel = new JPanel();

		scrollPanel.setLayout(new BorderLayout());
		scrollPanel.add(new JScrollPane(panel), BorderLayout.CENTER);

		return scrollPanel;
	}

	public List<String> getAssignments() {
		getReplacementsFromFrame();

		List<String> assignments = new ArrayList<String>();

		for (int i = 0; i < usedModels.size(); i++) {
			String assign = usedModels.get(i) + ":";

			for (String var : replacements.get(i).keySet()) {
				String param = replacements.get(i).get(var);

				assign += var + "=" + param + ",";
			}

			assignments.add(assign.substring(0, assign.length() - 1));
		}

		return assignments;
	}

	public BufferedDataTable getOutputTable(List<String> assignments,
			ExecutionContext exec) throws InvalidSettingsException,
			CanceledExecutionException, PmmException, InterruptedException {
		BufferedDataContainer buf = exec.createDataContainer(seiSchema
				.createSpec());

		getReplacementsFromNodeAssignments(assignments);

		if (replacements.isEmpty()) {
			replacements.add(new HashMap<String, String>());
		}

		for (int i = 0; i < usedModels.size(); i++) {
			KnimeRelationReader modelReader = new KnimeRelationReader(
					modelSchema, modelTable);

			while (modelReader.hasMoreElements()) {
				KnimeTuple modelRow = modelReader.nextElement();
				String modelIDSec = modelRow.getInt(Model2Schema.ATT_MODELID)
						+ "";
				String formulaSec = modelRow
						.getString(Model2Schema.ATT_FORMULA);
				String depVarSec = modelRow.getString(Model2Schema.ATT_DEPVAR);
				List<String> indepVarsSec = modelRow
						.getStringList(Model2Schema.ATT_INDEPVAR);

				KnimeRelationReader peiReader = new KnimeRelationReader(
						dataSchema, dataTable);

				while (peiReader.hasMoreElements()) {
					KnimeTuple peiRow = peiReader.nextElement();

					List<String> paramNames = peiRow
							.getStringList(Model1Schema.ATT_PARAMNAME);

					for (String symbol : replacements.get(i).keySet()) {
						String colname = replacements.get(i).get(symbol);

						formulaSec = MathUtilities.replaceVariable(formulaSec,
								symbol, colname);
						depVarSec = MathUtilities.replaceVariable(depVarSec,
								symbol, colname);

						if (indepVarsSec.contains(symbol)) {
							indepVarsSec.set(indepVarsSec.indexOf(symbol),
									colname);
						}
					}

					if (!usedModels.get(i).equals(modelIDSec)
							|| !paramNames.contains(depVarSec)) {
						continue;
					}

					KnimeTuple seiRow = new KnimeTuple(seiSchema, modelRow,
							peiRow);

					seiRow.setValue(Model2Schema.ATT_FORMULA, formulaSec);
					seiRow.setValue(Model2Schema.ATT_DEPVAR, depVarSec);
					seiRow.setValue(Model2Schema.ATT_INDEPVAR, indepVarsSec);
					seiRow.setValue(Model2Schema.ATT_DATABASEWRITABLE,
							Model2Schema.NOTWRITABLE);

					buf.addRowToTable(seiRow);
				}
			}
		}

		buf.close();

		return buf.getTable();
	}

	public boolean isValid() {
		return isValid;
	}

	private void readModelTable() throws PmmException {
		models = new ArrayList<String>();
		modelNames = new HashMap<String, String>();
		modelFormulas = new HashMap<String, String>();
		dependentVariables = new HashMap<String, String>();
		independentVariables = new HashMap<String, List<String>>();

		KnimeRelationReader reader = new KnimeRelationReader(
				new Model2Schema(), modelTable);

		while (reader.hasMoreElements()) {
			KnimeTuple row = reader.nextElement();
			String modelID = row.getInt(Model2Schema.ATT_MODELID) + "";

			if (dependentVariables.containsKey(modelID)) {
				continue;
			}

			models.add(modelID);
			modelNames.put(modelID, row.getString(Model2Schema.ATT_MODELNAME));
			modelFormulas.put(modelID, row.getString(Model2Schema.ATT_FORMULA));
			dependentVariables.put(modelID,
					row.getString(Model2Schema.ATT_DEPVAR));
			independentVariables.put(modelID,
					row.getStringList(Model2Schema.ATT_INDEPVAR));
		}
	}

	private void readDataTable() throws PmmException {
		independentParameters = new ArrayList<String>(Arrays.asList(
				TimeSeriesSchema.ATT_TEMPERATURE, TimeSeriesSchema.ATT_PH,
				TimeSeriesSchema.ATT_WATERACTIVITY));
		dependentParameters = new ArrayList<String>();

		KnimeRelationReader reader = new KnimeRelationReader(new KnimeSchema(
				new Model1Schema(), new TimeSeriesSchema()), dataTable);

		while (reader.hasMoreElements()) {
			KnimeTuple row = reader.nextElement();
			List<String> params = row.getStringList(Model1Schema.ATT_PARAMNAME);

			for (String param : params) {
				if (!dependentParameters.contains(param)) {
					dependentParameters.add(param);
				}
			}
		}
	}

	private void getReplacementsFromFrame() {
		usedModels = new ArrayList<String>();
		replacements = new ArrayList<Map<String, String>>();

		for (String model : comboBoxes.keySet()) {
			for (Map<String, JComboBox> modelBoxes : comboBoxes.get(model)) {
				Map<String, String> modelAssignments = new HashMap<String, String>();

				for (String var : modelBoxes.keySet()) {
					JComboBox box = modelBoxes.get(var);

					modelAssignments.put(var, (String) box.getSelectedItem());
				}

				usedModels.add(model);
				replacements.add(modelAssignments);
			}
		}
	}

	private void getReplacementsFromNodeAssignments(List<String> assignments) {
		usedModels = new ArrayList<String>();
		replacements = new ArrayList<Map<String, String>>();

		for (String s : assignments) {
			String[] toks = s.split(":");

			if (toks.length == 2) {
				String model = toks[0].trim();
				Map<String, String> modelReplacements = new HashMap<String, String>();

				for (String assignment : toks[1].split(",")) {
					String[] elements = assignment.split("=");

					if (elements.length == 2) {
						String variable = elements[0].trim();
						String parameter = elements[1].trim();

						modelReplacements.put(variable, parameter);
					}
				}

				usedModels.add(model);
				replacements.add(modelReplacements);
			}
		}
	}

	private void addOrRemoveButtonPressed(JButton button) {
		for (String model : addButtons.keySet()) {
			List<JButton> modelAddButtons = addButtons.get(model);
			List<JButton> modelRemoveButtons = removeButtons.get(model);
			List<Map<String, JComboBox>> modelBoxes = comboBoxes.get(model);
			JPanel leftPanel = boxPanels.get(model);
			JPanel rightPanel = buttonPanels.get(model);

			if (modelAddButtons.contains(button)) {
				int index = modelAddButtons.indexOf(button);
				Map<String, JComboBox> boxes = new HashMap<String, JComboBox>();
				JPanel assignmentPanel = new JPanel();

				assignmentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

				JComboBox depBox = new JComboBox(dependentParameters.toArray());

				depBox.addActionListener(this);
				boxes.put(dependentVariables.get(model), depBox);
				assignmentPanel.add(new JLabel(dependentVariables.get(model)
						+ ":"));
				assignmentPanel.add(depBox);

				for (String indepVar : independentVariables.get(model)) {
					JComboBox indepBox = new JComboBox(
							independentParameters.toArray());

					indepBox.addActionListener(this);
					boxes.put(indepVar, indepBox);
					assignmentPanel.add(new JLabel(indepVar + ":"));
					assignmentPanel.add(indepBox);
				}

				modelBoxes.add(index, boxes);
				leftPanel.add(assignmentPanel, index);

				JPanel buttonPanel = new JPanel();
				JButton addButton = new JButton("+");
				JButton removeButton = new JButton("-");

				addButton.addActionListener(this);
				removeButton.addActionListener(this);
				modelAddButtons.add(index, addButton);
				modelRemoveButtons.add(index, removeButton);
				buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
				buttonPanel.add(removeButton);
				buttonPanel.add(addButton);
				rightPanel.add(buttonPanel, index);
				leftPanel.revalidate();
				rightPanel.revalidate();

				break;
			} else if (modelRemoveButtons.contains(button)) {
				int index = modelRemoveButtons.indexOf(button);

				modelAddButtons.remove(index);
				modelRemoveButtons.remove(index);
				modelBoxes.remove(index);
				leftPanel.remove(index);
				rightPanel.remove(index);
				leftPanel.revalidate();
				rightPanel.revalidate();

				break;
			}
		}
	}

	private void checkIfInputIsValid() {
		Map<String, JComboBox> depVarBoxes = new HashMap<String, JComboBox>();
		isValid = true;

		for (String model : comboBoxes.keySet()) {
			String depVar = dependentVariables.get(model);

			for (Map<String, JComboBox> boxes : comboBoxes.get(model)) {
				JComboBox box = boxes.get(depVar);
				JComboBox sameValueBox = depVarBoxes.get(box.getSelectedItem());

				if (sameValueBox != null) {
					box.setForeground(Color.RED);
					sameValueBox.setForeground(Color.RED);
					isValid = false;
				} else {
					box.setForeground(Color.BLACK);
					depVarBoxes.put((String) box.getSelectedItem(), box);
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			checkIfInputIsValid();
		} else if (e.getSource() instanceof JButton) {
			addOrRemoveButtonPressed((JButton) e.getSource());
			checkIfInputIsValid();
		}
	}

}
