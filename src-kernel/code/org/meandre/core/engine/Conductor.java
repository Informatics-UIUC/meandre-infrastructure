package org.meandre.core.engine;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.engine.policies.component.availability.WrappedComponentAllInputsRequired;
import org.meandre.core.engine.policies.component.availability.WrappedComponentAnyInputRequired;
import org.meandre.core.engine.probes.NullProbeImpl;
import org.meandre.core.environments.lisp.clojure.ClojureExecutableComponentAdapter;
import org.meandre.core.environments.python.jython.JythonExecutableComponentAdapter;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.repository.*;
import org.meandre.core.utils.HexConverter;
import org.meandre.core.utils.NetworkTools;
import org.meandre.webui.PortScroller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

/** This class is devoted to the arrengement required to execute a given
 * flow description. The basic input the conductor is in charge of generate
 * the proper structures and connections to create the Executor object that
 * which will eventually execute the flow.
 *
 * @author Xavier Llor&agrave;
 */
public class Conductor {

	/** List of working implementations for executable components. */
	static private final String [] saExecutableComponentLanguages = { "java", "python", "lisp" };

	/** Set of loadable implementations */
	private Set<String> setLoadableLanguages = null;

	/** List of working implementations for executable components. */
	static private final String [] saExecutableComponentTypes = { "java/class", "jython", "clojure" };

	/** A simple separator character for URLs */
	static private final String URL_SEAPARTOR = "##--##--##";

	/** The default queue size for the active buffers */
	public static final int DEFAULT_QUEUE_SIZE = 10000;

	/** Set of loadable implementations */
	private Set<String> setLoadableComponents = null;

	/** The default active buffer size */
	private int iActiveBufferSize;

	/** The core root logger */
	protected static Logger log = KernelLoggerFactory.getCoreLogger();

	/** Embeded context to file URI mapper */
	private static Hashtable<Literal,String> htMapLiteralToFile = new Hashtable<Literal,String>();

	/** Embeded context to hash code mapper */
	private static Hashtable<Literal,Integer> htMapLiteralToHashcode = new Hashtable<Literal,Integer>();

	/** The core configuration object */
	private CoreConfiguration cnf;
	
	private ClassLoader parentClassloader;
	
	/** Initialize a conductor with a set of valid URLs.
	 *
	 */
	public Conductor( int iActiveBufferSize, CoreConfiguration cnf ) {
		this.cnf = cnf;
		
		log.fine("Creating a conductor object");
		this.iActiveBufferSize = iActiveBufferSize;
		setLoadableLanguages = new HashSet<String>();
		for ( String sLang:saExecutableComponentLanguages )
			setLoadableLanguages.add(sLang.toLowerCase());
		setLoadableComponents = new HashSet<String>();
		for ( String sType:saExecutableComponentTypes )
			setLoadableComponents.add(sType.toLowerCase());
	}


	/** Creates an execution object for the given flow description.
	 *
	 * @param qr The queryable repository containing component descriptions
	 * @param res The resource identifying the flow to prepare for execution
	 * @param console The output console
	 * @return The executor object
	 * @throws CorruptedDescriptionException Inconsistencies where found on the flow definition aborting the creation of the Executor
	 * @throws ConductorException The counductor could not create an executable flow
	 */
	public Executor buildExecutor(QueryableRepository qr, Resource res, PrintStream console )
	throws CorruptedDescriptionException, ConductorException {
		String sFlowUniqueExecutionID = res.toString()+NetworkTools.getNumericIPValue()+"/"+System.currentTimeMillis()+"/"+(Math.abs(new Random().nextInt()))+"/";
		return buildExecutor(qr,res,console,sFlowUniqueExecutionID);
	}

	/** Creates an execution object for the given flow description.
	 *
	 * @param qr The queryable repository containing component descriptions
	 * @param res The resource identifying the flow to prepare for execution
	 * @param console The output console
	 * @param sFUID The flow execution ID
	 * @return The executor object
	 * @throws CorruptedDescriptionException Inconsistencies where found on the flow definition aborting the creation of the Executor
	 * @throws ConductorException The counductor could not create an executable flow
	 */
	public Executor buildExecutor(QueryableRepository qr, Resource res, PrintStream console, String sFUID )
	throws CorruptedDescriptionException, ConductorException {
		MrProbe thdMrProbe = new MrProbe(log,new NullProbeImpl(),false,false);
		Executor exec = buildExecutor(qr, res,thdMrProbe,console,sFUID);
		thdMrProbe.setName(exec.getThreadGroupName()+"mr-probe");
		exec.initWebUI(PortScroller.getInstance(cnf).nextAvailablePort(exec.getFlowUniqueExecutionID()), "XStreamTest");
		return exec;
	}
	
	/** Creates an execution object for the given flow description.
	 *
	 * @param qr The queryable repository containing component descriptions
	 * @param res The resource identifying the flow to prepare for execution
	 * @param thdMrProbe The MrProbe to use
	 * @param console The output console
	 * @return The executor object
	 * @throws CorruptedDescriptionException Inconsistencies where found on the flow definition aborting the creation of the Executor
	 * @throws ConductorException The conductor could not create an executable flow
	 */
	public Executor buildExecutor(QueryableRepository qr, Resource res, MrProbe thdMrProbe, PrintStream console)
	throws CorruptedDescriptionException, ConductorException {
		String sFlowUniqueExecutionID = res.toString()+NetworkTools.getNumericIPValue()+"/"+System.currentTimeMillis()+"/"+(Math.abs(new Random().nextInt()))+"/";
		return buildExecutor(qr,res,thdMrProbe,console,sFlowUniqueExecutionID);
	}
	/** Creates an execution object for the given flow description.
	 *
	 * @param qr The queryable repository containing component descriptions
	 * @param res The resource identifying the flow to prepare for execution
	 * @param thdMrProbe The MrProbe to use
	 * @param console The output console
	 * @param sFUID The flow unique execution ID
	 * @return The executor object
	 * @throws CorruptedDescriptionException Inconsistencies where found on the flow definition aborting the creation of the Executor
	 * @throws ConductorException The conductor could not create an executable flow
	 */
	@SuppressWarnings("unchecked")
	public Executor buildExecutor(QueryableRepository qr, Resource res, MrProbe thdMrProbe, PrintStream console,String sFUID)
	throws CorruptedDescriptionException, ConductorException {
		// The unique execution flow ID
		String sFlowUniqueExecutionID = sFUID;
		String flowID = res.toString();
		// Map class names to classes
		Hashtable<String,Class> htMapNameToClass = new Hashtable<String,Class>();
		// Map reource names to classes
		Hashtable<Resource,String> htMapResourceToName = new Hashtable<Resource,String>();

		// Start MrProbe
		thdMrProbe.start();
		
		// Get the description
		FlowDescription fd = qr.getFlowDescription(res);

		log.info("Preparing flow "+sFlowUniqueExecutionID);
		
		// STEP 0: Gather all the required contexts and load them
		log.fine("STEP 0: Plugging the required contexts");
		URLClassLoader urlFlowCL = plugRequiredContexts(qr, htMapNameToClass, htMapResourceToName,fd);

		// STEP 1: Instantiate the flow instances
		log.fine("STEP 1: Instantiate the flow instances");
		Hashtable<Resource,ExecutableComponent> htECInstances = new Hashtable<Resource,ExecutableComponent>();
		for ( ExecutableComponentInstanceDescription ec:fd.getExecutableComponentInstances() ) {
			Resource   ins = ec.getExecutableComponentInstance();
			Resource ecomp = ec.getExecutableComponent();
			ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(ecomp);
			if ( ecd.getRunnable().equals("java") && ecd.getFormat().equals("java/class")) {
				// Creates a java instance of the component implementation
				try {
//					System.out.println(ecomp);
//					System.out.println(htMapResourceToName.keySet());
//					System.out.println(htMapResourceToName.get(ecomp));
//					System.out.println(htMapNameToClass.keySet());
					htECInstances.put(ins,(ExecutableComponent) htMapNameToClass.get(htMapResourceToName.get(ecomp)).newInstance());
				} catch (InstantiationException e) {
					thdMrProbe.done();
					throw new ConductorException("Condcuctor could not instantiate class "+htMapResourceToName.get(ecomp)+"\n"+e);
				} catch (IllegalAccessException e) {
					thdMrProbe.done();
					throw new ConductorException("Condcuctor could not instantiate class "+htMapResourceToName.get(ecomp)+"\n"+e);
				}
			}
			else if ( ecd.getRunnable().equals("python") && ecd.getFormat().equals("jython") ){
				// Creates a jython wrapper and sources all the scripts
				Set<RDFNode> setScripts = ecd.getContext();
				JythonExecutableComponentAdapter jeca;
				try {
					jeca = (JythonExecutableComponentAdapter) urlFlowCL.loadClass(JythonExecutableComponentAdapter.class.getName()).newInstance();
				} catch (InstantiationException e) {
					thdMrProbe.done();
					throw new ConductorException(e);
				} catch (IllegalAccessException e) {
					thdMrProbe.done();
					throw new ConductorException(e);
				} catch (ClassNotFoundException e) {
					thdMrProbe.done();
					throw new ConductorException(e);
				}
				for ( RDFNode rdfn:setScripts )
					if ( rdfn.isLiteral() )
						jeca.process(((Literal)rdfn).getLexicalForm());
				htECInstances.put(ins,jeca);
			}
			else if ( ecd.getRunnable().equals("lisp") && ecd.getFormat().equals("clojure") ){
				// Creates a jython wrapper and sources all the scripts
				Set<RDFNode> setScripts = ecd.getContext();
				ClojureExecutableComponentAdapter ceca;
				try {
					ceca = (ClojureExecutableComponentAdapter) urlFlowCL.loadClass(ClojureExecutableComponentAdapter.class.getName()).newInstance();
				} catch (InstantiationException e) {
					thdMrProbe.done();
					throw new ConductorException(e);
				} catch (IllegalAccessException e) {
					thdMrProbe.done();
					throw new ConductorException(e);
				} catch (ClassNotFoundException e) {
					thdMrProbe.done();
					throw new ConductorException(e);
				}
				for ( RDFNode rdfn:setScripts )
					if ( rdfn.isLiteral() )
						try {
							ceca.process(((Literal)rdfn).getLexicalForm());
						} catch (Exception e) {
							throw new ConductorException(e);
						}
				htECInstances.put(ins,ceca);
			}
		}

		// STEP 2: Create empty active buffers
		log.fine("STEP 2: Create empty active buffers");
		Hashtable<String,ActiveBuffer> htMapInAB = new Hashtable<String,ActiveBuffer>();
		for ( ConnectorDescription cd:fd.getConnectorDescriptions() ) {
			Resource resCSIns = cd.getTargetInstance();
			Resource resCSInsDP = cd.getTargetInstanceDataPort();
			//Resource resECID = fd.getExecutableComponentResourceForInstance(resCSIns);
			//ExecutableComponentDescription compDesc = qr.getExecutableComponentDescription(resECID);
			String sID = resCSIns.toString()+URL_SEAPARTOR+resCSInsDP.toString();
			if ( htMapInAB.get(sID)==null ) {
				//htMapInAB.put(sID,new ActiveBuffer(compDesc.getInput(resCSInsDP).getIdentifier(),iActiveBufferSize));
				htMapInAB.put(sID,new ActiveBuffer(sID,iActiveBufferSize));
			}
		}

		// STEP 3: Per instance create and input/output set
		log.fine("STEP 3: Per instance create and input/output set");
		Hashtable<String,Set<ActiveBuffer>> htInsInputSet = new Hashtable<String,Set<ActiveBuffer>>();
		Hashtable<String,Set<ActiveBuffer>> htInsOutputSet = new Hashtable<String,Set<ActiveBuffer>>();
		for ( ExecutableComponentInstanceDescription ecid:fd.getExecutableComponentInstances() ) {
			String sID = ecid.getExecutableComponentInstance().toString();
			htInsInputSet.put(sID, new HashSet<ActiveBuffer>());
			htInsOutputSet.put(sID, new HashSet<ActiveBuffer>());
		}

		// STEP 4: Populate input/output instance sets
		log.fine("STEP 4: Populate input/output instance sets");
		// The output <--> input port translation
		Hashtable<Resource,Hashtable<String,String>> htMapOutputTranslation = new Hashtable<Resource,Hashtable<String,String>>();
		// Logic to real input port translation
		Hashtable<Resource,Hashtable<String,String>> htMapInputLogicNameTranslation = new Hashtable<Resource,Hashtable<String,String>>();
		// Logic to real output port translation
		Hashtable<Resource,Hashtable<String,String>> htMapOutputLogicNameTranslation = new Hashtable<Resource,Hashtable<String,String>>();
		for ( ExecutableComponentInstanceDescription ecid:fd.getExecutableComponentInstances() ) {
			ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(ecid.getExecutableComponent());

			Hashtable<String, String> htMapInLog = new Hashtable<String,String>();
			htMapInputLogicNameTranslation.put(ecid.getExecutableComponentInstance(),htMapInLog);
			for ( DataPortDescription dpd:ecd.getInputs() ) {
				String  sIDIn = ecid.getExecutableComponentInstance()+URL_SEAPARTOR+dpd.getResource();
				htMapInLog.put(dpd.getName(), sIDIn);
			}

			Hashtable<String, String> htMapOutLog = new Hashtable<String,String>();
			htMapOutputLogicNameTranslation.put(ecid.getExecutableComponentInstance(),htMapOutLog);
			for ( DataPortDescription dpd:ecd.getOutputs() ) {
				String  sIDOut = ecid.getExecutableComponentInstance()+URL_SEAPARTOR+dpd.getResource();
				htMapOutLog.put(dpd.getName(), sIDOut);
			}

			Hashtable<String, String> htMapOut = new Hashtable<String,String>();
			htMapOutputTranslation.put(ecid.getExecutableComponentInstance(),htMapOut);

		}
		for ( ConnectorDescription cd:fd.getConnectorDescriptions() ) {
			String sIDOutSet = cd.getSourceInstance().toString();
			String  sIDInSet = cd.getTargetInstance().toString();
			String  sIDIn = cd.getTargetInstance().toString()+URL_SEAPARTOR+cd.getTargetInstanceDataPort().toString();

			ActiveBuffer ab = htMapInAB.get(sIDIn);

			// Update the output mapping name for a given instance

			ExecutableComponentDescription compDesc = qr.getExecutableComponentDescription(fd.getExecutableComponentResourceForInstance(cd.getSourceInstance()));
			DataPortDescription dpdOut = compDesc.getOutput(cd.getSourceInstanceDataPort());
			Hashtable<String,String> htMapOut = htMapOutputTranslation.get(cd.getSourceInstance());
			htMapOut.put(sIDOutSet+URL_SEAPARTOR+dpdOut.getIdentifier(), ab.getName());

			// Update the input and output sets
			htInsOutputSet.get(sIDOutSet).add(ab);
			htInsInputSet.get(sIDInSet).add(ab);
		}

		// STEP 5: Reconstruct properties per instance set
		log.fine("STEP 5: Reconstruct properties per instance set");
		Hashtable<Resource,Hashtable<String,String>> htInstaceProperties =  new Hashtable<Resource,Hashtable<String,String>>();
		for ( ExecutableComponentInstanceDescription ecid:fd.getExecutableComponentInstances() ) {
			PropertiesDescriptionDefinition  ecdProp = qr.getExecutableComponentDescription(ecid.getExecutableComponent()).getProperties();
			PropertiesDescription           ecidProp = ecid.getProperties();
			Hashtable<String,String> htProp = new Hashtable<String,String>();
			// Initialize the default properties of the component
			for ( String sKey:ecdProp.getKeys() )
				htProp.put(sKey,ecdProp.getValue(sKey));
			// Overwright particular properties by the instance preferences
			for ( String sKey:ecidProp.getKeys() )
				htProp.put(sKey,ecidProp.getValue(sKey));
			htInstaceProperties.put(ecid.getExecutableComponentInstance(),htProp);
		}

		// STEP 6: Create thread groups
		log.fine("STEP 6: Create thread groups");
		ThreadGroup tgFlow = new ThreadGroup(sFlowUniqueExecutionID);

		// STEP 7: Create wrapping components and add the wrapping components to the execution set
		log.fine("STEP 7: Create wrapping components and add the wrapping components to the execution set");
		Set<WrappedComponent> setWC = new HashSet<WrappedComponent>();
		for ( ExecutableComponentInstanceDescription ecid:fd.getExecutableComponentInstances() ) {
			Resource resECI = ecid.getExecutableComponentInstance();
			String sResECI = resECI.toString();
			String firing = qr.getExecutableComponentDescription(ecid.getExecutableComponent()).getFiringPolicy().toLowerCase();
			try {
				if ( firing.equals("all") )
					setWC.add(
							new WrappedComponentAllInputsRequired (
									sFlowUniqueExecutionID,
									flowID,
									ecid.getExecutableComponentInstance().toString(),
									(ExecutableComponent) htECInstances.get(resECI),
									htInsInputSet.get(sResECI),
									htInsOutputSet.get(sResECI),
									htMapOutputTranslation.get(resECI),
									htMapInputLogicNameTranslation.get(resECI),
									htMapOutputLogicNameTranslation.get(resECI),
									tgFlow,
									sResECI,
									htInstaceProperties.get(resECI),
									thdMrProbe,
									cnf,
									console
								)
						);
				else if ( firing.equals("any") )
					setWC.add(
							new WrappedComponentAnyInputRequired (
									sFlowUniqueExecutionID,
									flowID,
									ecid.getExecutableComponentInstance().toString(),
									(ExecutableComponent) htECInstances.get(resECI),
									htInsInputSet.get(sResECI),
									htInsOutputSet.get(sResECI),
									htMapOutputTranslation.get(resECI),
									htMapInputLogicNameTranslation.get(resECI),
									htMapOutputLogicNameTranslation.get(resECI),
									tgFlow,
									sResECI,
									htInstaceProperties.get(resECI),
									thdMrProbe,
									cnf,
									console
								)
						);
			} catch (InterruptedException e) {
				thdMrProbe.done();
				throw new ConductorException("Condcuctor could not instantiate the wapping component for instance"+sResECI+"\n"+e);
			}

		}

		// STEP 8: reate the executor
		log.fine("STEP 8: Create the executor");
		try {
			Executor exec = new Executor(sFlowUniqueExecutionID,tgFlow,setWC, null);
			log.info("Flow prepared. "+sFlowUniqueExecutionID+" waiting for execution");
			return exec;
		} catch (InterruptedException e) {
			thdMrProbe.done();
			throw new ConductorException("Condcuctor could not create the executor object\n"+e);
		}
	}

	/** Loads and plugs the required contexts to the flow being prepared.
	 *
	 * @param qr The queriable repository
	 * @param htMapNameToClass The map from the name to the class
	 * @param htMapResourceToName The map from the resource to a name
	 * @param fd The flow descriptor
	 * @return The flow classloader
	 * @throws CorruptedDescriptionException The descriptor is corrupted
	 * @throws ConductorException The literal containing the resource could not be dumped to a file
	 */
	@SuppressWarnings("unchecked")
	protected URLClassLoader plugRequiredContexts(QueryableRepository qr,
			Hashtable<String, Class> htMapNameToClass, Hashtable<Resource, String> htMapResourceToName,
			FlowDescription fd) throws CorruptedDescriptionException, ConductorException {

		// Set of loadable context URLs
		Set<String> setContexts = new HashSet<String>();
		// Set of loadable locations
		Set<String> setURLLocations = new HashSet<String>();
		// Map reource names to classes
		Hashtable<String,Resource> htMapNameToResource = new Hashtable<String,Resource>();
		// Map resource names to runnable
		Hashtable<Resource,String> htMapResourceToRunnable = new Hashtable<Resource,String>();
		// Map resource names to runnable
		Hashtable<Resource,String> htMapResourceToFormat = new Hashtable<Resource,String>();


		// Preparing the URLs for the class loader
		log.fine("Processing executable components contexts and locations");
		Set<ExecutableComponentInstanceDescription> setIns = fd.getExecutableComponentInstances();
		for ( ExecutableComponentInstanceDescription ins:setIns ) {
			ExecutableComponentDescription comp = qr.getExecutableComponentDescription(ins.getExecutableComponent());
            // If the resource does not exist throw an exeception
			if (comp == null)
				throw new CorruptedDescriptionException("The required executable component " + ins.getExecutableComponent() + " does not exist in the current repository");

			// Check if it is loadable
			if ( setLoadableLanguages.contains(comp.getRunnable().toLowerCase()) )
				htMapResourceToRunnable.put(comp.getExecutableComponent(),comp.getRunnable().toLowerCase());
			else
				throw new CorruptedDescriptionException("Cannot process executable components runnable as "+comp.getRunnable());

			// Check if it is loadable
			if ( setLoadableComponents.contains(comp.getFormat().toLowerCase()) )
				htMapResourceToFormat.put(comp.getExecutableComponent(), comp.getFormat().toLowerCase());
			else
				throw new CorruptedDescriptionException("Component of type "+comp.getFormat()+" could not be loaded by the conductor");

			// Retrieve the contextes
			for ( RDFNode rdfnodeContext:comp.getContext() )
				if ( rdfnodeContext.isResource() )
					// Just a usual URI pointing to the resource
					setContexts.add(((Resource)rdfnodeContext).getURI().trim());
				else
					if ( comp.getRunnable().equals("java") && comp.getFormat().equals("java/class"))
						// The literal is dumped to the file system and a URI generated
						setContexts.add(prepareJarLiteralToTheFileSystem((Literal)rdfnodeContext));

			// Retrieve the location
			boolean bChomped = false;
			String sURI = comp.getLocation().getURI().trim();
			String sURIHead = null;
			for ( String sCntx:setContexts )
				if ( sURI.startsWith(sCntx) ) {
					bChomped = true;
					sURIHead = sURI.substring(0,sCntx.length());
					sURI = sURI.substring(sCntx.length());
				}

			// Check if location does not match any context
			if ( bChomped ) {
				setURLLocations.add(sURI);
				setContexts.remove(sURIHead);
				htMapResourceToName.put(comp.getExecutableComponent(), sURI);
				htMapNameToResource.put(sURI, comp.getExecutableComponent());
			}
			else
				throw new CorruptedDescriptionException("Location "+sURI+" does not match any of the executable component contexts");
		}

		// Load the component classes
		return loadExecutableComponentImplementation(setContexts, setURLLocations, htMapNameToClass, htMapNameToResource, htMapResourceToRunnable, htMapResourceToFormat);
	}
	
	

	/** This methods dump the Literal to the file system and creates a URI for the class loader.
	 *
	 * @param lit The literal to dump
	 * @return The URI to the loader resource
	 * @throws ConductorException The literal could not be dump to a file
	 */
	private String prepareJarLiteralToTheFileSystem(Literal lit) throws ConductorException {
		String sURI = htMapLiteralToFile.get(lit);

		if ( sURI==null || htMapLiteralToHashcode.get(lit)!=lit.hashCode() ) {
			// The Literal has not been mapped to a file before
			byte [] ba = (byte[]) lit.getValue();
			String sURIPath = new File(".").toURI().toString();
			String sFilePath = cnf.getRunResourcesDirectory()+File.separator+"java"+File.separator;
			new File(sFilePath).mkdirs();
			sFilePath += HexConverter.stringToHex("lit-hash-"+lit.hashCode())+".jar";
			sURIPath += sFilePath;
			sURIPath = sURIPath.replaceAll("(\\./)+", "/").replaceAll("/+", "/");
			try {
				FileOutputStream fos = new FileOutputStream(new File(sFilePath));
				fos.write(ba);
				fos.close();
				htMapLiteralToFile.put(lit, sURIPath);
				htMapLiteralToHashcode.put(lit, lit.hashCode());
			} catch (FileNotFoundException e) {
				throw new ConductorException(e);
			} catch (IOException e) {
				throw new ConductorException(e);
			}
			return sURIPath;
		}
		else
			return sURI;
	}

	/** Loads the java implementations of the given locations and contexts
	 * storing the results into a map.
	 *
	 * @param setURLContext The set of URL context
	 * @param setURLLocations The set of location to load
	 * @param htMapNameToClass The map to populate
	 * @param htMapNameToResource The map to resource
	 * @param htMapResourceToFormat The map to runnable type
	 * @param htMapResourceToRunnable The map to format type
	 * @return The flow classloader
	 * @throws CorruptedDescriptionException A corrupted description was found
	 */
	@SuppressWarnings("unchecked")
	protected URLClassLoader loadExecutableComponentImplementation(
			Set<String> setURLContext,
			Set<String> setURLLocations,
			Hashtable<String, Class> htMapNameToClass,
			Hashtable<String, Resource> htMapNameToResource,
			Hashtable<Resource, String> htMapResourceToRunnable,
			Hashtable<Resource, String> htMapResourceToFormat )
	throws CorruptedDescriptionException {
		log.fine("Preparing the class loader");
		int iCnt = 0;
		URL [] ua = new URL[setURLContext.size()];
		for ( String sURL:setURLContext )
			try {
				if (sURL.endsWith("/") )
					ua[iCnt++] = new URL(sURL);
				else if (sURL.endsWith(".jar")) {
					ua[iCnt++] = new URL("jar:"+sURL+"!/");
				}
					
			} catch (MalformedURLException e) {
				throw new CorruptedDescriptionException("Context URL "+sURL+" is not a valid URL");
			}
			// Initializing the java class loader
			URLClassLoader urlCL=null;
			if(this.getParentClassloader()==null){
				urlCL=  URLClassLoader.newInstance(ua);	
			}else{
				urlCL = URLClassLoader.newInstance(ua, this.getParentClassloader());
			}
		 
	
		for ( String sClassName:setURLLocations ) {
			try {
				if ( htMapResourceToRunnable.get(htMapNameToResource.get(sClassName)).equals("java") ) {
					log.fine("Loading java component "+sClassName);
					Class clazz = Class.forName(sClassName, true, urlCL);
					htMapNameToClass.put(sClassName,clazz);
				}
			}  
			catch (java.lang.LinkageError e){
				log.warning("Class "+sClassName+" appearing multiple times. Discarding the copies.");
				log.warning("Having multiple copies of class"+sClassName+" can cause erratic behavior if they do not align!");
			}
			catch (ClassNotFoundException e) {
				log.severe("Class "+sClassName+" could not be loaded");
				throw new CorruptedDescriptionException(e);
			}
		}
		return urlCL;

	}


	/**
	 * @return the parentClassloader
	 */
	public ClassLoader getParentClassloader() {
		return parentClassloader;
	}


	/**
	 * @param parentClassloader the parentClassloader to set
	 */
	public void setParentClassloader(ClassLoader parentClassloader) {
		this.parentClassloader = parentClassloader;
	}
	
}
