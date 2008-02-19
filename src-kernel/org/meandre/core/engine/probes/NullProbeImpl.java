package org.meandre.core.engine.probes;

import java.util.Date;

import org.meandre.core.engine.Probe;

/** This class implements a probe for the engine that just dumps the information
 * to the provided PrintStream.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class NullProbeImpl 
implements Probe {

	/** Create a new probe given that does not record anything
	 * 
	 */
	public NullProbeImpl () {
	}
	
	/** The flow started executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowStart(String sFlowUniqueID, Date ts){
	}
	
	/** The flow stopped executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowFinish(String sFlowUniqueID, Date ts){
	}
	
	/** The flow aborted the execution.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowAbort(String sFlowUniqueID, Date ts){
	}

	/** The executable component finished initialization.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the initialization
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentInitialized(String sECID, Object owc, Date ts){
	}
	

	/** The executable component finished disposing itself.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentDisposed(String sECID, Object owc, Date ts){
	}
	

	/** The executable component pushed a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pushed
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentPushData(String sECID, Object owc, Object odata, Date ts) {
	}

	/** The executable component pulled a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pulled
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentPullData(String sECID, Object owc, Object odata, Date ts) {
	}

}
