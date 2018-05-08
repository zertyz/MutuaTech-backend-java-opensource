package main;

import java.sql.SQLException;
import java.util.Arrays;

import mutua.icc.instrumentation.Instrumentation;
import adapters.PostgreSQLAdapter;
import adapters.exceptions.PreparedProcedureException;
import static main.PostgreSQLAdapterConfiguration.PostgreSQLStatements.*;
import static main.PostgreSQLAdapterConfiguration.PostgreSQLParameters.*;


/** <pre>
 * PostgreSQLTester.java
 * =====================
 * (created by luiz, Jan 26, 2015)
 *
 * Some 'JDBCAdapter' / 'PostgreSQLAdapter' spikes
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class PostgreSQLTester {

	public static void postgreSQLTesterMain(String[] args) throws SQLException, PreparedProcedureException {
		
		System.out.println("PostgreSQLTester.jar: tests the correct database setup for use with Mutua's JDBCAdapter + PostgreSQL driver.");
		System.out.println("                      Please, proceed as described bellow:");
		System.out.println("                      1) Created a database & user configured to use your default chosen schema -- please,");
		System.out.println("                         see one of the commands bellow, giving preference to the later, which was tested:");
		System.out.println("                           ALTER USER user_name SET search_path to 'schemaname'");
		System.out.println("                           -- OR --");
		System.out.println("                           ALTER DATABASE dbname SET search_path TO 'schemaname'");
		System.out.println("                      2) Assure you have 'schemaname' created with the appropriate permissions to allow");
		System.out.println("                         tables, indexes, stored procedures and trigger creation (which may be later revoked).");
		System.out.println("                      3) Drop all objects from 'schemaname' prior to every execution of this test. You may");
		System.out.println("                         execute DROP TABLE SimpleTable; DROP TABLE NotSoSimple; DROP FUNCTION somefunc;");
		System.out.println("                         DROP FUNCTION UpdateOrInsertNotSoSimple;");
		System.out.println("                      4) Run this test.");
		System.out.println("                      5) Carefully investigate all the output to see if everything went fine. You should see,");
		System.out.println("                         on the database, the tables 'SimpleTable' (4000 records) and 'NotSoSimple' (1 record)");
		System.out.println("                         and the procedures 'somefunc' and 'UpdateOrInsertNotSoSimple'.");
		System.out.println("                      6) In case there was an error while creating the tables or stored procedures, try to");
		System.out.println("                         execute the related queries manually to spot the problem and adjust the database/user/schema");
		System.out.println("                         permissions accordingly. You may restart the test from step (3).");
		System.out.println("");
		System.out.println("Tested database setup:");
		System.out.println("CREATE DATABASE mutuajdbcadaptertest WITH");
		System.out.println("    OWNER = postgres");
		System.out.println("    ENCODING = 'UTF8'");
		System.out.println("    CONNECTION LIMIT = -1");
		System.out.println("    TEMPLATE template0;");
		System.out.println("CREATE USER minprivuser WITH");
		System.out.println("    LOGIN");
		System.out.println("    SUPERUSER");
		System.out.println("    CREATEDB");
		System.out.println("    CREATEROLE");
		System.out.println("    INHERIT");
		System.out.println("    REPLICATION");
		System.out.println("    CONNECTION LIMIT -1");
		System.out.println("    PASSWORD 'minprivpassword';");
		System.out.println("GRANT ALL ON DATABASE mutuajdbcadaptertest TO minprivuser WITH GRANT OPTION;");
		System.out.println("CREATE SCHEMA schemaname AUTHORIZATION minprivuser;");
		System.out.println("GRANT ALL ON SCHEMA schemaname TO minprivuser WITH GRANT OPTION;");
		System.out.println("ALTER DATABASE mutuajdbcadaptertest SET search_path TO 'schemaname';");
		System.out.println("");
		
		if ((args.length < 5) || (args.length > 7)) {
			System.out.println("Usage: java -jar PostgreSQLTester.jar <postgresql host/ip> <port> <database name> <user> <password>");
			System.out.println("                                      ['true' or 'false' for creating the test database model, if necessary]");
			System.out.println("                                      ['true' or 'false' for logging queries]");
			System.out.println();
			System.out.println("Example: java -jar PostgreSQLTester.jar ::1 5432 mutuajdbcadaptertest minprivuser minprivpassword true true");
			return;
		}
		
		String  hostname                     = args[0];
		int     port                         = Integer.parseInt(args[1]);
		String  database                     = args[2];
		String  user                         = args[3];
		String  password                     = args[4];
		boolean allowDataStructuresAssertion = args.length >= 6 ? Boolean.parseBoolean(args[5]) : true;
		boolean shouldDebugQueries           = args.length >= 7 ? Boolean.parseBoolean(args[6]) : true;

		System.out.println("Configuration:");
		System.out.println("\thostname                    : "+hostname);
		System.out.println("\tport                        : "+port);
		System.out.println("\tdatabase                    : "+database);
		System.out.println("\tuser                        : "+user);
		System.out.println("\tpassword                    : "+password);
		System.out.println("\tallowDataStructuresAssertion: "+allowDataStructuresAssertion);
		System.out.println("\tshouldDebugQueries          : "+shouldDebugQueries);
		
		Instrumentation.reportDebug("Attempting to get a PostgreSQL connection...");
		PostgreSQLAdapterConfiguration.configureDefaultValuesForNewInstances(allowDataStructuresAssertion, shouldDebugQueries, hostname, port, database, user, password);
		PostgreSQLAdapter db = PostgreSQLAdapterConfiguration.getDBAdapter();
		db.resetDatabase();

		// INSERT
		int result = db.invokeUpdateProcedure(InsertSimpleRecord,
		                                      ID,    11,
		                                      PHONE, "2192820997");
		System.out.println("Result: " + result);
		
		// QUERY
		String phone = (String) db.invokeScalarProcedure(GetSimpleIdFromPhone,
		                                                 ID, 11);
		System.out.println("Result: " + phone);
		
		// DELETE
		result = db.invokeUpdateProcedure(DeleteSimpleRecord,
		                                  ID, 11);
		System.out.println("Result: " + result);

		// NO PARAM STORED PROCEDURE
		int sp = (Integer) db.invokeScalarProcedure(NoParamStoredProcedure);
		System.out.println("Result: " + sp);

		// PARAM STORED PROCEDURE
		db.invokeRowProcedure(ParamStoredProcedure,
                              ID,    11,
                              PHONE, "2192820997");
		System.out.println("Result: NULL");
		
		System.out.println(Arrays.deepToString(db.invokeArrayProcedure(ArrayQuery, VALUE_LIST, new String[] {"one"})));
		
		// Batch INSERT (40 elements at a time)
		int c = 0;
		for (int batchCount=0; batchCount<100; batchCount++) {
			int[] results = db.invokeUpdateBatchProcedure(InsertSimpleRecord, new Object[][] {
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
				{ID, 12+c, PHONE, Long.toString(21991234900L+(c++))},
			});
			System.out.println("Batch Results: " + Arrays.toString(results));
		}

	}

}
