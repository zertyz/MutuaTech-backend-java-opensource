package mutua.events;

import java.util.Hashtable;

import mutua.events.annotations.EventConsumer;
import mutua.events.annotations.EventListener;
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
 * Each 'EventServer' instance must have his own 'IEventLink' instance for them behave like different instances 
 *
 * @see DirectEventLink, NonblockingEventLink, QueueEventLink 
 * @version $Id$
 * @author luiz
 */

public abstract class IEventLink<SERVICE_EVENTS_ENUMERATION> {
	
	protected Hashtable<EventClient<SERVICE_EVENTS_ENUMERATION>, IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>> clientsAndListenerMethodInvokers;
	protected Hashtable<EventClient<SERVICE_EVENTS_ENUMERATION>, IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>> clientsAndConsumerMethodInvokers;
	private Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration;
	
	public IEventLink(Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration) {
		clientsAndListenerMethodInvokers = new Hashtable<EventClient<SERVICE_EVENTS_ENUMERATION>, IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>>();
		clientsAndConsumerMethodInvokers = new Hashtable<EventClient<SERVICE_EVENTS_ENUMERATION>, IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>>();
		this.eventsEnumeration           = eventsEnumeration;
	}
	
	/** Attempt to add an 'EventClient' to the client's list.
	 *  returns false if the client is already present. */
	public boolean addClient(EventClient<SERVICE_EVENTS_ENUMERATION> client) throws IndirectMethodNotFoundException {
		if (clientsAndListenerMethodInvokers.containsKey(client) && clientsAndConsumerMethodInvokers.containsKey(client)) {
			return false;
		} else {
			IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION> clientMethodInvoker = new IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>(client, eventsEnumeration, EventListener.class);
			clientsAndListenerMethodInvokers.put(client, clientMethodInvoker);
			IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION> consumerMethodInvoker = new IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION>(client, eventsEnumeration, EventConsumer.class);
			clientsAndConsumerMethodInvokers.put(client, consumerMethodInvoker);
			return true;
		}
	}
	
	/** Attempt to delete an 'EventClient' from the client's list.
	 *  returns false if the client isn't present. */
	public boolean deleteClient(EventClient<SERVICE_EVENTS_ENUMERATION> client) {
		if (clientsAndListenerMethodInvokers.containsKey(client) || clientsAndConsumerMethodInvokers.containsKey(client)) {
			clientsAndListenerMethodInvokers.remove(client);
			clientsAndConsumerMethodInvokers.remove(client);
			return true;
		} else {
			return false;
		}
	}
	
	/** Takes actions to notify all client's appropriate 'Listener' methods that an event happened.
	 *  Returns the 'eventId' or -1, if not applicable */
	public abstract int reportListenableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event);
	
	/** Takes actions to notify the appropriate 'Consumer' method of one of the client's that an event happened,
	 *  returning the 'eventId' (or -1 if not applicable) */
	public abstract int reportConsumableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event);
	
	/** Returns true if, for sure, events for the specified 'serviceId' cannot be consumed by any consumer */
	public boolean areEventsNotConsumable(Object serviceId) {
//		// true for direct event link
//		if (Arrays.binarySearch(eventConsumerServiceIds, serviceId) < 0) {
//			return true;
//		} else {
			return false;
//		}
	}

}
