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
package de.bund.bfr.knime.pmm.xlsmodelreader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.hsh.bfr.db.DBKernel;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.pmm.bfrdbiface.lib.Bfrdb;
import de.bund.bfr.knime.pmm.common.AgentXml;
import de.bund.bfr.knime.pmm.common.CatalogModelXml;
import de.bund.bfr.knime.pmm.common.DBUtilities;
import de.bund.bfr.knime.pmm.common.IndepXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MdInfoXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.ParamXml;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.XLSReader;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.knime.pmm.common.ui.FilePanel;
import de.bund.bfr.knime.pmm.common.ui.UI;
import de.bund.bfr.knime.pmm.common.ui.FilePanel.FileListener;
import de.bund.bfr.knime.pmm.common.units.Categories;

/**
 * <code>NodeDialog</code> for the "XLSModelReader" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class XLSModelReaderNodeDialog extends NodeDialogPane implements
		ActionListener, ItemListener, FileListener {

	private static final String DO_NOT_USE = "Do Not Use";
	private static final String OTHER_PARAMETER = "Select Other";
	private static final String SELECT = "Select";
	private static final String USE_SECONDARY_MODEL = "Use Sec. Model";
	private static final String RELOAD = "Reload";

	private XLSReader xlsReader;

	private SettingsHelper set;

	private JPanel mainPanel;

	private FilePanel filePanel;
	private JComboBox<String> sheetBox;
	private List<String> fileSheetList;
	private List<String> fileColumnList;

	private JButton addLiteratureButton;
	private JButton removeLiteratureButton;
	private JList<LiteratureItem> literatureList;

	private JPanel modelPanel;
	private JButton modelButton;
	private JButton modelReloadButton;
	private Map<String, JComboBox<String>> modelBoxes;
	private Map<String, JButton> secModelButtons;
	private Map<String, JButton> secModelReloadButtons;
	private Map<String, Map<String, JComboBox<String>>> secModelBoxes;
	private Map<String, Map<String, JComboBox<String>>> secMinBoxes;
	private Map<String, Map<String, JComboBox<String>>> secMaxBoxes;
	private Map<String, Map<String, JComboBox<String>>> secUnitBoxes;

	private JPanel agentPanel;
	private JComboBox<String> agentBox;
	private JButton agentButton;
	private Map<String, JButton> agentButtons;

	private JPanel matrixPanel;
	private JComboBox<String> matrixBox;
	private JButton matrixButton;
	private Map<String, JButton> matrixButtons;

	private JPanel columnsPanel;
	private Map<String, JComboBox<String>> columnBoxes;
	private Map<String, JButton> columnButtons;
	private Map<String, JComboBox<String>> columnUnitBoxes;

	private JLabel noLabel;

	/**
	 * New pane for configuring the XLSModelReader node.
	 */
	protected XLSModelReaderNodeDialog() {
		xlsReader = new XLSReader();

		filePanel = new FilePanel("XLS File", FilePanel.OPEN_DIALOG);
		filePanel.setAcceptAllFiles(false);
		filePanel.addFileFilter(".xls", "Excel Spreadsheat (*.xls)");
		filePanel.addFileListener(this);
		sheetBox = new JComboBox<String>();
		sheetBox.addItemListener(this);
		fileSheetList = new ArrayList<String>();
		fileColumnList = new ArrayList<String>();

		addLiteratureButton = new JButton("Add");
		addLiteratureButton.addActionListener(this);
		removeLiteratureButton = new JButton("Remove");
		removeLiteratureButton.addActionListener(this);
		literatureList = new JList<LiteratureItem>();

		noLabel = new JLabel();
		noLabel.setPreferredSize(new Dimension(100, 50));

		modelPanel = new JPanel();
		modelPanel.setBorder(BorderFactory.createTitledBorder("Models"));
		modelPanel.setLayout(new BorderLayout());
		modelPanel.add(noLabel, BorderLayout.CENTER);
		modelBoxes = new LinkedHashMap<String, JComboBox<String>>();
		secModelButtons = new LinkedHashMap<String, JButton>();
		secModelReloadButtons = new LinkedHashMap<String, JButton>();
		secModelBoxes = new LinkedHashMap<String, Map<String, JComboBox<String>>>();
		secMinBoxes = new LinkedHashMap<String, Map<String, JComboBox<String>>>();
		secMaxBoxes = new LinkedHashMap<String, Map<String, JComboBox<String>>>();
		secUnitBoxes = new LinkedHashMap<String, Map<String, JComboBox<String>>>();

		agentPanel = new JPanel();
		agentPanel.setBorder(BorderFactory
				.createTitledBorder(AttributeUtilities
						.getName(TimeSeriesSchema.ATT_AGENT)));
		agentPanel.setLayout(new BorderLayout());
		agentPanel.add(noLabel, BorderLayout.CENTER);
		agentButtons = new LinkedHashMap<String, JButton>();

		matrixPanel = new JPanel();
		matrixPanel.setBorder(BorderFactory
				.createTitledBorder(AttributeUtilities
						.getName(TimeSeriesSchema.ATT_MATRIX)));
		matrixPanel.setLayout(new BorderLayout());
		matrixPanel.add(noLabel, BorderLayout.CENTER);
		matrixButtons = new LinkedHashMap<String, JButton>();

		columnsPanel = new JPanel();
		columnsPanel.setBorder(BorderFactory
				.createTitledBorder("XLS Column -> PMM-Lab assignments"));
		columnsPanel.setLayout(new BorderLayout());
		columnsPanel.add(noLabel, BorderLayout.CENTER);
		columnBoxes = new LinkedHashMap<String, JComboBox<String>>();
		columnButtons = new LinkedHashMap<String, JButton>();
		columnUnitBoxes = new LinkedHashMap<String, JComboBox<String>>();

		JPanel northLiteraturePanel = new JPanel();

		northLiteraturePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northLiteraturePanel.add(addLiteratureButton);
		northLiteraturePanel.add(removeLiteratureButton);

		JPanel literaturePanel = new JPanel();

		literaturePanel.setBorder(BorderFactory
				.createTitledBorder("Literature"));
		literaturePanel.setLayout(new BorderLayout());
		literaturePanel.add(northLiteraturePanel, BorderLayout.NORTH);
		literaturePanel.add(new JScrollPane(literatureList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

		JPanel optionsPanel = new JPanel();

		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		optionsPanel.add(literaturePanel);
		optionsPanel.add(modelPanel);
		optionsPanel.add(agentPanel);
		optionsPanel.add(matrixPanel);
		optionsPanel.add(columnsPanel);

		JPanel sheetPanel = new JPanel();

		sheetPanel.setBorder(BorderFactory.createTitledBorder("Sheet"));
		sheetPanel.setLayout(new BorderLayout());
		sheetPanel.add(sheetBox, BorderLayout.NORTH);

		JPanel fileSheetPanel = new JPanel();

		fileSheetPanel.setLayout(new BorderLayout());
		fileSheetPanel.add(filePanel, BorderLayout.CENTER);
		fileSheetPanel.add(sheetPanel, BorderLayout.EAST);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(fileSheetPanel, BorderLayout.NORTH);
		mainPanel.add(optionsPanel, BorderLayout.CENTER);

		addTab("Options", mainPanel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		set = new SettingsHelper();
		set.loadSettings(settings);

		filePanel.removeFileListener(this);
		filePanel.setFileName(set.getFileName());
		filePanel.addFileListener(this);

		try {
			fileSheetList = xlsReader.getSheets(new File(set.getFileName()));
		} catch (Exception e) {
			fileSheetList = new ArrayList<String>();
		}

		sheetBox.removeItemListener(this);
		sheetBox.removeAllItems();

		for (String sheet : fileSheetList) {
			sheetBox.addItem(sheet);
		}

		UI.select(sheetBox, set.getSheetName());
		sheetBox.addItemListener(this);

		try {
			fileColumnList = xlsReader.getColumns(
					new File(filePanel.getFileName()),
					(String) sheetBox.getSelectedItem());
		} catch (Exception e) {
			fileColumnList = new ArrayList<String>();
		}

		if (set.getAgentColumn() == null) {
			if (set.getAgent() != null) {
				set.setAgentColumn(OTHER_PARAMETER);
			} else {
				set.setAgentColumn(DO_NOT_USE);
			}
		}

		if (set.getMatrixColumn() == null) {
			if (set.getMatrix() != null) {
				set.setMatrixColumn(OTHER_PARAMETER);
			} else {
				set.setMatrixColumn(DO_NOT_USE);
			}
		}

		literatureList.setListData(set.getLiterature().toArray(
				new LiteratureItem[0]));

		updateModelPanel();
		updateAgentPanel();
		updateMatrixPanel();
		updateColumnsPanel();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		cleanMaps();

		if (set.getFileName() == null) {
			throw new InvalidSettingsException("No file is specfied");
		}

		if (set.getSheetName() == null) {
			throw new InvalidSettingsException("No sheet is selected");
		}

		if (fileColumnList.isEmpty()) {
			throw new InvalidSettingsException("Specified file is invalid");
		}

		if (set.getModelTuple() == null) {
			throw new InvalidSettingsException("No model is specified");
		}

		if (set.getAgentColumn() != null
				&& set.getAgentColumn().equals(OTHER_PARAMETER)
				&& set.getAgent() == null) {
			throw new InvalidSettingsException("No assignment for "
					+ TimeSeriesSchema.ATT_AGENT);
		}

		if (set.getMatrixColumn() != null
				&& set.getMatrixColumn().equals(OTHER_PARAMETER)
				&& set.getMatrix() == null) {
			throw new InvalidSettingsException("No assignment for "
					+ TimeSeriesSchema.ATT_MATRIX);
		}

		Set<Object> assignments = new LinkedHashSet<Object>();

		for (String column : set.getColumnMappings().keySet()) {
			Object assignment = set.getColumnMappings().get(column);

			if (assignment == null) {
				throw new InvalidSettingsException("Column \"" + column
						+ "\" has no assignment");
			}

			if (!assignments.add(assignment)) {
				String name = null;

				if (assignment instanceof MiscXml) {
					name = ((MiscXml) assignment).getName();
				} else if (assignment instanceof String) {
					name = (String) assignment;
				}

				throw new InvalidSettingsException("\"" + name
						+ "\" can only be assigned once");
			}
		}

		if (set.getAgentColumn() != null
				&& !set.getAgentColumn().equals(OTHER_PARAMETER)) {
			set.setAgent(null);
		}

		if (set.getMatrixColumn() != null
				&& !set.getMatrixColumn().equals(OTHER_PARAMETER)) {
			set.setMatrix(null);
		}

		if (set.getAgentColumn() != null
				&& (set.getAgentColumn().equals(OTHER_PARAMETER) || set
						.getAgentColumn().equals(DO_NOT_USE))) {
			set.setAgentColumn(null);
		}

		if (set.getMatrixColumn() != null
				&& (set.getMatrixColumn().equals(OTHER_PARAMETER) || set
						.getMatrixColumn().equals(DO_NOT_USE))) {
			set.setMatrixColumn(null);
		}

		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == modelButton) {
			Integer id;

			if (set.getModelTuple() != null) {
				id = DBKernel.openPrimModelDBWindow(((CatalogModelXml) set
						.getModelTuple()
						.getPmmXml(Model1Schema.ATT_MODELCATALOG).get(0))
						.getId());
			} else {
				id = DBKernel.openPrimModelDBWindow(null);
			}

			if (id != null) {
				Bfrdb db = new Bfrdb(DBKernel.getLocalConn(true));

				try {
					set.setModelTuple(db.getPrimModelById(id));
				} catch (SQLException ex) {
					ex.printStackTrace();
				}

				updateModelPanel();
			}
		} else if (e.getSource() == modelReloadButton) {
			Integer id = ((CatalogModelXml) set.getModelTuple()
					.getPmmXml(Model1Schema.ATT_MODELCATALOG).get(0)).getId();

			if (id != null) {
				Bfrdb db = new Bfrdb(DBKernel.getLocalConn(true));

				try {
					set.setModelTuple(db.getPrimModelById(id));
				} catch (SQLException ex) {
					ex.printStackTrace();
				}

				updateModelPanel();
			}
		} else if (e.getSource() == agentButton) {
			Integer id;

			if (set.getAgent() != null) {
				id = DBKernel.openAgentDBWindow(set.getAgent().getId());
			} else {
				id = DBKernel.openAgentDBWindow(null);
			}

			if (id != null) {
				String name = DBKernel.getValue("Agenzien", "ID", id + "",
						"Agensname") + "";

				set.setAgent(new AgentXml(id, name, null, DBKernel
						.getLocalDBUUID()));
				agentButton.setText(name);
			}
		} else if (e.getSource() == matrixButton) {
			Integer id;

			if (set.getMatrix() != null) {
				id = DBKernel.openMatrixDBWindow(set.getMatrix().getId());
			} else {
				id = DBKernel.openMatrixDBWindow(null);
			}

			if (id != null) {
				String name = DBKernel.getValue("Matrices", "ID", id + "",
						"Matrixname") + "";

				set.setMatrix(new MatrixXml(id, name, null, DBKernel
						.getLocalDBUUID()));
				matrixButton.setText(name);
			}
		} else if (e.getSource() == addLiteratureButton) {
			Integer id = DBKernel.openLiteratureDBWindow(null);
			Set<Integer> ids = new LinkedHashSet<Integer>();

			for (LiteratureItem item : set.getLiterature()) {
				ids.add(item.getId());
			}

			if (id != null && !ids.contains(id)) {
				LiteratureItem l = DBUtilities.getLiteratureItem(id);

				set.getLiterature().add(l);				
				literatureList.setListData(set.getLiterature().toArray(
						new LiteratureItem[0]));
			}
		} else if (e.getSource() == removeLiteratureButton) {
			if (literatureList.getSelectedIndices().length > 0) {
				int[] indices = literatureList.getSelectedIndices();

				Arrays.sort(indices);

				for (int i = indices.length - 1; i >= 0; i--) {
					set.getLiterature().remove(indices[i]);
				}

				literatureList.setListData(set.getLiterature().toArray(
						new LiteratureItem[0]));
			}
		} else {
			for (String param : secModelButtons.keySet()) {
				if (e.getSource() == secModelButtons.get(param)) {
					KnimeTuple secModelTuple = set.getSecModelTuples().get(
							param);
					Integer id;

					if (secModelTuple != null) {
						id = DBKernel
								.openSecModelDBWindow(((CatalogModelXml) secModelTuple
										.getPmmXml(
												Model2Schema.ATT_MODELCATALOG)
										.get(0)).getId());
					} else {
						id = DBKernel.openSecModelDBWindow(null);
					}

					if (id != null) {
						Bfrdb db = new Bfrdb(DBKernel.getLocalConn(true));

						try {
							addSecModel(param, db.getSecModelById(id));
						} catch (SQLException ex) {
							ex.printStackTrace();
						}

						updateModelPanel();
					}

					break;
				}
			}

			for (String param : secModelReloadButtons.keySet()) {
				if (e.getSource() == secModelReloadButtons.get(param)) {
					KnimeTuple secModelTuple = set.getSecModelTuples().get(
							param);
					Integer id = ((CatalogModelXml) secModelTuple.getPmmXml(
							Model2Schema.ATT_MODELCATALOG).get(0)).getId();

					if (id != null) {
						Bfrdb db = new Bfrdb(DBKernel.getLocalConn(true));

						try {
							addSecModel(param, db.getSecModelById(id));
						} catch (SQLException ex) {
							ex.printStackTrace();
						}

						updateModelPanel();
					}

					break;
				}
			}

			for (String value : agentButtons.keySet()) {
				if (e.getSource() == agentButtons.get(value)) {
					Integer id;

					if (set.getAgentMappings().get(value) != null) {
						id = DBKernel.openAgentDBWindow(set.getAgentMappings()
								.get(value).getId());
					} else {
						id = DBKernel.openAgentDBWindow(null);
					}

					if (id != null) {
						String name = DBKernel.getValue("Agenzien", "ID", id
								+ "", "Agensname")
								+ "";

						agentButtons.get(value).setText(name);
						set.getAgentMappings().put(
								value,
								new AgentXml(id, name, null, DBKernel
										.getLocalDBUUID()));
					}

					break;
				}
			}

			for (String value : matrixButtons.keySet()) {
				if (e.getSource() == matrixButtons.get(value)) {
					Integer id;

					if (set.getMatrixMappings().get(value) != null) {
						id = DBKernel.openMatrixDBWindow(set
								.getMatrixMappings().get(value).getId());
					} else {
						id = DBKernel.openMatrixDBWindow(null);
					}

					if (id != null) {
						String name = DBKernel.getValue("Matrices", "ID", id
								+ "", "Matrixname")
								+ "";

						matrixButtons.get(value).setText(name);
						set.getMatrixMappings().put(
								value,
								new MatrixXml(id, name, null, DBKernel
										.getLocalDBUUID()));
					}

					break;
				}
			}

			for (String column : columnButtons.keySet()) {
				if (e.getSource() == columnButtons.get(column)) {
					Integer id;

					if (set.getColumnMappings().get(column) instanceof MiscXml) {
						id = DBKernel.openMiscDBWindow(((MiscXml) set
								.getColumnMappings().get(column)).getId());
					} else {
						id = DBKernel.openMiscDBWindow(null);
					}

					if (id != null) {
						String name = DBKernel.getValue("SonstigeParameter",
								"ID", id + "", "Parameter") + "";
						String description = DBKernel.getValue(
								"SonstigeParameter", "ID", id + "",
								"Beschreibung")
								+ "";
						List<String> categoryIDs = Arrays.asList(DBKernel
								.getValue("SonstigeParameter", "ID", id + "",
										"Kategorie").toString().split(","));
						String unit = null;

						if (!categoryIDs.isEmpty()) {
							unit = Categories.getCategory(categoryIDs.get(0))
									.getStandardUnit();
						}

						columnButtons.get(column).setText(name);
						set.getColumnMappings().put(
								column,
								new MiscXml(id, name, description, null,
										categoryIDs, unit, DBKernel
												.getLocalDBUUID()));
						updateColumnsPanel();
					}

					break;
				}
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() != ItemEvent.SELECTED) {
			return;
		}

		if (e.getSource() == sheetBox) {
			try {
				set.setSheetName((String) sheetBox.getSelectedItem());
				fileColumnList = xlsReader.getColumns(
						new File(filePanel.getFileName()), set.getSheetName());
			} catch (Exception ex) {
				fileColumnList = new ArrayList<String>();
			}

			updateModelPanel();
			updateColumnsPanel();
			updateAgentPanel();
			updateMatrixPanel();
			mainPanel.revalidate();
		} else if (e.getSource() == agentBox) {
			set.setAgentColumn((String) agentBox.getSelectedItem());
			updateAgentPanel();
		} else if (e.getSource() == matrixBox) {
			set.setMatrixColumn((String) matrixBox.getSelectedItem());
			updateMatrixPanel();
		} else {
			for (String param : modelBoxes.keySet()) {
				if (e.getSource() == modelBoxes.get(param)) {
					JComboBox<String> box = modelBoxes.get(param);

					if (box.getSelectedItem().equals(DO_NOT_USE)) {
						set.getModelMappings().remove(param);
					} else if (box.getSelectedItem()
							.equals(USE_SECONDARY_MODEL)) {
						set.getModelMappings().put(param, null);
					} else {
						set.getModelMappings().put(param,
								(String) box.getSelectedItem());
					}

					updateModelPanel();
					break;
				}
			}

			for (String param1 : secModelBoxes.keySet()) {
				for (String param2 : secModelBoxes.get(param1).keySet()) {
					if (e.getSource() == secModelBoxes.get(param1).get(param2)) {
						JComboBox<String> box = secModelBoxes.get(param1).get(
								param2);

						if (box.getSelectedItem().equals(DO_NOT_USE)) {
							set.getSecModelMappings().get(param1)
									.put(param2, null);
						} else {
							set.getSecModelMappings()
									.get(param1)
									.put(param2, (String) box.getSelectedItem());
						}

						break;
					}
				}
			}

			for (String param1 : secMinBoxes.keySet()) {
				for (String param2 : secMinBoxes.get(param1).keySet()) {
					if (e.getSource() == secMinBoxes.get(param1).get(param2)) {
						JComboBox<String> box = secMinBoxes.get(param1).get(
								param2);

						if (box.getSelectedItem().equals(DO_NOT_USE)) {
							set.getSecModelIndepMins().get(param1)
									.put(param2, null);
						} else {
							set.getSecModelIndepMins()
									.get(param1)
									.put(param2, (String) box.getSelectedItem());
						}

						break;
					}
				}
			}

			for (String param1 : secMaxBoxes.keySet()) {
				for (String param2 : secMaxBoxes.get(param1).keySet()) {
					if (e.getSource() == secMaxBoxes.get(param1).get(param2)) {
						JComboBox<String> box = secMaxBoxes.get(param1).get(
								param2);

						if (box.getSelectedItem().equals(DO_NOT_USE)) {
							set.getSecModelIndepMaxs().get(param1)
									.put(param2, null);
						} else {
							set.getSecModelIndepMaxs()
									.get(param1)
									.put(param2, (String) box.getSelectedItem());
						}

						break;
					}
				}
			}

			for (String param1 : secUnitBoxes.keySet()) {
				for (String param2 : secUnitBoxes.get(param1).keySet()) {
					if (e.getSource() == secUnitBoxes.get(param1).get(param2)) {
						JComboBox<String> box = secUnitBoxes.get(param1).get(
								param2);

						set.getSecModelIndepUnits().get(param1)
								.put(param2, (String) box.getSelectedItem());
						break;
					}
				}
			}

			for (String column : columnBoxes.keySet()) {
				if (e.getSource() == columnBoxes.get(column)) {
					String selected = (String) columnBoxes.get(column)
							.getSelectedItem();

					if (selected.equals(MdInfoXml.ATT_COMMENT)
							|| selected
									.equals(AttributeUtilities.AGENT_DETAILS)
							|| selected
									.equals(AttributeUtilities.MATRIX_DETAILS)) {
						set.getColumnMappings().put(column, selected);
					} else if (selected
							.equals(AttributeUtilities.ATT_TEMPERATURE)) {
						set.getColumnMappings().put(
								column,
								new MiscXml(
										AttributeUtilities.ATT_TEMPERATURE_ID,
										AttributeUtilities.ATT_TEMPERATURE,
										null, null, Arrays.asList(Categories
												.getTempCategory().getName()), Categories
												.getTempCategory()
												.getStandardUnit()));
					} else if (selected.equals(AttributeUtilities.ATT_PH)) {
						set.getColumnMappings().put(
								column,
								new MiscXml(AttributeUtilities.ATT_PH_ID,
										AttributeUtilities.ATT_PH, null, null,
										Arrays.asList(Categories.getPhCategory().getName()),
										Categories.getPhUnit()));
					} else if (selected.equals(AttributeUtilities.ATT_AW)) {
						set.getColumnMappings().put(
								column,
								new MiscXml(AttributeUtilities.ATT_AW_ID,
										AttributeUtilities.ATT_AW, null, null,
										Arrays.asList(Categories.getAwCategory().getName()),
										Categories.getAwUnit()));
					} else if (selected.equals(OTHER_PARAMETER)) {
						set.getColumnMappings().put(column, null);
					} else if (selected.equals(DO_NOT_USE)) {
						set.getColumnMappings().remove(column);
					}

					updateColumnsPanel();
					break;
				}
			}

			for (String column : columnUnitBoxes.keySet()) {
				if (e.getSource() == columnUnitBoxes.get(column)) {
					String unit = (String) columnUnitBoxes.get(column)
							.getSelectedItem();
					MiscXml condition = (MiscXml) set.getColumnMappings().get(
							column);

					condition.setUnit(unit);

					break;
				}
			}
		}
	}

	@Override
	public void fileChanged(FilePanel source) {
		set.setFileName(filePanel.getFileName());

		try {
			fileSheetList = xlsReader.getSheets(new File(set.getFileName()));
		} catch (Exception e) {
			fileSheetList = new ArrayList<String>();
		}

		sheetBox.removeItemListener(this);
		sheetBox.removeAllItems();

		for (String sheet : fileSheetList) {
			sheetBox.addItem(sheet);
		}

		if (!fileSheetList.isEmpty()) {
			sheetBox.setSelectedIndex(0);
		}

		set.setSheetName((String) sheetBox.getSelectedItem());
		sheetBox.addItemListener(this);

		try {
			fileColumnList = xlsReader.getColumns(new File(set.getFileName()),
					(String) sheetBox.getSelectedItem());
		} catch (Exception e) {
			fileColumnList = new ArrayList<String>();
		}

		updateModelPanel();
		updateColumnsPanel();
		updateAgentPanel();
		updateMatrixPanel();
		mainPanel.revalidate();
	}

	private void updateModelPanel() {
		modelBoxes.clear();
		secModelButtons.clear();
		secModelReloadButtons.clear();
		secModelBoxes.clear();
		secMinBoxes.clear();
		secMaxBoxes.clear();
		secUnitBoxes.clear();

		if (set.getModelTuple() != null) {
			modelButton = new JButton(((CatalogModelXml) set.getModelTuple()
					.getPmmXml(Model1Schema.ATT_MODELCATALOG).get(0)).getName());
			modelReloadButton = new JButton(RELOAD);
			modelReloadButton.setEnabled(true);
		} else {
			modelButton = new JButton(SELECT);
			modelReloadButton = new JButton(RELOAD);
			modelReloadButton.setEnabled(false);
		}

		modelButton.addActionListener(this);
		modelReloadButton.addActionListener(this);

		JPanel northPanel = new JPanel();
		int row = 0;

		JPanel modelButtonPanel = new JPanel();

		modelButtonPanel.setLayout(new BorderLayout());
		modelButtonPanel.add(modelButton, BorderLayout.WEST);
		modelButtonPanel.add(modelReloadButton, BorderLayout.EAST);

		northPanel.setLayout(new GridBagLayout());
		northPanel.add(new JLabel("Primary Model:"), createConstraints(0, row));
		northPanel.add(modelButtonPanel, createConstraints(1, row));
		row++;

		if (set.getModelTuple() != null) {
			List<String> options = new ArrayList<String>();

			options.add(DO_NOT_USE);
			options.add(USE_SECONDARY_MODEL);
			options.addAll(fileColumnList);

			for (PmmXmlElementConvertable el : set.getModelTuple()
					.getPmmXml(Model1Schema.ATT_PARAMETER).getElementSet()) {
				ParamXml element = (ParamXml) el;
				JComboBox<String> box = new JComboBox<String>(
						options.toArray(new String[0]));

				if (!set.getModelMappings().containsKey(element.getName())) {
					UI.select(box, DO_NOT_USE);
				} else if (set.getModelMappings().get(element.getName()) == null) {
					UI.select(box, USE_SECONDARY_MODEL);
				} else {
					UI.select(box, set.getModelMappings()
							.get(element.getName()));
				}

				box.addItemListener(this);
				modelBoxes.put(element.getName(), box);

				northPanel.add(new JLabel(element.getName() + ":"),
						createConstraints(0, row));
				northPanel.add(box, createConstraints(1, row));
				row++;
			}

			for (PmmXmlElementConvertable el : set.getModelTuple()
					.getPmmXml(Model1Schema.ATT_PARAMETER).getElementSet()) {
				ParamXml element = (ParamXml) el;

				if (!set.getModelMappings().containsKey(element.getName())
						|| set.getModelMappings().get(element.getName()) != null) {
					continue;
				}

				KnimeTuple secModelTuple = set.getSecModelTuples().get(
						element.getName());
				JButton secButton;
				JButton secReloadButton;
				JPanel secButtonPanel = new JPanel();

				if (secModelTuple != null) {
					secButton = new JButton(
							((CatalogModelXml) secModelTuple.getPmmXml(
									Model2Schema.ATT_MODELCATALOG).get(0))
									.getName());
					secReloadButton = new JButton(RELOAD);
					secReloadButton.setEnabled(true);
				} else {
					secButton = new JButton(SELECT);
					secReloadButton = new JButton(RELOAD);
					secReloadButton.setEnabled(false);
				}

				secButtonPanel.setLayout(new BorderLayout());
				secButtonPanel.add(secButton, BorderLayout.WEST);
				secButtonPanel.add(secReloadButton, BorderLayout.EAST);

				secButton.addActionListener(this);
				secReloadButton.addActionListener(this);
				secModelButtons.put(element.getName(), secButton);
				secModelReloadButtons.put(element.getName(), secReloadButton);
				northPanel.add(new JLabel(element.getName() + ":"),
						createConstraints(0, row));
				northPanel.add(secButtonPanel, createConstraints(1, row));
				row++;

				if (secModelTuple != null) {
					List<String> secOptions = new ArrayList<String>();

					secOptions.add(DO_NOT_USE);
					secOptions.addAll(fileColumnList);

					secModelBoxes.put(element.getName(),
							new LinkedHashMap<String, JComboBox<String>>());
					secMinBoxes.put(element.getName(),
							new LinkedHashMap<String, JComboBox<String>>());
					secMaxBoxes.put(element.getName(),
							new LinkedHashMap<String, JComboBox<String>>());
					secUnitBoxes.put(element.getName(),
							new LinkedHashMap<String, JComboBox<String>>());

					for (PmmXmlElementConvertable el2 : secModelTuple
							.getPmmXml(Model2Schema.ATT_PARAMETER)
							.getElementSet()) {
						ParamXml element2 = (ParamXml) el2;
						JComboBox<String> box = new JComboBox<String>(
								secOptions.toArray(new String[0]));
						Map<String, String> mappings = set
								.getSecModelMappings().get(element.getName());

						if (mappings.get(element2.getName()) == null) {
							UI.select(box, DO_NOT_USE);
						} else {
							UI.select(box, mappings.get(element2.getName()));
						}

						box.addItemListener(this);
						secModelBoxes.get(element.getName()).put(
								element2.getName(), box);

						northPanel.add(new JLabel(element2.getName() + ":"),
								createConstraints(0, row));
						northPanel.add(box, createConstraints(1, row));
						row++;
					}

					for (PmmXmlElementConvertable el2 : secModelTuple
							.getPmmXml(Model2Schema.ATT_INDEPENDENT)
							.getElementSet()) {
						IndepXml indep = (IndepXml) el2;

						if (indep.getUnit() == null) {
							continue;
						}

						JComboBox<String> minBox = new JComboBox<String>(
								secOptions.toArray(new String[0]));
						JComboBox<String> maxBox = new JComboBox<String>(
								secOptions.toArray(new String[0]));
						JComboBox<String> unitBox = new JComboBox<String>(Categories
								.getCategory(indep.getCategory()).getAllUnits()
								.toArray(new String[0]));
						Map<String, String> mins = set.getSecModelIndepMins()
								.get(element.getName());
						Map<String, String> maxs = set.getSecModelIndepMaxs()
								.get(element.getName());
						Map<String, String> units = set.getSecModelIndepUnits()
								.get(element.getName());

						if (mins.get(indep.getName()) == null) {
							UI.select(minBox, DO_NOT_USE);
						} else {
							UI.select(minBox, mins.get(indep.getName()));
						}

						if (maxs.get(indep.getName()) == null) {
							UI.select(maxBox, DO_NOT_USE);
						} else {
							UI.select(maxBox, maxs.get(indep.getName()));
						}

						if (units.get(indep.getName()) == null) {
							UI.select(unitBox, null);
						} else {
							UI.select(unitBox, units.get(indep.getName()));
						}

						minBox.addItemListener(this);
						maxBox.addItemListener(this);
						unitBox.addItemListener(this);
						secMinBoxes.get(element.getName()).put(indep.getName(),
								minBox);
						secMaxBoxes.get(element.getName()).put(indep.getName(),
								maxBox);
						secUnitBoxes.get(element.getName()).put(
								indep.getName(), unitBox);

						northPanel.add(new JLabel("Min " + indep.getName()
								+ ":"), createConstraints(0, row));
						northPanel.add(minBox, createConstraints(1, row));
						row++;
						northPanel.add(new JLabel("Max " + indep.getName()
								+ ":"), createConstraints(0, row));
						northPanel.add(maxBox, createConstraints(1, row));
						row++;
						northPanel.add(new JLabel(indep.getName() + " Unit:"),
								createConstraints(0, row));
						northPanel.add(unitBox, createConstraints(1, row));
						row++;
					}
				}
			}
		}

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(northPanel, BorderLayout.NORTH);

		modelPanel.removeAll();
		modelPanel.add(new JScrollPane(panel), BorderLayout.CENTER);
		modelPanel.revalidate();
	}

	private void updateAgentPanel() {
		agentButtons.clear();
		agentBox = new JComboBox<String>(new String[] { DO_NOT_USE, OTHER_PARAMETER });
		agentButton = new JButton(OTHER_PARAMETER);

		for (String column : fileColumnList) {
			agentBox.addItem(column);
		}

		UI.select(agentBox, set.getAgentColumn());

		if (set.getAgent() != null) {
			agentButton.setText(set.getAgent().getName());
		} else {
			agentButton.setText(OTHER_PARAMETER);
		}

		agentBox.addItemListener(this);
		agentButton.addActionListener(this);

		JPanel northPanel = new JPanel();

		northPanel.setLayout(new GridBagLayout());
		northPanel.add(new JLabel("XLS Column:"), createConstraints(0, 0));
		northPanel.add(agentBox, createConstraints(1, 0));

		if (agentBox.getSelectedItem().equals(DO_NOT_USE)) {
			// Do nothing
		} else if (agentBox.getSelectedItem().equals(OTHER_PARAMETER)) {
			northPanel.add(agentButton, createConstraints(1, 1));
		} else {
			int row = 1;
			String column = (String) agentBox.getSelectedItem();
			Set<String> values;

			try {
				values = xlsReader.getValuesInColumn(
						new File(filePanel.getFileName()),
						(String) sheetBox.getSelectedItem(), column);
			} catch (Exception e) {
				values = new LinkedHashSet<String>();
			}

			for (String value : values) {
				JButton button = new JButton();

				if (set.getAgentMappings().get(value) != null) {
					button.setText(set.getAgentMappings().get(value).getName());
				} else {
					button.setText(OTHER_PARAMETER);
				}

				button.addActionListener(this);
				agentButtons.put(value, button);

				northPanel.add(new JLabel(value + ":"),
						createConstraints(0, row));
				northPanel.add(button, createConstraints(1, row));
				row++;
			}
		}

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(northPanel, BorderLayout.NORTH);

		agentPanel.removeAll();
		agentPanel.add(new JScrollPane(panel), BorderLayout.CENTER);
		agentPanel.revalidate();
	}

	private void updateMatrixPanel() {
		matrixButtons.clear();
		matrixBox = new JComboBox<String>(
				new String[] { DO_NOT_USE, OTHER_PARAMETER });
		matrixButton = new JButton(OTHER_PARAMETER);

		for (String column : fileColumnList) {
			matrixBox.addItem(column);
		}

		UI.select(matrixBox, set.getMatrixColumn());

		if (set.getMatrix() != null) {
			matrixButton.setText(set.getMatrix().getName());
		} else {
			matrixButton.setText(OTHER_PARAMETER);
		}

		matrixBox.addItemListener(this);
		matrixButton.addActionListener(this);

		JPanel northPanel = new JPanel();

		northPanel.setLayout(new GridBagLayout());
		northPanel.add(new JLabel("XLS Column:"), createConstraints(0, 0));
		northPanel.add(matrixBox, createConstraints(1, 0));

		if (matrixBox.getSelectedItem().equals(DO_NOT_USE)) {
			// Do nothing
		} else if (matrixBox.getSelectedItem().equals(OTHER_PARAMETER)) {
			northPanel.add(matrixButton, createConstraints(1, 1));
		} else {
			int row = 1;
			String column = (String) matrixBox.getSelectedItem();
			Set<String> values;

			try {
				values = xlsReader.getValuesInColumn(
						new File(filePanel.getFileName()),
						(String) sheetBox.getSelectedItem(), column);
			} catch (Exception e) {
				values = new LinkedHashSet<String>();
			}

			for (String value : values) {
				JButton button = new JButton();

				if (set.getMatrixMappings().get(value) != null) {
					button.setText(set.getMatrixMappings().get(value).getName());
				} else {
					button.setText(OTHER_PARAMETER);
				}

				button.addActionListener(this);
				matrixButtons.put(value, button);

				northPanel.add(new JLabel(value + ":"),
						createConstraints(0, row));
				northPanel.add(button, createConstraints(1, row));
				row++;
			}
		}

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(northPanel, BorderLayout.NORTH);

		matrixPanel.removeAll();
		matrixPanel.add(new JScrollPane(panel), BorderLayout.CENTER);
		matrixPanel.revalidate();
	}

	private void updateColumnsPanel() {
		if (!fileColumnList.isEmpty()) {
			columnBoxes.clear();
			columnButtons.clear();
			columnUnitBoxes.clear();

			JPanel northPanel = new JPanel();
			int row = 0;

			northPanel.setLayout(new GridBagLayout());

			for (String column : fileColumnList) {
				JComboBox<String> box = new JComboBox<String>(new String[] {
						DO_NOT_USE, OTHER_PARAMETER, MdInfoXml.ATT_COMMENT,
						AttributeUtilities.AGENT_DETAILS,
						AttributeUtilities.MATRIX_DETAILS,
						AttributeUtilities.ATT_TEMPERATURE,
						AttributeUtilities.ATT_PH, AttributeUtilities.ATT_AW });
				JButton button = new JButton();

				if (set.getColumnMappings().containsKey(column)) {
					Object mapping = set.getColumnMappings().get(column);

					if (mapping == null) {
						UI.select(box, OTHER_PARAMETER);
						button.setEnabled(true);
						button.setText(OTHER_PARAMETER);
					} else if (mapping instanceof String) {
						UI.select(box, mapping);
						button.setEnabled(false);
						button.setText(OTHER_PARAMETER);
					} else if (mapping instanceof MiscXml) {
						int id = ((MiscXml) mapping).getId();

						if (id == AttributeUtilities.ATT_TEMPERATURE_ID) {
							UI.select(box, AttributeUtilities.ATT_TEMPERATURE);
							button.setEnabled(false);
							button.setText(OTHER_PARAMETER);
						} else if (id == AttributeUtilities.ATT_PH_ID) {
							UI.select(box, AttributeUtilities.ATT_PH);
							button.setEnabled(false);
							button.setText(OTHER_PARAMETER);
						} else if (id == AttributeUtilities.ATT_AW_ID) {
							UI.select(box, AttributeUtilities.ATT_AW);
							button.setEnabled(false);
							button.setText(OTHER_PARAMETER);
						} else {
							UI.select(box, OTHER_PARAMETER);
							button.setEnabled(true);
							button.setText(((MiscXml) mapping).getName());
						}
					}
				} else {
					UI.select(box, DO_NOT_USE);
					button.setEnabled(false);
					button.setText(OTHER_PARAMETER);
				}

				box.addItemListener(this);
				button.addActionListener(this);
				columnBoxes.put(column, box);
				columnButtons.put(column, button);

				northPanel.add(new JLabel(column + ":"),
						createConstraints(0, row));
				northPanel.add(box, createConstraints(1, row));
				northPanel.add(button, createConstraints(2, row));

				if (set.getColumnMappings().get(column) instanceof MiscXml) {
					MiscXml condition = (MiscXml) set.getColumnMappings().get(
							column);
					List<String> allUnits = new ArrayList<String>();

					for (String cat : condition.getCategories()) {
						allUnits.addAll(Categories.getCategory(cat)
								.getAllUnits());
					}

					JComboBox<String> unitBox = new JComboBox<String>(
							allUnits.toArray(new String[0]));

					UI.select(unitBox, condition.getUnit());
					unitBox.addItemListener(this);
					columnUnitBoxes.put(column, unitBox);
					northPanel.add(unitBox, createConstraints(3, row));
				}

				row++;
			}

			JPanel panel = new JPanel();

			panel.setLayout(new BorderLayout());
			panel.add(northPanel, BorderLayout.NORTH);

			columnsPanel.removeAll();
			columnsPanel.add(new JScrollPane(panel), BorderLayout.CENTER);
			columnsPanel.revalidate();
		} else {
			columnsPanel.removeAll();
			columnsPanel.add(noLabel, BorderLayout.CENTER);
			columnsPanel.revalidate();
			columnButtons.clear();
		}
	}

	private GridBagConstraints createConstraints(int x, int y) {
		return new GridBagConstraints(x, y, 1, 1, 0, 0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0);
	}

	private void cleanMaps() {
		Map<String, String> newModelMappings = new LinkedHashMap<String, String>();
		Map<String, KnimeTuple> newSecModelTuples = new LinkedHashMap<String, KnimeTuple>();
		Map<String, Map<String, String>> newSecModelMappings = new LinkedHashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> newSecModelIndepMins = new LinkedHashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> newSecModelIndepMaxs = new LinkedHashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> newSecModelIndepUnits = new LinkedHashMap<String, Map<String, String>>();
		Map<String, AgentXml> newAgentMappings = new LinkedHashMap<String, AgentXml>();
		Map<String, MatrixXml> newMatrixMappings = new LinkedHashMap<String, MatrixXml>();
		Map<String, Object> newColumnMappings = new LinkedHashMap<String, Object>();

		for (String param : modelBoxes.keySet()) {
			if (set.getModelMappings().containsKey(param)) {
				String value = set.getModelMappings().get(param);

				if (value == null || fileColumnList.contains(value)) {
					newModelMappings.put(param,
							set.getModelMappings().get(param));
				}
			}
		}

		for (String param : secModelButtons.keySet()) {
			if (set.getSecModelTuples().containsKey(param)) {
				newSecModelTuples
						.put(param, set.getSecModelTuples().get(param));
			}
		}

		for (String param1 : secModelBoxes.keySet()) {
			newSecModelMappings
					.put(param1, new LinkedHashMap<String, String>());

			for (String param2 : secModelBoxes.get(param1).keySet()) {
				if (set.getSecModelMappings().containsKey(param1)
						&& set.getSecModelMappings().get(param1)
								.containsKey(param2)) {
					newSecModelMappings.get(param1).put(param2,
							set.getSecModelMappings().get(param1).get(param2));
				}
			}
		}

		for (String param1 : secMinBoxes.keySet()) {
			newSecModelIndepMins.put(param1,
					new LinkedHashMap<String, String>());

			for (String param2 : secMinBoxes.get(param1).keySet()) {
				if (set.getSecModelIndepMins().containsKey(param1)
						&& set.getSecModelIndepMins().get(param1)
								.containsKey(param2)) {
					newSecModelIndepMins.get(param1).put(param2,
							set.getSecModelIndepMins().get(param1).get(param2));
				}
			}
		}

		for (String param1 : secMaxBoxes.keySet()) {
			newSecModelIndepMaxs.put(param1,
					new LinkedHashMap<String, String>());

			for (String param2 : secMaxBoxes.get(param1).keySet()) {
				if (set.getSecModelIndepMaxs().containsKey(param1)
						&& set.getSecModelIndepMaxs().get(param1)
								.containsKey(param2)) {
					newSecModelIndepMaxs.get(param1).put(param2,
							set.getSecModelIndepMaxs().get(param1).get(param2));
				}
			}
		}

		for (String param1 : secUnitBoxes.keySet()) {
			newSecModelIndepUnits.put(param1,
					new LinkedHashMap<String, String>());

			for (String param2 : secUnitBoxes.get(param1).keySet()) {
				if (set.getSecModelIndepUnits().containsKey(param1)
						&& set.getSecModelIndepUnits().get(param1)
								.containsKey(param2)) {
					newSecModelIndepUnits.get(param1)
							.put(param2,
									set.getSecModelIndepUnits().get(param1)
											.get(param2));
				}
			}
		}

		for (String agent : agentButtons.keySet()) {
			if (set.getAgentMappings().containsKey(agent)) {
				newAgentMappings.put(agent, set.getAgentMappings().get(agent));
			}
		}

		for (String matrix : matrixButtons.keySet()) {
			if (set.getMatrixMappings().containsKey(matrix)) {
				newMatrixMappings.put(matrix,
						set.getMatrixMappings().get(matrix));
			}
		}

		for (String column : columnBoxes.keySet()) {
			if (set.getColumnMappings().containsKey(column)) {
				newColumnMappings.put(column,
						set.getColumnMappings().get(column));
			}
		}

		set.setModelMappings(newModelMappings);
		set.setSecModelTuples(newSecModelTuples);
		set.setSecModelMappings(newSecModelMappings);
		set.setSecModelIndepMins(newSecModelIndepMins);
		set.setSecModelIndepMaxs(newSecModelIndepMaxs);
		set.setSecModelIndepUnits(newSecModelIndepUnits);
		set.setAgentMappings(newAgentMappings);
		set.setMatrixMappings(newMatrixMappings);
		set.setColumnMappings(newColumnMappings);
	}

	private void addSecModel(String param, KnimeTuple tuple) {
		set.getSecModelTuples().put(param, tuple);

		if (!set.getSecModelMappings().containsKey(param)) {
			set.getSecModelMappings().put(param,
					new LinkedHashMap<String, String>());
		}

		if (!set.getSecModelIndepMins().containsKey(param)) {
			set.getSecModelIndepMins().put(param,
					new LinkedHashMap<String, String>());
		}

		if (!set.getSecModelIndepMaxs().containsKey(param)) {
			set.getSecModelIndepMaxs().put(param,
					new LinkedHashMap<String, String>());
		}

		if (!set.getSecModelIndepUnits().containsKey(param)) {
			set.getSecModelIndepUnits().put(param,
					new LinkedHashMap<String, String>());
		}
	}
}
