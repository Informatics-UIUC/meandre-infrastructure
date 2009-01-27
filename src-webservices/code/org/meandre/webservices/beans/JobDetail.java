/*
 * @(#) JobDetail.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.webservices.beans;

/** This bean is populated by the WSExecuteLogic
 * 
 * @author Amit Kumar
 * Created on Jun 19, 2008 11:15:38 PM
 *
 */
public class JobDetail {
	private int port;
	private String hostname;
	private String flowInstanceId;
	private String token;
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}
	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	/**
	 * @return the flowInstanceId
	 */
	public String getFlowInstanceId() {
		return flowInstanceId;
	}
	/**
	 * @param flowInstanceId the flowInstanceId to set
	 */
	public void setFlowInstanceId(String flowInstanceId) {
		this.flowInstanceId = flowInstanceId;
	}
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
}
