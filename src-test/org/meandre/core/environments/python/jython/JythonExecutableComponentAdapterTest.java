package org.meandre.core.environments.python.jython;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Hashtable;

import org.junit.Test;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextImpl;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.engine.ActiveBuffer;

/** This class is used to test the Jython execution compoment adapter.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class JythonExecutableComponentAdapterTest {
	
	/** A simple Hello World printing script */
	protected static String sSimpleScript = "print \"Hello World!\"";
	
	/** A simple Hello World printing script */
	protected static String sSimpleExecutableComponent = "" +
			"def initialize(ccp):\n" +
			"   print \"Initilize called\"\n"+
			"\n" +
			"def execute(cc):\n" +
			"   print \"Execute called\", cc\n" +
			"   print dir(cc)\n" +
			"   print cc.executionInstanceID \n" +
			"   print cc.flowExecutionInstanceID \n" +
			"\n" +
			"def dispose(ccp):\n" +
			"   print \"Dispose called\"\n" +
			"\n";
	
	/** Test a very simple adapter that prints "Hello World!".
	 * 
	 */
	@Test
	public void testSimpleAdapter () {
		JythonExecutableComponentAdapter jeca =  new JythonExecutableComponentAdapter(sSimpleScript);
		String sRes = jeca.getOutput().toString();
		assertEquals("Hello World!\n",sRes);
		assertEquals(0, jeca.getError().size());
	}
	
	/** Test the initialize method.
	 * 
	 */
	@Test
	public void testInitializeAndDispose() {
		String sRes = null;
		ComponentContext cc = new ComponentContextImpl("Nothing","Nothing",new HashSet<ActiveBuffer>(),new HashSet<ActiveBuffer>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>());
		JythonExecutableComponentAdapter jeca = new JythonExecutableComponentAdapter(sSimpleExecutableComponent);
		jeca.initialize(cc);
		sRes = jeca.getOutput().toString();
		assertEquals("Initilize called\n",sRes);
		jeca.dispose(cc);
		sRes = jeca.getOutput().toString();
		assertEquals("Initilize called\nDispose called\n",sRes);
	}

	/** Test the execute method.
	 * 
	 */
	@Test
	public void testExecute() {
		String sRes = null;
		ComponentContext cc = new ComponentContextImpl("Nothing","Nothing",new HashSet<ActiveBuffer>(),new HashSet<ActiveBuffer>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>());
		JythonExecutableComponentAdapter jeca = new JythonExecutableComponentAdapter(sSimpleExecutableComponent);
		try {
			jeca.execute(cc);
		} catch (ComponentExecutionException e) {
			fail("This execption should not be thrown "+e);
		} catch (ComponentContextException e) {
			fail("This execption should not be thrown "+e);
		}
		sRes = jeca.getOutput().toString();
		assertTrue(sRes.startsWith("Execute called"));
		// System.out.println(sRes);
	}

}
