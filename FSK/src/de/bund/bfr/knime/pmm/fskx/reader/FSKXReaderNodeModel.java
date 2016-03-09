/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors: Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.pmm.fskx.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.r.node.local.port.RPortObject;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import de.bund.bfr.knime.pmm.FSMRUtils;
import de.bund.bfr.knime.pmm.extendedtable.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.fskx.FSKFiles;
import de.bund.bfr.knime.pmm.fskx.FSKUtil;
import de.bund.bfr.knime.pmm.fskx.FSKXTuple;
import de.bund.bfr.knime.pmm.fskx.FSKXTuple.KEYS;
import de.bund.bfr.knime.pmm.fskx.MissingValueError;
import de.bund.bfr.knime.pmm.fskx.RScript;
import de.bund.bfr.knime.pmm.openfsmr.FSMRTemplate;
import de.bund.bfr.knime.pmm.openfsmr.OpenFSMRSchema;
import de.unirostock.sems.cbarchive.CombineArchiveException;

public class FSKXReaderNodeModel extends NodeModel {

  // configuration keys
  public static final String CFGKEY_FILE = "filename";

  // defaults for persistent state
  private static final String DEFAULT_FILE = "c:/temp/foo.numl";

  // defaults for persistent state
  private SettingsModelString filename = new SettingsModelString(CFGKEY_FILE, DEFAULT_FILE);

  // *** Internal Model Keys ***
  private static final String FILE_NAME = "fskreaderNodeInternals.xml";
  private static final String INTERNAL_MODEL = "internalModel";

  private static final PortType[] inPortTypes = {};
  private static final PortType[] outPortTypes =
      {BufferedDataTable.TYPE, BufferedDataTable.TYPE, RPortObject.TYPE};

  protected FSKXReaderNodeModel() {
    // 0 input ports and 3 output ports
    super(inPortTypes, outPortTypes);
  }

  /**
   * {@inheritDoc}
   *
   * @throws MissingValueError
   */
  @Override
  protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
      throws CombineArchiveException, FileAccessException, MissingValueError {

    FSKFiles files = new FSKFiles(this.filename.getStringValue());

    // Creates R table, meta data table and R port with the R workspace
    BufferedDataTable rTable = createRTable(files, exec);
    BufferedDataTable metaDataTable = createMetaDataTable(files, exec);
    RPortObject rPort = files.getWorkspace() == null ? null : new RPortObject(files.getWorkspace());

    return new PortObject[] {rTable, metaDataTable, rPort};
  }

  /** {@inheritDoc} */
  @Override
  protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
      throws InvalidSettingsException {
    return new PortObjectSpec[] {null, null, null};
  }

  /** {@inheritDoc} */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    this.filename.saveSettingsTo(settings);
  }

  /** {@inheritDoc} */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    this.filename.loadSettingsFrom(settings);
  }

  /** {@inheritDoc} */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    this.filename.validateSettings(settings);
  }

  /** {@inheritDoc} */
  @Override
  protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {
    File file = new File(nodeInternDir, FILE_NAME);
    try (FileInputStream fis = new FileInputStream(file)) {
      NodeSettingsRO settings = NodeSettings.loadFromXML(fis);
      loadValidatedSettingsFrom(settings);
    } catch (InvalidSettingsException e) {
      System.err.println(e.getMessage());
    } catch (IOException e) {
      throw e;
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {

    NodeSettings settings = new NodeSettings(INTERNAL_MODEL);
    this.filename.saveSettingsTo(settings);

    File file = new File(nodeInternDir, FILE_NAME);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      settings.saveToXML(fos);
    } catch (IOException e) {
      throw e;
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void reset() {
    // does nothing
  }

  class FileAccessException extends Exception {

    private static final long serialVersionUID = 1L;

    public FileAccessException(final String descr) {
      super(descr);
    }
  }

  /**
   * Creates a {@link BufferedDataTable} with the R code: model, parameters and visualization
   * script.
   * 
   * @param files {@link {@link FSKXReaderNodeModel.FSKFiles}
   * @param exec {@link ExecutionContext}
   * @throws FileAccessException
   * @throws MissingValueError
   * @return {@link BufferedDataTable}
   */
  private BufferedDataTable createRTable(final FSKFiles files, final ExecutionContext exec)
      throws FileAccessException, MissingValueError {
    final Set<String> librariesSet = new HashSet<>(); // Set of libraries
    final Set<String> sourcesSet = new HashSet<>(); // Set of sources

    final EnumMap<FSKXTuple.KEYS, String> valuesMap = new EnumMap<>(FSKXTuple.KEYS.class);

    // R model script
    try {
      RScript modelScript = new RScript(files.getModelScript());

      // no IOException occur -> process content of model script and add libraries and sources
      valuesMap.put(KEYS.ORIG_MODEL, modelScript.getOriginalScript());
      valuesMap.put(KEYS.SIMP_MODEL, modelScript.getSimplifiedScript());
      librariesSet.addAll(modelScript.getLibraries());
      sourcesSet.addAll(modelScript.getSources());
    } catch (IOException e) {
      System.err.println(e);
      throw new FileAccessException("Model script could not be accessed");
    }

    // R parameters script
    if (files.getParamScript() != null) {
      try {
        RScript script = new RScript(files.getParamScript());

        // no IOException occur -> process content of parameters script and add libraries and
        // sources
        valuesMap.put(KEYS.ORIG_PARAM, script.getOriginalScript());
        valuesMap.put(KEYS.SIMP_PARAM, script.getSimplifiedScript());
        librariesSet.addAll(script.getLibraries());
        sourcesSet.addAll(script.getSources());
      } catch (IOException e) {
        System.err.println(e.getMessage());
        setWarningMessage(files.getParamScript().getName() + ": cannot be read");
      }
    }

    // R visualization script
    if (files.getVizScript() != null) {
      try {
        RScript script = new RScript(files.getVizScript());

        // no IOException occur -> process content of parameters script and add libraries and
        // sources
        valuesMap.put(KEYS.ORIG_VIZ, script.getOriginalScript());
        valuesMap.put(KEYS.SIMP_VIZ, script.getSimplifiedScript());
        librariesSet.addAll(script.getLibraries());
        sourcesSet.addAll(script.getSources());
      } catch (IOException e) {
        System.err.println(e.getMessage());
        setWarningMessage(e.getMessage());
      }
    }

    // copy libraries and sources to valuesMap
    valuesMap.put(KEYS.LIBS, String.join(";", librariesSet)); // Adds R libraries
    valuesMap.put(KEYS.SOURCES, String.join(";", sourcesSet)); // Adds R sources

    // Creates model table spec and container
    DataTableSpec spec = FSKUtil.createFSKTableSpec();
    BufferedDataContainer container = exec.createDataContainer(spec);
    container.addRowToTable(new FSKXTuple(valuesMap));
    container.close();

    return container.getTable();
  }

  /**
   * Creates a {@link BufferedDataTable} with the model meta data. If an error occurs the table will
   * be empty and the will show a warning.
   * 
   * @param files {@link FSKXReaderNodeModel.FSKFiles}
   * @param exec {@link ExecutionContext}
   * @return {@link BufferedDataTable}
   */
  private BufferedDataTable createMetaDataTable(final FSKFiles files, final ExecutionContext exec) {
    BufferedDataContainer container = exec.createDataContainer(new OpenFSMRSchema().createSpec());
    if (files.getMetaData() != null) {

      SBMLDocument doc;
      try {
        doc = new SBMLReader().readSBML(files.getMetaData());

        // No errors occur -> process doc and populate table
        FSMRTemplate template = FSMRUtils.processPrevalenceModel(doc);
        KnimeTuple tuple = FSMRUtils.createTupleFromTemplate(template);
        container.addRowToTable(tuple);
      } catch (XMLStreamException | IOException e) {
        setWarningMessage(e.getMessage());
        e.printStackTrace(System.err);
      }
    }
    container.close();

    return container.getTable();
  }
}