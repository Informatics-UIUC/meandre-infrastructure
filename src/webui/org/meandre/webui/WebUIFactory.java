package org.meandre.webui;

import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.WSCoreBootstrapper;
import org.meandre.core.store.Store;

/** The web ui factory to manager allows modules to create, register,
 * remove, and distroy web ui compnents.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WebUIFactory {
	
	/** The logger for the bootstrapper */
	protected static Logger log = null;
	
	// Initializing the logger and its handlers
	static {
		log = Logger.getLogger(WSCoreBootstrapper.class.getName());
		log.setLevel(Level.CONFIG);
		log.addHandler(WSCoreBootstrapper.handler);
	}
		
	/** The semaphore to implement mutual exclusion */
	private static Semaphore semMutEX = new Semaphore(1,true);
	
	/** The incremental port counter to start execution ports */
	private static int iPortScroller = Store.getBasePort();
	
	/** The hashtable maintaning the current active webUIs */
	protected static Hashtable<String,WebUI> htActiveWebUI = new Hashtable<String,WebUI>();
	
	/** Get a WebUI for the given executing flow. If the webui does not exist
	 * for the given flow, it gets created and initalized the first 
	 * time it is requrested.
	 * 
	 * @param sFlowUniqueID The flow execution unique ID
	 * @return The webui object
	 * @throws WebUIException An exception occurred while initializing a web
	 */
	static public WebUI getWebUI ( String sFlowUniqueID ) throws WebUIException {
		WebUI webui = htActiveWebUI.get(sFlowUniqueID);
		
		if ( webui==null ) {
			try {
				semMutEX.acquire();
				int iNewPort = ++iPortScroller;
				semMutEX.release();		
				webui = new WebUI(sFlowUniqueID,iNewPort);
				htActiveWebUI.put(sFlowUniqueID, webui);
			} catch (InterruptedException e) {
				throw new WebUIException(e);
			}
			
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
	
//	/** A main for testing WebUIs.
//	 * 
//	 * @param sArgs The command line arguments
//	 * @throws Exception Something went wrong
//	 */
//	static public void main ( String [] sArgs ) throws Exception {
//		WebUI webui = WebUIFactory.getWebUI("potato-flow");
//		
//		Thread.sleep(10000);
//		System.out.println("Adding hellow world");
//		WebUIFragment wuif = new WebUIFragment("potato-hw-1", new WebUIHelloWorldFragment("potato-hw-1","Hello world!"));
//		webui.addFragment(wuif);
//		
//		Thread.sleep(10000);
//		System.out.println("Adding hellow world 2");
//		WebUIFragment wuif2 = new WebUIFragment("potato-hw-2", new WebUIHelloWorldFragment("potato-hw-2","Hello world again!"));
//		webui.addFragment(wuif2);
//		
//		Thread.sleep(10000);
//		System.out.println("Removing hello world ");
//		webui.removeFragment(wuif);
//		
//		Thread.sleep(10000);
//		System.out.println("Removing hello world 2 ");
//		webui.removeFragment(wuif2);
//		
//		Thread.sleep(10000);
//		System.out.println("Shutting down");
//		
//		webui.shutdown();
//	}
}
