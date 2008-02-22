/**
 * 
 */
package org.meandre.core.store.security;

import static org.junit.Assert.*;

import org.junit.Test;
import org.meandre.core.store.Store;
import org.meandre.core.store.security.SecurityStore;
import org.meandre.core.store.security.SecurityStoreException;
import org.meandre.core.store.system.SystemStore;

/** Test the behavior of the security store.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class StoreTest {

	/**
	 * Test method for {@link org.meandre.core.store.Store#getSecurityStore()}.
	 */
	@Test
	public void testStore() {
		SecurityStore secStore = Store.getSecurityStore();

		assertEquals(1714,Store.getBasePort());
		
		assertEquals(1,secStore.getUsersNickNames().size());
		try {
			assertEquals(1,secStore.getUsers().size());
		} catch (SecurityStoreException e) {
			fail("Exception should not have been thrown:"+e);
		}
		
		SystemStore sysStore = Store.getSystemStore(Store.getAdminUserNickName());
		assertEquals(1,sysStore.getPropertyKeys().size());
	}

}
