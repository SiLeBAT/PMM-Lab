package de.bund.bfr.knime.pmm.annotation;

import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLTriple;

/**
 * Created date xml node. Uses the dcterms:created tag. Example:
 * <dcterms:created>Thu Jan 01 01:00:00 CET 1970</dcterms:created>
 * 
 * @author Miguel Alba
 */
public class CreatedNode extends SBMLNodeBase {

	public CreatedNode(String created) {
		XMLTriple triple = new XMLTriple("created", null, "dcterms");
		node = new XMLNode(triple);
		node.addChild(new XMLNode(created));
	}
	
	public CreatedNode(XMLNode node) {
		this.node = node;
	}
	
	public String getCreated() {
		return node.getChild(0).getCharacters();
	}
}