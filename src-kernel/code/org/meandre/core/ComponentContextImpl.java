package org.meandre.core;


import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.ActiveBuffer;
import org.meandre.core.engine.ActiveBufferException;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.MrProper;
import org.meandre.core.engine.WrappedComponent;
import org.meandre.core.engine.policies.component.availability.WrappedComponentAllInputsRequired;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.utils.NetworkTools;
import org.meandre.plugins.MeandrePlugin;
import org.meandre.plugins.PluginFactory;
import org.meandre.webui.ConfigurableWebUIFragmentCallback;
import org.meandre.webui.PortScroller;
import org.meandre.webui.WebUI;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFactory;
import org.meandre.webui.WebUIFragment;
import org.meandre.webui.WebUIFragmentCallback;

/** This class implement the component context for executable components.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class ComponentContextImpl implements ComponentContext {

	/** The core root logger */
	protected static Logger log = KernelLoggerFactory.getCoreLogger();

	/** The unique flow execution ID */
	protected String sFlowUniqueExecutionID = null;

	/** The flow ID */
	protected String flowID = null;

	/** The unique flow execution ID */
	protected String sComponentInstanceID = null;

	/** The name of this component instance */
	protected final String sComponentInstanceName;

	/** The data proxy for input and outputs */
	private DataProxy dp = null;

	/** The set of possible inputs */
	private Set<String> setInputs = null;

	/** The set of possible outputs. */
	private Set<String> setOutputs = null;

	/** The array of output buffer names */
	private String [] saOutputNames = null;

	private final String[] saConnectedOutputNames;

	/** The hash table containing the active buffers */
	private Hashtable<String,ActiveBuffer> htActiveBufferOuputs = null;

	/** The hash table containing the component properties */
	private Hashtable<String,String> htProperties = null;

	/** The base webui */
	private WebUI webui = null;

	/** The webui fragments tracking system */
	private final Hashtable<WebUIFragmentCallback,WebUIFragment> htWebUIframent = new Hashtable<WebUIFragmentCallback,WebUIFragment>();

	/** The input logic name mapping */
	private Hashtable<String, String> htInputLogicNameMap = null;

	/** The output logic name mapping */
	private Hashtable<String, String> htOutputLogicNameMap = null;

	/** The reverse input mapping */
	private Hashtable<String, String> htInputLogicNameMapReverse = null;

	/** The array of available input names */
	private final String[] saInputNames;

	private final String[] saConnectedInputNames;

	/** MrProbe thread */
	private MrProbe thdMrProbe = null;

	/** The wrapped component parent */
	private WrappedComponent wcParent = null;

	/** The wrapped component parent */
	private CoreConfiguration ccCnf = null;

	/** The output console for the flow */
	private PrintStream console = null;

	/** Create a component context with the given input and output active buffers
	 * for a given wrapped component.
	 *
	 * @param sFlowUniqueID The unique flow execution ID
	 * @param sComponentInstanceID The unique component ID
	 * @param setABInputs The name of the input active buffers
	 * @param setABOutputs The name of the output active buffers
	 * @param htOutputMap The map of the output to the real active buffer name
	 * @param htOutputLogicNameMap The input logic name map
	 * @param htInputLogicNameMap The output logic name map
	 * @param htProperties The component properties
	 * @param thdMrProbe The MrProbe thread
	 * @param cnf The core configuration
	 * @param console The output console
	 */
	public ComponentContextImpl(String sFlowUniqueID,String flowID,
			String sComponentInstanceID, String sComponentInstanceName, Set<ActiveBuffer> setABInputs,
			Set<ActiveBuffer> setABOutputs,
			Hashtable<String, String> htOutputMap,
			Hashtable<String, String> htInputLogicNameMap,
			Hashtable<String, String> htOutputLogicNameMap,
			Hashtable<String, String> htProperties,
			MrProbe thdMrProbe,
			WrappedComponent wc,
			CoreConfiguration cnf,
			PrintStream console) {

		// Create the data proxy
		this.sFlowUniqueExecutionID = sFlowUniqueID;
		this.flowID=flowID;
		this.sComponentInstanceID = sComponentInstanceID;
		this.sComponentInstanceName = sComponentInstanceName;
		this.thdMrProbe = thdMrProbe;
		this.wcParent = wc;
		this.ccCnf = cnf;
		this.console = console;

		this.htInputLogicNameMap = htInputLogicNameMap;
		this.htOutputLogicNameMap = htOutputLogicNameMap;

		this.dp = new DataProxy(setABInputs);
		this.htActiveBufferOuputs = new Hashtable<String,ActiveBuffer>();
		this.htProperties = htProperties;
		// Create the proper input set
		this.setInputs = htInputLogicNameMap.keySet();
        this.setOutputs = htOutputLogicNameMap.keySet();
		//this.saInputNames = new String[setInputs.size()];
		this.saInputNames = new String[this.setInputs.size()];
		this.htInputLogicNameMapReverse = new Hashtable<String,String>();
		int iCnt = 0;
		for ( String sKey:this.setInputs) {
		    saInputNames[iCnt++] = sKey;
		    this.htInputLogicNameMapReverse.put(htInputLogicNameMap.get(sKey), sKey);
		}

		Map<String, String> htOutputLogicNameMapReverse = new HashMap<String, String>();
		for (String sKey : this.setOutputs)
		    htOutputLogicNameMapReverse.put(htOutputLogicNameMap.get(sKey), sKey);

		int numConnectedInputs = setABInputs.size();
		this.saConnectedInputNames = new String[numConnectedInputs];

		iCnt = 0;
		for (ActiveBuffer ab : setABInputs)
		    this.saConnectedInputNames[iCnt++] = this.htInputLogicNameMapReverse.get(ab.getName());

		// Create the proper output set
		this.saOutputNames = new String[htOutputLogicNameMap.size()];
		iCnt = 0;
		for ( String sOutputName:htOutputLogicNameMap.keySet() ) {
			saOutputNames[iCnt++] = sOutputName;
		}

		// Create a reverse hash table with the maping of output names
		Map<String,List<String>> htReverse = new HashMap<String,List<String>>();
		for ( String sOutputName:htOutputMap.keySet() ) {
			final String logicName = htOutputMap.get(sOutputName);
			List<String> names = htReverse.get(logicName);
			if (names == null) {
			    names = new ArrayList<String>();
			    htReverse.put(logicName, names);
			}
            names.add(sOutputName);
		}

		Set<String> setConnectedOutputNames = new HashSet<String>();
		for (ActiveBuffer ab : setABOutputs) {
		    for (String outName : htReverse.get(ab.getName()))
                setConnectedOutputNames.add(htOutputLogicNameMapReverse.get(outName));
		}
		this.saConnectedOutputNames = new String[setConnectedOutputNames.size()];
        setConnectedOutputNames.toArray(this.saConnectedOutputNames);

		for ( String sOut:htOutputMap.keySet() ) {
			String sIn = htOutputMap.get(sOut);
			for ( ActiveBuffer ab:setABOutputs)
				if ( ab.getName().equals(sIn))
					this.htActiveBufferOuputs.put(sOut,ab);
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
	@Override
    public String getPublicResourcesDirectory () {
		return ccCnf.getPublicResourcesDirectory();
	}


	/** Returns the path to the run directory.
	 *
	 * @return Path to public resources directory.
	 */
	@Override
    public String getRunDirectory () {
		return ccCnf.getRunResourcesDirectory();
	}


	/** The name of the available inputs.
	 *
	 * @return The array containing the names
	 */
	@Override
    public String [] getInputNames () {

		return saInputNames;
	}

	/** The name of the available outputs.
	 *
	 * @return The array containing the names
	 */
	@Override
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
	 * @param sInputBuffer The name of the input
	 * @return The data component
	 * @throws ComponentContextException Violation of the component context detected
	 */
	@Override
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
	@Override
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
	@Override
    public boolean isInputAvailable ( String sInputBuffer ) throws ComponentContextException {
		if ( sInputBuffer==null )
			// Unconnected input
			return false;

		if ( !setInputs.contains(sInputBuffer) )
		    throw new ComponentContextException("The requested input "+sInputBuffer+" does not exist.");

		if ( dp.getInput(htInputLogicNameMap.get(sInputBuffer))!=null )
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
	@Override
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
	@Override
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
	@Override
    public void startWebUIFragment ( WebUIFragmentCallback wuiCall ) {
		synchronized ( htWebUIframent ) {
			WebUIFragment wuif = null;
			try {
				wuif = new WebUIFragment(sComponentInstanceID,(ConfigurableWebUIFragmentCallback)wuiCall);
			}
			catch ( ClassCastException e) {
				wuif = new WebUIFragment(sComponentInstanceID, wuiCall);
			}
			webui.addFragment(wuif);
			htWebUIframent.put(wuiCall,wuif);
		}
	}

	/** Stops the web-based user interface.
	 *
	 * @param wuiCall The webui call back object
	 */
	@Override
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
	@Override
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
	@Override
    public URL getWebUIUrl ( boolean bName ) throws ComponentContextException {
		URL urlRes = null;

		try {
			String sHost = (bName)?NetworkTools.getLocalHostName():NetworkTools.getLocalHostIP();
			urlRes = new URL("http://"+sHost+":"+webui.getPort()+ccCnf.getAppContext()+"/");
		} catch (MalformedURLException e) {
			throw new ComponentContextException(e);
		}

		return urlRes;
	}

	/** Gets the proxied webUI URL.
	 *
	 * @param bName True if the url needs to be build using the name.
	 *              False build the URL using the IP address.
	 * @return The proxy webUI URL
	 * @throws ComponentContextException Problem recovering the IP
	 *
	 *
	 */
	@Override
    public URL getProxyWebUIUrl ( boolean bName ) throws ComponentContextException {
		URL urlRes = null;

		try {
			String sHost = (bName)?NetworkTools.getLocalHostName():NetworkTools.getLocalHostIP();
			urlRes = new URL("http://"+sHost+":"+ccCnf.getBasePort()+ccCnf.getAppContext()+"/webui/"+webui.getPort()+"/");
		} catch (MalformedURLException e) {
			throw new ComponentContextException(e);
		}

		return urlRes;
	}

	/** Given a request it returns the proper path base to use.
	 *
	 * @param request The request received
	 * @return The dynamic URL
	 * @throws ComponentContextException The URL could not be generated
	 */
	@Override
    public String getInitialURLPath ( HttpServletRequest request ) throws ComponentContextException {
		URI uri;
		try {
			uri = new URI(request.getRequestURI());
		} catch (URISyntaxException e) {
			throw new ComponentContextException(e);
		}
		if ( uri.getPath().startsWith(ccCnf.getAppContext()+"/webui/") )
			return ccCnf.getAppContext()+"/webui/"+webui.getPort()+"/";
		else
			return ccCnf.getAppContext()+"/";
	}

	/** Returns the logging facility.
	 *
	 * @return The logger object
	 */
	@Override
    public Logger getLogger() {
		return log;
	}

	/** Returns the unique ID of the executable instance for the current execution component.
	 *
	 * @return The unique execution instance ID
	 */
	@Override
    public String getExecutionInstanceID () {
		return sComponentInstanceID;
	}


	/**Returns the uniqueID of the flow  instance for the current flow
	 *	@return The unique flow ID
	 */
	@Override
    public String getFlowExecutionInstanceID() {
		return this.sFlowUniqueExecutionID;
	}

	/**Returns the flowID of the flow
	 *
	 */
	@Override
    public String getFlowID() {
		return this.flowID;
	}

	/** Request the abortion of the flow.
	 *
	 */
	@Override
    public void requestFlowAbortion() {
		wcParent.getMrProper().abort();
		log.warning("Abort requested by component "+wcParent.getExecutableComponentInstanceID());
	}

	/** Returns true if the flow has started a termination request.
	 *
	 * @return True if the flow is aborting
	 */
	@Override
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
	 * @param id The plugin id
	 * @return The Meandre plugin
	 */
	@Override
    public MeandrePlugin getPlugin(String id) {
		PluginFactory  pluginFactory = PluginFactory.getPluginFactory(ccCnf);
		MeandrePlugin mp=pluginFactory.getPlugin(id);
		return mp;
	}

	/** Returns the output console for the flow.
	 *
	 * @return The output console
	 */
	@Override
    public PrintStream getOutputConsole() {
		return console;
	}

	@Override
    public String getInstanceName() {
	    return this.sComponentInstanceName;
	}

	@Override
    public String[] getConnectedInputs() {
	    return this.saConnectedInputNames;
	}

	@Override
    public String[] getConnectedOutputs() {
	    return this.saConnectedOutputNames;
	}

	@Override
    public FiringPolicy getFiringPolicy() {
	    return (wcParent instanceof WrappedComponentAllInputsRequired) ? FiringPolicy.all : FiringPolicy.any;
	}
}
