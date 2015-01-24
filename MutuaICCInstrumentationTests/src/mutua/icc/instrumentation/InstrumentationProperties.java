package mutua.icc.instrumentation;

import mutua.serialization.ISerializationRule;

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

public enum InstrumentationProperties implements IInstrumentableProperty {
	
	
	DAY_OF_WEEK("dayOfWeek", String.class),
	
	MAIL("mail", TestType.class) {
		@Override
		public void appendSerializedValue(StringBuffer logLine, Object value) {
			TestType mail = (TestType)value;
			logLine.append("from='").append(mail.from).append("',").
			append("to='").append(mail.to).append("'");
		}
	}

	
	;

	
	private String instrumentationPropertyName;
	private Class<?> instrumentationPropertyType;
	
	
	private InstrumentationProperties(String instrumentationPropertyName, Class<?> instrumentationPropertyType) {
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