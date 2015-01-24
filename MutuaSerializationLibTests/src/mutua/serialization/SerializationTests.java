package mutua.serialization;

import java.util.Enumeration;

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

	@Test
	public void serializationTest() {
		
		SerializationRepository serializer = new SerializationRepository(SerializationRules.class);
		
		TestType email = new TestType("from me", "to you");
		
		StringBuffer buffer = new StringBuffer();
		
		serializer.serialize(buffer, "This the attempt number ");
		serializer.serialize(buffer, 1);
		serializer.serialize(buffer, new StringBuffer(" and the results are: "));
		serializer.serialize(buffer, email);
		
		System.out.println(buffer.toString());
		
	}

}
