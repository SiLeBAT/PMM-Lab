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
package de.bund.bfr.knime.pmm.common.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import de.bund.bfr.knime.pmm.common.ui.SpacePanel;

public class DataAndModelSelectionPanel extends JPanel implements
		ActionListener, CellEditorListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private List<SelectionListener> listeners;

	private ColorAndShapeCreator colorAndShapes;

	private JTable selectTable;
	private CheckBoxRenderer checkBoxRenderer;
	private JButton selectAllButton;
	private JButton unselectAllButton;
	private JButton invertSelectionButton;
	private Map<String, JComboBox> comboBoxes;

	public DataAndModelSelectionPanel(List<String> ids,
			boolean selectionsExclusive, List<String> stringColumns,
			List<List<String>> stringColumnValues, List<String> doubleColumns,
			List<List<Double>> doubleColumnValues,
			List<Boolean> isStringColumnVisible,
			List<Boolean> isStringColumnFilterable) {
		listeners = new ArrayList<SelectionListener>();

		JPanel upperPanel = new JPanel();

		upperPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		if (!selectionsExclusive) {
			JPanel selectPanel = new JPanel();

			selectAllButton = new JButton("All");
			selectAllButton.addActionListener(this);
			unselectAllButton = new JButton("None");
			unselectAllButton.addActionListener(this);
			invertSelectionButton = new JButton("Invert");
			invertSelectionButton.addActionListener(this);

			selectPanel.setBorder(BorderFactory.createTitledBorder("Select"));
			selectPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			selectPanel.add(selectAllButton);
			selectPanel.add(unselectAllButton);
			selectPanel.add(invertSelectionButton);
			upperPanel.add(selectPanel);
		}

		if (isStringColumnFilterable.contains(true)) {
			JPanel filterPanel = new JPanel();

			filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));
			filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			comboBoxes = new HashMap<String, JComboBox>();

			for (int i = 0; i < stringColumns.size(); i++) {
				if (isStringColumnFilterable.get(i)) {
					List<String> values = new ArrayList<String>();

					values.add("");
					values.addAll(new HashSet<String>(stringColumnValues.get(i)));
					Collections.sort(values);

					JComboBox box = new JComboBox(values.toArray(new String[0]));

					box.addActionListener(this);
					filterPanel.add(new JLabel(stringColumns.get(i) + ":"));
					filterPanel.add(box);
					comboBoxes.put(stringColumns.get(i), box);
				}
			}

			upperPanel.add(filterPanel);
		}

		colorAndShapes = new ColorAndShapeCreator(ids.size());

		SelectTableModel model = new SelectTableModel(ids,
				colorAndShapes.getColorList(),
				colorAndShapes.getShapeNameList(), stringColumns,
				stringColumnValues, doubleColumns, doubleColumnValues,
				selectionsExclusive);

		selectTable = new JTable(model);
		selectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectTable.getSelectionModel().addListSelectionListener(this);
		selectTable.setRowHeight((new JComboBox()).getPreferredSize().height);
		selectTable.setRowSorter(new SelectTableRowSorter(model, null));
		checkBoxRenderer = new CheckBoxRenderer();
		selectTable.getColumn("ID").setMinWidth(0);
		selectTable.getColumn("ID").setMaxWidth(0);
		selectTable.getColumn("ID").setPreferredWidth(0);
		selectTable.getColumn("Selected").setCellEditor(new CheckBoxEditor());
		selectTable.getColumn("Selected").setCellRenderer(checkBoxRenderer);
		selectTable.getColumn("Selected").getCellEditor()
				.addCellEditorListener(this);
		selectTable.getColumn("Color").setCellEditor(new ColorEditor());
		selectTable.getColumn("Color").setCellRenderer(new ColorRenderer());
		selectTable.getColumn("Color").getCellEditor()
				.addCellEditorListener(this);
		selectTable.getColumn("Shape").setCellEditor(
				new DefaultCellEditor(new JComboBox(
						ColorAndShapeCreator.SHAPE_NAMES)));
		selectTable.getColumn("Shape").getCellEditor()
				.addCellEditorListener(this);

		for (int i = 0; i < stringColumns.size(); i++) {
			if (!isStringColumnVisible.get(i)) {
				selectTable.getColumn(stringColumns.get(i)).setMinWidth(0);
				selectTable.getColumn(stringColumns.get(i)).setMaxWidth(0);
				selectTable.getColumn(stringColumns.get(i))
						.setPreferredWidth(0);
			}
		}

		setLayout(new BorderLayout());
		add(new SpacePanel(upperPanel), BorderLayout.NORTH);
		add(new JScrollPane(selectTable,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
	}

	public String getFocusedID() {
		int row = selectTable.getSelectedRow();

		if (row != -1) {
			return (String) selectTable.getValueAt(row, 0);
		}

		return null;
	}

	public List<String> getSelectedIDs() {
		List<String> selectedIDs = new ArrayList<String>();

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			if ((Boolean) selectTable.getValueAt(i, 1)) {
				selectedIDs.add((String) selectTable.getValueAt(i, 0));
			}
		}

		return selectedIDs;
	}

	public void setSelectedIDs(List<String> selectedIDs) {
		Set<String> idSet = new HashSet<String>(selectedIDs);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			if (idSet.contains(selectTable.getValueAt(i, 0))) {
				selectTable.setValueAt(true, i, 1);
			} else {
				selectTable.setValueAt(false, i, 1);
			}
		}

		fireSelectionChanged();
	}

	public Map<String, Color> getColors() {
		Map<String, Color> paints = new HashMap<String, Color>(
				selectTable.getRowCount());

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			paints.put((String) selectTable.getValueAt(i, 0),
					(Color) selectTable.getValueAt(i, 2));
		}

		return paints;
	}

	public void setColors(Map<String, Color> colors) {
		for (int i = 0; i < selectTable.getRowCount(); i++) {
			Color color = colors.get(selectTable.getValueAt(i, 0));

			if (color != null) {
				selectTable.setValueAt(color, i, 2);
			}
		}
	}

	public Map<String, Shape> getShapes() {
		Map<String, Shape> shapes = new HashMap<String, Shape>(
				selectTable.getRowCount());
		Map<String, Shape> shapeMap = colorAndShapes.getShapeByNameMap();

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			shapes.put((String) selectTable.getValueAt(i, 0),
					shapeMap.get(selectTable.getValueAt(i, 3)));
		}

		return shapes;
	}

	public void setShapes(Map<String, Shape> shapes) {
		Map<Shape, String> shapeMap = colorAndShapes.getNameByShapeMap();

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			Shape shape = shapes.get(selectTable.getValueAt(i, 0));

			if (shape != null) {
				selectTable.setValueAt(shapeMap.get(shape), i, 3);
			}
		}
	}

	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}

	public void fireSelectionChanged() {
		for (SelectionListener listener : listeners) {
			listener.selectionChanged();
		}
	}

	public void fireInfoSelectionChanged() {
		for (SelectionListener listener : listeners) {
			listener.focusChanged();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == selectAllButton) {
			for (int i = 0; i < selectTable.getRowCount(); i++) {
				selectTable.setValueAt(true, i, 1);
			}
		} else if (e.getSource() == unselectAllButton) {
			for (int i = 0; i < selectTable.getRowCount(); i++) {
				selectTable.setValueAt(false, i, 1);
			}
		} else if (e.getSource() == invertSelectionButton) {
			for (int i = 0; i < selectTable.getRowCount(); i++) {
				selectTable.setValueAt(!(Boolean) selectTable.getValueAt(i, 1),
						i, 1);
			}
		} else {
			Map<String, String> filters = new HashMap<String, String>();

			for (String column : comboBoxes.keySet()) {
				JComboBox box = comboBoxes.get(column);

				if (!box.getSelectedItem().equals("")) {
					filters.put(column, (String) box.getSelectedItem());
				}
			}

			selectTable.setRowSorter(new SelectTableRowSorter(
					(SelectTableModel) selectTable.getModel(), filters));
		}

		fireSelectionChanged();
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		fireSelectionChanged();
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		fireInfoSelectionChanged();
	}

	public static interface SelectionListener {

		public void selectionChanged();

		public void focusChanged();
	}

	private abstract class AbstractSelectTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<String> ids;
		private List<Boolean> selections;
		private List<Color> colors;
		private List<String> shapes;
		private List<String> stringColumns;
		private List<List<String>> stringColumnValues;
		private List<String> doubleColumns;
		private List<List<Double>> doubleColumnValues;

		public AbstractSelectTableModel(List<String> ids, List<Color> colors,
				List<String> shapes, List<String> stringColumns,
				List<List<String>> stringColumnValues,
				List<String> doubleColumns,
				List<List<Double>> doubleColumnValues) {
			this.ids = ids;
			this.colors = colors;
			this.shapes = shapes;
			selections = new ArrayList<Boolean>(Collections.nCopies(ids.size(),
					false));
			this.stringColumns = stringColumns;
			this.stringColumnValues = stringColumnValues;
			this.doubleColumns = doubleColumns;
			this.doubleColumnValues = doubleColumnValues;

			if (this.doubleColumns == null) {
				this.doubleColumns = new ArrayList<String>();
			}
		}

		@Override
		public int getColumnCount() {
			return 4 + stringColumns.size() + doubleColumns.size();
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "ID";
			case 1:
				return "Selected";
			case 2:
				return "Color";
			case 3:
				return "Shape";
			default:
				if (column - 4 < stringColumns.size()) {
					return stringColumns.get(column - 4);
				} else {
					return doubleColumns.get(column - 4 - stringColumns.size());
				}
			}
		}

		@Override
		public int getRowCount() {
			return ids.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column) {
			case 0:
				return ids.get(row);
			case 1:
				return selections.get(row);
			case 2:
				return colors.get(row);
			case 3:
				return shapes.get(row);
			default:
				if (column - 4 < stringColumns.size()) {
					return stringColumnValues.get(column - 4).get(row);
				} else {
					return doubleColumnValues.get(
							column - 4 - stringColumns.size()).get(row);
				}
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return String.class;
			case 1:
				return Boolean.class;
			case 2:
				return Color.class;
			case 3:
				return String.class;
			default:
				if (column - 4 < stringColumns.size()) {
					return String.class;
				} else {
					return Double.class;
				}
			}
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			switch (column) {
			case 0:
				ids.set(row, (String) value);
				break;
			case 1:
				selections.set(row, (Boolean) value);
				break;
			case 2:
				colors.set(row, (Color) value);
				break;
			case 3:
				shapes.set(row, (String) value);
				break;
			default:
				if (column - 4 < stringColumns.size()) {
					stringColumnValues.get(column - 4).set(row, (String) value);
				} else {
					doubleColumnValues.get(column - 4 - stringColumns.size())
							.set(row, (Double) value);
				}
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 1 || column == 2 || column == 3;
		}
	}

	private class SelectTableModel extends AbstractSelectTableModel {

		private static final long serialVersionUID = 1L;

		private boolean exclusive;

		public SelectTableModel(List<String> ids, List<Color> colors,
				List<String> shapes, List<String> stringColumns,
				List<List<String>> stringColumnValues,
				List<String> doubleColumns,
				List<List<Double>> doubleColumnValues, boolean exclusive) {
			super(ids, colors, shapes, stringColumns, stringColumnValues,
					doubleColumns, doubleColumnValues);
			this.exclusive = exclusive;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			super.setValueAt(value, row, column);

			if (exclusive && column == 1 && value.equals(Boolean.TRUE)) {
				for (int i = 0; i < getRowCount(); i++) {
					if (i != row) {
						super.setValueAt(false, i, 1);
						fireTableCellUpdated(i, 1);
					}
				}
			}

			fireTableCellUpdated(row, column);
		}
	}

	private class ColorEditor extends AbstractCellEditor implements
			TableCellEditor {

		private static final long serialVersionUID = 1L;

		private JButton colorButton;

		public ColorEditor() {
			colorButton = new JButton();
			colorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Color newColor = JColorChooser.showDialog(colorButton,
							"Choose Color", colorButton.getBackground());

					if (newColor != null) {
						colorButton.setBackground(newColor);
						ColorEditor.this.stopCellEditing();
					}
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			colorButton.setBackground((Color) value);

			return colorButton;
		}

		@Override
		public Object getCellEditorValue() {
			return colorButton.getBackground();
		}

	}

	private class ColorRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ColorRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setBackground((Color) color);

			return this;
		}
	}

	private class CheckBoxRenderer extends JCheckBox implements
			TableCellRenderer {

		private static final long serialVersionUID = -8337460338388283099L;

		public CheckBoxRenderer() {
			super();
			setHorizontalAlignment(JLabel.CENTER);
			setBorderPainted(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			int fittedColumn = -1;

			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumnName(i).equals(ChartConstants.IS_FITTED)) {
					fittedColumn = i;
					break;
				}
			}

			if (isSelected) {
				setForeground(table.getSelectionForeground());
				super.setBackground(table.getSelectionBackground());
			} else if (fittedColumn != -1
					&& table.getValueAt(row, fittedColumn).equals(
							ChartConstants.NO)) {
				setForeground(Color.RED);
				setBackground(Color.RED);
			} else if (fittedColumn != -1
					&& table.getValueAt(row, fittedColumn).equals(
							ChartConstants.WARNING)) {
				setForeground(Color.YELLOW);
				setBackground(Color.YELLOW);
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}

			setSelected((value != null && ((Boolean) value).booleanValue()));

			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			} else {
				setBorder(new EmptyBorder(1, 1, 1, 1));
			}

			return this;
		}
	}

	private class CheckBoxEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 1L;

		public CheckBoxEditor() {
			super(new JCheckBox());
			((JCheckBox) getComponent())
					.setHorizontalAlignment(JCheckBox.CENTER);
		}
	}

	private class SelectTableRowSorter extends TableRowSorter<SelectTableModel> {

		private Map<Integer, String> filters;

		public SelectTableRowSorter(SelectTableModel model,
				Map<String, String> filters) {
			super(model);
			this.filters = new HashMap<Integer, String>();

			if (filters != null) {
				for (String column : filters.keySet()) {
					for (int i = 0; i < model.getColumnCount(); i++) {
						if (column.equals(model.getColumnName(i))) {
							this.filters.put(i, filters.get(column));
						}
					}
				}

				addFilters();
			}
		}

		private void addFilters() {
			setRowFilter(new RowFilter<SelectTableModel, Object>() {

				@Override
				public boolean include(
						javax.swing.RowFilter.Entry<? extends SelectTableModel, ? extends Object> entry) {
					for (int column : filters.keySet()) {
						if (!entry.getStringValue(column).equals(
								filters.get(column))) {
							return false;
						}
					}

					return true;
				}
			});
		}
	}

}
