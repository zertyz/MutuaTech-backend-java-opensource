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
 * Annotation that holds the information of which regular expression should a protocol message match
 * in order to consider the annotated 'P2PServicesAPI' method the one to process the request
 *
 * @see P2PServicesAPI
 * @version $Id$
 * @author luiz
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RecognizePattern {

	/** The pattern that activate this method */
	String value();
	// Pattern compiledPattern default new Pattern(value()) may it increase performance?

}
