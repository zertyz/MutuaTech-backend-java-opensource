package mutua.serialization;

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

	// TODO use a hashmap for performance
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
			addSerializationRule((ISerializationRule<?>)rule);
		}
	}
	
	public SerializationRepository(ISerializationRule<?>[] serializationRules) {
		typeToSerializationRuleMap = new Hashtable<Class<?>, ISerializationRule<?>>();
		addSerializationRules(serializationRules);
	}
	
	public void addSerializationRules(ISerializationRule<?>[] serializationRules) {
		for (ISerializationRule<?> serializationRule : serializationRules) {
			addSerializationRule(serializationRule);
		}
	}
	
	public void addSerializationRule(ISerializationRule<?> serializationRule) {
		Class<?> serializationType = serializationRule.getType();
		typeToSerializationRuleMap.put(serializationType, serializationRule);
	}

	private static String[][] stringEscapeSequences = {
		{"\\\\", "\\\\\\\\"},
		{"\n",   "\\\\n"},
		{"\r",   "\\\\r"},
		{"\t",   "\\\\t"},
	};
	public void serialize(StringBuffer buffer, Object instance) {
		if (instance == null) {
			buffer.append("null");
		} else {
			Class<?> instanceType = instance.getClass();
			serialize(buffer, instance, instanceType);
		}
	}

	private void serialize(StringBuffer buffer, Object instance, Class<?> instanceType) {
		if (instanceType == String.class) {
			String s = (String)instance;
			for (int i=0; i<stringEscapeSequences.length; i++) {
				s = s.replaceAll(stringEscapeSequences[i][0], stringEscapeSequences[i][1]);
			}
			buffer.append(s);
		} else if (instanceType == String[].class) {
			String[] ss = (String[])instance;
			for (int i=0; i<ss.length; i++) {
				buffer.append('"');
				serialize(buffer, ss[i], String.class);
				buffer.append('"');
				if (i < ss.length-1) {
					buffer.append(',');
				}
			}
		} else if ((instanceType == Integer.TYPE) || (instanceType == Long.TYPE) ||
		           (instanceType == Number.class) || (instanceType == Boolean.class) ||
		           (instanceType == StringBuffer.class) || (instanceType == Enum.class)) {
			buffer.append(instance);
		} else {
			ISerializationRule serializationRule = typeToSerializationRuleMap.get(instanceType);
			if (serializationRule == null) {
				// try a super type
				Class<?> superType = instanceType.getSuperclass();
				if (superType == Object.class) {
					throw new RuntimeException("No serialization rule found to serialize type '" + instanceType + "'");
				} else {
					serialize(buffer, instance, superType);
				}
			} else {
				serializationRule.appendSerializedValue(buffer, instance);
			}
		}
	}

}
