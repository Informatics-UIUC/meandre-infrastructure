/**
 * 
 */
package org.meandre.core.environments.lisp.clojure;

import java.io.StringReader;
import java.util.logging.Logger;

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
public class CECA  {

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


	public static void main ( String[] sArgs ) throws Exception {

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
		
		processService("(defn foo [x] (+ x 1))");
		
		Var foo = RT.var("user","foo"); 
		Object a = foo.invoke(1);
		System.out.println(a.toString());
	}
	

	public static void processService(String script) throws Exception {
		StringReader sr = new StringReader(script);
		Compiler.load(sr); 
	}

}
