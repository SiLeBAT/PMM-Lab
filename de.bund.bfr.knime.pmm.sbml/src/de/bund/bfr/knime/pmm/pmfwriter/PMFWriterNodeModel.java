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
package de.bund.bfr.knime.pmm.pmfwriter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDate;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLTriple;

import de.bund.bfr.knime.pmm.annotation.GlobalModelIdNode;
import de.bund.bfr.knime.pmm.annotation.MetadataAnnotation;
import de.bund.bfr.knime.pmm.annotation.Model1Annotation;
import de.bund.bfr.knime.pmm.annotation.Model2Annotation;
import de.bund.bfr.knime.pmm.annotation.PrimaryModelNode;
import de.bund.bfr.knime.pmm.annotation.ReferenceSBMLNode;
import de.bund.bfr.knime.pmm.common.AgentXml;
import de.bund.bfr.knime.pmm.common.CatalogModelXml;
import de.bund.bfr.knime.pmm.common.DepXml;
import de.bund.bfr.knime.pmm.common.EstModelXml;
import de.bund.bfr.knime.pmm.common.IndepXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.ParamXml;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.PmmUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.knime.pmm.common.units.Categories;
import de.bund.bfr.knime.pmm.common.units.Category;
import de.bund.bfr.knime.pmm.common.units.ConvertException;
import de.bund.bfr.knime.pmm.common.units.UnitsFromDB;
import de.bund.bfr.knime.pmm.dbutil.DBUnits;
import de.bund.bfr.knime.pmm.file.ExperimentalDataFile;
import de.bund.bfr.knime.pmm.file.ManualSecondaryModelFile;
import de.bund.bfr.knime.pmm.file.ManualTertiaryModelFile;
import de.bund.bfr.knime.pmm.file.OneStepSecondaryModelFile;
import de.bund.bfr.knime.pmm.file.OneStepTertiaryModelFile;
import de.bund.bfr.knime.pmm.file.PrimaryModelWDataFile;
import de.bund.bfr.knime.pmm.file.PrimaryModelWODataFile;
import de.bund.bfr.knime.pmm.file.TwoStepSecondaryModelFile;
import de.bund.bfr.knime.pmm.file.TwoStepTertiaryModelFile;
import de.bund.bfr.knime.pmm.model.ExperimentalData;
import de.bund.bfr.knime.pmm.model.ManualSecondaryModel;
import de.bund.bfr.knime.pmm.model.ManualTertiaryModel;
import de.bund.bfr.knime.pmm.model.OneStepSecondaryModel;
import de.bund.bfr.knime.pmm.model.OneStepTertiaryModel;
import de.bund.bfr.knime.pmm.model.PrimaryModelWData;
import de.bund.bfr.knime.pmm.model.PrimaryModelWOData;
import de.bund.bfr.knime.pmm.model.TwoStepSecondaryModel;
import de.bund.bfr.knime.pmm.model.TwoStepTertiaryModel;
import de.bund.bfr.knime.pmm.sbmlutil.Agent;
import de.bund.bfr.knime.pmm.sbmlutil.Coefficient;
import de.bund.bfr.knime.pmm.sbmlutil.DataFile;
import de.bund.bfr.knime.pmm.sbmlutil.LimitsConstraint;
import de.bund.bfr.knime.pmm.sbmlutil.Matrix;
import de.bund.bfr.knime.pmm.sbmlutil.Metadata;
import de.bund.bfr.knime.pmm.sbmlutil.Model1Rule;
import de.bund.bfr.knime.pmm.sbmlutil.Model2Rule;
import de.bund.bfr.knime.pmm.sbmlutil.ModelType;
import de.bund.bfr.knime.pmm.sbmlutil.PMFUnitDefinition;
import de.bund.bfr.knime.pmm.sbmlutil.SecDep;
import de.bund.bfr.knime.pmm.sbmlutil.SecIndep;
import de.bund.bfr.knime.pmm.sbmlutil.Uncertainties;
import de.bund.bfr.knime.pmm.sbmlutil.Util;
import de.bund.bfr.numl.NuMLDocument;

/**
 * Model implementation of PMFWriter
 * 
 * @author Miguel Alba
 */
public class PMFWriterNodeModel extends NodeModel {
	protected static final String CFG_OUT_PATH = "outPath";
	protected static final String CFG_MODEL_NAME = "modelName";
	protected static final String CFG_CREATOR_GIVEN_NAME = "CreatorGivenName";
	protected static final String CFG_CREATOR_FAMILY_NAME = "CreatorFamilyName";
	protected static final String CFG_CREATOR_CONTACT = "CreatorContact";
	protected static final String CFG_CREATED_DATE = "CreationDate";
	protected static final String CFG_LAST_MODIFIED_DATE = "ModifiedDate";
	protected static final String CFG_ISSECONDARY = "isSecondary";
	protected static final String CFG_OVERWRITE = "overwrite";
	protected static final String CFG_SPLITMODELS = "splitModels";

	private SettingsModelString outPath = new SettingsModelString(CFG_OUT_PATH, null);
	private SettingsModelString modelName = new SettingsModelString(CFG_MODEL_NAME, null);
	private SettingsModelString creatorGivenName = new SettingsModelString(CFG_CREATOR_GIVEN_NAME, null);
	private SettingsModelString creatorFamilyName = new SettingsModelString(CFG_CREATOR_FAMILY_NAME, null);
	private SettingsModelString creatorContact = new SettingsModelString(CFG_CREATOR_CONTACT, null);
	private SettingsModelDate createdDate = new SettingsModelDate(CFG_CREATED_DATE);
	private SettingsModelDate modifiedDate = new SettingsModelDate(CFG_LAST_MODIFIED_DATE);
	private SettingsModelBoolean isSecondary = new SettingsModelBoolean(CFG_ISSECONDARY, false);
	private SettingsModelBoolean overwrite = new SettingsModelBoolean(CFG_OVERWRITE, true);
	private SettingsModelBoolean splitModels = new SettingsModelBoolean(CFG_SPLITMODELS, false);

	Parser parser; // current parser

	/**
	 * Constructor for the node model.
	 */
	protected PMFWriterNodeModel() {
		super(1, 0);

		// Sets current date in the dialog components
		long currentDate = Calendar.getInstance().getTimeInMillis();
		createdDate.setTimeInMillis(currentDate);
		modifiedDate.setTimeInMillis(currentDate);
	}

	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		KnimeSchema schema = null;
		ModelType modelType = null;
		List<KnimeTuple> tuples;

		DataTableSpec spec = inData[0].getSpec();
		// Table has the structure Model1 + Model2 + Data
		if (SchemaFactory.conformsM12DataSchema(spec)) {
			schema = SchemaFactory.createM12DataSchema();
			tuples = PmmUtilities.getTuples(inData[0], schema);
			if (hasData(tuples)) {
				boolean identical = identicalEstModels(tuples);
				if (isSecondary.getBooleanValue() == true) {
					if (identical) {
						modelType = ModelType.ONE_STEP_SECONDARY_MODEL;
					} else {
						modelType = ModelType.TWO_STEP_SECONDARY_MODEL;
					}
				} else {
					if (identical) {
						modelType = ModelType.ONE_STEP_TERTIARY_MODEL;
					} else {
						modelType = ModelType.TWO_STEP_TERTIARY_MODEL;
					}
				}
			} else {
				modelType = ModelType.MANUAL_TERTIARY_MODEL;
			}
		}

		// Table has Model1 + Data
		else if (SchemaFactory.conformsM1DataSchema(spec)) {
			schema = SchemaFactory.createM1DataSchema();
			tuples = PmmUtilities.getTuples(inData[0], schema);

			// Check every tuple. If any tuple has data (number of data points >
			// 0) then assigns PRIMARY_MODEL_WDATA. Otherwise it assigns
			// PRIMARY_MODEL_WODATA
			modelType = ModelType.PRIMARY_MODEL_WODATA;
			for (KnimeTuple tuple : tuples) {
				PmmXmlDoc mdData = tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES);
				if (mdData.size() > 0) {
					modelType = ModelType.PRIMARY_MODEL_WDATA;
					break;
				}
			}
		}

		// Table only has data
		else if (SchemaFactory.conformsDataSchema(spec)) {
			schema = SchemaFactory.createDataSchema();
			tuples = PmmUtilities.getTuples(inData[0], schema);
			modelType = ModelType.EXPERIMENTAL_DATA;
		}

		// Table only has secondary model cells
		else if (SchemaFactory.conformsM2Schema(spec)) {
			schema = SchemaFactory.createM2Schema();
			tuples = PmmUtilities.getTuples(inData[0], schema);
			modelType = ModelType.MANUAL_SECONDARY_MODEL;
		} else {
			throw new Exception();
		}

		// Retrieve info from dialog
		Metadata metadata = new Metadata();

		if (creatorGivenName.getStringValue().isEmpty()) {
			setWarningMessage("Given name missing");
		} else {
			metadata.setGivenName(creatorGivenName.getStringValue());
		}

		if (creatorFamilyName.getStringValue().isEmpty()) {
			setWarningMessage("Creator family name missing");
		} else {
			metadata.setFamilyName(creatorFamilyName.getStringValue());
		}

		if (creatorContact.getStringValue().isEmpty()) {
			setWarningMessage("Creator contact missing");
		} else {
			metadata.setContact(creatorContact.getStringValue());
		}

		if (createdDate.getSelectedFields() == 1) {
			metadata.setCreatedDate(createdDate.getDate().toString());
		} else {
			setWarningMessage("Created date missing");
		}

		if (modifiedDate.getSelectedFields() == 1) {
			metadata.setModifiedDate(modifiedDate.getDate().toString());
		} else {
			setWarningMessage("Modified date missing");
		}

		String dir = outPath.getStringValue();
		String mdName = modelName.getStringValue();

		// Check for existing file -> shows warning if despite overwrite being
		// false the user still executes the node
		String filepath = String.format("%s/%s.pmf", dir, mdName);
		File f = new File(filepath);
		if (f.exists() && !f.isDirectory() && !overwrite.getBooleanValue()) {
			setWarningMessage(filepath + " was not overwritten");
			return new BufferedDataTable[] {};
		}

		if (modelType == ModelType.EXPERIMENTAL_DATA) {
			parser = new ExperimentalDataParser();
		} else if (modelType == ModelType.PRIMARY_MODEL_WDATA) {
			parser = new PrimaryModelWDataParser();
		} else if (modelType == ModelType.PRIMARY_MODEL_WODATA) {
			parser = new PrimaryModelWODataParser();
		} else if (modelType == ModelType.TWO_STEP_SECONDARY_MODEL) {
			parser = new TwoStepSecondaryModelParser();
		} else if (modelType == ModelType.ONE_STEP_SECONDARY_MODEL) {
			parser = new OneStepSecondaryModelParser();
		} else if (modelType == ModelType.MANUAL_SECONDARY_MODEL) {
			parser = new ManualSecondaryModelParser();
		} else if (modelType == ModelType.TWO_STEP_TERTIARY_MODEL) {
			parser = new TwoStepTertiaryModelParser();
		} else if (modelType == ModelType.ONE_STEP_TERTIARY_MODEL) {
			parser = new OneStepTertiaryModelParser();
		} else if (modelType == ModelType.MANUAL_TERTIARY_MODEL) {
			parser = new ManualTertiaryModelParser();
		}

		parser.write(tuples, dir, mdName, metadata, splitModels.getBooleanValue(), exec);

		return new BufferedDataTable[] {};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (outPath.getStringValue() == null || modelName.getStringValue() == null) {
			throw new InvalidSettingsException("Node must be configured");
		}

		if (outPath.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Missing outpath");
		}

		if (modelName.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Missing model name");
		}
		return new DataTableSpec[] {};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		outPath.saveSettingsTo(settings);
		modelName.saveSettingsTo(settings);
		creatorGivenName.saveSettingsTo(settings);
		creatorFamilyName.saveSettingsTo(settings);
		creatorContact.saveSettingsTo(settings);
		createdDate.saveSettingsTo(settings);
		modifiedDate.saveSettingsTo(settings);
		isSecondary.saveSettingsTo(settings);
		overwrite.saveSettingsTo(settings);
		splitModels.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		outPath.loadSettingsFrom(settings);
		modelName.loadSettingsFrom(settings);
		creatorGivenName.loadSettingsFrom(settings);
		creatorFamilyName.loadSettingsFrom(settings);
		creatorContact.loadSettingsFrom(settings);
		createdDate.loadSettingsFrom(settings);
		modifiedDate.loadSettingsFrom(settings);
		isSecondary.loadSettingsFrom(settings);
		overwrite.loadSettingsFrom(settings);
		splitModels.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		outPath.validateSettings(settings);
		modelName.validateSettings(settings);
		creatorGivenName.validateSettings(settings);
		creatorFamilyName.validateSettings(settings);
		creatorContact.validateSettings(settings);
		createdDate.validateSettings(settings);
		modifiedDate.validateSettings(settings);
		isSecondary.validateSettings(settings);
		overwrite.validateSettings(settings);
		splitModels.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	static boolean identicalEstModels(List<KnimeTuple> tuples) {
		int id = ((EstModelXml) tuples.get(0).getPmmXml(Model1Schema.ATT_ESTMODEL).get(0)).getId();
		for (KnimeTuple tuple : tuples.subList(1, tuples.size())) {
			EstModelXml estModel = (EstModelXml) tuple.getPmmXml(Model1Schema.ATT_ESTMODEL).get(0);
			if (id != estModel.getId()) {
				return false;
			}
		}
		return true;
	}

	public boolean hasData(List<KnimeTuple> tuples) {
		for (KnimeTuple tuple : tuples) {
			PmmXmlDoc mdData = (PmmXmlDoc) tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES);
			if (mdData != null && mdData.size() > 0) {
				return true;
			}
		}
		return false;
	}
}

class TableReader {
	protected final static int LEVEL = 3;
	protected final static int VERSION = 1;

	public static void renameLog(KnimeTuple tuple) {
		PmmXmlDoc modelXml = tuple.getPmmXml(Model1Schema.ATT_MODELCATALOG);
		CatalogModelXml model = (CatalogModelXml) modelXml.get(0);

		model.setFormula(MathUtilities.replaceVariable(model.getFormula(), "log", "log10"));
		tuple.setValue(Model1Schema.ATT_MODELCATALOG, modelXml);
	}

	public static void replaceCelsiusAndFahrenheit(KnimeTuple tuple) {
		final String CELSIUS = "�C";
		final String FAHRENHEIT = "�F";
		final String KELVIN = "K";

		PmmXmlDoc indepXml = tuple.getPmmXml(Model1Schema.ATT_INDEPENDENT);
		PmmXmlDoc modelXml = tuple.getPmmXml(Model1Schema.ATT_MODELCATALOG);
		CatalogModelXml model = (CatalogModelXml) modelXml.get(0);
		Category temp = Categories.getTempCategory();

		for (PmmXmlElementConvertable el : indepXml.getElementSet()) {
			IndepXml indep = (IndepXml) el;

			if (CELSIUS.equals(indep.getUnit())) {
				try {
					String replacement = "(" + temp.getConversionString(indep.getName(), KELVIN, CELSIUS) + ")";

					model.setFormula(MathUtilities.replaceVariable(model.getFormula(), indep.getName(), replacement));
					indep.setUnit(KELVIN);
					indep.setMin(temp.convert(indep.getMin(), CELSIUS, KELVIN));
					indep.setMax(temp.convert(indep.getMax(), CELSIUS, KELVIN));
				} catch (ConvertException e) {
					e.printStackTrace();
				}
			} else if (FAHRENHEIT.equals(indep.getUnit())) {
				try {
					String replacement = "(" + temp.getConversionString(indep.getName(), KELVIN, FAHRENHEIT) + ")";

					model.setFormula(MathUtilities.replaceVariable(model.getFormula(), indep.getName(), replacement));
					indep.setUnit(FAHRENHEIT);
					indep.setMin(temp.convert(indep.getMin(), FAHRENHEIT, KELVIN));
					indep.setMax(temp.convert(indep.getMax(), FAHRENHEIT, KELVIN));
				} catch (ConvertException e) {
					e.printStackTrace();
				}
			}
		}

		tuple.setValue(Model1Schema.ATT_INDEPENDENT, indepXml);
		tuple.setValue(Model1Schema.ATT_MODELCATALOG, modelXml);
	}

	public static void addNamespaces(SBMLDocument doc) {
		doc.addDeclaredNamespace("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		doc.addDeclaredNamespace("xmlns:pmml", "http://www.dmg.org/PMML-4_2");
		doc.addDeclaredNamespace("xmlns:pmf", "http://sourceforge.net/projects/microbialmodelingexchange/files/PMF-ML");
		doc.addDeclaredNamespace("xmlns:dc", "http://purl.org/dc/elements/1.1");
		doc.addDeclaredNamespace("xmlns:dcterms", "http://purl.org/dc/terms/");
		doc.addDeclaredNamespace("xmlns:pmmlab",
				"http://sourceforge.net/projects/microbialmodelingexchange/files/PMF-ML");
		doc.addDeclaredNamespace("xmlns:numl", "http://www.numl.org/numl/level1/version1");
		doc.addDeclaredNamespace("xmlns:xlink", "http//www.w3.org/1999/xlink");
	}

	public static void addUnitDefinitions(Model model, DepXml depXml, List<IndepXml> indepXmls,
			List<ParamXml> constXmls) {
		// Get units from dep, indeps and consts
		HashSet<String> units = new HashSet<>();
		if (depXml.getUnit() != null) {
			units.add(depXml.getUnit());
		}

		for (IndepXml indepXml : indepXmls) {
			if (indepXml.getUnit() != null) {
				units.add(indepXml.getUnit());
			}
		}

		for (ParamXml paramXml : constXmls) {
			if (paramXml.getUnit() != null) {
				units.add(paramXml.getUnit());
			}
		}

		// Creates and adds unit definitions for the units present in DB.
		// Missing
		// units in DB will not be retrievable and thus will lack a list of
		// units
		for (String unit : units) {
			UnitDefinition unitDefinition = new UnitDefinition(Util.createId(unit));
			unitDefinition.setLevel(LEVEL);
			unitDefinition.setVersion(VERSION);
			unitDefinition.setName(unit);

			// Current unit `unit` is in the DB
			if (DBUnits.getDBUnits().containsKey(unit)) {
				UnitsFromDB dbUnit = DBUnits.getDBUnits().get(unit);

				String mathml = dbUnit.getMathML_string();
				if (mathml != null && !mathml.isEmpty()) {
					PMFUnitDefinition pud = PMFUnitDefinition.xmlToPMFUnitDefinition(dbUnit.getMathML_string());
					UnitDefinition ud = pud.getUnitDefinition();

					// Add annotation with transformation
					unitDefinition.setAnnotation(ud.getAnnotation());

					for (Unit wrapperUnit : ud.getListOfUnits()) {
						unitDefinition.addUnit(new Unit(wrapperUnit));
					}
				}
			}

			model.addUnitDefinition(unitDefinition);
		}
	}

	public static Map<Integer, Map<Integer, List<KnimeTuple>>> sortGlobalModels(List<KnimeTuple> tuples) {
		// Sort tertiary models
		Map<Integer, Map<Integer, List<KnimeTuple>>> gms = new HashMap<>();
		for (KnimeTuple tuple : tuples) {
			Integer gmID = tuple.getInt(Model2Schema.ATT_GLOBAL_MODEL_ID);
			Integer condID = tuple.getInt(TimeSeriesSchema.ATT_CONDID);

			// global model is in globalModels
			if (gms.containsKey(gmID)) {
				// Get global model
				Map<Integer, List<KnimeTuple>> gm = gms.get(gmID);
				// globalModel has tertiary model with condID => Add tuple to
				// this tertiary model
				if (gm.containsKey(condID)) {
					gm.get(condID).add(tuple);
				}
				// Otherwise, create a tertiary model with condID and add it the
				// current tuple
				else {
					LinkedList<KnimeTuple> tertiaryModel = new LinkedList<>();
					tertiaryModel.add(tuple);
					gm.put(condID, tertiaryModel);
				}
			}

			// else, create tertiary model with condID and add it to new global
			// model
			else {
				// Create new global model
				HashMap<Integer, List<KnimeTuple>> gm = new HashMap<>();

				// Create tertiary model and add it to new global model
				LinkedList<KnimeTuple> tertiaryModel = new LinkedList<>();
				tertiaryModel.add(tuple);
				gm.put(condID, tertiaryModel);

				// Add new global model
				gms.put(gmID, gm);
			}
		}
		return gms;
	}

}

interface Parser {
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception;
}

/**
 * Parse tuples from a table with timeseries.
 */
class ExperimentalDataParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception {
		metadata.setType(ModelType.EXPERIMENTAL_DATA.toString());

		List<ExperimentalData> eds = new LinkedList<>();
		for (KnimeTuple tuple : tuples) {
			NuMLDocument numlDoc = new DataParser(tuple, metadata).getDocument();
			eds.add(new ExperimentalData(numlDoc));
		}
		ExperimentalDataFile.write(dir, mdName, eds, exec);
	}
}

/**
 * Parse tuples from a table with primary models with data.
 */
class PrimaryModelWDataParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception {

		metadata.setType(ModelType.PRIMARY_MODEL_WDATA.toString());

		List<PrimaryModelWData> pms = new LinkedList<>();
		for (KnimeTuple tuple : tuples) {
			SBMLDocument sbmlDoc = new Model1Parser(tuple, metadata).getDocument();
			if (tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES).size() > 0) {
				NuMLDocument numlDoc = new DataParser(tuple, metadata).getDocument();
				pms.add(new PrimaryModelWData(sbmlDoc, numlDoc));
			} else {
				pms.add(new PrimaryModelWData(sbmlDoc, null));
			}
		}

		PrimaryModelWDataFile.write(dir, mdName, pms, exec);
	}
}

/**
 * Parse tuples from a table with primary models without data.
 */
class PrimaryModelWODataParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception {

		metadata.setType(ModelType.PRIMARY_MODEL_WODATA.toString());

		List<PrimaryModelWOData> pms = new LinkedList<>();
		for (KnimeTuple tuple : tuples) {
			SBMLDocument sbmlDoc = new Model1Parser(tuple, metadata).getDocument();
			pms.add(new PrimaryModelWOData(sbmlDoc));
		}
		PrimaryModelWODataFile.write(dir, mdName, pms, exec);
	}
}

/**
 * Parse tuples from a table with primary models without data.
 */
class ManualSecondaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception {

		metadata.setType(ModelType.MANUAL_SECONDARY_MODEL.toString());

		List<ManualSecondaryModel> sms = new LinkedList<>();
		for (KnimeTuple tuple : tuples) {
			sms.add(parse(tuple, metadata));
		}

		if (splitModels) {
			for (int numModel = 0; numModel < sms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<ManualSecondaryModel> model = new LinkedList<>();
				model.add(sms.get(numModel));
				ManualSecondaryModelFile.write(dir, modelName, model, exec);
			}
		} else {
			ManualSecondaryModelFile.write(dir, mdName, sms, exec);
		}
	}

	private ManualSecondaryModel parse(KnimeTuple tuple, Metadata metadata) {

		// Retrieve Model2Schema cells
		CatalogModelXml catModel = (CatalogModelXml) tuple.getPmmXml(Model2Schema.ATT_MODELCATALOG).get(0);
		EstModelXml estModel = (EstModelXml) tuple.getPmmXml(Model2Schema.ATT_ESTMODEL).get(0);
		DepXml dep = (DepXml) tuple.getPmmXml(Model2Schema.ATT_DEPENDENT).get(0);
		PmmXmlDoc indepDoc = tuple.getPmmXml(Model2Schema.ATT_INDEPENDENT);
		PmmXmlDoc paramsDoc = tuple.getPmmXml(Model2Schema.ATT_PARAMETER);
		PmmXmlDoc mLitDoc = tuple.getPmmXml(Model2Schema.ATT_MLIT);
		PmmXmlDoc emLitDoc = tuple.getPmmXml(Model2Schema.ATT_EMLIT);
		int globalModelID = tuple.getInt(Model2Schema.ATT_GLOBAL_MODEL_ID);

		// Gets independent parameters
		List<IndepXml> indepXmls = new LinkedList<>();
		for (PmmXmlElementConvertable item : indepDoc.getElementSet()) {
			indepXmls.add((IndepXml) item);
		}

		// Gets constant parameters
		List<ParamXml> constXmls = new LinkedList<>();
		for (PmmXmlElementConvertable item : paramsDoc.getElementSet()) {
			constXmls.add((ParamXml) item);
		}

		SBMLDocument doc = new SBMLDocument(TableReader.LEVEL, TableReader.VERSION);
		// Enables Hierarchical Composition package
		doc.enablePackage(CompConstants.shortLabel);

		// Adds document annotation
		doc.setAnnotation(new MetadataAnnotation(metadata).getAnnotation());

		TableReader.addNamespaces(doc);

		// Create model definition
		String modelId = "model_" + dep.getName();
		Model model = doc.createModel(modelId);
		if (estModel.getName() != null) {
			model.setName(estModel.getName());
		}

		TableReader.addUnitDefinitions(model, dep, indepXmls, constXmls);

		// Adds dep
		Parameter depParam = new SecDep(dep).getParam();
		// Adds dep constraint
		LimitsConstraint depLc = new LimitsConstraint(dep.getName(), dep.getMin(), dep.getMax());
		if (depLc.getConstraint() != null) {
			model.addConstraint(depLc.getConstraint());
		}
		model.addParameter(depParam);

		// Add independent parameters
		for (IndepXml indepXml : indepXmls) {
			// Creates SBML parameter
			model.addParameter(new SecIndep(indepXml).getParam());
			// Adds constraint
			LimitsConstraint lc = new LimitsConstraint(indepXml.getName(), indepXml.getMin(), indepXml.getMax());
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Adds constant parameters
		for (ParamXml paramXml : constXmls) {
			// Creates SBML parameter
			model.addParameter(new Coefficient(paramXml).getParameter());
			// Adds constraint
			LimitsConstraint lc = new LimitsConstraint(paramXml.getName(), paramXml.getMin(), paramXml.getMax());
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Gets model literature
		List<LiteratureItem> mLits = new LinkedList<>();
		for (PmmXmlElementConvertable item : mLitDoc.getElementSet()) {
			mLits.add((LiteratureItem) item);
		}

		// Gets estimated model literature
		List<LiteratureItem> emLits = new LinkedList<>();
		for (PmmXmlElementConvertable item : emLitDoc.getElementSet()) {
			emLits.add((LiteratureItem) item);
		}

		Model2Rule rule2 = Model2Rule.convertCatalogModelXmlToModel2Rule(catModel, mLits);
		model.addRule(rule2.getRule());

		// Add annotation
		Uncertainties uncertainties = new Uncertainties(estModel);
		model.setAnnotation(new Model2Annotation(globalModelID, uncertainties, emLits).getAnnotation());

		return new ManualSecondaryModel(doc);
	}
}

/**
 * Parse tuples from a table with primary models without data.
 */
class TwoStepSecondaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception {

		metadata.setType(ModelType.TWO_STEP_SECONDARY_MODEL.toString());

		// Sort secondary models
		Map<Integer, List<KnimeTuple>> secTuples = new HashMap<>();
		for (KnimeTuple tuple : tuples) {
			// Get secondary EstModel
			EstModelXml estModel = (EstModelXml) tuple.getPmmXml(Model2Schema.ATT_ESTMODEL).get(0);
			if (secTuples.containsKey(estModel.getId())) {
				secTuples.get(estModel.getId()).add(tuple);
			} else {
				List<KnimeTuple> tlist = new LinkedList<>();
				tlist.add(tuple);
				secTuples.put(estModel.getId(), tlist);
			}
		}

		// For the tuples of every secondary model
		List<TwoStepSecondaryModel> sms = new LinkedList<>();
		for (List<KnimeTuple> tupleList : secTuples.values()) {
			TwoStepSecondaryModel model = parse(tupleList, metadata);
			sms.add(model);
		}

		if (splitModels) {
			for (int numModel = 0; numModel < sms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<TwoStepSecondaryModel> model = new LinkedList<>();
				model.add(sms.get(numModel));
				TwoStepSecondaryModelFile.write(dir, modelName, model, exec);
			}
		} else {
			TwoStepSecondaryModelFile.write(dir, mdName, sms, exec);
		}
	}

	private TwoStepSecondaryModel parse(List<KnimeTuple> tuples, Metadata metadata) {
		/**
		 * <ol>
		 * <li>Create n SBMLDocument for primary models</li>
		 * <li>Parse data and create n NuMLDocument</li>
		 * <li>Create SBMLDocument for secondary model</li>
		 * </ol>
		 */
		List<PrimaryModelWData> primModels = new LinkedList<>();
		for (KnimeTuple tuple : tuples) {
			SBMLDocument sbmlDoc = new Model1Parser(tuple, metadata).getDocument();
			if (tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES).size() > 0) {
				NuMLDocument numlDoc = new DataParser(tuple, metadata).getDocument();
				primModels.add(new PrimaryModelWData(sbmlDoc, numlDoc));
			} else {
				primModels.add(new PrimaryModelWData(sbmlDoc, null));
			}
		}

		// We get the first tuple to query the Model2 columns which are the same
		// for all the tuples of the secondary model
		KnimeTuple firstTuple = tuples.get(0);
		SBMLDocument secDoc = new Model2Parser(firstTuple, metadata).getDocument();
		// Adds annotation for the primary models
		XMLNode metadataNode = secDoc.getModel().getAnnotation().getNonRDFannotation().getChildElement("metadata", "");
		for (PrimaryModelWData pmwd : primModels) {
			String primModelId = pmwd.getSBMLDoc().getModel().getId();
			metadataNode.addChild(new PrimaryModelNode(primModelId + ".sbml").getNode());
		}

		// Creates and return TwoStepSecondaryModel
		return new TwoStepSecondaryModel(secDoc, primModels);
	}
}

/**
 * Parse tuples from a table with primary models without data.
 */
class OneStepSecondaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception {

		metadata.setType(ModelType.ONE_STEP_SECONDARY_MODEL.toString());

		// Sort tuples according to its secondary model
		Map<Integer, List<KnimeTuple>> secMap = new HashMap<>();
		for (KnimeTuple tuple : tuples) {
			// Get secondary EstModelXml
			EstModelXml estModel = (EstModelXml) tuple.getPmmXml(Model2Schema.ATT_ESTMODEL).get(0);

			if (secMap.containsKey(estModel.getId())) {
				secMap.get(estModel.getId()).add(tuple);
			} else {
				List<KnimeTuple> ltup = new LinkedList<>();
				ltup.add(tuple);
				secMap.put(estModel.getId(), ltup);
			}
		}

		// For the tuples of every secondary model
		List<OneStepSecondaryModel> sms = new LinkedList<>();
		for (List<KnimeTuple> ltup : secMap.values()) {
			OneStepSecondaryModel model = parse(ltup, metadata);
			sms.add(model);
		}

		if (splitModels) {
			for (int numModel = 0; numModel < sms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<OneStepSecondaryModel> model = new LinkedList<>();
				model.add(sms.get(numModel));
				OneStepSecondaryModelFile.write(dir, modelName, model, exec);
			}
		} else {
			OneStepSecondaryModelFile.write(dir, mdName, sms, exec);
		}
	}

	private OneStepSecondaryModel parse(List<KnimeTuple> tuples, Metadata metadata) {
		KnimeTuple firstTuple = tuples.get(0);

		// Retrieve Model2Schema cells
		EstModelXml secEstModel = (EstModelXml) firstTuple.getPmmXml(Model2Schema.ATT_ESTMODEL).get(0);

		SBMLDocument doc = new Model1Parser(firstTuple, metadata).getDocument();
		Model model = doc.getModel();
		model.setId(Util.createId("model" + secEstModel.getId()));
		CompSBMLDocumentPlugin compDocPlugin = (CompSBMLDocumentPlugin) doc.getPlugin(CompConstants.shortLabel);
		CompModelPlugin compModelPlugin = (CompModelPlugin) model.getPlugin(CompConstants.shortLabel);

		// Create secondary model
		Model secModel = new Model2Parser(firstTuple, metadata).getDocument().getModel();
		compDocPlugin.addModelDefinition(new ModelDefinition(secModel));

		Submodel submodel = compModelPlugin.createSubmodel("submodel");
		submodel.setModelRef(secModel.getId());

		// Parse data sets and create NuML documents
		List<NuMLDocument> numlDocs = new LinkedList<>();
		for (KnimeTuple tuple : tuples) {
			numlDocs.add(new DataParser(tuple, metadata).getDocument());
		}

		OneStepSecondaryModel ossm = new OneStepSecondaryModel(doc, numlDocs);
		return ossm;
	}
}

class TwoStepTertiaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception {
		metadata.setType(ModelType.TWO_STEP_TERTIARY_MODEL.toString());

		List<TwoStepTertiaryModel> tms = new LinkedList<>();

		// Sort global models
		Map<Integer, Map<Integer, List<KnimeTuple>>> gms = TableReader.sortGlobalModels(tuples);

		// Parse tertiary models
		int modelCounter = 0;
		for (Map<Integer, List<KnimeTuple>> tertiaryInstances : gms.values()) {
			List<List<KnimeTuple>> tuplesList = new LinkedList<>();
			for (List<KnimeTuple> tertiaryInstance : tertiaryInstances.values()) {
				tuplesList.add(tertiaryInstance);
			}
			// We have a list of tertiary instances. Each instance has the same
			// microbial data yet different data. Then we'll create a
			// TwoTertiaryModel from the first instance and create the data from
			// every instance.
			TwoStepTertiaryModel tm = parse(tuplesList, modelCounter, metadata);
			tms.add(tm);

			modelCounter++;
		}

		if (splitModels) {
			for (int numModel = 0; numModel < tms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<TwoStepTertiaryModel> model = new LinkedList<>();
				model.add(tms.get(numModel));
				TwoStepTertiaryModelFile.write(dir, modelName, model, exec);
			}
		} else {
			TwoStepTertiaryModelFile.write(dir, mdName, tms, exec);
		}
	}

	private TwoStepTertiaryModel parse(List<List<KnimeTuple>> tupleList, int modelNum, Metadata metadata) {

		List<PrimaryModelWData> primModels = new LinkedList<>();
		List<SBMLDocument> secDocs = new LinkedList<>();

		// Parse primary models and their data from every instance. Each
		// instance has an unique primary model and data set
		for (List<KnimeTuple> instance : tupleList) {
			// Get first tuple: All the tuples of an instance have the same
			// primary model
			KnimeTuple tuple = instance.get(0);
			SBMLDocument sbmlDoc = new Model1Parser(tuple, metadata).getDocument();
			if (tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES).size() > 0) {
				NuMLDocument numlDoc = new DataParser(tuple, metadata).getDocument();
				primModels.add(new PrimaryModelWData(sbmlDoc, numlDoc));
			} else {
				primModels.add(new PrimaryModelWData(sbmlDoc, null));
			}
		}

		// Parse secondary models from the first instance (all the instance have
		// the same secondary models)
		List<KnimeTuple> firstInstance = tupleList.get(0);
		for (KnimeTuple tuple : firstInstance) {
			SBMLDocument secDoc = new Model2Parser(tuple, metadata).getDocument();
			secDocs.add(secDoc);
		}

		// Creates tertiary model
		SBMLDocument tertDoc = new SBMLDocument(TableReader.LEVEL, TableReader.VERSION);
		// Enable Hierarchical Compositon package
		tertDoc.enablePackage(CompConstants.shortLabel);
		CompSBMLDocumentPlugin compDocPlugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);
		TableReader.addNamespaces(tertDoc);

		// Adds document annotation
		tertDoc.setAnnotation(new MetadataAnnotation(metadata).getAnnotation());

		// Creates ExternalModelDefinition
		for (SBMLDocument secDoc : secDocs) {
			// Gets model definition id from secDoc
			String mdId = secDoc.getModel().getId();

			// Creates and adds an ExternalModelDefinition to the tertiary model
			ExternalModelDefinition emd = compDocPlugin.createExternalModelDefinition(mdId);
			emd.setSource(mdId + ".sbml");
			emd.setModelRef(mdId);
		}

		Model model = tertDoc.createModel("model");
		KnimeTuple aTuple = tupleList.get(0).get(0);

		// Builds metadata node
		XMLTriple metadataTriple = new XMLTriple("metadata", null, "pmf");
		XMLNode metadataNode = new XMLNode(metadataTriple);
		model.getAnnotation().setNonRDFAnnotation(metadataNode);

		// Builds global model id node
		int gmId = aTuple.getInt(Model2Schema.ATT_GLOBAL_MODEL_ID);
		metadataNode.addChild(new GlobalModelIdNode(gmId).getNode());

		// Get literature references
		PmmXmlDoc litDoc = aTuple.getPmmXml(Model1Schema.ATT_EMLIT);
		List<LiteratureItem> lits = new LinkedList<>();
		for (PmmXmlElementConvertable item : litDoc.getElementSet()) {
			lits.add((LiteratureItem) item);
		}

		// Builds reference nodes
		for (LiteratureItem lit : lits) {
			metadataNode.addChild(new ReferenceSBMLNode(lit).getNode());
		}

		// Gets a primary model
		Model primModel = primModels.get(0).getSBMLDoc().getModel();

		// Adds species
		Species species = primModel.getSpecies(0);
		model.addSpecies(new Species(species));

		// Adds compartment
		Compartment compartment = primModel.getCompartment(0);
		model.addCompartment(new Compartment(compartment));

		// Adds rule
		AssignmentRule rule = (AssignmentRule) primModel.getRule(0);
		model.addRule(new AssignmentRule(rule));

		// Assigns parameters of the primary model
		for (Parameter p : primModel.getListOfParameters()) {
			Parameter p2 = new Parameter(p);
			if (p2.isSetAnnotation()) {
				p2.setAnnotation(new Annotation());
			}
			model.addParameter(p2);
		}

		// Assigns unit definitions of the primary model
		model.setListOfUnitDefinitions(new ListOf<UnitDefinition>(primModel.getListOfUnitDefinitions()));

		TwoStepTertiaryModel tstm = new TwoStepTertiaryModel(tertDoc, primModels, secDocs);
		return tstm;
	}
}

/**
 * One Step Fit Tertiary Model
 * 
 * @author Miguel Alba
 */
class OneStepTertiaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception {

		metadata.setType(ModelType.ONE_STEP_TERTIARY_MODEL.toString());

		List<OneStepTertiaryModel> tms = new LinkedList<>();

		// Sort global models
		Map<Integer, Map<Integer, List<KnimeTuple>>> gms = TableReader.sortGlobalModels(tuples);

		// Parse tertiary models
		int modelCounter = 0;
		for (Map<Integer, List<KnimeTuple>> tertiaryInstances : gms.values()) {
			List<List<KnimeTuple>> tuplesList = new LinkedList<>();
			for (List<KnimeTuple> tertiaryInstance : tertiaryInstances.values()) {
				tuplesList.add(tertiaryInstance);
			}
			// We have a list of tertiary instances. Each instance has the same
			// microbial data yet different data. Then we'll create a
			// TwoTertiaryModel from the first instance and create the data from
			// every instance.
			OneStepTertiaryModel tm = parse(tuplesList, modelCounter, metadata);
			tms.add(tm);

			modelCounter++;
		}

		if (splitModels) {
			for (int numModel = 0; numModel < tms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<OneStepTertiaryModel> model = new LinkedList<>();
				model.add(tms.get(numModel));
				OneStepTertiaryModelFile.write(dir, modelName, model, exec);
			}
		} else {
			OneStepTertiaryModelFile.write(dir, mdName, tms, exec);
		}

	}

	private OneStepTertiaryModel parse(List<List<KnimeTuple>> tupleList, int modelNum, Metadata metadata) {
		// We'll get microbial data from the first instance
		List<KnimeTuple> firstInstance = tupleList.get(0);
		// and the primary model from the first tuple
		KnimeTuple firstTuple = firstInstance.get(0);

		SBMLDocument tertDoc = new Model1Parser(firstTuple, metadata).getDocument();
		CompSBMLDocumentPlugin compDocPlugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);

		// Add submodels and model definitions
		List<SBMLDocument> secDocs = new LinkedList<>();
		for (KnimeTuple tuple : firstInstance) {

			SBMLDocument secDoc = new Model2Parser(tuple, metadata).getDocument();

			// Creates and adds an ExternalModelDefinition
			String secModelId = secDoc.getModel().getId();
			ExternalModelDefinition emd = compDocPlugin.createExternalModelDefinition(secModelId);
			emd.setSource(secModelId + ".sbml");
			emd.setModelRef(secModelId);

			// Add annotation for the primary model
			XMLNode metadataNode = secDoc.getModel().getAnnotation().getNonRDFannotation().getChildElement("metadata",
					"");
			metadataNode.addChild(new PrimaryModelNode(tertDoc.getModel().getId() + ".sbml").getNode());

			// Save secondary model
			secDocs.add(secDoc);
		}

		List<NuMLDocument> numlDocs = new LinkedList<>();
		for (List<KnimeTuple> instance : tupleList) {
			// Get first tuple: All the tuples of an instance have the same data
			KnimeTuple tuple = instance.get(0);
			if (tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES).size() > 0) {
				numlDocs.add(new DataParser(tuple, metadata).getDocument());
			}
		}

		OneStepTertiaryModel tstm = new OneStepTertiaryModel(tertDoc, secDocs, numlDocs);
		return tstm;
	}
}

class ManualTertiaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			ExecutionContext exec) throws Exception {

		metadata.setType(ModelType.MANUAL_TERTIARY_MODEL.toString());

		List<ManualTertiaryModel> tms = new LinkedList<>();

		// Sort global models
		Map<Integer, Map<Integer, List<KnimeTuple>>> gms = TableReader.sortGlobalModels(tuples);

		// Parse tertiary models
		int modelCounter = 0;
		for (Map<Integer, List<KnimeTuple>> tertiaryInstances : gms.values()) {
			List<List<KnimeTuple>> tuplesList = new LinkedList<>();
			for (List<KnimeTuple> tertiaryInstance : tertiaryInstances.values()) {
				tuplesList.add(tertiaryInstance);
			}
			// We have a list of tertiary instances. Each instance has the same
			// microbial data yet different data. Then we'll create a
			// TwoTertiaryModel from the first instance and create the data from
			// every instance.
			ManualTertiaryModel tm = parse(tuplesList, mdName, modelCounter, metadata);
			tms.add(tm);

			modelCounter++;
		}

		if (splitModels) {
			for (int numModel = 0; numModel < tms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<ManualTertiaryModel> model = new LinkedList<>();
				model.add(tms.get(numModel));
				ManualTertiaryModelFile.write(dir, modelName, model, exec);
			}
		} else {
			ManualTertiaryModelFile.write(dir, mdName, tms, exec);
		}
	}

	private ManualTertiaryModel parse(List<List<KnimeTuple>> tupleList, String mdName, int modelNum,
			Metadata metadata) {
		// We'll get microbial data from the first instance
		List<KnimeTuple> firstInstance = tupleList.get(0);
		// and the primary model from the first tuple
		KnimeTuple firstTuple = firstInstance.get(0);

		// Creates SBMLDocument for the tertiary model
		SBMLDocument tertDoc = new Model1Parser(firstTuple, metadata).getDocument();
		CompSBMLDocumentPlugin compDocPlugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);
		CompModelPlugin compModelPlugin = (CompModelPlugin) tertDoc.getModel().getPlugin(CompConstants.shortLabel);

		// Add submodels and model definitions
		List<SBMLDocument> secDocs = new LinkedList<>();
		for (KnimeTuple tuple : firstInstance) {
			SBMLDocument secDoc = new Model2Parser(tuple, metadata).getDocument();

			// Creates ExternalModelDefinition
			String emdId = secDoc.getModel().getId();
			ExternalModelDefinition emd = new ExternalModelDefinition(emdId, TableReader.LEVEL, TableReader.VERSION);
			String emdSource = String.format("%s_%s_%s.%s", mdName, modelNum, emdId, "sbml");
			emd.setSource(emdSource);
			emd.setModelRef(emdId);

			compDocPlugin.addExternalModelDefinition(emd);

			Submodel submodel = compModelPlugin.createSubmodel(emdId);
			submodel.setModelRef(emdId);

			secDocs.add(secDoc); // Save secondary model
		}

		ManualTertiaryModel mtm = new ManualTertiaryModel(tertDoc, secDocs);
		return mtm;
	}
}

class DataParser {

	public NuMLDocument numlDocument;

	public DataParser(KnimeTuple tuple, Metadata metadata) {

		// Gets CondID and CombaseID
		int condId = tuple.getInt(TimeSeriesSchema.ATT_CONDID);
		String combaseId = tuple.getString(TimeSeriesSchema.ATT_COMBASEID);

		// Creates dim
		LinkedHashMap<Integer, List<Double>> dim = new LinkedHashMap<>();
		PmmXmlDoc mdData = tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES);
		for (PmmXmlElementConvertable item : mdData.getElementSet()) {
			TimeSeriesXml point = (TimeSeriesXml) item;
			dim.put(dim.size(), Arrays.asList(point.getTime(), point.getConcentration()));
		}

		// Gets first point
		TimeSeriesXml aPoint = (TimeSeriesXml) tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES).get(0);
		String concUnit = aPoint.getConcentrationUnit();
		String timeUnit = aPoint.getTimeUnit();

		MatrixXml matrixXml = (MatrixXml) tuple.getPmmXml(TimeSeriesSchema.ATT_MATRIX).get(0);

		Map<String, Double> miscs = new HashMap<>();
		for (PmmXmlElementConvertable item : tuple.getPmmXml(TimeSeriesSchema.ATT_MISC).getElementSet()) {
			MiscXml misc = (MiscXml) item;
			miscs.put(misc.getName(), misc.getValue());
		}
		Matrix matrix = new Matrix(matrixXml, miscs);

		AgentXml agentXml = (AgentXml) tuple.getPmmXml(TimeSeriesSchema.ATT_AGENT).get(0);
		Agent agent = new Agent(agentXml, concUnit, matrix.getCompartment(), null);

		// Gets microbial data literature
		List<LiteratureItem> lits = new LinkedList<>();
		for (PmmXmlElementConvertable item : tuple.getPmmXml(TimeSeriesSchema.ATT_LITMD).getElementSet()) {
			lits.add((LiteratureItem) item);
		}

		DataFile dataFile = new DataFile(condId, combaseId, dim, concUnit, timeUnit, matrix, agent, lits, metadata);
		numlDocument = dataFile.getDocument();
	}

	public NuMLDocument getDocument() {
		return numlDocument;
	}
}

class Model1Parser {

	SBMLDocument sbmlDocument;

	public Model1Parser(KnimeTuple tuple, Metadata metadata) {

		TableReader.replaceCelsiusAndFahrenheit(tuple);
		TableReader.renameLog(tuple);

		// Retrieves TimeSeriesSchema cells
		AgentXml agentXml = (AgentXml) tuple.getPmmXml(TimeSeriesSchema.ATT_AGENT).get(0);
		MatrixXml matrixXml = (MatrixXml) tuple.getPmmXml(TimeSeriesSchema.ATT_MATRIX).get(0);
		int condId = tuple.getInt(TimeSeriesSchema.ATT_CONDID);
		PmmXmlDoc miscDoc = tuple.getPmmXml(TimeSeriesSchema.ATT_MISC);

		// Retrieves Model1Schema cells
		CatalogModelXml catModel = (CatalogModelXml) tuple.getPmmXml(Model1Schema.ATT_MODELCATALOG).get(0);
		EstModelXml estModel = (EstModelXml) tuple.getPmmXml(Model1Schema.ATT_ESTMODEL).get(0);
		DepXml dep = (DepXml) tuple.getPmmXml(Model1Schema.ATT_DEPENDENT).get(0);
		IndepXml indep = (IndepXml) tuple.getPmmXml(Model1Schema.ATT_INDEPENDENT).get(0);
		PmmXmlDoc paramsDoc = tuple.getPmmXml(Model1Schema.ATT_PARAMETER);

		String modelId = Util.createId("model" + estModel.getId());

		// Creates SBMLDocument for the primary model
		sbmlDocument = new SBMLDocument(TableReader.LEVEL, TableReader.VERSION);

		// Enables Hierarchical Composition package and adds namespaces
		sbmlDocument.enablePackage(CompConstants.shortLabel);
		TableReader.addNamespaces(sbmlDocument);

		// Adds document annotation
		sbmlDocument.setAnnotation(new MetadataAnnotation(metadata).getAnnotation());

		// Creates model and names it
		Model model = sbmlDocument.createModel(modelId);
		if (estModel.getName() != null) {
			model.setName(estModel.getName());
		}

		// Gets model references
		List<LiteratureItem> mLits = new LinkedList<>();
		PmmXmlDoc mLitDoc = tuple.getPmmXml(Model1Schema.ATT_MLIT);
		for (PmmXmlElementConvertable item : mLitDoc.getElementSet()) {
			mLits.add((LiteratureItem) item);
		}

		// Gets estimated model references
		List<LiteratureItem> emLits = new LinkedList<>();
		PmmXmlDoc emLitDoc = tuple.getPmmXml(Model1Schema.ATT_EMLIT);
		for (PmmXmlElementConvertable item : emLitDoc.getElementSet()) {
			emLits.add((LiteratureItem) item);
		}

		Uncertainties uncertainties = new Uncertainties(estModel);
		model.setAnnotation(new Model1Annotation(uncertainties, emLits, condId).getAnnotation());

		// Creates and adds compartment to model
		Map<String, Double> miscsMap = new HashMap<>();
		for (PmmXmlElementConvertable item : miscDoc.getElementSet()) {
			MiscXml miscXml = (MiscXml) item;
			miscsMap.put(miscXml.getName(), miscXml.getValue());
		}
		Matrix matrix = new Matrix(matrixXml, miscsMap);

		Compartment compartment = matrix.getCompartment();
		model.addCompartment(compartment);

		// Creates species and adds it to the model
		Agent agent = new Agent(agentXml, dep.getUnit(), compartment, dep.getDescription());
		model.addSpecies(agent.getSpecies());

		// Adds dep constraint
		LimitsConstraint depLc = new LimitsConstraint(agent.getSpecies().getId(), dep.getMin(), dep.getMax());
		if (depLc.getConstraint() != null) {
			model.addConstraint(depLc.getConstraint());
		}

		// Adds independent parameter
		Parameter indepParam = new Parameter(Categories.getTime());
		indepParam.setValue(0.0);
		indepParam.setConstant(false);
		indepParam.setUnits(indep.getUnit());
		model.addParameter(indepParam);

		// Adds indep constraint
		if (!indep.getName().isEmpty()) {
			LimitsConstraint lc = new LimitsConstraint(indep.getName(), indep.getMin(), indep.getMax());
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Adds constant parameters
		List<ParamXml> constXmls = new LinkedList<>();
		for (PmmXmlElementConvertable item : paramsDoc.getElementSet()) {
			constXmls.add((ParamXml) item);
		}

		for (ParamXml paramXml : constXmls) {

			// Adds constant parameter
			Parameter param = new Coefficient(paramXml).getParameter();
			model.addParameter(param);

			// Adds constraint
			LimitsConstraint lc = new LimitsConstraint(paramXml.getName(), paramXml.getMin(), paramXml.getMax());
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Adds unit definitions
		List<IndepXml> indepXmls = new LinkedList<>(Arrays.asList(indep));
		TableReader.addUnitDefinitions(model, dep, indepXmls, constXmls);

		// Creates rule of the model and adds it to the rest of rules
		Model1Rule rule = Model1Rule.convertCatalogModelXmlToModel1Rule(catModel, agent.getSpecies().getId(), mLits);
		model.addRule(rule.getRule());
	}

	public SBMLDocument getDocument() {
		return sbmlDocument;
	}
}

class Model2Parser {

	SBMLDocument sbmlDocument;

	public Model2Parser(KnimeTuple tuple, Metadata metadata) {

		TableReader.replaceCelsiusAndFahrenheit(tuple);
		TableReader.renameLog(tuple);

		// Retrieve Model2Schema cells
		CatalogModelXml catModel = (CatalogModelXml) tuple.getPmmXml(Model2Schema.ATT_MODELCATALOG).get(0);
		EstModelXml estModel = (EstModelXml) tuple.getPmmXml(Model2Schema.ATT_ESTMODEL).get(0);
		DepXml dep = (DepXml) tuple.getPmmXml(Model2Schema.ATT_DEPENDENT).get(0);
		PmmXmlDoc indepDoc = tuple.getPmmXml(Model2Schema.ATT_INDEPENDENT);
		PmmXmlDoc paramsDoc = tuple.getPmmXml(Model2Schema.ATT_PARAMETER);
		PmmXmlDoc mLitDoc = tuple.getPmmXml(Model2Schema.ATT_MLIT);
		PmmXmlDoc emLitDoc = tuple.getPmmXml(Model2Schema.ATT_EMLIT);
		int globalModelID = tuple.getInt(Model2Schema.ATT_GLOBAL_MODEL_ID);

		// Creates SBMLDocument for the secondary model
		sbmlDocument = new SBMLDocument(TableReader.LEVEL, TableReader.VERSION);

		// Enables Hierarchical Composition package and adds namespaces
		sbmlDocument.enablePackage(CompConstants.shortLabel);
		TableReader.addNamespaces(sbmlDocument);

		// Adds document annotation
		sbmlDocument.setAnnotation(new MetadataAnnotation(metadata).getAnnotation());

		// Creates model and names it
		Model model = sbmlDocument.createModel("model_" + dep.getName());
		if (estModel.getName() != null) {
			model.setName(estModel.getName());
		}

		// Gets model references
		List<LiteratureItem> mLits = new LinkedList<>();
		for (PmmXmlElementConvertable item : mLitDoc.getElementSet()) {
			mLits.add((LiteratureItem) item);
		}

		// Gets estimated model references
		List<LiteratureItem> emLits = new LinkedList<>();
		for (PmmXmlElementConvertable item : emLitDoc.getElementSet()) {
			emLits.add((LiteratureItem) item);
		}

		// Adds model annotation
		Uncertainties uncertainties = new Uncertainties(estModel);
		model.setAnnotation(new Model2Annotation(globalModelID, uncertainties, emLits).getAnnotation());

		// Gets independent parameters
		List<IndepXml> indepXmls = new LinkedList<>();
		for (PmmXmlElementConvertable item : indepDoc.getElementSet()) {
			indepXmls.add((IndepXml) item);
		}

		// Gets constant parameters
		List<ParamXml> constXmls = new LinkedList<>();
		for (PmmXmlElementConvertable item : paramsDoc.getElementSet()) {
			constXmls.add((ParamXml) item);
		}

		// Adds dep
		Parameter depParam = new SecDep(dep).getParam();
		// Adds dep constraint
		LimitsConstraint depLc = new LimitsConstraint(dep.getName(), dep.getMin(), dep.getMax());
		if (depLc.getConstraint() != null) {
			model.addConstraint(depLc.getConstraint());
		}
		model.addParameter(depParam);

		// Adds independent parameters
		for (IndepXml indepXml : indepXmls) {
			// Creates SBML parameter
			model.addParameter(new SecIndep(indepXml).getParam());
			// Adds constraint
			LimitsConstraint lc = new LimitsConstraint(indepXml.getName(), indepXml.getMin(), indepXml.getMax());
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Adds constant parameters
		for (ParamXml paramXml : constXmls) {
			// Creates SBML parameter
			model.addParameter(new Coefficient(paramXml).getParameter());
			// Adds constraint
			LimitsConstraint lc = new LimitsConstraint(paramXml.getName(), paramXml.getMin(), paramXml.getMax());
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Adds unit definitions
		TableReader.addUnitDefinitions(model, dep, indepXmls, constXmls);

		// Creates rule of the model and adds it to the rest of rules
		Model2Rule rule = Model2Rule.convertCatalogModelXmlToModel2Rule(catModel, mLits);
		model.addRule(rule.getRule());
	}

	public SBMLDocument getDocument() {
		return sbmlDocument;
	}
}