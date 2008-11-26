package org.meandre.core.store;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.store.security.SecurityStore;
import org.meandre.core.store.system.SystemStore;
import org.meandre.core.store.system.SystemStoreImpl;
import org.meandre.core.utils.Constants;
import org.meandre.core.utils.ModelIO;
import org.meandre.core.utils.NetworkTools;
import org.meandre.jobs.storage.backend.JobInformationBackendAdapter;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

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
     * The default configuration file name
     */
    private String sLocationFile = "meandre-default-locations.txt";

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
     * The default set of location to create per user
     */
    private Set<String> setDefaultLocations = new HashSet<String>();

    /**
     * The repository cache entry
     */
    private Hashtable<String, RepositoryImpl> htMapRepImpl = new Hashtable<String, RepositoryImpl>();

    /**
     * The Jena database connection object
     */
	private DBConnection dbConn = null;

	/** The core configuration object */
	private CoreConfiguration cnf;

	/** The job backend adapter information */
	private JobInformationBackendAdapter baJobInfo;

    /** Initialize a default store.
     * 
     * @param cnf The core configuration object
     */
    public Store(CoreConfiguration cnf) {
    	this.cnf = cnf;
    	propStoreConfig = new Properties();
    	initializeDefaultProperties();
        initializeStore();
    }
    
    /** Initialize a default store to install its file resources in a
     * specified directory. The resources are the store's config file
     * and jena database files.
     * 
     * @param sInstallDir The base install directory
     * @param cnf The core configuration object
     */
    public Store(String sInstallDir,CoreConfiguration cnf) {
        this.cnf = cnf;
    	sConfigPath = sInstallDir;
        sInstallDirPath = sInstallDir;
        propStoreConfig = new Properties();
        initializeDefaultProperties();
        initializeStore();
    }  
    /** Initialize a store with the provided properties
     * 
     * @param props The properties for the store
     * @param cnf The core configuration object
     */
    public Store ( Properties props,CoreConfiguration cnf ) {
    	this.cnf = cnf;
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
        

        // Try to open the default location file
    	try {
            //
            // Load the properties from the default location
            //
    		File file = new File(sConfigPath + File.separator + sLocationFile);
            FileReader fr = new FileReader(file);
            LineNumberReader lnr = new LineNumberReader(fr);
            String sTmp;
            while ( (sTmp=lnr.readLine())!=null ) setDefaultLocations.add(sTmp.trim());
            fr.close();
        }
    	catch (Exception eWrite) {
            log.warning("Meandre default location file " +sConfigPath + File.separator +sLocationFile+" could not be loaded");
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
        
        // Initialize the Job Information backend adapter
        try {
			 // Instantiate the adaptor
			baJobInfo = (JobInformationBackendAdapter) Class.forName(
					"org.meandre.jobs.storage.backend."+getDatabaseFlavor()+"JobInformationBackendAdapter"
				).newInstance();
			// Link it to a store
			baJobInfo.linkToService(getConnectionToDB(),cnf.getBasePort());
		} catch (InstantiationException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Could not instantiate job information backend adapter! "+baos.toString());
		} catch (IllegalAccessException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Could not found access class for job information backend adapter! "+baos.toString());
		} catch (ClassNotFoundException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Could not found class for job information backend adapter! "+baos.toString());
		}	
		
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
        // MeandreStore/log/
        String logDir = sInstallDirPath + File.separator + "MeandreStore";
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
            // Creating a new repository
            htMapRepImpl.put(sNickName, rep);
            for ( String sLocation:setDefaultLocations )
            	addLocation(sNickName, sLocation, "Default location", cnf);
            rep.refreshCache(rep.getModel());
            return rep;
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
	 * your way around. Otherwise you could end rendering the store in inconsistent
	 * states or even worst. Derby is left on auto commit since its embedded usage.
	 * For all other database types it returns a new connection with auto commit off.
	 * 
	 * @return The JDBC database connection object. Returns null if the connection 
	 *         object could not be retrieved.
	 */
	public Connection getConnectionToDB () {
		try {
				Connection connNew = DriverManager.getConnection(getURL(), getUserName(), getPassword());
				connNew.setAutoCommit(false);
				return connNew;
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
	
	/** Publishes the component described by the URI. If the URI already
	 * exist, the call returns false not modifying the published description;
	 * return true otherwise.
	 * 
	 * @param sURI The URI to publish
	 * @param sRemoteUser The publishing user.
	 */
	public boolean publishURI(String sURI, String sRemoteUser) {
		
		boolean bPublished = false;
		QueryableRepository qr = getRepositoryStore(sRemoteUser);
		Resource resURI = qr.getModel().createResource(sURI);
		Model modToPublish = null;
		Model modPublic = getPublicRepositoryStore();
		QueryableRepository qrPublic = new RepositoryImpl(modPublic);
		
		if ( qrPublic.getExecutableComponentDescription(resURI)==null && 
			 qrPublic.getFlowDescription(resURI)==null ) {
			// The URI does not exist
			ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(resURI);
			FlowDescription fd = qr.getFlowDescription(resURI);
			if ( ecd!=null ) {
				modToPublish = ecd.getModel();
			}
			else if ( fd!=null ) {
				modToPublish = fd.getModel();
			}
			
			if ( modToPublish!=null ) {	
				bPublished = true;
				modPublic.begin();
				modPublic.add(modToPublish);
				modPublic.commit();
			}
		}
		
		return bPublished;
	}
	

	/** Unpublishes the component described by the URI. If the URI already
	 * exist, the call returns false not modifying the published description;
	 * return true otherwise.
	 * 
	 * @param sURI The URI to publish
	 * @param sRemoteUser The publishing user.
	 * @return 
	 */
	public boolean unpublishURI(String sURI, String sRemoteUser) {
		
		boolean bUnpublished = false;
		QueryableRepository qr = getRepositoryStore(sRemoteUser);
		Resource resURI = qr.getModel().createResource(sURI);
		Model modToUnpublish = null;
		Model modPublic = getPublicRepositoryStore();
		QueryableRepository qrPublic = new RepositoryImpl(modPublic);
		
		if ( qrPublic.getExecutableComponentDescription(resURI)!=null || 
			 qrPublic.getFlowDescription(resURI)!=null ) {
			// The URI does not exist
			ExecutableComponentDescription ecd = qrPublic.getExecutableComponentDescription(resURI);
			FlowDescription fd = qrPublic.getFlowDescription(resURI);
			if ( ecd!=null ) {
				modToUnpublish = ecd.getModel();
			}
			else if ( fd!=null ) {
				modToUnpublish = fd.getModel();
			}
						
			if ( modToUnpublish!=null ) {
				bUnpublished = true;
				modPublic.begin();
				modPublic.remove(modToUnpublish);
				modPublic.commit();
			}
		}
		
		return bUnpublished;
	}
	

	/** Checks if a location already exist on the users system repository.
	 * 
	 * @param ss The users system repository
	 * @param sLocation The location
	 * @return True if the location already exist
	 */
	private boolean isAlreadyAUsedLocation(SystemStore ss, String sLocation) {
		boolean bExist = false;
		Set<Hashtable<String, String>> setProps = ss.getProperty(SystemStore.REPOSITORY_LOCATION);
		
		for ( Hashtable<String, String> ht:setProps )
			if ( ht.get("value").equals(sLocation) ) {
				bExist = true;
				break;
			}

		return bExist;
	}

	/** Removes a location from the repository.
	 * 
	 * @param sUser The system store user
	 * @param sLocation The location to remove
	 * @param cnf The core configuration object
	 * @return True if the location could be successfully removed
	 */
	public boolean removeLocation(String sUser, String sLocation, CoreConfiguration cnf) {
		boolean bRes = true;
		
		// Retrieve system store
		SystemStore ss = getSystemStore(cnf,sUser);
	    
		if ( !isAlreadyAUsedLocation(ss, sLocation)) {
			//
			// Location does not exist
			//
			bRes = false;
		}
		else {
			//
			// Location does exist
			//
			
			//
			// Regenerate the users repository
			//
			QueryableRepository qr = getRepositoryStore(sUser);
			Model mod = qr.getModel();
			
			try {
				URL url = new URL(sLocation);
				Model modelTmp = ModelFactory.createDefaultModel();
					
				modelTmp.setNsPrefix("", "http://www.meandre.org/ontology/");
				modelTmp.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
					modelTmp.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
				modelTmp.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");
	    
				//
				// Read the location and check its consistency
				//
				try{
					ModelIO.readModelInDialect(modelTmp, url);
				}
				catch(Exception e){
                    log.warning("Store failed removing Location: " +
                            url.toString() + "\n"+
                            e.toString());
                    bRes = false;
                    return bRes;
                    
                }
               
                // Check the pulled repository consistency
                QueryableRepository qrTmp = new RepositoryImpl(modelTmp);
				
				//
				// Remove the components provided by the location
				//
				// Remove the executable components
				for ( Resource recd:qrTmp.getAvailableExecutableComponents() ) {
					ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(recd);
					if ( ecd!=null ) {
						mod.begin();
						mod.remove(ecd.getModel());
						mod.commit();
					}
				}
				
				for ( Resource rfd:qrTmp.getAvailableFlows() ) {
					FlowDescription fd = qr.getFlowDescription(rfd);
					if ( fd!=null ) {
						mod.begin();
						mod.remove(fd.getModel());
						mod.commit();
					}
				}
				
				qr.refreshCache();
				bRes = true;
			}
			catch ( Exception e ) {
				log.warning("Failed to load location\n"+e.toString());
				bRes = false;
				qr.refreshCache();
			}
			
			// 
			// Remove the location form the system properties
			//
			ss.removeProperty(SystemStore.REPOSITORY_LOCATION, sLocation);
			
		}
		
		return bRes;
	}


	/** This method adds a location to the user repository. Also checks that is
	 * a valid description of it.
	 * 
	 * @param The user adding the location
	 * @param sLocation The location to add
	 * @param sDescription The description of the location to add
	 * @param cnf The core configuration object
	 * @return True if the location could be successfully added
	 */
	public boolean addLocation(String sUser, String sLocation, String sDescription, CoreConfiguration cnf) {
		
		boolean bRes = false;
		
		// Retrieve system store
		SystemStore ss = getSystemStore(cnf,sUser);
	    
		if ( !isAlreadyAUsedLocation(ss,sLocation) ) {
			//
			// New location
			//
			try {
					
				URL url = new URL(sLocation);
				Model modelTmp = ModelFactory.createDefaultModel();
					
				modelTmp.setNsPrefix("", "http://www.meandre.org/ontology/");
				modelTmp.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
					modelTmp.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
				modelTmp.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");
	    
				//
				// Read the location and check its consistency
				//			
				ModelIO.readModelInDialect(modelTmp, url);
				
				//
				// Test the location
				//
				QueryableRepository qrNew = new RepositoryImpl(modelTmp);
				
				//
				// If now exception was thrown, add the location to the list
				// and update the user repository 
				//
				ss.setProperty(SystemStore.REPOSITORY_LOCATION, sLocation, sDescription);
				QueryableRepository qr = getRepositoryStore(sUser);
				
				// Modifying the repository
				Model model = qr.getModel();
				model.begin();
				 
				// Adding components
				for ( ExecutableComponentDescription ecd:qrNew.getAvailableExecutableComponentDescriptions())
					if ( !qr.getAvailableExecutableComponents().contains(ecd.getExecutableComponent())) {
						model.add(ecd.getModel());
						// What it should be 
						//if ( ecd.getMode()==ExecutableComponentDescription.WEBUI_COMPONENT ) {
						// The failsafe
						if ( true ) {
								for ( RDFNode rdfNode:ecd.getContext() ) {
								boolean bFile = !rdfNode.toString().endsWith("/");
								if ( rdfNode.isResource() && bFile ) {
									// Need to pull the files
									try {
										URL urlCntx = new URL(rdfNode.toString());
										String [] saSplit = urlCntx.getFile().split("/");
										String sFile = saSplit[saSplit.length-1]; 
										new File(cnf.getPublicResourcesDirectory()+File.separator+"contexts"+File.separator+"java"+File.separator).mkdirs();
							    		File savedFile = new File(cnf.getPublicResourcesDirectory()+File.separator+"contexts"+File.separator+"java"+File.separator+sFile);
										FileOutputStream fos = new FileOutputStream(savedFile);
										int iChar; 
										InputStream is = urlCntx.openStream();
										while ( (iChar=is.read())>=0 ) fos.write(iChar);
										fos.close();
									} catch (Exception e) {
										ByteArrayOutputStream  baos = new ByteArrayOutputStream();
										PrintStream ps = new PrintStream(baos);
										e.printStackTrace(ps);
										log.warning("Problems pulling context "+rdfNode+"\n"+baos);
										throw new IOException(e.toString());
									}
								}
							}
						}
					}
					else
						log.warning("Component "+ecd.getExecutableComponent()+" already exist in the current repository. Discarding it.");
				
				// Adding flows
				for ( FlowDescription fd:qrNew.getAvailableFlowDescriptions())
					if ( !qr.getAvailableFlows().contains(fd.getFlowComponent()))
						model.add(fd.getModel());
					else
						log.warning("Flow "+fd.getFlowComponent()+" already exist in the current repository. Discarding it.");
				
				model.commit();
				getRepositoryStore(sUser).refreshCache(model);
				bRes = true;
			}
			catch ( Exception e ) {
				log.warning("WSLocationsLogic.removeLocation: Failed to add location\n"+e.toString());
				bRes = false;
				getRepositoryStore(sUser).refreshCache();
			}
		}
		else {
			//
			// Existing location (update the description)
			//
			ss.removeProperty(SystemStore.REPOSITORY_LOCATION, sLocation);
			ss.setProperty(SystemStore.REPOSITORY_LOCATION, sLocation, sDescription);
		}

		
		return bRes;
	}
	
	/** Regenerates a user repository using the current locations for the user.
	 *
	 * @param sUser The system store user
	 * @return True if the location could be successfully removed
	 */
	public boolean regenerateRepository(String sUser) {
		boolean bRes = true;

		// Preserves the current components uploaded not belonging to a location
		
		// Regenerating the user repository entries
		SystemStore ss = getSystemStore(cnf,sUser);
		Set<Hashtable<String, String>> setProps = ss.getProperty(SystemStore.REPOSITORY_LOCATION);
		for ( Hashtable<String, String> ht:setProps ) {
			String sLoc = ht.get("value");
			removeLocation(sUser, sLoc, cnf);
			addLocation(sUser, sLoc, ht.get("description"), cnf);
		}

		return bRes;
	}

	
	/** Add the given flows in RDF into the the user model.
	 *
	 * @param sUser The system store user
	 * @param sRDF The repository to add
	 * @param bOverwrite Should components be overwritten 
	 * @return The set of added flow
	 */
	public Set<String> addFlowsToRepository(String sUser, String sRDF, boolean bOverwrite) {
		QueryableRepository qr = getRepositoryStore(sUser);
		Model modNew = ModelFactory.createDefaultModel();

		modNew.setNsPrefix("", "http://www.meandre.org/ontology/");
		modNew.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		modNew.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		modNew.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

		StringReader srModel = new StringReader(sRDF);
		boolean bValidModel = true;
		
		try {
			modNew.read(srModel,null,"TTL");
		}
		catch ( Exception eTTL ) {
			try{
				modNew.read(srModel,null,"N-TRIPLE");
			}
			catch ( Exception eNT ) {
				try{
					modNew.read(srModel,null);
				}
				catch ( Exception eRDF ) {
					bValidModel = false;
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					eRDF.printStackTrace(new PrintStream(baos));
					log.warning("Repository could not be read\n"+baos.toString());
				}
			}
		}

		Set<String> setURIs = new HashSet<String>();
		if ( bValidModel ) {
			QueryableRepository qrNew = new RepositoryImpl(modNew);
			Model modUser = qr.getModel();
			for ( FlowDescription fd:qrNew.getAvailableFlowDescriptions()) {
				if ( !qr.getAvailableFlows().contains(fd.getFlowComponent())) {
					// The model for the flow to add
					Model fdModel = fd.getModel();
					// Add to the user repository
					modUser.begin();
					modUser.add(fdModel);
					modUser.commit();
					// Add the URI to modified
					setURIs.add(fd.getFlowComponent().toString());
				}
				else if ( bOverwrite ) {
					// Flow is there by needs to be overwritten
					Model modOld = qr.getFlowDescription(fd.getFlowComponent()).getModel();
					Model modRep = qr.getModel();
					modRep.begin();
					modRep.remove(modOld);
					modRep.add(fd.getModel());
					modRep.commit();
					// Add the URI to modified
					setURIs.add(fd.getFlowComponent().toString());
				}
				else {
					log.info("Flow already exist and the overwrite flag has not been provided. Discading flow update "+fd.getFlowComponent());
				}
			}
			qr.refreshCache();
		}
			
		return setURIs;	
	}
	
	/** Returns the list of published components and flows in the current server.
	 * 
	 * @return The set of published components' URI
	 */
	public Set<String> getPublishedComponentsAndFlows () {
		Set<String> setRes = new HashSet<String>();
		Model modPublic = getPublicRepositoryStore();
		QueryableRepository qrPublic = new RepositoryImpl(modPublic);
		
		for ( ExecutableComponentDescription ecd:qrPublic.getAvailableExecutableComponentDescriptions() )
			setRes.add(ecd.getExecutableComponent().toString());
		
		for ( FlowDescription fd:qrPublic.getAvailableFlowDescriptions() )
			setRes.add(fd.getFlowComponent().toString());
		
		
		return setRes;
	}
	
	/** Returns the job information backend adapter object linked to this store.
	 * 
	 * @return The backend adapter to the job information
	 */
	public JobInformationBackendAdapter getJobInformation () {
		return this.baJobInfo;
	}
}
