package org.meandre.webservices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.meandre.webservices.utils.WSLoggerFactory;


/**
 * Bootstraps a Meandre execution engine.
 *
 * @author Xavier Llor&agrave;
 * @modified Amit Kumar: Added Security Manager
 * @modified Amit Kumar: Added plugins.
 *
 */
public class WSCoreBootstrapper {

	/** The base URL for Meandre WS */
	public final static String WS_BASE_URL = "http://meandre.org/services/";

	/** The base directory for Jetty */
	public static final String JETTY_HOME = ".";

	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();

	/** The basic handler for all the loggers */
	public static Handler handler = null;

	/**
	 * Boostraps the Meandre execution engine.
	 *
	 * @param args
	 *            Command line arguments
	 * @throws Exception
	 *             Something went wrong, really wrong.
	 */
	public static void main(String[] args) throws Exception {
		log.config("Bootstrapping Menadre Workflow Engine");

		log.config("Installing MeandreSecurityManager");
		
		// Had to disable to allow the JMX thing to work
//		if( System.getSecurityManager() == null )
//		    System.setSecurityManager( new MeandreSecurityManager() );
		
		try {
			log.config("Starting Jetty server");
			
			setJMXProperties();
			
			MeandreServer ms = new MeandreServer();
			ms.start();
		}
		catch (AccessControlException e) {
			throw e;
		}
	}

	/** Sets the basic policies for the JMX to work.
	 * 
	 * @throws IOException Could not dump the required files
	 * 
	 */
	private static void setJMXProperties() throws IOException {
		
		// Dumping the required jmxremote.access file
		File fja = new File("."+File.separator+"jmxremote.access");
		PrintWriter pwa = new PrintWriter(new FileWriter(fja));
		pwa.println("monitorRole readonly");
		pwa.println("controlRole readwrite"); 
		pwa.close();
		
		// Dumping the required jmxremote.password file
		File fjp = new File("."+File.separator+"jmxremote.password");
		PrintWriter pwp = new PrintWriter(new FileWriter(fjp));
		pwp.println("monitorRole  jmxAdmin");
		pwp.println("controlRole  jmxAdmin"); 
		pwp.close();
		
		
		// Dumping the policy file
		String sJavaHome = System.getProperty("java.home");
		File fjpf = new File("."+File.separator+"java.policy");
		PrintWriter pwpf = new PrintWriter(new FileWriter(fjpf));
		pwpf.println("grant codeBase \"file:"+sJavaHome+"/lib/ext/*\" {"); 
		pwpf.println("        permission java.security.AllPermission;"); 
		pwpf.println("};"); 
		pwpf.println(); 
		pwpf.println("grant {"); 
		pwpf.println("        permission java.security.AllPermission;"); 
		pwpf.println("};"); 
		pwpf.close();
		
		//System.setProperty("com.sun.management.jmxremote", com.sun.management.jmx)
		System.setProperty("com.sun.management.jmxremote.password.file", "jmxremote.password");
		System.setProperty("java.security.policy","java.policy");
		
	}

	


}
