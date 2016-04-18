package mutua.icc.instrumentation;

import java.lang.reflect.Method;

import mutua.serialization.SerializationRepository;

/** <pre>
 * MutuaEventAdditionalEventLinksInstrumentationProperties.java
 * ============================================================
 * (created by luiz, Feb 1, 2015)
 *
 * Defines the available properties that can participate on instrumentation events
 *
 * @see MutuaEventAdditionalEventLinksInstrumentationEvents
 * @version $Id$
 * @author luiz
 */

public enum MutuaEventAdditionalEventLinksInstrumentationProperties implements IInstrumentableProperty {

	
	// thread management
	////////////////////
	
	IP_QUEUE_TABLE_NAME ("queueTableName", String.class),

	
	// errors
	/////////
	
	
		
	;

	
	private String instrumentationPropertyName;
	private Class<?> instrumentationPropertyType;
	
	
	private MutuaEventAdditionalEventLinksInstrumentationProperties(String instrumentationPropertyName, Class<?> instrumentationPropertyType) {
		this.instrumentationPropertyName = instrumentationPropertyName;
		this.instrumentationPropertyType = instrumentationPropertyType;
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
