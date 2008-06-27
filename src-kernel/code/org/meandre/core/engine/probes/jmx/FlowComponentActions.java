/*
 * @(#) FlowComponentActions.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.core.engine.probes.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;


public class FlowComponentActions extends NotificationBroadcasterSupport
implements FlowComponentActionsMBean{
	
	int sequenceNumber;
	
	HashMap<String, ComponentData> componentList = new HashMap<String, ComponentData>(40);

	public synchronized void addComponentData(String componentId, ComponentData componentData) {
		componentList.put(componentId, componentData);
	}

	public ComponentData getComponentData(String componentId) {
		return this.componentList.get(componentId);
	}

	public HashMap<String, ComponentData> getComponentDataHashMap() {
		return componentList;
	}

	/**Returns a list of components that have a certain action.
	 * 
	 */
	public ArrayList<ComponentData> getComponents(String action) {
		ArrayList<ComponentData> componentActionList = new ArrayList<ComponentData>(5);
		Iterator<String> it = this.componentList.keySet().iterator();
		String key;
		while(it.hasNext()){
		key = it.next();
			if(this.componentList.get(key).hasAction(action)){
			ComponentData cd = this.componentList.get(key);
			componentActionList.add(cd);
			}
		}
		return componentActionList;
	}

	public int getCountComponentList() {
		return componentList.size();
	}

	public void removeComponentData(String componentId) {
		this.componentList.remove(componentId);
	}

	
	
	/*
			// if cooling down then set the execution time to be
			// cooling down timestamp-fired timestamp 
			// if read property then update the numReadProperties
			// if pull or push data increment the pull push data count
	 * (non-Javadoc)
	 * @see org.meandre.core.engine.probes.jmx.FlowComponentActionsMBean#updateComponentData(java.lang.String, org.meandre.core.engine.probes.jmx.ComponentAction)
	 */
	public void updateComponentData(String componentId,
			ComponentAction componentAction) {
		if(this.componentList.get(componentId)!=null){
			if(componentAction.getAction()== ComponentStates.FIRED){
				this.componentList.get(componentId).setLastActivityTime(componentAction.getActionTime());
				this.componentList.get(componentId).setTimeFired(componentAction.getActionTime());
				int numFired = this.componentList.get(componentId).getNumFired();
				this.componentList.get(componentId).setNumFired(numFired+1);
				
			}else if(componentAction.getAction()== ComponentStates.COOLING_DOWN){
				this.componentList.get(componentId).setLastActivityTime(componentAction.getActionTime());
				ComponentData cd=this.componentList.get(componentId);
				long l=cd.getExecutionTime();
				l = l + (componentAction.getActionTime()-cd.getTimeFired());
				this.componentList.get(componentId).setExecutionTime(l);
			}else if(componentAction.getAction()== ComponentStates.ABORTED){
				this.componentList.get(componentId).setLastActivityTime(componentAction.getActionTime());
			}else if(componentAction.getAction()== ComponentStates.DATA_IN){
				this.componentList.get(componentId).setLastActivityTime(componentAction.getActionTime());
				ComponentData cd=this.componentList.get(componentId);
				long l = cd.getNumDataIn();
				this.componentList.get(componentId).setNumDataIn(l+1);
			}else if(componentAction.getAction()== ComponentStates.DATA_OUT){
				this.componentList.get(componentId).setLastActivityTime(componentAction.getActionTime());
				ComponentData cd=this.componentList.get(componentId);
				long l = cd.getNumDataOut();
				this.componentList.get(componentId).setNumDataOut(l+1);
			}else if(componentAction.getAction()== ComponentStates.PROP_READ){
				this.componentList.get(componentId).setLastActivityTime(componentAction.getActionTime());
				ComponentData cd=this.componentList.get(componentId);
				long l = cd.getNumReadProperties();
				this.componentList.get(componentId).setNumReadProperties(l+1);
			}else if(componentAction.getAction()== ComponentStates.DISPOSED){
				this.componentList.get(componentId).setLastActivityTime(componentAction.getActionTime());
			}else if(componentAction.getAction()== ComponentStates.INITIALIZED){
				this.componentList.get(componentId).setLastActivityTime(componentAction.getActionTime());
			}
			ComponentAction prevAction=null;
			if(this.componentList.get(componentId).actions.size()>0){
			prevAction=this.componentList.get(componentId).actions.peek();
			}
			
			String prevActionString=null;
			if(prevAction!=null){
			prevActionString = prevAction.getAction();	
			}else{
			prevActionString ="REST";
			}
			//add this action
			this.componentList.get(componentId).addAction(componentAction);
		
			Notification n =
			    new AttributeChangeNotification(this,
							    sequenceNumber++,
							    System.currentTimeMillis(),
							    "A Component action occured",
							    "componentId",
							    "string",
							    componentId+"="+prevActionString,
							    componentId+"="+componentAction.getAction());
			/* Now send the notification using the sendNotification method
			   inherited from the parent class
			   NotificationBroadcasterSupport.  */
			n.setUserData(componentAction);
			sendNotification(n);
		}
		
		
	}

}
