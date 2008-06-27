package org.meandre.client.jmx.listener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.meandre.client.jmx.observer.ComponentStateObserver;
import org.meandre.core.engine.probes.jmx.ComponentAction;
import org.meandre.core.engine.probes.jmx.ComponentStates;
import org.meandre.core.engine.probes.jmx.FlowComponentActionsMBean;

/*
 * @(#) NonBlockingNewFlowDataListener.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */

public class NonBlockingComponentStateListener implements NotificationListener {

	  private final Executor executor =
	        Executors.newSingleThreadExecutor();
	  
	 private  ComponentStateObserver cso;
	  
	  
	  public NonBlockingComponentStateListener(ComponentStateObserver cso){
		  this.cso =cso;
	  }
	  
	  

	    public void handleNotification(final Notification n, final Object handback) {
	        executor.execute(new Runnable() {
	            public void run() {
	            	FlowComponentActionsMBean proxy = (FlowComponentActionsMBean)handback;
	            	ComponentAction caction= (ComponentAction)n.getUserData();
	            	String action = caction.getAction();
	            	String subjectName = caction.getSubjectName();
	            	String id = caction.getId();
	            
	            	if(action.equalsIgnoreCase(ComponentStates.ABORTED)){
	            		cso.aborted(id);
	            	}else if(action.equalsIgnoreCase(ComponentStates.COOLING_DOWN)){
	            		cso.cooling(id);
	            	}else if(action.equalsIgnoreCase(ComponentStates.DISPOSED)){
	            		cso.disposed(id);
	            	}else if(action.equalsIgnoreCase(ComponentStates.INITIALIZED)){
	            		cso.init(id);
	            	}else if(action.equalsIgnoreCase(ComponentStates.DATA_IN)){
	            		cso.readInput(id, subjectName);
	            	}else if(action.equalsIgnoreCase(ComponentStates.DATA_OUT)){
	            		cso.writeOutput(id, subjectName);
	            	}else if(action.equalsIgnoreCase(ComponentStates.PROP_READ)){
	            		cso.readProperty(id, subjectName);
	            	}else if(action.equalsIgnoreCase(ComponentStates.FIRED)){
	            		cso.fired(id);
	            	}else {
	            		System.out.println("Unsupported component action " + action);
	            	}
	            }
	        });
	    }


}
