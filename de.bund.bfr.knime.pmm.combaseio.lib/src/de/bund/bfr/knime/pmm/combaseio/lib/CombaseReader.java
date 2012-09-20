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
package de.bund.bfr.knime.pmm.combaseio.lib;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;

import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.PmmTimeSeries;

public class CombaseReader implements Enumeration<PmmTimeSeries> {
	
	private BufferedReader reader;
	private PmmTimeSeries next;
	
	public CombaseReader( final String filename )
	throws FileNotFoundException, IOException, Exception {
		
		reader = new BufferedReader( new InputStreamReader(
			new FileInputStream( filename ), "UTF-16LE" ) );
		step();
	}
	
	public void close() throws IOException {
		reader.close();
	}
	
	public PmmTimeSeries nextElement() {

		PmmTimeSeries ret;
		
		ret = next;
		
		try {
			step();
		}
		catch( Exception e ) {
			e.printStackTrace( System.err );
		}
		
		return ret;
	}
	
	public boolean hasMoreElements() {
		return next != null;
	}
	
	private void step() throws IOException, Exception {
		
		String line;
		String[] token;
		int i, pos;
		double t, logc;
				
		// initialize next time series
		next = new PmmTimeSeries();
		
		while( true ) {
			
			line = reader.readLine();

			if( line == null ) {
				next = null;
				return;
			}
			
			// split up token
			token = line.split( "\t" );
						
			if( token.length < 2 )
				continue;
			
			if( token[ 0 ].isEmpty() )
				continue;
			
			for( i = 0; i < token.length; i++ )
				token[ i ] = token[ i ].replaceAll( "[^a-zA-Z0-9� \\.\\(\\)_/\\+\\-\\*,:]", "" );
			token[ 0 ] = token[ 0 ].toLowerCase();
			
			// fetch record id
			if( token[ 0 ].equals( "recordid" ) ) {
				next.setCombaseId( token[ 1 ] );
				continue;
			}
			
			// fetch organism
			if( token[ 0 ].equals( "organism" ) ) {
				next.setAgentDetail( token[ 1 ] );
				continue;
			}
			
			// fetch environment
			if( token[ 0 ].equals( "environment" ) ) {
				next.setMatrixDetail( token[ 1 ] );
				continue;
			}
			
			// fetch temperature
			if( token[ 0 ].equals( "temperature" ) ) {
				
				pos = token[ 1 ].indexOf( " " );
				if( !token[ 1 ].endsWith( " �C" ) )
					throw new PmmException( "Temperature unit must be [�C]" );
				next.setTemperature( parse( token[ 1 ].substring( 0, pos ) ) );
				continue;
			}
			
			// fetch pH
			if( token[ 0 ].equals( "ph" ) ) {
				next.setPh( parse( token[ 1 ] ) );
				continue;
			}
			
			// fetch water activity
			if( token[ 0 ].equals( "water activity" ) ) {
				next.setWaterActivity( parse( token[ 1 ] ) );
				continue;
			}
			
			// fetch conditions
			if( token[ 0 ].equals( "conditions" ) ) {
				next.setCommasepMisc( token[ 1 ] );
				continue;
			}
			
			// fetch maximum rate
			/* if( token[ 0 ].equals( "maximum rate" ) ) {

				next.setMaximumRate( parse( token[ 1 ] ) );
				continue;
			}
			
			if( token[ 0 ].startsWith( "doubling time" ) ) {
				
				next.setDoublingTime( parse( token[ 1 ] ) );
				continue;
			} */
			
			if( token[ 0 ].startsWith( "time" ) && token[ 1 ].equals( "logc" ) ) {
				
				if( !token[ 0 ].endsWith( " (h)" ) )
					throw new Exception( "Time unit must be [h]." );
				
				while( true ) {
					
					line = reader.readLine();
					
					if( line == null )
						return;
					
					if( line.replaceAll( "\\t\"", "" ).isEmpty() )
						break;
					
					token = line.split( "\t" );
					
					for( i = 0; i < token.length; i++ )
						token[ i ] = token[ i ].replaceAll( "[^a-zA-Z0-9� \\.\\(\\)/,]", "" );
					
					if( token.length < 2 )
						break;

					t = parse( token[ 0 ] );
					logc = parse( token[ 1 ] );
					
					if( Double.isNaN( t ) || Double.isNaN( logc ) )
						continue;
					
					next.add( t, logc );

					
				}
				break;
			}
		}
	}
	
	private static double parse( String num ) {
		
		double n;
		
		n = Double.NaN;
		
		num = num.toLowerCase();
		num = num.trim();
		if( num.equals( "no growth" ) )
			return 0;
		
		
		try {
			
			num = num.replaceAll( "[a-zA-Z\\(\\)\\s]", "" );
			num = num.replaceAll( ",", "." );
			n = Double.valueOf( num );
			
		}
		catch( Exception e ) {}
		
		return n;
	}

}
