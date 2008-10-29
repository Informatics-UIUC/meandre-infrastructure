/**
 * 
 */
package org.meandre.webservices.servlets;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.store.Store;

/** Provides the servlet that serves auxiliar requests mostly they deal with 
 * form generation for other services.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSAuxiliarServlet extends MeandreBaseServlet {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;
	
	/** Creates the servlet to provide auxiliar information.
	 * 
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	public WSAuxiliarServlet(Store store, CoreConfiguration cnf) {
		super(store, cnf);
	}

	

}
