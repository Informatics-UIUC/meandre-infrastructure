package org.meandre.demo.repository;



import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.core.store.repository.ConnectorDescription;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.repository.DataPortDescription;
import org.meandre.core.store.repository.ExecutableComponentDescription;
import org.meandre.core.store.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.store.repository.FlowDescription;
import org.meandre.core.store.repository.PropertiesDescription;
import org.meandre.core.store.repository.PropertiesDescriptionDefinition;
import org.meandre.core.store.repository.TagsDescription;
import org.meandre.demo.components.ConcatenateStringsComponent;
import org.meandre.demo.components.ForkByReference;
import org.meandre.demo.components.PrintObjectComponent;
import org.meandre.demo.components.PushStringComponent;
import org.meandre.demo.utils.DemoLoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** Complementary test class to generate repositories that act as test cases.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class DemoRepositoryGenerator {

	/** The demo logger */
	public static Logger log = DemoLoggerFactory.getDemoLogger();
	
	/** Base testing URL */
	public static String sTestBaseURL = "http://test.org";
	
	/** Base testing componentsURL */
	public static String sTestBaseComponentURL = sTestBaseURL+"/component/";
	
	/** Base testing flow URL */
	public static String sTestBaseFlowURL = sTestBaseURL+"/flow/";
	
	/** Create the description for org.meandre.demo.components.PushStringComponent.
	 * 
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getPushStringComponent() {
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL+"push-string");
		
		// General properties
		String sName = "Push String";
		String sDescription = "Pushes the string stored into the properties to the output";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<Resource> setContext = new HashSet<Resource>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL+PushStringComponent.class.getName());
		
		// Empty input ports
		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		
		// One output port
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
		Resource resDPDOutput = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/string");
		String sDPDIdent = resDPDOutput.toString(); 
		String sDPDName = "string";
		String sDPDDesc = "The string being pushed";
		try {
			setOutputs.add(new DataPortDescription(resDPDOutput,sDPDIdent,sDPDName,sDPDDesc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// Properties
		Hashtable<String,String> htValues = new Hashtable<String,String>();
		Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
		htValues.put("message", "Hello World!!! Happy Meandring!!!");
		htDescriptions.put("message", "The string message to be pushed trough the output port");
		PropertiesDescriptionDefinition pddProperties = new PropertiesDescriptionDefinition(htValues,htDescriptions);
		
		// Tags
		HashSet<String> hsTags = new HashSet<String>();
		hsTags.add("demo");
		hsTags.add("string");
		hsTags.add("hello_world");
 		TagsDescription tagDesc = new TagsDescription(hsTags);
		
		String sRunnable = "java";
		String sFiringPolicy = "all";
		String sFormat = "java/class";
		
		try {
			ecdRes = new ExecutableComponentDescription(resExecutableComponent,
					sName, sDescription, sRights, sCreator, dateCreation,
					sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
					setInputs, setOutputs, pddProperties, tagDesc);
			
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		return ecdRes;
	}

	/** Create the description for org.meandre.demo.components.ConcatenateStringsComponent.
	 * 
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getConcatenateStringsComponent() {
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL+"concatenate-strings");
		
		// General properties
		String sName = "Concatenate Strings";
		String sDescription = "Concatenates the to input string to the output";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<Resource> setContext = new HashSet<Resource>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL+ConcatenateStringsComponent.class.getName());
		
		// Empty input ports
		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		Resource resDPDInput1 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/input/string_one");
		String sDPDIn1Ident = resDPDInput1.toString(); 
		String sDPDIn1Name = "string_one";
		String sDPDIn1Desc = "The first string to concatenate";
		try {
			setInputs.add(new DataPortDescription(resDPDInput1,sDPDIn1Ident,sDPDIn1Name,sDPDIn1Desc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		Resource resDPDInput2 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/input/string_two");
		String sDPDIn2Ident = resDPDInput2.toString(); 
		String sDPDIn2Name = "string_two";
		String sDPDIn2Desc = "The second string to concatenate";
		try {
			setInputs.add(new DataPortDescription(resDPDInput2,sDPDIn2Ident,sDPDIn2Name,sDPDIn2Desc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// One output port
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
		Resource resDPDOutput = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/concatenated_string");
		String sDPDIdent = resDPDOutput.toString(); 
		String sDPDName = "concatenated_string";
		String sDPDDesc = "The concatenated string";
		try {
			setOutputs.add(new DataPortDescription(resDPDOutput,sDPDIdent,sDPDName,sDPDDesc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// Properties
		Hashtable<String,String> htValues = new Hashtable<String,String>();
		Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
		PropertiesDescriptionDefinition pddProperties = new PropertiesDescriptionDefinition(htValues,htDescriptions);
		
		// Tags
		HashSet<String> hsTags = new HashSet<String>();
		hsTags.add("demo");
		hsTags.add("string");
		hsTags.add("concatenate");
 		TagsDescription tagDesc = new TagsDescription(hsTags);
		
		String sRunnable = "java";
		String sFiringPolicy = "all";
		String sFormat = "java/class";
		
		try {
			ecdRes = new ExecutableComponentDescription(resExecutableComponent,
					sName, sDescription, sRights, sCreator, dateCreation,
					sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
					setInputs, setOutputs, pddProperties, tagDesc);
			
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		return ecdRes;
	}

	/** Create the description for org.meandre.demo.components.PushString.
	 * 
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getPrintObjectComponent() {
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL+"print-object");
		
		// General properties
		String sName = "Print Object";
		String sDescription = "Prints the object in the input to the standard output";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<Resource> setContext = new HashSet<Resource>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL+PrintObjectComponent.class.getName());
		
		// Empty input ports
		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		Resource resDPDInput = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/input/object");
		String sDPDInIdent = resDPDInput.toString(); 
		String sDPDInName = "object";
		String sDPDInDesc = "The object to print";
		try {
			setInputs.add(new DataPortDescription(resDPDInput,sDPDInIdent,sDPDInName,sDPDInDesc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// One output port
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
		
		// Properties
		Hashtable<String,String> htValues = new Hashtable<String,String>();
		Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
		PropertiesDescriptionDefinition pddProperties = new PropertiesDescriptionDefinition(htValues,htDescriptions);
		
		// Tags
		HashSet<String> hsTags = new HashSet<String>();
		hsTags.add("demo");
		hsTags.add("object");
		hsTags.add("print");
		TagsDescription tagDesc = new TagsDescription(hsTags);
		
		String sRunnable = "java";
		String sFiringPolicy = "all";
		String sFormat = "java/class";
		
		try {
			ecdRes = new ExecutableComponentDescription(resExecutableComponent,
					sName, sDescription, sRights, sCreator, dateCreation,
					sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
					setInputs, setOutputs, pddProperties, tagDesc);
			
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		return ecdRes;
	}

	/** Create the description for org.meandre.demo.components.PushString.
	 * 
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getReferenceForkComponent() {
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL+"fork_by_reference");
		
		// General properties
		String sName = "Reference Fork";
		String sDescription = "Pushes the input object to its two ports";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<Resource> setContext = new HashSet<Resource>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sTestBaseComponentURL+ForkByReference.class.getName());
		
		// Empty input ports
		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		Resource resDPDInput = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/input/object");
		String sDPDInIdent = resDPDInput.toString(); 
		String sDPDInName = "object";
		String sDPDInDesc = "The object to fork";
		try {
			setInputs.add(new DataPortDescription(resDPDInput,sDPDInIdent,sDPDInName,sDPDInDesc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// One output port
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
		Resource resDPDOutput = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/object_ref_one");
		String sDPDIdent = resDPDOutput.toString(); 
		String sDPDName = "object_ref_one";
		String sDPDDesc = "The first reference of the object";
		try {
			setOutputs.add(new DataPortDescription(resDPDOutput,sDPDIdent,sDPDName,sDPDDesc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		Resource resDPDOutput2 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/object_ref_two");
		String sDPDIdent2 = resDPDOutput2.toString(); 
		String sDPDName2 = "object_ref_two";
		String sDPDDesc2 = "The first reference of the object";
		try {
			setOutputs.add(new DataPortDescription(resDPDOutput2,sDPDIdent2,sDPDName2,sDPDDesc2));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// Properties
		Hashtable<String,String> htValues = new Hashtable<String,String>();
		Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
		PropertiesDescriptionDefinition pddProperties = new PropertiesDescriptionDefinition(htValues,htDescriptions);
		
		// Tags
		HashSet<String> hsTags = new HashSet<String>();
		hsTags.add("demo");
		hsTags.add("object");
		hsTags.add("fork");
 		TagsDescription tagDesc = new TagsDescription(hsTags);
		
		String sRunnable = "java";
		String sFiringPolicy = "all";
		String sFormat = "java/class";
		
		try {
			ecdRes = new ExecutableComponentDescription(resExecutableComponent,
					sName, sDescription, sRights, sCreator, dateCreation,
					sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
					setInputs, setOutputs, pddProperties, tagDesc);
			
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		return ecdRes;
	}

	
	/** Generates a 4 component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldRepository() {
		
		///
		// Create the components
		//
		ExecutableComponentDescription ecdPS = getPushStringComponent(); 
		ExecutableComponentDescription ecdCS = getConcatenateStringsComponent(); 
		ExecutableComponentDescription ecdPO = getPrintObjectComponent(); 
		
		//
		// Assemble the flow
		//
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"test-hello-world");
		
		//
		// Flow properties
		//
		String sName = "Hello World!!!";
		String sDescription = "A simple hello world test";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		//
		// Create the instances
		//
		Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances = new HashSet<ExecutableComponentInstanceDescription>();

		Resource resInsPS0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/push_string/0");
		Resource resInsPS0Component = ecdPS.getExecutableComponent();
		String sInsPS0Name = "Push String 0";
		String sInsPS0Desc = "Push hello world";
		PropertiesDescription pdPInsPS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS0 = new ExecutableComponentInstanceDescription(resInsPS0,resInsPS0Component,sInsPS0Name,sInsPS0Desc,pdPInsPS0Properties);

		Resource resInsPS1 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/push_string/1");
		Resource resInsPS1Component = ecdPS.getExecutableComponent();
		String sInsPS1Name = "Push String 1";
		String sInsPS1Desc = "Push hello world";
		PropertiesDescription pdPInsPS1Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS1 = new ExecutableComponentInstanceDescription(resInsPS1,resInsPS1Component,sInsPS1Name,sInsPS1Desc,pdPInsPS1Properties);

		Resource resInsCS0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/concatenate_string/2");
		Resource resInsCS0Component = ecdCS.getExecutableComponent();
		String sInsCS0Name = "Concatenate String 0";
		String sInsCS0Desc = "Concatenates two strings";
		PropertiesDescription pdPInsCS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCS0 = new ExecutableComponentInstanceDescription(resInsCS0,resInsCS0Component,sInsCS0Name,sInsCS0Desc,pdPInsCS0Properties);

		Resource resInsPO0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/print-object/3");
		Resource resInsPO0Component = ecdPO.getExecutableComponent();
		String sInsPO0Name = "Print Object 0";
		String sInsPO0Desc = "Prints the concatenated object";
		PropertiesDescription pdPInsPO0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPO0 = new ExecutableComponentInstanceDescription(resInsPO0,resInsPO0Component,sInsPO0Name,sInsPO0Desc,pdPInsPO0Properties);

		setExecutableComponentInstances.add(ecidPS0);
		setExecutableComponentInstances.add(ecidPS1);
		setExecutableComponentInstances.add(ecidCS0);
		setExecutableComponentInstances.add(ecidPO0);
		
		//
		// Connecting the instances
		//
		Set<ConnectorDescription> setConnectorDescription = new HashSet<ConnectorDescription>();
		
		Iterator<DataPortDescription> iter = ecdCS.getInputs().iterator();
		Resource resOne = iter.next().getResource();
		Resource resTwo = iter.next().getResource();
		
		ConnectorDescription cdPS0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/0"),
				ecidPS0.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resOne);

		ConnectorDescription cdPS1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/1"),
				ecidPS1.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resTwo);
		
		ConnectorDescription cdPO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/2"),
				ecidCS0.getExecutableComponentInstance(), ecdCS.getOutputs().iterator().next().getResource(),
				ecidPO0.getExecutableComponentInstance(), ecdPO.getInputs().iterator().next().getResource());

		setConnectorDescription.add(cdPS0);
		setConnectorDescription.add(cdPS1);
		setConnectorDescription.add(cdPO0);
		
		//
		// Tags
		//
		HashSet<String> hsTags = new HashSet<String>();
		hsTags.add("demo");
		hsTags.add("hello_world");
 		TagsDescription tagsDesc = new TagsDescription(hsTags);
		
 		//
 		// Create the flow
 		//
		FlowDescription fd = new FlowDescription(resFlowComponent, sName,
				sDescription, sRights, sCreator, dateCreation,
				setExecutableComponentInstances, setConnectorDescription,
				tagsDesc);
		
		//
		// Return the aggregated model
		//
		return ecdPS.getModel().add(ecdCS.getModel())
                               .add(ecdPO.getModel())
                               .add(fd.getModel());
	}



	/** Generates a 4 component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out; three extra dangling instances
	 * placed to the check proper functionality.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldWithDanglingComponentsRepository () {
		
		///
		// Create the components
		//
		ExecutableComponentDescription ecdPS = getPushStringComponent(); 
		ExecutableComponentDescription ecdCS = getConcatenateStringsComponent(); 
		ExecutableComponentDescription ecdPO = getPrintObjectComponent(); 
		
		//
		// Assemble the flow
		//
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"test-hello-world");
		
		//
		// Flow properties
		//
		String sName = "Hello World!!!";
		String sDescription = "A simple hello world test";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		//
		// Create the instances
		//
		Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances = new HashSet<ExecutableComponentInstanceDescription>();

		// Real
		Resource resInsPS0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/push_string/0");
		Resource resInsPS0Component = ecdPS.getExecutableComponent();
		String sInsPS0Name = "Push String 0";
		String sInsPS0Desc = "Push hello world";
		PropertiesDescription pdPInsPS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS0 = new ExecutableComponentInstanceDescription(resInsPS0,resInsPS0Component,sInsPS0Name,sInsPS0Desc,pdPInsPS0Properties);

		Resource resInsPS1 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/push_string/1");
		Resource resInsPS1Component = ecdPS.getExecutableComponent();
		String sInsPS1Name = "Push String 1";
		String sInsPS1Desc = "Push hello world";
		PropertiesDescription pdPInsPS1Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS1 = new ExecutableComponentInstanceDescription(resInsPS1,resInsPS1Component,sInsPS1Name,sInsPS1Desc,pdPInsPS1Properties);

		Resource resInsCS0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/concatenate_string/2");
		Resource resInsCS0Component = ecdCS.getExecutableComponent();
		String sInsCS0Name = "Concatenate String 0";
		String sInsCS0Desc = "Concatenates two strings";
		PropertiesDescription pdPInsCS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCS0 = new ExecutableComponentInstanceDescription(resInsCS0,resInsCS0Component,sInsCS0Name,sInsCS0Desc,pdPInsCS0Properties);

		Resource resInsPO0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/print-object/3");
		Resource resInsPO0Component = ecdPO.getExecutableComponent();
		String sInsPO0Name = "Print Object 0";
		String sInsPO0Desc = "Prints the concatenated object";
		PropertiesDescription pdPInsPO0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPO0 = new ExecutableComponentInstanceDescription(resInsPO0,resInsPO0Component,sInsPO0Name,sInsPO0Desc,pdPInsPO0Properties);

		setExecutableComponentInstances.add(ecidPS0);
		setExecutableComponentInstances.add(ecidPS1);
		setExecutableComponentInstances.add(ecidCS0);
		setExecutableComponentInstances.add(ecidPO0);
		
		// Dangling instances
		
		Resource resInsPSD = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/push_string/4");
		Resource resInsPSDComponent = ecdPS.getExecutableComponent();
		String sInsPSDName = "Push String Dangling";
		String sInsPSDDesc = "Push hello world";
		PropertiesDescription pdPInsPSDProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPSD = new ExecutableComponentInstanceDescription(resInsPSD,resInsPSDComponent,sInsPSDName,sInsPSDDesc,pdPInsPSDProperties);

		Resource resInsCSD = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/concatenate_string/5");
		Resource resInsCSDComponent = ecdCS.getExecutableComponent();
		String sInsCSDName = "Concatenate String Dangling";
		String sInsCSDDesc = "Concatenates two strings";
		PropertiesDescription pdPInsCSDProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCSD = new ExecutableComponentInstanceDescription(resInsCSD,resInsCSDComponent,sInsCSDName,sInsCSDDesc,pdPInsCSDProperties);

		Resource resInsPOD = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/print-object/6");
		Resource resInsPODComponent = ecdPO.getExecutableComponent();
		String sInsPODName = "Print Object Dangling";
		String sInsPODDesc = "Prints the concatenated object";
		PropertiesDescription pdPInsPODProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPOD = new ExecutableComponentInstanceDescription(resInsPOD,resInsPODComponent,sInsPODName,sInsPODDesc,pdPInsPODProperties);

		setExecutableComponentInstances.add(ecidPSD);
		setExecutableComponentInstances.add(ecidCSD);
		setExecutableComponentInstances.add(ecidPOD);
		
		
		//
		// Connecting the instances
		//
		Set<ConnectorDescription> setConnectorDescription = new HashSet<ConnectorDescription>();
		
		Iterator<DataPortDescription> iter = ecdCS.getInputs().iterator();
		Resource resOne = iter.next().getResource();
		Resource resTwo = iter.next().getResource();
		
		ConnectorDescription cdPS0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/0"),
				ecidPS0.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resOne);

		ConnectorDescription cdPS1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/1"),
				ecidPS1.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resTwo);
		
		ConnectorDescription cdPO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/2"),
				ecidCS0.getExecutableComponentInstance(), ecdCS.getOutputs().iterator().next().getResource(),
				ecidPO0.getExecutableComponentInstance(), ecdPO.getInputs().iterator().next().getResource());

		setConnectorDescription.add(cdPS0);
		setConnectorDescription.add(cdPS1);
		setConnectorDescription.add(cdPO0);
		
		//
		// Tags
		//
		HashSet<String> hsTags = new HashSet<String>();
		hsTags.add("demo");
		hsTags.add("hello_world");
 		TagsDescription tagsDesc = new TagsDescription(hsTags);
		
 		//
 		// Create the flow
 		//
		FlowDescription fd = new FlowDescription(resFlowComponent, sName,
				sDescription, sRights, sCreator, dateCreation,
				setExecutableComponentInstances, setConnectorDescription,
				tagsDesc);
		
		//
		// Return the aggregated model
		//
		return ecdPS.getModel().add(ecdCS.getModel())
                               .add(ecdPO.getModel())
                               .add(fd.getModel());
	}

	/** Generates a 4 component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out; three extra dangling instances
	 * placed to the check proper functionality.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository () {
		
		///
		// Create the components
		//
		ExecutableComponentDescription ecdPS = getPushStringComponent(); 
		ExecutableComponentDescription ecdCS = getConcatenateStringsComponent(); 
		ExecutableComponentDescription ecdPO = getPrintObjectComponent(); 
		ExecutableComponentDescription ecdFO = getReferenceForkComponent();
		//
		// Assemble the flow
		//
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"test-hello-world");
		
		//
		// Flow properties
		//
		String sName = "Hello World!!!";
		String sDescription = "A simple hello world test";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		//
		// Create the instances
		//
		Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances = new HashSet<ExecutableComponentInstanceDescription>();
	
		// Real
		Resource resInsPS0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/push_string/0");
		Resource resInsPS0Component = ecdPS.getExecutableComponent();
		String sInsPS0Name = "Push String 0";
		String sInsPS0Desc = "Push hello world";
		PropertiesDescription pdPInsPS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS0 = new ExecutableComponentInstanceDescription(resInsPS0,resInsPS0Component,sInsPS0Name,sInsPS0Desc,pdPInsPS0Properties);
	
		Resource resInsPS1 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/push_string/1");
		Resource resInsPS1Component = ecdPS.getExecutableComponent();
		String sInsPS1Name = "Push String 1";
		String sInsPS1Desc = "Push hello world";
		PropertiesDescription pdPInsPS1Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS1 = new ExecutableComponentInstanceDescription(resInsPS1,resInsPS1Component,sInsPS1Name,sInsPS1Desc,pdPInsPS1Properties);
	
		Resource resInsCS0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/concatenate_string/2");
		Resource resInsCS0Component = ecdCS.getExecutableComponent();
		String sInsCS0Name = "Concatenate String 0";
		String sInsCS0Desc = "Concatenates two strings";
		PropertiesDescription pdPInsCS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCS0 = new ExecutableComponentInstanceDescription(resInsCS0,resInsCS0Component,sInsCS0Name,sInsCS0Desc,pdPInsCS0Properties);
	
		Resource resInsRF0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/fork_by_reference/3");
		Resource resInsRF0Component = ecdFO.getExecutableComponent();
		String sInsRF0Name = "Forks an object";
		String sInsRF0Desc = "Fork and object by reference";
		PropertiesDescription pdPInsRF0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidFO0 = new ExecutableComponentInstanceDescription(resInsRF0,resInsRF0Component,sInsRF0Name,sInsRF0Desc,pdPInsRF0Properties);
	
		Resource resInsPO0 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/print-object/4");
		Resource resInsPO0Component = ecdPO.getExecutableComponent();
		String sInsPO0Name = "Print Object 0";
		String sInsPO0Desc = "Prints the concatenated object";
		PropertiesDescription pdPInsPO0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPO0 = new ExecutableComponentInstanceDescription(resInsPO0,resInsPO0Component,sInsPO0Name,sInsPO0Desc,pdPInsPO0Properties);
	
		Resource resInsPO1 = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/print-object/5");
		Resource resInsPO1Component = ecdPO.getExecutableComponent();
		String sInsPO1Name = "Print Object 0";
		String sInsPO1Desc = "Prints the concatenated object";
		PropertiesDescription pdPInsPO1Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPO1 = new ExecutableComponentInstanceDescription(resInsPO1,resInsPO1Component,sInsPO1Name,sInsPO1Desc,pdPInsPO1Properties);
	
		setExecutableComponentInstances.add(ecidPS0);
		setExecutableComponentInstances.add(ecidPS1);
		setExecutableComponentInstances.add(ecidCS0);
		setExecutableComponentInstances.add(ecidFO0);
		setExecutableComponentInstances.add(ecidPO0);
		setExecutableComponentInstances.add(ecidPO1);
		
		// Dangling instances
		
		Resource resInsPSD = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/push_string/6");
		Resource resInsPSDComponent = ecdPS.getExecutableComponent();
		String sInsPSDName = "Push String Dangling";
		String sInsPSDDesc = "Push hello world";
		PropertiesDescription pdPInsPSDProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPSD = new ExecutableComponentInstanceDescription(resInsPSD,resInsPSDComponent,sInsPSDName,sInsPSDDesc,pdPInsPSDProperties);
	
		Resource resInsCSD = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/concatenate_string/7");
		Resource resInsCSDComponent = ecdCS.getExecutableComponent();
		String sInsCSDName = "Concatenate String Dangling";
		String sInsCSDDesc = "Concatenates two strings";
		PropertiesDescription pdPInsCSDProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCSD = new ExecutableComponentInstanceDescription(resInsCSD,resInsCSDComponent,sInsCSDName,sInsCSDDesc,pdPInsCSDProperties);
	
		Resource resInsPOD = ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"instance/print-object/8 ");
		Resource resInsPODComponent = ecdPO.getExecutableComponent();
		String sInsPODName = "Print Object Dangling";
		String sInsPODDesc = "Prints the concatenated object";
		PropertiesDescription pdPInsPODProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPOD = new ExecutableComponentInstanceDescription(resInsPOD,resInsPODComponent,sInsPODName,sInsPODDesc,pdPInsPODProperties);
	
		setExecutableComponentInstances.add(ecidPSD);
		setExecutableComponentInstances.add(ecidCSD);
		setExecutableComponentInstances.add(ecidPOD);
		
		
		//
		// Connecting the instances
		//
		Set<ConnectorDescription> setConnectorDescription = new HashSet<ConnectorDescription>();
		
		Iterator<DataPortDescription> iter = ecdCS.getInputs().iterator();
		Resource resOne = iter.next().getResource();
		Resource resTwo = iter.next().getResource();
		
		Iterator<DataPortDescription> iter2 = ecdFO.getOutputs().iterator();
		Resource resFOOne = iter2.next().getResource();
		Resource resFOTwo = iter2.next().getResource();
		
		ConnectorDescription cdPS0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/0"),
				ecidPS0.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resOne);
	
		ConnectorDescription cdPS1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/1"),
				ecidPS1.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resTwo);
		
		ConnectorDescription cdPO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/2"),
				ecidCS0.getExecutableComponentInstance(), ecdCS.getOutputs().iterator().next().getResource(),
				ecidFO0.getExecutableComponentInstance(), ecdFO.getInputs().iterator().next().getResource());
	
		ConnectorDescription cdFO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/3"),
				ecidFO0.getExecutableComponentInstance(), resFOOne,
				ecidPO0.getExecutableComponentInstance(), ecdPO.getInputs().iterator().next().getResource());
	
		ConnectorDescription cdFO1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sTestBaseFlowURL+"connector/4"),
				ecidFO0.getExecutableComponentInstance(), resFOTwo,
				ecidPO1.getExecutableComponentInstance(), ecdPO.getInputs().iterator().next().getResource());
	
		setConnectorDescription.add(cdPS0);
		setConnectorDescription.add(cdPS1);
		setConnectorDescription.add(cdPO0);
		setConnectorDescription.add(cdFO0);
		setConnectorDescription.add(cdFO1);
		
		//
		// Tags
		//
		HashSet<String> hsTags = new HashSet<String>();
		hsTags.add("demo");
		hsTags.add("hello_world");
		TagsDescription tagsDesc = new TagsDescription(hsTags);
		
		//
		// Create the flow
		//
		FlowDescription fd = new FlowDescription(resFlowComponent, sName,
				sDescription, sRights, sCreator, dateCreation,
				setExecutableComponentInstances, setConnectorDescription,
				tagsDesc);
		
		//
		// Return the aggregated model
		//
		return ecdPS.getModel().add(ecdCS.getModel())
	                           .add(ecdPO.getModel())
	                           .add(ecdFO.getModel())
	                           .add(fd.getModel());
	}

	
	
}
