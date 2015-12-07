/*******************************************************************************
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012-2014, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * Christian Th�ns (BfR)
 * Matthias Filter (BfR)
 * Armin A. Weiser (BfR)
 * Alexander Falenski (BfR)
 * J�rgen Brandt (BfR)
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
package de.bund.bfr.knime.pmm.manualmodelconf;

import javax.swing.JOptionPane;

import org.hsh.bfr.db.DBKernel;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.pmm.manualmodelconf.ui.MMC_M;
import de.bund.bfr.knime.pmm.manualmodelconf.ui.MMC_TS;

/**
 * <code>NodeDialog</code> for the "ManualModelConf" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author ManualModelConf
 */
public class ManualModelConfNodeDialog extends NodeDialogPane {
	
	private MMC_M m_mmcm;
	private MMC_TS m_mmcts;

	/**
     * New pane for configuring the ManualModelConf node.
     */
    protected ManualModelConfNodeDialog(boolean formulaCreator) {
    	try {    
    		m_mmcts = new MMC_TS();
    		m_mmcm = new MMC_M(JOptionPane.getRootFrame(), 1, "", formulaCreator, m_mmcts);
    		m_mmcm.setConnection(DBKernel.getLocalConn(true));
    		this.addTab("Model Definition", m_mmcm);    	
    		
    		if (!formulaCreator) {
        		this.addTab("Microbial Data", m_mmcts);        			
    		}
    		
    	}
    	catch( Exception e ) {
    		e.printStackTrace( System.err );
    	}
    }

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {	
		//m_confui.stopCellEditing();
		m_mmcm.stopCellEditing();
		//settings.addString( ManualModelConfNodeModel.PARAM_XMLSTRING, m_confui.toXmlString() );
		settings.addString( ManualModelConfNodeModel.PARAM_XMLSTRING, m_mmcm.listToXmlString() );
		String tStr = m_mmcm.tssToXmlString();
		settings.addString( ManualModelConfNodeModel.PARAM_TSXMLSTRING, tStr );//-1673022417
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		String mStr = null;
		String tsStr = null;
		// MMC_M
		try {
			if (settings.containsKey(ManualModelConfNodeModel.PARAM_XMLSTRING)) {
				mStr = settings.getString(ManualModelConfNodeModel.PARAM_XMLSTRING);
			}
		}
		catch( InvalidSettingsException e ) {
			e.printStackTrace();
		}

		//MMC_TS
		try {
			if (settings.containsKey(ManualModelConfNodeModel.PARAM_TSXMLSTRING)) {
				tsStr = settings.getString(ManualModelConfNodeModel.PARAM_TSXMLSTRING);
			}
		}
		catch (Exception e) {} // e.printStackTrace();
		
		if (tsStr != null && !tsStr.isEmpty()) m_mmcts.setTS(tsStr);
		if (mStr != null) m_mmcm.setFromXmlString(mStr);		
	}
}

