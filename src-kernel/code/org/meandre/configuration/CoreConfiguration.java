package org.meandre.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.utils.Constants;

/** This class contains basic configuration informations requried by the core.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class CoreConfiguration {

	/** The private run directory property*/
	public static final String MEANDRE_PRIVATE_RUN_DIRECTORY = "MEANDRE_PRIVATE_RUN_DIRECTORY";
	
	/** The public resource directory */
	public static final String MEANDRE_PUBLIC_RESOURCE_DIRECTORY = "MEANDRE_PUBLIC_RESOURCE_DIRECTORY";

	/** The base port */
	public static final String MEANDRE_BASE_PORT = "MEANDRE_BASE_PORT";

	/** The config path */
	public static final String MEANDRE_CORE_CONFIG_FILE = "MEANDRE_CORE_CONFIG_FILE";

    /** The config path */
	public static final String MEANDRE_HOME_DIRECTORY = "MEANDRE_HOME_DIRECTORY";
    
    /** The Default port*/
	public static final int DEFAULT_PORT =1714;
	
	/** The configuration properties */
	private Properties propsCore;
	
	/** The logger from the core */
	private Logger log;
	
	/**The Default install dir*/
	private String INSTALL_DIR =".";

	/** Creates a core configuration object with the default property values.
	 * It uses the existing property file and uses those values if it exists.
	 * 
	 */
	public CoreConfiguration () {
		log = KernelLoggerFactory.getCoreLogger();
		
		propsCore = new Properties();
		propsCore.setProperty(MEANDRE_BASE_PORT, Integer.toString(DEFAULT_PORT));
        propsCore.setProperty(MEANDRE_PUBLIC_RESOURCE_DIRECTORY, INSTALL_DIR + File.separator + "published_resources");
        propsCore.setProperty(MEANDRE_PRIVATE_RUN_DIRECTORY,INSTALL_DIR + File.separator + "run");
        propsCore.setProperty(MEANDRE_CORE_CONFIG_FILE, INSTALL_DIR + File.separator + "meandre-config-core.xml");
        propsCore.setProperty(MEANDRE_HOME_DIRECTORY,INSTALL_DIR);   
        
        initializeConfiguration();    	
	}
	

	/** Creates a core configuration object with the default property values.
	 * It uses the existing property file and uses those values if it exists.
	 * 
	 * @param props The properties to use
	 */
	public CoreConfiguration ( Properties props ) {
		log = KernelLoggerFactory.getCoreLogger();
		
		propsCore = props;   
        
        initializeConfiguration();    	
	}
	
	/**
	 * Creates a core configuration where all file resources will be in
	 * the specified sInstallDir and the server will run on the specified
	 * port.
	 * @param port the port the web services will run on
	 * @param sInstallDir the directory used for Meandre's persistent data
	 */
	public CoreConfiguration(int port, String sInstallDir){
	    
	    propsCore = new Properties();
	        
	    propsCore.setProperty(MEANDRE_BASE_PORT, Integer.toString(port));
        propsCore.setProperty(MEANDRE_PUBLIC_RESOURCE_DIRECTORY, sInstallDir + File.separator + "published_resources");
        propsCore.setProperty(MEANDRE_PRIVATE_RUN_DIRECTORY, sInstallDir + File.separator + "run");
        propsCore.setProperty(MEANDRE_CORE_CONFIG_FILE, sInstallDir + File.separator + "meandre-config-core.xml");
        propsCore.setProperty(MEANDRE_HOME_DIRECTORY, sInstallDir);  
        INSTALL_DIR = sInstallDir;
        log = KernelLoggerFactory.getCoreLogger();
	     
        initializeConfiguration();    
	}
	
	
	/** Initialize the supporting file structures for the given configuration.
	 * 
	 */
	private void initializeConfiguration() {
		
		File fileCnf = new File(propsCore.getProperty(MEANDRE_CORE_CONFIG_FILE));
		if ( !fileCnf.exists() ) {
			FileOutputStream fos;
	        try {
	        	// Create home dir
	        	new File(getHomeDirectory()).mkdir();
	            
	        	// Dump properties
	        	
	            fos = new FileOutputStream(fileCnf);
	            propsCore.storeToXML(fos, "Meandre default configuration file (" + Constants.MEANDRE_VERSION + ")");
	            fos.close();
	            
	            // Create the run file
	            new File(getPublicResourcesDirectory()).mkdir();
	            new File(getRunResourcesDirectory()).mkdir();
	
	        }
	        catch (Exception eWrite) {
	            log.warning("Meandre configuration file " +
	            		    propsCore.getProperty(MEANDRE_CORE_CONFIG_FILE) +
	                        " could not be written to disk!");
	
	        }
		}
	}


	/**
     * Returns the location of run resources directory for Meandre
     *
     * @return The resource directory
     */
    public String getRunResourcesDirectory() {
        return propsCore.getProperty(MEANDRE_PRIVATE_RUN_DIRECTORY);
    }

    /**
     * Returns the location of public resources for Meandre
     *
     * @return The resource directory
     */
    public String getPublicResourcesDirectory() {
        return propsCore.getProperty(MEANDRE_PUBLIC_RESOURCE_DIRECTORY);
    }
    
    /**
     * Returns the base port for Meandre
     *
     * @return The base port
     */
    public int getBasePort() {
        return Integer.parseInt(propsCore.getProperty(MEANDRE_BASE_PORT));
    }

    /**
     * the working directory for a meandre instance. this is where persistent
     * data and config files will be written by default.
     * 
     * @return a path in string form. may or may not be a relative path (e.g. ".")
     */
    public String getHomeDirectory(){
        return propsCore.getProperty(MEANDRE_HOME_DIRECTORY);
    }

//    /** Returns the current host IP address.
//     * 
//     * @return The host IP address
//     */
//	public String getHostName() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	/** Compare if two configurations are the same.
	 * 
	 * @param objCnf The configuration to compare
	 * @return True if equal, false otherwhise.
	 */
	public boolean equals ( Object objCnf ) {
		
		// Check type
		if ( !(objCnf instanceof CoreConfiguration) )
			return false;
		
		// Type cast
		CoreConfiguration cnf = (CoreConfiguration)objCnf;
		
		// Check property sizes
		if ( propsCore.size()!=cnf.propsCore.size() ) 
			return false;
		
		// Check property values
		for ( Object objKey:cnf.propsCore.keySet() ) {
			if ( !cnf.propsCore.containsKey(objKey) )
				return false;
			else if ( !cnf.propsCore.getProperty(objKey.toString()).equals(propsCore.getProperty(objKey.toString())) )
				return false;
		}
		
		return true;
	}
}
