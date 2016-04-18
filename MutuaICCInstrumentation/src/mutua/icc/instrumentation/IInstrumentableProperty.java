package mutua.icc.instrumentation;

import java.lang.reflect.Method;

import mutua.serialization.SerializationRepository;

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

public interface IInstrumentableProperty {
	
	/** Returns the name of the property */
	String getInstrumentationPropertyName();
	
	/** Returns the value data type of the property */
	Class<?> getInstrumentationPropertyType();
	
	/** Returns the {@link SerializationRepository#getSerializationMethod(Class)} for the class this property represents. Should be implemented as:<pre>
	 *  return SerializationRepository.getSerializationMethod(instrumentationPropertyType); */
	Method getTextualSerializationMethod();
	
}
