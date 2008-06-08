package org.meandre.plugins.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.plugins.MeandrePlugin;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;


/**A servlet that provides access to files in
 * the zip/jar/tar.gz files. The files are mounted
 * as a Virtual file system.
 *
 * @author Amit Kumar
 * Created on Oct 25, 2007 1:00:45 AM
 * @modified for the core on Feb 23rd 2008
 *
 */
public class VFSServlet  
extends HttpServlet 
implements MeandrePlugin{

	/** A default serial ID. */
	private static final long serialVersionUID = 1L;

	/** Location of the vfs files*/
	private static  String PUBLIC_RESOURCES_DIR = "resources/public";
	
	/** Unknow mime type */
	public static final String UNKNOWN_MIME_TYPE = "application/x-unknown-mime-type";
	
	/** The URL separator */
	private static final String URL_SEP="/";
	
	/** The mime properties */
	protected Properties mimeProperties;
	
	/** The plugin properties */
	protected Properties vfsProperties;

	/** Get the plugin logger */
	protected Logger log;
	
	private Boolean inited;

	/** Sets the pluggin logger 
	 * 
	 * @param log The logger
	 */
	public void setLogger ( Logger log ) {
		this.log = log;
	}
	
	/**Initialize the property file
	 *
	 */
	public void init() throws ServletException{
		mimeProperties = new Properties();
		    try {
		    	mimeProperties.load(getClass().getResourceAsStream("mime-types.properties"));
		    } catch (IOException e) {
		    	log.warning("Could not read mime-types.properties " );
		    }

		    vfsProperties = new Properties();
		    try {
		    	vfsProperties.load(getClass().getResourceAsStream("vfs.properties"));
		    } catch (IOException e) {
		    	log.warning("Could not read vfs.properties " );
		    }
		    PUBLIC_RESOURCES_DIR = vfsProperties.getProperty("mount_dir", "mnt");

		    log.info("Mounting from folder: " + PUBLIC_RESOURCES_DIR );
		    File file = new File( PUBLIC_RESOURCES_DIR);

		    if(!file.exists()){
		    	log.info("Creating the folder... "+ file.getAbsolutePath());
		    	file.mkdirs();
		    }

		    String[] fileName = file.list();
		    for(int i=0; i < fileName.length; i++){
		    	log.info("Mounting file: "+fileName[i]);
		    }

	}

	/** Responds to a get request.
	 * 
	 * @param req The request object
	 * @param res The response object
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		OutputStream os = res.getOutputStream();
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
		String componentName = locations[1];
		if (componentName.equals("..")) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		java.io.File file = getFile(new File(PUBLIC_RESOURCES_DIR), locations[1]);

		if (file==null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String fileName = File.separator;
		for (int fileTokenLen = 2; fileTokenLen < locations.length; fileTokenLen++) {
			fileName = fileName + System.getProperty("file.separator") + locations[fileTokenLen];
		}
		File f = new File(file.getAbsolutePath(),fileName);

		if(!f.exists()){
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		res.setStatus(HttpServletResponse.SC_OK);
		res.setContentType(getMimeType(fileName));
		InputStream is = new FileInputStream(f);
		long length = f.length();
		// Create the byte array to hold the data
		if(length >  Integer.MAX_VALUE){
			res.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT);
			return;
		}

        byte[] bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while ( (offset < bytes.length)
                &&
                ( (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) ) {
            offset += numRead;
            os.write(bytes);
        }

	    os.flush();
	}

	/** Return the requested file.
	 * 
	 * @param file The file
	 * @param sFileName The file name
	 * @return The file
	 */
	private java.io.File getFile(File file, String sFileName) {
		// check of jar file exists
		File compressedFileSystem = new File(file,sFileName+".jar");
		log.info("Checking: "+ compressedFileSystem.getAbsolutePath());
		if(compressedFileSystem.exists()){
			return compressedFileSystem;
		}

		log.info("Checking: "+ compressedFileSystem.getAbsolutePath());
		compressedFileSystem = new File(file,sFileName+".zip");
		if(compressedFileSystem.exists()){
			return compressedFileSystem;
		}
		log.info("Not found returning... null");
		return null;
	}

	/** Returns the mimetype for the file name.
	 * 
	 * @param fileName The filename to check 
	 * @return The mime type of the requested file
	 */
	private String getMimeType(String fileName) {
		String fileExtension = null;
		int i = fileName.lastIndexOf(".");
		if(i==-1){
			return UNKNOWN_MIME_TYPE;
		}else{
			fileExtension =  fileName.substring(i+1);
		}
		String mimeType =  mimeProperties.getProperty(fileExtension);
		if(mimeType==null){
			mimeType= UNKNOWN_MIME_TYPE;
		}
		return mimeType;
	}


	/** The servlet information.
	 * 
	 * @return The servlet information
	 */
	public String getServletInfo() {
		return "A servlet that retrieves documents from a jar file";
	}

	/** The alias name. If it is a servlet is the context path.
	 * 
	 * @return The alias
	 */
	public String getAlias() {
		return "/plugins/vfs/*";
	}

	/** The name of the plugin.
	 * 
	 * @return The name of the plugin
	 */
	public String getName() {
		return "Virtual file server for jar mounting";
	}

	/** The plugin class name.
	 * 
	 * @return The class name
	 */
	public String getPluginClassName() {
		return this.getClass().getName();
	}

	/** Is the plugin a resource?.
	 * 
	 * @return True if the plugin is a resource
	 */
	public boolean isResource() {
		return false;
	}

	/** Is the plugin a servlet?
	 * 
	 * @return True if it is a servlet
	 */
	public boolean isServlet() {
		return true;
	}
	
	/** Return the plugin properties.
	 * 
	 * @return The properties of the plugin
	 */
	public Properties getProperties() {
		return vfsProperties;
	}

	public void inited(Boolean success) {
		this.inited= success;
	}

	/**Return the status of the plugin
	 * 
	 */
	public boolean isInited() {
		return inited;
	}

}