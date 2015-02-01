package mutua.events;

import java.util.concurrent.ArrayBlockingQueue;

import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodInvoker;
import mutua.imi.IndirectMethodNotFoundException;

/** <pre>
 * QueueEventLink.java
 * ===================
 * (created by luiz, Jan 26, 2015)
 *
 * Implements a local RAM memory backed buffer and non-blocking server dispatching 'IEventLink'
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class QueueEventLink<SERVICE_EVENTS_ENUMERATION> extends	IEventLink<SERVICE_EVENTS_ENUMERATION> {

	
	// TODO redesign of MutuaEvents -- listeners: there may be as many as you want; consumers: only one for each service.
	// TODO after that, please refactor the .run methods above against the 'DirectEventLink' counterparts
	class ConsumerWorker extends Thread {
		@Override
		public void run() {
			System.out.println("A consumer started");
			while (true) {
				try {
					IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event = consumableEventsQueue.take();
					for (EventClient<SERVICE_EVENTS_ENUMERATION> client : clientsAndConsumerMethodInvokers.keySet()) try {
						IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION> imi = clientsAndConsumerMethodInvokers.get(client);
						imi.invokeMethod(event);
					} catch (IndirectMethodNotFoundException e) {
						e.printStackTrace();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class ListenerWorker extends Thread {
		@Override
		public void run() {
			System.out.println("A listener started");
			while (true) {
				try {
					IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event = listenableEventsQueue.take();
					for (EventClient<SERVICE_EVENTS_ENUMERATION> client : clientsAndListenerMethodInvokers.keySet()) try {
						IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION> imi = clientsAndListenerMethodInvokers.get(client);
						imi.invokeMethod(event);
					} catch (IndirectMethodNotFoundException e) {
						// nothing to do -- there is no problema if no one is listening
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	protected ArrayBlockingQueue<IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>> listenableEventsQueue;
	protected ArrayBlockingQueue<IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>> consumableEventsQueue;
	
	private ListenerWorker   listenersWorkerThread;
	private Thread[]         consumerWorkerThreads;
	
	
	public QueueEventLink(Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration, int capacity, int numberOfConsumers) {
		super(eventsEnumeration);
		listenableEventsQueue = new ArrayBlockingQueue<IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>>(capacity);
		consumableEventsQueue = new ArrayBlockingQueue<IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>>(capacity);
		
		// initiate the threads
		listenersWorkerThread = new ListenerWorker();
		listenersWorkerThread.start();
		consumerWorkerThreads = new Thread[numberOfConsumers];
		for (int i=0; i<consumerWorkerThreads.length; i++) {
			consumerWorkerThreads[i] = new ConsumerWorker();
			consumerWorkerThreads[i].start();
		}
	}

	@Override
	public void reportListenableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		try {
			listenableEventsQueue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reportConsumableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		try {
			consumableEventsQueue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

