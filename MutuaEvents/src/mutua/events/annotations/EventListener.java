package mutua.events.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * EventListener.java
 * ==================
 * (created by luiz, Jan 23, 2015)
 *
 * This annotation marks an 'EventClient' method as an event listener of the provided 'eventId's
 *
 * @see EventClient
 * @version $Id$
 * @author luiz
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EventListener {

	String[] value();
	
}