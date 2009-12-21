package org.meandre.plugins.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.logger.PluginsLoggerFactory;
import org.meandre.plugins.MeandrePlugin;
import org.meandre.support.io.IOUtils;

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

	//alias path
	public static final String SERVLET_PATH = "/plugins/jar/";

	/**
     * The core root logger
     */
    protected Logger log = PluginsLoggerFactory.getPluginsLogger();

	private boolean bInited = Boolean.FALSE;


	/** Core configuration object */
	@SuppressWarnings("unused")
	private CoreConfiguration cnf = new CoreConfiguration();

    private final Map<String, String> _md5map = Collections.synchronizedMap(new HashMap<String, String>());


	/** Sets the core configuration object to use.
	 *
	 * @param cnf The core configuration object
	 */
	public void setCoreConfiguration ( CoreConfiguration cnf ) {
		this.cnf = cnf;
		PLUGIN_JAR_DIR = cnf.getPublicResourcesDirectory() + File.separator + "contexts" + File.separator + "java";
	}

	@Override
	public void init() throws ServletException {
	    synchronized (_md5map) {
    	    log.fine("Initializing the JarToolServlet...");

    	    try {
    	        File fResPath = new File(PLUGIN_JAR_DIR);
    			fResPath.mkdirs();

    	        _md5map.clear();

    	        File[] md5Files = fResPath.listFiles(new FilenameFilter() {
    	            public boolean accept(File dir, String name) {
    	                return name.toLowerCase().endsWith(".md5");
    	            }
    	        });

    	        for (File f : md5Files) {
    	            String md5Value = IOUtils.getTextFromReader(IOUtils.getReaderForResource(f.toURI()));
    	            String resName = f.getName();                              // ex: context.jar.md5
    	            resName = resName.substring(0, resName.lastIndexOf("."));  // ex: context.jar

    	            // sanity check
    	            if (!new File(f.getParent(), resName).exists()) {
    	                f.delete(); // delete orphaned .md5 file
    	                continue;
    	            }

    	            _md5map.put(md5Value.toLowerCase(), resName);
    	        }
    	    } catch (IOException e) {
    	        throw new ServletException(e);
    	    }
	    }
	}

	public String getResourceForMD5(String sMD5) {
	    return _md5map.get(sMD5.toLowerCase());
	}

	public void setLogger ( Logger log ) {
		this.log = log;
	}

	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		log.finer("Request path: " + path);

		String[] locations;

		if (path == null || (locations = path.split(URL_SEP)).length < 2) {
			res.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
			return;
		}

		String fileName = locations[1];

		if (fileName.trim().toLowerCase().endsWith(".md5")) {
		    String resName = getResourceForMD5(fileName.split("\\.")[0]);
		    if (resName != null)
		        fileName = resName;
		    else {
		        res.sendError(HttpServletResponse.SC_NOT_FOUND);
		        return;
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

	public void addResource(String sFile, String sMD5) throws ResourceConflictException, IOException {
	    if (sFile == null || sMD5 == null)
	        throw new NullPointerException("need to supply valid values for the 'sFile' and 'sMD5' parameters");

	    String sResName = getResourceForMD5(sMD5);
	    if (sResName != null) {
	        if (!sResName.equals(sFile))
	            throw new ResourceConflictException(sResName + " already exists - trying to add it again as " + sFile);
	        else
	            return;
	    }

	    File resFile = new File(PLUGIN_JAR_DIR, sFile);
	    // make sure the resource file actually exists
	    if (!resFile.exists())
	        throw new FileNotFoundException(sFile);

	    File md5File = new File(PLUGIN_JAR_DIR, sFile + ".md5");
	    Writer writer = null;

	    try {
	        writer = IOUtils.getWriterForResource(md5File.toURI());
            writer.write(sMD5);
        }
        catch (IOException e) {
            log.log(Level.SEVERE, "Cannot create the MD5 checksum file for " + sFile, e);
            throw e;
        }
        finally {
            if (writer != null)
                try {
                    writer.close();
                }
                catch (Exception e) {}
        }

	    _md5map.put(sMD5.toLowerCase(), sFile);

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
		    joRes.put("md5", IOUtils.getTextFromReader(new FileReader(file.toString() + ".md5")).replaceAll("\\r|\\n", ""));

		else

		// size
		if (command.equalsIgnoreCase("size"))
		    joRes.put("size", file.length());

		else

		// date
		if (command.equalsIgnoreCase("date"))
		    joRes.put("lastModified", file.lastModified());

		else

		if (command.equalsIgnoreCase("name"))
		    joRes.put("name", file.getName());

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
		return SERVLET_PATH + "*";
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
