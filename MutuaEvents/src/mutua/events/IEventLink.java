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
	
	//private final EventServices services;
	private Object[] eventListenerServiceIds;
	private Object[] eventConsumerServiceIds;
	
	protected Hashtable<EventClient, IndirectMethodInvoker<E>> clientsAndMethodInvokers;
	private Class<E> eventsEnumeration;
	
	public IEventLink(Class<E> eventsEnumeration) {
		clientsAndMethodInvokers = new Hashtable<EventClient, IndirectMethodInvoker<E>>();
		this.eventsEnumeration   = eventsEnumeration;
	}
	
	/** Attempt to add an 'EventClient' to the client's list.
	 *  returns false if the client is already present. 
	 * @throws IndirectMethodNotFoundException */
	public boolean addClient(EventClient client) throws IndirectMethodNotFoundException {
		if (clientsAndMethodInvokers.containsKey(client)) {
			return false;
		} else {
			IndirectMethodInvoker<E> methodInvoker = new IndirectMethodInvoker<E>(client, eventsEnumeration, EventListener.class, EventConsumer.class);
			clientsAndMethodInvokers.put(client, methodInvoker);
			return true;
		}
	}
	
	/** Attempt to delete an 'EventClient' from the client's list.
	 *  returns false if the client isn't present. */
	public boolean deleteClient(EventClient client) {
		if (clientsAndMethodInvokers.containsKey(client)) {
			clientsAndMethodInvokers.remove(client);
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
		// true for direct event link
		if (Arrays.binarySearch(eventConsumerServiceIds, serviceId) < 0) {
			return true;
		} else {
			return false;
		}
	}

}
