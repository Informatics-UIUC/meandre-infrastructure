/**
 * 
 */
package org.meandre.engine.terracotta.datatypes;

import org.meandre.engine.terracotta.agent.ExecutableComponentAgent;

/** The firing event links a firing request to the executable component
 * agent that will process it.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class FiringEvent {
	
	/** The firing requrest */
	private FiringRequest fr;
	
	/** The executable component agent serving this firing event */
	private ExecutableComponentAgent eca;

	/** Creates an empty firing event.
	 * 
	 */
	public FiringEvent () {
		 fr = null;
		 eca = null;
	}
	
	/** Create a firing event from the provided information.
	 * 
	 * @param fr The firing request
	 * @param eca The executable component agent that will take care of it.
	 */
	public FiringEvent ( FiringRequest fr, ExecutableComponentAgent eca ) {
		this.fr = fr;
		this.eca = eca;
	}
	
	/** Sets the firing requestf or this firing event
	 * 
	 * @param fr the firing request to set
	 */
	public void setFiringRequest(FiringRequest fr) {
		this.fr = fr;
	}

	/** Gets the firing request for this firing event
	 * 
	 * @return the firing request
	 */
	public FiringRequest getFiringRequest() {
		return fr;
	}

	/** Sets the executable component agent for this firing event
	 * 
	 * @param eca the executable component agent to set
	 */
	public void setExecutableComponentAgent(ExecutableComponentAgent eca) {
		this.eca = eca;
	}

	/** Gets the executable component agent for this firing event
	 * 
	 * @return the executable component agent
	 */
	public ExecutableComponentAgent getExecutableComponentAgent() {
		return eca;
	}

}
