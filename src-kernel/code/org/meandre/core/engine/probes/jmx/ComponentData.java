/*
 * @(#) ComponentData.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.core.engine.probes.jmx;


import java.util.Iterator;
import java.util.Stack;
/** Information and history of component lifecycle.
 * 
 * @author Amit Kumar
 * Created on Jun 22, 2008 3:07:08 PM
 *
 */
public class ComponentData {
	
	int MAX_STACK_SIZE = 10;
	
	// component id
	String id;
	// actions related to the component with id = id
	Stack<ComponentAction> actions = new Stack<ComponentAction>();
	// number of times fired
	int numFired;
	// the last time activity was noticed on this component
	long lastActivityTime;
	// the last time this component was fired
	long timeFired;
	// total execution time
	long executionTime;
	// number of inputs read
	long numDataIn;
	// number of times data pushed
	long numDataOut;
	// number of times properties read
	long numReadProperties;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the timeFired
	 */
	public int getNumFired() {
		return numFired;
	}
	/**
	 * @param timeFired the timeFired to set
	 */
	public void setNumFired(int numFired) {
		this.numFired = numFired;
	}
	/**
	 * @return the numDataIn
	 */
	public long getNumDataIn() {
		return numDataIn;
	}
	/**
	 * @param numDataIn the numDataIn to set
	 */
	public void setNumDataIn(long numDataIn) {
		this.numDataIn = numDataIn;
	}
	/**
	 * @return the numDataOut
	 */
	public long getNumDataOut() {
		return numDataOut;
	}
	/**
	 * @param numDataOut the numDataOut to set
	 */
	public void setNumDataOut(long numDataOut) {
		this.numDataOut = numDataOut;
	}
	/**
	 * @return the numReadProperties
	 */
	public long getNumReadProperties() {
		return numReadProperties;
	}
	/**
	 * @param numReadProperties the numReadProperties to set
	 */
	public void setNumReadProperties(long numReadProperties) {
		this.numReadProperties = numReadProperties;
	}
	

	public void addAction(ComponentAction action){
		this.actions.push(action);
	}
	/**
	 * @return the timeFired
	 */
	public long getTimeFired() {
		return timeFired;
	}
	/**
	 * @param timeFired the timeFired to set
	 */
	public void setTimeFired(long timeFired) {
		this.timeFired = timeFired;
	}
	/**
	 * @return the executionTime
	 */
	public long getExecutionTime() {
		return executionTime;
	}
	/**
	 * @param executionTime the executionTime to set
	 */
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	/**
	 * @return the lastActivityTime
	 */
	public long getLastActivityTime() {
		return lastActivityTime;
	}
	/**
	 * @param lastActivityTime the lastActivityTime to set
	 */
	public void setLastActivityTime(long lastActivityTime) {
		this.lastActivityTime = lastActivityTime;
	}
	
	
	/**Checks if the stack has an action of type
	 * 
	 * @param status
	 * @return
	 */
	public boolean hasAction(String action){
		Iterator<ComponentAction> cit=this.actions.iterator();
		while(cit.hasNext()){
			if(cit.next().getAction().equals(action)){
				return true;
			}
		}
		return false;
	}

}
