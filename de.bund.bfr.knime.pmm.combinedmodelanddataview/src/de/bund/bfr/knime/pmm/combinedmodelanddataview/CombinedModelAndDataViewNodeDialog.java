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
package de.bund.bfr.knime.pmm.combinedmodelanddataview;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.data.DataTable;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.pmm.common.chart.ChartAllPanel;
import de.bund.bfr.knime.pmm.common.chart.ChartConfigPanel;
import de.bund.bfr.knime.pmm.common.chart.ChartConstants;
import de.bund.bfr.knime.pmm.common.chart.ChartCreator;
import de.bund.bfr.knime.pmm.common.chart.ChartSelectionPanel;
import de.bund.bfr.knime.pmm.common.chart.Plotable;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.SchemaFactory;

/**
 * <code>NodeDialog</code> for the "CombinedModelAndDataView" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class CombinedModelAndDataViewNodeDialog extends DataAwareNodeDialogPane
		implements ChartSelectionPanel.SelectionListener,
		ChartConfigPanel.ConfigListener {

	private TableReader reader;
	private SettingsHelper set;

	private ChartCreator chartCreator;
	private ChartSelectionPanel selectionPanel;
	private ChartConfigPanel configPanel;

	/**
	 * New pane for configuring the CombinedModelAndDataView node.
	 */
	protected CombinedModelAndDataViewNodeDialog() {
		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		addTab("Options", panel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			BufferedDataTable[] input) throws NotConfigurableException {
		set = new SettingsHelper();
		set.loadSettings(settings);

		DataTable table = input[0];

		if (SchemaFactory.createDataSchema().conforms(table)) {
			reader = new TableReader(table, true);
		} else {
			reader = new TableReader(table, false);
		}

		((JPanel) getTab("Options")).removeAll();
		((JPanel) getTab("Options")).add(createMainComponent());
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		if (!selectionPanel.getSelectedIDs().isEmpty()) {
			set.setSelectedID(selectionPanel.getSelectedIDs().get(0));
		} else {
			set.setSelectedID(null);
		}

		set.setCurrentParamX(configPanel.getParamX());
		set.setParamXValues(configPanel.getParamXValues());
		set.setColors(selectionPanel.getColors());
		set.setShapes(selectionPanel.getShapes());
		set.setManualRange(configPanel.isUseManualRange());
		set.setMinX(configPanel.getMinX());
		set.setMaxX(configPanel.getMaxX());
		set.setMinY(configPanel.getMinY());
		set.setMaxY(configPanel.getMaxY());
		set.setDrawLines(configPanel.isDrawLines());
		set.setShowLegend(configPanel.isShowLegend());
		set.setAddLegendInfo(configPanel.isAddInfoInLegend());
		set.setDisplayHighlighted(configPanel.isDisplayFocusedRow());
		set.setExportAsSvg(configPanel.isExportAsSvg());
		set.setUnitX(configPanel.getUnitX());
		set.setUnitY(configPanel.getUnitY());
		set.setTransformY(configPanel.getTransformY());
		set.setStandardVisibleColumns(false);
		set.setVisibleColumns(selectionPanel.getVisibleColumns());
		set.setModelFilter(selectionPanel.getFilter(Model1Schema.MODELNAME));
		set.setDataFilter(selectionPanel.getFilter(AttributeUtilities.DATAID));
		set.setFittedFilter(selectionPanel.getFilter(ChartConstants.STATUS));
		set.saveSettings(settings);
	}

	private JComponent createMainComponent() {
		if (set.isStandardVisibleColumns()) {
			set.setVisibleColumns(reader.getStandardVisibleColumns());
		}

		configPanel = new ChartConfigPanel(ChartConfigPanel.PARAMETER_FIELDS,
				false, null);

		if (set.getSelectedID() != null
				&& reader.getPlotables().get(set.getSelectedID()) != null) {
			Plotable plotable = reader.getPlotables().get(set.getSelectedID());

			configPanel.setParameters(plotable.getFunctionValue(),
					plotable.getPossibleArgumentValues(true, true),
					plotable.getMinArguments(), plotable.getMaxArguments(),
					plotable.getCategories(), plotable.getUnits(), null);
			configPanel.setParamX(set.getCurrentParamX());
			configPanel.setParamXValues(set.getParamXValues());
			configPanel.setUnitX(set.getUnitX());
			configPanel.setUnitY(set.getUnitY());
		}

		configPanel.setUseManualRange(set.isManualRange());
		configPanel.setMinX(set.getMinX());
		configPanel.setMaxX(set.getMaxX());
		configPanel.setMinY(set.getMinY());
		configPanel.setMaxY(set.getMaxY());
		configPanel.setDrawLines(set.isDrawLines());
		configPanel.setShowLegend(set.isShowLegend());
		configPanel.setAddInfoInLegend(set.isAddLegendInfo());
		configPanel.setDisplayFocusedRow(set.isDisplayHighlighted());
		configPanel.setExportAsSvg(set.isExportAsSvg());
		configPanel.setTransformY(set.getTransformY());
		configPanel.addConfigListener(this);
		selectionPanel = new ChartSelectionPanel(reader.getIds(), true,
				reader.getStringColumns(), reader.getStringColumnValues(),
				reader.getDoubleColumns(), reader.getDoubleColumnValues(),
				reader.getConditions(), reader.getConditionValues(), null,
				null, reader.getConditionUnits(), set.getVisibleColumns(),
				reader.getFilterableStringColumns(), reader.getData(),
				reader.getParameterData(), reader.getFormulas());
		selectionPanel.setColors(set.getColors());
		selectionPanel.setShapes(set.getShapes());
		selectionPanel.setFilter(Model1Schema.MODELNAME, set.getModelFilter());
		selectionPanel
				.setFilter(AttributeUtilities.DATAID, set.getDataFilter());
		selectionPanel.setFilter(ChartConstants.STATUS, set.getFittedFilter());
		selectionPanel.addSelectionListener(this);
		chartCreator = new ChartCreator(reader.getPlotables(),
				reader.getShortLegend(), reader.getLongLegend());

		if (set.getSelectedID() != null) {
			selectionPanel.setSelectedIDs(Arrays.asList(set.getSelectedID()));
		}

		return new ChartAllPanel(chartCreator, selectionPanel, configPanel);
	}

	private void createChart() {
		String selectedID = null;

		if (configPanel.isDisplayFocusedRow()) {
			selectedID = selectionPanel.getFocusedID();
		} else {
			if (!selectionPanel.getSelectedIDs().isEmpty()) {
				selectedID = selectionPanel.getSelectedIDs().get(0);
			}
		}

		if (selectedID != null) {
			Plotable plotable = chartCreator.getPlotables().get(selectedID);

			configPanel.setParameters(plotable.getFunctionValue(),
					plotable.getPossibleArgumentValues(true, true),
					plotable.getMinArguments(), plotable.getMaxArguments(),
					plotable.getCategories(), plotable.getUnits(), null);
			plotable.setFunctionArguments(configPanel.getParamsX());
			chartCreator.setParamX(configPanel.getParamX());
			chartCreator.setParamY(configPanel.getParamY());
			chartCreator.setUnitX(configPanel.getUnitX());
			chartCreator.setUnitY(configPanel.getUnitY());
			chartCreator.setTransformY(configPanel.getTransformY());
		} else {
			configPanel.setParameters(null, null, null, null, null, null, null);
			chartCreator.setParamX(null);
			chartCreator.setParamY(null);
			chartCreator.setUnitX(null);
			chartCreator.setUnitY(null);
			chartCreator.setTransformY(null);
		}

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
		chartCreator.createChart(selectedID);
	}

	@Override
	public void configChanged() {
		createChart();
	}

	@Override
	public void selectionChanged() {
		createChart();
	}

	@Override
	public void focusChanged() {
		if (configPanel.isDisplayFocusedRow()) {
			createChart();
		}
	}

}
