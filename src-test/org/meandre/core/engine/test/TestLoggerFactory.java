package org.meandre.core.engine.test;

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
public class TestLoggerFactory {
	
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
		logDemo = Logger.getLogger(TestLoggerFactory.class.getName());
		logDemo.setLevel(Level.FINEST);
		try {
			new File("."+File.separator+"log").mkdir();
			logDemo.addHandler(handlerDemo = new FileHandler("."+File.separator+"log"+File.separator+"meandre-test.log",LOG_FILE_SIZE,LOG_NUM_ROTATING_FILES));
		} catch (SecurityException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-test.log");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-tes.log");
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
	
}
