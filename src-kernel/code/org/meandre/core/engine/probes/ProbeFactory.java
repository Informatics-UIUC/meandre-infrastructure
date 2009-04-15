package org.meandre.core.engine.probes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.Probe;
import org.meandre.core.engine.ProbeException;
import org.meandre.core.logger.ProbesLoggerFactory;
import org.meandre.core.utils.Constants;

/**This class provides a basic factory for managing probes.
*
* @author Xavier Llor&agrave;
*/
public class ProbeFactory {


	/** The default configuration path to the file name */
	private String sConfigPath = ".";

	/** The default configuration file name */
	private final String sConfigFile = "meandre-config-probes.xml";

	/** The probe logger */
	private Logger log = ProbesLoggerFactory.getProbesLogger();

	/** The probe factory store */
	private static Properties propProbeFactoryConfig;

	/** The core configuration object */
	private CoreConfiguration cnf;
	
	/** Creates an uninitialized plugin factory and avoids other to instantiate it.
	 *
	 */
	private ProbeFactory (CoreConfiguration crcnf) {
		cnf = crcnf;
		sConfigPath = cnf.getHomeDirectory();
	}

	/** Stores the PluginFactory**/
	private static HashMap<CoreConfiguration, ProbeFactory> probeFactoryList = new HashMap<CoreConfiguration, ProbeFactory>(5);

	/** Initialize the probe factory given a certain core configuration.
	 *
	 * @param cnf The core configuration object
	 */
	public static ProbeFactory getProbeFactory ( CoreConfiguration cnf ) {
		// check the hashmap for the pluginfactory
		if(probeFactoryList.get(cnf)!=null){
			return probeFactoryList.get(cnf);
		}
		ProbeFactory pf = new ProbeFactory(cnf);

		ProbeFactory.propProbeFactoryConfig = new Properties();
	    FileInputStream fis;
	    String sConfigFilePath = pf.sConfigPath + File.separator + pf.sConfigFile;
		try {
			//
			// Load the properties from the default location
			//
			fis = new FileInputStream(sConfigFilePath);
			ProbeFactory.propProbeFactoryConfig.loadFromXML(fis);
			fis.close();
		} catch (Exception eRead) {
			//
			// The file could not be loaded
			// Creating a default one
			//
			pf.log.warning("Meandre configuration file "+
					sConfigFilePath+
					" could not be loaded. Creating a default one.");

			ProbeFactory.initializeProbesDefaultProperties(pf);
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(sConfigFilePath);
				ProbeFactory.propProbeFactoryConfig.storeToXML(fos, "Meandre default probes configuration file ("+Constants.MEANDRE_VERSION+")");
			    fos.close();
			} catch (Exception eWrite) {
				pf.log.warning("Meandre plugins configuration file "+
						sConfigFilePath+
					    " could not be written to disk!");
			}

		}

		// Create the run file
		new File(pf.sConfigPath+File.separator+pf.cnf.getRunResourcesDirectory()).mkdir();

		//store the plugin factory in the hashmap and return when asked for
		probeFactoryList.put(cnf, pf);
		return pf;
	}
	
	/** Initialize the default properties. .
	 *
	 * @param pf  The probe factory being initialized
	 */
	private static void initializeProbesDefaultProperties(ProbeFactory pf) {
		ProbeFactory.propProbeFactoryConfig.setProperty("rdf", MeandreRDFDialectProbeImpl.class.getName());
		ProbeFactory.propProbeFactoryConfig.setProperty("null", NullProbeImpl.class.getName());
		ProbeFactory.propProbeFactoryConfig.setProperty("statistics", StatisticsProbeImpl.class.getName());
		ProbeFactory.propProbeFactoryConfig.setProperty("console", ToPrintStreamProbeImpl.class.getName());
	}
	

	/** Return a new instance of the requested probe
	 *
	 * @param sID The id of the probe
	 * @return Returns the probe
	 * @throws ProbeException The requested probe could not be instantiated and initialized
	 */
	public Probe getProbe(String sID) throws ProbeException{
		String sClassName = ProbeFactory.propProbeFactoryConfig.getProperty(sID);
		if ( sClassName==null ) return null;
		try {
			Probe p =  (Probe)(Class.forName(sClassName).newInstance());
			p.initialize();
			return p;
		}
		catch ( Throwable t ) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			ps.println("Faile to instantiate probe "+sID) ;
			t.printStackTrace(ps);
			log.warning(baos.toString());
			throw new ProbeException(t);
		}		
	}

	/** Returns the properties of the probes factory.
	 *
	 * @return The properties of the factory
	 */
	public Properties getPropPluginFactoryConfig() {
		return  propProbeFactoryConfig;
	}

}
