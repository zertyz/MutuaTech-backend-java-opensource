package mutua.icc.instrumentation;

import mutua.serialization.ISerializationRule;

/** <pre>
 * IInstrumentableProperty.java
 * ============================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines an instrumentable property, which provides data on a specific format
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public interface IInstrumentableProperty extends ISerializationRule {
	
	/** Returns the name of the property */
	String getInstrumentationPropertyName();
	
}
