package mutua.events;

import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodNotFoundException;

/** <pre>
 * EventServer.java
 * ================
 * (created by luiz, Jan 23, 2015)
 *
 * Base class for all event servers. Helps with the dispatch of events.
 * Listenable events are events that will be delivered for the 'EventListener' annotated methods of all clients;
 * Consumable events are events that may be consumed by an 'EventConsumer' annotated method of one of the clients;
 * Need to be Consumed events are like the above, except that they should be consumed. False is returned if it can't be consumed.
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class EventServer<E> {

	protected final IEventLink<E> link;
	
	protected EventServer(IEventLink<E> link) {
		this.link = link;
	}
	
	protected void dispatchListenableEvent(E serviceId, Object... parameters) {
		link.reportListenableEvent(new IndirectMethodInvocationInfo<E>(serviceId, parameters));
	}
	
	protected void dispatchConsumableEvent(E serviceId, Object... parameters) {
		link.reportConsumableEvent(new IndirectMethodInvocationInfo<E>(serviceId, parameters));
	}

	protected void dispatchListenableAndConsumableEvent(E serviceId, Object... parameters) {
		IndirectMethodInvocationInfo<E> event = new IndirectMethodInvocationInfo<E>(serviceId, parameters);
		link.reportListenableEvent(event);
		link.reportConsumableEvent(event);
	}
	
	protected boolean dispatchNeedToBeConsumedEvent(E serviceId, Object... parameters) {
		if (link.areEventsNotConsumable(serviceId)) {
			return false;
		} else {
			link.reportConsumableEvent(new IndirectMethodInvocationInfo<E>(serviceId, parameters));
			return true;
		}
	}
	
	protected boolean dispatchListenableAndNeedToBeConsumedEvent(E serviceId, Object... parameters) {
		if (link.areEventsNotConsumable(serviceId)) {
			return false;
		} else {
			IndirectMethodInvocationInfo<E> event = new IndirectMethodInvocationInfo<E>(serviceId, parameters);
			link.reportListenableEvent(event);
			link.reportConsumableEvent(event);
			return true;
		}
	}
	
	/** @see IEventLink#addClient(EventClient) */
	public boolean addClient(EventClient client) throws IndirectMethodNotFoundException {
		return link.addClient(client);
	}
	
	/** @see IEventLink#deleteClient(EventClient) */
	public boolean deleteClient(EventClient client) {
		return link.deleteClient(client);
	}
	
}

