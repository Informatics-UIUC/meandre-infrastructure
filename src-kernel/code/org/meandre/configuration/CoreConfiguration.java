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
	private static final String MEANDRE_PRIVATE_RUN_DIRECTORY = "MEANDRE_PRIVATE_RUN_DIRECTORY";
	
	/** The public resource directory */
	private static final String MEANDRE_PUBLIC_RESOURCE_DIRECTORY = "MEANDRE_PUBLIC_RESOURCE_DIRECTORY";

	/** The base port */
	private static final String MEANDRE_BASE_PORT = "MEANDRE_BASE_PORT";

	/** The config path */
	private static final String MEANDRE_CORE_CONFIG_FILE = "MEANDRE_CORE_CONFIG_FILE";
	
	/** The configuration properties */
	private Properties propsCore;
	
	/** The logger from the core */
	private Logger log;

	/** Creates a core configuration object with the default property values.
	 * 
	 */
	public CoreConfiguration () {
		
		propsCore = new Properties();
		
		propsCore.setProperty(MEANDRE_BASE_PORT, "1714");
        propsCore.setProperty(MEANDRE_PUBLIC_RESOURCE_DIRECTORY, "." + File.separator + "published_resources");
        propsCore.setProperty(MEANDRE_PRIVATE_RUN_DIRECTORY, "." + File.separator + "run");
        propsCore.setProperty(MEANDRE_CORE_CONFIG_FILE,"." + File.separator + "meandre-config-core.xml");
        
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
            fos = new FileOutputStream(propsCore.getProperty(MEANDRE_CORE_CONFIG_FILE));
            propsCore.storeToXML(fos, "Meandre default configuration file (" + Constants.MEANDRE_VERSION + ")");
            fos.close();
        }
        catch (Exception eWrite) {
            log.warning("Meandre configuration file " +
            		    propsCore.getProperty(MEANDRE_CORE_CONFIG_FILE) +
                        " could not be written to disk!");

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

	
}
