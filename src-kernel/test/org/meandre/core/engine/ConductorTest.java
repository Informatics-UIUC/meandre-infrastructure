package org.meandre.core.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.demo.repository.DemoRepositoryGenerator;
import org.meandre.webui.WebUI;

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
		// Redirect the output
		PrintStream psOut = System.out;
		PrintStream psErr = System.err;
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
		Executor exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(), new PrintStream(baosOut));
		System.setOut(new PrintStream(baosOut));
		System.setErr(new PrintStream(baosErr));
		WebUI webui = exec.initWebUI(1704,Math.random()+"");
		exec.execute(webui);
		System.setOut(psOut);
		System.setErr(psErr);
		// Restore the output
		assertTrue(exec.hadGracefullTermination());
		assertEquals(0,exec.getAbortMessage().size());
		assertEquals(0,baosErr.size());
		
		String sResult = "P1 Hello World!!! Happy Meandring!!! (P1,C0123456) Hello World!!! Happy Meandring!!! (P1,C0123456) \n";
		assertEquals(sResult.length(),baosOut.size());
	}

	/** Run the basic hello world test with dangling outputs.
	 * 
	 * @param conductor The conductor to use
	 * @param qr The query repository
	 * @throws CorruptedDescriptionException A corrupted description  was found
	 * @throws ConductorException The conductor could not build the flow
	 */
	private void runHelloWorldHetereogenousFlow(Conductor conductor,
			QueryableRepository qr) throws CorruptedDescriptionException,
			ConductorException {
		// Redirect the output
		PrintStream psOut = System.out;
		PrintStream psErr = System.err;
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baosOut));
		System.setErr(new PrintStream(baosErr));
		// Create the execution
		Executor exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(), new PrintStream(baosOut));
		exec.execute(exec.initWebUI(1705,Math.random()+""));
		System.setOut(psOut);
		System.setErr(psErr);
		// Restore the output
		assertTrue(exec.hadGracefullTermination());
		//System.out.println(baosErr.toString());
		assertEquals(0,exec.getAbortMessage().size());
		//System.out.println(baosErr.toString());
		String sError = baosErr.toString();
		assertTrue(sError.contains("jetty-"));
		assertTrue(sError.contains("Started SocketConnector@"));
		
		String sResult = "HELLO WORLD!!! HAPPY MEANDRING!!!  (P1,C0123456)  HELLO WORLD!!! HAPPY MEANDRING!!! (P1,C0123456)  \n";
		assertEquals(sResult.length(),baosOut.size());
	}

	/** Run the basic hello world test with dangling outputs.
	 * 
	 * @param conductor The conductor to use
	 * @param qr The query repository
	 * @throws CorruptedDescriptionException A corrupted description  was found
	 * @throws ConductorException The conductor could not build the flow
	 */
	private void runHelloWorldMoreHetereogenousFlow(Conductor conductor,
			QueryableRepository qr) throws CorruptedDescriptionException,
			ConductorException {
		// Redirect the output
		PrintStream psOut = System.out;
		PrintStream psErr = System.err;
		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
		// Create the execution
		Executor exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(), new PrintStream(baosOut));
		System.setOut(new PrintStream(baosOut));
		System.setErr(new PrintStream(baosErr));
		exec.execute(exec.initWebUI(1706,Math.random()+""));
		System.setOut(psOut);
		System.setErr(psErr);
		// Restore the output
		assertTrue(exec.hadGracefullTermination());
		System.out.println(baosErr.toString());
		assertEquals(0,exec.getAbortMessage().size());
		System.out.println(baosErr.toString());
		assertEquals(0,baosErr.size());
		
		String sResult = "HELLO WORLD!!! HAPPY MEANDRING!!!  (P1,C0123456)  HELLO WORLD!!! HAPPY MEANDRING!!! (P1,C0123456)  \n";
		assertEquals(sResult.length(),baosOut.size());
	}
	
	/**
	 * Test method for {@link org.meandre.core.engine.Conductor#buildExecutor(org.meandre.core.store.repository.QueryableRepository, com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testBuildExecutorHelloWorld() {
		CoreConfiguration cnf = new CoreConfiguration();
		Conductor conductor = new Conductor(10,cnf);
		
		try {
			// Run simple hello world
			Model model = DemoRepositoryGenerator.getTestHelloWorldRepository();
			QueryableRepository qr = new RepositoryImpl(model);
			assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("meandre://test.org/component/concatenate-strings")));
			// Run the basic test
			runHelloWorldFlow(conductor, qr);
			
		} catch (CorruptedDescriptionException e) {
			fail("Corrupted description encounterd: "+e);
		} catch (ConductorException e) {
			fail("Conductor exception: "+e);
		}
	}

	/**
	 * Test method for {@link org.meandre.core.engine.Conductor#buildExecutor(org.meandre.core.store.repository.QueryableRepository, com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testBuildExecutorHetereogenous() {
		CoreConfiguration cnf = new CoreConfiguration();
		Conductor conductor = new Conductor(10,cnf);
		
		try {
			// Run simple hetereogenous hello world (Java+Python)
			Model model = DemoRepositoryGenerator.getTestHelloWorldHetereogenousRepository();
			RepositoryImpl qr = new RepositoryImpl(model);
			//for ( Resource res:qr.getAvailableExecutableComponents())
			//	System.out.println(res);
			assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("meandre://test.org/component/to-uppercase")));
			// Run the basic test
			runHelloWorldHetereogenousFlow(conductor, qr);
			
		} catch (CorruptedDescriptionException e) {
			fail("Corrupted description encounterd: "+e);
		} catch (ConductorException e) {
			fail("Conductor exception: "+e);
		}
	}

	/**
	 * Test method for {@link org.meandre.core.engine.Conductor#buildExecutor(org.meandre.core.store.repository.QueryableRepository, com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testBuildExecutorMoreHetereogenous() {
		CoreConfiguration cnf = new CoreConfiguration();
		Conductor conductor = new Conductor(10,cnf);
		
		try {
			// Run simple hetereogenous hello world (Java+Python)
			Model model = DemoRepositoryGenerator.getTestHelloWorldMoreHetereogenousRepository();
			RepositoryImpl qr = new RepositoryImpl(model);
			//for ( Resource res:qr.getAvailableExecutableComponents())
			//	System.out.println(res);
			assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("meandre://test.org/component/to-uppercase")));
			// Run the basic test
			runHelloWorldMoreHetereogenousFlow(conductor, qr);
			
		} catch (CorruptedDescriptionException e) {
			fail("Corrupted description encounterd: "+e);
		} catch (ConductorException e) {
			fail("Conductor exception: "+e);
		}
	}
	/**
	 * Test method for {@link org.meandre.core.engine.Conductor#buildExecutor(org.meandre.core.store.repository.QueryableRepository, com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testBuildExecutorHelloWorldWithDanglingComponents() {
		CoreConfiguration cnf = new CoreConfiguration();
		Conductor conductor = new Conductor(10,cnf);
		
		try {
			// Run simple hello world dangling input/outputs
			Model model = DemoRepositoryGenerator.getTestHelloWorldWithDanglingComponentsRepository();
			RepositoryImpl qr = new RepositoryImpl(model);
			assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("meandre://test.org/component/concatenate-strings")));
			// Create the execution
			assertNotNull(conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(),System.out));
					
		} catch (CorruptedDescriptionException e) {
			fail("Corrupted description encounterd: "+e);
		} catch (ConductorException e) {
			fail("Conductor exception: "+e);
		}
	}

	/**
	 * Test method for {@link org.meandre.core.engine.Conductor#buildExecutor(org.meandre.core.store.repository.QueryableRepository, com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testBuildExecutorWithDanglingComponentsAndInAndOutForks() {
		CoreConfiguration cnf = new CoreConfiguration();
		Conductor conductor = new Conductor(10,cnf);
		
		try {
			// Run simple hello world dangling input/outputs + fork
			Model model = DemoRepositoryGenerator.getTestHelloWorldWithDanglingComponentsAndInAndOutForksRepository();
			RepositoryImpl qr = new RepositoryImpl(model);
			assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("meandre://test.org/component/concatenate-strings")));
			// Create the execution
			assertNotNull(conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(),System.out));
			
		} catch (CorruptedDescriptionException e) {
			fail("Corrupted description encounterd: "+e);
		} catch (ConductorException e) {
			fail("Conductor exception: "+e);
		}
	}

	/**
	 * Test method for {@link org.meandre.core.engine.Conductor#buildExecutor(org.meandre.core.store.repository.QueryableRepository, com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testDanglingPartiallyConnectedWitExecutor() {
		CoreConfiguration cnf = new CoreConfiguration();
		Conductor conductor = new Conductor(10,cnf);
		
		try {
			// Run simple hello world
			Model model = DemoRepositoryGenerator.getTestHelloWorldPartialDanglingRepository();
			QueryableRepository qr = new RepositoryImpl(model);
			assertNotNull(qr.getExecutableComponentDescription(ModelFactory.createDefaultModel().createResource("meandre://test.org/component/concatenate-strings")));
			// Create the execution
			assertNotNull(conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next(),System.out));
				
		} catch (CorruptedDescriptionException e) {
			fail("Corrupted description encounterd: "+e);
		} catch (ConductorException e) {
			fail("Conductor exception: "+e);
		}
	}

}
