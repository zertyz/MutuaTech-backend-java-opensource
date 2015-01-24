package mutua.events;

import java.util.ArrayList;
import java.util.Arrays;
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
 * Defines how event servers and clients will communicate
 *
 * @see DirectEventLink, NonblockingEventLink, QueueEventLink 
 * @version $Id$
 * @author luiz
 */

public abstract class IEventLink<E> {
	
	protected Hashtable<EventClient, IndirectMethodInvoker<E>> clientsAndListenerMethodInvokers;
	protected Hashtable<EventClient, IndirectMethodInvoker<E>> clientsAndConsumerMethodInvokers;
	private Class<E> eventsEnumeration;
	
	public IEventLink(Class<E> eventsEnumeration) {
		clientsAndListenerMethodInvokers = new Hashtable<EventClient, IndirectMethodInvoker<E>>();
		clientsAndConsumerMethodInvokers = new Hashtable<EventClient, IndirectMethodInvoker<E>>();
		this.eventsEnumeration   = eventsEnumeration;
	}
	
	/** Attempt to add an 'EventClient' to the client's list.
	 *  returns false if the client is already present. */
	public boolean addClient(EventClient client) throws IndirectMethodNotFoundException {
		if (clientsAndListenerMethodInvokers.containsKey(client) && clientsAndConsumerMethodInvokers.containsKey(client)) {
			return false;
		} else {
			IndirectMethodInvoker<E> clientMethodInvoker = new IndirectMethodInvoker<E>(client, eventsEnumeration, EventListener.class);
			clientsAndListenerMethodInvokers.put(client, clientMethodInvoker);
			IndirectMethodInvoker<E> consumerMethodInvoker = new IndirectMethodInvoker<E>(client, eventsEnumeration, EventConsumer.class);
			clientsAndConsumerMethodInvokers.put(client, consumerMethodInvoker);
			return true;
		}
	}
	
	/** Attempt to delete an 'EventClient' from the client's list.
	 *  returns false if the client isn't present. */
	public boolean deleteClient(EventClient client) {
		if (clientsAndListenerMethodInvokers.containsKey(client) || clientsAndConsumerMethodInvokers.containsKey(client)) {
			clientsAndListenerMethodInvokers.remove(client);
			clientsAndConsumerMethodInvokers.remove(client);
			return true;
		} else {
			return false;
		}
	}
	
	/** Takes actions to notify all client's appropriate 'Listener' methods that an event happened */
	public abstract void reportListenableEvent(IndirectMethodInvocationInfo<E> event);
	
	/** Takes actions to notify the appropriate 'Consumer' method of one of the client's that an event happened */
	public abstract void reportConsumableEvent(IndirectMethodInvocationInfo<E> event);
	
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
