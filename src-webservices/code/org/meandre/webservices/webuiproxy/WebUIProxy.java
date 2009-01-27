/**
 * 
 */
package org.meandre.webservices.webuiproxy;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.webservices.logger.WSLoggerFactory;
import org.meandre.webui.WebUI;
import org.meandre.webui.WebUIFactory;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;

/** Implements a handlers that is able to proxy the request to a webui
 * starting from the meandre server.
 * 
 * @author Xavier Llor&agrarve;
 *
 */
public class WebUIProxy implements Handler {

	/** The parent server */
	private Server server;
	
	/** The logger to use */
	private final static Logger log = WSLoggerFactory.getWSLogger();

	/** Create a new webui proxy instance.
	 * 
	 */
	public WebUIProxy ( ) {
		this.server = null;
	}
	
	/** Destroys the proxy */
	public void destroy() {
		
	}

	/** Returns the base server for this proxy.
	 * 
	 * @return The parent server
	 */
	public Server getServer() {
		return server;
	}

	/** The main handling object.
	 * 
	 * @param sRequestedURL The requested URL
	 * @param request The resquest object
	 * @param response The response object
	 * @param iDispatch The dispatch status of the request
	 */
	public void handle(String sRequestedURL, HttpServletRequest request,
			HttpServletResponse response, int iDispatch) throws IOException,
			ServletException {
		
		Request base_request = (request instanceof Request) ? 
				               (Request)request :
				               HttpConnection.getCurrentConnection().getRequest();

        if ( sRequestedURL.startsWith("/webui/") ) {
        	String sUser = request.getRemoteUser();
        	sUser = (sUser==null) ? "anonymous" : sUser;
        	log.info(request.getRemoteAddr()+":"+request.getRemotePort()+"/"+sUser+" --> WebUI proxy --> "+sRequestedURL);
            String [] sArgs = sRequestedURL.split("/");
   	   		if ( sArgs.length<3 )  {
				// Missing port
   	   			base_request.setHandled(true);
   	   			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else {
				int iPort = Integer.parseInt(sArgs[2]);
				WebUI webui = WebUIFactory.getExistingWebUIOnPort(iPort);
				if ( webui==null ) {
					// Non running port
					base_request.setHandled(true);
		   			response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
				else {
					// Forward the request
					String newTarget = sRequestedURL.replaceFirst("^/webui/"+iPort, "");
					if ( newTarget.equals("") ) newTarget = "/";
					webui.getWebUIDispatcher().handle(
							newTarget, 
							request, 
							response, 
							iDispatch
						);
				}
			}
		}

	}

	/** Sets the parent server.
	 * 
	 * @param server The parent server
	 */
	public void setServer(Server server) {
		this.server = server;
	}

	/** Returns true if the proxy failed
	 * 
	 * @return The failed status value
	 */
	public boolean isFailed() {
		return false;
	}

	/** Returns always true since the proxy is always running
	 * 
	 * @return The running status value
	 */
	public boolean isRunning() {
		return true;
	}

	/** Returns true since the proxy is always started
	 * 
	 * @return The started status value
	 */
	
	public boolean isStarted() {
		return true;
	}

	/** Returns false since the proxy has no starting overhead
	 * 
	 * @return The starting status value
	 */
	public boolean isStarting() {
		return false;
	}

	/** Returns false since the proxy is always running
	 * 
	 * @return The stopped status value
	 */
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	/** Returns false since the proxy has no stopping overhead
	 * 
	 * @return The stopping status value
	 */
	public boolean isStopping() {
		// TODO Auto-generated method stub
		return false;
	}

	/** Starts the proxy. Does actually nothing.
	 */
	public void start() throws Exception {
	}

	/** Stops the proxy. Does actually nothing.
	 */
	public void stop() throws Exception {
	}

}
