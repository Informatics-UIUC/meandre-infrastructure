package org.meandre.core.repository;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import org.junit.Test;
import org.meandre.core.store.repository.PropertiesDescriptionDefinition;

/** Test case for the behavior of PropertiesDescriptionDefinition class.
 * 
 * @author Xavier Llor&agrave
 *
 */
public class PropertiesDescriptionDefinitionTest {

	/** Counter to track different test examples generated */
	private static int iNumTestExamplesGenerated = 0;
	
	/** The number of properties to use for testing */
	private static int iNumProps = 10;
	
	/** Creates a test instance.
	 * 
	 * @return The test instance
	 */
	static PropertiesDescriptionDefinition createTestInstance() {
		int iInstance = iNumTestExamplesGenerated++;
		
		Hashtable<String,String> htValues = new Hashtable<String,String>();
		Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
		
		for ( int i=0 ; i<iNumProps ; i++ ) {
			htValues.put("key-"+iInstance+"-"+i, "value-"+iInstance+"-"+i);
			htDescriptions.put("key-"+iInstance+"-"+i, "description-"+iInstance+"-"+i);
		}
			
		return new PropertiesDescriptionDefinition(htValues,htDescriptions);
	}
	
	/** Creates a PropertiesDescriptionDefinition for testing purposes.
	 * 
	 * @return The test purposes PDD
	 */
	private PropertiesDescriptionDefinition createATestPDD () {
		Hashtable<String,String> htValues = new Hashtable<String,String>();
		Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
		
		for ( int i=0 ; i<iNumProps ; i++ ) {
			htValues.put("key-"+i, "value-"+i);
			htDescriptions.put("key-"+i, "description-"+i);
		}
			
		return new PropertiesDescriptionDefinition(htValues,htDescriptions);
	}
	
	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescriptionDefinition#PropertiesDescriptionDefinition(java.util.Hashtable, java.util.Hashtable)}.
	 */
	@Test
	public void testPropertiesDescriptionDefinition() {
		
		PropertiesDescriptionDefinition pdd = createATestPDD();
		
		assertEquals(iNumProps,pdd.getDescriptions().size());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescriptionDefinition#getDescriptions()}.
	 */
	@Test
	public void testGetDescriptions() {
		
		PropertiesDescriptionDefinition pdd = createATestPDD();
		HashSet<String> hs = new HashSet<String>();
		
		// Recreating the description values
		for ( int i=0 ; i<iNumProps ; i++ ) 
			hs.add("description-"+i);
		
		// Getting the collection
		Collection<String> colpdd = pdd.getDescriptions();
		
		// Testing
		assertEquals(hs.containsAll(colpdd),true);
		assertEquals(hs.size(),colpdd.size());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescriptionDefinition#getDescription(java.lang.String)}.
	 */
	@Test
	public void testGetDescription() {

		PropertiesDescriptionDefinition pdd = createATestPDD();
		
		for ( int i=0 ; i<iNumProps ; i++ ) 
			assertEquals(pdd.getDescription("key-"+i),"description-"+i);
		
	}

}
