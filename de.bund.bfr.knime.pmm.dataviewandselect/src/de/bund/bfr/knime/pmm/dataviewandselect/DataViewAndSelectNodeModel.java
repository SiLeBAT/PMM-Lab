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

import java.awt.Color;
import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;

import de.bund.bfr.knime.pmm.common.CollectionUtilities;
import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.chart.ChartConstants;
import de.bund.bfr.knime.pmm.common.chart.ChartCreator;
import de.bund.bfr.knime.pmm.common.chart.ChartUtilities;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * This is the model implementation of DataViewAndSelect.
 * 
 * 
 * @author Christian Thoens
 */
public class DataViewAndSelectNodeModel extends NodeModel {

	static final String CFG_SELECTEDIDS = "SelectedIDs";
	static final String CFG_COLORS = "Colors";
	static final String CFG_SHAPES = "Shapes";
	static final String CFG_SELECTALLIDS = "SelectAllIDs";
	static final String CFG_MANUALRANGE = "ManualRange";
	static final String CFG_MINX = "MinX";
	static final String CFG_MAXX = "MaxX";
	static final String CFG_MINY = "MinY";
	static final String CFG_MAXY = "MaxY";
	static final String CFG_DRAWLINES = "DrawLines";
	static final String CFG_SHOWLEGEND = "ShowLegend";
	static final String CFG_ADDLEGENDINFO = "AddLegendInfo";
	static final String CFG_DISPLAYHIGHLIGHTED = "DisplayHighlighted";
	static final String CFG_TRANSFORMY = "TransformY";
	static final String CFG_VISIBLECOLUMNS = "VisibleColumns";

	static final int DEFAULT_SELECTALLIDS = 0;
	static final int DEFAULT_MANUALRANGE = 0;
	static final double DEFAULT_MINX = 0.0;
	static final double DEFAULT_MAXX = 100.0;
	static final double DEFAULT_MINY = 0.0;
	static final double DEFAULT_MAXY = 10.0;
	static final int DEFAULT_DRAWLINES = 0;
	static final int DEFAULT_SHOWLEGEND = 1;
	static final int DEFAULT_ADDLEGENDINFO = 0;
	static final int DEFAULT_DISPLAYHIGHLIGHTED = 0;
	static final String DEFAULT_TRANSFORMY = ChartConstants.NO_TRANSFORM;
	static final String DEFAULT_VISIBLECOLUMNS = TimeSeriesSchema.DATAID;

	private List<String> selectedIDs;
	private Map<String, Color> colors;
	private Map<String, Shape> shapes;
	private int selectAllIDs;
	private int manualRange;
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private int drawLines;
	private int showLegend;
	private int addLegendInfo;
	private int displayHighlighted;
	private String transformY;
	private List<String> visibleColumns;

	private KnimeSchema schema;

	/**
	 * Constructor for the node model.
	 */
	protected DataViewAndSelectNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] {
				BufferedDataTable.TYPE, ImagePortObject.TYPE });
		schema = new TimeSeriesSchema();
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		BufferedDataTable table = (BufferedDataTable) inObjects[0];
		TableReader reader = new TableReader(table, schema);
		List<String> ids;

		if (selectAllIDs == 1) {
			ids = reader.getIds();
		} else {
			ids = selectedIDs;
		}

		BufferedDataContainer container = exec.createDataContainer(schema
				.createSpec());
		Set<String> idSet = new LinkedHashSet<String>(ids);
		int index = 0;

		for (int i = 0; i < reader.getAllTuples().size(); i++) {
			KnimeTuple tuple = reader.getAllTuples().get(i);
			String id = reader.getAllIds().get(i);

			if (idSet.contains(id)) {
				container.addRowToTable(tuple);
			}

			exec.checkCanceled();
			exec.setProgress((double) index / (double) table.getRowCount(), "");
			index++;
		}

		container.close();

		ChartCreator creator = new ChartCreator(reader.getPlotables(),
				reader.getShortLegend(), reader.getLongLegend());

		creator.setParamX(TimeSeriesSchema.TIME);
		creator.setParamY(TimeSeriesSchema.LOGC);
		creator.setTransformY(ChartConstants.NO_TRANSFORM);
		creator.setColors(colors);
		creator.setShapes(shapes);
		creator.setUseManualRange(manualRange == 1);
		creator.setMinX(minX);
		creator.setMaxX(maxX);
		creator.setMinY(minY);
		creator.setMaxY(maxY);
		creator.setDrawLines(drawLines == 1);
		creator.setShowLegend(showLegend == 1);
		creator.setAddInfoInLegend(addLegendInfo == 1);
		creator.setTransformY(transformY);

		return new PortObject[] {
				container.getTable(),
				new ImagePortObject(ChartUtilities.convertToPNGImageContent(
						creator.getChart(ids), 640, 480),
						new ImagePortObjectSpec(PNGImageContent.TYPE)) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		try {
			if (!schema.conforms((DataTableSpec) inSpecs[0])) {
				throw new InvalidSettingsException("Wrong input!");
			}

			return new PortObjectSpec[] { schema.createSpec(),
					new ImagePortObjectSpec(PNGImageContent.TYPE) };
		} catch (PmmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(CFG_SELECTEDIDS,
				CollectionUtilities.getStringFromList(selectedIDs));
		settings.addString(CFG_COLORS,
				CollectionUtilities.getStringFromMap(colors));
		settings.addString(CFG_SHAPES,
				CollectionUtilities.getStringFromMap(shapes));
		settings.addInt(CFG_SELECTALLIDS, selectAllIDs);
		settings.addInt(CFG_MANUALRANGE, manualRange);
		settings.addDouble(CFG_MINX, minX);
		settings.addDouble(CFG_MAXX, maxX);
		settings.addDouble(CFG_MINY, minY);
		settings.addDouble(CFG_MAXY, maxY);
		settings.addInt(CFG_DRAWLINES, drawLines);
		settings.addInt(CFG_SHOWLEGEND, showLegend);
		settings.addInt(CFG_ADDLEGENDINFO, addLegendInfo);
		settings.addInt(CFG_DISPLAYHIGHLIGHTED, displayHighlighted);
		settings.addString(CFG_TRANSFORMY, transformY);
		settings.addString(CFG_VISIBLECOLUMNS,
				CollectionUtilities.getStringFromList(visibleColumns));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			selectedIDs = CollectionUtilities.getStringListFromString(settings
					.getString(CFG_SELECTEDIDS));
		} catch (InvalidSettingsException e) {
			selectedIDs = new ArrayList<String>();
		}

		try {
			colors = CollectionUtilities.getColorMapFromString(settings
					.getString(CFG_COLORS));
		} catch (InvalidSettingsException e) {
			colors = new LinkedHashMap<String, Color>();
		}

		try {
			shapes = CollectionUtilities.getShapeMapFromString(settings
					.getString(CFG_SHAPES));
		} catch (InvalidSettingsException e) {
			shapes = new LinkedHashMap<String, Shape>();
		}

		try {
			selectAllIDs = settings.getInt(CFG_SELECTALLIDS);
		} catch (InvalidSettingsException e) {
			selectAllIDs = DEFAULT_SELECTALLIDS;
		}

		try {
			manualRange = settings.getInt(CFG_MANUALRANGE);
		} catch (InvalidSettingsException e) {
			manualRange = DEFAULT_MANUALRANGE;
		}

		try {
			minX = settings.getDouble(CFG_MINX);
		} catch (InvalidSettingsException e) {
			minX = DEFAULT_MINX;
		}

		try {
			maxX = settings.getDouble(CFG_MAXX);
		} catch (InvalidSettingsException e) {
			maxX = DEFAULT_MAXX;
		}

		try {
			minY = settings.getDouble(CFG_MINY);
		} catch (InvalidSettingsException e) {
			minY = DEFAULT_MINY;
		}

		try {
			maxY = settings.getDouble(CFG_MAXY);
		} catch (InvalidSettingsException e) {
			maxY = DEFAULT_MAXY;
		}

		try {
			drawLines = settings.getInt(CFG_DRAWLINES);
		} catch (InvalidSettingsException e) {
			drawLines = DEFAULT_DRAWLINES;
		}

		try {
			showLegend = settings.getInt(CFG_SHOWLEGEND);
		} catch (InvalidSettingsException e) {
			showLegend = DEFAULT_SHOWLEGEND;
		}

		try {
			addLegendInfo = settings.getInt(CFG_ADDLEGENDINFO);
		} catch (InvalidSettingsException e) {
			addLegendInfo = DEFAULT_ADDLEGENDINFO;
		}

		try {
			displayHighlighted = settings.getInt(CFG_DISPLAYHIGHLIGHTED);
		} catch (InvalidSettingsException e) {
			displayHighlighted = DEFAULT_DISPLAYHIGHLIGHTED;
		}

		try {
			transformY = settings.getString(CFG_TRANSFORMY);
		} catch (InvalidSettingsException e) {
			transformY = DEFAULT_TRANSFORMY;
		}

		try {
			visibleColumns = CollectionUtilities
					.getStringListFromString(settings
							.getString(CFG_VISIBLECOLUMNS));
		} catch (InvalidSettingsException e) {
			visibleColumns = CollectionUtilities
					.getStringListFromString(DEFAULT_VISIBLECOLUMNS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

}
