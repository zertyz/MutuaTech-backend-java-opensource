package adapters;

/** <pre>
 * IJDBCAdapterParameterDefinition.java
 * ====================================
 * (created by luiz, Jan 20, 2016)
 *
 * Interface used by enums meant to define tables & query parameters on '*AdapterConfiguration' classes implementing the
 * "JDBC Adapter Configuration" pattern. 
 *
 * @version $Id$
 * @author luiz
*/

public interface IJDBCAdapterParameterDefinition {

	/** method to return the name of this parameter -- the parameter name may not be confused with the table field name: the parameters
	 *  must be used where you'd like a value to be placed; if you want to refer to the name of the field, then hard code it as a string. 
	 *  This method's implementation should be "return name()" */
	String getParameterName();

}
