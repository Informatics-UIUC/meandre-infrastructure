/**
 * 
 */
package org.meandre.webservices.servlets;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.store.Store;

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
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	public WSJobServlet(Store store, CoreConfiguration cnf) {
		super(store, cnf);
	}

	

}
