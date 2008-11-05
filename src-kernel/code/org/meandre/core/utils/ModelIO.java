/**
 * 
 */
package org.meandre.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.hp.hpl.jena.rdf.model.Model;

/** This class provides helper functions for IO if models
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ModelIO {
	
	/** Tries to read the model from any of the supported dialects.
	 * 
	 * @param model The model to read
	 * @param is The input stream to use
	 * @throws IOException The model could not be read
	 */
	public static void attemptReadModel ( Model model, InputStream is ) 
	throws IOException {
		//
		// Read the location and check its consistency
		//
		try {
			model.read(is,null,"TTL");
		}
		catch ( Exception eTTL ) {
			try {
				model.read(is,null,"N-TRIPLE");
			}
			catch ( Exception eNT ) {
				try {
					model.read(is,null);
				}
				catch ( Exception eRDF ) {
					IOException ioe = new IOException();
					ioe.setStackTrace(eRDF.getStackTrace());
					throw ioe;
				}
			}
		}
	}
	
	/** Based on the URL tries to read the model based on the extension.
	 * If the extension is not recognized defaults to attempt to load 
	 * the model.
	 * 
	 * @param model The model to read
	 * @param url The location to pull
	 * @throws IOException The model is not in any readable format
	 */
	public static void readModelInDialect( Model model, URL url ) 
	throws IOException {
		String sLocation = url.toString();
		
		if ( sLocation.endsWith(".ttl"))
			model.read(url.openStream(),null,"TTL");
		else if ( sLocation.endsWith(".nt"))
			model.read(url.openStream(),null,"N-TRIPLE");
		else if ( sLocation.endsWith(".rdf"))
			model.read(url.openStream(),null);
		else
			ModelIO.attemptReadModel(model, url.openStream());
	}

}
