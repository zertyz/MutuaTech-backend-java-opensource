package mutua.icc.instrumentation;

import mutua.serialization.SerializationRepository.EfficientTextualSerializationMethod;

/** <pre>
 * TestType.java
 * =============
 * (created by luiz, Jan 21, 2015)
 *
 * Simple type to test instrumenting custom information without coupling to the instrumentation facility
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class TestType {
	
	public String from;
	public String to;

	public TestType(String from, String to) {
		this.from = from;
		this.to   = to;
	}
	
	@EfficientTextualSerializationMethod
	public void toString(StringBuffer buffer) {
		buffer.append("from='").append(from).append("', to='").append(to).append('\'');
	}
}
