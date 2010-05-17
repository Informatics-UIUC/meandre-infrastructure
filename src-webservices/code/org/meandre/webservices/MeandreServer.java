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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.security.Role;
import org.meandre.core.services.coordinator.CoordinatorServiceCallBack;
import org.meandre.core.services.coordinator.backend.CoordinatorBackendAdapter;
import org.meandre.core.services.coordinator.backend.CoordinatorBackendAdapterException;
import org.meandre.core.store.Store;
import org.meandre.core.utils.Constants;
import org.meandre.core.utils.FileUtil;
import org.meandre.plugins.PluginFactory;
import org.meandre.webservices.logger.WSLoggerFactory;
import org.meandre.webservices.servlets.WSAboutServlet;
import org.meandre.webservices.servlets.WSAuxiliarServlet;
import org.meandre.webservices.servlets.WSCoordinatorServlet;
import org.meandre.webservices.servlets.WSExecuteServlet;
import org.meandre.webservices.servlets.WSJobServlet;
import org.meandre.webservices.servlets.WSLocationsServlet;
import org.meandre.webservices.servlets.WSLogsServlet;
import org.meandre.webservices.servlets.WSPublicServlet;
import org.meandre.webservices.servlets.WSPublishServlet;
import org.meandre.webservices.servlets.WSRepositoryServlet;
import org.meandre.webservices.servlets.WSSecurityServlet;
import org.meandre.webservices.servlets.WSServerServlet;
import org.meandre.webservices.webuiproxy.WebUIProxy;
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
	private static final int MAXIMUM_JETTY_THREAD_IDLE_TIME = 300000;

	/** Maximum number of jetty theads */
	private static final int MAXIMUM_NUMBER_OF_JETTY_THREADS = 256;

	/** Minimum number of jetty threads */
	private static final int MINIMUM_NUMBER_OF_JETTY_THREADS = 6;

	/** The rate at which the realm sync file will be kept synchronized with the store */
	private static final int SECURITY_REALM_SYNC = 300000;

	/** The base URL for Meandre WS */
	public final static String WS_BASE_URL = "http://meandre.org/services/";

	/** The logger for the WebServices */
	private final Logger log;

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
	private CoordinatorBackendAdapter baToStore = null;

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
			store = new Store(propStore,cnf);
		}
		else
			store = new Store(cnf);

		cnf.initializeLogging();
	}

	/** Creates (or uses) a default installation in the given install directory
	 * running on the given port.
	 * @param port The port
	 * @param sInstallDir The directory
	 */
	public MeandreServer(int port, String sInstallDir) {
		log = WSLoggerFactory.getWSLogger();
		MEANDRE_HOME = sInstallDir;
		cnf = new CoreConfiguration(port, sInstallDir);
		cnf.initializeLogging();
		store = new Store(sInstallDir,cnf);
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
		cnf.initializeLogging();
	}

	/** Sets the Meandre core configuration to use for this server.
	 *
	 * @param config The Meandre core configuration
	 */
	public void setCoreConfiguration ( CoreConfiguration config ) {
		cnf = config;
		cnf.initializeLogging();
	}

	public void setGlobalLoggingLevel(Level kernelLogLevel, Level wsLogLevel) {
	    cnf.setGlobalLoggingLevel(kernelLogLevel, wsLogLevel);
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
		log.info("Starting the WS endpoint...");
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
		PluginFactory.initializeGlobalPublicFileServer(server,log,cnf);

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
		String sCntx = cnf.getAppContext();
		log.info("Joining Meandre Server "+Constants.MEANDRE_VERSION+" ("+Constants.MEANDRE_RELEASE_TAG+") on context <"+((sCntx.length()==0)?"/":sCntx)+">");
		server.join();

	}

	/** Stops the main Jetty server
	 * @throws Exception Jetty could not be stopped
	 *
	 */
	public void stop () throws Exception {
		log.info("Stoping Meandre Server "+Constants.MEANDRE_VERSION+" ("+Constants.MEANDRE_RELEASE_TAG+")");
		bStop  = true;
		baToStore.close();
		store.getJobInformation().close();
		server.stop();
	}

	/** Stops the main Jetty server with a certain delay
	 *
	 * @param iDelay
	 * @throws Exception Jetty could not be stopped
	 *
	 */
	public void delayedStop (final int iDelay) throws Exception {

		log.info("Stoping Meandre Server "+Constants.MEANDRE_VERSION+" ("+Constants.MEANDRE_RELEASE_TAG+")");
		bStop  = true;
		baToStore.close();
		store.getJobInformation().close();

		// Spawns a thread for the delayed stop
		Thread th = new Thread(new Runnable(){

			public void run() {

				try {
					Thread.sleep(iDelay);
					server.stop();
					log.info("Forcing abrupt shutdown of non finilized flows");
					System.exit(0);
				} catch (Exception e) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(baos);
					e.printStackTrace(ps);
					log.warning("Problem arised while shutting down the server!!!\n"+baos.toString());
				}
			}

		});
		th.start();
	}

	/** Initialize the webservices
	 *
	 * @param server The server object
	 * @return The contexts created
	 * @throws IOException Something went really wrong
	 */
	private Context initializeTheWebServices(Server server)
	throws IOException {

		String sCntx = cnf.getAppContext();

		// Install the WebUI proxy before any other handler
		server.addHandler(new WebUIProxy(cnf));

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
		cm.setPathSpec(sCntx+"/services/*");

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
		contextWS.addServlet(new ServletHolder(new WSPublicServlet(this,store,cnf)), sCntx+"/public/services/*");

		//
		// Adding restrictedly provided services
		//
		contextWS.addServlet(new ServletHolder(new WSAboutServlet(this,store,cnf)), 		sCntx+"/services/about/*");
		contextWS.addServlet(new ServletHolder(new WSLocationsServlet(this,store,cnf)),	sCntx+"/services/locations/*");
		contextWS.addServlet(new ServletHolder(new WSRepositoryServlet(this,store,cnf)),	sCntx+"/services/repository/*");
		contextWS.addServlet(new ServletHolder(new WSExecuteServlet(this,store,cnf)),		sCntx+"/services/execute/*");
		contextWS.addServlet(new ServletHolder(new WSPublishServlet(this,store,cnf)),		sCntx+"/services/publish/*");
		contextWS.addServlet(new ServletHolder(new WSSecurityServlet(this,store,cnf)),		sCntx+"/services/security/*");
		contextWS.addServlet(new ServletHolder(new WSCoordinatorServlet(this,store,cnf,baToStore)),		sCntx+"/services/coordinator/*");
		contextWS.addServlet(new ServletHolder(new WSJobServlet(this,store,cnf)),		sCntx+"/services/jobs/*");
		contextWS.addServlet(new ServletHolder(new WSLogsServlet(this,store,cnf)),		sCntx+"/services/logs/*");
		contextWS.addServlet(new ServletHolder(new WSServerServlet(this,store,cnf)),		sCntx+"/services/server/*");

		contextWS.addServlet(new ServletHolder(new WSAuxiliarServlet(this,store,cnf)),     sCntx+"/services/auxiliar/*");

		contextWS.setErrorHandler(new MeandreDefaultErrorHandler(cnf));


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
//								HashUserRealm hur = (HashUserRealm) sh.getUserRealm();
//								hur.setConfig(store.getRealmFilename());
								sh.setUserRealm(new HashUserRealm("Meandre Flow Execution Engine",store.getRealmFilename()));
								Thread.sleep(SECURITY_REALM_SYNC);
							} catch (Exception e) {
								log.warning("Security realm sync service:"+e.toString());
							} catch (Throwable t) {
								log.warning("Security realm sync service:"+t.toString());
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
			baToStore = (CoordinatorBackendAdapter) Class.forName(
					"org.meandre.core.services.coordinator.backend."+store.getDatabaseFlavor()+"CoordinatorBackendAdapter"
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
		} catch (CoordinatorBackendAdapterException e) {
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

	/**
	 * deletes all files on disk that are used by this server, it's store,
	 * repository, etc. This server must be stopped using the 'stop()' method
	 * before the uninstallation can be done.
	 *
	 * @throws IOException
	 */
	public static void uninstall(File installationDir) throws IOException{
		Set<File> installedFiles = getInstallationFiles(installationDir);
		WSLoggerFactory.getWSLogger().info(
				"Uninstalling Meandre files in directory: \'" +
				installationDir + "\'");
		for(File file: installedFiles){
			if(file.exists()){
				if(file.isDirectory()){
					FileUtil.deleteDirRecursive(file);
				}else{
					file.delete();
				}
			}
		}
	}

	/**
	 * retrieves a list of files that are generated by a MeandreServer by
	 * default. this list is actually static, and may not be complete if
	 * changes are made to objects MeandreServer uses but this list isn't
	 * updated.
	 */
	public static Set<File> getInstallationFiles(File meandreHome){
		Set<File> installFiles = new HashSet<File>();

		installFiles.add(new File(meandreHome, "meandre-config-store.xml"));
		installFiles.add(new File(meandreHome, "meandre-config-core.xml"));
		installFiles.add(new File(meandreHome, "MeandreStore"));
		installFiles.add(new File(meandreHome, "meandre-realm.properties"));
		installFiles.add(new File(meandreHome, "meandre-config-plugins.xml"));
		installFiles.add(new File(meandreHome, "derby.log"));
		installFiles.add(new File(meandreHome, "published_resources"));
		installFiles.add(new File(meandreHome, "run"));
		installFiles.add(new File(meandreHome, "mnt"));

		return installFiles;
	}


}
