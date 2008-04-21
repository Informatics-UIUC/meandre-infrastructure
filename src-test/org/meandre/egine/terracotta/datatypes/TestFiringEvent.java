package org.meandre.egine.terracotta.datatypes;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;
import org.meandre.engine.terracotta.datatypes.FiringEvent;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;


/** The objects of this class describe firing events.
 * 
 * @author Xavier Llor&agrave
 */
public class TestFiringEvent {

	/** The newline separator */
	private static final String sNL = System.getProperty("line.separator");
	
	/** Test the fire event creation.
	 * 
	 */
	@Test
	public void testFireEventCreation () {
		Model mod = ModelFactory.createDefaultModel();
		Resource resFID = mod.createResource("http://test.org/flow/instance/0");
		Resource resECID = mod.createResource("http://test.org/flow/instance/0/component/0");
		FiringEvent fe;
		
		for ( long l=0, lMax=100 ; l<lMax ; l++ ) {
			// Create and test fyring IDs
			fe = new FiringEvent();
			try {
				fe.init(resFID, resECID);
			} catch (InterruptedException e) {
				fail(e.toString());
			}
			assertEquals(l,fe.getFiringSequenceID());	
			
			// Bind some ports
			Random rnd = new Random();
			for ( int i=0, iMax=10 ; i<iMax ; i++ ) 
				fe.bindPort(mod.createResource(resECID.toString()+"/input/port-"+i), rnd.nextInt());
			
			// Check they are all binded
			assertEquals(10, fe.getNumberOfBindedPorts());
			
			String[] saFe= fe.toString().split(sNL);
			assertEquals(14, saFe.length);
		}
	}
	
}
