package mutua.icc.instrumentation;

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

	
	// ISerializationRule implementation
	////////////////////////////////////
	
	@Override
	public Class<?> getType() {
		return instrumentationPropertyType;
	}

	@Override
	public void appendSerializedValue(StringBuffer buffer, Object value) {
		throw new RuntimeException("Serialization Rule '" + this.getClass().getName() +
                                   "' didn't overrode 'appendSerializedValue' from " +
                                   "'ISerializationRule' for type '" + instrumentationPropertyType);
	}

}
