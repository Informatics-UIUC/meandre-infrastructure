package org.meandre.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.utils.Version;
import org.meandre.webservices.logger.WSLoggerFactory;

import de.schlichtherle.io.FileInputStream;

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

    /** The web app context */
    public static final String MEANDRE_APP_CONTEXT = "MEANDRE_APP_CONTEXT";

    /** The kernel logging level */
    public static final String MEANDRE_KERNEL_LOG_LEVEL = "MEANDRE_KERNEL_LOG_LEVEL";

    /** The web services logging level */
    public static final String MEANDRE_WS_LOG_LEVEL = "MEANDRE_WS_LOG_LEVEL";

    /** The Default port*/
    public static final int DEFAULT_PORT = 1714;

    /** The default conductor queue size */
    private static final String MEANDRE_CONDUCTOR_DEFAULT_QUEUE_SIZE = "MEANDRE_CONDUCTOR_DEFAULT_QUEUE_SIZE";

    /** The default conservative queue size */
    public static final int CONDUCTOR_DEFAULT_QUEUE_SIZE = 6;

    /** The configuration properties */
    private final Properties propsCore;

    /** The logger from the core */
    private final Logger log;

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
        propsCore.setProperty(MEANDRE_APP_CONTEXT,"");
        propsCore.setProperty(MEANDRE_CONDUCTOR_DEFAULT_QUEUE_SIZE,Integer.toString(CONDUCTOR_DEFAULT_QUEUE_SIZE));

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
    public CoreConfiguration(int port, String sInstallDir) {

        final File confCore = new File(sInstallDir + File.separator + "meandre-config-core.xml");

        Properties propDefaults = new Properties();

        if (confCore.exists()) {
            try {
                propDefaults.loadFromXML(new FileInputStream(confCore));
                propDefaults.setProperty(MEANDRE_CORE_CONFIG_FILE, confCore.toString());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            propDefaults.setProperty(MEANDRE_PUBLIC_RESOURCE_DIRECTORY, sInstallDir + File.separator + "published_resources");
            propDefaults.setProperty(MEANDRE_PRIVATE_RUN_DIRECTORY, sInstallDir + File.separator + "run");
            propDefaults.setProperty(MEANDRE_CORE_CONFIG_FILE, confCore.toString());
            propDefaults.setProperty(MEANDRE_HOME_DIRECTORY, sInstallDir);
            propDefaults.setProperty(MEANDRE_APP_CONTEXT,"");
            propDefaults.setProperty(MEANDRE_CONDUCTOR_DEFAULT_QUEUE_SIZE,Integer.toString(CONDUCTOR_DEFAULT_QUEUE_SIZE));
        }

        propsCore = new Properties(propDefaults);
        propsCore.setProperty(MEANDRE_BASE_PORT, Integer.toString(port));

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
                propsCore.storeToXML(fos, "Meandre default configuration file (" + Version.getVersion() + ")");
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

    /** Initialize the logging subsystem */
    public void initializeLogging() {
        if (propsCore == null) return;

        Level kernelLogLevel = Level.parse(propsCore.getProperty(MEANDRE_KERNEL_LOG_LEVEL, Level.INFO.getName()));
        Level wsLogLevel = Level.parse(propsCore.getProperty(MEANDRE_WS_LOG_LEVEL, Level.INFO.getName()));

        setGlobalLoggingLevel(kernelLogLevel, wsLogLevel);
    }

    public void setGlobalLoggingLevel(Level kernelLogLevel, Level wsLogLevel) {
        KernelLoggerFactory.setLevel(kernelLogLevel);
        WSLoggerFactory.setLevel(wsLogLevel);
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

    /** Returns the application context were to express Meandre
     *
     * @return The application context
     */
    public String getAppContext () {
        String sCntx = propsCore.getProperty(MEANDRE_APP_CONTEXT);
        return (sCntx==null || sCntx.length()==0 )?"":"/"+sCntx;
    }

    /** Returns the default conductor queue size.
     *
     * @return The default queue size
     */
    public int getConductorDefaultQueueSize() {
        String sDefaultQueueSize = propsCore.getProperty(MEANDRE_CONDUCTOR_DEFAULT_QUEUE_SIZE);
        return (sDefaultQueueSize==null || sDefaultQueueSize.length()==0 )?CONDUCTOR_DEFAULT_QUEUE_SIZE:Integer.parseInt(sDefaultQueueSize);
    }

    /** Compare if two configurations are the same.
     *
     * @param objCnf The configuration to compare
     * @return True if equal, false otherwhise.
     */
    @Override
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
