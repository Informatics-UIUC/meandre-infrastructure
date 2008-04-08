package org.meandre.zigzag.semantic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Set;

import org.meandre.core.store.repository.ConnectorDescription;
import org.meandre.core.store.repository.DataPortDescription;
import org.meandre.core.store.repository.ExecutableComponentDescription;
import org.meandre.core.store.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.store.repository.FlowDescription;
import org.meandre.core.store.repository.PropertiesDescription;
import org.meandre.core.store.repository.RepositoryImpl;
import org.meandre.core.utils.Constants;
import org.meandre.zigzag.parser.ParseException;
import org.meandre.zigzag.parser.ZigZag;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** This class stores the parsed flow and does the basic 
 * semantic checks required.
 * 
 * @author Xavier LLor&agrave;
 */
public class FlowGenerator {

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
		else 
			// The component does not exist in this repository
			throw new ParseException("The requested component "+resComp+" does not exist in repository "+urlComp+" (line: "+iLine+")");		
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
	 * @throws IOException The file could not be written
	 */
	public void generateMAU(String sFileName) throws IOException {
		FlowDescription fd = new FlowDescription();
		String sOutputFileName = sFileName.replaceAll(".zz$", ".mau");
		
		// Set the basic flow properties
		fd.setFlowComponent(ri.getModel().createResource(sBaseURL+"flow/"+sFileName));
		fd.setName(sFileName);
		fd.setRights("NCSA/UofI open source license");
		fd.setDescription("Automatically compiled from ZigZag file "+sFileName);
		fd.setCreator("ZigZag compiler");
		fd.setCreationDate(new Date());
		
		// Add the instances to the flow
		for ( ExecutableComponentInstanceDescription ecid:htECInsAlias.values() )
			fd.addExecutableComponentInstance(ecid);
		
		// Add the connectors to the flow
		fd.getConnectorDescriptions().addAll(hsConnectors);
		
		// Add the component descriptions to the model
		Model mod = fd.getModel();
		for ( ExecutableComponentInstanceDescription ecid:htECInsAlias.values() )
			mod.add(ri.getExecutableComponentDescription(ecid.getExecutableComponent()).getModel());
		
		// TODO: Embed the contexts
		
		System.out.println("ZigZag compilation finished successfuly!" );
		System.out.println("Writing MAU file: "+sOutputFileName);
		FileOutputStream fos = new FileOutputStream(sOutputFileName);
		mod.write(fos,"TTL");
		fos.close();
		System.out.println();
		
	}

		
}
