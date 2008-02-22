/*
 * @(#) WSAboutTest.java @VERSION@
 *
 * Copyright (c) 2008+ Amit Kumar
 *
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.webservices.about;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;


/**This class tests the WSAbout web service
 *
 * @author Amit Kumar
 * Created on Feb 22, 2008 3:50:22 AM
 *
 */

public class WSAboutTest {

	String meandreHostURL=null;
	String user =null;
	String password =null;

	String INSTALLATION_URL = "services/about/installation";
	String INSTALLATION_TEST_STRING_TXT = "Meandre Execution Engine version";
	String INSTALLATION_TEST_STRING_TTL = "@prefix meandreWS:  <http://meandre.org/services/>";
	String INSTALLATION_TEST_STRING_NT = "<http://meandre.org/services/>";
	String INSTALLATION_TEST_STRING_RDF = "<rdf:RDF";


	String USER_ROLES_URL = "services/about/user_roles";
	String USER_ROLES_TEST_STRING_TXT = "http://www.meandre.org/accounting/property/action/";
	String USER_ROLES_TEST_STRING_JSON = "{\"meandre_user_role";
	String USER_ROLES_TEST_STRING_XML_TAG_1 = "meandre_security";
	String USER_ROLES_TEST_STRING_XML_TAG_2 ="meandre_user_role";





	WebConversation wc = new WebConversation();

	@Before
	public void setUp() throws Exception {
		URL url =WSAboutTest.class.getResource("../webservices.properties");
		Properties properties =  new Properties();
		properties.load(url.openStream());
		meandreHostURL = properties.getProperty("meandreHostURL", "http://127.0.0.1:1714/");
		user = properties.getProperty("user", "admin");
		password = properties.getProperty("password", "admin");
		wc.setAuthorization(user, password);
	}

	@After
	public void tearDown() throws Exception {
		wc.clearContents();
	}

	@Test
	public void testInstallationTxtHttpServletRequestHttpServletResponse() {
		String format = "txt";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ INSTALLATION_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		} catch (SAXException e) {
			fail();
			e.printStackTrace();
		}
		try {
			assertEquals(response.getText().startsWith(
					INSTALLATION_TEST_STRING_TXT), Boolean.TRUE);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void testInstallationTTLHttpServletRequestHttpServletResponse() {
		String format = "ttl";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ INSTALLATION_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		} catch (SAXException e) {
			fail();
			e.printStackTrace();
		}
		try {
			assertEquals(response.getText().startsWith(
					INSTALLATION_TEST_STRING_TTL), Boolean.TRUE);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test
	public void testInstallationNTHttpServletRequestHttpServletResponse() {
		String format = "nt";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ INSTALLATION_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		} catch (SAXException e) {
			fail();
			e.printStackTrace();
		}
		try {
			assertEquals(response.getText().startsWith(
					INSTALLATION_TEST_STRING_NT), Boolean.TRUE);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void testInstallationRDFHttpServletRequestHttpServletResponse() {
		String format = "rdf";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ INSTALLATION_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		} catch (SAXException e) {
			fail();
			e.printStackTrace();
		}
		System.out.println(response.getContentType());
		try {
			assertEquals(response.getContentType(), "application/xml");
			assertEquals(response.getText().startsWith(
					INSTALLATION_TEST_STRING_RDF), Boolean.TRUE);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}


	@Test
	public void testUserXMLHttpServletRequestHttpServletResponse() {
		String format = "xml";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ USER_ROLES_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		} catch (SAXException e) {
			fail();
			e.printStackTrace();
		}
		try {
			assertEquals(response.getContentType(), "text/xml");
			assertEquals(response.getDOM().getElementsByTagName(USER_ROLES_TEST_STRING_XML_TAG_1).getLength(),1);
			assertTrue("The user roles == 0",response.getDOM().getElementsByTagName(USER_ROLES_TEST_STRING_XML_TAG_2).getLength()>0);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




	@Test
	public void testUserTXTHttpServletRequestHttpServletResponse() {
		String format = "txt";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ USER_ROLES_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		} catch (SAXException e) {
			fail();
			e.printStackTrace();
		}
		try {
			assertEquals(response.getContentType(), "text/plain");
			assertTrue("Error: in user_roles.txt",response.getText().startsWith(USER_ROLES_TEST_STRING_TXT));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Test
	public void testUserJSONHttpServletRequestHttpServletResponse() {
		String format = "json";
		WebRequest request = new GetMethodWebRequest(meandreHostURL
				+ USER_ROLES_URL + "." + format);
		WebResponse response = null;
		try {
			response = wc.getResponse(request);
		} catch (MalformedURLException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		} catch (SAXException e) {
			fail();
			e.printStackTrace();
		}
		try {
			assertEquals(response.getContentType(), "text/html");
			assertTrue("Error: in user_roles.json",response.getText().startsWith(USER_ROLES_TEST_STRING_JSON));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




}
