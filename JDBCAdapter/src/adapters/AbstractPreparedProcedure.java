package adapters;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import mutua.icc.instrumentation.Instrumentation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import adapters.exceptions.PreparedProcedureException;

/** <pre>
 * AbstractPreparedProcedure.java  --  $Id: JDBCPreparedProceduresHelper.java,v 1.1 2010/07/01 22:02:14 luiz Exp $
 * ==============================
 * (created by luiz, Dec 15, 2008)
 *
 * A member of "JDBC Adapter Configuration" pattern to abstract a 'PreparedProcedure', freeing it from the
 * need to be associated with a 'Connection' -- and responsible for building a 'PreparedProcedure' when requested.
 * Also keeps a cache of 'IJDBDAdapterSQLStatementDefinition' to 'PreparedProcedure(conn)', for optimization purposes
 */

public class AbstractPreparedProcedure {
	
	private String                            preparedProcedureSQL;
	private IJDBCAdapterParameterDefinition[] params;
	private Connection[]                      connectionPool;
	private PreparedStatement[][]             preparedStatementsList;

	/** Keep the structures needed to transform 'sqlStatementBits' into a 'PreparedProcedure',
	 *  according to the "JDBC Adapter Configuration" pattern. */
	public AbstractPreparedProcedure(Connection[] connectionPool, Object... sqlStatementBits) {
		preparedProcedureSQL     = calculatePreparedProcedureSQL(sqlStatementBits);
		params                   = calculateParameters(sqlStatementBits);
		this.connectionPool      = connectionPool;
		preparedStatementsList   = new PreparedStatement[connectionPool.length][1];
	}

	/** returns the 'sqlStatement' to be used to construct 'PreparedProcedures' associated with this instance */
	public String getPreparedProcedureSQL() {
		return preparedProcedureSQL;
	}
	
	/** Uses the internal cache mechanism to efficiently retrieve a ready to use a 'PreparedStatement' */
	public PreparedStatement getPreparedStatement(int connIndex, Object... parametersAndValuesPairs) throws SQLException {
		Connection conn = connectionPool[connIndex];
		// search for an available prepared statement on the list and return it
		synchronized (preparedStatementsList) {
			PreparedStatement[] preparedStatements = preparedStatementsList[connIndex];
			PreparedStatement psCandidate;
			for (int psIndex=0; psIndex<preparedStatements.length; psIndex++) {
				psCandidate = preparedStatements[psIndex];
				if (psCandidate != null) {
					// take from the pool and give it to the caller, if it is valid
					preparedStatements[psIndex] = null;
					return fillPreparedStatement(psCandidate, parametersAndValuesPairs);
				}
			}
		}
		// no prepared procedure available. create a brand new one
		PreparedStatement ps = conn.prepareStatement(preparedProcedureSQL);
		ps.setPoolable(true);
		return fillPreparedStatement(ps, parametersAndValuesPairs);
	}
	
	/** return a known connection prepared statement to the pool -- faster */
	public void returnToThePool(int connIndex, PreparedStatement ps) {
		// search for a null position on the list to insert the prepared statement into
		synchronized (preparedStatementsList) {
			PreparedStatement[] preparedStatements = preparedStatementsList[connIndex];
			int psIndex;
			for (psIndex=0; psIndex<preparedStatements.length; psIndex++) {
				if (preparedStatements[psIndex] == null) {
					break;
				}
			}
			// expand the array, if necessary
			if (psIndex == preparedStatements.length) {
				preparedStatements = Arrays.copyOf(preparedStatements, preparedStatements.length+1);
				preparedStatementsList[connIndex] = preparedStatements;
				Instrumentation.reportDebug("PreparedStatementList '"+preparedProcedureSQL+"'["+connIndex+"] just grew to "+preparedStatements.length+" elements!");
			}
			// return to the pool
			preparedStatements[psIndex] = ps;
		}
	}
	
	/** return an unknown connection prepared statement to the pool -- slower */
	public void returnToThePool(PreparedStatement ps) throws SQLException {
		Connection conn = ps.getConnection();
		// find the prepared statement list which 'ps' belongs to
		int connIndex;
		for (connIndex=0; connIndex<connectionPool.length; connIndex++) {
			if (conn == connectionPool[connIndex]) {
				break;
			}
		}
		returnToThePool(connIndex, ps);
	}
	
	/** walks through all prepared statements, cleaning the ones that are no longer usable.
	 *  This method was designed to be called after a cleaning on the connection pool has been made */
	public void checkPreparedStatements() throws SQLException {
		int connIndex;
		for (connIndex=0; connIndex<connectionPool.length; connIndex++) {
			Connection conn = connectionPool[connIndex];
			PreparedStatement[] preparedStatements = preparedStatementsList[connIndex];
			for (int psIndex=0; psIndex<preparedStatements.length; psIndex++) {
				PreparedStatement ps = preparedStatements[psIndex];
				// remove prepared procedures that are no longer valid or that belongs to a connection that is no longer used
				if ((ps != null) && ((ps.isClosed()) || (ps.getConnection() != conn)) ) {
					Instrumentation.reportDebug("AbstractPreparedProcedure '"+preparedProcedureSQL+"': Connection #"+connIndex+", PreparedStatement #"+psIndex+" is invalid. Removing...");
					preparedStatements[psIndex] = null;
				}
			}
		}
	}

	/** Fill a prepared statement with the values needed to execute it's query */
	public PreparedStatement fillPreparedStatement(PreparedStatement ps, Object... parametersAndValuesPairs) throws SQLException {
		int pairsLength = parametersAndValuesPairs.length/2;
		for (int paramsIndex=0; paramsIndex<params.length; paramsIndex++) {
			IJDBCAdapterParameterDefinition parameterFromConstructor = params[paramsIndex];
			for (int pairsIndex=0; pairsIndex<pairsLength; pairsIndex++) {
				IJDBCAdapterParameterDefinition parameterFromPairs = (IJDBCAdapterParameterDefinition)parametersAndValuesPairs[pairsIndex*2];
				if (parameterFromPairs == parameterFromConstructor) {
					Object value = parametersAndValuesPairs[pairsIndex*2+1];
					if (value instanceof String) {
						ps.setString(paramsIndex+1, (String)value);
					} else if (value instanceof Integer) {
						ps.setInt(paramsIndex+1, ((Integer)value).intValue());
					} else if (value instanceof Long) {
						ps.setLong(paramsIndex+1, ((Long)value).longValue());
					} else if (value instanceof byte[]) {
						ps.setBytes(paramsIndex+1, (byte[])value);
					} else if (value instanceof int[]) {
						int[] intArray = (int[])value;
						Object[] genericArray = new Object[intArray.length];
						System.arraycopy(intArray, 0, genericArray, 0, intArray.length);
						ps.setArray(paramsIndex+1, ps.getConnection().createArrayOf("int", genericArray));
					} else if (value instanceof String[]) {
						String[] stringArray = (String[])value;
						Object[] genericArray = new Object[stringArray.length];
						System.arraycopy(stringArray, 0, genericArray, 0, stringArray.length);
						ps.setArray(paramsIndex+1, ps.getConnection().createArrayOf("text", genericArray));
					} else if (value instanceof Serializable) {
						ps.setObject(paramsIndex+1, value);
					} else {
						throw new PreparedProcedureException("buildPreparedStatement: Don't know how to handle the type for the parameter named '" + 
						                                     parameterFromPairs.getParameterName() + "' in query '" + preparedProcedureSQL + "'");
					}
				}
			}
		}
		return ps;
	}

	
	/** method to build an SQL Statement that can be used on construct a 'PreparedProcedure'.
	 *  Ex: sqlStatements := {"INSERT INTO MyTable VALUES (",parameters.ID,", ",parameters.PHONE,")"}
	 *  where 'parameters.ID' and 'parameters.PHONE' are instances of {@link IJDBCAdapterParameterDefinition} */
	private static String calculatePreparedProcedureSQL(Object[] sqlStatementBits) {
		StringBuffer preparedProcedureSQL = new StringBuffer();
		for (Object sqlStatementBit : sqlStatementBits) {
			if (sqlStatementBit instanceof String) {
				preparedProcedureSQL.append((String)sqlStatementBit);
			} else if (sqlStatementBit instanceof Integer) {
				preparedProcedureSQL.append((Integer)sqlStatementBit);
			} else if (sqlStatementBit instanceof IJDBCAdapterParameterDefinition) {
				preparedProcedureSQL.append('?');
			} else if (sqlStatementBit instanceof Object[]) {
				preparedProcedureSQL.append(calculatePreparedProcedureSQL((Object[])sqlStatementBit));
			} else {
				throw new NotImplementedException();
			}
		}
		return preparedProcedureSQL.toString();
	}

	/** method to build the parameters array for the 'PreparedStatement' associated with 'sqlStatementBits'.
	 *  Note: parameters are not unique, for they may happen n number of times in the sql statement */
	private static IJDBCAdapterParameterDefinition[] calculateParameters(Object[] sqlStatementBits) {
		ArrayList<IJDBCAdapterParameterDefinition> parameters = new ArrayList<IJDBCAdapterParameterDefinition>(sqlStatementBits.length);
		for (Object sqlStatementBit : sqlStatementBits) {
			if (sqlStatementBit instanceof IJDBCAdapterParameterDefinition) {
				parameters.add((IJDBCAdapterParameterDefinition)sqlStatementBit);
			} else if (sqlStatementBit instanceof Object[]) {
				for (IJDBCAdapterParameterDefinition parameter : calculateParameters((Object[])sqlStatementBit)) {
					parameters.add(parameter);
				}
			}
		}
		return parameters.toArray(new IJDBCAdapterParameterDefinition[parameters.size()]);
	}

}