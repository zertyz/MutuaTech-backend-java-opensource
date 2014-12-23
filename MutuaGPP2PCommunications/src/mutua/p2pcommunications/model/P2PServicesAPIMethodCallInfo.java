package mutua.p2pcommunications.model;

import java.lang.reflect.Method;

/** <pre>
 * P2PServicesAPIMethodCallInfo.java
 * =================================
 * (created by luiz, Dec 22, 2014)
 *
 * Represents a P2P Services API method call that can be issued later, in a
 * flexible manner -- that is, by the framework, who doesn't know, a priori,
 * the API.
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class P2PServicesAPIMethodCallInfo {
	
	private final Method method;
	private final Object[] parameters;
	
	public P2PServicesAPIMethodCallInfo(Method method, Object[] parameters) {
		this.method     = method;
		this.parameters = parameters;
	}
	
	public Method getMethod() {
		return method;
	}
	public Object[] getParameters() {
		return parameters;
	}

}
