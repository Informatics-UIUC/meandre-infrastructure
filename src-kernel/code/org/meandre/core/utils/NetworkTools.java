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
	
	/** Returns the numeric version of the IP of the box where the server 
	 * is running
	 * 
	 * @return The numeric value
	 */
	public static String getNumericIPValue() {
		String sRes = "unknown";
		try {
			InetAddress ip = InetAddress.getLocalHost();
			byte [] ba = ip.getAddress();
			sRes = "";
			for ( byte b:ba ) {
				String sTmp = Integer.toHexString(b);
				if ( sTmp.length()<2 )
					sTmp = "0"+sTmp;
				else 
					sTmp = sTmp.substring(sTmp.length()-2);
				sRes += sTmp;
			}
		} catch (UnknownHostException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().warning(baos.toString());
		}
		
		return sRes;
	}
}
