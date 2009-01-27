package org.meandre.webservices.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** This class provides a simple redirection
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSRedirectionServlet extends HttpServlet {

	/** A default serail ID */
	private static final long serialVersionUID = 1L;

	/** Initialize the Python home dir properly */
	private String sURLPath = "/public/services/ping.html";
	/** Initialize the base dispatcher
	 *
	 */
	public WSRedirectionServlet () {
	}
	
	/** Initialize the base dispatcher
	 *
	 * @param sURL The URL to redirect
	 */
	public WSRedirectionServlet ( String sURL) {
		sURLPath = sURL;
	}
		
	/** Response to a get request.
	 * 
	 * @param req The request object
	 * @param resp The response object
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)  {
		dispatch("GET",req,resp);  
	}
	
	/** Response to a post request.
	 * 
	 * @param req The request object
	 * @param resp The response object
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)  {
		dispatch("POST",req,resp);	
	}
		
	/** Response to a put request.
	 * 
	 * @param req The request object
	 * @param resp The response object
	 */    
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)  {
		dispatch("PUT",req,resp);
	}
    
	/** Response to a delete request.
	 * 
	 * @param req The request object
	 * @param resp The response object
	 */
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)  {
		dispatch("DELET",req,resp);
	}
	
	/** Response to a head request.
	 * 
	 * @param req The request object
	 * @param resp The response object
	 */
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)  {
    	dispatch("HEAD",req,resp);
	}
	
    /** Response to a options request.
	 * 
	 * @param req The request object
	 * @param resp The response object
	 */
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)  {
    	dispatch("OPTIONS",req,resp);
	}
	
    /** Response to a trace request.
	 * 
	 * @param req The request object
	 * @param resp The response object
	 */
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp)  {
    	dispatch("TRACE",req,resp);
	}
	
    /** Dispatch the request.
     * 
     * @param method The method of the request
     * @param req The request object
     * @param resp The response
     */
    private void dispatch(String method, HttpServletRequest req, HttpServletResponse resp) {
		try {
			resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			PrintWriter pw = resp.getWriter();
			pw.print(
					"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n\n"+
					"<head>\n"+
					"<title>Meandre Server</title>\n"+
					"\t<meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\" />\n"+
					"\t<meta http-equiv=\"content-language\" content=\"EN\" />\n"+
					"\t<meta name=\"ROBOTS\" content=\"NOINDEX,NOFOLLOW\"/>\n"+
					"\t<meta name=\"description\" content=\"Meandre Flow\"/>\n"+
					"\t<meta http-equiv=\"refresh\" content=\"6;URL="+sURLPath+"\" />\n"+
					"\t<style type=\"text/css\">\n"+
					"\timg {" +
					"	border: 0px none white; " +
					"   float: right; " +
					"\t}\n" +
					"\tbody { " +
					"\t   color: #888; " +
					"\t   background: white; " +
					"\t   font-family: Helvetica, Arial, Verdana; " +
					"\t   font-size:11px; " +
					"\t}\n" +
					"\t#main p {" +
					"\t   text-align:center; " +
					"\t   margin:200px auto 200px auto;" +
					"}"+
					"</style>"+
					"</head>\n"+
					"<body>\n" +
					"<div id=\"menu\"> \n" +
					"<img src=\"/public/resources/system/logo-meandre.gif\" />\n" +
					"<div id=\"main\">\n"+
					"<p>You will be redirected to the Meandre Server page. Otherwise just click <a href=\""+sURLPath+"\">here</a>.</br>"+
					new Date()+"</p>"+
					"</div>\n"+
					"</div>\n"+
					"</body>\n"+
					"</html>"	
				);
			
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
