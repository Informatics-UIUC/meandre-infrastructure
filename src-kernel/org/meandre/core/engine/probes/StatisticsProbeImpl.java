package org.meandre.core.engine.probes;

import java.util.Date;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.core.engine.Probe;
import org.meandre.core.engine.ProbeException;

/** This class implements a probe for the engine that collects statistics of the 
 * execution.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class StatisticsProbeImpl 
implements Probe {

	/** The flow execution states */
	private enum FlowStates { 
			RUNNING, 			// The flow is running
			ENDED, 				// The flow ended
			ABORTED 			// The flow aborted
		};
	
	/** The executable component execution states */
	private enum ExecutableComponentStates { 
			INITIALIZED,		// The component was initialized
			FIRED, 				// The component got fired
			COOLING_DOWN, 		// The component is cooling down
			DISPOSED,			// The component got disposed
			ABORTED				// The executable component aborted
		}

	/** The flow execution ID */
	protected String sFlowID = null;
	
	/** The flow starting date */
	protected Date dateFlowStart = null;
	
	/** The latest updated date */
	protected Date dateLatestDate = null;
	
	/** The flow state */
	protected FlowStates flowStatus;
	
	/** The table of the executable component states */
	protected Hashtable<String,ExecutableComponentStates> htExecutableComponentsStates = null;
	
	/** The number of times a component got fired */
	protected Hashtable<String,Long> htExecutableComponentsTimesFired = null;

	/** The component execution total time */
	protected Hashtable<String,Long> htExecutableComponentsExecutionTime = null;

	/** When was the component fired */
	protected Hashtable<String,Long> htExecutableComponentsFiredTimeStamp = null;
	
	/** The number of data units received */
	protected Hashtable<String,Long> htExecutableComponentsExecutionDataIn = null;
	
	/** The number of data units generated */
	protected Hashtable<String,Long> htExecutableComponentsExecutionDataOut = null;

	/** The number of data units generated */
	protected Hashtable<String,Long> htExecutableComponentsExecutionReadProperties = null;

	/** Initialize the statistics probe.
	 * 
	 */
	public StatisticsProbeImpl () {
		this.sFlowID = null;
		this.flowStatus = null;
		this.dateFlowStart = null;
		this.dateLatestDate = null;
		this.htExecutableComponentsStates = new Hashtable<String,ExecutableComponentStates>();
		this.htExecutableComponentsTimesFired = new Hashtable<String,Long>();  
		this.htExecutableComponentsExecutionTime = new Hashtable<String,Long>(); 
		this.htExecutableComponentsFiredTimeStamp = new Hashtable<String,Long>();  
		this.htExecutableComponentsExecutionDataIn = new Hashtable<String,Long>();  
		this.htExecutableComponentsExecutionDataOut = new Hashtable<String,Long>(); 
		this.htExecutableComponentsExecutionReadProperties = new Hashtable<String,Long>(); 
	}
	
	/** The flow started executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowStart(String sFlowUniqueID, Date ts) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.sFlowID = sFlowUniqueID;
		this.dateFlowStart = ts;
		this.flowStatus = FlowStates.RUNNING;
	}
	
	/** The flow stopped executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowFinish(String sFlowUniqueID, Date ts) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.flowStatus = FlowStates.ENDED;
	}
	
	/** The flow aborted the execution.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowAbort(String sFlowUniqueID, Date ts) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.flowStatus = FlowStates.ABORTED;
	}

	/** The executable component finished initialization.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the initialization
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 */
	public void probeExecutableComponentInitialized(String sECID, Object owc, Date ts, boolean bSerializeState) {
		// Update the timestamp
		this.dateLatestDate = ts;

		this.htExecutableComponentsStates.put(sECID,ExecutableComponentStates.INITIALIZED);
		this.htExecutableComponentsTimesFired.put(sECID,0L);
		this.htExecutableComponentsExecutionTime.put(sECID,0L);
		this.htExecutableComponentsFiredTimeStamp.put(sECID,0L);
		this.htExecutableComponentsExecutionDataIn.put(sECID,0L);
		this.htExecutableComponentsExecutionDataOut.put(sECID,0L);
		this.htExecutableComponentsExecutionReadProperties.put(sECID,0L);
	}

	/** The executable component requested execution abortion.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the initialization
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 */
	public void probeExecutableComponentAbort(String sECID, Object owc, Date ts, boolean bSerializeState) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.htExecutableComponentsStates.put(sECID,ExecutableComponentStates.ABORTED);
	}

	/** The executable component finished disposing itself.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 * @param bSerializedData The data provided has been serialized
	 */
	public void probeExecutableComponentDisposed(String sECID, Object owc, Date ts, boolean bSerializeState) {
		// Update the timestamp
		this.dateLatestDate = ts;

		this.htExecutableComponentsStates.put(sECID,ExecutableComponentStates.DISPOSED);
	}

	/** The executable component pushed a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pushed
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 * @param bSerializedData The data provided has been serialized
	 */
	public void probeExecutableComponentPushData(String sECID, Object owc, Object odata, Date ts, boolean bSerializeState, boolean bSerializedData) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.htExecutableComponentsExecutionDataOut.put(
				sECID, 
				this.htExecutableComponentsExecutionDataOut.get(sECID)+1
			);
	}

	/** The executable component pulled a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pulled
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentPullData(String sECID, Object owc, Object odata, Date ts, boolean bSerializeState, boolean bSerializedData) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.htExecutableComponentsExecutionDataIn.put(
				sECID, 
				this.htExecutableComponentsExecutionDataIn.get(sECID)+1
			);
	}
	
	/** The executable component was fired.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 */
	public void probeExecutableComponentFired(String sECID, Object owc, Date ts, boolean bSerializeState) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.htExecutableComponentsStates.put(sECID,ExecutableComponentStates.FIRED);
		this.htExecutableComponentsFiredTimeStamp.put(sECID, ts.getTime());
		this.htExecutableComponentsTimesFired.put (
				sECID,
				this.htExecutableComponentsTimesFired.get(sECID)+1
			);
	}

	/** The executable component is cooling down.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 * @param bSerializeState The wrapped component is serialized
	 */
	public void probeExecutableComponentCoolingDown(String sECID, Object owc, Date ts, boolean bSerializeState) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.htExecutableComponentsStates.put(sECID,ExecutableComponentStates.COOLING_DOWN);
		this.htExecutableComponentsExecutionTime.put(
				sECID, 
				this.htExecutableComponentsExecutionTime.get(sECID)+
					ts.getTime()-this.htExecutableComponentsFiredTimeStamp.get(sECID)
			);
	}

	/** The executable component requested a property value.
	 * 
	 * @param sECID The unique executable component ID
	 * @param sPropertyName The requested property
	 * @param sPropertyValue The property value
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentGetProperty(String sECID, String sPropertyName, String sPropertyValue, Date ts) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.htExecutableComponentsExecutionReadProperties.put(
				sECID,
				this.htExecutableComponentsExecutionReadProperties.get(sECID)
			);
	}

	/** Returns the current statistics.
	 * 
	 * @return The JSONObject containing the statistics
	 * @throws ProbeException A problem arised with serialzing the statistics
	 */
	public JSONObject getSerializedStatistics () throws ProbeException {
		JSONObject joRes = new JSONObject();
	
		try {
			// Add the flow statistics
			joRes.put("flow_unique_id", sFlowID);
			joRes.put("flow_state", getFlowStateString(flowStatus));
			joRes.put("started_at", dateFlowStart);
			joRes.put("latest_probe_at", dateLatestDate);
			joRes.put("runtime", dateLatestDate.getTime()-dateFlowStart.getTime());
			
			// Add the executable component statistics
			JSONArray jaExecCompStats = new JSONArray();
			for ( String sKey:htExecutableComponentsStates.keySet() ) {
				JSONObject joExecComp = new JSONObject();
				joExecComp.put("executable_component_instance_id", sKey);
				joExecComp.put("executable_component_state", getExecutableComponentStateString(htExecutableComponentsStates.get(sKey)));
				joExecComp.put("times_fired", htExecutableComponentsTimesFired.get(sKey));
				joExecComp.put("accumulated_runtime", htExecutableComponentsExecutionTime.get(sKey));
				joExecComp.put("pieces_of_data_in", htExecutableComponentsExecutionDataIn.get(sKey));
				joExecComp.put("pieces_of_data_out", htExecutableComponentsExecutionDataOut.get(sKey));
				joExecComp.put("number_of_read_properties", htExecutableComponentsExecutionReadProperties.get(sKey));
				jaExecCompStats.put(joExecComp);
			}
			
			// Add the component statistics to the result object
			joRes.put("executable_components_statistics", jaExecCompStats);
		} catch (JSONException e) {
			throw new ProbeException(e);
		}
		
		
		return joRes;
	}

	/** Returns a readable version of the executable component state.
	 * 
	 * @param ecs The executable component state
	 * @return The string describing the executable component state
	 */
	private String getExecutableComponentStateString ( ExecutableComponentStates ecs) {
		switch (ecs) {
			case INITIALIZED:	return "initialized";
			case ABORTED:		return "aborted";
			case DISPOSED:		return "disposed";
			case FIRED:			return "fired";
			case COOLING_DOWN: 	return "cooling down";
			default:      		return "unknown flow execution status!";
		}
	}

	/** Returns a readable version of the flow execution state.
	 * 
	 * @param fs The flow state
	 * @return The string describing the flow state
	 */
	private String getFlowStateString  ( FlowStates fs )
	{
		switch (fs) {
			case ABORTED: return "aborted";
			case ENDED:   return "ended";
			case RUNNING: return "running";
			default:      return "unknown flow execution status!";
		}
	}
 }
