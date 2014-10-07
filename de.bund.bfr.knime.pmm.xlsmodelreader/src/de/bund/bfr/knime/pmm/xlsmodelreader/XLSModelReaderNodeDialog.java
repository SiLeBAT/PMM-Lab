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
import de.bund.bfr.knime.pmm.common.DepXml;
import de.bund.bfr.knime.pmm.common.IndepXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MdInfoXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.ParamXml;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.XLSReader;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.knime.pmm.common.ui.FilePanel;
import de.bund.bfr.knime.pmm.common.ui.FilePanel.FileListener;
import de.bund.bfr.knime.pmm.common.ui.UI;
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
	private JComboBox<String> depUnitBox;
	private JComboBox<String> indepMinBox;
	private JComboBox<String> indepMaxBox;
	private JComboBox<String> indepUnitBox;
	private Map<String, JButton> secModelButtons;
	private Map<String, JButton> secModelReloadButtons;
	private Map<String, Map<String, JComboBox<String>>> secModelBoxes;
	private Map<String, Map<String, JComboBox<String>>> secMinBoxes;
	private Map<String, Map<String, JComboBox<String>>> secMaxBoxes;
	private Map<String, Map<String, JComboBox<String>>> secCategoryBoxes;
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
		sheetBox = new JComboBox<>();
		sheetBox.addItemListener(this);
		fileSheetList = new ArrayList<>();
		fileColumnList = new ArrayList<>();

		addLiteratureButton = new JButton("Add");
		addLiteratureButton.addActionListener(this);
		removeLiteratureButton = new JButton("Remove");
		removeLiteratureButton.addActionListener(this);
		literatureList = new JList<>();

		noLabel = new JLabel();
		noLabel.setPreferredSize(new Dimension(100, 50));

		modelPanel = new JPanel();
		modelPanel.setBorder(BorderFactory.createTitledBorder("Models"));
		modelPanel.setLayout(new BorderLayout());
		modelPanel.add(noLabel, BorderLayout.CENTER);
		modelBoxes = new LinkedHashMap<>();
		depUnitBox = null;
		indepMinBox = null;
		indepMaxBox = null;
		indepUnitBox = null;
		secModelButtons = new LinkedHashMap<>();
		secModelReloadButtons = new LinkedHashMap<>();
		secModelBoxes = new LinkedHashMap<>();
		secMinBoxes = new LinkedHashMap<>();
		secMaxBoxes = new LinkedHashMap<>();
		secCategoryBoxes = new LinkedHashMap<>();
		secUnitBoxes = new LinkedHashMap<>();

		agentPanel = new JPanel();
		agentPanel.setBorder(BorderFactory
				.createTitledBorder(AttributeUtilities
						.getName(TimeSeriesSchema.ATT_AGENT)));
		agentPanel.setLayout(new BorderLayout());
		agentPanel.add(noLabel, BorderLayout.CENTER);
		agentButtons = new LinkedHashMap<>();

		matrixPanel = new JPanel();
		matrixPanel.setBorder(BorderFactory
				.createTitledBorder(AttributeUtilities
						.getName(TimeSeriesSchema.ATT_MATRIX)));
		matrixPanel.setLayout(new BorderLayout());
		matrixPanel.add(noLabel, BorderLayout.CENTER);
		matrixButtons = new LinkedHashMap<>();

		columnsPanel = new JPanel();
		columnsPanel.setBorder(BorderFactory
				.createTitledBorder("XLS Column -> PMM-Lab assignments"));
		columnsPanel.setLayout(new BorderLayout());
		columnsPanel.add(noLabel, BorderLayout.CENTER);
		columnBoxes = new LinkedHashMap<>();
		columnButtons = new LinkedHashMap<>();
		columnUnitBoxes = new LinkedHashMap<>();

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
			fileSheetList = new ArrayList<>();
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
			fileColumnList = new ArrayList<>();
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

		Set<Object> assignments = new LinkedHashSet<>();

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

		if (OTHER_PARAMETER.equals(set.getAgentColumn())
				|| DO_NOT_USE.equals(set.getAgentColumn())) {
			set.setAgentColumn(null);
		}

		if (OTHER_PARAMETER.equals(set.getMatrixColumn())
				|| DO_NOT_USE.equals(set.getMatrixColumn())) {
			set.setMatrixColumn(null);
		}

		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == modelButton) {
			Integer id;

			if (set.getModelTuple() != null) {
				id = DBKernel.openPrimModelDBWindow(
						modelButton,
						((CatalogModelXml) set.getModelTuple()
								.getPmmXml(Model1Schema.ATT_MODELCATALOG)
								.get(0)).getId());
			} else {
				id = DBKernel.openPrimModelDBWindow(modelButton, null);
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
				id = DBKernel.openAgentDBWindow(agentButton, set.getAgent()
						.getId());
			} else {
				id = DBKernel.openAgentDBWindow(agentButton, null);
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
				id = DBKernel.openMatrixDBWindow(matrixButton, set.getMatrix()
						.getId());
			} else {
				id = DBKernel.openMatrixDBWindow(matrixButton, null);
			}

			if (id != null) {
				String name = DBKernel.getValue("Matrices", "ID", id + "",
						"Matrixname") + "";

				set.setMatrix(new MatrixXml(id, name, null, DBKernel
						.getLocalDBUUID()));
				matrixButton.setText(name);
			}
		} else if (e.getSource() == addLiteratureButton) {
			Integer id = DBKernel.openLiteratureDBWindow(addLiteratureButton,
					null);
			Set<Integer> ids = new LinkedHashSet<>();

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
						id = DBKernel.openSecModelDBWindow(secModelButtons
								.get(param), ((CatalogModelXml) secModelTuple
								.getPmmXml(Model2Schema.ATT_MODELCATALOG)
								.get(0)).getId());
					} else {
						id = DBKernel.openSecModelDBWindow(
								secModelButtons.get(param), null);
					}

					if (id != null) {
						Bfrdb db = new Bfrdb(DBKernel.getLocalConn(true));

						try {
							set.getSecModelTuples().put(param,
									db.getSecModelById(id));
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
							set.getSecModelTuples().put(param,
									db.getSecModelById(id));
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
						id = DBKernel.openAgentDBWindow(
								agentButtons.get(value), set.getAgentMappings()
										.get(value).getId());
					} else {
						id = DBKernel.openAgentDBWindow(
								agentButtons.get(value), null);
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
						id = DBKernel
								.openMatrixDBWindow(matrixButtons.get(value),
										set.getMatrixMappings().get(value)
												.getId());
					} else {
						id = DBKernel.openMatrixDBWindow(
								matrixButtons.get(value), null);
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
						id = DBKernel.openMiscDBWindow(columnButtons
								.get(column), ((MiscXml) set
								.getColumnMappings().get(column)).getId());
					} else {
						id = DBKernel.openMiscDBWindow(
								columnButtons.get(column), null);
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
				fileColumnList = new ArrayList<>();
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
		} else if (e.getSource() == depUnitBox) {
			set.setModelDepUnit((String) depUnitBox.getSelectedItem());
		} else if (e.getSource() == indepMinBox) {
			set.setModelIndepMin((String) indepMinBox.getSelectedItem());
		} else if (e.getSource() == indepMaxBox) {
			set.setModelIndepMax((String) indepMaxBox.getSelectedItem());
		} else if (e.getSource() == indepUnitBox) {
			set.setModelIndepUnit((String) indepUnitBox.getSelectedItem());
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
					return;
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

						return;
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

						return;
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

						return;
					}
				}
			}

			for (String param1 : secCategoryBoxes.keySet()) {
				for (String param2 : secCategoryBoxes.get(param1).keySet()) {
					if (e.getSource() == secCategoryBoxes.get(param1).get(
							param2)) {
						JComboBox<String> box = secCategoryBoxes.get(param1)
								.get(param2);

						set.getSecModelIndepCategories().get(param1)
								.put(param2, (String) box.getSelectedItem());
						updateModelPanel();
						return;
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
						return;
					}
				}
			}

			for (String column : columnBoxes.keySet()) {
				if (e.getSource() == columnBoxes.get(column)) {
					String selected = (String) columnBoxes.get(column)
							.getSelectedItem();

					if (selected.equals(XLSReader.NAME_COLUMN)
							|| selected.equals(MdInfoXml.ATT_COMMENT)
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
												.getTempCategory().getName()),
										Categories.getTempCategory()
												.getStandardUnit()));
					} else if (selected.equals(AttributeUtilities.ATT_PH)) {
						set.getColumnMappings().put(
								column,
								new MiscXml(AttributeUtilities.ATT_PH_ID,
										AttributeUtilities.ATT_PH, null, null,
										Arrays.asList(Categories
												.getPhCategory().getName()),
										Categories.getPhUnit()));
					} else if (selected.equals(AttributeUtilities.ATT_AW)) {
						set.getColumnMappings().put(
								column,
								new MiscXml(AttributeUtilities.ATT_AW_ID,
										AttributeUtilities.ATT_AW, null, null,
										Arrays.asList(Categories
												.getAwCategory().getName()),
										Categories.getAwUnit()));
					} else if (selected.equals(OTHER_PARAMETER)) {
						set.getColumnMappings().put(column, null);
					} else if (selected.equals(DO_NOT_USE)) {
						set.getColumnMappings().remove(column);
					}

					updateColumnsPanel();
					return;
				}
			}

			for (String column : columnUnitBoxes.keySet()) {
				if (e.getSource() == columnUnitBoxes.get(column)) {
					String unit = (String) columnUnitBoxes.get(column)
							.getSelectedItem();
					MiscXml condition = (MiscXml) set.getColumnMappings().get(
							column);

					condition.setUnit(unit);
					return;
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
			fileSheetList = new ArrayList<>();
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
			fileColumnList = new ArrayList<>();
		}

		updateModelPanel();
		updateColumnsPanel();
		updateAgentPanel();
		updateMatrixPanel();
		mainPanel.revalidate();
	}

	private void updateModelPanel() {
		for (String param : set.getSecModelTuples().keySet()) {
			KnimeTuple tuple = set.getSecModelTuples().get(param);
			PmmXmlDoc indepXml = tuple.getPmmXml(Model2Schema.ATT_INDEPENDENT);

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

			if (!set.getSecModelIndepCategories().containsKey(param)) {
				set.getSecModelIndepCategories().put(param,
						new LinkedHashMap<String, String>());

				for (PmmXmlElementConvertable el : indepXml.getElementSet()) {
					IndepXml indep = (IndepXml) el;

					set.getSecModelIndepCategories().get(param)
							.put(indep.getName(), indep.getCategory());
				}
			}

			if (!set.getSecModelIndepUnits().containsKey(param)) {
				set.getSecModelIndepUnits().put(param,
						new LinkedHashMap<String, String>());

				for (PmmXmlElementConvertable el : indepXml.getElementSet()) {
					IndepXml indep = (IndepXml) el;

					set.getSecModelIndepUnits().get(param)
							.put(indep.getName(), indep.getUnit());
				}
			}
		}

		modelBoxes.clear();
		depUnitBox = null;
		indepMinBox = null;
		indepMaxBox = null;
		indepUnitBox = null;
		secModelButtons.clear();
		secModelReloadButtons.clear();
		secModelBoxes.clear();
		secMinBoxes.clear();
		secMaxBoxes.clear();
		secCategoryBoxes.clear();
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

		JPanel panel = new JPanel();
		int row = 0;

		JPanel modelButtonPanel = new JPanel();

		modelButtonPanel.setLayout(new BorderLayout());
		modelButtonPanel.add(modelButton, BorderLayout.WEST);
		modelButtonPanel.add(modelReloadButton, BorderLayout.EAST);

		panel.setLayout(new GridBagLayout());
		panel.add(new JLabel("Primary Model:"), createConstraints(0, row));
		panel.add(modelButtonPanel, createConstraints(1, row));
		row++;

		if (set.getModelTuple() != null) {
			List<String> paramOptions = new ArrayList<>();

			paramOptions.add(DO_NOT_USE);
			paramOptions.add(USE_SECONDARY_MODEL);
			paramOptions.addAll(fileColumnList);

			List<String> options = new ArrayList<>();

			options.add(DO_NOT_USE);
			options.addAll(fileColumnList);

			for (PmmXmlElementConvertable el : set.getModelTuple()
					.getPmmXml(Model1Schema.ATT_PARAMETER).getElementSet()) {
				ParamXml element = (ParamXml) el;
				JComboBox<String> box = new JComboBox<>(
						paramOptions.toArray(new String[0]));

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

				panel.add(new JLabel(element.getName() + ":"),
						createConstraints(0, row));
				panel.add(box, createConstraints(1, row));
				row++;
			}

			DepXml depXml = (DepXml) set.getModelTuple()
					.getPmmXml(Model1Schema.ATT_DEPENDENT).get(0);
			IndepXml indepXml = (IndepXml) set.getModelTuple()
					.getPmmXml(Model1Schema.ATT_INDEPENDENT).get(0);

			depUnitBox = new JComboBox<>(Categories
					.getCategory(depXml.getCategory()).getAllUnits()
					.toArray(new String[0]));
			indepMinBox = new JComboBox<>(options.toArray(new String[0]));
			indepMaxBox = new JComboBox<>(options.toArray(new String[0]));
			indepUnitBox = new JComboBox<>(Categories
					.getCategory(indepXml.getCategory()).getAllUnits()
					.toArray(new String[0]));

			if (set.getModelDepUnit() != null) {
				depUnitBox.setSelectedItem(set.getModelDepUnit());
			} else {
				depUnitBox.setSelectedItem(depXml.getUnit());
				set.setModelDepUnit(depXml.getUnit());
			}

			if (set.getModelIndepMin() != null) {
				indepMinBox.setSelectedItem(set.getModelIndepMin());
			} else {
				indepMinBox.setSelectedItem(DO_NOT_USE);
			}

			if (set.getModelIndepMax() != null) {
				indepMaxBox.setSelectedItem(set.getModelIndepMax());
			} else {
				indepMaxBox.setSelectedItem(DO_NOT_USE);
			}

			if (set.getModelIndepUnit() != null) {
				indepUnitBox.setSelectedItem(set.getModelIndepUnit());
			} else {
				indepUnitBox.setSelectedItem(indepXml.getUnit());
				set.setModelIndepUnit(indepXml.getUnit());
			}

			depUnitBox.addItemListener(this);
			indepMinBox.addItemListener(this);
			indepMaxBox.addItemListener(this);
			indepUnitBox.addItemListener(this);

			panel.add(new JLabel(depXml.getName() + " Unit:"),
					createConstraints(0, row));
			panel.add(depUnitBox, createConstraints(1, row));
			row++;
			panel.add(new JLabel(indepXml.getName() + " Min:"),
					createConstraints(0, row));
			panel.add(indepMinBox, createConstraints(1, row));
			row++;
			panel.add(new JLabel(indepXml.getName() + " Max:"),
					createConstraints(0, row));
			panel.add(indepMaxBox, createConstraints(1, row));
			row++;
			panel.add(new JLabel(indepXml.getName() + " Unit:"),
					createConstraints(0, row));
			panel.add(indepUnitBox, createConstraints(1, row));
			row++;

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
				panel.add(new JLabel(element.getName() + ":"),
						createConstraints(0, row));
				panel.add(secButtonPanel, createConstraints(1, row));
				row++;

				if (secModelTuple != null) {
					secModelBoxes.put(element.getName(),
							new LinkedHashMap<String, JComboBox<String>>());
					secMinBoxes.put(element.getName(),
							new LinkedHashMap<String, JComboBox<String>>());
					secMaxBoxes.put(element.getName(),
							new LinkedHashMap<String, JComboBox<String>>());
					secCategoryBoxes.put(element.getName(),
							new LinkedHashMap<String, JComboBox<String>>());
					secUnitBoxes.put(element.getName(),
							new LinkedHashMap<String, JComboBox<String>>());

					for (PmmXmlElementConvertable el2 : secModelTuple
							.getPmmXml(Model2Schema.ATT_PARAMETER)
							.getElementSet()) {
						ParamXml element2 = (ParamXml) el2;
						JComboBox<String> box = new JComboBox<>(
								options.toArray(new String[0]));
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

						panel.add(new JLabel(element2.getName() + ":"),
								createConstraints(0, row));
						panel.add(box, createConstraints(1, row));
						row++;
					}

					for (PmmXmlElementConvertable el2 : secModelTuple
							.getPmmXml(Model2Schema.ATT_INDEPENDENT)
							.getElementSet()) {
						IndepXml indep = (IndepXml) el2;

						if (indep.getUnit() == null) {
							continue;
						}

						Map<String, String> mins = set.getSecModelIndepMins()
								.get(element.getName());
						Map<String, String> maxs = set.getSecModelIndepMaxs()
								.get(element.getName());
						Map<String, String> categories = set
								.getSecModelIndepCategories().get(
										element.getName());
						Map<String, String> units = set.getSecModelIndepUnits()
								.get(element.getName());
						JComboBox<String> minBox = new JComboBox<>(
								options.toArray(new String[0]));
						JComboBox<String> maxBox = new JComboBox<>(
								options.toArray(new String[0]));
						JComboBox<String> categoryBox = new JComboBox<>(
								Categories.getAllCategories().toArray(
										new String[0]));
						JComboBox<String> unitBox = new JComboBox<>(Categories
								.getCategory(categories.get(indep.getName()))
								.getAllUnits().toArray(new String[0]));

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

						if (categories.get(indep.getName()) == null) {
							UI.select(categoryBox, null);
						} else {
							UI.select(categoryBox,
									categories.get(indep.getName()));
						}

						if (units.get(indep.getName()) == null) {
							UI.select(unitBox, null);
						} else {
							UI.select(unitBox, units.get(indep.getName()));
						}

						minBox.addItemListener(this);
						maxBox.addItemListener(this);
						categoryBox.addItemListener(this);
						unitBox.addItemListener(this);
						secMinBoxes.get(element.getName()).put(indep.getName(),
								minBox);
						secMaxBoxes.get(element.getName()).put(indep.getName(),
								maxBox);
						secCategoryBoxes.get(element.getName()).put(
								indep.getName(), categoryBox);
						secUnitBoxes.get(element.getName()).put(
								indep.getName(), unitBox);

						panel.add(new JLabel("Min " + indep.getName() + ":"),
								createConstraints(0, row));
						panel.add(minBox, createConstraints(1, row));
						row++;
						panel.add(new JLabel("Max " + indep.getName() + ":"),
								createConstraints(0, row));
						panel.add(maxBox, createConstraints(1, row));
						row++;
						panel.add(new JLabel(indep.getName() + " Category:"),
								createConstraints(0, row));
						panel.add(categoryBox, createConstraints(1, row));
						row++;
						panel.add(new JLabel(indep.getName() + " Unit:"),
								createConstraints(0, row));
						panel.add(unitBox, createConstraints(1, row));
						row++;
					}
				}
			}
		}

		modelPanel.removeAll();
		modelPanel.add(panel, BorderLayout.NORTH);
		modelPanel.revalidate();
	}

	private void updateAgentPanel() {
		List<String> items = new ArrayList<>();

		items.add(DO_NOT_USE);
		items.add(OTHER_PARAMETER);
		items.addAll(fileColumnList);

		agentButtons.clear();
		agentBox = new JComboBox<>(items.toArray(new String[0]));
		agentButton = new JButton(OTHER_PARAMETER);

		UI.select(agentBox, set.getAgentColumn());

		if (set.getAgent() != null) {
			agentButton.setText(set.getAgent().getName());
		} else {
			agentButton.setText(OTHER_PARAMETER);
		}

		agentBox.addItemListener(this);
		agentButton.addActionListener(this);

		JPanel panel = new JPanel();

		panel.setLayout(new GridBagLayout());
		panel.add(new JLabel("XLS Column:"), createConstraints(0, 0));
		panel.add(agentBox, createConstraints(1, 0));

		if (agentBox.getSelectedItem() == null
				|| agentBox.getSelectedItem().equals(DO_NOT_USE)) {
			// Do nothing
		} else if (agentBox.getSelectedItem().equals(OTHER_PARAMETER)) {
			panel.add(agentButton, createConstraints(1, 1));
		} else {
			int row = 1;
			String column = (String) agentBox.getSelectedItem();
			Set<String> values;

			try {
				values = xlsReader.getValuesInColumn(
						new File(filePanel.getFileName()),
						(String) sheetBox.getSelectedItem(), column);
			} catch (Exception e) {
				values = new LinkedHashSet<>();
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

				panel.add(new JLabel(value + ":"), createConstraints(0, row));
				panel.add(button, createConstraints(1, row));
				row++;
			}
		}

		JPanel p = new JPanel();

		p.setLayout(new BorderLayout());
		p.add(panel, BorderLayout.CENTER);

		if (set.getAgentColumn() != null
				&& !set.getAgentColumn().equals(OTHER_PARAMETER)
				&& !set.getAgentColumn().equals(DO_NOT_USE)) {
			try {
				List<Integer> missing = xlsReader.getMissingData(new File(
						filePanel.getFileName()), (String) sheetBox
						.getSelectedItem(), set.getAgentColumn());

				if (!missing.isEmpty()) {
					p.add(new JLabel("Data missing in rows: "
							+ getMissingString(missing)), BorderLayout.SOUTH);
				}
			} catch (Exception e) {
			}
		}

		agentPanel.removeAll();
		agentPanel.add(p, BorderLayout.NORTH);
		agentPanel.revalidate();
	}

	private void updateMatrixPanel() {
		List<String> items = new ArrayList<>();

		items.add(DO_NOT_USE);
		items.add(OTHER_PARAMETER);
		items.addAll(fileColumnList);

		matrixButtons.clear();
		matrixBox = new JComboBox<>(items.toArray(new String[0]));
		matrixButton = new JButton(OTHER_PARAMETER);

		UI.select(matrixBox, set.getMatrixColumn());

		if (set.getMatrix() != null) {
			matrixButton.setText(set.getMatrix().getName());
		} else {
			matrixButton.setText(OTHER_PARAMETER);
		}

		matrixBox.addItemListener(this);
		matrixButton.addActionListener(this);

		JPanel panel = new JPanel();

		panel.setLayout(new GridBagLayout());
		panel.add(new JLabel("XLS Column:"), createConstraints(0, 0));
		panel.add(matrixBox, createConstraints(1, 0));

		if (matrixBox.getSelectedItem() == null
				|| matrixBox.getSelectedItem().equals(DO_NOT_USE)) {
			// Do nothing
		} else if (matrixBox.getSelectedItem().equals(OTHER_PARAMETER)) {
			panel.add(matrixButton, createConstraints(1, 1));
		} else {
			int row = 1;
			String column = (String) matrixBox.getSelectedItem();
			Set<String> values;

			try {
				values = xlsReader.getValuesInColumn(
						new File(filePanel.getFileName()),
						(String) sheetBox.getSelectedItem(), column);
			} catch (Exception e) {
				values = new LinkedHashSet<>();
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

				panel.add(new JLabel(value + ":"), createConstraints(0, row));
				panel.add(button, createConstraints(1, row));
				row++;
			}
		}

		JPanel p = new JPanel();

		p.setLayout(new BorderLayout());
		p.add(panel, BorderLayout.CENTER);

		if (set.getMatrixColumn() != null
				&& !set.getMatrixColumn().equals(OTHER_PARAMETER)
				&& !set.getMatrixColumn().equals(DO_NOT_USE)) {
			try {
				List<Integer> missing = xlsReader.getMissingData(new File(
						filePanel.getFileName()), (String) sheetBox
						.getSelectedItem(), set.getMatrixColumn());

				if (!missing.isEmpty()) {
					p.add(new JLabel("Data missing in rows: "
							+ getMissingString(missing)), BorderLayout.SOUTH);
				}
			} catch (Exception e) {
			}
		}

		matrixPanel.removeAll();
		matrixPanel.add(p, BorderLayout.NORTH);
		matrixPanel.revalidate();
	}

	private void updateColumnsPanel() {
		if (!fileColumnList.isEmpty()) {
			columnBoxes.clear();
			columnButtons.clear();
			columnUnitBoxes.clear();

			JPanel panel = new JPanel();
			int row = 0;

			panel.setLayout(new GridBagLayout());

			for (String column : fileColumnList) {
				JComboBox<String> box = new JComboBox<>(new String[] {
						DO_NOT_USE, OTHER_PARAMETER, XLSReader.NAME_COLUMN,
						MdInfoXml.ATT_COMMENT,
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

				panel.add(new JLabel(column + ":"), createConstraints(0, row));
				panel.add(box, createConstraints(1, row));
				panel.add(button, createConstraints(2, row));

				if (set.getColumnMappings().get(column) instanceof MiscXml) {
					MiscXml condition = (MiscXml) set.getColumnMappings().get(
							column);
					List<String> allUnits = new ArrayList<>();

					for (String cat : condition.getCategories()) {
						allUnits.addAll(Categories.getCategory(cat)
								.getAllUnits());
					}

					JComboBox<String> unitBox = new JComboBox<>(
							allUnits.toArray(new String[0]));

					UI.select(unitBox, condition.getUnit());
					unitBox.addItemListener(this);
					columnUnitBoxes.put(column, unitBox);
					panel.add(unitBox, createConstraints(3, row));
				}

				row++;
			}

			columnsPanel.removeAll();
			columnsPanel.add(panel, BorderLayout.NORTH);
			columnsPanel.revalidate();
		} else {
			columnsPanel.removeAll();
			columnsPanel.add(noLabel, BorderLayout.NORTH);
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
		Map<String, String> newModelMappings = new LinkedHashMap<>();
		Map<String, KnimeTuple> newSecModelTuples = new LinkedHashMap<>();
		Map<String, Map<String, String>> newSecModelMappings = new LinkedHashMap<>();
		Map<String, Map<String, String>> newSecModelIndepMins = new LinkedHashMap<>();
		Map<String, Map<String, String>> newSecModelIndepMaxs = new LinkedHashMap<>();
		Map<String, Map<String, String>> newSecModelIndepCategories = new LinkedHashMap<>();
		Map<String, Map<String, String>> newSecModelIndepUnits = new LinkedHashMap<>();
		Map<String, AgentXml> newAgentMappings = new LinkedHashMap<>();
		Map<String, MatrixXml> newMatrixMappings = new LinkedHashMap<>();
		Map<String, Object> newColumnMappings = new LinkedHashMap<>();

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

		for (String param1 : secCategoryBoxes.keySet()) {
			newSecModelIndepCategories.put(param1,
					new LinkedHashMap<String, String>());

			for (String param2 : secCategoryBoxes.get(param1).keySet()) {
				if (set.getSecModelIndepCategories().containsKey(param1)
						&& set.getSecModelIndepCategories().get(param1)
								.containsKey(param2)) {
					newSecModelIndepCategories.get(param1).put(
							param2,
							set.getSecModelIndepCategories().get(param1)
									.get(param2));
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
		set.setSecModelIndepCategories(newSecModelIndepCategories);
		set.setSecModelIndepUnits(newSecModelIndepUnits);
		set.setAgentMappings(newAgentMappings);
		set.setMatrixMappings(newMatrixMappings);
		set.setColumnMappings(newColumnMappings);
	}

	private static String getMissingString(List<Integer> missing) {
		String s = "";

		if (missing.size() <= 10) {
			for (int m : missing) {
				s += "," + m;
			}
		} else {
			for (int i = 0; i < 10; i++) {
				s += "," + missing.get(i);
			}

			s += ",...";
		}

		if (!s.isEmpty()) {
			s = s.substring(1);
		}

		return s;
	}
}
