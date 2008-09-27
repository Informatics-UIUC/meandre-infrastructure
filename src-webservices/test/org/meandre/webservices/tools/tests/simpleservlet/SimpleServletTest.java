package org.meandre.webservices.tools.tests.simpleservlet;

import static org.junit.Assert.assertEquals;

import javax.servlet.Servlet;

import org.junit.Test;
import org.meandre.demo.repository.DemoRepositoryGenerator;
import org.meandre.webservices.tools.ServletConfigurableDispatcherTest;
import org.mortbay.jetty.servlet.ServletHolder;

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
	public void testServletLifeCycle () {
		// Set the servlet to test
		contextWS.addServlet(new ServletHolder((Servlet) new SimpleServlet()), "/test/*");
		
		// Run ping request
		String sContentTXT = getGetRequestContent("/test/ping.txt");
		assertEquals("pong\n", sContentTXT);
		String sContentJSON = getGetRequestContent("/test/ping.json");
		assertEquals("[\"pong\"]", sContentJSON);
		String sContentXML = getGetRequestContent("/test/ping.xml");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><meandre_response>pong</meandre_response>", sContentXML);

		// Run the demo repository request
		Model modDemo = DemoRepositoryGenerator.getTestHelloWorldRepository();
		Model modRDF = getGetModel("/test/demo.rdf", "RDF/XML-ABBREV");
		assertEquals(modDemo.size(),modRDF.size());
		Model modTTL = getGetModel("/test/demo.ttl", "TTL");
		assertEquals(modDemo.size(),modTTL.size());
		Model modNT = getGetModel("/test/demo.nt", "N-TRIPLE");
		assertEquals(modDemo.size(),modNT.size());
	}

}
