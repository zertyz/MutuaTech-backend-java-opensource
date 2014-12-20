package mutua.p2pcommunications;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * RecognizePattern.java
 * =====================
 * (created by luiz, Dec 20, 2014)
 *
 * This annotation holds the information of which regular expression should a protocol message match
 * in order to consider that the API method using this annotation is the one to process the message
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RecognizePattern {

	String value();
	// Pattern compiledPattern default new Pattern(value()) may it increase performance?

}
