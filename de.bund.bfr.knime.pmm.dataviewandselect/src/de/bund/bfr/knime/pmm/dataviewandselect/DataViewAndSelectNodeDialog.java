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
package de.bund.bfr.knime.pmm.dataviewandselect;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.chart.DataAndModelChartConfigPanel;
import de.bund.bfr.knime.pmm.common.chart.DataAndModelChartCreator;
import de.bund.bfr.knime.pmm.common.chart.DataAndModelChartInfoPanel;
import de.bund.bfr.knime.pmm.common.chart.DataAndModelSelectionPanel;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * <code>NodeDialog</code> for the "DataViewAndSelect" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class DataViewAndSelectNodeDialog extends DataAwareNodeDialogPane
		implements DataAndModelSelectionPanel.SelectionListener,
		DataAndModelChartConfigPanel.ConfigListener {

	private TableReader reader;

	private DataAndModelChartCreator chartCreator;
	private DataAndModelSelectionPanel selectionPanel;
	private DataAndModelChartConfigPanel configPanel;
	private DataAndModelChartInfoPanel infoPanel;

	/**
	 * New pane for configuring the DataViewAndSelect node.
	 */
	protected DataViewAndSelectNodeDialog() {
		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			BufferedDataTable[] input) throws NotConfigurableException {
		List<String> selectedIDs;
		Map<String, Color> colors;
		Map<String, Shape> shapes;
		int selectAllIDS;
		int manualRange;
		double minX;
		double maxX;
		double minY;
		double maxY;

		try {
			selectedIDs = DataViewAndSelectNodeModel.readSelectedIDs(settings);
		} catch (InvalidSettingsException e) {
			selectedIDs = new ArrayList<String>();
		}

		try {
			colors = DataViewAndSelectNodeModel.readColors(settings);
		} catch (InvalidSettingsException e) {
			colors = new HashMap<String, Color>();
		}

		try {
			shapes = DataViewAndSelectNodeModel.readShapes(settings);
		} catch (InvalidSettingsException e) {
			shapes = new HashMap<String, Shape>();
		}

		try {
			selectAllIDS = settings
					.getInt(DataViewAndSelectNodeModel.CFG_SELECTALLIDS);
		} catch (InvalidSettingsException e) {
			selectAllIDS = DataViewAndSelectNodeModel.DEFAULT_SELECTALLIDS;
		}

		try {
			manualRange = settings
					.getInt(DataViewAndSelectNodeModel.CFG_MANUALRANGE);
		} catch (InvalidSettingsException e) {
			manualRange = DataViewAndSelectNodeModel.DEFAULT_MANUALRANGE;
		}

		try {
			minX = settings.getDouble(DataViewAndSelectNodeModel.CFG_MINX);
		} catch (InvalidSettingsException e) {
			minX = DataViewAndSelectNodeModel.DEFAULT_MINX;
		}

		try {
			maxX = settings.getDouble(DataViewAndSelectNodeModel.CFG_MAXX);
		} catch (InvalidSettingsException e) {
			maxX = DataViewAndSelectNodeModel.DEFAULT_MAXX;
		}

		try {
			minY = settings.getDouble(DataViewAndSelectNodeModel.CFG_MINY);
		} catch (InvalidSettingsException e) {
			minY = DataViewAndSelectNodeModel.DEFAULT_MINY;
		}

		try {
			maxY = settings.getDouble(DataViewAndSelectNodeModel.CFG_MAXY);
		} catch (InvalidSettingsException e) {
			maxY = DataViewAndSelectNodeModel.DEFAULT_MAXY;
		}

		try {
			reader = new TableReader(input[0], new TimeSeriesSchema());
		} catch (PmmException e) {
			reader = null;
			e.printStackTrace();
		}

		if (selectAllIDS == 1) {
			selectedIDs = reader.getIds();
		}

		((JPanel) getTab("Options")).removeAll();
		((JPanel) getTab("Options")).add(createMainComponent(selectedIDs,
				colors, shapes, manualRange == 1, minX, maxX, minY, maxY));
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		DataViewAndSelectNodeModel.writeSelectedIDs(
				selectionPanel.getSelectedIDs(), settings);
		DataViewAndSelectNodeModel.writeColors(selectionPanel.getColors(),
				settings);
		DataViewAndSelectNodeModel.writeShapes(selectionPanel.getShapes(),
				settings);

		settings.addInt(DataViewAndSelectNodeModel.CFG_SELECTALLIDS, 0);

		if (configPanel.isUseManualRange()) {
			settings.addInt(DataViewAndSelectNodeModel.CFG_MANUALRANGE, 1);
		} else {
			settings.addInt(DataViewAndSelectNodeModel.CFG_MANUALRANGE, 0);
		}

		settings.addDouble(DataViewAndSelectNodeModel.CFG_MINX,
				configPanel.getMinX());
		settings.addDouble(DataViewAndSelectNodeModel.CFG_MAXX,
				configPanel.getMaxX());
		settings.addDouble(DataViewAndSelectNodeModel.CFG_MINY,
				configPanel.getMinY());
		settings.addDouble(DataViewAndSelectNodeModel.CFG_MAXY,
				configPanel.getMaxY());
	}

	private JComponent createMainComponent(List<String> selectedIDs,
			Map<String, Color> colors, Map<String, Shape> shapes,
			boolean manualRange, double minX, double maxX, double minY,
			double maxY) {
		configPanel = new DataAndModelChartConfigPanel(
				DataAndModelChartConfigPanel.NO_PARAMETER_INPUT);
		configPanel.setParamsX(Arrays.asList(TimeSeriesSchema.ATT_TIME));
		configPanel.setParamsY(Arrays.asList(TimeSeriesSchema.ATT_LOGC));
		configPanel.setUseManualRange(manualRange);
		configPanel.setMinX(minX);
		configPanel.setMaxX(maxX);
		configPanel.setMinY(minY);
		configPanel.setMaxY(maxY);
		configPanel.addConfigListener(this);
		selectionPanel = new DataAndModelSelectionPanel(reader.getIds(), false,
				reader.getStringColumns(), reader.getStringColumnValues(),
				reader.getDoubleColumns(), reader.getDoubleColumnValues(),
				Arrays.asList(true), Arrays.asList(false));
		selectionPanel.setColors(colors);
		selectionPanel.setShapes(shapes);
		selectionPanel.addSelectionListener(this);
		chartCreator = new DataAndModelChartCreator(reader.getPlotables(),
				reader.getShortLegend(), reader.getLongLegend());
		infoPanel = new DataAndModelChartInfoPanel(reader.getIds(),
				reader.getInfoParameters(), reader.getInfoParameterValues());

		if (selectedIDs != null) {
			selectionPanel.setSelectedIDs(selectedIDs);
		}

		JSplitPane upperSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				chartCreator, selectionPanel);
		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(configPanel, BorderLayout.WEST);
		bottomPanel.add(infoPanel, BorderLayout.CENTER);

		return new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperSplitPane,
				bottomPanel);
	}

	private void createChart() {
		chartCreator.setParamX(configPanel.getParamX());
		chartCreator.setParamY(configPanel.getParamY());
		chartCreator.setTransformY(configPanel.getTransformY());
		chartCreator.setUseManualRange(configPanel.isUseManualRange());
		chartCreator.setMinX(configPanel.getMinX());
		chartCreator.setMinY(configPanel.getMinY());
		chartCreator.setMaxX(configPanel.getMaxX());
		chartCreator.setMaxY(configPanel.getMaxY());
		chartCreator.setDrawLines(configPanel.isDrawLines());
		chartCreator.setShowLegend(configPanel.isShowLegend());
		chartCreator.setAddInfoInLegend(configPanel.isAddInfoInLegend());
		chartCreator.setColors(selectionPanel.getColors());
		chartCreator.setShapes(selectionPanel.getShapes());

		if (configPanel.isDisplayFocusedRow()) {
			chartCreator.createChart(selectionPanel.getFocusedID());
		} else {
			chartCreator.createChart(selectionPanel.getSelectedIDs());
		}
	}

	@Override
	public void selectionChanged() {
		createChart();
	}

	@Override
	public void focusChanged() {
		infoPanel.showID(selectionPanel.getFocusedID());

		if (configPanel.isDisplayFocusedRow()) {
			createChart();
		}
	}

	@Override
	public void configChanged() {
		createChart();
	}

}
