package org.meandre.plugins.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import org.meandre.support.io.FileUtils;
import org.meandre.support.text.StringUtils;

/**
 * This plugin checks whether a particular JAR file is present in the context of the server
 * either by verifying that a particular file name exists or by submitting an MD5 checksum
 * and checking whether it is already present in the server's context.
 *
 * @author Amit Kumar
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 */
public class JarToolServlet extends HttpServlet implements MeandrePlugin {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The URL separator */
	private static final String URL_SEP="/";

	// the directory where java jars are stored
	private static String PLUGIN_JAR_DIR = null;
	private static String MD5_DIR = null;

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
		MD5_DIR = PLUGIN_JAR_DIR + File.separator + "md5/";
	}

	public void init() throws ServletException{
		log.fine("Initializing the JarToolServlet...");
	}

	public void setLogger ( Logger log ) {
		this.log = log;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	    log.entering(getClass().getName(), "doGet");

		String path = req.getPathInfo();
		log.fine("Request path: " + path);

		String[] locations;

		if (path == null || (locations = path.split(URL_SEP)).length < 2) {
			res.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
			return;
		}

		String fileName = locations[1];

		if (fileName.trim().toLowerCase().endsWith(".md5")) {
		    // check if the MD5 signature exists
		    File md5File;

		    if ((md5File = new File(MD5_DIR, fileName.toLowerCase())).exists()) {
		        BufferedReader reader = new BufferedReader(new FileReader(md5File));
		        fileName = reader.readLine();
		        reader.close();
		    }
		}

		File file = new File(PLUGIN_JAR_DIR, fileName);
		if (!file.exists()) {
		    res.sendError(HttpServletResponse.SC_NOT_FOUND);
		    return;
		}

		res.setStatus(HttpServletResponse.SC_OK);

		switch (locations.length) {
		    case 2:
		         // TODO: send all the information about the jar (no command was supplied)
		        break;

		    case 3:
		        String command = locations[2];
	            try {
	                processRequest(command,file,res);
	            } catch (JSONException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
		        break;

	        default:
	            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
	            return;
		}
	}

	@SuppressWarnings("unchecked")
	private void processRequest(String command, File file, HttpServletResponse res) throws IOException, JSONException {
		log.finest("the command is " + command);

		JSONObject joRes = new JSONObject();

		// manifest
		if (command.equalsIgnoreCase("manifest")) {
		    Manifest manifest = getManifest(file);
		    if (manifest != null) {
		        log.finest("getting manifest " + manifest.toString());
		        Attributes attributes=manifest.getMainAttributes();
		        Iterator it = attributes.keySet().iterator();
		        Object key = null;
		        while(it.hasNext()){
		            key = it.next();
		            joRes.put(key+"", attributes.get(key));
		        }
		    } else {
		        log.warning("manifest does not exist.." + file);
		        res.sendError(HttpServletResponse.SC_NOT_FOUND);
		        return;
		    }
		}

		else

		// md5
		if (command.equalsIgnoreCase("md5"))
		    joRes.put("md5", StringUtils.getHexString(FileUtils.createMD5Checksum(file)));

		else

		// size
		if (command.equalsIgnoreCase("size"))
		    joRes.put("size", file.length());

		else

		// date
		if (command.equalsIgnoreCase("date"))
		    joRes.put("lastModified", file.lastModified());

		else

		// info
		if (command.equalsIgnoreCase("info")) {
		    String result = "lastModified="+file.lastModified()+"|";
		    result += "size="+file.length()+"|";
		    result += "md5=0|";   // for backward compatibility - removed actual calculation because not needed; can obtain via "md5" command
		    result += "name="+file.getName()+"|";

		    Manifest manifest = getManifest(file);
		    if (manifest != null) {
		        log.finest("getting manifest " + manifest.toString());
		        Attributes attributes = manifest.getMainAttributes();
		        Iterator it = attributes.keySet().iterator();
		        Object key = null;
		        while (it.hasNext()) {
		            key = it.next();
		            result = result +key+"="+attributes.get(key)+"|";
		        }
		    }
		    res.getOutputStream().print(result);
		    res.flushBuffer();
		    return;
		}

		else {
		    res.sendError(HttpServletResponse.SC_NOT_FOUND);
		    return;
		}

		res.getOutputStream().print(joRes.toString());
		res.flushBuffer();
	}

	private Manifest getManifest(File file) throws IOException {
		return new JarFile(file).getManifest();
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

	public void inited(Boolean success) {
		this.bInited = success;
	}

	/**
	 * Returns the initialization status of the plugin
	 *
	 */
	public boolean isInited() {
		return bInited;
	}

	/**
	 * This is a servlet not a filter
	 *
	 */
	public boolean isFilter() {
		return false;
	}
}
