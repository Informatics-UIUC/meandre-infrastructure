/**
 *
 */
package org.meandre.webservices.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.store.Store;
import org.meandre.core.utils.ModelIO;
import org.meandre.plugins.PluginFactory;
import org.meandre.plugins.tools.JarToolServlet;
import org.meandre.plugins.tools.ResourceConflictException;
import org.meandre.support.crypto.Crypto;
import org.meandre.webservices.MeandreServer;
import org.meandre.webservices.logger.WSLoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** Provides the servlet that serves Repository requests.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class WSRepositoryServlet extends MeandreBaseServlet {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** The web service logger */
	private static final Logger log = WSLoggerFactory.getWSLogger();

	/** The resource manager that interrogates the JarTool plugin for the existence of JARs based on MD5 checksums */
	private static JarToolServlet resManager;

	/** Creates the servlet to provide Repository information.
	 *
	 * @param server The Meandre server
	 * @param store The Meandre store to use
	 * @param cnf The Meandre configuration to use
	 */
	public WSRepositoryServlet(MeandreServer server, Store store, CoreConfiguration cnf) {
		super(server, store, cnf);
	}

	/** Adds components and flows to the user repository.
	 *
	 * @param request The request object
	 * @param store The Meandre store
	 * @param cnf The Meandre core configuration object
	 * @param sExtension The extension format
	 * @return The set of added URIs
	 * @throws FileUploadException An exception was encountered while uploading the model
	 */
	@SuppressWarnings("unchecked")
	public static final Set<String> addToRepository(HttpServletRequest request, Store store, CoreConfiguration cnf, String sExtension)
	throws IOException, FileUploadException {

		boolean bEmbed = false;
		boolean bOverwrite = false;
		Model modelAcc = ModelFactory.createDefaultModel();
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		List lstItems = upload.parseRequest(request);
		Iterator<FileItem> itr = lstItems.iterator();
		HashSet<ExecutableComponentDescription> setComponentsToAdd = new HashSet<ExecutableComponentDescription>();
		Hashtable<String,byte[]> htContextBytes = new Hashtable<String,byte[]>();

		// The user repository
		QueryableRepository qr = store.getRepositoryStore(request.getRemoteUser());

		while(itr.hasNext()) {
			FileItem item = itr.next();
		    // Get the name of the field
			String fieldName = item.getFieldName();
			byte[] data = item.get();

			// check if the current item is a form field or an uploaded file
            if(fieldName.equals("repository")) {

				String sRDFContent = new String(data);
				if (sRDFContent.length()==0 ) continue;

				Model modelTmp = ModelFactory.createDefaultModel();

				modelTmp.setNsPrefix("", "http://www.meandre.org/ontology/");
				modelTmp.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
				modelTmp.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
				modelTmp.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

				//
				// Read the location and check its consistency
				//
				ModelIO.attemptReadModel(modelTmp,sRDFContent);

				//
				// Accumulate the models
				//
				QueryableRepository qrTmp = new RepositoryImpl(modelTmp);
				QueryableRepository qrAcc = new RepositoryImpl(modelAcc);

				for ( ExecutableComponentDescription ecd:qrTmp.getAvailableExecutableComponentDescriptions() )
					if ( !qrAcc.getAvailableExecutableComponents().contains(ecd.getExecutableComponent()) )
						modelAcc.add(ecd.getModel());
					else
						log.warning("Discarding component "+ecd.getExecutableComponent()+". It already exists in the repository");

				for ( FlowDescription fd:qrTmp.getAvailableFlowDescriptions() )
					if ( !qrAcc.getAvailableFlows().contains(fd.getFlowComponent()) )
						modelAcc.add(fd.getModel());
					else
						log.warning("Discarding flow "+fd.getFlowComponent()+". It already exists in the repository");

			}
			// Check if we need to upload jar files
			else if(fieldName.equals("context")) {
				String sFile = item.getName();
				if ( sFile.length()==0) continue;
				htContextBytes.put(sFile, data);
			}
			else if ( fieldName.equals("embed") ) {
				String sValue = item.getString();
				if ( sValue.length()==0 ) continue;
				bEmbed = Boolean.parseBoolean(sValue);
			}
			else if ( fieldName.equals("overwrite") ) {
				String sValue = item.getString();
				if ( sValue.length()==0 ) continue;
				bOverwrite = Boolean.parseBoolean(sValue);
			}
		}

		// TODO: Check that this uploaded contexts are really proper contexts :D
		//
		// Check the uploaded description
		//
		QueryableRepository qrNew = new RepositoryImpl(modelAcc);

		//
		// The resulting set
		//
		Set<String> setAddedURIs = new HashSet<String>();

		synchronized ( qr ) {
			//
			// Add to the user repository
			//
			// Adding components
			for ( Resource resComp:qrNew.getAvailableExecutableComponents() ) {
				if ( qr.getExecutableComponentDescription(resComp)==null || bOverwrite) {
					// Component does not exist
					// Add to the set to modify before adding to the repository repository
					setComponentsToAdd.add(qrNew.getExecutableComponentDescription(resComp));
					setAddedURIs.add(resComp.toString());
				}
				else {
					// Component does exist
					log.warning("Component "+resComp+" already exists in "+request.getRemoteUser()+"'s repository. Ignoring it.");
				}
			}

			// Adding flows
			Model modUser = qr.getModel();
			modUser.begin();
			for ( Resource resFlow:qrNew.getAvailableFlows() ) {
				if ( qr.getFlowDescription(resFlow)==null ) {
					// Component does not exist
					modUser.add(qrNew.getFlowDescription(resFlow).getModel());
					setAddedURIs.add(resFlow.toString());
					log.info("Added flow "+resFlow+" to "+request.getRemoteUser()+"'s repository");

				}
				else {
					if ( bOverwrite ) {
						log.info("Overwriting flow "+resFlow);
						modUser.remove(qr.getFlowDescription(resFlow).getModel());
						modUser.add(qrNew.getFlowDescription(resFlow).getModel());
						setAddedURIs.add(resFlow.toString());
						log.info("Added flow "+resFlow+" to "+request.getRemoteUser()+"'s repository");
					}
					else
						// Component does exist
						log.warning("Flow "+resFlow+" already exists in "+request.getRemoteUser()+"'s repository. Ignoring it. No overwrite flag provided.");
				}
			}

			// Adding the components after adding the contexts
			Set<String> setFiles = htContextBytes.keySet();
			boolean bWriteOnce = true;
			for ( ExecutableComponentDescription ecd:setComponentsToAdd) {
				if ( htContextBytes.keySet().isEmpty() ) {
					if ( qr.getExecutableComponentDescription(ecd.getExecutableComponent())!=null ) {
						if ( bOverwrite ) {
							modUser.remove(qr.getExecutableComponentDescription(ecd.getExecutableComponent()).getModel());
							modUser.add(ecd.getExecutableComponent().getModel());
							log.info("Added component "+ecd.getExecutableComponent()+" to "+request.getRemoteUser()+"'s repository");
						}
						else
							log.warning("Discarding upload of the existing component "+ecd.getExecutableComponent()+". No overwrite flag provided.");
					}
					else {
						modUser.add(ecd.getModel());
						log.info("Added component "+ecd.getExecutableComponent()+" to "+request.getRemoteUser()+"'s repository");
					}
				}
				else {
					if ( (bEmbed && ecd.getRunnable().equals("java") && ecd.getFormat().equals("java/class") ) ||
							 (ecd.getRunnable().equals("python") && ecd.getFormat().equals("jython"))||
							 (ecd.getRunnable().equals("lisp") && ecd.getFormat().equals("clojure")) ) {
						//
						// Embed all the context per descriptor
						//
						for ( String sFile:setFiles) {
							Literal lit = modUser.createTypedLiteral(htContextBytes.get(sFile),XSDDatatype.XSDbase64Binary);
							ecd.getContext().add(lit);
						}
					}
					else if ( bWriteOnce ) {
                        String javaContextDir = cnf.getPublicResourcesDirectory()+File.separator+"contexts"+File.separator+"java"+File.separator;
                        new File(javaContextDir).mkdirs();

                        if (resManager == null)
                            resManager = (JarToolServlet)PluginFactory.getPluginFactory(cnf).getPlugin("JARTOOL");

					    //
						// Dump the context file to the disc only once
						//
						for ( String sFile:setFiles) {
							// Fail-safe check
							if ( sFile.length()==0 ) continue;

							// Dump the files to disk
							byte[] fileData = htContextBytes.get(sFile);
							if (fileData.length > 0) {     // MDR-160: ignore uploading context files of 0 size
							    // Create the MD5 checksum for the uploaded file (MDR-160)
							    String sMD5 = Crypto.getHexString(Crypto.createMD5Checksum(fileData)).toLowerCase();
							    String sJarFile = resManager.getResourceForMD5(sMD5);

							    if (sJarFile == null) {
							        File savedFile = new File(javaContextDir, sFile);
							        try {
							            FileOutputStream fos = new FileOutputStream(savedFile);
							            fos.write(fileData);
							            fos.close();
							        } catch (Exception e) {
							            log.warning("Problems writing context "+sFile+" to "+savedFile.getAbsolutePath());
							            throw new IOException(e.toString());
							        }

							        try {
                                        resManager.addResource(sFile, sMD5);
                                    }
                                    catch (ResourceConflictException e) {
                                        log.log(Level.WARNING, "JAR naming conflict!", e);
                                    }
							    } else {
							        log.finer("Skipping upload of '" + sFile + "'. Reason: Found as '" + sJarFile + "'");
							        sFile = sJarFile;
                                }
							} else
							    log.finer("Skipping upload of '" + sFile + "'. Reason: File size is 0");

							ecd.getContext().add(modUser.createResource("context://localhost/java/"+sFile));
						}
						// Avoid dumping them multiple times
						bWriteOnce = false;
					}

					if ( qr.getExecutableComponentDescription(ecd.getExecutableComponent())!=null ) {
						if ( bOverwrite ) {
							modUser.remove(qr.getExecutableComponentDescription(ecd.getExecutableComponent()).getModel());
							modUser.add(ecd.getModel());
							log.info("Adding component "+ecd.getExecutableComponent()+" to "+request.getRemoteUser()+"'s repository");
						}
						else
							log.warning("Discarding upload of the existem component "+ecd.getExecutableComponent()+". No overwrite flag provided.");
					}
					else {
						modUser.add(ecd.getModel());
					}
				}

			}

			// Commit changes
			modUser.commit();

			// Regenerate the cache after adding
			qr.refreshCache();
		}

		return setAddedURIs;
	}

}
