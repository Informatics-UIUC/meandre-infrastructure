package org.meandre.core.engine;

import java.io.File;
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
import org.meandre.core.store.repository.ConnectorDescription;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.repository.DataPortDescription;
import org.meandre.core.store.repository.ExecutableComponentDescription;
import org.meandre.core.store.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.store.repository.FlowDescription;
import org.meandre.core.store.repository.PropertiesDescription;
import org.meandre.core.store.repository.PropertiesDescriptionDefinition;
import org.meandre.core.store.repository.QueryableRepository;

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

	/*
	public void reload() {
		int iCnt = 0;
		URL [] ua = new URL[setLoadableLocations.size()];
		for ( URL url:setLoadableLocations )
			ua[iCnt++] = url;
		urlCL = new URLClassLoader(ua);
	}
	*/

	/** Creates an execution object for the given flow description.
	 *
	 * @param qr The queryable repository containing component descriptions
	 * @param resFlow The resource identifying the flow to prepare for execution
	 * @return The executor object
	 * @throws CorruptedDescriptionException Inconsistencies where found on the flow definition aborting the creation of the Executor
	 * @throws ConductorException The counductor could not create an executable flow
	 */
	@SuppressWarnings("unchecked")
	public Executor buildExecutor(QueryableRepository qr, Resource res) throws CorruptedDescriptionException, ConductorException {
		// The unique execution flow ID
		String sFlowUniqueExecutionID = res.toString()+File.pathSeparator+System.currentTimeMillis()+File.pathSeparator+(new Random().nextInt());
		// Set of loadable context URLs
		Set<String> setURLContext = new HashSet<String>();
		// Set of loadable locations
		Set<String> setURLLocations = new HashSet<String>();
		// Map class names to classes
		Hashtable<String,Class> htMapNameToClass = new Hashtable<String,Class>();
		// Map reource names to classes
		Hashtable<Resource,String> htMapResourceToName = new Hashtable<Resource,String>();
		// Map reource names to classes
		Hashtable<String,Resource> htMapNameToResource = new Hashtable<String,Resource>();
		// Map resource names to runnable
		Hashtable<Resource,String> htMapResourceToRunnable = new Hashtable<Resource,String>();
		// Map resource names to runnable
		Hashtable<Resource,String> htMapResourceToFormat = new Hashtable<Resource,String>();

		// Get the description
		FlowDescription fd = qr.getFlowDescription(res);

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
			for ( Resource resContext:comp.getContext() )
				setURLContext.add(resContext.getURI().trim());
			
			// Retrieve the location
			boolean bChomped = false;
			String sURI = comp.getLocation().getURI().trim();
			String sURIHead = null;
			for ( String sCntx:setURLContext )
				if ( sURI.startsWith(sCntx) ) {
					bChomped = true;
					sURIHead = sURI.substring(0,sCntx.length());
					sURI = sURI.substring(sCntx.length());
				}
			
			// Check if location does not match any context
			if ( bChomped ) {
				setURLLocations.add(sURI);
				setURLContext.remove(sURIHead);
				htMapResourceToName.put(comp.getExecutableComponent(), sURI);
				htMapNameToResource.put(sURI, comp.getExecutableComponent());
			}
			else
				throw new CorruptedDescriptionException("Location "+sURI+" does not match any of the executable component contexts");
		}
		
		// Load the component classes
		loadExecutableComponentImplementation(setURLContext, setURLLocations, htMapNameToClass, htMapNameToResource, htMapResourceToRunnable, htMapResourceToFormat);

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

			ActiveBuffer ab = htMapInAB.get(sIDIn);

			// Update the output mapping name for a given instance
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
	protected void loadExecutableComponentImplementation(Set<String> setURLContext, Set<String> setURLLocations, Hashtable<String, Class> htMapNameToClass, Hashtable<String, Resource> htMapNameToResource, Hashtable<Resource, String> htMapResourceToRunnable, Hashtable<Resource, String> htMapResourceToFormat) throws CorruptedDescriptionException {
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
