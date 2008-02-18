package org.meandre.core.engine.probes;

import java.io.PrintStream;
import java.util.Date;

import org.meandre.core.engine.Probe;

/** This class implements a probe for the engine that just dumps the information
 * to the provided PrintStream.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ToPrintStreamProbeImpl 
implements Probe {
	
	/** The destination PrintStream */
	protected PrintStream psOut = null;
	
	/** Create a new probe given a print stream
	 * 
	 * @param ps The target print stream
	 */
	public ToPrintStreamProbeImpl ( PrintStream ps ) {
		this.psOut = ps;
	}
	
	/** The flow started executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowStart(String sFlowUniqueID, Date ts){
		psOut.println("Flow "+sFlowUniqueID+" started executing at "+ts);
	}
	
	/** The flow stopped executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowFinish(String sFlowUniqueID, Date ts){
		psOut.println("Flow "+sFlowUniqueID+" finished executing at "+ts);
	}
	
	/** The flow aborted the execution.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowAbort(String sFlowUniqueID, Date ts){
		psOut.println("Flow "+sFlowUniqueID+" aborted execution at "+ts);
	}
	
	/** The executable component finished initialization.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the initialization
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentInitialized(String sECID, Object owc, Date ts) {
		psOut.println("Executable component "+sECID+" inialized at "+ts+" to state "+owc.toString());
	}
	

	/** The executable component finished disposing itself.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentDisposed(String sECID, Object owc, Date ts) {
		psOut.println("Executable component "+sECID+" inialized at "+ts+" to state "+owc.toString());		
	}
	

}
