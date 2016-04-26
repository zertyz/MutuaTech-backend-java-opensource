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
	
	// property value flags
	public static final int ABSOLUTE_VALUE    = 1;
	public static final int INCREMENTAL_VALUE = 2;
	
	public final String   propertyName;
	public final Class<?> propertyType;
	public final int      valueFlags;
	public final Method   serializationMethod;
	
	public InstrumentableProperty(String propertyName, Class<?> propertyType, int valueFlags) {
		this.propertyName        = propertyName;
		this.propertyType        = propertyType;
		this.valueFlags          = valueFlags;
		this.serializationMethod = SerializationRepository.getSerializationMethod(propertyType);
	}
}
