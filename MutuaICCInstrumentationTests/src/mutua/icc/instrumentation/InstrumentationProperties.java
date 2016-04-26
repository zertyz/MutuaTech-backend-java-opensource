package mutua.icc.instrumentation;

import java.lang.reflect.Method;

import mutua.serialization.SerializationRepository;
import mutua.serialization.SerializationRepository.EfficientTextualSerializationMethod;

/** <pre>
 * InstrumentationProperties.java
 * ==============================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines the available properties to participate on instrumentation events
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public enum InstrumentationProperties implements InstrumentableProperty {
	
	
	DAY_OF_WEEK("dayOfWeek", String.class),
	MAIL       ("mail",      TestType.class),
	
	ARBITRARY_TOSTRING_METHOD("My own toString of an Object[]") {
		// code based on 'AbstractPreparedProcedure#buildPreparedStatement'
		@EfficientTextualSerializationMethod
		public void toString(StringBuffer buffer) {
			Object[] myObjectArrayData = (Object[])(Object)this;
			buffer.append("My own toString worked!");
		}
	},

	
	;

	
	private String instrumentationPropertyName;
	private Class<?> instrumentationPropertyType;
	
	
	private InstrumentationProperties(String instrumentationPropertyName, Class<?> instrumentationPropertyType) {
		this.instrumentationPropertyName = instrumentationPropertyName;
		this.instrumentationPropertyType = instrumentationPropertyType;
	}
	
	private InstrumentationProperties(String instrumentationPropertyName) {
		this.instrumentationPropertyName = instrumentationPropertyName;
		this.instrumentationPropertyType = this.getClass();
	}

	
	// IInstrumentableProperty implementation
	/////////////////////////////////////////
	
	@Override
	public String getInstrumentationPropertyName() {
		return instrumentationPropertyName;
	}

	@Override
	public Class<?> getInstrumentationPropertyType() {
		return instrumentationPropertyType;
	}

	@Override
	public Method getTextualSerializationMethod() {
		return SerializationRepository.getSerializationMethod(instrumentationPropertyType);
	}
}