/*
 * @(#) PortScrollerTest.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.core.utils;


import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.webui.PortScroller;

/**This class tests the port scrolling 
 * It reuses the ports that have been released.
 * 
 * @author Amit Kumar
 * Created on Jun 23, 2008 12:18:17 PM
 *
 */
public class PortScrollerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testPortReuse(){
		CoreConfiguration cnf = new CoreConfiguration(1000,".");
		assertEquals(cnf.getBasePort(),1000);
		
		PortScroller ps = PortScroller.getInstance(cnf);
		int i=ps.nextAvailablePort("dumbell1");
		assertEquals(1001,i);
		i=ps.nextAvailablePort("dumbell2");
		i=ps.nextAvailablePort("dumbell3");
		
		ps.releasePort("dumbell2");
	
		i=ps.nextAvailablePort("dumbell4");
		assertEquals(1002,i);

		i=ps.nextAvailablePort("dumbell5");
		assertEquals(1004,i);
	}

}
