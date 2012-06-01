package org.meandre.core.environments.lisp.clojure;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Hashtable;

import org.junit.Test;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextImpl;
import org.meandre.core.engine.ActiveBuffer;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.WrappedComponent;
import org.meandre.core.engine.policies.component.availability.WrappedComponentAllInputsRequired;
import org.meandre.core.engine.probes.NullProbeImpl;
import org.meandre.core.engine.test.TestLoggerFactory;


/** This class is used to test the Clojure execution component adapter.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class ClojureExecutableComponentAdapterTest {

	/** Test the constructor and the process facility of the
	 * adapter.
	 *
	 */
	@Test
	public void testClojureExecutableComponentAdapterProcess () {
		ClojureExecutableComponentAdapter ceca = new ClojureExecutableComponentAdapter();
		try {
			ceca.process("(def a (+ 1 1))");
			ceca.process("(+ a a a)");
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	/** Test the constructor and the process and initialize facility of the
	 * adapter.
	 *
	 */
	@Test
	public void testClojureExecutableComponentAdapterInitialize () {
		try {
			// Set up the face component and context
			MrProbe thdMrProbe = new MrProbe(TestLoggerFactory.getTestLogger(), new NullProbeImpl(), false, false);
			thdMrProbe.start();
			ClojureExecutableComponentAdapter ceca = new ClojureExecutableComponentAdapter();
			ceca.trapOutputAndErrorStreams();
			ceca.process("(defn initialize [x] (.(. System out) (println \"Initialize Called\")) )");
			CoreConfiguration cnf = new CoreConfiguration();

			WrappedComponent wc = new WrappedComponentAllInputsRequired(
					"http://nothing.org/","http://nothing.org/",
					"http://nothing.org/", "Nothing", ceca,
					new HashSet<ActiveBuffer> (), new HashSet<ActiveBuffer> (),
					new Hashtable<String, String> (),
					new Hashtable<String, String> (),
					new Hashtable<String, String> (), null,
					"nothing", new Hashtable<String, String> (), thdMrProbe, cnf, System.out, null);

			ComponentContext cc = new ComponentContextImpl("Nothing","Nothing","Nothing","Nothing",new HashSet<ActiveBuffer>(),new HashSet<ActiveBuffer>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),thdMrProbe,wc,cnf, System.out, null);
			ceca.initialize(cc);
			ceca.untrapOutputAndErrorStreams();
			assertTrue(ceca.getOutput().toString().indexOf("Initialize Called")>=0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	/** Test the constructor and the process and initialize facility of the
	 * adapter.
	 *
	 */
	@Test
	public void testClojureExecutableComponentAdapterExecute () {
		try {
			// Set up the face component and context
			MrProbe thdMrProbe = new MrProbe(TestLoggerFactory.getTestLogger(), new NullProbeImpl(), false, false);
			thdMrProbe.start();
			CoreConfiguration cnf = new CoreConfiguration();
			ClojureExecutableComponentAdapter ceca = new ClojureExecutableComponentAdapter();
			ceca.trapOutputAndErrorStreams();
			ceca.process("(defn initialize [x] (.(. System out) (println \"Initialize Called\")) )");
			ceca.process("(defn execute [x] (.(. System out) (println (. x (getFlowID)))) )");

			WrappedComponent wc = new WrappedComponentAllInputsRequired(
					"http://nothing.org/","http://nothing.org/",
					"http://nothing.org/", "Nothing", ceca,
					new HashSet<ActiveBuffer> (), new HashSet<ActiveBuffer> (),
					new Hashtable<String, String> (),
					new Hashtable<String, String> (),
					new Hashtable<String, String> (), null,
					"nothing", new Hashtable<String, String> (), thdMrProbe,cnf, System.out, null);

			ComponentContext cc = new ComponentContextImpl("Nothing","Nothing","Nothing","Nothing",new HashSet<ActiveBuffer>(),new HashSet<ActiveBuffer>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),thdMrProbe,wc,cnf, System.out, null);

			ceca.initialize(cc);
			ceca.execute(cc);

			ceca.untrapOutputAndErrorStreams();

			assertTrue(ceca.getOutput().toString().indexOf("Initialize Called")>=0);
			assertTrue(ceca.getOutput().toString().indexOf("Nothing")>=0);

			assertTrue(ceca.getError().size()==0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}


	/** Test the constructor and the process and initialize facility of the
	 * adapter.
	 *
	 */
	@Test
	public void testClojureExecutableComponentAdapterDispose () {
		try {
			// Set up the face component and context
			MrProbe thdMrProbe = new MrProbe(TestLoggerFactory.getTestLogger(), new NullProbeImpl(), false, false);
			thdMrProbe.start();
			CoreConfiguration cnf = new CoreConfiguration();
			ClojureExecutableComponentAdapter ceca = new ClojureExecutableComponentAdapter();
			ceca.trapOutputAndErrorStreams();
			ceca.process("(defn initialize [x] (.(. System out) (println \"Initialize Called\")) )");
			ceca.process("(defn execute [x] (.(. System out) (println (. x (getFlowID)))) )");
			ceca.process("(defn dispose [x] (.(. System out) (println \"Dispose Called\")) )");

			WrappedComponent wc = new WrappedComponentAllInputsRequired(
					"http://nothing.org/","http://nothing.org/",
					"http://nothing.org/", "Nothing", ceca,
					new HashSet<ActiveBuffer> (), new HashSet<ActiveBuffer> (),
					new Hashtable<String, String> (),
					new Hashtable<String, String> (),
					new Hashtable<String, String> (), null,
					"nothing", new Hashtable<String, String> (), thdMrProbe,cnf, System.out, null);

			ComponentContext cc = new ComponentContextImpl("Nothing","Nothing","Nothing","Nothing",new HashSet<ActiveBuffer>(),new HashSet<ActiveBuffer>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),new Hashtable<String, String>(),thdMrProbe,wc,cnf, System.out, null);

			ceca.initialize(cc);
			ceca.execute(cc);
			ceca.dispose(cc);

			ceca.untrapOutputAndErrorStreams();

			assertTrue(ceca.getOutput().toString().indexOf("Initialize Called")>=0);
			assertTrue(ceca.getOutput().toString().indexOf("Nothing")>=0);
			assertTrue(ceca.getOutput().toString().indexOf("Dispose Called")>=0);

			assertTrue(ceca.getError().size()==0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

}
