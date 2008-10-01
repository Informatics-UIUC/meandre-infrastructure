package org.meandre.webservices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.security.Role;
import org.meandre.core.services.coordinator.CoordinatorServiceCallBack;
import org.meandre.core.services.coordinator.backend.BackendAdapter;
import org.meandre.core.services.coordinator.backend.BackendAdapterException;
import org.meandre.core.store.Store;
import org.meandre.core.utils.Constants;
import org.meandre.plugins.PluginFactory;
import org.meandre.webservices.logger.WSLoggerFactory;
import org.meandre.webservices.servlets.WSAboutServlet;
import org.meandre.webservices.servlets.WSExecuteServlet;
import org.meandre.webservices.servlets.WSLocationsServlet;
import org.meandre.webservices.servlets.WSPublicServlet;
import org.meandre.webservices.servlets.WSPublishServlet;
import org.meandre.webservices.servlets.WSRepository;
import org.meandre.webservices.servlets.WSSecurityServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.BoundedThreadPool;


/**
 * Bootstraps a Meandre execution engine.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class MeandreServer {

	/** Maximum jetty thread idle time */
	private static final int MAXIMUM_JETTY_THREAD_IDLE_TIME = 3000000;

	/** Maximum number of jetty theads */
	private static final int MAXIMUM_NUMBER_OF_JETTY_THREADS = 256;

	/** Minimum number of jetty threads */
	private static final int MINIMUM_NUMBER_OF_JETTY_THREADS = 6;

	/** The rate at which the realm sync file will be kept synchronized with the store */
	private static final int SECURITY_REALM_SYNC = 20000;

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

	/** The backend adapter linked to this server */
	private BackendAdapter baToStore = null;
	
	/** Should the server be stoped? */
	private boolean bStop = false;
	
	/** Creates a Meandre server with the default configuration.
	 * 
	 */
	public MeandreServer () {
		log = WSLoggerFactory.getWSLogger();
		MEANDRE_HOME = ".";
		
		// Get the core configuration
		File propFileCore = new File(MEANDRE_HOME+File.separator+"meandre-config-core.xml");
		if ( propFileCore.exists() ) {
			Properties propsCore = new Properties();
			try {
				propsCore.loadFromXML(new FileInputStream(propFileCore));
			} catch (FileNotFoundException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				log.warning(baos.toString());
			} catch (IOException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				log.warning(baos.toString());
			}
			cnf = new CoreConfiguration(propsCore);
		}
		else
			cnf = new CoreConfiguration();
		
		// Get the store
		File propFileStore = new File(MEANDRE_HOME+File.separator+"meandre-config-store.xml");
		if ( propFileStore.exists() ) {
			Properties propStore = new Properties();
			try {
				propStore.loadFromXML(new FileInputStream(propFileStore));
			} catch (FileNotFoundException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				log.warning(baos.toString());
			} catch (IOException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				log.warning(baos.toString());
			}
			store = new Store(propStore);
		}
		else
			store = new Store();
		
	}
	
	/** Creates (or uses) a default installation in the given install directory
	 * running on the given port.  
	 * @param port The port
	 * @param sInstallDir The directory
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
		log.info("Starting Meandre Server "+Constants.MEANDRE_VERSION+" ("+Constants.MEANDRE_RELEASE_TAG+")");
		
		server = new Server(cnf.getBasePort());

		// Overwrite the default thread pool
		BoundedThreadPool tp = new BoundedThreadPool();
		tp.setMinThreads(MINIMUM_NUMBER_OF_JETTY_THREADS);
		tp.setMaxThreads(MAXIMUM_NUMBER_OF_JETTY_THREADS);
		tp.setMaxIdleTimeMs(MAXIMUM_JETTY_THREAD_IDLE_TIME);
		server.setThreadPool(tp);
		
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
			join();
	}

	/** Joins the main Jetty server.
	 * 
	 * @throws InterruptedException Jetty could not be joined
	 * 
	 */
	public void join () throws InterruptedException {
		log.info("Joining Meandre Server "+Constants.MEANDRE_VERSION+" ("+Constants.MEANDRE_RELEASE_TAG+")");
		server.join();
		
	}
	
	/** Stops the main Jetty server and MXBeanServer container
	 * @throws Exception Jetty could not be stopped
	 * 
	 */
	public void stop () throws Exception {
		log.info("Stoping Meandre Server "+Constants.MEANDRE_VERSION+" ("+Constants.MEANDRE_RELEASE_TAG+")");
		bStop  = true;
		baToStore.close();
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

		// Setup the security sync service
		final SecurityHandler sh = new SecurityHandler();
		HashUserRealm hur = new HashUserRealm("Meandre Flow Execution Engine",store.getRealmFilename());
		sh.setUserRealm(hur);
		sh.setConstraintMappings(new ConstraintMapping[]{cm});

		// Start the security service sync between meandre and jetty
		fireSecuritySyncService(sh);
		
		// Register the server to the back end
		registerAndFireBackendAdapter();
	
		contextWS.addHandler(sh);

		//
		// Initializing the implementations repository
		//

		//
		// Adding the publicly provided services
		//
		contextWS.addServlet(new ServletHolder((Servlet) new WSPublicServlet(store,cnf)), "/public/services/*");
		
		//
		// Adding restrictedly provided services
		//
		contextWS.addServlet(new ServletHolder((Servlet) new WSAboutServlet(store,cnf)), 		"/services/about/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSLocationsServlet(store,cnf)),	"/services/locations/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSRepository(store,cnf)),	"/services/repository/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSExecuteServlet(store,cnf)),		"/services/execute/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSPublishServlet(store,cnf)),		"/services/publish/*");
		contextWS.addServlet(new ServletHolder((Servlet) new WSSecurityServlet(store,cnf)),		"/services/security/*");
		
		return contextWS;
	}

	/** Fires the Security Sync Service. This services syncs the meandre-realm.xml used 
	 * by the Jetty authentication to the Meandre security information.
	 * 
	 * @param sh The security handle	
	 */
	private void fireSecuritySyncService(final SecurityHandler sh) {
		// Force the refresh of the realm
		bStop = false;
		new Thread (
				new Runnable () {

					public void run() {
						while (!bStop ) {
							try {
								HashUserRealm hur = new HashUserRealm("Meandre Flow Execution Engine",store.getRealmFilename());
								sh.setUserRealm(hur);
								Thread.sleep(SECURITY_REALM_SYNC);
							} catch (InterruptedException e) {
								log.warning(e.toString());
							} catch (IOException e) {
								log.warning(e.toString());
							}
						}						
					}					
				}
			).start();
	}

	/** Registers and fires the backend adapter.
	 * 
	 */
	private void registerAndFireBackendAdapter() {
		// Instantiate the adaptor
		try {
			baToStore = (BackendAdapter) Class.forName(
					"org.meandre.core.services.coordinator.backend."+store.getDatabaseFlavor()+"BackendAdapter"
				).newInstance();
			
			// Link it to a store
			baToStore.linkToService(store.getConnectionToDB(),cnf.getBasePort(), new CoordinatorServiceCallBack() {

				public String getDescription() {
					return "Meandre Server "+Constants.MEANDRE_VERSION;
				}

				public boolean ping(String sIP,int iPort) {
					String sURL = "http://"+sIP+":"+iPort+"/public/services/ping.txt";
					try {
						URL url = new URL(sURL);
						LineNumberReader lnr = new LineNumberReader(new InputStreamReader(url.openStream()));
						
						boolean bRes = false;
						if ( lnr.readLine().equals("Pong"))
							bRes =  true;
						
						return bRes;
					} catch (MalformedURLException e) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(baos));
						log.fine(baToStore.getName()+" found a malformed URL "+sURL);
						return false;
					} catch (IOException e) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(baos));
						log.fine(baToStore.getName()+" could not ping "+sIP+" running at port "+iPort);
						return false;
					}
				}
				
			});
			
			// Create the backend schema if needed
			baToStore.createSchema();
			
			// Start the service
			baToStore.start();
			
		} catch (InstantiationException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Backend adapter could not be instantiated for flavor "+store.getDatabaseFlavor());
		} catch (IllegalAccessException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Backend adapter could not be reached for flavor "+store.getDatabaseFlavor());
		} catch (ClassNotFoundException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Unknow required back end adapter for flavor "+store.getDatabaseFlavor());
		} catch (BackendAdapterException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Backend adaptor could not create the default schema for the "+store.getDatabaseFlavor()+" backend flavor");
		}
		
		
		
	}


	/** get the Store this server is using. 
	 *
	 */
	public Store getStore(){
	    return store;
	    
	}


}
