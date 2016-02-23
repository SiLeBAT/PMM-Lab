package de.bund.bfr.knime.pmm.fskx.r2fsk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.google.common.base.Strings;
import com.google.common.io.Files;

import de.bund.bfr.knime.pmm.FSMRUtils;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.extendedtable.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.fskx.FSKUtil;
import de.bund.bfr.knime.pmm.fskx.FSKXTuple;
import de.bund.bfr.knime.pmm.fskx.FSKXTuple.KEYS;
import de.bund.bfr.knime.pmm.openfsmr.FSMRTemplate;
import de.bund.bfr.knime.pmm.openfsmr.OpenFSMRSchema;

public class R2FSKNodeModel extends NodeModel {

  // configuration key of the path of the R model script
  static final String CFGKEY_MODEL_SCRIPT = "modelScript";

  // configuration key of the path of the R parameters script
  static final String CFGKEY_PARAM_SCRIPT = "paramScript";

  // configuration key of the path of the R visualization script
  static final String CFGKEY_VISUALIZATION_SCRIPT = "visualizationScript";

  // configuration key of the path of the XLSX spreadsheet with the model meta data
  static final String CFGKEY_SPREADSHEET = "spreadsheet";

  // Settings models
  private SettingsModelString modelScriptPath = new SettingsModelString(CFGKEY_MODEL_SCRIPT, null);
  private SettingsModelString paramScriptPath = new SettingsModelString(CFGKEY_PARAM_SCRIPT, null);
  private SettingsModelString visualizationScriptPath =
      new SettingsModelString(CFGKEY_VISUALIZATION_SCRIPT, null);
  private SettingsModelString spreadsheetPath = new SettingsModelString(CFGKEY_SPREADSHEET, null);

  /** {@inheritDoc} */
  protected R2FSKNodeModel() {
    super(0, 2);
  }

  /** {@inheritDoc} */
  @Override
  protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {}

  /** {@inheritDoc} */
  @Override
  protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {}

  /** {@inheritDoc} */
  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) {
    modelScriptPath.saveSettingsTo(settings);
    paramScriptPath.saveSettingsTo(settings);
    visualizationScriptPath.saveSettingsTo(settings);
    spreadsheetPath.saveSettingsTo(settings);
  }

  /** {@inheritDoc} */
  @Override
  protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
    modelScriptPath.validateSettings(settings);
    paramScriptPath.validateSettings(settings);
    visualizationScriptPath.validateSettings(settings);
    spreadsheetPath.validateSettings(settings);
  }

  /** {@inheritDoc} */
  @Override
  protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
      throws InvalidSettingsException {
    modelScriptPath.loadSettingsFrom(settings);
    paramScriptPath.loadSettingsFrom(settings);
    visualizationScriptPath.loadSettingsFrom(settings);
    spreadsheetPath.loadSettingsFrom(settings);
  }

  /** {@inheritDoc} */
  @Override
  protected void reset() {}

  /**
   * {@inheritDoc}
   * 
   * @throws InvalidSettingsException if the model script is not specified
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws InvalidSettingsException {

    final Set<String> librariesSet = new HashSet<>(); // Set of libraries
    final Set<String> sourcesSet = new HashSet<>(); // Set of sources

    /**
     * Reads model script. Since the model script is mandatory, if modelScriptPath is not set, then
     * a InvalidSettingsException will be thrown
     */
    final String origModelScript; // original model script (with comments)
    final String simpModelScript; // simplified model script (without comments)
    try {
      origModelScript = readFile(modelScriptPath.getStringValue()); // may throw errors

      // If no errors are thrown, proceed to extract libraries and sources
      final String[] lines = origModelScript.split("\\r?\\n");
      librariesSet.addAll(FSKUtil.extractLibrariesFromLines(lines));
      sourcesSet.addAll(FSKUtil.extractSourcesFromLines(lines));

      // Creates simplified model script
      simpModelScript = FSKUtil.createSimplifiedScript(lines);
    } catch (NullPointerException e) {
      // modelScriptPath is not set (null)
      throw new InvalidSettingsException("Unspecified model script");
    } catch (IOException e) {
      throw new InvalidSettingsException(modelScriptPath.getStringValue() + ": cannot be read");
    }

    // Reads parameters script. The parameters script is optional, thus if paraScriptPath is not set
    // paramScriptLines will be assigned an empty list
    String origParamScript;
    String simpParamScript;
    try {
      origParamScript = readFile(paramScriptPath.getStringValue()); // may throw errors

      // If no errors are thrown, proceed to extract libraries and sources
      final String[] lines = origParamScript.split("\\r?\\n");
      librariesSet.addAll(FSKUtil.extractLibrariesFromLines(lines));
      sourcesSet.addAll(FSKUtil.extractSourcesFromLines(lines));

      // Creates simplified parameters list
      simpParamScript = FSKUtil.createSimplifiedScript(lines);
    } catch (NullPointerException e) {
      // do not do anything if paramScriptPath is not set
      origParamScript = simpParamScript = "";
    } catch (IOException e) {
      throw new InvalidSettingsException(paramScriptPath.getStringValue() + ": cannot be read");
    }

    // Reads visualization script. The visualization script is optional, thus if
    // visualizationScriptPath is not set visualizationScriptLines will be assigned an empty list
    String origVisualizationScript;
    String simpVisualizationScript;
    try {
      origVisualizationScript = readFile(visualizationScriptPath.getStringValue());

      // If no errors are thrown, proceed to extract libraries and sources
      final String[] lines = origVisualizationScript.split("\\r?\\n");
      librariesSet.addAll(FSKUtil.extractLibrariesFromLines(lines));
      sourcesSet.addAll(FSKUtil.extractSourcesFromLines(lines));

      // Creates simplified visualization script
      simpVisualizationScript = FSKUtil.createSimplifiedScript(lines);
    } catch (NullPointerException e) {
      // do not do anything if visualizationScriptPath is not set
      origVisualizationScript = simpVisualizationScript = "";
    } catch (IOException e) {
      throw new InvalidSettingsException(
          visualizationScriptPath.getStringValue() + ": cannot be read");
    }

    /**
     * Process the SBMLDocument with the model meta data. If an error occurs or the file is not
     * specified the data table will be empty.
     */
    KnimeTuple metaDataTuple;
    final String spreadsheetPathString = spreadsheetPath.getStringValue();
    if (Strings.isNullOrEmpty(spreadsheetPathString)) {
      metaDataTuple = new KnimeTuple(SchemaFactory.createM1DataSchema());
    } else {
      try {
        FileInputStream fis = new FileInputStream(new File(spreadsheetPathString));
        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkbook = new XSSFWorkbook(fis);
        fis.close();
        FSMRTemplate template = FSMRUtils.processSpreadsheet(myWorkbook);
        metaDataTuple = FSMRUtils.createTupleFromTemplate(template);
      } catch (IOException e) {
        // creates empty table and print error trace
        metaDataTuple = new KnimeTuple(SchemaFactory.createM1DataSchema());
        e.printStackTrace();
      }
    }

    // Creates table spec and container
    final DataTableSpec tableSpec = FSKUtil.createFSKTableSpec();
    final BufferedDataContainer container = exec.createDataContainer(tableSpec);

    // Adds row and closes the container
    final EnumMap<FSKXTuple.KEYS, String> valuesMap = new EnumMap<>(KEYS.class);
    valuesMap.put(KEYS.ORIG_MODEL, origModelScript); // adds original model script
    valuesMap.put(KEYS.SIMP_MODEL, simpModelScript); // adds simplified model script
    valuesMap.put(KEYS.ORIG_PARAM, origParamScript); // adds original parameters script
    valuesMap.put(KEYS.SIMP_PARAM, simpParamScript); // adds simplified parameters script
    valuesMap.put(KEYS.ORIG_VIZ, origVisualizationScript); // adds original visualization script
    valuesMap.put(KEYS.SIMP_VIZ, simpVisualizationScript); // adds simplified visualization script
    valuesMap.put(KEYS.LIBS, String.join(";", librariesSet));  // adds R libraries
    valuesMap.put(KEYS.SOURCES, String.join(";", sourcesSet));  // adds R sources

    container.addRowToTable(new FSKXTuple(valuesMap));
    container.close();

    // Meta data table: creates model table spec and container
    final DataTableSpec modelTableSpec = new OpenFSMRSchema().createSpec();
    final BufferedDataContainer modelContainer = exec.createDataContainer(modelTableSpec);
    modelContainer.addRowToTable(metaDataTuple);
    modelContainer.close();

    return new BufferedDataTable[] {container.getTable(), modelContainer.getTable()};
  }

  /** {@inheritDoc} */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    return new DataTableSpec[] {null, null};
  }

  /**
   * Reads content of a text file.
   * 
   * @param filepath
   * @throw NullPointerException if filepath is null
   * @throw IOException if the file specified by filepath cannot be read
   * @return string with the contents of the file
   */
  private String readFile(final String filepath) throws NullPointerException, IOException {
    final File file = new File(filepath); // throws NullPointerException
    final String contents = Files.toString(file, Charsets.UTF_8); // throws IOException

    return contents;
  }

}
