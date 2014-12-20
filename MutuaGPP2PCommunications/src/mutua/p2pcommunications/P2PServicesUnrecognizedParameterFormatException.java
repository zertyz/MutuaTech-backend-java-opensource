package mutua.p2pcommunications;

/** <pre>
 * P2PServicesUnrecognizedParameterFormatException.java
 * ====================================================
 * (created by luiz, Dec 20, 2014)
 *
 * Signals a parameter was not recognized as valid by a 'P2PServiceAPI'
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class P2PServicesUnrecognizedParameterFormatException extends Exception {

	private static final long serialVersionUID = 5521991234899L;
	
	public P2PServicesUnrecognizedParameterFormatException(String message) {
		super(message);
	}

}
