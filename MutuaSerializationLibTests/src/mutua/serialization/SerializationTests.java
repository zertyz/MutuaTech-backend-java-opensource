package mutua.serialization;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/** <pre>
 * SerializationTests.java
 * =======================
 * (created by luiz, Jan 24, 2015)
 *
 * Tests the serialization library
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class SerializationTests {
	
	public enum ESomeNumbers {ONE, TWO, THREE};

	@Test
	public void serializationTest() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		TestType email = new TestType("from me", "to you");
		
		StringBuffer buffer = new StringBuffer();
		
		SerializationRepository.serialize(buffer, "This the attempt number ");
		SerializationRepository.serialize(buffer, 1);
		SerializationRepository.serialize(buffer, new StringBuffer(" and the results are: "));
		SerializationRepository.serialize(buffer, email);
		SerializationRepository.serialize(buffer, ESomeNumbers.THREE);
		
		System.out.println(buffer.toString());
		
	}
	
	@Test
	public void stringSerializationBackAndForthTest() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String originalString   = "The quick\nbrown\rfox\"jumps'over\tthe\\lazy§dog!";
		String serializedString = "The quick\\nbrown\\rfox\"jumps'over\\tthe\\\\lazy§dog!";
		
		// several string serialization strategies
		assertEquals("'String serialize(Object)' didn't work", serializedString, SerializationRepository.serialize((Object)originalString));
		assertEquals("'StringBuffer serialize(StringBuffer, Object)' didn't work", serializedString, SerializationRepository.serialize(new StringBuffer(), (Object)originalString).toString());
		assertEquals("'StringBuffer serialize(StringBuffer, String)' didn't work", serializedString, SerializationRepository.serialize(new StringBuffer(), originalString).toString());
		Method m = SerializationRepository.getSerializationMethod(String.class);
		StringBuffer b = new StringBuffer();
		SerializationRepository.invokeSerializationMethod(m, b, originalString);
		assertEquals("'invokeSerializationMethod' didn't work", serializedString, b.toString());
		
		// back and forth
		assertEquals("Serializing   of a nice string didn't work", serializedString, SerializationRepository.serialize(originalString));
		assertEquals("Deserializing of a nice string didn't work", originalString,   SerializationRepository.deserialize(serializedString));
		
		// just a sanity check
		assertEquals("Something is wrong. I am not sane...", originalString, SerializationRepository.deserialize(SerializationRepository.serialize(originalString)));
	}
	
	/***********************
	** PERFORMANCE SPIKES **
	***********************/ 
	
	private static String replaceAllSerializationLoop(int count, String subject) {
		long start = System.currentTimeMillis();
		StringBuffer buffer = new StringBuffer(subject.length()*2);
		String result = null;
		for (int i=0; i<count; i++) {
			result = subject.replaceAll("\\\\", "\\\\\\\\").
	                         replaceAll("\n", "\\\\n").
	                         replaceAll("\r", "\\\\r").
	                         replaceAll("\t", "\\\\t");
			buffer.append(result);
			buffer.setLength(0);
		}
		return (System.currentTimeMillis() - start) + " ms ('"+result+"')";
	}
	
//	 group 1: \\; group 2: \n; group 3: \r; group 4: \t
//	 "(?:\\\\)|(?:\n)|(?:\r)|(?:\t)"
	static Pattern p = Pattern.compile("([\\\\\n\r\t])");
	private static String regexSerializationLoop(int count, String subject) {
		long start = System.currentTimeMillis();
		StringBuffer buffer = new StringBuffer(subject.length()*2);
		for (int i=0; i<count; i++) {
			buffer.setLength(0);
			Matcher m = p.matcher(subject);
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
		}
		return (System.currentTimeMillis() - start) + " ms ('"+buffer.toString()+"')";
	}
	
	private static String replaceSerializationLoop(int count, String subject) {
		long start = System.currentTimeMillis();
		StringBuffer buffer = new StringBuffer(subject.length()*2);
		String result = null;
		for (int i=0; i<count; i++) {
			result = subject.replace("\\", "\\\\").
	                         replace("\n", "\\n").
	                         replace("\r", "\\r").
	                         replace("\t", "\\t");
			buffer.append(result);
			buffer.setLength(0);
		}
		return (System.currentTimeMillis() - start) + " ms ('"+result+"')";
	}
	
	static Pattern dp = Pattern.compile("(\\\\[\\\\nrt])");
	private static String regexDeserializationLoop(int count, String serializedSubject) {
		long start = System.currentTimeMillis();
		StringBuffer buffer = new StringBuffer(serializedSubject.length());
		for (int i=0; i<count; i++) {
			buffer.setLength(0);
			Matcher m = dp.matcher(serializedSubject);
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
		}
		return (System.currentTimeMillis() - start) + " ms ('"+buffer.toString()+"')";
	}
	
	//@Test
	public void performanceTests() {
		int loopCount = 1000000;
		System.out.println("ReplaceAll Serialization Algorithm:");
		System.out.println("\t1) Single unescapable char string: " + replaceAllSerializationLoop(loopCount, "a")); 
		System.out.println("\t2) Single   escapable char string: " + replaceAllSerializationLoop(loopCount, "\n")); 
		System.out.println("\t3) Four chars unescapable string : " + replaceAllSerializationLoop(loopCount, "abcde")); 
		System.out.println("\t4) Four chars   escapable string : " + replaceAllSerializationLoop(loopCount, "\\\n\r\t"));
		System.out.println("\t5) SMS Game Help Phrasing String : " + replaceAllSerializationLoop(loopCount, "You can play the {{appName}} game in 2 ways: guessing someone's word or inviting someone to play with your word You'll get 1 lucky number each word you guess. Whenever you invite a friend or user to play, you win another lucky number Every week, 1 lucky number is selected to win the prize. Send an option to {{shortCode}}: (J)Play online; (C)Invite a friend or user; (R)anking; (A)Help"));
		System.out.println("Regex Serialization Algorithm:");
		System.out.println("\t1) Single unescapable char string: " + regexSerializationLoop(loopCount, "a")); 
		System.out.println("\t2) Single   escapable char string: " + regexSerializationLoop(loopCount, "\n")); 
		System.out.println("\t3) Four chars unescapable string : " + regexSerializationLoop(loopCount, "abcde")); 
		System.out.println("\t4) Four chars   escapable string : " + regexSerializationLoop(loopCount, "\\\n\r\t"));
		System.out.println("\t5) SMS Game Help Phrasing String : " + regexSerializationLoop(loopCount, "You can play the {{appName}} game in 2 ways: guessing someone's word or inviting someone to play with your word You'll get 1 lucky number each word you guess. Whenever you invite a friend or user to play, you win another lucky number Every week, 1 lucky number is selected to win the prize. Send an option to {{shortCode}}: (J)Play online; (C)Invite a friend or user; (R)anking; (A)Help"));
		System.out.println("Replace Serialization Algorithm:");
		System.out.println("\t1) Single unescapable char string: " + replaceSerializationLoop(loopCount, "a")); 
		System.out.println("\t2) Single   escapable char string: " + replaceSerializationLoop(loopCount, "\n")); 
		System.out.println("\t3) Four chars unescapable string : " + replaceSerializationLoop(loopCount, "abcde")); 
		System.out.println("\t4) Four chars   escapable string : " + replaceSerializationLoop(loopCount, "\\\n\r\t"));
		System.out.println("\t5) SMS Game Help Phrasing String : " + replaceSerializationLoop(loopCount, "You can play the {{appName}} game in 2 ways: guessing someone's word or inviting someone to play with your word You'll get 1 lucky number each word you guess. Whenever you invite a friend or user to play, you win another lucky number Every week, 1 lucky number is selected to win the prize. Send an option to {{shortCode}}: (J)Play online; (C)Invite a friend or user; (R)anking; (A)Help"));
		System.out.println("Regex Deserialization Algorithm:");
		System.out.println("\t1) Single unescapable char string: " + regexDeserializationLoop(loopCount, "a")); 
		System.out.println("\t2) Single   escapable char string: " + regexDeserializationLoop(loopCount, "\\n")); 
		System.out.println("\t3) Four chars unescapable string : " + regexDeserializationLoop(loopCount, "abcde")); 
		System.out.println("\t4) Four chars   escapable string : " + regexDeserializationLoop(loopCount, "\\\\\\n\\r\\t"));
		System.out.println("\t5) SMS Game Help Phrasing String : " + regexDeserializationLoop(loopCount, "You can play the {{appName}} game in 2 ways: guessing someone's word or inviting someone to play with your word You'll get 1 lucky number each word you guess. Whenever you invite a friend or user to play, you win another lucky number Every week, 1 lucky number is selected to win the prize. Send an option to {{shortCode}}: (J)Play online; (C)Invite a friend or user; (R)anking; (A)Help"));
	}

}