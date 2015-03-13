package de.bund.bfr.knime.pmm.sbmlutil;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;

/** Wrapper class for SBML unit definitions */
public class UnitDefinitionWrapper {
	private UnitDefinition unitDefinition;

	public UnitDefinitionWrapper(UnitDefinition unitDefinition) {
		this.unitDefinition = unitDefinition;
	}

	public UnitDefinition getUnitDefinition() {
		return unitDefinition;
	}

	/**
	 * Creates a UnitDefinitionWrapper from a XML string.
	 * 
	 * @param xml
	 *            . XML string containing a valid SBML UnitDefinition.
	 * @return
	 */
	public static UnitDefinitionWrapper xmlToUnitDefinition(String xml) {
		String preXml = "<?xml version='1.0' encoding='UTF-8' standalone='no'?>"
				+ "<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\">"
				+ "<model id=\"ID\">" + "<listOfUnitDefinitions>";
		String postXml = "</listOfUnitDefinitions>" + "</model>" + "</sbml>";

		String totalXml = preXml + xml + postXml;

		try {
			// Create SBMLDocument and get its model
			SBMLDocument sbmlDoc = SBMLReader.read(totalXml);
			Model model = sbmlDoc.getModel();

			// Create a new UnitDefinition from XML
			UnitDefinition xmlUnitDef = model.getUnitDefinition(0);
			UnitDefinition ud = new UnitDefinition(3, 1);
			for (Unit unit : xmlUnitDef.getListOfUnits()) {
				ud.addUnit(new Unit(unit));
			}

			UnitDefinitionWrapper wrapper = new UnitDefinitionWrapper(ud);
			return wrapper;
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return null;
		}
	}
}
