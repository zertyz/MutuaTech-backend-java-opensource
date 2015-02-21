package mutua.icc.configuration.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Enumeration;

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

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurableElement {

	/** This should be specified when declaring a new configuration property */
	String value() default "";

	/** This should be specified when declaring an existing configuration property.
	 *  sameAs := "package.className.publicStaticFieldName"
	 *  if using on inner class enumerations:
	 *  sameAs := "package.className$EnumName.ENTRY_NAME" */
	String sameAs() default "";

	/** Through this property, the annotation will get the comment from the methods javadoc, if we could. Since we cant, you must annotate the method
	 *  with annotation: @ConfigurableElement("this was supposed to be the method's javadoc") void shittyMethod() {};
	 *  sameAsMethod := "package.className.MethodName" */
	String sameAsMethod() default "";

}
