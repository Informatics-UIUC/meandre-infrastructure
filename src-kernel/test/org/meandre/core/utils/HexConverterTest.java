package org.meandre.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

/** Thic class runs some tests on the HEX converter.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class HexConverterTest {

	/** The random number generator used to generate strings */
	private static Random rnd = new Random(69);
	
	/** Test the conversion from String to HEX and viceversa. 
	 * 
	 */
	@Test
	public void testHexConversion () {
		final int REPETITIONS = 100;
		final int MIN_LENGTH = 10;
		final int MAX_LENGTH = 246;
		
		try {
			for ( int i=0 ; i<REPETITIONS ; i++ ) {
				int iLen = MIN_LENGTH+rnd.nextInt(MAX_LENGTH);
				StringBuffer sb = new StringBuffer();
				for ( int j=0 ; j<iLen ; sb.append((char)(rnd.nextInt()%65536)),j++ );
				String sOrig = sb.toString();
				String sHex = HexConverter.stringToHex(sOrig);
				assertEquals(0,sHex.length()%4);
				String sRec = HexConverter.hexToString(sHex);
				assertEquals(sOrig,sRec);
			}
		}
		catch ( HexConverterException e ) {
			fail(e.toString());
		}
	}
	

}
