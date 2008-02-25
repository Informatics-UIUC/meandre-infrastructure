package org.meandre.core.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** This class implements the basic factory for loggers.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class LoggerFactory {

	/** Number of rotating files */
	private static final int LOG_NUM_ROTATING_FILES = 10;

	/** Size of each log file */
	private static final int LOG_FILE_SIZE = 20971520;

	/** The base logger for the core */
	private static Logger logCore = null;
	
	/** The basic handler for the core logger */
	public static Handler handlerCore = null;

	/** The base logger for the plugins */
	private static Logger logPlugins = null;
	
	/** The basic handler for the plugins */
	public static Handler handlerPlugins = null;

	// Initializing the logger and its handlers
	static {
		logCore = Logger.getLogger(LoggerFactory.class.getName());
		logCore.setLevel(Level.FINEST);
		logPlugins = Logger.getLogger(LoggerFactory.class.getName());
		logPlugins.setLevel(Level.FINEST);
		try {
			new File("."+File.separator+"log").mkdir();
			logCore.addHandler(handlerCore = new FileHandler("."+File.separator+"log"+File.separator+"meandre-kernel.log",LOG_FILE_SIZE,LOG_NUM_ROTATING_FILES));
		} catch (SecurityException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-kernel.log");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-kernel.log");
			System.exit(1);
		}
		try {
			logPlugins.addHandler(handlerPlugins = new FileHandler("."+File.separator+"log"+File.separator+"meandre-plugins.log",LOG_FILE_SIZE,LOG_NUM_ROTATING_FILES));
		} catch (SecurityException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-plugins.log");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-plugins.log");
			System.exit(1);
		}
		handlerCore.setLevel(Level.FINEST);
		handlerPlugins.setLevel(Level.FINEST);
	}
	
	/** Returns the core main logger.
	 * 
	 * @return The core logger 
	 */
	public static Logger getCoreLogger() {
		return logCore;
	}
	
	/** Returns the plugins main logger.
	 * 
	 * @return The plugins logger
	 */
	public static Logger getPluginsLogger() {
		return logPlugins;
	}
	
}
