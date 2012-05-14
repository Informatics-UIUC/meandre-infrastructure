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
 * component when ANY the inputs are populated with at least one data
 * component.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class WrappedComponentAnyInputRequired extends WrappedComponent {

	/** First firing */
	private boolean bFirst;

	/** Builds a runnable component wrapper given the abstracted EcecutableComponent.
	 * This wrapped component just fires the execution of and executalbe
     * component when ANY the inputs are populated with at least one data
     * component.
     *
	 * @param sFlowUniqueID The flow unique execution ID
	 * @param sComponentInstanceID The instance unique ID
	 * @param ec The executable component to wrap
	 * @param setInputs The input active buffers
	 * @param setOutputs The output active buffers
	 * @param htOutputMap the ouput identifier name
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
	public WrappedComponentAnyInputRequired(String sFlowUniqueID,String flowID,
			String sComponentInstanceID, String sComponentInstanceName, ExecutableComponent ec,
			Set<ActiveBuffer> setInputs, Set<ActiveBuffer> setOutputs,
			Hashtable<String, String> htOutputMap,
			Hashtable<String, String> htInputLogicNameMap,
			Hashtable<String, String> htOutputLogicNameMap, ThreadGroup tg,
			String sThreadName, Hashtable<String, String> htProperties, MrProbe thdMrProbe,
			CoreConfiguration cnf, PrintStream console, Properties flowParams)
			throws InterruptedException {
		super(sFlowUniqueID, flowID,sComponentInstanceID, sComponentInstanceName, ec, setInputs, setOutputs, htOutputMap, htInputLogicNameMap, htOutputLogicNameMap, tg, sThreadName, htProperties, thdMrProbe,cnf,console, flowParams);

		this.bFirst = true;
	}

	/** The wrapped component is ready for execution.
	 *
	 * @return A boolean asking if
	 */
	@Override
    protected boolean isExecutable()  {
		boolean bRes      = false;

		String[] saIN = cc.getInputNames();
		for ( String sInput:saIN ) {
			try {
				if ( cc.isInputAvailable(sInput) ) {
					bRes = true;
					break;
				}
			} catch (ComponentContextException e) {
				synchronized (baStatusFlags) {
					baStatusFlags[TERMINATION] = true;
				}
				log.severe("The requested input does not exist "+e.toString());
			}
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
			// Dangling output case. Never fire.
			return false;
		}
	}

}
