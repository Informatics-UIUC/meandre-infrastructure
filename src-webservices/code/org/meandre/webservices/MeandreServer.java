package org.meandre.webservices;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.meandre.configuration.CoreConfiguration;
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
 *
 */
public class MeandreServer {

	/** The base URL for Meandre WS */
	public final static String WS_BASE_URL = "http://meandre.org/services/";

	/** The logger for the WebServices */
	private Logger log;

	/** The base directory for Jetty */
	public String MEANDRE_HOME;

	/** The basic handler for all the loggers */
	public Handler handler;

	/** The store to use */
	private Store store;
	
	/** The core configuration to use */
	private CoreConfiguration cnf;

	/** The main Jetty server */
	private Server server;

	/** Creates a Meandre server with the default configuration.
	 * 
	 */
	public MeandreServer () {
		log = WSLoggerFactory.getWSLogger();
		MEANDRE_HOME = ".";
		store = new Store();
		cnf = new CoreConfiguration();
	}
	
	
	/** Creates a Meandre server running on the provided home directory and store.
	 * 
	 * @param sMeandreHome The Meandre home directory
	 * @param config The Meandre core configuration to use
	 * @param storeMS The Meandre store to use
	 */
	public MeandreServer ( String sMeandreHome, CoreConfiguration config, Store storeMS ) {
		log = WSLoggerFactory.getWSLogger();
		MEANDRE_HOME = sMeandreHome;
		store = storeMS;
		cnf = config;
	}

	/** Sets the Meandre core configuration to use for this server.
	 * 
	 * @param config The Meandre core configuration
	 */
	public void setCoreConfiguration ( CoreConfiguration config ) {
		cnf = config;
	}
	
	/** Sets the Meandre store for the given server.
	 * 
	 * @param storeMS The Meandre store to use
	 */
	public void setStore ( Store storeMS ) {
		store = storeMS;
	}


	/** Sets the Meandre home directory.
	 * 
	 * @param sMeandreHome The Meandre home directory
	 */
	public void setMeandreHome ( String sMeandreHome ) {
		MEANDRE_HOME = sMeandreHome;
	}
	
	/**
	 * Run the embedded Jetty server and joins it.
	 *
	 * @throws Exception Jetty could not be started or joined
	 */
	public void start () throws Exception {
		start(true);
	}
	
	/**
	 * Run the embedded Jetty server.
	 * @param bJoin Should the thread joing the server or return
	 *
	 * @throws Exception Jetty could not be started or joined
	 */
	public void start (boolean bJoin) throws Exception {

		server = new Server(cnf.getBasePort());

		// Initialize global file server
		PluginFactory pf = PluginFactory.getPluginFactory(cnf);
		pf.initializeGlobalPublicFileServer(server,log);

		// Initialize the web services
		Context cntxGlobal = initializeTheWebServices(server);
		
		// Initialize the plugins
		pf.initializeGlobalCorePlugins(server,cntxGlobal,log);

		// Launch the server
		server.start();
		if ( bJoin )
			server.join();
	}
	
	/** Joins the main Jetty server.
	 * 
	 * @throws InterruptedException Jetty could not be joined
	 * 
	 */
	public void join () throws InterruptedException {
		server.join();
		
	}
	
	/** Stops the main Jetty server.
	 * 
	 * @throws Exception Jetty could not be stopped
	 * 
	 */
	public void stop () throws Exception {
		server.stop();
	}

	/** Initialize the webservices
	 *
	 * @param server The server object
	 * @return The contexts created
	 * @throws IOException Something went really wrong
	 */
	private Context initializeTheWebServices(Server server)
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
		sJettyHome = (sJettyHome==null)?MEANDRE_HOME:sJettyHome;

		SecurityHandler sh = new SecurityHandler();
		sh.setUserRealm(new HashUserRealm("Meandre Flow Execution Engine",sJettyHome+File.separator+cnf.getRunResourcesDirectory()+File.separator+store.getRealmFilename()));
		sh.setConstraintMappings(new ConstraintMapping[]{cm});

		contextWS.addHandler(sh);

		//
		// Initializing the implementations repository
		//

		//
		// Adding the publicly provided services
		//
		contextWS.addServlet(new ServletHolder((Servlet) new WSPublic(store)), "/public/services/*");

		//
		// Adding restrictedly provided services
		//
		contextWS.addServlet(new ServletHolder((Servlet) new WSAbout(store)), 		"/services/about/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSLocations(store)),	"/services/locations/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSRepository(store,cnf)),	"/services/repository/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSExecute(store,cnf)),		"/services/execute/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSPublish(store)),		"/services/publish/*");
	
		return contextWS;
	}



}
