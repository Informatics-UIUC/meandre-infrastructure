package org.meandre.core.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.environments.python.jython.JythonExecutableComponentAdapter;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.demo.repository.DemoRepositoryGenerator;
import org.python.core.PyObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.thoughtworks.xstream.XStream;

/** This test unit is used to test the XStream serialization library.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class XStreamTest {
	
	/** Simple test required for object serialization using XStream library.
	 * 
	 */
	@Test
	public void simpleXStreamTest() {
		String sValue = "Hello";
		XStream xstream = new XStream();
		
		String sRes = xstream.toXML(sValue);
		assertEquals("<string>Hello</string>",sRes);
		
		String sResBack = (String) xstream.fromXML(sRes);
		assertEquals(sValue,sResBack);
	}
	
	
	/** A little more complicated object serialization using XStream library.
	 * 
	 */
	@Test
	public void repositoryXStreamTest() {
		Model model = ModelFactory.createDefaultModel();
		QueryableRepository qr = new RepositoryImpl(model);
		XStream xstream = new XStream();
		String sSerializedRepository = xstream.toXML(qr);
		QueryableRepository qr2 = (QueryableRepository) xstream.fromXML(sSerializedRepository);
		assertEquals(
				qr.getAvailableExecutableComponentDescriptions().size(),
				qr2.getAvailableExecutableComponentDescriptions().size()
			);
		assertEquals(
				qr.getAvailableFlowDescriptions().size(),
				qr2.getAvailableFlowDescriptions().size()
			);
	}
	
	/** Test the serialization library against the core main objects.
	 * 
	 */
	@Test
	public void coreSerializationTest() {
		try {
			// Preparing serialization objects
			XStream xstream = new XStream();
			
			// Objects to serialize
			Model model = DemoRepositoryGenerator.getTestHelloWorldHetereogenousRepository();
			QueryableRepository qr = new RepositoryImpl(model);
			CoreConfiguration cnf = new CoreConfiguration();
			Conductor conductor = new Conductor(10,cnf);
			Executor exec = conductor.buildExecutor(qr, qr.getAvailableFlows().iterator().next());
			
			// Starting serialization tests
			assertTrue(0<xstream.toXML(model).length());
			assertTrue(0<xstream.toXML(qr).length());
			assertTrue(0<xstream.toXML(conductor).length());
			
			assertTrue(0<xstream.toXML(conductor).length());
			
			//assertTrue(0<xstream.toXML(exec).length());
			for ( WrappedComponent wc:exec.getWrappedComponents())
				if ( wc.getExecutableComponentImplementation().getClass()==JythonExecutableComponentAdapter.class ) {
					JythonExecutableComponentAdapter jeca = (JythonExecutableComponentAdapter)wc.getExecutableComponentImplementation();
					PyObject pyobj = jeca.getLocals();
					String sDict = pyobj.toString();
					assertTrue(0<sDict.length());
				}
				else
					assertTrue(0<xstream.toXML(wc.getExecutableComponentImplementation()).length());
			
		}
		catch ( Exception e ) {
			e.printStackTrace();
			fail("This exeception should have not been thrown:"+e.toString());
		}
	}
	
}
