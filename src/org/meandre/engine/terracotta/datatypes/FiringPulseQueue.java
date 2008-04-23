/**
 * 
 */
package org.meandre.engine.terracotta.datatypes;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.meandre.engine.terracotta.agent.ExecutableComponentAgent;

/** This class implements the queue of firing
 * @author Xavier Llor&agrave;
 *
 */
public class FiringPulseQueue {

	/** The pending firing request queue */
	protected Queue<FiringRequest> frqPending;
	
	/** The set of currently available agents */
	protected Map<ExecutableComponentAgent,ExecutableComponentAgent.Status> mecaAvailable;
	
	/** Initializes a shareable and concurrent save firing event queue.
	 * 
	 * 
	 */
	public FiringPulseQueue () {
		// Initializes the pending queue
		frqPending = new ConcurrentLinkedQueue<FiringRequest>(); 
		// Initializes the set of avaialable componets
		mecaAvailable = new ConcurrentHashMap<ExecutableComponentAgent,ExecutableComponentAgent.Status>();
	}
	
}
