package org.meandre.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;

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
		
		return sRes.toUpperCase();
	}
	
	/** Returns the string version of the IP of the box where the server 
	 * is running
	 * 
	 * @return The numeric value
	 */
	public static String getStringNameAndIPValue() {
		String sRes = "unknown";
		try {
			InetAddress ip = InetAddress.getLocalHost();
			sRes = ip.toString();
		} catch (UnknownHostException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().warning(baos.toString());
		}
		
		return sRes;
	}
	
	/** Returns the server ID for the current server.
	 * 
	 * @param iPort The port where the server is running
	 * @return The ID
	 */
	public static String getServerID (int iPort) {
		return NetworkTools.getNumericIPValue()+Integer.toHexString(iPort).toUpperCase();
	}
	
	/** Returns the MD5 hash for the given String.
	 * 
	 * @param sString The string to use
	 * @return The stringfied has value
	 */
	public static String getMD5 ( String sString ) {
		try {
			 MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			 digest.update(sString.getBytes());
			 String sRes ="";
			 for ( byte b:digest.digest())
				 sRes += Integer.toHexString(b).toUpperCase();
			 return sRes;
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().warning(baos.toString());
			return null;
		}
	}
}
