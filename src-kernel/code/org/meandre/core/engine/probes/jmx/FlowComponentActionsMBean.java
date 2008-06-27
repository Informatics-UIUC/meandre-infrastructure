/*
 * @(#) FlowComponentActionsMBean.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.core.engine.probes.jmx;

import java.util.ArrayList;
import java.util.HashMap;

public interface FlowComponentActionsMBean {
	
	public int getCountComponentList();
	HashMap<String, ComponentData> getComponentDataHashMap();
	public ComponentData getComponentData(String componentId);
	public  void addComponentData(String componentId, ComponentData componentData);
	public void removeComponentData(String componentId);
	public void updateComponentData(String componentId, ComponentAction componentAction);
	ArrayList<ComponentData> getComponents(String status);
	

}
