package org.meandre.core.engine;

import java.util.Date;

/** This interface define the API for the probing facility.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public interface Probe {

	/** The probe commands */
	public static enum ProbeCommands { 
		FLOW_STARTED,			// Flow started executing
		FLOW_FINISHED,			// Flow finished executing
		FLOW_ABORTED,			// Flow aborted execution
		
		EXECUTABLE_COMPONENT_INITIALIZED,	// The executable component has been initialized
		EXECUTABLE_COMPONENT_DISPOSED,		// The executable component has been initialized

		EXECUTABLE_COMPONENT_PUSH_DATA, 	// The executable component has push a piece of data
		EXECUTABLE_COMPONENT_PULL_DATA	 	// The executable component has pull a piece of data
	}
	
	
	/** The flow started executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowStart(String sFlowUniqueID, Date ts);
	
	/** The flow stopped executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowFinish(String sFlowUniqueID, Date ts);
	
	/** The flow aborted the execution.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowAbort(String sFlowUniqueID, Date ts);

	/** The executable component finished initialization.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the initialization
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentInitialized(String sECID, Object owc, Date ts);
	
	/** The executable component finished disposing itself.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentDisposed(String sECID, Object owc, Date ts);

	/** The executable component pushed a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pushed
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentPushData(String sECID, Object owc, Object odata, Date ts);

	/** The executable component pulled a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pulled
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentPullData(String sECID, Object owc, Object odata, Date ts);
	

}
