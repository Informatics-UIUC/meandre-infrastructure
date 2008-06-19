/*
 * @(#) FileUploadFilter.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.plugins.filepart;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.meandre.plugins.MeandrePlugin;
import org.mortbay.servlet.MultiPartFilter;

public class FileUploadFilter extends MultiPartFilter implements MeandrePlugin{

	/** Get the plugin logger */
	protected Logger log;

	boolean inited= Boolean.FALSE;
	String alias = "/*";


	public void init(FilterConfig filterConfig)  throws ServletException{
		super.init(filterConfig);
		log.info("Starting the Filter...");
	}

	public String getAlias() {
		return alias;
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public String getPluginClassName() {
		return this.getClass().getName();
	}

	public Properties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public void inited(Boolean success) {
		this.inited = success;
	}

	public boolean isFilter() {
		return Boolean.TRUE;
	}

	public boolean isInited() {
		return  inited;
	}

	public boolean isResource() {
		return Boolean.FALSE;
	}

	public boolean isServlet() {
		return Boolean.FALSE;
	}

	public void setLogger(Logger log) {
		this.log = log;
	}

	public void doFilter(ServletRequest request, 
			ServletResponse response,
			FilterChain chain)
	throws IOException, ServletException 
	{
		super.doFilter(request, response,chain);
	}


}
