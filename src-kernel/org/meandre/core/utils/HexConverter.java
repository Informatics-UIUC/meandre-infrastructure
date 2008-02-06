package org.meandre.core.utils;

/** This class contains tools to convert Strings to Hex and viceversa.
 * 
 * @author Xavier
 *
 */
public class HexConverter {
	
	/** Given a string it turns it into a string containing its HEX value.
	 * 
	 * @param sString The string to convert
	 * @return The converted string to Hex
	 */
	public static String stringToHex (String sString) {
		StringBuffer sbRes = new StringBuffer();
		
		for ( char c:sString.toCharArray() ) {
			String sHex = Integer.toHexString(c);
			while ( sHex.length()<4 )
				sHex = "0"+sHex;
			sbRes.append(sHex);
		}
		
		return sbRes.toString();
	}
	
	/** Geven a string encoded in HEX returns its string value.
	 * 
	 * @param sHex The HEX string to convert
	 * @return The original string
	 * @throws HexConverterException The reverse converstion failed
	 */
	public static String hexToString (String sHex) 
	throws HexConverterException {
		
		char [] caHex = sHex.toCharArray();
		
		// Sanity check
		if ( (caHex.length%4)!=0 )
			// Wrong length
			throw new HexConverterException("HEX string size not multiple of 4 ("+caHex.length+")");
		
		// Decode
		StringBuffer sbRes = new StringBuffer();
		
		for ( int i=0, iMax=caHex.length ; i<iMax ; i+=4 ) {
			String sHexChar = ""+caHex[i]+caHex[i+1]+caHex[i+2]+caHex[i+3];
			sbRes.append((char)Integer.parseInt(sHexChar, 16));
		}
		
		return sbRes.toString();
	}

}
