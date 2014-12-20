package mutua.p2pcommunications;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * RecognizedBy.java
 * =================
 * (created by luiz, Dec 20, 2014)
 *
 * This annotation holds the information of which API method should interpret what is being
 * generated by the API method using this annotation
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RecognizedBy {

	String value();

}
