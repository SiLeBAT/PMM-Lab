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
package fskx.writer;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class FSKXWriterNodeDialog extends DefaultNodeSettingsPane {

	protected FSKXWriterNodeDialog() {
		final String fileHistoryId = "fileHistory";
		final int dlgType = JFileChooser.SAVE_DIALOG;
		final boolean directoryOnly = false;
		final String validExtensions = ".fskx|.FSKX";

		SettingsModelString filePath = new SettingsModelString(FSKXWriterNodeModel.CFG_FILE, null);
		DialogComponentFileChooser fileDlg = new DialogComponentFileChooser(filePath, fileHistoryId, dlgType,
				directoryOnly, validExtensions);
		fileDlg.setBorderTitle("Output file");

		addDialogComponent(fileDlg);
	}
}
