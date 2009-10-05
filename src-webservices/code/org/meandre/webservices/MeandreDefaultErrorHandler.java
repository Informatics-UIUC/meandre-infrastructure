package org.meandre.webservices;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.utils.NetworkTools;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.util.StringUtil;

/** The default error handler to provide nicer outputs.
 * 
 * @author xavier 
 *
 */
public class MeandreDefaultErrorHandler extends ErrorHandler
{
	/** The core configuration information. */
	protected CoreConfiguration cnf;
	
	/** The base server port */
	private int port;
	
	/** The host name */
	private String host;

	/** Creates a new error handler for the given configuration object.
	 * 
	 * @param cnf The core configuration
	 */
	public MeandreDefaultErrorHandler ( CoreConfiguration cnf ) {
		super();
		this.cnf  = cnf;
		this.host = NetworkTools.getLocalHostName();
		this.port = cnf.getBasePort();
	}

	protected void writeErrorPageHead(HttpServletRequest request, Writer writer, int code, String message)
	throws IOException
	{
		writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n");
		writer.write("<title>Meandre Server: Error ");
		writer.write(Integer.toString(code));
		writer.write(' ');
		if (message!=null)
			writer.write(deScript(message));
		writer.write("</title>\n");  
		writer.write("<style type=\"text/css\">\n");
		writer.write(     "\timg {" );
		writer.write(    "   border: 0px none white; " );
		writer.write(    "   float: left; " );
		writer.write(    "\t}\n" );
		writer.write(    "\thr { " );
		writer.write(    "\t   height: 1px; " );
		writer.write(    "\t   background: orange; " );
		writer.write(    "\t} " );
		writer.write(    "\tbody { " );
		writer.write(    "\t   color: #444; " );
		writer.write(    "\t   background: white; " );
		writer.write(    "\t   font-family: Verdana, Arial, Helvetica ; " );
		writer.write(    "\t   font-size:11px; " );
		writer.write(    "\t}\n" );
		writer.write(   "\t#main {" );
		writer.write(   "\t   margin:200px auto 200px auto;");
		writer.write(   "}\n");
		writer.write(    "</style>\n");
	}

	/** Injects the logo on the error page message.
	 * 
	 * @param request The request object
	 * @param writer The writer object
	 * @param code The error code
	 * @param message The error message
	 * @string uri The error URI
	 * @string shwoStacks Should the stack trace be shown?
	 */
	protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks)
	throws IOException
	{
		String uri= request.getRequestURI();
		String user = request.getRemoteUser();
		if ( user==null ) user = "anonymous";
		
		// The fancy header
		writer.write("<img style='width: 190px; border: 0px none white;' src='" + cnf.getAppContext()+"/public/resources/system/logo-meandre.gif' />");
		writer.write("<p style='text-align:right; float=right;'><strong>"+new Date()+"</strong>. User <strong>"+user+"</strong> on host <strong>"+host+"</strong> at port <strong>"+port+"</strong>.</p>");
		writer.write("<br/><hr />");			
		
		// The message
		writeErrorPageMessage(request,writer,code,message,uri);
		
		// Wrap the stack trace
		if (showStacks) {
			writer.write("<div style='border: 1px; border-color: gray;'>");
			writeErrorPageStacks(request,writer);
			writer.write("</div>");
		}
		// The footer
		writer.write("<br/><hr /><small>Follow this link to reach the <a href='" + cnf.getAppContext()+"/public/services/ping.html' />Meandre infrastructure admin interface</a></small> ");
		writer.write("<i style='float:right;'><small>Powered by <a href='http://seasr.org/meandre'>Meandre Infrastructure</a> and <a href='http://mortbay.org/jetty/'>Jetty://</a></small></i>");
		for (int i= 0; i < 20; i++)
			writer.write("<br/>                                                \n");
	}

	/** Adds proper recoding for html funky codes :D
	 * 
	 * @param string The string to recode
	 * @return The recoded string
	 */
	protected String deScript(String string)
	{
		if (string==null)
			return null;
		string=StringUtil.replace(string, "&", "&amp;");
		string=StringUtil.replace(string, "<", "&lt;");
		string=StringUtil.replace(string, ">", "&gt;");
		return string;
	}

} 


