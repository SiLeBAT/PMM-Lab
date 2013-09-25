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
package de.bund.bfr.knime.pmm.common.generictablemodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;

import de.bund.bfr.knime.pmm.common.PmmException;

public class DbRelationReader implements Enumeration<KnimeTuple> {
	
	private String query;
	private HashMap<String,String> mapKnimeDb;
	private Connection conn;
	private PreparedStatement stat;
	private ResultSet result;
	private KnimeSchema schema;
	private boolean hasMore;
	
	public DbRelationReader( KnimeSchema schema ) throws PmmException {
		
		if( schema == null )
			throw new PmmException( "Schema must not be null." );
		
		this.schema = schema;
		mapKnimeDb = new HashMap<String,String>();
		hasMore = false;
	}
	
	public void addSibling( String knime, String db ) throws PmmException {
		
		if( !schema.containsAtt( knime ) )
			throw new PmmException( "The attribute '"+knime+"' does not exist in schema." );
		
		mapKnimeDb.put( knime, db );
	}
	
	@Override
	public boolean hasMoreElements() {
		return hasMore;
	}
	
	public void init() throws SQLException, PmmException {
		
		// String att;
		
		if( result != null )
			result.close();
		
		/* att = missingAttribute();
		
		if( att != null )
			throw new PmmException( "Mapping incomplete. Attribute '"+att+"' has no sibling." ); */
		
		result = stat.executeQuery();
		hasMore = result.next();
	}
	
	public String missingAttribute() {
		
		int i;
		
		for( i = 0; i < schema.size(); i++ )
			if( !mapKnimeDb.containsKey( schema.getName( i ) ) )
				return schema.getName( i );
		
		return null;
	}

	@Override
	public KnimeTuple nextElement() {
		
		KnimeTuple tuple;
		String value;
		
		tuple = null;
		if( !hasMore )
			return null;
		
		try {
			
			tuple = new KnimeTuple( schema );
			
			for( String knimeAtt : mapKnimeDb.keySet() ) {
				
				value = result.getString( mapKnimeDb.get( knimeAtt ) );
				tuple.setValue( knimeAtt, value );
			}
			
			hasMore = result.next();	
		}
		catch( Exception e ) {
			e.printStackTrace( System.err );
		}

		
		return tuple;
	}
	
	public void setConn( final Connection conn ) throws SQLException {
		
		this.conn = conn;
		
		if( query != null )
			stat = conn.prepareStatement( query );
	}
	
	public void setQuery( final String query ) throws SQLException {
		
		this.query = query;
		
		if( !( conn == null || query == null ) )
			stat = conn.prepareStatement( query );
		else
			stat = null;
	}
	
	public void setStat( final PreparedStatement stat ) throws SQLException {
		
		this.stat = stat;
		query = null;
		
		if( stat == null )
			conn = null;
		else
			conn = stat.getConnection();
	}
	
	public void close() throws SQLException {
		
		result.close();
		stat.close();
		conn.close();
	}
	
}
