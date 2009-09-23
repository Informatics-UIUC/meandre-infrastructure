package org.meandre.webui;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.configuration.CoreConfiguration;
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
	private final WebUI webUIParent;

	/** The core configuration */
	private final CoreConfiguration cnf;

	/** The date the webUI was started */
	private final Date startedAt;
	private final long startedAtMS;

	/** Creates the default WebUI handler.
	 *
	 * @param webUIParent The parent webUI
	 * @param cnf The core configuration
	 * @param date The date when the flow was started
	 */
	public DefaultWebUIHandler ( WebUI webUIParent, CoreConfiguration cnf, Date date ) {
		this.webUIParent = webUIParent;
		this.cnf = cnf;
		this.startedAt = date;
		this.startedAtMS = this.startedAt.getTime();
	}

	/** Implements the default, no web ui available, response.
	 *
	 * @param target The target path
	 * @param request The request object
	 * @param response The response object
	 * @param dispatch The dispatch flag
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
				"\t<meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\">\n"+
				"\t<meta http-equiv=\"content-language\" content=\"EN\">\n"+
				"\t<meta name=\"ROBOTS\" content=\"NOINDEX,NOFOLLOW\">\n"+
				"\t<meta http-equiv=\"CACHE-CONTROL\" content=\"NO-CACHE\">\n"+
				"\t<meta http-equiv=\"Pragma\" content=\"no-cache\">\n" +
				"\t<meta name=\"description\" content=\"Meandre Flow\">\n"+
				"\t<meta http-equiv=\"refresh\" content=\"6\">\n"+
				"\t<style type=\"text/css\">\n"+
				"\timg {" +
				"	border: 0px none white; " +
				"   float: right; " +
				"\t}\n" +
				"\tbody { " +
				"\t   color: #777; " +
				"\t   background: white; " +
				"\t   font-family: Helvetica, Arial, Verdana; " +
				"\t   font-size:14px; " +
				"\t}\n" +
				"\t#main p {" +
				"\t   text-align:left; " +
				"\t   margin:200px auto 200px auto;" +
				"}"+
				"</style>"+
				"</head>\n"+
				"<body>\n" +
				"<div id=\"menu\"> \n" +
				"<img src=\""+cnf.getAppContext()+"/public/resources/system/logo-meandre.gif\" />\n" +
				"<div id=\"main\">\n"+
				"<p>The flow is currently running. Please wait for your results to be displayed here." +
				"<br/>No WebUI available at this point of execution.<br/>"+
				"Started: "+this.startedAt+"<br/>"+
				"Time elapsed: "+((new Date().getTime()-this.startedAtMS))/1000.0+" seconds </p>"+
				"</div>\n"+
				"</div>\n"+
				"</body>\n"+
				"</html>"
		);

	}
}