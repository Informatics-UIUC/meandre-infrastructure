package org.meandre.webservices.pub;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.core.store.Store;
import org.meandre.demo.repository.DemoRepositoryGenerator;
import org.meandre.webservices.utils.WSLoggerFactory;

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
public class WSPublic extends HttpServlet {

    /** A default serial ID */
	private static final long serialVersionUID = 1L;
	
	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();
	
	/**
	 * Dispatches web requests for Meandre web services.
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
    	
    	if ( sTarget.endsWith("/repository") ) {
			if ( sExtension.endsWith("rdf") ) {
				dumpRepository(request,response,Store.getPublicRepositoryStore(),"RDF/XML-ABBREV");
			}
			else if ( sExtension.endsWith("ttl") ) {
				dumpRepository(request,response,Store.getPublicRepositoryStore(),"TTL");
			}
			else if ( sExtension.endsWith("nt") ) {
				dumpRepository(request,response,Store.getPublicRepositoryStore(),"N-TRIPLE");
			}
			else  {
				// 
				// Invalid request found
				//
				log.info("Uknown public service requested "+sTarget);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
    	}
    	else if ( sTarget.endsWith("/demo_repository") ) {
			if ( sExtension.endsWith("rdf") ) {
				dumpRepository(request,response,DemoRepositoryGenerator.getTestHelloWorldHetereogenousRepository(),"RDF/XML-ABBREV");
			}
			else if ( sExtension.endsWith("ttl") ) {
				dumpRepository(request,response,DemoRepositoryGenerator.getTestHelloWorldHetereogenousRepository(),"TTL");
			}
			else if ( sExtension.endsWith("nt") ) {
				dumpRepository(request,response,DemoRepositoryGenerator.getTestHelloWorldHetereogenousRepository(),"N-TRIPLE");
			}
			else  {
				// 
				// Invalid request found
				//
				log.info("Uknown public service requested "+sTarget);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
    	}
    	else {
    		// 
			// Invalid request found
			//
			log.info("Uknown public service requested "+sTarget);
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
	private void dumpRepository(HttpServletRequest request,
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

	
}
