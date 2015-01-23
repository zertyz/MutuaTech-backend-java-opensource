package mutua.imi;

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
	
	private static String getText(IndirectMethodInvoker<?> indirectMethodInvoker, Object methodId) {
		return "Method not found while attempting to call " + methodId + " on the context of " + indirectMethodInvoker;
	}

	public IndirectMethodNotFoundException(IndirectMethodInvoker<?> indirectMethodInvoker, Object methodId) {
		super(getText(indirectMethodInvoker, methodId));
	}

	public IndirectMethodNotFoundException(IndirectMethodInvoker<?> indirectMethodInvoker, Object methodId, Exception cause) {
		super(getText(indirectMethodInvoker, methodId), cause);
	}
	
	public IndirectMethodNotFoundException(Exception cause) {
		super(cause);
	}

}
