/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.pmf.sbml;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLTriple;

/**
 * Represents the unitDefinition XML element of a PMF-SBML file.
 *
 * @author Miguel Alba
 * @author Christian Thoens
 */
public class PMFUnitDefinition {

	private static final int LEVEL = 3;
	private static final int VERSION = 1;

	private static final String TRANSFORMATION = "transformation";

	String transformationName;
	UnitDefinition unitDefinition;

	/**
	 * Creates a PMFUnitDefinition instance from an id, name, transformationName and units.
	 */
	public PMFUnitDefinition(String id, String name, String transformationName, PMFUnit[] units) {
		unitDefinition = new UnitDefinition(id, name, LEVEL, VERSION);
		if (units != null) {
			for (PMFUnit unit : units) {
				unitDefinition.addUnit(new Unit(unit.unit));
			}
		}

		if (transformationName != null) {
			// Creates transformation node
			XMLNode transformationNode = new XMLNode(new XMLTriple(TRANSFORMATION, "", "pmmlab"));
			transformationNode.addChild(new XMLNode(transformationName));

			// Creates metadata node
			XMLNode metadata = new XMLNode(new XMLTriple("metadata", null, "pmf"));
			metadata.addChild(transformationNode);

			// Sets annotation
			unitDefinition.getAnnotation().setNonRDFAnnotation(metadata);

			// Copies transformation
			this.transformationName = transformationName;
		}
	}

	/**
	 * Creates a PMFUnitDefinition instance from an UnitDefinition.
	 */
	public PMFUnitDefinition(UnitDefinition unitDefinition) {
		this.unitDefinition = unitDefinition;
		if (unitDefinition.isSetAnnotation()) {
			XMLNode metadata = unitDefinition.getAnnotation().getNonRDFannotation().getChildElement("metadata", "");
			XMLNode transformationNode = metadata.getChildElement(TRANSFORMATION, "");
			transformationName = transformationNode.getChild(0).getCharacters();
		}
	}

	public UnitDefinition getUnitDefinition() {
		return unitDefinition;
	}

	public String getId() {
		return unitDefinition.getId();
	}


	public String getName() {
		return unitDefinition.getName();
	}


	public PMFUnit[] getUnits() {
		ListOf<Unit> units = unitDefinition.getListOfUnits();
		PMFUnit[] pmfUnits = new PMFUnit[units.size()];

		for (int i = 0; i < units.size(); i++) {
			Unit sbmlUnit = units.get(i);

			double multiplier = sbmlUnit.getMultiplier();
			int scale = sbmlUnit.getScale();
			Unit.Kind kind = sbmlUnit.getKind();
			double exponent = sbmlUnit.getExponent();

			pmfUnits[i] = new PMFUnit(multiplier, scale, kind, exponent);
		}

		return pmfUnits;
	}


	public String getTransformationName() {
		return transformationName;
	}

	/**
	 * Sets the id value with 'id'.
	 */
	public void setId(String id) {
		unitDefinition.setId(id);
	}

	/**
	 * Sets the name value with 'name'.
	 */
	public void setName(String name) {
		unitDefinition.setName(name);
	}

	/**
	 * Sets the units this {@link PMFUnitDefinition}.
	 */
	public void setUnits(PMFUnit[] units) {
		unitDefinition.clear();
		for (PMFUnit unit : units) {
			unitDefinition.addUnit(new Unit(unit.unit));
		}
	}

	/**
	 * Sets the transformation name value with 'transformationName'.
	 */
	public void setTransformationName(String transformationName) {
		this.transformationName = transformationName;
	}

	/**
	 * true if the transformation name is not null.
	 */
	public boolean isSetTransformationName() {
		return transformationName != null;
	}

	public String toString() {
		String string = String.format("unitDefinition [id=%s, name=%s]", unitDefinition.getId(),
				unitDefinition.getName());
		return string;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		PMFUnitDefinition other = (PMFUnitDefinition) obj;

		if (!unitDefinition.getId().equals(other.getId()))
			return false;

		if (!unitDefinition.getName().equals(other.getName()))
			return false;

		if (transformationName != null && other.transformationName != null
				&& !transformationName.equals(other.transformationName)) {
			return false;
		}

		if (!unitDefinition.getListOfUnits().equals(other.unitDefinition.getListOfUnits()))
			return false;

		return true;
	}
}