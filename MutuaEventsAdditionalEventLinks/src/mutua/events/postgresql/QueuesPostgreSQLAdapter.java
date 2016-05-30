package mutua.events.postgresql;

import java.sql.SQLException;

import adapters.AbstractPreparedProcedure;
import adapters.IJDBCAdapterParameterDefinition;
import adapters.JDBCAdapterInstrumentationMethods;
import adapters.PostgreSQLAdapter;

/** <pre>
 * QueuesPostgreSQLAdapter.java
 * ============================
 * (created by luiz, Jan 30, 2015)
 *
 * Provides 'PostgreSQLAdapter's to manipulate 'IEventLink' queue databases
 * TODO I should study http://ledgersmbdev.blogspot.com.br/2012/09/objectrelational-interlude-messaging-in.html to improve this
 * Also, Study LISTEN/NOTIFY PostgreSQL events
 * 
 * Note: this class implements a special variation of "Mutua Configurable Class" pattern, in which case, for flexibility
 * reasons, we are not using a singleton -- allowing several different queues to be created and requiring clients to manage the
 * generated instances for themselves.
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public final class QueuesPostgreSQLAdapter extends PostgreSQLAdapter {


	// the version information for database tables present on this class, to be stored on the 'Meta' table. Useful for future data conversions.
	private static String modelVersionForMetaTable = "2015.08.20";
	
	// Mutua Configurable Class pattern
	///////////////////////////////////
	
	//private static SMSAppModulePostgreSQLAdapter instance = null;
	
	// JDBCAdapter default values
	private static String HOSTNAME;
	private static int    PORT;
	private static String DATABASE;
	private static String USER;
	private static String PASSWORD;
	private static boolean ALLOW_DATA_STRUCTURES_ASSERTION;
	private static boolean SHOULD_DEBUG_QUERIES;
		
	/** method to be called when attempting to configure the default behavior for new instances of 'QueuesPostgreSQLAdapter' */
	public static void configureDefaultValuesForNewInstances(
		boolean allowDataStructuresAssertion, boolean shouldDebugQueries,
	    String hostname, int port, String database, String user, String password) {
		
		ALLOW_DATA_STRUCTURES_ASSERTION = allowDataStructuresAssertion;
		SHOULD_DEBUG_QUERIES            = shouldDebugQueries;
		HOSTNAME = hostname;
		PORT     = port;
		DATABASE = database;
		USER     = user;
		PASSWORD = password;
		
		//instance = new SMSAppModulePostgreSQLAdapter(LOG);	// start/restart the singleton with the new settings
	}
		
	@Override
	protected String[] getDropDatabaseCommand() {
		// TODO the Meta table norm may be incorporated into PostgreSQLAdapter
		String[][] tableDefinitions = getTableDefinitions();
		String[] superStatements = super.getDropDatabaseCommand();
		String[] statements = new String[superStatements.length + tableDefinitions.length];
		
		int i;
		for (i=0; i<superStatements.length; i++) {
			statements[i] = superStatements[i];
		}
		for (String[] tableDefinition : tableDefinitions) {
			String databaseName = tableDefinition[0];
			statements[i++] = "DELETE FROM Meta WHERE tableName='"+databaseName+"';";
		}
		return statements;
	}


	private final String queueTableName;
	private final String fieldsCreationLine;
	@Override
	protected String[][] getTableDefinitions() {
		return new String[][] {
			{queueTableName+"Head", "CREATE TABLE "+queueTableName+"Head(" +
			                        "lastFetchedEventId  INT  NOT NULL)",
			                             
			                        // Meta record (assumes the Meta table was created by 'SMSAppModulePostgreSQLAdapter.java')
			                        "INSERT INTO Meta(tableName, modelVersion) VALUES ('"+queueTableName+"Head', '"+modelVersionForMetaTable+"')",

			                        // Default record
			                        "INSERT INTO "+queueTableName+"Head(lastFetchedEventId) VALUES(-1)"},
			                             
		    {queueTableName, "CREATE TABLE "+queueTableName+"(" +
		                     "eventId    SERIAL        PRIMARY KEY, " +
		                     fieldsCreationLine +
		                     "cts        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP)",
		                     
		                     // Meta record
		                     "INSERT INTO Meta(tableName, modelVersion) VALUES ('"+queueTableName+"', '"+modelVersionForMetaTable+"')"},
		                     
 			{queueTableName+"Fallback", "CREATE TABLE "+queueTableName+"Fallback(" +
			                            "eventId   INT        NOT NULL, " +
			                            "cts       TIMESTAMP  DEFAULT   CURRENT_TIMESTAMP)",
					                     
					                    // Meta record
					                    "INSERT INTO Meta(tableName, modelVersion) VALUES ('"+queueTableName+"Fallback', '"+modelVersionForMetaTable+"')"},
			};

	}
	
	/* for testing purposes */
	public void resetQueues() throws SQLException {
		invokeUpdateProcedure(ResetTables);
	}
	
	/** receives a 'valuesList' like "${PHONE}, ${TEXT}", a 'fieldsList' like "phone, text", a 'comparator' like "=" and
	 *  a 'logicOperator' like "AND" and return a text like "phone=${PHONE} AND text=${TEXT}"*/
	private static Object[] getWhereConditions(IJDBCAdapterParameterDefinition[] valuesList, String fieldsList, String comparator, String logicOperator) {
		String[] fields = fieldsList.split(", *");
		Object[] ret = new Object[fields.length*3-1];
		int index = 0;
		for (int i=0; i<fields.length; i++) {
			ret[index++] = fields[i] + comparator;
			ret[index++] = valuesList[i];
			if (i < fields.length-1 ) {
				ret[index++] = logicOperator;
			}
		}
		return ret;
	}
	
	/** for a 'originalList' like {A, B, C}, returns a list like {A, 'separator', B, 'separator', C} */
	private static Object[] listObjects(Object[] originalList, Object separator) {
		Object[] ret = new Object[originalList.length*2 - 1];
		int index = 0;
		for (int i=0; i<originalList.length; i++) {
			Object item = originalList[i];
			ret[index++] = item;
			if (i < originalList.length-1) {
				ret[index++] = separator;
			}
		}
		return ret;
	}

	/***************
	** PARAMETERS **
	***************/
	
	public enum QueueParameters implements IJDBCAdapterParameterDefinition {

		LAST_FETCHED_EVENT_ID   (Integer.class),
		
		;
		
		private QueueParameters(Class<? extends Object> a) {}

		@Override
		public String getParameterName() {
			return name();
		}
	}

	/***************
	** STATEMENTS **
	***************/
	
	/** Drop all tables associated with 'queueTable', for testing purposes only */
	public final AbstractPreparedProcedure ResetTables;
	/** Insert statement able to be executed in batch, based on {@link #InsertNewQueueElement} */
	public final AbstractPreparedProcedure BatchInsertNewQueueElement;
	/** Inserts a row consisting of 'valuesExpressionForInsertNewQueueElementQuery' on the 'queueTable', returning the assigned 'eventId' */
	public final AbstractPreparedProcedure InsertNewQueueElement;
	/** Tells the queue that the head should be moved to {@link QueueParameters#LAST_FETCHED_EVENT_ID} */
	public final AbstractPreparedProcedure UpdateLastFetchedEventId;
	/** Used by consumers to get the next queue elements to be processed */
	public final AbstractPreparedProcedure FetchNextQueueElements;
	/** Insert procedure on the fallback queue, to be used when the element could not be consumed */ 
	public final AbstractPreparedProcedure InsertIntoFallbackQueue;
	/** Retrieves & deletes all 'eventId's from the fallback queue */
	public final AbstractPreparedProcedure PopFallbackElements;


	private QueuesPostgreSQLAdapter(String queueTableName, String fieldsCreationLine,
	                                String queueElementFieldList,
	                                IJDBCAdapterParameterDefinition[] parametersListForInsertNewQueueElementQuery,
                                    int queueNumberOfWorkerThreads) throws SQLException {
		super(false, SHOULD_DEBUG_QUERIES, HOSTNAME, PORT, DATABASE, USER, PASSWORD);
		this.queueTableName     = queueTableName;
		this.fieldsCreationLine = fieldsCreationLine;
		// the execution of the following method was delayed by invoking the super constructor with 'false' in order for the fields
		// needed by 'getTableDefinitions' to be set
		if (ALLOW_DATA_STRUCTURES_ASSERTION) {
			JDBCAdapterInstrumentationMethods.reportAdministrationWarningMessage("WARNING: executing delayed 'assureDataStructures' for '"+getClass().getName()+"'");
			assureDataStructures();
		}
		
		// statements
		/////////////

		ResetTables = new AbstractPreparedProcedure(connectionPool,
			"TRUNCATE ",queueTableName," CASCADE;",
			"TRUNCATE ",queueTableName,"Fallback CASCADE;" +
			"UPDATE ",queueTableName,"Head SET lastFetchedEventId=-1;");
		InsertNewQueueElement = new AbstractPreparedProcedure(connectionPool,
			"INSERT INTO ",queueTableName,"(",queueElementFieldList,") VALUES(",listObjects(parametersListForInsertNewQueueElementQuery, ", "),") RETURNING eventId");
		BatchInsertNewQueueElement = new AbstractPreparedProcedure(connectionPool,
			"INSERT INTO ",queueTableName,"(",queueElementFieldList,") VALUES(",listObjects(parametersListForInsertNewQueueElementQuery, ", "),")");
		UpdateLastFetchedEventId = new AbstractPreparedProcedure(connectionPool,
			"UPDATE ",queueTableName,"Head SET lastFetchedEventId=",QueueParameters.LAST_FETCHED_EVENT_ID);
		FetchNextQueueElements = new AbstractPreparedProcedure(connectionPool,
			"SELECT ",queueElementFieldList,", eventId FROM ",queueTableName," WHERE eventId > (SELECT lastFetchedEventId FROM ",
			queueTableName,"Head) ORDER BY eventId ASC LIMIT ",queueNumberOfWorkerThreads);
		InsertIntoFallbackQueue = new AbstractPreparedProcedure(connectionPool,
			"INSERT INTO ",queueTableName,"Fallback(eventId) SELECT eventId FROM ",queueTableName," WHERE ",
			getWhereConditions(parametersListForInsertNewQueueElementQuery, queueElementFieldList, "=", " AND ")," ORDER BY eventId DESC LIMIT 1");
		PopFallbackElements = new AbstractPreparedProcedure(connectionPool,
			"DELETE FROM ",queueTableName,"Fallback RETURNING eventId");
	}

	// public methods
	/////////////////
		
	/** Gets a JDBCAdapter instance to manage PostgreSQL queues. For advanced instance options, please see {@link #configureQueuesDatabaseModule} */
	public synchronized static QueuesPostgreSQLAdapter getQueuesDBAdapter(String queueTableName, String fieldsCreationLine, String queueElementFieldList,
	                                                                      IJDBCAdapterParameterDefinition[] parametersListForInsertNewQueueElementQuery,
	                                                                      int queueNumberOfWorkerThreads) throws SQLException {
		
		return new QueuesPostgreSQLAdapter(queueTableName, fieldsCreationLine, queueElementFieldList,
		                                   parametersListForInsertNewQueueElementQuery, queueNumberOfWorkerThreads);
	}
}