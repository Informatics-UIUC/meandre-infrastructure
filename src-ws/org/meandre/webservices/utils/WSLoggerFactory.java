package org.meandre.webservices.utils;

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
public class WSLoggerFactory {
	
	/** The base logger for the demo code */
	private static Logger logWS = null;
	
	/** The basic handler for all the loggers */
	public static Handler handlerWS = null;
	
	// Initializing the logger and its handlers
	static {
		logWS = Logger.getLogger(WSLoggerFactory.class.getName());
		logWS.setLevel(Level.FINEST);
		try {
			logWS.addHandler(handlerWS = new FileHandler("meandre-log-ws.xml"));
		} catch (SecurityException e) {
			System.err.println("Could not initialize meandre-log-ws.xml");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize meandre-log-ws.xml");
			System.exit(1);
		}
		handlerWS.setLevel(Level.FINEST);
	}
	
	/** Returns the core main logger.
	 * 
	 * @return The core logger 
	 */
	public static Logger getWSLogger() {
		return logWS;
	}
	
}
