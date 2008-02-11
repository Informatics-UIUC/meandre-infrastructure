package org.meandre.core;

import java.net.URL;

import org.meandre.webui.WebUIFragmentCallback;

/** The component context contains methods to access the context environment
 * during the execution of a component.
 *
 * @author Xavier Llor&agrave;
 * @last-modified: Amit Kumar added the getFlowExecutionInstanceID function defn
 *
 */
public interface ComponentContext 
extends ComponentContextProperties {

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
	/** Return the unique ID of the executable flow.
	 *
	 * @return
	 */
	public String getFlowExecutionInstanceID();
}
