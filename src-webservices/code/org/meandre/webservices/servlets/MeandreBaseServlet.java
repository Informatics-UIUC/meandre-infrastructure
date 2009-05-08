/**
 * 
 */
package org.meandre.webservices.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.services.coordinator.backend.CoordinatorBackendAdapter;
import org.meandre.core.store.Store;
import org.meandre.plugins.PluginFactory;
import org.meandre.webservices.MeandreServer;
import org.meandre.webservices.logger.WSLoggerFactory;
import org.meandre.webservices.tools.ServletConfigurableDispatcher;
import org.python.core.PyDictionary;


/** This class is the base class that adds Meandre related information to the
 * ServletConfigurableDispatcher base class. All the Meandre WS servlets extends
 * this class.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class MeandreBaseServlet 
extends ServletConfigurableDispatcher {

	/** A default serial version number */
	private static final long serialVersionUID = 1L;

	/** The Meandre store in use */
	protected Store store;
	
	/** The Meandre core configuration object */
	protected CoreConfiguration cnf;
	
	/** The Meandre security store available */
	protected SecurityManager secStore;

	/** The Meandre plugin factory */
	protected PluginFactory plugins;
	
	/** The back end adaptor to the shared store. */
	protected CoordinatorBackendAdapter backendAdaptor;

	/** The parent Meandre server */
	private MeandreServer server;
	
	/** Creates the base servlet and sets up the access to the required
	 * store and configuration object.
	 * 
	 * @param server The Meandre server
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	protected MeandreBaseServlet (MeandreServer server, Store store, CoreConfiguration cnf) {
		this.server = server;
		this.store = store;
		this.cnf = cnf;
		this.secStore = store.getSecurityStore();
		this.plugins = PluginFactory.getPluginFactory(cnf);
	}
	
	/** Initialize the servlet. Adds the store varibles to the Python interpreter.
	 * 
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		configureMeandreEnvironment();
	}
    
	/** Initialize the servlet with the given servlet configuration object.
	 *  Adds the store varibles to the Python interpreter.
	 * 
	 * @param config The servlet configuration object to use
	 */
//	@Override
//	public void init(ServletConfig config) throws ServletException {
//		super.init(config);
//		configureMeandreEnvironment();
//	}

	/** Add the configuration objects to the the Python interpreter.
	 * 
	 */
	private void configureMeandreEnvironment() {
		
		WSLoggerFactory.getWSLogger().info("Initializing service "+this.getClass().getSimpleName());
		// Add the extra global variables
		pi.set("meandre_server", this.server);
		pi.set("meandre_store", this.store);
		pi.set("meandre_config", this.cnf);
		pi.set("meandre_security", this.secStore);
		pi.set("meandre_plugins", this.plugins);
		if (this.backendAdaptor!=null )
			pi.set("meandre_coordinator", this.backendAdaptor);
		
		// Process the script
		process(
				MeandreBaseServlet.class.getResourceAsStream(
						MeandreBaseServlet.class.getSimpleName()+".py"
					)
			);
		
		
		
	}
	
}
