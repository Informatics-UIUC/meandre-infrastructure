package org.meandre.core.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.meandre.core.store.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.store.repository.PropertiesDescriptionDefinition;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** Testing the behavior of executable component instance descriptions
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ExecutableComponentInstanceDescriptionTest {

	/** Counter to track different test examples generated */
	private static int iNumTestExamplesGenerated = 0;
	
	/** Creates a test instance.
	 * 
	 * @return The test instance
	 */
	static ExecutableComponentInstanceDescription createTestInstance() {
		int iInstance = iNumTestExamplesGenerated++;
		
		Resource resExecutableComponentInstace = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/ecid/"+iInstance);
		Resource resComponent = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/ecd/"+iInstance);
		String sName = "instance-"+iInstance;
		String sDescription = "description-"+iInstance;
		PropertiesDescriptionDefinition pdProperties = PropertiesDescriptionDefinitionTest.createTestInstance();
		
		ExecutableComponentInstanceDescription ecid = new ExecutableComponentInstanceDescription(resExecutableComponentInstace,resComponent,sName,sDescription,pdProperties);

		assertEquals(resExecutableComponentInstace, ecid.getExecutableComponentInstance());
		assertEquals(resComponent, ecid.getExecutableComponent());
		assertEquals(sName, ecid.getName());
		assertEquals(sDescription, ecid.getDescription());
		assertEquals(pdProperties, ecid.getProperties());
		
		return ecid;
	}
	
	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#ExecutableComponentInstanceDescription()}.
	 */
	@Test
	public void testExecutableComponentInstanceDescription() {
		ExecutableComponentInstanceDescription ecid = new ExecutableComponentInstanceDescription();

		assertNull(ecid.getExecutableComponentInstance());
		assertNull(ecid.getExecutableComponent());
		assertEquals(ecid.getName(),"");
		assertEquals(ecid.getDescription(),"");
		assertNull(ecid.getProperties());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#ExecutableComponentInstanceDescription(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource, java.lang.String, java.lang.String, org.meandre.core.store.repository.PropertiesDescription)}.
	 */
	@Test
	public void testExecutableComponentInstanceDescriptionResourceResourceStringStringPropertiesDescription() {
		createTestInstance();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#setExecutableComponentInstance(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testSetExecutableComponentInstance() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		
		Resource resNew = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/ecid/new");
		
		ecid.setExecutableComponentInstance(resNew);
		assertEquals(ecid.getExecutableComponentInstance(),resNew);		

	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#getExecutableComponentInstance()}.
	 */
	@Test
	public void testGetExecutableComponentInstance() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		ecid.getExecutableComponentInstance();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#setExecutableComponent(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testSetExecutableComponent() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		
		Resource resNew = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/ecid/new");
		
		ecid.setExecutableComponent(resNew);
		assertEquals(ecid.getExecutableComponent(),resNew);	
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#getExecutableComponent()}.
	 */
	@Test
	public void testGetExecutableComponent() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		ecid.getExecutableComponent();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		
		String sNewName = new Date().toString();
		
		ecid.setName(sNewName);
		assertEquals(ecid.getName(),sNewName);	
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#getName()}.
	 */
	@Test
	public void testGetName() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		ecid.getName();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#setDescription(java.lang.String)}.
	 */
	@Test
	public void testSetDescription() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		
		String sNewDesc = new Date().toString();
		
		ecid.setDescription(sNewDesc);
		assertEquals(ecid.getDescription(),sNewDesc);	
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		ecid.getDescription();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#setProperties(org.meandre.core.store.repository.PropertiesDescription)}.
	 */
	@Test
	public void testSetProperties() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		
		PropertiesDescriptionDefinition pdNewProperties = PropertiesDescriptionDefinitionTest.createTestInstance();
		
		ecid.setProperties(pdNewProperties);
		assertEquals(ecid.getProperties(),pdNewProperties);	
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ExecutableComponentInstanceDescription#getProperties()}.
	 */
	@Test
	public void testGetProperties() {
		ExecutableComponentInstanceDescription ecid = createTestInstance();
		ecid.getProperties();
	}

}
