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
package de.bund.bfr.pmf.file;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Element;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;

import de.bund.bfr.pmf.ModelType;
import de.bund.bfr.pmf.file.uri.URIFactory;
import de.bund.bfr.pmf.model.ManualSecondaryModel;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;

/**
 * Case 2c: Manual secondary models. Secondary models generated manually.
 * 
 * @author Miguel Alba
 */
public class ManualSecondaryModelFile {

	private static final URI SBML_URI = URIFactory.createSBMLURI();
	private static final URI PMF_URI = URIFactory.createPMFURI();

	private static final SBMLReader READER = new SBMLReader();
	private static final SBMLWriter WRITER = new SBMLWriter();

	public static List<ManualSecondaryModel> readPMF(String filename) throws Exception {
		return read(filename, SBML_URI);
	}

	public static List<ManualSecondaryModel> readPMFX(String filename) throws Exception {
		return read(filename, PMF_URI);
	}

	public static void writePMF(String dir, String filename, List<ManualSecondaryModel> models) throws Exception {

		// Creates CombineArchive name
		String caName = String.format("%s/%s.pmf", dir, filename);
		write(caName, SBML_URI, models);
	}

	public static void writePMFX(String dir, String filename, List<ManualSecondaryModel> models) throws Exception {

		// Creates CombineArchive name
		String caName = String.format("%s/%s.pmfx", dir, filename);
		write(caName, PMF_URI, models);
	}

	private static List<ManualSecondaryModel> read(String filename, URI modelURI) throws Exception {

		List<ManualSecondaryModel> models = new LinkedList<>();

		// Creates CombineArchive
		CombineArchive ca = new CombineArchive(new File(filename));

		// Parse models in the PMF file
		for (ArchiveEntry entry : ca.getEntriesWithFormat(modelURI)) {
			InputStream stream = Files.newInputStream(entry.getPath(), StandardOpenOption.READ);
			SBMLDocument sbmlDoc = READER.readSBMLFromStream(stream);
			stream.close();
			String sbmlDocName = entry.getFileName();
			models.add(new ManualSecondaryModel(sbmlDocName, sbmlDoc));
		}

		ca.close();
		return models;
	}

	private static void write(String filename, URI modelURI, List<ManualSecondaryModel> models) throws Exception {

		// Removes previous CombineArchive if it exists
		File fileTmp = new File(filename);
		if (fileTmp.exists()) {
			fileTmp.delete();
		}

		// Creates new CombineArchive
		CombineArchive ca = new CombineArchive(new File(filename));

		// Add models and data
		for (ManualSecondaryModel model : models) {
			// Creates tmp file for the model
			File sbmlTmp = File.createTempFile("temp1", "");
			sbmlTmp.deleteOnExit();

			// Writes model to sbmlTmp and add it to the file
			WRITER.write(model.getDoc(), sbmlTmp);
			ca.addEntry(sbmlTmp, model.getDocName(), modelURI);
		}

		// Adds description with model type
		ModelType modelType = ModelType.MANUAL_SECONDARY_MODEL;
		Element metadataAnnotation = new PMFMetadataNode(modelType, new HashSet<String>(0)).node;
		ca.addDescription(new DefaultMetaDataObject(metadataAnnotation));

		ca.pack();
		ca.close();
	}
}
