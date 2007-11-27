package org.meandre.core;

import java.util.logging.Logger;

import org.meandre.webui.WebUIFragmentCallback;

/** The component context contains methods to access the context environment
 * of a component.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public interface ComponentContext {

	/** The name of the available inputs.
	 * 
	 * @return The array containing the names
	 */
	public String [] getInputNames ();
	
	/** The name of the available outputs.
	 * 
	 * @return The array containing the names
	 */
	public String [] getOutputNames ();
	
	/** Returns the current data component on the given active buffer.
	 * 
	 * @return The input name
	 * @throws ComponentContextException A violation of the component context was detected
	 */
	public Object getDataComponentFromInput ( String sInputBuffer ) throws ComponentContextException;
	
	/** Push an object to the given named output.
	 * 
	 * @param sOutputBuffer The name of the output
	 * @param obj The data component to push
	 * @throws ComponentContextException Violation of the component context detected
	 */
	public void pushDataComponentToOutput ( String sOutputBuffer, Object obj ) throws ComponentContextException;
	
	/** Checks if a given input is available.
	 * 
	 * @return The name of the input
	 * @throws ComponentContextException A violation of the component context was detected
	 */
	public boolean isInputAvailable ( String sInputBuffer ) throws ComponentContextException;
	

	/** Returns the list of property names.
	 * 
	  * @return The array of property names
	 */
	public String[] getPropertyNames( );
	
	/** Check a given component property value. If the property does not exist
	 * the call returns null.
	 * 
	 * @param sKey The property key 
	 * @return The property value (null if property does not exist)
	 */
	public String getProperty ( String sKey );
	
	/** Starts the web-based user interface given the proper implementation of the
	 * webui callback to deal with user action on the client.
	 * 
	 * @param wuiCall The webui call back object
	 */
	public void startWebUIFragment ( WebUIFragmentCallback wuiCall ) ;
	
	/** Stops the web-based user interface.
	 * 
	 * @param wuiCall The webui call back object
	 */
	public void stopWebUIFragment (WebUIFragmentCallback wuiCall);

	/** Stops all the web-based user interface created by this module.
	 * 
	 */
	public void stopAllWebUIFragments ();
	
	/** Returns the logging facility.
	 * 
	 * @return The logger object
	 */
	public Logger getLogger();

	/** Returns the unique ID of the executable instance for the current flow.
	 * 
	 * @return The unique execution instance ID
	 */
	public String getExecutionInstanceID ();
}
