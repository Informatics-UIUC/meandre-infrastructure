package org.meandre.core.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.core.ExecutableComponent;
import org.meandre.core.engine.policies.component.availability.WrappedComponentAllInputsRequired;
import org.meandre.core.engine.policies.component.availability.WrappedComponentAnyInputRequired;
import org.meandre.core.logger.LoggerFactory;
import org.meandre.core.store.Store;
import org.meandre.core.store.repository.ConnectorDescription;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.repository.DataPortDescription;
import org.meandre.core.store.repository.ExecutableComponentDescription;
import org.meandre.core.store.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.store.repository.FlowDescription;
import org.meandre.core.store.repository.PropertiesDescription;
import org.meandre.core.store.repository.PropertiesDescriptionDefinition;
import org.meandre.core.store.repository.QueryableRepository;
import org.meandre.core.utils.HexConverter;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/** This class is devoted to the arrengement required to execute a given
 * flow description. The basic input the conductor is in charge of generate
 * the proper structures and connections to create the Executor object that
 * which will eventually execute the flow.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class Conductor {

	/** List of working implementations for executable components. */
	static private final String [] saExecutableComponentLanguages = { "java" };

	/** Set of loadable implementations */
	private Set<String> setLoadableLanguages = null;

	/** List of working implementations for executable components. */
	static private final String [] saExecutableComponentTypes = { "java/class" };

	/** A simple separator caracter for URLs */
	static private final String URL_SEAPARTOR = "##--##--##";

	/** The default queue size for the active buffers */
	public static final int DEFAULT_QUEUE_SIZE = 10;

	/** Set of loadable implementations */
	private Set<String> setLoadableComponents = null;

	/** The default active buffer size */
	private int iActiveBufferSize;

	/** The core root logger */
	protected static Logger log = LoggerFactory.getCoreLogger();

	/** Embeded context to file URI mapper */
	private static Hashtable<Literal,String> htMapLiteralToFile = new Hashtable<Literal,String>();
	
	/** Embeded context to hash code mapper */
	private static Hashtable<Literal,Integer> htMapLiteralToHashcode = new Hashtable<Literal,Integer>();
	
	/** Initialize a conductor with a set of valid URLs.
	 *
	 */
	public Conductor( int iActiveBufferSize ) {
		log.info("Creating a conductor object");
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
	 * @param resFlow The resource identifying the flow to prepare for execution
	 * @return The executor object
	 * @throws CorruptedDescriptionException Inconsistencies where found on the flow definition aborting the creation of the Executor
	 * @throws ConductorException The counductor could not create an executable flow
	 */
	@SuppressWarnings("unchecked")
	public Executor buildExecutor(QueryableRepository qr, Resource res) 
	throws CorruptedDescriptionException, ConductorException {
		// The unique execution flow ID
		String sFlowUniqueExecutionID = res.toString()+File.pathSeparator+System.currentTimeMillis()+File.pathSeparator+(new Random().nextInt());
		// Map class names to classes
		Hashtable<String,Class> htMapNameToClass = new Hashtable<String,Class>();
		// Map reource names to classes
		Hashtable<Resource,String> htMapResourceToName = new Hashtable<Resource,String>();
		
		// Get the description
		FlowDescription fd = qr.getFlowDescription(res);

		// STEP 0: Gather all the required contexts and load them
		log.info("STEP 0: Plugging the required contexts");
		plugRequiredContexts(qr, htMapNameToClass, htMapResourceToName,fd);

		// STEP 1: Instantiate the flow instances
		log.info("STEP 1: Instantiate the flow instances");
		Hashtable<Resource,Object> htECInstances = new Hashtable<Resource,Object>();
		for ( ExecutableComponentInstanceDescription ec:fd.getExecutableComponentInstances() ) {
			Resource   ins = ec.getExecutableComponentInstance();
			Resource ecomp = ec.getExecutableComponent();
			try {
				htECInstances.put(ins,htMapNameToClass.get(htMapResourceToName.get(ecomp)).newInstance());
			} catch (InstantiationException e) {
				throw new ConductorException("Condcuctor could not instantiate class "+htMapResourceToName.get(ecomp)+"\n"+e);
			} catch (IllegalAccessException e) {
				throw new ConductorException("Condcuctor could not instantiate class "+htMapResourceToName.get(ecomp)+"\n"+e);
			}
		}

		// STEP 2: Create empty active buffers
		log.info("STEP 2: Create empty active buffers");
		Hashtable<String,ActiveBuffer> htMapInAB = new Hashtable<String,ActiveBuffer>();
		for ( ConnectorDescription cd:fd.getConnectorDescriptions() ) {
			Resource resCSIns = cd.getTargetInstance();
			Resource resCSInsDP = cd.getTargetIntaceDataPort();
			//Resource resECID = fd.getExecutableComponentResourceForInstance(resCSIns);
			//ExecutableComponentDescription compDesc = qr.getExecutableComponentDescription(resECID);
			String sID = resCSIns.toString()+URL_SEAPARTOR+resCSInsDP.toString();
			if ( htMapInAB.get(sID)==null ) {
				//htMapInAB.put(sID,new ActiveBuffer(compDesc.getInput(resCSInsDP).getIdentifier(),iActiveBufferSize));
				htMapInAB.put(sID,new ActiveBuffer(sID,iActiveBufferSize));
			}
		}

		// STEP 3: Per instance create and input/output set
		log.info("STEP 3: Per instance create and input/output set");
		Hashtable<String,Set<ActiveBuffer>> htInsInputSet = new Hashtable<String,Set<ActiveBuffer>>();
		Hashtable<String,Set<ActiveBuffer>> htInsOutputSet = new Hashtable<String,Set<ActiveBuffer>>();
		for ( ExecutableComponentInstanceDescription ecid:fd.getExecutableComponentInstances() ) {
			String sID = ecid.getExecutableComponentInstance().toString();
			htInsInputSet.put(sID, new HashSet<ActiveBuffer>());
			htInsOutputSet.put(sID, new HashSet<ActiveBuffer>());
		}

		// STEP 4: Populate input/output instance sets
		log.info("STEP 4: Populate input/output instance sets");
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
			String  sIDIn = cd.getTargetInstance().toString()+URL_SEAPARTOR+cd.getTargetIntaceDataPort().toString();

			fd.getModel().write(System.out,"TTL",null);
			ActiveBuffer ab = htMapInAB.get(sIDIn);
			
			// Update the output mapping name for a given instance
			//System.out.println(cd.getSourceInstance());
			//System.out.println(fd.getExecutableComponentResourceForInstance(cd.getSourceInstance()));
			//System.out.println(qr.getExecutableComponentDescription(fd.getExecutableComponentResourceForInstance(cd.getSourceInstance()))==null);
			
			ExecutableComponentDescription compDesc = qr.getExecutableComponentDescription(fd.getExecutableComponentResourceForInstance(cd.getSourceInstance()));
			DataPortDescription dpdOut = compDesc.getOutput(cd.getSourceIntaceDataPort());
			Hashtable<String,String> htMapOut = htMapOutputTranslation.get(cd.getSourceInstance());
			htMapOut.put(sIDOutSet+URL_SEAPARTOR+dpdOut.getIdentifier(), ab.getName());
			
			// Update the input and output sets
			htInsOutputSet.get(sIDOutSet).add(ab);
			htInsInputSet.get(sIDInSet).add(ab);
		}

		// STEP 5: Reconstruct properties per instance set
		log.info("STEP 5: Reconstruct properties per instance set");
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
		log.info("STEP 6: Create thread groups");
		ThreadGroup tgFlow = new ThreadGroup(res.toString());

		// STEP 7: Create wrapping components and add the wrapping components to the execution set
		log.info("STEP 7: Create wrapping components and add the wrapping components to the execution set");
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
									ecid.getExecutableComponentInstance().toString(),
									(ExecutableComponent) htECInstances.get(resECI),
									htInsInputSet.get(sResECI),
									htInsOutputSet.get(sResECI),
									htMapOutputTranslation.get(resECI),
									htMapInputLogicNameTranslation.get(resECI),
									htMapOutputLogicNameTranslation.get(resECI),
									tgFlow,
									sResECI,
									htInstaceProperties.get(resECI)
								)
						);
				else if ( firing.equals("any") )
					setWC.add(
							new WrappedComponentAnyInputRequired (
									sFlowUniqueExecutionID,
									ecid.getExecutableComponentInstance().toString(),
									(ExecutableComponent) htECInstances.get(resECI),
									htInsInputSet.get(sResECI),
									htInsOutputSet.get(sResECI),
									htMapOutputTranslation.get(resECI),
									htMapInputLogicNameTranslation.get(resECI),
									htMapOutputLogicNameTranslation.get(resECI),
									tgFlow,
									sResECI,
									htInstaceProperties.get(resECI)
								)
						);
			} catch (InterruptedException e) {
				throw new ConductorException("Condcuctor could not instantiate the wapping component for instance"+sResECI+"\n"+e);
			}

		}

		// STEP 8: reate the executor
		log.info("STEP 8: Create the executor");
		try {
			return new Executor(sFlowUniqueExecutionID,tgFlow,setWC);
		} catch (InterruptedException e) {
			throw new ConductorException("Condcuctor could not create the executor object\n"+e);
		}
	}

	/** Loads and plugs the required contexts to the flow being prepared.
	 * 
	 * @param qr The queriable repository
	 * @param htMapNameToClass The map from the name to the class
	 * @param htMapResourceToName The map from the resource to a name
	 * @param fd The flow descriptor 
	 * @throws CorruptedDescriptionException The descriptor is corrupted
	 * @throws ConductorException The literal containing the resource could not be dumped to a file
	 */
	@SuppressWarnings("unchecked")
	protected void plugRequiredContexts(QueryableRepository qr,
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
		log.info("Processing executable components contexts and locations");
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
					// The literal is dumped to the file system and a URI generated
					setContexts.add(prepareLiteralToTheFileSystem((Literal)rdfnodeContext));
			
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
		loadExecutableComponentImplementation(setContexts, setURLLocations, htMapNameToClass, htMapNameToResource, htMapResourceToRunnable, htMapResourceToFormat);
	}

	/** This methods dump the Literal to the file system and creates a URI for the class loader.
	 * 
	 * @param lit The literal to dump
	 * @return The URI to the loader resource
	 * @throws ConductorException The literal could not be dump to a file
	 */
	private String prepareLiteralToTheFileSystem(Literal lit) throws ConductorException {
		String sURI = htMapLiteralToFile.get(lit);
		
		if ( sURI==null || htMapLiteralToHashcode.get(lit)!=lit.hashCode() ) {
			// The Literal has not been mapped to a file before
			byte [] ba = (byte[]) lit.getValue();
			String sURIPath = new File(".").toURI().toString();
			String sFilePath = Store.getRunResourcesDirectory()+File.separator+"java"+File.separator;
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
	 * @throws CorruptedDescriptionException A corrupted description was found
	 */
	@SuppressWarnings("unchecked")
	protected void loadExecutableComponentImplementation(
			Set<String> setURLContext, 
			Set<String> setURLLocations,
			Hashtable<String, Class> htMapNameToClass,
			Hashtable<String, Resource> htMapNameToResource,
			Hashtable<Resource, String> htMapResourceToRunnable,
			Hashtable<Resource, String> htMapResourceToFormat )
	throws CorruptedDescriptionException {
		log.info("Preparing the class loader");
		int iCnt = 0;
		URL [] ua = new URL[setURLContext.size()];
		for ( String sURL:setURLContext )
			try {
				ua[iCnt++] = new URL(sURL);
			} catch (MalformedURLException e) {
				throw new CorruptedDescriptionException("Context URL "+sURL+" is not a valid URL");
			}
		// Initializing the java class loader
		URLClassLoader urlCL = new URLClassLoader(ua);
		for ( String sClassName:setURLLocations )
			try {
				if ( htMapResourceToRunnable.get(htMapNameToResource.get(sClassName)).equals("java") ) {
					log.info("Loading java component "+sClassName);
					htMapNameToClass.put(sClassName,Class.forName(sClassName,true,urlCL));
				}
			} catch (ClassNotFoundException e) {
				throw new CorruptedDescriptionException(e);
			}
	}
}
