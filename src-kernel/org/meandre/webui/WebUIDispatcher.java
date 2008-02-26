package org.meandre.webui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

/** This class dispatches the webui calls.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WebUIDispatcher extends AbstractHandler {

	/** The parent of this handler */
	@SuppressWarnings("unused")
	private WebUI webUIParent = null;

	/** The list of attached UI handlers */
	protected List<WebUIFragment> lstHandlers = null;

	/** The default webUI handler */
	private DefaultWebUIHandler hdlDefault = null;

	/**
	 * Creates the default WebUI handler.
	 * 
	 */
	public WebUIDispatcher(WebUI webUIParent) {
		// Initialize
		this.webUIParent = webUIParent;
		this.lstHandlers = new LinkedList<WebUIFragment>();
		// Create the default handler
		this.hdlDefault = new DefaultWebUIHandler(webUIParent);
	}

	/**
	 * Dispatches web requests for the given targeted to the parent WebUI. It is
	 * important to keep in mind that fragments are identified by their instance
	 * URI. This means that, for instance, allows data from form to be properly
	 * routed
	 * 
	 * @param target
	 *            The target path
	 * @param request
	 *            The request object
	 * @param response
	 *            The response object
	 * @param dispath
	 *            The dispatch flag
	 * @throws IOException
	 *             An IO exception arised when processing the request
	 * @throws ServletException
	 *             The servlet could not complete the request
	 */
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {
		if (lstHandlers.size() > 0) {
			// Seting the request and response status
			Request base_request = (request instanceof Request) ? (Request) request
					: HttpConnection.getCurrentConnection().getRequest();
			base_request.setHandled(true);
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");
			
			response.getWriter().println(getHeader());
			
			boolean bHasParams = !request.getParameterMap().isEmpty();

			int iSize = lstHandlers.size();
			int iCnt  = 0;
			for ( ; iCnt<iSize ; iCnt++ ) {
				WebUIFragment wuif = lstHandlers.get(iCnt);
				try {
					// Check if any paramater has been passed
					if (bHasParams) {
						// Only pass them them to the target
						if (target.startsWith("/" + wuif.getFragmentID()))
							wuif.handle(request, response);
						else
							wuif.emptyRequest(response);
					} else {
						// Empty call updated all fragments
						wuif.emptyRequest(response);
					}
					// This implementations avoid concurrent access to the
					// list.
					if ( iSize != lstHandlers.size() ) {
						iSize--;
						iCnt--;
					}
						
				} catch (WebUIException e) {
					throw new ServletException(e);
				}
			}
			response.getWriter().println(getFooter());
			
		} else {
			hdlDefault.handle(target, request, response, dispatch);
		}
	}

	/**
	 * Returns the header of the webui page.
	 * 
	 * @return The header
	 */
	protected String getHeader() {
		return "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
				+ "<head>"
				+ "<title>Meandre Flow &raquo; Home &raquo; Run </title>"
				+ "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\" />"
				+ "<meta http-equiv=\"content-language\" content=\"EN\" />"
				+ "<meta name=\"ROBOTS\" content=\"NOINDEX,NOFOLLOW\"/>"
				+ "<meta name=\"description\" content=\"Meandre Flow Execution\"/>"
				+ "</head>" + "<body>";
	}

	/** Returns the footer of the webui page.
	 * 
	 * @return The footer
	 */ 
	protected String getFooter () {
		return "</body></html>\n";
	}
	/**
	 * Add a fragment to the list of dispatcheable fragments.
	 * 
	 * @param wuif
	 *            The fragment to add
	 */
	public void add(WebUIFragment wuif) {
		lstHandlers.add(wuif);
	}

	/**
	 * Remove a fragment from the list of dispatcheable fragments.
	 * 
	 * @param wuif
	 *            The fragment to remove
	 */
	public void remove(WebUIFragment wuif) {
		lstHandlers.remove(wuif);
	}

}
