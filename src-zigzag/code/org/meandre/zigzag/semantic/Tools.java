package org.meandre.zigzag.semantic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.utils.ModelIO;
import org.meandre.zigzag.parser.ParseException;

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
	public static URI filterURItoURL ( String sURI, int iLine ) 
	throws ParseException {
		String sURL = sURI.substring(1, sURI.length()-1).trim();
		try {
			return new URI(sURL);
		} catch (URISyntaxException e) {
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
			ModelIO.readModelInDialect(mod, url);
		} catch (Exception e) {
			throw new ParseException("Could not retrieve repository from "+url+" (line: "+iLine+")");
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
	public static void prepareJarsToTheFileSystem(FlowDescription fd, QueryableRepository qr, File fileFolderContexts ) 
	throws ParseException {
		
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
						URI uri = new URI(res.getURI().replaceAll(" ", "%20"));
						if ( uri.toString().endsWith(".jar")) {
							String [] sa = uri.getPath().split("/");
							InputStream is = uri.toURL().openStream();
							FileOutputStream fos = new FileOutputStream(new File(fileFolderContexts.toString()+File.separator+sa[sa.length-1]));
							int iTmp;
							while ( (iTmp=is.read())!=-1 )
								fos.write(iTmp);
							fos.close();
						}
					} catch (MalformedURLException e) {
						throw new ParseException(e.toString());
					} catch (IOException e) {
						throw new ParseException(e.toString());
					} catch (URISyntaxException e) {
						throw new ParseException(e.toString());
					}
				}
				else 
					throw new ParseException("Unknow context type: "+node);
			}
					
		}
		
	}

	/** Creates a Jar file (MAU file) for the provided directory.
	 * 
	 * @param fileJar The output jar file
	 * @param fileDirectory The directory to process
	 * @throws IOException There was an IO problem
	 */
	public static void generateJarFromDirectory(File fileJar, File fileDirectory) throws IOException {
		
		Collection<File> files = Tools.getCollectionOfFiles(fileDirectory);

		byte buffer[] = new byte[8192];
		// Open archive file
		try {
			FileOutputStream stream = new FileOutputStream(fileJar);
			JarOutputStream out = new JarOutputStream(stream, new Manifest());
			Iterator<File> iter = files.iterator();
			while ( iter.hasNext() ) {
				File f = iter.next();
				File fReal = new File(fileDirectory.toString()+f.toString());
				JarEntry jarAdd = new JarEntry(f.toString().substring(1).replace('\\','/'));
				jarAdd.setTime(fReal.lastModified());
				out.putNextEntry(jarAdd);
				
				// Write file to archive
				FileInputStream in = new FileInputStream(fReal);
				while (true) {
					int nRead = in.read(buffer, 0, buffer.length);
					if (nRead <= 0)
						break;
					out.write(buffer, 0, nRead);
				}
				in.close();
				// Delete the jared file
				fReal.delete();
			}
			out.close();
		    stream.close();
		    // Clean up
		    new File(fileDirectory+File.separator+"repository").delete();
		    new File(fileDirectory+File.separator+"contexts").delete();
		    new File(fileDirectory+File.separator+"plugins").delete();
		    fileDirectory.delete();
		    
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}

		
	}

	/** Returns a collection of file for the given folder.
	 * 
	 * @param fileDirectory The directory to process
	 * @return The collection of files
	 */
	private static Collection<File> getCollectionOfFiles(File fileDirectory) {
		LinkedList<File> res = new LinkedList<File>();
		
		if ( fileDirectory.isDirectory() ) {
			Queue<File> queuePending = new LinkedList<File>();
			queuePending.add(fileDirectory);
			while ( !queuePending.isEmpty() ) {
				File fileDir = queuePending.poll();
				for ( String sFile:fileDir.list() ) {
					File file = new File(fileDir+File.separator+sFile);
					if ( file.isDirectory() )
						queuePending.offer(file);
					else
						res.add(new File(file.toString().replaceAll(fileDirectory.toString(), "")));
				}
			}
		}
		
		return res;
	}

}
