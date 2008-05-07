/*
 * @(#) JarToolServlet.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.plugins.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.plugins.MeandrePlugin;

/**This plugin reads the jar file and returns attributes for
 * the jar file.
 * 
 * @author Amit Kumar
 * Created on Mar 30, 2008 6:46:54 PM
 *
 */
public class JarToolServlet extends HttpServlet 
implements MeandrePlugin{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URL separator */
	private static final String URL_SEP="/";
	
	// the directory where java jars are stored
	private static  String PLUGIN_JAR_DIR = "published_resources"+File.separator+"contexts"+File.separator+"java"; 
			
	
	//alias path
	private String aliasPath = "/plugins/jar/*";

	/** Get the plugin logger */
	protected Logger log;
	
	public void init() throws ServletException{
		log.info("Initing the JarToolServlet...");
	}
	
	public void setLogger ( Logger log ) {
		this.log = log;
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path == null) {
			log.log(Level.SEVERE, "The path is null");
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if(!new File(PLUGIN_JAR_DIR).exists()){
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			log.log(Level.SEVERE, "The plugin jar directory " + PLUGIN_JAR_DIR + " does not exist.");
			return;
		}
		String locations[] = path.split(URL_SEP);

		if (locations.length <2) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} 
		
		res.setStatus(HttpServletResponse.SC_OK);
		String fileName = locations[1];
		if(locations.length==2){
			File file = new File(PLUGIN_JAR_DIR,fileName);
			if(!file.exists()){
				res.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}else{
				// send all the information about the jar
			}
		}else if(locations.length==3){
			String command = locations[2];
			try {
				processRequest(command,fileName,res);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	
	}

	@SuppressWarnings("unchecked")
	private void processRequest(String command, String fileName,
			HttpServletResponse res) throws IOException, JSONException {
		log.info("the command is " + command);
		JSONObject joRes = new JSONObject();
		File file = new File(PLUGIN_JAR_DIR,fileName);
		if(command.equalsIgnoreCase("manifest")){
			Manifest manifest = getManifest(file);
			if(manifest!=null){
			log.info("getting manifest " + manifest.toString());
			Attributes attributes=manifest.getMainAttributes();
			Iterator it = attributes.keySet().iterator();
			Object key = null;
			while(it.hasNext()){
				key = it.next();
				joRes.put(key+"", attributes.get(key));
			}
			}else{
				log.info("manifest does not exist..");
				res.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}else if(command.equalsIgnoreCase("md5")){
			String md5 = getMD5(file);
			joRes.put("md5", md5);
		}else if(command.equalsIgnoreCase("size")){
			joRes.put("size", file.length());
		}else if(command.equalsIgnoreCase("date")){
			joRes.put("lastModified", file.lastModified());
		}else if(command.equalsIgnoreCase("info")){
			String result  ="lastModified="+file.lastModified()+"|";
			result = result+"size="+file.length()+"|";
			String md5 = getMD5(file);
			result = result+"md5="+md5+"|";
			result = result+"name="+file.getName()+"|";
		
			Manifest manifest = getManifest(file);
			if(manifest!=null){
			log.info("getting manifest " + manifest.toString());
			Attributes attributes=manifest.getMainAttributes();
			Iterator it = attributes.keySet().iterator();
			Object key = null;
			while(it.hasNext()){
				key = it.next();
				result = result +key+"="+attributes.get(key)+"|";
			}
			}
			res.getOutputStream().print(result);
			res.flushBuffer();
			return;	
		}else{
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		res.getOutputStream().print(joRes.toString());
		res.flushBuffer();
	}

	/**returns the hex String
	 * 
	 * @param file
	 * @return
	 */
	private String getMD5(File file) {
		byte[] bytes=null;
		try {
			bytes=createChecksum(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(bytes==null){
			return null;
		}else if(bytes.length==0){
			return null;
		}
		String hexString=null;
		try {
		hexString=	StringUtils.getHexString(bytes);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hexString;
	}
	

	private Manifest getManifest(File file) throws IOException {
		JarFile jarFile = new JarFile(file);
		
		int len = jarFile.size();
		System.out.println("len is: " + len);
		return jarFile.getManifest();
	}

	public String getAlias() {
		return aliasPath;
	}

	public String getName() {
		return "Servlet for Tooling";
	}

	public String getPluginClassName() {
		return this.getClass().getName();
	}

	public Properties getProperties() {
		return null;
	}

	public boolean isResource() {
		return false;
	}

	public boolean isServlet() {
		return true;
	}
	
	public  byte[] createChecksum(File file) throws
    Exception{
		InputStream fis =  new FileInputStream(file);
		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
}




}
