/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.pmm.pmfxwriter;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

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
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLTriple;

import com.google.common.base.Strings;

import de.bund.bfr.knime.pmm.common.CatalogModelXml;
import de.bund.bfr.knime.pmm.common.DepXml;
import de.bund.bfr.knime.pmm.common.EstModelXml;
import de.bund.bfr.knime.pmm.common.IndepXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.ParamXml;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
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
import de.bund.bfr.knime.pmm.common.writer.DataParser;
import de.bund.bfr.knime.pmm.common.writer.Model1Parser;
import de.bund.bfr.knime.pmm.common.writer.Model2Parser;
import de.bund.bfr.knime.pmm.common.writer.Util;
import de.bund.bfr.pmf.ModelType;
import de.bund.bfr.pmf.PMFUtil;
import de.bund.bfr.pmf.file.ExperimentalDataFile;
import de.bund.bfr.pmf.file.ManualSecondaryModelFile;
import de.bund.bfr.pmf.file.ManualTertiaryModelFile;
import de.bund.bfr.pmf.file.OneStepSecondaryModelFile;
import de.bund.bfr.pmf.file.OneStepTertiaryModelFile;
import de.bund.bfr.pmf.file.PrimaryModelWDataFile;
import de.bund.bfr.pmf.file.PrimaryModelWODataFile;
import de.bund.bfr.pmf.file.TwoStepSecondaryModelFile;
import de.bund.bfr.pmf.file.TwoStepTertiaryModelFile;
import de.bund.bfr.pmf.model.ExperimentalData;
import de.bund.bfr.pmf.model.ManualSecondaryModel;
import de.bund.bfr.pmf.model.ManualTertiaryModel;
import de.bund.bfr.pmf.model.OneStepSecondaryModel;
import de.bund.bfr.pmf.model.OneStepTertiaryModel;
import de.bund.bfr.pmf.model.PrimaryModelWData;
import de.bund.bfr.pmf.model.PrimaryModelWOData;
import de.bund.bfr.pmf.model.TwoStepSecondaryModel;
import de.bund.bfr.pmf.model.TwoStepTertiaryModel;
import de.bund.bfr.pmf.numl.NuMLDocument;
import de.bund.bfr.pmf.sbml.DataSourceNode;
import de.bund.bfr.pmf.sbml.GlobalModelIdNode;
import de.bund.bfr.pmf.sbml.LimitsConstraint;
import de.bund.bfr.pmf.sbml.Metadata;
import de.bund.bfr.pmf.sbml.MetadataAnnotation;
import de.bund.bfr.pmf.sbml.Model2Annotation;
import de.bund.bfr.pmf.sbml.ModelRule;
import de.bund.bfr.pmf.sbml.PMFCoefficient;
import de.bund.bfr.pmf.sbml.PMFUnitDefinition;
import de.bund.bfr.pmf.sbml.PrimaryModelNode;
import de.bund.bfr.pmf.sbml.Reference;
import de.bund.bfr.pmf.sbml.ReferenceSBMLNode;
import de.bund.bfr.pmf.sbml.SBMLFactory;
import de.bund.bfr.pmf.sbml.SecDep;
import de.bund.bfr.pmf.sbml.SecIndep;
import de.bund.bfr.pmf.sbml.Uncertainties;

/**
 * Model implementation of PMFXWriter
 * 
 * @author Miguel Alba
 */
public class PMFXWriterNodeModel extends NodeModel {
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
	protected static final String CFG_REFERENCE_LINK = "referenceLink";
	protected static final String CFG_LIC = "license";
	protected static final String CFG_NOTES = "notes";

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
	private SettingsModelString referenceLink = new SettingsModelString(CFG_REFERENCE_LINK, null);
	private SettingsModelString license = new SettingsModelString(CFG_LIC, null);
	private SettingsModelString notes = new SettingsModelString(CFG_NOTES, null);

	Parser parser; // current parser

	protected PMFXWriterNodeModel() {
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
		Metadata metadata = SBMLFactory.createMetadata();

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
		metadata.setType(modelType);
		metadata.setRights(Strings.emptyToNull(license.getStringValue()));
		metadata.setReferenceLink(Strings.emptyToNull(referenceLink.getStringValue()));
		String modelNotes = Strings.emptyToNull(notes.getStringValue());

		String dir = outPath.getStringValue();
		String mdName = modelName.getStringValue();

		// Check for existing file -> shows warning if despite overwrite being
		// false the user still executes the nod
		String filepath = String.format("%s/%s.pmfx", dir, mdName);
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

		parser.write(tuples, dir, mdName, metadata, splitModels.getBooleanValue(), modelNotes, exec);

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
		license.saveSettingsTo(settings);
		referenceLink.saveSettingsTo(settings);
		notes.saveSettingsTo(settings);
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
		license.loadSettingsFrom(settings);
		referenceLink.loadSettingsFrom(settings);
		notes.loadSettingsFrom(settings);
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
		license.validateSettings(settings);
		referenceLink.validateSettings(settings);
		notes.validateSettings(settings);
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

	public static boolean hasData(List<KnimeTuple> tuples) {
		for (KnimeTuple tuple : tuples) {
			PmmXmlDoc mdData = tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES);
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
		final String CELSIUS = "°C";
		final String FAHRENHEIT = "°F";
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
			List<ParamXml> constXmls) throws XMLStreamException {
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
		// Missing units in DB will not be retrievable and thus will lack a list
		// of units
		for (String unit : units) {
			PMFUnitDefinition unitDefinition = Util.createUnitFromDB(unit);
			if (unitDefinition != null) {
				model.addUnitDefinition(unitDefinition.getUnitDefinition());
			}
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
			String notes, ExecutionContext exec) throws Exception;
}

/**
 * Parse tuples from a table with timeseries.
 */
class ExperimentalDataParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			String notes, ExecutionContext exec) throws Exception {

		List<ExperimentalData> eds = new LinkedList<>();
		for (int i = 0; i < tuples.size(); i++) {
			KnimeTuple tuple = tuples.get(i);

			String docName = String.format("%s_%d.numl", mdName, i);
			NuMLDocument doc = new DataParser(tuple, metadata, notes).getDocument();

			ExperimentalData ed = new ExperimentalData(docName, doc);
			eds.add(ed);
		}
		ExperimentalDataFile.writePMFX(dir, mdName, eds);
	}
}

/**
 * Parse tuples from a table with primary models with data.
 */
class PrimaryModelWDataParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			String notes, ExecutionContext exec) throws Exception {

		List<PrimaryModelWData> pms = new LinkedList<>();

		for (KnimeTuple tuple : tuples) {
			PrimaryModelWData pm;

			Model1Parser m1Parser = new Model1Parser(tuple, metadata, notes);
			SBMLDocument sbmlDoc = m1Parser.getDocument();
			String sbmlDocName = String.format("%s_%d.pmf", mdName, pms.size());

			if (tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES).size() > 0) {
				DataParser dataParser = new DataParser(tuple, metadata, notes);
				NuMLDocument numlDoc = dataParser.getDocument();
				String numlDocName = String.format("%s_%d.numl", mdName, pms.size());

				// Adds DataSourceNode to the model
				XMLNode dsn = new DataSourceNode(numlDocName).getNode();
				sbmlDoc.getModel().getAnnotation().getNonRDFannotation().getChildElement("metadata", "").addChild(dsn);

				pm = new PrimaryModelWData(sbmlDocName, sbmlDoc, numlDocName, numlDoc);
			} else {
				pm = new PrimaryModelWData(sbmlDocName, sbmlDoc, null, null);
			}
			pms.add(pm);
		}

		PrimaryModelWDataFile.writePMFX(dir, mdName, pms);
	}
}

/**
 * Parse tuples from a table with primary models without data.
 */
class PrimaryModelWODataParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			String notes, ExecutionContext exec) throws Exception {

		List<PrimaryModelWOData> pms = new LinkedList<>();
		for (KnimeTuple tuple : tuples) {
			Model1Parser m1Parser = new Model1Parser(tuple, metadata, notes);

			SBMLDocument sbmlDoc = m1Parser.getDocument();
			String sbmlDocName = String.format("%s_%d.pmf", mdName, pms.size());

			PrimaryModelWOData pm = new PrimaryModelWOData(sbmlDocName, sbmlDoc);
			pms.add(pm);
		}
		PrimaryModelWODataFile.writePMFX(dir, mdName, pms);
	}
}

/**
 * Parse tuples from a table with primary models without data.
 */
class TwoStepSecondaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			String notes, ExecutionContext exec) throws Exception {

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
			TwoStepSecondaryModel model = parse(tupleList, sms.size(), mdName, metadata, notes);
			sms.add(model);
		}

		if (splitModels) {
			for (int numModel = 0; numModel < sms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<TwoStepSecondaryModel> model = new LinkedList<>();
				model.add(sms.get(numModel));
				TwoStepSecondaryModelFile.writePMFX(dir, modelName, model);
			}
		} else {
			TwoStepSecondaryModelFile.writePMFX(dir, mdName, sms);
		}
	}

	private static TwoStepSecondaryModel parse(List<KnimeTuple> tuples, int modelNum, String mdName, Metadata metadata,
			String notes) {
		/**
		 * <ol>
		 * <li>Create n SBMLDocument for primary models</li>
		 * <li>Parse data and create n NuMLDocument</li>
		 * <li>Create SBMLDocument for secondary model</li>
		 * </ol>
		 */
		List<PrimaryModelWData> primModels = new LinkedList<>();
		for (int i = 0; i < tuples.size(); i++) {
			KnimeTuple tuple = tuples.get(i);
			PrimaryModelWData pm;

			Model1Parser m1Parser = new Model1Parser(tuple, metadata, notes);

			SBMLDocument sbmlDoc = m1Parser.getDocument();
			String sbmlDocName = String.format("%s.pmf", sbmlDoc.getModel().getId());

			XMLNode metadataNode = sbmlDoc.getModel().getAnnotation().getNonRDFannotation().getChildElement("metadata",
					"");
			if (tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES).size() > 0) {
				DataParser dataParser = new DataParser(tuple, metadata, notes);

				NuMLDocument numlDoc = dataParser.getDocument();
				String numlDocName = String.format("%s.numl", sbmlDoc.getModel().getId());

				// Adds DataSourceNode to the model
				DataSourceNode dsn = new DataSourceNode(numlDocName);
				metadataNode.addChild(dsn.getNode());

				pm = new PrimaryModelWData(sbmlDocName, sbmlDoc, numlDocName, numlDoc);
			} else {
				pm = new PrimaryModelWData(sbmlDocName, sbmlDoc, null, null);
			}

			primModels.add(pm);
		}

		// We get the first tuple to query the Model2 columns which are the same
		// for all the tuples of the secondary model
		KnimeTuple firstTuple = tuples.get(0);
		Model2Parser m2Parser = new Model2Parser(firstTuple, metadata, notes);

		SBMLDocument secDoc = m2Parser.getDocument();
		String secDocName = String.format("%s_%d.pmf", mdName, modelNum);
		// Adds annotation for the primary models
		XMLNode metadataNode = secDoc.getModel().getAnnotation().getNonRDFannotation().getChildElement("metadata", "");
		for (PrimaryModelWData pmwd : primModels) {
			PrimaryModelNode node = new PrimaryModelNode(pmwd.getModelDocName());
			metadataNode.addChild(node.getNode());
		}

		// Creates and return TwoStepSecondaryModel
		return new TwoStepSecondaryModel(secDocName, secDoc, primModels);
	}
}

/**
 * Parse tuples from a table with primary models without data.
 */
class OneStepSecondaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			String notes, ExecutionContext exec) throws Exception {

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
			int modelCounter = sms.size();
			OneStepSecondaryModel model = parse(ltup, mdName, modelCounter, metadata, notes);
			sms.add(model);
		}

		if (splitModels) {
			for (int numModel = 0; numModel < sms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<OneStepSecondaryModel> model = new LinkedList<>();
				model.add(sms.get(numModel));
				OneStepSecondaryModelFile.writePMFX(dir, modelName, model);
			}
		} else {
			OneStepSecondaryModelFile.writePMFX(dir, mdName, sms);
		}
	}

	private static OneStepSecondaryModel parse(List<KnimeTuple> tuples, String mdName, int modelNum, Metadata metadata,
			String notes) {
		KnimeTuple firstTuple = tuples.get(0);

		// Retrieve Model2Schema cells
		EstModelXml secEstModel = (EstModelXml) firstTuple.getPmmXml(Model2Schema.ATT_ESTMODEL).get(0);

		Model1Parser m1Parser = new Model1Parser(firstTuple, metadata, notes);
		SBMLDocument doc = m1Parser.getDocument();
		String docName = String.format("%s_%d.pmf", mdName, modelNum);

		Model model = doc.getModel();
		model.setId(PMFUtil.createId("model" + secEstModel.getId()));
		CompSBMLDocumentPlugin compDocPlugin = (CompSBMLDocumentPlugin) doc.getPlugin(CompConstants.shortLabel);
		CompModelPlugin compModelPlugin = (CompModelPlugin) model.getPlugin(CompConstants.shortLabel);

		// Create secondary model
		Model secModel = new Model2Parser(firstTuple, metadata, notes).getDocument().getModel();
		ModelDefinition md = new ModelDefinition(secModel);
		compDocPlugin.addModelDefinition(md);

		Submodel submodel = compModelPlugin.createSubmodel("submodel");
		submodel.setModelRef(secModel.getId());

		// Parse data sets and create NuML documents
		XMLNode metadataNode = md.getAnnotation().getNonRDFannotation().getChildElement("metadata", "");
		List<NuMLDocument> numlDocs = new LinkedList<>();
		List<String> numlDocNames = new LinkedList<>();
		for (KnimeTuple tuple : tuples) {
			String numlDocName = String.format("data%d.numl", numlDocs.size());
			numlDocNames.add(numlDocName);

			DataParser dataParser = new DataParser(tuple, metadata, notes);
			NuMLDocument numlDoc = dataParser.getDocument();
			numlDocs.add(numlDoc);

			// Adds DataSourceNode to the model
			metadataNode.addChild(new DataSourceNode(numlDocName).getNode());
		}

		OneStepSecondaryModel ossm = new OneStepSecondaryModel(docName, doc, numlDocNames, numlDocs);
		return ossm;
	}
}

/**
 * Parse tuples from a table with primary models without data.
 */
class ManualSecondaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			String notes, ExecutionContext exec) throws Exception {

		List<ManualSecondaryModel> sms = new LinkedList<>();
		for (KnimeTuple tuple : tuples) {
			int mdNum = sms.size();
			sms.add(parse(tuple, mdName, mdNum, metadata, notes));
		}

		if (splitModels) {
			for (int numModel = 0; numModel < sms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<ManualSecondaryModel> model = new LinkedList<>();
				model.add(sms.get(numModel));
				ManualSecondaryModelFile.writePMFX(dir, modelName, model);
			}
		} else {
			ManualSecondaryModelFile.writePMFX(dir, mdName, sms);
		}
	}

	private static ManualSecondaryModel parse(KnimeTuple tuple, String mdName, int mdNum, Metadata metadata,
			String notes) {

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

		String docName = String.format("%s_%d.pmf", mdName, mdNum);
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

		if (notes != null) {
			try {
				model.setNotes(notes);
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}

		try {
			TableReader.addUnitDefinitions(model, dep, indepXmls, constXmls);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Adds dep
		Parameter depParam = new SecDep(dep.getName(), dep.getDescription(), dep.getUnit()).getParam();
		// Adds dep constraint
		LimitsConstraint depLc = new LimitsConstraint(dep.getName(), dep.getMin(), dep.getMax());
		if (depLc.getConstraint() != null) {
			model.addConstraint(depLc.getConstraint());
		}
		model.addParameter(depParam);

		// Add independent parameters
		for (IndepXml indepXml : indepXmls) {
			// Creates SBML parameter
			// model.addParameter(new SecIndep(indepXml).getParam());
			SecIndep secIndep = new SecIndep(indepXml.getName(), indepXml.getDescription(), indepXml.getUnit());
			model.addParameter(secIndep.getParam());
			// Adds constraint
			LimitsConstraint lc = new LimitsConstraint(indepXml.getName(), indepXml.getMin(), indepXml.getMax());
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Adds constant parameters
		for (ParamXml paramXml : constXmls) {
			// Creates SBML parameter
			PMFCoefficient coefficient = Util.paramXml2Coefficient(paramXml);
			model.addParameter(coefficient.getParameter());

			// Adds constraint
			LimitsConstraint lc = new LimitsConstraint(paramXml.getName(), paramXml.getMin(), paramXml.getMax());
			if (lc.getConstraint() != null) {
				model.addConstraint(lc.getConstraint());
			}
		}

		// Gets model literature
		Reference[] mLits = new Reference[mLitDoc.size()];
		for (int i = 0; i < mLitDoc.size(); i++) {
			mLits[i] = Util.literatureItem2Reference((LiteratureItem) mLitDoc.get(i));
		}

		// Gets estimated model literature
		Reference[] emLits = new Reference[emLitDoc.size()];
		for (int i = 0; i < emLitDoc.size(); i++) {
			emLits[i] = Util.literatureItem2Reference((LiteratureItem) emLitDoc.get(i));
		}

		ModelRule rule2 = Util.createM2Rule(catModel, mLits);
		model.addRule(rule2.getRule());

		// Add annotation
		Uncertainties uncertainties = Util.estModel2Uncertainties(estModel);
		model.setAnnotation(new Model2Annotation(globalModelID, uncertainties, emLits).getAnnotation());

		return new ManualSecondaryModel(docName, doc);
	}
}

class TwoStepTertiaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			String notes, ExecutionContext exec) throws Exception {

		List<TwoStepTertiaryModel> tms = new LinkedList<>();

		// Sort global models
		Map<Integer, Map<Integer, List<KnimeTuple>>> gms = TableReader.sortGlobalModels(tuples);

		for (Map<Integer, List<KnimeTuple>> tertiaryInstances : gms.values()) {
			List<List<KnimeTuple>> tuplesList = new LinkedList<>();
			for (List<KnimeTuple> tertiaryInstance : tertiaryInstances.values()) {
				tuplesList.add(tertiaryInstance);
			}
			// We have a list of tertiary instances. Each instance has the same
			// microbial data yet different data. Then we'll create a
			// TwoTertiaryModel from the first instance and create the data from
			// every instance.
			int modelNum = tms.size();
			TwoStepTertiaryModel tm = parse(tuplesList, modelNum, mdName, metadata, notes);
			tms.add(tm);
		}

		if (splitModels) {
			for (int numModel = 0; numModel < tms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<TwoStepTertiaryModel> model = new LinkedList<>();
				model.add(tms.get(numModel));
				TwoStepTertiaryModelFile.writePMFX(dir, modelName, model);
			}
		} else {
			TwoStepTertiaryModelFile.writePMFX(dir, mdName, tms);
		}
	}

	private static TwoStepTertiaryModel parse(List<List<KnimeTuple>> tupleList, int modelNum, String mdName,
			Metadata metadata, String notes) {

		List<PrimaryModelWData> primModels = new LinkedList<>();
		List<SBMLDocument> secDocs = new LinkedList<>();

		// Parse primary models and their data from every instance. Each
		// instance has an unique primary model and data set
		for (List<KnimeTuple> instance : tupleList) {
			// Get first tuple: All the tuples of an instance have the same
			// primary model
			KnimeTuple tuple = instance.get(0);
			int instanceNum = primModels.size();
			PrimaryModelWData pm;

			Model1Parser m1Parser = new Model1Parser(tuple, metadata, notes);

			SBMLDocument sbmlDoc = m1Parser.getDocument();
			String sbmlDocName = String.format("%s_%d_%d.pmf", mdName, modelNum, instanceNum);
			XMLNode metadataNode = sbmlDoc.getModel().getAnnotation().getNonRDFannotation().getChildElement("metadata",
					"");

			if (tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES).size() > 0) {
				NuMLDocument numlDoc = new DataParser(tuple, metadata, notes).getDocument();
				String numlDocName = String.format("%s_%d_%d.numl", mdName, modelNum, instanceNum);

				// Adds DataSourceNode to the model
				metadataNode.addChild(new DataSourceNode(numlDocName).getNode());

				primModels.add(new PrimaryModelWData(sbmlDocName, sbmlDoc, numlDocName, numlDoc));

			} else {
				pm = new PrimaryModelWData(sbmlDocName, sbmlDoc, null, null);
				primModels.add(pm);
			}
		}

		// Parse secondary models from the first instance (all the instance have
		// the same secondary models)
		List<KnimeTuple> firstInstance = tupleList.get(0);
		for (KnimeTuple tuple : firstInstance) {
			SBMLDocument secDoc = new Model2Parser(tuple, metadata, notes).getDocument();

			// Adds annotations for the primary models
			XMLNode metadataNode = secDoc.getModel().getAnnotation().getNonRDFannotation().getChildElement("metadata",
					"");
			for (PrimaryModelWData pm : primModels) {
				if (pm.getDataDocName() != null) {
					metadataNode.addChild(new PrimaryModelNode(pm.getModelDocName()).getNode());
				}
			}

			secDocs.add(secDoc);
		}

		// Creates tertiary model
		String tertDocName = String.format("%s_%s.pmf", mdName, modelNum);
		SBMLDocument tertDoc = new SBMLDocument(TableReader.LEVEL, TableReader.VERSION);
		// Enable Hierarchical Compositon package
		tertDoc.enablePackage(CompConstants.shortLabel);
		CompSBMLDocumentPlugin compDocPlugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);
		TableReader.addNamespaces(tertDoc);

		// Adds document annotation
		tertDoc.setAnnotation(new MetadataAnnotation(metadata).getAnnotation());

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
			Reference ref = Util.literatureItem2Reference(lit);
			metadataNode.addChild(new ReferenceSBMLNode(ref).getNode());
		}

		// Gets a primary model
		Model primModel = primModels.get(0).getModelDoc().getModel();

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
		
		CompModelPlugin modelPlugin = (CompModelPlugin) model.getPlugin(CompConstants.shortLabel);

		// Creates ExternalModelDefinition
		List<String> secDocNames = new LinkedList<>();
		for (SBMLDocument secDoc : secDocs) {
			// Gets model definition id from secDoc
			String mdId = secDoc.getModel().getId();

			String secDocName = secDoc.getModel().getId() + ".pmf";
			secDocNames.add(secDocName);

			// Creates and adds an ExternalModelDefinition to the tertiary model
			ExternalModelDefinition emd = compDocPlugin.createExternalModelDefinition(mdId);
			emd.setSource(secDocName);
			emd.setModelRef(mdId);
			
			String depId = ((AssignmentRule) secDoc.getModel().getRule(0)).getVariable();

			// Creates submodel
			Submodel submodel = modelPlugin.createSubmodel("submodel_" + depId);
			submodel.setModelRef(mdId);

			Parameter parameter = model.getParameter(depId);

			CompSBasePlugin plugin = (CompSBasePlugin) parameter.getPlugin(CompConstants.shortLabel);
			ReplacedBy replacedBy = plugin.createReplacedBy();
			replacedBy.setIdRef(depId);
			replacedBy.setSubmodelRef(submodel.getId());
		}

		// Assigns unit definitions of the primary model
		model.setListOfUnitDefinitions(new ListOf<UnitDefinition>(primModel.getListOfUnitDefinitions()));

		TwoStepTertiaryModel tstm = new TwoStepTertiaryModel(tertDocName, tertDoc, primModels, secDocNames, secDocs);
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
			String notes, ExecutionContext exec) throws Exception {

		List<OneStepTertiaryModel> tms = new LinkedList<>();

		// Sort global models
		Map<Integer, Map<Integer, List<KnimeTuple>>> gms = TableReader.sortGlobalModels(tuples);

		// Parse tertiary models
		for (Map<Integer, List<KnimeTuple>> tertiaryInstances : gms.values()) {
			List<List<KnimeTuple>> tuplesList = new LinkedList<>();
			for (List<KnimeTuple> tertiaryInstance : tertiaryInstances.values()) {
				tuplesList.add(tertiaryInstance);
			}
			/**
			 * We have a list of tertiary instances. Each instance has the same
			 * microbial data yet different data. Then we'll create a
			 * TwoTertiaryModel from the first instance and create the data from
			 * every instance.
			 */
			int mdNum = tms.size();
			OneStepTertiaryModel tm = parse(tuplesList, mdName, mdNum, metadata, notes);
			tms.add(tm);
		}

		if (splitModels) {
			for (int numModel = 0; numModel < tms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<OneStepTertiaryModel> model = new LinkedList<>();
				model.add(tms.get(numModel));
				OneStepTertiaryModelFile.writePMFX(dir, modelName, model);
			}
		} else {
			OneStepTertiaryModelFile.writePMFX(dir, mdName, tms);
		}
	}

	private static OneStepTertiaryModel parse(List<List<KnimeTuple>> tupleList, String mdName, int mdNum,
			Metadata metadata, String notes) {

		List<String> numlDocNames = new LinkedList<>();
		List<NuMLDocument> numlDocs = new LinkedList<>();
		for (List<KnimeTuple> instance : tupleList) {
			// Get first tuple: All the tuples of an instance have the same data
			KnimeTuple tuple = instance.get(0);
			if (tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES).size() > 0) {
				int dataCounter = numlDocs.size();
				String numlDocName = String.format("data_%d_%d.numl", mdNum, dataCounter);
				numlDocNames.add(numlDocName);

				DataParser dataParser = new DataParser(tuple, metadata, notes);
				NuMLDocument numlDoc = dataParser.getDocument();
				numlDocs.add(numlDoc);
			}
		}

		// We'll get microbial data from the first instance
		List<KnimeTuple> firstInstance = tupleList.get(0);
		// and the primary model from the first tuple
		KnimeTuple firstTuple = firstInstance.get(0);

		Model1Parser m1Parser = new Model1Parser(firstTuple, metadata, notes);
		SBMLDocument tertDoc = m1Parser.getDocument();
		String tertDocName = String.format("%s_%s.sbml", mdName, mdNum);
		CompSBMLDocumentPlugin compDocPlugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);

		// Adds DataSourceNode to the tertiary model
		XMLNode tertMetadataNode = tertDoc.getModel().getAnnotation().getNonRDFannotation().getChildElement("metadata",
				"");
		for (String numlDocName : numlDocNames) {
			tertMetadataNode.addChild(new DataSourceNode(numlDocName).getNode());
		}
		
		CompModelPlugin modelPlugin = (CompModelPlugin) tertDoc.getModel().getPlugin(CompConstants.shortLabel);

		// Add submodels and model definitions
		List<String> secDocNames = new LinkedList<>();
		List<SBMLDocument> secDocs = new LinkedList<>();
		for (KnimeTuple tuple : firstInstance) {

			SBMLDocument secDoc = new Model2Parser(tuple, metadata, notes).getDocument();

			String secModelId = secDoc.getModel().getId();
			String secDocName = secModelId + ".sbml";

			secDocNames.add(secDocName);
			secDocs.add(secDoc);

			// Creates and adds an ExternalModelDefinition
			ExternalModelDefinition emd = compDocPlugin.createExternalModelDefinition(secModelId);
			emd.setSource(secDocName);
			emd.setModelRef(secModelId);

			String depId = ((AssignmentRule) secDoc.getModel().getRule(0)).getVariable();
			
			Submodel submodel = modelPlugin.createSubmodel("submodel_" + depId);
			submodel.setModelRef(emd.getId());
			
			Parameter parameter = tertDoc.getModel().getParameter(depId);

			CompSBasePlugin plugin = (CompSBasePlugin) parameter.getPlugin(CompConstants.shortLabel);
			ReplacedBy replacedBy = plugin.createReplacedBy();
			replacedBy.setIdRef(depId);
			replacedBy.setSubmodelRef(submodel.getId());

			// Add annotation for the primary model
			XMLNode secMetadataNode = secDoc.getModel().getAnnotation().getNonRDFannotation()
					.getChildElement("metadata", "");
			secMetadataNode.addChild(new PrimaryModelNode(tertDocName).getNode());

			// Adds DataSourceNodes to the sec model
			for (String numlDocName : numlDocNames) {
				secMetadataNode.addChild(new DataSourceNode(numlDocName).getNode());
			}
		}

		OneStepTertiaryModel tstm = new OneStepTertiaryModel(tertDocName, tertDoc, secDocNames, secDocs, numlDocNames,
				numlDocs);
		return tstm;
	}
}

class ManualTertiaryModelParser implements Parser {

	@Override
	public void write(List<KnimeTuple> tuples, String dir, String mdName, Metadata metadata, boolean splitModels,
			String notes, ExecutionContext exec) throws Exception {

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
			ManualTertiaryModel tm = parse(tuplesList, mdName, modelCounter, metadata, notes);
			tms.add(tm);

			modelCounter++;
		}

		if (splitModels) {
			for (int numModel = 0; numModel < tms.size(); numModel++) {
				String modelName = mdName + Integer.toString(numModel);
				List<ManualTertiaryModel> model = new LinkedList<>();
				model.add(tms.get(numModel));
				ManualTertiaryModelFile.writePMFX(dir, modelName, model);
			}
		} else {
			ManualTertiaryModelFile.writePMFX(dir, mdName, tms);
		}
	}

	private static ManualTertiaryModel parse(List<List<KnimeTuple>> tupleList, String mdName, int modelNum,
			Metadata metadata, String notes) {
		// We'll get microbial data from the first instance
		List<KnimeTuple> firstInstance = tupleList.get(0);
		// and the primary model from the first tuple
		KnimeTuple firstTuple = firstInstance.get(0);

		// Creates SBMLDocument for the tertiary model
		Model1Parser m1Parser = new Model1Parser(firstTuple, metadata, notes);
		SBMLDocument tertDoc = m1Parser.getDocument();
		String tertDocName = String.format("%s_%s.pmf", mdName, modelNum);

		CompSBMLDocumentPlugin compDocPlugin = (CompSBMLDocumentPlugin) tertDoc.getPlugin(CompConstants.shortLabel);
		CompModelPlugin compModelPlugin = (CompModelPlugin) tertDoc.getModel().getPlugin(CompConstants.shortLabel);

		// Add submodels and model definitions
		List<String> secDocNames = new LinkedList<>();
		List<SBMLDocument> secDocs = new LinkedList<>();

		for (int i = 0; i < firstInstance.size(); i++) {
			KnimeTuple tuple = firstInstance.get(i);

			Model2Parser m2Parser = new Model2Parser(tuple, metadata, notes);
			SBMLDocument secDoc = m2Parser.getDocument();

			String emdId = secDoc.getModel().getId();
			String secDocName = String.format("%s_%s.pmf", mdName, emdId);

			secDocNames.add(secDocName);
			secDocs.add(secDoc);

			// Creates ExternalModelDefinition
			ExternalModelDefinition emd = new ExternalModelDefinition(emdId, TableReader.LEVEL, TableReader.VERSION);
			emd.setSource(secDocName);
			emd.setModelRef(emdId);

			compDocPlugin.addExternalModelDefinition(emd);

			Submodel submodel = compModelPlugin.createSubmodel(emdId);
			submodel.setModelRef(emdId);

			String depId = ((AssignmentRule) secDoc.getModel().getRule(0)).getVariable();
			Parameter parameter = tertDoc.getModel().getParameter(depId);

			CompSBasePlugin plugin = (CompSBasePlugin) parameter.getPlugin(CompConstants.shortLabel);
			ReplacedBy replacedBy = plugin.createReplacedBy();
			replacedBy.setIdRef(depId);
			replacedBy.setSubmodelRef(emdId);
		}

		ManualTertiaryModel mtm = new ManualTertiaryModel(tertDocName, tertDoc, secDocNames, secDocs);
		return mtm;
	}
}
