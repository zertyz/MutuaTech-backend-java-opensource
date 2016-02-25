package mutua.events;

import static mutua.icc.instrumentation.MutuaEventAdditionalEventLinksInstrumentationEvents.*;
import static mutua.icc.instrumentation.MutuaEventAdditionalEventLinksInstrumentationProperties.*;

import java.lang.annotation.Annotation;
import java.sql.SQLException;

import mutua.events.postgresql.QueuesPostgreSQLAdapter;
import mutua.icc.instrumentation.Instrumentation;
import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodNotFoundException;

import static mutua.events.postgresql.QueuesPostgreSQLAdapter.QueueParameters.*;

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
	
	
	// Mutua Configurable Class pattern
	///////////////////////////////////
	
	private static Instrumentation<?, ?> LOG;
	
	/** The amount of milliseconds the PostgreSQLQueueEventLink ConsumerDispatcher thread should wait between queries for new " +
	 *  "queue entries to process. Set to 0 to rely on the internal notification mechanisms and only when queue producers and " +
	 *  "consumers are running on the same machine and on the same process. */
	private static long QUEUE_POOLING_TIME = 0;
	/** The number of threads that will consume the queue events */
	private static int  QUEUE_NUMBER_OF_WORKER_THREADS = 10;
	/** The annotation classes for the {@link IEventLink} */
	private static Class<? extends Annotation>[] ANNOTATION_CLASSES;
	
	/** method to be called when attempting to configure the default behavior for new instances of 'PostgreSQLQueueEventLink'.<pre>
	 *  @param annotationClasses            the Annotation classes that should annotate methods which will consume the events hereby
	 *  @param queuePoolingTime             if < 0, the default value won't be touched
	 *  @param queueNumberOfWorkerThreads   if <= 0, the default value won't be touched */
	public static void configureDefaultValuesForNewInstances(Instrumentation<?, ?> log, Class<? extends Annotation>[] annotationClasses, long queuePoolingTime, int queueNumberOfWorkerThreads) {		
			LOG                            = log;
			ANNOTATION_CLASSES             = annotationClasses;
			QUEUE_POOLING_TIME             = queuePoolingTime           >= 0 ? queuePoolingTime           : QUEUE_POOLING_TIME;
			QUEUE_NUMBER_OF_WORKER_THREADS = queueNumberOfWorkerThreads >  0 ? queueNumberOfWorkerThreads : QUEUE_NUMBER_OF_WORKER_THREADS;;
	}

	
	/** Designed to be run by a single thread, this class fetches the database and assigns work for instances of 'ConsumerWorker' */
	class ConsumerDispatcher extends Thread {
		
		private final Instrumentation<?, ?> log;
		private final long queuePoolingTime;
		private final Class<?> eventsEnumeration;
		private final QueuesPostgreSQLAdapter dba;
		public boolean stop = false;
		public boolean processing = true;
		public int notificationCount = 0;	// for the internal notification mechanism, when 'QUEUE_POOLING_TIME' is 0
		
		public ConsumerDispatcher(Instrumentation<?, ?> log, long queuePoolingTime, Class<?> eventsEnumeration, QueuesPostgreSQLAdapter dba) {
			this.log = log;
			this.queuePoolingTime = queuePoolingTime;
			this.eventsEnumeration = eventsEnumeration;
			this.dba = dba;
			setName("PostgreSQLQueueEventLink ConsumerDispatcher thread");

		}
		
		@Override
		public void run() {
			
			// wait until there is a consumer -- 'addConsumer' notifies 'this'
			if (consumerMethodInvoker == null) try {
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
					Object[][] rowsOfQueueEntries = dba.invokeArrayProcedure(dba.FetchNextQueueElements);
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
							dba.invokeUpdateProcedure(dba.UpdateLastFetchedEventId, LAST_FETCHED_EVENT_ID, lastFetchedEventId);
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
								wait(queuePoolingTime);
							} catch (InterruptedException e) {}
							processing = true;
						}
					}
				}
					
			}
			processing = false;
		}
	}
	
	
	private   final Instrumentation<?, ?>                                log;
	private   final QueuesPostgreSQLAdapter                              dba;
	private   final QueueEventLink<SERVICE_EVENTS_ENUMERATION>           localEventDispatchingQueue;
	protected final String                                               queueTableName;
	private   final IDatabaseQueueDataBureau<SERVICE_EVENTS_ENUMERATION> dataBureau;
	private   final ConsumerDispatcher                                   cdThread;
	

	public PostgreSQLQueueEventLink(Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration,
	                                final String queueTableName, final IDatabaseQueueDataBureau<SERVICE_EVENTS_ENUMERATION> dataBureau) throws SQLException {
		super(eventsEnumeration, ANNOTATION_CLASSES);
		this.log = LOG;
		this.queueTableName = queueTableName;
		this.dataBureau = dataBureau;
		dba = QueuesPostgreSQLAdapter.getQueuesDBAdapter(eventsEnumeration, queueTableName, dataBureau.getFieldsCreationLine(),
		                                                 dataBureau.getQueueElementFieldList(),
		                                                 dataBureau.getParametersListForInsertNewQueueElementQuery(),
		                                                 QUEUE_NUMBER_OF_WORKER_THREADS);
		
		// creates an 'QueueEventLink' that will report events to the listeners and consumers of this instance
		int localEventDispatchingQueueCapacity = 2*QUEUE_NUMBER_OF_WORKER_THREADS;
		localEventDispatchingQueue = new QueueEventLink<SERVICE_EVENTS_ENUMERATION>(eventsEnumeration, ANNOTATION_CLASSES, localEventDispatchingQueueCapacity, QUEUE_NUMBER_OF_WORKER_THREADS) {
			@Override
			// this 'QueueEventLink' adds to the fallback queue whenever the consumpsion of an element generates an uncought exception
			public void pushFallback(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event, Throwable t) {
				log.reportThrowable(t, "Error consuming event '"+event.toString()+"' from the queue '"+queueTableName+"'. Adding it to the fallback queue...");
				try {
					// TODO 'pushFallback' should receive the 'eventId', shouldn't it?
					Object[] rowParameters = dataBureau.serializeQueueEntry(event);
					dba.invokeUpdateProcedure(dba.InsertIntoFallbackQueue, rowParameters);
				} catch (SQLException e) {
					log.reportThrowable(e, "Error adding event '"+event.toString()+"' to the fallback queue '"+queueTableName+"Fallback'");
				}
			}
		};
		localEventDispatchingQueue.consumerMethodInvoker            = consumerMethodInvoker;
		localEventDispatchingQueue.clientsAndListenerMethodInvokers = clientsAndListenerMethodInvokers;
		
		// start the consumers dispatch manager thread
		cdThread = new ConsumerDispatcher(log, QUEUE_POOLING_TIME, eventsEnumeration, dba);
		cdThread.start();
	}

	@Override
	public int reportListenableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		throw new RuntimeException("Listenable Events not available for database queue event link");
	}

	@Override
	public int reportConsumableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		
		// TODO an optimization is possible for a single node (single machine) service: the quele element should be inserted on the database, but also on the ram Queue, if it has enough slots available. In this case, the lastConsumedEvent must be also set after inserting.
		// TODO PostgreSQL have a LISTEN/NOTIFY command set which might bring some performance improvements -- it can be done with pgjdbc-ng driver http://blog.databasepatterns.com/2014/04/postgresql-nofify-websocket-spring-mvc.html but not with the usual JDBC implementation, since JDBC does not allow asynchronous notifications: https://jdbc.postgresql.org/documentation/80/listennotify.html 
		
		try {
			// insert into the queue
			Object[] rowParameters = dataBureau.serializeQueueEntry(event);
			int eventId = (Integer) dba.invokeScalarProcedure(dba.InsertNewQueueElement, rowParameters);
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
	public void setConsumer(EventClient<SERVICE_EVENTS_ENUMERATION> consumerClient) throws IndirectMethodNotFoundException {
		localEventDispatchingQueue.setConsumer(consumerClient);
		consumerMethodInvoker = localEventDispatchingQueue.consumerMethodInvoker;
		// notify that the processing of events may commence
		synchronized (cdThread) {
			cdThread.notify();
		}
	}

	@Override
	public void unsetConsumer() {
		// TODO: what would happen if we delete all consumers while there are still elements on the queue? Fix for this scenario
		localEventDispatchingQueue.unsetConsumer();
		consumerMethodInvoker = localEventDispatchingQueue.consumerMethodInvoker;
	}

	public boolean hasPendingEvents() {
//System.err.println("="+cdThread.notificationCount);
		return cdThread.processing || ((cdThread.queuePoolingTime == 0) && (cdThread.notificationCount > 0));
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
		Object[][] data = dba.invokeArrayProcedure(dba.PopFallbackElements);
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