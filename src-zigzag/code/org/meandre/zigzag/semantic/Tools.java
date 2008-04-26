package org.meandre.zigzag.semantic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.zigzag.parser.ParseException;

import sun.tools.jstat.ParserException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/** This class provides basic auxiliar tools
 * 
 * 
 * @author Xavier LLor&agrave;
 *
 */
public abstract class Tools {
	
	/** Remove the delimiting characters from the URI.
	 * 
	 * @param sURI The URI to process
	 * @param iLine The line to report in case of error
	 * @return The resulting URL
	 * @throws ParseException The URI is not a valid repository URL
	 */
	public static URL filterURItoURL ( String sURI, int iLine ) 
	throws ParseException {
		String sURL = sURI.substring(1, sURI.length()-1).trim();
		try {
			return new URL(sURL);
		} catch (MalformedURLException e) {
			throw new ParseException ( "The URL "+sURL+" is not a valid repository one (line: "+2+")");
		}
	}

	/** Filters the property value by removing the enclosing quotes.
	 * 
	 * @param sProperty The property value
	 * @return The filtered property value
	  @throws ParseException 
	 */
	public static String filterPropertyValue ( String sProperty ) {
		return sProperty.substring(1, sProperty.length()-1).trim();
	}
	
	/** Pull the repository checking the 3 different formats.
	 * 
	 * @param url The URL to pull from
	 * @param iLine The line where the request was generated
	 * @return The Model of the pulled repository
	 * @throws ParseException The repository could not be properly pulled
	 */
	public static Model pullRepository(URL url, int iLine) throws ParseException {
		Model mod = ModelFactory.createDefaultModel();
		try {
			mod.read(url.openStream(),null);
		} catch (Exception e) {
			try {
				mod.read(url.openStream(),null,"TTL");
			} catch (Exception e1) {
				try {
					mod.read(url.openStream(),null,"N-TRIPLE");
				} catch (Exception e2) {
					throw new ParseException("Could not retrieve repository from "+url+" (line: "+iLine+")");
				}
			}
		}
		return mod;
	}
	
	/** Create a directory and makes sure is a unique folder.
	 * 
	 * @param sOutputFolderName The output folder
	 * @return The file handler
	 */
	public static File createUniqueDirectory (String sOutputFolderName ) {
		File outpuDirectory = null;
		boolean bCreated;
		do {
			outpuDirectory = new File(sOutputFolderName+"."+System.currentTimeMillis());
			bCreated = outpuDirectory.mkdir();
		} while ( !bCreated );
		
		return outpuDirectory;
	}
	
	/** This methods dump the Literal to the file system and creates a URI for the class loader.
	 *
	 * @param fd The flow descriptor
	 * @param qr The repository implementation
	 * @param fileDirCntxs The folder containing the contexts
	 * @throws ParserException Something went really wrong
	 */
	public static void prepareJarLiteralsToTheFileSystem(FlowDescription fd, QueryableRepository qr, File fileFolderContexts ) 
	throws ParserException {
		
		for ( ExecutableComponentInstanceDescription ecid:fd.getExecutableComponentInstances() ) {
			ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(ecid.getExecutableComponent());
			Set<RDFNode> setCntxs = ecd.getContext();	
			for ( RDFNode node:setCntxs ) {
				if ( node.isLiteral() ) {
					// Do nothing, the descriptor will carry the jar over
				}
				else if ( node.isResource() ) {
					try {
						// Pull the URL and dump it to the local file
						Resource res = (Resource)node;
						URL url = new URL(res.getURI());
						String [] sa = url.getPath().split("/");
						InputStream is = url.openStream();
						FileOutputStream fos = new FileOutputStream(new File(fileFolderContexts.toString()+File.separator+sa[sa.length-1]));
						int iTmp;
						while ( (iTmp=is.read())!=-1 )
							fos.write(iTmp);
						fos.close();
					} catch (MalformedURLException e) {
						throw new ParserException(e.toString());
					} catch (IOException e) {
						throw new ParserException(e.toString());
					}
				}
				else 
					throw new ParserException("Unknow context type: "+node);
			}
					
		}
		
	}

}
