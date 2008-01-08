package org.meandre.core.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.WSCoreBootstrapper;
import org.meandre.core.store.repository.QueryableRepository;
import org.meandre.core.store.repository.RepositoryImpl;
import org.meandre.core.store.security.SecurityStore;
import org.meandre.core.store.security.SecurityStoreException;
import org.meandre.core.store.security.local.SecurityStoreImpl;
import org.meandre.core.store.system.SystemStore;
import org.meandre.core.store.system.SystemStoreImpl;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/** This class provides the basic configuration for the Meandre store.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class Store {

	/** The base storage URL */
	private static final String BASE_STORAGE_URL = "http://meandre.org/metadata/store/";

	/** The authentication realm file */
	private static final String MEANDRE_AUTHENTICATION_REALM_FILENAME = "MEANDRE_AUTHENTICATION_REALM_FILENAME";

	/** The authentication realm file */
	private static final String MEANDRE_ADMIN_USER = "MEANDRE_ADMIN_USER";

	/** The base Meandre port */
	private static final String MEANDRE_BASE_PORT = "MEANDRE_BASE_PORT";

	/** The directory containing the public resources */
	private static final String MEANDRE_PUBLIC_RESOURCE_DIRECTORY = "MEANDRE_PUBLIC_RESOURCE_DIRECTORY";

	/** The Jena database backend name constant */
	private static final String JENA_DB = "DB";

	/** The Jena database user name constant */
	private static final String JENA_DB_PASSWD = "DB_PASSWD";

	/** The Jena database user password constant */
	private static final String JENA_DB_USER_NAME = "DB_USER";

	/** The Jena database URL constant */
	private static final String JENA_DB_URL = "DB_URL";

	/** The Jena database driver class name constant */
	private static final String JENA_DB_DRIVER_CLASS_NAME = "DB_DRIVER_CLASS";

	/** The default configuration path to the file name */
	private final static String sConfigPath = ".";

	/** The default configuration file name */
	private final static String sConfigFile = "meandre-config.xml";


	/** Contains the basic properties of storage mechanism */
	private static Properties propStoreConfig = null;

	/** The logger  */
	private static Logger log = null;

	/** The default Model Maker */
	private static ModelMaker makerJenaModel = null;

	/** The public repository URL */
	private static String PUBLIC_REPOSITORY_URL = BASE_STORAGE_URL+"public/repository";

	/** The security store URL */
	public static String SECURITY_STORE_URL = BASE_STORAGE_URL+"security";

	/** The base system store URL */
	public static String MEANDRE_ONTOLOGY_BASE_URL = "http://www.meandre.org/ontology/";

	/** The base system store URL */
	public static String BASE_SYSTEM_STORE_URL = BASE_STORAGE_URL+"system/";

	/** The base repository store URL */
	public static String BASE_REPSITORY_STORE_URL = BASE_STORAGE_URL+"repository/";

	/** The base repository configuration URL */
	public static String BASE_REPSITORY_STORE_CONFIG_URL = BASE_STORAGE_URL+"configuration/";

	/** The security store */
	@SuppressWarnings("unused")
	private static SecurityStoreImpl ssSecurityStore = null;

	/** The default initialization based on properties. */
	static {

		// Initialize the logger
		log = Logger.getLogger(WSCoreBootstrapper.class.getName());
		log.setLevel(Level.CONFIG);
		log.addHandler(WSCoreBootstrapper.handler);

		// Try to open the config file
		propStoreConfig = new Properties();
	    FileInputStream fis;
		try {
			//
			// Load the properties from the default location
			//
			fis = new FileInputStream(sConfigPath+File.separator+sConfigFile);
			propStoreConfig.loadFromXML(fis);
			fis.close();
		} catch (Exception eRead) {
			//
			// The file could not be loaded
			// Creating a default one
			//
			log.warning("Meandre configuration file "+
					    sConfigPath+File.separator+sConfigFile+
					    " could not be loaded. Creating a default one.");

			initializeDefaultProperties();
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(sConfigPath+File.separator+sConfigFile);
				propStoreConfig.storeToXML(fos, "Meandre default configuration file ("+WSCoreBootstrapper.VERSION+")");
			    fos.close();
			} catch (Exception eWrite) {
				log.warning("Meandre configuration file "+
					    sConfigPath+File.separator+sConfigFile+
					    " could not be written to disk!");
			eWrite.printStackTrace();

			}
		}

		// Report the current configuration to the log file
		log.info("JENA RBM driver: "+getDriverClassName());
		log.info("JENA RBM database: "+getDBName());
		log.info("JENA RBM user: "+getUserName());
		log.info("JENA RBM password: "+getPassword());
		log.info("JENA RBM URL: "+getURL());

		// Loads the default database driver for Jena storage
		try {
			log.info("Loading connection driver "+getDriverClassName());
			Class.forName(getDriverClassName());
		} catch (ClassNotFoundException e) {
			log.info("Driver "+getDriverClassName()+" could not be loaded");
		}

		// Initializes the model maker
		log.info("Initializing JENA RDBModelMaker");

		// Create database connection
		IDBConnection conn = new DBConnection(getURL(),getUserName(),getPassword(),getDBName());
		makerJenaModel = ModelFactory.createModelRDBMaker(conn) ;

		log.info("Initialization of JENA RDBModelMaker done");
		initializeStore();

	}

	/** Creates the default properties for Meandre.
	 *
	 */
	protected static void initializeDefaultProperties() {
		//
		// Meandre properties
		//
		propStoreConfig.setProperty(MEANDRE_BASE_PORT, "1714");
		propStoreConfig.setProperty(MEANDRE_AUTHENTICATION_REALM_FILENAME,"meandre-realm.properties");
		propStoreConfig.setProperty(MEANDRE_PUBLIC_RESOURCE_DIRECTORY, "."+File.separator+"published_resources");
		propStoreConfig.setProperty(MEANDRE_ADMIN_USER,"admin");

		//
		// Jena Derby properties
		//
		propStoreConfig.setProperty(JENA_DB_DRIVER_CLASS_NAME, "org.apache.derby.jdbc.EmbeddedDriver");
		propStoreConfig.setProperty(JENA_DB_URL,"jdbc:derby:MeandreStore;create=true");
		propStoreConfig.setProperty(JENA_DB_USER_NAME,"");
		propStoreConfig.setProperty(JENA_DB_PASSWD,"");
		propStoreConfig.setProperty(JENA_DB,"Derby");
	}

	/** Populates the store with the default metadata.
	 *
	 */
	protected static void initializeStore() {

		try {
			ssSecurityStore  = new SecurityStoreImpl(getMaker().createModel(SECURITY_STORE_URL));
		} catch (SecurityStoreException e) {
			log.severe("Security store could not be initialized. Aborting Meandre execution!\n"+e);
			System.exit(-1);
		}

	}

	/** Gets the driver class name used.
	 *
	 * @return The class name
	 */
	protected static String getDriverClassName() {
		return propStoreConfig.getProperty(JENA_DB_DRIVER_CLASS_NAME);
	}


	/** Gets the password.
	 *
	 * @return The password
	 */
	protected static String getPassword() {
		return propStoreConfig.getProperty(JENA_DB_PASSWD);
	}

	/** Gets the user name.
	 *
	 * @return The user name
	 */
	protected static String getUserName() {
		return propStoreConfig.getProperty(JENA_DB_USER_NAME);
	}


	/** Gets the database URL.
	 *
	 * @return The database URL
	 */
	protected static String getURL() {
		return propStoreConfig.getProperty(JENA_DB_URL);
	}


	/** Gets the database name.
	 *
	 * @return The database name
	 */
	protected static String getDBName() {
		return propStoreConfig.getProperty(JENA_DB);
	}

	/** Gets the Model Maker to use.
	 *
	 * @return The model maker
	 */
	protected static ModelMaker getMaker() {
		return makerJenaModel;
	}

	/** Return the security store for the given Meandre instance.
	 *
	 * @return The security store
	 */
	public static SecurityStore getSecurityStore() {
		return ssSecurityStore;
	}

	/** Returns the a system store for the given user.
	 *
	 * @param sNickName The user nickname
	 * @return The system store for the given user
	 */
	public static SystemStore getSystemStore ( String sNickName ) {
		return new SystemStoreImpl(getMaker().createModel(BASE_SYSTEM_STORE_URL+sNickName));
	}

	/** Returns the persistent repository for a given user.
	 *
	 * @param sNickName The user nickname
	 * @return The repository store for the given user
	 */
	public static QueryableRepository getRepositoryStore ( String sNickName ) {
		return new RepositoryImpl(getMaker().createModel(BASE_REPSITORY_STORE_URL+sNickName));
	}

	/** Returns the persistent public repository.
	 *
	 * @return The public repository store for the given user
	 */
	public static Model getPublicRepositoryStore () {
		return getMaker().createModel(PUBLIC_REPOSITORY_URL);
	}

	/** Returns the base port for Meandre
	 *
	 * @return The base port
	 */
	public static int getBasePort () {
		return Integer.parseInt(propStoreConfig.getProperty(MEANDRE_BASE_PORT));
	}

	/** Returns the base authentication realm file for Meandre
	 *
	 * @return The base port
	 */
	public static String getRealmFilename () {
		return propStoreConfig.getProperty(MEANDRE_AUTHENTICATION_REALM_FILENAME);
	}

	/** Returns the admin user nick name for Meandre
	 *
	 * @return The base port
	 */
	public static String getAdminUserNickName () {
		return propStoreConfig.getProperty(MEANDRE_ADMIN_USER);
	}

	/** Returns the location of public resources for Meandre
	 *
	 * @return The resource directory
	 */
	public static String getPublicResourceDirectory() {
		return propStoreConfig.getProperty(MEANDRE_PUBLIC_RESOURCE_DIRECTORY);
	}

	/** Returns all the store properties.
	 *
	 * @return All the properties
	 */
	public static Properties getAllProperties () {
		return propStoreConfig;
	}
}
