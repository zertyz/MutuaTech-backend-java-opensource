package mutua.icc.instrumentation;

import java.lang.reflect.Method;

/** <pre>
 * DefaultInstrumentationProperties.java
 * =====================================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines common instrumentation properties that are available to participate on instrumentation events
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public enum DefaultInstrumentationProperties implements InstrumentableProperty {

	
	DIP_MSG      ("msg",        String.class),
	DIP_THROWABLE("stackTrace", Throwable.class),
	
	
	;

	
	private String instrumentationPropertyName;
	private Class<?> instrumentationPropertyType;
	
	
	private DefaultInstrumentationProperties(String instrumentationPropertyName, Class<?> instrumentationPropertyType) {
		this.instrumentationPropertyName = instrumentationPropertyName;
		this.instrumentationPropertyType = instrumentationPropertyType;
	}

	
	// IInstrumentableProperty implementation
	/////////////////////////////////////////
	
	@Override
	public String getInstrumentationPropertyName() {
		return instrumentationPropertyName;
	}

	
	// ISerializationRule implementation
	////////////////////////////////////
	
	@Override
	public Class<?> getInstrumentationPropertyType() {
		return instrumentationPropertyType;
	}

	@Override
	public Method getTextualSerializationMethod() {
		return null;
	}

}
