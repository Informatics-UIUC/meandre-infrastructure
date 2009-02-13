package org.meandre.webui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.MrProper;
import org.meandre.core.engine.WrappedComponent;
import org.meandre.core.engine.probes.NullProbeImpl;
import org.meandre.core.logger.KernelLoggerFactory;

/** This class is intended for testing the WebUI functionality.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WebUITest {
	
	/** The default test port */
	private final static int TEST_PORT = 1936;
	
	/** The default test directory */
	private final static String TEST_DIR = "test"+File.separator;
	
	/** The base flow URI for the tests */
	private final static String TEST_FLOW_URI = "meandre://mytest.com/test/";
	
	/** The default flow uinique ID */
	private String sFlowUniqueID;
	
	/** The default mrProper */
	private MrProper mrProper;
	
	/** The default mrProbe; */
	private MrProbe mrProbe;
	
	/** The default core configuration */
	private CoreConfiguration cnf;
	
	/** The web UI port to use */
	private int port;
	
	/** Pull the content of a get request.
	 * 
	 * @param sMethod The method to pull
	 * @return The content pulled
	 */
	protected String getGetRequestContent ( String sMethod ) {
		try {
			
			URL url = new URL("http://localhost:"+port+sMethod);
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

	/** Prepares the fixture before the test is conducted.
	 * 
	 */
	@Before
	public void prepareFixture () {
		this.port = TEST_PORT;
		this.cnf = new CoreConfiguration(this.port,TEST_DIR);
		this.sFlowUniqueID = TEST_FLOW_URI+System.currentTimeMillis()+"/";
		this.mrProbe = new MrProbe(KernelLoggerFactory.getCoreLogger(), new NullProbeImpl(),false,false);
		try {
			this.mrProper = new MrProper(new ThreadGroup("test"),new HashSet<WrappedComponent>());
		} catch (InterruptedException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			fail("Failed to create mrPropper "+baos.toString());
		}
	}
	
	/** Cleans the fixture after the test.
	 * 
	 */
	@After
	public void cleanUpFixture () {
		this.sFlowUniqueID = null;
		this.mrProper = null;
		this.mrProbe = null;
		this.cnf = null;
		this.port=-1;
	}
	
	/** Test the basic default handler when no fragments are available.
	 * 
	 */
	@Test
	public void WebuUIDefaultTest () {
		
		 try {
			WebUI webui = WebUIFactory.getWebUI(this.sFlowUniqueID, this.mrProper, this.mrProbe, this.cnf, this.port);
			assertNotNull(webui);
			String sString = getGetRequestContent("/");
			assertTrue(sString.indexOf("No WebUI available at this point of execution")>0);
			WebUIFactory.disposeWebUI(this.sFlowUniqueID);
			
		 } catch (WebUIException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			fail("Failed to create webui "+baos.toString());
		}
		
	}
	

	/** Test a single fragment call.
	 * 
	 */
	@Test
	public void WebuUIOneFragmentTest () {
		
		 try {
			WebUI  webui = WebUIFactory.getWebUI(this.sFlowUniqueID, this.mrProper, this.mrProbe, this.cnf, this.port);
			assertNotNull(webui);
			
			// Create a dummy fagment
			WebUIFragment wuif = new WebUIFragment("a-fragment", new WebUIFragmentCallback(){

				public void emptyRequest(HttpServletResponse response)
						throws WebUIException {
					try {
						response.setStatus(HttpServletResponse.SC_OK);
						response.setContentType("plain/text");
						response.getWriter().print("Empty");
					} catch (IOException e) {
						throw new WebUIException(e);
					}
				}

				public void handle(HttpServletRequest request,
						HttpServletResponse response) throws WebUIException {
					String sParam = request.getParameter("param");
					try {
						response.setStatus(HttpServletResponse.SC_OK);
						response.setContentType("plain/text");
						response.getWriter().print("Not empty: "+sParam);
					} catch (IOException e) {
						throw new WebUIException(e);
					}
				}}) ;
			
			// Add the fragment
			webui.addFragment(wuif);
			
			// Test the empty request
			String sEmpytResponse = getGetRequestContent("/");
			//System.out.println(sEmpytResponse);
			assertEquals("Empty",sEmpytResponse);
			
			// Test the none empty request
			String sNotEmptyResponse = getGetRequestContent("/a-fragment?param=Hello");
			//System.out.println(sNotEmptyResponse);
			assertEquals("Not empty: Hello",sNotEmptyResponse);
			
			// Dispose the webui
			WebUIFactory.disposeWebUI(this.sFlowUniqueID);
			
		 } catch (WebUIException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			fail("Failed to create webui "+baos.toString());
		}
		
	}


	/** Test a single configurable fragment call.
	 * 
	 */
	@Test
	public void WebuUIConfigurableFragmentTest () {
		
		final String URL_FRAGMENT_PATH = "/test/url";
		 try {
			WebUI  webui = WebUIFactory.getWebUI(this.sFlowUniqueID, this.mrProper, this.mrProbe, this.cnf, this.port);
			assertNotNull(webui);
			
			// Create a dummy fagment
			WebUIFragment wuif = new WebUIFragment("a-fragment", new ConfigurableWebUIFragmentCallback(){

				public void emptyRequest(HttpServletResponse response)
						throws WebUIException {
					fail("Empty should have never been called");
				}

				public void handle(HttpServletRequest request,
						HttpServletResponse response) throws WebUIException {
					String sParam = request.getParameter("param");
					try {
						response.setStatus(HttpServletResponse.SC_OK);
						response.setContentType("plain/text");
						response.getWriter().print("Not empty: "+sParam);
					} catch (IOException e) {
						throw new WebUIException(e);
					}
				}

				public String getContextPath() {
					return URL_FRAGMENT_PATH;
				}
			}) ;
			
			// Add the fragment
			webui.addFragment(wuif);
			
			// Test the empty request
			String sEmpytResponse = getGetRequestContent("/");
			//System.out.println(sEmpytResponse);
			assertEquals("",sEmpytResponse);
			
			// Test the none empty request
			String sNotEmptyResponse = getGetRequestContent(URL_FRAGMENT_PATH+"?param=Hello");
			//System.out.println(sNotEmptyResponse);
			assertEquals("Not empty: Hello",sNotEmptyResponse);
			
			// Dispose the webui
			WebUIFactory.disposeWebUI(this.sFlowUniqueID);
			
		 } catch (WebUIException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			fail("Failed to create webui "+baos.toString());
		}
		
	}


}
