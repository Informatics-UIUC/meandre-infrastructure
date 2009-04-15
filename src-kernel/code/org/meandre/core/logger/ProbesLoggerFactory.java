package org.meandre.core.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** This class implements the basic factory for probes.
 * 
 * @author Xavier Llor&agrave; 
 */
public class ProbesLoggerFactory {

	/** Number of rotating files */
	private static final int LOG_NUM_ROTATING_FILES = 10;

	/** Size of each log file */
	private static final int LOG_FILE_SIZE = 20971520;

	/** The base logger for the probes */
	private static Logger logProbes = null;
	
	/** The basic handler for the probes */
	public static Handler handlerProbes = null;

	// Initializing the logger and its handlers
	static {
		logProbes = Logger.getLogger(ProbesLoggerFactory.class.getName());
		logProbes.setLevel(Level.INFO);
		try {
			handlerProbes = new FileHandler("."+File.separator+"log"+File.separator+"meandre-probes.log",LOG_FILE_SIZE,LOG_NUM_ROTATING_FILES);
			handlerProbes.setFormatter(new MeandreFormatter());
			logProbes.addHandler(handlerProbes);
			Logger logger=logProbes.getParent();
			if(logger!=null){
				Handler[] handlerList=logger.getHandlers();
				for(int i=0;i< handlerList.length;i++){
					handlerList[i].setFormatter(new MeandreFormatter());
				}	
			}
			Handler[] handlerList=logProbes.getHandlers();
			for(int i=0;i< handlerList.length;i++){
				handlerList[i].setFormatter(new MeandreFormatter());
			}
		} catch (SecurityException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-probes.log");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize "+"."+File.separator+"log"+File.separator+"meandre-probes.log");
			System.exit(1);
		}
		handlerProbes.setLevel(Level.INFO);
	}
	
	/** Returns the plugins main logger.
	 * 
	 * @return The plugins logger
	 */
	public static Logger getProbesLogger() {
		return logProbes;
	}
	
}
