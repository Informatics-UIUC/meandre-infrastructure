package org.meandre.core.services.coordinator.logger;

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
public class CoordinatorLoggerFactory {

	/** Number of rotating files */
	private static final int LOG_NUM_ROTATING_FILES = 10;

	/** Size of each log file */
	private static final int LOG_FILE_SIZE = 20971520;

	/** The base logger for the coordinator */
	private static Logger logCoord = null;
	
	/** The basic handler for the coordinator logger */
	public static Handler handlerCoord = null;

	// Initializing the logger and its handlers
	static {
		logCoord = Logger.getLogger(CoordinatorLoggerFactory.class.getName());
		logCoord.setLevel(Level.INFO);
		try {
			new File("."+File.separator+"log").mkdir();
			handlerCoord = new FileHandler("."+File.separator+"log"+File.separator+"meandre-coordinator.log",LOG_FILE_SIZE,LOG_NUM_ROTATING_FILES);
			handlerCoord.setFormatter(new CoordinatorFormatter());
			logCoord.addHandler(handlerCoord);
			Logger logger=logCoord.getParent();
			if(logger!=null){
				Handler[] handlerList=logger.getHandlers();
				for(int i=0;i< handlerList.length;i++){
					handlerList[i].setFormatter(new CoordinatorFormatter());
				}	
			}
			Handler[] handlerList=logCoord.getHandlers();
			for(int i=0;i< handlerList.length;i++){
				handlerList[i].setFormatter(new CoordinatorFormatter());
			}
		} catch (SecurityException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-coordinator.log");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-coordinator.log");
			System.exit(1);
		}
		handlerCoord.setLevel(Level.INFO);
	}
	
	/** Returns the core main logger.
	 * 
	 * @return The core logger 
	 */
	public static Logger getCoordinatorLogger() {
		return logCoord;
	}

	/** Set the level to use on for the logger and handler.
	 * 
	 * @param level The requested level
	 */
	public static void setLevel ( Level level ) {
		logCoord.setLevel(level);
		handlerCoord.setLevel(level);
	}
}
