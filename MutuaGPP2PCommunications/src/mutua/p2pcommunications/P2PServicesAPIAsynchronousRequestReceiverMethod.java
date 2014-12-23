package mutua.p2pcommunications;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * P2PServicesAPIAsynchronousRequestReceiverMethod.java
 * ====================================================
 * (created by luiz, Dec 22, 2014)
 *
 * Annotation that signals that the 'P2PServicesAPI' method implements an asynchronous communication and the answer is
 * given by the method named on the parameter.
 * 
 * API methods with this annotation must have a return type of 'P2PServicesAPIMethodCallInfo'.
 *
 * @see P2PServicesAPI, P2PServicesAPIMethodCallInfo
 * @version $Id$
 * @author luiz
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface P2PServicesAPIAsynchronousRequestReceiverMethod {

	/** the method name that will generate the response, completing the asynchronous communication */
	String value();

}
