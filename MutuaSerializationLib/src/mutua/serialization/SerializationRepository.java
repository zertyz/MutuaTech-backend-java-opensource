package mutua.serialization;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** <pre>
 * SerializationRepository.java
 * ============================
 * (created by luiz, Jan 24, 2015)
 *
 * High efficiency Serialization / Deserialization class.
 *
 * @version $Id$
 * @author luiz
 */

// TODO 14/4/16: Refactoring de Serialization. Passos:
//1) Apenas provê métodos para serializar/deserializar -- todos estáticos
//2) Há um método concentrador que recebe um Object. Se for de um tipo conhecido, chama um dos métodos existentes. Se for de um tipo desconhecido, tentar chamar o toString(StringBuffer) e depois o toString(), alertando que este método é ineficiente
//3) Há um outro chamado getSerializationMethod, que recebe um Object e retorna um Method pronto para receber como argumentos: 1) o Object e 2) O String Buffer. O método para serializar eficientemente deve, então, se dar através da chamada deste method -- feita através de mais um: callSerializationMethod(Method, Object, StringBuffer)

public class SerializationRepository {
	
	/** Annotation to be used by 'public void toString(StringBuffer buffer)' methods to document and denote they follow the {@link SerializationRepository} requisites for such a method */
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface EfficientTextualSerializationMethod {}
	
	private static final Pattern stringSerializationPattern   = Pattern.compile("([\\\\\n\r\t])");
	private static final Pattern stringDeserializationPattern = Pattern.compile("(\\\\[\\\\nrt])");
	
	/** Efficiently serializes a string, so that it can be used on text files (and later reverted by {@link #deserialize(String)}) */
	public static StringBuffer serialize(StringBuffer buffer, String subject) {
		if (subject == null) {
			buffer.append("NULL");
			return buffer;
		}
		Matcher m = stringSerializationPattern.matcher(subject);
		while (m.find()) {
			char toEscape = m.group(1).charAt(0);
			switch (toEscape) {
			case '\\':
				m.appendReplacement(buffer, "\\\\\\\\");
				break;
			case '\n':
				m.appendReplacement(buffer, "\\\\n");
				break;
			case '\r':
				m.appendReplacement(buffer, "\\\\r");
				break;
			case '\t':
				m.appendReplacement(buffer, "\\\\t");
				break;
			default:
				m.appendReplacement(buffer, Matcher.quoteReplacement(String.valueOf(toEscape)));
			}
		}
		m.appendTail(buffer);
		
		return buffer;
	}
	
	/** Efficiently serializes an int array */
	public static StringBuffer serialize(StringBuffer buffer, int[] intArray) {
		if (intArray == null) {
			buffer.append("NULL");
			return buffer;
		}
		buffer.append('{');
		for (int i=0; i<intArray.length; i++) {
			if (i > 0) {
				buffer.append(',');
			}
			buffer.append(intArray[i]);
		}
		buffer.append('}');
		return buffer;
	}
	
	/** Efficiently serializes a string array */
	public static StringBuffer serialize(StringBuffer buffer, String[] stringArray) {
		if (stringArray == null) {
			buffer.append("NULL");
			return buffer;
		}
		buffer.append('{');
		for (int i=0; i<stringArray.length; i++) {
			if (i > 0) {
				buffer.append(',');
			}
			buffer.append('"');
			serialize(buffer, stringArray[i]);
			buffer.append('"');
		}
		buffer.append('}');
		return buffer;
	}
	
	/** Efficiently serializes a 2D string array */
	public static StringBuffer serialize(StringBuffer buffer, String[][] string2DArray) {
		if (string2DArray == null) {
			buffer.append("NULL");
			return buffer;
		}
		buffer.append('{');
		for (int i=0; i<string2DArray.length; i++) {
			if (i > 0) {
				buffer.append(", ");
			}
			serialize(buffer, string2DArray[i]);
		}
		buffer.append('}');
		return buffer;
	}
	
	/** Efficiently serializes a <String, String> Map in JSON style*/
	public static StringBuffer serialize(StringBuffer buffer, Map<String, String> map) {
		if (map == null) {
			buffer.append("NULL");
			return buffer;
		}
		buffer.append('{');
		for (String key : map.keySet()) {
			buffer.append('\'');
			serialize(buffer, key);
			buffer.append("': ");
			buffer.append('\'');
			serialize(buffer, map.get(key));
			buffer.append("',");
		}
		if (map.size() > 0) {
			buffer.setLength(buffer.length()-1);	// remove the last comma
		}
		buffer.append('}');
		return buffer;
	}
	
	/** Efficiently serializes an array of objects */
	public static StringBuffer serialize(StringBuffer buffer, Method serializationMethod, Object[] objects) {
		if (objects == null) {
			buffer.append("NULL");
			return buffer;
		}
		try {
			buffer.append('{');
			for (int i=0; i<objects.length; i++) {
				if (i > 0) {
					buffer.append(',');
				}
				buffer.append('"');
				invokeSerializationMethod(serializationMethod, buffer, objects[i]);
				buffer.append('"');
			}
			buffer.append('}');
			return buffer;
		} catch (Throwable t) {
			throw new RuntimeException("Exception while serializing an object array", t);
		}
	}
	

	
	/** Inefficiently serializes an 'object' into a StringBuffer -- for an efficient serialization, use {@link #getSerializationMethod} or some of the 'serialize(<NativeType>)' methods */
	public static StringBuffer serialize(StringBuffer buffer, Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<?> type = object.getClass();
		if (type == String.class) {
			serialize(buffer, (String)object);
		} else if (type == String[].class) {
			serialize(buffer, (String[])object);
		} else if (type == String[][].class) {
			serialize(buffer, (String[][])object);
		} else if (type == int[].class) {
			serialize(buffer, (int[])object);
		} else if ((type == Integer.TYPE)       || (type == Long.TYPE) ||
		           (type == Number.class)       || (type == Boolean.class) ||
		           (type == StringBuffer.class) || (type == Enum.class)) {
			buffer.append(object);
		} else {
			// serializes a general purpose java object
			Method method = getSerializationMethod(type);
			invokeSerializationMethod(method, buffer, object);
		}
		
		return buffer;
	}
	
	/** Inefficiently serializes an array of objects into a StringBuffer -- for an efficient serialization, use {@link #serialize(StringBuffer, Method, Object[])} */
	public static StringBuffer serialize(StringBuffer buffer, Object[] objects) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (objects == null) {
			buffer.append("NULL");
			return buffer;
		}
		buffer.append('{');
		for (int i=0; i<objects.length; i++) {
			if (i > 0) {
				buffer.append(',');
			}
			buffer.append('"');
			serialize(buffer, objects[i]);
			buffer.append('"');
		}
		buffer.append('}');
		return buffer;
	}
	
	/** Inefficiently serializes an 'object' into a String -- for an efficient serialization, use {@link #getSerializationMethod} or some of the 'serialize(<NativeType>)' methods */
	public static String serialize(Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return serialize(new StringBuffer(), object).toString();
	}
		
	public static Method getSerializationMethod(Class<?> type) throws SecurityException {
		Method method;
		
		// find the best efficient textual serialization method
		// either 'public void toString(StringBuffer buffer);' or 'public static void toString(Object this, StringBuffer buffer);'
		try {
			method = type.getMethod("toString", StringBuffer.class);
		} catch (NoSuchMethodException t1) {
			try {
				method = type.getMethod("toString", Object.class, StringBuffer.class);
			} catch (NoSuchMethodException t2) {
				// no special serialization method -- meaning the default 'toString()' will be called
				return null;
			}
		}
		
		method.setAccessible(true);
		return method;
	}
	
	public static void invokeSerializationMethod(Method m, final StringBuffer buffer, Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (object == null) {
			buffer.append("NULL");
		} else if (m != null) try {
			// call the pumped-up public void toString(StringBuffer buffer)
			m.invoke(object, buffer);
		} catch (IllegalArgumentException e) {
			// try the static version instead -- to allow custom serialization --- for instance, of an Object[][] structure
			m.invoke(null, object, buffer);
		} else {
			// call specific methods for native types
			if (object instanceof String) {
				serialize(buffer, (String)object);
			} else if (object instanceof String[]) {
				serialize(buffer, (String[])object);
			} else if (object instanceof Throwable) {
				PrintWriter pw = new PrintWriter(new Writer() {
					@Override
					public void write(char[] cbuf, int off, int len) throws IOException {
						serialize(buffer, new String(cbuf, off, len));
					}
					@Override
					public void flush() throws IOException {}
					@Override
					public void close() throws IOException {}
					
				});
				buffer.append('"');
				((Throwable)object).printStackTrace(pw);
				buffer.append('"');
			} else if (object instanceof Class) {
				buffer.append('\'').append(((Class<?>)object).getName()).append('\'');
			} else {
				// call the original 'public String toString()'
				buffer.append(object);
			}
		}
	}

	/** Efficiently deserializes an int array read from a human-readable string (or produced by {@link #serialize(StringBuffer, int[])}) */
	public static int[] deserializeIntArray(String serializedIntArray) {
		if (serializedIntArray.length() <= 2) {
			return new int[0];
		}
		String[] serializedElements = serializedIntArray.substring(1, serializedIntArray.length()-1).split(",");
		int[] intArray = new int[serializedElements.length];
		for (int i=0; i<intArray.length; i++) {
			intArray[i] = Integer.parseInt(serializedElements[i]);
		}
		return intArray;
	}

	/** Efficiently deserializes a string read from a human-readable text file (or produced by {@link #serialize(StringBuffer, String)}) */
	public static String deserialize(String serializedSubject) {
		StringBuffer buffer = new StringBuffer(serializedSubject.length());
		Matcher m = stringDeserializationPattern.matcher(serializedSubject);
		while (m.find()) {
			char toUnescape = m.group(1).charAt(1);
			switch (toUnescape) {
			case '\\':
				m.appendReplacement(buffer, "\\\\");
				break;
			case 'n':
				m.appendReplacement(buffer, "\n");
				break;
			case 'r':
				m.appendReplacement(buffer, "\r");
				break;
			case 't':
				m.appendReplacement(buffer, "\t");
				break;
			default:
				m.appendReplacement(buffer, String.valueOf(toUnescape));
			}
		}
		m.appendTail(buffer);
		return buffer.toString();
	}

}
