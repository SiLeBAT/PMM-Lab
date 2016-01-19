package de.bund.bfr.knime.pmm.js.common;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Strings;

/**
 * PmmLab parameter. Holds:
 * <ul>
 * <li>name
 * <li>original name
 * <li>value
 * <li>error
 * <li>minimum value
 * <li>maximum value
 * <li>P
 * <li>T
 * <li>minimum guess
 * <li>maximum guess
 * <li>unit
 * <li>description
 * <li>correlation names
 * <li>correlation values
 * </ul>
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class Param implements ViewValue {

	// Configuration keys
	static final String NAME = "name";
	static final String ORIGNAME = "origname";
	static final String ISSTART = "isStart";
	static final String VALUE = "value";
	static final String ERROR = "error";
	static final String MIN = "min";
	static final String MAX = "max";
	static final String P = "P";
	static final String T = "t";
	static final String MINGUESS = "minGuess";
	static final String MAXGUESS = "maxGuess";
	static final String CATEGORY = "category";
	static final String UNIT = "unit";
	static final String DESCRIPTION = "description";
	static final String CORRELATION_NAMES = "correlationNames";
	static final String CORRELATION_VALUES = "correlationValues";

	private String name;
	private String origname;
	private Boolean isStart;
	private Double value;
	private Double error;
	private Double min;
	private Double max;
	private Double p;
	private Double t;
	private Double minGuess;
	private Double maxGuess;
	private String category;
	private String unit;
	private String description;
	private String[] correlationNames;
	private double[] correlationValues;

	/** Returns the name of this {@link Param}. If not set, returns null. */
	public String getName() {
		return name;
	}

	/**
	 * Returns the original name of this {@link Param}. If not set, returns
	 * null.
	 */
	public String getOrigName() {
		return origname;
	}

	/**
	 * Returns TRUE if this {@link Param} is a start parameter. If not set, returns
	 * null.
	 */
	public Boolean isStart() {
		return isStart;
	}

	/** Returns the value of this {@link Param}. If not set, returns null. */
	public Double getValue() {
		return value;
	}

	/** Returns the error of this {@link Param}. If not set, returns null. */
	public Double getError() {
		return error;
	}

	/**
	 * Returns the minimum value of this {@link Param}. If not set, returns
	 * null.
	 */
	public Double getMin() {
		return min;
	}

	/**
	 * Returns the maximum value of this {@link Param}. If not set, returns
	 * null.
	 */
	public Double getMax() {
		return max;
	}

	/** Returns the P of this {@link Param}. If not set, returns null. */
	public Double getP() {
		return p;
	}

	/** Returns the T of this {@link Param}. If not set, returns null. */
	public Double getT() {
		return t;
	}

	/**
	 * Returns the minimum guess of this {@link Param}. If not set, returns
	 * null.
	 */
	public Double getMinGuess() {
		return minGuess;
	}

	/**
	 * Returns the maximum guess of this {@link Param}. If not set, returns
	 * null.
	 */
	public Double getMaxGuess() {
		return maxGuess;
	}

	/** Returns the category of this {@link Param}. If not set, returns null. */
	public String getCategory() {
		return category;
	}

	/** Returns the unit of this {@link Param}. If not set, returns null. */
	public String getUnit() {
		return unit;
	}

	/**
	 * Returns the description of this {@link Param}. If not set, returns null.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the correlation names of this {@link Param}. If not set, returns
	 * null.
	 */
	public String[] getCorrelationNames() {
		return correlationNames;
	}

	/**
	 * Returns the correlation values of this {@link Param}. If not set, returns
	 * null.
	 */
	public double[] getCorrelationValues() {
		return correlationValues;
	}

	/** Sets the name value with 'name'. Converts empty strings to null. */
	public void setName(final String name) {
		this.name = Strings.emptyToNull(name);
	}

	/**
	 * Sets the original name value with 'origName'. Converts empty strings to
	 * null.
	 */
	public void setOrigName(final String origName) {
		this.origname = Strings.emptyToNull(origName);
	}

	/** Sets the state of the parameter as start parameter to 'isStart'. */
	public void setIsStart(final Boolean isStart) {
		this.isStart = isStart;
	}

	/** Sets the value with 'value'. */
	public void setValue(final Double value) {
		this.value = value;
	}

	/** Sets the error with 'error'. */
	public void setError(final Double error) {
		this.error = error;
	}

	/** Sets the minimum value with 'min'. */
	public void setMin(final Double min) {
		this.min = min;
	}

	/** Sets the maximum value with 'max'. */
	public void setMax(final Double max) {
		this.max = max;
	}

	/** Sets P with 'p'. */
	public void setP(final Double p) {
		this.p = p;
	}

	/** Sets T with 't'. */
	public void setT(final Double t) {
		this.t = t;
	}

	/** Sets the minimum guess with 'minGuess'. */
	public void setMinGuess(final Double minGuess) {
		this.minGuess = minGuess;
	}

	/** Sets the maximum guess with 'maxGuess'. */
	public void setMaxGuess(final Double maxGuess) {
		this.maxGuess = maxGuess;
	}

	/** Sets the category with 'category'. Ignores null and empty strings. */
	public void setCategory(final String category) {
		this.category = Strings.emptyToNull(category);
	}

	/** Sets the unit with 'unit'. Ignores null and empty strings. */
	public void setUnit(final String unit) {
		this.unit = Strings.emptyToNull(unit);
	}

	/**
	 * Sets the description with 'description'. Ignores null and empty strings.
	 */
	public void setDescription(final String description) {
		this.description = Strings.emptyToNull(description);
	}

	/** Sets the correlation names with 'correlationNames'. */
	public void setCorrelationNames(final String[] correlationNames) {
		this.correlationNames = correlationNames;
	}

	/** Sets the correlation values with 'correlationValues'. */
	public void setCorrelationValues(final double[] correlationValues) {
		this.correlationValues = correlationValues;
	}

	public void saveToNodeSettings(NodeSettingsWO settings) {
		SettingsHelper.addString(NAME, name, settings);
		SettingsHelper.addString(ORIGNAME, origname, settings);
		SettingsHelper.addBoolean(ISSTART, isStart, settings);
		SettingsHelper.addDouble(VALUE, value, settings);
		SettingsHelper.addDouble(ERROR, error, settings);
		SettingsHelper.addDouble(MIN, min, settings);
		SettingsHelper.addDouble(MAX, max, settings);
		SettingsHelper.addDouble(P, p, settings);
		SettingsHelper.addDouble(T, t, settings);
		SettingsHelper.addDouble(MINGUESS, minGuess, settings);
		SettingsHelper.addDouble(MAXGUESS, maxGuess, settings);
		SettingsHelper.addString(CATEGORY, category, settings);
		SettingsHelper.addString(UNIT, unit, settings);
		SettingsHelper.addString(DESCRIPTION, description, settings);
		settings.addStringArray(CORRELATION_NAMES, correlationNames);
		settings.addDoubleArray(CORRELATION_VALUES, correlationValues);
	}

	public void loadFromNodeSettings(NodeSettingsRO settings) {
		name = SettingsHelper.getString(NAME, settings);
		origname = SettingsHelper.getString(ORIGNAME, settings);
		isStart = SettingsHelper.getBoolean(ISSTART, settings);
		value = SettingsHelper.getDouble(VALUE, settings);
		error = SettingsHelper.getDouble(ERROR, settings);
		min = SettingsHelper.getDouble(MIN, settings);
		max = SettingsHelper.getDouble(MAX, settings);
		p = SettingsHelper.getDouble(P, settings);
		t = SettingsHelper.getDouble(T, settings);
		minGuess = SettingsHelper.getDouble(MINGUESS, settings);
		maxGuess = SettingsHelper.getDouble(MAXGUESS, settings);
		category = SettingsHelper.getString(CATEGORY, settings);
		unit = SettingsHelper.getString(UNIT, settings);
		description = SettingsHelper.getString(DESCRIPTION, settings);
		try {
			correlationNames = settings.getStringArray(CORRELATION_NAMES);
			correlationValues = settings.getDoubleArray(CORRELATION_VALUES);
		} catch (InvalidSettingsException e) {
			correlationNames = null;
			correlationValues = null;
		}
	}
}
