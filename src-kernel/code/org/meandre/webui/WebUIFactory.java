package org.meandre.webui;

import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.MrProper;
import org.meandre.core.logger.KernelLoggerFactory;

/** The web ui factory to manager allows modules to create, register,
 * remove, and distroy web ui compnents.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WebUIFactory {
	
	/** The core root logger */
	protected static Logger log = KernelLoggerFactory.getCoreLogger();
		
	// /** The semaphore to implement mutual exclusion */
	// private static Semaphore semMutEX = new Semaphore(1,true);
	
	// /** The incremental port counter to start execution ports */
	// private static int iPortScroller = -1;
	
	/** The hashtable maintaning the current active webUIs */
	protected static Hashtable<String,WebUI> htActiveWebUI = new Hashtable<String,WebUI>();
	
	/** Get a WebUI for the given executing flow. If the webui does not exist
	 * for the given flow, it gets created and initalized the first 
	 * time it is requrested.
	 * 
	 * @param sFlowUniqueID The flow execution unique ID
	 * @param cnf The core configuration 
	 * @return The webui object
	 * @throws WebUIException An exception occurred while initializing a web
	 */
	static public WebUI getWebUI ( String sFlowUniqueID, MrProper mrProper, MrProbe mrProbe, CoreConfiguration cnf, int port ) throws WebUIException {
		WebUI webui = htActiveWebUI.get(sFlowUniqueID);
		
		if ( webui==null ) {
			webui = new WebUI(sFlowUniqueID,mrProper,mrProbe,port,log,cnf);
			htActiveWebUI.put(sFlowUniqueID, webui);
		}
		
		return webui;
	}
	
	/** Get a WebUI for the given executing flow. If the webui does not exist
	 * returns null.
	 * 
	 * @param sFlowUniqueID The flow execution unique ID
	 * @return The webui object
	 * @throws WebUIException An exception occurred while initializing a web
	 */
	static public WebUI getExistingWebUI ( String sFlowUniqueID ) throws WebUIException {
		WebUI webui = htActiveWebUI.get(sFlowUniqueID);
		
		return webui;
	}


	/** Shuts down all the WebUI for the given executing flow. It
	 * detaches all the handlers registerd by components, and shuts down
	 * the server providing the acces point.
	 * 
	 * @param sFlowUniqueID The flow execution unique ID
	 * @throws WebUIException The server could not be shuted down
	 */
	static public void disposeWebUI (  String sFlowUniqueID ) throws WebUIException {
		WebUI webui = htActiveWebUI.get(sFlowUniqueID);
		
		if ( webui!=null ) {
			try {
				webui.shutdown();
			} catch (Exception e) {
				throw new WebUIException(e);
			}
			htActiveWebUI.remove(sFlowUniqueID);
		}		
	}
	
	/** Returns the set of running flows.
	 * 
	 * @return The set of running flow ids
	 */
	static public Set<String> getFlows () {
		return htActiveWebUI.keySet();
	}
	

}
