package adapters;

import static adapters.JDBCAdapterInstrumentationMethods.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import mutua.icc.instrumentation.Instrumentation;
import adapters.exceptions.JDBCAdapterError;

/** <pre>
 * JDBCAdapter.java  --  $Id: JDBCHelper.java,v 1.1 2010/07/01 22:02:14 luiz Exp $
 * ================
 * (created by luiz, Dec 12, 2008)
 *
 * Condenses common tasks while dealing with JDBC database drivers
 * 
 * Classes with specific configurations should extend this one and use it as stated in the
 * 'JDBCHelperTests' project.
 * 
 * This class implements 'InstrumentationPropagableEventsClient' to receive Exception notifications,
 * on which scenario, the connections are rechecked.
 */

public abstract class JDBCAdapter {
	
	
	// instance variables
	/** Indicates whether or not to perform any needed administrative tasks, such as database creation */
	protected boolean allowDataStructuresAssertion;
	/** Set to true to have all database queries logged */
	protected boolean shouldDebugQueries;
	/** Hostname (or IP) of the database server */
	protected String hostname;
	/** Connection port for the database server */
	protected int    port;
	/** The database name to connect to, containing the whole model needed by this application */
	protected String database;
	/** The user name to access the given 'DATABASE' -- note: administrative rights, such as the creation of tables, might be necessary */
	protected String user;
	/** The database plain text password for provided 'USER' */
	protected String password;
	/** the connection pool. Should be set to the statically defined pool set on each specialized adapter's class */ 
	private Connection[] pool = null;
	
	
	protected JDBCAdapter() {}


	/*******************
	** DATABASE SETUP **
	*******************/
	
	// load the needed JDBC drivers
	private static void loadDriverClasses(Class<?> jdbcDriverClass) {
		// simply getting a Class instance already means the class is loaded -- unless one is allowing ProGuard to optimize methods...
	}
	
	// verifies that the appropriate database exists, creating if necessary
	private void assureDatabaseIsOk() throws SQLException {
		Connection con = createAdministrativeConnection();
		if (con == null) {
			reportAdministrationWarningMessage("Specialized 'JDBCAdapter' class '"+getClass().getName()+"' states it can't handle database administration features -- therefore we are not going to check if the database '"+database+"' exists");
			return;
		}
		Statement stm = con.createStatement();
		Object[][] databases = getArrayFromQueryExecution(con, getShowDatabasesCommand());

		// search
		for (int i=0; i<databases.length; i++) {
			String fetchedDatabase = ((String)databases[i][0]).toLowerCase();
			if (database.toLowerCase().equals(fetchedDatabase)) {
				// already exists, do nothing
				return;
			}
		}
		
		// database does not exist. Create it
		reportAdministrationWarningMessage("Database '"+database+"' seems not to exist. Attempting to create it...");
		String createDatabaseSQL = "CREATE DATABASE "+database+";";
		if (shouldDebugQueries) {
			reportDatabaseSQL(createDatabaseSQL);
		}
		stm.executeUpdate(createDatabaseSQL);
		reportAdministrationWarningMessage("Database '"+database+"': created.");
		
		stm.close();
		con.close();
	}	

	// verifies that the appropriate tables exist, creating if needed
	private void assureTablesAreOk() throws SQLException {
		SQLException t = null;
		String[][] tableDefinitions = getTableDefinitions();
		if (tableDefinitions == null) {
			reportAdministrationWarningMessage("Specialized 'JDBCAdapter' class '"+getClass().getName()+"' states it can't handle table administration features -- therefore we are not going to check if the needed tables exist");
			return;
		}

		Connection con = createDatabaseConnection();
		Statement stm = con.createStatement();
		String requiredTableName = null;
		try {
			if (shouldDebugQueries) {
				reportDatabaseSQL(getShowTablesCommand());
			}
			Object[][] tables = getArrayFromQueryExecution(con, getShowTablesCommand());
	
			// match
			for (int i=0; i<tableDefinitions.length; i++) {
				requiredTableName         = tableDefinitions[i][0];
				String[] tableCreationStatements = tableDefinitions[i];		// to be used from [1] on
				// find it
				boolean found = false;
				for (int j=0; j<tables.length; j++) {
					String observedTableName = ((String)tables[j][0]).toLowerCase();
					if (requiredTableName.toLowerCase().equals(observedTableName)) {
						found = true;
					}
				}
				if (!found) {
					reportAdministrationWarningMessage("Table '"+requiredTableName+"' seems not to exist. Attempting to create it...");
					for (int j=1; j<tableCreationStatements.length; j++) {
						if (shouldDebugQueries) {
							reportDatabaseSQL(tableCreationStatements[j]);
						}
						stm.addBatch(tableCreationStatements[j]);
					}
					int[] result = stm.executeBatch();
					reportAdministrationWarningMessage("Table '"+requiredTableName+"': created uppon the execution of "+(tableCreationStatements.length-1)+" statement(s).");
				}
			}
		} catch (SQLException e) {
			t = e;
			Instrumentation.reportThrowable(e, "Error creating table '"+requiredTableName+"'");
			for (SQLException nextException = e.getNextException(); nextException != null; nextException = nextException.getNextException()) {
				Instrumentation.reportThrowable(nextException, "Neasted exception while creating table '"+requiredTableName+"'");
			}
		} finally {
			stm.close();
			con.close();
		}
		if (t != null) {
			throw t;
		}
	}


	/****************************
	** DATABASE INFRASTRUCTURE **
	****************************/


	/** creates a connection able to create the database */
	protected abstract Connection createAdministrativeConnection() throws SQLException;
	
	/** creates a connection able to manipulate the database structure and contents */
	protected abstract Connection createDatabaseConnection() throws SQLException;
	
	private int connectionPoolCounter = 0;
	/** returns a connection reference, from the pool */
	private int getNextConnectionPoolIndex() {
		synchronized (pool) {
			return (connectionPoolCounter++)%pool.length;
		}
	}
	
	/** returns the number of columns in this 'ResultSet' assuming it is already initialized
	/*  (that is, 'rs.next()' has already been called) */
	private static int getColumnCount(ResultSet rs) {
		int columnCount = 0;
		while (true) try {
			rs.getObject(columnCount+1);
			columnCount++;
		} catch (SQLException e) {
			break;
		}
		return columnCount;
	}

	/** retrieves an array with all elements in the 'ResultSet' generated by the execution of the
	/*  provided 'sql' query */
	private static Object[][] getArrayFromQueryExecution(PreparedStatement ps) throws SQLException {
		ArrayList<Object[]> returnBuffer = new ArrayList<Object[]>();
		int columnCount = -1;
		Object[] rowContents;

		// execute the query and traverse results
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			// determine the number of columns
			if (columnCount == -1) {
				columnCount = getColumnCount(rs);
			}
			
			// fetch the contents for this row
			rowContents = new Object[columnCount];
			for (int i=1; i<=columnCount; i++) {
				rowContents[i-1] = rs.getObject(i);
			}
			returnBuffer.add(rowContents);
		}
		
		rs.close();
		
		return returnBuffer.toArray(new Object[returnBuffer.size()][Math.max(columnCount, 0)]);
	}
	
	protected static Object[][] getArrayFromQueryExecution(Connection connection, String sql) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql);
		Object[][] result = getArrayFromQueryExecution(ps);
		ps.close();
		return result;
	}

	private static Hashtable<String, Boolean> assuredDatabases = new Hashtable<String, Boolean>();
	/** check database and tables presence... create if needed.
	/* assure it will be done only once during the life of the virtual machine */
	protected void assureDataStructures() {
		String key = hostname + "_" + port + "_" + database + "_" + getClass().getCanonicalName() + "_" + Arrays.deepToString(getTableDefinitions());
		if (!assuredDatabases.containsKey(key)) try {
			assureDatabaseIsOk();
			assureTablesAreOk();
			assuredDatabases.put(key, true);
		} catch (Exception e) {
			throw new JDBCAdapterError("Error during database & tables verification/setup", e);
		}
	}
	
	/**********************************
	** METHODS FOR EXTENDING CLASSES **
	**********************************/
	 
	/** Returns the statement that will make the server list all user created tables for a given database */
	protected abstract String getShowTablesCommand();

	/** Returns the statement that will make the server list all user created databases */
	protected abstract String getShowDatabasesCommand();

	/** Returns the statement that will make the server delete all tables -- and possibly the database itself */
	protected abstract String[] getDropDatabaseCommand();

	/** tells what is the database structure, so we can manage to verify & create it
	 *  example:<pre>
	 *  return new String[][] {
	 * 	    {"SimpleTable", "CREATE TABLE SimpleTable (id int, phone char(20))"},
	 * 	    {"NotSoSimple", "CREATE TABLE NotSoSimple (id int NOT NULL AUTO_INCREMENT PRIMARY KEY, phone char(20) UNIQUE)",
	 * 	                    "INSERT INTO NotSoSimple VALUES (0, '2191234899')"},
	 *  }; */
	protected abstract String[][] getTableDefinitions();
	
	/** instantiate a brand new object to deal with the database, with it's own 'log' and data structure assertion option */	
	protected JDBCAdapter(
		Class<?> jdbcDriverClass, boolean allowDataStructuresAssertion, boolean shouldDebugQueries,
		String hostname, int port, String database, String user, String password, Connection[] pool) throws SQLException {

		this.allowDataStructuresAssertion = allowDataStructuresAssertion;
		this.shouldDebugQueries           = shouldDebugQueries;
		this.hostname      = hostname;
		this.port          = port;
		this.database      = database;
		this.user          = user;
		this.password      = password;
		this.pool = pool;
		
		
		loadDriverClasses(jdbcDriverClass);
		
		if (allowDataStructuresAssertion) {
			assureDataStructures();
		} else {
			reportAdministrationWarningMessage("WARNING: '"+this.getClass().getName()+"' instance initiated without data structures verification");
		}
		checkAndPopulatePoolOfConnections();
	}
	
	private void checkAndPopulatePoolOfConnections() throws SQLException {
		for (int i=0; i<pool.length; i++) {
			if (pool[i] != null) try {
				if (pool[i].isValid(30)) {
					reportAdministrationWarningMessage("Connection Pool: Connection #" + i + " is valid.");
					continue;
				} else {
					pool[i].close();
				}
			} catch (SQLException e) {}
			reportAdministrationWarningMessage("Connection Pool: Connection #" + i + " is invalid. Attempting to (re)open it...");
			pool[i] = createDatabaseConnection();
		}
	}

	
	/***************************
	** EXCEPTION NOTIFICATION **
	***************************/
	
	/** reports that an exception has happened and checks the environment to attempt to prevent further errors */
	private void handleException(SQLException e, AbstractPreparedProcedure abstractPreparedProcedure, Object[] parametersAndValuesPairs) {
		reportRetryingQueryDueToException(e, abstractPreparedProcedure, parametersAndValuesPairs);
		synchronized (pool) {
			try {
				checkAndPopulatePoolOfConnections();
				abstractPreparedProcedure.checkPreparedStatements();
			} catch (Throwable t) {
				Instrumentation.reportThrowable(t, "Exception while validating the pool of connections & prepared statements. Please consider checking the database and/or rebooting the server & restarting the application");
			}
		}
	}


	

	/**************************
	** PUBLIC ACCESS METHODS **
	**************************/
	
	private int rawInvokeUpdateProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		int connIndex = getNextConnectionPoolIndex();
		PreparedStatement ps = abstractPreparedProcedure.getPreparedStatement(connIndex, parametersAndValuesPairs);
		int result = ps.executeUpdate();
		abstractPreparedProcedure.returnToThePool(connIndex, ps);
		return result;
	}
	/** executes an INSERT, UPDATE, DELETE, and possibly other commands */
	public int invokeUpdateProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		if (shouldDebugQueries) {
			reportDatabaseSQL(abstractPreparedProcedure, parametersAndValuesPairs);
		}
		try {
			return rawInvokeUpdateProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		} catch (SQLException e) {
			handleException(e, abstractPreparedProcedure, parametersAndValuesPairs);
			return rawInvokeUpdateProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		}
	}
	
	/** Similar to {@link #rawInvokeUpdateProcedure}, but receives several 'parametersAndValuesPairs' */
	private int[] rawInvokeUpdateBatchProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object[][] parametersAndValuesPairsSet) throws SQLException {
		int connIndex = getNextConnectionPoolIndex();
		PreparedStatement ps = null;
		for (Object[] parametersAndValuesPairs : parametersAndValuesPairsSet) {
			if (ps == null) {
				ps = abstractPreparedProcedure.getPreparedStatement(connIndex, parametersAndValuesPairs);
				ps.addBatch();
			} else {
				abstractPreparedProcedure.fillPreparedStatement(ps, parametersAndValuesPairs);
				ps.addBatch();
			}
		}
		if (ps != null) {
			int[] results = ps.executeBatch();
			abstractPreparedProcedure.returnToThePool(connIndex, ps);
			return results;
		} else {
			return null;
		}
	}
	/** executes, efficiently, many INSERT, UPDATE and/or DELETE queries at once, using the JDBC batch features.
	 *  parametersAndValuesPairsSet := {parametersAndValuesPairs for command 1, parametersAndValuesPairs for command 2, ...} */
	public int[] invokeUpdateBatchProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object[][] parametersAndValuesPairsSet) throws SQLException {
		if (shouldDebugQueries) {
			for (Object[] parametersAndValuesPairs : parametersAndValuesPairsSet) {
				reportDatabaseSQL(abstractPreparedProcedure, parametersAndValuesPairs);
			}
		}
		try {
			return rawInvokeUpdateBatchProcedure(abstractPreparedProcedure, parametersAndValuesPairsSet);
		} catch (SQLException e) {
			handleException(e, abstractPreparedProcedure, parametersAndValuesPairsSet);
			return rawInvokeUpdateBatchProcedure(abstractPreparedProcedure, parametersAndValuesPairsSet);
		}
	}
	
	private Object rawInvokeScalarProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		int connIndex = getNextConnectionPoolIndex();
		PreparedStatement ps = abstractPreparedProcedure.getPreparedStatement(connIndex, parametersAndValuesPairs);
		ResultSet resultSet = ps.executeQuery();
		try {
			if (resultSet.next()) {
				// if the field is has binary data, try to read it as a java serializable object
				if (resultSet.getMetaData().getColumnType(1) == Types.LONGVARBINARY) try {
					InputStream is = resultSet.getBlob(1).getBinaryStream();
				    ObjectInputStream oip;
					oip = new ObjectInputStream(is);
				    Object javaObject = oip.readObject();
				    oip.close();
				    is.close();
				    return javaObject;
				} catch (IOException e1) {
				} catch (ClassNotFoundException e2) {}
				Object result = resultSet.getObject(1);
				return result;
			} else {
				return null;
			}
		} finally {
			resultSet.close();
			abstractPreparedProcedure.returnToThePool(connIndex, ps);
		}
	}
	/** executes SELECT statements that return a single value */
	public Object invokeScalarProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		if (shouldDebugQueries) {
			reportDatabaseSQL(abstractPreparedProcedure, parametersAndValuesPairs);
		}
		try {
			return rawInvokeScalarProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		} catch (SQLException e) {
			handleException(e, abstractPreparedProcedure, parametersAndValuesPairs);
			return rawInvokeScalarProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		}
	}
	
	private Object[] rawInvokeRowProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		int connIndex = getNextConnectionPoolIndex();
		PreparedStatement ps = abstractPreparedProcedure.getPreparedStatement(connIndex, parametersAndValuesPairs);
		Object[][] result = getArrayFromQueryExecution(ps);
		abstractPreparedProcedure.returnToThePool(connIndex, ps);
		if ((result == null) || (result.length == 0)) {
			return null;
		} else {
			return result[0];
		}
	}
	/** executes a query (typically via SELECT statement) that will return a single row, with some number of fields
	 *  in it, which the order is known -- possibly via SELECT a, b, c... clause */
	public Object[] invokeRowProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		if (shouldDebugQueries) {
			reportDatabaseSQL(abstractPreparedProcedure, parametersAndValuesPairs);
		}
		try {
			return rawInvokeRowProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		} catch (SQLException e) {
			handleException(e, abstractPreparedProcedure, parametersAndValuesPairs);
			return rawInvokeRowProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		}
	}
	
	private Object[][] rawInvokeArrayProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		int connIndex = getNextConnectionPoolIndex();
		PreparedStatement ps = abstractPreparedProcedure.getPreparedStatement(connIndex, parametersAndValuesPairs);
		Object[][] result = getArrayFromQueryExecution(ps);
		abstractPreparedProcedure.returnToThePool(connIndex, ps);
		return result;
	}
	/** executes a query (typically via SELECT statement) that will return a virtual table that can be contained into RAM -- that is, has a
	 *  few and foreseeable amount of elements -- possibly using the LIMIT clause */
	public Object[][] invokeArrayProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		if (shouldDebugQueries) {
			reportDatabaseSQL(abstractPreparedProcedure, parametersAndValuesPairs);
		}
		try {
			return rawInvokeArrayProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		} catch (SQLException e) {
			handleException(e, abstractPreparedProcedure, parametersAndValuesPairs);
			return rawInvokeArrayProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		}
	}

	private ResultSet rawInvokeVirtualTableProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		int connIndex = getNextConnectionPoolIndex();
		PreparedStatement ps = abstractPreparedProcedure.getPreparedStatement(connIndex, parametersAndValuesPairs);
		return ps.executeQuery();
	}
	/** executes a query (typically via SELECT statement) that will produce huge quantities of results and, thus, won't fit into RAM
	 *  the returned 'ResultSet' needs to be closed after use and the associated 'PreparedStatement', returned to the AbstractPreparedProcedure pool */
	public ResultSet invokeVirtualTableProcedure(AbstractPreparedProcedure abstractPreparedProcedure, Object... parametersAndValuesPairs) throws SQLException {
		if (shouldDebugQueries) {
			reportDatabaseSQL(abstractPreparedProcedure, parametersAndValuesPairs);
		}
		try {
			return rawInvokeVirtualTableProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		} catch (SQLException e) {
			handleException(e, abstractPreparedProcedure, parametersAndValuesPairs);
			return rawInvokeVirtualTableProcedure(abstractPreparedProcedure, parametersAndValuesPairs);
		}
	}
	
	/** erases all database contents -- solo for testing purposes */
	public void resetDatabase() {
		try {
			// erase all
			reportAdministrationWarningMessage("ATTENDING TO THE REQUEST OF ERASING ALL DATA OF DATABASE '"+database+"'");
			Connection conn = createAdministrativeConnection();
			Statement stm = conn.createStatement();
			
			for (String sql : getDropDatabaseCommand()) {
				if (shouldDebugQueries) {
					reportDatabaseSQL(sql);
				}
				stm.addBatch(sql);
			}
			stm.executeBatch();
			stm.close();
			conn.close();
			
			// recreate
			assureDatabaseIsOk();
			assureTablesAreOk();
		} catch (Exception e) {
			throw new JDBCAdapterError("Error during database & tables reset", e);
		}
	}

}