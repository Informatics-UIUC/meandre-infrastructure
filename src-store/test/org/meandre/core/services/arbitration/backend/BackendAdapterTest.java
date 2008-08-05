package org.meandre.core.services.arbitration.backend;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.store.Store;


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
	private void instantiateBackendAdapter ( String sCN ) {
		
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
	
	/** Gets and adapter based on the current store configuration object.
	 * 
	 */
	private BackendAdapter createBackendAdaptorFromStore () {
		CoreConfiguration cnf = new CoreConfiguration();
		Store store = new Store();
		
		try {
			// Instantiate the adaptor
			BackendAdapter ba = (BackendAdapter) Class.forName(
					"org.meandre.core.services.arbitration.backend."+store.getDatabaseFlavor()+"BackendAdapter"
				).newInstance();
			
			// Link it to a store
			ba.linkToCoreAndStore(cnf,store);
			
			assertNotNull(ba);
			
			return ba;
			
		} catch (InstantiationException e) {
			fail("Backend adapter could not be instantiated for backend flavor "+store.getDatabaseFlavor()+". "+e.toString());
		} catch (IllegalAccessException e) {
			fail("Illigal access exception to access backend adapter flavor "+store.getDatabaseFlavor()+". "+e.toString());
		} catch (ClassNotFoundException e) {
			fail("The backend adapter for flavor "+store.getDatabaseFlavor()+" could not be found. "+e.toString());
		}
		
		return null;
	}
	
	
	/** Test the creation of the required database schema.
	 * 
	 */
	@Test
	public void testCreateAndDropSchema () {
		BackendAdapter ba = createBackendAdaptorFromStore();
		
		// Try to create the schema
		try {
			ba.createSchema();
			ba.dropSchema();
		} catch (BackendAdapterException e) {
			fail("The schema could not be created and dropped! "+e.toString());
		}
		
	}
	
	/** Test the creation of the required database schema.
	 * 
	 */
	@Test
	public void testRegisterServer () {
		int iRepetitions = 5;
		BackendAdapter ba = createBackendAdaptorFromStore();
		
		// Try to create the schema
		try {
			// Create the schema
			ba.createSchema();
			
			for ( ; iRepetitions>=0 ; iRepetitions-- ) {
				// Register the server
				ba.updateServerStatus();
				
				// Sleep a bit so I can check the table contents
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					fail("The sleep operation was interrupted! "+e.toString());
				}
			}
			
			// Drop the schema
			//ba.dropSchemaLeavingLogsBehind();
		} catch (BackendAdapterException e) {
			fail("The schema could not be created and dropped! "+e.toString());
		}
		
	}
	
}
