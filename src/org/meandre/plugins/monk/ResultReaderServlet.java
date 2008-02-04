/*
 * @(#) ResultReaderServlet.java @VERSION@
 *
 * Copyright (c) 2008+ Amit Kumar
 *
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.plugins.monk;

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.plugins.MeandrePlugin;

/**This servlet reads all the posted variables and prints out the values.
 * It is essentially test code but it may be
 *
 * @author Amit Kumar
 * Created on Jan 29, 2008 9:22:22 PM
 *
 */
public class ResultReaderServlet extends HttpServlet implements MeandrePlugin {

	String alias;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	public void setAlias(String alias){
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	public String getName() {
		return this.getClass().getName();
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



	/**Nothing to initialize
	 *
	 */
	public void init() throws ServletException{
		System.out.println(this.getClass().getName() + ": Servlet initing.");

	}



	public  void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,java.io.IOException{
	Enumeration<String> enum1= req.getParameterNames();
	String paramName=null;
	while(enum1.hasMoreElements()){
			paramName = enum1.nextElement();
			res.getWriter().write(paramName+ " " + req.getParameter(paramName) + " <br/>");

	}
	res.getWriter().write("here it is ssome value...");
	}


}
