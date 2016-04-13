package mutua.events;

import java.lang.annotation.Annotation;
import java.util.Hashtable;

import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodInvoker;
import mutua.imi.IndirectMethodNotFoundException;

/** <pre>
 * IEventLink.java
 * ===============
 * (created by luiz, Jan 23, 2015)
 *
 * Defines how event servers and clients will communicate.
 * 
 * Each 'EventServer' instance must have his own 'IEventLink' instance for them behave like different instances.
 * 
 * Each 'EventClient' consumer must implement all consumable events, or else some events won't be consumed -- since, currently, only
 * 1 consumer is allowed.
 *
 * @see DirectEventLink, NonblockingEventLink, QueueEventLink 
 * @version $Id$
 * @author luiz
 */

public abstract class IEventLink<SERVICE_EVENTS_ENUMERATION> {
	
	// TODO is it possible to use a HashMap to be 2x faster?
	protected Hashtable<EventClient<SERVICE_EVENTS_ENUMERATION>, IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>> clientsAndListenerMethodInvokers;
	protected IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION> consumerMethodInvoker;
	private final Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration;
	private final Class<? extends Annotation>[] annotationClasses;
	
	public IEventLink(Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration, Class<? extends Annotation>[] annotationClasses) {
		clientsAndListenerMethodInvokers = new Hashtable<EventClient<SERVICE_EVENTS_ENUMERATION>, IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>>();
		consumerMethodInvoker            = null;
		this.eventsEnumeration           = eventsEnumeration;
		this.annotationClasses           = annotationClasses;
	}
	
	/** Attempt to add an 'EventClient' to the listener's clients list.
	 *  returns false if the client is already present. */
	public boolean addListener(EventClient<SERVICE_EVENTS_ENUMERATION> listenerClient) throws IndirectMethodNotFoundException {
		if (clientsAndListenerMethodInvokers.containsKey(listenerClient)) {
			return false;
		} else {
			IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION> clientMethodInvoker = new IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>(listenerClient, eventsEnumeration, annotationClasses);
			clientsAndListenerMethodInvokers.put(listenerClient, clientMethodInvoker);
			return true;
		}
	}
	
	/** Attempt to delete an 'EventClient' from the listener's clients list.
	 *  returns false if the client isn't present. */
	public boolean removeListener(EventClient<SERVICE_EVENTS_ENUMERATION> listenerClient) {
		if (clientsAndListenerMethodInvokers.containsKey(listenerClient)) {
			clientsAndListenerMethodInvokers.remove(listenerClient);
			return true;
		} else {
			return false;
		}
	}
	
	/** Attempt to set an 'EventClient' as the consumer for all consumable events.
	 *  Throws an exception if a consumer has already been set. */
	public void setConsumer(EventClient<SERVICE_EVENTS_ENUMERATION> consumerClient) throws IndirectMethodNotFoundException {
		if (this.consumerMethodInvoker == null) {
			this.consumerMethodInvoker = new IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>(consumerClient, eventsEnumeration, annotationClasses);
		} else {
			throw new RuntimeException("Attempt to set a new consumer client '"+consumerClient.getClass().getName()+"' for IEventLink '"+this.getClass().getName()+"', but another one has been already set '"+consumerMethodInvoker+"'");
		}
	}
	
	/** Unset any previously set consumers, allowing for a new consumer to be set with 'setConsumer' */
	public void unsetConsumer() {
		this.consumerMethodInvoker = null;
	}
	
	/** Takes actions to notify all client's appropriate 'Listener' methods that an event happened.
	 *  Returns the 'eventId' or -1, if not applicable */
	public abstract int reportListenableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event);
	
	/** Similar to {@link #reportListenableEvent}, but works for a set of events. Extending classes are encouraged to override this method if
	 *  there is an opportunity to make it more efficient than the original implementation. */
	public int[] reportListenableEvents(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>... events) {
		int[] eventIds = new int[events.length];
		for (int i=0; i<events.length; i++) {
			eventIds[i] = reportListenableEvent(events[i]);
		}
		return eventIds;
	}

	/** Takes actions to notify the appropriate 'Consumer' method of one of the client's that an event happened,
	 *  returning the 'eventId' (or -1 if not applicable) */
	public abstract int reportConsumableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event);
	
	/** Similar to {@link #reportConsumableEvent}, but works for a set of events. Extending classes are encouraged to override this method if
	 *  there is an opportunity to make it more efficient than the original implementation. */
	public int[] reportConsumableEvents(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>... events) {
		int[] eventIds = new int[events.length];
		for (int i=0; i<events.length; i++) {
			eventIds[i] = reportConsumableEvent(events[i]);
		}
		return eventIds;
	}
	
}
