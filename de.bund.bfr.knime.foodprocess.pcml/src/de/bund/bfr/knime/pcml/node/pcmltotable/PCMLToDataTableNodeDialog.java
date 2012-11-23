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
package de.bund.bfr.knime.pcml.node.pcmltotable;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

/**
 * <code>NodeDialog</code> for the "PCML to Data Table" Node.
 * 
 * 
 * @author Heiko Hofer
 */
public class PCMLToDataTableNodeDialog extends NodeDialogPane {
	private final PCMLToDataTableNodeSettings settings;
	

    /**
     * New pane for configuring the FoodProcess node.
     */
    protected PCMLToDataTableNodeDialog() {
    	this.settings = new PCMLToDataTableNodeSettings();
		addTab("Settings", createPanel());
		
    }
    
    /** Create the configuration panel. */
	private Component createPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.BASELINE;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        /*
        Insets leftInsets = new Insets(3, 8, 3, 8);
        Insets rightInsets = new Insets(3, 0, 3, 8);
        Insets rightCategoryInsets = new Insets(11, 0, 3, 8);
        */
        Insets leftCategoryInsets = new Insets(11, 8, 3, 8);

        c.gridx = 0;
        c.insets = leftCategoryInsets;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        
        
        // create controls here if there are any
        
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        p.add(new JPanel(), c);
    	
		return p;
	}

	@Override
	public void saveSettingsTo( final NodeSettingsWO s ) {		
    	settings.saveSettings(s);
    }
	
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO s,
    		final PortObjectSpec[] specs) throws NotConfigurableException {
    	settings.loadSettingsForDialog(s);
    	//DataTableSpec spec = (DataTableSpec)specs[0];
    }

}

