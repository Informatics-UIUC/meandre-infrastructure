/**
 * 
 */
package org.meandre.webservices.servlets;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.store.Store;
import org.meandre.webservices.MeandreServer;

/** Provides the servlet that allows to see the server logs.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSLogsServlet extends MeandreBaseServlet {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;
	
	/** Creates the servlet to provide log information.
	 * 
	 * @param server The Meandre server
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	public WSLogsServlet(MeandreServer server, Store store, CoreConfiguration cnf) {
		super(server,store, cnf);
	}


	//public final static syncronized 
	

}
