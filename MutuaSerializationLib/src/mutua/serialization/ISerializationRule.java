package mutua.serialization;

/** <pre>
 * ISerializationRule.java
 * =======================
 * (created by luiz, Jan 24, 2015)
 *
 * Interface that defines the contract of a serialization rule.
 * 
 * Mey be used on enumerations to implement a 'ESerializationRules' for a project:
 * public enum ESerializationRules implements ISerializationRule...
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public interface ISerializationRule<T> {
	
	/** Returns the value data type of the property */
	Class<T> getType();
	
	/** Responsible for appending to 'buffer' the serialized data that represents 'value'.
	 * Recommendation: use the following for the default implementation:
	 * throw new RuntimeException("Class " + this.getClass().getName() +
	 *                            " doesn't know how to serialize type '" + type +
	 *                            "' -- it needs to override the 'appendSerializedValue' " +
	 *                            "method of 'ISerializationRule'"); */
	void appendSerializedValue(StringBuffer buffer, T value);

}
