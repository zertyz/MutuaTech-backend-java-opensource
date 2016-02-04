package mutua.tests;

import adapters.PostgreSQLAdapter;
import mutua.events.PostgreSQLQueueEventLink;
import mutua.events.postgresql.QueuesPostgreSQLAdapter;
import mutua.icc.instrumentation.DefaultInstrumentationProperties;
import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.pour.PourFactory.EInstrumentationDataPours;

/** <pre>
 * MutuaEventsAdditionalEventLinksConfiguration.java
 * =================================================
 * (created by luiz, Jan 28, 2016)
 *
 * Configures the classes' static options for the 'MutuaEventsAdditionalEventLinks' module.
 * 
 * Follows the "Mutua Configurable Module" pattern.
 *
 * @version $Id$
 * @author luiz
*/

public class MutuaEventsAdditionalEventLinksTestsConfiguration {
	
	// log
	public static Instrumentation<DefaultInstrumentationProperties, String> LOG = new Instrumentation<DefaultInstrumentationProperties, String>(
		"MutuaEventsAdditionalEventLinksTests", DefaultInstrumentationProperties.DIP_MSG, EInstrumentationDataPours.CONSOLE, null);
	
	public static int PERFORMANCE_TESTS_LOAD_FACTOR = 1;
	
	// PostgreSQLQueueEventLink
	public static long QUEUE_POOLING_TIME             = 0;
	public static int  QUEUE_NUMBER_OF_WORKER_THREADS = 10;


	// database (all)
	public static boolean ALLOW_DATA_STRUCTURES_ASSERTION = true;
	public static boolean SHOULD_DEBUG_QUERIES            = false;
	public static String  HOSTNAME = "venus";
	public static int     PORT     = 5432;
	public static String  DATABASE = "hangman";
	public static String  USER     = "hangman";
	public static String  PASSWORD = "hangman";
	
	// PostgreSQL
	public static int POSTGRESQL_CONNECTION_POOL_SIZE = PostgreSQLAdapter.CONNECTION_POOL_SIZE;
	
	/** method to be called when attempting to configure the default behavior of 'MutuaEventsAdditionalEventLinksTests' module.
	 *  The following default values won't be touched if:
	 *  @param log is null
	 *  @param performanceTestsLoadFactor is < 0
	 *  @param postgresqlConnectionPoolSize is <= 0 */
	public static void configureMutuaEventsAdditionalEventLinksTests(Instrumentation<DefaultInstrumentationProperties, String> log, 
		int performanceTestsLoadFactor, long queuePoolingTime, int queueNumberOfWorkerThreads,
		boolean allowDataStructuresAssertion, boolean shouldDebugQueries,
		String hostname, int port, String database, String user, String password,
		int postgresqlConnectionPoolSize) {
		
		LOG = log != null ? log : LOG;
		
		PERFORMANCE_TESTS_LOAD_FACTOR  = performanceTestsLoadFactor >= 0 ? performanceTestsLoadFactor : PERFORMANCE_TESTS_LOAD_FACTOR;
		QUEUE_POOLING_TIME             = queuePoolingTime;
		QUEUE_NUMBER_OF_WORKER_THREADS = queueNumberOfWorkerThreads;
		
		ALLOW_DATA_STRUCTURES_ASSERTION = allowDataStructuresAssertion;
		SHOULD_DEBUG_QUERIES            = shouldDebugQueries;
		
		HOSTNAME = hostname;
		PORT     = port;
		DATABASE = database;
		USER     = user;
		PASSWORD = password;
		
		POSTGRESQL_CONNECTION_POOL_SIZE = postgresqlConnectionPoolSize > 0 ? postgresqlConnectionPoolSize : POSTGRESQL_CONNECTION_POOL_SIZE;
		
		applyConfiguration();
		
		log.reportDebug(MutuaEventsAdditionalEventLinksTestsConfiguration.class.getName() + ": new configuration loaded.");
	}

	public static void applyConfiguration() {
		PostgreSQLAdapter.configureDefaultValuesForNewInstances(null, POSTGRESQL_CONNECTION_POOL_SIZE);
		QueuesPostgreSQLAdapter.configureDefaultValuesForNewInstances(LOG, ALLOW_DATA_STRUCTURES_ASSERTION, SHOULD_DEBUG_QUERIES, HOSTNAME, PORT, DATABASE, USER, PASSWORD);
		PostgreSQLQueueEventLink.configureDefaultValuesForNewInstances(LOG, QUEUE_POOLING_TIME, QUEUE_NUMBER_OF_WORKER_THREADS);
	}
	
	static {
		applyConfiguration();
	}
}
