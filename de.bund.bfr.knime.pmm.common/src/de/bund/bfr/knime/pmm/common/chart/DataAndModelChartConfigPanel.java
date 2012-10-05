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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.knime.pmm.common.ui.DoubleTextField;
import de.bund.bfr.knime.pmm.common.ui.SpacePanel;
import de.bund.bfr.knime.pmm.common.ui.TextListener;

public class DataAndModelChartConfigPanel extends JPanel implements
		ActionListener, TextListener {

	public static final int NO_PARAMETER_INPUT = 1;
	public static final int PARAMETER_FIELDS = 2;
	public static final int PARAMETER_BOXES = 3;

	private static final long serialVersionUID = 1L;

	private static final double DEFAULT_MINX = 0.0;
	private static final double DEFAULT_MAXX = 1.0;
	private static final double DEFAULT_MINY = 0.0;
	private static final double DEFAULT_MAXY = 1.0;

	private List<ConfigListener> listeners;

	private JCheckBox drawLinesBox;
	private JCheckBox showLegendBox;
	private JCheckBox addInfoInLegendBox;
	private JCheckBox displayFocusedRowBox;

	private JCheckBox manualRangeBox;
	private DoubleTextField minXField;
	private DoubleTextField minYField;
	private DoubleTextField maxXField;
	private DoubleTextField maxYField;

	private JComboBox xBox;
	private JComboBox yBox;
	private JComboBox yTransBox;
	private List<List<Double>> possibleValues;

	private JPanel parameterValuesPanel;
	private List<JLabel> parameterLabels;
	private List<JComponent> parameterInputs;

	private int type;

	private String lastParamX;

	public DataAndModelChartConfigPanel(int type) {
		this.type = type;
		listeners = new ArrayList<ConfigListener>();
		setLayout(new GridLayout(3, 1));

		JPanel displayOptionsPanel = new JPanel();

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

		displayOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		displayOptionsPanel.setBorder(BorderFactory
				.createTitledBorder("Display Options"));
		displayOptionsPanel.add(drawLinesBox);
		displayOptionsPanel.add(showLegendBox);
		displayOptionsPanel.add(addInfoInLegendBox);
		displayOptionsPanel.add(displayFocusedRowBox);
		add(new SpacePanel(displayOptionsPanel));

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

		rangePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rangePanel.setBorder(BorderFactory.createTitledBorder("Range"));
		rangePanel.add(manualRangeBox);
		rangePanel.add(new JLabel("Min X:"));
		rangePanel.add(minXField);
		rangePanel.add(new JLabel("Max X:"));
		rangePanel.add(maxXField);
		rangePanel.add(new JLabel("Min Y:"));
		rangePanel.add(minYField);
		rangePanel.add(new JLabel("Max Y:"));
		rangePanel.add(maxYField);
		add(new SpacePanel(rangePanel));

		JPanel parametersPanel = new JPanel();

		xBox = new JComboBox();
		xBox.addActionListener(this);
		yBox = new JComboBox();
		yBox.addActionListener(this);
		yTransBox = new JComboBox(ChartConstants.TRANSFORMS);
		yTransBox.addActionListener(this);

		parameterValuesPanel = new JPanel();
		parameterInputs = new ArrayList<JComponent>();
		parameterLabels = new ArrayList<JLabel>();

		parametersPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		parametersPanel.setBorder(BorderFactory
				.createTitledBorder("Parameters"));
		parametersPanel.add(new JLabel("X:"));
		parametersPanel.add(xBox);
		parametersPanel.add(new JLabel("Y:"));
		parametersPanel.add(yBox);
		parametersPanel.add(new JLabel("Y Transform:"));
		parametersPanel.add(yTransBox);
		parametersPanel.add(parameterValuesPanel);
		add(new SpacePanel(parametersPanel));

		lastParamX = null;
	}

	public void addConfigListener(ConfigListener listener) {
		listeners.add(listener);
	}

	public void removeConfigListener(ConfigListener listener) {
		listeners.remove(listener);
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

	public String getParamX() {
		return (String) xBox.getSelectedItem();
	}

	public String getParamY() {
		return (String) yBox.getSelectedItem();
	}

	public String getTransformY() {
		return (String) yTransBox.getSelectedItem();
	}
	
	public void setTransformY(String transformY) {
		yTransBox.setSelectedItem(transformY);
	}

	public Map<String, Double> getParamsXValues() {
		Map<String, Double> values = new HashMap<String, Double>();

		values.put((String) xBox.getSelectedItem(), 0.0);

		for (int i = 0; i < parameterInputs.size(); i++) {
			if (parameterInputs.get(i) instanceof DoubleTextField) {
				DoubleTextField field = (DoubleTextField) parameterInputs
						.get(i);

				if (field.getValue() != null) {
					values.put(parameterLabels.get(i).getText(),
							field.getValue());
				} else {
					values.put(parameterLabels.get(i).getText(), 0.0);
				}
			} else if (parameterInputs.get(i) instanceof JComboBox) {
				JComboBox box = (JComboBox) parameterInputs.get(i);

				values.put(parameterLabels.get(i).getText(),
						(Double) box.getSelectedItem());
			}
		}

		return values;
	}

	public void setParamsX(List<String> parameters) {
		setParamsX(parameters, null);
	}

	public void setParamsX(List<String> parameters,
			List<List<Double>> possibleValues) {
		boolean parametersChanged = false;

		if (parameters == null) {
			parameters = new ArrayList<String>();
		}

		if (parameters.size() != xBox.getItemCount()) {
			parametersChanged = true;
		}

		for (int i = 0; i < xBox.getItemCount(); i++) {
			if (!parameters.contains(xBox.getItemAt(i))) {
				parametersChanged = true;
				break;
			}
		}

		if (possibleValues != null
				&& !possibleValues.equals(this.possibleValues)) {
			parametersChanged = true;
		}

		this.possibleValues = possibleValues;

		if (parametersChanged) {
			xBox.removeActionListener(this);
			xBox.removeAllItems();

			for (String param : parameters) {
				xBox.addItem(param);
			}

			if (!parameters.isEmpty()) {
				if (parameters.contains(lastParamX)) {
					xBox.setSelectedItem(lastParamX);
				} else if (parameters.contains(TimeSeriesSchema.ATT_TIME)) {
					xBox.setSelectedItem(TimeSeriesSchema.ATT_TIME);
				} else {
					xBox.setSelectedIndex(0);
				}

				lastParamX = (String) xBox.getSelectedItem();
			}

			xBox.addActionListener(this);
			updateParametersPanel();
		}
	}

	public void setParamsY(List<String> paramsY) {
		yBox.removeActionListener(this);
		yBox.removeAllItems();

		if (paramsY == null) {
			paramsY = new ArrayList<String>();
		}

		for (String param : paramsY) {
			yBox.addItem(param);
		}

		if (!paramsY.isEmpty()) {
			if (yBox.getItemAt(0).equals(xBox.getSelectedItem())
					&& yBox.getItemCount() >= 2) {
				yBox.setSelectedIndex(1);
			} else {
				yBox.setSelectedIndex(0);
			}
		}

		yBox.addActionListener(this);
	}

	private void updateParametersPanel() {
		if (type == NO_PARAMETER_INPUT) {
			return;
		}

		for (JLabel label : parameterLabels) {
			parameterValuesPanel.remove(label);
		}

		for (JComponent input : parameterInputs) {
			parameterValuesPanel.remove(input);
		}

		parameterLabels.clear();
		parameterInputs.clear();

		for (int i = 0; i < xBox.getItemCount(); i++) {
			if (i == xBox.getSelectedIndex()) {
				continue;
			}

			JLabel label = new JLabel((String) xBox.getItemAt(i));
			JComponent input = null;

			if (type == PARAMETER_FIELDS) {
				double value = 0.0;

				if (possibleValues != null && possibleValues.get(i) != null) {
					value = possibleValues.get(i).get(0);
				}

				input = new DoubleTextField();
				input.setPreferredSize(new Dimension(50, input
						.getPreferredSize().height));
				((DoubleTextField) input).setValue(value);
				((DoubleTextField) input).addTextListener(this);
			} else if (type == PARAMETER_BOXES) {
				input = new JComboBox(possibleValues.get(i).toArray(
						new Double[0]));
				((JComboBox) input).setSelectedIndex(0);
				((JComboBox) input).addActionListener(this);
			}

			parameterLabels.add(label);
			parameterInputs.add(input);
			parameterValuesPanel.add(label);
			parameterValuesPanel.add(input);
			parameterValuesPanel.updateUI();
		}
	}

	private void fireConfigChanged() {
		for (ConfigListener listener : listeners) {
			listener.configChanged();
		}
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
		} else if (e.getSource() == showLegendBox) {
			if (showLegendBox.isSelected()) {
				addInfoInLegendBox.setEnabled(true);
			} else {
				addInfoInLegendBox.setEnabled(false);
			}
		} else if (e.getSource() == xBox) {
			lastParamX = (String) xBox.getSelectedItem();
			updateParametersPanel();
		}

		fireConfigChanged();
	}

	@Override
	public void textChanged() {
		fireConfigChanged();
	}

	public static interface ConfigListener {

		public void configChanged();
	}

}
