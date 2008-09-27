package org.meandre.webservices.tools.tests.simpleservlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.meandre.webservices.tools.ServletConfigurableDispatcher;

/** This servlet provides a simple test instance to test the dispatching capabilities.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class SimpleServlet extends ServletConfigurableDispatcher {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;
	

	/** Initialize the servlet. Creates an instance of the python interpreter.
	 * 
	 */
	public void init() throws ServletException {
		super.init();
		process(SimpleServlet.class.getResourceAsStream(
				SimpleServlet.class.getSimpleName()+".py"
			));
	}
	 
	/** Initialize the servlet with the given servlet configuration object.
	 * Creates and instance of the python interpreter.
	 * 
	 * @param config The servlet configuration object to use
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		process(SimpleServlet.class.getResourceAsStream(
				SimpleServlet.class.getSimpleName()+".py"
			));
	}
}
