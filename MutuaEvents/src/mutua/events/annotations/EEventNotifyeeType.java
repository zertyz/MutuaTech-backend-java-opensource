package mutua.events.annotations;

/** <pre>
 * EEventNotifyeeType.java
 * =======================
 * (created by luiz, Feb 24, 2016)
 *
 * An enumeration to be used as part of 'EventConsumer' & 'EventListener' Annotation Patterns
 * to tell whether the event processor function is a Consumer or a Listener
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
*/

public enum EEventNotifyeeType {
	EVENT_LISTENER,
	EVENT_CONSUMER,
}
