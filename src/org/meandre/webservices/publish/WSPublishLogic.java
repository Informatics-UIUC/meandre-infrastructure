package org.meandre.webservices.publish;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.meandre.Bootstrapper;
import org.meandre.core.store.Store;
import org.meandre.core.store.repository.ExecutableComponentDescription;
import org.meandre.core.store.repository.FlowDescription;
import org.meandre.core.store.repository.QueryableRepository;
import org.meandre.core.store.repository.RepositoryImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/** The publish services logic
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSPublishLogic {

    /** The logger for the bootstrapper */
    protected static Logger log = null;

    // Initializing the logger and its handlers
    static {
        log = Logger.getLogger(Bootstrapper.class.getName());
        log.setLevel(Level.CONFIG);
        log.addHandler(Bootstrapper.handler);
    }

    /** Publish a URI and returns the uri in text if succeed.
     * 
     * @param request The request object
     * @param response The response object
     * @throws IOException Something went wrong
     */
	public static void publishURIAsTxt(HttpServletRequest request,
			HttpServletResponse response ) throws IOException {
		
		String sURI = request.getParameter("uri");
		sURI = (sURI==null)?"":sURI;
		
		boolean bPublished = publishURI(sURI,request.getRemoteUser() );
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		
		if ( bPublished ) 
			response.getWriter().println(sURI);
	}

	/** Publish the URI as a JSON object.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Something went wrong
	 */
	public static void publishURIAsJSON(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sURI = request.getParameter("uri");
		sURI = (sURI==null)?"":sURI;
		
		boolean bPublished = publishURI(sURI,request.getRemoteUser() );
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		
		try {
			if ( bPublished ) {
				PrintWriter pw = response.getWriter();
				JSONObject jo = new JSONObject();
				jo.put("meandre_uri", sURI);
				pw.println(jo);
			}
		} catch (JSONException e) {
			throw new IOException(e.toString());
		}
	}


	/** Publish the URI as a XML object.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Something went wrong
	 */
	public static void publishURIAsXML(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sURI = request.getParameter("uri");
		sURI = (sURI==null)?"":sURI;
		
		boolean bPublished = publishURI(sURI,request.getRemoteUser() );
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/xml");
		
		try {
			if ( bPublished ) {
				PrintWriter pw = response.getWriter();
				JSONObject jo = new JSONObject();
				jo.put("meandre_uri", sURI);
				pw.println(XML.toString(jo,"meandre_publishing"));
			}
			else {
				PrintWriter pw = response.getWriter();
				JSONObject jo = new JSONObject();
				pw.println(XML.toString(jo,"meandre_publishing"));
			}
		} catch (JSONException e) {
			throw new IOException(e.toString());
		}
	}

	/** Publishes the component described by the URI. If the URI already
	 * exist, the call returns false not modifying the published description;
	 * return true otherwise.
	 * 
	 * @param sURI The URI to publish
	 * @param sRemoteUser The publishing user.
	 */
	private static boolean publishURI(String sURI, String sRemoteUser) {
		
		boolean bPublished = false;
		QueryableRepository qr = Store.getRepositoryStore(sRemoteUser);
		Resource resURI = qr.getModel().createResource(sURI);
		Model modToPublish = null;
		Model modPublic = Store.getPublicRepositoryStore();
		QueryableRepository qrPublic = new RepositoryImpl(modPublic);
		
		if ( qrPublic.getExecutableComponentDescription(resURI)==null && 
			 qrPublic.getFlowDescription(resURI)==null ) {
			// The URI does not exist
			ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(resURI);
			FlowDescription fd = qr.getFlowDescription(resURI);
			if ( ecd!=null ) {
				modToPublish = ecd.getModel();
			}
			else if ( fd!=null ) {
				modToPublish = fd.getModel();
			}
			
			
			if ( modToPublish!=null ) {
				
				bPublished = true;
				modPublic.begin();
				modPublic.add(modToPublish);
				modPublic.commit();
			}
		}
		
		return bPublished;
	}


    /** Publish a URI and returns the uri in text if succeed.
     * 
     * @param request The resquest object
     * @param response The response object
     * @throws IOException Something went wrong
     */
	public static void unpublishURIAsTxt(HttpServletRequest request,
			HttpServletResponse response ) throws IOException {
		
		String sURI = request.getParameter("uri");
		sURI = (sURI==null)?"":sURI;
		
		unpublishURI(sURI,request.getRemoteUser() );
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		
		response.getWriter().println();
	}


	/** Publish the URI as a JSON object.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Something went wrong
	 */
	public static void unpublishURIAsJSON(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sURI = request.getParameter("uri");
		sURI = (sURI==null)?"":sURI;
		
		unpublishURI(sURI,request.getRemoteUser() );
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		
		try {
			PrintWriter pw = response.getWriter();
			JSONObject jo = new JSONObject();
			jo.put("meandre_uri", sURI);
			pw.println(jo);
		} catch (JSONException e) {
			throw new IOException(e.toString());
		}
	}

	/** Publish the URI as a XML object.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Something went wrong
	 */
	public static void unpublishURIAsXML(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sURI = request.getParameter("uri");
		sURI = (sURI==null)?"":sURI;
		
		unpublishURI(sURI,request.getRemoteUser() );
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/xml");
		
		try {
			PrintWriter pw = response.getWriter();
			JSONObject jo = new JSONObject();
			jo.put("meandre_uri", sURI);
			pw.println(XML.toString(jo,"meandre_publishing"));
		} catch (JSONException e) {
			throw new IOException(e.toString());
		}
	}


	/** Publishes the component described by the URI. If the URI already
	 * exist, the call returns false not modifying the published description;
	 * return true otherwise.
	 * 
	 * @param sURI The URI to publish
	 * @param sRemoteUser The publishing user.
	 */
	private static void unpublishURI(String sURI, String sRemoteUser) {
		
		QueryableRepository qr = Store.getRepositoryStore(sRemoteUser);
		Resource resURI = qr.getModel().createResource(sURI);
		Model modToUnpublish = null;
		Model modPublic = Store.getPublicRepositoryStore();
		QueryableRepository qrPublic = new RepositoryImpl(modPublic);
		
		if ( qrPublic.getExecutableComponentDescription(resURI)!=null || 
			 qrPublic.getFlowDescription(resURI)!=null ) {
			// The URI does not exist
			ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(resURI);
			FlowDescription fd = qr.getFlowDescription(resURI);
			if ( ecd!=null ) {
				modToUnpublish = ecd.getModel();
			}
			else if ( fd!=null ) {
				modToUnpublish = fd.getModel();
			}
			
			
			if ( modToUnpublish!=null ) {
				modPublic.begin();
				modPublic.remove(modToUnpublish);
				modPublic.commit();
			}
		}
		
	}

}
