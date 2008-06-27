/*
 * @(#) ComponentStates.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.core.engine.probes.jmx;

/**Defined component states
 * 
 * @author Amit Kumar
 * Created on Jun 22, 2008 2:49:27 PM
 *
 */
public final class ComponentStates {
	
	public static final String INITIALIZED="INITIALIZED";		// The component was initialized
	public static final String FIRED ="FIRED";				// The component got fired
	public static final String COOLING_DOWN ="COOLING_DOWN"; 		// The component is cooling down
	public static final String DISPOSED ="DISPOSED";			// The component got disposed
	public static final String ABORTED ="ABORTED";	
	public static final String DATA_IN  ="DATA_IN";
	public static final String DATA_OUT  ="DATA_OUT";
	public static final String PROP_READ  ="PROP_READ";
	
	
	
}
