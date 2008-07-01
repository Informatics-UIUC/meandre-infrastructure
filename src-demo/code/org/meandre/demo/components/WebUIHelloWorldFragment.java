package org.meandre.demo.components;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

/** A demo of a web UI callback.
 *
 * @author Xavier llor&agrave;
 *
 */
public class WebUIHelloWorldFragment implements ExecutableComponent, WebUIFragmentCallback {

	/** The blocking semaphore */
	private Semaphore sem = new Semaphore(1,true);

	/** The message to print */
	private String sMsg = null;

	/** The instance ID */
	private String sInstanceID = null;

	/** This method gets call when a request with no parameters is made to a
	 * component WebUI fragment.
	 *
	 * @param response The response object
	 * @throws WebUIException Some problem encountered during execution and something went wrong
	 */
	public void emptyRequest(HttpServletResponse response)
	throws WebUIException {
		try {
			response.getWriter().println(getViz());
		} catch (IOException e) {
			throw new WebUIException(e);
		}
	}

	/** A simple message.
	 *
	 * @return The HTML containing the page
	 */
	private String getViz() {

		StringBuffer sb = new StringBuffer("<center><strong><em>"+sMsg+"</em></strong></center>\n");
		
		sb.append("<a href=\"/"+sInstanceID +"?done=true ");
		sb.append("title=\"Done with WebUI fragment\" ");
		sb.append("tooltip=\"Done with WebUI fragment\" >");
		sb.append("Done with WebUI fragment</a>");
		
		return sb.toString();
	}

	/** This method gets called when a call with parameters is done to a given component
	 * webUI fragment
	 *
	 * @param target The target path
	 * @param request The request object
	 * @param response The response object
	 * @throws WebUIException A problem occurred during the call back
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response)
	throws WebUIException {
		String sDone = request.getParameter("done");
		if ( sDone!=null ) {
			sem.release();
		}
		else
			emptyRequest(response);
	}

	/** This method is called when the Menadre Flow execution is completed.
	 *
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	 * @param ccp The properties associated to a component context
	 */
	public void dispose ( ComponentContextProperties ccp ) 
	throws ComponentExecutionException, ComponentContextException {

	}

	/** When ready for execution.
	 *
	 * @param cc The component context
	 * @throws ComponentExecutionException An exeception occurred during execution
	 * @throws ComponentContextException Illigal access to context
	 */
	public void execute(ComponentContext cc) throws ComponentExecutionException, ComponentContextException {
		try {
			
			sMsg  = cc.getDataComponentFromInput("message").toString();
			sInstanceID = cc.getExecutionInstanceID();
			
			sem.acquire();
			cc.startWebUIFragment(this);
			sem.acquire();
			sem.release();
			
			System.out.println(">>>Done");
			cc.stopWebUIFragment(this);
		}
		catch ( Exception e ) {
			throw new ComponentExecutionException(e);
		}

	}

	/** This method is invoked when the Meandre Flow is being prepared for 
	 * getting run.
	 *
	 * @param ccp The properties associated to a component context
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	 */
	public void initialize ( ComponentContextProperties ccp ) 
	throws ComponentExecutionException, ComponentContextException {

	}

}
