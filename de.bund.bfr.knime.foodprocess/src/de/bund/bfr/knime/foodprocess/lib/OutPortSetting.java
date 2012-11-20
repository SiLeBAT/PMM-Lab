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
package de.bund.bfr.knime.foodprocess.lib;

import lombok.Data;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.Config;

@Data
public class OutPortSetting {
	
	private final String PARAM_PARAMETERS = "parameters";
	private final String PARAM_MATRIX = "matrix";
	private final String PARAM_OUTFLUX = "outFlux";
	private final String PARAM_FROMINPORT = "fromInPort";
	
	private String matrix;
	private Double outFlux;
		
	private Double[] fromInPort;
	
	private ParametersSetting parametersSetting;
	
	public OutPortSetting( final int n_inports ) {
		fromInPort = new Double[ n_inports ];
		parametersSetting = new ParametersSetting();
	}
	
	@Override
	public String toString() {
		
		String ret;
		int i;
		
		ret = "out flux : "+outFlux+" %\n";
		ret += "new matrix definition : "+matrix+"\n";
		for( i = 0; i < fromInPort.length; i++ ) {
			ret += "from in port "+i+" : "+fromInPort[ i ]+" %\n";
		}
		/*
		ret += "volume : "+volume+" "+"\n";
		ret += "volume_func : "+volume_func+" "+"\n";
		ret += "temperature : "+temperature+"\n";
		ret += "temperature_func : "+temperature_func+" "+"\n";
		ret += "pH : "+ph+"\n";
		ret += "ph_func : "+ph_func+" "+"\n";
		ret += "aw : "+aw+"\n";
		ret += "aw_func : "+aw_func+" "+"\n";
		ret += "pressure : "+pressure+"\n";
		ret += "pressure_func : "+pressure_func+" "+"\n";
		*/
		return ret;
	}
	
	public void saveSettings( final Config config ) {		
		int i;
		
		Config c = config.addConfig(PARAM_PARAMETERS);
		parametersSetting.saveSettings( c );
		config.addString( PARAM_MATRIX, matrix );
		if (outFlux != null) {
			config.addDouble( PARAM_OUTFLUX, outFlux );			
		}

		for( i = 0; i < fromInPort.length; i++ ) {
			if (fromInPort[ i ] != null) {
				config.addDouble( PARAM_FROMINPORT+"_"+i, fromInPort[ i ] );
			}
		}
		
	}
	
	public void loadSettings( final Config config ) throws InvalidSettingsException {		
		int i;
		Config c;
		
		matrix = config.getString( PARAM_MATRIX );
		outFlux = config.containsKey(PARAM_OUTFLUX) ? config.getDouble( PARAM_OUTFLUX ) : null;

		for( i = 0; i < fromInPort.length; i++ ) {
			fromInPort[ i ] = config.containsKey(PARAM_FROMINPORT+"_"+i) ? config.getDouble( PARAM_FROMINPORT+"_"+i ) : null;
		}
		
		parametersSetting = new ParametersSetting();
		c = config.getConfig(PARAM_PARAMETERS);
		parametersSetting.loadSettings( c );
	}
	
	public void loadSettingsForDialog( final Config config ) {		
		try {
			loadSettings( config );
		}
		catch( InvalidSettingsException e ) {
			e.printStackTrace( System.err );
			assert false;
		}		
	}
}
