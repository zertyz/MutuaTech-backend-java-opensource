package adapters.exceptions;

/** <pre>
 * JDBCAdapterError.java  --  $Id: JDBCHelperError.java,v 1.1 2010/07/01 22:02:14 luiz Exp $
 * =====================
 * (created by luiz, Dec 15, 2008)
 *
 * Inform of MySQL conversation errors
 */

public class JDBCAdapterError extends Error {

	private static final long serialVersionUID = 1L;

	public JDBCAdapterError(String message, Throwable cause) {
		super(message, cause);
	}

}


