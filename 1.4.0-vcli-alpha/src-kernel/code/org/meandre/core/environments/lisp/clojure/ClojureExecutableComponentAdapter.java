/**
 * 
 */
package org.meandre.core.environments.lisp.clojure;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.logging.Logger;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.environments.ScriptingEnvironmentAdapter;
import org.meandre.core.logger.KernelLoggerFactory;

import clojure.lang.Compiler;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

/** This class is the adapter that wraps Lisp executable components using Clojure.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class ClojureExecutableComponentAdapter  
implements ExecutableComponent, ScriptingEnvironmentAdapter {

	/** The logger facility */
	static final Logger log = KernelLoggerFactory.getCoreLogger();
	
	/** Clojure user */
	//static final Symbol USER = Symbol.create("meandre-"+System.currentTimeMillis());
	static final Symbol USER = Symbol.create("user");
	
	/** Clojure clojure property */
	static final Symbol CLOJURE = Symbol.create("clojure");

	/** Clojure in-ns property */
	static final Var in_ns = RT.var("clojure", "in-ns");
	
	/** Clojure refer property */
	static final Var refer = RT.var("clojure", "refer");
	
	/** Clojure *ns* property */
	static final Var ns = RT.var("clojure", "*ns*");
	
	/** Clojure *warn-on-reflection* property */
	static final Var warn_on_reflection = RT.var("clojure", "*warn-on-reflection*");

	/** The output stream of the interpreter */
	protected ByteArrayOutputStream baosOut = null;

	/** The error stream of the interpreter */
	protected ByteArrayOutputStream baosErr = null;

	/** The clojure output stream */
	private PrintStream psOutClojure;

	/** The clojure error stream */
	private PrintStream psErrClojure;
	
	/** The original output stream */
	private PrintStream psOutOrig;

	/** The original error stream */
	private PrintStream psErrOrig;

	/** Build the Clojure adapter.
	 * 
	 */
	public ClojureExecutableComponentAdapter () {
		try {
			RT.init();
			
			//*ns* must be thread-bound for in-ns to work
			//thread-bind *warn-on-reflection* so it can be set!
			//must have corresponding popThreadBindings in finally clause
			Var.pushThreadBindings(
					RT.map(ns, ns.get(),
					       warn_on_reflection, warn_on_reflection.get()));

			//create and move into the user namespace
			in_ns.invoke(USER);
			refer.invoke(CLOJURE);
			
			// Set the default failsafe streams
			psOutOrig = System.out;
			psErrOrig = System.err;
			
		} catch (Exception e) {
			KernelLoggerFactory.getCoreLogger().warning("Clojure init: "+e.toString());
			try {
				dispose(null);
			} catch (ComponentExecutionException e1) {
				KernelLoggerFactory.getCoreLogger().warning("Clojure dispose: "+e.toString());
			} catch (ComponentContextException e1) {
				KernelLoggerFactory.getCoreLogger().warning("Clojure dispose: "+e.toString());
			}
		}
		
	}
	

	/** Traps the input and output error streams.
	 * 
	 */
	public void trapOutputAndErrorStreams () {
		// Create the streams
		baosOut=new ByteArrayOutputStream();
		baosErr=new ByteArrayOutputStream();
		psOutClojure = new PrintStream(baosOut);
		psErrClojure = new PrintStream(baosErr);
		
		setStreams();
	}
	

	/** Untraps the input and output error streams.
	 * 
	 */
	public void untrapOutputAndErrorStreams () {
		resetStreams();
	}
	
	/** Process the given script on an already prepared adapter.
	 *
	 * @param script The script to process
	 * @throws Exception Something when wrong
	 */
	public void process(String script) throws Exception {
		StringReader sr = new StringReader(script);
		Compiler.load(sr); 
	}

	/** Redirects the streams.
	 * 
	 */
	private void setStreams() {
		psOutOrig = System.out;
		psErrOrig = System.err;
		System.setOut(psOutClojure);
		System.setErr(psErrClojure);
	}
	
	/** Resets the streams.
	 * 
	 */
	private void resetStreams()  {
		System.setOut(psOutOrig);
		System.setErr(psErrOrig);
	}


	/** Returns the interpreter output stream.
	 *
	 * @return The output stream
	 */
	public ByteArrayOutputStream getOutput() {
		return baosOut;
	}

	/** Returns the interpreter error stream.
	 *
	 * @return The error stream
	 */
	public ByteArrayOutputStream getError() {
		return baosErr;
	}

	/** Invokes the initialize method.
	 * 
	 * @param ccp The component context properties
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	*/
	public void initialize(ComponentContextProperties ccp) 
	throws ComponentExecutionException, ComponentContextException {
		Var cjCCP = RT.var(USER.getName(), "initialize");
		try {
			cjCCP.invoke(ccp);
		} catch (Exception e) {
			e.printStackTrace();
			untrapOutputAndErrorStreams();
		}
	}

	/** Invokes the execute method.
	 * 
	 * @param cc The component context 
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	
	 */
	public void execute(ComponentContext cc) 
	throws ComponentExecutionException, ComponentContextException {
		Var cjCCP = RT.var(USER.getName(), "execute");
		try {
			cjCCP.invoke(cc);
		} catch (Exception e) {
			e.printStackTrace();
			untrapOutputAndErrorStreams();
		}
	}


	/** Invokes the dispose method.
	 * 
	 * @param ccp The component context 
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	 */
	public void dispose(ComponentContextProperties ccp) 
	throws ComponentExecutionException, ComponentContextException {
		Var cjCCP = RT.var(USER.getName(), "dispose");
		try {
			cjCCP.invoke(ccp);
		} catch (Exception e) {
			e.printStackTrace();
			untrapOutputAndErrorStreams();
		}
	}


}
