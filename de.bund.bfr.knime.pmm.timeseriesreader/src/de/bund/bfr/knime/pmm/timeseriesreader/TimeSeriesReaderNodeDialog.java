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
package de.bund.bfr.knime.pmm.timeseriesreader;

import java.util.LinkedHashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.hsh.bfr.db.DBKernel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.config.Config;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;

import de.bund.bfr.knime.pmm.common.ui.DbConfigurationUi;
import de.bund.bfr.knime.pmm.common.ui.DoubleTextField;
import de.bund.bfr.knime.pmm.common.ui.MdReaderUi;

/**
 * <code>NodeDialog</code> for the "TimeSeriesReader" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Jorgen Brandt
 */
public class TimeSeriesReaderNodeDialog extends NodeDialogPane {
	
	private DbConfigurationUi dbui;
	private MdReaderUi tsui;

    /**
     * New pane for configuring the TimeSeriesReader node.
     */
    protected TimeSeriesReaderNodeDialog() {    	
    	JPanel panel;
    	
    	panel = new JPanel();
    	panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );    	
    	
    	dbui = new DbConfigurationUi();
    	panel.add( dbui );
    	
    	tsui = new MdReaderUi(DBKernel.getItemListMisc());
    	panel.add( tsui );
    	
    	addTab("Database connection", panel);
    }
        
	@Override
	protected void saveSettingsTo( final NodeSettingsWO settings )
			throws InvalidSettingsException {		
		settings.addString( TimeSeriesReaderNodeModel.PARAM_FILENAME, dbui.getFilename() );
		settings.addString( TimeSeriesReaderNodeModel.PARAM_LOGIN, dbui.getLogin() );
		settings.addString( TimeSeriesReaderNodeModel.PARAM_PASSWD, dbui.getPasswd() );
		settings.addBoolean( TimeSeriesReaderNodeModel.PARAM_OVERRIDE, dbui.isOverride() );
		settings.addString( TimeSeriesReaderNodeModel.PARAM_MATRIXSTRING, tsui.getMatrixString() );
		settings.addString( TimeSeriesReaderNodeModel.PARAM_AGENTSTRING, tsui.getAgentString() );
		settings.addString( TimeSeriesReaderNodeModel.PARAM_LITERATURESTRING, tsui.getLiteratureString() );
		
		LinkedHashMap<String, DoubleTextField[]> params = tsui.getParameter();
		Config c = settings.addConfig(TimeSeriesReaderNodeModel.PARAM_PARAMETERS);
		String[] pars = new String[params.size()];
		String[] mins = new String[params.size()];
		String[] maxs = new String[params.size()];
		int i=0;
		for (String par : params.keySet()) {
			DoubleTextField[] dbl = params.get(par);
			pars[i] = par;
			mins[i] = ""+dbl[0].getValue();
			maxs[i] = ""+dbl[1].getValue();
			i++;
		}
		c.addStringArray(TimeSeriesReaderNodeModel.PARAM_PARAMETERNAME, pars);
		c.addStringArray(TimeSeriesReaderNodeModel.PARAM_PARAMETERMIN, mins);
		c.addStringArray(TimeSeriesReaderNodeModel.PARAM_PARAMETERMAX, maxs);
	}

	@Override
	protected void loadSettingsFrom( final NodeSettingsRO settings, final PortObjectSpec[] specs )  {
		
		try {			
			dbui.setFilename( settings.getString( TimeSeriesReaderNodeModel.PARAM_FILENAME ) );
			dbui.setLogin( settings.getString( TimeSeriesReaderNodeModel.PARAM_LOGIN ) );
			dbui.setPasswd( settings.getString( TimeSeriesReaderNodeModel.PARAM_PASSWD ) );
			dbui.setOverride( settings.getBoolean( TimeSeriesReaderNodeModel.PARAM_OVERRIDE ) );
			tsui.setMatrixString( settings.getString( TimeSeriesReaderNodeModel.PARAM_MATRIXSTRING ) );
			tsui.setAgentString( settings.getString( TimeSeriesReaderNodeModel.PARAM_AGENTSTRING ) );
			tsui.setLiteratureString(settings.getString( TimeSeriesReaderNodeModel.PARAM_LITERATURESTRING ) );
			
			Config c = settings.getConfig(TimeSeriesReaderNodeModel.PARAM_PARAMETERS);
			String[] pars = c.getStringArray(TimeSeriesReaderNodeModel.PARAM_PARAMETERNAME);
			String[] mins = c.getStringArray(TimeSeriesReaderNodeModel.PARAM_PARAMETERMIN);
			String[] maxs = c.getStringArray(TimeSeriesReaderNodeModel.PARAM_PARAMETERMAX);

			LinkedHashMap<String, DoubleTextField[]> params = new LinkedHashMap<String, DoubleTextField[]>();
			for (int i=0;i<pars.length;i++) {
				DoubleTextField[] dbl = new DoubleTextField[2];
				dbl[0] = new DoubleTextField(true);
				dbl[1] = new DoubleTextField(true);
				if (!mins[i].equals("null")) dbl[0].setValue(Double.parseDouble(mins[i]));
				if (!maxs[i].equals("null")) dbl[1].setValue(Double.parseDouble(maxs[i]));
				params.put(pars[i], dbl);
			}
			tsui.setParameter(params);
		}
		catch( InvalidSettingsException ex ) {
			
			ex.printStackTrace( System.err );
		}
		
	}


}