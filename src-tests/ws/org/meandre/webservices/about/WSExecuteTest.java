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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;
import org.meandre.webservices.BaseServletTest;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class WSExecuteTest extends BaseServletTest {



	String LIST_FLOW_URL = "services/execute/list_running_flows";
	String FLOW_URL = "services/execute/flow";


	String LIST_FLOW_URL_JSON="{\"meandre_running_flows\"";
	String LIST_FLOW_URL_XML ="<meandre_execution>";
	String LIST_FLOW_URL_TXT =" ";

	


	@Test
	public void testFlowJson(){
		String format = "json";
		WebRequest request = new GetMethodWebRequest(getMeandreHostUrl()
				+ LIST_FLOW_URL + "." + format);
		WebResponse response = null;
		try {
			response = getWebConversation().getResponse(request);
		} catch (MalformedURLException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		}
		try {
			assertEquals(response.getText().startsWith(
					LIST_FLOW_URL_JSON), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void testFlowXML(){
		String format = "xml";
		WebRequest request = new GetMethodWebRequest(getMeandreHostUrl()
				+ LIST_FLOW_URL + "." + format);
		WebResponse response = null;
		try {
			response = getWebConversation().getResponse(request);
		} catch (MalformedURLException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (SAXException e) {
			fail(e.toString());
		}
		try {
			assertEquals(response.getText().startsWith(
					LIST_FLOW_URL_XML), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	
}
