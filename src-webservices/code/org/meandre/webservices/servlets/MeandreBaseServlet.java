/**
 * 
 */
package org.meandre.webservices.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.store.Store;
import org.meandre.plugins.PluginFactory;
import org.meandre.webservices.tools.ServletConfigurableDispatcher;


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
	
	/** Creates the base servlet and sets up the access to the required
	 * store and configuration object.
	 * 
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	protected MeandreBaseServlet (Store store, CoreConfiguration cnf) {
		this.store = store;
		this.cnf = cnf;
		this.secStore = store.getSecurityStore();
		this.plugins = PluginFactory.getPluginFactory(cnf);
	}
	
	/** Initialize the servlet. Adds the store varibles to the Python interpreter.
	 * 
	 */
	public void init() throws ServletException {
		super.init();
		configureMeandreEnvironment();
	}
    
	/** Initialize the servlet with the given servlet configuration object.
	 *  Adds the store varibles to the Python interpreter.
	 * 
	 * @param config The servlet configuration object to use
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		configureMeandreEnvironment();
	}

	/** Add the configuration objects to the the Python interpreter.
	 * 
	 */
	private void configureMeandreEnvironment() {
		// Process the script
		process(
				MeandreBaseServlet.class.getResourceAsStream(
						MeandreBaseServlet.class.getSimpleName()+".py"
					)
			);
		// Add the extra global variables
		pi.set("meandre_store", this.store);
		pi.set("meandre_config", this.cnf);
		pi.set("meandre_security", this.secStore);
		pi.set("meandre_plugins", this.plugins);
	}
	
}
