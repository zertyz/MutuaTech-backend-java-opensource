package main;

import java.sql.Connection;
import java.sql.SQLException;

import adapters.AbstractPreparedProcedure;
import adapters.IJDBCAdapterParameterDefinition;
import adapters.JDBCAdapter;
import adapters.PostgreSQLAdapter;

/** <pre>
 * PostgreSQLAdapterConfiguration.java
 * ===================================
 * (created by luiz, Jan 26, 2015)
 *
 * Provides the needed 'PostgreSQLAdapter' configuration to access and operate on the database
 *
 * @see PostgreSQLAdapter
 * @version $Id$
 * @author luiz
 */

public class PostgreSQLAdapterConfiguration extends PostgreSQLAdapter {
	
	// Mutua Configurable Class pattern
	///////////////////////////////////
	
	/** this class' singleton instance */
	private static PostgreSQLAdapterConfiguration instance = null;
	
	// JDBCAdapter default values
	/** @see JDBCAdapter#hostname */
	private static String HOSTNAME;
	/** @see JDBCAdapter#port */
	private static int    PORT;
	/** @see JDBCAdapter#database */
	private static String DATABASE;
	/** @see JDBCAdapter#user */
	private static String USER;
	/** @see JDBCAdapter#password */
	private static String PASSWORD;
	/** @see JDBCAdapter#allowDataStructuresAssertion */
	private static boolean ALLOW_DATA_STRUCTURES_ASSERTION;
	/** @see JDBCAdapter#shouldDebugQueries */
	private static boolean SHOULD_DEBUG_QUERIES;	
	
	/** method to be called when attempting to configure the singleton for new instances of this 'PostgreSQLAdapter'.
	 *  @param allowDataStructuresAssertion see {@link #ALLOW_DATA_STRUCTURES_ASSERTION}
	 *  @param shouldDebugQueries           see {@link #SHOULD_DEBUG_QUERIES}
	 *  @param hostname                     see {@link #HOSTNAME}
	 *  @param port                         see {@link #PORT}
	 *  @param database                     see {@link #DATABASE}
	 *  @param user                         see {@link #USER}
	 *  @param password                     see {@link #PASSWORD} */
	public static void configureDefaultValuesForNewInstances(
		boolean allowDataStructuresAssertion, boolean shouldDebugQueries,
	    String hostname, int port, String database, String user, String password) throws SQLException {
				
		ALLOW_DATA_STRUCTURES_ASSERTION = allowDataStructuresAssertion;
		SHOULD_DEBUG_QUERIES            = shouldDebugQueries;
		HOSTNAME = hostname;
		PORT     = port;
		DATABASE = database;
		USER     = user;
		PASSWORD = password;

		instance = null;
	}

	private PostgreSQLAdapterConfiguration() throws SQLException {
		super(ALLOW_DATA_STRUCTURES_ASSERTION, SHOULD_DEBUG_QUERIES, HOSTNAME, PORT, DATABASE, USER, PASSWORD);
	}

	@Override
	protected String[][] getTableDefinitions() {
		return new String[][] {
			{"SimpleTable", "CREATE TABLE SimpleTable (id INT, phone TEXT)"},
			{"NotSoSimple", "CREATE TABLE NotSoSimple (id    SERIAL NOT NULL PRIMARY KEY, " +
			                "                          phone TEXT   UNIQUE)",

			                "CREATE OR REPLACE FUNCTION somefunc() RETURNS integer AS $$\n"+
	                        "DECLARE\n"+
	                        "    quantity integer := 30;\n"+
	                        "BEGIN\n"+
	                        "    RAISE NOTICE 'Quantity here is %', quantity;  -- Quantity here is 30\n"+
	                        "    quantity := 50;\n"+
	                        "    --\n"+
	                        "    -- Create a subblock\n"+
	                        "    --\n"+
	                        "    DECLARE\n"+
	                        "        quantity integer := 80;\n"+
	                        "    BEGIN\n"+
	                        "        RAISE NOTICE 'Quantity here is %', quantity;  -- Quantity here is 80\n"+
	                        "    END;\n"+
	                        "    \n"+
	                        "    RAISE NOTICE 'Quantity here is %', quantity;  -- Quantity here is 50\n"+
	                        "\n"+
	                        "    RETURN quantity;\n"+
	                        "END;\n"+
	                        "$$ LANGUAGE plpgsql;\n",

	                        "CREATE OR REPLACE FUNCTION UpdateOrInsertNotSoSimple(new_phone CHAR(20), new_id int) RETURNS void AS $$\n"+
	                        "BEGIN\n"+
	                        "UPDATE NotSoSimple SET phone=new_phone WHERE id=new_id;\n"+
	                        "IF NOT FOUND THEN \n"+
	                        "INSERT INTO NotSoSimple(phone) VALUES (new_phone);\n"+
	                        "END IF;\n"+
	                        "END;\n"+
	                        "$$ LANGUAGE plpgsql;\n"}
		};
	}
	
	/***************
	** PARAMETERS **
	***************/
	
	public enum PostgreSQLParameters implements IJDBCAdapterParameterDefinition {
		ID   (Integer.class),
		PHONE(String.class),
		VALUE_LIST(String[].class),
		
		;
		
		private PostgreSQLParameters(Class<? extends Object> a) {}

		@Override
		public String getParameterName() {
			return name();
		}
	}
	
	/***************
	** STATEMENTS **
	***************/
	
	public static final class PostgreSQLStatements {
		/** Inserts an 'ID' and 'PHONE' into the 'InsertSimpleRecord' */
		public static final AbstractPreparedProcedure InsertSimpleRecord = new AbstractPreparedProcedure(connectionPool,
			"INSERT INTO SimpleTable VALUES (",PostgreSQLParameters.ID,", ",PostgreSQLParameters.PHONE,")");
		/** Returns the 'PHONE' associated with 'ID' */
		public static final AbstractPreparedProcedure GetSimpleIdFromPhone = new AbstractPreparedProcedure(connectionPool,
			"SELECT phone FROM SimpleTable WHERE id=",PostgreSQLParameters.ID);
		/** removes the record denoted by 'ID' */
		public static final AbstractPreparedProcedure DeleteSimpleRecord = new AbstractPreparedProcedure(connectionPool,
			"DELETE FROM SimpleTable WHERE id=",PostgreSQLParameters.ID);
		/** Inserts a 'PHONE' into the 'InsertNotSoSimpleRecord' */
		public static final AbstractPreparedProcedure InsertNotSoSimpleRecord = new AbstractPreparedProcedure(connectionPool,
			"INSERT INTO NotSoSimple(phone) VALUES (",PostgreSQLParameters.PHONE,")");
		/** Calls a stored procedure without parameters */
		public static final AbstractPreparedProcedure NoParamStoredProcedure = new AbstractPreparedProcedure(connectionPool,
			"SELECT * FROM somefunc()");
		/** Calls a stored procedure with parameters */
		public static final AbstractPreparedProcedure ParamStoredProcedure = new AbstractPreparedProcedure(connectionPool,
			"SELECT * FROM UpdateOrInsertNotSoSimple(",PostgreSQLParameters.PHONE,", ",PostgreSQLParameters.ID,")");
		/** Array prepared statements generation */
		public static final AbstractPreparedProcedure ArrayQuery = new AbstractPreparedProcedure(connectionPool,
			"SELECT * FROM (VALUES ('one'), ('two'), ('three')) AS t (phone) WHERE NOT phone = ANY(",PostgreSQLParameters.VALUE_LIST,")");
	}

	// public access methods
	////////////////////////
	
	public static PostgreSQLAdapter getDBAdapter() throws SQLException {
		if (instance == null) {
			instance = new PostgreSQLAdapterConfiguration();
		}
		return instance;
	}
}