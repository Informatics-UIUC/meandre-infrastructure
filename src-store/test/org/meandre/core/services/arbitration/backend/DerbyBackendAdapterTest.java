package org.meandre.core.services.arbitration.backend;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;


/** This class implements test for the Derby Backend Adapter.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class DerbyBackendAdapterTest {

	/** Check that the resource mapping file is there.
	 * 
	 */
	@BeforeClass
	public static void testClassPreparation () {
		InputStream is = DerbyBackendAdapter.class.getResourceAsStream(DerbyBackendAdapter.DERBY_QUERY_MAP_FILE);
		
		assertNotNull(is);
	}
	
	/** Check that the instantiations works properly.
	 * 
	 */
	@Test
	public void testDerbyBackendAdapterInstantiation () {
		
		DerbyBackendAdapter dba = new DerbyBackendAdapter();
		
		assertFalse(dba.getNumberOfQueries()==0);
		assertEquals("Derby", dba.getFlavor());
		assertEquals("@@@", dba.getMarker());
	}
}
