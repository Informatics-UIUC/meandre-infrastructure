package org.meandre.jobs.storage.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
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
		try {
			if (!ba.conn.isClosed()) {
			String sServerID = ba.getServerID();
				ba.updateJobStatusInServer(
						JobInformationBackendAdapter.JOB_STATUS_RUNNING, 
						JobInformationBackendAdapter.JOB_STATUS_KILLED)
					;
				ba.conn.close();
				log.info("Shutdown hook for "+sServerID+" exiting");
			}
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Error closing the job information connection to the backend! "+baos.toString());
		}
	}
}
