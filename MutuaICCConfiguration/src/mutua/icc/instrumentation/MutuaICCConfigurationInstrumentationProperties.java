package mutua.icc.instrumentation;

import java.lang.reflect.Method;

import mutua.serialization.SerializationRepository;

/** <pre>
 * MutuaICCConfigurationInstrumentationProperties.java
 * ===================================================
 * (created by luiz, Jan 29, 2015)
 *
 * Defines the available properties that can participate on instrumentation events
 *
 * @see MutuaICCConfigurationInstrumentationEvents
 * @version $Id$
 * @author luiz
 */

public enum MutuaICCConfigurationInstrumentationProperties implements InstrumentableProperty {

	
	// class loading
	////////////////
	
	IP_FILE_NAME ("file", String.class),
	IP_CLASS     ("class", Class.class),
	

	// field setting
	////////////////
	
	IP_CONFIGURATION_FIELD_NAME               ("fieldName", String.class),
	IP_CONFIGURATION_STRING_FIELD_VALUE       ("value",     String.class),
	IP_CONFIGURATION_NUMBER_FIELD_VALUE       ("value",     long.class),
	IP_CONFIGURATION_BOOLEAN_FIELD_VALUE      ("value",     Boolean.class),
	IP_CONFIGURATION_STRING_ARRAY_FIELD_VALUE ("values",    String[].class),
	IP_CONFIGURATION_ENUMERATION_FIELD_VALUE  ("value",     Enum.class),
	
	
	// errors
	/////////
	
	IP_ERROR_MSG ("errorMsg", String.class),
	
		
	;

	
	private String instrumentationPropertyName;
	private Class<?> instrumentationPropertyType;
	
	
	private MutuaICCConfigurationInstrumentationProperties(String instrumentationPropertyName, Class<?> instrumentationPropertyType) {
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
		return SerializationRepository.getSerializationMethod(instrumentationPropertyType);
	}

}
