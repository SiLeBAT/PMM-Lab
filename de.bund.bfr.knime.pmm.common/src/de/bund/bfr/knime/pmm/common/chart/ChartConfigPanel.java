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
package de.bund.bfr.knime.pmm.common.chart;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.ui.DoubleTextField;
import de.bund.bfr.knime.pmm.common.ui.TextListener;
import de.bund.bfr.knime.pmm.common.units.Categories;
import de.bund.bfr.knime.pmm.common.units.Category;

public class ChartConfigPanel extends JPanel implements ActionListener,
		TextListener, ChangeListener, MouseListener {

	public static final int NO_PARAMETER_INPUT = 1;
	public static final int PARAMETER_FIELDS = 2;
	public static final int PARAMETER_BOXES = 3;

	private static final long serialVersionUID = 1L;

	private static final double DEFAULT_MINX = 0.0;
	private static final double DEFAULT_MAXX = 1.0;
	private static final double DEFAULT_MINY = 0.0;
	private static final double DEFAULT_MAXY = 1.0;

	private static final int SLIDER_MAX = 100;

	private List<ConfigListener> configListeners;
	private List<ExtraButtonListener> buttonListeners;

	private JCheckBox drawLinesBox;
	private JCheckBox showLegendBox;
	private JCheckBox addInfoInLegendBox;
	private JCheckBox displayFocusedRowBox;
	private JCheckBox showConfidenceBox;

	private JCheckBox manualRangeBox;
	private DoubleTextField minXField;
	private DoubleTextField minYField;
	private DoubleTextField maxXField;
	private DoubleTextField maxYField;

	private JComboBox<String> xBox;
	private JComboBox<String> yBox;
	private JComboBox<String> xUnitBox;
	private JComboBox<String> yUnitBox;
	private JComboBox<String> yTransBox;
	private JButton extraButton;
	private String lastParamX;
	private Map<String, List<Double>> parametersX;
	private Map<String, List<Boolean>> selectedValuesX;
	private Map<String, Double> minParamValuesX;
	private Map<String, Double> maxParamValuesX;
	private Map<String, String> categories;
	private Map<String, String> units;

	private JPanel parameterValuesPanel;
	private List<JPanel> parameterSubPanels;
	private List<JButton> parameterButtons;
	private List<JLabel> parameterLabels;
	private List<DoubleTextField> parameterFields;
	private List<JSlider> parameterSliders;

	private int type;

	public ChartConfigPanel(int type, boolean allowConfidenceInterval,
			String extraButtonLabel) {
		this.type = type;
		configListeners = new ArrayList<>();
		buttonListeners = new ArrayList<>();
		lastParamX = null;

		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new GridBagLayout());

		drawLinesBox = new JCheckBox("Draw Lines");
		drawLinesBox.setSelected(false);
		drawLinesBox.addActionListener(this);
		showLegendBox = new JCheckBox("Show Legend");
		showLegendBox.setSelected(true);
		showLegendBox.addActionListener(this);
		addInfoInLegendBox = new JCheckBox("Add Info in Lengend");
		addInfoInLegendBox.setSelected(false);
		addInfoInLegendBox.addActionListener(this);
		displayFocusedRowBox = new JCheckBox("Display Highlighted Row");
		displayFocusedRowBox.setSelected(false);
		displayFocusedRowBox.addActionListener(this);

		JPanel displayOptionsPanel = new JPanel();

		displayOptionsPanel.setLayout(new BoxLayout(displayOptionsPanel,
				BoxLayout.Y_AXIS));
		displayOptionsPanel.setLayout(new GridBagLayout());
		displayOptionsPanel.add(drawLinesBox, createConstraints(0, 0, 1, 1));
		displayOptionsPanel.add(displayFocusedRowBox,
				createConstraints(1, 0, 1, 1));
		displayOptionsPanel.add(showLegendBox, createConstraints(0, 1, 1, 1));
		displayOptionsPanel.add(addInfoInLegendBox,
				createConstraints(1, 1, 1, 1));

		if (allowConfidenceInterval) {
			showConfidenceBox = new JCheckBox("Show Confidence Interval");
			showConfidenceBox.setSelected(false);
			showConfidenceBox.addActionListener(this);
			displayOptionsPanel.add(showConfidenceBox,
					createConstraints(0, 2, 2, 1));
		}

		JPanel outerDisplayOptionsPanel = new JPanel();

		outerDisplayOptionsPanel.setBorder(BorderFactory
				.createTitledBorder("Display Options"));
		outerDisplayOptionsPanel.setLayout(new BorderLayout());
		outerDisplayOptionsPanel.add(displayOptionsPanel, BorderLayout.WEST);
		mainPanel.add(outerDisplayOptionsPanel, createConstraints(0));

		JPanel rangePanel = new JPanel();

		manualRangeBox = new JCheckBox("Set Manual Range");
		manualRangeBox.setSelected(false);
		manualRangeBox.addActionListener(this);
		minXField = new DoubleTextField();
		minXField.setValue(DEFAULT_MINX);
		minXField.setPreferredSize(new Dimension(50, minXField
				.getPreferredSize().height));
		minXField.setEnabled(false);
		minXField.addTextListener(this);
		minYField = new DoubleTextField();
		minYField.setValue(DEFAULT_MINY);
		minYField.setPreferredSize(new Dimension(50, minYField
				.getPreferredSize().height));
		minYField.setEnabled(false);
		minYField.addTextListener(this);
		maxXField = new DoubleTextField();
		maxXField.setValue(DEFAULT_MAXX);
		maxXField.setPreferredSize(new Dimension(50, maxXField
				.getPreferredSize().height));
		maxXField.setEnabled(false);
		maxXField.addTextListener(this);
		maxYField = new DoubleTextField();
		maxYField.setValue(DEFAULT_MAXY);
		maxYField.setPreferredSize(new Dimension(50, maxYField
				.getPreferredSize().height));
		maxYField.setEnabled(false);
		maxYField.addTextListener(this);

		rangePanel.setLayout(new GridBagLayout());
		rangePanel.add(manualRangeBox, createConstraints(0, 0, 4, 1));
		rangePanel.add(new JLabel("Min X:"), createConstraints(0, 1, 1, 1));
		rangePanel.add(minXField, createConstraints(1, 1, 1, 1));
		rangePanel.add(new JLabel("Max X:"), createConstraints(2, 1, 1, 1));
		rangePanel.add(maxXField, createConstraints(3, 1, 1, 1));
		rangePanel.add(new JLabel("Min Y:"), createConstraints(0, 2, 1, 1));
		rangePanel.add(minYField, createConstraints(1, 2, 1, 1));
		rangePanel.add(new JLabel("Max Y:"), createConstraints(2, 2, 1, 1));
		rangePanel.add(maxYField, createConstraints(3, 2, 1, 1));

		JPanel outerRangePanel = new JPanel();

		outerRangePanel.setBorder(BorderFactory.createTitledBorder("Range"));
		outerRangePanel.setLayout(new BorderLayout());
		outerRangePanel.add(rangePanel, BorderLayout.WEST);
		mainPanel.add(outerRangePanel, createConstraints(1));

		xBox = new JComboBox<String>();
		xBox.addActionListener(this);
		yBox = new JComboBox<String>();
		xUnitBox = new JComboBox<String>();
		xUnitBox.addActionListener(this);
		yUnitBox = new JComboBox<String>();
		yUnitBox.addActionListener(this);
		yTransBox = new JComboBox<String>(ChartConstants.TRANSFORMS);
		yTransBox.addActionListener(this);

		JPanel parametersPanel = new JPanel();

		parametersPanel.setLayout(new GridBagLayout());
		parametersPanel.add(new JLabel("X:"), createConstraints(0, 0, 1, 1));
		parametersPanel.add(xBox, createConstraints(1, 0, 1, 1));
		parametersPanel.add(new JLabel("X Unit:"),
				createConstraints(2, 0, 1, 1));
		parametersPanel.add(xUnitBox, createConstraints(3, 0, 1, 1));
		parametersPanel.add(new JLabel("Y:"), createConstraints(0, 1, 1, 1));
		parametersPanel.add(yBox, createConstraints(1, 1, 1, 1));
		parametersPanel.add(new JLabel("Y Unit:"),
				createConstraints(2, 1, 1, 1));
		parametersPanel.add(yUnitBox, createConstraints(3, 1, 1, 1));
		parametersPanel.add(new JLabel("Y Transform:"),
				createConstraints(2, 2, 1, 1));
		parametersPanel.add(yTransBox, createConstraints(3, 2, 1, 1));

		if (extraButtonLabel != null) {
			extraButton = new JButton(extraButtonLabel);
			extraButton.addActionListener(this);
			parametersPanel.add(extraButton, createConstraints(0, 3, 4, 1));
		}

		JPanel outerParametersPanel = new JPanel();

		outerParametersPanel.setBorder(BorderFactory
				.createTitledBorder("Parameters"));
		outerParametersPanel.setLayout(new BorderLayout());
		outerParametersPanel.add(parametersPanel, BorderLayout.WEST);
		mainPanel.add(outerParametersPanel, createConstraints(2));

		parameterValuesPanel = new JPanel();
		parameterValuesPanel.setBorder(BorderFactory
				.createTitledBorder("Parameter Values"));
		parameterValuesPanel.setLayout(new BoxLayout(parameterValuesPanel,
				BoxLayout.Y_AXIS));
		parameterSubPanels = new ArrayList<>();
		parameterFields = new ArrayList<>();
		parameterButtons = new ArrayList<>();
		parameterLabels = new ArrayList<>();
		parameterSliders = new ArrayList<>();
		mainPanel.add(parameterValuesPanel, createConstraints(3));

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.NORTH);
	}

	public void addConfigListener(ConfigListener listener) {
		configListeners.add(listener);
	}

	public void removeConfigListener(ConfigListener listener) {
		configListeners.remove(listener);
	}

	public void addExtraButtonListener(ExtraButtonListener listener) {
		buttonListeners.add(listener);
	}

	public void removeExtraButtonListener(ExtraButtonListener listener) {
		buttonListeners.remove(listener);
	}

	public boolean isUseManualRange() {
		return manualRangeBox.isSelected();
	}

	public void setUseManualRange(boolean manualRange) {
		manualRangeBox.setSelected(manualRange);

		if (manualRangeBox.isSelected()) {
			minXField.setEnabled(true);
			minYField.setEnabled(true);
			maxXField.setEnabled(true);
			maxYField.setEnabled(true);
		} else {
			minXField.setEnabled(false);
			minYField.setEnabled(false);
			maxXField.setEnabled(false);
			maxYField.setEnabled(false);
		}
	}

	public double getMinX() {
		if (minXField.isValueValid()) {
			return minXField.getValue();
		} else {
			return DEFAULT_MINX;
		}
	}

	public void setMinX(double minX) {
		minXField.setValue(minX);
	}

	public double getMinY() {
		if (minYField.isValueValid()) {
			return minYField.getValue();
		} else {
			return DEFAULT_MINY;
		}
	}

	public void setMinY(double minY) {
		minYField.setValue(minY);
	}

	public double getMaxX() {
		if (maxXField.isValueValid()) {
			return maxXField.getValue();
		} else {
			return DEFAULT_MAXX;
		}
	}

	public void setMaxX(double maxX) {
		maxXField.setValue(maxX);
	}

	public double getMaxY() {
		if (maxYField.isValueValid()) {
			return maxYField.getValue();
		} else {
			return DEFAULT_MAXY;
		}
	}

	public void setMaxY(double maxY) {
		maxYField.setValue(maxY);
	}

	public boolean isDrawLines() {
		return drawLinesBox.isSelected();
	}

	public void setDrawLines(boolean drawLines) {
		drawLinesBox.setSelected(drawLines);
	}

	public boolean isShowLegend() {
		return showLegendBox.isSelected();
	}

	public void setShowLegend(boolean showLegend) {
		showLegendBox.setSelected(showLegend);

		if (showLegendBox.isSelected()) {
			addInfoInLegendBox.setEnabled(true);
		} else {
			addInfoInLegendBox.setEnabled(false);
		}
	}

	public boolean isAddInfoInLegend() {
		return addInfoInLegendBox.isSelected();
	}

	public void setAddInfoInLegend(boolean addInfoInLegend) {
		addInfoInLegendBox.setSelected(addInfoInLegend);
	}

	public boolean isDisplayFocusedRow() {
		return displayFocusedRowBox.isSelected();
	}

	public void setDisplayFocusedRow(boolean displayFocusedRow) {
		displayFocusedRowBox.setSelected(displayFocusedRow);
	}

	public boolean isShowConfidenceInterval() {
		return showConfidenceBox.isSelected();
	}

	public void setShowConfidenceInterval(boolean showConfidenceInterval) {
		showConfidenceBox.setSelected(showConfidenceInterval);
	}

	public String getParamX() {
		return (String) xBox.getSelectedItem();
	}

	public void setParamX(String paramX) {
		xBox.setSelectedItem(paramX);
	}

	public String getParamY() {
		return (String) yBox.getSelectedItem();
	}

	public String getUnitX() {
		return (String) xUnitBox.getSelectedItem();
	}

	public void setUnitX(String unitX) {
		xUnitBox.setSelectedItem(unitX);
	}

	public String getUnitY() {
		return (String) yUnitBox.getSelectedItem();
	}

	public void setUnitY(String unitY) {
		yUnitBox.setSelectedItem(unitY);
	}

	public String getTransformY() {
		return (String) yTransBox.getSelectedItem();
	}

	public void setTransformY(String transformY) {
		yTransBox.setSelectedItem(transformY);
	}

	public void setParameters(String paramY,
			Map<String, List<Double>> parametersX,
			Map<String, Double> minParamXValues,
			Map<String, Double> maxParamXValues,
			Map<String, String> categories, Map<String, String> units,
			String lockedParamX) {
		boolean parametersChanged = false;

		if (parametersX == null) {
			parametersX = new LinkedHashMap<>();
		}

		if (minParamXValues == null) {
			minParamXValues = new LinkedHashMap<>();
		}

		if (maxParamXValues == null) {
			maxParamXValues = new LinkedHashMap<>();
		}

		if (categories == null) {
			categories = new LinkedHashMap<>();
		}

		if (units == null) {
			units = new LinkedHashMap<>();
		}

		if (this.parametersX == null
				|| parametersX.size() != this.parametersX.size()) {
			parametersChanged = true;
		} else {
			for (String param : parametersX.keySet()) {
				if (!this.parametersX.containsKey(param)) {
					parametersChanged = true;
					break;
				} else if (!parametersX.get(param).equals(
						this.parametersX.get(param))) {
					parametersChanged = true;
					break;
				}
			}
		}

		this.parametersX = parametersX;
		this.minParamValuesX = minParamXValues;
		this.maxParamValuesX = maxParamXValues;
		this.categories = categories;
		this.units = units;

		if (parametersChanged) {
			xBox.removeActionListener(this);
			xBox.removeAllItems();

			if (lockedParamX != null) {
				xBox.addItem(lockedParamX);
			} else {
				for (String param : parametersX.keySet()) {
					xBox.addItem(param);
				}
			}

			if (!parametersX.isEmpty()) {
				if (parametersX.containsKey(lastParamX)) {
					xBox.setSelectedItem(lastParamX);
				} else if (parametersX.containsKey(AttributeUtilities.TIME)) {
					xBox.setSelectedItem(AttributeUtilities.TIME);
				} else {
					xBox.setSelectedIndex(0);
				}

				lastParamX = (String) xBox.getSelectedItem();
			} else {
				lastParamX = null;
			}

			xBox.addActionListener(this);
			selectedValuesX = new LinkedHashMap<String, List<Boolean>>();

			for (String param : parametersX.keySet()) {
				List<Double> values = parametersX.get(param);

				if (!values.isEmpty()) {
					List<Boolean> selected = new ArrayList<Boolean>(
							Collections.nCopies(values.size(), true));

					selectedValuesX.put(param, selected);
				} else {
					selectedValuesX.put(param, new ArrayList<Boolean>());
				}
			}

			updateParametersPanel();
			updateXUnitBox();
		}

		if (paramY == null) {
			yBox.removeAllItems();
			updateYUnitBox();
		} else if (!paramY.equals(yBox.getSelectedItem())) {
			yBox.removeAllItems();
			yBox.addItem(paramY);
			yBox.setSelectedIndex(0);
			updateYUnitBox();
		}
	}

	public Map<String, List<Boolean>> getSelectedValuesX() {
		return selectedValuesX;
	}

	public void setSelectedValuesX(Map<String, List<Boolean>> selectedValuesX) {
		this.selectedValuesX = selectedValuesX;
	}

	public Map<String, List<Double>> getParamsX() {
		Map<String, List<Double>> valueLists = new LinkedHashMap<String, List<Double>>();

		if (type == PARAMETER_FIELDS) {
			for (int i = 0; i < parameterFields.size(); i++) {
				DoubleTextField field = (DoubleTextField) parameterFields
						.get(i);
				String paramName = parameterLabels.get(i).getText()
						.replace(":", "");

				if (field.getValue() != null) {
					valueLists.put(
							paramName,
							new ArrayList<Double>(Arrays.asList(field
									.getValue())));
				} else {
					valueLists.put(paramName,
							new ArrayList<Double>(Arrays.asList(0.0)));
				}
			}
		} else if (type == PARAMETER_BOXES) {
			for (String param : parametersX.keySet()) {
				List<Double> values = parametersX.get(param);
				List<Boolean> selected = selectedValuesX.get(param);
				List<Double> newValues = new ArrayList<Double>();

				for (int j = 0; j < values.size(); j++) {
					if (selected.get(j)) {
						newValues.add(values.get(j));
					}
				}

				if (newValues.isEmpty()) {
					newValues.add(0.0);
				}

				valueLists.put(param, newValues);
			}
		}

		valueLists.put((String) xBox.getSelectedItem(), new ArrayList<Double>(
				Arrays.asList(0.0)));

		return valueLists;
	}

	public Map<String, Double> getParamXValues() {
		Map<String, List<Double>> paramsX = getParamsX();
		Map<String, Double> paramXValues = new LinkedHashMap<>();

		for (Map.Entry<String, List<Double>> entry : paramsX.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				paramXValues.put(entry.getKey(), entry.getValue().get(0));
			}
		}

		return paramXValues;
	}

	public void setParamXValues(Map<String, Double> paramXValues) {
		for (int i = 0; i < parameterFields.size(); i++) {
			DoubleTextField field = (DoubleTextField) parameterFields.get(i);
			String paramName = parameterLabels.get(i).getText()
					.replace(":", "");

			field.setValue(paramXValues.get(paramName));
		}
	}

	private void updateXUnitBox() {
		String var = (String) xBox.getSelectedItem();

		xUnitBox.removeActionListener(this);
		xUnitBox.removeAllItems();

		Category category = Categories.getCategory(categories.get(var));

		for (String unit : category.getAllUnits()) {
			xUnitBox.addItem(unit);
		}

		xUnitBox.setSelectedItem(units.get(var));
		xUnitBox.addActionListener(this);
	}

	private void updateYUnitBox() {
		String var = (String) yBox.getSelectedItem();

		yUnitBox.removeActionListener(this);
		yUnitBox.removeAllItems();

		Category category = Categories.getCategory(categories.get(var));

		for (String unit : category.getAllUnits()) {
			yUnitBox.addItem(unit);
		}

		yUnitBox.setSelectedItem(units.get(var));
		yUnitBox.addActionListener(this);
	}

	private void updateParametersPanel() {
		if (type == NO_PARAMETER_INPUT) {
			return;
		}

		for (JPanel panel : parameterSubPanels) {
			parameterValuesPanel.remove(panel);
		}

		parameterSubPanels.clear();
		parameterFields.clear();
		parameterButtons.clear();
		parameterLabels.clear();
		parameterSliders.clear();

		for (String param : parametersX.keySet()) {
			if (param.equals(xBox.getSelectedItem())) {
				continue;
			}

			if (type == PARAMETER_FIELDS) {
				JLabel label = new JLabel(param + ":");
				DoubleTextField input = new DoubleTextField();
				JSlider slider = null;
				Double value = null;
				Double min = minParamValuesX.get(param);
				Double max = maxParamValuesX.get(param);

				if (!parametersX.get(param).isEmpty()) {
					value = parametersX.get(param).get(0);
				}

				if (min != null && max != null) {
					if (value == null) {
						value = min;
					}

					if (value < min) {
						slider = new JSlider(0, SLIDER_MAX, doubleToInt(min,
								min, max));
					} else if (value > max) {
						slider = new JSlider(0, SLIDER_MAX, doubleToInt(max,
								min, max));
					} else {
						slider = new JSlider(0, SLIDER_MAX, doubleToInt(value,
								min, max));
					}

					slider.setPreferredSize(new Dimension(50, slider
							.getPreferredSize().height));
					slider.addChangeListener(this);
					slider.addMouseListener(this);
				}

				if (value == null) {
					value = 0.0;
				}

				input.setPreferredSize(new Dimension(50, input
						.getPreferredSize().height));
				input.setValue(value);
				input.addTextListener(this);

				parameterFields.add(input);
				parameterLabels.add(label);
				parameterSliders.add(slider);

				JPanel subPanel = new JPanel();

				subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				subPanel.add(label);

				if (slider != null) {
					subPanel.add(slider);
				}

				subPanel.add(input);
				parameterSubPanels.add(subPanel);
				parameterValuesPanel.add(subPanel);
			} else if (type == PARAMETER_BOXES) {
				JButton selectButton = new JButton(param + " Values");

				selectButton.addActionListener(this);
				parameterButtons.add(selectButton);

				JPanel subPanel = new JPanel();

				subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				subPanel.add(selectButton);
				parameterSubPanels.add(subPanel);
				parameterValuesPanel.add(subPanel);
			}
		}

		Container container = getParent();

		while (container != null) {
			if (container instanceof JPanel) {
				container.revalidate();
				break;
			}

			container = container.getParent();
		}
	}

	private void fireConfigChanged() {
		for (ConfigListener listener : configListeners) {
			listener.configChanged();
		}
	}

	private void fireButtonPressed() {
		for (ExtraButtonListener listener : buttonListeners) {
			listener.buttonPressed();
		}
	}

	private int doubleToInt(double d, double min, double max) {
		return (int) ((d - min) / (max - min) * (double) SLIDER_MAX);
	}

	private double intToDouble(int i, double min, double max) {
		return (double) i / (double) SLIDER_MAX * (max - min) + min;
	}

	private GridBagConstraints createConstraints(int x, int y, int w, int h) {
		return new GridBagConstraints(x, y, w, h, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2,
						2, 2, 2), 0, 0);
	}

	private GridBagConstraints createConstraints(int y) {
		return new GridBagConstraints(0, y, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == manualRangeBox) {
			if (manualRangeBox.isSelected()) {
				minXField.setEnabled(true);
				minYField.setEnabled(true);
				maxXField.setEnabled(true);
				maxYField.setEnabled(true);
			} else {
				minXField.setEnabled(false);
				minYField.setEnabled(false);
				maxXField.setEnabled(false);
				maxYField.setEnabled(false);
			}

			fireConfigChanged();
		} else if (e.getSource() == showLegendBox) {
			if (showLegendBox.isSelected()) {
				addInfoInLegendBox.setEnabled(true);
			} else {
				addInfoInLegendBox.setEnabled(false);
			}

			fireConfigChanged();
		} else if (e.getSource() == xBox) {
			lastParamX = (String) xBox.getSelectedItem();
			updateXUnitBox();
			updateParametersPanel();
			fireConfigChanged();
		} else if (e.getSource() == extraButton) {
			fireButtonPressed();
		} else if (parameterButtons.contains(e.getSource())) {
			JButton button = (JButton) e.getSource();
			String param = button.getText().replace(" Values", "");
			SelectDialog dialog = new SelectDialog(param,
					parametersX.get(param), selectedValuesX.get(param));

			dialog.setVisible(true);

			if (dialog.isApproved()) {
				selectedValuesX.put(param, dialog.getSelected());
				fireConfigChanged();
			}
		} else {
			fireConfigChanged();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		int i = parameterSliders.indexOf(e.getSource());
		String paramName = parameterLabels.get(i).getText().replace(":", "");
		JSlider slider = parameterSliders.get(i);
		DoubleTextField field = parameterFields.get(i);

		field.removeTextListener(this);
		field.setValue(intToDouble(slider.getValue(),
				minParamValuesX.get(paramName), maxParamValuesX.get(paramName)));
		field.addTextListener(this);
	}

	@Override
	public void textChanged(Object source) {
		if (parameterFields.contains(source)) {
			int i = parameterFields.indexOf(source);
			String paramName = parameterLabels.get(i).getText()
					.replace(":", "");
			DoubleTextField field = parameterFields.get(i);
			JSlider slider = parameterSliders.get(i);

			if (field.getValue() != null && slider != null) {
				int value = doubleToInt(field.getValue(),
						minParamValuesX.get(paramName),
						maxParamValuesX.get(paramName));

				slider.removeChangeListener(this);

				if (value < 0) {
					slider.setValue(0);
				} else if (value > SLIDER_MAX) {
					slider.setValue(SLIDER_MAX);
				} else {
					slider.setValue(value);
				}

				slider.addChangeListener(this);
			}
		}

		fireConfigChanged();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int i = parameterSliders.indexOf(e.getSource());
		String paramName = parameterLabels.get(i).getText().replace(":", "");
		JSlider slider = parameterSliders.get(i);
		DoubleTextField field = parameterFields.get(i);

		field.setValue(intToDouble(slider.getValue(),
				minParamValuesX.get(paramName), maxParamValuesX.get(paramName)));
	}

	public static interface ConfigListener {

		public void configChanged();
	}

	public static interface ExtraButtonListener {

		public void buttonPressed();
	}

	private class SelectDialog extends JDialog implements ActionListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private List<Boolean> selected;

		private List<JCheckBox> selectBoxes;

		private JButton okButton;
		private JButton cancelButton;

		public SelectDialog(String title, List<Double> values,
				List<Boolean> initialSelected) {
			super(JOptionPane.getFrameForComponent(ChartConfigPanel.this),
					title, true);

			approved = false;
			selected = null;

			selectBoxes = new ArrayList<JCheckBox>();
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);

			JPanel centerPanel = new JPanel();
			JPanel bottomPanel = new JPanel();

			centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			centerPanel.setLayout(new GridLayout(values.size(), 1, 5, 5));

			for (int i = 0; i < values.size(); i++) {
				JCheckBox box = new JCheckBox(values.get(i) + "");

				box.setSelected(initialSelected.get(i));
				box.addActionListener(this);
				selectBoxes.add(box);
				centerPanel.add(box);
			}

			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			bottomPanel.add(okButton);
			bottomPanel.add(cancelButton);

			setLayout(new BorderLayout());
			add(centerPanel, BorderLayout.CENTER);
			add(bottomPanel, BorderLayout.SOUTH);
			pack();

			setResizable(false);
			setLocationRelativeTo(ChartConfigPanel.this);
		}

		public boolean isApproved() {
			return approved;
		}

		public List<Boolean> getSelected() {
			return selected;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				approved = true;
				selected = new ArrayList<Boolean>();

				for (JCheckBox box : selectBoxes) {
					selected.add(box.isSelected());
				}

				dispose();
			} else if (e.getSource() == cancelButton) {
				dispose();
			} else {
				boolean noSelection = true;

				for (JCheckBox box : selectBoxes) {
					if (box.isSelected()) {
						noSelection = false;
						break;
					}
				}

				if (noSelection) {
					okButton.setEnabled(false);
				} else {
					okButton.setEnabled(true);
				}
			}
		}

	}

}
