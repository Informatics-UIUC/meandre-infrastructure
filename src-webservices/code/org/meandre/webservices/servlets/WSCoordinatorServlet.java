/**
 * 
 */
package org.meandre.webservices.servlets;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.services.coordinator.backend.CoordinatorBackendAdapter;
import org.meandre.core.store.Store;
import org.meandre.webservices.MeandreServer;

/** Provides the servlet that serves Execute requests.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSCoordinatorServlet extends MeandreBaseServlet {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Creates the servlet to provide Coordinator information.
	 * 
	 * @param server The Meandre server
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 * @param baToStore 
	 */
	public WSCoordinatorServlet(MeandreServer server, Store store, CoreConfiguration cnf, CoordinatorBackendAdapter baToStore) {
		super(server, store, cnf);
		this.backendAdaptor = baToStore;
	}
	
}