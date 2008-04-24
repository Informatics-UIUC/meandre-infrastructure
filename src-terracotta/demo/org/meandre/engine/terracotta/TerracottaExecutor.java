package org.meandre.engine.terracotta;

import org.meandre.engine.terracotta.datatypes.FiringPulseQueue;
import org.meandre.engine.terracotta.datatypes.FiringRequest;

/** The basic class that implements the Terracotta execution engine.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class TerracottaExecutor 
implements Runnable {

	/** The available roles of the executor */
	protected static enum Role { MASTER, WORKER };
	
	/** The firing pulse queue */
	protected FiringPulseQueue fpq;
	
	/** The role of the terracota executor */
	protected Role role;
	
	/** Initialize the terracota executor object.
	 * 
	 */
	public TerracottaExecutor () {
		fpq = null;
	}
	
	/** Sets the firing pulse queue.
	 * 
	 * @param fpq The firing pulse queue to use.
	 */
	public void setFiringPulseQueue ( FiringPulseQueue fpq ) {
		this.fpq = fpq;
	}
	
	/** Sets the role of this terracota executor.
	 * 
	 * @param role The role of the executor
	 */
	public void setRole(Role role) {
		this.role = role;
	}
	
	/** The thread runnable method.
	 * 
	 */
	public void run() {
		if ( role==Role.MASTER ) 
			runMaster();
		else
			runWorker();
		
	}
	
	/** Turns the terracota executor into a master.
	 * 
	 */
	protected void runMaster() {
		for ( int i=0, iMax=100 ; i<iMax ; i++ )
			fpq.offer(new FiringRequest());
	}
	
	/** Turns the terracota executor into a worker.
	 * 
	 */
	protected void runWorker() {
		//while ( fpq.availableFiringRequests() );
			
	}

	/** The Terracotta executor entry point.
	 * 
	 * @param The commandline utilities
	 */
	public static void main ( String [] sArgs ) {
		FiringPulseQueue fpq = new FiringPulseQueue();

		System.out.println("Creating the master executor...");
		TerracottaExecutor teMaster = new TerracottaExecutor();
		teMaster.setFiringPulseQueue(fpq);
		teMaster.setRole(Role.MASTER);
		
		System.out.println("Creating the worke executors...");
		int iNumWorkers = 4;
		TerracottaExecutor[] teaWorkers = new TerracottaExecutor[iNumWorkers];
		for ( int i=0 ; i<iNumWorkers ; i++ ) {	
			teaWorkers[i] = new TerracottaExecutor();
			teaWorkers[i] .setFiringPulseQueue(fpq);
			teaWorkers[i] .setRole(Role.WORKER);
		}
		
		System.out.println("Starting the masterexecutor thread...");
		Thread thd = new Thread(teMaster);
		thd.start();
		System.out.println("Starting the masterexecutor thread...");
		Thread [] thda = new Thread[iNumWorkers];
		for ( int i=0 ; i<iNumWorkers ; i++ ) {	
			thda[i] = new Thread(teMaster);
			thda[i].start();
		}
		System.out.println("Waiting the executor thread to finish...");
		try {
			thd.join();
			for ( int i=0 ; i<iNumWorkers ; i++ ) 
				thda[i].join();
		    System.out.println("...done");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}
