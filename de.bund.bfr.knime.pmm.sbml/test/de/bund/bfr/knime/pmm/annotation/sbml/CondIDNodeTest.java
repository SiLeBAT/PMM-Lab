package de.bund.bfr.knime.pmm.annotation.sbml;

import static org.junit.Assert.*;

import org.junit.Test;

import de.bund.bfr.knime.pmm.annotation.sbml.CondIDNode;

public class CondIDNodeTest {

	@Test
	public void test() {
		CondIDNode cin = new CondIDNode(1);
		CondIDNode cin2 = new CondIDNode(cin.getNode());
		assertEquals(1, cin2.getCondId());
	}
}
