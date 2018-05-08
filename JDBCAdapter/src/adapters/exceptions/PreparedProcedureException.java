package adapters.exceptions;

import java.sql.SQLException;

/** <pre>
 * PreparedProcedureException.java  --  $Id: PreparedProcedureException.java,v 1.1 2010/07/01 22:02:14 luiz Exp $
 * ===============================
 * (created by luiz, Dec 15, 2008)
 *
 * Informs about misusage scenarios involving Prepared Procedures 
 */

public class PreparedProcedureException extends SQLException {

	private static final long serialVersionUID = 1L;

	public PreparedProcedureException(String message) {
		super(message);
	}
	
}


