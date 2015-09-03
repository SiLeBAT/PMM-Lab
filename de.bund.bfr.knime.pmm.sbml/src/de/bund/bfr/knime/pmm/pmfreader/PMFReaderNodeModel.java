package de.bund.bfr.knime.pmm.pmfreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
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
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ModelDefinition;

import de.bund.bfr.knime.pmm.common.AgentXml;
import de.bund.bfr.knime.pmm.common.CatalogModelXml;
import de.bund.bfr.knime.pmm.common.DepXml;
import de.bund.bfr.knime.pmm.common.EstModelXml;
import de.bund.bfr.knime.pmm.common.IndepXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MatrixXml;
import de.bund.bfr.knime.pmm.common.MdInfoXml;
import de.bund.bfr.knime.pmm.common.ParamXml;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;
import de.bund.bfr.knime.pmm.common.units.Categories;
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
import de.bund.bfr.knime.pmm.sbmlutil.Limits;
import de.bund.bfr.knime.pmm.sbmlutil.Matrix;
import de.bund.bfr.knime.pmm.sbmlutil.Model1Annotation;
import de.bund.bfr.knime.pmm.sbmlutil.Model1Rule;
import de.bund.bfr.knime.pmm.sbmlutil.Model2Annotation;
import de.bund.bfr.knime.pmm.sbmlutil.Model2Rule;
import de.bund.bfr.knime.pmm.sbmlutil.ModelType;
import de.bund.bfr.knime.pmm.sbmlutil.ReaderUtils;
import de.bund.bfr.knime.pmm.sbmlutil.SecIndep;
import de.bund.bfr.knime.pmm.sbmlutil.Uncertainties;
import de.bund.bfr.numl.NuMLDocument;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.meta.MetaDataObject;

/**
 * This is the model implementation of SBMLReader.
 * 
 * Author: Miguel de Alba Aparicio (malba@optimumquality.es)
 */
public class PMFReaderNodeModel extends NodeModel {

	// configuration keys
	public static final String CFGKEY_FILE = "filename";
	// defaults for persistent state
	private static final String DEFAULT_FILE = "c:/temp/foo.xml";

	// persistent state
	private SettingsModelString filename = new SettingsModelString(CFGKEY_FILE, DEFAULT_FILE);

	Reader reader; // current reader

	/**
	 * Constructor for the node model.
	 */
	protected PMFReaderNodeModel() {
		// 0 input ports and 1 input port
		super(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		BufferedDataTable[] table = null;
		table = loadPMF(exec);
		return table;
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
		return new DataTableSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		filename.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		filename.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		// TODO check if the settings could be applied to our model
		// e.g. if the count is in a certain range (which is ensured by the
		// SettingsModel).
		// Do not actually set any values of any member variables.
		filename.validateSettings(settings);
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

	// Load PMF file
	private BufferedDataTable[] loadPMF(final ExecutionContext exec) throws Exception {
		// Get model type from annotation in the metadata file

		// a) Open archive
		String filepath = filename.getStringValue();
		CombineArchive ca = new CombineArchive(new File(filepath));

		// b) Get annotation
		MetaDataObject mdo = ca.getDescriptions().get(0);
		Element metaParent = mdo.getXmlDescription();
		Element metaElement = metaParent.getChild("modeltype");
		String modelType = metaElement.getText();

		// c) Close archive
		ca.close();

		if (modelType.equals(ModelType.EXPERIMENTAL_DATA.name())) {
			reader = new ExperimentalDataReader();
		} else if (modelType.equals(ModelType.PRIMARY_MODEL_WDATA.name())) {
			reader = new PrimaryModelWDataReader();
		} else if (modelType.equals(ModelType.PRIMARY_MODEL_WODATA.name())) {
			reader = new PrimaryModelWODataReader();
		} else if (modelType.equals(ModelType.TWO_STEP_SECONDARY_MODEL.name())) {
			reader = new TwoStepSecondaryModelReader();
		} else if (modelType.equals(ModelType.ONE_STEP_SECONDARY_MODEL.name())) {
			reader = new OneStepSecondaryModelReader();
		} else if (modelType.equals(ModelType.MANUAL_SECONDARY_MODEL.name())) {
			reader = new ManualSecondaryModelReader();
		} else if (modelType.equals(ModelType.TWO_STEP_TERTIARY_MODEL.name())) {
			reader = new TwoStepTertiaryModelReader();
		} else if (modelType.equals(ModelType.ONE_STEP_TERTIARY_MODEL.name())) {
			reader = new OneStepTertiaryModelReader();
		} else if (modelType.equals(ModelType.MANUAL_TERTIARY_MODEL.name())) {
			reader = new ManualTertiaryModelReader();
		}

		BufferedDataContainer container = reader.read(filepath, exec);
		BufferedDataTable[] table = { container.getTable() };
		return table;
	}
}

/**
 * Reader interface
 * 
 * @author Miguel Alba
 */
interface Reader {
	/**
	 * Read models from a CombineArchive and returns a Knime table with them
	 * 
	 * @throws Exception
	 */
	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception;
}

class ExperimentalDataReader implements Reader {

	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec spec = SchemaFactory.createDataSchema().createSpec();
		BufferedDataContainer container = exec.createDataContainer(spec);

		// Reads in experimental data from file
		List<ExperimentalData> eds = ExperimentalDataFile.read(filepath);

		// Creates tuples and adds them to the container
		for (ExperimentalData ed : eds) {
			KnimeTuple tuple = parse(ed);
			container.addRowToTable(tuple);
			exec.setProgress((float) container.size() / eds.size());
		}

		container.close();
		return container;
	}

	private KnimeTuple parse(ExperimentalData ed) {
		NuMLDocument numlDoc = ed.getNuMLDoc();

		DataFile df = new DataFile(numlDoc);
		String timeUnit = df.getTimeUnit();
		String concUnit = df.getConcUnit();

		// Gets concentration unit object type from DB
		UnitsFromDB ufdb = DBUnits.getDBUnits().get(concUnit);
		String concUnitObjectType = ufdb.getObject_type();

		MatrixXml matrixXml = df.getMatrix();
		AgentXml agentXml = df.getAgent();

		// Gets time series
		PmmXmlDoc mdData = ReaderUtils.createTimeSeries(timeUnit, concUnit, concUnitObjectType, df.getData());

		PmmXmlDoc miscDoc = ReaderUtils.parseMiscs(df.getMiscs());

		PmmXmlDoc litDoc = new PmmXmlDoc();
		for (LiteratureItem lit : df.getLits()) {
			litDoc.add(lit);
		}

		// Creates empty model info
		MdInfoXml mdInfo = new MdInfoXml(null, "", "", null, false);

		// Creates and fills tuple
		KnimeTuple tuple = new KnimeTuple(SchemaFactory.createDataSchema());
		tuple.setValue(TimeSeriesSchema.ATT_CONDID, df.getCondID());
		tuple.setValue(TimeSeriesSchema.ATT_COMBASEID, df.getCombaseID());
		tuple.setValue(TimeSeriesSchema.ATT_AGENT, new PmmXmlDoc(agentXml));
		tuple.setValue(TimeSeriesSchema.ATT_MATRIX, new PmmXmlDoc(matrixXml));
		tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES, mdData);
		tuple.setValue(TimeSeriesSchema.ATT_MISC, miscDoc);
		tuple.setValue(TimeSeriesSchema.ATT_MDINFO, new PmmXmlDoc(mdInfo));
		tuple.setValue(TimeSeriesSchema.ATT_LITMD, litDoc);
		tuple.setValue(TimeSeriesSchema.ATT_DBUUID, "?");
		return tuple;
	}
}

class PrimaryModelWDataReader implements Reader {

	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec spec = SchemaFactory.createM1DataSchema().createSpec();
		BufferedDataContainer container = exec.createDataContainer(spec);

		// Reads in models from file
		List<PrimaryModelWData> models = PrimaryModelWDataFile.read(filepath);

		// Creates tuples and adds them to the container
		for (PrimaryModelWData model : models) {
			KnimeTuple tuple = parse(model);
			container.addRowToTable(tuple);
			exec.setProgress((float) container.size() / models.size());
		}

		container.close();
		return container;
	}

	private KnimeTuple parse(PrimaryModelWData pm) {
		SBMLDocument sbmlDoc = pm.getSBMLDoc();
		NuMLDocument numlDoc = pm.getNuMLDoc();

		Model model = sbmlDoc.getModel();

		// Parse model annotations
		Model1Annotation m1Annot = new Model1Annotation(model.getAnnotation().getNonRDFannotation());

		Model1Rule rule = new Model1Rule((AssignmentRule) model.getRule(0));
		CatalogModelXml catModel = rule.toCatModel();

		// Parse constraints
		Map<String, Limits> limits = ReaderUtils.parseConstraints(model.getListOfConstraints());

		// time series cells
		int condID = m1Annot.getCondID();
		Agent agent = new Agent(model.getSpecies(0));
		Matrix matrix = new Matrix(model.getCompartment(0));

		DataFile df = new DataFile(numlDoc);
		condID = df.getCondID();
		String combaseId = df.getCombaseID();

		String timeUnit = df.getTimeUnit();
		String concUnit = df.getConcUnit();

		// Gets concentration unit object type from DB
		UnitsFromDB ufdb = DBUnits.getDBUnits().get(concUnit);
		String concUnitObjectType = ufdb.getObject_type();

		// Gets data
		PmmXmlDoc mdDataCell = ReaderUtils.createTimeSeries(timeUnit, concUnit, concUnitObjectType, df.getData());

		// Gets literature
		PmmXmlDoc mdLitCell = new PmmXmlDoc();
		for (LiteratureItem lit : df.getLits()) {
			mdLitCell.add(lit);
		}

		// Parse model variables: Temperature, pH and water activity
		PmmXmlDoc miscCell = ReaderUtils.parseMiscs(matrix.getMiscs());

		MdInfoXml mdInfo = new MdInfoXml(null, null, null, null, null);

		// primary model cells
		// Parse dependent parameter (primary models only have one dependent
		// variable)
		DepXml depXml = new DepXml("Value");
		String depUnitID = agent.getSpecies().getUnits();
		if (depUnitID != null) {
			String depUnitName = model.getUnitDefinition(depUnitID).getName();
			depXml.setUnit(depUnitName);
			depXml.setCategory(DBUnits.getDBUnits().get(depUnitName).getKind_of_property_quantity());
		}
		depXml.setDescription(agent.getDescription());

		// Parse indep
		Parameter indepParam = model.getParameter(Categories.getTime());
		IndepXml indepXml = new IndepXml(indepParam.getId(), null, null);
		String indepUnitID = indepParam.getUnits();
		if (!indepUnitID.equalsIgnoreCase(Unit.Kind.DIMENSIONLESS.getName())) {
			String unitName = model.getUnitDefinition(indepUnitID).getName();
			indepXml.setUnit(unitName);
			indepXml.setCategory(Categories.getTimeCategory().getName());
			indepXml.setDescription(Categories.getTime());
		}
		// Get limits
		if (limits.containsKey(indepParam.getId())) {
			Limits indepLimits = limits.get(indepParam.getId());
			indepXml.setMax(indepLimits.getMax());
			indepXml.setMin(indepLimits.getMin());
		}

		// Parse Consts
		LinkedList<Parameter> constParams = new LinkedList<>();
		for (Parameter param : model.getListOfParameters()) {
			if (param.isConstant()) {
				constParams.add(param);
			}
		}

		PmmXmlDoc paramCell = new PmmXmlDoc();
		for (Parameter constParam : constParams) {
			ParamXml paramXml = new Coefficient(constParam).toParamXml();

			// Assign unit and category
			String unitID = constParam.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = model.getUnitDefinition(unitID).getName();
				paramXml.setUnit(unitName);
				paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (limits.containsKey(constParam.getId())) {
				Limits constLimits = limits.get(constParam.getId());
				paramXml.setMax(constLimits.getMax());
				paramXml.setMin(constLimits.getMin());
			}

			paramCell.add(paramXml);
		}

		Uncertainties uncertainties = m1Annot.getUncertainties();
		EstModelXml estModel = uncertainties.getEstModelXml();
		if (model.isSetName()) {
			estModel.setName(model.getName());
		}
		PmmXmlDoc estModelCell = new PmmXmlDoc(estModel);

		PmmXmlDoc emLiteratureCell = new PmmXmlDoc();
		for (LiteratureItem lit : m1Annot.getLits()) {
			emLiteratureCell.add(lit);
		}

		// Add cells to the row
		KnimeTuple row = new KnimeTuple(SchemaFactory.createM1DataSchema());

		// time series cells
		row.setValue(TimeSeriesSchema.ATT_CONDID, condID);
		row.setValue(TimeSeriesSchema.ATT_COMBASEID, combaseId);
		row.setValue(TimeSeriesSchema.ATT_AGENT, new PmmXmlDoc(agent.toAgentXml()));
		row.setValue(TimeSeriesSchema.ATT_MATRIX, new PmmXmlDoc(matrix.toMatrixXml()));
		row.setValue(TimeSeriesSchema.ATT_TIMESERIES, mdDataCell);
		row.setValue(TimeSeriesSchema.ATT_MISC, miscCell);
		row.setValue(TimeSeriesSchema.ATT_MDINFO, new PmmXmlDoc(mdInfo));
		row.setValue(TimeSeriesSchema.ATT_LITMD, mdLitCell);
		row.setValue(TimeSeriesSchema.ATT_DBUUID, "?");

		// primary model cells
		row.setValue(Model1Schema.ATT_MODELCATALOG, new PmmXmlDoc(catModel));
		row.setValue(Model1Schema.ATT_DEPENDENT, new PmmXmlDoc(depXml));
		row.setValue(Model1Schema.ATT_INDEPENDENT, new PmmXmlDoc(indepXml));
		row.setValue(Model1Schema.ATT_PARAMETER, paramCell);
		row.setValue(Model1Schema.ATT_ESTMODEL, estModelCell);
		row.setValue(Model1Schema.ATT_MLIT, new PmmXmlDoc());
		row.setValue(Model1Schema.ATT_EMLIT, emLiteratureCell);
		row.setValue(Model1Schema.ATT_DATABASEWRITABLE, Model1Schema.WRITABLE);
		row.setValue(Model1Schema.ATT_DBUUID, "?");

		return row;
	}
}

class PrimaryModelWODataReader implements Reader {

	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec spec = SchemaFactory.createM1DataSchema().createSpec();
		BufferedDataContainer container = exec.createDataContainer(spec);

		// Reads in models from file
		List<PrimaryModelWOData> models = PrimaryModelWODataFile.read(filepath);

		// Creates tuples and adds them to the container
		for (PrimaryModelWOData model : models) {
			KnimeTuple tuple = parse(model);
			container.addRowToTable(tuple);
			exec.setProgress((float) container.size() / models.size());
		}

		container.close();
		return container;
	}

	private KnimeTuple parse(PrimaryModelWOData pm) {
		SBMLDocument sbmlDoc = pm.getSBMLDoc();
		Model model = sbmlDoc.getModel();

		// Parse model annotations
		Model1Annotation m1Annot = new Model1Annotation(model.getAnnotation().getNonRDFannotation());

		Model1Rule rule = new Model1Rule((AssignmentRule) model.getRule(0));
		CatalogModelXml catModel = rule.toCatModel();

		// Parse constraints
		Map<String, Limits> limits = ReaderUtils.parseConstraints(model.getListOfConstraints());

		// time series cells
		final int condID = m1Annot.getCondID();
		Agent agent = new Agent(model.getSpecies(0));
		Matrix matrix = new Matrix(model.getCompartment(0));

		// Parse model variables: Temperature, pH and water activity
		PmmXmlDoc miscCell = ReaderUtils.parseMiscs(matrix.getMiscs());

		MdInfoXml mdInfo = new MdInfoXml(null, null, null, null, null);

		// primary model cells
		// Parse dependent parameter (primary models only have one dependent
		// variable)
		DepXml depXml = new DepXml("Value");
		String depUnitID = agent.getSpecies().getUnits();
		if (depUnitID != null) {
			String depUnitName = model.getUnitDefinition(depUnitID).getName();
			depXml.setUnit(depUnitName);
			depXml.setCategory(DBUnits.getDBUnits().get(depUnitName).getKind_of_property_quantity());
		}
		depXml.setDescription(agent.getDescription());

		// Parse indep
		Parameter indepParam = model.getParameter(Categories.getTime());
		IndepXml indepXml = new IndepXml(indepParam.getId(), null, null);
		String indepUnitID = indepParam.getUnits();
		if (!indepUnitID.equalsIgnoreCase(Unit.Kind.DIMENSIONLESS.getName())) {
			String unitName = model.getUnitDefinition(indepUnitID).getName();
			indepXml.setUnit(unitName);
			indepXml.setCategory(Categories.getTimeCategory().getName());
			indepXml.setDescription(Categories.getTime());
		}
		// Get limits
		if (limits.containsKey(indepParam.getId())) {
			Limits indepLimits = limits.get(indepParam.getId());
			indepXml.setMax(indepLimits.getMax());
			indepXml.setMin(indepLimits.getMin());
		}

		// Parse Consts
		LinkedList<Parameter> constParams = new LinkedList<>();
		for (Parameter param : model.getListOfParameters()) {
			if (param.isConstant()) {
				constParams.add(param);
			}
		}

		PmmXmlDoc paramCell = new PmmXmlDoc();
		for (Parameter constParam : constParams) {
			ParamXml paramXml = new Coefficient(constParam).toParamXml();

			// Assign unit and category
			String unitID = constParam.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = model.getUnitDefinition(unitID).getName();
				paramXml.setUnit(unitName);
				paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (limits.containsKey(constParam.getId())) {
				Limits constLimits = limits.get(constParam.getId());
				paramXml.setMax(constLimits.getMax());
				paramXml.setMin(constLimits.getMin());
			}

			paramCell.add(paramXml);
		}
		
		Uncertainties uncertainties = m1Annot.getUncertainties();
		EstModelXml estModel = uncertainties.getEstModelXml();

		if (model.isSetName()) {
			estModel.setName(model.getName());
		}

		PmmXmlDoc emLiteratureCell = new PmmXmlDoc();
		for (LiteratureItem lit : m1Annot.getLits()) {
			emLiteratureCell.add(lit);
		}

		// Add cells to the row
		KnimeTuple row = new KnimeTuple(SchemaFactory.createM1DataSchema());

		// time series cells
		row.setValue(TimeSeriesSchema.ATT_CONDID, condID);
		row.setValue(TimeSeriesSchema.ATT_COMBASEID, "?");
		row.setValue(TimeSeriesSchema.ATT_AGENT, new PmmXmlDoc(agent.toAgentXml()));
		row.setValue(TimeSeriesSchema.ATT_MATRIX, new PmmXmlDoc(matrix.toMatrixXml()));
		row.setValue(TimeSeriesSchema.ATT_TIMESERIES, new PmmXmlDoc());
		row.setValue(TimeSeriesSchema.ATT_MISC, miscCell);
		row.setValue(TimeSeriesSchema.ATT_MDINFO, new PmmXmlDoc(mdInfo));
		row.setValue(TimeSeriesSchema.ATT_LITMD, new PmmXmlDoc());
		row.setValue(TimeSeriesSchema.ATT_DBUUID, "?");

		// primary model cells
		row.setValue(Model1Schema.ATT_MODELCATALOG, new PmmXmlDoc(catModel));
		row.setValue(Model1Schema.ATT_DEPENDENT, new PmmXmlDoc(depXml));
		row.setValue(Model1Schema.ATT_INDEPENDENT, new PmmXmlDoc(indepXml));
		row.setValue(Model1Schema.ATT_PARAMETER, paramCell);
		row.setValue(Model1Schema.ATT_ESTMODEL, new PmmXmlDoc(estModel));
		row.setValue(Model1Schema.ATT_MLIT, new PmmXmlDoc());
		row.setValue(Model1Schema.ATT_EMLIT, emLiteratureCell);
		row.setValue(Model1Schema.ATT_DATABASEWRITABLE, Model1Schema.WRITABLE);
		row.setValue(Model1Schema.ATT_DBUUID, "?");

		return row;
	}
}

class TwoStepSecondaryModelReader implements Reader {

	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec spec = SchemaFactory.createM12DataSchema().createSpec();
		BufferedDataContainer container = exec.createDataContainer(spec);

		// Reads in models from file
		List<TwoStepSecondaryModel> models = TwoStepSecondaryModelFile.read(filepath);

		// Creates tuples and adds them to the container
		for (TwoStepSecondaryModel tssm : models) {
			List<KnimeTuple> tuples = parse(tssm);
			for (KnimeTuple tuple : tuples) {
				container.addRowToTable(tuple);
			}
			exec.setProgress((float) container.size() / models.size());
		}

		container.close();
		return container;
	}

	private List<KnimeTuple> parse(TwoStepSecondaryModel tssm) {
		// create n rows for n secondary models
		List<KnimeTuple> rows = new LinkedList<>();

		// Parse secondary model
		Model secModel = tssm.getSecDoc().getModel();

		// Parse constraints
		Map<String, Limits> secLimits = ReaderUtils.parseConstraints(secModel.getListOfConstraints());

		// Parse rule
		Model2Rule secRule = new Model2Rule((AssignmentRule) secModel.getRule(0));
		CatalogModelXml secCatModel = secRule.toCatModel();

		// Parse dep
		String secDepName = secRule.getRule().getVariable();
		DepXml secDepXml = new DepXml(secDepName);
		Parameter depParam = secModel.getParameter(secDepName);
		if (depParam.getUnits() != null && !depParam.getUnits().isEmpty()) {
			// Add unit
			String unitID = depParam.getUnits();
			String unitName = secModel.getUnitDefinition(unitID).getName();
			secDepXml.setUnit(unitName);

			// Add unit category
			if (unitName.equals("min") || unitName.equals("h")) {
				secDepXml.setCategory(Categories.getTimeCategory().getName());
			} else if (unitName.equals("�C")) {
				secDepXml.setCategory(Categories.getTempCategory().getName());
			}
		}

		// Sort const and indep params
		LinkedList<Parameter> secIndepParams = new LinkedList<>();
		LinkedList<Parameter> secConstParams = new LinkedList<>();
		for (Parameter param : secModel.getListOfParameters()) {
			if (param.isConstant()) {
				secConstParams.add(param);
			} else if (!param.getId().equals(secDepName)) {
				secIndepParams.add(param);
			}
		}

		// Parse indeps
		PmmXmlDoc secIndepCell = new PmmXmlDoc();
		for (Parameter param : secIndepParams) {
			IndepXml indepXml = new SecIndep(param).toIndepXml();

			// Assign unit and category
			String unitID = param.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = secModel.getUnitDefinition(unitID).getName();
				indepXml.setUnit(unitName);
				indepXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (secLimits.containsKey(param.getId())) {
				Limits indepLimits = secLimits.get(param.getId());
				indepXml.setMax(indepLimits.getMax());
				indepXml.setMin(indepLimits.getMin());
			}

			secIndepCell.add(indepXml);
		}

		// Parse consts
		PmmXmlDoc secConstCell = new PmmXmlDoc();
		for (Parameter param : secConstParams) {
			ParamXml paramXml = new Coefficient(param).toParamXml();

			// Assign unit and category
			String unitID = param.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = secModel.getUnitDefinition(unitID).getName();
				paramXml.setUnit(unitName);
				paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (secLimits.containsKey(param.getId())) {
				Limits constLimits = secLimits.get(param.getId());
				paramXml.setMax(constLimits.getMax());
				paramXml.setMin(constLimits.getMin());
			}

			secConstCell.add(paramXml);
		}

		// Get model annotation
		Model2Annotation m2Annot = new Model2Annotation(secModel.getAnnotation().getNonRDFannotation());

		// EstModel
		Uncertainties uncertainties = m2Annot.getUncertainties();
		EstModelXml secEstModel = uncertainties.getEstModelXml();
		if (secModel.isSetName()) {
			secEstModel.setName(secModel.getName());
		}

		// Get globalModelID from annotation
		int globalModelID = m2Annot.getGlobalModelID();

		// Get EM_Literature (references) from annotation
		PmmXmlDoc secEmLitCell = new PmmXmlDoc();
		for (LiteratureItem lit : m2Annot.getLiteratureItems()) {
			secEmLitCell.add(lit);
		}

		for (PrimaryModelWData pmwd : tssm.getPrimModels()) {

			Model model = pmwd.getSBMLDoc().getModel();

			// parse annotation
			Model1Annotation primModelAnnotation = new Model1Annotation(model.getAnnotation().getNonRDFannotation());

			Model1Rule rule = new Model1Rule((AssignmentRule) model.getRule(0));
			CatalogModelXml catModel = rule.toCatModel();

			// Parse constraints
			ListOf<Constraint> constraints = model.getListOfConstraints();
			Map<String, Limits> limits = ReaderUtils.parseConstraints(constraints);

			// time series cells
			final int condID = primModelAnnotation.getCondID();
			Agent agent = new Agent(model.getSpecies(0));

			Matrix matrix = new Matrix(model.getCompartment(0));

			// Add data
			PmmXmlDoc mdData = new PmmXmlDoc();
			String combaseId = "?";
			if (pmwd.getNuMLDoc() != null) {
				DataFile df = new DataFile(pmwd.getNuMLDoc());
				combaseId = df.getCombaseID();
				String timeUnit = df.getTimeUnit();
				String concUnit = df.getConcUnit();

				// Gets concentration unit object type from DB
				UnitsFromDB ufdb = DBUnits.getDBUnits().get(concUnit);
				String concUnitObjectType = ufdb.getObject_type();

				// Gets data
				mdData = ReaderUtils.createTimeSeries(timeUnit, concUnit, concUnitObjectType, df.getData());
			}

			// Parse model variables
			Map<String, Double> miscs = matrix.getMiscs();
			PmmXmlDoc miscCell = ReaderUtils.parseMiscs(miscs);

			MdInfoXml mdInfo = new MdInfoXml(null, null, null, null, null);

			// primary model cells
			// Parse dependent parameter (primary models only have one dependent
			// variable)
			DepXml depXml = new DepXml("Value");
			String depUnitID = agent.getSpecies().getUnits();
			if (depUnitID != null) {
				String depUnitName = model.getUnitDefinition(depUnitID).getName();
				depXml.setUnit(depUnitName);
				depXml.setCategory(DBUnits.getDBUnits().get(depUnitName).getKind_of_property_quantity());
			}
			depXml.setDescription(agent.getDescription());

			// Parse indep
			Parameter indepParam = model.getParameter(Categories.getTime());
			IndepXml indepXml = new IndepXml(indepParam.getId(), null, null);
			String indepUnitID = indepParam.getUnits();
			if (!indepUnitID.equalsIgnoreCase(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = model.getUnitDefinition(indepUnitID).getName();
				indepXml.setUnit(unitName);
				indepXml.setCategory(Categories.getTimeCategory().getName());
				indepXml.setDescription(Categories.getTime());
			}
			// Get limits
			if (limits.containsKey(indepParam.getId())) {
				Limits indepLimits = limits.get(indepParam.getId());
				indepXml.setMax(indepLimits.getMax());
				indepXml.setMin(indepLimits.getMin());
			}

			// Parse Consts
			LinkedList<Parameter> constParams = new LinkedList<>();
			for (Parameter param : model.getListOfParameters()) {
				if (param.isConstant()) {
					constParams.add(param);
				}
			}

			PmmXmlDoc paramCell = new PmmXmlDoc();
			for (Parameter constParam : constParams) {
				ParamXml paramXml = new Coefficient(constParam).toParamXml();

				// Assign unit and category
				String unitID = constParam.getUnits();
				if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
					String unitName = model.getUnitDefinition(unitID).getName();
					paramXml.setUnit(unitName);
					paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
				}

				// Get limits
				if (limits.containsKey(constParam.getId())) {
					Limits constLimits = limits.get(constParam.getId());
					paramXml.setMax(constLimits.getMax());
					paramXml.setMin(constLimits.getMin());
				}

				paramCell.add(paramXml);
			}

			uncertainties = primModelAnnotation.getUncertainties();
			EstModelXml estModel = uncertainties.getEstModelXml();

			if (model.isSetName()) {
				estModel.setName(model.getName());
			}

			PmmXmlDoc emLiteratureCell = new PmmXmlDoc();
			for (LiteratureItem lit : primModelAnnotation.getLits()) {
				emLiteratureCell.add(lit);
			}

			KnimeTuple row = new KnimeTuple(SchemaFactory.createM12DataSchema());

			// TimeSeriesSchema cells
			row.setValue(TimeSeriesSchema.ATT_CONDID, condID);
			row.setValue(TimeSeriesSchema.ATT_COMBASEID, combaseId);
			row.setValue(TimeSeriesSchema.ATT_AGENT, new PmmXmlDoc(agent.toAgentXml()));
			row.setValue(TimeSeriesSchema.ATT_MATRIX, new PmmXmlDoc(matrix.toMatrixXml()));
			row.setValue(TimeSeriesSchema.ATT_TIMESERIES, mdData);
			row.setValue(TimeSeriesSchema.ATT_MISC, miscCell);
			row.setValue(TimeSeriesSchema.ATT_MDINFO, new PmmXmlDoc(mdInfo));
			row.setValue(TimeSeriesSchema.ATT_LITMD, new PmmXmlDoc());
			row.setValue(TimeSeriesSchema.ATT_DBUUID, "?");

			// Model1Schema cells
			row.setValue(Model1Schema.ATT_MODELCATALOG, new PmmXmlDoc(catModel));
			row.setValue(Model1Schema.ATT_DEPENDENT, new PmmXmlDoc(depXml));
			row.setValue(Model1Schema.ATT_INDEPENDENT, new PmmXmlDoc(indepXml));
			row.setValue(Model1Schema.ATT_PARAMETER, paramCell);
			row.setValue(Model1Schema.ATT_ESTMODEL, new PmmXmlDoc(estModel));
			row.setValue(Model1Schema.ATT_MLIT, new PmmXmlDoc());
			row.setValue(Model1Schema.ATT_EMLIT, emLiteratureCell);
			row.setValue(Model1Schema.ATT_DATABASEWRITABLE, Model1Schema.WRITABLE);
			row.setValue(Model1Schema.ATT_DBUUID, "?");

			// Model2Schema cells
			row.setValue(Model2Schema.ATT_MODELCATALOG, new PmmXmlDoc(secCatModel));
			row.setValue(Model2Schema.ATT_DEPENDENT, new PmmXmlDoc(secDepXml));
			row.setValue(Model2Schema.ATT_INDEPENDENT, secIndepCell);
			row.setValue(Model2Schema.ATT_PARAMETER, secConstCell);
			row.setValue(Model2Schema.ATT_ESTMODEL, new PmmXmlDoc(secEstModel));
			row.setValue(Model2Schema.ATT_MLIT, new PmmXmlDoc());
			row.setValue(Model2Schema.ATT_EMLIT, secEmLitCell);
			row.setValue(Model2Schema.ATT_DATABASEWRITABLE, Model2Schema.WRITABLE);
			row.setValue(Model2Schema.ATT_DBUUID, "?");
			row.setValue(Model2Schema.ATT_GLOBAL_MODEL_ID, globalModelID);

			rows.add(row);
		}

		return rows;
	}
}

class OneStepSecondaryModelReader implements Reader {

	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec spec = SchemaFactory.createM12DataSchema().createSpec();
		BufferedDataContainer container = exec.createDataContainer(spec);

		// Reads in models from file
		List<OneStepSecondaryModel> models = OneStepSecondaryModelFile.read(filepath);

		// Creates tuples and adds them to the container
		for (OneStepSecondaryModel ossm : models) {
			List<KnimeTuple> tuples = parse(ossm);
			for (KnimeTuple tuple : tuples) {
				container.addRowToTable(tuple);
			}
			exec.setProgress((float) container.size() / models.size());
		}

		container.close();
		return container;
	}

	private List<KnimeTuple> parse(OneStepSecondaryModel ossm) {
		List<KnimeTuple> rows = new LinkedList<>();

		// Create primary model
		Model model = ossm.getSBMLDoc().getModel();

		// parse annotation
		Model1Annotation primModelAnnotation = new Model1Annotation(model.getAnnotation().getNonRDFannotation());

		Model1Rule rule = new Model1Rule((AssignmentRule) model.getRule(0));
		CatalogModelXml catModel = rule.toCatModel();

		// Parse constraints
		ListOf<Constraint> constraints = model.getListOfConstraints();
		Map<String, Limits> limits = ReaderUtils.parseConstraints(constraints);

		// time series cells
		final int condID = primModelAnnotation.getCondID();
		// TODO: need to fix the whole method. In the meantime a random
		// combaseId will be used
		// final String combaseID = primModelAnnotation.getCombaseID();
		String combaseID = Integer.toString(MathUtilities.getRandomNegativeInt());
		Agent agent = new Agent(model.getSpecies(0));

		Matrix matrix = new Matrix(model.getCompartment(0));

		// Parse model variables
		Map<String, Double> miscs = matrix.getMiscs();
		PmmXmlDoc miscCell = ReaderUtils.parseMiscs(miscs);

		MdInfoXml mdInfo = new MdInfoXml(null, null, null, null, null);

		// primary model cells
		// Parse dependent parameter (primary models only have one dependent
		// variable)
		DepXml depXml = new DepXml("Value");
		String depUnitID = agent.getSpecies().getUnits();
		if (depUnitID != null) {
			String depUnitName = model.getUnitDefinition(depUnitID).getName();
			depXml.setUnit(depUnitName);
			depXml.setCategory(DBUnits.getDBUnits().get(depUnitName).getKind_of_property_quantity());
		}
		depXml.setDescription(agent.getDescription());

		// Parse indep
		Parameter indepParam = model.getParameter(Categories.getTime());
		IndepXml indepXml = new IndepXml(indepParam.getId(), null, null);
		String indepUnitID = indepParam.getUnits();
		if (!indepUnitID.equalsIgnoreCase(Unit.Kind.DIMENSIONLESS.getName())) {
			String unitName = model.getUnitDefinition(indepUnitID).getName();
			indepXml.setUnit(unitName);
			indepXml.setCategory(Categories.getTimeCategory().getName());
			indepXml.setDescription(Categories.getTime());
		}
		// Get limits
		if (limits.containsKey(indepParam.getId())) {
			Limits indepLimits = limits.get(indepParam.getId());
			indepXml.setMax(indepLimits.getMax());
			indepXml.setMin(indepLimits.getMin());
		}

		// Parse Consts
		LinkedList<Parameter> constParams = new LinkedList<>();
		for (Parameter param : model.getListOfParameters()) {
			if (param.isConstant()) {
				constParams.add(param);
			}
		}

		PmmXmlDoc paramCell = new PmmXmlDoc();
		for (Parameter constParam : constParams) {
			ParamXml paramXml = new Coefficient(constParam).toParamXml();

			// Assign unit and category
			String unitID = constParam.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = model.getUnitDefinition(unitID).getName();
				paramXml.setUnit(unitName);
				paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (limits.containsKey(constParam.getId())) {
				Limits constLimits = limits.get(constParam.getId());
				paramXml.setMax(constLimits.getMax());
				paramXml.setMin(constLimits.getMin());
			}

			paramCell.add(paramXml);
		}

		Uncertainties uncertainties = primModelAnnotation.getUncertainties();
		EstModelXml estModel = uncertainties.getEstModelXml();
		if (model.isSetName()) {
			estModel.setName(model.getName());
		}

		PmmXmlDoc emLiteratureCell = new PmmXmlDoc();
		for (LiteratureItem lit : primModelAnnotation.getLits()) {
			emLiteratureCell.add(lit);
		}

		// Parse secondary model
		CompSBMLDocumentPlugin secCompPlugin = (CompSBMLDocumentPlugin) ossm.getSBMLDoc()
				.getPlugin(CompConstants.shortLabel);
		ModelDefinition secModel = secCompPlugin.getModelDefinition(0);

		// Parse constraints
		ListOf<Constraint> secConstraints = secModel.getListOfConstraints();
		Map<String, Limits> secLimits = ReaderUtils.parseConstraints(secConstraints);

		// Parse rule
		Model2Rule secRule = new Model2Rule((AssignmentRule) secModel.getRule(0));
		CatalogModelXml secCatModel = secRule.toCatModel();

		// Parse dep
		String secDepName = secRule.getRule().getVariable();
		DepXml secDepXml = new DepXml(secDepName);
		Parameter depParam = secModel.getParameter(secDepName);
		if (depParam.getUnits() != null && !depParam.getUnits().isEmpty()) {
			// Add unit
			String unitID = depParam.getUnits();
			String unitName = secModel.getUnitDefinition(unitID).getName();
			secDepXml.setUnit(unitName);

			// Add unit category
			if (unitName.equals("min") || unitName.equals("h")) {
				secDepXml.setCategory(Categories.getTimeCategory().getName());
			} else if (unitName.equals("�C")) {
				secDepXml.setCategory(Categories.getTempCategory().getName());
			}
		}

		// Sort const and indep params
		LinkedList<Parameter> secIndepParams = new LinkedList<>();
		LinkedList<Parameter> secConstParams = new LinkedList<>();
		for (Parameter param : secModel.getListOfParameters()) {
			if (param.isConstant()) {
				secConstParams.add(param);
			} else if (!param.getId().equals(secDepName)) {
				secIndepParams.add(param);
			}
		}

		// Parse indeps
		PmmXmlDoc secIndepCell = new PmmXmlDoc();
		for (Parameter param : secIndepParams) {
			IndepXml secIndepXml = new SecIndep(param).toIndepXml();

			// Assign unit and category
			String unitID = param.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = secModel.getUnitDefinition(unitID).getName();
				secIndepXml.setUnit(unitName);
				secIndepXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (secLimits.containsKey(param.getId())) {
				Limits indepLimits = secLimits.get(param.getId());
				secIndepXml.setMax(indepLimits.getMax());
				secIndepXml.setMin(indepLimits.getMin());
			}

			secIndepCell.add(secIndepXml);
		}

		// Parse consts
		PmmXmlDoc secConstCell = new PmmXmlDoc();
		for (Parameter param : secConstParams) {
			ParamXml paramXml = new Coefficient(param).toParamXml();

			// Assign unit and category
			String unitID = param.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = secModel.getUnitDefinition(unitID).getName();
				paramXml.setUnit(unitName);
				paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (secLimits.containsKey(param.getId())) {
				Limits constLimits = secLimits.get(param.getId());
				paramXml.setMax(constLimits.getMax());
				paramXml.setMin(constLimits.getMin());
			}

			secConstCell.add(paramXml);
		}

		// Get model annotation
		Model2Annotation m2Annot = new Model2Annotation(secModel.getAnnotation().getNonRDFannotation());

		// EstModel
		uncertainties = primModelAnnotation.getUncertainties();
		EstModelXml secEstModel = uncertainties.getEstModelXml();
		if (secModel.isSetName()) {
			secEstModel.setName(secModel.getName());
		}

		// Get globalModelID from annotation
		int globalModelID = m2Annot.getGlobalModelID();

		// Get EM_Literature (references) from annotation
		PmmXmlDoc secEmLitCell = new PmmXmlDoc();
		for (LiteratureItem lit : m2Annot.getLiteratureItems()) {
			secEmLitCell.add(lit);
		}

		KnimeTuple row = new KnimeTuple(SchemaFactory.createM12DataSchema());

		// TimeSeriesSchema cells
		row.setValue(TimeSeriesSchema.ATT_CONDID, condID);
		row.setValue(TimeSeriesSchema.ATT_COMBASEID, combaseID);
		row.setValue(TimeSeriesSchema.ATT_AGENT, new PmmXmlDoc(agent.toAgentXml()));
		row.setValue(TimeSeriesSchema.ATT_MATRIX, new PmmXmlDoc(matrix.toMatrixXml()));
		row.setValue(TimeSeriesSchema.ATT_MISC, miscCell);
		row.setValue(TimeSeriesSchema.ATT_MDINFO, new PmmXmlDoc(mdInfo));
		row.setValue(TimeSeriesSchema.ATT_LITMD, new PmmXmlDoc());
		row.setValue(TimeSeriesSchema.ATT_DBUUID, "?");

		// Model1Schema cells
		row.setValue(Model1Schema.ATT_MODELCATALOG, new PmmXmlDoc(catModel));
		row.setValue(Model1Schema.ATT_DEPENDENT, new PmmXmlDoc(depXml));
		row.setValue(Model1Schema.ATT_INDEPENDENT, new PmmXmlDoc(indepXml));
		row.setValue(Model1Schema.ATT_PARAMETER, paramCell);
		row.setValue(Model1Schema.ATT_ESTMODEL, new PmmXmlDoc(estModel));
		row.setValue(Model1Schema.ATT_MLIT, new PmmXmlDoc());
		row.setValue(Model1Schema.ATT_EMLIT, emLiteratureCell);
		row.setValue(Model1Schema.ATT_DATABASEWRITABLE, Model1Schema.WRITABLE);
		row.setValue(Model1Schema.ATT_DBUUID, "?");

		// Model2Schema cells
		row.setValue(Model2Schema.ATT_MODELCATALOG, new PmmXmlDoc(secCatModel));
		row.setValue(Model2Schema.ATT_DEPENDENT, new PmmXmlDoc(secDepXml));
		row.setValue(Model2Schema.ATT_INDEPENDENT, secIndepCell);
		row.setValue(Model2Schema.ATT_PARAMETER, secConstCell);
		row.setValue(Model2Schema.ATT_ESTMODEL, new PmmXmlDoc(secEstModel));
		row.setValue(Model2Schema.ATT_MLIT, new PmmXmlDoc());
		row.setValue(Model2Schema.ATT_EMLIT, secEmLitCell);
		row.setValue(Model2Schema.ATT_DATABASEWRITABLE, Model2Schema.WRITABLE);
		row.setValue(Model2Schema.ATT_DBUUID, "?");
		row.setValue(Model2Schema.ATT_GLOBAL_MODEL_ID, globalModelID);

		// Add data
		for (NuMLDocument numlDoc : ossm.getNuMLDocs()) {
			DataFile df = new DataFile(numlDoc);
			String timeUnit = df.getTimeUnit();
			String concUnit = df.getConcUnit();

			// Gets concentration unit object type from DB
			UnitsFromDB ufdb = DBUnits.getDBUnits().get(concUnit);
			String concUnitObjectType = ufdb.getObject_type();

			PmmXmlDoc mdData = ReaderUtils.createTimeSeries(timeUnit, concUnit, concUnitObjectType, df.getData());
			row.setValue(TimeSeriesSchema.ATT_TIMESERIES, mdData);
			rows.add(row);
		}

		return rows;
	}
}

class ManualSecondaryModelReader implements Reader {

	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec spec = SchemaFactory.createM2Schema().createSpec();
		BufferedDataContainer container = exec.createDataContainer(spec);

		// Reads in models from file
		List<ManualSecondaryModel> models = ManualSecondaryModelFile.read(filepath);

		// Creates tuples and adds them to the container
		for (ManualSecondaryModel model : models) {
			KnimeTuple tuple = parse(model);
			container.addRowToTable(tuple);
			exec.setProgress((float) container.size() / models.size());
		}

		container.close();
		return container;
	}

	private KnimeTuple parse(ManualSecondaryModel sm) {
		SBMLDocument sbmlDoc = sm.getSBMLDoc();
		Model model = sbmlDoc.getModel();

		// Parse constraints
		Map<String, Limits> limits = ReaderUtils.parseConstraints(model.getListOfConstraints());

		// Parse rule
		Model2Rule rule = new Model2Rule((AssignmentRule) model.getRule(0));
		CatalogModelXml catModel = rule.toCatModel();

		// Parse dep
		String depName = rule.getRule().getVariable();
		DepXml depXml = new DepXml(depName);
		Parameter depParam = model.getParameter(depName);
		if (depParam.getUnits() != null && !depParam.getUnits().isEmpty()) {
			// Add unit
			String unitID = depParam.getUnits();
			String unitName = model.getUnitDefinition(unitID).getName();
			depXml.setUnit(unitName);

			// Add unit category
			if (unitName.equals("min") || unitName.equals("h")) {
				depXml.setCategory(Categories.getTimeCategory().getName());
			} else if (unitName.equals("�C")) {
				depXml.setCategory(Categories.getTempCategory().getName());
			}
		}

		// Sort const and indep params
		LinkedList<Parameter> indepParams = new LinkedList<>();
		LinkedList<Parameter> constParams = new LinkedList<>();
		for (Parameter param : model.getListOfParameters()) {
			if (param.isConstant()) {
				constParams.add(param);
			} else if (!param.getId().equals(depName)) {
				indepParams.add(param);
			}
		}

		// Parse indeps
		PmmXmlDoc indepCell = new PmmXmlDoc();
		for (Parameter param : indepParams) {
			IndepXml indepXml = new SecIndep(param).toIndepXml();

			// Assign unit and category
			String unitID = param.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = model.getUnitDefinition(unitID).getName();
				indepXml.setUnit(unitName);
				indepXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (limits.containsKey(param.getId())) {
				Limits indepLimits = limits.get(param.getId());
				indepXml.setMax(indepLimits.getMax());
				indepXml.setMin(indepLimits.getMin());
			}

			indepCell.add(indepXml);
		}

		// Parse consts
		PmmXmlDoc constCell = new PmmXmlDoc();
		for (Parameter param : constParams) {
			ParamXml paramXml = new Coefficient(param).toParamXml();

			// Assign unit and category
			String unitID = param.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = model.getUnitDefinition(unitID).getName();
				paramXml.setUnit(unitName);
				paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (limits.containsKey(param.getId())) {
				Limits constLimits = limits.get(param.getId());
				paramXml.setMax(constLimits.getMax());
				paramXml.setMin(constLimits.getMin());
			}

			constCell.add(paramXml);
		}

		// Get model annotation
		Model2Annotation modelAnnotation = new Model2Annotation(model.getAnnotation().getNonRDFannotation());

		// EstModel
		Uncertainties uncertainties = modelAnnotation.getUncertainties();
		EstModelXml estModelXml = uncertainties.getEstModelXml();
		if (model.isSetName()) {
			estModelXml.setName(model.getName());
		}

		// Get globalModelID from annotation
		int globalModelID = modelAnnotation.getGlobalModelID();

		// Get EM_Literature (references) from annotation
		PmmXmlDoc emLiteratureCell = new PmmXmlDoc();
		for (LiteratureItem lit : modelAnnotation.getLiteratureItems()) {
			emLiteratureCell.add(lit);
		}

		// Add cells to the row
		KnimeTuple row = new KnimeTuple(SchemaFactory.createM2Schema());
		row.setValue(Model2Schema.ATT_MODELCATALOG, new PmmXmlDoc(catModel));
		row.setValue(Model2Schema.ATT_DEPENDENT, new PmmXmlDoc(depXml));
		row.setValue(Model2Schema.ATT_INDEPENDENT, indepCell);
		row.setValue(Model2Schema.ATT_PARAMETER, constCell);
		row.setValue(Model2Schema.ATT_ESTMODEL, new PmmXmlDoc(estModelXml));
		row.setValue(Model2Schema.ATT_MLIT, new PmmXmlDoc());
		row.setValue(Model2Schema.ATT_EMLIT, emLiteratureCell);
		row.setValue(Model2Schema.ATT_DATABASEWRITABLE, Model2Schema.WRITABLE);
		row.setValue(Model2Schema.ATT_DBUUID, "?");
		row.setValue(Model2Schema.ATT_GLOBAL_MODEL_ID, globalModelID);
		return row;
	}
}

class TwoStepTertiaryModelReader implements Reader {

	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec spec = SchemaFactory.createM12DataSchema().createSpec();
		BufferedDataContainer container = exec.createDataContainer(spec);

		// Read in models from file
		List<TwoStepTertiaryModel> models = TwoStepTertiaryModelFile.read(filepath);

		// Creates tuples and adds them to the container
		for (TwoStepTertiaryModel tssm : models) {
			List<KnimeTuple> tuples = parse(tssm);
			for (KnimeTuple tuple : tuples) {
				container.addRowToTable(tuple);
			}
			exec.setProgress((float) container.size() / models.size());
		}

		container.close();
		return container;
	}

	private List<KnimeTuple> parse(TwoStepTertiaryModel tstm) {

		List<KnimeTuple> secTuples = parseSecModels(tstm.getSecDocs());
		List<KnimeTuple> primTuples = parsePrimModels(tstm.getPrimModels());

		List<KnimeTuple> tuples = new LinkedList<>();
		for (KnimeTuple m1Tuple : primTuples) {
			for (KnimeTuple m2Tuple : secTuples) {

				// Creates tuple
				KnimeTuple tuple = new KnimeTuple(SchemaFactory.createM12DataSchema());

				// Copies data columns from m1Tuple
				tuple.setValue(TimeSeriesSchema.ATT_CONDID, m1Tuple.getInt(TimeSeriesSchema.ATT_CONDID));
				tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES, m1Tuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES));
				tuple.setValue(TimeSeriesSchema.ATT_COMBASEID, m1Tuple.getString(TimeSeriesSchema.ATT_COMBASEID));
				tuple.setValue(TimeSeriesSchema.ATT_AGENT, m1Tuple.getPmmXml(TimeSeriesSchema.ATT_AGENT));
				tuple.setValue(TimeSeriesSchema.ATT_MATRIX, m1Tuple.getPmmXml(TimeSeriesSchema.ATT_MATRIX));
				tuple.setValue(TimeSeriesSchema.ATT_MISC, m1Tuple.getPmmXml(TimeSeriesSchema.ATT_MISC));
				tuple.setValue(TimeSeriesSchema.ATT_MDINFO, m1Tuple.getPmmXml(TimeSeriesSchema.ATT_MDINFO));
				tuple.setValue(TimeSeriesSchema.ATT_LITMD, m1Tuple.getPmmXml(TimeSeriesSchema.ATT_LITMD));
				tuple.setValue(TimeSeriesSchema.ATT_DBUUID, m1Tuple.getString(TimeSeriesSchema.ATT_DBUUID));

				// Copies m1 columns from m1Tuple
				tuple.setValue(Model1Schema.ATT_MODELCATALOG, m1Tuple.getPmmXml(Model1Schema.ATT_MODELCATALOG));
				tuple.setValue(Model1Schema.ATT_DEPENDENT, m1Tuple.getPmmXml(Model1Schema.ATT_DEPENDENT));
				tuple.setValue(Model1Schema.ATT_INDEPENDENT, m1Tuple.getPmmXml(Model1Schema.ATT_INDEPENDENT));
				tuple.setValue(Model1Schema.ATT_PARAMETER, m1Tuple.getPmmXml(Model1Schema.ATT_PARAMETER));
				tuple.setValue(Model1Schema.ATT_ESTMODEL, m1Tuple.getPmmXml(Model1Schema.ATT_ESTMODEL));
				tuple.setValue(Model1Schema.ATT_MLIT, m1Tuple.getPmmXml(Model1Schema.ATT_MLIT));
				tuple.setValue(Model1Schema.ATT_EMLIT, m1Tuple.getPmmXml(Model1Schema.ATT_EMLIT));
				tuple.setValue(Model1Schema.ATT_DATABASEWRITABLE, m1Tuple.getInt(Model1Schema.ATT_DATABASEWRITABLE));
				tuple.setValue(Model1Schema.ATT_DBUUID, m1Tuple.getString(Model1Schema.ATT_DBUUID));

				// Copies m2 columns from m2Tuple
				tuple.setValue(Model2Schema.ATT_MODELCATALOG, m2Tuple.getPmmXml(Model2Schema.ATT_MODELCATALOG));
				tuple.setValue(Model2Schema.ATT_DEPENDENT, m2Tuple.getPmmXml(Model2Schema.ATT_DEPENDENT));
				tuple.setValue(Model2Schema.ATT_INDEPENDENT, m2Tuple.getPmmXml(Model2Schema.ATT_INDEPENDENT));
				tuple.setValue(Model2Schema.ATT_PARAMETER, m2Tuple.getPmmXml(Model2Schema.ATT_PARAMETER));
				tuple.setValue(Model2Schema.ATT_ESTMODEL, m2Tuple.getPmmXml(Model2Schema.ATT_ESTMODEL));
				tuple.setValue(Model2Schema.ATT_MLIT, m2Tuple.getPmmXml(Model2Schema.ATT_MLIT));
				tuple.setValue(Model2Schema.ATT_EMLIT, m2Tuple.getPmmXml(Model2Schema.ATT_EMLIT));
				tuple.setValue(Model2Schema.ATT_DATABASEWRITABLE, m2Tuple.getInt(Model2Schema.ATT_DATABASEWRITABLE));
				tuple.setValue(Model2Schema.ATT_DBUUID, m2Tuple.getString(Model2Schema.ATT_DBUUID));
				tuple.setValue(Model2Schema.ATT_GLOBAL_MODEL_ID, m2Tuple.getInt(Model2Schema.ATT_GLOBAL_MODEL_ID));

				// Adds tuple
				tuples.add(tuple);
			}
		}

		return tuples;
	}

	private List<KnimeTuple> parseSecModels(List<SBMLDocument> secDocs) {

		List<KnimeTuple> secTuples = new LinkedList<>();

		for (SBMLDocument secDoc : secDocs) {
			// Gets model definition
			Model md = secDoc.getModel();

			// Parse constraints
			Map<String, Limits> limits = ReaderUtils.parseConstraints(md.getListOfConstraints());

			Model2Rule rule2 = new Model2Rule((AssignmentRule) md.getRule(0));

			// Create dependent
			String depName = rule2.getRule().getVariable();
			DepXml secDepXml = new DepXml(depName);

			// Sort constant and independent parameters
			LinkedList<Parameter> secIndepParams = new LinkedList<>();
			LinkedList<Parameter> secConstParams = new LinkedList<>();
			for (Parameter param : md.getListOfParameters()) {
				if (param.isConstant()) {
					secConstParams.add(param);
				} else if (!param.getId().equals(depName)) {
					secIndepParams.add(param);
				}
			}

			// Parse sec indeps
			PmmXmlDoc indepCell = new PmmXmlDoc();
			for (Parameter param : secIndepParams) {
				IndepXml indepXml = new SecIndep(param).toIndepXml();

				// Assign unit and category
				String unitID = param.getUnits();
				if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
					String unitName = md.getUnitDefinition(unitID).getName();
					indepXml.setUnit(unitName);
					indepXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
				}

				// Get limits
				if (limits.containsKey(param.getId())) {
					Limits indepLimits = limits.get(param.getId());
					indepXml.setMax(indepLimits.getMax());
					indepXml.setMin(indepLimits.getMin());
				}

				indepCell.add(indepXml);
			}

			// Parse sec consts
			PmmXmlDoc constCell = new PmmXmlDoc();
			for (Parameter param : secConstParams) {
				ParamXml paramXml = new Coefficient(param).toParamXml();

				// Assign unit and category
				String unitID = param.getUnits();
				if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
					String unitName = md.getUnitDefinition(unitID).getName();
					paramXml.setUnit(unitName);
					paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
				}

				// Get limits
				if (limits.containsKey(param.getId())) {
					Limits constLimits = limits.get(param.getId());
					paramXml.setMax(constLimits.getMax());
					paramXml.setMin(constLimits.getMin());
				}

				constCell.add(paramXml);
			}

			Model2Annotation secModelAnnotation = new Model2Annotation(md.getAnnotation().getNonRDFannotation());

			// EstModel
			Uncertainties uncertainties = secModelAnnotation.getUncertainties();
			EstModelXml estModel = uncertainties.getEstModelXml();
			if (md.isSetName()) {
				estModel.setName(md.getName());
			}

			int globalModelID = secModelAnnotation.getGlobalModelID();

			// Add references to PMM Lab table
			PmmXmlDoc emLitCell = new PmmXmlDoc();
			for (LiteratureItem lit : secModelAnnotation.getLiteratureItems()) {
				emLitCell.add(lit);
			}

			// Add cells to the row
			KnimeTuple tuple = new KnimeTuple(SchemaFactory.createM2Schema());
			tuple.setValue(Model2Schema.ATT_MODELCATALOG, new PmmXmlDoc(rule2.toCatModel()));
			tuple.setValue(Model2Schema.ATT_DEPENDENT, new PmmXmlDoc(secDepXml));
			tuple.setValue(Model2Schema.ATT_INDEPENDENT, indepCell);
			tuple.setValue(Model2Schema.ATT_PARAMETER, constCell);
			tuple.setValue(Model2Schema.ATT_ESTMODEL, new PmmXmlDoc(estModel));
			tuple.setValue(Model2Schema.ATT_MLIT, new PmmXmlDoc());
			tuple.setValue(Model2Schema.ATT_EMLIT, emLitCell);
			tuple.setValue(Model2Schema.ATT_DATABASEWRITABLE, Model2Schema.WRITABLE);
			tuple.setValue(Model2Schema.ATT_DBUUID, "?");
			tuple.setValue(Model2Schema.ATT_GLOBAL_MODEL_ID, globalModelID);

			secTuples.add(tuple);
		}

		return secTuples;
	}

	private List<KnimeTuple> parsePrimModels(List<PrimaryModelWData> primModels) {

		List<KnimeTuple> primTuples = new LinkedList<>();

		for (PrimaryModelWData pm : primModels) {
			SBMLDocument sbmlDoc = pm.getSBMLDoc();
			NuMLDocument numlDoc = pm.getNuMLDoc();

			Model model = sbmlDoc.getModel();

			// Parse model annotations
			Model1Annotation m1Annot = new Model1Annotation(model.getAnnotation().getNonRDFannotation());

			Model1Rule rule = new Model1Rule((AssignmentRule) model.getRule(0));
			CatalogModelXml catModel = rule.toCatModel();

			// Parse constraints
			Map<String, Limits> limits = ReaderUtils.parseConstraints(model.getListOfConstraints());

			// time series cells
			final int condID = MathUtilities.getRandomNegativeInt();
			Agent agent = new Agent(model.getSpecies(0));
			Matrix matrix = new Matrix(model.getCompartment(0));

			DataFile df = new DataFile(numlDoc);
			String combaseId = df.getCombaseID();
			String timeUnit = df.getTimeUnit();
			String concUnit = df.getConcUnit();

			// Gets concentration unit object type from DB
			UnitsFromDB ufdb = DBUnits.getDBUnits().get(concUnit);
			String concUnitObjectType = ufdb.getObject_type();

			PmmXmlDoc mdDataCell = ReaderUtils.createTimeSeries(timeUnit, concUnit, concUnitObjectType, df.getData());

			// Parse model variables: Temperature, pH and water activity
			PmmXmlDoc miscCell = ReaderUtils.parseMiscs(matrix.getMiscs());

			// Creates empty model info
			MdInfoXml mdInfo = new MdInfoXml(null, null, null, null, null);

			// primary model cells
			// Parse dependent parameter (primary models only have one dependent
			// variable)
			DepXml depXml = new DepXml("Value");
			String depUnitID = agent.getSpecies().getUnits();
			if (depUnitID != null) {
				String depUnitName = model.getUnitDefinition(depUnitID).getName();
				depXml.setUnit(depUnitName);
				depXml.setCategory(DBUnits.getDBUnits().get(depUnitName).getKind_of_property_quantity());
			}
			depXml.setDescription(agent.getDescription());

			// Parse indep
			Parameter indepParam = model.getParameter(Categories.getTime());
			IndepXml indepXml = new IndepXml(indepParam.getId(), null, null);
			String indepUnitID = indepParam.getUnits();
			if (!indepUnitID.equalsIgnoreCase(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = model.getUnitDefinition(indepUnitID).getName();
				indepXml.setUnit(unitName);
				indepXml.setCategory(Categories.getTimeCategory().getName());
				indepXml.setDescription(Categories.getTime());
			}
			// Get limits
			if (limits.containsKey(indepParam.getId())) {
				Limits indepLimits = limits.get(indepParam.getId());
				indepXml.setMax(indepLimits.getMax());
				indepXml.setMin(indepLimits.getMin());
			}
			PmmXmlDoc indepCell = new PmmXmlDoc(indepXml);

			// Parse Consts
			LinkedList<Parameter> constParams = new LinkedList<>();
			for (Parameter param : model.getListOfParameters()) {
				if (param.isConstant()) {
					constParams.add(param);
				}
			}

			PmmXmlDoc paramCell = new PmmXmlDoc();
			for (Parameter constParam : constParams) {
				ParamXml paramXml = new Coefficient(constParam).toParamXml();

				// Assign unit and category
				String unitID = constParam.getUnits();
				if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
					String unitName = model.getUnitDefinition(unitID).getName();
					paramXml.setUnit(unitName);
					paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
				}

				// Get limits
				if (limits.containsKey(constParam.getId())) {
					Limits constLimits = limits.get(constParam.getId());
					paramXml.setMax(constLimits.getMax());
					paramXml.setMin(constLimits.getMin());
				}

				paramCell.add(paramXml);
			}

			Uncertainties uncertainties = m1Annot.getUncertainties();
			EstModelXml estModel = uncertainties.getEstModelXml();
			if (model.isSetName()) {
				estModel.setName(model.getName());
			}

			PmmXmlDoc emLitCell = new PmmXmlDoc();
			for (LiteratureItem lit : m1Annot.getLits()) {
				emLitCell.add(lit);
			}

			// Add cells to the row
			KnimeTuple tuple = new KnimeTuple(SchemaFactory.createM1DataSchema());

			// time series cells
			tuple.setValue(TimeSeriesSchema.ATT_CONDID, condID);
			tuple.setValue(TimeSeriesSchema.ATT_COMBASEID, combaseId);
			tuple.setValue(TimeSeriesSchema.ATT_AGENT, new PmmXmlDoc(agent.toAgentXml()));
			tuple.setValue(TimeSeriesSchema.ATT_MATRIX, new PmmXmlDoc(matrix.toMatrixXml()));
			tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES, mdDataCell);
			tuple.setValue(TimeSeriesSchema.ATT_MISC, miscCell);
			tuple.setValue(TimeSeriesSchema.ATT_MDINFO, new PmmXmlDoc(mdInfo));
			tuple.setValue(TimeSeriesSchema.ATT_LITMD, new PmmXmlDoc());
			tuple.setValue(TimeSeriesSchema.ATT_DBUUID, "?");

			// primary model cells
			tuple.setValue(Model1Schema.ATT_MODELCATALOG, new PmmXmlDoc(catModel));
			tuple.setValue(Model1Schema.ATT_DEPENDENT, new PmmXmlDoc(depXml));
			tuple.setValue(Model1Schema.ATT_INDEPENDENT, indepCell);
			tuple.setValue(Model1Schema.ATT_PARAMETER, paramCell);
			tuple.setValue(Model1Schema.ATT_ESTMODEL, new PmmXmlDoc(estModel));
			tuple.setValue(Model1Schema.ATT_MLIT, new PmmXmlDoc());
			tuple.setValue(Model1Schema.ATT_EMLIT, emLitCell);
			tuple.setValue(Model1Schema.ATT_DATABASEWRITABLE, Model1Schema.WRITABLE);
			tuple.setValue(Model1Schema.ATT_DBUUID, "?");

			primTuples.add(tuple);
		}

		return primTuples;
	}
}

class OneStepTertiaryModelReader implements Reader {

	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec spec = SchemaFactory.createM12DataSchema().createSpec();
		BufferedDataContainer container = exec.createDataContainer(spec);

		// Read in models from file
		List<OneStepTertiaryModel> models = OneStepTertiaryModelFile.read(filepath);

		// Creates tuples and adds them to the container
		for (OneStepTertiaryModel ostm : models) {
			List<KnimeTuple> tuples = parse(ostm);
			for (KnimeTuple tuple : tuples) {
				container.addRowToTable(tuple);
			}
			exec.setProgress((float) container.size() / models.size());
		}

		container.close();
		return container;
	}

	private List<KnimeTuple> parse(OneStepTertiaryModel ostm) {

		KnimeTuple primTuple = parsePrimModel(ostm.getTertDoc());
		List<KnimeTuple> dataTuples = parseData(ostm.getTertDoc(), ostm.getDataDocs());
		List<KnimeTuple> secTuples = parseSecModels(ostm.getSecDocs());

		List<KnimeTuple> tuples = new LinkedList<>();

		int instanceCounter = 1;
		for (KnimeTuple dataTuple : dataTuples) {
			for (KnimeTuple secTuple : secTuples) {
				// Creates new tuple
				KnimeTuple tuple = new KnimeTuple(SchemaFactory.createM12DataSchema());

				// Copies data columns
				tuple.setValue(TimeSeriesSchema.ATT_CONDID, instanceCounter);
				tuple.setValue(TimeSeriesSchema.ATT_COMBASEID, dataTuple.getString(TimeSeriesSchema.ATT_COMBASEID));
				tuple.setValue(TimeSeriesSchema.ATT_AGENT, dataTuple.getPmmXml(TimeSeriesSchema.ATT_AGENT));
				tuple.setValue(TimeSeriesSchema.ATT_MATRIX, dataTuple.getPmmXml(TimeSeriesSchema.ATT_MATRIX));
				tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES, dataTuple.getPmmXml(TimeSeriesSchema.ATT_TIMESERIES));
				tuple.setValue(TimeSeriesSchema.ATT_MISC, dataTuple.getPmmXml(TimeSeriesSchema.ATT_MISC));
				tuple.setValue(TimeSeriesSchema.ATT_MDINFO, dataTuple.getPmmXml(TimeSeriesSchema.ATT_MDINFO));
				tuple.setValue(TimeSeriesSchema.ATT_LITMD, dataTuple.getPmmXml(TimeSeriesSchema.ATT_LITMD));
				tuple.setValue(TimeSeriesSchema.ATT_DBUUID, dataTuple.getString(TimeSeriesSchema.ATT_DBUUID));

				// Copies model1 columns
				tuple.setValue(Model1Schema.ATT_MODELCATALOG, primTuple.getPmmXml(Model1Schema.ATT_MODELCATALOG));
				tuple.setValue(Model1Schema.ATT_DEPENDENT, primTuple.getPmmXml(Model1Schema.ATT_DEPENDENT));
				tuple.setValue(Model1Schema.ATT_INDEPENDENT, primTuple.getPmmXml(Model1Schema.ATT_INDEPENDENT));
				tuple.setValue(Model1Schema.ATT_PARAMETER, primTuple.getPmmXml(Model1Schema.ATT_PARAMETER));
				tuple.setValue(Model1Schema.ATT_ESTMODEL, primTuple.getPmmXml(Model1Schema.ATT_ESTMODEL));
				tuple.setValue(Model1Schema.ATT_MLIT, primTuple.getPmmXml(Model1Schema.ATT_MLIT));
				tuple.setValue(Model1Schema.ATT_EMLIT, primTuple.getPmmXml(Model1Schema.ATT_EMLIT));
				tuple.setValue(Model1Schema.ATT_DATABASEWRITABLE, primTuple.getInt(Model1Schema.ATT_DATABASEWRITABLE));
				tuple.setValue(Model1Schema.ATT_DBUUID, primTuple.getString(Model1Schema.ATT_DBUUID));

				// Copies model2 columns
				tuple.setValue(Model2Schema.ATT_MODELCATALOG, secTuple.getPmmXml(Model2Schema.ATT_MODELCATALOG));
				tuple.setValue(Model2Schema.ATT_DEPENDENT, secTuple.getPmmXml(Model2Schema.ATT_DEPENDENT));
				tuple.setValue(Model2Schema.ATT_INDEPENDENT, secTuple.getPmmXml(Model2Schema.ATT_INDEPENDENT));
				tuple.setValue(Model2Schema.ATT_PARAMETER, secTuple.getPmmXml(Model2Schema.ATT_PARAMETER));
				tuple.setValue(Model2Schema.ATT_ESTMODEL, secTuple.getPmmXml(Model2Schema.ATT_ESTMODEL));
				tuple.setValue(Model2Schema.ATT_MLIT, secTuple.getPmmXml(Model2Schema.ATT_MLIT));
				tuple.setValue(Model2Schema.ATT_EMLIT, secTuple.getPmmXml(Model2Schema.ATT_EMLIT));
				tuple.setValue(Model2Schema.ATT_DATABASEWRITABLE, secTuple.getInt(Model2Schema.ATT_DATABASEWRITABLE));
				tuple.setValue(Model2Schema.ATT_DBUUID, secTuple.getString(Model2Schema.ATT_DBUUID));
				tuple.setValue(Model2Schema.ATT_GLOBAL_MODEL_ID, secTuple.getInt(Model2Schema.ATT_GLOBAL_MODEL_ID));

				tuples.add(tuple);
			}
			instanceCounter++;
		}

		return tuples;
	}

	private KnimeTuple parsePrimModel(SBMLDocument sbmlDoc) {
		Model model = sbmlDoc.getModel();

		// Parse model annotations
		Model1Annotation m1Annot = new Model1Annotation(model.getAnnotation().getNonRDFannotation());

		Model1Rule rule = new Model1Rule((AssignmentRule) model.getRule(0));
		CatalogModelXml catModel = rule.toCatModel();

		// Parses constraints
		Map<String, Limits> limits = ReaderUtils.parseConstraints(model.getListOfConstraints());

		// Parse dependent parameter (primary models only have one dependent
		// variable)
		DepXml depXml = new DepXml("Value");
		Agent organism = new Agent(model.getSpecies(0));
		String depUnitID = organism.getSpecies().getUnits();
		if (depUnitID != null) {
			String depUnitName = model.getUnitDefinition(depUnitID).getName();
			depXml.setUnit(depUnitName);
			depXml.setCategory(DBUnits.getDBUnits().get(depUnitName).getKind_of_property_quantity());
		}
		depXml.setDescription(organism.getDescription());

		// Parse indep
		Parameter indepParam = model.getParameter(Categories.getTime());
		IndepXml indepXml = new IndepXml(indepParam.getId(), null, null);
		String indepUnitID = indepParam.getUnits();
		if (!indepUnitID.equalsIgnoreCase(Unit.Kind.DIMENSIONLESS.getName())) {
			String unitName = model.getUnitDefinition(indepUnitID).getName();
			indepXml.setUnit(unitName);
			indepXml.setCategory(Categories.getTimeCategory().getName());
			indepXml.setDescription(Categories.getTime());
		}
		// Get limits
		if (limits.containsKey(indepParam.getId())) {
			Limits indepLimits = limits.get(indepParam.getId());
			indepXml.setMax(indepLimits.getMax());
			indepXml.setMin(indepLimits.getMin());
		}

		// Parse Consts
		LinkedList<Parameter> constParams = new LinkedList<>();
		for (Parameter param : model.getListOfParameters()) {
			if (param.isConstant()) {
				constParams.add(param);
			}
		}

		PmmXmlDoc paramCell = new PmmXmlDoc();
		for (Parameter constParam : constParams) {
			ParamXml paramXml = new Coefficient(constParam).toParamXml();

			// Assign unit and category
			String unitID = constParam.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = model.getUnitDefinition(unitID).getName();
				paramXml.setUnit(unitName);
				paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (limits.containsKey(constParam.getId())) {
				Limits constLimits = limits.get(constParam.getId());
				paramXml.setMax(constLimits.getMax());
				paramXml.setMin(constLimits.getMin());
			}

			paramCell.add(paramXml);
		}

		Uncertainties uncertainties = m1Annot.getUncertainties();
		EstModelXml estModel = uncertainties.getEstModelXml();
		if (model.isSetName()) {
			estModel.setName(model.getName());
		}

		PmmXmlDoc emLiteratureCell = new PmmXmlDoc();
		for (LiteratureItem lit : m1Annot.getLits()) {
			emLiteratureCell.add(lit);
		}

		// primary model cells
		KnimeTuple tuple = new KnimeTuple(SchemaFactory.createM1DataSchema());
		tuple.setValue(Model1Schema.ATT_MODELCATALOG, new PmmXmlDoc(catModel));
		tuple.setValue(Model1Schema.ATT_DEPENDENT, new PmmXmlDoc(depXml));
		tuple.setValue(Model1Schema.ATT_INDEPENDENT, new PmmXmlDoc(indepXml));
		tuple.setValue(Model1Schema.ATT_PARAMETER, paramCell);
		tuple.setValue(Model1Schema.ATT_ESTMODEL, new PmmXmlDoc(estModel));
		tuple.setValue(Model1Schema.ATT_MLIT, new PmmXmlDoc());
		tuple.setValue(Model1Schema.ATT_EMLIT, emLiteratureCell);
		tuple.setValue(Model1Schema.ATT_DATABASEWRITABLE, Model1Schema.WRITABLE);
		tuple.setValue(Model1Schema.ATT_DBUUID, "?");

		return tuple;
	}

	private List<KnimeTuple> parseData(SBMLDocument tertDoc, List<NuMLDocument> dataDocs) {

		MdInfoXml mdInfo = new MdInfoXml(null, null, null, null, null);

		List<KnimeTuple> tuples = new LinkedList<>();
		for (NuMLDocument dataDoc : dataDocs) {
			DataFile df = new DataFile(dataDoc);
			String timeUnit = df.getTimeUnit();
			String concUnit = df.getConcUnit();

			// Gets concentration unit object type from DB
			UnitsFromDB ufdb = DBUnits.getDBUnits().get(concUnit);
			String concUnitObjectType = ufdb.getObject_type();

			// Gets matrix and agent
			AgentXml agentXml = df.getAgent();
			MatrixXml matrixXml = df.getMatrix();
			
			// Gets miscs
			PmmXmlDoc miscCell = ReaderUtils.parseMiscs(df.getMiscs());
			
			// Gets data
			PmmXmlDoc mdDataCell = ReaderUtils.createTimeSeries(timeUnit, concUnit, concUnitObjectType, df.getData());
			
			// Gets literature
			PmmXmlDoc litDoc = new PmmXmlDoc();
			for (LiteratureItem lit : df.getLits()) {
				litDoc.add(lit);
			}

			KnimeTuple tuple = new KnimeTuple(SchemaFactory.createDataSchema());
			tuple.setValue(TimeSeriesSchema.ATT_CONDID, df.getCondID());
			tuple.setValue(TimeSeriesSchema.ATT_COMBASEID, df.getCombaseID());
			tuple.setValue(TimeSeriesSchema.ATT_MATRIX, new PmmXmlDoc(matrixXml));
			tuple.setValue(TimeSeriesSchema.ATT_AGENT, new PmmXmlDoc(agentXml));
			tuple.setValue(TimeSeriesSchema.ATT_TIMESERIES, mdDataCell);
			tuple.setValue(TimeSeriesSchema.ATT_MISC, miscCell);
			tuple.setValue(TimeSeriesSchema.ATT_MDINFO, new PmmXmlDoc(mdInfo));
			tuple.setValue(TimeSeriesSchema.ATT_LITMD, litDoc);
			tuple.setValue(TimeSeriesSchema.ATT_DBUUID, "?");

			tuples.add(tuple);
		}

		return tuples;
	}

	private List<KnimeTuple> parseSecModels(List<SBMLDocument> secDocs) {

		List<KnimeTuple> secTuples = new LinkedList<>();

		for (SBMLDocument secDoc : secDocs) {
			// Gets model definition
			Model md = secDoc.getModel();

			// Parse constraints
			Map<String, Limits> limits = ReaderUtils.parseConstraints(md.getListOfConstraints());

			Model2Rule rule2 = new Model2Rule((AssignmentRule) md.getRule(0));

			// Create dependent
			String depName = rule2.getRule().getVariable();
			DepXml depXml = new DepXml(depName);

			// Sort constant and independent parameters
			LinkedList<Parameter> indepParams = new LinkedList<>();
			LinkedList<Parameter> constParams = new LinkedList<>();
			for (Parameter param : md.getListOfParameters()) {
				if (param.isConstant()) {
					constParams.add(param);
				} else if (!param.getId().equals(depName)) {
					indepParams.add(param);
				}
			}

			// Parse sec indeps
			PmmXmlDoc indepCell = new PmmXmlDoc();
			for (Parameter param : indepParams) {
				IndepXml indepXml = new SecIndep(param).toIndepXml();

				// Assign unit and category
				String unitID = param.getUnits();
				if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
					String unitName = md.getUnitDefinition(unitID).getName();
					indepXml.setUnit(unitName);
					indepXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
				}

				// Get limits
				if (limits.containsKey(param.getId())) {
					Limits indepLimits = limits.get(param.getId());
					indepXml.setMax(indepLimits.getMax());
					indepXml.setMin(indepLimits.getMin());
				}

				indepCell.add(indepXml);
			}

			// Parse sec consts
			PmmXmlDoc constCell = new PmmXmlDoc();
			for (Parameter param : constParams) {
				ParamXml paramXml = new Coefficient(param).toParamXml();

				// Assign unit and category
				String unitID = param.getUnits();
				if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
					String unitName = md.getUnitDefinition(unitID).getName();
					paramXml.setUnit(unitName);
					paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
				}

				// Get limits
				if (limits.containsKey(param.getId())) {
					Limits constLimits = limits.get(param.getId());
					paramXml.setMax(constLimits.getMax());
					paramXml.setMin(constLimits.getMin());
				}

				constCell.add(paramXml);
			}

			Model2Annotation m2Annot = new Model2Annotation(md.getAnnotation().getNonRDFannotation());

			// EstModel
			Uncertainties uncertainties = m2Annot.getUncertainties();
			EstModelXml estModel = uncertainties.getEstModelXml();
			if (md.isSetName()) {
				estModel.setName(md.getName());
			}

			int globalModelID = m2Annot.getGlobalModelID();

			// Add references to PMM Lab table
			PmmXmlDoc emLitCell = new PmmXmlDoc();
			for (LiteratureItem lit : m2Annot.getLiteratureItems()) {
				emLitCell.add(lit);
			}

			// Add cells to the row
			KnimeTuple tuple = new KnimeTuple(SchemaFactory.createM2Schema());
			tuple.setValue(Model2Schema.ATT_MODELCATALOG, new PmmXmlDoc(rule2.toCatModel()));
			tuple.setValue(Model2Schema.ATT_DEPENDENT, new PmmXmlDoc(depXml));
			tuple.setValue(Model2Schema.ATT_INDEPENDENT, indepCell);
			tuple.setValue(Model2Schema.ATT_PARAMETER, constCell);
			tuple.setValue(Model2Schema.ATT_ESTMODEL, new PmmXmlDoc(estModel));
			tuple.setValue(Model2Schema.ATT_MLIT, new PmmXmlDoc());
			tuple.setValue(Model2Schema.ATT_EMLIT, emLitCell);
			tuple.setValue(Model2Schema.ATT_DATABASEWRITABLE, Model2Schema.WRITABLE);
			tuple.setValue(Model2Schema.ATT_DBUUID, "?");
			tuple.setValue(Model2Schema.ATT_GLOBAL_MODEL_ID, globalModelID);

			secTuples.add(tuple);
		}

		return secTuples;
	}
}

class ManualTertiaryModelReader implements Reader {

	public BufferedDataContainer read(String filepath, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec spec = SchemaFactory.createM12DataSchema().createSpec();
		BufferedDataContainer container = exec.createDataContainer(spec);

		// Read in models from file
		List<ManualTertiaryModel> models = ManualTertiaryModelFile.read(filepath);

		// Creates tuples and adds them to the container
		for (ManualTertiaryModel mtm : models) {
			List<KnimeTuple> tuples = parse(mtm);
			for (KnimeTuple tuple : tuples) {
				container.addRowToTable(tuple);
			}
			exec.setProgress((float) container.size() / models.size());
		}

		container.close();
		return container;
	}

	private List<KnimeTuple> parse(ManualTertiaryModel mtm) {
		Model model = mtm.getTertDoc().getModel();
		ListOf<Parameter> listOfParameters = model.getListOfParameters();

		// create n rows for n secondary models
		List<KnimeTuple> rows = new ArrayList<>();

		// parse annotation
		Model1Annotation primModelAnnotation = new Model1Annotation(model.getAnnotation().getNonRDFannotation());

		// Parse constraints
		ListOf<Constraint> constraints = model.getListOfConstraints();
		Map<String, Limits> limits = ReaderUtils.parseConstraints(constraints);

		// time series cells
		final int condID = primModelAnnotation.getCondID();
		Agent organism = new Agent(model.getSpecies(0));
		PmmXmlDoc organismCell = new PmmXmlDoc(organism.toAgentXml());

		Matrix matrix = new Matrix(model.getCompartment(0));
		PmmXmlDoc matrixCell = new PmmXmlDoc(matrix.toMatrixXml());

		Map<String, Double> miscs = matrix.getMiscs();
		PmmXmlDoc miscCell = ReaderUtils.parseMiscs(miscs);

		PmmXmlDoc mdInfoCell = new PmmXmlDoc(new MdInfoXml(null, null, null, null, null));
		PmmXmlDoc mdLiteratureCell = new PmmXmlDoc();
		String mdDBUID = "?";

		// primary model cells
		Model1Rule rule1 = new Model1Rule((AssignmentRule) model.getRule(0));
		CatalogModelXml catModel = rule1.toCatModel();
		PmmXmlDoc catModelCell = new PmmXmlDoc(catModel);

		// Parse dep
		DepXml depXml = new DepXml("Value");
		String depUnitID = organism.getSpecies().getUnits();
		if (depUnitID != null) {
			if (depUnitID.equals("dimensionless")) {
				depXml.setUnit("dimensionless");
				depXml.setCategory("Dimensionless quantity");
			} else {
				String depUnitName = model.getUnitDefinition(depUnitID).getName();
				depXml.setUnit(depUnitName);
				depXml.setCategory(DBUnits.getDBUnits().get(depUnitName).getKind_of_property_quantity());
			}
		}
		depXml.setDescription(organism.getDescription());
		PmmXmlDoc depCell = new PmmXmlDoc(depXml);

		// Parse indep
		Parameter indepParam = listOfParameters.get(Categories.getTime());
		IndepXml indepXml = new IndepXml(indepParam.getId(), null, null);
		String indepUnitID = indepParam.getUnits();
		if (!indepUnitID.isEmpty() && !indepUnitID.equalsIgnoreCase(Unit.Kind.DIMENSIONLESS.getName())) {
			String unitName = model.getUnitDefinition(indepUnitID).getName();
			indepXml.setUnit(unitName);
			indepXml.setCategory(Categories.getTimeCategory().getName());
			indepXml.setDescription(Categories.getTime());
		} 

		// Get limits
		if (limits.containsKey(indepParam.getId())) {
			Limits indepLimits = limits.get(indepParam.getId());
			indepXml.setMax(indepLimits.getMax());
			indepXml.setMin(indepLimits.getMin());
		}
		PmmXmlDoc indepCell = new PmmXmlDoc(indepXml);

		// Parse consts
		LinkedList<Parameter> constParams = new LinkedList<>();
		for (Parameter param : listOfParameters) {
			if (param.isConstant()) {
				constParams.add(param);
			}
		}
		PmmXmlDoc paramCell = new PmmXmlDoc();
		for (Parameter constParam : constParams) {
			ParamXml paramXml = new Coefficient(constParam).toParamXml();

			// Assign unit and category
			String unitID = constParam.getUnits();
			if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
				String unitName = model.getUnitDefinition(unitID).getName();
				paramXml.setUnit(unitName);
				paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
			}

			// Get limits
			if (limits.containsKey(constParam.getId())) {
				Limits constLimits = limits.get(constParam.getId());
				paramXml.setMax(constLimits.getMax());
				paramXml.setMin(constLimits.getMin());
			}

			paramCell.add(paramXml);
		}

		// Parse uncertainty measures from the document's annotations
		Uncertainties  uncertainties = primModelAnnotation.getUncertainties();
		EstModelXml estModel = uncertainties.getEstModelXml();
		if (model.isSetName()) {
			estModel.setName(model.getName());
		}

		PmmXmlDoc emLiteratureCell = new PmmXmlDoc();
		for (LiteratureItem lit : primModelAnnotation.getLits()) {
			emLiteratureCell.add(lit);
		}

		String mDBUID = "?";

		for (SBMLDocument secDoc : mtm.getSecDocs()) {
			// Gets model definition
			Model secModel = secDoc.getModel();

			ListOf<Parameter> secParams = secModel.getListOfParameters();

			// Parse constraints
			ListOf<Constraint> secConstraints = secModel.getListOfConstraints();
			Map<String, Limits> secLimits = ReaderUtils.parseConstraints(secConstraints);

			// secondary model columns (19-27)
			Model2Rule rule2 = new Model2Rule((AssignmentRule) secModel.getRule(0));
			CatalogModelXml catModelSec = rule2.toCatModel();
			PmmXmlDoc catModelSecCell = new PmmXmlDoc(catModelSec);

			// Create sec dep
			String depName = rule2.getRule().getVariable();
			Parameter depParam = listOfParameters.get(depName);
			Coefficient depCoeff = new Coefficient(depParam);
			DepXml secDepXml = new DepXml(depName);
			String depUnit;
			if (depParam.getUnits().equals(Unit.Kind.DIMENSIONLESS.getName())) {
				depUnit = null;
			} else {
				depUnit = depParam.getUnits();

				UnitsFromDB dbUnit = DBUnits.getDBUnits().get(depUnit);
				secDepXml.setCategory(dbUnit.getKind_of_property_quantity());
				;
			}
			secDepXml.setUnit(depUnit);
			secDepXml.setDescription(depCoeff.getDescription());
			PmmXmlDoc dependentSecCell = new PmmXmlDoc(secDepXml);

			// Sort const and indep params
			LinkedList<Parameter> secIndepParams = new LinkedList<>();
			LinkedList<Parameter> secConstParams = new LinkedList<>();
			for (Parameter param : secParams) {
				if (param.isConstant()) {
					secConstParams.add(param);
				} else if (!param.getId().equals(depName)) {
					secIndepParams.add(param);
				}
			}

			// Parse sec indeps
			PmmXmlDoc secIndepCell = new PmmXmlDoc();
			for (Parameter param : secIndepParams) {
				IndepXml secIndepXml = new SecIndep(param).toIndepXml();

				// Assign unit and category
				String unitID = param.getUnits();
				if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
					String unitName = secModel.getUnitDefinition(unitID).getName();
					secIndepXml.setUnit(unitName);
					secIndepXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
				}

				// Get limits
				if (secLimits.containsKey(param.getId())) {
					Limits indepLimits = secLimits.get(param.getId());
					secIndepXml.setMax(indepLimits.getMax());
					secIndepXml.setMin(indepLimits.getMin());
				}

				secIndepCell.add(secIndepXml);
			}

			// Parse sec consts
			PmmXmlDoc secConstCell = new PmmXmlDoc();
			for (Parameter param : secConstParams) {
				ParamXml paramXml = new Coefficient(param).toParamXml();

				// Assign unit and category
				String unitID = param.getUnits();
				if (!unitID.equals(Unit.Kind.DIMENSIONLESS.getName())) {
					String unitName = secModel.getUnitDefinition(unitID).getName();
					paramXml.setUnit(unitName);
					paramXml.setCategory(DBUnits.getDBUnits().get(unitName).getKind_of_property_quantity());
				}

				// Get limits
				if (secLimits.containsKey(param.getId())) {
					Limits constLimits = secLimits.get(param.getId());
					paramXml.setMax(constLimits.getMax());
					paramXml.setMin(constLimits.getMin());
				}

				secConstCell.add(paramXml);
			}

			PmmXmlDoc mLiteratureSecCell = new PmmXmlDoc();

			Model2Annotation secModelAnnotation = new Model2Annotation(secModel.getAnnotation().getNonRDFannotation());

			// EstModel
			uncertainties = secModelAnnotation.getUncertainties();
			EstModelXml secEstModelXml = uncertainties.getEstModelXml();
			if (secModel.isSetName()) {
				secEstModelXml.setName(secModel.getName());
			}
			PmmXmlDoc estModelSecCell = new PmmXmlDoc(secEstModelXml);

			final int globalModelID = MathUtilities.getRandomNegativeInt();

			// Add references to PMM Lab table
			PmmXmlDoc emLiteratureSecCell = new PmmXmlDoc();
			for (LiteratureItem lit : secModelAnnotation.getLiteratureItems()) {
				emLiteratureSecCell.add(lit);
			}

			String mDBUIDSEC = "?";

			// Add cells to the row
			KnimeTuple row = new KnimeTuple(SchemaFactory.createM12DataSchema());

			row.setValue(TimeSeriesSchema.ATT_CONDID, condID);
			row.setValue(TimeSeriesSchema.ATT_COMBASEID, "?");
			row.setValue(TimeSeriesSchema.ATT_AGENT, organismCell);
			row.setValue(TimeSeriesSchema.ATT_MATRIX, matrixCell);
			row.setValue(TimeSeriesSchema.ATT_TIMESERIES, new PmmXmlDoc());
			row.setValue(TimeSeriesSchema.ATT_MISC, miscCell);
			row.setValue(TimeSeriesSchema.ATT_MDINFO, mdInfoCell);
			row.setValue(TimeSeriesSchema.ATT_LITMD, mdLiteratureCell);
			row.setValue(TimeSeriesSchema.ATT_DBUUID, mdDBUID);

			row.setValue(Model1Schema.ATT_MODELCATALOG, catModelCell);
			row.setValue(Model1Schema.ATT_DEPENDENT, depCell);
			row.setValue(Model1Schema.ATT_INDEPENDENT, indepCell);
			row.setValue(Model1Schema.ATT_PARAMETER, paramCell);
			row.setValue(Model1Schema.ATT_ESTMODEL, new PmmXmlDoc(estModel));
			row.setValue(Model1Schema.ATT_MLIT, new PmmXmlDoc());
			row.setValue(Model1Schema.ATT_EMLIT, emLiteratureCell);
			row.setValue(Model1Schema.ATT_DATABASEWRITABLE, Model1Schema.WRITABLE);
			row.setValue(Model1Schema.ATT_DBUUID, mDBUID);

			row.setValue(Model2Schema.ATT_MODELCATALOG, catModelSecCell);
			row.setValue(Model2Schema.ATT_DEPENDENT, dependentSecCell);
			row.setValue(Model2Schema.ATT_INDEPENDENT, secIndepCell);
			row.setValue(Model2Schema.ATT_PARAMETER, secConstCell);
			row.setValue(Model2Schema.ATT_ESTMODEL, estModelSecCell);
			row.setValue(Model2Schema.ATT_MLIT, mLiteratureSecCell);
			row.setValue(Model2Schema.ATT_EMLIT, emLiteratureSecCell);
			row.setValue(Model2Schema.ATT_DATABASEWRITABLE, Model2Schema.WRITABLE);
			row.setValue(Model2Schema.ATT_DBUUID, mDBUIDSEC);
			row.setValue(Model2Schema.ATT_GLOBAL_MODEL_ID, globalModelID);

			rows.add(row);
		}

		return rows;
	}
}
