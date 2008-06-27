package org.meandre.client.jmx;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.meandre.core.engine.probes.jmx.FlowComponentActionsMBean;




/*
 * @(#) FlowComponentMonitor.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */

public class FlowComponentMonitor {
	ObjectName  flowComponentActionName;
	String token;
	MBeanServerConnection beanServerConnection;
	FlowComponentActionsMBean proxy;

	public FlowComponentMonitor(MBeanServerConnection beanServerConnection, String token) throws MalformedObjectNameException, NullPointerException {
		this.beanServerConnection = beanServerConnection;
		this.token = token;
		flowComponentActionName= new 
		ObjectName("org.meandre.core.engine.probes.jmx:type=FlowComponentActions,token="+token);
		 proxy = (FlowComponentActionsMBean) MBeanServerInvocationHandler
		.newProxyInstance(this.beanServerConnection, flowComponentActionName, FlowComponentActionsMBean.class,
				true);
	}
	
	

	public void addNotificationListener(NotificationListener listener, 
			NotificationFilter filter) throws InstanceNotFoundException, IOException {
		beanServerConnection.addNotificationListener(flowComponentActionName, listener, filter, proxy);
	}
	
	
	public void removeNotification(NotificationListener listener ) 
	throws InstanceNotFoundException, ListenerNotFoundException, IOException{
		beanServerConnection.removeNotificationListener(flowComponentActionName, listener);
	}



	/**
	 * @return the flowComponentActionName
	 */
	public ObjectName getFlowComponentActionName() {
		return flowComponentActionName;
	}



	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

}
