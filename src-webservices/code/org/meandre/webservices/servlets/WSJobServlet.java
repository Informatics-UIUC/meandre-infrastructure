/**
 * 
 */
package org.meandre.webservices.servlets;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.store.Store;
import org.meandre.webservices.MeandreServer;

/** Provides the servlet that serves Job requests.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSJobServlet extends MeandreBaseServlet {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;
	
	/** Creates the servlet to provide Job information.
	 * 
	 * @param server The Meandre server
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	public WSJobServlet(MeandreServer server, Store store, CoreConfiguration cnf) {
		super(server, store, cnf);
	}

	

}