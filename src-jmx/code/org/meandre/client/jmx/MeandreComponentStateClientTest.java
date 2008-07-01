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
import java.util.Hashtable;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;

import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.meandre.client.jmx.listener.NonBlockingComponentStateListener;
import org.meandre.client.jmx.observer.ComponentStateObserver;


public class MeandreComponentStateClientTest {
	
	Hashtable<String,String> flowList = new Hashtable<String,String>(10);
	
	ObjectName flowListName;
	MBeanServerConnection mbsc;
	JMXConnector jmxc;
	
	public MeandreComponentStateClientTest() throws IOException, MalformedObjectNameException, NullPointerException{
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
		
		jmxc = JMXConnectorFactory.connect(url, null);
	}
	
	public boolean connect() throws IOException{
		mbsc = jmxc.getMBeanServerConnection();	
		if(mbsc!=null){
			return true;
		}
		return false;
	}
	
	

	public static void main(String[] args) 
	throws MalformedObjectNameException, NullPointerException, 
	IOException, InstanceNotFoundException, InterruptedException {
			MeandreComponentStateClientTest mct = new MeandreComponentStateClientTest();
			mct.connect();
			String token= "1214517596931";
			FlowComponentMonitor flowComponentMonitor = new FlowComponentMonitor(mct.mbsc, token);
			ComponentStateObserver cso = new ComponentStateObserverImpl();
			NonBlockingComponentStateListener nblfd = new NonBlockingComponentStateListener(cso);
			flowComponentMonitor.addNotificationListener(nblfd, null);
			//Thread.currentThread().sleep(Integer.MAX_VALUE);
			Thread.sleep(Integer.MAX_VALUE);
	}

	
}
