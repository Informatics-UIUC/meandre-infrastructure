package org.meandre.core.repository;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.DataPortDescription;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.PropertiesDescriptionDefinition;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.repository.TagsDescription;
import org.meandre.demo.components.PushStringComponent;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This class test the behavior of an executable component description.
 * 
 * @author Xavier Llor&agrave;
 * 
 */
public class ExecutableComponentDescriptionTest {

	/** Counter to track different test examples generated */
	private static int iNumTestExamplesGenerated = 0;
	
	/** Creates a test instance.
	 * 
	 * @return The test instance
	 */
	ExecutableComponentDescription createTestInstance() {
		
		ExecutableComponentDescription dpdres = null;
		
		int iInstance = -1;
		
		// Get the next instances available
		iInstance = iNumTestExamplesGenerated++;
			
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource("http://meandre.org/test/ecd/"+iInstance);
		String sName = "test-"+iInstance;
		String sDescription = "desc-"+iInstance;
		String sRights = "rights-"+iInstance;
		String sCreator = "creator-"+iInstance;
		Date dateCreation = new Date();
		
		Set<RDFNode> setContext = new HashSet<RDFNode>();
		setContext.add(ModelFactory.createDefaultModel().createResource("http://meandre.org/test/ecd/"+iInstance+"/"));
		
		Resource resLocation = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/ecd/"+iInstance+"/"+PushStringComponent.class.getName());
		
		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		setInputs.add(DataPortDescriptionTest.createTestInstance());
		
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
		setOutputs.add(DataPortDescriptionTest.createTestInstance());
		
		PropertiesDescriptionDefinition pddProperties = PropertiesDescriptionDefinitionTest.createTestInstance();
		
		TagsDescription tagDesc = TagsDescriptionTest.createTestInstance();
		
		String sRunnable  = "test-"+iInstance;
		String sFiringPolicy = "test-"+iInstance;
		String sFormat = "test-"+iInstance;
		
		try {
			dpdres = new ExecutableComponentDescription(resExecutableComponent,
					sName, sDescription, sRights, sCreator, dateCreation,
					sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
					setInputs, setOutputs, pddProperties, tagDesc);
			
			fail("An exception should have been trown for using wrong runnable, format, and firing policy");
		} catch (CorruptedDescriptionException e) {
			// Correct behavior
		}
		
		sRunnable = "java";
		try {
			dpdres = new ExecutableComponentDescription(resExecutableComponent,
					sName, sDescription, sRights, sCreator, dateCreation,
					sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
					setInputs, setOutputs, pddProperties, tagDesc);
			
			fail("An exception should have been trown for using wrong format, and firing policy");
		} catch (CorruptedDescriptionException e) {
			// Correct behavior
		}
		
		sFormat = "java/class";
		try {
			dpdres = new ExecutableComponentDescription(resExecutableComponent,
					sName, sDescription, sRights, sCreator, dateCreation,
					sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
					setInputs, setOutputs, pddProperties, tagDesc);
			
			fail("An exception should have been trown for using wrong firing policy");
		} catch (CorruptedDescriptionException e) {
			// Correct behavior
		}
		
		sFiringPolicy = "all";
		try {
			dpdres = new ExecutableComponentDescription(resExecutableComponent,
					sName, sDescription, sRights, sCreator, dateCreation,
					sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
					setInputs, setOutputs, pddProperties, tagDesc);
			
		} catch (CorruptedDescriptionException e) {
			fail("An exception should have been never trown");
		}
		
		return dpdres;
	}
	
	

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#ExecutableComponentDescription(com.hp.hpl.jena.rdf.model.Resource, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.util.Set, com.hp.hpl.jena.rdf.model.Resource, java.util.Set, java.util.Set, org.meandre.core.store.repository.PropertiesDescriptionDefinition, org.meandre.core.store.repository.TagsDescription)}.
	 */
	@Test
	public void testExecutableComponentDescription() {
		createTestInstance();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getExecutableComponent()}.
	 */
	@Test
	public void testGetExecutableComponent() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getExecutableComponent();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getName()}.
	 */
	@Test
	public void testGetName() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getName();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getDescription();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getRights()}.
	 */
	@Test
	public void testGetRights() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getRights();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getCreator()}.
	 */
	@Test
	public void testGetCreator() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getCreator();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getCreationDate()}.
	 */
	@Test
	public void testGetCreationDate() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getCreationDate();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getRunnable()}.
	 */
	@Test
	public void testGetRunnable() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getRunnable();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getFiringPolicy()}.
	 */
	@Test
	public void testGetFiringPolicy() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getFiringPolicy();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getFormat()}.
	 */
	@Test
	public void testGetFormat() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getFormat();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getContext()}.
	 */
	@Test
	public void testGetContext() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getContext();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getLocation()}.
	 */
	@Test
	public void testGetLocation() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getLocation();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getInputs()}.
	 */
	@Test
	public void testGetInputs() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getInputs();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getInput(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testGetInput() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getInput(ecd.getInputs().iterator().next().getResource());
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getOutputs()}.
	 */
	@Test
	public void testGetOutputs() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getOutputs();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getOutput(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testGetOutput() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getOutput(ecd.getOutputs().iterator().next().getResource());
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getProperties()}.
	 */
	@Test
	public void testGetProperties() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getProperties();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getTags()}.
	 */
	@Test
	public void testGetTags() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.getTags();
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#getModel()}.
	 */
	@Test
	public void testGetModel() {
		ExecutableComponentDescription ecd = createTestInstance();
		Model model = ecd.getModel();
		RepositoryImpl ri = new RepositoryImpl(model);
		ExecutableComponentDescription ecdRegenerated = ri.getExecutableComponentDescription(ecd.getExecutableComponent());
		Model modRegen = ecdRegenerated.getModel();
		
		// Check statements one way
		assertTrue(modRegen.containsAll(model));
		
		// Check statements the other way
		assertTrue(model.containsAll(modRegen));
	}

	/**
	 * Test method for
	 * {@link org.meandre.core.store.repository.ExecutableComponentDescription#toString()}.
	 */
	@Test
	public void testToString() {
		ExecutableComponentDescription ecd = createTestInstance();
		ecd.toString();
	}

}
