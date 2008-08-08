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
 * @modified -Amit Kumar added Formatter
 */
public class PluginsLoggerFactory {

	/** Number of rotating files */
	private static final int LOG_NUM_ROTATING_FILES = 10;

	/** Size of each log file */
	private static final int LOG_FILE_SIZE = 20971520;

	/** The base logger for the plugins */
	private static Logger logPlugins = null;
	
	/** The basic handler for the plugins */
	public static Handler handlerPlugins = null;

	// Initializing the logger and its handlers
	static {
		logPlugins = Logger.getLogger(PluginsLoggerFactory.class.getName());
		logPlugins.setLevel(Level.INFO);
		try {
			handlerPlugins = new FileHandler("."+File.separator+"log"+File.separator+"meandre-plugins.log",LOG_FILE_SIZE,LOG_NUM_ROTATING_FILES);
			handlerPlugins.setFormatter(new MeandreFormatter());
			logPlugins.addHandler(handlerPlugins);
			Logger logger=logPlugins.getParent();
			if(logger!=null){
				Handler[] handlerList=logger.getHandlers();
				for(int i=0;i< handlerList.length;i++){
					handlerList[i].setFormatter(new MeandreFormatter());
				}	
			}
			Handler[] handlerList=logPlugins.getHandlers();
			for(int i=0;i< handlerList.length;i++){
				handlerList[i].setFormatter(new MeandreFormatter());
			}
		} catch (SecurityException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-plugins.log");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-plugins.log");
			System.exit(1);
		}
		handlerPlugins.setLevel(Level.INFO);
	}
	
	/** Returns the plugins main logger.
	 * 
	 * @return The plugins logger
	 */
	public static Logger getPluginsLogger() {
		return logPlugins;
	}
	
}
