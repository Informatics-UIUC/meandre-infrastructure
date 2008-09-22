package org.meandre.webservices.tools;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Servlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.meandre.demo.repository.DemoRepositoryGenerator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/** This class provides a set of tests to the unified base servlet dispatcher shared 
 * by all the servlets. It takes care of proper routing requests to methods and return
 * formats.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ServletConfigurableDispatcherTest  {

	/** The default port for testing */
	private final static int iTestPort = 6969;
	
	/** The main Jetty server */
	private Server server;
	
	/** The main context for the webservices */
	private Context contextWS;

	/** Creates a fixture to run servlet configurations.
	 * 
	 */
	@Before 
	public void setUp() { 
		server = new Server(iTestPort);
		contextWS = new Context(server,"/",Context.NO_SESSIONS);
		try {
			server.start();
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Server failed to start because "+baos.toString());
		}
	}
	
	/** Tears the server down.
	 * 
	 */
	@After
	public void tearDown () {
		try {
			server.stop();
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Server failed to stop because "+baos.toString());
		}
	}

	
	/** Pull the content of a get request.
	 * 
	 * @param sMethod The method to pull
	 * @return The content pulled
	 */
	private String getGetRequestContent ( String sMethod ) {
		try {
			
			URL url = new URL("http://localhost:"+iTestPort+sMethod);
			InputStream is = url.openStream();
			int iRead;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ( (iRead=is.read())>=0 )
				baos.write(iRead);
			return baos.toString();
			
		} catch (MalformedURLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Fail to assamble the required URL "+baos.toString());
			return null;
		} catch (IOException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to retrive URL content "+baos.toString());
			return null;
		}
	}
	

	/** Pull the model of a get request.
	 * 
	 * @param sMethod The method to pull
	 * @param sFormat The format of the model
	 * @return The model pulled
	 */
	private Model getGetModel ( String sMethod, String sFormat ) {
		try {
			Model mod = ModelFactory.createDefaultModel();
			URL url = new URL("http://localhost:"+iTestPort+sMethod);
			InputStream is = url.openStream();
			mod.read(is, null, sFormat);
			return mod;
		} catch (MalformedURLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Fail to assamble the required URL "+baos.toString());
			return null;
		} catch (IOException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			fail("Failed to retrive URL content "+baos.toString());
			return null;
		}
	}
	
	/** Simple test of the life cycle of the servlet configurable dispatcher.
	 * 
	 */
	@Test
	public void testServletLifeCycle () {
		// Set the servlet to test
		contextWS.addServlet(new ServletHolder((Servlet) new TestServlet()), "/test/*");
		
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
