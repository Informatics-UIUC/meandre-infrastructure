package org.meandre.core.environments.python.jython;

import java.io.ByteArrayOutputStream;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.python.util.PythonInterpreter;

/** This class is the adapter that wraps Jython executable compoents.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class JythonExecutableComponentAdapter implements ExecutableComponent {

	/** The Jython interpreter that will use this executable component. */
	protected PythonInterpreter pi = null;
	
	/** The script containing the executable component definitions */
	protected String sScript = null;

	/** The output stream of the interpreter */
	protected ByteArrayOutputStream baosOut = null;

	/** The error stream of the interpreter */
	protected ByteArrayOutputStream baosErr = null;
	
	/** Initialize the interpreter and runs the provided scripts containing the 
	 * executable component function calls
	 * 
	 * @param sScript The script to run.
	 */
	public JythonExecutableComponentAdapter ( String sScript ) {
		// Setup the interpreter
		pi = new PythonInterpreter();
		pi.setOut(baosOut=new ByteArrayOutputStream());
		pi.setErr(baosErr=new ByteArrayOutputStream());
		pi.exec(this.sScript=sScript);
	}
	
	/** This method is invoked when the Meandre Flow is being prepared for 
	 * getting run.
	 *
	 * @param ccp The properties associated to a component context
	 */
	public void initialize ( ComponentContextProperties ccp ) {
		// Call the python initialize function
		pi.set("ccp", ccp);
		pi.exec("initialize(ccp)");
	}
	
	/** When Meandre schedules a component for execution, this method is 
	 * invoked. The ComponentContext object encapsulate the API a component 
	 * may use to interact with Meandre infrastructure.
	 * 
	 * @param cc The Meandre component context object
	 * @throws ComponentExecutionException If a fatal condition arises during 
	 *         the execution of a component, a ComponentExecutionException 
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context 
	 *         access was detected
	 */
	public void execute ( ComponentContext cc ) 
	throws ComponentExecutionException, ComponentContextException {
		// Call the execute method
		pi.set("cc", cc);
		pi.exec("execute(cc)");
	}

	/** This method is called when the Menadre Flow execution is completed.
	 *
	 * @param ccp The properties associated to a component context
	 */
	public void dispose ( ComponentContextProperties ccp ) {
		// Call the python initialize function
		pi.set("ccp", ccp);
		pi.exec("dispose(ccp)");
		
		// Get rid of the interpreter
		pi.cleanup();
		pi = null;
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
}
