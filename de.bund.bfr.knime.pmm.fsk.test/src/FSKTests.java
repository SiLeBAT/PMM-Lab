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
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.bund.bfr.knime.pmm.fskx.DCFReaderTest;
import de.bund.bfr.knime.pmm.fskx.RDependencyTest;
import de.bund.bfr.knime.pmm.fskx.RMetaDataNodeTest;
import de.bund.bfr.knime.pmm.fskx.RPackageMetadataTest;
import de.bund.bfr.knime.pmm.fskx.RScriptTest;
import de.bund.bfr.knime.pmm.fskx.RVersionTest;

@RunWith(Suite.class)
@SuiteClasses({
  DCFReaderTest.class,
  RDependencyTest.class,
  RMetaDataNodeTest.class,
  RPackageMetadataTest.class,
  RScriptTest.class,
  RVersionTest.class
	})
public class FSKTests {
  // nothing here
}
