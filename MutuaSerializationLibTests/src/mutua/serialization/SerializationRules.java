package mutua.serialization;

/** <pre>
 * ESerializationRules.java
 * ========================
 * (created by luiz, Jan 24, 2015)
 *
 * Enumeration that concentrates all project serialization rules
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public enum SerializationRules implements ISerializationRule {

	NAME(String.class),
	
	MAIL(TestType.class) {
		@Override
		public void appendSerializedValue(StringBuffer buffer, Object value) {
			TestType email = (TestType)value;
			buffer.append("from='").append(email.from).append("',").
			append("to='").append(email.to).append("'");
		}
	},
		
	;
	
	private Class<?> type;
	
	private SerializationRules(Class<?> type) {
		this.type = type;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public void appendSerializedValue(StringBuffer buffer, Object value) {
		throw new RuntimeException("Serialization Rule '" + this.getClass().getName() +
		                            "' didn't overrode 'appendSerializedValue' from " +
		                            "'ISerializationRule' for type '" + type);
	}

}
