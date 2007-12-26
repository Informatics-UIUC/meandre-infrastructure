package org.meandre.core.engine;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.WSCoreBootstrapper;
import org.meandre.webui.WebUI;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFactory;

/** This class is the main execution engine of Meandre. Given a set of 
 * WrappedComponents describing a MeandreFlow, it executes the flow.
 * 
 * @author Xavier Llora
 *
 */
public class Executor {
	
	/** The logger for the bootstrapper */
	protected static Logger log = null;
	
	// Initializing the logger and its handlers
	static {
		log = Logger.getLogger(WSCoreBootstrapper.class.getName());
		log.setLevel(Level.CONFIG);
		log.addHandler(WSCoreBootstrapper.handler);
	}
	
	/** The thread group of the components. */
	private ThreadGroup tg = null;
	
	/** The set of wrapped compnents */
	private Set<? extends WrappedComponent> setWC = null;

	/** The monitoring and cleaning thread for this Meandre Flow */
	@SuppressWarnings("unused")
	private MrProper thdMrPropper = null;

	/** The flow unique execution ID */
	private String sFlowUniqueExecutionID = null;

	/** Constructs and executor based on the set of wrapped components.
	 * @param sFlowUniqueExecutionID The unique flow execution ID
	 * @param tg The group name
	 * @param setWC The set of wrapped components to use
	 * @throws InterruptedException The semaphore got interrupted when trying to block MrPropper
	 */
	public Executor ( String sFlowUniqueExecutionID, ThreadGroup tg , Set<? extends WrappedComponent> setWC ) throws InterruptedException {
		this.sFlowUniqueExecutionID = sFlowUniqueExecutionID;
		this.tg = tg;
		this.setWC = setWC;
		this.thdMrPropper = new MrProper(tg,setWC);
		for ( WrappedComponent wc:setWC )
			wc.thdMrProper = thdMrPropper;
	}
	
	/** Fires the execution of a given MeandreFlow.
	 * 
	 * @param iPriority The execution priority
	 */
	public void execute ( int iPriority ) {
		WebUI webui = null;
		try {
			 webui = WebUIFactory.getWebUI(sFlowUniqueExecutionID);
		} catch (WebUIException e) {
			log.warning("WebUI could not be started: "+e.getMessage());
		}
		
		tg.setMaxPriority(iPriority);
		for ( WrappedComponent wc:setWC )
			wc.start();
		thdMrPropper.start();
		try {
			for ( WrappedComponent wc:setWC )
				wc.join();
			thdMrPropper.join();
		} catch (InterruptedException e) {
			log.warning("Executor join failed: "+e.getMessage());
		}
		
		try {
			if ( webui!=null )
				WebUIFactory.disposeWebUI(sFlowUniqueExecutionID);
		} catch (WebUIException e) {
			log.warning("WebUI could not be stoped: "+e.getMessage());
		}
	}
	
	/** Fires the execution of a given MeandreFlow.
	 * 
	 * 
	 */
	public void execute () {
		execute(Thread.NORM_PRIORITY);
	}
	
	/** Returns the current termination criteria. 
	 * 
	 * @return The current termination criteria
	 */
	public boolean hadGracefullTermination () {
		boolean bFlag = true;
		
		for ( WrappedComponent wc:setWC )
			if ( !wc.hadGracefullTermination() )
				return false;
		
		return bFlag;
	}
	
	/** Returns the aborting message if any. If there was a gracefull
	 * termination the call returns null.
	 * 
	 * @return The abort message
	 */
	public Set<String> getAbortMessage () {
		String sMsg = null;
		Set<String> setRes = new HashSet<String>();
		
		for ( WrappedComponent wc:setWC )
			if ( (sMsg=wc.getAbortMessage())!=null )
				setRes.add(sMsg);
		
		return setRes;
	}


}
