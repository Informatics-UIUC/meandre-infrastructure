package org.meandre.webservices.about;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
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
import org.meandre.plugins.monk.DataStoreInitializeServlet;
import org.meandre.plugins.monk.ResultReaderServlet;
import org.meandre.plugins.proxy.HttpProxyServlet;
import org.meandre.webservices.servlets.WSAbout;
import org.meandre.webservices.servlets.WSExecute;
import org.meandre.webservices.servlets.WSLocations;
import org.meandre.webservices.servlets.WSPublic;
import org.meandre.webservices.servlets.WSPublish;
import org.meandre.webservices.servlets.WSRepository;
import org.meandre.webservices.utils.WSLoggerFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;


/**This class tests the WSAbout web service
 *
 * @author Amit Kumar
 * Created on Feb 22, 2008 3:50:22 AM
 * Modified by Xavier Llor&agrave;
 *
 */

public class WSAboutTest {


	/** The base URL for Meandre WS */
	public final static String WS_BASE_URL = "http://meandre.org/services/";

	/** The base directory for Jetty */
	public static final String JETTY_HOME = ".";

	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();

	/** The basic handler for all the loggers */
	public static Handler handler = null;



	String meandreHostURL=null;
	String user =null;
	String password =null;

	String INSTALLATION_URL = "services/about/installation";
	String INSTALLATION_TEST_STRING_TXT = "Meandre Execution Engine version";
	String INSTALLATION_TEST_STRING_TTL = "@prefix meandreWS:  <http://meandre.org/services/>";
	String INSTALLATION_TEST_STRING_NT = "<http://meandre.org/services/>";
	String INSTALLATION_TEST_STRING_RDF = "<rdf:RDF";


	String USER_ROLES_URL = "services/about/user_roles";
	String USER_ROLES_TEST_STRING_TXT = "http://www.meandre.org/accounting/property/action/";
	String USER_ROLES_TEST_STRING_JSON = "{\"meandre_user_role";
	String USER_ROLES_TEST_STRING_XML_TAG_1 = "meandre_security";
	String USER_ROLES_TEST_STRING_XML_TAG_2 ="meandre_user_role";





	WebConversation wc = new WebConversation();

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
    	 Store.setSConfigPath("test-data");
 		// initialize store -reads properties file
 		Store.init();

 		Server server = new Server(Store.getBasePort());

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
		// TODO Auto-generated method stub

	}

	@Before
	public void setUp() throws Exception {
		Properties properties =  new Properties();
		properties.load(new FileInputStream(new File("test-data/webservices.properties")));
		meandreHostURL = properties.getProperty("meandreHostURL", "http://127.0.0.1:1714/");
		user = properties.getProperty("user", "admin");
		password = properties.getProperty("password", "admin");
		wc.setAuthorization(user, password);

	}

	@After
	public void tearDown() throws Exception {
		wc.clearContents();
	}

	@Test
	public void testInstallationTxtHttpServletRequestHttpServletResponse() {
		String format = "txt";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ INSTALLATION_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		}
		try {
			assertEquals(response.getText().startsWith(
					INSTALLATION_TEST_STRING_TXT), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void testInstallationTTLHttpServletRequestHttpServletResponse() {
		String format = "ttl";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ INSTALLATION_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		}
		try {
			assertEquals(response.getText().startsWith(
					INSTALLATION_TEST_STRING_TTL), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}

	}

	@Test
	public void testInstallationNTHttpServletRequestHttpServletResponse() {
		String format = "nt";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ INSTALLATION_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		}
		try {
			assertEquals(response.getText().startsWith(
					INSTALLATION_TEST_STRING_NT), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void testInstallationRDFHttpServletRequestHttpServletResponse() {
		String format = "rdf";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ INSTALLATION_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		}
		System.out.println(response.getContentType());
		try {
			assertEquals(response.getContentType(), "application/xml");
			assertEquals(response.getText().startsWith(
					INSTALLATION_TEST_STRING_RDF), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}
	}


	@Test
	public void testUserXMLHttpServletRequestHttpServletResponse() {
		String format = "xml";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ USER_ROLES_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		}
		try {
			//assertEquals(response.getContentType(), "text/xml");
			assertEquals(response.getDOM().getElementsByTagName(USER_ROLES_TEST_STRING_XML_TAG_1).getLength(),1);
			assertTrue("The user roles == 0",response.getDOM().getElementsByTagName(USER_ROLES_TEST_STRING_XML_TAG_2).getLength()>0);
		} catch (SAXException e) {
			fail(e.toString());
		}
	}




	@Test
	public void testUserTXTHttpServletRequestHttpServletResponse() {
		String format = "txt";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ USER_ROLES_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		}
		try {
			assertEquals(response.getContentType(), "text/plain");
			assertTrue("Error: in user_roles.txt",response.getText().startsWith(USER_ROLES_TEST_STRING_TXT));
		} catch (IOException e) {
			fail(e.toString());
		}
	}


	@Test
	public void testUserJSONHttpServletRequestHttpServletResponse() {
		String format = "json";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ USER_ROLES_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		}
		try {
			assertEquals(response.getContentType(), "text/html");
			assertTrue("Error: in user_roles.json",response.getText().startsWith(USER_ROLES_TEST_STRING_JSON));
		} catch (IOException e) {
			fail(e.toString());
		}
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



}
