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

import org.meandre.core.logger.PluginsLoggerFactory;
import org.meandre.core.store.Store;
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
	private  static String sConfigPath = ".";

	/** The default configuration file name */
	private final static String sConfigFile = "meandre-plugins.xml";

	/** The plugins logger */
	private static Logger log = PluginsLoggerFactory.getPluginsLogger();
	
	/** The plugin factory store */
	private static Properties propPluginFactoryConfig;

	static {
		propPluginFactoryConfig = new Properties();
	    FileInputStream fis;
		try {
			//
			// Load the properties from the default location
			//
			fis = new FileInputStream(sConfigPath+File.separator+sConfigFile);
			propPluginFactoryConfig.loadFromXML(fis);
			fis.close();
		} catch (Exception eRead) {
			//
			// The file could not be loaded
			// Creating a default one
			//
			log.warning("Meandre configuration file "+
					    sConfigPath+File.separator+sConfigFile+
					    " could not be loaded. Creating a default one.");

			initializePluginsDefaultProperties();
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(sConfigPath+File.separator+sConfigFile);
				propPluginFactoryConfig.storeToXML(fos, "Meandre default plugins configuration file ("+Constants.MEANDRE_VERSION+")");
			    fos.close();
			} catch (Exception eWrite) {
				log.warning("Meandre plugins configuration file "+
					    sConfigPath+File.separator+sConfigFile+
					    " could not be written to disk!");
			}
		}

		// Create the run file
		new File(sConfigPath+File.separator+Store.getRunResourcesDirectory()).mkdir();

	}

	/** Initialize the default properties. The default properties include the global VFS.
	 * 
	 */
	private static void initializePluginsDefaultProperties() {
		propPluginFactoryConfig.setProperty("VFS", VFSServlet.class.getName());	
		propPluginFactoryConfig.setProperty("JARTOOL", JarToolServlet.class.getName());		
	}

	/** Initialize the public file server for shared resources
	 *
	 * @param server The server to user
	 * @param log The logger to use
	 */
	public static void initializeGlobalPublicFileServer(Server server, Logger log) {
		//
		// Initializing the public file server
		//
		Context contextResources = new Context(server,"/public/resources",Context.NO_SESSIONS);

		File file = new File(Store.getPublicResourcesDirectory());

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
	public static void initializeGlobalCorePlugins(Server server, Context cntxGlobal, Logger log) {
		//
		// Initializing the public file server
		//
		//Context contextPlugins = new Context(server,"/plugins/",Context.NO_SESSIONS);
		
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
		return propPluginFactoryConfig;
	}

}
