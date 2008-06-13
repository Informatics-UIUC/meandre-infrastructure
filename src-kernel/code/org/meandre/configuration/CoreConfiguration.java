package org.meandre.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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
	private static final String MEANDRE_PRIVATE_RUN_DIRECTORY = "MEANDRE_PRIVATE_RUN_DIRECTORY";
	
	/** The public resource directory */
	private static final String MEANDRE_PUBLIC_RESOURCE_DIRECTORY = "MEANDRE_PUBLIC_RESOURCE_DIRECTORY";

	/** The base port */
	private static final String MEANDRE_BASE_PORT = "MEANDRE_BASE_PORT";

	/** The config path */
	private static final String MEANDRE_CORE_CONFIG_FILE = "MEANDRE_CORE_CONFIG_FILE";

    /** The config path */
    private static final String MEANDRE_HOME_DIRECTORY = "MEANDRE_HOME_DIRECTORY";
    
	/** The configuration properties */
	private Properties propsCore;
	
	/** The logger from the core */
	private Logger log;
	
	/** The Default port*/
	private static final int DEFAULT_PORT =1714;
	
	/**The Default install dir*/
	private static final String INSTALL_DIR =".";

	/** Creates a core configuration object with the default property values.
	 * It uses the existing property file and uses those values if it exists.
	 * 
	 */
	public CoreConfiguration () {
		log = KernelLoggerFactory.getCoreLogger();
		if(!checkAndUseConfigurationIfExists(INSTALL_DIR) ){
			   propsCore = new Properties();
		       propsCore.setProperty(MEANDRE_BASE_PORT, Integer.toString(DEFAULT_PORT));
		        propsCore.setProperty(MEANDRE_PUBLIC_RESOURCE_DIRECTORY, INSTALL_DIR + File.separator + "published_resources");
		        propsCore.setProperty(MEANDRE_PRIVATE_RUN_DIRECTORY,INSTALL_DIR + File.separator + "run");
		        propsCore.setProperty(MEANDRE_CORE_CONFIG_FILE, INSTALL_DIR + File.separator + "meandre-config-core.xml");
		        propsCore.setProperty(MEANDRE_HOME_DIRECTORY,INSTALL_DIR);   
		        initializeConfiguration();    	
		}
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
        log = KernelLoggerFactory.getCoreLogger();
	        
        initializeConfiguration();    
	}
	
	
	/** Creates a core configuration object with the default property values.
	 * 
	 * @param props The properties to use
	 */
	public CoreConfiguration ( Properties props ) {
		propsCore = props;
		initializeConfiguration();
	}
	
	/** Initialize the supporting file structures for the given configuration.
	 * 
	 */
	private void initializeConfiguration() {
		FileOutputStream fos;
        try {
            fos = new FileOutputStream("meandre-config-core.xml");
            propsCore.storeToXML(fos, "Meandre default configuration file (" + Constants.MEANDRE_VERSION + ")");
            fos.close();
            
            // Create the run file
            new File(getRunResourcesDirectory()).mkdir();

        }
        catch (Exception eWrite) {
            log.warning("Meandre configuration file " +
            		    propsCore.getProperty(MEANDRE_CORE_CONFIG_FILE) +
                        " could not be written to disk!");

        }
		
	}
	
	
	/**Call this function if the core property file is already present.
	 * 
	 */
	private boolean checkAndUseConfigurationIfExists(String installDir) {
		InputStream fis;
        try {
            fis = new FileInputStream(installDir+File.separator+"meandre-config-core.xml");
            propsCore = new Properties();
            propsCore.load(fis);
            fis.close();
            // Create the run file
            new File(getRunResourcesDirectory()).mkdir();
        }
        catch (Exception eWrite) {
            log.warning("Meandre configuration file " +
            			installDir+File.separator+"meandre-config-core.xml" +
                        " could not be read -creating new!");
            return Boolean.FALSE;
        }
		return Boolean.TRUE;
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

    /** Returns the current host IP address.
     * 
     * @return The host IP address
     */
	public String getHostName() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
