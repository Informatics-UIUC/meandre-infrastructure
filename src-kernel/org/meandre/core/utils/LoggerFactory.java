package org.meandre.core.utils;

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
	
	/** The base logger for the core */
	private static Logger logCore = null;
	
	/** The basic handler for all the loggers */
	public static Handler handlerCore = null;
	
	// Initializing the logger and its handlers
	static {
		logCore = Logger.getLogger(LoggerFactory.class.getName());
		logCore.setLevel(Level.FINEST);
		try {
			logCore.addHandler(handlerCore = new FileHandler("meandre-log.xml"));
		} catch (SecurityException e) {
			System.err.println("Could not initialize meandre-log.xml");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize meandre-log.xml");
			System.exit(1);
		}
		handlerCore.setLevel(Level.FINEST);
	}
	
	/** Returns the core main logger.
	 * 
	 * @return The core logger 
	 */
	public static Logger getCoreLogger() {
		return logCore;
	}
	
}
