package main;

import java.sql.SQLException;

import adapters.MySQLAdapter;
import adapters.exceptions.PreparedProcedureException;

import static main.MySQLAdapterConfiguration.MySQLStatements.*;
import static main.MySQLAdapterConfiguration.MySQLParameters.*;

/**
 * MySQLTester.java  --  $Id: MySQLTester.java,v 1.1 2010/07/01 22:03:06 luiz Exp $
 * ================
 * (created by luiz, Dec 15, 2008)
 *
 * Some 'JDBCAdapter' / 'MySQLAdapter' spikes
 */

public class MySQLTester {
	
	public static void mysqltesterMain(String[] args) throws SQLException, PreparedProcedureException {
		System.out.println("MySQLAdapterTester is running...");
		MySQLAdapter db = MySQLAdapterConfiguration.getDBAdapter();
		
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
	}

}


