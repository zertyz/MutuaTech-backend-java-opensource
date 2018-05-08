package adapters.exceptions;

/** <pre>
 * JDBCAdapterException.java  --  $Id: JDBCHelperException.java,v 1.1 2010/07/01 22:02:14 luiz Exp $
 * ========================
 * (created by luiz, Dec 18, 2008)
 *
 * Top of all 'JDBCAdapter' exceptions, including 'SQLException' intended to reduce the number
 * of exceptions that one has to catch in order to the 'JDBCAdapter' class 
 */

public class JDBCAdapterException extends PreparedProcedureException {
	
	private static final long serialVersionUID = 1L;

	public JDBCAdapterException(String message) {
		super(message);
	}
	
}


