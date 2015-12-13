package mutua.events;

import static mutua.events.postgresql.QueuesPostgreSQLAdapter.log;
import static mutua.icc.instrumentation.MutuaEventAdditionalEventLinksInstrumentationEvents.*;
import static mutua.icc.instrumentation.MutuaEventAdditionalEventLinksInstrumentationProperties.*;

import java.sql.SQLException;

import mutua.events.postgresql.QueuesPostgreSQLAdapter;
import mutua.icc.configuration.annotations.ConfigurableElement;
import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodNotFoundException;
import adapters.dto.PreparedProcedureInvocationDto;

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
		private QueuesPostgreSQLAdapter dba;
		public boolean stop = false;
		public boolean processing = true;
		public int notificationCount = 0;	// for the internal notification mechanism, when 'QUEUE_POOLING_TIME' is 0
		
		public ConsumerDispatcher(Class<?> eventsEnumeration, QueuesPostgreSQLAdapter dba) {
			this.eventsEnumeration = eventsEnumeration;
			this.dba = dba;
			setName("PostgreSQLQueueEventLink ConsumerDispatcher thread");

		}
		
		@Override
		public void run() {
			
			// wait until there are consumers -- addClient notifies 'this'
			if (clientsAndConsumerMethodInvokers.size() == 0) try {
				synchronized (this) {
					wait();
				}
			} catch (Throwable t) {}
			
			int lastFetchedEventId = -1;
			// main loop
			while (!stop) {
				boolean fetched = false;
				int eventId = -1;
				try {
					PreparedProcedureInvocationDto getEventIdsProcedure = new PreparedProcedureInvocationDto("FetchNextQueueElements");
					Object[][] rowsOfQueueEntries = dba.invokeArrayProcedure(getEventIdsProcedure);
					for (Object[] queueEntry : rowsOfQueueEntries) {
						// queueEntry := { [the fields listed by 'dataBureau.getQueueElementFieldList'], eventId}
						eventId = (Integer)queueEntry[queueEntry.length-1];
						IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event = dataBureau.deserializeQueueEntry(eventId, queueEntry);
						localEventDispatchingQueue.reportConsumableEvent(event);
					}
					// update the pointers
					if (rowsOfQueueEntries.length > 0) {
						fetched = true;
						synchronized (this) {
							notificationCount -= rowsOfQueueEntries.length;
						}
						if (eventId > lastFetchedEventId) {
							lastFetchedEventId = eventId;
							PreparedProcedureInvocationDto updateLastFetchedEventIdProcedure = new PreparedProcedureInvocationDto("UpdateLastFetchedEventId");
							updateLastFetchedEventIdProcedure.addParameter("LAST_FETCHED_EVENT_ID", lastFetchedEventId);
							dba.invokeUpdateProcedure(updateLastFetchedEventIdProcedure);
						}
					}
				} catch (Throwable t) {
					log.reportThrowable(t, "Exception on ConsumerDispatcher thread");
				}
				
				// wait for a new element or proceed right away?
				if (!fetched) {
					synchronized (this) {
						// use the internal notification mechanism to detect notifications even before we were waiting on them
						if (notificationCount != 0) {
							notificationCount -= Math.min(notificationCount, 1);	// prevents possible loops
							continue;
						} else {
							log.reportDebug("No new element by now on queue '"+queueTableName+"'");
							processing = false;
							try {
								wait(QUEUE_POOLING_TIME);
							} catch (InterruptedException e) {}
							processing = true;
						}
					}
				}
					
			}
			processing = false;
		}
	}
	
	
	private   final QueuesPostgreSQLAdapter dba;
	private   final QueueEventLink<SERVICE_EVENTS_ENUMERATION> localEventDispatchingQueue;
	protected final String queueTableName;
	private   final IDatabaseQueueDataBureau<SERVICE_EVENTS_ENUMERATION> dataBureau;
	private   final ConsumerDispatcher cdThread;
	

	public PostgreSQLQueueEventLink(Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration, final String queueTableName,
	                                final IDatabaseQueueDataBureau<SERVICE_EVENTS_ENUMERATION> dataBureau) throws SQLException {
		super(eventsEnumeration);
		this.queueTableName = queueTableName;
		this.dataBureau = dataBureau;
		dba = QueuesPostgreSQLAdapter.getQueuesDBAdapter(eventsEnumeration, queueTableName, dataBureau.getFieldsCreationLine(),
		                                                 dataBureau.getQueueElementFieldList(),
		                                                 dataBureau.getValuesExpressionForInsertNewQueueElementQuery());
		
		// creates an 'QueueEventLink' that will report events to the listeners and consumers of this instance
		int localEventDispatchingQueueCapacity = 2*QUEUE_NUMBER_OF_WORKER_THREADS;
		localEventDispatchingQueue = new QueueEventLink<SERVICE_EVENTS_ENUMERATION>(eventsEnumeration, localEventDispatchingQueueCapacity, QUEUE_NUMBER_OF_WORKER_THREADS) {
			@Override
			public void pushFallback(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event, Throwable t) {
				log.reportThrowable(t, "Error adding event '"+event.toString()+"' to the queue '"+queueTableName+"'");
				try {
					PreparedProcedureInvocationDto procedure = new PreparedProcedureInvocationDto("InsertIntoFallbackQueue");
					dataBureau.serializeQueueEntry(event, procedure);
					dba.invokeUpdateProcedure(procedure);
				} catch (SQLException e) {
					log.reportThrowable(e, "Error adding event '"+event.toString()+"' to the fallback queue '"+queueTableName+"Fallback'");
				}
			}
		};
		localEventDispatchingQueue.clientsAndConsumerMethodInvokers = clientsAndConsumerMethodInvokers;
		localEventDispatchingQueue.clientsAndListenerMethodInvokers = clientsAndListenerMethodInvokers;
		
		// start the consumers dispatch manager thread
		cdThread = new ConsumerDispatcher(eventsEnumeration, dba);
		cdThread.start();
	}

	@Override
	public int reportListenableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		throw new RuntimeException("Listenable Events not available for now, for database queue event link");
	}

	@Override
	public int reportConsumableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		try {
			// insert into the queue
			PreparedProcedureInvocationDto procedure = new PreparedProcedureInvocationDto("InsertNewQueueElement");
			dataBureau.serializeQueueEntry(event, procedure);
			int eventId = (Integer) dba.invokeScalarProcedure(procedure);
			// notify the consumers dispatch manager
			synchronized (cdThread) {
				// inform the internal notification mechanism
				cdThread.notificationCount++;
//System.err.println("+"+cdThread.notificationCount);
				cdThread.notify();
			}
			
			return eventId;
			
		} catch (Throwable t) {
			log.reportThrowable(t, "Error while attempting to insert PostgreSQL queue element");
			return -1;
		}
	}
	
	
	
	@Override
	public boolean addClient(EventClient<SERVICE_EVENTS_ENUMERATION> client) throws IndirectMethodNotFoundException {
		boolean result = localEventDispatchingQueue.addClient(client);
		synchronized (cdThread) {
			cdThread.notify();
		}
		return result;
	}

	@Override
	public boolean deleteClient(EventClient<SERVICE_EVENTS_ENUMERATION> client) {
		// TODO: what would happen if we delete all consumers while there are still elements on the queue? Fix for this scenario
		return localEventDispatchingQueue.deleteClient(client);
	}

	public boolean hasPendingEvents() {
//System.err.println("="+cdThread.notificationCount);
		return cdThread.processing || ((QUEUE_POOLING_TIME == 0) && (cdThread.notificationCount > 0));
	}
	
	public void stop() {
		log.reportEvent(IE_INTERRUPTING, IP_QUEUE_TABLE_NAME, queueTableName);
		synchronized (cdThread) {
			cdThread.stop  = true;
			cdThread.notify();
		}
		for (int i=0; (i<10) && cdThread.processing; i++) {
			log.reportEvent(IE_WAITING_CONSUMERS);
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}
		if (cdThread.processing) {
			log.reportEvent(IE_FORCING_CONSUMERS_SHUTDOWN);
		}
		cdThread.interrupt();
		log.reportEvent(IE_INTERRUPTION_COMPLETE, IP_QUEUE_TABLE_NAME, queueTableName);
	}
	
	/** Returns any 'eventIds' that could not be processed and are on the fallback queue. When this method is called, the fallback queue is
	 *  emptied */
	public int[] popFallbackEventIds() throws SQLException {
		PreparedProcedureInvocationDto procedure = new PreparedProcedureInvocationDto("PopFallbackElements");
		Object[][] data = dba.invokeArrayProcedure(procedure);
		int[] eventIds = new int[data.length];
		for (int i=0; i<eventIds.length; i++) {
			eventIds[i] = (Integer)data[i][0];
		}
		return eventIds;
	}
	
	/** for testing purposes only */
	public void resetQueues() throws SQLException {
		dba.resetQueues();
		synchronized (cdThread) {
			cdThread.notificationCount = 0;
			cdThread.notify();
		}
	}
	
}