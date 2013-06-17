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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.hsh.bfr.db.DBKernel;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.pmm.common.AgentXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MdInfoXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.PmmConstants;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.XLSReader;
import de.bund.bfr.knime.pmm.common.XmlConverter;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.knime.pmm.common.ui.DoubleTextField;
import de.bund.bfr.knime.pmm.common.ui.IntTextField;
import de.bund.bfr.knime.pmm.common.ui.StringTextField;
import de.bund.bfr.knime.pmm.common.ui.TextListener;
import de.bund.bfr.knime.pmm.common.ui.TimeSeriesTable;
import de.bund.bfr.knime.pmm.common.units.NumberConcentration;
import de.bund.bfr.knime.pmm.common.units.NumberContent;
import de.bund.bfr.knime.pmm.common.units.Categories;
import de.bund.bfr.knime.pmm.common.units.Category;
import de.bund.bfr.knime.pmm.common.units.PH;
import de.bund.bfr.knime.pmm.common.units.Temperature;
import de.bund.bfr.knime.pmm.common.units.Time;
import de.bund.bfr.knime.pmm.common.units.WaterActivity;

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
	private static final String NO_PARAMETER = "Do Not Use";
	private static final String SELECT = "Select";

	private XLSReader xlsReader;

	private JPanel panel;
	private JButton clearButton;
	private JButton stepsButton;
	private JButton xlsButton;
	private TimeSeriesTable table;
	private JButton addLiteratureButton;
	private JButton removeLiteratureButton;
	private JList<String> literatureList;
	private List<LiteratureItem> literature;
	private List<String> literatureData;
	private AgentXml agent;
	private JButton agentButton;
	private MatrixXml matrix;
	private JButton matrixButton;
	private StringTextField commentField;
	private DoubleTextField temperatureField;
	private DoubleTextField phField;
	private DoubleTextField waterActivityField;
	private JComboBox<String> timeBox;
	private JComboBox<String> logcBox;
	private JComboBox<String> tempBox;
	private JComboBox<String> phBox;
	private JComboBox<String> awBox;

	private List<MiscXml> conditions;
	private List<JButton> condButtons;
	private List<DoubleTextField> condValueFields;
	private List<JComboBox<String>> condUnitFields;
	private List<JButton> addButtons;
	private List<JButton> removeButtons;

	private JPanel settingsPanel;
	private int settingsPanelRows;

	/**
	 * New pane for configuring the TimeSeriesCreator node.
	 */
	protected TimeSeriesCreatorNodeDialog() {
		List<String> concentrationUnits = new ArrayList<>();

		concentrationUnits.addAll(new NumberContent().getAllUnits());
		concentrationUnits.addAll(new NumberConcentration().getAllUnits());

		xlsReader = new XLSReader();

		condButtons = new ArrayList<>();
		conditions = new ArrayList<>();
		condValueFields = new ArrayList<>();
		condUnitFields = new ArrayList<>();
		addButtons = new ArrayList<>();
		removeButtons = new ArrayList<>();

		panel = new JPanel();
		settingsPanel = new JPanel();
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		settingsPanel.setLayout(new GridBagLayout());
		xlsButton = new JButton("Read from XLS file");
		xlsButton.addActionListener(this);
		stepsButton = new JButton("Set equidistant time steps");
		stepsButton.addActionListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		addLiteratureButton = new JButton("+");
		addLiteratureButton.addActionListener(this);
		removeLiteratureButton = new JButton("-");
		removeLiteratureButton.addActionListener(this);
		literatureList = new JList<>();
		agentButton = new JButton(SELECT);
		agentButton.addActionListener(this);
		matrixButton = new JButton(SELECT);
		matrixButton.addActionListener(this);
		commentField = new StringTextField(true);
		temperatureField = new DoubleTextField(true);
		temperatureField.setPreferredSize(new Dimension(100, temperatureField
				.getPreferredSize().height));
		phField = new DoubleTextField(PmmConstants.MIN_PH, PmmConstants.MAX_PH,
				true);
		phField.setPreferredSize(new Dimension(100,
				phField.getPreferredSize().height));
		waterActivityField = new DoubleTextField(
				PmmConstants.MIN_WATERACTIVITY, PmmConstants.MAX_WATERACTIVITY,
				true);
		waterActivityField.setPreferredSize(new Dimension(100,
				waterActivityField.getPreferredSize().height));
		timeBox = new JComboBox<String>(new Time().getAllUnits().toArray(
				new String[0]));
		logcBox = new JComboBox<String>(
				concentrationUnits.toArray(new String[0]));
		tempBox = new JComboBox<String>(new Temperature().getAllUnits()
				.toArray(new String[0]));
		phBox = new JComboBox<String>(new PH().getAllUnits().toArray(
				new String[0]));
		awBox = new JComboBox<String>(new WaterActivity().getAllUnits()
				.toArray(new String[0]));

		settingsPanel.add(
				new JLabel(AttributeUtilities
						.getName(TimeSeriesSchema.ATT_LITMD) + ":"),
				createConstraints(0, 0));
		settingsPanel.add(
				new JLabel(AttributeUtilities
						.getName(TimeSeriesSchema.ATT_AGENT) + ":"),
				createConstraints(0, 2));
		settingsPanel.add(
				new JLabel(AttributeUtilities
						.getName(TimeSeriesSchema.ATT_MATRIX) + ":"),
				createConstraints(0, 3));
		settingsPanel.add(new JLabel(MdInfoXml.ATT_COMMENT + ":"),
				createConstraints(0, 4));
		settingsPanel.add(
				new JLabel(AttributeUtilities.getName(AttributeUtilities.TIME)
						+ ":"), createConstraints(0, 5));
		settingsPanel.add(
				new JLabel(AttributeUtilities.getName(AttributeUtilities.LOGC)
						+ ":"), createConstraints(0, 6));
		settingsPanel.add(
				new JLabel(AttributeUtilities
						.getName(AttributeUtilities.ATT_TEMPERATURE) + ":"),
				createConstraints(0, 7));
		settingsPanel.add(
				new JLabel(AttributeUtilities
						.getName(AttributeUtilities.ATT_PH) + ":"),
				createConstraints(0, 8));
		settingsPanel.add(
				new JLabel(AttributeUtilities
						.getName(AttributeUtilities.ATT_WATERACTIVITY) + ":"),
				createConstraints(0, 9));

		settingsPanel.add(new JScrollPane(literatureList),
				new GridBagConstraints(1, 0, 2, 2, 0, 2,
						GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
						new Insets(3, 3, 3, 3), 0, 0));
		settingsPanel.add(agentButton, createConstraints(1, 2));
		settingsPanel.add(matrixButton, createConstraints(1, 3));
		settingsPanel.add(commentField, createConstraints(1, 4));
		settingsPanel.add(temperatureField, createConstraints(1, 7));
		settingsPanel.add(phField, createConstraints(1, 8));
		settingsPanel.add(waterActivityField, createConstraints(1, 9));

		settingsPanel.add(timeBox, createConstraints(2, 5));
		settingsPanel.add(logcBox, createConstraints(2, 6));
		settingsPanel.add(tempBox, createConstraints(2, 7));
		settingsPanel.add(phBox, createConstraints(2, 8));
		settingsPanel.add(awBox, createConstraints(2, 9));

		settingsPanel.add(addLiteratureButton, createConstraints(3, 0));
		settingsPanel.add(removeLiteratureButton, createConstraints(4, 0));

		settingsPanelRows = 10;

		addButtons(0);

		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(xlsButton);
		buttonPanel.add(stepsButton);
		buttonPanel.add(clearButton);

		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(settingsPanel, BorderLayout.WEST);

		table = new TimeSeriesTable(ROW_COUNT, true, true);
		panel.setLayout(new BorderLayout());
		panel.add(northPanel, BorderLayout.NORTH);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final DataTableSpec[] specs) throws NotConfigurableException {
		try {
			literature = XmlConverter.xmlToObject(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_LITERATURE),
					new ArrayList<LiteratureItem>());
			literatureData = new ArrayList<>();

			for (LiteratureItem item : literature) {
				literatureData.add(item.getAuthor() + "-" + item.getYear());
			}

			literatureList.setListData(literatureData.toArray(new String[0]));
		} catch (InvalidSettingsException e) {
			literature = new ArrayList<>();
			literatureData = new ArrayList<>();
			literatureList.setListData(literatureData.toArray(new String[0]));
		}

		try {
			agent = XmlConverter
					.xmlToObject(
							settings.getString(TimeSeriesCreatorNodeModel.CFGKEY_AGENT),
							null);
		} catch (InvalidSettingsException e) {
			agent = null;
		}

		try {
			matrix = XmlConverter.xmlToObject(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_MATRIX), null);
		} catch (InvalidSettingsException e) {
			matrix = null;
		}

		try {
			commentField.setValue(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_COMMENT));
		} catch (InvalidSettingsException e) {
		}

		try {
			List<TimeSeriesXml> timeSeries = XmlConverter.xmlToObject(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_TIMESERIES),
					new ArrayList<TimeSeriesXml>());

			for (int i = 0; i < timeSeries.size(); i++) {
				table.setTime(i, timeSeries.get(i).getTime());
				table.setLogc(i, timeSeries.get(i).getConcentration());
			}
		} catch (InvalidSettingsException e) {
		} catch (NullPointerException e) {
		}

		try {
			timeBox.setSelectedItem(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_TIMEUNIT));
		} catch (InvalidSettingsException e) {
			timeBox.setSelectedItem(TimeSeriesCreatorNodeModel.DEFAULT_TIMEUNIT);
		}

		try {
			logcBox.setSelectedItem(settings
					.getString(TimeSeriesCreatorNodeModel.CFGKEY_LOGCUNIT));
		} catch (InvalidSettingsException e) {
			logcBox.setSelectedItem(TimeSeriesCreatorNodeModel.DEFAULT_LOGCUNIT);
		}

		List<MiscXml> miscValues;
		int n = removeButtons.size();

		try {
			miscValues = XmlConverter.xmlToObject(
					settings.getString(TimeSeriesCreatorNodeModel.CFGKEY_MISC),
					new ArrayList<MiscXml>());
		} catch (InvalidSettingsException e) {
			miscValues = new ArrayList<>();
		}

		for (int i = 0; i < n; i++) {
			removeButtons(0);
		}

		for (MiscXml misc : miscValues) {
			if (misc.getID() == AttributeUtilities.ATT_TEMPERATURE_ID) {
				temperatureField.setValue(misc.getValue());
				tempBox.setSelectedItem(misc.getUnit());
			} else if (misc.getID() == AttributeUtilities.ATT_PH_ID) {
				phField.setValue(misc.getValue());
				phBox.setSelectedItem(misc.getUnit());
			} else if (misc.getID() == AttributeUtilities.ATT_AW_ID) {
				waterActivityField.setValue(misc.getValue());
				awBox.setSelectedItem(misc.getUnit());
			} else {
				addButtons(0);
				condButtons.get(0).setText(misc.getName());
				conditions.set(0, misc);
				condValueFields.get(0).setValue(misc.getValue());

				for (String category : misc.getCategories()) {
					for (String u : Categories.getCategory(category)
							.getAllUnits()) {
						condUnitFields.get(0).addItem(u);
					}
				}

				condUnitFields.get(0).setSelectedItem(misc.getUnit());
			}
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

		for (MiscXml m : conditions) {
			if (m == null) {
				throw new InvalidSettingsException("Invalid Value");
			}
		}

		for (int i = 0; i < condValueFields.size(); i++) {
			if (!condValueFields.get(i).isValueValid()) {
				throw new InvalidSettingsException("Invalid Value");
			}
		}

		if (commentField.getValue() != null) {
			settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_COMMENT,
					commentField.getValue());
		}

		List<TimeSeriesXml> timeSeries = new ArrayList<>();
		String timeUnit = (String) timeBox.getSelectedItem();
		String concentrationUnit = (String) logcBox.getSelectedItem();

		for (int i = 0; i < ROW_COUNT; i++) {
			Double time = table.getTime(i);
			Double logc = table.getLogc(i);

			if (time != null || logc != null) {
				timeSeries.add(new TimeSeriesXml(null, time, timeUnit, logc,
						concentrationUnit));
			}
		}

		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_LITERATURE,
				XmlConverter.objectToXml(literature));
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_AGENT,
				XmlConverter.objectToXml(agent));
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_MATRIX,
				XmlConverter.objectToXml(matrix));
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_TIMESERIES,
				XmlConverter.objectToXml(timeSeries));
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_TIMEUNIT,
				(String) timeBox.getSelectedItem());
		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_LOGCUNIT,
				(String) logcBox.getSelectedItem());

		List<MiscXml> miscValues = new ArrayList<>();

		if (temperatureField.getValue() != null) {
			miscValues.add(new MiscXml(AttributeUtilities.ATT_TEMPERATURE_ID,
					AttributeUtilities.ATT_TEMPERATURE, null, temperatureField
							.getValue(), Arrays.asList(Categories.TEMPERATURE),
					(String) tempBox.getSelectedItem()));
		}

		if (phField.getValue() != null) {
			miscValues.add(new MiscXml(AttributeUtilities.ATT_PH_ID,
					AttributeUtilities.ATT_PH, null, phField.getValue(), Arrays
							.asList(Categories.PH), (String) phBox
							.getSelectedItem()));
		}

		if (waterActivityField.getValue() != null) {
			miscValues.add(new MiscXml(AttributeUtilities.ATT_AW_ID,
					AttributeUtilities.ATT_WATERACTIVITY, null,
					waterActivityField.getValue(), Arrays
							.asList(Categories.WATER_ACTIVITY), (String) awBox
							.getSelectedItem()));
		}

		for (int i = 0; i < conditions.size(); i++) {
			MiscXml cond = conditions.get(i);

			cond.setValue(condValueFields.get(i).getValue());

			if (condUnitFields.get(i).getSelectedItem() != null) {
				cond.setUnit((String) condUnitFields.get(i).getSelectedItem());
			}

			miscValues.add(cond);
		}

		settings.addString(TimeSeriesCreatorNodeModel.CFGKEY_MISC,
				XmlConverter.objectToXml(miscValues));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == xlsButton) {
			loadFromXLS();
		} else if (event.getSource() == clearButton) {
			int n = removeButtons.size();

			agentButton.setText(SELECT);
			agent = null;
			matrixButton.setText(SELECT);
			matrix = null;
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
		} else if (event.getSource() == addLiteratureButton) {
			Integer id = DBKernel.openLiteratureDBWindow(null);
			Set<Integer> ids = new LinkedHashSet<>();

			for (LiteratureItem item : literature) {
				ids.add(item.getID());
			}

			if (id != null && !ids.contains(id)) {
				String author = DBKernel.getValue("Literatur", "ID", id + "",
						"Erstautor") + "";
				String year = DBKernel.getValue("Literatur", "ID", id + "",
						"Jahr") + "";
				String title = DBKernel.getValue("Literatur", "ID", id + "",
						"Titel") + "";
				String mAbstract = DBKernel.getValue("Literatur", "ID",
						id + "", "Abstract") + "";

				literature.add(new LiteratureItem(author, Integer
						.parseInt(year), title, mAbstract, id));
				literatureData.add(author + "-" + year);
				literatureList.setListData(literatureData
						.toArray(new String[0]));
			}
		} else if (event.getSource() == removeLiteratureButton) {
			if (literatureList.getSelectedIndices().length > 0) {
				int[] indices = literatureList.getSelectedIndices();

				Arrays.sort(indices);

				for (int i = indices.length - 1; i >= 0; i--) {
					literature.remove(i);
					literatureData.remove(i);
				}

				literatureList.setListData(literatureData
						.toArray(new String[0]));
			}
		} else if (event.getSource() == agentButton) {
			Integer id;

			if (agent != null) {
				id = DBKernel.openAgentDBWindow(agent.getID());
			} else {
				id = DBKernel.openAgentDBWindow(null);
			}

			if (id != null) {
				String name = DBKernel.getValue("Agenzien", "ID", id + "",
						"Agensname") + "";

				agent = new AgentXml(id, name, null, DBKernel.getLocalDBUUID());
				agentButton.setText(name);
			}
		} else if (event.getSource() == matrixButton) {
			Integer id;

			if (matrix != null) {
				id = DBKernel.openMatrixDBWindow(matrix.getID());
			} else {
				id = DBKernel.openMatrixDBWindow(null);
			}

			if (id != null) {
				String name = DBKernel.getValue("Matrices", "ID", id + "",
						"Matrixname") + "";

				matrix = new MatrixXml(id, name, null,
						DBKernel.getLocalDBUUID());
				matrixButton.setText(name);
			}
		} else if (addButtons.contains(event.getSource())) {
			addButtons(addButtons.indexOf(event.getSource()));
			panel.revalidate();
		} else if (removeButtons.contains(event.getSource())) {
			removeButtons(removeButtons.indexOf(event.getSource()));
			panel.revalidate();
		} else if (condButtons.contains(event.getSource())) {
			int i = condButtons.indexOf(event.getSource());
			Integer id;

			if (conditions.get(i) != null) {
				id = DBKernel.openMiscDBWindow(conditions.get(i).getID());
			} else {
				id = DBKernel.openMiscDBWindow(null);
			}

			if (id != null) {
				String name = DBKernel.getValue("SonstigeParameter", "ID", id
						+ "", "Parameter")
						+ "";
				String description = DBKernel.getValue("SonstigeParameter",
						"ID", id + "", "Beschreibung") + "";
				List<String> categoryIDs = Arrays.asList(DBKernel
						.getValue("SonstigeParameter", "ID", id + "",
								"Kategorie").toString().split(","));

				condButtons.get(i).setText(name);
				conditions.set(i, new MiscXml(id, name, description, null,
						categoryIDs, null, DBKernel.getLocalDBUUID()));
				condUnitFields.get(i).removeAllItems();

				for (String categoryID : categoryIDs) {
					Category category = Categories.getCategory(categoryID);

					for (String u : category.getAllUnits()) {
						condUnitFields.get(i).addItem(u);
					}

					condUnitFields.get(i).setSelectedItem(
							category.getStandardUnit());
				}
			}
		}
	}

	private void addButtons(int i) {
		if (addButtons.isEmpty()) {
			JButton addButton = new JButton("+");

			addButton.addActionListener(this);

			addButtons.add(0, addButton);
			settingsPanel.add(addButton,
					createConstraints(3, settingsPanelRows));
		} else {
			JButton addButton = new JButton("+");
			JButton removeButton = new JButton("-");
			JButton button = new JButton(OTHER_PARAMETER);
			DoubleTextField valueField = new DoubleTextField(true);
			JComboBox<String> unitBox = new JComboBox<>();

			addButton.addActionListener(this);
			removeButton.addActionListener(this);
			button.addActionListener(this);
			valueField.setPreferredSize(new Dimension(100, valueField
					.getPreferredSize().height));

			for (JButton c : addButtons) {
				settingsPanel.remove(c);
			}

			for (JButton c : removeButtons) {
				settingsPanel.remove(c);
			}

			for (JButton c : condButtons) {
				settingsPanel.remove(c);
			}

			for (DoubleTextField c : condValueFields) {
				settingsPanel.remove(c);
			}

			for (JComboBox<String> c : condUnitFields) {
				settingsPanel.remove(c);
			}

			addButtons.add(i, addButton);
			removeButtons.add(i, removeButton);
			conditions.add(i, null);
			condButtons.add(i, button);
			condValueFields.add(i, valueField);
			condUnitFields.add(i, unitBox);

			for (int j = 0; j < addButtons.size(); j++) {
				settingsPanel.add(addButtons.get(j),
						createConstraints(3, settingsPanelRows + j));
			}

			for (int j = 0; j < removeButtons.size(); j++) {
				settingsPanel.add(removeButtons.get(j),
						createConstraints(4, settingsPanelRows + j));
			}

			for (int j = 0; j < condButtons.size(); j++) {
				settingsPanel.add(condButtons.get(j),
						createConstraints(0, settingsPanelRows + j));
			}

			for (int j = 0; j < condValueFields.size(); j++) {
				settingsPanel.add(condValueFields.get(j),
						createConstraints(1, settingsPanelRows + j));
			}

			for (int j = 0; j < condUnitFields.size(); j++) {
				settingsPanel.add(condUnitFields.get(j),
						createConstraints(2, settingsPanelRows + j));
			}

			settingsPanel.revalidate();
		}
	}

	private void removeButtons(int i) {
		settingsPanel.remove(addButtons.remove(i));
		settingsPanel.remove(removeButtons.remove(i));
		conditions.remove(i);
		settingsPanel.remove(condButtons.remove(i));
		settingsPanel.remove(condValueFields.remove(i));
		settingsPanel.remove(condUnitFields.remove(i));
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
				Object[] sheets = xlsReader.getSheets(
						fileChooser.getSelectedFile()).toArray();
				Object sheet = JOptionPane.showInputDialog(panel,
						"Select Sheet", "Input", JOptionPane.QUESTION_MESSAGE,
						null, sheets, sheets[0]);
				XLSDialog dialog = new XLSDialog(panel,
						fileChooser.getSelectedFile(), (String) sheet);

				dialog.setVisible(true);

				if (!dialog.isApproved()) {
					return;
				}

				Map<String, KnimeTuple> tuples = null;

				try {
					tuples = xlsReader.getTimeSeriesTuples(
							fileChooser.getSelectedFile(), (String) sheet,
							dialog.getMappings(), dialog.getTimeUnit(),
							dialog.getConcentrationUnit(), null, null, null,
							null);

					if (!xlsReader.getWarnings().isEmpty()) {
						JOptionPane.showMessageDialog(panel, xlsReader
								.getWarnings().get(0), "Warning",
								JOptionPane.WARNING_MESSAGE);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(panel, e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}

				Object[] values = tuples.keySet().toArray();
				Object selection = JOptionPane.showInputDialog(panel,
						"Select Time Series", "Input",
						JOptionPane.QUESTION_MESSAGE, null, values, values[0]);
				KnimeTuple tuple = tuples.get(selection);

				agentButton.setText(SELECT);
				agent = null;
				matrixButton.setText(SELECT);
				matrix = null;
				commentField.setValue(((MdInfoXml) tuple.getPmmXml(
						TimeSeriesSchema.ATT_MDINFO).get(0)).getComment());

				PmmXmlDoc miscXML = tuple.getPmmXml(TimeSeriesSchema.ATT_MISC);
				int n = removeButtons.size();

				for (int i = 0; i < n; i++) {
					removeButtons(0);
				}

				temperatureField.setValue(null);
				phField.setValue(null);
				waterActivityField.setValue(null);

				for (int i = 0; i < miscXML.getElementSet().size(); i++) {
					MiscXml misc = (MiscXml) miscXML.getElementSet().get(i);
					int id = misc.getID();
					String name = misc.getName();
					Double value = misc.getValue();
					String unit = misc.getUnit();

					if (value != null && value.isNaN()) {
						value = null;
					}

					if (id == AttributeUtilities.ATT_TEMPERATURE_ID) {
						temperatureField.setValue(value);
						tempBox.setSelectedItem(unit);
					} else if (id == AttributeUtilities.ATT_PH_ID) {
						phField.setValue(value);
						phBox.setSelectedItem(unit);
					} else if (id == AttributeUtilities.ATT_AW_ID) {
						waterActivityField.setValue(value);
						awBox.setSelectedItem(unit);
					} else {
						addButtons(0);
						condButtons.get(0).setText(name);
						conditions.set(0, misc);
						condValueFields.get(0).setValue(value);
						condUnitFields.get(0).setSelectedItem(unit);
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
								.getConcentration();
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

	private static GridBagConstraints createConstraints(int x, int y) {
		return new GridBagConstraints(x, y, 1, 1, 0, 0,
				GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
				new Insets(3, 3, 3, 3), 0, 0);
	}

	private class XLSDialog extends JDialog implements ActionListener,
			ItemListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;

		private Map<String, JComboBox<String>> mappingBoxes;
		private Map<String, JButton> mappingButtons;
		private Map<String, JComboBox<String>> unitBoxes;
		private Map<String, Object> mappings;
		private String timeUnit;
		private String concentrationUnit;

		private JButton okButton;
		private JButton cancelButton;

		public XLSDialog(Component owner, File file, String sheet)
				throws Exception {
			super(JOptionPane.getFrameForComponent(owner), "XLS File", true);

			approved = false;

			mappings = new LinkedHashMap<>();
			timeUnit = null;
			concentrationUnit = null;

			mappingBoxes = new LinkedHashMap<>();
			mappingButtons = new LinkedHashMap<>();
			unitBoxes = new LinkedHashMap<>();

			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);

			List<String> columnList = xlsReader.getColumns(file, sheet);
			JPanel northPanel = new JPanel();
			int row = 0;

			northPanel.setLayout(new GridBagLayout());

			for (String column : columnList) {
				JComboBox<String> box = new JComboBox<>(new String[] {
						XLSReader.ID_COLUMN, MdInfoXml.ATT_COMMENT,
						AttributeUtilities.TIME, AttributeUtilities.LOGC,
						AttributeUtilities.ATT_TEMPERATURE,
						AttributeUtilities.ATT_PH,
						AttributeUtilities.ATT_WATERACTIVITY, OTHER_PARAMETER,
						NO_PARAMETER });
				JButton button = new JButton();
				JComboBox<String> unitBox = new JComboBox<>();

				box.setSelectedItem(NO_PARAMETER);
				button.setEnabled(false);
				button.setText(OTHER_PARAMETER);

				box.addItemListener(this);
				button.addActionListener(this);
				unitBox.addItemListener(this);

				mappingBoxes.put(column, box);
				mappingButtons.put(column, button);
				unitBoxes.put(column, unitBox);

				northPanel.add(new JLabel(column + ":"),
						createConstraints(0, row));
				northPanel.add(box, createConstraints(1, row));
				northPanel.add(button, createConstraints(2, row));
				northPanel.add(unitBox, createConstraints(3, row));
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

		public Map<String, Object> getMappings() {
			return mappings;
		}

		public String getTimeUnit() {
			return timeUnit;
		}

		public String getConcentrationUnit() {
			return concentrationUnit;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				for (String column : mappingButtons.keySet()) {
					if (e.getSource() == mappingBoxes.get(column)) {
						JComboBox<String> box = mappingBoxes.get(column);
						JButton button = mappingButtons.get(column);
						String selected = (String) box.getSelectedItem();

						if (selected.equals(XLSReader.ID_COLUMN)
								|| selected.equals(MdInfoXml.ATT_COMMENT)
								|| selected.equals(AttributeUtilities.TIME)
								|| selected.equals(AttributeUtilities.LOGC)) {
							button.setEnabled(false);
							button.setText(OTHER_PARAMETER);
							mappings.put(column, selected);
						} else if (selected
								.equals(AttributeUtilities.ATT_TEMPERATURE)) {
							button.setEnabled(false);
							button.setText(OTHER_PARAMETER);
							mappings.put(column, new MiscXml(
									AttributeUtilities.ATT_TEMPERATURE_ID,
									AttributeUtilities.ATT_TEMPERATURE, null,
									null,
									Arrays.asList(Categories.TEMPERATURE),
									new Temperature().getStandardUnit()));
						} else if (selected.equals(AttributeUtilities.ATT_PH)) {
							button.setEnabled(false);
							button.setText(OTHER_PARAMETER);
							mappings.put(column,
									new MiscXml(AttributeUtilities.ATT_PH_ID,
											AttributeUtilities.ATT_PH, null,
											null, Arrays.asList(Categories.PH),
											new PH().getStandardUnit()));
						} else if (selected
								.equals(AttributeUtilities.ATT_WATERACTIVITY)) {
							button.setEnabled(false);
							button.setText(OTHER_PARAMETER);
							mappings.put(
									column,
									new MiscXml(
											AttributeUtilities.ATT_AW_ID,
											AttributeUtilities.ATT_WATERACTIVITY,
											null,
											null,
											Arrays.asList(Categories.WATER_ACTIVITY),
											new WaterActivity()
													.getStandardUnit()));
						} else if (selected.equals(OTHER_PARAMETER)) {
							button.setEnabled(true);
							button.setText(OTHER_PARAMETER);
							mappings.put(column, null);
						} else if (selected.equals(NO_PARAMETER)) {
							button.setEnabled(false);
							button.setText(OTHER_PARAMETER);
							mappings.remove(column);
						}

						break;
					}
				}

				for (String column : unitBoxes.keySet()) {
					if (e.getSource() == unitBoxes.get(column)) {
						String unit = (String) unitBoxes.get(column)
								.getSelectedItem();

						if (mappings.get(column) instanceof MiscXml) {
							MiscXml condition = (MiscXml) mappings.get(column);

							condition.setUnit(unit);
						} else if (mappings.get(column) instanceof String) {
							String mapping = (String) mappings.get(column);

							if (mapping.equals(AttributeUtilities.TIME)) {
								timeUnit = unit;
							} else if (mapping.equals(AttributeUtilities.LOGC)) {
								concentrationUnit = unit;
							}
						}

						break;
					}
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				for (Object value : mappings.values()) {
					if (value == null) {
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

						if (mappings.get(column) instanceof MiscXml) {
							oldID = ((MiscXml) mappings.get(column)).getID();
						}

						Integer id = DBKernel.openMiscDBWindow(oldID);

						if (id != null) {
							String name = DBKernel.getValue(
									"SonstigeParameter", "ID", id + "",
									"Parameter")
									+ "";
							String description = DBKernel.getValue(
									"SonstigeParameter", "ID", id + "",
									"Beschreibung")
									+ "";
							List<String> categoryIDs = Arrays.asList(DBKernel
									.getValue("SonstigeParameter", "ID",
											id + "", "Kategorie").toString()
									.split(","));

							unitBoxes.get(column).removeAllItems();

							String unit = null;

							for (String categoryID : categoryIDs) {
								Category category = Categories
										.getCategory(categoryID);

								for (String u : category.getAllUnits()) {
									unitBoxes.get(column).addItem(u);
								}

								unit = category.getStandardUnit();
								unitBoxes.get(column).setSelectedItem(unit);

								mappingButtons.get(column).setText(name);
							}

							mappings.put(column, new MiscXml(id, name,
									description, null, categoryIDs, unit));
							pack();
						}

						break;
					}
				}
			}
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
		public void textChanged(Object source) {
			if (numberField.isValueValid() && sizeField.isValueValid()) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}
	}

}
