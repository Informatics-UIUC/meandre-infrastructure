package org.meandre.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.logger.PluginsLoggerFactory;
import org.meandre.core.utils.Constants;
import org.meandre.plugins.tools.JarToolServlet;
import org.meandre.plugins.vfs.VFSServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**This class provides a basic factory for managing plugins.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class PluginFactory {

	/** The default configuration path to the file name */
	private String sConfigPath = ".";

	/** The default configuration file name */
	private final String sConfigFile = "meandre-config-plugins.xml";

	/** The plugins logger */
	private Logger log = PluginsLoggerFactory.getPluginsLogger();
	
	/** The plugin factory store */
	private static Properties propPluginFactoryConfig;

	/** The core configuration object */
	private CoreConfiguration cnf;

	/** Creates an unitialized plugin factory and avoids other to instantiate it.
	 * 
	 */
	private PluginFactory (CoreConfiguration crcnf) {
		cnf = crcnf;
		sConfigPath = cnf.getHomeDirectory();
	}
	
	/** Initialize the plugin factory given a certain core configuration.
	 * 
	 * @param cnf The core configuration object
	 */
	public static PluginFactory getPluginFactory ( CoreConfiguration cnf ) {
		PluginFactory pf = new PluginFactory(cnf);
		
		pf.propPluginFactoryConfig = new Properties();
	    FileInputStream fis;
	    String sConfigFilePath = pf.sConfigPath + File.separator + pf.sConfigFile;
		try {
			//
			// Load the properties from the default location
			//
			fis = new FileInputStream(sConfigFilePath);
			pf.propPluginFactoryConfig.loadFromXML(fis);
			fis.close();
		} catch (Exception eRead) {
			//
			// The file could not be loaded
			// Creating a default one
			//
			pf.log.warning("Meandre configuration file "+
					sConfigFilePath+
					" could not be loaded. Creating a default one.");

			PluginFactory.initializePluginsDefaultProperties(pf);
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(sConfigFilePath);
				pf.propPluginFactoryConfig.storeToXML(fos, "Meandre default plugins configuration file ("+Constants.MEANDRE_VERSION+")");
			    fos.close();
			} catch (Exception eWrite) {
				pf.log.warning("Meandre plugins configuration file "+
						sConfigFilePath+
					    " could not be written to disk!");
			}
		}

		// Create the run file
		new File(pf.sConfigPath+File.separator+pf.cnf.getRunResourcesDirectory()).mkdir();

		return pf;
	}

	/** Initialize the default properties. The default properties include the global VFS.
	 * 
	 * @param pf  The plugin factory being initialized
	 */
	private static void initializePluginsDefaultProperties(PluginFactory pf) {
		pf.propPluginFactoryConfig.setProperty("VFS", VFSServlet.class.getName());	
		pf.propPluginFactoryConfig.setProperty("JARTOOL", JarToolServlet.class.getName());		
	}

	/** Initialize the public file server for shared resources
	 *
	 * @param server The server to user
	 * @param log The logger to use
	 */
	public void initializeGlobalPublicFileServer(Server server, Logger log) {
		//
		// Initializing the public file server
		//
		Context contextResources = new Context(server,"/public/resources",Context.NO_SESSIONS);

		File file = new File(cnf.getPublicResourcesDirectory());

		if ( file.mkdir() ) {
			try {
				PrintStream ps = new PrintStream(new FileOutputStream(file.getAbsolutePath()+File.separator+"readme.txt"));
				ps.println("Meandre Execution Engine version "+Constants.MEANDRE_VERSION);
				ps.println("All rigths reserved by DITA, NCSA, UofI (2008).");
				ps.println("2007-2008. All rigths reserved by DITA, NCSA, UofI.");
				ps.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
				ps.println();
				ps.println("This directory contains all the publicly available implementations for the Meandre components.");
				ps.println();
				ps.println("Created on "+new Date());
				log.warning("Resource directory not existing. Initializing a new one.");
			} catch (FileNotFoundException e) {
				log.warning("Could not initialize the resource directory");
			}
		}

		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setCacheControl("no-cache");
		resource_handler.setResourceBase(file.getAbsolutePath());
		contextResources.setHandler(resource_handler);
	}

	
	/** Initialize the engine plugins to use.
	 * 
	 * @param server The server where the engine plugins run
	 * @param cntxGlobal The global context for the web services
	 * @param log The logger to use
	 */
	public void initializeGlobalCorePlugins(Server server, Context cntxGlobal, Logger log) {
		//
		// Initializing the public file server
		//
		
		for ( Object oKey:propPluginFactoryConfig.keySet()) {
			try {
				String sClassName = propPluginFactoryConfig.getProperty(oKey.toString());
				MeandrePlugin mpPlugin = (MeandrePlugin) Class.forName(sClassName).newInstance();
				if ( mpPlugin.isServlet() ) {
					mpPlugin.setLogger(log);
					cntxGlobal.addServlet(new ServletHolder((Servlet)mpPlugin), mpPlugin.getAlias());
				}
			} catch (InstantiationException e) {
				log.warning("Pluggin "+oKey+" could not be initialized\n"+e);
			} catch (IllegalAccessException e) {
				log.warning("Pluggin "+oKey+" could not be initialized\n"+e);
			} catch (ClassNotFoundException e) {
				log.warning("Pluggin "+oKey+" could not be initialized\n"+e);
			}
			
		}
	}


	/**
	 * @return the propPluginFactoryConfig
	 */
	public static Properties getPropPluginFactoryConfig() {
		return  propPluginFactoryConfig;
	}

}
