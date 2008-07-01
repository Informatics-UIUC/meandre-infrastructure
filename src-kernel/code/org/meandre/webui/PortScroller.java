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

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.meandre.configuration.CoreConfiguration;

/** Implements a port scroller with memory
 * 
 * @author Amit Kumar, Xavier Llor&agrave;
 * Created on Jun 23, 2008 1:11:15 AM
 * Modfied by X. to solve the MUTEX flawed design
 *
 */
public final class  PortScroller {

	/** The base port */
	private int basePort;
	
	/** The current port */
	private int currentPort;
	
	/** The map of port and name */
	Map<String, Integer> flowPortMap = new ConcurrentHashMap<String,Integer>(); 
	
	/** The queue of available ports */
	Queue<Integer> portQueue = new ConcurrentLinkedQueue<Integer>();

	/** The core configuration object used by the port scroller */
	private CoreConfiguration cnf;
	
	/** The unique object. This should have been done differently, I wil
	 * just fix the MUTEX flawed design. X.
	 */
	private static Map<CoreConfiguration, PortScroller> globalMapping = new ConcurrentHashMap<CoreConfiguration, PortScroller>(); 
	
	/** Create a port scroller object.
	 * 
	 * @param cnf The core configuration object
	 */
	protected PortScroller(CoreConfiguration cnf) {
		this.cnf = cnf;
		this.basePort = this.cnf.getBasePort();
		this.currentPort = this.cnf.getBasePort();
	}

	/** Returns the instance for this given base port.
	 * 
	 * @param cnf The core configuration object
	 * @return The port scroller
	 */
	public static PortScroller getInstance(CoreConfiguration cnf){
		PortScroller ps = getMappingForConfigurationObject(cnf);
		if ( ps!=null )
			return ps;
		else {
			ps = new PortScroller(cnf);
			globalMapping.put(cnf, ps);
			return ps;
		}
		
	}
	
	/** Check if the global mapping contains this core configuration.
	 * 
	 * @param cnf The core configuration object
	 * @return True if it contains this core configuration. False otherwise.
	 */
	private static PortScroller getMappingForConfigurationObject(
			CoreConfiguration cnf) {

		for ( CoreConfiguration c:globalMapping.keySet() )
			if ( c.equals(cnf) )
				return globalMapping.get(c);
		
		return null;
	}

	/**Return the next available port
	 * @param sFlowUniqueExecutionID 
	 * 
	 * @return
	 */
	public int nextAvailablePort(String sFlowUniqueExecutionID){
		if(flowPortMap.containsKey(sFlowUniqueExecutionID)){
			return this.flowPortMap.get(sFlowUniqueExecutionID);
		}
		int iThisPort = -1;
		synchronized(this){
			if(portQueue.isEmpty()) {
				iThisPort = ++currentPort;
				flowPortMap.put(sFlowUniqueExecutionID, iThisPort);	
			}
			else {
				iThisPort = portQueue.remove();
				this.flowPortMap.put(sFlowUniqueExecutionID, iThisPort);	
			}
		}
		return iThisPort;
	}
	
	/**Call this at the end of the flow
	 * 
	 * @param sFlowUniqueExecutionID
	 */
	public void releasePort(String sFlowUniqueExecutionID){
		synchronized ( this ) {
			if ( flowPortMap.containsKey(sFlowUniqueExecutionID)) {
				int thisPort = flowPortMap.get(sFlowUniqueExecutionID);
				flowPortMap.remove(sFlowUniqueExecutionID);
				portQueue.add(thisPort);
			}
		}
	}

	/**Returns the base port
	 * 
	 * @return The base port
	 */
	public int getBasePort(){
		return basePort;
	}
	
	/** Returns the port binded to a given executing flow ID.
	 * 
	 * @param flowUniqueExecutionID The unique executing flow ID
	 * @return The port number
	 */
	public int getPortOfRunningFlow(String flowUniqueExecutionID){
		synchronized ( this ) {
			return this.flowPortMap.get(flowUniqueExecutionID);
		}
	}
	
}
