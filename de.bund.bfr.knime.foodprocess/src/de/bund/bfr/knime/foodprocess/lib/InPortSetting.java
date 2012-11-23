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
public class InPortSetting {
	
	private final String PARAM_PARAMETERS = "parameters";
	
	private ParametersSetting parametersSetting;
	
	public InPortSetting() {
		parametersSetting = new ParametersSetting();
	}
	
	public void saveSettings( Config config ) {		
		Config c = config.addConfig(PARAM_PARAMETERS);
		assert c != null;		
		assert parametersSetting != null;		
		parametersSetting.saveSettings( c );
	}
	
	public void loadSettings( Config config ) throws InvalidSettingsException {		
		Config c;
		
		parametersSetting = new ParametersSetting();
		c = config.getConfig(PARAM_PARAMETERS);
		parametersSetting.loadSettings( c );
	}
	
	public void loadSettingsForDialog( Config config ) {		
		try {
			loadSettings( config );
		}
		catch( InvalidSettingsException e ) {
			e.printStackTrace( System.err );
			assert false;
		}		
	}
}
