package org.meandre.demo.repository;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.meandre.core.repository.ConnectorDescription;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.DataPortDescription;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.PropertiesDescription;
import org.meandre.core.repository.PropertiesDescriptionDefinition;
import org.meandre.core.repository.TagsDescription;
import org.meandre.demo.components.ConcatenateStringsComponent;
import org.meandre.demo.components.ForkByReference;
import org.meandre.demo.components.PrintObjectComponent;
import org.meandre.demo.components.PushStringComponent;
import org.meandre.demo.utils.DemoLoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
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
	
	/** A unique reference ID */
	private static long lUniqueID = 0;
	
	/** The semaphore con implement MUTEX against the unique ID */
	private static Semaphore semUniqueID = new Semaphore(1,true);
		
	/** Create the description for org.meandre.demo.components.PushStringComponent.
	 * 
	 * @param sBaseURL The base URL
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getPushStringComponent(String sBaseURL) {
		
		sBaseURL += "/component/";
		
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sBaseURL+"push_string");
		
		// General properties
		String sName = "Push String";
		String sDescription = "Pushes the string stored into the properties to the output";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<RDFNode> setContext = new HashSet<RDFNode>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sBaseURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sBaseURL+PushStringComponent.class.getName());
		
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
		htValues.put("times", "1");
		htDescriptions.put("times", "Number of time to push the string");
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

	/** Create the description for org.meandre.demo.components.ForkByReferenceComponent.
	 * 
	 * @param sBaseURL The base URL
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getForkComponent(String sBaseURL) {
		
		sBaseURL += "/component/";
		
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sBaseURL+"fork_2_by_reference");
		
		// General properties
		String sName = "Fork 2 by reference";
		String sDescription = "Pushes the string stored into the properties to the output";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<RDFNode> setContext = new HashSet<RDFNode>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sBaseURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sBaseURL+ForkByReference.class.getName());
		
		// Empty input ports
		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		Resource resDPDInput1 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/input/object");
		String sDPDIn1Ident = resDPDInput1.toString(); 
		String sDPDIn1Name = "object";
		String sDPDIn1Desc = "The object to fork";
		try {
			setInputs.add(new DataPortDescription(resDPDInput1,sDPDIn1Ident,sDPDIn1Name,sDPDIn1Desc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// One output port
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
		Resource resDPDOutput1 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/object-ref-one");
		String sDPDIdent1 = resDPDOutput1.toString(); 
		String sDPDName1 = "object_ref_one";
		String sDPDDesc1 = "The first object";
		try {
			setOutputs.add(new DataPortDescription(resDPDOutput1,sDPDIdent1,sDPDName1,sDPDDesc1));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		Resource resDPDOutput2 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/object-ref-two");
		String sDPDIdent2 = resDPDOutput2.toString(); 
		String sDPDName2 = "object_ref_two";
		String sDPDDesc2 = "The first object";
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
		hsTags.add("fork");
		hsTags.add("reference");
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
	 * @param sBaseURL The base URL
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getConcatenateStringsComponent(String sBaseURL) {
		
		sBaseURL += "/component/";
		
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sBaseURL+"concatenate-strings");
		
		// General properties
		String sName = "Concatenate Strings";
		String sDescription = "Concatenates the to input string to the output";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<RDFNode> setContext = new HashSet<RDFNode>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sBaseURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sBaseURL+ConcatenateStringsComponent.class.getName());
		
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

	/** The Python code that does the upper case conversion */
//	private final static String sPythonToUpper = 
//		"def initialize(ccp):\n" +
//		"   print \"Initialize called from Python\"\n"+
//		"\n" +
//		"def execute(cc):\n" +
//		"   print \"Execute called from Python\", cc \n" +
//		"   s = cc.getDataComponentFromInput(\"string\") \n" +
//		"   cc.pushDataComponentToOutput(\"string\",s.upper()) \n" +
//		"\n" +
//		"def dispose(ccp):\n" +
//		"   print \"Dispose called from Python\"\n" +
//		"\n";

	private final static String sPythonToUpper = 
		"def initialize(ccp):\n" +
		"   pass\n"+
		"\n" +
		"def execute(cc):\n" +
		"   s = cc.getDataComponentFromInput(\"string\") \n" +
		"   cc.pushDataComponentToOutput(\"string\",s.upper()) \n" +
		"\n" +
		"def dispose(ccp):\n" +
		"   pass\n" +
		"\n";
	
	/** Create the description for a python to uppercase component.
	 * 
	 * @param sBaseURL The base URL
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getToUpperComponent(String sBaseURL) {
		
		sBaseURL += "/component/";
		
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sBaseURL+"to-uppercase");
		
		// General properties
		String sName = "To Uppercase";
		String sDescription = "Turns the input string into an upper case and pushes it to the output";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<RDFNode> setContext = new HashSet<RDFNode>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sBaseURL));
		setContext.add(ModelFactory.createDefaultModel().createTypedLiteral(sPythonToUpper));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sBaseURL);
		
		// Empty input ports
		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		Resource resDPDInput1 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/input/string");
		String sDPDIn1Ident = resDPDInput1.toString(); 
		String sDPDIn1Name = "string";
		String sDPDIn1Desc = "The string to convert";
		try {
			setInputs.add(new DataPortDescription(resDPDInput1,sDPDIn1Ident,sDPDIn1Name,sDPDIn1Desc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// One output port
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
		Resource resDPDOutput = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/string");
		String sDPDIdent = resDPDOutput.toString(); 
		String sDPDName = "string";
		String sDPDDesc = "The uppercased string";
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
		hsTags.add("uppercase");
 		TagsDescription tagDesc = new TagsDescription(hsTags);
		
		String sRunnable = "python";
		String sFiringPolicy = "all";
		String sFormat = "jython";
		
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

	
	/** The Lisp code that does the upper case conversion */
//	private final static String sLispPassThrough = 
//		"(defn initialize [x] (.(. System out) (println \"Initialize called from Lisp\")) )\n"+
//		"(defn execute [x] \n"+
//		"        (.(. System out) (println \"Execute called from Lisp\")) \n" +
//		"        (. x (pushDataComponentToOutput \n"+
//		"                 \"string\"\n"+
//		"                 (. x (getDataComponentFromInput \"string\")))) )\n"+
//		"(defn dispose [x] (.(. System out) (println \"Dispose called from Lisp\")) )" +
//		"\n";
	
	private final static String sLispPassThrough = 
		"(defn initialize [x] 1 )\n"+
		"(defn execute [x] \n"+
		"        (. x (pushDataComponentToOutput \n"+
		"                 \"string\"\n"+
		"                 (. x (getDataComponentFromInput \"string\")))) )\n"+
		"(defn dispose [x] 1 )" +
		"\n";
	
	/** Create the description for a lisp that change Es for As component.
	 * 
	 * @param sBaseURL The base URL
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getPassThroughComponent(String sBaseURL) {
		
		sBaseURL += "/component/";
		
		
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sBaseURL+"pass-through");
		
		// General properties
		String sName = "Pass Through";
		String sDescription = "Passes the input string to the output string with no modifications";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<RDFNode> setContext = new HashSet<RDFNode>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sBaseURL));
		setContext.add(ModelFactory.createDefaultModel().createTypedLiteral(sLispPassThrough));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sBaseURL);
		
		// Empty input ports
		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		Resource resDPDInput1 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/input/string");
		String sDPDIn1Ident = resDPDInput1.toString(); 
		String sDPDIn1Name = "string";
		String sDPDIn1Desc = "The string to convert";
		try {
			setInputs.add(new DataPortDescription(resDPDInput1,sDPDIn1Ident,sDPDIn1Name,sDPDIn1Desc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// One output port
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
		Resource resDPDOutput = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/string");
		String sDPDIdent = resDPDOutput.toString(); 
		String sDPDName = "string";
		String sDPDDesc = "The converted string";
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
		hsTags.add("pass through");
 		TagsDescription tagDesc = new TagsDescription(hsTags);
		
		String sRunnable = "lisp";
		String sFiringPolicy = "all";
		String sFormat = "clojure";
		
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
	 * @param sBaseURL
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getPrintObjectComponent(String sBaseURL) {
		sBaseURL += "/component/";
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sBaseURL+"print-object");
		
		// General properties
		String sName = "Print Object";
		String sDescription = "Prints the object in the input to the standard output";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<RDFNode> setContext = new HashSet<RDFNode>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sBaseURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sBaseURL+PrintObjectComponent.class.getName());
		
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
		htValues.put("count", "true"); 
		htDescriptions.put("count", "If set to true prints the count of printed objects");
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
	 * @param sBaseURL The base URL
	 * @return The executable component description
	 */
	private static ExecutableComponentDescription getReferenceForkComponent(String sBaseURL) {
		sBaseURL += "/component/";
		
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sBaseURL+"fork_by_reference");
		
		// General properties
		String sName = "Reference Fork";
		String sDescription = "Pushes the input object to its two ports";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		// Context
		Set<RDFNode> setContext = new HashSet<RDFNode>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sBaseURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sBaseURL+ForkByReference.class.getName());
		
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
		return getTestHelloWorldRepository(sTestBaseURL);
	}
	
	/** Generates a unique 4 component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 * @throws InterruptedException Failed to get the new unique ID
	 */
	public static Model getNextTestHelloWorldRepository() throws InterruptedException {
		long l = -1;
		semUniqueID.acquire();
		l = lUniqueID++;
		semUniqueID.release();
		return getTestHelloWorldRepository(sTestBaseURL+"/id/"+l+"/");
	}
	
	/** Generates a 4 component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out.
	 * 
	 * @param sBaseURL The base URL
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldRepository(String sBaseURL) {
		
		///
		// Create the components
		//
		ExecutableComponentDescription ecdPS = getPushStringComponent(sBaseURL); 
		ExecutableComponentDescription ecdCS = getConcatenateStringsComponent(sBaseURL); 
		ExecutableComponentDescription ecdPO = getPrintObjectComponent(sBaseURL); 
		
		sBaseURL += "/flow/test-hello-world";
		
		//
		// Assemble the flow
		//
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource(sBaseURL);
		
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

		Resource resInsPS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/0");
		Resource resInsPS0Component = ecdPS.getExecutableComponent();
		String sInsPS0Name = "Push String 0";
		String sInsPS0Desc = "Push hello world";
		PropertiesDescription pdPInsPS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS0 = new ExecutableComponentInstanceDescription(resInsPS0,resInsPS0Component,sInsPS0Name,sInsPS0Desc,pdPInsPS0Properties);

		Resource resInsPS1 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/1");
		Resource resInsPS1Component = ecdPS.getExecutableComponent();
		String sInsPS1Name = "Push String 1";
		String sInsPS1Desc = "Push hello world";
		PropertiesDescription pdPInsPS1Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS1 = new ExecutableComponentInstanceDescription(resInsPS1,resInsPS1Component,sInsPS1Name,sInsPS1Desc,pdPInsPS1Properties);

		Resource resInsCS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/concatenate_string/2");
		Resource resInsCS0Component = ecdCS.getExecutableComponent();
		String sInsCS0Name = "Concatenate String 0";
		String sInsCS0Desc = "Concatenates two strings";
		PropertiesDescription pdPInsCS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCS0 = new ExecutableComponentInstanceDescription(resInsCS0,resInsCS0Component,sInsCS0Name,sInsCS0Desc,pdPInsCS0Properties);

		Resource resInsPO0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/print-object/3");
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
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/0"),
				ecidPS0.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resOne);

		ConnectorDescription cdPS1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/1"),
				ecidPS1.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resTwo);
		
		ConnectorDescription cdPO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/2"),
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
		return getTestHelloWorldWithDanglingComponentsRepository(sTestBaseURL);
	}
	
	/** Generates a 4 unique component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out; three extra dangling instances
	 * placed to the check proper functionality.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 * @throws InterruptedException Could not get a new unique ID
	 */
	public static Model getNextTestHelloWorldWithDanglingComponentsRepository () throws InterruptedException {
		long l = -1;
		semUniqueID.acquire();
		l = lUniqueID++;
		semUniqueID.release();
		return getTestHelloWorldWithDanglingComponentsRepository(sTestBaseURL+"/id/"+l+"/");
	}


	/** Generates a 4 component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out; three extra dangling instances
	 * placed to the check proper functionality.
	 * 
	 * @param sBaseURL The base URL
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldWithDanglingComponentsRepository (String sBaseURL) {
		
		///
		// Create the components
		//
		ExecutableComponentDescription ecdPS = getPushStringComponent(sBaseURL); 
		ExecutableComponentDescription ecdCS = getConcatenateStringsComponent(sBaseURL); 
		ExecutableComponentDescription ecdPO = getPrintObjectComponent(sBaseURL); 
		
		sBaseURL += "/flow/test-hello-world-dangling";
		
		//
		// Assemble the flow
		//
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource(sBaseURL);
		
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
		Resource resInsPS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/0");
		Resource resInsPS0Component = ecdPS.getExecutableComponent();
		String sInsPS0Name = "Push String 0";
		String sInsPS0Desc = "Push hello world";
		PropertiesDescription pdPInsPS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS0 = new ExecutableComponentInstanceDescription(resInsPS0,resInsPS0Component,sInsPS0Name,sInsPS0Desc,pdPInsPS0Properties);

		Resource resInsPS1 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/1");
		Resource resInsPS1Component = ecdPS.getExecutableComponent();
		String sInsPS1Name = "Push String 1";
		String sInsPS1Desc = "Push hello world";
		PropertiesDescription pdPInsPS1Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS1 = new ExecutableComponentInstanceDescription(resInsPS1,resInsPS1Component,sInsPS1Name,sInsPS1Desc,pdPInsPS1Properties);

		Resource resInsCS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/concatenate_string/2");
		Resource resInsCS0Component = ecdCS.getExecutableComponent();
		String sInsCS0Name = "Concatenate String 0";
		String sInsCS0Desc = "Concatenates two strings";
		PropertiesDescription pdPInsCS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCS0 = new ExecutableComponentInstanceDescription(resInsCS0,resInsCS0Component,sInsCS0Name,sInsCS0Desc,pdPInsCS0Properties);

		Resource resInsPO0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/print-object/3");
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
		
		Resource resInsPSD = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/4");
		Resource resInsPSDComponent = ecdPS.getExecutableComponent();
		String sInsPSDName = "Push String Dangling";
		String sInsPSDDesc = "Push hello world";
		PropertiesDescription pdPInsPSDProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPSD = new ExecutableComponentInstanceDescription(resInsPSD,resInsPSDComponent,sInsPSDName,sInsPSDDesc,pdPInsPSDProperties);

		Resource resInsCSD = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/concatenate_string/5");
		Resource resInsCSDComponent = ecdCS.getExecutableComponent();
		String sInsCSDName = "Concatenate String Dangling";
		String sInsCSDDesc = "Concatenates two strings";
		PropertiesDescription pdPInsCSDProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCSD = new ExecutableComponentInstanceDescription(resInsCSD,resInsCSDComponent,sInsCSDName,sInsCSDDesc,pdPInsCSDProperties);

		Resource resInsPOD = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/print-object/6");
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
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/0"),
				ecidPS0.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resOne);

		ConnectorDescription cdPS1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/1"),
				ecidPS1.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resTwo);
		
		ConnectorDescription cdPO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/2"),
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
		return getTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository(sTestBaseURL);
	}
	
	/** Generates a 4 unique component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out; three extra dangling instances
	 * placed to the check proper functionality.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 * @throws InterruptedException The unique ID could not be recovered
	 */
	public static Model getNextTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository () throws InterruptedException {
		long l = -1;
		semUniqueID.acquire();
		l = lUniqueID++;
		semUniqueID.release();
		return getTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository(sTestBaseURL+"/id/"+l+"/");
	}
	
	
	/** Generates a 4 component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out; three extra dangling instances
	 * placed to the check proper functionality.
	 * 
	 * @param sBaseURL The base URL
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository (String sBaseURL) {
		
		///
		// Create the components
		//
		ExecutableComponentDescription ecdPS = getPushStringComponent(sBaseURL); 
		ExecutableComponentDescription ecdCS = getConcatenateStringsComponent(sBaseURL); 
		ExecutableComponentDescription ecdPO = getPrintObjectComponent(sBaseURL); 
		ExecutableComponentDescription ecdFO = getReferenceForkComponent(sBaseURL);
		
		sBaseURL+="/flow/test-hello-world-in-out-fork/";
		
		//
		// Assemble the flow
		//
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource(sBaseURL);
		
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
		Resource resInsPS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/0");
		Resource resInsPS0Component = ecdPS.getExecutableComponent();
		String sInsPS0Name = "Push String 0";
		String sInsPS0Desc = "Push hello world";
		PropertiesDescription pdPInsPS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS0 = new ExecutableComponentInstanceDescription(resInsPS0,resInsPS0Component,sInsPS0Name,sInsPS0Desc,pdPInsPS0Properties);
	
		Resource resInsPS1 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/1");
		Resource resInsPS1Component = ecdPS.getExecutableComponent();
		String sInsPS1Name = "Push String 1";
		String sInsPS1Desc = "Push hello world";
		PropertiesDescription pdPInsPS1Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS1 = new ExecutableComponentInstanceDescription(resInsPS1,resInsPS1Component,sInsPS1Name,sInsPS1Desc,pdPInsPS1Properties);
	
		Resource resInsCS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/concatenate_string/2");
		Resource resInsCS0Component = ecdCS.getExecutableComponent();
		String sInsCS0Name = "Concatenate String 0";
		String sInsCS0Desc = "Concatenates two strings";
		PropertiesDescription pdPInsCS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCS0 = new ExecutableComponentInstanceDescription(resInsCS0,resInsCS0Component,sInsCS0Name,sInsCS0Desc,pdPInsCS0Properties);
	
		Resource resInsRF0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/fork_by_reference/3");
		Resource resInsRF0Component = ecdFO.getExecutableComponent();
		String sInsRF0Name = "Forks an object";
		String sInsRF0Desc = "Fork and object by reference";
		PropertiesDescription pdPInsRF0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidFO0 = new ExecutableComponentInstanceDescription(resInsRF0,resInsRF0Component,sInsRF0Name,sInsRF0Desc,pdPInsRF0Properties);
	
		Resource resInsPO0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/print-object/4");
		Resource resInsPO0Component = ecdPO.getExecutableComponent();
		String sInsPO0Name = "Print Object 0";
		String sInsPO0Desc = "Prints the concatenated object";
		PropertiesDescription pdPInsPO0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPO0 = new ExecutableComponentInstanceDescription(resInsPO0,resInsPO0Component,sInsPO0Name,sInsPO0Desc,pdPInsPO0Properties);
	
		Resource resInsPO1 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/print-object/5");
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
		
		Resource resInsPSD = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/6");
		Resource resInsPSDComponent = ecdPS.getExecutableComponent();
		String sInsPSDName = "Push String Dangling";
		String sInsPSDDesc = "Push hello world";
		PropertiesDescription pdPInsPSDProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPSD = new ExecutableComponentInstanceDescription(resInsPSD,resInsPSDComponent,sInsPSDName,sInsPSDDesc,pdPInsPSDProperties);
	
		Resource resInsCSD = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/concatenate_string/7");
		Resource resInsCSDComponent = ecdCS.getExecutableComponent();
		String sInsCSDName = "Concatenate String Dangling";
		String sInsCSDDesc = "Concatenates two strings";
		PropertiesDescription pdPInsCSDProperties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCSD = new ExecutableComponentInstanceDescription(resInsCSD,resInsCSDComponent,sInsCSDName,sInsCSDDesc,pdPInsCSDProperties);
	
		Resource resInsPOD = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/print-object/8 ");
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
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/0"),
				ecidPS0.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resOne);
	
		ConnectorDescription cdPS1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/1"),
				ecidPS1.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resTwo);
		
		ConnectorDescription cdPO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/2"),
				ecidCS0.getExecutableComponentInstance(), ecdCS.getOutputs().iterator().next().getResource(),
				ecidFO0.getExecutableComponentInstance(), ecdFO.getInputs().iterator().next().getResource());
	
		ConnectorDescription cdFO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/3"),
				ecidFO0.getExecutableComponentInstance(), resFOOne,
				ecidPO0.getExecutableComponentInstance(), ecdPO.getInputs().iterator().next().getResource());
	
		ConnectorDescription cdFO1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/4"),
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

	/** Generates a 5 hetereogenous component flow that pushes a string, concatenates it twice,
	 * turns it uppercase, and it finally prints it to the standard out.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldHetereogenousRepository() {
		return getTestHelloWorldHetereogenousRepository(sTestBaseURL);
	}
	
	/** Generates a 6 hetereogenous component flow that pushes a string, concatenates it twice,
	 * turns it uppercase, and it finally prints it to the standard out.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldMoreHetereogenousRepository() {
		return getTestHelloWorldMoreHetereogenousRepository(sTestBaseURL);
	}

	/**  Generates a 5 hetereogenous component flow that pushes a string, concatenates it twice,
	 * turns it uppercase, and it finally prints it to the standard out.
	 * 
	 * @param sBaseURL The base URL
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldHetereogenousRepository(String sBaseURL) {
		
		///
		// Create the components
		//
		ExecutableComponentDescription ecdPS = getPushStringComponent(sBaseURL); 
		ExecutableComponentDescription ecdCS = getConcatenateStringsComponent(sBaseURL); 
		ExecutableComponentDescription ecdPO = getPrintObjectComponent(sBaseURL); 
		ExecutableComponentDescription ecdUP = getToUpperComponent(sBaseURL);

		ExecutableComponentDescription ecdFR = getForkComponent(sBaseURL);
		
		sBaseURL += "/flow/test-hello-world-with-python/";
		
		//
		// Assemble the flow
		//
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource(sBaseURL);
		
		//
		// Flow properties
		//
		String sName = "Hello World With Java and Python Components!!!";
		String sDescription = "A simple hello world test";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		//
		// Create the instances
		//
		Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances = new HashSet<ExecutableComponentInstanceDescription>();

		Resource resInsPS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/0");
		Resource resInsPS0Component = ecdPS.getExecutableComponent();
		String sInsPS0Name = "Push String 0";
		String sInsPS0Desc = "Push hello world";
		PropertiesDescription pdPInsPS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS0 = new ExecutableComponentInstanceDescription(resInsPS0,resInsPS0Component,sInsPS0Name,sInsPS0Desc,pdPInsPS0Properties);

		Resource resInsPS1 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/1");
		Resource resInsPS1Component = ecdPS.getExecutableComponent();
		String sInsPS1Name = "Push String 1";
		String sInsPS1Desc = "Push hello world";
		PropertiesDescription pdPInsPS1Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS1 = new ExecutableComponentInstanceDescription(resInsPS1,resInsPS1Component,sInsPS1Name,sInsPS1Desc,pdPInsPS1Properties);

		Resource resInsCS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/concatenate_string/2");
		Resource resInsCS0Component = ecdCS.getExecutableComponent();
		String sInsCS0Name = "Concatenate String 0";
		String sInsCS0Desc = "Concatenates two strings";
		PropertiesDescription pdPInsCS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCS0 = new ExecutableComponentInstanceDescription(resInsCS0,resInsCS0Component,sInsCS0Name,sInsCS0Desc,pdPInsCS0Properties);

		Resource resInsUP0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/to-uppercase/3");
		Resource resInsUP0Component = ecdUP.getExecutableComponent();
		String sInsUP0Name = "To Uppercasde 0";
		String sInsUP0Desc = "Converts a strings to uppercase";
		PropertiesDescription pdPInsUP0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidUP0 = new ExecutableComponentInstanceDescription(resInsUP0,resInsUP0Component,sInsUP0Name,sInsUP0Desc,pdPInsUP0Properties);

		Resource resInsPO0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/print-object/4");
		Resource resInsPO0Component = ecdPO.getExecutableComponent();
		String sInsPO0Name = "Print Object 0";
		String sInsPO0Desc = "Prints the concatenated object";
		PropertiesDescription pdPInsPO0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPO0 = new ExecutableComponentInstanceDescription(resInsPO0,resInsPO0Component,sInsPO0Name,sInsPO0Desc,pdPInsPO0Properties);

		setExecutableComponentInstances.add(ecidPS0);
		setExecutableComponentInstances.add(ecidPS1);
		setExecutableComponentInstances.add(ecidCS0);
		setExecutableComponentInstances.add(ecidUP0);
		setExecutableComponentInstances.add(ecidPO0);
		
		//
		// Connecting the instances
		//
		Set<ConnectorDescription> setConnectorDescription = new HashSet<ConnectorDescription>();
		
		Iterator<DataPortDescription> iter = ecdCS.getInputs().iterator();
		Resource resOne = iter.next().getResource();
		Resource resTwo = iter.next().getResource();
		
		ConnectorDescription cdPS0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/0"),
				ecidPS0.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resOne);

		ConnectorDescription cdPS1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/1"),
				ecidPS1.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resTwo);
		
		ConnectorDescription cdUP0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/2"),
				ecidCS0.getExecutableComponentInstance(), ecdCS.getOutputs().iterator().next().getResource(),
				ecidUP0.getExecutableComponentInstance(), ecdUP.getInputs().iterator().next().getResource());

		ConnectorDescription cdPO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/3"),
				ecidUP0.getExecutableComponentInstance(), ecdUP.getOutputs().iterator().next().getResource(),
				ecidPO0.getExecutableComponentInstance(), ecdPO.getInputs().iterator().next().getResource());

		setConnectorDescription.add(cdPS0);
		setConnectorDescription.add(cdPS1);
		setConnectorDescription.add(cdUP0);
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
                               .add(ecdUP.getModel())
                               .add(ecdFR.getModel())
                               .add(fd.getModel());
	}


	/**  Generates a 5 hetereogenous component flow that pushes a string, concatenates it twice,
	 * turns it uppercase, and it finally prints it to the standard out.
	 * 
	 * @param sBaseURL The base URL
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldMoreHetereogenousRepository(String sBaseURL) {
		
		///
		// Create the components
		//
		ExecutableComponentDescription ecdPS = getPushStringComponent(sBaseURL); 
		ExecutableComponentDescription ecdCS = getConcatenateStringsComponent(sBaseURL); 
		ExecutableComponentDescription ecdPO = getPrintObjectComponent(sBaseURL); 
		ExecutableComponentDescription ecdUP = getToUpperComponent(sBaseURL);
		ExecutableComponentDescription ecdPT = getPassThroughComponent(sBaseURL);

		ExecutableComponentDescription ecdFR = getForkComponent(sBaseURL);
		
		sBaseURL += "/flow/test-hello-world-with-python-and-lisp/";
		
		//
		// Assemble the flow
		//
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource(sBaseURL);
		
		//
		// Flow properties
		//
		String sName = "Hello World With Java, Python, and Lisp Components!!!";
		String sDescription = "A simple hello world test";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		
		//
		// Create the instances
		//
		Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances = new HashSet<ExecutableComponentInstanceDescription>();

		Resource resInsPS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/0");
		Resource resInsPS0Component = ecdPS.getExecutableComponent();
		String sInsPS0Name = "Push String 0";
		String sInsPS0Desc = "Push hello world";
		PropertiesDescription pdPInsPS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS0 = new ExecutableComponentInstanceDescription(resInsPS0,resInsPS0Component,sInsPS0Name,sInsPS0Desc,pdPInsPS0Properties);

		Resource resInsPS1 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/1");
		Resource resInsPS1Component = ecdPS.getExecutableComponent();
		String sInsPS1Name = "Push String 1";
		String sInsPS1Desc = "Push hello world";
		PropertiesDescription pdPInsPS1Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS1 = new ExecutableComponentInstanceDescription(resInsPS1,resInsPS1Component,sInsPS1Name,sInsPS1Desc,pdPInsPS1Properties);

		Resource resInsCS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/concatenate_string/2");
		Resource resInsCS0Component = ecdCS.getExecutableComponent();
		String sInsCS0Name = "Concatenate String 0";
		String sInsCS0Desc = "Concatenates two strings";
		PropertiesDescription pdPInsCS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCS0 = new ExecutableComponentInstanceDescription(resInsCS0,resInsCS0Component,sInsCS0Name,sInsCS0Desc,pdPInsCS0Properties);

		Resource resInsUP0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/to-uppercase/3");
		Resource resInsUP0Component = ecdUP.getExecutableComponent();
		String sInsUP0Name = "To Uppercasde 0";
		String sInsUP0Desc = "Converts a strings to uppercase";
		PropertiesDescription pdPInsUP0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidUP0 = new ExecutableComponentInstanceDescription(resInsUP0,resInsUP0Component,sInsUP0Name,sInsUP0Desc,pdPInsUP0Properties);

		Resource resInsPT0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/pass-through/4");
		Resource resInsPT0Component = ecdPT.getExecutableComponent();
		String sInsPT0Name = "Pass Through 0";
		String sInsPT0Desc = "Prints the concatenated object";
		PropertiesDescription pdPInsPT0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPT0 = new ExecutableComponentInstanceDescription(resInsPT0,resInsPT0Component,sInsPT0Name,sInsPT0Desc,pdPInsPT0Properties);

		Resource resInsPO0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/print-object/5");
		Resource resInsPO0Component = ecdPO.getExecutableComponent();
		String sInsPO0Name = "Print Object 0";
		String sInsPO0Desc = "Prints the concatenated object";
		PropertiesDescription pdPInsPO0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPO0 = new ExecutableComponentInstanceDescription(resInsPO0,resInsPO0Component,sInsPO0Name,sInsPO0Desc,pdPInsPO0Properties);

		setExecutableComponentInstances.add(ecidPS0);
		setExecutableComponentInstances.add(ecidPS1);
		setExecutableComponentInstances.add(ecidCS0);
		setExecutableComponentInstances.add(ecidUP0);
		setExecutableComponentInstances.add(ecidPO0);
		setExecutableComponentInstances.add(ecidPT0);
		
		//
		// Connecting the instances
		//
		Set<ConnectorDescription> setConnectorDescription = new HashSet<ConnectorDescription>();
		
		Iterator<DataPortDescription> iter = ecdCS.getInputs().iterator();
		Resource resOne = iter.next().getResource();
		Resource resTwo = iter.next().getResource();
		
		ConnectorDescription cdPS0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/0"),
				ecidPS0.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resOne);

		ConnectorDescription cdPS1 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/1"),
				ecidPS1.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resTwo);
		
		ConnectorDescription cdUP0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/2"),
				ecidCS0.getExecutableComponentInstance(), ecdCS.getOutputs().iterator().next().getResource(),
				ecidUP0.getExecutableComponentInstance(), ecdUP.getInputs().iterator().next().getResource());

		ConnectorDescription cdUPT = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/3"),
				ecidUP0.getExecutableComponentInstance(), ecdUP.getOutputs().iterator().next().getResource(),
				ecidPT0.getExecutableComponentInstance(), ecdPT.getInputs().iterator().next().getResource());

		ConnectorDescription cdPO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/4"),
				ecidPT0.getExecutableComponentInstance(), ecdPT.getOutputs().iterator().next().getResource(),
				ecidPO0.getExecutableComponentInstance(), ecdPO.getInputs().iterator().next().getResource());

		setConnectorDescription.add(cdPS0);
		setConnectorDescription.add(cdPS1);
		setConnectorDescription.add(cdUP0);
		setConnectorDescription.add(cdUPT);
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
                               .add(ecdUP.getModel())
                               .add(ecdPT.getModel())
                               .add(ecdFR.getModel())
                               .add(fd.getModel());
	}


	/** Generates a 4 component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldPartialDanglingRepository() {
		return getTestHelloWorldPartialDanglingRepository(sTestBaseURL);
	}
	
	/** Generates a unique 4 component flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out.
	 * 
	 * @return The model with the 3 component descriptors and the flow
	 * @throws InterruptedException Failed to get the new unique ID
	 */
	public static Model getNextTestHelloWorldPartialDanglingRepository() throws InterruptedException {
		long l = -1;
		semUniqueID.acquire();
		l = lUniqueID++;
		semUniqueID.release();
		return getTestHelloWorldPartialDanglingRepository(sTestBaseURL+"/id/"+l+"/");
	}
	
	/** Generates a 3 component partial flow that pushes a string, concatenates it twice
	 * and it finally prints it to the standard out. The flow is partially defined, and 
	 * should hang if tried to run.
	 * 
	 * @param sBaseURL The base URL
	 * @return The model with the 3 component descriptors and the flow
	 */
	public static Model getTestHelloWorldPartialDanglingRepository(String sBaseURL) {
		
		///
		// Create the components
		//
		ExecutableComponentDescription ecdPS = getPushStringComponent(sBaseURL); 
		ExecutableComponentDescription ecdCS = getConcatenateStringsComponent(sBaseURL); 
		ExecutableComponentDescription ecdPO = getPrintObjectComponent(sBaseURL); 
		
		sBaseURL += "/flow/test-hello-world";
		
		//
		// Assemble the flow
		//
		Resource resFlowComponent = ModelFactory.createDefaultModel().createResource(sBaseURL);
		
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

		Resource resInsPS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/push_string/0");
		Resource resInsPS0Component = ecdPS.getExecutableComponent();
		String sInsPS0Name = "Push String 0";
		String sInsPS0Desc = "Push hello world";
		PropertiesDescription pdPInsPS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPS0 = new ExecutableComponentInstanceDescription(resInsPS0,resInsPS0Component,sInsPS0Name,sInsPS0Desc,pdPInsPS0Properties);

		Resource resInsCS0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/concatenate_string/2");
		Resource resInsCS0Component = ecdCS.getExecutableComponent();
		String sInsCS0Name = "Concatenate String 0";
		String sInsCS0Desc = "Concatenates two strings";
		PropertiesDescription pdPInsCS0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidCS0 = new ExecutableComponentInstanceDescription(resInsCS0,resInsCS0Component,sInsCS0Name,sInsCS0Desc,pdPInsCS0Properties);

		Resource resInsPO0 = ModelFactory.createDefaultModel().createResource(sBaseURL+"instance/print-object/3");
		Resource resInsPO0Component = ecdPO.getExecutableComponent();
		String sInsPO0Name = "Print Object 0";
		String sInsPO0Desc = "Prints the concatenated object";
		PropertiesDescription pdPInsPO0Properties = new PropertiesDescription();
		ExecutableComponentInstanceDescription ecidPO0 = new ExecutableComponentInstanceDescription(resInsPO0,resInsPO0Component,sInsPO0Name,sInsPO0Desc,pdPInsPO0Properties);

		setExecutableComponentInstances.add(ecidPS0);
		setExecutableComponentInstances.add(ecidCS0);
		setExecutableComponentInstances.add(ecidPO0);
		
		//
		// Connecting the instances
		//
		Set<ConnectorDescription> setConnectorDescription = new HashSet<ConnectorDescription>();
		
		Iterator<DataPortDescription> iter = ecdCS.getInputs().iterator();
		Resource resOne = iter.next().getResource();
		//Resource resTwo = iter.next().getResource();
		
		ConnectorDescription cdPS0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/0"),
				ecidPS0.getExecutableComponentInstance(), ecdPS.getOutputs().iterator().next().getResource(),
				ecidCS0.getExecutableComponentInstance(), resOne);

		ConnectorDescription cdPO0 = new ConnectorDescription(
				ModelFactory.createDefaultModel().createResource(sBaseURL+"connector/2"),
				ecidCS0.getExecutableComponentInstance(), ecdCS.getOutputs().iterator().next().getResource(),
				ecidPO0.getExecutableComponentInstance(), ecdPO.getInputs().iterator().next().getResource());

		setConnectorDescription.add(cdPS0);
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

	
}
