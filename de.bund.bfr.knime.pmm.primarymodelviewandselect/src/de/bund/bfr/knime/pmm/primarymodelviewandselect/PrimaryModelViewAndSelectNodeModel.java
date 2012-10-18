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
package de.bund.bfr.knime.pmm.primarymodelviewandselect;

import java.awt.Color;
import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.chart.ChartConstants;
import de.bund.bfr.knime.pmm.common.chart.ChartUtilities;
import de.bund.bfr.knime.pmm.common.chart.ColorAndShapeCreator;
import de.bund.bfr.knime.pmm.common.chart.DataAndModelChartCreator;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * This is the model implementation of ModelViewAndSelect.
 * 
 * 
 * @author Christian Thoens
 */
public class PrimaryModelViewAndSelectNodeModel extends NodeModel {

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
	static final String CFG_MODELFILTER = "ModelFilter";
	static final String CFG_DATAFILTER = "DataFilter";
	static final String CFG_FITTEDFILTER = "FittedFilter";

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
	static final String DEFAULT_MODELFILTER = "";
	static final String DEFAULT_DATAFILTER = "";
	static final String DEFAULT_FITTEDFILTER = "";

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
	private String modelFilter;
	private String dataFilter;
	private String fittedFilter;

	private KnimeSchema model1Schema;
	private KnimeSchema peiSchema;
	private KnimeSchema schema;

	/**
	 * Constructor for the node model.
	 */
	protected PrimaryModelViewAndSelectNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] {
				BufferedDataTable.TYPE, ImagePortObject.TYPE });
		selectedIDs = new ArrayList<String>();
		colors = new LinkedHashMap<String, Color>();
		shapes = new LinkedHashMap<String, Shape>();
		selectAllIDs = DEFAULT_SELECTALLIDS;
		manualRange = DEFAULT_MANUALRANGE;
		minX = DEFAULT_MINX;
		maxX = DEFAULT_MAXX;
		minY = DEFAULT_MINY;
		maxY = DEFAULT_MAXY;
		drawLines = DEFAULT_DRAWLINES;
		showLegend = DEFAULT_SHOWLEGEND;
		addLegendInfo = DEFAULT_ADDLEGENDINFO;
		displayHighlighted = DEFAULT_DISPLAYHIGHLIGHTED;
		transformY = DEFAULT_TRANSFORMY;
		modelFilter = DEFAULT_MODELFILTER;
		dataFilter = DEFAULT_DATAFILTER;
		fittedFilter = DEFAULT_FITTEDFILTER;

		try {
			model1Schema = new Model1Schema();
			peiSchema = new KnimeSchema(new Model1Schema(),
					new TimeSeriesSchema());
		} catch (PmmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		BufferedDataTable table = (BufferedDataTable) inObjects[0];
		TableReader reader = new TableReader(table, schema, schema == peiSchema);
		List<String> ids;

		if (selectAllIDs == 1) {
			ids = reader.getIds();
		} else {
			ids = selectedIDs;
		}

		BufferedDataContainer container = exec.createDataContainer(schema
				.createSpec());
		Set<String> idSet = new HashSet<String>(ids);
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

		DataAndModelChartCreator creator = new DataAndModelChartCreator(
				reader.getPlotables(), reader.getShortLegend(),
				reader.getLongLegend());

		creator.setParamX(TimeSeriesSchema.ATT_TIME);
		creator.setParamY(TimeSeriesSchema.ATT_LOGC);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		try {
			if (peiSchema.conforms((DataTableSpec) inSpecs[0])) {
				schema = peiSchema;
			} else if (model1Schema.conforms((DataTableSpec) inSpecs[0])) {
				schema = model1Schema;
			} else {
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
		if (selectedIDs != null) {
			writeSelectedIDs(selectedIDs, settings);
		}

		if (colors != null) {
			writeColors(colors, settings);
		}

		if (shapes != null) {
			writeShapes(shapes, settings);
		}

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
		settings.addString(CFG_MODELFILTER, modelFilter);
		settings.addString(CFG_DATAFILTER, dataFilter);
		settings.addString(CFG_FITTEDFILTER, fittedFilter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			selectedIDs = readSelectedIDs(settings);
		} catch (InvalidSettingsException e) {
			selectedIDs = new ArrayList<String>();
		}

		try {
			colors = readColors(settings);
		} catch (InvalidSettingsException e) {
			colors = new LinkedHashMap<String, Color>();
		}

		try {
			shapes = readShapes(settings);
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
			modelFilter = settings.getString(CFG_MODELFILTER);
		} catch (InvalidSettingsException e) {
			modelFilter = DEFAULT_MODELFILTER;
		}

		try {
			dataFilter = settings.getString(CFG_DATAFILTER);
		} catch (InvalidSettingsException e) {
			dataFilter = DEFAULT_DATAFILTER;
		}

		try {
			fittedFilter = settings.getString(CFG_FITTEDFILTER);
		} catch (InvalidSettingsException e) {
			fittedFilter = DEFAULT_FITTEDFILTER;
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

	protected static List<String> readSelectedIDs(NodeSettingsRO settings)
			throws InvalidSettingsException {
		String idString = settings.getString(CFG_SELECTEDIDS);
		List<String> selectedIDs = new ArrayList<String>();

		if (!idString.isEmpty()) {
			selectedIDs = new ArrayList<String>(Arrays.asList(idString
					.split(",")));
		} else {
			selectedIDs = new ArrayList<String>();
		}

		return selectedIDs;
	}

	protected static void writeSelectedIDs(List<String> selectedIDs,
			NodeSettingsWO settings) {
		StringBuilder idString = new StringBuilder();

		for (String id : selectedIDs) {
			idString.append(id);
			idString.append(",");
		}

		if (idString.length() > 0) {
			idString.deleteCharAt(idString.length() - 1);
		}

		settings.addString(CFG_SELECTEDIDS, idString.toString());
	}

	protected static Map<String, Color> readColors(NodeSettingsRO settings)
			throws InvalidSettingsException {
		String colorString = settings.getString(CFG_COLORS);
		Map<String, Color> colors = new LinkedHashMap<String, Color>();

		if (!colorString.isEmpty()) {

			for (String assignment : colorString.split(",")) {
				String[] sides = assignment.split(":");
				String id = sides[0];
				Color color = Color.decode(sides[1]);

				colors.put(id, color);
			}
		}

		return colors;
	}

	protected static void writeColors(Map<String, Color> colors,
			NodeSettingsWO settings) {
		StringBuilder colorString = new StringBuilder();

		for (String id : colors.keySet()) {
			Color color = colors.get(id);

			colorString.append(id);
			colorString.append(":");
			colorString.append("#");
			colorString
					.append(Integer.toHexString(color.getRGB()).substring(2));
			colorString.append(",");
		}

		if (colorString.length() > 0) {
			colorString.deleteCharAt(colorString.length() - 1);
		}

		settings.addString(CFG_COLORS, colorString.toString());
	}

	protected static Map<String, Shape> readShapes(NodeSettingsRO settings)
			throws InvalidSettingsException {
		String shapeString = settings.getString(CFG_SHAPES);
		Map<String, Shape> shapes = new LinkedHashMap<String, Shape>();
		Map<String, Shape> shapeMap = (new ColorAndShapeCreator(0))
				.getShapeByNameMap();

		if (!shapeString.isEmpty()) {
			for (String assignment : shapeString.split(",")) {
				String[] sides = assignment.split(":");
				String id = sides[0];
				String shapeName = sides[1];

				shapes.put(id, shapeMap.get(shapeName));
			}
		}

		return shapes;
	}

	protected static void writeShapes(Map<String, Shape> shapes,
			NodeSettingsWO settings) {
		StringBuilder shapeString = new StringBuilder();
		Map<Shape, String> shapeMap = (new ColorAndShapeCreator(0))
				.getNameByShapeMap();

		for (String id : shapes.keySet()) {
			Shape shape = shapes.get(id);

			shapeString.append(id);
			shapeString.append(":");
			shapeString.append(shapeMap.get(shape));
			shapeString.append(",");
		}

		if (shapeString.length() > 0) {
			shapeString.deleteCharAt(shapeString.length() - 1);
		}

		settings.addString(CFG_SHAPES, shapeString.toString());
	}

}
