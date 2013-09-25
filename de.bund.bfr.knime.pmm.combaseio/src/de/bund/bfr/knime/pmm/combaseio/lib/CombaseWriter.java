/*******************************************************************************
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Joergen Brandt (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Christian Thoens (BfR)
 * Annemarie Kaesbohrer (BfR)
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
package de.bund.bfr.knime.pmm.combaseio.lib;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.PmmTimeSeries;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;

public class CombaseWriter {

	private LinkedList<PmmTimeSeries> buffer;
	private String filename;
	
	public CombaseWriter( final String filename) {
		this.filename = filename;
		buffer = new LinkedList<PmmTimeSeries>();
	}
	
	public void add( final PmmTimeSeries candidate ) throws PmmException {
		
		if( candidate == null )
			throw new PmmException( "Candidate must not be null." );
		
		buffer.add( candidate );
	}
		
	public void flush() throws UnsupportedEncodingException, FileNotFoundException, IOException, PmmException {
		flush16le(); // "UTF-16LE"
	}

	public void flush16le()
	throws UnsupportedEncodingException, FileNotFoundException, IOException, PmmException {
		StringBuffer buf = new StringBuffer();
		for( PmmTimeSeries candidate : buffer ) {
			
			if( candidate.hasCombaseId() ) {
				buf.append( "\"RecordID\"\t\""+candidate.getCombaseId()+"\"\n" );
			}
			
			if( candidate.hasAgent() ) {
				buf.append( "\"Organism\"\t\""+candidate.getAgentDetail()+"\"\n" );
			}
			
			if( candidate.hasMatrix() ) {
				buf.append( "\"Environment\"\t\""+candidate.getMatrixDetail()+"\"\n" );
			}
			
			if( candidate.hasTemperature() ) {
				buf.append( "\"Temperature\"\t\""+candidate.getTemperature()+" �C\"\n" );
			}
			
			if( candidate.hasPh() ) {
				buf.append( "\"pH\"\t\""+candidate.getPh()+"\"\n" );
			}
			
			if( candidate.hasWaterActivity() ) {
				buf.append( "\"Water Activity\"\t\""+candidate.getWaterActivity()+"\"\n" );
			}
			
			if( candidate.hasMisc() ) {
				PmmXmlDoc doc = candidate.getMisc();
				if (doc != null) {
					//String xmlStr = doc.toXmlString();
					String cb = xml2Combase(doc);
					if (cb != null) buf.append("\"Conditions\"\t\""+cb+"\"\n");
				}
			}
			
			/* if( candidate.hasMaximumRate() ) {
				buf.append( "\"Maximum Rate\"\t\""+candidate.getMaximumRate()+"\"\n" );
			}
			
			if( candidate.hasDoublingTime() ) {
				buf.append( "\"Doubling Time (h)\"\t\""+candidate.getDoublingTime()+"\"\n" );
			} */
			
			buf.append( "\"Time (h)\"\t\"logc\"\n" );
			
			if( !candidate.isEmpty() ) {
				PmmXmlDoc tsXmlDoc = candidate.getTimeSeries();
            	for (PmmXmlElementConvertable el : tsXmlDoc.getElementSet()) {
            		if (el instanceof TimeSeriesXml) {
            			TimeSeriesXml tsx = (TimeSeriesXml) el;
            			buf.append("\"" + tsx.getTime() + "\"\t\"" + tsx.getConcentration() + "\"\n" );
            		}
            	}
			}
			
			buf.append( "\n\n\n" );
		}
		OutputStream out = new FileOutputStream(filename);
		out.write(encodeString(buf.toString()));
		out.close();
	}
	public static byte[] encodeString(final String message) {

	    byte[] tmp = null;
	    try {
	        tmp = message.getBytes("UTF-16LE");
	    } catch(UnsupportedEncodingException e) {
	        // should not possible
	        AssertionError ae =
	        new AssertionError("Could not encode UTF-16LE");
	        ae.initCause(e);
	        throw ae;
	    }

	    // use brute force method to add BOM
	    byte[] utf16lemessage = new byte[2 + tmp.length];
	    utf16lemessage[0] = (byte)0xFF;
	    utf16lemessage[1] = (byte)0xFE;
	    System.arraycopy(tmp, 0,
	                     utf16lemessage, 2,
	                     tmp.length);
	    return utf16lemessage;
	}	
	private String xml2Combase(PmmXmlDoc misc) {
		String result = null;
		if (misc != null) {
			result = "";
        	for (PmmXmlElementConvertable el : misc.getElementSet()) {      		
        		if (el instanceof MiscXml) {        		
        			MiscXml mx = (MiscXml) el;
        			if (!result.isEmpty()) result += ", ";
        			result += mx.getDescription();
        			if (mx.getUnit() != null && !mx.getUnit().isEmpty()) result += " (" + mx.getUnit() + ")";
        			if (mx.getValue() != null && !Double.isNaN(mx.getValue())) result += ":" + mx.getValue();
        		}
        	}
		}
		return result;
	}
}
