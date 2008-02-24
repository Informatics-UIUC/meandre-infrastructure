/*
 * @(#) WSExecuteTest.java @VERSION@
 *
 * Copyright (c) 2008+ Amit Kumar
 *
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.webservices.about;

import org.junit.Test;
import org.meandre.webservices.BaseServletTest;

public class WSExecuteTest extends BaseServletTest {



	String LIST_FLOW_URL = "services/execute/list_running_flows";
	String FLOW_URL = "services/execute/flow";


	String LIST_FLOW_URL_JSON="{\"meandre_running_flows\"";
	String LIST_FLOW_URL_XML ="<meandre_execution>";



	@Test
	public void testMe(){

	}

}
