package org.meandre.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.meandre.core.logger.KernelLoggerFactory;

/** Basic help tools for network manipulation
 * 
 * @author Xaiver Llor&agrave;
 *
 */
public class NetworkTools {
	
	/** Returns the best gest for the host name.
	 * 
	 * @return The host name
	 */
	public static String getLocalHostName () {
		try {
			return 	InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().warning(baos.toString());
		};
		return "localhost";
	}
}
