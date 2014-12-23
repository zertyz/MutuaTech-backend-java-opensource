package mutua.p2pcommunications;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * P2PServicesAPIRequestingMethod.java
 * ===================================
 * (created by luiz, Dec 22, 2014)
 *
 * Annotation that signals that the 'P2PServicesAPI' method is intended to ask something, via the network, to another API method 
 *
 * @see P2PServicesAPIAsynchronousRequestAnsweringMethod
 * @version $Id$
 * @author luiz
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface P2PServicesAPIRequestingMethod {

}
