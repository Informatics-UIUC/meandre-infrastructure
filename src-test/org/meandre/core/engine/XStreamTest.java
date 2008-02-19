package org.meandre.core.engine;

import static org.junit.Assert.*;

import org.junit.Test;
import org.meandre.core.store.repository.QueryableRepository;
import org.meandre.core.store.repository.RepositoryImpl;

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
				qr.getAvailableFlowDecriptions().size(),
				qr2.getAvailableFlowDecriptions().size()
			);
	}
	
}
