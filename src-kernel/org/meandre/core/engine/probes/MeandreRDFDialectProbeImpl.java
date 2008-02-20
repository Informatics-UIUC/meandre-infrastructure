package org.meandre.core.engine.probes;

import java.util.Date;

import org.meandre.core.engine.Probe;
import org.meandre.core.utils.vocabulary.MeandreProbingVocabulary;
import org.meandre.core.utils.vocabulary.RepositoryVocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/** This class implements a probe for the engine that just dumps the information
 * to the provided PrintStream.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class MeandreRDFDialectProbeImpl 
implements Probe {
	
	/** The model that stores the probed data */
	protected Model modProbe = null;

	/** Create a new probe that does record anything to memory.
	 * 
	 */
	public MeandreRDFDialectProbeImpl () {
		// Create the model
		this.modProbe = ModelFactory.createDefaultModel();
	}
	
	/** Create a new probe that does record anything to the provided model.
	 * 
	 * @param mod The storage model to use.
	 */
	public MeandreRDFDialectProbeImpl ( Model mod ) {
		// Create the model
		this.modProbe = mod;
	}
	
	/** Populates the name spaces.
	 * 
	 */
	protected void setNameSpaces() {
		// Initializing the basic namespaces
		modProbe.setNsPrefix("rdf", RDF.getURI());
		modProbe.setNsPrefix("rdfs", RDFS.getURI());
		modProbe.setNsPrefix("dc", DC.getURI());
		modProbe.setNsPrefix("meandre", RepositoryVocabulary.getURI());
		modProbe.setNsPrefix("meandreProbe", MeandreProbingVocabulary.getURI());
		
	}
	
	/** The flow started executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowStart(String sFlowUniqueID, Date ts){
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sFlowUniqueID),
				MeandreProbingVocabulary.flow_started,
				RepositoryVocabulary.flow_component
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		modProbe.add(stm);
	}
	
	/** The flow stopped executing.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowFinish(String sFlowUniqueID, Date ts){
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sFlowUniqueID),
				MeandreProbingVocabulary.flow_finished,
				RepositoryVocabulary.flow_component
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		modProbe.add(stm);
	}
	
	/** The flow aborted the execution.
	 * 
	 * @param sFlowUniqueID The unique execution flow ID
	 * @param ts The time stamp
	 */
	public void probeFlowAbort(String sFlowUniqueID, Date ts){
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sFlowUniqueID),
				MeandreProbingVocabulary.flow_aborted,
				RepositoryVocabulary.flow_component
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		modProbe.add(stm);
	}

	/** The executable component finished initialization.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the initialization
	 * @param ts The time stamp
	 * @param bSerializeState The state has been serialized
	 */
	public void probeExecutableComponentInitialized(String sECID, Object owc, Date ts, boolean bSerializeState){
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sECID),
				MeandreProbingVocabulary.executable_component_instance_initialized,
				RepositoryVocabulary.executable_component_instance
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		
		// The state
		if ( bSerializeState )
			reifstm.addProperty(MeandreProbingVocabulary.state, modProbe.createTypedLiteral((String)owc));
		
		modProbe.add(stm);
	}
	
	/** The executable component requested execution abortion.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the initialization
	 * @param ts The time stamp
	 * @param bSerializeState The state has been serialized
	 */
	public void probeExecutableComponentAbort(String sECID, Object owc, Date ts, boolean bSerializeState){
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sECID),
				MeandreProbingVocabulary.executable_component_instance_aborted,
				RepositoryVocabulary.executable_component_instance
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		
		// The state
		if ( bSerializeState )
			reifstm.addProperty(MeandreProbingVocabulary.state, modProbe.createTypedLiteral((String)owc));
		
		modProbe.add(stm);
	}

	/** The executable component finished disposing itself.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 * @param bSerializeState The state has been serialized
	 */
	public void probeExecutableComponentDisposed(String sECID, Object owc, Date ts, boolean bSerializeState){
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sECID),
				MeandreProbingVocabulary.executable_component_instance_disposed,
				RepositoryVocabulary.executable_component_instance
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		
		// The state
		if ( bSerializeState )
			reifstm.addProperty(MeandreProbingVocabulary.state, modProbe.createTypedLiteral((String)owc));
		
		modProbe.add(stm);
	}
	

	/** The executable component pushed a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pushed
	 * @param ts The time stamp
	 * @param bSerializeState The state has been serialized
	 * @param bSerializedData The serialized data
	 */
	public void probeExecutableComponentPushData(String sECID, Object owc, Object odata, Date ts, boolean bSerializeState, boolean bSerializedData) {
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sECID),
				MeandreProbingVocabulary.executable_component_instance_pushed_data,
				RepositoryVocabulary.executable_component_instance
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		
		// The data
		if ( bSerializedData )
			reifstm.addProperty(MeandreProbingVocabulary.data_piece, modProbe.createTypedLiteral((String)odata));
		
		// The state
		if ( bSerializeState )
			reifstm.addProperty(MeandreProbingVocabulary.state, modProbe.createTypedLiteral((String)owc));
		
		modProbe.add(stm);
	}

	/** The executable component pulled a piece of data.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param odata The data being pulled
	 * @param ts The time stamp
	 * @param bSerializeState The state has been serialized
	 * @param bSerializedData The serialized data
	 */
	public void probeExecutableComponentPullData(String sECID, Object owc, Object odata, Date ts, boolean bSerializeState, boolean bSerializedData) {
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sECID),
				MeandreProbingVocabulary.executable_component_instance_pulled_data,
				RepositoryVocabulary.executable_component_instance
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		
		// The data
		if ( bSerializedData )
			reifstm.addProperty(MeandreProbingVocabulary.data_piece, modProbe.createTypedLiteral((String)odata));
		
		// The state
		if ( bSerializeState )
			reifstm.addProperty(MeandreProbingVocabulary.state, modProbe.createTypedLiteral((String)owc));
		
		modProbe.add(stm);
	}

	/** The executable component was fired.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 * @param bSerializeState The state has been serialized
	 */
	public void probeExecutableComponentFired(String sECID, Object owc, Date ts, boolean bSerializeState) {
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sECID),
				MeandreProbingVocabulary.executable_component_instance_fired,
				RepositoryVocabulary.executable_component_instance
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		
		// The state
		if ( bSerializeState )
			reifstm.addProperty(MeandreProbingVocabulary.state, modProbe.createTypedLiteral((String)owc));
		
		modProbe.add(stm);
	}

	/** The executable component was fired.
	 * 
	 * @param sECID The unique executable component ID
	 * @param owc The wrapped component done with the disposing call
	 * @param ts The time stamp
	 * @param bSerializeState The state has been serialized
	 */
	public void probeExecutableComponentCoolingDown(String sECID, Object owc, Date ts, boolean bSerializeState) {
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sECID),
				MeandreProbingVocabulary.executable_component_instance_cooling_down,
				RepositoryVocabulary.executable_component_instance
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		
		// The state
		if ( bSerializeState )
			reifstm.addProperty(MeandreProbingVocabulary.state, modProbe.createTypedLiteral((String)owc));
		
		modProbe.add(stm);
	}


	/** The executable component requested a property value.
	 * 
	 * @param sECID The unique executable component ID
	 * @param sPropertyName The requested property
	 * @param sPropertyValue The property value
	 * @param ts The time stamp
	 */
	public void probeExecutableComponentGetProperty(String sECID, String sPropertyName, String sPropertyValue, Date ts) {
		Statement stm = modProbe.createStatement(
				modProbe.createResource(sECID),
				MeandreProbingVocabulary.executable_component_instance_get_property,
				RepositoryVocabulary.executable_component_instance
			);
		ReifiedStatement reifstm = modProbe.createReifiedStatement(stm);
		reifstm.addProperty(DC.date, modProbe.createTypedLiteral(ts));
		reifstm.addProperty(RepositoryVocabulary.key, modProbe.createTypedLiteral(sPropertyName));
		reifstm.addProperty(RepositoryVocabulary.value, modProbe.createTypedLiteral(sPropertyValue));
		modProbe.add(stm);
	}

	/** Returns the model that contains the statements.
	 * 
	 * @return The model
	 */
	public Model getModel () {
		return modProbe;
	}

}
