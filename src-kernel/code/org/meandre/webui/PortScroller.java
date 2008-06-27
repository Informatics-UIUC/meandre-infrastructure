/*
 * @(#) PortScroller.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.webui;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * @author Amit Kumar
 * Created on Jun 23, 2008 1:11:15 AM
 *
 */
public final class  PortScroller {

	private int basePort;
	private int currentPort;
	Hashtable<String, Integer> flowPortMap = new Hashtable<String,Integer>(10); 
	Queue<Integer> portQueue = new LinkedList<Integer>();
	private static PortScroller portScroller;
	
	
	
	private PortScroller(int basePort) {
		this.basePort = basePort;
		this.currentPort= basePort;
	}


	public static PortScroller getInstance(int basePort){
		if(portScroller==null){
			portScroller = new PortScroller(basePort);
		}
		return portScroller;
	}
	
	
	/**Return the next available port
	 * @param flowUniqueExecutionID 
	 * 
	 * @return
	 */
	public int nextAvailablePort(String flowUniqueExecutionID){
		if(this.flowPortMap.get(flowUniqueExecutionID)!=null){
			return this.flowPortMap.get(flowUniqueExecutionID);
		}
		int thisPort=-1;
			synchronized(this){
				if(portQueue.isEmpty()){
					currentPort++;
					thisPort = currentPort;
					this.flowPortMap.put(flowUniqueExecutionID, thisPort);	
				}else{
					thisPort=portQueue.remove();
					this.flowPortMap.put(flowUniqueExecutionID, thisPort);	
				}
			}
		
		return thisPort;
	}
	
	
	/**Returns current port
	 * 
	 * @return
	 */
	public int getCurrentPort(){
		return currentPort;
	}
	
	
	public int getPort(String flowUniqueExecutionID){
		return this.flowPortMap.get(flowUniqueExecutionID);
	}
	
	/**Call this at the end of the flow
	 * 
	 * @param flowUniqueExecutionID
	 */
	public void releasePort(String flowUniqueExecutionID){
		try{
		int thisPort = this.flowPortMap.get(flowUniqueExecutionID);
		synchronized(this){
		portQueue.add(thisPort);
		}
		}catch(Exception ex){
			
		}
		
	}


	
}
