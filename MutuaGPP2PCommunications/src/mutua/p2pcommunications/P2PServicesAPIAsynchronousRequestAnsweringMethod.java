package mutua.p2pcommunications;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * P2PServicesAPIAsynchronousRequestAnsweringMethod.java
 * =====================================================
 * (created by luiz, Dec 22, 2014)
 *
 * Annotation that signals that the annotated 'P2PServiceAPI' method is intended to answer to a request, via the network,
 * composing the asynchronous answer for that request
 *
 * @see P2PServicesAPIRequestingMethod, P2PServicesAPISynchronousRequestAnsweringMethod
 * @version $Id$
 * @author luiz
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface P2PServicesAPIAsynchronousRequestAnsweringMethod {

	/** The method name that generates the request being answer by the annotated method */
	String value();

}
