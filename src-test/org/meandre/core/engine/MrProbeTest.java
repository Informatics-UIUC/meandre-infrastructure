package org.meandre.core.engine;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.meandre.core.engine.probes.NullProbeImpl;
import org.meandre.core.engine.probes.ToPrintStreamProbeImpl;
import org.meandre.core.engine.test.TestLoggerFactory;

/** This class gathers together the tests related to MrProbe.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class MrProbeTest {
	
	/** The number of repetitions of the battery of calls */
	final int NUMBER_OF_REPETITIONS = 10;
	
	/** The number of calls in the battery */
	final int NUMBER_OF_CALLS = 3;
	
	
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

	private void runBatteryOfProbeCalls(MrProbe mp) {
		for ( int i=0; i<NUMBER_OF_REPETITIONS; i++ ) {
			mp.probeFlowStart("http://test.org/flow/0");
			mp.probeFlowFinish("http://test.org/flow/0");
			mp.probeFlowAbort("http://test.org/flow/0");
		}
	}
}
