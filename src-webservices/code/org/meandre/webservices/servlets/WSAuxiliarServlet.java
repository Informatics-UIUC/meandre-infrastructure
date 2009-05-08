/**
 * 
 */
package org.meandre.webservices.servlets;

import javax.servlet.ServletException;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.store.Store;
import org.meandre.webservices.MeandreServer;

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
	 * @param server The Meandre server
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	public WSAuxiliarServlet(MeandreServer server, Store store, CoreConfiguration cnf) {
		super(server, store, cnf);
	}

	/** Initialize the servlet. Creates an instance of the python interpreter.
	 * 
	 */
	public void init() throws ServletException {
		super.init();
		pi.set("meandre_config", this.cnf);
	}
}
