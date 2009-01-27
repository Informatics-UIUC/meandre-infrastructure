package org.meandre.demo.components;

import java.util.Random;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

/** This executable component just pushes a string to the output.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class PushStringComponent implements ExecutableComponent {

	/** A random number */
	private String sRandom;
	
	/** This method is invoked when the Meandre Flow is being prepared for 
	 * getting run.
	 *
	 * @param ccp The properties associated to a component context
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	 */
	public void initialize ( ComponentContextProperties ccp ) 
	throws ComponentExecutionException, ComponentContextException {
		sRandom = (""+new Random().nextFloat()).substring(0,7);
	}

	/** This method just pushes a string  to the output.
	 *
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected

	 */
	public void execute(ComponentContext cc) throws ComponentExecutionException, ComponentContextException {
		
		String sMsg = cc.getProperty("message");
		long lTimes = Long.parseLong(cc.getProperty("times"));

		for ( long l=0 ; l<lTimes ; l++ )
			cc.pushDataComponentToOutput("string",sMsg+" (P"+(l+1)+",C"+sRandom+") ");
	}

	/** This method is called when the Menadre Flow execution is completed.
	 *
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	 * @param ccp The properties associated to a component context
	 */
	public void dispose ( ComponentContextProperties ccp ) 
	throws ComponentExecutionException, ComponentContextException {

	}
}
