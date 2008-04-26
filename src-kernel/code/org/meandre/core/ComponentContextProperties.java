package org.meandre.core;

import java.net.URL;
import java.util.logging.Logger;

import org.meandre.webui.WebUIFragmentCallback;

/** The component context contains methods to access the context environment
 * properties of a component.
 *
 * @author Xavier Llor&agrave;
 *
 */
public interface ComponentContextProperties {

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


	/** Returns the logging facility.
	 *
	 * @return The logger object
	 */
	public Logger getLogger();


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

	/** Get the webUI URL.
	 *
	 * @param bName True if the url needs to be build using the name.
	 *              False build the URL using the IP address.
	 * @return The webUI URL
	 * @throws ComponentContextException Problem recovering the IP
	 *
	 *
	 */
	public URL getWebUIUrl ( boolean bName ) throws ComponentContextException;

	/** Returns the unique ID of the executable instance for the current flow.
	 *
	 * @return The unique execution instance ID
	 */
	
	public String getExecutionInstanceID ();
	
	/** Return the unique ID of the running flow.
	 *
	 * @return The unique ID of the running flow
	 */
	public String getFlowExecutionInstanceID();
	
	/**Return the flow ID of the flow
	 *
	 * @return The flow ID being run
	 */
	public String getFlowID();
}
