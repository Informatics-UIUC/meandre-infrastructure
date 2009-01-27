/**
 * 
 */
package org.meandre.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
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
	 * @param sModel The input stream to use
	 * @throws IOException The model could not be read
	 */
	public static void attemptReadModel ( Model model, String sModel ) 
	throws IOException {
		//
		// Read the location and check its consistency
		//
		
		try {
			model.read(new ByteArrayInputStream(sModel.getBytes()),null,"TTL");
		}
		catch ( Exception eTTL ) {
			try {
				model.read(new ByteArrayInputStream(sModel.getBytes()),null);
			}
			catch ( Exception eNT ) {
				try {
					model.read(new ByteArrayInputStream(sModel.getBytes()),null,"N-TRIPLE");
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
		StringBuffer sbContent = new StringBuffer();
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(url.openStream()));
		String sLine;
		while ( (sLine=lnr.readLine())!=null )
			sbContent.append(sLine+'\n');
		
		if ( sLocation.endsWith(".ttl"))
			model.read(new ByteArrayInputStream(sbContent.toString().getBytes()),null,"TTL");
		else if ( sLocation.endsWith(".nt"))
			model.read(new ByteArrayInputStream(sbContent.toString().getBytes()),null,"N-TRIPLE");
		else if ( sLocation.endsWith(".rdf"))
			model.read(new ByteArrayInputStream(sbContent.toString().getBytes()),null);
		else
			ModelIO.attemptReadModel(model, sbContent.toString());
	}

}
