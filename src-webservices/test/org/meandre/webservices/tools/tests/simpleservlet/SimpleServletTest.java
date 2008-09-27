package org.meandre.webservices.tools.tests.simpleservlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;

import javax.servlet.Servlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.tools.ant.taskdefs.Jar;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.meandre.demo.repository.DemoRepositoryGenerator;
import org.meandre.webservices.tools.ServletConfigurableDispatcherTest;
import org.mortbay.jetty.servlet.ServletHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.hp.hpl.jena.rdf.model.Model;

/** Extends the base servlet test class to test a TestServlet built on the
 * Python-based dispatcher
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class SimpleServletTest 
extends ServletConfigurableDispatcherTest {

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


	/** Simple test of the life cycle of the servlet configurable dispatcher.
	 * 
	 */
	@Test
	public void testServletArray () {
		// Set the servlet to test
		contextWS.addServlet(new ServletHolder((Servlet) new SimpleServlet()), "/test/*");
		
		// Get an array in text form
		String sContentTXT = getGetRequestContent("/test/array.txt");
		assertEquals(10, sContentTXT.split("\n").length);
		
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
}
