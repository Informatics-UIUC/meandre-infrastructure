/*
 * @(#) FlowStateObserverImpl.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.client.jmx;

import org.meandre.client.jmx.observer.FlowStateObserver;

/**Sample implementation of the flowstate observer
 * 
 * @author Amit Kumar
 * Created on Jun 25, 2008 3:05:40 AM
 *
 */
public class FlowStateObserverImpl implements FlowStateObserver {

	public void abort(String id, String message) {
		System.out.println("Flow:  " + id  + " aborted. Message: " + message );

	}

	public void finish(String id) {
		System.out.println("Flow with id " + id + "  finished.");
	}

	public void running(String id, String weburl) {
		System.out.println("Flow running  with id " + id  +" " + weburl);

	}

}
