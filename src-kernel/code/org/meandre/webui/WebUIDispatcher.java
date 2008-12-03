package org.meandre.webui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.Probe;
import org.meandre.core.engine.ProbeException;
import org.meandre.core.engine.probes.StatisticsProbeImpl;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;

/** This class dispatches the webui calls.
 * 
 * @author Xavier Llor&agrave;
 * @modified June 19th 2008 -fix for multipart requests.
 *
 */
public class WebUIDispatcher extends AbstractHandler {

	/** The parent of this handler */
	private WebUI webUIParent = null;

	/** The list of attached UI handlers */
	protected List<WebUIFragment> lstHandlers = null;

	/** The default webUI handler */
	private DefaultWebUIHandler hdlDefault = null;

	/** The default resource handler */
	private ResourceHandler resource_handler;

	/**
	 * Creates the default WebUI handler.
	 * @param cnf The core configuration object
	 * 
	 */
	public WebUIDispatcher(WebUI webUIParent, CoreConfiguration cnf) {
		// Initialize
		this.webUIParent = webUIParent;
		this.lstHandlers = new LinkedList<WebUIFragment>();
		// Create the default handler
		this.hdlDefault = new DefaultWebUIHandler(webUIParent);
		
		// The resouce file server
		File file = new File(cnf.getPublicResourcesDirectory());
		resource_handler = new ResourceHandler();
		resource_handler.setCacheControl("no-cache");
		resource_handler.setResourceBase(file.getAbsolutePath());
		Context contextResources = new Context(webUIParent.server,"/public/resources",Context.NO_SESSIONS);
		contextResources.setHandler(resource_handler);
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
		
		
		if ( target.startsWith("/admin/") ) {
			processAdminRequest(target,request,response,dispatch);
		}
		else if ( target.startsWith("/public/resources") ) {
			resource_handler.handle(target, request, response, dispatch);
		}
		else {
			if (lstHandlers.size() > 0) {
				Request base_request = (request instanceof Request) ? 
		                (Request) request : 
		                HttpConnection.getCurrentConnection().getRequest();

				if (response.isCommitted() || base_request.isHandled())
					return;
				base_request.setHandled(true);

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("text/html");
				
				//response.getWriter().println(getHeader());
				
				boolean bHasParams = !request.getParameterMap().isEmpty();
				String contentType = request.getContentType();
				boolean isMultiPart = Boolean.FALSE;
				if ( (contentType!=null && contentType.startsWith("multipart/form-data")) || 
					  request.getMethod().equalsIgnoreCase("post") || 
					  request.getMethod().equalsIgnoreCase("put")  ){
					isMultiPart = Boolean.TRUE;
				}
				int iSize = lstHandlers.size();
				int iCnt  = 0;
				for ( ; iCnt<iSize ; iCnt++ ) {
					WebUIFragment wuif = lstHandlers.get(iCnt);
					try {
						// Check if any paramater has been passed
						if (bHasParams || isMultiPart) {
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
				//response.getWriter().println(getFooter());
				
			} else {
				hdlDefault.handle(target, request, response, dispatch);
			}
		}
	}

	/** Process an admin requrest.
	 * 
	 * @param target The target
	 * @param request The requrest
	 * @param response The response
	 * @param dispatch The dispatch flag
	 * @throws IOException An exception on the response happened
	 */
	protected void processAdminRequest(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException {
		// Seting the request and response status
		Request base_request = (request instanceof Request) ? (Request) request
				: HttpConnection.getCurrentConnection().getRequest();
		base_request.setHandled(true);
		
		String [] saParts = new URL(request.getRequestURL().toString()).getPath().split("\\.");
   		String sTarget = saParts[0];
		String sExtension = "";
		PrintWriter pw = response.getWriter();	

    	if ( saParts.length==2 ) {
    		sExtension = saParts[1];
    	}
    	
    	if ( sTarget.startsWith("/admin/abort") ) {
    		if ( sExtension.equals("txt") ) {
    			this.webUIParent.getMrProper().abort();
    			response.setStatus(HttpServletResponse.SC_OK);
    			response.setContentType("text/plain");
    			response.getWriter().print("Abort request dispatched...");	
    		}
    		else {
    			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		}
    	}
    	else if ( sTarget.startsWith("/admin/statistics") ) {
    		Probe [] pa = this.webUIParent.getMrProbe().getProbes();
			for ( Probe p:pa ) {
				if ( p instanceof StatisticsProbeImpl ) {
					JSONObject jsonStats = null;
					try {
						jsonStats = ((StatisticsProbeImpl)p).getSerializedStatistics();
					} catch (ProbeException e) {
						throw new IOException(e.toString());
					}
					
		    		if ( sExtension.equals("txt")) {
		    			response.setStatus(HttpServletResponse.SC_OK);
		    			response.setContentType("text/plain");
		    			try {
			    			pw.println("----------------------------------------------------------------------------");
							pw.println();
							pw.println("Flow execution statistics");
							pw.println();
							pw.println("Flow unique execution ID :"+jsonStats.get("flow_unique_id"));
							pw.println("Flow state               :"+jsonStats.get("flow_state"));
							pw.println("Started at               :"+jsonStats.get("started_at"));
							pw.println("Last update              :"+jsonStats.get("latest_probe_at"));
							pw.println("Total run time (ms)      :"+jsonStats.get("runtime"));
							pw.println();
							pw.flush();
							
							JSONArray jaEXIS = (JSONArray) jsonStats.get("executable_components_statistics");
							for ( int i=0,iMax=jaEXIS.length() ; i<iMax ; i++ ) {
								JSONObject joEXIS = (JSONObject) jaEXIS.get(i);
								pw.println("\tExecutable components instance ID          :"+joEXIS.get("executable_component_instance_id"));
								pw.println("\tExecutable components state                :"+joEXIS.get("executable_component_state"));
								pw.println("\tTimes the executable components fired      :"+joEXIS.get("times_fired"));
								pw.println("\tAccumulated executable components run time :"+joEXIS.get("accumulated_runtime"));
								pw.println("\tPieces of data pulled                      :"+joEXIS.get("pieces_of_data_in"));
								pw.println("\tPieces of data pushed                      :"+joEXIS.get("pieces_of_data_out"));
								pw.println("\tNumber of properties read                  :"+joEXIS.get("number_of_read_properties"));
								pw.println();
							}
		    			}
		    			catch ( Exception e ) {
		    				throw new IOException(e.toString());
		    			}
						pw.flush();
		    			break;
		    		}
		    		else if ( sExtension.equals("json") ) {
		    			response.setStatus(HttpServletResponse.SC_OK);
		    			response.setContentType("text/plain");
		    			pw.print(jsonStats);
		    			break;
		    		}
		    		else if ( sExtension.equals("xml")) {
		    			response.setStatus(HttpServletResponse.SC_OK);
		    			response.setContentType("application/xml");
		    			try {
		    				response.getWriter().println(XML.toString(jsonStats,"flow_statistics"));
		    			} catch (JSONException e) {
		    				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    			}break;
		    		}
		    		else {
		    			response.setStatus(HttpServletResponse.SC_NOT_FOUND);	
		    			break;
		    		}
				}
			}
    	}
    	else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);	
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

	/**
	 * @return the lstHandlers
	 */
	public List<WebUIFragment> getLstHandlers() {
		return lstHandlers;
	}

}
