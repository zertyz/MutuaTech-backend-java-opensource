package mutua.icc.instrumentation;

import java.lang.reflect.Method;

import mutua.serialization.SerializationRepository;

/** <pre>
 * IInstrumentableProperty.java
 * ============================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines an "Instrumentable Property", which defines the variables/data format of the values contained in {@link InstrumentableEvent}s
 *
 * @version $Id$
 * @author luiz
 */

public class InstrumentableProperty {
	
	public final String   propertyName;
	public final Class<?> propertyType;
	public final Method   serializationMethod;
	
	/** Constructs an instrumentable property representing the given type */
	public InstrumentableProperty(String propertyName, Class<?> propertyType) {
		this.propertyName        = propertyName;
		this.propertyType        = propertyType;
		this.serializationMethod = SerializationRepository.getSerializationMethod(propertyType);
	}
}
