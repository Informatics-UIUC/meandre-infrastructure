package org.meandre.webui;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;
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

        DateTime start = new DateTime(this.startedAt);
        DateTime end = new DateTime(new Date());
        Period period = new Period(start, end, PeriodType.time().withMillisRemoved());

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n\n"+
                "<head>\n"+
                "<title>Meandre Flow &raquo; Excecution &raquo; WebUI &raquo; "+webUIParent.getFlowExecutionUniqueID()+" </title>\n"+
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\">\n"+
                "<meta http-equiv=\"content-language\" content=\"EN\">\n"+
                "<meta name=\"ROBOTS\" content=\"NOINDEX,NOFOLLOW\">\n"+
                "<meta http-equiv=\"CACHE-CONTROL\" content=\"NO-CACHE\">\n"+
                "<meta http-equiv=\"Pragma\" content=\"no-cache\">\n" +
                "<meta name=\"description\" content=\"Meandre Flow\">\n"+
                "<meta http-equiv=\"refresh\" content=\"6\">\n"+
                "<style type=\"text/css\">\n"+
                "\timg {" +
                "   border: 0px none white; " +
                "   float: right; " +
                "\t}\n" +
                "\tbody { " +
                "\t   color: #777; " +
                "\t   background: white; " +
                "\t   font-family: Helvetica, Arial, Verdana; " +
                "\t   font-size:14px; " +
                "\t}\n" +
                "\t#main {" +
                "\t   margin:200px auto 200px auto;" +
                "}\n"+
                "</style>\n"+
                "</head>\n\n"+
                "<body>\n" +
                "\t<table width='100%' border='0' id='main'>\n" +
                "\t<tr><td>\n" +
                "\t\t<table border='0' align='center'>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td rowspan='2'><img src='" + cnf.getAppContext()+"/public/resources/system/wait.gif" + "' width='40' height='40' /></td>\n" +
                "\t\t\t\t<td>The flow is currently running. Please wait.</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>This page will be refreshed automatically when there is data to display.</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>&nbsp;</td>\n" +
                "\t\t\t\t<td>&nbsp;</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>&nbsp;</td>\n" +
                "\t\t\t\t<td>The flow has been running for " + period.toString(PeriodFormat.getDefault()) + "</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t</table>\n"+
                "\t</td><td align='right'>\n" +
                "\t\t<img src=\""+cnf.getAppContext()+"/public/resources/system/logo-meandre.gif\" />\n" +
                "\t</tr>\n" +
                "\t</table>\n" +
                "</body>\n"+
                "</html>"
        );

	}
}