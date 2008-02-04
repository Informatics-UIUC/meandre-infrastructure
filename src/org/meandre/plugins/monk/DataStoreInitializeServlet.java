/*
 * @(#) DataStoreInitialize.java @VERSION@
 *
 * Copyright (c) 2007+ Amit Kumar
 *
 * The software is released under GNU GPL, Please
 * read License.txt
 *
 */
package org.meandre.plugins.monk;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.plugins.MeandrePlugin;

import edu.northwestern.at.monk.model.ModelException;
import edu.northwestern.at.monk.model.ModelInit;



/** This plugin initializes the monk datastore.
 * @author Amit Kumar
 * Created on Dec 2, 2007 4:31:41 AM
 *
 */
public class DataStoreInitializeServlet extends HttpServlet implements MeandrePlugin{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	final static String DATA_PROPERTY_1 =  "driver";
	final static String DATA_PROPERTY_2 =  "jdbcURL";
	final static String DATA_PROPERTY_3 =  "user";
	final static String DATA_PROPERTY_4 =  "password";
	final static String DATA_PROPERTY_5 =  "concurrentConnection";
	public static boolean isInited = Boolean.FALSE;




	// create the monk resources
	public void init(ServletConfig config) throws ServletException{
		super.init(config);
		Properties properties = new Properties();
		try {
			properties.load(DataStoreInitializeServlet.class.getResourceAsStream("monk.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("Starting the monkproxy2 application");
		String driver = properties.getProperty(DATA_PROPERTY_1);
		String jdbcURL = properties.getProperty(DATA_PROPERTY_2);
		String user = properties.getProperty(DATA_PROPERTY_3);
		String password = properties.getProperty(DATA_PROPERTY_4);
		String concurrentConnection = properties.getProperty(DATA_PROPERTY_5);
		System.out.println("JDBC url is: " + jdbcURL );
		System.out.println("initializing the monk datastore... This may take a while: "+ (new Date()).toString());

		int connection = 100;

		try{
		connection = Integer.parseInt(concurrentConnection);
		}catch (Exception e){
		connection = 100;
		}

		if(!isInited){
		try {
			ModelInit.init(driver,jdbcURL,user,password,connection);
		} catch (ModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(" Done initing... " + (new Date()).toString());
		}else{
		System.out.println(" Already inited skipping... " + (new Date()).toString());

		}
		this.getServletContext().setAttribute("INITED", isInited);
	}



	public  void service(HttpServletRequest req, HttpServletResponse res)
     throws ServletException,java.io.IOException{


	}


	public String getServletName(){
		return this.getClass().getName();
	}



	public String getAlias() {
		return null;
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




}
