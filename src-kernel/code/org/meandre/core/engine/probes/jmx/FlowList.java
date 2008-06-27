/*
 * @(#) FlowList.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.core.engine.probes.jmx;

import java.util.HashMap;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

/**This bean stores the list of running flows and the properties.
 * It is used by the JMX probe
 * 
 * @author Amit Kumar
 * Created on Jun 22, 2008 1:22:01 AM
 *
 */
public class FlowList extends NotificationBroadcasterSupport
implements FlowListMBean{
	
	HashMap<String,FlowData> flowList = new HashMap<String,FlowData>(10);
	private long sequenceNumber = 0l;

	public int getCountFlowList() {
		return flowList.size();
	}

	public FlowData getFlowData(String flowId) {
		return this.flowList.get(flowId);
	}

	public HashMap<String, FlowData> getFlowDataHashMap() {
		return flowList;
	}

	
	public synchronized void addFlowData(String flowId, FlowData flowData){
		int oldSize=this.flowList.size();
		this.flowList.put(flowId, flowData);
		Notification n =
		    new AttributeChangeNotification(this,
						    sequenceNumber++,
						    System.currentTimeMillis(),
						    "New Flow added",
						    "flowList",
						    "int",
						    flowId+":"+oldSize,
						    flowId+":"+this.flowList.size());
		n.setUserData(flowData);
		/* Now send the notification using the sendNotification method
		   inherited from the parent class
		   NotificationBroadcasterSupport.  */
		sendNotification(n);
	}
	
	
	public synchronized void removeFlowData(String flowId,FlowData flowData){
		int oldSize=this.flowList.size();
		this.flowList.remove(flowId);
		if(this.flowList.size()!= oldSize){
		Notification n =
		    new AttributeChangeNotification(this,
						    sequenceNumber++,
						    System.currentTimeMillis(),
						    "A Flow was removed",
						    "flowList",
						    "int",
						    flowId+":"+oldSize,
						    flowId+":"+this.flowList.size());
			n.setUserData(flowData);
		//n.setUserData(flowData.getToken()+"="+flowData.getStatus());
		/* Now send the notification using the sendNotification method
		   inherited from the parent class
		   NotificationBroadcasterSupport.  */
		sendNotification(n);
		}
	}
	
	
	public synchronized  void updateFlowData(String flowId, FlowData flowData){
		FlowData oldData = this.flowList.put(flowId, flowData);
		String status = "missing";
		if(oldData!=null){
			status = oldData.getStatus();
		}
		Notification n =
		    new AttributeChangeNotification(this,
						    sequenceNumber++,
						    System.currentTimeMillis(),
						    "A Flow was updated",
						    "flowList",
						    "string",
						    flowId+":"+status,
						    flowId+":"+flowData.getStatus());
		n.setUserData(flowData);
		/* Now send the notification using the sendNotification method
		   inherited from the parent class
		   NotificationBroadcasterSupport.  */
		sendNotification(n);
	}
	
	
	
	  public MBeanNotificationInfo[] getNotificationInfo() {
			String[] types = new String[] {
			    AttributeChangeNotification.ATTRIBUTE_CHANGE
			};
			String name = AttributeChangeNotification.class.getName();
			String description = "An attribute of this FlowList has changed";
			MBeanNotificationInfo info =
			    new MBeanNotificationInfo(types, name, description);
			return new MBeanNotificationInfo[] {info};
			
	  }
	

	

}
