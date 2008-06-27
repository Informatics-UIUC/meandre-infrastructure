/*
 * @(#) RMIRegistryServlet.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.plugins.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.plugins.MeandrePlugin;

/**
 * This plugins starts a RMI registry at a well known port
 * 
 * @author Amit Kumar Created on Jun 22, 2008 6:53:16 PM
 * 
 */
public class RMIRegistryServlet extends HttpServlet implements MeandrePlugin {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Registry registry;
	private int REG_PORT = 1099;
	boolean inited = false;
	private Logger _logger;

	/**It is important to run this in the constructor because 
	 * WebService require the MBeanServer
	 * start the rmi registry
	 */
	public RMIRegistryServlet(){
		try {
			registry= LocateRegistry.createRegistry(REG_PORT);
			System.out.println("Starting RMI registry");
		} catch (Exception e) {
			System.out.println("Error creating RMI registry");
			   try {
		            registry = LocateRegistry.getRegistry(REG_PORT);
		        } catch (RemoteException rex) {
		        	System.out.println("Error creating RMI registry");
		            return;
		        }
		}
	}


	
	public void init() throws ServletException {
		
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
			_logger.info("Getting request to list the services registered:");
			String[] names=registry.list();
			res.setContentType("text/plain");
			ServletOutputStream os=res.getOutputStream();
			os.println("port="+REG_PORT);
			if(names!=null){
			for(int i=0; i < names.length; i++){
				os.println("name"+i+"="+ names[i]);
			}
			}
			os.flush();
			os.close();
	}

	public String getAlias() {
		return "/rmiregistry";
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public String getPluginClassName() {
		return this.getClass().getName();
	}

	public Properties getProperties() {
		return null;
	}

	public void inited(Boolean success) {
		this.inited = success;
	}

	public boolean isFilter() {
		return false;
	}

	public boolean isInited() {
		return inited;
	}

	public boolean isResource() {
		return false;
	}

	public boolean isServlet() {
		return true;
	}

	public void setLogger(Logger log) {
		_logger = log;
	}

}
