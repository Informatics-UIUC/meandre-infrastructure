package org.meandre.core.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.meandre.core.engine.probes.MeandreRDFDialectProbeImpl;
import org.meandre.core.engine.probes.NullProbeImpl;
import org.meandre.core.engine.probes.ToPrintStreamProbeImpl;
import org.meandre.core.engine.test.TestLoggerFactory;
import org.meandre.core.store.repository.QueryableRepository;
import org.meandre.core.store.repository.RepositoryImpl;
import org.meandre.demo.repository.DemoRepositoryGenerator;

import com.hp.hpl.jena.rdf.model.Model;

/** This class gathers together the tests related to MrProbe.
 * 
 * @author Xavier Llor&agrave;
 *
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
			modProbe.write(System.out,"TTL",null);
			System.out.println(modProbe.size());
		}
		catch ( Exception e ) {
			fail("Unexpected exception: "+e.toString());
		}
	}

	/** Creates a MrProbe thread for the given probe, runs a battery of probe calls
	 * on it, and waits for it to die.
	 * 
	 * @param probe The probe to use
	 * @param dataSerialization Force data serialization
	 * @param stateSerialization Force state serialization
	 */
	private void runMrProbeTestWithProvidedProbe(Probe probe, boolean dataSerialization, boolean stateSerialization) {
		MrProbe mp = new MrProbe(TestLoggerFactory.getDemoLogger(),probe,false,false);
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
			Conductor cnd = new Conductor(10);
			Executor exec = cnd.buildExecutor(qr, qr.getAvailableFlowDecriptions().iterator().next().getFlowComponent());
			WrappedComponent wc = exec.getWrappedComponents().iterator().next();
			// Run the tests
			for ( int i=0; i<NUMBER_OF_REPETITIONS; i++ ) {
				mp.probeFlowStart(BASE_TEST_URI);
				mp.probeFlowFinish(BASE_TEST_URI);
				mp.probeFlowAbort(BASE_TEST_URI);
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
