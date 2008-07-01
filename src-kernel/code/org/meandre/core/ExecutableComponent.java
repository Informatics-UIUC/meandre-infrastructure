package org.meandre.core;

/** This interface describes the basic API of a Meandre executable component. 
 * All Meandre components need to implement this interface.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public interface ExecutableComponent {
	
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
	public void initialize ( ComponentContextProperties ccp ) throws ComponentExecutionException, ComponentContextException;
	
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
	public void execute ( ComponentContext cc ) throws ComponentExecutionException, ComponentContextException;

	/** This method is called when the Menadre Flow execution is completed.
	 *
	 * @throws ComponentExecutionException If a fatal condition arises during 
	 *         the execution of a component, a ComponentExecutionException 
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context 
	 *         access was detected
	 * @param ccp The properties associated to a component context
	 */
	public void dispose ( ComponentContextProperties ccp ) throws ComponentExecutionException, ComponentContextException;
}
