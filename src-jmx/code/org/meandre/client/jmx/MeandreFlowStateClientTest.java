/*
 * @(#)file      Client.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.1
 * @(#)lastedit  04/01/12
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package org.meandre.client.jmx;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.meandre.client.jmx.listener.NonBlockingFlowStateListener;
import org.meandre.client.jmx.observer.FlowStateObserver;
import org.meandre.core.engine.probes.jmx.FlowListMBean;

public class MeandreFlowStateClientTest {

	
	ObjectName flowListName;
	MBeanServerConnection mbsc;
	JMXConnector jmxc;
	
	public MeandreFlowStateClientTest() throws IOException, MalformedObjectNameException, NullPointerException{
		JMXServiceURL url = new JMXServiceURL(
		"service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
		jmxc = JMXConnectorFactory.connect(url, null);
		
		flowListName = new ObjectName(
		"org.meandre.core.engine.probes.jmx:type=FlowList");
	}
	
	public boolean connect() throws IOException{
		mbsc = jmxc.getMBeanServerConnection();	
		if(mbsc!=null){
			return true;
		}
		return false;
	}
	
	
	public void addFlowListListener(NonBlockingFlowStateListener nbl) throws InstanceNotFoundException, IOException{
		FlowListMBean proxy = (FlowListMBean) MBeanServerInvocationHandler
		.newProxyInstance(mbsc, flowListName, FlowListMBean.class,
				true);
		mbsc.addNotificationListener(flowListName, nbl, null, proxy);
		
	}
	
	public static void main(String[] args) 
	throws MalformedObjectNameException, NullPointerException, IOException, InstanceNotFoundException, InterruptedException {
			MeandreFlowStateClientTest mct = new MeandreFlowStateClientTest();
			mct.connect();
			FlowStateObserver fso = new FlowStateObserverImpl();
			NonBlockingFlowStateListener nbl = new NonBlockingFlowStateListener(fso);
			mct.addFlowListListener(nbl);
			
			
			//Thread.currentThread().sleep(Integer.MAX_VALUE);
			Thread.sleep(Integer.MAX_VALUE);

			/*
			 * nbs.addNotificationListener(javax.management.NotificationListener,
			 * javax.management.NotificationFilter, java.lang.Object)
			 */

			// echo("\nClose the connection to the server");
			// jmxc.close();
			// echo("\nBye! Bye!");

		
	}

	
}
