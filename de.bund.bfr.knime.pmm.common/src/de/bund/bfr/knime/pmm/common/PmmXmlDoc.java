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
package de.bund.bfr.knime.pmm.common;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

public class PmmXmlDoc {

	private static final String ELEMENT_PMMDOC = "PmmDoc";
	
	private LinkedList<PmmXmlElementConvertable> set;
	
	public PmmXmlDoc() {
		set = new LinkedList<PmmXmlElementConvertable>();
	}
	
	
	public PmmXmlDoc( String docString ) throws IOException, JDOMException {
		
		this();
		
		Document doc;
		SAXBuilder builder;
		Element rootElement;
		
		builder = new SAXBuilder();
		doc = builder.build( new StringReader( docString ) );
		
		rootElement = doc.getRootElement();
		
		for( Element el : rootElement.getChildren() )
			
			if( el.getName().equals( ParametricModel.ELEMENT_PARAMETRICMODEL ) )
				set.add( new ParametricModel( el ) );
		
		
	}
	
	public void add( PmmXmlElementConvertable el ) {
		set.add( el );
	}
	
	public Document toXmlDocument() {
		
		Document doc;
		Element rootElement;
		
		doc = new Document();
		
		rootElement = new Element( ELEMENT_PMMDOC );
		doc.setRootElement( rootElement );
		
		for( PmmXmlElementConvertable model : set )
			rootElement.addContent( model.toXmlElement() );
		
		return doc;
	}
	
	public String toXmlString() {
		
		Document doc;
		XMLOutputter xmlo;
		
		doc = toXmlDocument();
		
		xmlo = new XMLOutputter();
		return xmlo.outputString( doc );
	}
	
	public int size() { return set.size(); }
	public PmmXmlElementConvertable get( int i ) { return set.get( i ); }
	
	public Collection<PmmXmlElementConvertable> getModelSet() { return set; }
	
}
