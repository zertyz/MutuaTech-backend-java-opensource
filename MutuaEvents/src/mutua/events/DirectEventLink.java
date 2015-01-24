package mutua.events;

import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodInvoker;
import mutua.imi.IndirectMethodNotFoundException;

/** <pre>
 * DirectEventLink.java
 * ====================
 * (created by luiz, Jan 23, 2015)
 *
 * Implements a in-memory, same thread, blocking client dispatching 'IEventLink'
 *
 * @see IEventLink
 * @version $Id$
 * @author luiz
 */

public class DirectEventLink<E> extends IEventLink<E> {

	public DirectEventLink(Class<E> eventsEnumeration) {
		super(eventsEnumeration);
	}

	@Override
	public void reportListenableEvent(IndirectMethodInvocationInfo<E> event) {
		for (EventClient client : clientsAndListenerMethodInvokers.keySet()) try {
			IndirectMethodInvoker<E> imi = clientsAndListenerMethodInvokers.get(client);
			imi.invokeMethod(event);
		} catch (IndirectMethodNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reportConsumableEvent(IndirectMethodInvocationInfo<E> event) {
		for (EventClient client : clientsAndConsumerMethodInvokers.keySet()) try {
			IndirectMethodInvoker<E> imi = clientsAndConsumerMethodInvokers.get(client);
			imi.invokeMethod(event);
		} catch (IndirectMethodNotFoundException e) {
			e.printStackTrace();
		}
	}

}
