package de.bund.bfr.knime.pmm.js.modelplotter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
* JavaScript view value, containing information about the function, variables, and constants. 
* 
* @author Kilian Thiel, KNIME.com GmbH, Berlin, Germany
*/
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ModelPlotterViewValue extends JSONViewContent {
	
	private String chartTitle;
	
	private double y0;
	
	private String function;
	
	private String functionFull;
	
	private Map<String, Double> constants;

	private List<Variable> variables;
	
	private String xUnit;
	
	private String yUnit;
	
	private int minXAxis;
	
	private int maxXAxis;
	
	private int minYAxis;
	
	private int maxYAxis;
	
	/**
	 * @return the min value of the y axis
	 */
	public int getMinYAxis() {
		return minYAxis;
	}

	/**
	 * @param minYAxis the min value of the y axis
	 */
	public void setMinYAxis(int minYAxis) {
		this.minYAxis = minYAxis;
	}

	/**
	 * @return the max value of the y axis
	 */
	public int getMaxYAxis() {
		return maxYAxis;
	}

	/**
	 * @param maxYAxis the max value of the y axis
	 */
	public void setMaxYAxis(int maxYAxis) {
		this.maxYAxis = maxYAxis;
	}	
	
	/**
	 * @return the min value of the x axis
	 */
	public int getMinXAxis() {
		return minXAxis;
	}

	/**
	 * @param minXAxis the min value of the x axis
	 */
	public void setMinXAxis(int minXAxis) {
		this.minXAxis = minXAxis;
	}

	/**
	 * @return the max value of the x axis
	 */
	public int getMaxXAxis() {
		return maxXAxis;
	}

	/**
	 * @param maxXAxis the max value of the x axis
	 */
	public void setMaxXAxis(int maxXAxis) {
		this.maxXAxis = maxXAxis;
	}

	/**
	 * @return the unit of the x axis.
	 */
	public String getxUnit() {
		return xUnit;
	}

	/**
	 * @param xUnit the unit of the x axis to set
	 */
	public void setxUnit(String xUnit) {
		this.xUnit = xUnit;
	}

	/**
	 * @return the unit of the y axis.
	 */
	public String getyUnit() {
		return yUnit;
	}

	/**
	 * @param yUnit the unit of the y axis to set.
	 */
	public void setyUnit(String yUnit) {
		this.yUnit = yUnit;
	}

	/**
	 * @return the value of Y0.
	 */
	public double getY0() {
		return y0;
	}

	/**
	 * @param y0 value to set for Y0.
	 */
	public void setY0(double y0) {
		this.y0 = y0;
	}

	/**
	 * @return the chart title.
	 */
	public String getChartTitle() {
		return chartTitle;
	}
	
	/**
	 * @param chartTitle the title to set.
	 */
	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
	}
	
	/**
	 * @return the expression of the plotable function as string.
	 */
	public String getFunc() {
		return function;
	}

	/**
	 * @param f the function expression to set. 
	 */
	public void setFunc(String f) {
		this.function = f;
	}	
	
	/**
	 * @return The full function string.
	 */
	public String getFunctionFull() {
		return functionFull;
	}

	/**
	 * @param functionFull the full function string.
	 */
	public void setFunctionFull(String functionFull) {
		this.functionFull = functionFull;
	}

	/**
	 * @return the constants of the function. 
	 */
	public Map<String, Double> getConstants() {
		return constants;
	}
    
	/**
	 * @param constants the constants of the function to set.
	 */
	public void setConstants(Map<String, Double> constants) {
		this.constants = constants;
	}
	
	/**
	 * @return the arguments of the function.
	 */
	public List<Variable> getVariables() {
		return variables;
	}
	
	/**
	 * @param variables the variables of the function.
	 */
	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}
	
	@JsonAutoDetect
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
	public static class Variable {
		
		private static final String NAME = "name";
		private static final String MIN = "MIN";
		private static final String MAX = "MAX";
		private static final String DEF = "DEF";
		
		private String name;
		
		private double min;
		
		private double max;
		
		private double def;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public double getMin() {
			return min;
		}

		public void setMin(double min) {
			this.min = min;
		}

		public double getMax() {
			return max;
		}

		public void setMax(double max) {
			this.max = max;
		}

		public double getDef() {
			return def;
		}

		public void setDef(double def) {
			this.def = def;
		}
		
		public void saveToNodeSettings(NodeSettingsWO settings) {
			settings.addString(NAME, getName());
			settings.addDouble(MIN, getMin());
			settings.addDouble(MAX, getMax());
			settings.addDouble(DEF, getDef());
		}
		
		public void loadFromNodeSettings(NodeSettingsRO settings)
				throws InvalidSettingsException {
			setName(settings.getString(NAME));
			setMin(settings.getDouble(MIN));
			setMax(settings.getDouble(MAX));
			setDef(settings.getDouble(DEF));
		}
	}

	
	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		settings.addString(ModelPlotterViewConfig.CHART_TITLE, getChartTitle());
		settings.addDouble(ModelPlotterViewConfig.Y0, getY0());
		settings.addString(ModelPlotterViewConfig.FUNCTION, getFunc());
		settings.addString(ModelPlotterViewConfig.FUNCTION_FULL, getFunctionFull());
		settings.addString(ModelPlotterViewConfig.X_UNIT, getxUnit());
		settings.addString(ModelPlotterViewConfig.Y_UNIT, getyUnit());
		settings.addInt(ModelPlotterViewConfig.MIN_X_AXIS, getMinXAxis());
		settings.addInt(ModelPlotterViewConfig.MAX_X_AXIS, getMaxXAxis());
		settings.addInt(ModelPlotterViewConfig.MIN_Y_AXIS, getMinYAxis());
		settings.addInt(ModelPlotterViewConfig.MAX_Y_AXIS, getMaxYAxis());
		
		String[] keysArray = new String[constants.size()];
		double[] valuesArray = new double[constants.size()];
		int i = 0;
		for (Entry<String, Double> e : constants.entrySet()) {
			keysArray[i] = e.getKey();
			valuesArray[i] = e.getValue();
			i++;
		}
		settings.addStringArray(ModelPlotterViewConfig.CONSTANTS_KEYS, keysArray);
		settings.addDoubleArray(ModelPlotterViewConfig.CONSTANTS_VALUES, valuesArray);
		
		settings.addInt(ModelPlotterViewConfig.VARIABLES, variables.size());
		for (i = 0; i < variables.size(); i++) {
			NodeSettingsWO varSettings = settings
					.addNodeSettings(ModelPlotterViewConfig.VARIABLES + i);	
			variables.get(i).saveToNodeSettings(varSettings);
		}
	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		setChartTitle(settings.getString(ModelPlotterViewConfig.CHART_TITLE));
		setY0(settings.getDouble(ModelPlotterViewConfig.Y0));
		setFunc(settings.getString(ModelPlotterViewConfig.FUNCTION));
		setFunctionFull(settings.getString(ModelPlotterViewConfig.FUNCTION_FULL));
		setxUnit(settings.getString(ModelPlotterViewConfig.X_UNIT));
		setyUnit(settings.getString(ModelPlotterViewConfig.Y_UNIT));
		setMinXAxis(settings.getInt(ModelPlotterViewConfig.MIN_X_AXIS));
		setMaxXAxis(settings.getInt(ModelPlotterViewConfig.MAX_X_AXIS));
		setMinYAxis(settings.getInt(ModelPlotterViewConfig.MIN_Y_AXIS));
		setMaxYAxis(settings.getInt(ModelPlotterViewConfig.MAX_Y_AXIS));
		
		String[] constantKeys = settings.getStringArray(ModelPlotterViewConfig.CONSTANTS_KEYS);
		double[] constantValues = settings.getDoubleArray(ModelPlotterViewConfig.CONSTANTS_VALUES);
		constants = new LinkedHashMap<String, Double>();
		if (constantKeys.length != constantValues.length) {
			throw new InvalidSettingsException("Error during loading of model constants!");
		}
		for (int i = 0; i < constantKeys.length; i++) {
			constants.put(constantKeys[i], constantValues[i]);
		}
		
		int noVariables = settings.getInt(ModelPlotterViewConfig.VARIABLES);
		variables = new ArrayList<Variable>(noVariables);
		for (int i = 0; i < noVariables; i++) {
			Variable v = new Variable();
			NodeSettingsRO varSetting = settings
					.getNodeSettings(ModelPlotterViewConfig.VARIABLES + i);
			v.loadFromNodeSettings(varSetting);
			variables.add(v);
		}
	}
}
