package org.meandre.webservices;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.meandre.core.engine.MeandreSecurityManager;
import org.meandre.core.store.Store;
import org.meandre.core.store.security.Action;
import org.meandre.plugins.PluginFactory;
import org.meandre.webservices.servlets.WSAbout;
import org.meandre.webservices.servlets.WSExecute;
import org.meandre.webservices.servlets.WSLocations;
import org.meandre.webservices.servlets.WSPublic;
import org.meandre.webservices.servlets.WSPublish;
import org.meandre.webservices.servlets.WSRepository;
import org.meandre.webservices.utils.WSLoggerFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;


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
		if( System.getSecurityManager() == null )
		    System.setSecurityManager( new MeandreSecurityManager() );

		log.config("Starting Jetty server");
		startEmbeddedJetty();

	}

	/**
	 * Run the embedded Jetty server.
	 *
	 * @throws Exception
	 *             Jetty could not be started
	 */
	private static void startEmbeddedJetty() throws Exception {

		Store.setSConfigPath(".");
		// initialize store -reads properties file
		Store.init();

		Server server = new Server(Store.getBasePort());

		// Initialize global file server
		PluginFactory.initializeGlobalPublicFileServer(server,log);

		// Initialize the web services
		Context cntxGlobal = initializeTheWebServices(server);
		
		// Initialize the plugins
		PluginFactory.initializeGlobalCorePlugins(server,cntxGlobal,log);

		// Launch the server
		server.start();
		server.join();

	}

	/** Initialize the webservices
	 *
	 * @param server The server object
	 * @return The contexts created
	 * @throws IOException Something went really wrong
	 */
	private static Context initializeTheWebServices(Server server)
			throws IOException {
		//
		// Initializing the web services
		//
		Context contextWS = new Context(server,"/",Context.SESSIONS);

		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__BASIC_AUTH);
		//constraint.setRoles(new String[]{"user","admin","moderator"});
		constraint.setRoles(Action.ALL_BASIC_ACTION_URLS);
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/services/*");

		String sJettyHome = System.getProperty("jetty.home");
		sJettyHome = (sJettyHome==null)?JETTY_HOME:sJettyHome;

		SecurityHandler sh = new SecurityHandler();
		sh.setUserRealm(new HashUserRealm("Meandre Flow Execution Engine",sJettyHome+File.separator+Store.getRunResourcesDirectory()+File.separator+Store.getRealmFilename()));
		sh.setConstraintMappings(new ConstraintMapping[]{cm});

		contextWS.addHandler(sh);

		//
		// Initializing the implementations repository
		//

		//
		// Adding the publicly provided services
		//
		contextWS.addServlet(new ServletHolder((Servlet) new WSPublic()), "/public/services/*");

		//
		// Adding restrictedly provided services
		//
		contextWS.addServlet(new ServletHolder((Servlet) new WSAbout()), 		"/services/about/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSLocations()),	"/services/locations/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSRepository()),	"/services/repository/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSExecute()),		"/services/execute/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSPublish()),		"/services/publish/*");
	
		return contextWS;
	}



}
