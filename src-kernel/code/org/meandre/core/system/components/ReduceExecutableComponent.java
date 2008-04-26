/**
 * 
 */
package org.meandre.core.system.components;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.DataPortDescription;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.PropertiesDescriptionDefinition;
import org.meandre.core.repository.TagsDescription;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/** This system component takes and input stream of object and fans it
 * out in a round-robin maner to the number of available outputs.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ReduceExecutableComponent implements ExecutableComponent {

	/** The number of outputs */
	private int iNumberOfMapTargets;
	
	/** The current pushed value */
	private int iRoundRobin;

	/** The array of input queues */
	private Queue<Object>[] cqa;

	/** The translation between names and queue arrary index */
	private Hashtable<String,Integer> htTranslation;
	
	/** Returns the on-the-fly descriptor generated for this component.
	 * 
	 * @param iNumberOfMapTargets The number of outputs
	 * @return The executable component description
	 */
	public static ExecutableComponentDescription getExecutableComponentDescription ( int iNumberOfMapTargets ) {
		Logger log = KernelLoggerFactory.getCoreLogger();
		
		String sBaseURL = "http://www.meandre.org/system/component/reduce/"+iNumberOfMapTargets;
		
		ExecutableComponentDescription ecdRes = null;
		
		Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sBaseURL);
		
		// General properties
		String sName = "Map";
		String sDescription = "Collects "+iNumberOfMapTargets+" outputs from a parallelized instance by cycling through them in a round-robin fashion (equivalent to a gather operation).";
		String sRights = "University of Illinois/NCSA open source license";
		String sCreator = "Xavier Llor&agrave;";
		Date dateCreation = new Date();
		try {
			dateCreation = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).parse("April 23, 2008 6:39 PM");
		} catch (ParseException e) {
			log.warning(e.toString());
		}
		
		// Context
		Set<RDFNode> setContext = new HashSet<RDFNode>();
		setContext.add(ModelFactory.createDefaultModel().createResource(sBaseURL));
		
		// Location
		Resource resLocation = ModelFactory.createDefaultModel().createResource(sBaseURL+ReduceExecutableComponent.class.getName());
		
		// The output port
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
		Resource resDPDOutput1 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/object");
		String sDPDOut1Ident = resDPDOutput1.toString(); 
		String sDPDOut1Name = "object";
		String sDPDOut1Desc = "The object of the reconstructed stream";
		try {
			setOutputs.add(new DataPortDescription(resDPDOutput1,sDPDOut1Ident,sDPDOut1Name,sDPDOut1Desc));
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		// The input ports
		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		for ( int i=0 ; i<iNumberOfMapTargets ; i++ ) {
			Resource resDPDInput = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/input/object-"+i);
			String sDPDIdent = resDPDInput.toString(); 
			String sDPDName = "object-"+i;
			String sDPDDesc = "The object provided by the parellel instance "+i;
			try {
				setInputs.add(new DataPortDescription(resDPDInput,sDPDIdent,sDPDName,sDPDDesc));
			} catch (CorruptedDescriptionException e) {
				log.severe("An exception should have not been trown: "+e);
			}
		}
		
		// Properties
		Hashtable<String,String> htValues = new Hashtable<String,String>();
		Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
		htValues.put("number-of-inputs", ""+iNumberOfMapTargets);
		htDescriptions.put("number-of-inputs", "The number of inputs for this map process. *DO NOT MODIFY*");
		PropertiesDescriptionDefinition pddProperties = new PropertiesDescriptionDefinition(htValues,htDescriptions);
		
		// Tags
		HashSet<String> hsTags = new HashSet<String>();
		hsTags.add("system");
		hsTags.add("parallelization");
		hsTags.add("reduce");
 		TagsDescription tagDesc = new TagsDescription(hsTags);
		
		String sRunnable = "java";
		String sFiringPolicy = "any";
		String sFormat = "java/class";
		
		try {
			ecdRes = new ExecutableComponentDescription(resExecutableComponent,
					sName, sDescription, sRights, sCreator, dateCreation,
					sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
					setInputs, setOutputs, pddProperties, tagDesc);
			
		} catch (CorruptedDescriptionException e) {
			log.severe("An exception should have not been trown: "+e);
		}
		
		return ecdRes;
	}
	
	/** This method is invoked when the Meandre Flow is being prepared for 
	 * getting run.
	 *
	 * @param ccp The properties associated to a component context
	 */
	@SuppressWarnings("unchecked")
	public void initialize ( ComponentContextProperties ccp ) {
		iNumberOfMapTargets = Integer.valueOf(ccp.getProperty("number-of-inputs"));
		iRoundRobin = 0;
		
		// Create the temporary queues required to maintain the order
		cqa = new LinkedList[iNumberOfMapTargets];
		for ( int i=0 ; i<iNumberOfMapTargets ; i++ )
			cqa[i] = new LinkedList<Object>();
		
		// Create the translation array
		htTranslation = new Hashtable<String,Integer>();
		for ( int i=0 ; i<iNumberOfMapTargets ; i++ )
			htTranslation.put("object-"+i,i);
	}
	
	/** When Meandre schedules a component for execution, this method is 
	 * invoked. The ComponentContext object encapsulate the API a component 
	 * may use to interact with Meandre infrastructure.
	 * 
	 * @param cc The Meandre component context object
	 * @throws ComponentExecutionException If a fatal condition arises during 
	 *         the execution of a component, a ComponentExecutionException 
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context 
	 *         access was detected
	 */
	public void execute ( ComponentContext cc ) 
	throws ComponentExecutionException, ComponentContextException {
		
		updateQueues(cc);
		pushOrderedData(cc);
	}

	/** Pull the data from the inputs and pushes it to the ordering queues.
	 * 
	 * @param cc The component context
	 * @throws ComponentContextException Something went wrong while querying the component context
	 * 
	 */
	private void updateQueues(ComponentContext cc) throws ComponentContextException {
		for ( String sInput:cc.getInputNames() )
			if ( cc.isInputAvailable(sInput))
				cqa[htTranslation.get(sInput)].offer(cc.getDataComponentFromInput(sInput));
	}
	
	/** Pushed the data from the queues following the proper order
	 * 
	 * @param cc The component context
	 * @throws ComponentContextException Something went wrong while querying the component context
	 * 
	 */
	private void pushOrderedData(ComponentContext cc) throws ComponentContextException {
		while ( !cqa[iRoundRobin].isEmpty() ) {
			// Push the data from the current queue
			cc.pushDataComponentToOutput("object", cqa[iRoundRobin].poll());
			// Move to the next queue
			iRoundRobin = (iRoundRobin+1)%iNumberOfMapTargets;
		}
	}

	/** This method is called when the Menadre Flow execution is completed.
	 *
	 * @param ccp The properties associated to a component context
	 */
	public void dispose ( ComponentContextProperties ccp ) {
		iNumberOfMapTargets = iRoundRobin = 0;
		cqa = null;
		htTranslation = null;
	}
}
