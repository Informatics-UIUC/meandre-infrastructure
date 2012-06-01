package org.meandre.core.engine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextImpl;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.engine.policies.component.availability.WrappedComponentAnyInputRequired;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.utils.ExceptionFormatter;

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

	/** The core root logger */
	protected transient static Logger log = KernelLoggerFactory.getCoreLogger();

	/** Wrapped component status flags */
	protected transient boolean [] baStatusFlags = null;

	/** The semaphore use to control execution flow */
	protected transient Semaphore semBlocking = null;

	/** The input active buffers */
	private transient Hashtable<String,ActiveBuffer> htInputs = null;

	/** The output active buffers */
	private transient Hashtable<String,ActiveBuffer> htOutputs = null;

	/** The map of output names to the real active buffer name */
	@SuppressWarnings("unused")
	private transient Hashtable<String, String> htOutputMap = null;

	/** The executable component wrapped */
	protected ExecutableComponent ec = null;

	/** The component context object */
	protected transient ComponentContextImpl cc = null;

	/** The MrProper instance for this guy */
	protected transient MrProper thdMrProper = null;

	/** Has the component inputs? */
	protected transient int hasNInputs = -1;

	/** Abortion message thrown */
	protected transient String sAbortMessage = null;

	/** MrProbe instance */
	protected transient MrProbe thdMrProbe;


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
	 * @param thdMrProbe The MrProbe thread
	 * @param cnf The core configuration object
	 * @throws InterruptedException The semaphore could not be adquired twice
	 */
	public WrappedComponent(String sFlowUniqueID, String flowID,String sComponentInstanceID, String sComponentInstanceName,
			ExecutableComponent ec, Set<ActiveBuffer> setInputs,
			Set<ActiveBuffer> setOutputs,
			Hashtable<String, String> htOutputMap,
			Hashtable<String, String> htInputLogicNameMap,
			Hashtable<String, String> htOutputLogicNameMap, ThreadGroup tg,
			String sThreadName, Hashtable<String, String> htProperties, MrProbe thdMrProbe,
			CoreConfiguration cnf,
			PrintStream console,
			Properties flowParams)
	throws InterruptedException {
		super(tg,sThreadName);

		// Basic initialization
		this.ec            = ec;
		this.semBlocking   = new Semaphore(1,true); // With fairness
		this.cc            = new ComponentContextImpl(sFlowUniqueID,flowID,sComponentInstanceID, sComponentInstanceName, setInputs, setOutputs, htOutputMap, htInputLogicNameMap, htOutputLogicNameMap, htProperties, thdMrProbe, this, cnf, console, flowParams);
		this.hasNInputs     = htInputLogicNameMap.size();
		// Setting execution flags
		this.baStatusFlags = new boolean [4];

		this.baStatusFlags[RUNNING]     = true;
		this.baStatusFlags[EXECUTING]   = false;
		this.baStatusFlags[SLEEPING]    = false;
		this.baStatusFlags[TERMINATION] = false;

		// Cleaning Mr Proper object
		this.thdMrProper         = null;
		this.thdMrProbe     	 = thdMrProbe;

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

		// Waste the only ticket to the blocking semaphore
		this.semBlocking.acquire();
	}


	/** Implements the basic machinery to execute the wrapped component
	 *
	 */
	@Override
    public void run () {
		log.fine("Initializing a the wrapping component "+ec.toString());

		// Initialize the executable component
		try {
			this.ec.initialize(cc);
			this.thdMrProbe.probeWrappedComponentInitialize(this);
		} catch (ComponentExecutionException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace(cc.getOutputConsole());
			synchronized (baStatusFlags) {
				baStatusFlags[TERMINATION] = true;
				sAbortMessage = ExceptionFormatter.formatException(e);
				this.thdMrProbe.probeWrappedComponentAbort(this);
			}
		} catch (ComponentContextException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace(cc.getOutputConsole());
			synchronized (baStatusFlags) {
				baStatusFlags[TERMINATION] = true;
				sAbortMessage = ExceptionFormatter.formatException(e);
				this.thdMrProbe.probeWrappedComponentAbort(this);
			}
		}

		// Main loop
		try {
			while ( baStatusFlags[RUNNING] && !baStatusFlags[TERMINATION]) {

				if ( isExecutable() ) {

					// The executable component is ready for execution
					//log.finest("Component "+ec.toString()+" ready for execution");
					try {
						// Execute
						synchronized (baStatusFlags) {
							baStatusFlags[EXECUTING] = true;
						}
						try {
							this.thdMrProbe.probeWrappedComponentFired(this);
							ec.execute(cc);
							this.thdMrProbe.probeWrappedComponentCoolingDown(this);
						}
						catch ( NoClassDefFoundError ncde ) {
							synchronized (baStatusFlags) {
								baStatusFlags[EXECUTING] = false;
							}
							throw new ComponentExecutionException ( ncde );
						}
						finally {
						    // Reset the data proxy
					        cc.resetDataProxy();
						}
						synchronized (baStatusFlags) {
							baStatusFlags[EXECUTING] = false;
						}

						//log.finest("Component "+ec.toString()+" executed");

						// Clean the data proxy
						updateComponentContext();
						//log.finest("Component "+ec.toString()+" context updated");
					} catch (ComponentExecutionException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        e.printStackTrace(cc.getOutputConsole());
						synchronized (baStatusFlags) {
							baStatusFlags[TERMINATION] = true;
							sAbortMessage = ExceptionFormatter.formatException(e);
							this.thdMrProbe.probeWrappedComponentAbort(this);
						}
					} catch (ComponentContextException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        e.printStackTrace(cc.getOutputConsole());
						synchronized (baStatusFlags) {
							baStatusFlags[TERMINATION] = true;
							sAbortMessage = ExceptionFormatter.formatException(e);
							this.thdMrProbe.probeWrappedComponentAbort(this);
						}
					}
					catch (Exception e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        e.printStackTrace(cc.getOutputConsole());
						synchronized (baStatusFlags) {
							baStatusFlags[TERMINATION] = true;
							sAbortMessage = ExceptionFormatter.formatException(e);
							this.thdMrProbe.probeWrappedComponentAbort(this);
						}
					}
				}
				else {

					// The executable component is not ready for execution
					//log.finest("Component "+ec.toString()+" not ready for execution");
					try {
						// Should we proceed?
						synchronized (baStatusFlags) {
							baStatusFlags[SLEEPING]  = true;
						}
						thdMrProper.awake();
						semBlocking.acquire();
						synchronized (baStatusFlags) {
							baStatusFlags[SLEEPING] = false;
						}

						// Check if termination is requested
						if ( baStatusFlags[TERMINATION] || !baStatusFlags[RUNNING] )
							continue;
						//log.finest("Component "+ec.toString()+" awakened by data push");

						//String sLastUpdated = partialUpdateComponentContext();
						partialUpdateComponentContext();

						//log.finest("Component "+ec.toString()+" populated input "+qUpdatedActiveBuffer);
					} catch (InterruptedException e) {
						synchronized (baStatusFlags) {
							baStatusFlags[TERMINATION] = true;
							sAbortMessage = ExceptionFormatter.formatException(e);
							this.thdMrProbe.probeWrappedComponentAbort(this);
						}
						log.warning("The blocking sempahore for "+ec.toString()+" was interrupted!");
					}
					catch (ComponentContextException e) {
						synchronized (baStatusFlags) {
							baStatusFlags[TERMINATION] = true;
							sAbortMessage = ExceptionFormatter.formatException(e);
							this.thdMrProbe.probeWrappedComponentAbort(this);
						}
						log.severe("The requested input does not exist "+e.toString());
					}

				}
				//thdMrProper.awake();
			}
		}
		catch (Throwable t ) {
			// This should not have happened
            log.log(Level.SEVERE, t.getMessage(), t);
            t.printStackTrace(cc.getOutputConsole());
			synchronized (baStatusFlags) {
				baStatusFlags[TERMINATION] = true;
				sAbortMessage = ExceptionFormatter.formatException(new Exception(t));
				this.thdMrProbe.probeWrappedComponentAbort(this);
			}
			this.thdMrProbe.probeWrappedComponentAbort(this);
		}

		log.finer("Disposing WebUI if any." );
		cc.stopAllWebUIFragments();
		log.fine("Finalizing the execution of the wrapping component "+ec.toString());
		try {
			ec.dispose(cc);
		} catch (ComponentExecutionException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace(cc.getOutputConsole());
			synchronized (baStatusFlags) {
				baStatusFlags[TERMINATION] = true;
				sAbortMessage = ExceptionFormatter.formatException(e);
				this.thdMrProbe.probeWrappedComponentAbort(this);
			}
		} catch (ComponentContextException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace(cc.getOutputConsole());
			synchronized (baStatusFlags) {
				baStatusFlags[TERMINATION] = true;
				sAbortMessage = ExceptionFormatter.formatException(e);
				this.thdMrProbe.probeWrappedComponentAbort(this);
			}
		}
		catch (Throwable t) {
            log.log(Level.SEVERE, t.getMessage(), t);
            t.printStackTrace(cc.getOutputConsole());
			synchronized (baStatusFlags) {
				baStatusFlags[TERMINATION] = true;
				sAbortMessage = ExceptionFormatter.formatException(new Exception(t));
				this.thdMrProbe.probeWrappedComponentAbort(this);
			}
		}
		this.thdMrProbe.probeWrappedComponentDispose(this);
	}



	/** After flushing the outputed data components, this method rebuilds the
	 * component content, populating available inputs.
	 *
	 */
	private void updateComponentContext() {
		// Checking if new elements are available to populate the proxy
		try {
			for ( String sInput:htInputs.keySet() ) {
				Object obj = htInputs.get(sInput).popDataComponent();
				cc.setDataComponentToInput(sInput,obj);
				// TODO: This is not efficient, but needed for now to prevent problems with Streaming in the abstracts (SI and ST arriving at same time)
				if (this instanceof WrappedComponentAnyInputRequired && obj != null)
				    break;  // Load only 1 data input into the DataProxy even if multiple are available
			}
		}
		catch (ComponentContextException e) {
			synchronized (baStatusFlags) {
				baStatusFlags[TERMINATION] = true;
			}
			log.severe("The requested input does not exist "+e.toString());
		}
	}

	/** Keeps updating a partially populated component context.
	 *
	 * @throws ComponentContextException Something went really wrong
	 */
	private void partialUpdateComponentContext()
	throws ComponentContextException {
		// Retrieve the added element to the data proxy
		try {
			boolean bNotDone = true;
			String[] saIN =  new String[htInputs.size()];
			saIN = htInputs.keySet().toArray(saIN);
			Hashtable<String, String> htInputLogicNameMapReverse = cc.getInputLogicNameMapReverse();
			for ( int i=0,iMax=saIN.length ; bNotDone && i<iMax ; i++ ) {
				String sIN = saIN[i];
				if ( cc.getDataComponentFromInput(htInputLogicNameMapReverse.get(sIN))==null )
					if ( !this.htInputs.get(sIN).isEmpty() ) {
						cc.setDataComponentToInput(
								sIN,
								htInputs.get(sIN).popDataComponent()
						);
						bNotDone = false;
					}
			}
		}
		catch ( Exception e ) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.warning("Something went really wrong in "+this.ec.getClass().getName()+"\n" +baos.toString());
		}


		//		String sLastUpdated = qUpdatedActiveBuffer.poll().toString();
		//
		//		cc.setDataComponentToInput(
		//				sLastUpdated,
		//				htInputs.get(sLastUpdated).popDataComponent()
		//			);

	}

	/** Awakes the modules to the arrival of a new data.
	 *
	 * @param sInput The updated input active buffer
	 */
	public void awake(String sInput) {
		semBlocking.release();
	}

	/** Awakes the modules due a termination request.
	 *
	 */
	public void awake() {
		semBlocking.release();
	}

	/** Check if the input active buffers are empty.
	 *
	 * @return True if no data component is available in the active buffers
	 */
	public boolean emptyInputs () {
		for ( String sInput:htInputs.keySet() )
			if ( !htInputs.get(sInput).isEmpty()  )
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

	/** Sets MrProper.
	 *
	 * @param thdMrPropper MrPropper thread
	 */
	public void setMrPropper(MrProper thdMrPropper) {
		this.thdMrProper = thdMrPropper;
		this.cc.setMrPropper(thdMrPropper);
	}

	/** Returns the unique ID of this executable instance.
	 *
	 * @return The unique ID
	 */
	public String getExecutableComponentInstanceID () {
		return cc.getExecutionInstanceID();
	}

	/** Returns the executable component wrapped.
	 *
	 * @return The executable component
	 */
	public ExecutableComponent getExecutableComponentImplementation() {
		return ec;
	}


	/** Returns the MrProper for this wraped component.
	 *
	 * @return MrProbe
	 */
	public MrProper getMrProper() {
		return thdMrProper;
	}

	/** Returns the MrProbe for this wraped component.
	 *
	 * @return MrProbe
	 */
	public MrProbe getMrProbe() {
		return thdMrProbe;
	}

	/** Returns true if termination flag is on.
	 *
	 * @return The value of the termination flag
	 */
	public boolean isTerminating() {
		return baStatusFlags[TERMINATION];
	}
}
