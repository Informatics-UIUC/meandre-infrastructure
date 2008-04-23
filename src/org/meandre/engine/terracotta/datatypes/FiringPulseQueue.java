/**
 * 
 */
package org.meandre.engine.terracotta.datatypes;

import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import org.meandre.engine.terracotta.agent.ExecutableComponentAgent;

/** This class implements the queue of firing
 * @author Xavier Llor&agrave;
 *
 */
public class FiringPulseQueue {

	/** The pending firing request queue */
	protected Queue<FiringRequest> frqPending;
	
	/** The set of current firing events */
	protected Set<FiringEvent> setFiringEvents;
	
	/** The set of currently iddle agents */
	protected Set<ExecutableComponentAgent> secaIddle;
	
	/** The set of currently running agents */
	protected Set<ExecutableComponentAgent> secaRunning;
	
	/** The mutual exclusion semaphor for manipulating agent states */
	private Semaphore semMUTEXeca;
	
	/** Initializes a shareable and concurrent save firing event queue.
	 * 
	 * 
	 */
	public FiringPulseQueue () {
		// Initializes the pending queue
		frqPending = new ConcurrentLinkedQueue<FiringRequest>(); 
		// Initializes the set of current firing events
		setFiringEvents = new HashSet<FiringEvent>();
		// Initializes the set of iddle/running componets
		secaIddle = new HashSet<ExecutableComponentAgent>();
		secaRunning = new HashSet<ExecutableComponentAgent>();
		// Create the semaphore
		semMUTEXeca = new Semaphore(1,true);
	}
	
	/** Offers a firing request to the pending queue.
	 * 
	 * @param fr The firng request to offer
	 */
	public void offer ( FiringRequest fr ) {
		frqPending.offer(fr);
	}
	
	/** If there is while there are available agents keeps assigning 
	 * firing requests.
	 * 
	 */
	protected void scheduleIfPossible () {
		if ( !frqPending.isEmpty() ) {
			// Pending requests
			try {
				semMUTEXeca.acquire();
				// Assign agents
				while ( !frqPending.isEmpty() && !secaIddle.isEmpty() ) {
					FiringRequest fr = frqPending.poll();
					ExecutableComponentAgent eca = secaIddle.iterator().next();
					secaIddle.remove(eca);
					FiringEvent fe = new FiringEvent(fr,eca);
					// Add to the current firing event queue
					setFiringEvents.add(fe);
					// Give the fire request to the executable component agent
					eca.fire(fe);
				}
				semMUTEXeca.release();
			} catch (InterruptedException e) {
				// Semaphore failed
				e.printStackTrace();
			}
		}
		
	}
}
