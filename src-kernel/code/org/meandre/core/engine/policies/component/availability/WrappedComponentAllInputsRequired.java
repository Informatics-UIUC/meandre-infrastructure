package org.meandre.core.engine.policies.component.availability;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.engine.ActiveBuffer;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.WrappedComponent;

/** This wrapped component just fires the execution of and executalbe
 * component when ALL the inputs are populated with at least one data
 * component.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class WrappedComponentAllInputsRequired extends WrappedComponent {

    /** Flag for first firing. */
    private boolean bFirst = true;

    /** Builds a runnable component wrapper given the abstracted EcecutableComponent.
     * This wrapped component just fires the execution of and executalbe
     * component when ALL the inputs are populated with at least one data
     * component.
     *
     * @param sFlowUniqueID A unique flow execution ID
     * @param sComponentInstanceID The component instance ID
     * @param ec The executable component to wrap
     * @param setInputs The input active buffers
     * @param setOutputs The output active buffers
     * @param htOutputMap The output identifier map
     * @param htOutputLogicNameMap The input logic name map
     * @param htInputLogicNameMap The output logic name map
     * @param tg The thread group holding the thread
     * @param sThreadName The name of the thread
     * @param htProperties The component properties
     * @param thdMrProbe The MrProbe thread
     * @param cnf The core configuration object
     * @param console The output console
     * @throws InterruptedException The semaphore could not be adquired twice
     */
    public WrappedComponentAllInputsRequired(String sFlowUniqueID,String flowID,
            String sComponentInstanceID, String sComponentInstanceName, ExecutableComponent ec,
            Set<ActiveBuffer> setInputs, Set<ActiveBuffer> setOutputs,
            Hashtable<String, String> htOutputMap,
            Hashtable<String, String> htInputLogicNameMap,
            Hashtable<String, String> htOutputLogicNameMap, ThreadGroup tg,
            String sThreadName, Hashtable<String, String> htProperties, MrProbe thdMrProbe,
            CoreConfiguration cnf, PrintStream console, Properties flowParams)
            throws InterruptedException {
        super(sFlowUniqueID,flowID, sComponentInstanceID, sComponentInstanceName, ec, setInputs, setOutputs, htOutputMap, htInputLogicNameMap, htOutputLogicNameMap, tg, sThreadName, htProperties, thdMrProbe, cnf, console, flowParams);

        this.bFirst = true;
    }

    /** The wrapped component is ready for execution.
     *
     * @return A boolean indicating whether the component is ready for execution
     */
    @Override
    protected boolean isExecutable()  {
        boolean bRes = true;

        String[] saIN = cc.getInputNames();
        for ( String sInput:saIN )
            try {
                if ( !cc.isInputAvailable(sInput) ) {
                    bRes = false;
                    break;
                }
            } catch (ComponentContextException e) {
                synchronized (baStatusFlags) {
                    baStatusFlags[TERMINATION] = true;
                }
                log.severe("The requested input does not exist "+e.toString());
            }

        if ( saIN.length==hasNInputs && hasNInputs>0 )
            // All inputs connected
            return bRes;
        else if ( hasNInputs==0 ){
            // No inputs, so fire only once
            if ( bFirst ) {
                bFirst = false;
                return true;
            }
            else {
                return false;
            }
        }
        else {
            // Dangling input case. Never fire
            return false;
        }
    }

}
