package de.bund.bfr.knime.pmm.xml2table;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdom2.Attribute;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;

/**
 * <code>NodeDialog</code> for the "XML2Table" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author BfR
 */
public class XML2TableNodeDialog extends DataAwareNodeDialogPane implements
		ItemListener {

	private SettingsHelper set;
	private BufferedDataTable table;

	private JComboBox<String> columnBox;
	private JList<String> elementList;

	protected XML2TableNodeDialog() {
		set = new SettingsHelper();

		columnBox = new JComboBox<String>();
		columnBox.addItemListener(this);
		elementList = new JList<String>();

		JPanel columnPanel = new JPanel();

		columnPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.X_AXIS));
		columnPanel.add(new JLabel("Column:"));
		columnPanel.add(Box.createHorizontalStrut(5));
		columnPanel.add(columnBox);

		JPanel elementPanel = new JPanel();

		elementPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		elementPanel.setLayout(new BoxLayout(elementPanel, BoxLayout.X_AXIS));
		elementPanel.add(new JLabel("Elements:"));
		elementPanel.add(Box.createHorizontalStrut(5));
		elementPanel.add(new JScrollPane(elementList));

		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(columnPanel);
		panel.add(elementPanel);

		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(panel, BorderLayout.NORTH);

		addTab("Options", northPanel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			BufferedDataTable[] input) throws NotConfigurableException {
		set.loadSettings(settings);
		table = input[0];
		columnBox.removeItemListener(this);
		columnBox.removeAllItems();

		List<String> columns = getXmlColumns(table.getSpec());

		for (String column : columns) {
			columnBox.addItem(column);
		}

		columnBox.addItemListener(this);
		columnBox.setSelectedItem(null);

		if (columns.contains(set.getColumn())) {
			columnBox.setSelectedItem(set.getColumn());
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		set.setColumn((String) columnBox.getSelectedItem());
		set.setXmlElements(elementList.getSelectedValuesList().toArray(
				new String[0]));
		set.saveSettings(settings);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == columnBox
				&& e.getStateChange() == ItemEvent.SELECTED) {
			String column = (String) columnBox.getSelectedItem();

			if (column != null) {
				List<String> elements = getElements(table, column);
				int[] indices = new int[set.getXmlElements().length];				

				for (int i = 0; i < set.getXmlElements().length; i++) {
					indices[i] = elements.indexOf(set.getXmlElements()[i]);					
				}
				
				elementList.setListData(elements.toArray(new String[0]));
				elementList.setSelectedIndices(indices);
			}
		}
	}

	private static List<String> getXmlColumns(DataTableSpec spec) {
		List<String> columns = new ArrayList<String>();

		for (DataColumnSpec column : spec) {
			if (column.getType().equals(XMLCell.TYPE)) {
				columns.add(column.getName());
			}
		}

		return columns;
	}

	private static List<String> getElements(BufferedDataTable table,
			String column) {
		int index = table.getSpec().findColumnIndex(column);
		Set<String> elements = new LinkedHashSet<String>();

		for (DataRow row : table) {
			PmmXmlDoc xml = XML2TableNodeModel.createXml(row.getCell(index));

			if (xml != null) {
				for (PmmXmlElementConvertable e : xml.getElementSet()) {
					for (Attribute attr : e.toXmlElement().getAttributes()) {
						elements.add(attr.getName());
					}
				}
			}
		}

		return new ArrayList<String>(elements);
	}

}
