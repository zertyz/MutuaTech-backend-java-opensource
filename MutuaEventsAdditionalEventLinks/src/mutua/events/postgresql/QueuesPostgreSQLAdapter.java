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
	
	
	private QueuesPostgreSQLAdapter(Instrumentation<?, ?> log, String[][] preparedProceduresDefinitions) throws SQLException {
		super(log, preparedProceduresDefinitions);
	}

	@Override
	protected String[] getCredentials() {
		return new String[] {HOSTNAME, Integer.toString(PORT), DATABASE, USER, PASSWORD};
	}

	// fields set by the public get instance methods
	////////////////////////////////////////////////
	
	private static String queueTableName     = null;
	private static String fieldsCreationLine = null;
	
	@Override
	protected String[][] getTableDefinitions() {
		if (!ALLOW_DATABASE_ADMINISTRATION) {
			return null;
		}
		return new String[][] {
			{queueTableName, "CREATE TABLE "+queueTableName+"(" +
			                 "eventId    SERIAL        NOT NULL PRIMARY KEY, " +
			                 "methodId   VARCHAR(63)   NOT NULL, " +
			                 fieldsCreationLine +
			                 "ts        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP)"},
			                 // a better primary key is (methodId, eventId)
			{queueTableName+"Consumers", "CREATE TABLE "+queueTableName+"Consumers(" +
			                             "methodId           VARCHAR(63)   NOT NULL UNIQUE, " +
			                             "lastFetchedEventId INT           NOT NULL, " +
			                             "ts                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"},
 			{queueTableName+"Fallback", "CREATE TABLE "+queueTableName+"Fallback(" +
			                            "eventId   INT       NOT NULL, " +
			                            "ts        TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"},
			};

	}
	
	
	// public methods
	/////////////////
	
	/** Gets a JDBCAdapter instance to manage PostgreSQL queues. This method must be synchronized because of the way 'queueTableName' is passed along */
	public synchronized static JDBCAdapter getQueuesDBAdapter(Class<?> eventsEnumeration, String queueTableName, String fieldsCreationLine,
	                                                          String queueElementFieldList,
	                                                          String valuesExpressionForInsertNewQueueElementQuery) throws SQLException {
		QueuesPostgreSQLAdapter.queueTableName     = queueTableName;
		QueuesPostgreSQLAdapter.fieldsCreationLine = fieldsCreationLine;
		PostgreSQLAdapter dba = new QueuesPostgreSQLAdapter(log, new String[][] {
			{"ResetTables",               "DELETE FROM "+queueTableName+";" +
			                              "DELETE FROM "+queueTableName+"Fallback;" +
			                              "UPDATE "+queueTableName+"Consumers SET lastFetchedEventId=-1;"},
			{"InsertNewQueueElement",     "INSERT INTO "+queueTableName+"("+queueElementFieldList+") VALUES("+valuesExpressionForInsertNewQueueElementQuery+")"},
			{"UpdateLastFetchedEventId",  "UPDATE "+queueTableName+"Consumers SET lastFetchedEventId=${LAST_FETCHED_EVENT_ID} WHERE methodId=${METHOD_ID}"},
//			{"FetchNextQueueElementIds",  "SELECT eventId FROM "+queueTableName+" WHERE methodId=${METHOD_ID} AND eventId > (" +
//			                                  "SELECT lastFetchedEventId FROM "+queueTableName+"Consumers WHERE methodId=${METHOD_ID}) LIMIT "+PostgreSQLQueueEventLink.QUEUE_NUMBER_OF_WORKER_THREADS},
//			{"FetchQueueElementById",     "SELECT "+fieldListForFetchQueueElementById+" FROM "+queueTableName+" WHERE eventId=${EVENT_ID}"},
			// a better query is SELECT MOSMSes.methodId, carrier, phone, text, eventId FROM MOSMSes, MOSMSesConsumers WHERE MOSMSes.methodId=MOSMSesConsumers.methodId AND MOSMSes.eventId > MOSMSesConsumers.lastFetchedEventId ORDER BY eventId ASC;
			{"FetchNextQueueElements",    "SELECT "+queueElementFieldList+", eventId FROM "+queueTableName+" WHERE methodId=${METHOD_ID} AND eventId > (" +
			                                  "SELECT lastFetchedEventId FROM "+queueTableName+"Consumers WHERE methodId=${METHOD_ID}) LIMIT "+PostgreSQLQueueEventLink.QUEUE_NUMBER_OF_WORKER_THREADS},
			{"InsertIntoFallbackQueue",   "INSERT INTO "+queueTableName+"Fallback(eventId) VALUES(${EVENT_ID})"},
			{"InsertMethodId",            "INSERT INTO "+queueTableName+"Consumers(methodId, lastFetchedEventId) VALUES (${METHOD_ID}, -1)"},
		});
		QueuesPostgreSQLAdapter.queueTableName     = null;
		QueuesPostgreSQLAdapter.fieldsCreationLine = null;
		
		// assure we have all the necessary 'methodId' entries on 'PostgreSQLQueueEventLinkConsumers'
		Object[] methodIds = eventsEnumeration.getEnumConstants();
		for (int i=0; i<methodIds.length; i++) try {
			String methodId = ((Enum<?>)methodIds[i]).name();
			PreparedProcedureInvocationDto procedure = new PreparedProcedureInvocationDto("InsertMethodId");
			procedure.addParameter("METHOD_ID", methodId);
			dba.invokeUpdateProcedure(procedure);
		} catch (Throwable t) {}
		
		return dba;

	}
}
