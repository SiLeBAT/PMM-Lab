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
package de.bund.bfr.knime.pmm.common.generictablemodel;

import de.bund.bfr.knime.pmm.common.PmmException;

public class KnimeAttribute {
	
	public static final int TYPE_STRING = 0;
	public static final int TYPE_INT = 1;
	public static final int TYPE_DOUBLE = 2;
	public static final int TYPE_COMMASEP_INT = 3;
	public static final int TYPE_COMMASEP_DOUBLE = 4;
	public static final int TYPE_COMMASEP_STRING = 5;
	public static final int TYPE_MAP = 6;
	public static final int TYPE_XML = 7;

	private String name;
	private int type;
	
	
	protected KnimeAttribute( final String name, final int type ) throws PmmException {
		setName( name );
		setType( type );
	}
	
	public String getName() { return name; }
	public int getType() { return type; }
	
	public boolean isDouble() { return type == TYPE_DOUBLE; }
	public boolean isInt() { return type == TYPE_INT; }
	
	public void setName( final String name ) throws PmmException {
		
		if( name == null )
			throw new PmmException( "Column name must not be null." );
		
		if( name.isEmpty() )
			throw new PmmException( "Column name must not be empty." );

		this.name = name;
	}
	
	public void setType( final int type ) throws PmmException {
		
		if( type < 0 || type > 7 )
			throw new PmmException( "Unknown column type" );
		
		this.type = type;
	}
	
}
