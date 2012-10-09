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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	private ShapeRenderer checkBoxRenderer;
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
		this(ids, selectionsExclusive, stringColumns, stringColumnValues,
				doubleColumns, doubleColumnValues, isStringColumnVisible,
				isStringColumnFilterable, null);
	}

	public DataAndModelSelectionPanel(List<String> ids,
			boolean selectionsExclusive, List<String> stringColumns,
			List<List<String>> stringColumnValues, List<String> doubleColumns,
			List<List<Double>> doubleColumnValues,
			List<Boolean> isStringColumnVisible,
			List<Boolean> isStringColumnFilterable, List<Integer> colorCounts) {
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

		SelectTableModel model;

		if (colorCounts == null) {
			colorAndShapes = new ColorAndShapeCreator(ids.size());
			model = new SelectTableModel(ids, colorAndShapes.getColorList(),
					colorAndShapes.getShapeNameList(), stringColumns,
					stringColumnValues, doubleColumns, doubleColumnValues,
					false, selectionsExclusive);
		} else {
			List<List<Color>> colorLists = new ArrayList<List<Color>>();
			List<List<String>> shapeLists = new ArrayList<List<String>>();

			colorAndShapes = new ColorAndShapeCreator(
					Collections.max(colorCounts));

			for (int n : colorCounts) {
				ArrayList<Color> colors = new ArrayList<Color>();
				ArrayList<String> shapes = new ArrayList<String>();

				for (int i = 0; i < n; i++) {
					colors.add(colorAndShapes.getColorList().get(i));
					shapes.add(colorAndShapes.getShapeNameList().get(i));
				}

				colorLists.add(colors);
				shapeLists.add(shapes);
			}

			model = new SelectTableModel(ids, colorLists, shapeLists,
					stringColumns, stringColumnValues, doubleColumns,
					doubleColumnValues, true, selectionsExclusive);
		}

		selectTable = new JTable(model);
		selectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectTable.getSelectionModel().addListSelectionListener(this);
		selectTable.setRowHeight((new JComboBox()).getPreferredSize().height);
		selectTable.setRowSorter(new SelectTableRowSorter(model, null));
		checkBoxRenderer = new ShapeRenderer();
		selectTable.getColumn(SelectTableModel.ID).setMinWidth(0);
		selectTable.getColumn(SelectTableModel.ID).setMaxWidth(0);
		selectTable.getColumn(SelectTableModel.ID).setPreferredWidth(0);
		selectTable.getColumn(SelectTableModel.SELECTED).setCellEditor(
				new CheckBoxEditor());
		selectTable.getColumn(SelectTableModel.SELECTED).setCellRenderer(
				checkBoxRenderer);
		selectTable.getColumn(SelectTableModel.SELECTED).getCellEditor()
				.addCellEditorListener(this);

		if (colorCounts == null) {
			selectTable.getColumn(SelectTableModel.COLOR).setCellEditor(
					new ColorEditor());
			selectTable.getColumn(SelectTableModel.COLOR).setCellRenderer(
					new ColorRenderer());
			selectTable.getColumn(SelectTableModel.SHAPE).setCellEditor(
					new DefaultCellEditor(new JComboBox(
							ColorAndShapeCreator.SHAPE_NAMES)));
		} else {
			selectTable.getColumn(SelectTableModel.COLOR).setCellEditor(
					new ColorListEditor());
			selectTable.getColumn(SelectTableModel.COLOR).setCellRenderer(
					new ColorListRenderer());
			selectTable.getColumn(SelectTableModel.SHAPE).setCellEditor(
					new ShapeListEditor());
			selectTable.getColumn(SelectTableModel.SHAPE).setCellRenderer(
					new ShapeListRenderer());
		}

		selectTable.getColumn(SelectTableModel.COLOR).getCellEditor()
				.addCellEditorListener(this);
		selectTable.getColumn(SelectTableModel.SHAPE).getCellEditor()
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

	public String getFilter(String column) {
		if (comboBoxes.containsKey(column)) {
			return (String) comboBoxes.get(column).getSelectedItem();
		} else {
			return null;
		}
	}

	public void setFilter(String column, String filter) {
		if (comboBoxes.containsKey(column)) {
			comboBoxes.get(column).setSelectedItem(filter);
			applyFilters();
		}
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

	@SuppressWarnings("unchecked")
	public Map<String, List<Color>> getColorLists() {
		Map<String, List<Color>> paints = new HashMap<String, List<Color>>(
				selectTable.getRowCount());

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			paints.put((String) selectTable.getValueAt(i, 0),
					(List<Color>) selectTable.getValueAt(i, 2));
		}

		return paints;
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<Shape>> getShapeLists() {
		Map<String, List<Shape>> shapes = new HashMap<String, List<Shape>>(
				selectTable.getRowCount());
		Map<String, Shape> shapeMap = colorAndShapes.getShapeByNameMap();

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			List<Shape> list = new ArrayList<Shape>();

			for (String name : (List<String>) selectTable.getValueAt(i, 3)) {
				list.add(shapeMap.get(name));
			}

			shapes.put((String) selectTable.getValueAt(i, 0), list);
		}

		return shapes;
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
			applyFilters();
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

	private void applyFilters() {
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

	public static interface SelectionListener {

		public void selectionChanged();

		public void focusChanged();
	}

	private abstract class AbstractSelectTableModel extends AbstractTableModel {

		public static final String ID = "ID";
		public static final String SELECTED = "Selected";
		public static final String COLOR = "Color";
		public static final String SHAPE = "Shape";

		private static final long serialVersionUID = 1L;

		private boolean listBased;

		private List<String> ids;
		private List<Boolean> selections;
		private List<Color> colors;
		private List<List<Color>> colorLists;
		private List<String> shapes;
		private List<List<String>> shapeLists;
		private List<String> stringColumns;
		private List<List<String>> stringColumnValues;
		private List<String> doubleColumns;
		private List<List<Double>> doubleColumnValues;

		@SuppressWarnings("unchecked")
		public AbstractSelectTableModel(List<String> ids, List<?> colors,
				List<?> shapes, List<String> stringColumns,
				List<List<String>> stringColumnValues,
				List<String> doubleColumns,
				List<List<Double>> doubleColumnValues, boolean listBased) {
			this.listBased = listBased;

			if (!listBased) {
				this.colors = (List<Color>) colors;
				this.shapes = (List<String>) shapes;
			} else {
				this.colorLists = (List<List<Color>>) colors;
				this.shapeLists = (List<List<String>>) shapes;
			}

			this.ids = ids;
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
				return ID;
			case 1:
				return SELECTED;
			case 2:
				return COLOR;
			case 3:
				return SHAPE;
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
				if (!listBased) {
					return colors.get(row);
				} else {
					return colorLists.get(row);
				}
			case 3:
				if (!listBased) {
					return shapes.get(row);
				} else {
					return shapeLists.get(row);
				}
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
				if (!listBased) {
					return Color.class;
				} else {
					return List.class;
				}
			case 3:
				if (!listBased) {
					return String.class;
				} else {
					return List.class;
				}
			default:
				if (column - 4 < stringColumns.size()) {
					return String.class;
				} else {
					return Double.class;
				}
			}
		}

		@SuppressWarnings("unchecked")
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
				if (!listBased) {
					colors.set(row, (Color) value);
				} else {
					colorLists.set(row, (List<Color>) value);
				}
				break;
			case 3:
				if (!listBased) {
					shapes.set(row, (String) value);
				} else {
					shapeLists.set(row, (List<String>) value);
				}
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

		public SelectTableModel(List<String> ids, List<?> colors,
				List<?> shapes, List<String> stringColumns,
				List<List<String>> stringColumnValues,
				List<String> doubleColumns,
				List<List<Double>> doubleColumnValues, boolean listBased,
				boolean exclusive) {
			super(ids, colors, shapes, stringColumns, stringColumnValues,
					doubleColumns, doubleColumnValues, listBased);
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

				@Override
				public void actionPerformed(ActionEvent arg0) {
					Color newColor = JColorChooser.showDialog(colorButton,
							"Choose Color", colorButton.getBackground());

					if (newColor != null) {
						colorButton.setBackground(newColor);
						stopCellEditing();
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

	private class ColorListEditor extends AbstractCellEditor implements
			TableCellEditor {

		private static final long serialVersionUID = 1L;

		private JButton button;
		private List<Color> colorList;

		public ColorListEditor() {
			button = new JButton();
			colorList = new ArrayList<Color>();
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ColorListDialog dialog = new ColorListDialog(colorList);

					dialog.setVisible(true);

					if (dialog.isApproved()) {
						colorList = dialog.getColorList();
						stopCellEditing();
					}
				}
			});
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			colorList = (List<Color>) value;

			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return colorList;
		}
	}

	private class ShapeListEditor extends AbstractCellEditor implements
			TableCellEditor {

		private static final long serialVersionUID = 1L;

		private JButton button;
		private List<String> shapeList;

		public ShapeListEditor() {
			button = new JButton();
			shapeList = new ArrayList<String>();
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ShapeListDialog dialog = new ShapeListDialog(shapeList);

					dialog.setVisible(true);

					if (dialog.isApproved()) {
						shapeList = dialog.getShapeList();
						stopCellEditing();
					}
				}
			});
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			shapeList = (List<String>) value;

			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return shapeList;
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

	private class ShapeRenderer extends JCheckBox implements TableCellRenderer {

		private static final long serialVersionUID = -8337460338388283099L;

		public ShapeRenderer() {
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

	private class ColorListRenderer extends JComponent implements
			TableCellRenderer {

		private static final long serialVersionUID = 1L;

		private List<Color> colorList;

		public ColorListRenderer() {
			colorList = new ArrayList<Color>();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {
			colorList = (List<Color>) color;

			return this;
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (colorList.isEmpty()) {
				super.paintComponents(g);
			} else {
				double w = (double) getWidth() / (double) colorList.size();

				for (int i = 0; i < colorList.size(); i++) {
					g.setColor(colorList.get(i));
					g.fillRect((int) (i * w), 0, (int) w, getHeight());
				}
			}
		}
	}

	private class ShapeListRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ShapeListRenderer() {
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {
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

	private class ColorListDialog extends JDialog implements ActionListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private List<Color> colorList;

		private List<JButton> colorButtons;

		private JButton okButton;
		private JButton cancelButton;

		public ColorListDialog(List<Color> initialColors) {
			super(JOptionPane
					.getFrameForComponent(DataAndModelSelectionPanel.this),
					"Color Palette", true);

			approved = false;
			colorList = null;

			colorButtons = new ArrayList<JButton>();
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);

			JPanel centerPanel = new JPanel();
			JPanel bottomPanel = new JPanel();

			centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			centerPanel
					.setLayout(new GridLayout(initialColors.size(), 1, 5, 5));

			for (Color color : initialColors) {
				JButton button = new JButton();

				button.setBackground(color);
				button.setPreferredSize(new Dimension(
						button.getPreferredSize().width, 20));
				button.addActionListener(this);
				colorButtons.add(button);
				centerPanel.add(button);
			}

			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			bottomPanel.add(okButton);
			bottomPanel.add(cancelButton);

			setLayout(new BorderLayout());
			add(centerPanel, BorderLayout.CENTER);
			add(bottomPanel, BorderLayout.SOUTH);
			pack();

			setResizable(false);
			setLocationRelativeTo(DataAndModelSelectionPanel.this);
		}

		public boolean isApproved() {
			return approved;
		}

		public List<Color> getColorList() {
			return colorList;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				approved = true;
				colorList = new ArrayList<Color>();

				for (JButton button : colorButtons) {
					colorList.add(button.getBackground());
				}

				dispose();
			} else if (e.getSource() == cancelButton) {
				dispose();
			} else {
				JButton button = (JButton) e.getSource();
				Color newColor = JColorChooser.showDialog(button,
						"Choose Color", button.getBackground());

				if (newColor != null) {
					button.setBackground(newColor);
				}
			}
		}
	}

	private class ShapeListDialog extends JDialog implements ActionListener {

		private static final long serialVersionUID = 1L;

		private boolean approved;
		private List<String> shapeList;

		private List<JComboBox> shapeBoxes;

		private JButton okButton;
		private JButton cancelButton;

		public ShapeListDialog(List<String> initialShapes) {
			super(JOptionPane
					.getFrameForComponent(DataAndModelSelectionPanel.this),
					"Shape Palette", true);

			approved = false;
			shapeList = null;

			shapeBoxes = new ArrayList<JComboBox>();
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);

			JPanel centerPanel = new JPanel();
			JPanel bottomPanel = new JPanel();

			centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			centerPanel
					.setLayout(new GridLayout(initialShapes.size(), 1, 5, 5));

			for (String shape : initialShapes) {
				JComboBox box = new JComboBox(ColorAndShapeCreator.SHAPE_NAMES);

				box.setSelectedItem(shape);
				shapeBoxes.add(box);
				centerPanel.add(box);
			}

			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			bottomPanel.add(okButton);
			bottomPanel.add(cancelButton);

			setLayout(new BorderLayout());
			add(centerPanel, BorderLayout.CENTER);
			add(bottomPanel, BorderLayout.SOUTH);
			pack();

			setResizable(false);
			setLocationRelativeTo(DataAndModelSelectionPanel.this);
		}

		public boolean isApproved() {
			return approved;
		}

		public List<String> getShapeList() {
			return shapeList;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				approved = true;
				shapeList = new ArrayList<String>();

				for (JComboBox box : shapeBoxes) {
					shapeList.add((String) box.getSelectedItem());
				}

				dispose();
			} else if (e.getSource() == cancelButton) {
				dispose();
			}
		}
	}

}
