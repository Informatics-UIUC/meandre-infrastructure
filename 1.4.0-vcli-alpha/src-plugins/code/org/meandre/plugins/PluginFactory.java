package org.meandre.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.logger.PluginsLoggerFactory;
import org.meandre.core.utils.Constants;
import org.meandre.plugins.tools.JarToolServlet;
import org.meandre.plugins.vfs.VFSServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;

/**This class provides a basic factory for managing plugins.
 *
 * @author Xavier Llor&agrave;
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

	/** Files to transfer to the public directory */
	private final static String [] saResources = { "logo-meandre.gif" };
	
	/** The target directory insed the public resources dir */
	private final static String sRescoureDestination = "system/";
		
	/** Creates an unitialized plugin factory and avoids other to instantiate it.
	 *
	 */
	private PluginFactory (CoreConfiguration crcnf) {
		cnf = crcnf;
		sConfigPath = cnf.getHomeDirectory();
	}

	/** Store the plugin information**/
	private static HashMap<String,MeandrePlugin> pluginMap = new HashMap<String,MeandrePlugin>(5);

	/** Stores the PluginFactory**/
	private static HashMap<CoreConfiguration, PluginFactory> pluginFactoryList = new HashMap<CoreConfiguration, PluginFactory>(5);

	/** Initialize the plugin factory given a certain core configuration.
	 *
	 * @param cnf The core configuration object
	 */
	public static PluginFactory getPluginFactory ( CoreConfiguration cnf ) {
		// check the hashmap for the pluginfactory
		if(pluginFactoryList.get(cnf)!=null){
			return pluginFactoryList.get(cnf);
		}
		PluginFactory pf = new PluginFactory(cnf);

		PluginFactory.propPluginFactoryConfig = new Properties();
	    FileInputStream fis;
	    String sConfigFilePath = pf.sConfigPath + File.separator + pf.sConfigFile;
		try {
			//
			// Load the properties from the default location
			//
			fis = new FileInputStream(sConfigFilePath);
			PluginFactory.propPluginFactoryConfig.loadFromXML(fis);
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
				PluginFactory.propPluginFactoryConfig.storeToXML(fos, "Meandre default plugins configuration file ("+Constants.MEANDRE_VERSION+")");
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
		pluginFactoryList.put(cnf, pf);
		return pf;
	}

	/** Initialize the default properties. The default properties include the global VFS.
	 *
	 * @param pf  The plugin factory being initialized
	 */
	private static void initializePluginsDefaultProperties(PluginFactory pf) {
		PluginFactory.propPluginFactoryConfig.setProperty("VFS", VFSServlet.class.getName());
		PluginFactory.propPluginFactoryConfig.setProperty("JARTOOL", JarToolServlet.class.getName());
	}

	/** Initialize the public file server for shared resources
	 *
	 * @param server The server to user
	 * @param log The logger to use
	 * @param cnf The core configuration object
	 */
	public static void initializeGlobalPublicFileServer(Server server, Logger log, CoreConfiguration cnf) {
		//
		// Initializing the public file server
		//
		Context contextResources = new Context(server,"/public/resources",Context.NO_SESSIONS);

		File file = new File(cnf.getPublicResourcesDirectory());

		if ( file.mkdir() ) {
			try {
				PrintStream ps = new PrintStream(new FileOutputStream(file.getAbsolutePath()+File.separator+"readme.txt"));
				ps.println("Meandre Execution Engine version "+Constants.MEANDRE_VERSION);
				ps.println("All rights reserved by DITA, NCSA, UofI (2007-2009)");
				ps.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
				ps.println();
				ps.println("This directory contains all the publicly available implementations for the Meandre components.");
				ps.println();
				ps.println("Created on "+new Date());
				
				log.warning("The resource directory does not exist - creating a new one...");
				
			} catch (FileNotFoundException e) {
				log.warning("Could not create the resource directory");
			}
		}
		
		File fileResDir = new File(file.getAbsolutePath()+File.separator+sRescoureDestination);
		if  ( fileResDir.mkdir() )  {
			try {
				for ( String sFile:saResources ) {
					InputStream fis = PluginFactory.class.getResourceAsStream(sFile);
					FileOutputStream fos = new FileOutputStream(fileResDir.getAbsolutePath()+File.separator+sFile);
					int iTmp;
					while ( (iTmp=fis.read())!=-1 )
						fos.write(iTmp);
					fos.close();
					fis.close();
				}
				
				log.warning("The system resources missing. Adding them...");
			} catch (FileNotFoundException e) {
				log.warning("Could not create the resource directory");
			} catch (IOException e) {
				log.warning("Could not unpack public system resources");
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
				}else if(mpPlugin.isFilter()){
					mpPlugin.setLogger(log);
					cntxGlobal.addFilter(new FilterHolder((Filter)mpPlugin), mpPlugin.getAlias(), org.mortbay.jetty.Handler.DEFAULT);
				}
				mpPlugin.inited(Boolean.TRUE);
				pluginMap.put(oKey.toString(), mpPlugin);
			} catch (InstantiationException e) {
				log.warning("Pluggin "+oKey+" could not be initialized\n"+e);
			} catch (IllegalAccessException e) {
				log.warning("Pluggin "+oKey+" could not be initialized\n"+e);
			} catch (ClassNotFoundException e) {
				log.warning("Pluggin "+oKey+" could not be initialized\n"+e);
			}

		}
	}


	/** Return the plugin give its ID
	 *
	 * @param sID The id of the plugin
	 * @return Returns the plugin
	 */
	public MeandrePlugin getPlugin(String sID){
		return pluginMap.get(sID);
	}

	/** Returns the properties of the plugin factory.
	 *
	 * @return The properties of the factory
	 */
	public Properties getPropPluginFactoryConfig() {
		return  propPluginFactoryConfig;
	}

}
