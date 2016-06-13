package de.bund.bfr.knime.pmm.common.reader;

import java.util.LinkedList;
import java.util.List;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.ExecutionContext;

import de.bund.bfr.knime.pmm.FSMRUtils;
import de.bund.bfr.knime.pmm.extendedtable.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.extendedtable.pmmtablemodel.SchemaFactory;
import de.bund.bfr.knime.pmm.openfsmr.FSMRTemplate;
import de.bund.bfr.knime.pmm.openfsmr.OpenFSMRSchema;
import de.bund.bfr.pmfml.file.TwoStepSecondaryModelFile;
import de.bund.bfr.pmfml.model.PrimaryModelWData;
import de.bund.bfr.pmfml.model.TwoStepSecondaryModel;

public class TwoStepSecondaryModelReader implements Reader {

	public BufferedDataContainer[] read(String filepath, boolean isPMFX, ExecutionContext exec) throws Exception {
		// Creates table spec and container
		DataTableSpec modelSpec = SchemaFactory.createM12DataSchema().createSpec();
		BufferedDataContainer modelContainer = exec.createDataContainer(modelSpec);

		// Reads in models from file
		List<TwoStepSecondaryModel> models;
		if (isPMFX) {
			models = TwoStepSecondaryModelFile.readPMFX(filepath);
		} else {
			models = TwoStepSecondaryModelFile.readPMF(filepath);
		}

		// Creates tuples and adds them to the container
		for (TwoStepSecondaryModel tssm : models) {
			List<KnimeTuple> tuples = parse(tssm);
			for (KnimeTuple tuple : tuples) {
				modelContainer.addRowToTable(tuple);
			}
			exec.setProgress((float) modelContainer.size() / models.size());
		}

		modelContainer.close();

		// Gets template of the first primary model file
		FSMRTemplate template = FSMRUtils
				.processModelWithMicrobialData(models.get(0).getPrimModels().get(0).getModelDoc());
		KnimeTuple fsmrTuple = FSMRUtils.createTupleFromTemplate(template);

		// Creates container with 'fsmrTuple'
		DataTableSpec fsmrSpec = new OpenFSMRSchema().createSpec();
		BufferedDataContainer fsmrContainer = exec.createDataContainer(fsmrSpec);
		fsmrContainer.addRowToTable(fsmrTuple);
		fsmrContainer.close();

		return new BufferedDataContainer[] { modelContainer, fsmrContainer };
	}

	private List<KnimeTuple> parse(TwoStepSecondaryModel tssm) {
		// create n rows for n secondary models
		List<KnimeTuple> rows = new LinkedList<>();

		KnimeTuple m2Tuple = new Model2Tuple(tssm.getSecDoc().getModel()).getTuple();

		for (PrimaryModelWData pmwd : tssm.getPrimModels()) {
			KnimeTuple dataTuple;
			if (pmwd.getDataDoc() != null) {
				dataTuple = new DataTuple(pmwd.getDataDoc()).getTuple();
			} else {
				dataTuple = new DataTuple(pmwd.getModelDoc()).getTuple();
			}
			KnimeTuple m1Tuple = new Model1Tuple(pmwd.getModelDoc()).getTuple();

			KnimeTuple row = Util.mergeTuples(dataTuple, m1Tuple, m2Tuple);
			rows.add(row);
		}

		return rows;
	}
}