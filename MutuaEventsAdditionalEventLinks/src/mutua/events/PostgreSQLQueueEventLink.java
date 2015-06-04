package mutua.events;

import java.sql.SQLException;

import mutua.events.postgresql.QueuesPostgreSQLAdapter;
import static mutua.events.postgresql.QueuesPostgreSQLAdapter.log;
import mutua.icc.configuration.annotations.ConfigurableElement;
import mutua.imi.IndirectMethodInvocationInfo;
import adapters.JDBCAdapter;
import adapters.dto.PreparedProcedureInvocationDto;
import adapters.exceptions.PreparedProcedureException;
import static mutua.icc.instrumentation.MutuaEventAdditionalEventLinksInstrumentationEvents.*;
import static mutua.icc.instrumentation.MutuaEventAdditionalEventLinksInstrumentationProperties.*;

/** <pre>
 * PostgreSQLQueueEventLink.java
 * =============================
 * (created by luiz, Jan 30, 2015)
 *
 * Implements a serializable, distributable, buffered and backed by
 * a PostgreSQL database non-blocking server dispatching 'IEventLink'
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class PostgreSQLQueueEventLink<SERVICE_EVENTS_ENUMERATION> extends IEventLink<SERVICE_EVENTS_ENUMERATION> {
	
	
	// configuration
	////////////////
	
	@ConfigurableElement("The amount of milliseconds the PostgreSQLQueueEventLink ConsumerDispatcher thread should wait between queries for new " +
	                     "queue entries to process. Set to 0 to rely on the internal notification mechanisms and only when queue producers and " +
	                     "consumers are running on the same machine and on the same process.")
	public static long QUEUE_POOLING_TIME = 0;
	@ConfigurableElement("The number of threads that will consume the queue events")
	public static int  QUEUE_NUMBER_OF_WORKER_THREADS = 10;


	// TODO (Write only) fallback queue. 'lastProcessedEntry' is update as soon as an element is fetched from the database.
	//      If the consumer reports an error processing it, the element should go to the fallback queue.
	
	
	/** Designed to be run by a single thread, this class fetches the database and assigns work for instances of 'ConsumerWorker' */
	class ConsumerDispatcher extends Thread {
		
		private Class<?> eventsEnumeration;
		private JDBCAdapter dba;
		public boolean stop = false;
		public boolean processing = true;
		
		public ConsumerDispatcher(Class<?> eventsEnumeration, JDBCAdapter dba) {
			this.eventsEnumeration = eventsEnumeration;
			this.dba = dba;
			setName("PostgreSQLQueueEventLink ConsumerDispatcher thread");

		}
		
		@Override
		public void run() {
			while (!stop) {
				Object[] methodIds = eventsEnumeration.getEnumConstants();
				boolean fetched = false;
				for (int i=0; i<methodIds.length; i++) try {
					int lastFetchedEventId = -1;
					String serializedMethodId = ((Enum<?>)methodIds[i]).name();
					PreparedProcedureInvocationDto getEventIdsProcedure = new PreparedProcedureInvocationDto("FetchNextQueueElements");
					getEventIdsProcedure.addParameter("METHOD_ID", serializedMethodId);
					Object[][] rowsOfQueueEntries = dba.invokeArrayProcedure(getEventIdsProcedure);
					int eventId = -1;
					for (Object[] queueEntry : rowsOfQueueEntries) {
						// queueEntry := { [the fields listed by 'dataBureau.getQueueElementFieldList'], eventId }
						eventId = (Integer)queueEntry[queueEntry.length-1];
						IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event = dataBureau.deserializeQueueEntry(eventId, queueEntry);
						localEventDispatchingQueue.reportConsumableEvent(event);
					}
					// update the pointers
					if (eventId > lastFetchedEventId) {
						fetched = true;
						lastFetchedEventId = eventId;
						PreparedProcedureInvocationDto updateLastFetchedEventIdProcedure = new PreparedProcedureInvocationDto("UpdateLastFetchedEventId");
						updateLastFetchedEventIdProcedure.addParameter("LAST_FETCHED_EVENT_ID", lastFetchedEventId);
						updateLastFetchedEventIdProcedure.addParameter("METHOD_ID", serializedMethodId);
						dba.invokeUpdateProcedure(updateLastFetchedEventIdProcedure);
					}
				} catch (Throwable t) {
					log.reportThrowable(t, "Exception on ConsumerDispatcher thread");
				}
				
				// wait for a new element or proceed right away?
				if (!fetched) {
					log.reportDebug("No new element by now on queue '"+queueTableName+"'");
					synchronized (this) {
						processing = false;
						try {
							wait(QUEUE_POOLING_TIME);
						} catch (InterruptedException e) {}
						processing = true;
					}
				}
					
			}
			processing = false;
		}
	}
	
	
	private   final JDBCAdapter dba;
	private   final QueueEventLink<SERVICE_EVENTS_ENUMERATION> localEventDispatchingQueue;
	protected final String queueTableName;
	private   final IDatabaseQueueDataBureau<SERVICE_EVENTS_ENUMERATION> dataBureau;
	private   final ConsumerDispatcher cdThread;
	

	public PostgreSQLQueueEventLink(Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration, String queueTableName,
	                                IDatabaseQueueDataBureau<SERVICE_EVENTS_ENUMERATION> dataBureau) throws SQLException {
		super(eventsEnumeration);
		this.queueTableName = queueTableName;
		this.dataBureau = dataBureau;
		dba = QueuesPostgreSQLAdapter.getQueuesDBAdapter(eventsEnumeration, queueTableName, dataBureau.getFieldsCreationLine(),
		                                                 dataBureau.getQueueElementFieldList(),
		                                                 dataBureau.getValuesExpressionForInsertNewQueueElementQuery());
		
		// creates an 'QueueEventLink' that will report events to the listeners and consumers of this instance
		int localEventDispatchingQueueCapacity = 2*QUEUE_NUMBER_OF_WORKER_THREADS;
		localEventDispatchingQueue = new QueueEventLink<SERVICE_EVENTS_ENUMERATION>(eventsEnumeration, localEventDispatchingQueueCapacity, QUEUE_NUMBER_OF_WORKER_THREADS);
		localEventDispatchingQueue.clientsAndConsumerMethodInvokers = clientsAndConsumerMethodInvokers;
		localEventDispatchingQueue.clientsAndListenerMethodInvokers = clientsAndListenerMethodInvokers;
		
		// start the consumers dispatch manager thread
		cdThread = new ConsumerDispatcher(eventsEnumeration, dba);
		cdThread.start();
	}

	@Override
	public void reportListenableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		throw new RuntimeException("Listenable Events not available for now, for database queue event link");
	}

	@Override
	public void reportConsumableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		try {
			// insert into the queue
			PreparedProcedureInvocationDto procedure = new PreparedProcedureInvocationDto("InsertNewQueueElement");
			procedure.addParameter("METHOD_ID", event.getMethodId().toString());
			dataBureau.serializeQueueEntry(event, procedure);
			dba.invokeUpdateProcedure(procedure);
			// notify the consumers dispatch manager
			synchronized (cdThread) {
				cdThread.notify();
			}
		} catch (Throwable t) {
			log.reportThrowable(t, "Error while attempting to insert PostgreSQL queue element");
		}
	}
	
	public void stop() {
		log.reportEvent(IE_INTERRUPTING, IP_QUEUE_TABLE_NAME, queueTableName);
		cdThread.stop  = true;
		for (int i=0; (i<10) && (cdThread.processing); i++) {
			log.reportEvent(IE_WAITING_CONSUMERS);
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}
		if (cdThread.processing) {
			log.reportEvent(IE_FORCING_CONSUMERS_SHUTDOWN);
		}
		cdThread.interrupt();
		log.reportEvent(IE_INTERRUPTION_COMPLETE, IP_QUEUE_TABLE_NAME, queueTableName);
	}
	
}