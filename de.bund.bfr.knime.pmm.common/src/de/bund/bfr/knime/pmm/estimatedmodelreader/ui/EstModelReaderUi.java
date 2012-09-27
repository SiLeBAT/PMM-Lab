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
package de.bund.bfr.knime.pmm.estimatedmodelreader.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.ui.DoubleTextField;

public class EstModelReaderUi extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 20120828;
	private JRadioButton qualityButtonNone;
	private JRadioButton qualityButtonRms;
	private JRadioButton qualityButtonR2;
	private DoubleTextField qualityField;
	private TsReaderUi tsReaderUi;
	private ModelReaderUi modelReaderUi;
	
	public static final int MODE_OFF = 0;
	public static final int MODE_R2 = 1;
	public static final int MODE_RMS = 2;
	

	
	public EstModelReaderUi() {
		
		JPanel panel, panel0;
		ButtonGroup group;
		
		setPreferredSize( new Dimension( 300, 500 ) );

		
		modelReaderUi = new ModelReaderUi();
		modelReaderUi.addLevelListener( this );
		add( modelReaderUi );
		
		panel = new JPanel();
		panel.setBorder( BorderFactory.createTitledBorder( "Estimation quality" ) );
		panel.setLayout( new BorderLayout() );
		panel.setPreferredSize( new Dimension( 300, 125 ) );
		add( panel );
		
		group = new ButtonGroup();
		
		panel0 = new JPanel();
		panel0.setLayout( new GridLayout( 0, 1 ) );
		panel.add( panel0, BorderLayout.NORTH );
		
		
		qualityButtonNone = new JRadioButton( "Do not filter" );
		qualityButtonNone.setSelected( true );
		qualityButtonNone.addActionListener( this );
		group.add( qualityButtonNone );
		panel0.add( qualityButtonNone );
		
		qualityButtonRms = new JRadioButton( "Filter by RMS" );
		qualityButtonRms.addActionListener( this );
		group.add( qualityButtonRms );
		panel0.add( qualityButtonRms );
		
		qualityButtonR2 = new JRadioButton( "Filter by R squared" );
		qualityButtonR2.addActionListener( this );
		group.add( qualityButtonR2 );
		panel0.add( qualityButtonR2 );
		
		panel.add( new JLabel( "Quality threshold   " ), BorderLayout.WEST );
		
		qualityField = new DoubleTextField( false );
		qualityField.setText( "0.8" );
		qualityField.setEnabled( false );
		panel.add( qualityField, BorderLayout.CENTER );
		
		tsReaderUi = new TsReaderUi();
		add( tsReaderUi );
		
		updateTsReaderUi();
	}
	
	@Override
	public void actionPerformed( ActionEvent arg0 ) {

		if( arg0.getSource() instanceof JRadioButton ) {
			
			if( qualityButtonNone.isSelected() )
				qualityField.setEnabled( false );
			else
				qualityField.setEnabled( true );
			
			return;
		}
		
		if( arg0.getSource() instanceof JComboBox ) {
			
			updateTsReaderUi();
			
			return;
		}
		
	}
	
	public double getQualityThresh() throws InvalidSettingsException {
		
		if( !qualityField.isValueValid() )
			throw new InvalidSettingsException( "Threshold quality invalid." );
		
		return qualityField.getValue();
	}
	
	public int getQualityMode() {
		
		if( qualityButtonNone.isSelected() )
			return MODE_OFF;
		
		if( qualityButtonRms.isSelected() )
			return MODE_RMS;
		
		return MODE_R2;
	}
	
	public void setQualityMode( final int mode ) throws PmmException {
		
		
		switch( mode ) {
		
			case MODE_OFF :
				qualityButtonNone.setSelected( true );
				qualityField.setEnabled( false );
				break;
				
			case MODE_R2 :
				qualityButtonR2.setSelected( true );
				qualityField.setEnabled( true );
				break;
				
			case MODE_RMS :
				qualityButtonRms.setSelected( true );
				qualityField.setEnabled( true );
				break;
		
			default :
				throw new PmmException( "Invalid quality filter mode." );
		}
	}
	
	public void setQualityThresh( final double thresh ) {
		qualityField.setText( String.valueOf( thresh ) );
	}
	
    public static boolean passesFilter(
    		final int level,
    		final int qualityMode,
    		final double qualityThresh,
    		final boolean matrixEnabled,
    		final String matrixString,
    		final boolean agentEnabled,
    		final String agentString,
    		final KnimeTuple tuple )
    throws PmmException {
    	
    	double thresh;
    	
    	if( level == 0 )
    		if( !TsReaderUi.passesFilter( matrixEnabled, matrixString,
				agentEnabled, agentString, tuple ) )
    			return false;
    	
    	
    		
    		
        	
    	switch( qualityMode ) {
    	
    		case MODE_OFF :
    			return true;
    			
    		case MODE_RMS :
    			
    			if( level == 1 )
    				thresh = tuple.getDouble( Model1Schema.ATT_RMS );
				else
					thresh = tuple.getDouble( Model2Schema.ATT_RMS );
    			
    			if( thresh <= qualityThresh )
    				return true;
    			
    			return false;
    			
    		case MODE_R2 :
    			
    			if( level == 1 )
    				thresh = tuple.getDouble( Model1Schema.ATT_RSQUARED );
				else
					thresh = tuple.getDouble( Model2Schema.ATT_RSQUARED );
    			
    			if( thresh >= qualityThresh )
    				return true;
    			
    			return false;
    			
    		default :
    			throw new PmmException( "Unrecognized Quality Filter mode." );
    	}
    }
    
    private void updateTsReaderUi() {
    	
    	if( modelReaderUi.getLevel() == 1 )
    		tsReaderUi.setActive();
    	else
    		tsReaderUi.setInactive();
    }
}
