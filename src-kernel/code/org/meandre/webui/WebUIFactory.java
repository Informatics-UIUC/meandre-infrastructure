package org.meandre.webui;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.MrProper;
import org.meandre.core.logger.KernelLoggerFactory;

/** The web ui factory to manager allows modules to create, register,
 * remove, and destroy web ui components.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class WebUIFactory {

    /** The core root logger */
    protected static Logger log = KernelLoggerFactory.getCoreLogger();

    // /** The semaphore to implement mutual exclusion */
    // private static Semaphore semMutEX = new Semaphore(1,true);

    // /** The incremental port counter to start execution ports */
    // private static int iPortScroller = -1;

    /** The hashtable maintanins the current active webUIs */
    protected static Hashtable<String,WebUI> htActiveWebUI = new Hashtable<String,WebUI>();

    /** This hashtable maintains the port mapping to the webUI */
    protected static Hashtable<Integer,WebUI> htActivePortWebUI = new Hashtable<Integer, WebUI>();

    /** Get a WebUI for the given executing flow. If the webui does not exist
     * for the given flow, it gets created and initalized the first
     * time it is requrested.
     *
     * @param sFlowUniqueID The flow execution unique ID
     * @param cnf The core configuration
     * @return The webui object
     * @throws WebUIException An exception occurred while initializing a web
     */
    static public WebUI getWebUI ( String sFlowUniqueID, MrProper mrProper, MrProbe mrProbe, CoreConfiguration cnf, int port )
    throws WebUIException {
        WebUI webui = htActiveWebUI.get(sFlowUniqueID);

        if ( webui==null ) {
            webui = new WebUI(sFlowUniqueID,mrProper,mrProbe,port,log,cnf);
            htActiveWebUI.put(sFlowUniqueID, webui);
            htActivePortWebUI.put(port, webui);
        }

        return webui;
    }

    /** Gets a WebUI for the given executing flow. If the webui does not exist
     * returns null.
     *
     * @param sFlowUniqueID The flow execution unique ID
     * @return The webui object
     * @throws WebUIException An exception occurred while initializing a web
     */
    static public WebUI getExistingWebUI ( String sFlowUniqueID ) throws WebUIException {
        WebUI webui = htActiveWebUI.get(sFlowUniqueID);

        return webui;
    }

    /** Gets a WebUI for the given port. If the webui does not exist
     * returns null.
     *
     * @param iPort The port associated to the web ui
     * @return The webui object
     * @throws WebUIException An exception occurred while initializing a web
     */
    static public WebUI getExistingWebUIOnPort ( int iPort ) {
        return htActivePortWebUI.get(iPort);
    }


    /** Shuts down all the WebUI for the given executing flow. It
     * detaches all the handlers registerd by components, and shuts down
     * the server providing the acces point.
     *
     * @param sFlowUniqueID The flow execution unique ID
     * @throws WebUIException The server could not be shuted down
     */
    static public void disposeWebUI ( String sFlowUniqueID ) throws WebUIException {
        WebUI webui = htActiveWebUI.get(sFlowUniqueID);
        int iPort = webui.getPort();

        if ( webui!=null ) {
            try {
                webui.shutdown();
            } catch (Exception e) {
                throw new WebUIException(e);
            }
            htActiveWebUI.remove(sFlowUniqueID);
            htActivePortWebUI.remove(iPort);
        }
    }

    /** Returns the list of running flows.
     *
     * @return The list of running flow ids
     */
    static public List<String> getRunningFlowsWithWebUI () {
        WebUI [] wa = new WebUI[htActivePortWebUI.size()];
        LinkedList<String> lst = new LinkedList<String>();
        htActiveWebUI.values().toArray(wa);
        Arrays.sort(wa);
        for ( WebUI webui:wa )
            lst.add(webui.getFlowExecutionUniqueID());

        return lst;
    }

    /** Returns the set of ports running flows.
     *
     * @return The set of currently in use ports
     */
    static public Set<Integer> getActivePorts () {
        return htActivePortWebUI.keySet();
    }

}
