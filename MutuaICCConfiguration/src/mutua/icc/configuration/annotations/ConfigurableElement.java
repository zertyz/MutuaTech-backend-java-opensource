package mutua.icc.configuration.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * ConfigurationElement.java
 * =========================
 * (created by luiz, Jan 29, 2015)
 *
 * Annotation to gather configuration entries documentation on the code, and later report
 * them on the configuration file
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurableElement {

	/** This should be specified when declaring a new configuration property */
	String value() default "";

	/** This should be specified when declaring an existing configuration property.
	 * sameAs := "package.className.publicStaticFieldName" */
	String sameAs() default "";

}
