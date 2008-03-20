package org.meandre.webservices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.core.store.Store;
import org.meandre.core.store.security.Action;
import org.meandre.core.utils.Constants;
import org.meandre.webservices.servlets.WSAbout;
import org.meandre.webservices.servlets.WSExecute;
import org.meandre.webservices.servlets.WSLocations;
import org.meandre.webservices.servlets.WSPublic;
import org.meandre.webservices.servlets.WSPublish;
import org.meandre.webservices.servlets.WSRepository;
import org.meandre.webservices.utils.WSLoggerFactory;
import org.mortbay.jetty.Server;
/*
 * @(#) BaseServletTest.java @VERSION@
 *
 * Copyright (c) 2008+ Amit Kumar
 *
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.meterware.httpunit.WebConversation;

/** This class needs to be extended in order to test the Servlets.
 *
 * @author Amit Kumar
 * Created on Feb 23, 2008 7:22:21 PM
 *
 */
public abstract class BaseServletTest {

	/** The base directory for Jetty */
	public static final String JETTY_HOME = ".";

	String meandreHostURL=null;
	String user =null;
	String password =null;
	WebConversation wc = new WebConversation();



	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();

	/** The basic handler for all the loggers */
	public static Handler handler = null;

	private static Server server;



	 @BeforeClass
     public static void oneTimeSetUp() {
		try {
			startJettyServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }

     private static void startJettyServer() throws Exception {
    	 Store.setSConfigPath("test/meandre");
 		// initialize store -reads properties file
 		Store.init();

 		server = new Server(Store.getBasePort());

 		// Initialize global file server
 		initilizePublicFileServer(server);

 		// Initialize the web services
 		initializeTheWebServices(server);

 		// Launch the server
 		server.start();


	}

	@AfterClass
     public static void oneTimeTearDown() {
    	 stopJettyServer();
     }

	private static void stopJettyServer() {
		try {
			server.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	@Test
	public void testMe(){

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

		File file = new File(Store.getPublicResourcesDirectory());

		if ( file.mkdir() ) {
			try {
				PrintStream ps = new PrintStream(new FileOutputStream(file.getAbsolutePath()+File.separator+"readme.txt"));
				ps.println("Meandre Execution Engine version "+Constants.MEANDRE_VERSION);
				ps.println("All rigths reserved by DITA, NCSA, UofI (2007).");
				ps.println("2007. All rigths reserved by DITA, NCSA, UofI.");
				ps.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
				ps.println();
				ps.println("This directory contains all the publicly available implementations for the Meandre components.");
				ps.println();
				ps.println("Created on "+new Date());
				System.out.println("Resource directory not existing. Initializing a new one.");
			} catch (FileNotFoundException e) {
				System.out.println("Could not initialize the resource directory");
			}
		}

		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setCacheControl("no-cache");
		resource_handler.setResourceBase(file.getAbsolutePath());
		contextResources.setHandler(resource_handler);
	}




	@Before
	public void setUp() throws Exception {
		Properties properties =  new Properties();
		properties.load(new FileInputStream(new File("test/meandre/webservices.properties")));
		meandreHostURL = properties.getProperty("meandreHostURL", "http://127.0.0.1:1711/");
		user = properties.getProperty("user", "admin");
		password = properties.getProperty("password", "admin");
		wc.setAuthorization(user, password);

	}

	@After
	public void tearDown() throws Exception {
		wc.clearContents();
	}



	/**
	 *
	 * @returns WebConversation
	 */
	public WebConversation getWebConversation(){
		return wc;
	}


	/**Returns the meandre host url
	 *
	 * @return
	 */
	public String getMeandreHostUrl(){
		return this.meandreHostURL;
	}


	/**Return the Logger object
	 *
	 * @return
	 */
	public Logger getLogger(){
		return log;
	}

}
