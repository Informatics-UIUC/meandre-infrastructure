/*
 * @(#) DemoRepositoryGeneratorTest.java @VERSION@
 *
 * Copyright (c) 2008+ Amit Kumar
 *
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.demo.repository;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class DemoRepositoryGeneratorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetTestHelloWorldRepository() {
		 Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		 assertTrue(model!=null);
		 System.out.println(model);
	}


}
