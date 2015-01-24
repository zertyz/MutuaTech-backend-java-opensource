package mutua.icc.instrumentation;


/** <pre>
 * InstrumentableEvent.java
 * ========================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines an instrumentable event
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class InstrumentableEvent implements IInstrumentableEvent {
	
	private final String name;
	private final IInstrumentableProperty[] properties;
	
	public InstrumentableEvent(String name, IInstrumentableProperty property) {
		this.name       = name;
		this.properties = new IInstrumentableProperty[] {property};
	}

	public InstrumentableEvent(String name, IInstrumentableProperty property1, IInstrumentableProperty property2) {
		this.name       = name;
		this.properties = new IInstrumentableProperty[] {property1, property2};
	}

	public InstrumentableEvent(String name) {
		this.name       = name;
		this.properties = new IInstrumentableProperty[] {};
	}

	/** The name of the event */
	public String getName() {
		return name;
	}
	
	/** Properties that are related to this event */
	public IInstrumentableProperty[] getProperties() {
		return properties;
	}

	@Override
	public InstrumentableEvent getInstrumentableEvent() {
		return this;
	}
	
}
