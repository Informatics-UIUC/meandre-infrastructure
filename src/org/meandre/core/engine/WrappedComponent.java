package org.meandre.core.engine;

import java.util.Hashtable;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.Bootstrapper;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextImpl;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

/** This class is the Meandre implementation wrapper of an 
 * ExecutableComponent. This class implements the basic execution
 * mechanism of a component.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class WrappedComponent 
extends Thread {

	/** The running flag index */
	protected static final int RUNNING     = 0;

	/** The executing flag index */
	protected static final int EXECUTING   = 1;

	/** The termination flag index */
	protected static final int SLEEPING    = 2;
	
	/** The termination flag index */
	protected static final int TERMINATION = 3;
	
	/** The logger for the bootstrapper */
	protected static Logger log = null;
	
	// Initializing the logger and its handlers
	static {
		log = Logger.getLogger(Bootstrapper.class.getName());
		log.setLevel(Level.CONFIG);
		log.addHandler(Bootstrapper.handler);
	}
	
	/** The last updated input buffer */
	@SuppressWarnings("unchecked")
	private Queue qUpdatedActiveBuffer = null;
	
	/** Wrapped component status flags */
	protected boolean [] baStatusFlags = null;
	
	/** The semaphore use to control execution flow */
	protected Semaphore semBlocking = null;
	
	/** The input active buffers */
	private Hashtable<String,ActiveBuffer> htInputs = null;
	
	/** The output active buffers */
	private Hashtable<String,ActiveBuffer> htOutputs = null;
	
	/** The map of output names to the real active buffer name */
	@SuppressWarnings("unused")
	private Hashtable<String, String> htOutputMap = null;
	
	/** The executable component wrapped */
	protected ExecutableComponent ec = null;

	/** The component context object */
	protected ComponentContextImpl cc = null;

	/** The MrProper instance for this guy */
	protected MrProper thdMrProper = null;

	/** Has the component inputs? */
	protected int hasNInputs = -1;

	/** Abortion message thrown */
	protected String sAbortMessage = null;
	
	/** Builds a runnable component wrapper given the abstracted EcecutableComponent.
	 * 
	 * @param sFlowUniqueID A flow execution unique ID
	 * @param sComponentInstanceID The component instance ID
	 * @param ec The executable component to wrap
	 * @param setInputs The input active buffers
	 * @param setOutputs The output active buffers
	 * @param htOutputMap The output identifier map
	 * @param htOutputLogicNameMap The input logic name map
	 * @param htInputLogicNameMap The output logc name map
	 * @param tg The thread group holding the thread
	 * @param sThreadName The name of the thread
	 * @param htProperties The component properties
	 * @throws InterruptedException The semaphore could not be adquired twice
	 */
	@SuppressWarnings("unchecked")
	public WrappedComponent(String sFlowUniqueID, String sComponentInstanceID,
			ExecutableComponent ec, Set<ActiveBuffer> setInputs,
			Set<ActiveBuffer> setOutputs,
			Hashtable<String, String> htOutputMap,
			Hashtable<String, String> htInputLogicNameMap,
			Hashtable<String, String> htOutputLogicNameMap, ThreadGroup tg,
			String sThreadName, Hashtable<String, String> htProperties)
			throws InterruptedException {
		super(tg,sThreadName);
		
		// Basic initialization
		this.ec            = ec;
		this.semBlocking   = new Semaphore(1,true); // With fairness
		this.cc            = new ComponentContextImpl(sFlowUniqueID, sComponentInstanceID, setInputs, setOutputs, htOutputMap, htInputLogicNameMap, htOutputLogicNameMap, htProperties);
		this.hasNInputs     = htInputLogicNameMap.size(); 
		// Setting execution flags
		this.baStatusFlags = new boolean [4];
		
		this.baStatusFlags[RUNNING]     = true;
		this.baStatusFlags[EXECUTING]   = false;
		this.baStatusFlags[SLEEPING]    = false;
		this.baStatusFlags[TERMINATION] = false;
		
		// Cleaning Mr Proper object
		this.thdMrProper         = null;
		
		// Create hash tables for input and output active buffers
		this.htInputs = new Hashtable<String,ActiveBuffer>();
		for (ActiveBuffer ab:setInputs) {
			// Setting the input metadata set
			this.htInputs.put(ab.getName(),ab);
			// Registering me as a consumer
			ab.addConsumer(this);
		}
		
		this.htOutputs = new Hashtable<String,ActiveBuffer>();
		for (ActiveBuffer ab:setOutputs)
			this.htOutputs.put(ab.getName(),ab);
		
		this.htOutputMap = htOutputMap;
		
		// Clean the last updated active buffer
		this.qUpdatedActiveBuffer = new ConcurrentLinkedQueue();
		
		// Waste the only ticket to the blocking semaphore
		this.semBlocking.acquire();
		
		// Initialize the executable component
		this.ec.initialize();
	}
	
	
	/** Implements the basic machinery to execute the wrapped component
	 * 
	 */ 
	public void run () {
		log.info("Initializing a the wrapping component "+ec.toString());
		
		while ( baStatusFlags[RUNNING] && !baStatusFlags[TERMINATION]) {
			if ( isExecutable() ) {
				// The executable component is ready for execution
				log.finest("Component "+ec.toString()+" ready for execution");
				try {
					// Execute
					synchronized (baStatusFlags) {
						baStatusFlags[EXECUTING] = true;
					}
					ec.execute(cc);
					synchronized (baStatusFlags) {
						baStatusFlags[EXECUTING] = false;
					}
					log.finest("Component "+ec.toString()+" executed");
					
					log.finest("Component "+ec.toString()+" outputs pushed");
					// Clean the data proxy
					updateComponentContext();
					log.finest("Component "+ec.toString()+" outputs pushed");
				} catch (ComponentExecutionException e) {
					synchronized (baStatusFlags) {
						baStatusFlags[TERMINATION] = true;
						sAbortMessage = e.toString();
					}
					log.warning(e.getMessage());
				} catch (ComponentContextException e) {
					synchronized (baStatusFlags) {
						baStatusFlags[TERMINATION] = true;
						sAbortMessage = e.toString();
					}
					log.warning(e.getMessage());
				} 
				catch (Exception e) {
					synchronized (baStatusFlags) {
						baStatusFlags[TERMINATION] = true;
						sAbortMessage = e.toString();
					}
					log.warning(e.toString());
				} 
			}
			else {
				// The executable component is not ready for execution
				log.finest("Component "+ec.toString()+" not ready for execution");
				try {
					// Should we proceed?
					synchronized (baStatusFlags) {
						baStatusFlags[SLEEPING] = true;
					}
					thdMrProper.awake();
					semBlocking.acquire();
					synchronized (baStatusFlags) {
						baStatusFlags[SLEEPING] = false;
					}
					
					// Check if termination is requested
					if ( baStatusFlags[TERMINATION] || !baStatusFlags[RUNNING] )
						continue;
					log.finest("Component "+ec.toString()+" awakened by data push");
					// Retrieve the added element to the data proxy
					String sLastUpdated = qUpdatedActiveBuffer.poll().toString();
					cc.setDataComponentToInput(
							sLastUpdated, 
							htInputs.get(sLastUpdated).popDataComponent()
						);	
					log.finest("Component "+ec.toString()+" populated input "+qUpdatedActiveBuffer);
				} catch (InterruptedException e) {
					synchronized (baStatusFlags) {
						baStatusFlags[TERMINATION] = true;
						sAbortMessage = e.toString();
					}
					log.warning("The blocking sempahore for "+ec.toString()+" was interrupted!");
				} catch (ComponentContextException e) {
					synchronized (baStatusFlags) {
						baStatusFlags[TERMINATION] = true;
						sAbortMessage = e.toString();
					}
					log.severe("The requested input does not exist "+e.toString());
				}
				
			}
			thdMrProper.awake();
		}
		log.info("Disposing WebUI if any." );
		cc.stopAllWebUIFragments();
		log.info("Finalizing the execution of the wrapping component "+ec.toString());		
		ec.dispose();
		
	}

	/** After flushing the outputed data components, this method rebuilds the
	 * component content, populating available inputs.
	 *
	 */
	private void updateComponentContext() {
		// Reset the data proxy
		cc.resetDataProxy();
		
		// Checking if new elements are available to populate the proxy
		try {
			for ( String sInput:htInputs.keySet() ) {
				Object obj = htInputs.get(sInput).popDataComponent();
				cc.setDataComponentToInput(sInput,obj);
			} 
		}
		catch (ComponentContextException e) {
			synchronized (baStatusFlags) {
				baStatusFlags[TERMINATION] = true;
			}
			log.severe("The requested input does not exist "+e.toString());
		}
	}

	/** Awakes the modules to the arrival of a new data.
	 * 
	 * @param sInput The updated input active buffer
	 */
	@SuppressWarnings("unchecked")
	public void awake(String sInput) {
		if ( sInput != null )
			qUpdatedActiveBuffer.offer(sInput);
		semBlocking.release();
	}
	
	/** Awakes the modules due a termination request.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void awake() {
		semBlocking.release();
	}

	/** Check if the input active buffers are empty.
	 * 
	 * @return True if no data component is available in the active buffers
	 */
	public boolean emptyInputs () {
		for ( String sInput:htInputs.keySet() )
			if ( !htInputs.get(sInput).isEmpty() )
				return false;
		
		return true;
	}
	
	/** The wrapped component is ready for execution.
	 * 
	 * @return A boolean True it the wrapped component can be executed
	 */
	protected abstract boolean isExecutable();


	/** Returns the current termination criteria. 
	 * 
	 * @return The current termination criteria
	 */
	public boolean hadGracefullTermination () {
		boolean bFlag = false;
		
		synchronized (baStatusFlags) {
			bFlag = baStatusFlags[TERMINATION]==false;
		}
		
		return bFlag;
	}
	
	/** Returns the aborting message if any. If there was a gracefull
	 * termination the call returns null.
	 * 
	 * @return The abort message
	 */
	public String getAbortMessage () {
		return sAbortMessage;
	}
	
}
