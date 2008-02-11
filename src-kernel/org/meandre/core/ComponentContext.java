package org.meandre.core;

/** The component context contains methods to access the context environment
 * during the execution of a component.
 *
 * @author Xavier Llor&agrave;
 * @last-modified: Amit Kumar added the getFlowExecutionInstanceID function defn
 *
 */
public interface ComponentContext 
extends ComponentContextProperties {

	/** Returns the current data component on the given active buffer.
	 *
	 * @return The input name
	 * @throws ComponentContextException A violation of the component context was detected
	 */
	public Object getDataComponentFromInput ( String sInputBuffer ) throws ComponentContextException;

	/** Push an object to the given named output.
	 *
	 * @param sOutputBuffer The name of the output
	 * @param obj The data component to push
	 * @throws ComponentContextException Violation of the component context detected
	 */
	public void pushDataComponentToOutput ( String sOutputBuffer, Object obj ) throws ComponentContextException;

	/** Checks if a given input is available.
	 *
	 * @return The name of the input
	 * @throws ComponentContextException A violation of the component context was detected
	 */
	public boolean isInputAvailable ( String sInputBuffer ) throws ComponentContextException;

}
