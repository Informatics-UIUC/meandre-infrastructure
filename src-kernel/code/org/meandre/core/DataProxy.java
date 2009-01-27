package org.meandre.core;

import java.util.Hashtable;
import java.util.Set;

import org.meandre.core.engine.ActiveBuffer;

/** This class acts like a input/output proxy for a given component context.
 * It model the input and output active buffers and the data components flowing
 * through them
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class DataProxy {
	
	/** The name of the input active buffers */
	private String [] saInputActiveBufferNames = null;
	
	/** The available data components in the input active buffers */
	private Hashtable<String,Object> htInputDataComponents = null;
		
	/** Create a data proxy with the given input and output active buffers.
	 * 
	 * @param setInputs The name of the input active buffers
	 */
	public DataProxy ( Set<ActiveBuffer> setInputs ) {
		this.saInputActiveBufferNames = new String[setInputs.size()];
		
		int iCnt = 0;
		for ( ActiveBuffer abName:setInputs )
			this.saInputActiveBufferNames[iCnt++] = abName.getName();
		iCnt = 0;
		
		this.htInputDataComponents  = new Hashtable<String,Object> ();
	}
	
	/** Returns the names of the input active buffers.
	 * 
	 * @return The array with the name of the current active buffers.
	 */
	public String [] getActiveBufferInputName () {
		return saInputActiveBufferNames;
	}
		
	/** Remove the data component for the given input.
	 * 
	 * @param sName The input to clean
	 */
	public void cleanInput ( String sName ) {
		htInputDataComponents.remove(sName);
	}
	
	/** Remove all the data components form the input caches.
	 * 
	 */
	public void cleanAllInputs () {
		synchronized (htInputDataComponents ) {
			htInputDataComponents.clear();
		}
	}
		
	/** Sets the data component stored at the given input. 
	 * 
	 * @param sName The input name
	 * @param obj The object to set
	 */
	public void setInput ( String sName, Object obj ) {
		synchronized ( htInputDataComponents ) {
			htInputDataComponents.put(sName,obj);
		}
	}
	
	/** Gets the data component stored at the given input. Returns
	 * null if no data component is available.
	 * 
	 * @param sName The input name
	 * @return The available component.
	 */
	public Object getInput ( String sName ) {
		return htInputDataComponents.get(sName);
	}
	 	
}
