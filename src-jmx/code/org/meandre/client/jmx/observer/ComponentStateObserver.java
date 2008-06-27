/*
 * @(#) ComponentStateObserver.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.client.jmx.observer;

/**Implement this interface to capture the ComponentState changes
 * 
 * @author Amit Kumar
 * Created on Jun 25, 2008 12:38:28 AM
 *
 */
public interface ComponentStateObserver {
	
	public void init(String id);
	public void readProperty(String id, String propertyName);
	public void readInput(String id, String portName);
	public void writeOutput(String id, String portName);
	public void disposed(String id);
	public void aborted(String id);
	public void fired(String id);
	public void cooling(String id);
}
