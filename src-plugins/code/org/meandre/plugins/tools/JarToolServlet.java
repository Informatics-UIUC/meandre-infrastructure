package org.meandre.plugins.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.plugins.MeandrePlugin;

/**This plugin reads the jar file and returns attributes for
 * the jar file.
 *
 * @author Amit Kumar (modified by Xavier Llor&agrave; to fix the directory mismatch + add the setCoreConfig method)
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
	private static  String PLUGIN_JAR_DIR = null;

	//alias path
	private String aliasPath = "/plugins/jar/*";

	/** Get the plugin logger */
	protected Logger log;

	private boolean bInited = Boolean.FALSE;


	/** Core configuration object */
	@SuppressWarnings("unused")
	private CoreConfiguration cnf = new CoreConfiguration();


	/** Sets the core configuration object to use.
	 *
	 * @param cnf The core configuration object
	 */
	public void setCoreConfiguration ( CoreConfiguration cnf ) {
		this.cnf = cnf;
		PLUGIN_JAR_DIR = cnf.getPublicResourcesDirectory() + File.separator + "contexts" + File.separator + "java";
	}

	public void init() throws ServletException{
		log.fine("Initing the JarToolServlet...");
	}

	public void setLogger ( Logger log ) {
		this.log = log;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
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
		log.finest("the command is " + command);
		JSONObject joRes = new JSONObject();
		File file = new File(PLUGIN_JAR_DIR,fileName);
		if(command.equalsIgnoreCase("manifest")){
			Manifest manifest = getManifest(file);
			if(manifest!=null){
			log.finest("getting manifest " + manifest.toString());
			Attributes attributes=manifest.getMainAttributes();
			Iterator it = attributes.keySet().iterator();
			Object key = null;
			while(it.hasNext()){
				key = it.next();
				joRes.put(key+"", attributes.get(key));
			}
			}else{
				log.warning("manifest does not exist.." + fileName);
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
			log.finest("getting manifest " + manifest.toString());
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
	 * @param file The file to check
	 * @return The MD% of the file
	 */
	private String getMD5(File file) {
		byte[] bytes=null;
		try {
			bytes=createChecksum(file);
		} catch (Exception e) {
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(boas));
			log.warning(boas.toString());
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
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(boas));
			log.warning(boas.toString());
		}
		return hexString;
	}


	private Manifest getManifest(File file) throws IOException {
		JarFile jarFile = new JarFile(file);

		//int len = jarFile.size();
		//System.out.println("len is: " + len);
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

	public void inited(Boolean success) {
		this.bInited = success;
	}


	/**Return the status of the plugin
	 *
	 */
	public boolean isInited() {
		return bInited;
	}

	/**This is a servlet not a filter
	 *
	 */
	public boolean isFilter() {
		return false;
	}




}
