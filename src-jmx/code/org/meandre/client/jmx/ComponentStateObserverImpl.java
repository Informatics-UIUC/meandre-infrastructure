/*
 * @(#) ComponentStateObserverImpl.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.client.jmx;

import org.meandre.client.jmx.observer.ComponentStateObserver;

/**This ComponentStateObserver implementation displays the component
 * lifecycle
 * 
 * @author Amit Kumar
 * Created on Jun 25, 2008 1:28:32 AM
 *
 */
public class ComponentStateObserverImpl implements ComponentStateObserver {
	
	
	public void aborted(String id) {
		System.out.println("Component "+ id+ " aborted");
	}

	public void cooling(String id) {
		System.out.println("Component "+ id+ " cooling");
	}

	public void disposed(String id) {
		System.out.println("Component "+ id+ " disposed");
	}

	public void fired(String id) {
		System.out.println("Component "+ id+ " fired");
	}

	public void init(String id) {
		System.out.println("Component "+ id+ " init");
	}

	public void readInput(String id, String portName) {
		System.out.println("Component "+ id+ " reading input " + portName);
	}

	public void readProperty(String id, String propertyName) {
		System.out.println("Component "+ id+ " reading property " + propertyName);
	}

	public void writeOutput(String id, String portName) {
		System.out.println("Component "+ id+ " writing " + portName);
	}

}
