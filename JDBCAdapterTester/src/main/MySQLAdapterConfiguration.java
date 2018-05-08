package main;

import java.sql.SQLException;

import adapters.AbstractPreparedProcedure;
import adapters.IJDBCAdapterParameterDefinition;
import adapters.MySQLAdapter;

/**
 * MysqlAdapterConfiguration.java  --  $Id: MySQLHelperConfiguration.java,v 1.1 2010/07/01 22:03:06 luiz Exp $
 * ==============================
 * (created by luiz, Dec 22, 2008)
 *
 * Provides the needed 'MySQLAdapter' configuration to access and operate on the database
 */

public class MySQLAdapterConfiguration extends MySQLAdapter {
	 
	
	private MySQLAdapterConfiguration() throws SQLException {
		super(true, true, "192.168.0.3", 3306, "MysqlHelperTester", "root", "");
	}

	@Override
	protected String[][] getTableDefinitions() {
		return new String[][] {
			{"SimpleTable", "CREATE TABLE SimpleTable (id INT, phone TEXT)"},
			{"NotSoSimple", "CREATE TABLE NotSoSimple (id    INT  NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
			                "                          phone TEXT UNIQUE);"},
		};
	}
	
	/***************
	** PARAMETERS **
	***************/
	
	public enum MySQLParameters implements IJDBCAdapterParameterDefinition {
		ID   (Integer.class),
		PHONE(String.class),
		
		;
		
		private MySQLParameters(Class<? extends Object> a) {}

		@Override
		public String getParameterName() {
			return name();
		}
	}
	
	/***************
	** STATEMENTS **
	***************/
	
	public static final class MySQLStatements {
		/** Inserts an 'ID' and 'PHONE' into the 'InsertSimpleRecord' */
		public final static AbstractPreparedProcedure InsertSimpleRecord = new AbstractPreparedProcedure(connectionPool,
			"INSERT INTO SimpleTable VALUES (${ID}, ${PHONE})");
		/** Returns the 'PHONE' associated with 'ID' */
		public final static AbstractPreparedProcedure GetSimpleIdFromPhone = new AbstractPreparedProcedure(connectionPool,
			"SELECT phone FROM SimpleTable WHERE id=${ID}");
		/** removes the record denoted by 'ID' */
		public final static AbstractPreparedProcedure DeleteSimpleRecord = new AbstractPreparedProcedure(connectionPool,
			"DELETE FROM SimpleTable WHERE id=${ID}");
		/** Inserts a 'PHONE' into the 'InsertNotSoSimpleRecord' */
		public final static AbstractPreparedProcedure InsertNotSoSimpleRecord = new AbstractPreparedProcedure(connectionPool,
			"INSERT INTO NotSoSimple VALUES (${PHONE})");
	}
	
	// public access methods
	////////////////////////
	
	public static MySQLAdapter getDBAdapter() throws SQLException {
		return new MySQLAdapterConfiguration();
	}

}