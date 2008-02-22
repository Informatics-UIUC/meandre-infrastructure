package org.meandre.core.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.repository.DataPortDescription;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** Testing the data port description behavior.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class DataPortDescriptionTest {

	/** Counter to track different test examples generated */
	private static int iNumTestExamplesGenerated = 0;
	
	/** Creates a test instance.
	 * 
	 * @return The test instance
	 */
	static DataPortDescription createTestInstance() {
		int iInstance  = iNumTestExamplesGenerated++;
		
		String sIdent = "http://meandre.org/test/dpd/"+iInstance;
		String  sName = "dpd-test-"+iInstance;
		String  sDesc = "A Test data port ("+iInstance+")";
		Resource  res = ModelFactory.createDefaultModel().createResource(sIdent);
		
		try {
			return new DataPortDescription(res,sIdent,sName,sDesc);
		} catch (CorruptedDescriptionException e) {
			fail("Could not create a new test instance!!!");
			return null;
		}	
	}
	
	/**
	 * Test method for {@link org.meandre.core.store.repository.DataPortDescription#DataPortDescription(com.hp.hpl.jena.rdf.model.Resource, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testDataPortDescription() {
		String sIdent = "http://meandre.org/test/dpd";
		String  sName = "dpd-test";
		String  sDesc = "A Test data port";
		Resource  res = ModelFactory.createDefaultModel().createResource(sIdent);
		
		// Test the right constructor
		try {
			DataPortDescription dpd = new DataPortDescription(res,sIdent,sName,sDesc);
		
			assertEquals(dpd.getResource(),res);
			assertEquals(dpd.getIdentifier(),sIdent);
			assertEquals(dpd.getName(),sName);
			assertEquals(dpd.getDescription(),sDesc);
			assertEquals(dpd.getResource().toString(),sIdent);
			
		} catch (CorruptedDescriptionException e) {
			fail("A exception was impropertly thrown"+e);
		}
		
		// Test the right constructor
		try {
			new DataPortDescription(res,sIdent+sIdent,sName,sDesc);
		
			fail("An exception should have been thrown because the resource and the identifier are different");
		} catch (CorruptedDescriptionException e) {
			// Correct behavior
		}
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.DataPortDescription#getResource()}.
	 */
	@Test
	public void testGetResource() {
		String sIdent = "http://meandre.org/test/dpd";
		String  sName = "dpd-test";
		String  sDesc = "A Test data port";
		Resource  res = ModelFactory.createDefaultModel().createResource(sIdent);
		
		// Test the right constructor
		try {
			DataPortDescription dpd = new DataPortDescription(res,sIdent,sName,sDesc);
		
			assertEquals(dpd.getResource(),res);
			
		} catch (CorruptedDescriptionException e) {
			fail("A exception was impropertly thrown"+e);
		}
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.DataPortDescription#getIdentifier()}.
	 */
	@Test
	public void testGetIdentifier() {
		String sIdent = "http://meandre.org/test/dpd";
		String  sName = "dpd-test";
		String  sDesc = "A Test data port";
		Resource  res = ModelFactory.createDefaultModel().createResource(sIdent);
		
		// Test the right constructor
		try {
			DataPortDescription dpd = new DataPortDescription(res,sIdent,sName,sDesc);
		
			assertEquals(dpd.getIdentifier(),sIdent);
			
		} catch (CorruptedDescriptionException e) {
			fail("A exception was impropertly thrown"+e);
		}
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.DataPortDescription#getName()}.
	 */
	@Test
	public void testGetName() {
		String sIdent = "http://meandre.org/test/dpd";
		String  sName = "dpd-test";
		String  sDesc = "A Test data port";
		Resource  res = ModelFactory.createDefaultModel().createResource(sIdent);
		
		// Test the right constructor
		try {
			DataPortDescription dpd = new DataPortDescription(res,sIdent,sName,sDesc);
		
			assertEquals(dpd.getName(),sName);
			
		} catch (CorruptedDescriptionException e) {
			fail("A exception was impropertly thrown"+e);
		}
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.DataPortDescription#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		String sIdent = "http://meandre.org/test/dpd";
		String  sName = "dpd-test";
		String  sDesc = "A Test data port";
		Resource  res = ModelFactory.createDefaultModel().createResource(sIdent);
		
		// Test the right constructor
		try {
			DataPortDescription dpd = new DataPortDescription(res,sIdent,sName,sDesc);
		
			assertEquals(dpd.getDescription(),sDesc);
			
		} catch (CorruptedDescriptionException e) {
			fail("A exception was impropertly thrown"+e);
		}
	}

}
