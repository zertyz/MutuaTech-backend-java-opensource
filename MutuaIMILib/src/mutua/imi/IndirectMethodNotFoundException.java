package mutua.imi;

import java.util.Arrays;

/** <pre>
 * IndirectMethodNotFound.java
 * ===========================
 * (created by luiz, Jan 23, 2015)
 *
 * Represents a failed invocation, where the represented method by the
 * 'IndirectMethodInvocationInfo' was not found by the 'IndirectMethodInvoker'
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class IndirectMethodNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private static String getText(IndirectMethodInvoker<?> indirectMethodInvoker, IndirectMethodInvocationInfo<?> invocationInfo) {
		return "Method not found while attempting to call '" + invocationInfo.getMethodId() + "("+Arrays.deepToString(invocationInfo.getParameters())+")', in the context of " + indirectMethodInvoker;
	}

	public IndirectMethodNotFoundException(IndirectMethodInvoker<?> indirectMethodInvoker, IndirectMethodInvocationInfo<?> invocationInfo) {
		super(getText(indirectMethodInvoker, invocationInfo));
	}

	public IndirectMethodNotFoundException(IndirectMethodInvoker<?> indirectMethodInvoker, IndirectMethodInvocationInfo<?> invocationInfo, Exception cause) {
		super(getText(indirectMethodInvoker, invocationInfo), cause);
	}
	
	public IndirectMethodNotFoundException(Exception cause) {
		super(cause);
	}

}
