/*
 * @(#) FlowListMBean.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.core.engine.probes.jmx;

import java.util.HashMap;

/**Interface implemented by FlowList bean provides
 * read only data to the clients.
 * 
 * @author Amit Kumar
 * Created on Jun 22, 2008 1:28:20 AM
 *
 */
public interface FlowListMBean {
	
	public int getCountFlowList();
	HashMap<String,FlowData> getFlowDataHashMap();
	public FlowData getFlowData(String flowId);
	public  void addFlowData(String flowId, FlowData flowData);
	public void removeFlowData(String flowId,FlowData flowData);
	public void updateFlowData(String flowId, FlowData flowData);
	

}
