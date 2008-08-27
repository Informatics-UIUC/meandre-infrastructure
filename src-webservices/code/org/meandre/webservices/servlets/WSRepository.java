package org.meandre.webservices.servlets;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.json.JSONException;
import org.json.XML;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.security.Role;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.security.User;
import org.meandre.core.store.Store;
import org.meandre.webservices.controllers.WSRepositoryLogic;
import org.meandre.webservices.logger.WSLoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/** A basic handler to display public Meandre information.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class WSRepository extends HttpServlet {

    /** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();

	/** The store to use */
	private Store store;

	/** The repository logic object. */
	private WSRepositoryLogic wsRepositoryLogic;
	
	/** Creates a repository servlet using the current store.
	 * 
	 * @param store The store to use
	 * @param cnf Teh core configuration
	 */
	public WSRepository(Store store, CoreConfiguration cnf) {
		this.store = store;
		this.wsRepositoryLogic = new WSRepositoryLogic(store,cnf);
	}

	/**
	 * Dispatches POST web requests for Meandre web services.
	 *
	 * @param sTarget
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    	String [] saParts = new URL(request.getRequestURL().toString()).getPath().split("\\.");
   		String sTarget = saParts[0];
		String sExtension = "";

    	if ( saParts.length==2 ) {
    		sExtension = saParts[1];
    	}

    	if ( sTarget.endsWith("/add") ) {
    		if (requestorHasRole(request, Role.FLOWS) &&
    		        requestorHasRole(request, Role.COMPONENTS)) {
                      addRepositoryAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/add_flow_descriptors") ) {
    		if (requestorHasRole(request, Role.FLOWS)) {
    			addFlowsAction(request, response, sTarget, sExtension);
       		}
       		else {
       			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
       		}
       	}
    	else {
    		//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
    	}
    }

	/**
	 * Dispatches GET web requests for Meandre web services.
	 *
	 * @param sTarget
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    	String [] saParts = new URL(request.getRequestURL().toString()).getPath().split("\\.");
   		String sTarget = saParts[0];
		String sExtension = "";

    	if ( saParts.length==2 ) {
    		sExtension = saParts[1];
    	}

    	if ( sTarget.endsWith("/dump") ) {
    		if (requestorHasRole(request, Role.REPOSITORY)) {
                dumpAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/regenerate") ) {
    		if (requestorHasRole(request, Role.REPOSITORY)) {
                regenerateAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/list_components") ) {
    		if (requestorHasRole(request, Role.COMPONENTS)) {
                listComponentsAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/list_flows") ) {
    		if (requestorHasRole(request, Role.FLOWS)) {
                listFlowsAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/tags") ) {
    		if (requestorHasRole(request, Role.FLOWS) &&
    		        requestorHasRole(request, Role.COMPONENTS)) {
                tagsAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/tags_components") ) {
    		if (requestorHasRole(request, Role.COMPONENTS)) {
                tagsComponentsAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/tags_flows") ) {
    		if (requestorHasRole(request, Role.FLOWS)) {
                tagsFlowsAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/components_by_tag") ) {
    		if (requestorHasRole(request, Role.COMPONENTS)) {
                componentsByTagAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/flows_by_tag") ) {
    		if (requestorHasRole(request, Role.FLOWS)) {
                flowsByTagAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/describe_component") ) {
    		if (requestorHasRole(request, Role.COMPONENTS)) {
                describeComponentAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/describe_all_components") ) {
    		if (requestorHasRole(request, Role.COMPONENTS)) {
                describeAllComponentsAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/describe_flow") ) {
    		if (requestorHasRole(request, Role.FLOWS)) {
                describeFlowAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/describe_all_flows") ) {
    		if (requestorHasRole(request, Role.FLOWS)) {
                describeAllFlowsAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/search_components") ) {
    		if (requestorHasRole(request, Role.COMPONENTS)) {
                searchComponentAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/search_flows") ) {
    		if (requestorHasRole(request, Role.FLOWS)) {
                searchFlowAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("/remove") ) {
    		if (requestorHasRole(request, Role.FLOWS) &&
    		        requestorHasRole(request, Role.COMPONENTS)) {
                   removeAction(request, response, sTarget, sExtension);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else {
    		//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
    	}


	}

    /** Remove the component/flow from the reprosity.
	 *
	 * @param request The request object
	 * @param response The response object
	 * @param sTarget The target string
	 * @param sExtension The extension string
	 * @throws IOException A problem arised
	 */
	private void removeAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException{

		String sURI = request.getParameter("uri");

		if ( sURI==null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.removeURIAsTxt(request.getRemoteUser(),sURI));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.removeURIAsJSON(request.getRemoteUser(),sURI),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of components
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.removeURIAsJSON(request.getRemoteUser(),sURI));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	/** Add a repository descriptor to the repository.
     *
     * @param request The request object
     * @param response The response object
     * @param sTarget The target string
     * @param sExtension The extension string
     * @throws IOException A problem arised
	 */
	private void addRepositoryAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException{

		try {
			boolean [] baDump = {false};
			Model modelFlow = wsRepositoryLogic.addToRepository(request,sExtension,baDump);

			if ( modelFlow == null ) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
			else if ( sExtension.endsWith("rdf") && baDump[0] ) {
				dumpModel(request,response,modelFlow,"RDF/XML-ABBREV");
			}
			else if ( sExtension.endsWith("ttl") && baDump[0] ) {
				dumpModel(request,response,modelFlow,"TTL");
			}
			else if ( sExtension.endsWith("nt") && baDump[0] ) {
				dumpModel(request,response,modelFlow,"N-TRIPLE");
			}
			else if ( baDump[0]) {
				//
				// Invalid request found
				//
				log.info("Uknown repository service requested "+sTarget);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (FileUploadException e) {
			throw new IOException(e.toString());
		}

	}

	/** Add a flow to the repository.
     *
     * @param request The request object
     * @param response The response object
     * @param sTarget The target string
     * @param sExtension The extension string
     * @throws IOException A problem arised
	 */
	private void addFlowsAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException{

		try {
			Model modelFlow = wsRepositoryLogic.addFlowsToRepository(request,sExtension);
			boolean bDump = false;
			if ( request.getParameter("dump")!=null )
				bDump = request.getParameter("dump").equals("true");

			if ( modelFlow == null ) {
				log.warning("No flow provided");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
			else if ( sExtension.endsWith("rdf") && bDump ) {
				log.info("Dumping the uploaded flow");
				dumpModel(request,response,modelFlow,"RDF/XML-ABBREV");
			}
			else if ( sExtension.endsWith("ttl") && bDump ) {
				log.info("Dumping the uploaded flow");
				dumpModel(request,response,modelFlow,"TTL");
			}
			else if ( sExtension.endsWith("nt") && bDump ) {
				log.info("Dumping the uploaded flow");
				dumpModel(request,response,modelFlow,"N-TRIPLE");
			}
			else  if ( bDump ){
				//
				// Invalid request found
				//
				log.info("Uknown repository service requested "+sTarget);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (FileUploadException e) {
			throw new IOException(e.toString());
		}

	}

    /** Returns all the components for the given tag.
	 *
	 * @param request The request object
	 * @param response The response object
	 * @param sTarget The target string
	 * @param sExtension The extension string
	 * @throws IOException A problem arised
	 */
	private void flowsByTagAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException{

		String sQuery = request.getParameter("q");

		if ( sQuery==null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getFlowsByTagAsTxt(request.getRemoteUser(),sQuery));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.getFlowsByTagAsJSON(request.getRemoteUser(),sQuery),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of components
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getFlowsByTagAsJSON(request.getRemoteUser(),sQuery));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/** Returns all the components for the given tag.
     *
     * @param request The request object
     * @param response The response object
     * @param sTarget The target string
     * @param sExtension The extension string
     * @throws IOException A problem arised
	 */
	private void componentsByTagAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException{

		String sQuery = request.getParameter("q");

		if ( sQuery==null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getComponentsByTagAsTxt(request.getRemoteUser(),sQuery));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.getComponentsByTagAsJSON(request.getRemoteUser(),sQuery),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of components
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getComponentsByTagAsJSON(request.getRemoteUser(),sQuery));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

    /** Search for flows matching the given criteria.
     *
     * @param request The request object
     * @param response The response objectlistFlowAction
     * @param sTarget The target string
     * @param sExtension The extension string
     * @throws IOException A problem arised
	 */
	private void searchFlowAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException{

		String sQuery = request.getParameter("q");
        String sOrder = request.getParameter("order");  // string - can be "date", "name"
        String sLimit = request.getParameter("limit");  // integer

        // validate sOrder
        boolean bOrderValid = true;
        if ( sOrder != null )
            bOrderValid = sOrder.matches("^date$|^name$");

        // validate sLimit
        boolean bLimitValid = true;
        int limit = -1;
        if ( sLimit != null ) {
            try {
                limit = Integer.valueOf(sLimit);
                if ( limit <= 0 ) bLimitValid = false;
            }
            catch (NumberFormatException e) {
                bLimitValid = false;
            }
        }

        if ( sQuery==null || !bOrderValid || !bLimitValid ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getSearchFlowsAsTxt(request.getRemoteUser(),sQuery,sOrder,limit));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.getSearchFlowsAsJSON(request.getRemoteUser(),sQuery,sOrder,limit),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of flows
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getSearchFlowsAsJSON(request.getRemoteUser(),sQuery,sOrder,limit));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


    /** Search for components matching the given criteria.
     *
     * @param request The request object
     * @param response The response object
     * @param sTarget The target string
     * @param sExtension The extension string
     * @throws IOException A problem arised
	 */
	private void searchComponentAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException{

		String sQuery = request.getParameter("q");
        String sOrder = request.getParameter("order");  // string - can be "date", "name"
        String sLimit = request.getParameter("limit");  // integer

        // validate sOrder
        boolean bOrderValid = true;
        if ( sOrder != null )
            bOrderValid = sOrder.matches("^date$|^name$");

        // validate sLimit
        boolean bLimitValid = true;
        int limit = -1;
        if ( sLimit != null ) {
            try {
                limit = Integer.valueOf(sLimit);
                if ( limit <= 0 ) bLimitValid = false;
            }
            catch (NumberFormatException e) {
                bLimitValid = false;
            }
        }

		if ( sQuery==null || !bOrderValid || !bLimitValid ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getSearchComponentsAsTxt(request.getRemoteUser(),sQuery,sOrder,limit));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.getSearchComponentsAsJSON(request.getRemoteUser(),sQuery,sOrder,limit),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of components
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getSearchComponentsAsJSON(request.getRemoteUser(),sQuery,sOrder,limit));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


    /** Describes the requested flow.
     *
     * @param request The request object
     * @param response The response object
     * @param sTarget The target string
     * @param sExtension The extension string
     * @throws IOException A problem arised
	 */
	private void describeFlowAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException{

		String sComponent = request.getParameter("uri");

		if ( sComponent==null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if ( sExtension.endsWith("rdf") ) {
			Model mod = wsRepositoryLogic.getFlowDescription(request.getRemoteUser(),sComponent);
			if ( mod == null )
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			else
				dumpModel(request,response,mod,"RDF/XML-ABBREV");
		}
		else if ( sExtension.endsWith("ttl") ) {
			Model mod = wsRepositoryLogic.getFlowDescription(request.getRemoteUser(),sComponent);
			if ( mod == null )
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			else
				dumpModel(request,response,mod,"TTL");
		}
		else if ( sExtension.endsWith("nt") ) {
			Model mod = wsRepositoryLogic.getFlowDescription(request.getRemoteUser(),sComponent);
			if ( mod == null )
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			else
				dumpModel(request,response,mod,"N-TRIPLE");
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	/** Describe all the available flows.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @param sTarget The target 
	 * @param sExtension The extension of the call
	 * @throws IOException Something went wrong when dumping the model
	 */
	private void describeAllFlowsAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension ) throws IOException {
		
		if ( sExtension.endsWith("rdf") ) {
			Model mod = wsRepositoryLogic.getAllFlowsDescription(request.getRemoteUser());
			dumpModel(request,response,mod,"RDF/XML-ABBREV");
		}
		else if ( sExtension.endsWith("ttl") ) {
			Model mod = wsRepositoryLogic.getAllFlowsDescription(request.getRemoteUser());
			dumpModel(request,response,mod,"TTL");
		}
		else if ( sExtension.endsWith("nt") ) {
			Model mod = wsRepositoryLogic.getAllFlowsDescription(request.getRemoteUser());
			dumpModel(request,response,mod,"N-TRIPLE");
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}



    /** Describes the requested component.
     *
     * @param request The request object
     * @param response The response object
     * @param sTarget The target string
     * @param sExtension The extension string
     * @throws IOException A problem arised
	 */
	private void describeComponentAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException{

		String sComponent = request.getParameter("uri");

		if ( sComponent==null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if ( sExtension.endsWith("rdf") ) {
			Model mod = wsRepositoryLogic.getComponentDescription(request.getRemoteUser(),sComponent);
			if ( mod == null)
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			else
				dumpModel(request,response,mod,"RDF/XML-ABBREV");
		}
		else if ( sExtension.endsWith("ttl") ) {
			Model mod = wsRepositoryLogic.getComponentDescription(request.getRemoteUser(),sComponent);
			if ( mod == null)
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			else
				dumpModel(request,response,mod,"TTL");
		}
		else if ( sExtension.endsWith("nt") ) {
			Model mod = wsRepositoryLogic.getComponentDescription(request.getRemoteUser(),sComponent);
			if ( mod == null)
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			else
				dumpModel(request,response,mod,"N-TRIPLE");
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown about service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}


	/** Describe all the available components.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @param target The target 
	 * @param extension The extension of the call
	 * @throws IOException Something went wrong when dumping the model
	 */
    private void describeAllComponentsAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException {
		// TODO Auto-generated method stub
    	
    	if ( sExtension.endsWith("rdf") ) {
			Model mod = wsRepositoryLogic.getAllComponentsDescription(request.getRemoteUser());
			dumpModel(request,response,mod,"RDF/XML-ABBREV");
		}
		else if ( sExtension.endsWith("ttl") ) {
			Model mod = wsRepositoryLogic.getAllComponentsDescription(request.getRemoteUser());
			dumpModel(request,response,mod,"TTL");
		}
		else if ( sExtension.endsWith("nt") ) {
			Model mod = wsRepositoryLogic.getAllComponentsDescription(request.getRemoteUser());
			dumpModel(request,response,mod,"N-TRIPLE");
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		
	}
    
	/** The tag flow action.
	 *
	 * @param request The request object
	 * @param response The response object
	 * @param sTarget The target
	 * @param sExtension The extension
	 * @throws IOException A problem arised
	 */
	private void tagsFlowsAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension)
			throws IOException {
		if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfFlowTagsAsTxt(request.getRemoteUser()));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.getListOfFlowTags(request.getRemoteUser()),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of components
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfFlowTags(request.getRemoteUser()));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/** The tags components action.
	 *
	 * @param request The request object
	 * @param response The response object
	 * @param sTarget The target
	 * @param sExtension The extension
	 * @throws IOException A problem arised
	 */
	private void tagsComponentsAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension)
			throws IOException {
		if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfComponentTagsAsTxt(request.getRemoteUser()));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.getListOfComponentTags(request.getRemoteUser()),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of components
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfComponentTags(request.getRemoteUser()));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/** The tags action
	 *
	 * @param request The request object
	 * @param response The response object
	 * @param sTarget The target
	 * @param sExtension The extension
	 * @throws IOException A problem arised
	 */
	private void tagsAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension)
			throws IOException {
		if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfTagsAsTxt(request.getRemoteUser()));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.getListOfTags(request.getRemoteUser()),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of components
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfTags(request.getRemoteUser()));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/** The list flows action
	 *
	 * @param request The request object
	 * @param response The response object
	 * @param sTarget The target
	 * @param sExtension The extension
	 * @throws IOException A problem arised
	 */
	private void listFlowsAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension)
			throws IOException {

        String sOrder = request.getParameter("order");  // string - can be "date", "name"
        String sLimit = request.getParameter("limit");  // integer

        // validate sOrder
        boolean bOrderValid = true;
        if ( sOrder != null )
            bOrderValid = sOrder.matches("^date$|^name$");

        // validate sLimit
        boolean bLimitValid = true;
        int limit = -1;
        if ( sLimit != null ) {
            try {
                limit = Integer.valueOf(sLimit);
                if ( limit <= 0 ) bLimitValid = false;
            }
            catch (NumberFormatException e) {
                bLimitValid = false;
            }
        }

        if (!(bOrderValid & bLimitValid)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        else if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfFlowsAsTxt(request.getRemoteUser(),sOrder,limit));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.getListOfFlows(request.getRemoteUser(),sOrder,limit),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of flows
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfFlows(request.getRemoteUser(),sOrder,limit));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/** The list components action
	 *
	 * @param request The request object
	 * @param response The response object
	 * @param sTarget The target
	 * @param sExtension The extension
	 * @throws IOException A problem arised
	 */
	private void listComponentsAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension)
			throws IOException {

        String sOrder = request.getParameter("order");  // string - can be "date", "name"
        String sLimit = request.getParameter("limit");  // integer

        // validate sOrder
        boolean bOrderValid = true;
        if ( sOrder != null )
            bOrderValid = sOrder.matches("^date$|^name$");

        // validate sLimit
        boolean bLimitValid = true;
        int limit = -1;
        if ( sLimit != null ) {
            try {
                limit = Integer.valueOf(sLimit);
                if ( limit <= 0 ) bLimitValid = false;
            }
            catch (NumberFormatException e) {
                bLimitValid = false;
            }
        }

        if (!(bOrderValid & bLimitValid)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        else if ( sExtension.endsWith("txt") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfComponentsAsTxt(request.getRemoteUser(),sOrder,limit));
		}
		else if ( sExtension.endsWith("xml") ) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			try {
				response.getWriter().println(XML.toString(wsRepositoryLogic.getListOfComponents(request.getRemoteUser(),sOrder,limit),"meandre_repository"));
			} catch (JSONException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		else if ( sExtension.endsWith("json") ) {
			//
			// Dump the list of components
			//
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			response.getWriter().println(wsRepositoryLogic.getListOfComponents(request.getRemoteUser(),sOrder,limit));
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/** The regenerate action
	 *
	 * @param request The request object
	 * @param response The response object
	 * @param extension
	 * @param target
	 * @param sTarget The target
	 * @param sExtension The extension
	 * @throws IOException A problem arised
	 */
	private void regenerateAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension) throws IOException {

		if ( sExtension.endsWith("txt") ) {
			URL urlRequest = new URL(request.getRequestURL().toString());
			if ( wsRepositoryLogic.regenerateRepository(request.getRemoteUser(),urlRequest.getProtocol(),urlRequest.getHost(),urlRequest.getPort()) ) {
				//
				// Repository successfully regenerated
				//
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("text/plain");
				response.getWriter().print("Repository successfully regenerated");
			}
			else {
				//
				// Repository successfully regenerated
				//
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
			}
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

	}

	/** The dump action.
     *
	 * @param request The request object
	 * @param response The response object
	 * @param sTarget The target
	 * @param sExtension The extension
	 * @throws IOException A problem arised
	 */
		private void dumpAction(HttpServletRequest request,
			HttpServletResponse response, String sTarget, String sExtension)
			throws IOException {
		if ( sExtension.endsWith("rdf") ) {
			dumpModel(request,response,store.getRepositoryStore(request.getRemoteUser()).getModel(),"RDF/XML-ABBREV");
		}
		else if ( sExtension.endsWith("ttl") ) {
			dumpModel(request,response,store.getRepositoryStore(request.getRemoteUser()).getModel(),"TTL");
		}
		else if ( sExtension.endsWith("nt") ) {
			dumpModel(request,response,store.getRepositoryStore(request.getRemoteUser()).getModel(),"N-TRIPLE");
		}
		else  {
			//
			// Invalid request found
			//
			log.info("Uknown repository service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

    /** Dumps the public repository.
     *
     * @param request The request object
     * @param response The response object
     * @param sFormat The format
     * @throws IOException Problem arised when dumping the output
     */
	private void dumpModel(HttpServletRequest request,
			HttpServletResponse response, Model model, String sFormat) throws IOException {

		model.setNsPrefix("meandre", Store.MEANDRE_ONTOLOGY_BASE_URL );
		model.setNsPrefix("xsd", XSD.getURI());
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("rdfs",RDFS.getURI());
		model.setNsPrefix("dc",DC.getURI());

		response.setStatus(HttpServletResponse.SC_OK);

		if ( sFormat.equals("RDF/XML-ABBREV") )
			response.setContentType("application/xml");
		else
			response.setContentType("text/plain");

		model.write(response.getOutputStream(),sFormat);

	}
    /**
     * checks to see if the user who issued the http request has a
     * given meandre role.
     * 
     * @param request this request's remote user will be checked 
     * @param roleToCheck 
     * @return whether or not that user has the role
     */
    private boolean requestorHasRole(HttpServletRequest request, 
            Role roleToCheck){
        boolean hasRole = false;
        try{
            SecurityManager secMan = store.getSecurityStore();
            User usr = secMan.getUser(request.getRemoteUser());
            hasRole = secMan.hasRoleGranted(usr, roleToCheck);
        }catch(SecurityStoreException sse){
            log.warning("Security Exception while verifying permissions for" + 
                    "role: " + roleToCheck.toString() + 
                    ". Permission being denied");
            hasRole = false;
        }
        return hasRole;

    }

}
