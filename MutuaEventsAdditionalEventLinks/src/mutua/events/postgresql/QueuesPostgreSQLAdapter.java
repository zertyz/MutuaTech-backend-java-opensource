package mutua.events.postgresql;

import java.sql.SQLException;

import mutua.events.PostgreSQLQueueEventLink;
import mutua.icc.configuration.annotations.ConfigurableElement;
import mutua.icc.instrumentation.Instrumentation;
import adapters.JDBCAdapter;
import adapters.PostgreSQLAdapter;
import adapters.dto.PreparedProcedureInvocationDto;

/** <pre>
 * QueuesPostgreSQLAdapter.java
 * ============================
 * (created by luiz, Jan 30, 2015)
 *
 * Provides 'PostgreSQLAdapter's to manipulate 'IEventLink' queue databases
 * TODO I should study http://ledgersmbdev.blogspot.com.br/2012/09/objectrelational-interlude-messaging-in.html to improve this
 * Also, Study LISTEN/NOTIFY PostgreSQL events
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class QueuesPostgreSQLAdapter extends PostgreSQLAdapter {


	// the version information for database tables present on this class, to be stored on the 'Meta' table. Useful for future data conversions.
	private static String modelVersionForMetaTable = "2015.08.20";
	
	// configuration
	////////////////
	
	@ConfigurableElement("The application's instrumentation instance to be used to log PostgreSQL database events")
	public static Instrumentation<?, ?> log;

	@ConfigurableElement("Hostname (or IP) of the PostgreSQL server")
	public static String HOSTNAME;
	@ConfigurableElement("Connection port for the PostgreSQL server")
	public static int    PORT;
	@ConfigurableElement("The PostgreSQL database with the application's data scope")
	public static String DATABASE;
	@ConfigurableElement("The PostgreSQL user name to access 'DATABASE' -- note: administrative rights, such as the creation of tables, might be necessary")
	public static String USER;
	@ConfigurableElement("The PostgreSQL plain text password for 'USER'")
	public static String PASSWORD;
	
	
	// fields set by the public get instance methods which must be set via the static field,
	// since 'getTableDefinitions' is called before the instance fields can be set
	private        String queueTableName;
	private        String fieldsCreationLine;
	private static String staticQueueTableName;
	private static String staticFieldsCreationLine;
	
	private QueuesPostgreSQLAdapter(Instrumentation<?, ?> log, String[][] preparedProceduresDefinitions) throws SQLException {
		super(log, preparedProceduresDefinitions);
	}

	@Override
	protected String[] getCredentials() {
		return new String[] {HOSTNAME, Integer.toString(PORT), DATABASE, USER, PASSWORD};
	}

	@Override
	protected String getDropDatabaseCommand() {
		String statements = "";		
		String[][] tableDefinitions = getTableDefinitions();
		
		for (String[] tableDefinition : tableDefinitions) {
			String databaseName = tableDefinition[0];
			statements += "DELETE FROM Meta WHERE tableName='"+databaseName+"';";
		}
		return super.getDropDatabaseCommand() + statements;
	}


	@Override
	protected String[][] getTableDefinitions() {
		if (!ALLOW_DATABASE_ADMINISTRATION) {
			return null;
		}
		
		// on the first run, set the instance fields based on the static ones
		// (this is needed because the super constructor calls 'getTableDefinitions' before this constructor has the chance to set these fields)
		if ((queueTableName == null) && (fieldsCreationLine == null)) {
			if ((staticQueueTableName == null) || (staticFieldsCreationLine == null)) {
				throw new RuntimeException("one of 'staticQueueTableName' or 'staticFieldsCreationLine' were not provided");
			}
			queueTableName     = staticQueueTableName;
			fieldsCreationLine = staticFieldsCreationLine;
			staticQueueTableName     = null;
			staticFieldsCreationLine = null;
		}
		
		return new String[][] {
			{queueTableName+"Head", "CREATE TABLE "+queueTableName+"Head(" +
			                        "lastFetchedEventId  INT  NOT NULL);" +
			                             
			                        // Meta record (assumes the Meta table was created by 'SMSAppModulePostgreSQLAdapter.java')
			                        "INSERT INTO Meta(tableName, modelVersion) VALUES ('"+queueTableName+"Head', '"+modelVersionForMetaTable+"')",

			                        // Default record
			                        "INSERT INTO "+queueTableName+"Head(lastFetchedEventId) VALUES(-1);"},
			                             
		    {queueTableName, "CREATE TABLE "+queueTableName+"(" +
		                     "eventId    SERIAL        PRIMARY KEY, " +
		                     fieldsCreationLine +
		                     "cts        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP);" +
		                     
		                     // Meta record
		                     "INSERT INTO Meta(tableName, modelVersion) VALUES ('"+queueTableName+"', '"+modelVersionForMetaTable+"')"},
		                     
 			{queueTableName+"Fallback", "CREATE TABLE "+queueTableName+"Fallback(" +
			                            "eventId   INT        NOT NULL, " +
			                            "cts       TIMESTAMP  DEFAULT   CURRENT_TIMESTAMP);" +
					                     
					                    // Meta record
					                    "INSERT INTO Meta(tableName, modelVersion) VALUES ('"+queueTableName+"Fallback', '"+modelVersionForMetaTable+"')"},
			};

	}
	
	/* for testing purposes */
	public void resetQueues() throws SQLException {
		PreparedProcedureInvocationDto procedure = new PreparedProcedureInvocationDto("ResetTables");
		invokeUpdateProcedure(procedure);
	}
	
	/** receives a 'valuesList' like "${PHONE}, ${TEXT}", a 'fieldsList' like "phone, text", a 'comparator' like "=" and
	 *  a 'logicOperator' like "AND" and return a text like "phone=${PHONE} AND text=${TEXT}"*/
	private static String getWhereConditions(String valuesList, String fieldsList, String comparator, String logicOperator) {
		String[] values = valuesList.split(", *");
		String[] fields = fieldsList.split(", *");
		String whereConditions = "";
		for (int i=0; i<fields.length; i++) {
			whereConditions = fields[i] + comparator + values[i] + (i < (fields.length-1) ? logicOperator : "");
		}
		return whereConditions;
	}
	
	// public methods
	/////////////////
	
	/** Gets a JDBCAdapter instance to manage PostgreSQL queues. This method must be synchronized because of the way 'queueTableName'
	  * and 'fieldsCreationLine' are passed along */
	public synchronized static QueuesPostgreSQLAdapter getQueuesDBAdapter(Class<?> eventsEnumeration, String queueTableName, String fieldsCreationLine,
	                                                                      String queueElementFieldList,
	                                                                      String valuesExpressionForInsertNewQueueElementQuery) throws SQLException {
		staticQueueTableName     = queueTableName;
		staticFieldsCreationLine = fieldsCreationLine;
		QueuesPostgreSQLAdapter dba = new QueuesPostgreSQLAdapter(log, new String[][] {
			{"ResetTables",               "DELETE FROM "+queueTableName+";" +
			                              "DELETE FROM "+queueTableName+"Fallback;" +
			                              "UPDATE "+queueTableName+"Head SET lastFetchedEventId=-1;"},
			{"InsertNewQueueElement",     "INSERT INTO "+queueTableName+"("+queueElementFieldList+") VALUES("+valuesExpressionForInsertNewQueueElementQuery+")"},
			{"UpdateLastFetchedEventId",  "UPDATE "+queueTableName+"Head SET lastFetchedEventId=${LAST_FETCHED_EVENT_ID}"},
			{"FetchNextQueueElements",    "SELECT "+queueElementFieldList+", eventId FROM "+queueTableName+" WHERE eventId > (SELECT lastFetchedEventId FROM "+queueTableName+"Head) ORDER BY eventId ASC LIMIT "+PostgreSQLQueueEventLink.QUEUE_NUMBER_OF_WORKER_THREADS},
			{"InsertIntoFallbackQueue",   "INSERT INTO "+queueTableName+"Fallback(eventId) SELECT eventId FROM "+queueTableName+" WHERE "+getWhereConditions(valuesExpressionForInsertNewQueueElementQuery, queueElementFieldList, "=", "AND")+" ORDER BY eventId DESC"},
			{"PopFallbackElements",       "DELETE FROM "+queueTableName+"Fallback RETURNING eventId"},
		});
		
		return dba;

	}
}