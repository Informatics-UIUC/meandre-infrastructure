package org.meandre.core.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** This class implements the basic factory for loggers.
 *
 * @author Xavier Llor&agrave; (modified -Amit Kumar added Formatter)
 */
public class KernelLoggerFactory {

	/** Number of rotating files */
	private static final int LOG_NUM_ROTATING_FILES = 10;

	/** Size of each log file */
	private static final int LOG_FILE_SIZE = 20971520;

	/** The base logger for the core */
	private static Logger logCore = null;

	/** The basic handler for the core logger */
	public static Handler handlerCore = null;

	// Initializing the logger and its handlers
	static {
		logCore = Logger.getLogger(KernelLoggerFactory.class.getName());
		logCore.setLevel(Level.INFO);
		try {
			new File("."+File.separator+"log").mkdir();
			handlerCore = new FileHandler("."+File.separator+"log"+File.separator+"meandre-kernel.log",LOG_FILE_SIZE,LOG_NUM_ROTATING_FILES);
			handlerCore.setFormatter(new MeandreFormatter());
			logCore.addHandler(handlerCore);
			Logger logger=logCore.getParent();
			if(logger!=null){
				Handler[] handlerList=logger.getHandlers();
				for(int i=0;i< handlerList.length;i++){
					handlerList[i].setFormatter(new MeandreFormatter());
				}
			}
			Handler[] handlerList=logCore.getHandlers();
			for(int i=0;i< handlerList.length;i++){
				handlerList[i].setFormatter(new MeandreFormatter());
			}
		} catch (SecurityException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-kernel.log");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-kernel.log");
			System.exit(1);
		}
		handlerCore.setLevel(Level.INFO);
	}

	/** Returns the core main logger.
	 *
	 * @return The core logger
	 */
	public static Logger getCoreLogger() {
		return logCore;
	}

	/** Set the level to use on for the logger and handler.
	 *
	 * @param level The requested level
	 */
	public static void setLevel ( Level level ) {
	    logCore.info(String.format("Changing the logging level from %s to %s", logCore.getLevel().getName(), level.getName()));
		logCore.setLevel(level);
		handlerCore.setLevel(level);
	}

}
