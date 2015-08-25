package mutua.imi;

import java.util.Arrays;

/** <pre>
 * IndirectMethodInvocationInfo.java
 * =================================
 * (created by luiz, Jan 23, 2015)
 *
 * Represents a method call, able to be executed and serialized / deserialized
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class IndirectMethodInvocationInfo<METHOD_ID_TYPE> {

	private final METHOD_ID_TYPE methodId;
	private final Object[] parameters;
	
	public IndirectMethodInvocationInfo(METHOD_ID_TYPE methodId, Object... parameters) {
		this.methodId  = methodId;
		this.parameters = parameters;
	}
	
	public METHOD_ID_TYPE getMethodId() {
		return methodId;
	}
	
	public Object[] getParameters() {
		return parameters;
	}
	
	@Override
	public String toString() {
		return methodId.toString()+Arrays.toString(parameters);
	}
}
