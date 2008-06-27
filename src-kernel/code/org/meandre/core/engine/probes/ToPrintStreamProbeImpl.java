package org.meandre.core.engine.probes;

import java.io.PrintStream;
import java.util.Date;

import org.meandre.core.engine.Probe;

/** This class implements a probe for the engine that just dumps the information
 * to the provided PrintStream.
 * 
 * @author Xavier Llor&agrave;
 * @modified by Amit Kumar -Support for portnames
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
	public void probeFlowStart(String sFlowUniqueID, Date ts, String weburl,String token){
		psOut.println("Flow "+sFlowUniqueID+" started executing at "+ts + " weburl " + weburl);
	}
	
	/** The flow stopped executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowFinish(String sFlowUniqueID, Date ts,String token){
		psOut.println("Flow "+sFlowUniqueID+" finished executing at "+ts);
	}
	
	/** The flow aborted the execution.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowAbort(String sFlowUniqueID, Date ts,String token,String message){
		psOut.println("Flow "+sFlowUniqueID+" aborted execution at "+ts);
	}
	
	/** The executable component finished initialization.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the initialization
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 */
	public void probeExecutableComponentInitialized(String sECID, Object owc, Date ts, boolean bSerializedState) {
		psOut.println("Executable component "+sECID+" inialized at "+ts+" to state "+owc.toString());
	}
	
	/** The executable component requested execution abortion.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the initialization
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 */
	public void probeExecutableComponentAbort(String sECID, Object owc, Date ts, boolean bSerializedState) {
		psOut.println("Executable component "+sECID+" requested execution abortion at "+ts+" to state "+owc.toString());
	}

	/** The executable component finished disposing itself.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 */
	public void probeExecutableComponentDisposed(String sECID, Object owc, Date ts, boolean bSerializedState) {
		psOut.println("Executable component "+sECID+" disposed at "+ts+" to state "+owc.toString());		
	}
	

	/** The executable component pushed a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pushed
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 * @param bSerializedData The serialized data
	 */
	public void probeExecutableComponentPushData(String sECID, Object owc, Object odata, Date ts,String portName, boolean bSerializedState, boolean bSerializedData) {
		psOut.println("Executable component "+sECID+" pushed data "+odata.toString()+" at "+ts+" to state "+owc.toString());	
	}

	/** The executable component pulled a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pulled
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 * @param bSerializedData The serialized data
	 */
	public void probeExecutableComponentPullData(String sECID, Object owc, Object odata, Date ts, String portName,boolean bSerializedState, boolean bSerializedData) {
		psOut.println("Executable component "+sECID+" pulled data "+odata.toString()+" at "+ts+" to state "+owc.toString());	
	}

	/** The executable component was fired.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 */
	public void probeExecutableComponentFired(String sECID, Object owc, Date ts, boolean bSerializedState) {
		psOut.println("Executable component "+sECID+" fire at "+ts+" to state "+owc.toString());		
	}
	

	/** The executable component is cooling down.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 */
	public void probeExecutableComponentCoolingDown(String sECID, Object owc, Date ts, boolean bSerializedState) {
		psOut.println("Executable component "+sECID+" is cooling down at "+ts+" to state "+owc.toString());		
	}
	

	/** The executable component requested a property value.
	 * 
	 * @param sECID The unique executable component ID
	 * @param sPropertyName The requested property
	 * @param sPropertyValue The property value
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentGetProperty(String sECID, String sPropertyName, String sPropertyValue, Date ts)  {
		psOut.println("Executable component "+sECID+" requested property "+sPropertyName+" and got "+sPropertyValue+" at "+ts.toString());		
	}
	



}
