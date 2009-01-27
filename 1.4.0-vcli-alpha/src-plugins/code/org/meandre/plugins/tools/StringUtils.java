/*
 * @(#) StringUtils.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.plugins.tools;

import java.io.UnsupportedEncodingException;

public class StringUtils {
	 static final byte[] HEX_CHAR_TABLE = {
		    (byte)'0', (byte)'1', (byte)'2', (byte)'3',
		    (byte)'4', (byte)'5', (byte)'6', (byte)'7',
		    (byte)'8', (byte)'9', (byte)'a', (byte)'b',
		    (byte)'c', (byte)'d', (byte)'e', (byte)'f'
		  };    

		  public static String getHexString(byte[] raw) 
		    throws UnsupportedEncodingException 
		  {
		    byte[] hex = new byte[2 * raw.length];
		    int index = 0;

		    for (byte b : raw) {
		      int v = b & 0xFF;
		      hex[index++] = HEX_CHAR_TABLE[v >>> 4];
		      hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		    }
		    return new String(hex, "ASCII");
		  }
}
