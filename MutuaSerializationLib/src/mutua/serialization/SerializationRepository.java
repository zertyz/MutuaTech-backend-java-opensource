package mutua.serialization;

import java.util.ArrayList;
import java.util.Hashtable;

/** <pre>
 * SerializationRepository.java
 * ============================
 * (created by luiz, Jan 24, 2015)
 *
 * Aglomerates serialization rules and perform serializations on them
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class SerializationRepository {

	private Hashtable<Class<?>, ISerializationRule<?>> typeToSerializationRuleMap;
	
	public SerializationRepository(Class<?>... serializationRulesEnumerations) {
		typeToSerializationRuleMap = new Hashtable<Class<?>, ISerializationRule<?>>();
		for (Class<?> serializationRulesEnumeration : serializationRulesEnumerations) {
			addSerializationRules(serializationRulesEnumeration);
		}
	}

	public void addSerializationRules(Class<?> serializationRulesEnumeration) {
		Object[] rules = serializationRulesEnumeration.getEnumConstants();
		for (Object rule : rules) {
			ISerializationRule<?> serializationRule = (ISerializationRule<?>)rule;
			Class<?> serializationType = serializationRule.getType();
			typeToSerializationRuleMap.put(serializationType, serializationRule);
		}
	}

	private static String[][] stringEscapeSequences = {
		{"\n", "\\\\n"},
		{"\r", "\\\\r"},
		{"\t", "\\\\t"},
	};
	public void serialize(StringBuffer buffer, Object instance) {
		Class<?> instanceType = instance.getClass();
		if (instanceType == String.class) {
			String s = (String)instance;
			for (int i=0; i<stringEscapeSequences.length; i++) {
				s = s.replaceAll(stringEscapeSequences[i][0], stringEscapeSequences[i][1]);
			}
			buffer.append(s);
		} else if ((instanceType == Integer.class) || (instanceType == StringBuffer.class)) {
			buffer.append(instance);
		} else {
			ISerializationRule serializationRule = typeToSerializationRuleMap.get(instanceType);
			if (serializationRule == null) {
				throw new RuntimeException("No serialization rule found to serialize type '" + instanceType + "'");
			} else {
				serializationRule.appendSerializedValue(buffer, instance);
			}
		}
	}
	
	

}
