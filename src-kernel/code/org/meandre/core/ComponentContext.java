package org.meandre.core;

import javax.servlet.http.HttpServletRequest;

import org.meandre.plugins.MeandrePlugin;

/** The component context contains methods to access the context environment
 * during the execution of a component.
 *
 * @author Xavier Llor&agrave;
 *
 */
public interface ComponentContext extends ComponentContextProperties {

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

	/** Request the abortion of the flow.
	 *
	 */
	public void requestFlowAbortion();

	/** Returns true if the flow has started a termination request.
	 *
	 * @return True if the flow is aborting
	 */
	public boolean isFlowAborting();

	/** Returns the MeandrePlugin for a particular id.
	 *
	 */
	@Override
    public MeandrePlugin getPlugin(String id);

	/** Given a request it returns the proper base URL to use.
	 *
	 * @param request The request received
	 * @return The initial path to use
	 * @throws ComponentContextException The URL could not be generated
	 */
	public String getInitialURLPath ( HttpServletRequest request ) throws ComponentContextException;
}
