package org.meandre.webservices.tools;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.meandre.core.utils.ModelIO;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/** This class provides a set of tests to the unified base servlet dispatcher shared 
 * by all the servlets. It takes care of proper routing requests to methods and return
 * formats.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class ServletConfigurableDispatcherTest  {

	/** The default port for testing */
	private final static int iTestPort = 6969;
	
	/** The main Jetty server */
	private Server server;
	
	/** The main context for the webservices */
	protected Context contextWS;

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
	protected String getGetRequestContent ( String sMethod ) {
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
	protected Model getGetModel ( String sMethod, String sFormat ) {
		try {
			Model mod = ModelFactory.createDefaultModel();
			URL url = new URL("http://localhost:"+iTestPort+sMethod);
			ModelIO.readModelInDialect(mod, url);
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
	
}
