package org.meandre.webservices.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.core.logger.MeandreFormatter;



/** This class implements a disposable logger factory for demo components and flows.
 * 
 * @author Xavier Llor&agrave;
 * @modified Amit Kumar -Added Formatter
 */
public class WSLoggerFactory {
	

	/** Number of rotating files */
	private static final int LOG_NUM_ROTATING_FILES = 10;

	/** Size of each log file */
	private static final int LOG_FILE_SIZE = 20971520;

	/** The base logger for the demo code */
	private static Logger logWS = null;
	
	/** The basic handler for all the loggers */
	public static Handler handlerWS = null;
	
	// Initializing the logger and its handlers
	static {
		logWS = Logger.getLogger(WSLoggerFactory.class.getName());
		logWS.setLevel(Level.INFO);
		try {
			new File("."+File.separator+"log").mkdir();
			handlerWS = new FileHandler("."+File.separator+"log"+File.separator+"meandre-webservices.log",LOG_FILE_SIZE,LOG_NUM_ROTATING_FILES);
			handlerWS.setFormatter(new MeandreFormatter());
			logWS.addHandler(handlerWS);
			Logger logger=logWS.getParent();
			if(logger!=null){
				Handler[] handlerList=logger.getHandlers();
				for(int i=0;i< handlerList.length;i++){
					handlerList[i].setFormatter(new MeandreFormatter());
				}	
			}
			Handler[] handlerList=logWS.getHandlers();
			for(int i=0;i< handlerList.length;i++){
				handlerList[i].setFormatter(new MeandreFormatter());
			}
		} catch (SecurityException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-webservices.log");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-webservices.log");
			System.exit(1);
		}
		handlerWS.setLevel(Level.INFO);
	}
	
	/** Returns the core main logger.
	 * 
	 * @return The core logger 
	 */
	public static Logger getWSLogger() {
		return logWS;
	}
	
	/** Set the level to use on for the logger and handler.
	 * 
	 * @param level The requested level
	 */
	public static void setLevel ( Level level ) {
		logWS.setLevel(level);
		handlerWS.setLevel(level);
	}
}
