package org.meandre.core.engine;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.meandre.core.ExecutableComponent;
import org.meandre.core.environments.python.jython.JythonExecutableComponentAdapter;
import org.python.core.PyObject;

import com.thoughtworks.xstream.XStream;

/** This class is the one in charge of keeping track of what is going on/happen during
 * the execution of a flow. It performs logging/provenance based on the provided interface
 * implementation provide during the construction of MrProbe.
 * 
 * DISCLAIMER: After discussing with Amit Kumar I finally discarded the "MrPippingTom" name
 * for its negative connotation and run with the probe (a little bit more neutral, not great,
 * but a little better). 
 * 
 * @author Xavier Llor&agrave;
 * @param <WrappedComponent>
 *
 */
public class MrProbe 
extends Thread {
	
	/** The concurrent queue buffering the probing statements. */
	protected ConcurrentLinkedQueue<Object[]> clqStatements = null;
	
	/** The work synchronization semaphore. */
	protected Semaphore semWorkAvailable = null;

	/** The logger to use. */
	private Logger log = null;
	
	/** The probe implementation object */
	private Probe probe = null;
	
	/** The finalization flag */
	private boolean bDone = false;

	/** Should the data be serialized? */
	private boolean bDataSerialization = false;

	/** Should the state be serialized? */
	private boolean bStateSerialization = false;
	
	/** The XStream serialization object */
	private XStream xstream = null;

	/** Initialize MrProper.
	 * 
	 * @param logger The logger to use 
	 * @param probe The probe to use
	 * @param bDataSerialization Serializes the data in the Probe calls
	 * @param bStateSerialization Serializes the component state in the Probe calls
	 */
	public MrProbe(Logger logger, Probe probe, boolean bDataSerialization, boolean bStateSerialization) {
		this.bDone = false;
		this.log = logger;
		this.probe = probe;
		this.bDataSerialization = bDataSerialization;
		this.bStateSerialization = bStateSerialization;
		this.semWorkAvailable = new Semaphore(1,true);
		this.clqStatements = new ConcurrentLinkedQueue<Object[]>();
		this.xstream = new XStream();
		
		try {
			this.semWorkAvailable.acquire();
		} catch (InterruptedException e) {
			this.log.warning("Could not adquire "+MrProbe.class.getName()+" semaphore");
		}
	}
	
	/** The main thread run method.
	 * 
	 */
	public void run () {
		do {
			// Try to acquire another one
			try {
				this.semWorkAvailable.acquire();
			} catch (InterruptedException e) {
				this.log.warning("Could not adquire "+MrProbe.class.getName()+" semaphore");
			}
			// Process the next element
			Object [] oa = clqStatements.poll();
			if ( oa!=null )
				processProbeCommand(oa);
			
		} while ( !bDone || ( bDone && !clqStatements.isEmpty()) );
	}


	/** Sets the done flag to signal that the thread should finish executing.
	 * 
	 */
	public void done() {
		bDone = true;
		semWorkAvailable.release();
	}
	
	/** The flow started executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowStart(String sFlowUniqueID){
		Object[] oa = {Probe.ProbeCommands.FLOW_STARTED,sFlowUniqueID,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	/** The flow stopped executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowFinish(String sFlowUniqueID){
		Object[] oa = {Probe.ProbeCommands.FLOW_FINISHED,sFlowUniqueID,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	/** The flow aborted the execution.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowAbort(String sFlowUniqueID){
		Object[] oa = {Probe.ProbeCommands.FLOW_ABORTED,sFlowUniqueID,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	/** The wrapped component was aborted.
	 * 
	 * @param wc The wrapped component
	 */
	public void probeWrappedComponentAbort ( WrappedComponent wc ) {
		Object oSWCXML = wc;
		
		// Check for state serialization
		if ( bStateSerialization ) 
			oSWCXML = serializeWrappedComponent(wc);
		
		Object[] oa = {Probe.ProbeCommands.EXECUTABLE_COMPONENT_ABORTED,wc.getExecutableComponentInstanceID(),oSWCXML,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	

	/** The wrapped component was initialized.
	 * 
	 * @param wc The wrapped component
	 */
	public void probeWrappedComponentInitialize ( WrappedComponent wc ) {
		Object oSWCXML = wc;
		
		// Check for state serialization
		if ( bStateSerialization ) 
			oSWCXML = serializeWrappedComponent(wc);
		
		Object[] oa = {Probe.ProbeCommands.EXECUTABLE_COMPONENT_INITIALIZED,wc.getExecutableComponentInstanceID(),oSWCXML,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	/** The wrapped component was disposed.
	 * 
	 * @param wc The wrapped component
	 */
	public void probeWrappedComponentDispose ( WrappedComponent wc ) {
		Object oSWCXML = wc;
		
		// Check for state serialization
		if ( bStateSerialization ) 
			oSWCXML = serializeWrappedComponent(wc);
		
		Object[] oa = {Probe.ProbeCommands.EXECUTABLE_COMPONENT_DISPOSED,wc.getExecutableComponentInstanceID(),oSWCXML,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	/** The wrapped component pushed some data.
	 * 
	 * @param wc The wrapped component
	 * @param sPortName The port name
	 * @param objData The pushed data
	 */
	public void probeWrappedComponentPushData ( WrappedComponent wc, String sPortName, Object objData ) {
		Object oSWCXML = wc;
		Object oSDataXML = objData;
		
		// Check for state serialization
		if ( bStateSerialization ) 
			oSWCXML = serializeWrappedComponent(wc);
		
		// Check for data serialization
		if ( bDataSerialization )
			oSDataXML = serializeDataPiece(objData);
		
		Object[] oa = {Probe.ProbeCommands.EXECUTABLE_COMPONENT_PUSH_DATA,wc.getExecutableComponentInstanceID(),oSWCXML,oSDataXML,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	/** The wrapped component poll some data.
	 * 
	 * @param wc The wrapped component
	 * @param sPortName The port name
	 * @param objData The pushed data
	 */
	public void probeWrappedComponentPullData ( WrappedComponent wc, String sPortName, Object objData ) {
		Object oSWCXML = wc;
		Object oSDataXML = objData;
		
		// Check for state serialization
		if ( bStateSerialization ) 
			oSWCXML = serializeWrappedComponent(wc);
		
		// Check for data serialization
		if ( bDataSerialization )
			oSDataXML = serializeDataPiece(objData);
		
		Object[] oa = {Probe.ProbeCommands.EXECUTABLE_COMPONENT_PULL_DATA,wc.getExecutableComponentInstanceID(),oSWCXML,oSDataXML,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	/** The wrapped component requested a property value.
	 * 
	 * @param wc The wrapped component
	 * @param sPropertyName The requested property
	 * @param sPropertyValue The requested value
	 */
	public void probeWrappedComponentGetProperty( WrappedComponent wc, String sPropertyName, String sPropertyValue ) {
		Object[] oa = {Probe.ProbeCommands.EXECUTABLE_COMPONENT_GET_PROPERTY,wc.getExecutableComponentInstanceID(),sPropertyName,sPropertyValue,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	
	/** The wrapped component was fired.
	 * 
	 * @param wc The wrapped component
	 */
	public void probeWrappedComponentFired ( WrappedComponent wc ) {
		Object oSWCXML = wc;
		
		// Check for state serialization
		if ( bStateSerialization ) 
			oSWCXML = serializeWrappedComponent(wc);
		
		Object[] oa = {Probe.ProbeCommands.EXECUTABLE_COMPONENT_FIRED,wc.getExecutableComponentInstanceID(),oSWCXML,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	
	/** The wrapped component is cooling down.
	 * 
	 * @param wc The wrapped component
	 */
	public void probeWrappedComponentCoolingDown ( WrappedComponent wc ) {
		Object oSWCXML = wc;
		
		// Check for state serialization
		if ( bStateSerialization ) 
			oSWCXML = serializeWrappedComponent(wc);
		
		Object[] oa = {Probe.ProbeCommands.EXECUTABLE_COMPONENT_COOLING_DOWN,wc.getExecutableComponentInstanceID(),oSWCXML,new Date()};
		clqStatements.add(oa);
		semWorkAvailable.release();
	}
	
	
	/** The executable component gets serialize to keep its state.
	 * 
	 * @param wc The Wrapped component to serialize
	 * @return The XML serialized object
	 */
	protected String serializeWrappedComponent ( WrappedComponent wc ) {
		String sRes = null;
	
		ExecutableComponent ec = wc.getExecutableComponentImplementation();
		if ( ec.getClass()==JythonExecutableComponentAdapter.class ) {
			JythonExecutableComponentAdapter jeca = (JythonExecutableComponentAdapter)wc.getExecutableComponentImplementation();
			PyObject pyobj = jeca.getLocals();
			sRes = pyobj.toString();
		}
		else
			sRes = xstream.toXML(ec);
		
		return sRes;
	}
	
	/** The data object to serialize .
	 * 
	 * @param obj The data piece to serialize
	 * @return The XML serialized object
	 */
	protected String serializeDataPiece ( Object obj ) {
		return xstream.toXML(obj);
	}


	/** Processes the probe command calling to the proper probe call on the 
	 * probe instantiation object.
	 * 
	 * @param oa The probe command to process
	 */
	protected void processProbeCommand(Object[] oa) {
		switch((Probe.ProbeCommands)oa[0]) {
			case FLOW_STARTED:
				probe.probeFlowStart((String)oa[1], (Date)oa[2]);
				break;
			case FLOW_FINISHED:
				probe.probeFlowFinish((String)oa[1], (Date)oa[2]);
				break;
			case FLOW_ABORTED:
				probe.probeFlowAbort((String)oa[1], (Date)oa[2]);
				break;
			case EXECUTABLE_COMPONENT_INITIALIZED:
				probe.probeExecutableComponentInitialized((String)oa[1],oa[2],(Date)oa[3],bStateSerialization);
				break;
			case EXECUTABLE_COMPONENT_ABORTED:
				probe.probeExecutableComponentAbort((String)oa[1],oa[2],(Date)oa[3],bStateSerialization);
				break;
			case EXECUTABLE_COMPONENT_DISPOSED:
				probe.probeExecutableComponentDisposed((String)oa[1],oa[2],(Date)oa[3],bStateSerialization);
				break;
			case EXECUTABLE_COMPONENT_PUSH_DATA:
				probe.probeExecutableComponentPushData((String)oa[1],oa[2],oa[3],(Date)oa[4],bStateSerialization,bDataSerialization);
				break;
			case EXECUTABLE_COMPONENT_PULL_DATA:
				probe.probeExecutableComponentPullData((String)oa[1],oa[2],oa[3],(Date)oa[4],bStateSerialization,bDataSerialization);
				break;
			case EXECUTABLE_COMPONENT_GET_PROPERTY:
				probe.probeExecutableComponentGetProperty((String)oa[1],(String)oa[2],(String)oa[3],(Date)oa[4]);
				break;
			case EXECUTABLE_COMPONENT_FIRED:
				probe.probeExecutableComponentFired((String)oa[1],oa[2],(Date)oa[3],bStateSerialization);
				break;
			case EXECUTABLE_COMPONENT_COOLING_DOWN:
				probe.probeExecutableComponentCoolingDown((String)oa[1],oa[2],(Date)oa[3],bStateSerialization);
				break;
			default:
				log.warning("Unknown probe command "+oa[0]);
				break;
		}
		
	}

}
