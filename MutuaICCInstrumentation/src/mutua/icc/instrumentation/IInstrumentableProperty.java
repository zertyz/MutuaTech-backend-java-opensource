package mutua.icc.instrumentation;

/** <pre>
 * IInstrumentableProperty.java
 * ============================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines an instrumentable property, which provides data on a specific format
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class IInstrumentableProperty<T> {
	
	private final String name;
	private final Class<?> type;
	
	public IInstrumentableProperty(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}
	
	/** The name of the property */
	public String getName() {
		return name;
	}
	
	/** The value data type of the property */
	public Class<?> getType() {
		return type;
	}

	/** This class should be overwritten for custom property types */
	public void appendValueToLogLine(StringBuffer logLine, T value) {
		throw new RuntimeException("Don't know how to log event property '"+name+"' of type '"+type+"'");
	}

}
