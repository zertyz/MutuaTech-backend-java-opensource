package mutua.events;

import java.lang.annotation.Annotation;
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
	
	// TODO redesign of MutuaEvents -- please refactor the .run methods above against the 'DirectEventLink' counterparts
	class ConsumerWorker extends Thread {
		
		private QueueEventLink<SERVICE_EVENTS_ENUMERATION> queueEventLink;
		
		public ConsumerWorker(QueueEventLink<SERVICE_EVENTS_ENUMERATION> queueEventLink) {
			this.queueEventLink = queueEventLink;
		}
		
		@Override
		public void run() {
			IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event = null;
			while (true) try {
				// process the next event on the queue
				event = null;	// needed, since 'take()' may suffer an interruption
				event = consumableEventsQueue.take();
				
				while (consumerMethodInvoker == null) {
					// do nothing if there are no consumers -- 'addConsumer' notifies when a consumer client arrives
					synchronized (queueEventLink) {
						System.out.println("QueueEventLink ConsumerWorker: no consumer client registered yet. Waiting...");
						queueEventLink.wait();
						System.out.println("QueueEventLink ConsumerWorker: Waking up, since we got a notification that a consumer client is now available!");
					}
				}
				
				consumerMethodInvoker.invokeMethod(event);
				continue;
					
			} catch (Throwable t) {
				t.printStackTrace();
				if (event != null) {
					queueEventLink.pushFallback(event, t);
				}
			}
		}
	}

	class ListenerWorker extends Thread {
		@Override
		public void run() {
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
	
	
	public QueueEventLink(Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration, Class<? extends Annotation>[] annotationClasses, int capacity, int numberOfConsumers) {
		super(eventsEnumeration, annotationClasses);
		listenableEventsQueue = new ArrayBlockingQueue<IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>>(capacity);
		consumableEventsQueue = new ArrayBlockingQueue<IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>>(capacity);
		
		// initiate the threads
		listenersWorkerThread = new ListenerWorker();
		listenersWorkerThread.start();
		consumerWorkerThreads = new Thread[numberOfConsumers];
		for (int i=0; i<consumerWorkerThreads.length; i++) {
			consumerWorkerThreads[i] = new ConsumerWorker(this);
			consumerWorkerThreads[i].start();
		}
	}

	/** Method called for events that could not be processed (threw an exception). Override to make it actually do something -- i.e., add to a fallback list */
	public void pushFallback(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event, Throwable t) {
		System.out.println("QueueEventLink: A consumer method generated an uncouth exception");
		t.printStackTrace();
	}

	@Override
	public int reportListenableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		try {
			listenableEventsQueue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int reportConsumableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		try {
			consumableEventsQueue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public void setConsumer(EventClient<SERVICE_EVENTS_ENUMERATION> consumerClient) throws IndirectMethodNotFoundException {
		super.setConsumer(consumerClient);
		synchronized (this) {
			this.notifyAll();
		}
	}

}