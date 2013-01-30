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
package de.bund.bfr.knime.pmm.common.pmmtablemodel;

import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;

public class TimeSeriesSchema extends KnimeSchema {
	
	public static final String TIME = "Time";
	public static final String LOGC = "Log10C";

	
	public static final String ATT_CONDID = "CondID";
	public static final String ATT_COMBASEID = "CombaseID";
	public static final String ATT_MISC = "Misc";

	public static final String ATT_AGENT = "Organism";
	public static final String ATT_MATRIX = "Matrix";

	public static final String ATT_TIMESERIES = "MD_Data";
	public static final String ATT_COMMENT = "Comment";

	public static final String ATT_LITMD = "MD_Literatur";
	public static final String ATT_DBUUID = "MD_DB_UID";

	public static final String DATAID = "DataID";
	public static final String DATAPOINTS = "Data Points";
	
	public TimeSeriesSchema() {

		try {
			addIntAttribute(ATT_CONDID);
			addStringAttribute(ATT_COMBASEID);
			addXmlAttribute(ATT_AGENT);
			addXmlAttribute(ATT_MATRIX);

			addXmlAttribute(ATT_TIMESERIES);

			addXmlAttribute(ATT_MISC);

			addStringAttribute(ATT_COMMENT);

			addXmlAttribute(ATT_LITMD);
			
			addStringAttribute(ATT_DBUUID);
		}
		catch( PmmException ex ) {
			ex.printStackTrace( System.err );
		}
	}
	
}
