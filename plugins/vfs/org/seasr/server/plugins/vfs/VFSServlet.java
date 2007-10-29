package org.seasr.server.plugins.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;


/**A servlet that provides access to files in
 * the zip/jar/tar.gz files. The files are mounted
 * as a Virtual file system.
 * 
 * @author Amit Kumar
 * Created on Oct 25, 2007 1:00:45 AM
 *
 */
public class VFSServlet extends HttpServlet {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* Location of the vfs files*/
	private static final String PUBLIC_RESOURCES_DIR = "resources/public";
	public static final String UNKNOWN_MIME_TYPE = "application/x-unknown-mime-type";
	Properties mimeProperties;
	
	
	/**Initialize the property file
	 * 
	 */
	public void init() throws ServletException{
		mimeProperties = new Properties();
		    try {
		    	mimeProperties.load(getClass().getClassLoader().getResourceAsStream("org/seasr/server/plugins/vfs/mime-types.properties"));
		    } catch (IOException e) {
		    	System.out.println("Could not read mime-types.properties " );
		    } 
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		OutputStream os = res.getOutputStream();
		String path = req.getPathInfo();
		if (path == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String locations[] = path.split(File.separator);
		
		if (locations.length <=2) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String componentName = locations[1];
		if (componentName.equals("..")) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String jarFileName = locations[2] + ".jar";
		java.io.File file = new File(PUBLIC_RESOURCES_DIR + File.separator
				+ componentName + File.separator + jarFileName);
		if (!file.exists()) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String fileName = File.separator;
		for (int fileTokenLen = 3; fileTokenLen < locations.length; fileTokenLen++) {
			fileName = fileName + File.separator + locations[fileTokenLen];
		}
		File f = new File(file.getAbsolutePath()+"/"+fileName);
		
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

	
	public String getServletInfo() {
		return "A servlet that retrieves documents from a jar file";
	}

}