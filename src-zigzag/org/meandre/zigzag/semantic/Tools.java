package org.meandre.zigzag.semantic;

import java.net.MalformedURLException;
import java.net.URL;

import org.meandre.zigzag.parser.ParseException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
}
