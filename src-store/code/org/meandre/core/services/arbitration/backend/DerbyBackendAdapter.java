/**
 * 
 */
package org.meandre.core.services.arbitration.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.meandre.core.logger.KernelLoggerFactory;

/** Implements the Derby backend adapter. This adapter translates
 * the requests into the proper Derby friendly queries.
 * 
 * @author Xavier Llorˆ
 *
 */
public class DerbyBackendAdapter 
extends BackendAdapter {
	
	/** The name of the resource file containing the query mapping */
	static final String DERBY_QUERY_MAP_FILE = "query_map_derby.xml";

	/** Initialize the query map */
	public DerbyBackendAdapter() {
		try {
			super.propMapping.loadFromXML(DerbyBackendAdapter.class.getResourceAsStream(DERBY_QUERY_MAP_FILE));
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Derby query map missing! "+baos.toString());
		}
	}

	/** Return the number of queries stored in the adapter.
	 * 
	 * @return The number of queries
	 */
	public int getNumberOfQueries () {
		return propMapping.size();
	}
}
