package org.meandre.core.engine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.utils.NetworkTools;
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
	
	/** The core root logger */
	protected static Logger log = KernelLoggerFactory.getCoreLogger();

	/** The thread group of the components. */
	private ThreadGroup tg = null;
	
	/** The set of wrapped compnents */
	private Set<? extends WrappedComponent> setWC = null;

	/** The monitoring and cleaning thread for this Meandre Flow */
	@SuppressWarnings("unused")
	private MrProper thdMrPropper = null;

	/** The flow unique execution ID */
	private String sFlowUniqueExecutionID = null;

	/** The core configuration object */
	private CoreConfiguration cnf;
	
	// /**Unique ID provided by the client*/
	// private String token;

	/** Constructs and executor based on the set of wrapped components.
	 * @param sFlowUniqueExecutionID The unique flow execution ID
	 * @param tg The group name
	 * @param setWC The set of wrapped components to use
	 * @param cnf The core configuration
	 * @throws InterruptedException The semaphore got interrupted when trying to block MrPropper
	 */
	public Executor ( String sFlowUniqueExecutionID, ThreadGroup tg , Set<? extends WrappedComponent> setWC, CoreConfiguration cnf ) throws InterruptedException {
		this.cnf = cnf;
		this.sFlowUniqueExecutionID = sFlowUniqueExecutionID;
		this.tg = tg;
		this.setWC = setWC;
		this.thdMrPropper = new MrProper(tg,setWC);
		for ( WrappedComponent wc:setWC )
			wc.setMrPropper(thdMrPropper);
	}
	
	/** Fires the execution of a given MeandreFlow.
	 * 
	 * @param iPriority The execution priority
	 */
	public void execute ( int iPriority, WebUI webui ) {
		// Setting up MrProbe info
		WrappedComponent wcTmp = setWC.iterator().next();
		MrProbe thdMrProbe = wcTmp.thdMrProbe;
		String sFlowExecutionID = wcTmp.cc.getFlowExecutionInstanceID();
		
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
		
		// MrProbe last words
		if ( hadGracefullTermination() ) 
			thdMrProbe.probeFlowFinish(sFlowExecutionID);
		else
			thdMrProbe.probeFlowAbort(sFlowExecutionID,concat(getAbortMessage()));
		
		thdMrProbe.done();
	}
	
	
	private String concat(Set<String> abortMessage) {
		StringBuffer sbuffer =new StringBuffer();
		sbuffer.append("Error Message:\n");
		if(abortMessage!=null){
			if(abortMessage.size()>0){
				Iterator<String> its = abortMessage.iterator();
				while(its.hasNext()){
				sbuffer.append(its.next()+"\n");
				}
			}
			
		}
		return sbuffer.toString();
	}

	/**Call this function to get the webui
	 * 
	 */
	public WebUI initWebUI(int nextPortForUse,String token){
		//this.token = token;
		WrappedComponent wcTmp = setWC.iterator().next();
		MrProbe thdMrProbe = wcTmp.thdMrProbe;
		String sFlowExecutionID = wcTmp.cc.getFlowExecutionInstanceID();
		
		// MrProbe start
		thdMrProbe.probeFlowStart(sFlowExecutionID,getHostWebUrl(nextPortForUse));
		
		WebUI webui = null;
		try {
			 webui = WebUIFactory.getWebUI(sFlowUniqueExecutionID,thdMrPropper,thdMrProbe,cnf,nextPortForUse);
		} catch (WebUIException e) {
			log.warning("WebUI could not be started: "+e.getMessage());
		}
		
		return webui;
	}
	
	/**This function is protocol dependent. When changing the application
	 * to Https -we need to modify this function
	 * 
	 * @param nextPortForUse
	 * @return
	 */
	private String getHostWebUrl(int nextPortForUse) {
			String sServer = NetworkTools.getLocalHostName();
			return "http://"+sServer+":"+nextPortForUse+"/";
	}
	
	/** Fires the execution of a given MeandreFlow.
	 * 
	 * 
	 */
	public void execute( WebUI webui) {
		execute(Thread.NORM_PRIORITY, webui);
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

	/** Returns the set of wrapped components.
	 * 
	 * @return The set of wrapped components
	 */
	public Set<? extends WrappedComponent> getWrappedComponents() {
		return setWC;
	}

	/**
	 * @return the FlowUniqueExecutionID
	 */
	public String getFlowUniqueExecutionID() {
		return sFlowUniqueExecutionID;
	}

}
