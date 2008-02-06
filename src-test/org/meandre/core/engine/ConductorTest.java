package org.meandre.core.engine;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.repository.QueryableRepository;
import org.meandre.core.store.repository.RepositoryImpl;
import org.meandre.demo.repository.DemoRepositoryGenerator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** Test the conductor behavior
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ConductorTest {
	/** Run the basic hello world test with dangling outputs.
	 * 
	 * @param conductor The conductor to use
	 * @param qr The query repository
	 * @throws CorruptedDescriptionException A corrupted description  was found
	 * @throws ConductorException The conductor could not build the flow
	 */
	private void runHelloWorldFlow(Conductor conductor,
			QueryableRepository qr) throws CorruptedDescriptionException,
			ConductorException {
		// Create the execution
		Executor exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next());
		// Redirect the output
		PrintStream psOut = System.out;
		PrintStream psErr = System.err;
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baosOut));
		System.setErr(new PrintStream(baosErr));
		exec.execute();
		System.setOut(psOut);
		System.setErr(psErr);
		// Restore the output
		assertTrue(exec.hadGracefullTermination());
		assertEquals(0,exec.getAbortMessage().size());
		assertEquals(0,baosErr.size());
		
		String sResult = "Hello World!!! Happy Meandring!!!Hello World!!! Happy Meandring!!!\n";
		assertEquals(sResult.length(),baosOut.size());
		assertEquals(sResult,baosOut.toString());
	}

	/** Run the basic hello world test.
	 * 
	 * @param conductor The conductor to use
	 * @param qr The query repository
	 * @throws CorruptedDescriptionException A corrupted description  was found
	 * @throws ConductorException The conductor could not build the flow
	 */
	private void runHelloWorldFlowWidthDanglingComponents(Conductor conductor,
			QueryableRepository qr) throws CorruptedDescriptionException,
			ConductorException {	
	
		// Create the execution
		Executor exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next());
		// Redirect the output
		PrintStream psOut = System.out;
		PrintStream psErr = System.err;
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baosOut));
		System.setErr(new PrintStream(baosErr));
		exec.execute();
		System.setOut(psOut);
		System.setErr(psErr);
		// Restore the output
		assertTrue(exec.hadGracefullTermination());
		assertEquals(0,exec.getAbortMessage().size());
		assertEquals(0,baosErr.size());
		
		String sResult = "Hello World!!! Happy Meandring!!!Hello World!!! Happy Meandring!!!\n";
		assertEquals(sResult.length(),baosOut.size());
		assertEquals(sResult,baosOut.toString());
	}

	/** Run the basic hello world test with dangling instances and fork.
	 * 
	 * @param conductor The conductor to use
	 * @param qr The query repository
	 * @throws CorruptedDescriptionException A corrupted description  was found
	 * @throws ConductorException The conductor could not build the flow
	 */ 
	private void runHelloWorldFlowWidthDanglingComponentsAndFork(Conductor conductor,
			QueryableRepository qr) throws CorruptedDescriptionException,
			ConductorException {	
		// Create the execution
		Executor exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next());
		// Redirect the output
		PrintStream psOut = System.out;
		PrintStream psErr = System.err;
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baosOut));
		System.setErr(new PrintStream(baosErr));
		exec.execute();
		System.setOut(psOut);
		System.setErr(psErr);
		// Restore the output
		assertTrue(exec.hadGracefullTermination());
		assertEquals(0,exec.getAbortMessage().size());
		assertEquals(0,baosErr.size());
		
		String sResult = "Hello World!!! Happy Meandring!!!Hello World!!! Happy Meandring!!!\n";
		sResult += sResult;
		assertEquals(sResult.length(),baosOut.size());
		assertEquals(sResult,baosOut.toString());
	}

	/**
	 * Test method for {@link org.meandre.core.engine.Conductor#buildExecutor(org.meandre.core.store.repository.QueryableRepository, com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testBuildExecutor() {
		Conductor conductor = new Conductor(10);
		
		try {
			// Run simple hello world
			Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
			QueryableRepository qr = new RepositoryImpl(model);
			assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("http://test.org/component/concatenate-strings")));
			// Run the basic text
			runHelloWorldFlow(conductor, qr);
			
			// Run simple hello world dangling input/outputs
			model = DemoRepositoryGenerator.getTestHelloWorldWithDanglingComponentsRepository();
			qr = new RepositoryImpl(model);
			assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("http://test.org/component/concatenate-strings")));
			// Run the basic text
			runHelloWorldFlowWidthDanglingComponents(conductor, qr);
			
			// Run simple hello world dangling input/outputs + fork
			model = DemoRepositoryGenerator.getTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository();
			qr = new RepositoryImpl(model);
			assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("http://test.org/component/concatenate-strings")));
			// Run the basic text
			runHelloWorldFlowWidthDanglingComponentsAndFork(conductor, qr);
			
		} catch (CorruptedDescriptionException e) {
			fail("Corrupted description encounterd: "+e);
		} catch (ConductorException e) {
			fail("Conductor exception: "+e);
		}
	}
	
	
	/** This test keeps updating a repository and reflushing it. Used to check for 
	 * memory leaks.
	 */
	@Test
	public void runRepetitiveUpdaterTest() {
		
		int REPETITIONS = 20;
		try {
			// Run simple hello world dangling input/outputs + fork
			Model model = DemoRepositoryGenerator.getNextTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository();
			RepositoryImpl qr = new RepositoryImpl(model);
			
			for ( int i=0 ; i<REPETITIONS ; i++ ) {
				Model modNew = DemoRepositoryGenerator.getNextTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository();
				model.add(modNew);
				qr.refreshCache(model);
//				System.out.println(i);
//				System.out.println(qr.getModel().size());
//				System.out.println(qr.getAvailableExecutableComponentDescriptions().size());
//				System.out.println(qr.getAvailableFlowDecriptions().size());
				assertEquals(i+2,qr.getAvailableFlowDecriptions().size());
				assertEquals(4*(i+2),qr.getAvailableExecutableComponentDescriptions().size());
			}
		} catch (InterruptedException e) {
			fail(e.toString());
		}
		
	}

}
