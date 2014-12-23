package mutua.p2pcommunications;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * P2PServicesAPISynchronousRequestReceiverMethod.java
 * ===================================================
 * (created by luiz, Dec 22, 2014)
 *
 * Annotation that signals that the annotated 'P2PServicesAPI' method implements an answer receiver method,
 * completing the communication for the request started by the API method received as parameter.
 * 
 * Methods annotated with this one, should return a boolean value indicating whether the answer was accepted
 * or not.
 *
 * @see P2PServicesAPI
 * @version $Id$
 * @author luiz
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface P2PServicesAPIAnswerReceiverMethod {

	/** The API method who started the communication */
	String value();

}
