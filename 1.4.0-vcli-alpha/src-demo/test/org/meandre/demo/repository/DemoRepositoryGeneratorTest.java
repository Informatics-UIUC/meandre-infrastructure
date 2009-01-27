package org.meandre.demo.repository;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class DemoRepositoryGeneratorTest {

	/** Test that you can get a model for the hello world repository.
	 * 
	 */
	@Test
	public void testGetTestHelloWorldRepository() {
		 Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		 assertTrue(model!=null);
	}


}
