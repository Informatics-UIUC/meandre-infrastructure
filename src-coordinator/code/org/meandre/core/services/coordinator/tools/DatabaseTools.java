package org.meandre.core.services.coordinator.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.meandre.core.services.coordinator.backend.CoordinatorBackendAdapterException;

/** Basic methods to run queries on a database.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class DatabaseTools {

	/** Runs and update query against the backend.
	 * 
	 * @param sQuery The query to run the update
	 * @throws CoordinatorBackendAdapterException Thrown when there was a problem with the backend running the update
	 */
	public static void executeUpdateQuery(Connection conn, String sQuery)
	throws DatabaseBackendAdapterException {
		
		// Run the update
		try {
			Statement stm = conn.createStatement();
			stm.executeUpdate(sQuery);
		} catch (SQLException e) {
			throw new DatabaseBackendAdapterException(e);
		}
	}
	
	/** Runs an update prepare statement with the given values.
	 * 
	 * @param conn The connection object to use
	 * @param sQuery The query to run
	 * @param oaValues The values to use for the update
	 * @throws CoordinatorBackendAdapterException Something when wrong running the update
	 */
	public static int executeUpdateQueryWithParams(Connection conn, String sQuery, Object[] oaValues ) 
	throws DatabaseBackendAdapterException {
		
		// Run the update
		try {
			PreparedStatement pstm = conn.prepareStatement(sQuery);
			for ( int i=1,iMax=oaValues.length ; i<=iMax ; i++ )
				pstm.setObject(i, oaValues[i-1]);
			return pstm.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseBackendAdapterException(e);
		}
	}

	/** Return a list of list with the results of select query in text form.
	 * 
	 * @param conn The connection object to use
	 * @param sQuery The query to run
	 * @return The resulting list of lists of text
	 * @throws CoordinatorBackendAdapterException Something when wrong running the select
	 */
	public static List<List<String>> selectTextColumns (Connection conn, String sQuery ) 
	throws DatabaseBackendAdapterException {
		List<List<String>> lstRes = new LinkedList<List<String>>();
		try {
			Statement pstm = conn.createStatement();
			ResultSet rs = pstm.executeQuery(sQuery);
			int iColCount = rs.getMetaData().getColumnCount();
			
			while(rs.next()) {
				List<String> lstRow = new LinkedList<String>();
				for( int j=1 ; j<=iColCount ; j++) { 
					lstRow.add(rs.getString(j));
				}
				lstRes.add(lstRow);
			}
			rs.close();
		} catch (SQLException e) {
			throw new DatabaseBackendAdapterException(e);
		}
		return lstRes;
	}
	

	/** Return a list of list with the results of select query in text form populating the query with the
	 * provided parameters.
	 * 
	 * @param conn The connection object to use
	 * @param sQuery The query to run
	 * @param oaValues The parameters to set in the query
	 * @return The resulting list of lists of text
	 * @throws CoordinatorBackendAdapterException Something when wrong running the select
	 */
	public static List<List<String>> selectTextColumnsWithParams (Connection conn, String sQuery, Object [] oaValues ) 
	throws DatabaseBackendAdapterException {
		List<List<String>> lstRes = new LinkedList<List<String>>();
		try {
			PreparedStatement pstm = conn.prepareStatement(sQuery);
			for ( int i=1,iMax=oaValues.length ; i<=iMax ; i++ )
				pstm.setObject(i, oaValues[i-1]);
			ResultSet rs = pstm.executeQuery();
			int iColCount = rs.getMetaData().getColumnCount();
			
			while(rs.next()) {
				List<String> lstRow = new LinkedList<String>();
				for( int j=1 ; j<=iColCount ; j++) { 
					lstRow.add(rs.getString(j));
				}
				lstRes.add(lstRow);
			}
			rs.close();
		} catch (SQLException e) {
			throw new DatabaseBackendAdapterException(e);
		}
		return lstRes;
	}

	/** Return a list of list with the results of select query in text form.
	 * 
	 * @param conn The connection object to use
	 * @param sQuery The query to run
	 * @param iLimit The maxim number of entries returned (0 or negative to return all)
	 * @return The resulting list of lists of text with the column name
	 * @throws CoordinatorBackendAdapterException Something when wrong running the select
	 */
	public static List<Map<String,String>> selectTextColumnsWithName (Connection conn, String sQuery, int iLimit ) 
	throws DatabaseBackendAdapterException {
		List<Map<String,String>> lstRes = new LinkedList<Map<String,String>>();
		try {
			Statement pstm = conn.createStatement();
			ResultSet rs = pstm.executeQuery(sQuery);
			ResultSetMetaData rsMD = rs.getMetaData();
			int iColCount = rsMD.getColumnCount();
			int iCnt = 0;
			while(rs.next() && ( iLimit<=0 || iCnt<iLimit) ) {
				Map<String,String> mapRow = new Hashtable<String,String>();
				for( int j=1 ; j<=iColCount ; j++) { 
					String sColumnLabel = rsMD.getColumnLabel(j).toLowerCase();
					String sValue = rs.getString(j);
					sValue = ( sValue==null )?"":sValue.trim();
					mapRow.put(sColumnLabel,sValue);
				}
				lstRes.add(mapRow);
				iCnt++;
			}
			rs.close();
		} catch (SQLException e) {
			throw new DatabaseBackendAdapterException(e);
		}
		return lstRes;
	}

	/** Return a list of list with the results of select query in text form.
	 * 
	 * @param conn The connection object to use
	 * @param sQuery The query to run
	 * @param oaValues The values to use for the query
	 * @return The resulting list of lists of text
	 * @throws CoordinatorBackendAdapterException Something when wrong running the select
	 */
	public static List<List<String>> selectTextColumns (Connection conn, String sQuery, Object [] oaValues) 
	throws DatabaseBackendAdapterException {
		List<List<String>> lstRes = new LinkedList<List<String>>();
		try {
			PreparedStatement pstm = conn.prepareStatement(sQuery);
			for ( int i=1,iMax=oaValues.length ; i<=iMax ; i++ )
				pstm.setObject(i, oaValues[i-1]);
			ResultSet rs = pstm.executeQuery();
			int iColCount = rs.getMetaData().getColumnCount();
			
			while(rs.next()) {
				List<String> lstRow = new LinkedList<String>();
				for( int j=1 ; j<=iColCount ; j++) { 
					lstRow.add(rs.getString(j));
				}
				lstRes.add(lstRow);
			}
			rs.close();
		} catch (SQLException e) {
			throw new DatabaseBackendAdapterException(e);
		}
		return lstRes;
	}

}
