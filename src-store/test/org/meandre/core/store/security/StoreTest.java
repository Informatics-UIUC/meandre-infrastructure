/**
 * 
 */
package org.meandre.core.store.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.store.Store;
import org.meandre.core.store.system.SystemStore;

/** Test the behavior of the security store.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class StoreTest {

     private static String _workingDir = 
        new File(".").getAbsolutePath() + File.separator + "test" + File.separator + "output";   

	/**
	 * Test method for {@link org.meandre.core.store.Store#getSecurityStore()}.
	 */
	@Test
	public void testStore() {
		
		CoreConfiguration cnf = new CoreConfiguration(1714, _workingDir);
		Store store = new Store(cnf);
		
		SecurityManager secStore = store.getSecurityStore();

		assertEquals(1714, cnf.getBasePort());
		

		try {
			assertEquals(1,secStore.getUsers().size());
		} catch (SecurityStoreException e) {
			fail("Exception should not have been thrown:"+e);
		}
		
		SystemStore sysStore = store.getSystemStore(cnf,store.getAdminUserNickName());
		// TODO Check why only on clean checkouts returns 0
		assertTrue(sysStore.getPropertyKeys().size()==0 || sysStore.getPropertyKeys().size()==1);
	}

}
