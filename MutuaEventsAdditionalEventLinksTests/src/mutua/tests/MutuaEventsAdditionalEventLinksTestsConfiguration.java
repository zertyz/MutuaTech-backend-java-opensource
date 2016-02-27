package mutua.tests;

import java.lang.annotation.Annotation;
import java.sql.SQLException;

import adapters.PostgreSQLAdapter;
import mutua.events.PostgreSQLQueueEventLink;
import mutua.events.TestAdditionalEventServer.TestAdditionalEvent;
import mutua.events.postgresql.QueuesPostgreSQLAdapter;
import mutua.icc.instrumentation.DefaultInstrumentationProperties;
import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.pour.PourFactory.EInstrumentationDataPours;

/** <pre>
 * MutuaEventsAdditionalEventLinksConfiguration.java
 * =================================================
 * (created by luiz, Jan 28, 2016)
 *
 * Configure the classes' default values for new instances of the 'MutuaEventsAdditionalEventLinks' test application.
 * 
 * Typically, the configure* methods on this class must be invoked prior to its usage. 
 * 
 * Follows the "Mutua Configurable Module" pattern.
 *
 * @author luiz
*/

public class MutuaEventsAdditionalEventLinksTestsConfiguration {
	
	// log
	public static Instrumentation<DefaultInstrumentationProperties, String> LOG;
	
	public static int PERFORMANCE_TESTS_LOAD_FACTOR;
	
	// default values
	/////////////////
	
	public final static Class<? extends Annotation>[] ANNOTATION_CLASSES = new Class[] {TestAdditionalEvent.class};
	
	// PostgreSQLQueueEventLink default values
	public final static long QUEUE_POOLING_TIME             = 0;
	public final static int  QUEUE_NUMBER_OF_WORKER_THREADS = 10;
	
	

	
	/**************************
	** CONFIGURATION METHODS **
	**************************/
	
	/** method to be called when attempting to configure the default behavior of 'MutuaEventsAdditionalEventLinksTests' module.
	 *  The following default values won't be touched if:<pre>
	 *  @param queuePoolingTime             is -1
	 *  @param queueNumberOfWorkerThreads   is -1
	 *  @param postgresqlConnectionPoolSize is -1 */
	public static void configureDefaultValuesForNewInstances(Instrumentation<DefaultInstrumentationProperties, String> log, 
		int performanceTestsLoadFactor, long queuePoolingTime, int queueNumberOfWorkerThreads,
		String postgreSQLconnectionProperties, int postgreSQLConnectionPoolSize,
		boolean postgreSQLAllowDataStructuresAssertion, boolean postgreSQLShouldDebugQueries,
		String postgreSQLHostname, int postgreSQLPort, String postgreSQLDatabase, String postgreSQLUser, String postgreSQLPassword) throws SQLException {
		
		LOG                           = log;
		PERFORMANCE_TESTS_LOAD_FACTOR = performanceTestsLoadFactor;		
		
		// PostgreSQL
		PostgreSQLAdapter.configureDefaultValuesForNewInstances(postgreSQLconnectionProperties, postgreSQLConnectionPoolSize);
		QueuesPostgreSQLAdapter.configureDefaultValuesForNewInstances(log, postgreSQLAllowDataStructuresAssertion, postgreSQLShouldDebugQueries, postgreSQLHostname, postgreSQLPort, postgreSQLDatabase, postgreSQLUser, postgreSQLPassword);
		PostgreSQLQueueEventLink.configureDefaultValuesForNewInstances(log, queuePoolingTime, queueNumberOfWorkerThreads);
		
		System.err.println(MutuaEventsAdditionalEventLinksTestsConfiguration.class.getCanonicalName() + ": test configuration loaded.");

	}

	static {
		
		try {
			configureDefaultValuesForNewInstances(
				// log
					new Instrumentation<DefaultInstrumentationProperties, String>("MutuaEventsAdditionalEventLinksTests",
						DefaultInstrumentationProperties.DIP_MSG, EInstrumentationDataPours.CONSOLE, null),
				// load factor, pooling time and worker threads
				1, QUEUE_POOLING_TIME, QUEUE_NUMBER_OF_WORKER_THREADS,
				// PostgreSQL properties
				null,	// connection properties
				-1,		// connection pool size
				true,	// assert structures
				false,	// debug queries
				"venus", 5432, "hangman", "hangman", "hangman");
		} catch (SQLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
}
