package org.meandre.webui;

import java.io.File;

import javax.servlet.Servlet;

import org.meandre.core.store.Store;
import org.meandre.plugins.proxy.HttpProxyServlet;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/** This class implements the basic machinery of a flow web-based UI.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class WebUI {

	/** The flow execution unique ID */
	private String sFlowUniqueID = null;

	/** The port assigned to the WebUI */
	private int iPort = -1;

	/** The embeded jetty server */
	private Server server = null;

	/** The dispatcher for this WebUI */
	private WebUIDispatcher webUIDispatcher = null;

	/** Creates a WebUI for the given flow on the specified port.
	 *
	 * @param flowUniqueID Unique flow execution ID
	 * @param iPort The execution port
	 * @throws WebUIException The server could not be started
	 */
	public WebUI(String flowUniqueID, int iPort) throws WebUIException {
		// Storing config values
		this.sFlowUniqueID = flowUniqueID;
		this.iPort = iPort;
		// Creating the server and the connector
		this.server = new Server();
		Connector connector = new SocketConnector();
		connector.setPort(this.iPort);
		this.server.setConnectors(new Connector[] { connector });


		// Add the default WebUI dispatcher handler
		webUIDispatcher = new WebUIDispatcher(this);
		this.server.addHandler(webUIDispatcher);
		try {
			this.server.start();
		} catch (Exception e) {
			throw new WebUIException(e);
		}
	}

	/** Disposes and shuts down a the WebUI.
	 *
	 * @throws Exception The server could not be stoped
	 */
	public void shutdown() throws Exception {
		this.server.stop();
		this.server.getGracefulShutdown();
	}

	/** Returns the flow execution unique ID.
	 *
	 * @return The flow execution unique ID
	 */
	public String getFlowExecutionUniqueID () {
		return sFlowUniqueID;
	}

	/** Return the port where the  webUI is running.
	 *
	 * @return The port
	 */
	public int getPort () {
		return iPort;
	}

	/** Adds a web ui fragment to the current web ui.
	 *
	 * @param wuif The fragment to add
	 */
	public void addFragment( WebUIFragment wuif ) {
		this.webUIDispatcher.add(wuif);
	}

	/** Removes a web ui fragment form the web ui.
	 *
	 * @param wuif The fragment to remove
	 */
	public void removeFragment ( WebUIFragment wuif ) {
		this.webUIDispatcher.remove(wuif);
	}
}
