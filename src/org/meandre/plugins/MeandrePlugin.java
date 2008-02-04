package org.meandre.plugins;

/**Interface implemented by the plugins
 *
 * @author Amit Kumar
 * @created Jan 10, 2008
 *
 */
public interface MeandrePlugin {
	String getName();
	boolean isServlet();
	boolean isResource();
	String getAlias();
	String getPluginClass();
}
