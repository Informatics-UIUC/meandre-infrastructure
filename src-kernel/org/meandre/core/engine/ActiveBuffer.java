package org.meandre.core.engine;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/** An active buffer buffers the passed objects between ExecutableComponents.
 * It is active because when an obect is inserted in the buffer by a publisher,
 * it notifies all the subscribers letting them run.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ActiveBuffer {

	/** The capacity semaphore */
	private Semaphore semCapacity = null;
	
	/** The active pipe consumer */
	private Set<WrappedComponent> wcConsumer = null;
	
	/** The queue containing the elements so far */
	private Queue<Object> queueBuffer = null;

	/** The active buffer logic name */
	private String sName = null;
	
	/** Creates an empty active buffer.
	 * 
	 * @param sName The active buffer logic name
	 * @param iCapacity The maximum allowable capacity for the active buffer
	 *
	 */
	public ActiveBuffer(String sName, int iCapacity) {
		this.sName          = sName;
		this.wcConsumer     = new HashSet<WrappedComponent>();
		this.queueBuffer    = new ConcurrentLinkedQueue<Object>();
		this.semCapacity    = new Semaphore(iCapacity,true); // Important to add fairness condition
	}
	
	/** Gets the active buffer logic name.
	 * 
	 * @return The name of the active buffer
	 */
	public String getName () {
		return sName;
	}
	
	/** Sets the WrappedComponent which consumes out of the pipe.
	 * 
	 * @param wc The wrapped component 
	 */
	public void addConsumer ( WrappedComponent wc ) {
		synchronized ( wcConsumer ) {
			wcConsumer.add(wc) ;
		}
	}
	
	/** Removes a WrappedComponent as a consumer for the given ActiveBuffer.
	 * 
	 * @param wc The wrapped component 
	 */
	public void removeConsumer ( WrappedComponent wc ) {
		synchronized ( wcConsumer ) {
			wcConsumer.remove(wc);
		}
	}
	
	/** Gets a data component out of the active buffer. If the queue
	 * is empty, it returns null.
	 * 
	 * @return The retrieved object.
	 */
	public Object popDataComponent () {
		Object objRes = null;
		
		objRes = queueBuffer.poll();
		semCapacity.release();
		
		return objRes;
	}
	
	/** Pushes a data component to the end of the active buffer queue.
	 * 
	 * @param queueObjects The object to push to the active buffer
	 * @throws ActiveBufferException An exception ocurred while aquiring the capacity semaphore
	 */
	@SuppressWarnings("unchecked")
	public synchronized void pushDataComponent ( Object obj) throws ActiveBufferException{
		try {
			semCapacity.acquire();
			queueBuffer.offer(obj);
			// Waking up the consumers
			for ( WrappedComponent wc:wcConsumer )
				wc.awake(sName);
			
		} catch (InterruptedException e) {
			throw new ActiveBufferException("Capacity semaphore acquire interrupted",e);
		}
		
	}
	
	/** Pushes queue of data component to the end of the active buffer queue.
	 * 
	 * @param queueObjects The objects to push to the active buffer
	 * @throws ActiveBufferException An exception ocurred while aquiring the capacity semaphore
	 */
	@SuppressWarnings("unchecked")
	public synchronized void pushDataComponent ( Queue queueObjects ) throws ActiveBufferException{
		try {
			// Queueing the objects
			while ( !queueObjects.isEmpty() ) {
				semCapacity.acquire();
				queueBuffer.offer((WrappedComponent) queueObjects.poll());
			}
			// Waking up the consumers
			for ( WrappedComponent wc:wcConsumer )
				wc.awake(sName);
			
		} catch (InterruptedException e) {
			throw new ActiveBufferException("Capacity semaphore acquire interrupted",e);
		}
		
	}

	/** Check if the buffer is empty.
	 * 
	 * @return True is no data component is in the buffer
	 */
	public boolean isEmpty() {
		return queueBuffer.isEmpty();
	}
	
	
}
