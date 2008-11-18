/**
 * 
 */
package org.meandre.webservices.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.store.Store;
import org.meandre.core.utils.ModelIO;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** Provides the servlet that serves Execute requests.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSExecuteServlet extends MeandreBaseServlet {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;
	
	/** Creates the servlet to provide Execute information.
	 * 
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	public WSExecuteServlet(Store store, CoreConfiguration cnf) {
		super(store, cnf);
		
	}


	/** Adds components and flows to the user repository.
	 *
	 * @param request The request object
	 * @param store The Meandre store
	 * @throws FileUploadException An exception araised while uploading the model
	 */
	@SuppressWarnings("unchecked")
	public static final QueryableRepository extractRepository(HttpServletRequest request, Store store)
	throws IOException, FileUploadException {

		Model modelAcc = ModelFactory.createDefaultModel();
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		List lstItems = upload.parseRequest(request);
		Iterator<FileItem> itr = lstItems.iterator();

		while(itr.hasNext()) {
			FileItem item = itr.next();
		    // Get the name of the field
			String fieldName = item.getFieldName();

			// check if the current item is a form field or an uploaded file
			if(fieldName.equals("repository")) {

				String sContent = new String(item.get());

				Model modelTmp = ModelFactory.createDefaultModel();

				modelTmp.setNsPrefix("", "http://www.meandre.org/ontology/");
				modelTmp.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
				modelTmp.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
				modelTmp.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

				//
				// Read the location and check its consistency
				//
				ModelIO.attemptReadModel(modelTmp, sContent);

				//
				// Accumulate the models
				//
				modelAcc.add(modelTmp);
			}
			
		}

		// There was no repository uploaded
		if ( modelAcc.isEmpty() ) return null;
		
		// The current user repository 
		QueryableRepository qr = store.getRepositoryStore(request.getRemoteUser());

		// Create the repository to use
		QueryableRepository qrRes = new RepositoryImpl(modelAcc);
		for (FlowDescription fd:qrRes.getAvailableFlowDescriptions()) {
			for ( ExecutableComponentInstanceDescription ecid: fd.getExecutableComponentInstances() )
				if ( !qrRes.getAvailableExecutableComponents().contains(ecid.getExecutableComponent()) ) {
					qrRes.getAvailableExecutableComponentDescriptions().add(
							qr.getExecutableComponentDescription(ecid.getExecutableComponent()));
				}
		}
	
		return qrRes;
	}

}
