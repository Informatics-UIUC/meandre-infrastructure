/**
 * 
 */
package org.meandre.core.services.arbitration.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.meandre.core.logger.KernelLoggerFactory;

/** Implements the Derby backend adapter. This adapter translates
 * the requests into the proper Derby friendly queries.
 * 
 * @author Xavier Llorˆ
 *
 */
public class MySQLBackendAdapter 
extends BackendAdapter {
	
	/** The name of the resource file containing the query mapping */
	static final String QUERY_MAP_FILE = "query_map_mysql.xml";

	/** Initialize the query map */
	public MySQLBackendAdapter() {
		super();
		try {
			Properties props = new Properties();
			props.loadFromXML(DerbyBackendAdapter.class.getResourceAsStream(QUERY_MAP_FILE));
			super.propQueryMapping.putAll(props);
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Derby query map missing! "+baos.toString());
		}
	}


}
