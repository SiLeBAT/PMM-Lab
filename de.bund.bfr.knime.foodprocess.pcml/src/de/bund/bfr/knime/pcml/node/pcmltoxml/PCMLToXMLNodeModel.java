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
package de.bund.bfr.knime.pcml.node.pcmltoxml;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.pcml.port.PCMLPortObject;
import de.bund.bfr.pcml10.PCMLDocument;

/**
 * This is the model implementation of "PCML to XML" node.
 * 
 * @author Heiko Hofer
 */
public class PCMLToXMLNodeModel extends NodeModel {
	private final PCMLToXMLNodeSettings settings;
	
    /**
     * Constructor for the node model.
     */
    protected PCMLToXMLNodeModel() {    	
        super(new PortType[] {
        	new PortType(PCMLPortObject.class, false),
        }, new PortType[] {
        	BufferedDataTable.TYPE,
        });
        settings = new PCMLToXMLNodeSettings();
    }

    /**
     * {@inheritDoc}
     */      
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
    		throws InvalidSettingsException {
    	return new PortObjectSpec[]{createOutSpec()};
    }
    
    /**
     * {@inheritDoc}
     */    
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec)
    		throws Exception {
    	PCMLPortObject pcmlPortObject = (PCMLPortObject)inObjects[0];
    	PCMLDocument pcmlDoc = pcmlPortObject.getPcmlDoc();

    	DataCell xmlCell = XMLCellFactory.create(pcmlDoc.toString());
    	BufferedDataContainer cont = exec.createDataContainer(createOutSpec());
    	DataRow row = new DefaultRow(RowKey.createRowKey(0), xmlCell);
    	cont.addRowToTable(row);
    	cont.close();
    	
    	BufferedDataTable outTable = cont.getTable();    	
    	return new PortObject[]{outTable};
    }

	// Creates the spec of the output
    private DataTableSpec createOutSpec() {
    	DataColumnSpecCreator creator = new DataColumnSpecCreator("PCML", 
    			XMLCell.TYPE);
    	DataColumnSpec colSpec = creator.createSpec();
    	
    	DataTableSpec outSpec = new DataTableSpec(colSpec);
    	return outSpec;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    	// no internals, nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         this.settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        this.settings.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	PCMLToXMLNodeSettings s = new PCMLToXMLNodeSettings();
        s.loadSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    	// no internals, nothing to load
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    	// no internals, nothing to save
    }

}

