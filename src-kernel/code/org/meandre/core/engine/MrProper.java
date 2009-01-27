package org.meandre.core.engine;

import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;


/** This thread monitors finalization criteria of the wrapped component
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class MrProper extends Thread {
	
	/** The core root logger */
	protected static Logger log = KernelLoggerFactory.getCoreLogger();

	/** The thread group of the components. */
	private ThreadGroup tg = null;
	
	/** The set of wrapped compnents */
	private Set<? extends WrappedComponent> setWC = null;

	/** The basic semaphore to control component execution */
	private Semaphore semBlocking = null;
	
	/** The flag to terminate execution of MrProper */
	private boolean bNotDone = true;

	/** Creates a monitor to the given Meandre Flow.
	 * 
	 * @param tg The thread group
	 * @param setWC The set of wrapped components to monitor
	 * @throws InterruptedException The controlling flow semaphore was interrupted
	 */
	public MrProper(ThreadGroup tg, Set<? extends WrappedComponent> setWC) throws InterruptedException {
		super(tg,tg.getName()+"mr-proper");
		
		this.bNotDone    = false;
		this.tg          = tg;
		this.setWC       = setWC;
		this.semBlocking = new Semaphore(1,true);
		// Prepare to block the excecution
		this.semBlocking.acquire();
	}

	/** Implements the basic machinery to execute the wrapped component
	 * 
	 */ 
	public void run () {
		
		log.fine("Starting MrProper for "+tg.getName()+" flow." );
		do {
			try {
				// Check if cleaning needed
				semBlocking.acquire();
				// Put it to sleep for a second to mitigate live cycling with data-intensive short computation flows
				Thread.sleep(1000);
				// Check for termination flag
				bNotDone = !checkTerminationFlag();
				// Check for components not running
				bNotDone = ( bNotDone )? !checkComponentsNotRunning() : bNotDone;
				
			} catch (InterruptedException e) {
				log.warning("MrProper was interrupted for flow "+tg.getName());
			}
		}
		while ( bNotDone );
	}
	
	/** Check if any of the wrapped components arised a termination flag.
	 *
	 * @return True if should be terminated
	 */
	private boolean checkTerminationFlag() {
		boolean bStop = false;
		String  sName = null;
		
		// Check if forced termination is required
		for ( WrappedComponent wc:setWC ) 
			synchronized ( wc.baStatusFlags ) {
				if ( wc.baStatusFlags[WrappedComponent.TERMINATION] ) {
					bStop = true;
					sName = wc.getName();
					break;
				}
			}
		
		// Propagate termination
		if ( bStop )  {
			log.severe("Aborting execution. Component "+sName+" requested execution termination." );
			for ( WrappedComponent wc:setWC ) {
				synchronized (wc.baStatusFlags) {
					wc.baStatusFlags[WrappedComponent.TERMINATION] = true;
				}
				wc.awake();
			}
			log.severe("Aborting execution. Requested execution termination propagated." );
			log.severe("Disposing WebUI." );
			setWC.iterator().next().cc.stopAllWebUIFragments();
			
		}
		
		return bStop;
	}

	/** Check that no data is left and all components are sleeping.
	 * 
	 * @return True is no work is left
	 */
	private boolean checkComponentsNotRunning() {
		boolean bStop = true;
		
		// Check if active buffers are empty
		for ( WrappedComponent wc:setWC ) 
			synchronized ( wc.baStatusFlags ) {
				if ( !wc.emptyInputs() || !wc.baStatusFlags[WrappedComponent.SLEEPING] ) {
					bStop = false;
					break;
				}
			}
		
		// Propagate termination
		if ( bStop )  {
			log.fine("No data available, no component executing, requesting graceful finalization" );
			for ( WrappedComponent wc:setWC ) {
				synchronized (wc.baStatusFlags) {
					wc.baStatusFlags[WrappedComponent.RUNNING] = false;
				}
				wc.awake();
			}
			log.fine("Requested graceful finalization propagated." );
			log.info("Finishing flow "+tg.getName());
		}
		return bStop;
	}

	/** Instructs MrProper to clean the Meandre Flow
	 * 
	 */
	public void awake () {
		semBlocking.release();
	}

	/** Request abort termination to the components.
	 * 
	 */
	public void abort() {
		log.warning("Abort execution requested" );
		for ( WrappedComponent wc:setWC ) {
			synchronized (wc.baStatusFlags) {
				wc.baStatusFlags[WrappedComponent.TERMINATION] = true;
			}
			wc.awake();
		}
		log.warning("Abort execution requested propagated to the executable components." );
		// Set MrPropper to finish and release him if slept
		bNotDone = false;
		semBlocking.release();
	}

}
