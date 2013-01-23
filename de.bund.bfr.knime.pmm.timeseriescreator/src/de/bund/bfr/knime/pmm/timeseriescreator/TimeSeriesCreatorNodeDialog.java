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
package de.bund.bfr.knime.pmm.timeseriescreator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.pmm.common.ListUtilities;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.PmmConstants;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.XLSReader;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.knime.pmm.common.ui.DoubleTextField;
import de.bund.bfr.knime.pmm.common.ui.IntTextField;
import de.bund.bfr.knime.pmm.common.ui.StringTextField;
import de.bund.bfr.knime.pmm.common.ui.TextListener;
import de.bund.bfr.knime.pmm.common.ui.TimeSeriesTable;

/**
 * <code>NodeDialog</code> for the "TimeSeriesCreator" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class TimeSeriesCreatorNodeDialog extends NodeDialogPane implements
		ActionListener {

	private static final int ROW_COUNT = 1000;
	private static final int DEFAULT_TIMESTEPNUMBER = 10;
	private static final double DEFAULT_TIMESTEPSIZE = 1.0;

	private static final String OTHER_PARAMETER = "Other Parameter";
	private static final String NO_PARAMETER = "None";

	private JPanel panel;
	private JButton clearButton;
	private JButton stepsButton;
	private JButton xlsButton;
	private TimeSeriesTable table;
	private StringTextField agentField;
	private StringTextField matrixField;
	private StringTextField commentField;
	private DoubleTextField temperatureField;
	private DoubleTextField phField;
	private DoubleTextField waterActivityField;
	private JComboBox<String> timeBox;
	private JComboBox<String> logcBox;
	private JComboBox<String> tempBox;

	private List<JButton> condButtons;
	private List<Integer> condIDs;
	private List<DoubleTextField> condValueFields;
	private List<JButton> addButtons;
	private List<JButton> removeButtons;

	private JPanel settingsNamePanel;
	private JPanel settingsValuePanel;
	private JPanel settingsUnitPanel;
	private JPanel addPanel;
	private JPanel removePanel;

	/**
	 * New pane for configuring the TimeSeriesCreator node.
	 */
	protected TimeSeriesCreatorNodeDialog() {
		condButtons = new ArrayList<>();
		condIDs = new ArrayList<>();
		condValueFields = new ArrayList<>();
		addButtons = new ArrayList<>();
		removeButtons = new ArrayList<>();

		panel = new JPanel();
		settingsNamePanel = new JPanel();
		settingsValuePanel = new JPanel();
		settingsUnitPanel = new JPanel();
		addPanel = new JPanel();
		removePanel = new JPanel();
		xlsButton = new JButton("Read from XLS file");
		xlsButton.addActionListener(this);
		stepsButton = new JButton("Set equidistant time steps");
		stepsButton.addActionListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		agentField = new StringTextField(true);
		matrixField = new StringTextField(true);
		commentField = new StringTextField(true);
		temperatureField = new DoubleTextField(true);
		phField = new DoubleTextField(PmmConstants.MIN_PH, PmmConstants.MAX_PH,
				true);
		waterActivityField = new DoubleTextField(
				PmmConstants.MIN_WATERACTIVITY, PmmConstants.MAX_WATERACTIVITY,
				true);
		timeBox = new JComboBox<String>(AttributeUtilities
				.getUnitsForAttribute(TimeSeriesSchema.TIME).toArray(
						new String[0]));
		logcBox = new JComboBox<String>(AttributeUtilities
				.getUnitsForAttribute(TimeSeriesSchema.LOGC).toArray(
						new String[0]));
		tempBox = new JComboBox<String>(AttributeUtilities
				.getUnitsForAttribute(AttributeUtilities.ATT_TEMPERATURE)
				.toArray(new String[0]));

		settingsNamePanel
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		settingsNamePanel.setLayout(new GridLayout(-1, 1, 5, 5));
		settingsNamePanel.add(new JLabel(AttributeUtilities
				.getFullName(TimeSeriesSchema.ATT_AGENTNAME) + ":"));
		settingsNamePanel.add(new JLabel(AttributeUtilities
				.getFullName(TimeSeriesSchema.ATT_MATRIXNAME) + ":"));
		settingsNamePanel.add(new JLabel(TimeSeriesSchema.ATT_COMMENT + ":"));
		settingsNamePanel.add(new JLabel(AttributeUtilities
				.getFullName(TimeSeriesSchema.TIME) + ":"));
		settingsNamePanel.add(new JLabel(AttributeUtilities
				.getFullName(TimeSeriesSchema.LOGC) + ":"));
		settingsNamePanel.add(new JLabel(AttributeUtilities
				.getFullName(AttributeUtilities.ATT_TEMPERATURE) + ":"));
		settingsNamePanel.add(new JLabel(AttributeUtilities
				.getFullName(AttributeUtilities.ATT_PH) + ":"));
		settingsNamePanel.add(new JLabel(AttributeUtilities
				.getFullName(AttributeUtilities.ATT_WATERACTIVITY) + ":"));

		settingsValuePanel.setBorder(BorderFactory
				.createEmptyBorder(5, 5, 5, 5));
		settingsValuePanel.setLayout(new GridLayout(-1, 1, 5, 5));
		settingsValuePanel.add(agentField);
		settingsValuePanel.add(matrixField);
		settingsValuePanel.add(commentField);
		addEmptyLabel(settingsValuePanel, 2);
		settingsValuePanel.add(temperatureField);
		settingsValuePanel.add(phField);
		settingsValuePanel.add(waterActivityField);

		settingsUnitPanel
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		settingsUnitPanel.setLayout(new GridLayout(-1, 1, 5, 5));
		addEmptyLabel(settingsUnitPanel, 3);
		settingsUnitPanel.add(timeBox);
		settingsUnitPanel.add(logcBox);
		settingsUnitPanel.add(tempBox);
		addEmptyLabel(settingsUnitPanel, 2);

		addPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		addPanel.setLayout(new GridLayout(-1, 1, 5, 5));
		addEmptyLabel(addPanel, 8);

		removePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		removePanel.setLayout(new GridLayout(-1, 1, 5, 5));
		addEmptyLabel(removePanel, 8);

		addButtons(0);

		JPanel panel4 = new JPanel();

		panel4.setLayout(new GridLayout(1, 2));
		panel4.add(addPanel);
		panel4.add(removePanel);

		JPanel panel3 = new JPanel();

		panel3.setLayout(new BorderLayout());
		panel3.add(settingsUnitPanel, BorderLayout.CENTER);
		panel3.add(panel4, BorderLayout.EAST);

		JPanel panel2 = new JPanel();

		panel2.setLayout(new BorderLayout());
		panel2.add(settingsValuePanel, BorderLayout.CENTER);
		panel2.add(panel3, BorderLayout.EAST);

		JPanel panel1 = new JPanel();

		panel1.setLayout(new BorderLayout());
		panel1.add(settingsNamePanel, BorderLayout.WEST);
		panel1.add(panel2, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(xlsButton);
		buttonPanel.add(stepsButton);
		buttonPanel.add(clearButton);

		table = new TimeSeriesTable(ROW_COUNT, true, true);
		panel.setLayout(new BorderLayout());
		panel.add(panel1, BorderLayout.NORTH);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final DataTableSpec[] specs) throws NotConfigurableException {
		try {
			agentField.setValue(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_AGENT));
		} catch (InvalidSettingsException e) {
		}

		try {
			matrixField.setValue(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_MATRIX));
		} catch (InvalidSettingsException e) {
		}

		try {
			commentField.setValue(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_COMMENT));
		} catch (InvalidSettingsException e) {
		}

		try {
			temperatureField.setValue(settings
					.getDouble(TimeSeriesCreatorNodeModel.CFGKEY_TEMPERATURE));
		} catch (InvalidSettingsException e) {
		}

		try {
			phField.setValue(settings
					.getDouble(TimeSeriesCreatorNodeModel.CFGKEY_PH));
		} catch (InvalidSettingsException e) {
		}

		try {
			waterActivityField
					.setValue(settings
							.getDouble(TimeSeriesCreatorNodeModel.CFGKEY_WATERACTIVITY));
		} catch (InvalidSettingsException e) {
		}

		try {
			List<Double> timeValues = ListUtilities
					.getDoubleListFromString(settings
							.getString(TimeSeriesCreatorNodeModel.CFGKEY_TIMEVALUES));

			for (int i = 0; i < timeValues.size(); i++) {
				if (!Double.isNaN(timeValues.get(i))) {
					table.setTime(i, timeValues.get(i));
				}
			}
		} catch (InvalidSettingsException e) {
		} catch (NullPointerException e) {
		}

		try {
			List<Double> logcValues = ListUtilities
					.getDoubleListFromString(settings
							.getString(TimeSeriesCreatorNodeModel.CFGKEY_LOGCVALUES));

			for (int i = 0; i < logcValues.size(); i++) {
				if (!Double.isNaN(logcValues.get(i))) {
					table.setLogc(i, logcValues.get(i));
				}
			}
		} catch (InvalidSettingsException e) {
		} catch (NullPointerException e) {
		}

		try {
			timeBox.setSelectedItem(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_TIMEUNIT));
		} catch (InvalidSettingsException e) {
			timeBox.setSelectedItem(AttributeUtilities
					.getStandardUnit(TimeSeriesSchema.TIME));
		}

		try {
			logcBox.setSelectedItem(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_LOGCUNIT));
		} catch (InvalidSettingsException e) {
			logcBox.setSelectedItem(AttributeUtilities
					.getStandardUnit(TimeSeriesSchema.LOGC));
		}

		try {
			tempBox.setSelectedItem(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_TEMPUNIT));
		} catch (InvalidSettingsException e) {
			tempBox.setSelectedItem(AttributeUtilities
					.getStandardUnit(AttributeUtilities.ATT_TEMPERATURE));
		}

		List<Integer> miscIDs;
		List<Double> miscValues;
		int n = removeButtons.size();

		try {
			miscIDs = ListUtilities.getIntListFromString(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_MISCIDS));
		} catch (InvalidSettingsException e) {
			miscIDs = new ArrayList<>();
		}

		try {
			miscValues = ListUtilities.getDoubleListFromString(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_MISCVALUES));
		} catch (InvalidSettingsException e) {
			miscValues = new ArrayList<Double>();
		}

		for (int i = 0; i < n; i++) {
			removeButtons(0);
		}

		for (int i = 0; i < miscIDs.size(); i++) {
			int id = miscIDs.get(i);
			Double value = miscValues.get(i);

			if (value != null && value.isNaN()) {
				value = null;
			}

			addButtons(0);
			condButtons.get(0).setText(
					DBKernel.getValue("SonstigeParameter", "ID", id + "",
							"Parameter") + "");
			condIDs.set(0, id);
			condValueFields.get(0).setValue(value);
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		if (!temperatureField.isValueValid()) {
			throw new InvalidSettingsException("Invalid Value");
		}

		if (!phField.isValueValid()) {
			throw new InvalidSettingsException("Invalid Value");
		}

		if (!waterActivityField.isValueValid()) {
			throw new InvalidSettingsException("Invalid Value");
		}

		for (Integer id : condIDs) {
			if (id == null) {
				throw new InvalidSettingsException("Invalid Value");
			}
		}

		for (int i = 0; i < condValueFields.size(); i++) {
			if (!condValueFields.get(i).isValueValid()) {
				throw new InvalidSettingsException("Invalid Value");
			}
		}

		if (agentField.getValue() != null) {
			settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_AGENT,
					agentField.getValue());
		}

		if (matrixField.getValue() != null) {
			settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_MATRIX,
					matrixField.getValue());
		}

		if (commentField.getValue() != null) {
			settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_COMMENT,
					commentField.getValue());
		}

		if (temperatureField.getValue() != null) {
			settings.addDouble(TimeSeriesCreatorNodeModel.CFGKEY_TEMPERATURE,
					temperatureField.getValue());
		}

		if (phField.getValue() != null) {
			settings.addDouble(TimeSeriesCreatorNodeModel.CFGKEY_PH,
					phField.getValue());
		}

		if (waterActivityField.getValue() != null) {
			settings.addDouble(TimeSeriesCreatorNodeModel.CFGKEY_WATERACTIVITY,
					waterActivityField.getValue());
		}

		List<Double> timeList = new ArrayList<Double>();
		List<Double> logcList = new ArrayList<Double>();

		for (int i = 0; i < ROW_COUNT; i++) {
			Double time = table.getTime(i);
			Double logc = table.getLogc(i);

			if (time != null || logc != null) {
				timeList.add(time);
				logcList.add(logc);
			}
		}

		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_TIMEVALUES,
				ListUtilities.getStringFromList(timeList));
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_LOGCVALUES,
				ListUtilities.getStringFromList(logcList));
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_TIMEUNIT,
				(String) timeBox.getSelectedItem());
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_LOGCUNIT,
				(String) logcBox.getSelectedItem());
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_TEMPUNIT,
				(String) tempBox.getSelectedItem());

		List<Double> miscValues = new ArrayList<Double>();

		for (int i = 0; i < condValueFields.size(); i++) {
			miscValues.add(condValueFields.get(i).getValue());
		}

		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_MISCIDS,
				ListUtilities.getStringFromList(condIDs));
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_MISCVALUES,
				ListUtilities.getStringFromList(miscValues));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == xlsButton) {
			loadFromXLS();
		} else if (event.getSource() == clearButton) {
			int n = removeButtons.size();

			agentField.setValue(null);
			matrixField.setValue(null);
			commentField.setValue(null);
			temperatureField.setValue(null);
			phField.setValue(null);
			waterActivityField.setValue(null);

			for (int i = 0; i < n; i++) {
				removeButtons(0);
			}

			for (int i = 0; i < ROW_COUNT; i++) {
				table.setTime(i, null);
				table.setLogc(i, null);
			}

			panel.revalidate();
			table.repaint();
		} else if (event.getSource() == stepsButton) {
			TimeStepDialog dialog = new TimeStepDialog(panel);

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				int stepNumber = dialog.getNumberOfSteps();
				double stepSize = dialog.getStepSize();

				for (int i = 0; i < ROW_COUNT; i++) {
					Double time = null;

					if (i < stepNumber) {
						time = i * stepSize;
					}

					table.setTime(i, time);
					table.setLogc(i, null);
				}

				table.repaint();
			}
		} else if (addButtons.contains(event.getSource())) {
			addButtons(addButtons.indexOf(event.getSource()));
			panel.revalidate();
		} else if (removeButtons.contains(event.getSource())) {
			removeButtons(removeButtons.indexOf(event.getSource()));
			panel.revalidate();
		} else if (condButtons.contains(event.getSource())) {
			int i = condButtons.indexOf(event.getSource());
			Integer miscID = openDBWindow(condIDs.get(i));

			if (miscID != null) {
				String misc = ""
						+ DBKernel.getValue("SonstigeParameter", "ID", miscID
								+ "", "Parameter");

				condButtons.get(i).setText(misc);
				condIDs.set(i, miscID);
			}
		}

	}

	private void addEmptyLabel(JPanel panel, int n) {
		for (int i = 0; i < n; i++) {
			panel.add(new JLabel());
		}
	}

	private void addButtons(int i) {
		if (addButtons.isEmpty()) {
			JButton addButton = new JButton("+");

			addButton.addActionListener(this);

			addButtons.add(0, addButton);
			addEmptyLabel(settingsNamePanel, 1);
			addEmptyLabel(settingsValuePanel, 1);
			addEmptyLabel(settingsUnitPanel, 1);
			addPanel.add(addButton);
			addEmptyLabel(removePanel, 1);
		} else {
			int panelIndex = i + 8;
			JButton addButton = new JButton("+");
			JButton removeButton = new JButton("-");
			JButton button = new JButton(NO_PARAMETER);
			DoubleTextField valueField = new DoubleTextField(true);

			addButton.addActionListener(this);
			removeButton.addActionListener(this);
			button.addActionListener(this);

			addButtons.add(i, addButton);
			removeButtons.add(i, removeButton);
			condIDs.add(i, null);
			condButtons.add(i, button);
			condValueFields.add(i, valueField);
			addPanel.add(addButton, panelIndex);
			removePanel.add(removeButton, panelIndex);
			settingsNamePanel.add(button, panelIndex);
			settingsValuePanel.add(valueField, panelIndex);
			settingsUnitPanel.add(new JLabel(), panelIndex);
		}
	}

	private void removeButtons(int i) {
		int panelIndex = i + 8;

		addButtons.remove(i);
		removeButtons.remove(i);
		condIDs.remove(i);
		condButtons.remove(i);
		condValueFields.remove(i);
		addPanel.remove(panelIndex);
		removePanel.remove(panelIndex);
		settingsNamePanel.remove(panelIndex);
		settingsValuePanel.remove(panelIndex);
		settingsUnitPanel.remove(panelIndex);
	}

	private Integer openDBWindow(Integer id) {
		MyTable myT = DBKernel.myList.getTable("SonstigeParameter");
		Object newVal = DBKernel.myList.openNewWindow(myT, id,
				"SonstigeParameter", null, null, null, null, true);

		if (newVal instanceof Integer) {
			return (Integer) newVal;
		} else {
			return null;
		}
	}

	private void loadFromXLS() {
		JFileChooser fileChooser = new JFileChooser();
		FileFilter xlsFilter = new FileFilter() {

			@Override
			public String getDescription() {
				return "Excel Spreadsheat (*.xls)";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".xls");
			}
		};

		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(xlsFilter);

		if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
			try {
				XLSDialog dialog = new XLSDialog(panel,
						fileChooser.getSelectedFile());

				dialog.setVisible(true);

				if (!dialog.isApproved()) {
					return;
				}

				Map<String, KnimeTuple> tuples = XLSReader.getTimeSeriesTuples(
						fileChooser.getSelectedFile(), dialog.getMappings());
				Object[] values = tuples.keySet().toArray();
				Object selection = JOptionPane.showInputDialog(panel,
						"Select Time Series", "Input",
						JOptionPane.QUESTION_MESSAGE, null, values, values[0]);
				KnimeTuple tuple = tuples.get(selection);

				agentField.setValue(tuple
						.getString(TimeSeriesSchema.ATT_AGENTDETAIL));
				matrixField.setValue(tuple
						.getString(TimeSeriesSchema.ATT_MATRIXDETAIL));
				commentField.setValue(tuple
						.getString(TimeSeriesSchema.ATT_COMMENT));

				PmmXmlDoc miscXML = tuple.getPmmXml(TimeSeriesSchema.ATT_MISC);
				int n = removeButtons.size();

				for (int i = 0; i < n; i++) {
					removeButtons(0);
				}

				for (int i = 0; i < miscXML.getElementSet().size(); i++) {
					MiscXml misc = (MiscXml) miscXML.getElementSet().get(i);
					int id = misc.getID();
					String name = misc.getName();
					Double value = misc.getValue();

					if (value != null && value.isNaN()) {
						value = null;
					}

					if (id == AttributeUtilities.ATT_TEMPERATURE_ID) {
						temperatureField.setValue(value);
					} else if (id == AttributeUtilities.ATT_PH_ID) {
						phField.setValue(value);
					} else if (id == AttributeUtilities.ATT_AW_ID) {
						waterActivityField.setValue(value);
					} else {
						addButtons(0);
						condButtons.get(0).setText(name);
						condIDs.set(0, id);
						condValueFields.get(0).setValue(value);
					}
				}

				PmmXmlDoc timeSeriesXml = tuple
						.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES);
				int count = timeSeriesXml.getElementSet().size();

				if (count > ROW_COUNT) {
					JOptionPane.showMessageDialog(panel,
							"Number of measured points XLS-file exceeds maximum number of rows ("
									+ ROW_COUNT + ")", "Warning",
							JOptionPane.WARNING_MESSAGE);
				}

				for (int i = 0; i < ROW_COUNT; i++) {
					Double time = null;
					Double logc = null;

					if (i < count) {
						time = ((TimeSeriesXml) timeSeriesXml.get(i)).getTime();
						logc = ((TimeSeriesXml) timeSeriesXml.get(i))
								.getLog10C();
					}

					table.setTime(i, time);
					table.setLogc(i, logc);
				}

				panel.revalidate();
				table.repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class XLSDialog extends JDialog implements ActionListener,
			ItemListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;

		private Map<String, JComboBox<String>> mappingBoxes;
		private Map<String, JButton> mappingButtons;
		private Map<String, MiscXml> mappings;

		private JButton okButton;
		private JButton cancelButton;

		public XLSDialog(Component owner, File file) throws Exception {
			super(JOptionPane.getFrameForComponent(owner), "XLS File", true);

			approved = false;

			mappings = new LinkedHashMap<>();
			mappingBoxes = new LinkedHashMap<>();
			mappingButtons = new LinkedHashMap<>();

			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);

			List<String> columnList = XLSReader.getTimeSeriesMiscColumns(file);
			JPanel northPanel = new JPanel();
			int row = 0;

			northPanel.setLayout(new GridBagLayout());

			for (String column : columnList) {
				JComboBox<String> box = new JComboBox<>(new String[] {
						AttributeUtilities.ATT_TEMPERATURE,
						AttributeUtilities.ATT_PH,
						AttributeUtilities.ATT_WATERACTIVITY, OTHER_PARAMETER });
				JButton button = new JButton();

				box.setSelectedItem(OTHER_PARAMETER);
				button.setEnabled(true);
				button.setText(NO_PARAMETER);

				box.addItemListener(this);
				button.addActionListener(this);

				mappings.put(column, null);
				mappingBoxes.put(column, box);
				mappingButtons.put(column, button);

				northPanel.add(new JLabel(column + ":"),
						createConstraints(0, row));
				northPanel.add(box, createConstraints(1, row));
				northPanel.add(button, createConstraints(2, row));
				row++;
			}

			JPanel bottomPanel = new JPanel();

			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			bottomPanel.add(okButton);
			bottomPanel.add(cancelButton);

			setLayout(new BorderLayout());
			add(northPanel, BorderLayout.CENTER);
			add(bottomPanel, BorderLayout.SOUTH);
			pack();

			setResizable(false);
			setLocationRelativeTo(owner);
		}

		public boolean isApproved() {
			return approved;
		}

		public Map<String, MiscXml> getMappings() {
			return mappings;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				for (String column : mappingButtons.keySet()) {
					if (e.getSource() == mappingBoxes.get(column)) {
						JComboBox<String> box = mappingBoxes.get(column);
						JButton button = mappingButtons.get(column);

						if (box.getSelectedItem().equals(
								AttributeUtilities.ATT_TEMPERATURE)) {
							button.setEnabled(false);
							button.setText(NO_PARAMETER);
							mappings.put(column, new MiscXml(
									AttributeUtilities.ATT_TEMPERATURE_ID,
									AttributeUtilities.ATT_TEMPERATURE, null,
									null, null));
						} else if (box.getSelectedItem().equals(
								AttributeUtilities.ATT_PH)) {
							button.setEnabled(false);
							button.setText(NO_PARAMETER);
							mappings.put(column,
									new MiscXml(AttributeUtilities.ATT_PH_ID,
											AttributeUtilities.ATT_PH, null,
											null, null));
						} else if (box.getSelectedItem().equals(
								AttributeUtilities.ATT_WATERACTIVITY)) {
							button.setEnabled(false);
							button.setText(NO_PARAMETER);
							mappings.put(column, new MiscXml(
									AttributeUtilities.ATT_AW_ID,
									AttributeUtilities.ATT_WATERACTIVITY, null,
									null, null));
						} else {
							button.setEnabled(true);
							button.setText(NO_PARAMETER);
							mappings.put(column, null);
						}

						break;
					}
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				for (MiscXml misc : mappings.values()) {
					if (misc == null) {
						JOptionPane.showMessageDialog(this,
								"All Columns must be assigned", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				approved = true;
				dispose();
			} else if (e.getSource() == cancelButton) {
				dispose();
			} else {
				for (String column : mappingButtons.keySet()) {
					if (e.getSource() == mappingButtons.get(column)) {
						Integer oldID = null;

						if (mappings.get(column) != null) {
							oldID = mappings.get(column).getID();
						}

						Integer miscID = openDBWindow(oldID);

						if (miscID != null) {
							String misc = ""
									+ DBKernel.getValue("SonstigeParameter",
											"ID", miscID + "", "Parameter");

							mappingButtons.get(column).setText(misc);
							mappings.put(column, new MiscXml(miscID, misc,
									null, null, null));
							pack();
						}

						break;
					}
				}
			}
		}

		private GridBagConstraints createConstraints(int x, int y) {
			return new GridBagConstraints(x, y, 1, 1, 0, 0,
					GridBagConstraints.LINE_START, GridBagConstraints.NONE,
					new Insets(2, 2, 2, 2), 0, 0);
		}
	}

	private class TimeStepDialog extends JDialog implements ActionListener,
			TextListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private int numberOfSteps;
		private double stepSize;

		private IntTextField numberField;
		private DoubleTextField sizeField;

		private JButton okButton;
		private JButton cancelButton;

		public TimeStepDialog(Component owner) {
			super(JOptionPane.getFrameForComponent(owner), "Time Steps", true);

			approved = false;
			numberOfSteps = 0;
			stepSize = 0.0;

			numberField = new IntTextField(1, ROW_COUNT);
			numberField.setValue(DEFAULT_TIMESTEPNUMBER);
			numberField.setPreferredSize(new Dimension(150, numberField
					.getPreferredSize().height));
			numberField.addTextListener(this);
			sizeField = new DoubleTextField(0.0, Double.POSITIVE_INFINITY);
			sizeField.setPreferredSize(new Dimension(150, sizeField
					.getPreferredSize().height));
			sizeField.setValue(DEFAULT_TIMESTEPSIZE);
			sizeField.addTextListener(this);
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);

			JPanel centerPanel = new JPanel();
			JPanel leftPanel = new JPanel();
			JPanel rightPanel = new JPanel();
			JPanel bottomPanel = new JPanel();

			leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			leftPanel.setLayout(new GridLayout(2, 1, 5, 5));
			leftPanel.add(new JLabel("Number of Time Steps:"));
			leftPanel.add(new JLabel("Step Size:"));

			rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			rightPanel.setLayout(new GridLayout(2, 1, 5, 5));
			rightPanel.add(numberField);
			rightPanel.add(sizeField);

			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(leftPanel, BorderLayout.WEST);
			centerPanel.add(rightPanel, BorderLayout.CENTER);

			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			bottomPanel.add(okButton);
			bottomPanel.add(cancelButton);

			setLayout(new BorderLayout());
			add(centerPanel, BorderLayout.CENTER);
			add(bottomPanel, BorderLayout.SOUTH);
			pack();

			setResizable(false);
			setLocationRelativeTo(owner);
		}

		public boolean isApproved() {
			return approved;
		}

		public int getNumberOfSteps() {
			return numberOfSteps;
		}

		public double getStepSize() {
			return stepSize;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				approved = true;
				numberOfSteps = numberField.getValue();
				stepSize = sizeField.getValue();
				dispose();
			} else if (e.getSource() == cancelButton) {
				dispose();
			}
		}

		@Override
		public void textChanged() {
			if (numberField.isValueValid() && sizeField.isValueValid()) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}
	}

}
