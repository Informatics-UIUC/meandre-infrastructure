package org.meandre.client.jmx.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.meandre.client.jmx.observer.FlowStateObserver;
import org.meandre.core.engine.probes.jmx.FlowData;
import org.meandre.core.engine.probes.jmx.FlowListMBean;
import org.meandre.core.engine.probes.jmx.FlowStates;

/*
 * @(#) NonBlockingListener.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */

public class NonBlockingFlowStateListener implements NotificationListener {

	  private final Executor executor =
		  	Executors.newSingleThreadExecutor();
	  
	  private FlowStateObserver fso;
	 

	    public NonBlockingFlowStateListener(FlowStateObserver fso) {
	    	this.fso = fso;
	    }

		public void handleNotification(final Notification n, final Object handback) {
	        executor.execute(new Runnable() {
	            public void run() {
	               // blockingOperation();
	            	FlowListMBean proxy = (FlowListMBean)handback;
	            	HashMap<String,FlowData> hm=proxy.getFlowDataHashMap();
	        	    Iterator<String> it = hm.keySet().iterator();
	        	    String key = null;
	        	    FlowData value=null;
	        	    while(it.hasNext()){
	        	    	key = it.next();
	        	    	value = hm.get(key);
	        	    	System.out.println(hm.get(key).getId()+ "  " + hm.get(key).getWebUrl() + " " +value.toString());
	        	    }
	        	    FlowData flowData = (FlowData)n.getUserData();
	        	    
	        	    if(flowData!=null){
	        	    String flowId=	flowData.getId();
	        	    String webUrl=flowData.getWebUrl();
	        	    String status = flowData.getStatus();
	        	    String message = flowData.getMessage();
	        	    
	        	    if(status.equalsIgnoreCase(FlowStates.RUNNING)){
	        	    	fso.running(flowId, webUrl);
	        	    }else if(status.equalsIgnoreCase(FlowStates.ENDED)){
	        	    	fso.finish(flowId);
	        	    }else if(status.equalsIgnoreCase(FlowStates.ABORTED)){
	        	    	fso.abort(flowId, message);
	        	    }
	        	    
	        	    /*StringTokenizer stok = new StringTokenizer(flowInfo,"=");
	        	    	if(stok.countTokens()==2){
	        	    		String token = stok.nextToken();
	        	    		String status = stok.nextToken();
	        	    		String thisId=flowList.get(token);
	        	    		if(thisId==null && status.equalsIgnoreCase(FlowStates.RUNNING)){
	        	    			// start of the flow
	        	    			flowList.put(token, status);
	        	    		}else if(thisId != null && status.equalsIgnoreCase(FlowStates.ABORTED)){
	        	    			// abort an error happened
	        	    			flowList.put(token, status);
	        	    		}else if(thisId != null && status.equalsIgnoreCase(FlowStates.ENDED)){
	        	    			// flow ended
	        	    			flowList.put(token, status);
		        	    	}
	        	    	}
	        	    	*/
	        	    }
	        	    
	        	    
	            	
	            
	            }

				
	        });
	    }


}
