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
package de.bund.bfr.knime.pmm.combaseio;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmm.combaseio.lib.CombaseReader;

import de.bund.bfr.knime.pmm.common.PmmTimeSeries;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * This is the model implementation of CombaseReader.
 * 
 *
 * @author Jorgen Brandt
 */
public class CombaseReaderNodeModel extends NodeModel {
		
		
	protected static final String PARAM_FILENAME = "filename";
	protected static final String PARAM_SKIPEMPTY = "skipEmpty";

	private String filename;
	private boolean skipEmpty;
    
    /**
     * Constructor for the node model.
     */
    protected CombaseReaderNodeModel() {
    	
        // super( 0, 2 );
    	super( 0, 1 );
        
        filename = "";
        skipEmpty = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	BufferedDataContainer buf, buf2;
    	CombaseReader reader;
    	PmmTimeSeries candidate;
    	DataCell[] row;
    	int j;
    	PmmXmlDoc doc = new PmmXmlDoc();

    	// initialize combase reader
    	reader = new CombaseReader( filename );
    	
    	// initialize table buffer
    	// buf = exec.createDataContainer( PmmTimeSeriesSchema.createSpec( new File( filename ).getName() ) );
    	buf = exec.createDataContainer( new TimeSeriesSchema().createSpec() );
    	
    	j = 0;
    	while( reader.hasMoreElements() ) {
    		
    		// fetch time series
    		candidate = reader.nextElement();
    		
    		if( skipEmpty && candidate.isEmpty() )
    			continue;
    		
    		doc.add( candidate );
			buf.addRowToTable( new DefaultRow( String.valueOf( j++ ), candidate ) );
    	
    	}
    	reader.close();
    	buf.close();
    	
    	buf2 = exec.createDataContainer( createXmlSpec() );
    	row = new StringCell[ 1 ];
    	row[ 0 ] = new StringCell( doc.toXmlString() );
    	
    	buf2.addRowToTable( new DefaultRow( "0", row ) );
    	buf2.close();
    	
        // return new BufferedDataTable[]{ buf.getTable(), buf2.getTable() };
    	return new BufferedDataTable[]{ buf.getTable() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure( final DataTableSpec[] inSpecs )
            throws InvalidSettingsException {

    	if( filename.isEmpty() )
    		throw new InvalidSettingsException( "Filename must be specified." );
    	
    	
        // return new DataTableSpec[] { PmmTimeSeriesSchema.createSpec( new File( filename ).getName() ), createXmlSpec() };
    	return new DataTableSpec[] { new TimeSeriesSchema().createSpec() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo( final NodeSettingsWO settings ) {

    	settings.addString( PARAM_FILENAME, filename );
    	// settings.addBoolean( PARAM_SKIPEMPTY, skipEmpty );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {

    	filename = settings.getString( PARAM_FILENAME );
    	// skipEmpty = settings.getBoolean( PARAM_SKIPEMPTY );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {}
        
    protected static DataTableSpec createXmlSpec() {
    	
    	DataColumnSpec[] spec;
    	
    	spec = new DataColumnSpec[ 1 ];
    	// spec[ 0 ] = new DataColumnSpecCreator( "xmlString", XMLCell.TYPE ).createSpec();
    	spec[ 0 ] = new DataColumnSpecCreator( "xmlString", StringCell.TYPE ).createSpec();
    	
    	return new DataTableSpec( spec );
    }

}

