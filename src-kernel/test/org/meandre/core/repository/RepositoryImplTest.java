/**
 * 
 */
package org.meandre.core.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.demo.repository.DemoRepositoryGenerator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** Class to test the behavious of the repository implementations
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class RepositoryImplTest {

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#RepositoryImpl(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testRepositoryImpl() {
		Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr = new RepositoryImpl(model);
		
		assertEquals(qr.getAvailableExecutableComponents().size(),3);		
		assertEquals(qr.getAvailableFlows().size(),1);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#refreshCache(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testRefreshCacheModel() {
		Model modelOne = DemoRepositoryGenerator.getTestHelloWorldRepository();
		Model modelTwo = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr1 = new RepositoryImpl(modelOne);
		QueryableRepository qr2 = new RepositoryImpl(modelTwo);
		
		qr1.refreshCache(modelTwo);
		
		assertEquals(qr1.getAvailableExecutableComponents().size(),qr2.getAvailableExecutableComponents().size());		
		assertEquals(qr1.getAvailableFlows().size(),qr2.getAvailableFlows().size());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#getModel()}.
	 */
	@Test
	public void testGetModel() {
		Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr = new RepositoryImpl(model);
		
		Model modelBack = qr.getModel();

		assertTrue(model.containsAll(modelBack));
		assertTrue(modelBack.containsAll(model));
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#getAvailableExecutableComponents()}.
	 */
	@Test
	public void testGetAvailableExecutableComponents() {
		Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr = new RepositoryImpl(model);
		
		assertEquals(qr.getAvailableExecutableComponents().size(),3);	
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#getAvailableExecutableComponents(java.lang.String)}.
	 */
	@Test
	public void testGetAvailableExecutableComponentsString() {
		Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr = new RepositoryImpl(model);
		
		assertEquals(qr.getAvailableExecutableComponents("demo").size(),3);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#getExecutableComponentDescription(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testGetExecutableComponentDescription() {	
		Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr = new RepositoryImpl(model);
		
		for ( Resource res:qr.getAvailableExecutableComponents() )
			assertNotNull(qr.getExecutableComponentDescription(res));
		
		assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("meandre://test.org/component/concatenate-strings")));
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#getAvailableFlows()}.
	 */
	@Test
	public void testGetAvailableFlows() {
		Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr = new RepositoryImpl(model);
		
		assertEquals(qr.getAvailableFlows().size(),1);
		
		assertNotNull(qr.getFlowDescription(qr.getAvailableFlows().iterator().next()));
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#getAvailableFlows(java.lang.String)}.
	 */
	@Test
	public void testGetAvailableFlowsString() {
		Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr = new RepositoryImpl(model);
		
		assertEquals(qr.getAvailableFlows("demo").size(),1);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#getFlowDescription(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testGetFlowDescription() {
		Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr = new RepositoryImpl(model);
		
		for ( Resource res:qr.getAvailableFlows() )
			assertNotNull(qr.getFlowDescription(res));
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.RepositoryImpl#getTags()}.
	 */
	@Test
	public void testGetTags() {
		Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
		
		QueryableRepository qr = new RepositoryImpl(model);
		
		assertEquals(qr.getTags().size(),6);	

		assertEquals(qr.getFlowTags().size(),2);
		assertEquals(qr.getComponentTags().size(),6);
	}
	
	/** This test keeps updating a repository and reflushing it. Used to check for 
	 * memory leaks.
	 */
	@Test
	public void runRepetitiveUpdaterTest() {
		
		int REPETITIONS = 30;
		try {
			// Run simple hello world dangling input/outputs + fork
			Model model = DemoRepositoryGenerator.getNextTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository();
			RepositoryImpl qr = new RepositoryImpl(model);
			
			for ( int i=0 ; i<REPETITIONS ; i++ ) {
				Model modNew = DemoRepositoryGenerator.getNextTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository();
				model.add(modNew);
				qr.refreshCache(model);
//				System.out.println(i);
//				System.out.println(qr.getModel().size());
//				System.out.println(qr.getAvailableExecutableComponentDescriptions().size());
//				System.out.println(qr.getAvailableFlowDecriptions().size());
				assertEquals(i+2,qr.getAvailableFlowDescriptions().size());
				assertEquals(4*(i+2),qr.getAvailableExecutableComponentDescriptions().size());
			}
		} catch (InterruptedException e) {
			fail(e.toString());
		}
		
	}

}
