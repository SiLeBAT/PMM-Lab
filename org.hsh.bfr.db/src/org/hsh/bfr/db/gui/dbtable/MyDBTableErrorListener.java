/*******************************************************************************
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * J�rgen Brandt (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Christian Th�ns (BfR)
 * Annemarie K�sbohrer (BfR)
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
/**
 * 
 */
package org.hsh.bfr.db.gui.dbtable;

import java.awt.Dimension;

import org.hsh.bfr.db.MyLogger;
import org.hsh.bfr.db.imports.InfoBox;

import quick.dbtable.DBTableErrorListener;

/**
 * @author Weiser
 *
 */
public class MyDBTableErrorListener implements DBTableErrorListener {
	
	public boolean errorOccured(int errorId, String errorMessage, Exception unexpectedException) {
		if (errorId == 6) {
			//errorId=6: Invalid index (Unable to delete!) passiert dummerweise beim L�schen der letzten Zeile in einer Table... wieso auch immer
			return true;
		}
		else if (errorId == 3 && errorMessage.equals("data exception: string data, right truncation")) {
			String text = "Der Text in einem Feld (gelb markiert) ist zu lang und kann so nicht abgespeichert werden!\nBitte �ndern!";
			InfoBox ib = new InfoBox(text, true, new Dimension(600, 200), null, false);	
			ib.setVisible(true);
			return true;
		}
		else {
			MyLogger.handleException(unexpectedException);
			MyLogger.handleMessage(errorId + "\twwwwwwwwwwwwwwwww\t"+errorMessage);
			//System.err.println(errorId + "\twwwwwwwwwwwwwwwww\t"+errorMessage);
			return false;
		}
		/*
 If it is an error generated by DBTable , then error id will have a value not equal to zero
 If it is an unexpected exception, the errorId will have a value zero, the caught exception
 will be in the unexpectedException argument

 Following are the valid error id & the error message displayed & the reason for the error

 1 - This cell can't be empty - When user empties out a non null column cell
 2 - Unable to find the Database Driver! - JDBC driver not found
 3 - Unable to Update! - When a record is updated
 4 - UnKnown Exception
 5 - Unable to insert! - When a new record is inserted
 6 - Unable to delete! - when a record is deleted
 7 - Error in boundSql! - When there is an error in boundSql for a column
 8 - Database error - When fetching records from the database
 9 - Sorting not supported for your database! - when table header is clicked
 25 - During replaceAll, after replacing errorOccured will be called with errorMessage={number of replacements}, unexpectedException=null
      You can use this to show your own messages
		 */
	}
}
