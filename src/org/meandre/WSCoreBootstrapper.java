package org.meandre;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.eclipse.equinox.servletbridge.BridgeServlet;
import org.meandre.core.engine.MeandreSecurityManager;
import org.meandre.core.store.Store;
import org.meandre.core.store.security.Action;
import org.meandre.webservices.about.WSAbout;
import org.meandre.webservices.execute.WSExecute;
import org.meandre.webservices.locations.WSLocations;
import org.meandre.webservices.pub.WSPublic;
import org.meandre.webservices.publish.WSPublish;
import org.meandre.webservices.repository.WSRepository;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
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
 *
 */
public class WSCoreBootstrapper {

	/** The version */
	public final static String VERSION = "1.1vcli (Carquinyoli)";

	/** The base URL for Meandre WS */
	public final static String WS_BASE_URL = "http://meandre.org/services/";

	/** The base directory for Jetty */
	public static final String JETTY_HOME = ".";

	/** The logger for the bootstrapper */
	private static Logger log = null;

	/** The basic handler for all the loggers */
	public static Handler handler = null;

	// Initializing the logger and its handlers
	static {
		log = Logger.getLogger(WSCoreBootstrapper.class.getName());
		log.setLevel(Level.FINEST);
		try {
			log.addHandler(handler = new FileHandler("meandre-log.xml"));
		} catch (SecurityException e) {
			System.err.println("Could not initialize meandre-log.xml");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize meandre-log.xml");
			System.exit(1);
		}

		handler.setLevel(Level.FINEST);
	}

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

		Server server = new Server(Store.getBasePort());

		// Initialize global file server
		initilizePublicFileServer(server);

		// Initialize the web services
		initializeTheWebServices(server);

		// Launch the server
		server.start();
		server.join();

	}

	/** Initialize the webservices
	 *
	 * @param server The server object
	 * @throws IOException Something went really wrong
	 */
	private static void initializeTheWebServices(Server server)
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
		sh.setUserRealm(new HashUserRealm("Meandre Flow Execution Engine",sJettyHome+File.separator+Store.getRealmFilename()));
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
		/*
		contextWS.addServlet(new ServletHolder((Servlet) new WSAbout()), 		"/services/about/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSLocations()),	"/services/locations/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSRepository()),	"/services/repository/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSExecute()),		"/services/execute/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSPublish()),		"/services/publish/*");
*/
		//
		// OSGi bridge servlet
		//
		contextWS.addServlet(new ServletHolder((Servlet)new BridgeServlet() ),"/plugins/*");

	}

	/** Initialize the public file server for shared resources
	 *
	 * @param server The server to user
	 */
	private static void initilizePublicFileServer(Server server) {
		//
		// Initializing the public file server
		//
		Context contextResources = new Context(server,"/public/resources",Context.NO_SESSIONS);

		File file = new File(Store.getPublicResourceDirectory());

		if ( file.mkdir() ) {
			try {
				PrintStream ps = new PrintStream(new FileOutputStream(file.getAbsolutePath()+File.separator+"readme.txt"));
				ps.println("Meandre Execution Engine version "+WSCoreBootstrapper.VERSION);
				ps.println("All rigths reserved by DITA, NCSA, UofI (2007).");
				ps.println("2007. All rigths reserved by DITA, NCSA, UofI.");
				ps.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
				ps.println();
				ps.println("This directory contains all the publicly available implementations for the Meandre components.");
				ps.println();
				ps.println("Created on "+new Date());
				log.warning("Resource directory not existing. Initializing a new one.");
			} catch (FileNotFoundException e) {
				log.warning("Could not initialize the resource directory");
			}
		}

		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setCacheControl("no-cache");
		resource_handler.setResourceBase(file.getAbsolutePath());
		contextResources.setHandler(resource_handler);
	}



}
