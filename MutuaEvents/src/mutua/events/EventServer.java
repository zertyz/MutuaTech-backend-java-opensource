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

public class EventServer<SERVICE_EVENTS_ENUMERATION> {

	protected final IEventLink<SERVICE_EVENTS_ENUMERATION> link;
	
	protected EventServer(IEventLink<SERVICE_EVENTS_ENUMERATION> link) {
		this.link = link;
	}
	
	/** @see IEventLink#reportListenableEvent*/
	protected int dispatchListenableEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		return link.reportListenableEvent(new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters));
	}
	
	/** @see IEventLink#reportListenableEvents*/
	protected int[] dispatchListenableEvents(SERVICE_EVENTS_ENUMERATION serviceId, Object[][] parametersSet) {
		IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>[] events = (IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>[]) new IndirectMethodInvocationInfo<?>[parametersSet.length];
		for (int i=0; i<parametersSet.length; i++) {
			events[i] = new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parametersSet[i]);
		}
		return link.reportListenableEvents(events);
	}
	
	/** @see IEventLink#reportConsumableEvent */
	protected int dispatchConsumableEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		return link.reportConsumableEvent(new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters));
	}
	
	/** @see IEventLink#reportConsumableEvents */
	protected int[] dispatchConsumableEvents(SERVICE_EVENTS_ENUMERATION serviceId, Object[][] parametersSet) {
		IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>[] events = (IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>[]) new IndirectMethodInvocationInfo<?>[parametersSet.length];
		for (int i=0; i<parametersSet.length; i++) {
			events[i] = new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parametersSet[i]);
		}
		return link.reportConsumableEvents(events);
	}
	
	/** Similar to {@link #dispatchConsumableEvents(Object, Object[][])}, but for the special case where each event only has 1 parameter */
	protected int[] dispatchConsumableEvents(SERVICE_EVENTS_ENUMERATION serviceId, Object[] parameterSet) {
		IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>[] events = (IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>[]) new IndirectMethodInvocationInfo<?>[parameterSet.length];
		for (int i=0; i<parameterSet.length; i++) {
			events[i] = new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameterSet[i]);
		}
		return link.reportConsumableEvents(events);
	}

	protected void dispatchListenableAndConsumableEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event = new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters);
		link.reportListenableEvent(event);
		link.reportConsumableEvent(event);
	}
	
	/** @see IEventLink#addListener(EventClient) */
	public boolean addListener(EventClient<SERVICE_EVENTS_ENUMERATION> listenerClient) throws IndirectMethodNotFoundException {
		return link.addListener(listenerClient);
	}
	
	/** @see IEventLink#removeListener(EventClient) */
	public boolean removeListener(EventClient<SERVICE_EVENTS_ENUMERATION> listenerClient) {
		return link.removeListener(listenerClient);
	}
	
	/** @see IEventLink#setConsumer(EventClient) */
	public void setConsumer(EventClient<SERVICE_EVENTS_ENUMERATION> consumerClient) throws IndirectMethodNotFoundException {
		link.setConsumer(consumerClient);
	}
	
	/** @see IEventLink#unsetConsumer() */
	public void unsetConsumer() {
		link.unsetConsumer();
	}
	
}

