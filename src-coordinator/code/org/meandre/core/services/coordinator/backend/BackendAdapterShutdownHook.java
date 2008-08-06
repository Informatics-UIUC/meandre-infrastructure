package org.meandre.core.services.coordinator.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.services.coordinator.logger.CoordinatorLoggerFactory;


/** This class implements a shutdown hook for the backend adapter. This hook
 * is intended to be able to clean up the server status when the server goes
 * down.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class BackendAdapterShutdownHook 
extends Thread {

	/** The backend adapter assocciated to the shutdown hook */
	private BackendAdapter ba = null;

	/** Creates a shutdown hook for a backend adapter.
	 * 
	 * @param ba The backend adapter to clean
	 */
	public BackendAdapterShutdownHook ( BackendAdapter ba ) {
		this.ba = ba;
	}
	
	/** The cleaning code for the shutdown hook.
	 * 
	 */
	public void run () {
		try {
			CoordinatorLoggerFactory.getCoordinatorLogger().info("Shutdown hook for "+ba.getName()+" called");
			ba.close();
		} catch (BackendAdapterException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().warning(baos.toString());
		}
	}
}
