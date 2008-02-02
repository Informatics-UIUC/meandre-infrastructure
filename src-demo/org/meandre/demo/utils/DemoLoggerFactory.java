package org.meandre.demo.utils;

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
	
	/** The base logger for the demo code */
	private static Logger logDemo = null;
	
	/** The basic handler for all the loggers */
	public static Handler handlerDemo = null;
	
	// Initializing the logger and its handlers
	static {
		logDemo = Logger.getLogger(DemoLoggerFactory.class.getName());
		logDemo.setLevel(Level.FINEST);
		try {
			logDemo.addHandler(handlerDemo = new FileHandler("meandre-log-demo.xml"));
		} catch (SecurityException e) {
			System.err.println("Could not initialize meandre-log-demo.xml");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize meandre-log-demo.xml");
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
