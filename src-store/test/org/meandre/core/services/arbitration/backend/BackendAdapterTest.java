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
public class BackendAdapterTest {

	/** Contains the list of backend addapters to test */
	protected static String [] sbaNames = { 
		DerbyBackendAdapter.class.getName(), 
		MySQLBackendAdapter.class.getName(),
	};
	
	/** Check that the resource mapping file is there.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void testClassPreparation () {
		// Check the base adapter
		try {
			InputStream dis = BackendAdapter.class.getResourceAsStream(BackendAdapter.COMMON_MAP_FILE);
			assertNotNull(dis);
			if ( dis!=null )
				dis.close();
		} catch (Exception e) {
			fail("This exception should have not be thrown!!!\n"+e.toString());
		}
		
		// Check the flavored adapters
		for ( String sCN:sbaNames ) {
			try {
				Class c = Class.forName(sCN);
				String[] sCNa = sCN.split("\\"+".");
				String sPropFile = sCNa[sCNa.length-1].replaceAll("BackendAdapter", "").toLowerCase();
				sPropFile = "query_map_"+sPropFile+".xml";
				InputStream dis = c.getResourceAsStream(sPropFile);
				assertNotNull(dis);
				if ( dis!=null )
					dis.close();
			} catch (Exception e) {
				fail("This exception should have not be thrown!!!\n"+e.toString());
			}
		}
	}
	
	/** Check that the instantiations works properly.
	 * 
	 */
	@Test
	public void testBackendAdapterInstantiation () {
		for ( String sCN:sbaNames )
			instantiateBackendAdapter(sCN);
	}
	
	/** Tries to instanciate a backend of the given class.
	 * 
	 * @param The class name to check
	 */
	public void instantiateBackendAdapter ( String sCN ) {
		
		try {
			BackendAdapter ba = (BackendAdapter) Class.forName(sCN).newInstance();
			String[] sCNa = sCN.split("\\"+".");
			String sCNClassName = sCNa[sCNa.length-1];
			
			assertFalse(ba.getNumberOfQueries()==0);
			assertTrue(sCNClassName.startsWith(ba.getFlavor()));
			assertEquals("@@@", ba.getMarker());	
			
		} catch (Exception e) {
			fail("This exception should have not be thrown!!!\n"+e.toString());
		}
	}
}
