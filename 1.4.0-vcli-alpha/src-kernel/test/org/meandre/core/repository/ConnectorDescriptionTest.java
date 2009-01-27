package org.meandre.core.repository;

import static org.junit.Assert.*;

import org.junit.Test;
import org.meandre.core.repository.ConnectorDescription;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** Testing the connector description object behavior.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ConnectorDescriptionTest {

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#ConnectorDescription()}.
	 */
	@Test
	public void testConnectorDescription() {
		
		ConnectorDescription cd = new ConnectorDescription();
		
		assertEquals(cd.getConnector(),null);
		assertEquals(cd.getSourceInstance(),null);
		assertEquals(cd.getSourceInstanceDataPort(),null);
		assertEquals(cd.getTargetInstance(),null);
		assertEquals(cd.getTargetInstanceDataPort(),null);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#ConnectorDescription(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testConnectorDescriptionResourceResourceResourceResourceResource() {
		
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getConnector(),resConnector);
		assertEquals(cd.getSourceInstance(),resInstanceSource);
		assertEquals(cd.getSourceInstanceDataPort(),resInstanceDataPortSource);
		assertEquals(cd.getTargetInstance(),resInstanceTarget);
		assertEquals(cd.getTargetInstanceDataPort(),resInstanceDataPortTarget);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#setConnector(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testSetConnector() {
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getConnector(),resConnector);
		
		Resource resConnectorNew = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cdnew");
		cd.setConnector(resConnectorNew);
		assertEquals(cd.getConnector(),resConnectorNew);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#getConnector()}.
	 */
	@Test
	public void testGetConnector() {
		
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getConnector(),resConnector);

	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#setSourceInstance(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testSetSourceInstance() {
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getSourceInstance(),resInstanceSource);
		
		Resource resInstanceSourceNew = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isnew");

		cd.setSourceInstance(resInstanceSourceNew);
		assertEquals(cd.getSourceInstance(),resInstanceSourceNew);	
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#getSourceInstance()}.
	 */
	@Test
	public void testGetSourceInstance() {
		
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getSourceInstance(),resInstanceSource);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#setSourceInstanceDataPort(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testSetSourceIntaceDataPort() {
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getSourceInstanceDataPort(),resInstanceDataPortSource);
		
		Resource resInstanceDataPortSourceNew = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdpnew");
		
		cd.setSourceInstanceDataPort(resInstanceDataPortSourceNew);
		assertEquals(cd.getSourceInstanceDataPort(),resInstanceDataPortSourceNew);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#getSourceInstanceDataPort()}.
	 */
	@Test
	public void testGetSourceIntaceDataPort() {
		
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getSourceInstanceDataPort(),resInstanceDataPortSource);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#setTargetInstance(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testSetTargetInstance() {
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getTargetInstance(),resInstanceTarget);
		
		Resource resInstanceTargetNew = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itnew");
		
		cd.setTargetInstance(resInstanceTargetNew);
		assertEquals(cd.getTargetInstance(),resInstanceTargetNew);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#getTargetInstance()}.
	 */
	@Test
	public void testGetTargetInstance() {
		
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getTargetInstance(),resInstanceTarget);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#setTargetInstanceDataPort(com.hp.hpl.jena.rdf.model.Resource)}.
	 */
	@Test
	public void testSetTargetIntaceDataPort() {

		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getTargetInstanceDataPort(),resInstanceDataPortTarget);
		
		Resource resInstanceDataPortTargetNew = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdpnew");
		cd.setTargetInstanceDataPort(resInstanceDataPortTargetNew);
		assertEquals(cd.getTargetInstanceDataPort(),resInstanceDataPortTargetNew);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#getTargetInstanceDataPort()}.
	 */
	@Test
	public void testGetTargetIntaceDataPort() {
		
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		assertEquals(cd.getTargetInstanceDataPort(),resInstanceDataPortTarget);

	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.ConnectorDescription#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Resource resConnector = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd = new ConnectorDescription(resConnector,
				resInstanceSource, resInstanceDataPortSource,
				resInstanceTarget, resInstanceDataPortTarget);
		
		Resource resConnector2 = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/cd");
		Resource resInstanceSource2 = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/is");
		Resource resInstanceDataPortSource2 = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/isdp");
		Resource resInstanceTarget2 = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/it");
		Resource resInstanceDataPortTarget2 = ModelFactory.createDefaultModel().createResource("http://meandre.org/test/itdp");
		
		ConnectorDescription cd2 = new ConnectorDescription(resConnector2,
				resInstanceSource2, resInstanceDataPortSource2,
				resInstanceTarget2, resInstanceDataPortTarget2);
		
		assertTrue(cd.equals(cd2));
	}

}
