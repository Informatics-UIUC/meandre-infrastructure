package org.meandre.core.store;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.store.security.SecurityStore;
import org.meandre.core.store.system.SystemStore;
import org.meandre.core.store.system.SystemStoreImpl;
import org.meandre.core.utils.Constants;
import org.meandre.core.utils.NetworkTools;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * This class provides the basic configuration for the Meandre store.
 *
 * @author Xavier Llor&agrave;
 */
public class Store {

    /**
     * The base storage URL
     */
    private static final String BASE_STORAGE_URL = "http://meandre.org/metadata/store/";

    /**
     * The meandre store config file
     */
    private static final String MEANDRE_STORE_CONFIG_FILE = "MEANDRE_STORE_CONFIG_FILE";
    /**
     * The authentication realm file
     */
    private static final String MEANDRE_AUTHENTICATION_REALM_FILENAME = "MEANDRE_AUTHENTICATION_REALM_FILENAME";

    /**
     * The authentication realm file
     */
    private static final String MEANDRE_ADMIN_USER = "MEANDRE_ADMIN_USER";

    /**
     * The Jena database backend name constant
     */
    private static final String JENA_DB = "DB";

    /**
     * The Jena database user name constant
     */
    private static final String JENA_DB_PASSWD = "DB_PASSWD";

    /**
     * The Jena database user password constant
     */
    private static final String JENA_DB_USER_NAME = "DB_USER";

    /**
     * The Jena database URL constant
     */
    private static final String JENA_DB_URL = "DB_URL";

    /**
     * The Jena database driver class name constant
     */
    private static final String JENA_DB_DRIVER_CLASS_NAME = "DB_DRIVER_CLASS";

    /**
     * The default configuration path to the file name
     */
    private String sConfigPath = ".";
    
    /**
     * The directory path to install working files.
     */
    private String sInstallDirPath = ".";
    
    /**
     * The default configuration file name
     */
    private String sConfigFile = "meandre-config-store.xml";
    
    


    /**
     * Contains the basic properties of storage mechanism
     */
    private Properties propStoreConfig = null;

    /**
     * The core root logger
     */
    protected Logger log = KernelLoggerFactory.getCoreLogger();

    /**
     * The default Model Maker
     */
    private transient ModelMaker makerJenaModel = null;

    /**
     * The public repository URL
     */
    private static final String PUBLIC_REPOSITORY_URL = BASE_STORAGE_URL + "public/repository";

    /**
     * The security store URL
     */
    public static final String SECURITY_STORE_URL = BASE_STORAGE_URL + "security";

    /**
     * The base system store URL
     */
    public static final String MEANDRE_ONTOLOGY_BASE_URL = "http://www.meandre.org/ontology/";

    /**
     * The base system store URL
     */
    public static final String BASE_SYSTEM_STORE_URL = BASE_STORAGE_URL + "system/";

    /**
     * The base repository store URL
     */
    public static final String BASE_REPOSITORY_STORE_URL = BASE_STORAGE_URL + "repository/";

    /**
     * The base repository configuration URL
     */
    public static final String BASE_REPSITORY_STORE_CONFIG_URL = BASE_STORAGE_URL + "configuration/";

    /**
     * The security store
     */
    private SecurityStore ssSecurityStore = null;


    /**
     * The repository cache entry
     */
    private Hashtable<String, RepositoryImpl> htMapRepImpl = new Hashtable<String, RepositoryImpl>();

    /**
     * The Jena database connection object
     */
	private DBConnection dbConn = null;

    /** Initialize a default store.
     * 
     */
    public Store() {
    	propStoreConfig = new Properties();
    	initializeDefaultProperties();
        initializeStore();
    }
    
    /** Initialize a default store to install its file resources in a
     * specified directory. The resources are the store's config file
     * and jena database files.
     * 
     * @param sInstallDir The base install directory
     */
    public Store(String sInstallDir) {
        sConfigPath = sInstallDir;
        sInstallDirPath = sInstallDir;
        propStoreConfig = new Properties();
        initializeDefaultProperties();
        initializeStore();
    }  
    /** Initialize a store with the provided properties
     * 
     * @param props The properties for the store
     */
    public Store ( Properties props ) {
    	propStoreConfig = props;
    	initializeStore();
    }
    
    /** Initialize a Store with the given properties.
     * 
     * @param props The properties to use to initialize the store
     */
    public void initializeStore ( Properties props ) {
    	propStoreConfig = props;
    	initializeStore();
    }
    
     
    /** The initialization of the store based on properties.
     */
    protected void initializeStore() {
        //the fully expanded path to the config file        
        String sConfigFileAbs = sConfigPath + File.separator + sConfigFile;
        
        // Try to open the config file
    	FileInputStream fis;
        try {
            //
            // Load the properties from the default location
            //
            fis = new FileInputStream(sConfigFileAbs);
            propStoreConfig.loadFromXML(fis);
            fis.close();
        }
        catch (Exception eRead) {
            //
            // The file could not be loaded
            // Creating a default one
            //
            log.warning("Meandre configuration file " +
                        sConfigFileAbs +
                        " could not be loaded. Creating a default one.");

            initializeDefaultProperties();
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(sConfigFileAbs);
                propStoreConfig.storeToXML(fos, "Meandre default configuration file (" + Constants.MEANDRE_VERSION + ")");
                fos.close();
            }
            catch (Exception eWrite) {
                log.warning("Meandre configuration file " +
                            sConfigFileAbs +
                            " could not be written to disk!");

            }
        }

        // Report the current configuration to the log file
        log.config("JENA RBM driver: " + getDriverClassName());
        log.config("JENA RBM database: " + getDBName());
        log.config("JENA RBM user: " + getUserName());
        log.config("JENA RBM password: " + getPassword());
        log.config("JENA RBM URL: " + getURL());

        // Loads the default database driver for Jena storage
        try {
            log.config("Loading connection driver " + getDriverClassName());
            Class.forName(getDriverClassName());
        }
        catch (ClassNotFoundException e) {
            log.warning("Driver " + getDriverClassName() + " could not be loaded");
        }

        // Initializes the model maker
        log.config("Initializing JENA RDBModelMaker");

        // Create database connection
        dbConn = new DBConnection(getURL(), getUserName(), getPassword(), getDBName());
        makerJenaModel = ModelFactory.createModelRDBMaker(dbConn);

        log.config("Initialization of JENA RDBModelMaker done");
        initializeSecurityStore();
        

    }

    /**
     * Creates the default properties for Meandre.
     */
    protected void initializeDefaultProperties() {
        //
        // Meandre properties
        //
    	propStoreConfig.setProperty(MEANDRE_STORE_CONFIG_FILE, sConfigPath + File.separator + "meandre-config-store.xml");
        propStoreConfig.setProperty(MEANDRE_AUTHENTICATION_REALM_FILENAME, sConfigPath + File.separator + "meandre-realm.properties");
        propStoreConfig.setProperty(MEANDRE_ADMIN_USER, "admin");

        //
        // Jena DB relational background properties
        //
        String storeDir = sInstallDirPath + File.separator + "MeandreStore";
        //when the logDevice is set, derby will abort if the log directory is already
        //in use saying "it may be in use by another database", so we have to make
        // DerbyLog/log/
        String logDir = sInstallDirPath + File.separator + "DerbyLog";
        String derbyUrl = "jdbc:derby:" + storeDir + ";create=true" + ";logDevice=" + logDir;
        		
        propStoreConfig.setProperty(JENA_DB_DRIVER_CLASS_NAME, "org.apache.derby.jdbc.EmbeddedDriver");
        propStoreConfig.setProperty(JENA_DB_URL, derbyUrl);
        propStoreConfig.setProperty(JENA_DB_USER_NAME, "");
        propStoreConfig.setProperty(JENA_DB_PASSWD, "");
        propStoreConfig.setProperty(JENA_DB, "Derby");
    }

    /**
     * Populates the store with the default metadata.
     */
    protected void initializeSecurityStore() {

        try {
            ssSecurityStore = new SecurityStore(getMaker().createModel(SECURITY_STORE_URL),this);
        }
        catch (SecurityStoreException e) {
            log.severe("Security store could not be initialized. Aborting Meandre execution!\n" + e);
            System.exit(-1);
        }

    }


    /**
     * Gets the driver class name used.
     *
     * @return The class name
     */
    protected String getDriverClassName() {
        return propStoreConfig.getProperty(JENA_DB_DRIVER_CLASS_NAME);
    }


    /**
     * Gets the password.
     *
     * @return The password
     */
    protected String getPassword() {
        return propStoreConfig.getProperty(JENA_DB_PASSWD);
    }

    /**
     * Gets the user name.
     *
     * @return The user name
     */
    protected String getUserName() {
        return propStoreConfig.getProperty(JENA_DB_USER_NAME);
    }


    /**
     * Gets the database URL.
     *
     * @return The database URL
     */
    protected String getURL() {
        return propStoreConfig.getProperty(JENA_DB_URL);
    }


    /**
     * Gets the database name.
     *
     * @return The database name
     */
    protected String getDBName() {
        return propStoreConfig.getProperty(JENA_DB);
    }

    /**
     * Gets the Model Maker to use.
     *
     * @return The model maker
     */
    protected ModelMaker getMaker() {
        return makerJenaModel;
    }

    /**
     * Return the security store for the given Meandre instance.
     *
     * @return The security store
     */
    public SecurityStore getSecurityStore() {
        return ssSecurityStore;
    }

    /**
     * Returns the a system store for the given user.
     *
     * @param sNickName The user nickname
     * @param cnf The core configuration
     * @return The system store for the given user
     */
    public SystemStore getSystemStore(CoreConfiguration cnf, String sNickName) {
    	String sHostName = NetworkTools.getLocalHostName();
    	
        return new SystemStoreImpl(
        		getMaker().createModel(BASE_SYSTEM_STORE_URL + sNickName),
        		sHostName,
        		cnf.getBasePort()
        	);
    }

    /**
     * Returns the persistent repository for a given user.
     *
     * @param sNickName The user nickname
     * @return The repository store for the given user
     */
    public QueryableRepository getRepositoryStore(String sNickName) {
        if (htMapRepImpl.containsKey(sNickName))
            return htMapRepImpl.get(sNickName);
        else {
            RepositoryImpl rep = new RepositoryImpl(getMaker().createModel(BASE_REPOSITORY_STORE_URL + sNickName));
            htMapRepImpl.put(sNickName, rep);
            return new RepositoryImpl(getMaker().createModel(BASE_REPOSITORY_STORE_URL + sNickName));
        }
    }

    /**
     * Returns the persistent public repository.
     *
     * @return The public repository store for the given user
     */
    public Model getPublicRepositoryStore() {
        return getMaker().createModel(PUBLIC_REPOSITORY_URL);
    }

    /**
     * Returns the base authentication realm file for Meandre
     *
     * @return The base port
     */
    public String getRealmFilename() {
        return propStoreConfig.getProperty(MEANDRE_AUTHENTICATION_REALM_FILENAME);
    }

    /**
     * Returns the admin user nick name for Meandre
     *
     * @return The base port
     */
    public String getAdminUserNickName() {
        return propStoreConfig.getProperty(MEANDRE_ADMIN_USER);
    }

    /**
     * Returns all the store properties.
     *
     * @return All the properties
     */
	public Properties getAllProperties () {
		return propStoreConfig;
	}

	/**
	 * Returns the paths to the configuration files
	 * 
	 * @return The path to the configuration files
	 */
	public String getConfigurationPath() {
		return sConfigPath;
	}

	/**
	 * Sets the paths to the configuration files.
	 * 
	 * @param sPath The path to the configuration files
	 */
	public void setConfigurationPath(String sPath) {
		sConfigPath = sPath;
	}
	
	/** Returns the JDBC connection to the database backend. This call should
	 * be used carefully. Just access the database backend if your really know
	 * your way around. Otherwise you could end rendering the store in inconsitent
	 * states or even worst.
	 * 
	 * @return The JDBC database connection object. Returns null if the connection 
	 *         object could not be retrieved.
	 */
	public Connection getConnectionToDB () {
		try {
			return dbConn.getConnection();
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.warning("Could not retrieve the connection to the database backend. "+baos.toString());
			return null;
		}
	}
	
	/** Returns the database flavor of the backend currently used.
	 * 
	 * @return The database flavour
	 */
	public String getDatabaseFlavor () {
		return dbConn.getDatabaseType();
	}
}
