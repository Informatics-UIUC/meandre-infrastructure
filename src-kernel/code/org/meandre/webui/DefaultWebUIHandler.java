package org.meandre.webui;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

/** This class implements a default WebUI handler instanciated for each
 * WebUI and run when no other handlers are pressent.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class DefaultWebUIHandler extends AbstractHandler {
	
	/** The parent of this handler */
	private WebUI webUIParent = null;

	/** Creates the default WebUI handler.
	 * 
	 */
	public DefaultWebUIHandler ( WebUI webUIParent ) {
		this.webUIParent = webUIParent;
	}
	
	/** Implements the default, no web ui available, response.
	 * 
	 * @param target The target path
	 * @param request The request object
	 * @param response The response object
	 * @param dispath The dispatch flag
	 * @throws IOException An IO exception arised when processing the request
	 * @throws ServletException The servlet could not complete the request
	 */
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) 
	throws IOException, ServletException {
		Request base_request = (request instanceof Request) ? 
				                  (Request) request : 
				                  HttpConnection.getCurrentConnection().getRequest();
		
        if (response.isCommitted() || base_request.isHandled())
		      return;
		base_request.setHandled(true);

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(
				"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n\n"+
				"<head>\n"+
				"<title>Meandre Flow &raquo; Excecution &raquo; WebUI &raquo; "+webUIParent.getFlowExecutionUniqueID()+" </title>\n"+
				"\t<meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\" />\n"+
				"\t<meta http-equiv=\"content-language\" content=\"EN\" />\n"+
				"\t<meta name=\"ROBOTS\" content=\"NOINDEX,NOFOLLOW\"/>\n"+
				"\t<meta name=\"description\" content=\"Meandre Flow\"/>\n"+
				"\t<meta http-equiv=\"refresh\" content=\"2\" />\n"+
				"</head>\n"+
				"<body>\n" +
				"<div id=\"menu\"> \n" +
				"<h1>Meandre</h1>\n" +
				"<div id=\"main\">\n"+
				"No GUI component available at this point of execution.<br/>"+
				new Date()+
				"</div>\n"+
				"</div>\n"+
				"</body>\n"+
				"</html>"
		);
	
	}
}