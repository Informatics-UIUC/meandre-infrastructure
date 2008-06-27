/*
 * @(#) FlowStateObserver.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.client.jmx.observer;

/**This interface is implemented by the Observers that
 * want to know the status the flow.
 * 
 * @author Amit Kumar
 * Created on Jun 25, 2008 2:21:52 AM
 *
 */
public interface FlowStateObserver {
	public void running(String id, String webUrl);
	public void finish(String id);
	public void abort(String id, String message);
}
