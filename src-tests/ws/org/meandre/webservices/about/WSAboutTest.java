package org.meandre.webservices.about;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;
import org.meandre.webservices.BaseServletTest;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;


/**This class tests the WSAbout web service
 *
 * @author Amit Kumar
 * Created on Feb 22, 2008 3:50:22 AM
 * Modified by Xavier Llor&agrave;
 *
 */

public class WSAboutTest extends BaseServletTest {


	/** The base URL for Meandre WS */
	public final static String WS_BASE_URL = "http://meandre.org/services/";

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






	@Test
	public void testInstallationTxtHttpServletRequestHttpServletResponse() {
		String format = "txt";
		WebRequest request = new GetMethodWebRequest(getMeandreHostUrl()
				+ INSTALLATION_URL + "." + format);
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
					INSTALLATION_TEST_STRING_TXT), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void testInstallationTTLHttpServletRequestHttpServletResponse() {
		String format = "ttl";
		WebRequest request = new GetMethodWebRequest(getMeandreHostUrl()
				+ INSTALLATION_URL + "." + format);
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
					INSTALLATION_TEST_STRING_TTL), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}

	}

	@Test
	public void testInstallationNTHttpServletRequestHttpServletResponse() {
		String format = "nt";
		WebRequest request = new GetMethodWebRequest(getMeandreHostUrl()
				+ INSTALLATION_URL + "." + format);
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
					INSTALLATION_TEST_STRING_NT), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void testInstallationRDFHttpServletRequestHttpServletResponse() {
		String format = "rdf";
		WebRequest request = new GetMethodWebRequest(getMeandreHostUrl()
				+ INSTALLATION_URL + "." + format);
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
		System.out.println(response.getContentType());
		try {
			assertEquals(response.getContentType(), "application/xml");
			assertEquals(response.getText().startsWith(
					INSTALLATION_TEST_STRING_RDF), Boolean.TRUE);
		} catch (IOException e) {
			fail(e.toString());
		}
	}


	@Test
	public void testUserXMLHttpServletRequestHttpServletResponse() {
		String format = "xml";
		WebRequest request = new GetMethodWebRequest(getMeandreHostUrl()
				+ USER_ROLES_URL + "." + format);
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
			//assertEquals(response.getContentType(), "text/xml");
			//assertEquals(response.getDOM().getElementsByTagName(USER_ROLES_TEST_STRING_XML_TAG_1).getLength(),1);
			assertTrue("The user roles == 0",response.getDOM().getElementsByTagName(USER_ROLES_TEST_STRING_XML_TAG_2).getLength()>0);
		} catch (SAXException e) {
			fail(e.toString());
		}
	}




	@Test
	public void testUserTXTHttpServletRequestHttpServletResponse() {
		String format = "txt";
		WebRequest request = new GetMethodWebRequest(getMeandreHostUrl()
				+ USER_ROLES_URL + "." + format);
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
			assertEquals(response.getContentType(), "text/plain");
			assertTrue("Error: in user_roles.txt",response.getText().startsWith(USER_ROLES_TEST_STRING_TXT));
		} catch (IOException e) {
			fail(e.toString());
		}
	}


	@Test
	public void testUserJSONHttpServletRequestHttpServletResponse() {
		String format = "json";
		WebRequest request = new GetMethodWebRequest(getMeandreHostUrl()
				+ USER_ROLES_URL + "." + format);
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
			assertEquals(response.getContentType(), "text/html");
			assertTrue("Error: in user_roles.json",response.getText().startsWith(USER_ROLES_TEST_STRING_JSON));
		} catch (IOException e) {
			fail(e.toString());
		}
	}




}
