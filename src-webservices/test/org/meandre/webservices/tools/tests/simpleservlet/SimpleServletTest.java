package org.meandre.webservices.tools.tests.simpleservlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.meandre.demo.repository.DemoRepositoryGenerator;
import org.meandre.webservices.tools.ServletConfigurableDispatcherTestBase;
import org.mortbay.jetty.servlet.ServletHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;

/** Extends the base servlet test class to test a TestServlet built on the
 * Python-based dispatcher
 *
 * @author Xavier Llor&agrave;
 *
 */
public class SimpleServletTest
extends ServletConfigurableDispatcherTestBase {

	/** Simple test of the life cycle of the servlet configurable dispatcher.
	 *
	 */
	@Test
	public void testServletPingPong () {
		// Set the servlet to test
		contextWS.addServlet(new ServletHolder((Servlet) new SimpleServlet()), "/test/*");

		// Run ping request
		String sContentTXT = getGetRequestContent("/test/ping.txt");
		assertEquals("pong\n", sContentTXT);
		String sContentJSON = getGetRequestContent("/test/ping.json");
		assertEquals("[\"pong\"]", sContentJSON);
		String sContentXML = getGetRequestContent("/test/ping.xml");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><meandre_response><meandre_item>pong</meandre_item></meandre_response>", sContentXML);
	}

	/** Simple test of the life cycle of the servlet configurable dispatcher.
	 *
	 */
	@Test
	public void testServletDemoRepositoryRDF () {
		// Set the servlet to test
		contextWS.addServlet(new ServletHolder((Servlet) new SimpleServlet()), "/test/*");

		// Run the demo repository request
		Model modDemo = DemoRepositoryGenerator.getTestHelloWorldRepository();
		Model modRDF = getGetModel("/test/get_rdf.rdf", "RDF/XML-ABBREV");
		assertEquals(modDemo.size(),modRDF.size());
		Model modTTL = getGetModel("/test/get_rdf.ttl", "TTL");
		assertEquals(modDemo.size(),modTTL.size());
		Model modNT = getGetModel("/test/get_rdf.nt", "N-TRIPLE");
		assertEquals(modDemo.size(),modNT.size());
	}


	/** Simple test of a call that returns an array of the servlet configurable dispatcher.
	 *
	 */
	@Test
	public void testServletArray () {
		// Set the servlet to test
		contextWS.addServlet(new ServletHolder((Servlet) new SimpleServlet()), "/test/*");

		// Get an array in text form
		String sContentTXT = getGetRequestContent("/test/array.txt");
		String [] saResponse = sContentTXT.split("\n");
		assertEquals(10, saResponse.length);
		for ( int i=0,iMax=saResponse.length ; i<iMax ; i++ )
			assertEquals("value"+i, saResponse[i]);

		// Get an array in JSON form
		try {
			String sContentJSON = getGetRequestContent("/test/array.json");
			JSONArray jsaResponse = new JSONArray(sContentJSON);
			assertEquals(10, jsaResponse.length());
			for ( int i=0 ; i<10 ; i++)
				assertEquals("value"+i,jsaResponse.getString(i));
		} catch (JSONException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to parse the JSON array content because "+baos.toString());
		}

		// Get an array in XML format
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			ByteArrayInputStream baisXML = new ByteArrayInputStream(getGetRequestContent("/test/array.xml").getBytes());
			Document dom = db.parse(baisXML);
			Element elementResponseRoot = dom.getDocumentElement();
			// Check the root node
			assertEquals("meandre_response",elementResponseRoot.getTagName());
			NodeList nlResponseItems = elementResponseRoot.getElementsByTagName("meandre_item");
			// Check there are 10 response elements
			assertEquals(10,nlResponseItems.getLength());
			//Check the node values
			for(int i = 0 ; i<nlResponseItems.getLength() ; i++) {
				// Get the response item
				Element el = (Element)nlResponseItems.item(i);
				// Check the value
				assertEquals("value"+i,el.getFirstChild().getNodeValue());
			}
		} catch (ParserConfigurationException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to configure XML parser because "+baos.toString());
		} catch (SAXException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to parse the XML response content because "+baos.toString());
		} catch (IOException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to parse the XML response content because "+baos.toString());
		}
	}


	/** Simple test of a call that returns a dictionary of the servlet configurable dispatcher.
	 *
	 */
	@Test
	public void testServletDictionary () {
		// Set the servlet to test
		contextWS.addServlet(new ServletHolder((Servlet) new SimpleServlet()), "/test/*");

		// Get a dictionary in text form
		try {
			String sContentTXT = getGetRequestContent("/test/dictionary.txt");
			ByteArrayInputStream baisXML = new ByteArrayInputStream(sContentTXT.getBytes());
			Properties prop = new Properties();
			prop.load(baisXML);
			assertEquals(2,prop.size());
			assertEquals("SimpleServlet",prop.get("name"));
			assertEquals("get_dictionary_info",prop.get("method"));
		} catch (IOException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to load the text dictionary because "+baos.toString());
		}


		// Get a dictionary in json form
		try {
			String sContentJSON = getGetRequestContent("/test/dictionary.json");
			JSONArray jsaResponse = new JSONArray(sContentJSON);
			assertEquals(1, jsaResponse.length());
			JSONObject joDict = jsaResponse.getJSONObject(0);
			assertEquals("SimpleServlet",joDict.getString("name"));
			assertEquals("get_dictionary_info",joDict.getString("method"));
		} catch (JSONException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to parse the JSON array content because "+baos.toString());
		}

		// Get a dictionary in XML format
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			ByteArrayInputStream baisXML = new ByteArrayInputStream(getGetRequestContent("/test/dictionary.xml").getBytes());
			Document dom = db.parse(baisXML);
			Element elementResponseRoot = dom.getDocumentElement();
			// Check the root node
			assertEquals("meandre_response",elementResponseRoot.getTagName());
			NodeList nlResponseItems = elementResponseRoot.getElementsByTagName("meandre_item");
			// Check there are 10 response elements
			assertEquals(1,nlResponseItems.getLength());
			//Check the node values
			Element el = (Element)nlResponseItems.item(0);
			// Check the values
			NodeList nlResponseItemsName = el.getElementsByTagName("name");
			assertEquals(1,nlResponseItemsName.getLength());
			assertEquals("SimpleServlet",((Element)nlResponseItemsName.item(0)).getFirstChild().getNodeValue());
			NodeList nlResponseItemsGet  = el.getElementsByTagName("method");
			assertEquals(1,nlResponseItemsGet.getLength());
			assertEquals("get_dictionary_info",((Element)nlResponseItemsGet.item(0)).getFirstChild().getNodeValue());

		} catch (ParserConfigurationException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to configure XML parser because "+baos.toString());
		} catch (SAXException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to parse the XML response content because "+baos.toString());
		} catch (IOException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to parse the XML response content because "+baos.toString());
		}

	}

	/** Simple test of a call that returns a dictionary of the parameters
	 * passed to the servlet request.
	 *
	 */
	@Test
	public void testServletParameterEcho () {
		// Set the servlet to test
		contextWS.addServlet(new ServletHolder((Servlet) new SimpleServlet()), "/test/*");

		// Check passing multiple parameters with unique values
		try {
			int iNumberOfParameters = 10;
			String sParameters = "?param0=value0";
			for ( int i=1 ; i<iNumberOfParameters ; i++ )
				sParameters+="&param"+i+"=value"+i;
			String sContentJSON = getGetRequestContent("/test/request_echo.json"+sParameters);
			JSONArray jsaResponse = new JSONArray(sContentJSON);
			assertEquals(1,jsaResponse.length());
			JSONObject jsoDictionary = jsaResponse.getJSONObject(0);
			assertEquals(iNumberOfParameters,jsoDictionary.length());
			for ( int i=0 ; i<iNumberOfParameters ; i++ ) {
				JSONArray jaValues = jsoDictionary.getJSONArray("param"+i);
				assertEquals(1, jaValues.length());
				assertEquals("value"+i,jaValues.getString(0));
			}
		} catch (JSONException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to parse the JSON array content because "+baos.toString());
		}

		// Check passing multiple parameters with multiple values
		try {
			int iNumberOfParameters = 10;
			String sParameters = "?param0=value0";
			for ( int i=1 ; i<iNumberOfParameters ; i++ )
				sParameters+="&param"+i+"=value"+i;
			for ( int i=0 ; i<iNumberOfParameters ; i++ )
				sParameters+="&param"+i+"=value"+(iNumberOfParameters+i);
			String sContentJSON = getGetRequestContent("/test/request_echo.json"+sParameters);
			JSONArray jsaResponse = new JSONArray(sContentJSON);
			assertEquals(1,jsaResponse.length());
			JSONObject jsoDictionary = jsaResponse.getJSONObject(0);
			assertEquals(iNumberOfParameters,jsoDictionary.length());
			for ( int i=0 ; i<iNumberOfParameters ; i++ ) {
				JSONArray jaValues = jsoDictionary.getJSONArray("param"+i);
				assertEquals(2, jaValues.length());
				HashSet<String> hs = new HashSet<String>();
				hs.add(jaValues.getString(0));
				hs.add(jaValues.getString(1));
				assertTrue(hs.contains("value"+i));
				assertTrue(hs.contains("value"+(iNumberOfParameters+i)));
			}
		} catch (JSONException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to parse the JSON array content because "+baos.toString());
		}
	}
}
