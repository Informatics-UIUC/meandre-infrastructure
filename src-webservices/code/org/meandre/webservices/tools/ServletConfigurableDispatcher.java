package org.meandre.webservices.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.webservices.logger.WSLoggerFactory;
import org.python.util.PythonInterpreter;

/** This class provides a unified base servlet dispatcher shared by all the
 * servlets. It takes care of proper routing requests to methods and return
 * formats.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class ServletConfigurableDispatcher extends HttpServlet {

	/** A default serail ID */
	private static final long serialVersionUID = 1L;

	/** Initialize the Python home dir properly */
	static {
		File fp = new File((new File(".")).toString()+File.separator+"run"+File.separator+"Jython");
		fp.mkdir();
		Properties props = new Properties();
		props.setProperty("python.path", fp.toString());
		props.setProperty("python.home", fp.toString());
		PythonInterpreter.initialize(System.getProperties(), props,
		                             new String[] {""});
	}
	
	/** The Jython interpreter that will use this executable component. */
	protected PythonInterpreter pi = null;

	/** Initialize the base dispatcher
	 *
	 */
	public ServletConfigurableDispatcher ( ) {
		pi = null;
	}
	
	/** Initialize the servlet. Creates an instance of the python interpreter.
	 * 
	 */
	public void init() throws ServletException {
		super.init();
		initPythonInterpreter();
	}
    
	/** Initialize the servlet with the given servlet configuration object.
	 * Creates and instance of the python interpreter.
	 * 
	 * @param config The servlet configuration object to use
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		initPythonInterpreter();
	}
	
	private void initPythonInterpreter () {
		// Setup the interpreter
		pi = new PythonInterpreter();
		// Run the initialization script
		String pyResourceName = 
		    ServletConfigurableDispatcher.class.getSimpleName()+".py";
		InputStream pyResourceStream = 
		    ServletConfigurableDispatcher.class.getResourceAsStream(pyResourceName);
		//check if we could find the initialization python script using the classloader
		if(pyResourceStream == null){
		    throw new NullPointerException(
		            "Could not find the init script \'" + pyResourceName + 
		            "\' via the classloader. Make sure it is in your classpath.");
		}
		//run the script
		process(pyResourceStream);
		// Run the dispatcher definition
		pyResourceName = this.getClass().getSimpleName()+".py";
		pyResourceStream = this.getClass().getResourceAsStream(pyResourceName);
        //check if we could find the dispatcher python script using the classloader
        if(pyResourceStream == null){
            throw new NullPointerException(
                    "Could not find the init script \'" + pyResourceName + 
                    "\' via the classloader. Make sure it is in your classpath.");
        }		
		process(pyResourceStream);
	}
	
	/** Destroys the servlet and releases the Python interpreter after cleaning it up.
	 * 
	 */
    public void destroy() {
		pi.cleanup();
		pi = null;
		super.destroy();
	}

	/** Process the given script on an already prepared interpreter.
	 *
	 * @param sScript The script to process
	 */
	protected void process ( String sScript ) {
		pi.exec(sScript);
	}
	

	/** Process the given script on an already prepared interpreter.
	 *
	 * @param is The input stream that contains the script
	 */
	protected void process ( InputStream is ) {
		pi.execfile(is);
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
		long lctm = System.currentTimeMillis();
		try {
			// Pull the method and format
			String[] saParts = new URL(req.getRequestURL().toString()).getPath().split("\\.");
			String[] saPath = saParts[0].split("/");
			String sTarget = saPath[saPath.length-1];
			String sExtension = (saParts.length==2 )?saParts[1]:"";
	    	
			// Create the names
			String sReq = "req"+lctm;
			String sRes = "resp"+lctm;
			
			// Set the objects
			pi.set(sReq,req);
			pi.set(sRes,resp);
			
			// Dispatch the request
			pi.exec("dispatch('"+method+"',"+sReq+","+sRes+",'"+sTarget+"','"+sExtension+"')");
			
			// Delete the objects
			pi.exec("del("+sReq+")");
			pi.exec("del("+sRes+")");
			
		} catch (Exception e) {
			Logger log = WSLoggerFactory.getWSLogger();
			log.warning("Could not process request "+
					    req.getRequestURL().toString()+
					    "\n"+e.toString());
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (IOException e1) {
				log.warning("Failed to response");
			}
		}
   		
	}

}
