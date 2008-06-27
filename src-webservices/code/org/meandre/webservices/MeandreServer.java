package org.meandre.webservices;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.Servlet;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.security.Role;
import org.meandre.core.store.Store;
import org.meandre.plugins.PluginFactory;
import org.meandre.webservices.servlets.WSAbout;
import org.meandre.webservices.servlets.WSExecute;
import org.meandre.webservices.servlets.WSLocations;
import org.meandre.webservices.servlets.WSPublic;
import org.meandre.webservices.servlets.WSPublish;
import org.meandre.webservices.servlets.WSRepository;
import org.meandre.webservices.servlets.WSSecurity;
import org.meandre.webservices.utils.WSLoggerFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.management.MBeanContainer;


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

	private Registry registry;
	
	private int REG_PORT = 1099;
	
	private JMXConnectorServer cs =null;
	private  MBeanServer mBeanServer =null;
	private  MBeanContainer mBeanContainer=null;
	
	/** Creates a Meandre server with the default configuration.
	 * 
	 */
	public MeandreServer () {
		log = WSLoggerFactory.getWSLogger();
		MEANDRE_HOME = ".";
		store = new Store();
		cnf = new CoreConfiguration();
	}
	
	/**
	 * creates (or uses) a default installation in the given install dir
	 * running on the given port.  
	 * @param port
	 * @param sInstallDir
	 */
	public MeandreServer(int port, String sInstallDir){
        log = WSLoggerFactory.getWSLogger();
        MEANDRE_HOME = sInstallDir;
        store = new Store(sInstallDir);
        cnf = new CoreConfiguration(port, sInstallDir);	    
	    
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
		startRMIRegistry();
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
	
	/**Start the RMIRegistry used by the JMX server
	 * 
	 */
	private void startRMIRegistry() {
		try {
			registry= LocateRegistry.createRegistry(REG_PORT);
			System.out.println("Starting RMI registry");
		} catch (Exception e) {
			System.out.println("Error creating RMI registry");
			   try {
		            registry = LocateRegistry.getRegistry(REG_PORT);
		        } catch (RemoteException rex) {
		        	System.out.println("Error creating RMI registry");
		            return;
		        }
		}
		
	}

	/** Joins the main Jetty server.
	 * 
	 * @throws InterruptedException Jetty could not be joined
	 * 
	 */
	public void join () throws InterruptedException {
		server.join();
		
	}
	
	/** Stops the main Jetty server and MXBeanServer container
	 * @throws Exception Jetty could not be stopped
	 * 
	 */
	public void stop () throws Exception {
		try {
			cs.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
		server.stop();
		}
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
		
		
		//initialize the roles
		Set<Role> allRoles = Role.getStandardRoles();
		Iterator<Role> allRolesIter = allRoles.iterator();
		String[] rolesAsUrls = new String[allRoles.size()];
		for(int i = 0; i < allRoles.size(); i++){
		    rolesAsUrls[i] = allRolesIter.next().getUrl();
		}
		constraint.setRoles(rolesAsUrls);
		
		
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/services/*");

		String sJettyHome = System.getProperty("jetty.home");
		sJettyHome = (sJettyHome==null)?MEANDRE_HOME:sJettyHome;

		SecurityHandler sh = new SecurityHandler();
		sh.setUserRealm(new HashUserRealm("Meandre Flow Execution Engine",store.getRealmFilename()));
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
		// Start the MBeanServer
		//
		
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
				
	     mBeanServer= ManagementFactory.getPlatformMBeanServer();
		 mBeanContainer = new MBeanContainer(mBeanServer);
		 server.getContainer().addEventListener(mBeanContainer);
		 mBeanContainer.start();
		
		 
		 cs=JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer);


		 cs.start();

		//
		// Adding restrictedly provided services
		//
		contextWS.addServlet(new ServletHolder((Servlet) new WSAbout(store)), 		"/services/about/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSLocations(store,cnf)),	"/services/locations/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSRepository(store,cnf)),	"/services/repository/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSExecute(store,cnf,mBeanServer)),		"/services/execute/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSPublish(store)),		"/services/publish/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSSecurity(store)),		"/services/security/*");
	
		return contextWS;
	}

	/** get the Store this server is using. 
	 *
	 */
	public Store getStore(){
	    return store;
	    
	}


}
