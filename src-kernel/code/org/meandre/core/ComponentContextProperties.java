package org.meandre.core;

import java.io.PrintStream;
import java.net.URL;
import java.util.logging.Logger;

import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.plugins.MeandrePlugin;
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

	/** Gets the webUI URL.
	 *
	 * @param bName True if the url needs to be build using the name.
	 *              False build the URL using the IP address.
	 * @return The webUI URL
	 * @throws ComponentContextException Problem recovering the IP
	 *
	 *
	 */
	public URL getWebUIUrl ( boolean bName ) throws ComponentContextException;

	/** Gets the proxied webUI URL.
	 *
	 * @param bName True if the url needs to be build using the name.
	 *              False build the URL using the IP address.
	 * @return The proxy webUI URL
	 * @throws ComponentContextException Problem recovering the IP
	 *
	 *
	 */
	public URL getProxyWebUIUrl ( boolean bName ) throws ComponentContextException;

	/** Returns the unique ID of the executable instance for the current flow.
	 *
	 * @return The unique execution instance ID
	 */

	public String getExecutionInstanceID ();

	/** Returns the unique ID of the running flow.
	 *
	 * @return The unique ID of the running flow
	 */
	public String getFlowExecutionInstanceID();

	/**Return the flow ID of the flow
	 *
	 * @return The flow ID being run
	 */
	public String getFlowID();

	/** Returns the plugin
	 *
	 * @param id The plugin id
	 * @return The meandre plugin
	 */
	public MeandrePlugin getPlugin(String id);

	/** Returns the output console for the flow.
	 *
	 * @return The output console
	 */
	public PrintStream getOutputConsole();


	/** Returns the path to the public resources directory.
	 *
	 * @return Path to public resources directory.
	 */
	public String getPublicResourcesDirectory ();


	/** Returns the path to the run directory.
	 *
	 * @return Path to public resources directory.
	 */
	public String getRunDirectory ();

    /**
     * Returns the names of the input ports that have inbound connections
     *
     * @return The names of the input ports that have inbound connections
     */
    public String[] getConnectedInputs();

    /**
     * Returns the names of the output ports that have outbound connections
     *
     * @return The names of the output ports that have outbound connections
     */
    public String[] getConnectedOutputs();

    /**
     * Returns the Firing Policy of the component
     *
     * @return The Firing Policy of the component
     */
    public FiringPolicy getFiringPolicy();
}
