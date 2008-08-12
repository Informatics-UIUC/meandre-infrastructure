package org.meandre.plugins;

import java.util.Properties;
import java.util.logging.Logger;

import org.meandre.configuration.CoreConfiguration;

/**Interface implemented by the plugins
 *
 * @author Amit Kumar
 * @created Jan 10, 2008
 * @Modified by Xavier Llor&agrave;
 *
 */
public interface MeandrePlugin {
	
	/** The name of the plugin.
	 * 
	 * @return The name of the plugin
	 */
	public String getName();
	
	/** Check if the plugin is a Filter*/
	public boolean isFilter();
	
	/** Is the plugin a servlet?
	 * 
	 * @return True if it is a servlet
	 */
	public boolean isServlet();
	
	/** Is the plugin a resource?.
	 * 
	 * @return True if the plugin is a resource
	 */
	public boolean isResource();
	
	/** The alias name. If it is a servlet is the context path.
	 * 
	 * @return The alias
	 */
	public String getAlias();
	
	/** The plugin class name.
	 * 
	 * @return The class name
	 */
	public String getPluginClassName();
	
	/** Return the plugin properties.
	 * 
	 * @return The properties of the plugin
	 */
	public Properties getProperties();

	/** Sets the logger to use
	 * 
	 * @param log The logger
	 */
	public void setLogger(Logger log);

	/**Set this to true plugin was found and instantiated
	 * 
	 * @param success
	 */
	public void inited(Boolean success);
	
	/**Return true or false
	 * 
	 * @return
	 */
	public boolean isInited();
	
	/** Sets the core configuration object to use.
	 * 
	 * @param cnf The core configuration object
	 */
	public void setCoreConfiguration ( CoreConfiguration cnf );
}