package org.meandre.demo.components;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

/** This executable component just prints the inputed object to the 
 * standard output.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class PrintObjectComponent implements ExecutableComponent {

	/** This method is invoked when the Meandre Flow is being prepared for
	 * getting run.
	 *
	 */
	public void initialize () {

	}

	/** This method just pushes a concatenated version of the entry to the
	 * output.
	 *
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected

	 */
	public void execute(ComponentContext cc) throws ComponentExecutionException, ComponentContextException {
		
		System.out.println(cc.getDataComponentFromInput("object").toString());
		
	}

	/** This method is called when the Menadre Flow execution is completed.
	 *
	 */
	public void dispose () {

	}

}
