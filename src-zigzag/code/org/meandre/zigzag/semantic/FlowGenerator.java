package org.meandre.zigzag.semantic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

import org.meandre.core.repository.ConnectorDescription;
import org.meandre.core.repository.DataPortDescription;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.PropertiesDescription;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.system.components.MapExecutableComponent;
import org.meandre.core.system.components.ReduceExecutableComponent;
import org.meandre.core.utils.Constants;
import org.meandre.zigzag.parser.ParseException;
import org.meandre.zigzag.parser.ZigZag;

import sun.tools.jstat.ParserException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** This class stores the parsed flow and does the basic 
 * semantic checks required.
 * 
 * @author Xavier LLor&agrave;
 */
public class FlowGenerator {

	/** The default parallelization degree */
	private final static int AUTO_PARALLELIZATION_DEGREE = Runtime.getRuntime().availableProcessors();
	
	/** The composed repository */
	protected RepositoryImpl ri;
	
	/** The repository sources */
	protected HashSet<String> hsRepositoryLocations;
	
	/** The table containing all the aliased components */
	protected Hashtable<String,ExecutableComponentDescription> htECAlias;
	
	/** This table contains the available instances */
	protected Hashtable<String,ExecutableComponentInstanceDescription> htECInsAlias;
	
	/** The table contains the map between source bindings */
	protected Hashtable<String,ExecutableComponentInstanceDescription> htPortECIns;
	
	/** The set of available bindings */
	protected HashSet<String> hsPortSet;
	
	/** The set of defined connectors */
	protected HashSet<ConnectorDescription> hsConnectors;
	
	/** The parallel instance count */
	protected Hashtable<String,Integer> htParallelInstances;
	
	/** The parallel instance should be ordered? */
	protected Hashtable<String,Boolean> htParallelOrderedInstances;
	
	/** The base URL */
	protected String sBaseURL;
	
	/** The connector counter */
	protected int iConnectorCounter;
	
	/** Initialize the flow generator and print the basic info.
	 * @param sName The name to use
	 * 
	 */
	public void init (String sName) {
		// Print the welcome information
		System.out.println("Meandre ZigZag scripting language compiler ["+ZigZag.ZIGZAG_VERSION+"/"+Constants.MEANDRE_VERSION+"]");
		System.out.println("All rigths reserved by DITA, NCSA, UofI (2007-2008).");
		System.out.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
		System.out.println();
		System.out.flush();
		
		System.out.println("ZigZag compiling file "+sName); 
    	System.out.println();
    	
		// Initializing the repository
		System.out.println("Initializing the repository...");
		ri = new RepositoryImpl(ModelFactory.createDefaultModel());
		hsRepositoryLocations = new HashSet<String>();
		
		// Initializing the component alias table
		htECAlias = new  Hashtable<String,ExecutableComponentDescription>();
		htECInsAlias = new  Hashtable<String,ExecutableComponentInstanceDescription>();
		
		// Parallization information for instances
		htParallelInstances = new Hashtable<String,Integer>();
		htParallelOrderedInstances = new Hashtable<String,Boolean>();
		
		// Binding information
		htPortECIns = new  Hashtable<String,ExecutableComponentInstanceDescription>();
		hsPortSet = new HashSet<String>();
		
		// The connectors
		iConnectorCounter = 0;
		hsConnectors = new HashSet<ConnectorDescription>();
		
		// The base URL
		sBaseURL = "http://meandre.org/zigzag/"+sName+"/"+System.currentTimeMillis()+"/";
		
	}
	
	/** Import a repository 
	 *
	 * @param iLine The line where the import happen
	 * @throws ParseException Problem arised while importing the URI
	 */
	public void importRepository ( String sURI, int iLine ) 
	throws ParseException {
		
		URL url = Tools.filterURItoURL(sURI, iLine);
		
		if ( !hsRepositoryLocations.contains(url.toString()) ) {	
			// Only add the location components if not imported before
			Model mod = Tools.pullRepository(url, iLine);
			Model modStore = ri.getModel();
			RepositoryImpl riNew = new RepositoryImpl(mod);
			// Add the components 
			for ( ExecutableComponentDescription ec:riNew.getAvailableExecutableComponentDescriptions() ) 
				if ( ri.getExecutableComponentDescription(ec.getExecutableComponent())==null )
					modStore.add(ec.getModel());
			// Ignoring the flows
			
			System.out.println("Importing repository "+url);
			ri.refreshCache(modStore);	
			hsRepositoryLocations.add(url.toString());
		}
	}

	/** Imports a component only from the repository.
	 * 
	 * @param sURI The repository URI
	 * @param sCompURI The component URI
	 * @param iLine The line where the request was done
	 * @throws ParseException The component could not be imported
	 */
	public void importRepository(String sURI, String sCompURI, int iLine) throws ParseException {
		URL url = Tools.filterURItoURL(sURI, iLine);
		
		Model mod = Tools.pullRepository(url, iLine);
		Model modStore = ri.getModel();
		Resource resComp = modStore.createResource(Tools.filterURItoURL(sCompURI, iLine).toString());
		RepositoryImpl riNew = new RepositoryImpl(mod);
		ExecutableComponentDescription ecd = riNew.getExecutableComponentDescription(resComp);
		FlowDescription fd = riNew.getFlowDescription(resComp);
		if ( ecd!=null ) {
			// Check if it is a component
			if ( ri.getExecutableComponentDescription(resComp)==null )
				modStore.add(ecd.getModel());
		}
		else if ( fd!=null ) {
			// Check it it is a flow
			throw new ParseException("The requested component "+resComp+" is a flow not a component (line: "+iLine+")");	
		}
		else 
			// The component does not exist in this repository
			throw new ParseException("The requested component "+resComp+" does not exist in repository "+url+" (line: "+iLine+")");		
	}

	/** Creates and alias for the given component.
	 * 
	 * @param sCompURI The component URI
	 * @param sAlias The alias to use
	 * @param iLine The line where the request was done
	 * @throws ParseException Invalid component URI
	 */
	public void aliasCoponent(String sCompURI, String sAlias, int iLine) throws ParseException {
		URL urlComp = Tools.filterURItoURL(sCompURI, iLine);
		
		Resource resComp = ri.getModel().createResource(Tools.filterURItoURL(sCompURI, iLine).toString());
		ExecutableComponentDescription ecd = ri.getExecutableComponentDescription(resComp);
		FlowDescription fd = ri.getFlowDescription(resComp);
		if ( ecd!=null ) {
			// Check if it is a component
			if ( ecd!=null ) {
				htECAlias.put(sAlias,ecd);
				System.out.println("Component "+urlComp+" aliased as "+sAlias);
			}
		}
		else if ( fd!=null ) {
			// Not a componet a flow instead
			throw new ParseException("The requested component "+resComp+" is a flow not a component (line: "+iLine+")");		
		}
		else  {
			// The component does not exist in this repository
			 StringBuffer sbEx = new StringBuffer();
			 sbEx.append("The requested component "+resComp+" does not exist in repository (line: "+iLine+")\nAvailable components are:\n");
			 for ( ExecutableComponentDescription ecdTmp:ri.getAvailableExecutableComponentDescriptions() )
				 sbEx.append("\t"+ecdTmp.getExecutableComponent().toString()+"\n");
			throw new ParseException(sbEx.toString());	
		}
	}

	/** Instantiate the components.
	 * 
	 * @param qSymbols The queue containing the symbol names
	 * @param qComponents The queue containing the component names
	 * @param iLine The line where the request was done
	 * @throws ParseException The instantiation could not be done
	 */
	public void instantiateComponents(Queue<String> qSymbols, Queue<String> qComponents, int iLine) throws ParseException {
		
		for ( int i=0,iMax=qSymbols.size() ; i<iMax ; i++ ) {
			String sSymbol = qSymbols.poll();
			String sComponent = qComponents.poll();
			if ( htECInsAlias.containsKey(sSymbol) )
				throw new ParseException("Instance "+sSymbol+" already instantiated before (line: "+iLine+")");	
			else if ( !htECAlias.containsKey(sComponent) )
				throw new ParseException("Component "+sComponent+" is undefined (line: "+iLine+")");	
			else {
				// Retrieving the component
				ExecutableComponentDescription ecd = htECAlias.get(sComponent);
				// Creating the instance
				ExecutableComponentInstanceDescription ecid = new ExecutableComponentInstanceDescription();
				// Populating the instance
				ecid.setName(sSymbol);
				ecid.setDescription(ecd.getDescription());
				ecid.setExecutableComponent(ecd.getExecutableComponent());
				// Creating the instance URI
				Resource resECI = ri.getModel().createResource(
						ecd.getExecutableComponent().toString()+
						"/instance/"+sSymbol);
				ecid.setExecutableComponentInstance(resECI);
				// Adding the instance
				htECInsAlias.put(sSymbol, ecid);
				System.out.println("Instantiating component "+sComponent+" as "+sSymbol);
			}
		}
	}


	/** Mark and instance as a parallel one.
	 * 
	 * @param sAlias The instance alias
	 * @param iCopies The number of copies
	 */
	public void markParallel(String sAlias, int iCopies) {
		htParallelInstances.put(sAlias, iCopies);
		htParallelOrderedInstances.put(sAlias, false);
		System.out.print("Instance "+sAlias+" mark for paralellization (");
		System.out.println((iCopies==0)?"AUTO replicas)":""+iCopies+" replicas)");
	}

	/** Mark a parallel component for enforced order.
	 * 
	 * @param sAlias The instance alias
	 */
	public void forceOrderedParallel(String sAlias) {
		htParallelOrderedInstances.put(sAlias, true);
		System.out.println("Instance "+sAlias+" mark for ordered parallelization");
	}
	
	/** Sets the property values.
	 * 
	 * @param qLeftIns The left instance
	 * @param qLeftProp The left instance property
	 * @param qRightIns The right instance
	 * @param qRightProp The right instance property
	 * @param iLine The line where the assigment was invoked
	 * @throws ParseException The assigments could not be conducted
	 */
	public void setProperties(Queue<String> qLeftIns, Queue<String> qLeftProp,
			Queue<String> qRightIns, Queue<String> qRightProp, int iLine) throws ParseException {

		for ( int i=0,iMax=qLeftIns.size() ; i<iMax ; i++ ) {
			String sLeftIns = qLeftIns.poll();
			String sLeftProp = qLeftProp.poll();
			String sRightIns = qRightIns.poll();
			String sRightProp = qRightProp.poll();
		
			if ( !htECInsAlias.containsKey(sLeftIns) ) {
				throw new ParseException("Target instance "+sLeftIns+" is undefined (line: "+iLine+")");
			}
			else if ( !htECInsAlias.containsKey(sRightIns) && sRightProp!=null ) {
				throw new ParseException("Source instance "+sRightIns+" is undefined (line: "+iLine+")");
			}
			else {
				ExecutableComponentInstanceDescription ecid = htECInsAlias.get(sLeftIns);
				ExecutableComponentDescription ecd = ri.getExecutableComponentDescription(ecid.getExecutableComponent());
				if ( !ecd.getProperties().getKeys().contains(sLeftProp) )
					throw new ParseException("Unknown target property "+sLeftIns+"."+sLeftProp+". Available keys "+ecd.getProperties().getKeys()+" (line: "+iLine+")");
				
				if ( sRightProp==null ) {
					String sPropVal = Tools.filterPropertyValue(sRightIns);
					PropertiesDescription pd = ecid.getProperties();
					if ( pd==null )
						pd = new PropertiesDescription();
					pd.add(sLeftProp, sPropVal);
					ecid.setProperties(pd);
					System.out.println("Property "+sLeftIns+"."+sLeftProp+" set to \""+sPropVal+"\"");
				}
				else {
					ExecutableComponentInstanceDescription ecidRight = htECInsAlias.get(sRightIns);
					ExecutableComponentDescription ecdRight = ri.getExecutableComponentDescription(ecidRight.getExecutableComponent());
					if ( !ecdRight.getProperties().getKeys().contains(sRightProp) )
						throw new ParseException("Unknown target property "+sRightIns+"."+sRightProp+". Available keys "+ecdRight.getProperties().getKeys()+" (line: "+iLine+")");
					
					PropertiesDescription pd = ecid.getProperties();
					if ( pd==null )
						pd = new PropertiesDescription();
					
					pd.add(sLeftProp, ecdRight.getProperties().getValue(sRightProp));
					ecid.setProperties(pd);
				}
			}
			
			
		}
	}

	/** Create a binding port.
	 * 
	 * @param sTargetIns The name of the binding port
	 * @param iLine The originating line
	 * @throws ParseException The binding is already defined
	 */
	public void createBindingPort(String sTargetIns, int iLine) throws ParseException {
		if ( hsPortSet.contains(sTargetIns) ) 
			throw new ParseException("The binding "+sTargetIns+" already defined "+hsPortSet+" (line: "+iLine+")");
		else
			hsPortSet.add(sTargetIns);
	}
	
	/** Binds two ports.
	 * 
	 * @param sSourceIns The source instance
	 * @param sSourcePort The source port
	 * @param sTargetIns The target instance
	 * @param sTargetPort The target port
	 * @param iLine The line called from
	 * @throws ParseException Something went really bad :D
	 */
	public void bindPort(String sSourceIns, String sSourcePort, 
			String sTargetIns, String sTargetPort, int iLine ) throws ParseException {

		// Check source instance exist
		if ( !hsPortSet.contains(sSourceIns) )
			throw new ParseException("Unknown source instance "+sSourceIns+" "+hsPortSet+" (line: "+iLine+")");
		
		// Check target instance exist
		if ( !htECInsAlias.containsKey(sTargetIns) )
			throw new ParseException("Unknown target instance "+sTargetIns+" "+hsPortSet+" (line: "+iLine+")");
		
		// Check source port exist
		ExecutableComponentInstanceDescription ecidSource = htPortECIns.get(sSourceIns);
		ExecutableComponentDescription ecdSource = ri.getExecutableComponentDescription(ecidSource.getExecutableComponent());
		if ( !existPort(ecdSource.getOutputs(),sSourcePort) ) {
			throw new ParseException("Unknown source port "+sSourcePort+" for binding "+sSourceIns+" where only "+setOfPort(ecdSource.getOutputs())+" are available (line: "+iLine+")");
		}
		
		// Check target port exist
		ExecutableComponentInstanceDescription ecidTarget = htECInsAlias.get(sTargetIns);
		ExecutableComponentDescription ecdTarget = ri.getExecutableComponentDescription(ecidTarget.getExecutableComponent());
		if ( !existPort(ecdTarget.getInputs(),sTargetPort) ) {
			throw new ParseException("Unknown target port "+sTargetPort+" for instance "+sTargetIns+" where only "+setOfPort(ecdTarget.getInputs())+" are available (line: "+iLine+")");
		}
		
		// Adding the connector
		ConnectorDescription cd = new ConnectorDescription();
		Resource res = ri.getModel().createResource(sBaseURL+"connector/"+(iConnectorCounter++));
		cd.setConnector(res);
		cd.setSourceInstance(ecidSource.getExecutableComponentInstance());
		cd.setSourceIntaceDataPort(getPortResource(ecdSource.getOutputs(),sSourcePort));
		cd.setTargetInstance(ecidTarget.getExecutableComponentInstance());
		cd.setTargetIntaceDataPort(getPortResource(ecdTarget.getInputs(),sTargetPort));
		hsConnectors.add(cd);
				
		System.out.println("Binding "+sSourceIns+"."+sSourcePort+" to "+sTargetIns+"."+sTargetPort);
	}

	/** Return the data port resource for the given name.
	 * 
	 * @param setDataport The set of data ports
	 * @param sPortName The port name
	 * @return The resource
	 */
	private Resource getPortResource(Set<DataPortDescription> setDataport,
			String sPortName) {
		for ( DataPortDescription dpd:setDataport ) 
			if ( dpd.getName().equals(sPortName) )
				return dpd.getResource();
		
		return null;
	}
	
	
	/** Check if there is a data port with the given name.
	 * 
	 * @param setDataport The set of data ports
	 * @param sPortName The port name
	 * @return True if exist
	 */
	private boolean existPort(Set<DataPortDescription> setDataport,
			String sPortName) {
		for ( DataPortDescription dpd:setDataport ) 
			if ( dpd.getName().equals(sPortName) )
				return true;
		
		return false;
	}
	
	/** Retun the available ports.
	 * 
	 * @param setDataport The set of data ports
	 * @return The set of available ports
	 */
	private Set<String> setOfPort(Set<DataPortDescription> setDataport) {
		HashSet<String> setRes = new HashSet<String>();
		for ( DataPortDescription dpd:setDataport ) 
			setRes.add(dpd.getName() );
		return setRes;
	}

	/** Links the binding variable with the instance.
	 * 
	 * @param sBinding The binding variable
	 * @param sIns The instance
	 * @param iLine The line called from
	 * @throws ParseException The instance does not exist
	 */
	public void bindBindingPort(String sBinding, String sIns, int iLine) throws ParseException {
		
		if ( !htECInsAlias.keySet().contains(sIns) )
			throw new ParseException("Unknown target instance "+sIns+" (line: "+iLine+")");
		else 
			htPortECIns.put(sBinding, htECInsAlias.get(sIns));
	}

	/** Generates the MAU file for the successfully compiled code.
	 * 
	 * @param sFileName The filename
	 * @throws ParseException Something went wrong
	 */
	public void generateMAU(String sFileName) throws ParseException {
		String sOutputFileName = sFileName.replaceAll(".zz$", ".mau");
		generateMAUToFile(sOutputFileName);
	}
		
	/** Generates the MAU file for the successfully compiled code.
	 * 
	 * @param sOutputFileName The filename
	 * @throws ParseException Something went wrong
	 */
	public void generateMAUToFile(String sOutputFileName) throws ParseException {
		System.out.println("ZigZag compilation finished successfuly!" );
		System.out.print("Preparing flow descriptor... " );
		
		FlowDescription fd = new FlowDescription();
		
		
		// Set the basic flow properties
		sOutputFileName = (sOutputFileName.endsWith("/"))?sOutputFileName.replaceAll("/$", ""):sOutputFileName;
		fd.setFlowComponent(ri.getModel().createResource(sBaseURL+"flow/"+sOutputFileName));
		fd.setName(sOutputFileName);
		fd.setRights("NCSA/UofI open source license");
		fd.setDescription("Automatically compiled from ZigZag file "+sOutputFileName);
		fd.setCreator("ZigZag compiler");
		fd.setCreationDate(new Date());
		
		// Add the instances to the flow
		for ( ExecutableComponentInstanceDescription ecid:htECInsAlias.values() )
			fd.addExecutableComponentInstance(ecid);
		
		// Add the connectors to the flow
		fd.getConnectorDescriptions().addAll(hsConnectors);
		
		System.out.println("done");
		
		if ( htParallelInstances.size()>0 ) {
			System.out.println("Postprocessing flow for parallelization");
			postProcessinParallelization(fd);
			System.out.println("Postprocessing flow for parallelization finished");
		}
		
		
		// Add the component descriptions to the model
		System.out.print("Assembling MAU repository... ");
		Model mod = fd.getModel();
		for ( ExecutableComponentInstanceDescription ecid:fd.getExecutableComponentInstances() )
			mod.add(ri.getExecutableComponentDescription(ecid.getExecutableComponent()).getModel());
		
		System.out.println("done");
		
		// Generate the compressed MAU file
		try {
			generateCompressedMau(sOutputFileName, fd, mod);
		}
		catch ( Exception e ) {
			throw new ParseException(e.toString());
		}
		
	}

	/** Generates the compressed MAU file.
	 * 
	 * @param sOutputFileName The output file to generate
	 * @param fd The flow description
	 * @param mod The model described by the repository
	 * @throws ParserException Something went wrong
	 */
	private void generateCompressedMau(String sOutputFileName, FlowDescription fd, Model mod)
			throws ParserException {
		try {
			System.out.println("Writing MAU file: "+sOutputFileName);
			
			// Prepare the tree structure to jar
			File outputDirectory = Tools.createUniqueDirectory(sOutputFileName);
			File odRepository = new File(outputDirectory.getAbsoluteFile()+File.separator+"repository");
			File odContexts = new File(outputDirectory.getAbsoluteFile()+File.separator+"contexts");
			File odPlugins = new File(outputDirectory.getAbsoluteFile()+File.separator+"plugins");
			
			if ( !odRepository.mkdir() || !odContexts.mkdir() || !odPlugins.mkdir() )
				throw new IOException("Could not create MAU file structure");
			
			System.out.println("\tMAU file base structure generated");
			
			// Write the repository file
			FileOutputStream fos = new FileOutputStream(odRepository.getAbsolutePath()+File.separator+"repository.ttl");
			mod.write(fos,"TTL");
			fos.close();
			System.out.println("\tMAU repository generated");
			
			// Process the contexts
			Tools.prepareJarsToTheFileSystem(fd,ri,odContexts);
			
			// Generate the jar and delete the temp directories
			Tools.generateJarFromDirectory(new File(sOutputFileName), outputDirectory);
			
			System.out.println();
		}
		catch ( Exception e ) {
			throw new ParserException(e.toString());
		}
		
	}

	/** Postprocessing for automatization of the parallelization.
	 * 
	 * @param fd The current flow descriptor
	 */
	private void postProcessinParallelization(FlowDescription fd) {
		
		Model mod = fd.getModel();
		
		// Foreach instance that needs to be parallelized
		for ( String sInsAlias:htParallelInstances.keySet() ) {
			System.out.print("\tStarting parallelization of instance "+sInsAlias+"... ");
			
			// Retrieving the instance descriptor and the number of parallelization requried
			ExecutableComponentInstanceDescription ecid = htECInsAlias.get(sInsAlias);
			int iParallel = htParallelInstances.get(sInsAlias);
			boolean bOrdered = htParallelOrderedInstances.get(sInsAlias);
			System.out.print("degree: "+((iParallel==0)?"AUTO":""+iParallel)+((bOrdered)?" ordered":" unordereed"));
			iParallel = (iParallel==0)?AUTO_PARALLELIZATION_DEGREE:iParallel;
			
			// Replicate the instance
			ExecutableComponentInstanceDescription[] ecida = new ExecutableComponentInstanceDescription[iParallel];
			for ( int i=0 ; i<iParallel ; i++ ) {
				ExecutableComponentInstanceDescription ecidNew = new ExecutableComponentInstanceDescription();
				
				String sNewResIns = ecid.getExecutableComponentInstance().toString()+"/paralell/"+i;
				ecidNew.setDescription(ecid.getDescription());
				ecidNew.setExecutableComponent(ecid.getExecutableComponent());
				ecidNew.setExecutableComponentInstance(mod.createResource(sNewResIns));
				ecidNew.setName(ecid.getName()+"-parallel-"+i);
				PropertiesDescription ecidProp = ecid.getProperties();
				Hashtable<String,String> htNewInsPropValues = new Hashtable<String,String>();
				if ( ecidProp!=null )
					for ( String sKey:ecidProp.getKeys() ) 
						htNewInsPropValues.put(sKey,ecidProp.getValue(sKey));
				htNewInsPropValues.put("parallel_id", ""+i);
				htNewInsPropValues.put("parallel_instances", ""+iParallel);
				ecidNew.setProperties(new PropertiesDescription(htNewInsPropValues));
				
				ecida[i] = ecidNew;
				fd.addExecutableComponentInstance(ecidNew);
			}
			
			// Get the executable component description for the instance
			ExecutableComponentDescription ecd = ri.getExecutableComponentDescription(ecid.getExecutableComponent());
			
			// Process the outputs
			if ( bOrdered ) {
				// Connect the output of ordered parallelization
				
				// Create the current reducer instance
				for ( DataPortDescription dpdOutput:ecd.getOutputs() ) {
					// Create the reducer component and add it to the model
					ExecutableComponentDescription ecdRed = ReduceExecutableComponent.getExecutableComponentDescription(iParallel);
					ri.refreshCache(ri.getModel().add(ecdRed.getModel()));
					
					// Create a reducer instance
					ExecutableComponentInstanceDescription ecidReducer = new ExecutableComponentInstanceDescription();
					String sNewResIns = ecid.getExecutableComponentInstance().toString()+"/reduce/"+dpdOutput.getName()+"/"+iParallel;
					ecidReducer.setDescription("Ordered reducer for "+ecid.getName());
					ecidReducer.setExecutableComponent(ecdRed.getExecutableComponent());
					ecidReducer.setExecutableComponentInstance(mod.createResource(sNewResIns));
					ecidReducer.setName(ecid.getName()+"-reducer-"+dpdOutput.getName()+"-"+iParallel);
					Hashtable<String,String> htNewInsPropValues = new Hashtable<String,String>();
					ecidReducer.setProperties(new PropertiesDescription(htNewInsPropValues));
					fd.addExecutableComponentInstance(ecidReducer);
					
					// Rebind reducer to target instance
					DataPortDescription dpdReducerOutput = ecdRed.getOutputs().iterator().next();
					rebindReducerToTarget(fd,ecidReducer,dpdReducerOutput,ecid,dpdOutput);
					
					// Bind all the instance port to this reducer
					for ( int i=0 ; i<iParallel ; i++ )
						bindParallelInstanceToReducer(fd,ecdRed,ecidReducer,dpdOutput,ecida[i],i);
				}
				
			}
			else {
				// Connect the output of unordered parallelization
				for ( int i=0 ; i<iParallel ; i++ ) {
					bindUnorderedParallelInstnaces(fd,ecd,ecid,ecida[i],i);
				}
				// Remove all connectors
				removeOldOutputBindings(fd,ecid);
			}
			
			// Rebind the port to the mapper
			for ( DataPortDescription dpdInput:ecd.getInputs() ) {
				System.out.print("... rebinding port "+dpdInput.getName());
				// The component has inputs and a mapper is needed
				ExecutableComponentDescription ecdMap = MapExecutableComponent.getExecutableComponentDescription(iParallel);
				ri.refreshCache(ri.getModel().add(ecdMap.getModel()));
				
				// Instantiate the mapper and add the mapper instance
				ExecutableComponentInstanceDescription ecidMapper = new ExecutableComponentInstanceDescription();
				String sNewResIns = ecid.getExecutableComponentInstance().toString()+"/mapper/"+dpdInput.getName()+"/"+iParallel;
				ecidMapper.setDescription("Mapper for "+ecid.getName());
				ecidMapper.setExecutableComponent(ecdMap.getExecutableComponent());
				ecidMapper.setExecutableComponentInstance(mod.createResource(sNewResIns));
				ecidMapper.setName(ecid.getName()+"-mapper-"+dpdInput.getName()+"-"+iParallel);
				Hashtable<String,String> htNewInsPropValues = new Hashtable<String,String>();
				ecidMapper.setProperties(new PropertiesDescription(htNewInsPropValues));
				fd.addExecutableComponentInstance(ecidMapper);
				
				
				// Rebind the connections to the mapper
				rebindTargetPort(fd,ecdMap,ecid,ecidMapper,dpdInput);
				
				// Rebind the mapper to the instances
				for ( int i=0 ; i<iParallel ; i++ ) {
					Resource resMapperOutputPort = null;
					Iterator<DataPortDescription> iter = ecdMap.getOutputs().iterator();
					while ( resMapperOutputPort==null ) {
						DataPortDescription dpdTmp = iter.next();
						if ( dpdTmp.getName().equals("object-"+i) )
							resMapperOutputPort = dpdTmp.getResource();
					}
					bindMapperToParallelInstance(fd,ecidMapper,resMapperOutputPort,ecida[i], dpdInput, i);
				}
			}
			
			// Remove the parallelized instance
			fd.getExecutableComponentInstances().remove(ecid);
			
			
			System.out.println(" ...done");
		}
		
	}

	/** Binds each of the parallel instance to its reducer.
	 * 
	 * @param fd The flow descriptor
	 * @param ecdRed The reducer executable component descriptor
	 * @param ecidReducer The executable component instance for the reducer
	 * @param dpdOutput The parallel instance being processed
	 * @param ecidParallel The parallel instance executable component instance description
	 * @param iRound The parallel instance
	 */
	private void bindParallelInstanceToReducer(
			FlowDescription fd,
			ExecutableComponentDescription ecdRed, ExecutableComponentInstanceDescription ecidReducer,
			DataPortDescription dpdOutput,
			ExecutableComponentInstanceDescription ecidParallel,
			int iRound) {

		Resource resReducerPort = null;
		Iterator<DataPortDescription> iter = ecdRed.getInputs().iterator();
		while ( resReducerPort==null ) {
			DataPortDescription dpdTmp = iter.next();
			if ( dpdTmp.getName().equals("object-"+iRound) )
				resReducerPort = dpdTmp.getResource();
		}
			

		ConnectorDescription cdParallelInstanceToReducer = new ConnectorDescription();
		
		Resource cdRes = fd.getModel().createResource(ecidReducer.getExecutableComponentInstance().toString()+"/output/connector/"+iRound);
		cdParallelInstanceToReducer.setConnector(cdRes);
		cdParallelInstanceToReducer.setSourceInstance(ecidParallel.getExecutableComponentInstance());
		cdParallelInstanceToReducer.setSourceIntaceDataPort(dpdOutput.getResource());
		cdParallelInstanceToReducer.setTargetInstance(ecidReducer.getExecutableComponentInstance());
		cdParallelInstanceToReducer.setTargetIntaceDataPort(resReducerPort);
		
		fd.getConnectorDescriptions().add(cdParallelInstanceToReducer);
	}

	/** Binds the reducer to the given target instance and port.
	 * 
	 * @param fd The flow descriptor
	 * @param ecidReducer The executable component instance for the reducer
	 * @param dpdEcidReducer 
	 * @param ecid The instance being replaced
	 * @param dpdOutput The data port being processed
	 */
	private void rebindReducerToTarget(FlowDescription fd,
			ExecutableComponentInstanceDescription ecidReducer,
			DataPortDescription dpdEcidReducer, ExecutableComponentInstanceDescription ecid,
			DataPortDescription dpdOutput) {
		
		for ( ConnectorDescription cd:fd.getConnectorDescriptions() ) 
			if ( cd.getSourceInstance().equals(ecid.getExecutableComponentInstance()) && 
				 cd.getSourceIntaceDataPort().equals(dpdOutput.getResource()) ) {
				cd.setSourceInstance(ecidReducer.getExecutableComponentInstance());
				cd.setSourceIntaceDataPort(dpdEcidReducer.getResource());
			}
	}

	/** Binds the unordered parallel instances.
	 * 
	 * @param fd The flow descriptor
	 * @param ecd The executable component descriptor
	 * @param ecid The original executable instance description
	 * @param ecidNewPar The new unordered parallel instance description 
	 * @param iParallel The parallel instance being processed
	 */
	private void bindUnorderedParallelInstnaces(
			FlowDescription fd,
			ExecutableComponentDescription ecd,
			ExecutableComponentInstanceDescription ecid,
			ExecutableComponentInstanceDescription ecidNewPar,
			int iParallel) {
		
		Set<ConnectorDescription> setToBeAdded = new HashSet<ConnectorDescription>();
		
		int iCnt=0;
		for ( ConnectorDescription cd:fd.getConnectorDescriptions() ) 
			if  ( cd.getSourceInstance().equals(ecid.getExecutableComponentInstance()) ) {
				ConnectorDescription cdMapToParallelInstance = new ConnectorDescription();
				
				Resource cdRes = fd.getModel().createResource(ecidNewPar.getExecutableComponentInstance().toString()+"/parallel/"+iParallel+"/connector/"+(iCnt++));
				cdMapToParallelInstance.setConnector(cdRes);
				cdMapToParallelInstance.setSourceInstance(ecidNewPar.getExecutableComponentInstance());
				cdMapToParallelInstance.setSourceIntaceDataPort(cd.getSourceIntaceDataPort());
				cdMapToParallelInstance.setTargetInstance(cd.getTargetInstance());
				cdMapToParallelInstance.setTargetIntaceDataPort(cd.getTargetIntaceDataPort());
				
				setToBeAdded.add(cdMapToParallelInstance);
			}
		
		// Add the new ones
		for ( ConnectorDescription cd:setToBeAdded )
			fd.getConnectorDescriptions().add(cd);
	}

	/** Remove all the output connector for the given instance.
	 * 
	 * @param fd The flow descriptor
	 * @param ecid The executable instance for which connnectors need to be removed
	 */
	private void removeOldOutputBindings ( FlowDescription fd, ExecutableComponentInstanceDescription ecid) {
		Set<ConnectorDescription> setConnectors = fd.getConnectorDescriptions();
		Set<ConnectorDescription> setToBeRemoved = new HashSet<ConnectorDescription>();
		
		// Collect the ones that need to be removed
		for ( ConnectorDescription cd:setConnectors ) 
			if  ( cd.getSourceInstance().equals(ecid.getExecutableComponentInstance()) )
				setToBeRemoved.add(cd);
		
		// Remove them
		for ( ConnectorDescription cd:setToBeRemoved )
			setConnectors.remove(cd);
	}
	
	/** Rebinds the port to the indicated mapper.
	 * 
	 * @param fd The flow description 
	 * @param ecdMap The mapper description
	 * @param ecid The original instance
	 * @param ecidMapper The executable component instance of the mapper
	 * @param dpdInput The identifier of the input
	 */
	private void rebindTargetPort(FlowDescription fd,
			ExecutableComponentDescription ecdMap, ExecutableComponentInstanceDescription ecid,
			ExecutableComponentInstanceDescription ecidMapper, DataPortDescription dpdInput) {
		
		for ( ConnectorDescription cd:fd.getConnectorDescriptions() ) 
			if ( cd.getTargetInstance().equals(ecid.getExecutableComponentInstance()) &&
				 cd.getTargetIntaceDataPort().equals(dpdInput.getResource() )) {
				cd.setTargetInstance(ecidMapper.getExecutableComponentInstance());
				cd.setTargetIntaceDataPort(ecdMap.getInputs().iterator().next().getResource());
			}
		
	}

	/** Binds the mapper to the parallel instances.
	 * 
	 * @param fd The flow descriptor
	 * @param ecidMapper The mapper instance
	 * @param resMapperOutputPort The mapper output port description
	 * @param ecidParallelInstance The component instance
	 * @param dpdInput The current port being processed
	 * @param iRound The parallel instance being processes
	 */
	private void bindMapperToParallelInstance(
			FlowDescription fd,
			ExecutableComponentInstanceDescription ecidMapper,
			Resource resMapperOutputPort,
			ExecutableComponentInstanceDescription ecidParallelInstance, DataPortDescription dpdInput, int iRound) {
		
		ConnectorDescription cdMapToParallelInstance = new ConnectorDescription();
		
		Resource cdRes = fd.getModel().createResource(ecidMapper.getExecutableComponentInstance().toString()+"/output/connector/"+iRound);
		cdMapToParallelInstance.setConnector(cdRes);
		cdMapToParallelInstance.setSourceInstance(ecidMapper.getExecutableComponentInstance());
		cdMapToParallelInstance.setSourceIntaceDataPort(resMapperOutputPort);
		cdMapToParallelInstance.setTargetInstance(ecidParallelInstance.getExecutableComponentInstance());
		cdMapToParallelInstance.setTargetIntaceDataPort(dpdInput.getResource());
		
		fd.getConnectorDescriptions().add(cdMapToParallelInstance);
	}


		
}
