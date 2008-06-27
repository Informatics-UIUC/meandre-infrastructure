/*
 * @(#) JMXProbeImpl.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.core.engine.probes.jmx;

import java.util.Date;
import java.util.Hashtable;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.core.engine.Probe;
import org.meandre.core.engine.ProbeException;
public class JMXProbeImpl implements Probe {
	
	private FlowList flowList;
	private MBeanServer mbeanServer;
	ObjectName flowName;
	private FlowComponentActions flowComponentActions;
	
	
	
	private enum FlowStates { 
			RUNNING, 			// The flow is running
			ENDED, 				// The flow ended
			ABORTED 			// The flow aborted
		};
	
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

	
	/**Initialize the JMSProbeImpl
	 * @param beanServer 
	 * 
	 * @param mbeansServer
	 */
	public JMXProbeImpl(MBeanServer mbeanServer,FlowList flowList,String token){
		this.flowList = flowList;
		this.mbeanServer = mbeanServer;
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
		
		try {
			flowName= new ObjectName("org.meandre.core.engine.probes.jmx:type=FlowComponentActions,token="+token);
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		// Register the FlowComponentActions bean
		flowComponentActions = new FlowComponentActions();
		try {
			this.mbeanServer.registerMBean(flowComponentActions,flowName);
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
	}


	
	
	/** The flow started executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowStart(String sFlowUniqueID, Date ts, String weburl) {
		// Update the timestamp
	
		this.dateLatestDate = ts;
		this.sFlowID = sFlowUniqueID;
		this.dateFlowStart = ts;
		this.flowStatus = FlowStates.RUNNING;

		FlowData flowData = new FlowData();
		flowData.setId(sFlowUniqueID);
		flowData.setLatestDate(ts);
		flowData.setDateStart(ts);
		flowData.setStatus("RUNNING");
		flowData.setWebUrl(weburl);
		flowList.addFlowData(sFlowUniqueID, flowData);
	}
	
	/** The flow stopped executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowFinish(String sFlowUniqueID, Date ts) {
		// Update the timestamp
		this.dateLatestDate = ts;
		FlowData flowData = new FlowData();
		flowData.setId(sFlowUniqueID);
		flowData.setLatestDate(ts);
		flowData.setStatus("ENDED");
		flowData.setWebUrl("nil");
		flowList.updateFlowData(sFlowUniqueID, flowData);
		this.flowStatus = FlowStates.ENDED;
		// don't need this anymore
		try {
			this.mbeanServer.unregisterMBean(flowName);
		} catch (InstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** The flow aborted the execution.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowAbort(String sFlowUniqueID, Date ts,String message) {
		// Update the timestamp
		this.dateLatestDate = ts;
		FlowData flowData = new FlowData();
		flowData.setId(sFlowUniqueID);
		flowData.setLatestDate(ts);
		flowData.setStatus("ABORTED");
		flowData.setWebUrl("nil");
		flowList.updateFlowData(sFlowUniqueID, flowData);
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
		ComponentAction caction = new ComponentAction();
		caction.setId(sECID);
		caction.setAction(ComponentStates.INITIALIZED);
		caction.setActionTime(ts.getTime());
		ComponentData cdata = new ComponentData();
		cdata.setId(sECID);
		flowComponentActions.addComponentData(sECID,cdata);
		flowComponentActions.updateComponentData(sECID, caction);
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
		ComponentAction caction = new ComponentAction();
		caction.setId(sECID);
		caction.setAction(ComponentStates.ABORTED);
		caction.setActionTime(ts.getTime());
		flowComponentActions.updateComponentData(sECID, caction);
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
		ComponentAction caction = new ComponentAction();
		caction.setId(sECID);
		caction.setAction(ComponentStates.DISPOSED);
		caction.setActionTime(ts.getTime());
		flowComponentActions.updateComponentData(sECID, caction);
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
	public void probeExecutableComponentPushData(String sECID, Object owc, Object odata, Date ts, 
			String portName,boolean bSerializeState, boolean bSerializedData) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.htExecutableComponentsExecutionDataOut.put(
				sECID, 
				this.htExecutableComponentsExecutionDataOut.get(sECID)+1
			);
		
		ComponentAction caction = new ComponentAction();
		caction.setId(sECID);
		caction.setAction(ComponentStates.DATA_OUT);
		caction.setActionTime(ts.getTime());
		caction.setSubjectName(portName);
		flowComponentActions.updateComponentData(sECID, caction);
	
	}

	/** The executable component pulled a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pulled
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentPullData(String sECID, Object owc, Object odata, Date ts,
			String portName, boolean bSerializeState, boolean bSerializedData) {
		// Update the timestamp
		this.dateLatestDate = ts;
		
		this.htExecutableComponentsExecutionDataIn.put(
				sECID, 
				this.htExecutableComponentsExecutionDataIn.get(sECID)+1
			);
		ComponentAction caction = new ComponentAction();
		caction.setId(sECID);
		caction.setAction(ComponentStates.DATA_IN);
		caction.setActionTime(ts.getTime());
		caction.setSubjectName(portName);
		flowComponentActions.updateComponentData(sECID, caction);
	
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
		ComponentAction caction = new ComponentAction();
		caction.setId(sECID);
		caction.setAction(ComponentStates.FIRED);
		caction.setActionTime(ts.getTime());
		flowComponentActions.updateComponentData(sECID, caction);
	
		
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
		ComponentAction caction = new ComponentAction();
		caction.setId(sECID);
		caction.setAction(ComponentStates.COOLING_DOWN);
		caction.setActionTime(ts.getTime());
		flowComponentActions.updateComponentData(sECID, caction);
	
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
		
		if ( this.htExecutableComponentsExecutionReadProperties.get(sECID)==null ) 
			this.htExecutableComponentsExecutionReadProperties.put(sECID,1L);
		else
			this.htExecutableComponentsExecutionReadProperties.put(
					sECID,
					this.htExecutableComponentsExecutionReadProperties.get(sECID)+1
				);
		ComponentAction caction = new ComponentAction();
		caction.setId(sECID);
		caction.setAction(ComponentStates.PROP_READ);
		caction.setActionTime(ts.getTime());
		caction.setSubjectName(sPropertyName);
		flowComponentActions.updateComponentData(sECID, caction);
	

		
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
				Long lNumRP;
				if ( htExecutableComponentsExecutionReadProperties.get(sKey)==null )
					lNumRP = 0L;
				else 
					lNumRP = htExecutableComponentsExecutionReadProperties.get(sKey);
				joExecComp.put("number_of_read_properties", lNumRP);
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
