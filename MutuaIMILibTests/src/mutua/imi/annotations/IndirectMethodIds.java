package mutua.imi.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * IndirectMethodIds.java
 * ================
 * (created by luiz, Jan 23, 2015)
 *
 * Marks a method with IDs for later indirect invocation, for testing & demonstration purposes.
 *
 * @see IndirectMethodId
 * @version $Id$
 * @author luiz
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface IndirectMethodIds {

	EClientMethods[] value();
	
}
