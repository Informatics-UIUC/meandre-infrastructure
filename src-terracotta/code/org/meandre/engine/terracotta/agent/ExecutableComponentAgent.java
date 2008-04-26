/**
 * 
 */
package org.meandre.engine.terracotta.agent;

import org.meandre.core.ExecutableComponent;
import org.meandre.engine.terracotta.datatypes.FiringEvent;

/** This class creates the agent that wrapps and executable componet.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ExecutableComponentAgent {

	/** The executable component agent possible status */
	public enum Status { IDDLE, RUNNING };
	
	/** The executable component being wrapped */
	protected ExecutableComponent ec;
	
	/** The default constructor.
	 * 
	 */
	public ExecutableComponentAgent () {
		ec = null;
	}
	
	/** Sets the executable component that this agent will wrap.
	 * 
	 * @param ec The executable component
	 */
	public void setExecutableComponent( ExecutableComponent ec ) {
		this.ec = ec;
	}

	/** Fires the execution of an firing event.
	 * 
	 * @param fe The firing event to process
	 */
	public void fire(FiringEvent fe) {
		// TODO Auto-generated method stub
		
	}
}
