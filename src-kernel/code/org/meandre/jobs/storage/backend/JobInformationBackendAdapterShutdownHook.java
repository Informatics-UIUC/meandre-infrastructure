package org.meandre.jobs.storage.backend;

import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;


/** This class implements a shutdown hook for the backend adapter. This hook
 * is intended to be able to clean up the server status when the server goes
 * down.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class JobInformationBackendAdapterShutdownHook 
extends Thread {

	/** The backend adapter assocciated to the shutdown hook */
	private JobInformationBackendAdapter ba = null;
	
	/** The logger to use */
	private Logger log = KernelLoggerFactory.getCoreLogger();

	/** Creates a shutdown hook for a backend adapter.
	 * 
	 * @param ba The backend adapter to clean
	 */
	public JobInformationBackendAdapterShutdownHook ( JobInformationBackendAdapter ba ) {
		this.ba = ba;
	}
	
	/** The cleaning code for the shutdown hook.
	 * 
	 */
	public void run () {
		String sServerID = ba.getServerID();
		ba.updateJobStatusInServer(
				sServerID, 
				JobInformationBackendAdapter.JOB_STATUS_RUNNING, 
				JobInformationBackendAdapter.JOB_STATUS_KILLED)
			;
		ba.close();
		log.info("Shutdown hook for "+sServerID+" exiting");
	}
}
