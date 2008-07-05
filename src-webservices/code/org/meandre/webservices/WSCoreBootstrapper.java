package org.meandre.webservices;

import java.util.logging.Handler;
import java.util.logging.Logger;

import org.meandre.webservices.utils.WSLoggerFactory;


/**
 * Bootstraps a Meandre execution engine.
 *
 * @author Xavier Llor&agrave;
 * @modified Amit Kumar: Added Security Manager
 * @modified Amit Kumar: Added plugins.
 *
 */
public class WSCoreBootstrapper {

	/** The base URL for Meandre WS */
	public final static String WS_BASE_URL = "http://meandre.org/services/";

	/** The base directory for Jetty */
	public static final String JETTY_HOME = ".";

	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();

	/** The basic handler for all the loggers */
	public static Handler handler = null;

	/**
	 * Boostraps the Meandre execution engine.
	 *
	 * @param args
	 *            Command line arguments
	 * @throws Exception
	 *             Something went wrong, really wrong.
	 */
	public static void main(String[] args) throws Exception {
		log.config("Bootstrapping Menadre Workflow Engine");

		log.config("Installing MeandreSecurityManager");
		
		// Had to disable to allow the JMX thing to work
//		if( System.getSecurityManager() == null )
//		    System.setSecurityManager( new MeandreSecurityManager() );
		
		log.config("Starting Meandre server");
		MeandreServer ms = new MeandreServer();
		ms.start();
	}



	


}
