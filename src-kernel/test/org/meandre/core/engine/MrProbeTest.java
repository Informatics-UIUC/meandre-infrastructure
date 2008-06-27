package org.meandre.core.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.probes.MeandreRDFDialectProbeImpl;
import org.meandre.core.engine.probes.NullProbeImpl;
import org.meandre.core.engine.probes.StatisticsProbeImpl;
import org.meandre.core.engine.probes.ToPrintStreamProbeImpl;
import org.meandre.core.engine.test.TestLoggerFactory;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.demo.repository.DemoRepositoryGenerator;

import com.hp.hpl.jena.rdf.model.Model;

/** This class gathers together the tests related to MrProbe.
 * 
 * @author Xavier Llor&agrave;
 * @modified by Amit Kumar -Added error message to the abort flow probe
 */
public class MrProbeTest {
	
	/** The base URI for the tests */
	private static final String BASE_TEST_URI = "http://test.org/flow/0";

	/** The number of repetitions of the battery of calls */
	private final int NUMBER_OF_REPETITIONS = 10;
	
	/** The number of calls in the battery */
	private final int NUMBER_OF_CALLS = 11;

	/** MrProbe test against the null probe with no serialization.
	 * 
	 */
	@Test
	public void nullProbeMrProperTest () {
		try {
			Probe probe = new NullProbeImpl();
			runMrProbeTestWithProvidedProbe(probe, false, false);
		}
		catch ( Exception e ) {
			fail("Unexpected exception: "+e.toString());
		}
	}
	
	/** Runs the battery of test against MrProbe and the print stream probe with no serialization.
	 * 
	 */
	@Test
	public void printStreamMrProbeTest () {
		try {
			// Prepare the stream
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			// Create the probe
			Probe probe = new ToPrintStreamProbeImpl(ps);
			// Run the probe
			runMrProbeTestWithProvidedProbe(probe, false, false);
			// Test the output
			String[] sa = baos.toString().split(System.getProperty("line.separator"));
			assertEquals(NUMBER_OF_CALLS*NUMBER_OF_REPETITIONS, sa.length);
			// Release the content
			ps.close();
		}
		catch ( Exception e ) {
			fail("Unexpected exception: "+e.toString());
		}
	}
	
	/** Runs a battery of test against the rdf backended probe.
	 * 
	 */
	@Test
	public void meandreMemoryRDFDialectProblemTest () {
		try {
			// Create the probe
			MeandreRDFDialectProbeImpl mmrdpi = new MeandreRDFDialectProbeImpl();
			
			// Run the probe
			runMrProbeTestWithProvidedProbe(mmrdpi, false, false);
			
			// Dump the results
			Model modProbe = mmrdpi.getModel();
			assertEquals(581L,modProbe.size());
		}
		catch ( Exception e ) {
			fail("Unexpected exception: "+e.toString());
		}
	}
	
	/** Runs the Hello World example flow in all possible provenance configurations.
	 * 
	 */
	@Test
	public void meandreHelloWorldProvenanceTest () {
		try {
			Model model = DemoRepositoryGenerator.getTestHelloWorldHetereogenousRepository();
			QueryableRepository qr = new RepositoryImpl(model);
			CoreConfiguration cnf = new CoreConfiguration();
			
			// Basic test running the NullProbeImpl
			Conductor conductor = new Conductor(10,cnf);
			Executor exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next());
			runExecutor(exec);

			// Basic test running basic provenance to an RDF model
			MeandreRDFDialectProbeImpl rdfModProbe = new MeandreRDFDialectProbeImpl();
			MrProbe mrProbe = new MrProbe(TestLoggerFactory.getTestLogger(),rdfModProbe,false,false);
			conductor = new Conductor(10,cnf);
			exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(),mrProbe);
			runExecutor(exec);

			// Basic test running basic provenance and data serialization to an RDF model
			rdfModProbe = new MeandreRDFDialectProbeImpl();
			mrProbe = new MrProbe(TestLoggerFactory.getTestLogger(),rdfModProbe,true,false);
			conductor = new Conductor(10,cnf);
			exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(),mrProbe);
			runExecutor(exec);

			// Basic test running state storage provenance to an RDF model
			rdfModProbe = new MeandreRDFDialectProbeImpl();
			mrProbe = new MrProbe(TestLoggerFactory.getTestLogger(),rdfModProbe,false,true);
			conductor = new Conductor(10,cnf);
			exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(),mrProbe);
			runExecutor(exec);

			// Basic test running state storage provenance to an RDF model
			rdfModProbe = new MeandreRDFDialectProbeImpl();
			mrProbe = new MrProbe(TestLoggerFactory.getTestLogger(),rdfModProbe,true,true);
			conductor = new Conductor(10,cnf);
			exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(),mrProbe);
			runExecutor(exec);
			
			// Model mod = rdfModProbe.getModel();
			// mod.write(System.out,"TTL",null);
			// System.out.println(mod.size());
		}
		catch ( Exception e ) {
			e.printStackTrace();
			fail("This exception should have not been thrown "+e);
		}
	}
	

	/** Runs the Hello World example flow with the statics probe.
	 * 
	 */
	@Test
	public void meandreHelloWorldStatisticsTest () {
		try {
			Model model = DemoRepositoryGenerator.getTestHelloWorldHetereogenousRepository();
			QueryableRepository qr = new RepositoryImpl(model);
			CoreConfiguration cnf = new CoreConfiguration();
			Conductor conductor = new Conductor(10,cnf);
			StatisticsProbeImpl spi = new StatisticsProbeImpl();
			MrProbe mrProbe = new MrProbe(TestLoggerFactory.getTestLogger(),spi,false,false);
			Executor exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(),mrProbe);
			runExecutor(exec);
			
			// Test the stats are there
			JSONObject jsonStats = spi.getSerializedStatistics();
			assertNotNull(jsonStats.get("flow_unique_id"));
			assertNotNull(jsonStats.get("flow_state"));
			assertNotNull(jsonStats.get("started_at"));
			assertNotNull(jsonStats.get("latest_probe_at"));
			assertNotNull(jsonStats.get("runtime"));
			
			JSONArray jaEXIS = (JSONArray) jsonStats.get("executable_components_statistics");
			assertNotNull(jaEXIS);
			for ( int i=0,iMax=jaEXIS.length() ; i<iMax ; i++ ) {
				JSONObject joEXIS = (JSONObject) jaEXIS.get(i);
				assertNotNull(joEXIS.get("executable_component_instance_id"));
				assertNotNull(joEXIS.get("executable_component_state"));
				assertNotNull(joEXIS.get("times_fired"));
				assertNotNull(joEXIS.get("accumulated_runtime"));
				assertNotNull(joEXIS.get("pieces_of_data_in"));
				assertNotNull(joEXIS.get("pieces_of_data_out"));
				assertNotNull(joEXIS.get("number_of_read_properties"));
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
			fail("This exception should have not been thrown "+e);
		}
	}

	/** Runs an executor for the demo hello world example.
	 * 
	 * @param exec The executor to use
	 * @return
	 */
	private void runExecutor(Executor exec) {
		PrintStream psOut = System.out;
		PrintStream psErr = System.err;
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baosOut));
		System.setErr(new PrintStream(baosErr));
		exec.execute(exec.initWebUI(1707,Math.random()+""));
		System.setOut(psOut);
		System.setErr(psErr);
		// Restore the output
		assertTrue(exec.hadGracefullTermination());
		assertEquals(0,exec.getAbortMessage().size());
		System.out.println(baosErr);
		assertEquals(0,baosErr.size());
		
		String sResult = "HELLO WORLD!!! HAPPY MEANDRING!!! (P1,C01234567) HELLO WORLD!!! HAPPY MEANDRING!!! (P1,C01234567)  \n";
		assertEquals(sResult.length(),baosOut.size());
	}

	/** Creates a MrProbe thread for the given probe, runs a battery of probe calls
	 * on it, and waits for it to die.
	 * 
	 * @param probe The probe to use
	 * @param dataSerialization Force data serialization
	 * @param stateSerialization Force state serialization
	 */
	private void runMrProbeTestWithProvidedProbe(Probe probe, boolean dataSerialization, boolean stateSerialization) {
		MrProbe mp = new MrProbe(TestLoggerFactory.getTestLogger(),probe,false,false);
		// Start the probe execution
		mp.start();
		// Runs a battery of probe calls
		runBatteryOfProbeCalls(mp);
		// End the probe execution
		mp.done();
		// Wait for MrProbe to finish
		try {
			mp.join();
		} catch (InterruptedException e) {
			fail("Unexpected exception: "+e.toString());
		}
	}

	/** Run a battery of probe calls.
	 * 
	 * @param mp The MrProbe object to use
	 */
	private void runBatteryOfProbeCalls(MrProbe mp) {
		try {
			// Preparing some wrapped components for testing
			Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
			QueryableRepository qr = new RepositoryImpl(model);
			CoreConfiguration cnf = new CoreConfiguration();
			Conductor cnd = new Conductor(10,cnf);
			Executor exec = cnd.buildExecutor(qr, qr.getAvailableFlowDescriptions().iterator().next().getFlowComponent());
			WrappedComponent wc = exec.getWrappedComponents().iterator().next();
			// Run the tests
			String rands=Math.random()+"";
			for ( int i=0; i<NUMBER_OF_REPETITIONS; i++ ) {
				mp.probeFlowStart(BASE_TEST_URI,"http://127.0.0.1:1704/",rands);
				mp.probeFlowFinish(BASE_TEST_URI,rands);
				mp.probeFlowAbort(BASE_TEST_URI,rands,"Some Error message");
				mp.probeWrappedComponentInitialize(wc);
				mp.probeWrappedComponentAbort(wc);
				mp.probeWrappedComponentDispose(wc);
				mp.probeWrappedComponentPushData(wc, "string", "hello");
				mp.probeWrappedComponentPullData(wc, "string", "hello");
				mp.probeWrappedComponentGetProperty(wc, "property", "value");
				mp.probeWrappedComponentFired(wc);
				mp.probeWrappedComponentCoolingDown(wc);
			}
		}
		catch (Exception e) {
			fail("An exception should have never been thrown "+e.toString());
		}
	}
}
