package org.meandre.core.repository;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.meandre.core.repository.ConnectorDescription;
import org.meandre.core.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.repository.TagsDescription;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** Testing the behavior of the flow description
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class FlowDescriptionTest {

	/** Counter to track different test examples generated */
	private static int iNumTestExamplesGenerated = 0;
	
	/** Creates a test instance.
	 * 
	 * @return The test instance
	 */
	static FlowDescription createTestInstance() {
		int iInstance = iNumTestExamplesGenerated++;
		
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/fd/"+iInstance);
		String sName = "name-"+iInstance;
		String sDescription = "description-"+iInstance;
		String sRights = "rights-"+iInstance;
		String sCreator = "creator-"+iInstance;
		Date dateCreation = new Date();
		
		Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances = new HashSet<ExecutableComponentInstanceDescription>();
		Set<ConnectorDescription> setConnectorDescription = new HashSet<ConnectorDescription>();
		TagsDescription tagsDesc = new TagsDescription();
		
		return new FlowDescription(resFlowComponent, sName, sDescription,
				sRights, sCreator, dateCreation,
				setExecutableComponentInstances, setConnectorDescription,
				tagsDesc);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#FlowDescription()}.
	 */
	@Test
	public void testFlowDescription() {
		FlowDescription fd = new FlowDescription();
		
		assertNull(fd.getFlowComponent());
		
		assertEquals(fd.getName(),"");
		assertEquals(fd.getDescription(),"");
		assertEquals(fd.getRights(),"");
		assertEquals(fd.getCreator(),"");
		
		assertEquals(fd.getCreationDate().getClass().getName(),Date.class.getName());
		
		assertEquals(fd.getExecutableComponentInstances().size(),0);
		assertEquals(fd.getConnectorDescriptions().size(),0);
		assertEquals(fd.getTags().getTags().size(), 0);
		
		assertEquals(0L,fd.getModel().size());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#FlowDescription(com.hp.hpl.jena.rdf.model.Resource, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Set, java.util.Set, org.meandre.core.store.repository.TagsDescription)}.
	 */
	@Test
	public void testFlowDescriptionResourceStringStringStringStringDateSetOfExecutableComponentInstanceDescriptionSetOfConnectorDescriptionTagsDescription() {
		createTestInstance();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#setFlowComponent(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testSetFlowComponent() {
		FlowDescription fd = createTestInstance();
		
		Resource resFlowComponentNew = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/fd/new");
		
		fd.setFlowComponent(resFlowComponentNew);
		assertEquals(fd.getFlowComponent(),resFlowComponentNew);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getFlowComponent()}.
	 */
	@Test
	public void testGetFlowComponent() {
		FlowDescription fd = createTestInstance();
		fd.getFlowComponent();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() {
		FlowDescription fd = createTestInstance();
		
		String sNewName = "New Name";
		
		fd.setName(sNewName);
		assertEquals(fd.getName(),sNewName);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getServerID()}.
	 */
	@Test
	public void testGetName() {
		FlowDescription fd = createTestInstance();
		fd.getName();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#setDescription(java.lang.String)}.
	 */
	@Test
	public void testSetDescription() {
		FlowDescription fd = createTestInstance();
		
		String sNewDesc = "New Description";
		
		fd.setDescription(sNewDesc);
		assertEquals(fd.getDescription(),sNewDesc);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		FlowDescription fd = createTestInstance();
		fd.getDescription();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#setRights(java.lang.String)}.
	 */
	@Test
	public void testSetRights() {
		FlowDescription fd = createTestInstance();
		
		String sNewRights = "New Rights";
		
		fd.setRights(sNewRights);
		assertEquals(fd.getRights(),sNewRights);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getRights()}.
	 */
	@Test
	public void testGetRights() {
		FlowDescription fd = createTestInstance();
		fd.getRights();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#setCreator(java.lang.String)}.
	 */
	@Test
	public void testSetCreator() {
		FlowDescription fd = createTestInstance();
		
		String sNewCreator = "New Creatr";
		
		fd.setCreator(sNewCreator);
		assertEquals(fd.getCreator(),sNewCreator);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getCreator()}.
	 */
	@Test
	public void testGetCreator() {
		FlowDescription fd = createTestInstance();
		fd.getCreator();
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#setCreationDate(java.util.Date)}.
	 */
	@Test
	public void testSetCreationDate() {
		FlowDescription fd = createTestInstance();
		
		Date dateNew = new Date();
		
		fd.setCreationDate(dateNew);
		assertEquals(fd.getCreationDate(),dateNew);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getCreationDate()}.
	 */
	@Test
	public void testGetCreationDate() {
		FlowDescription fd = createTestInstance();
		assertEquals(fd.getCreationDate().getClass().getName(),Date.class.getName());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getExecutableComponentResourceForInstance(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testGetExecutableComponentResourceForInstance() {
		FlowDescription fd = createTestInstance();
		
		ExecutableComponentInstanceDescription ecid = ExecutableComponentInstanceDescriptionTest.createTestInstance();
		
		assertNull(fd.getExecutableComponentResourceForInstance(ecid.getExecutableComponent()));
		
		fd.addExecutableComponentInstance(ecid);
		
		assertNotNull(fd.getExecutableComponentResourceForInstance(ecid.getExecutableComponentInstance()));
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#addExecutableComponentInstance(org.meandre.core.store.repository.ExecutableComponentInstanceDescription)}.
	 */
	@Test
	public void testAddExecutableComponentInstance() {
		FlowDescription fd = createTestInstance();
		
		ExecutableComponentInstanceDescription ecid = ExecutableComponentInstanceDescriptionTest.createTestInstance();
		
		int iSize = fd.getExecutableComponentInstances().size();
		
		fd.addExecutableComponentInstance(ecid);
		
		assertEquals(iSize+1,fd.getExecutableComponentInstances().size());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#removeExecutableComponentInstance(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testRemoveExecutableComponentInstanceResource() {
		FlowDescription fd = createTestInstance();
		int iSize = -1;
		
		ExecutableComponentInstanceDescription ecid = ExecutableComponentInstanceDescriptionTest.createTestInstance();
		
		fd.addExecutableComponentInstance(ecid);
		iSize = fd.getExecutableComponentInstances().size();
		fd.removeExecutableComponentInstance(ecid.getExecutableComponentInstance());
		assertEquals(iSize-1,fd.getExecutableComponentInstances().size());

		fd.removeExecutableComponentInstance(ecid.getExecutableComponentInstance());
		assertEquals(0,fd.getExecutableComponentInstances().size());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#removeExecutableComponentInstance(org.meandre.core.store.repository.ExecutableComponentInstanceDescription)}.
	 */
	@Test
	public void testRemoveExecutableComponentInstanceExecutableComponentInstanceDescription() {
		FlowDescription fd = createTestInstance();
		int iSize = -1;
		
		ExecutableComponentInstanceDescription ecid = ExecutableComponentInstanceDescriptionTest.createTestInstance();
		
		fd.addExecutableComponentInstance(ecid);
		iSize = fd.getExecutableComponentInstances().size();
		fd.removeExecutableComponentInstance(ecid);
		assertEquals(iSize-1,fd.getExecutableComponentInstances().size());
		
		fd.removeExecutableComponentInstance(ecid);
		assertEquals(0,fd.getExecutableComponentInstances().size());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getExecutableComponentInstances()}.
	 */
	@Test
	public void testGetExecutableComponentInstances() {
		FlowDescription fd = createTestInstance();
		assertEquals(fd.getExecutableComponentInstances().size(),0);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getConnectorDescriptions()}.
	 */
	@Test
	public void testGetConnectorDescriptions() {
		FlowDescription fd = createTestInstance();
		assertEquals(fd.getConnectorDescriptions().size(),0);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getTags()}.
	 */
	@Test
	public void testGetTags() {
		FlowDescription fd = createTestInstance();
		assertEquals(fd.getTags().getTags().size(),0);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.FlowDescription#getModel()}.
	 */
	@Test
	public void testGetModel() {
		FlowDescription fd = createTestInstance();
		Model model = fd.getModel();
		RepositoryImpl ri = new RepositoryImpl(model);
		FlowDescription fdRegenerated = ri.getFlowDescription(fd.getFlowComponent());
		Model modRegen = fdRegenerated.getModel();
		
		// Check statements one way
		assertTrue(modRegen.containsAll(model));
		
		// Check statements the other way
		assertTrue(model.containsAll(modRegen));
	}

}
