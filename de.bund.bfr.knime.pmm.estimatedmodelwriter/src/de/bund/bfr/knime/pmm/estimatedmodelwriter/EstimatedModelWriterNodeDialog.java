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
package de.bund.bfr.knime.pmm.estimatedmodelwriter;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;

import de.bund.bfr.knime.pmm.common.ui.DbConfigurationUi;

/**
 * <code>NodeDialog</code> for the "EstimatedModelWriter" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Jorgen Brandt
 */
public class EstimatedModelWriterNodeDialog extends NodeDialogPane {

	private DbConfigurationUi dbui;

    /**
     * New pane for configuring the EstimatedModelWriter node.
     */
    protected EstimatedModelWriterNodeDialog() {
    	
    	dbui = new DbConfigurationUi();    	
    	//addTab("Database connection", dbui);
    }
    
	@Override
	protected void saveSettingsTo( NodeSettingsWO settings )
			throws InvalidSettingsException {
		
		settings.addString( EstimatedModelWriterNodeModel.PARAM_FILENAME, dbui.getFilename() );
		settings.addString( EstimatedModelWriterNodeModel.PARAM_LOGIN, dbui.getLogin() );
		settings.addString( EstimatedModelWriterNodeModel.PARAM_PASSWD, dbui.getPasswd() );
		settings.addBoolean( EstimatedModelWriterNodeModel.PARAM_OVERRIDE, dbui.isOverride() );
	}

	protected void loadSettingsFrom( NodeSettingsRO settings, PortObjectSpec[] specs )  {
		
		try {
			
			dbui.setFilename( settings.getString( EstimatedModelWriterNodeModel.PARAM_FILENAME ) );
			dbui.setLogin( settings.getString( EstimatedModelWriterNodeModel.PARAM_LOGIN ) );
			dbui.setPasswd( settings.getString( EstimatedModelWriterNodeModel.PARAM_PASSWD ) );
			dbui.setOverride( settings.getBoolean( EstimatedModelWriterNodeModel.PARAM_OVERRIDE ) );
		}
		catch( InvalidSettingsException ex ) {
			
			ex.printStackTrace( System.err );
		}
		
	}


}

