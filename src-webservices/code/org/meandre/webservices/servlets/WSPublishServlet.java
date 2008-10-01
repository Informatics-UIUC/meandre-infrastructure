/**
 * 
 */
package org.meandre.webservices.servlets;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.store.Store;

/** Provides the servlet that serves Publish requests.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSPublishServlet extends MeandreBaseServlet {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;
	
	/** Creates the servlet to provide Publish information.
	 * 
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	public WSPublishServlet(Store store, CoreConfiguration cnf) {
		super(store, cnf);
	}

	

}
