/**
 *
 */
package org.meandre.core.system.components;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
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
import org.meandre.core.system.components.ext.StreamDelimiter;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/** This system component takes and input stream of object and fans it
 * out in a round-robin manner to the number of available outputs.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class MapExecutableComponent implements ExecutableComponent {

    /** The number of outputs */
    private int iNumberOfMapTargets;

    /** The current pushed value */
    private int iRoundRobin;

    /** Returns the on-the-fly descriptor generated for this component.
     *
     * @param iNumberOfMapTargets The number of outputs
     * @return The executable component description
     */
    public static ExecutableComponentDescription getExecutableComponentDescription( int iNumberOfMapTargets ) {
        Logger log = KernelLoggerFactory.getCoreLogger();

        String sBaseURL = "http://www.meandre.org/system/component/map/"+iNumberOfMapTargets;

        ExecutableComponentDescription ecdRes = null;

        Resource resExecutableComponent =  ModelFactory.createDefaultModel().createResource(sBaseURL);

        // General properties
        String sName = "Map";
        String sDescription = "Passes the input object to the possible "+iNumberOfMapTargets+" outputs by cycling through them in a round-robin fashion (equivalent to a scatter operation).";
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
        Resource resLocation = ModelFactory.createDefaultModel().createResource(sBaseURL+MapExecutableComponent.class.getName());

        // Empty input port
        Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
        Resource resDPDInput1 = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/input/object");
        String sDPDIn1Ident = resDPDInput1.toString();
        String sDPDIn1Name = "object";
        String sDPDIn1Desc = "The object to be maped";
        try {
            setInputs.add(new DataPortDescription(resDPDInput1,sDPDIn1Ident,sDPDIn1Name,sDPDIn1Desc));
        } catch (CorruptedDescriptionException e) {
            log.severe("An exception should have not been trown: "+e);
        }

        // Multiple output ports
        Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
        for ( int i=0 ; i<iNumberOfMapTargets ; i++ ) {
            Resource resDPDOutput = ModelFactory.createDefaultModel().createResource(resExecutableComponent.toString()+"/output/object-"+i);
            String sDPDIdent = resDPDOutput.toString();
            String sDPDName = "object-"+i;
            String sDPDDesc = "The mapped object";
            try {
                setOutputs.add(new DataPortDescription(resDPDOutput,sDPDIdent,sDPDName,sDPDDesc));
            } catch (CorruptedDescriptionException e) {
                log.severe("An exception should have not been trown: "+e);
            }
        }

        // Properties
        Hashtable<String,String> htValues = new Hashtable<String,String>();
        Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
        htValues.put("number-of-outputs", ""+iNumberOfMapTargets);
        htDescriptions.put("number-of-outputs", "The number of outputs for this map process. *DO NOT MODIFY*");
        PropertiesDescriptionDefinition pddProperties = new PropertiesDescriptionDefinition(htValues,htDescriptions);

        // Tags
        HashSet<String> hsTags = new HashSet<String>();
        hsTags.add("system");
        hsTags.add("parallelization");
        hsTags.add("map");
         TagsDescription tagDesc = new TagsDescription(hsTags);

        String sRunnable = "java";
        String sFiringPolicy = "all";
        String sFormat = "java/class";

        try {
            ecdRes = new ExecutableComponentDescription(resExecutableComponent,
                    sName, sDescription, sRights, sCreator, dateCreation,
                    sRunnable, sFiringPolicy, sFormat, setContext, resLocation,
                    setInputs, setOutputs, pddProperties, tagDesc, ExecutableComponentDescription.COMPUTE_COMPONENT);

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
    @Override
    public void initialize ( ComponentContextProperties ccp ) {
        iNumberOfMapTargets = Integer.valueOf(ccp.getProperty("number-of-outputs"));
        iRoundRobin = 0;
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
    @Override
    public void execute ( ComponentContext cc )
    throws ComponentExecutionException, ComponentContextException {
        Object obj = cc.getDataComponentFromInput("object");
        if ( obj instanceof StreamDelimiter ) {
            // Replicate out the object to all components
            StreamDelimiter sd = (StreamDelimiter)obj;
            // cc.getOutputConsole().print("Forwarding " + sd.getClass().getSimpleName() + " (streamId: " + sd.getStreamId() + ") to all " + iNumberOfMapTargets + " parallel instances....");
            for (int i = 0; i < iNumberOfMapTargets; i++)
                cc.pushDataComponentToOutput("object-" + i, sd);
        }
        else {
            // Push the object and round robin
            cc.pushDataComponentToOutput("object-" + iRoundRobin, obj);
            iRoundRobin = (iRoundRobin + 1) % iNumberOfMapTargets;
        }
    }

    /** This method is called when the Menadre Flow execution is completed.
     *
     * @param ccp The properties associated to a component context
     */
    @Override
    public void dispose ( ComponentContextProperties ccp ) {
        iNumberOfMapTargets = iRoundRobin = 0;
    }
}
