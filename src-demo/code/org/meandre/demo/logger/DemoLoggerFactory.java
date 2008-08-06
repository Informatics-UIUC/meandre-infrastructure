package org.meandre.demo.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/** This class implements a disposable logger factory for demo components and flows.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class DemoLoggerFactory {
	
	/** Number of rotating files */
	private static final int LOG_NUM_ROTATING_FILES = 10;

	/** Size of each log file */
	private static final int LOG_FILE_SIZE = 20971520;

	/** The base logger for the demo code */
	private static Logger logDemo = null;
	
	/** The basic handler for all the loggers */
	public static Handler handlerDemo = null;
	
	// Initializing the logger and its handlers
	static {
		logDemo = Logger.getLogger(DemoLoggerFactory.class.getName());
		logDemo.setLevel(Level.FINEST);
		try {
			new File("."+File.separator+"log").mkdir();
			logDemo.addHandler(handlerDemo = new FileHandler("."+File.separator+"log"+File.separator+"meandre-demo.log",LOG_FILE_SIZE,LOG_NUM_ROTATING_FILES));
		} catch (SecurityException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-demo.log");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-demo.log");
			System.exit(1);
		}
		handlerDemo.setLevel(Level.FINEST);
	}
	
	/** Returns the core main logger.
	 * 
	 * @return The core logger 
	 */
	public static Logger getDemoLogger() {
		return logDemo;
	}
	
	/** Set the level to use on for the logger and handler.
	 * 
	 * @param level The requested level
	 */
	public static void setLevel ( Level level ) {
		logDemo.setLevel(level);
		handlerDemo.setLevel(level);
	}
	
}
