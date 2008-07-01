package org.meandre.core;


import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.ActiveBuffer;
import org.meandre.core.engine.ActiveBufferException;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.MrProper;
import org.meandre.core.engine.WrappedComponent;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.plugins.MeandrePlugin;
import org.meandre.plugins.PluginFactory;
import org.meandre.webui.PortScroller;
import org.meandre.webui.WebUI;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFactory;
import org.meandre.webui.WebUIFragment;
import org.meandre.webui.WebUIFragmentCallback;

/** This class implement the component context for executable components.
 *
 * @author Xavier Llor&agrave;
 * @last-modified: Amit Kumar -added the getter for sFlowUniqueExecutionID
 * @last-modified: Amit Kumar -added support for flowID
 * @last-mofified: Amit Kumar -added support for getPlugin
 *
 *
 */
public class ComponentContextImpl
implements ComponentContext {

	/** The core root logger */
	protected static Logger log = KernelLoggerFactory.getCoreLogger();

	/** The unique flow execution ID */
	protected String sFlowUniqueExecutionID = null;

	/** The flow ID */
	protected String flowID = null;

	/** The unique flow execution ID */
	protected String sComponentInstanceID = null;

	/** The data proxy for input and outputs */
	private DataProxy dp = null;

	/** The set of possible inputs */
	private Set<String> setInputs = null;

	/** The set of possible outputs. */
	private Set<String> setOutputs = null;

	/** The array of output buffer names */
	private String [] saOutputNames = null;

	/** The hash table containing the active buffers */
	private Hashtable<String,ActiveBuffer> htActiveBufferOuputs = null;

	/** The hash table containing the component properties */
	private Hashtable<String,String> htProperties = null;

	/** The base webui */
	private WebUI webui = null;

	/** The webui fragments tracking system */
	private Hashtable<WebUIFragmentCallback,WebUIFragment> htWebUIframent = new Hashtable<WebUIFragmentCallback,WebUIFragment>();

	/** The input logic name mapping */
	private Hashtable<String, String> htInputLogicNameMap = null;

	/** The output logic name mapping */
	private Hashtable<String, String> htOutputLogicNameMap = null;

	/** The reverse input mapping */
	private Hashtable<String, String> htInputLogicNameMapReverse = null;

	/** The array of available input names */
	private String[] saInputNames;

	/** MrProbe thread */
	private MrProbe thdMrProbe = null;
	
	/** The wrapped component parent */
	private WrappedComponent wcParent = null;

	/** The wrapped component parent */
	private CoreConfiguration ccCnf = null;
	


	/** Create a component context with the given input and output active buffers
	 * for a given wrapped component.
	 *
	 * @param sFlowUniqueID The unique flow execution ID
	 * @param sComponentInstanceID The unique component ID
	 * @param setInputs The name of the input active buffers
	 * @param setOutputs The name of the output active buffers
	 * @param htOutputMap The map of the output to the real active buffer name
	 * @param htOutputLogicNameMap The input logic name map
	 * @param htInputLogicNameMap The output logic name map
	 * @param htProperties The component properties
	 * @param thdMrProbe The MrProbe thread
	 * @param cnf The core configuration
	 */
	public ComponentContextImpl(String sFlowUniqueID,String flowID,
			String sComponentInstanceID, Set<ActiveBuffer> setInputs,
			Set<ActiveBuffer> setOutputs,
			Hashtable<String, String> htOutputMap,
			Hashtable<String, String> htInputLogicNameMap,
			Hashtable<String, String> htOutputLogicNameMap,
			Hashtable<String, String> htProperties,
			MrProbe thdMrProbe,
			WrappedComponent wc,
			CoreConfiguration cnf ) {

		// Create the data proxy
		this.sFlowUniqueExecutionID = sFlowUniqueID;
		this.flowID=flowID;
		this.sComponentInstanceID = sComponentInstanceID;
		this.thdMrProbe = thdMrProbe;
		this.wcParent = wc;
		this.ccCnf = cnf;

		this.htInputLogicNameMap = htInputLogicNameMap;
		this.htOutputLogicNameMap = htOutputLogicNameMap;

		this.dp = new DataProxy(setInputs);
		this.htActiveBufferOuputs = new Hashtable<String,ActiveBuffer>();
		this.htProperties = htProperties;
		// Create the proper input set
		this.setInputs = htInputLogicNameMap.keySet();
		//this.saInputNames = new String[setInputs.size()];
		this.saInputNames = new String[this.setInputs.size()];
		this.htInputLogicNameMapReverse = new Hashtable<String,String>();
		int iCnt = 0;
		if ( setInputs.size()>0 ){
			for ( String sKey:this.setInputs) {
				saInputNames[iCnt++] = sKey;
				this.htInputLogicNameMapReverse.put(htInputLogicNameMap.get(sKey), sKey);
			}
		}

		// Create the proper output set
		this.setOutputs    = htOutputLogicNameMap.keySet();
		this.saOutputNames = new String[htOutputLogicNameMap.size()];
		iCnt = 0;
		for ( String sOutputName:htOutputLogicNameMap.keySet() ) {
			saOutputNames[iCnt++] = sOutputName;
		}

		// Create a reverse hash table with the maping of output names
		Hashtable<String,String> htReverse = new Hashtable<String,String>();
		for ( String sOutputName:htOutputMap.keySet() )
			htReverse.put(htOutputMap.get(sOutputName), sOutputName);

		for ( ActiveBuffer abOutput:setOutputs ) {
			this.htActiveBufferOuputs.put(htReverse.get(abOutput.getName()),abOutput);
		}

		try {
			 webui = WebUIFactory.getWebUI(sFlowUniqueExecutionID,wcParent.getMrProper(),
					 thdMrProbe,cnf, 
					 PortScroller.getInstance(cnf).nextAvailablePort(sFlowUniqueExecutionID));
		} catch (WebUIException e) {
			log.warning("WebUI could not be retrieved: "+e.getMessage());
		}
	}

	/** Returns the path to the public resources directory.
	 * 
	 * @return Path to public resources directory.
	 */
	public String getPublicResourcesDirectory () {
		return ccCnf.getPublicResourcesDirectory();
	}

	
	/** The name of the available inputs.
	 *
	 * @return The array containing the names
	 */
	public String [] getInputNames () {

		return saInputNames;
	}

	/** The name of the available outputs.
	 *
	 * @return The array containing the names
	 */
	public String [] getOutputNames () {
		return saOutputNames;
	}

	/** Add a given data component to the given active buffer.
	 *
	 * @param sInputBuffer The name of the input
	 * @param obj The data component
	 * @throws ComponentContextException Violation of the component context detected
	 */
	public void setDataComponentToInput ( String sInputBuffer, Object obj ) throws ComponentContextException {
		String sLogicName = htInputLogicNameMapReverse.get(sInputBuffer);
		if ( !setInputs.contains(sLogicName) )
			throw new ComponentContextException("The requested input "+sInputBuffer+" does not exist.");
		if ( obj!=null )
			dp.setInput(sInputBuffer,obj);
	}

	/** Returns the current data component on the given active buffer.
	 *
	 * @param The name of the input
	 * @return The data component
	 * @throws ComponentContextException Violation of the component context detected
	 */
	public Object getDataComponentFromInput ( String sInputBuffer ) throws ComponentContextException {
		if ( !setInputs.contains(sInputBuffer) )
			throw new ComponentContextException("The requested input "+sInputBuffer+" does not exist.");
		
		Object obj = dp.getInput(htInputLogicNameMap.get(sInputBuffer));
		thdMrProbe.probeWrappedComponentPullData(wcParent, sInputBuffer, obj);
		return obj;
	}

	/** Push an object to the given named output.
	 *
	 * @param sOutputBuffer The name of the output
	 * @param obj The object to push
	 * @throws ComponentContextException Violation of the component context detected
	 */
	public void pushDataComponentToOutput ( String sOutputBuffer, Object obj ) throws ComponentContextException {
		thdMrProbe.probeWrappedComponentPushData(wcParent, sOutputBuffer, obj);
		if ( obj==null )
			throw new ComponentContextException("Null cannot be pushed to "+sOutputBuffer+" in component instance "+sComponentInstanceID);
		
		if ( !setOutputs.contains(sOutputBuffer) )
			throw new ComponentContextException("The requested output "+sOutputBuffer+" does not exist.");
		try {
			ActiveBuffer ab = htActiveBufferOuputs.get(htOutputLogicNameMap.get(sOutputBuffer));
			if ( ab!=null )
				ab.pushDataComponent(obj);
			/* The dangling output log
			else
				log.info("Dangling output has received data in a push. Discarding the data.");
			*/
		} catch (ActiveBufferException e) {
			throw new ComponentContextException(e);
		}
	}


	/** Checks if a given input is available.
	 *
	 * @return The name of the input
	 * @throws ComponentContextException A violation of the component context is detected
	 */
	public boolean isInputAvailable ( String sInputBuffer ) throws ComponentContextException {
		if ( sInputBuffer==null )
			// Unconnected input
			return false;
		else if ( dp.getInput(htInputLogicNameMap.get(sInputBuffer))!=null )
			return true;
		else
			return false;
	}


	/** Resets the data proxy contents.
	 *
	 */
	public void resetDataProxy () {
		dp.cleanAllInputs();
	}



	/** Returns the list of property names.
	 *
	  * @return The array of property names
	 */
	public String[] getPropertyNames( ) {
		String [] saPropNames = new String[htProperties.keySet().size()];
		int i=0;

		for ( String sKey:htProperties.keySet() )
			saPropNames[i++] = sKey;

		return saPropNames;
	}

	/** Check a given component property value. If the property does not exist
	 * the call returns null.
	 *
	 * @param sKey The property key
	 * @return The property value (null if property does not exist)
	 */
	public String getProperty ( String sKey ) {
		String sPropertyValue = htProperties.get(sKey);
		thdMrProbe.probeWrappedComponentGetProperty(wcParent, sKey, sPropertyValue);
		return sPropertyValue;
	}


	/** Starts the web-based user interface given the proper implementation of the
	 * webui callback to deal with user action on the client.
	 *
	 * @param wuiCall The webui call back object
	 */
	public void startWebUIFragment ( WebUIFragmentCallback wuiCall ) {
		synchronized ( htWebUIframent ) {
			WebUIFragment wuif = new WebUIFragment(sComponentInstanceID, wuiCall);
			webui.addFragment(wuif);
			htWebUIframent.put(wuiCall,wuif);
		}
	}

	/** Stops the web-based user interface.
	 *
	 * @param wuiCall The webui call back object
	 */
	public void stopWebUIFragment (WebUIFragmentCallback wuiCall) {
		synchronized ( htWebUIframent ) {
			WebUIFragment wuif = htWebUIframent.get(wuiCall);
			if ( wuif!=null )
				webui.removeFragment(wuif);
		}
	}

	/** Stops all the web-based user interface created by this module.
	 *
	 */
	public void stopAllWebUIFragments () {
		synchronized ( htWebUIframent ) {
			Enumeration<WebUIFragment> webUIFragments = htWebUIframent.elements();
			while (webUIFragments.hasMoreElements())
				webui.removeFragment(webUIFragments.nextElement());
		}
	}


	/** Get the webUI URL.
	 *
	 * @param bName True if the url needs to be build using the name.
	 *              False build the URL using the IP address.
	 * @return The webUI URL
	 * @throws ComponentContextException Problem recovering the IP
	 *
	 *
	 */
	public URL getWebUIUrl ( boolean bName ) throws ComponentContextException {
		URL urlRes = null;

		try {

			InetAddress addr = InetAddress.getLocalHost();
			String sHostName = null;

			if ( bName )
				sHostName = "http://"+addr.getCanonicalHostName()+":"+webui.getPort()+"/";
			else
				sHostName = "http://"+addr.toString()+":"+webui.getPort()+"/";

			urlRes = new URL(sHostName);

		} catch ( UnknownHostException e ) {
			throw new ComponentContextException(e);
		} catch (MalformedURLException e) {
			throw new ComponentContextException(e);
		}

		return urlRes;
	}

	/** Returns the logging facility.
	 *
	 * @return The logger object
	 */
	public Logger getLogger() {
		return log;
	}

	/** Returns the unique ID of the executable instance for the current execution component.
	 *
	 * @return The unique execution instance ID
	 */
	public String getExecutionInstanceID () {
		return sComponentInstanceID;
	}


	/**Returns the uniqueID of the flow  instance for the current flow
	 *	@return The unique flow ID
	 */
	public String getFlowExecutionInstanceID() {
		return this.sFlowUniqueExecutionID;
	}

	/**Returns the flowID of the flow
	 *
	 */
	public String getFlowID() {
		return this.flowID;
	}
	
	/** Returns true if the flow has started a termination request.
	 * 
	 * @return True if the flow is aborting
	 */
	public boolean isFlowAborting() {
		return this.wcParent.isTerminating();
	}
	
	/** Returns the reverse name active buffer relation.
	 * 
	 * @return The reverse map
	 */
	public Hashtable<String, String> getInputLogicNameMapReverse() {
		return htInputLogicNameMapReverse;
	}

	/** Sets Mr Propper.
	 * 
	 * @param thdMrPropper The Mr Propper to be set.
	 */
	public void setMrPropper(MrProper thdMrPropper) {
		this.webui.setMrPropper(thdMrPropper);	
	}

	/**Returns the plugin or null if there was a failure initing
	 * the plugin
	 * 
	 */
	public MeandrePlugin getPlugin(String id) {
		PluginFactory  pluginFactory = PluginFactory.getPluginFactory(ccCnf);
		MeandrePlugin mp=pluginFactory.getPlugin(id);
		return mp;
	}
}
