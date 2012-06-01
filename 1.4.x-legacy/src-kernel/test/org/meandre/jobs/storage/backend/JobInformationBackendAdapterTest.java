/**
 * 
 */
package org.meandre.jobs.storage.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.services.coordinator.logger.CoordinatorLoggerFactory;
import org.meandre.core.store.Store;

/** This class test the job information backend adapter.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class JobInformationBackendAdapterTest {


	/** The job id to use for testing purposes */
	private static final String JID = "JID-"+System.currentTimeMillis()+"-";

	/** The logger to use */
	public final static Logger log = KernelLoggerFactory.getCoreLogger();
	
	/** Contains the list of backend addapters to test */
	protected static String [] sbaNames = { 
		DerbyJobInformationBackendAdapter.class.getName(), 
		MySQLJobInformationBackendAdapter.class.getName(),
	};
	
	
	/** Check that the resource mapping file is there.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void testClassPreparation () {
		
		log.info("Running test class preparation");
		
		// Reseting the logging level
		CoordinatorLoggerFactory.setLevel(Level.FINEST);
		
		// Check the base adapter
		try {
			InputStream dis = JobInformationBackendAdapter.class.getResourceAsStream(JobInformationBackendAdapter.COMMON_MAP_FILE);
			assertNotNull(dis);
			if ( dis!=null )
				dis.close();
		} catch (Exception e) {
			fail("This exception should have not be thrown!!!\n"+e.toString());
		}
		
		// Check the flavored adapters
		for ( String sCN:sbaNames ) {
			try {
				Class c = Class.forName(sCN);
				String[] sCNa = sCN.split("\\"+".");
				String sPropFile = sCNa[sCNa.length-1].replaceAll("JobInformationBackendAdapter", "").toLowerCase();
				sPropFile = "job_information_query_map_"+sPropFile+".xml";
				InputStream dis = c.getResourceAsStream(sPropFile);
				assertNotNull(dis);
				if ( dis!=null )
					dis.close();
			} catch (Exception e) {
				fail("This exception should have not be thrown!!!\n"+e.toString());
			}
		}
		
		log.info("Test class preparation done");
		
	}
	
	/** Cleans the schema after each test.
	 * 
	 */
	@After
	public void cleanAfterTesting() {
		JobInformationBackendAdapter ba = createBackendAdaptorFromStore();
		ba.close();
	}


	/** Gets and adapter based on the current store configuration object.
	 * 
	 */
	private JobInformationBackendAdapter createBackendAdaptorFromStore () {
		CoreConfiguration cnf = new CoreConfiguration();
		Store store = new Store(cnf);
		
		try {
			// Instantiate the adaptor
			JobInformationBackendAdapter ba = (JobInformationBackendAdapter) Class.forName(
					"org.meandre.jobs.storage.backend."+store.getDatabaseFlavor()+"JobInformationBackendAdapter"
				).newInstance();
			
			// Link it to a store
			ba.linkToService(store.getConnectionToDB(),cnf.getBasePort());
			
			assertNotNull(ba);
			
			return ba;
			
		} catch (InstantiationException e) {
			fail("Backend adapter could not be instantiated for backend flavor "+store.getDatabaseFlavor()+". "+e.toString());
		} catch (IllegalAccessException e) {
			fail("Illigal access exception to access backend adapter flavor "+store.getDatabaseFlavor()+". "+e.toString());
		} catch (ClassNotFoundException e) {
			fail("The backend adapter for flavor "+store.getDatabaseFlavor()+" could not be found. "+e.toString());
		}
		
		return null;
	}
	

	/** Test the creation of the required database schema.
	 * 
	 */
	@Test
	public void testCreateAndDropSchema () {

		log.info("Running test create and drop schema" );
		
		JobInformationBackendAdapter ba = createBackendAdaptorFromStore();
		
		// Remove the installed shutdown hook
		Runtime.getRuntime().removeShutdownHook(ba.getShutdownHook());
	
		log.info("Test create and drop schema done" );
		
	}

	/** Test the console basic IO.
	 * 
	 */
	@Test
	public void testConsosolBasicIO () {

		log.info("Running test of the basic console IO" );
		
		JobInformationBackendAdapter ba = createBackendAdaptorFromStore();
		
		// Try to create the schema
		try {
			final int TIMES = 10;
			
			for ( int i=0, iMax=TIMES ; i<iMax ; i++ ) {
				ba.println(JID, "line "+i);
				Thread.sleep(10);
			}
			
			String sConsole = ba.getConsole(JID);
			String [] sLine = sConsole.split("\n");
			assertEquals(TIMES,sLine.length);
			for ( int i=0, iMax=TIMES ; i<iMax ; i++ )
				assertEquals("line "+i, sLine[i]);
			
			// Remove the installed shutdown hook
			Runtime.getRuntime().removeShutdownHook(ba.getShutdownHook());
		} catch (InterruptedException e) {
			fail("Sleep interrupted");
		}
	
		log.info("Test  of the basic console IO done" );
		
	}


	/** Test the console log IO.
	 * 
	 */
	@Test
	public void testLogBasicIO () {

		log.info("Running test of the basic log IO" );
		
		JobInformationBackendAdapter ba = createBackendAdaptorFromStore();
		
		// Try to create the schema
		try {
			final int TIMES = 10;
			
			for ( int i=0, iMax=TIMES ; i<iMax ; i++ ) {
				ba.log(JID, "TEST", "log "+i);
				Thread.sleep(10);
			}
			
			String sLog = ba.getLog(JID);
			String [] sLine = sLog.split("\n");
			assertEquals(TIMES,sLine.length);
			for ( int i=0, iMax=TIMES ; i<iMax ; i++ )
				assertEquals("TEST: log "+i, sLine[i]);
			
			// Remove the installed shutdown hook
			Runtime.getRuntime().removeShutdownHook(ba.getShutdownHook());
		} catch (InterruptedException e) {
			fail("Sleep interrupted");
		}
	
		log.info("Test  of the basic log IO done" );
		
	}


	/** Test the job status manipulation IO.
	 * 
	 */
	@Test
	public void testJobStatus () {

		log.info("Running test of the job status manipulation" );
		
		JobInformationBackendAdapter ba = createBackendAdaptorFromStore();
		
		final int TIMES = 10;
		
		// Start jobs
		for ( int i=0; i<TIMES; i++ )
			ba.startJob(JID+i,"test");
		
		// Retrieve jobs
		for ( int i=0; i<TIMES; i++ )
			assertEquals(
					JobInformationBackendAdapter.JOB_STATUS_RUNNING, 
					ba.getJobStatus(JID+i)
				);
		
		// Retrieve all the jobs
		for ( Map<String,String> mapStatus:ba.getJobStatuses() )
			if ( mapStatus.get("job_id").startsWith(JID) ) {
				assertEquals(
						JobInformationBackendAdapter.JOB_STATUS_RUNNING, 
						mapStatus.get("status")
					);
				assertEquals(
						"test", 
						mapStatus.get("user_id")
					);
			}
		
		// Change jobs status
		for ( int i=0; i<TIMES; i++ )
			ba.updateJobStatus(JID+i,JobInformationBackendAdapter.JOB_STATUS_COMPLETED);
		
		// Check the changes statuses
		for ( Map<String,String> mapStatus:ba.getJobStatuses() )
			if ( mapStatus.get("job_id").startsWith(JID) )
				assertEquals(
					JobInformationBackendAdapter.JOB_STATUS_COMPLETED, 
					mapStatus.get("status")
				);
		
		
		// Simulate server death
		ba.updateJobStatusInServer(
				JobInformationBackendAdapter.JOB_STATUS_COMPLETED, 
				JobInformationBackendAdapter.JOB_STATUS_KILLED)
			;
		ba.updateJobStatusInServer(
				JobInformationBackendAdapter.JOB_STATUS_ABORTED, 
				JobInformationBackendAdapter.JOB_STATUS_KILLED)
			;
		
		// Check the changes statuses
		for ( Map<String,String> mapStatus:ba.getJobStatuses() )
			assertEquals(
					JobInformationBackendAdapter.JOB_STATUS_KILLED, 
					mapStatus.get("status")
				);
		
		// Remove the installed shutdown hook
		Runtime.getRuntime().removeShutdownHook(ba.getShutdownHook()); 
	
		log.info("Test  of the basic log IO done" );
		
	}
}
