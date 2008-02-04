/*
 * @(#) HttpProxyServlet.java @VERSION@
 *
 * Copyright (c) 2007+ Amit Kumar
 *
 * The software is released under GNU GPL, Please
 * read License.txt
 *
 */
package org.meandre.plugins.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.plugins.MeandrePlugin;


/**Used for CROSS DOMAIN AJAX
 *
 * @author Amit Kumar
 * Created on Nov 2, 2007 1:57:57 AM
 *
 */
public class HttpProxyServlet  extends HttpServlet  implements MeandrePlugin {
	/**
	 *default serial version uid
	 */
	private static final long serialVersionUID = 1L;

	private String alias;

	/** The logger for the bootstrapper */
	private static Logger log = null;

	/** The basic handler for all the loggers */
	public static Handler handler = null;

	// Initializing the logger and its handlers
	static {
		log = Logger.getLogger(HttpProxyServlet.class.getName());
		log.setLevel(Level.FINEST);
		try {
			log.addHandler(handler = new FileHandler("proxy-plugin-log.xml"));
		} catch (SecurityException e) {
			System.err.println("Could not initialize proxy-plugin-log.xml");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Could not initialize proxy-plugin-log.xml");
			System.exit(1);
		}

		handler.setLevel(Level.FINEST);
	}

	/**Nothing to initialize
	 *
	 */
	public void init() throws ServletException{
		System.out.println(this.getClass().getName() + ": Servlet initing.");

	}

	/**Get implementation, expects a target parameter
	 *
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		InputStream is = null;
		ServletOutputStream out = null;
		//key
		//site
		String target = req.getParameter("target");
		log.info("Calling: " + target + " by " + req.getRemoteAddr());
		if(target==null){
			res.setStatus(404);
			return;
		}
		try {
			URL url = new URL(target);
			URLConnection uc = url.openConnection();
			res.setContentType(uc.getContentType());
			is = uc.getInputStream();
			out = res.getOutputStream();
			byte[] buf = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
		} catch (MalformedURLException e) {
			log.info("Error in the url: " + e.getMessage());
			res.setStatus(404);
		} catch (IOException e) {
			log.info("IOException: " + e.getMessage());
			res.setStatus(404);
		} finally {
			if(is != null)
				is.close();
			if(out != null)
				out.close();
		}

		log.info("done proxy call.");
	}


	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	public String getName() {
		return "Meandre Plugin";
	}

	public String getPluginClass() {
		return this.getClass().getName();
	}

	public boolean isResource() {
		return false;
	}

	public boolean isServlet() {
		return true;
	}



}
