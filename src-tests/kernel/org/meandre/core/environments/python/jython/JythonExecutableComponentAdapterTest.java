package org.meandre.core.environments.python.jython;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.junit.Test;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextImpl;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.engine.ActiveBuffer;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.WrappedComponent;
import org.meandre.core.engine.policies.component.availability.WrappedComponentAllInputsRequired;
import org.meandre.core.engine.probes.NullProbeImpl;
import org.meandre.core.engine.test.TestLoggerFactory;

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
			"\n" +
			"def initialize(ccp):\n" +
			"   print \"Initilize called\"\n"+
			"\n" +
			"def execute(cc):\n" +
			"   print \"Execute called\", cc\n" +
			"   print dir(cc)\n" +
			"   print cc.executionInstanceID \n" +
			"   print cc.flowExecutionInstanceID \n" +
			"   s = \"Execute called\" \n" +
//			"   s = cc.getDataComponentFromInput(\"string\") \n" +
//			"   cc.pushDataComponentToOutput(\"string\",s.upper()) \n" +
			"\n" +
			"def dispose(ccp):\n" +
			"   print \"Dispose called\"\n" +
			"\n";

	/** Test a very simple adapter that prints "Hello World!".
	 *
	 */
	@Test
	public void testSimpleAdapter () {
		JythonExecutableComponentAdapter jeca =  new JythonExecutableComponentAdapter();
		jeca.process(sSimpleScript);
		String sRes = jeca.getOutput().toString();
		assertEquals("Hello World!\n",sRes);
		assertEquals(0, jeca.getError().size());
	}

	/** Test the initialize method.
	 * @throws InterruptedException Something went wrong
	 *
	 */
	@Test
	public void testInitializeAndDispose() throws InterruptedException {
		String sRes = null;
		MrProbe thdMrProbe = new MrProbe(TestLoggerFactory.getTestLogger(), new NullProbeImpl(), false, false);
		thdMrProbe.start();
		JythonExecutableComponentAdapter jeca = new JythonExecutableComponentAdapter();
		jeca.process(sSimpleExecutableComponent);
		WrappedComponent wc = new WrappedComponentAllInputsRequired(
				"http://noting.org/","http://noting.org/",
				"http://noting.org/", jeca,
				new HashSet<ActiveBuffer> (), new HashSet<ActiveBuffer> (),
				new Hashtable<String, String> (),
				new Hashtable<String, String> (),
				new Hashtable<String, String> (), null,
				"nothing", new Hashtable<String, String> (), thdMrProbe);
				
		ComponentContext cc = new ComponentContextImpl("Nothing","Nothing","Nothing",new HashSet<ActiveBuffer>(),new HashSet<ActiveBuffer>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),thdMrProbe,wc);
		jeca.initialize(cc);
		sRes = jeca.getOutput().toString();
		assertEquals("Initilize called\nInitilize called\n",sRes);
		jeca.dispose(cc);
		sRes = jeca.getOutput().toString();
		assertEquals("Initilize called\nInitilize called\nDispose called\n",sRes);
		thdMrProbe.done();
	}

	/** Test the execute method.
	 * @throws InterruptedException 
	 *
	 */
	@Test
	public void testExecute() throws InterruptedException {
		System.out.println(sSimpleExecutableComponent);
		String sRes = null;
		MrProbe thdMrProbe = new MrProbe(TestLoggerFactory.getTestLogger(), new NullProbeImpl(), false, false);
		thdMrProbe.start();
		JythonExecutableComponentAdapter jeca = new JythonExecutableComponentAdapter();
		jeca.process(sSimpleExecutableComponent);
		WrappedComponent wc = new WrappedComponentAllInputsRequired(
				"http://noting.org/","http://noting.org/",
				"http://noting.org/", jeca,
				new HashSet<ActiveBuffer> (), new HashSet<ActiveBuffer> (),
				new Hashtable<String, String> (),
				new Hashtable<String, String> (),
				new Hashtable<String, String> (), null,
				"nothing", new Hashtable<String, String> (), thdMrProbe);
		ComponentContext cc = new ComponentContextImpl("Nothing","Nothing","Nothing",new HashSet<ActiveBuffer>(),new HashSet<ActiveBuffer>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),thdMrProbe,wc);
		try {
			jeca.execute(cc);
		} catch (ComponentExecutionException e) {
			fail("This execption should not be thrown "+e);
		} catch (ComponentContextException e) {
			fail("This execption should not be thrown "+e);
		}
		sRes = jeca.getOutput().toString();
		System.out.println(sSimpleExecutableComponent);
		System.out.println(sRes);
		assertTrue(sRes.indexOf("Execute called")>0);
		thdMrProbe.done();
	}

}
