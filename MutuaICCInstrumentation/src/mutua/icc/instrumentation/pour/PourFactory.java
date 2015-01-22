package mutua.icc.instrumentation.pour;

/** <pre>
 * PourFactory.java
 * ================
 * (created by luiz, Jan 21, 2015)
 *
 * Select among pours of 'EInstrumentationDataPours'
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class PourFactory {
	
	public enum EInstrumentationDataPours {
		RAM,
		CONSOLE,
		DATABASE,
		ROLLING_FILE,
		COMPRESSED_ROLLING_FILE,
		NETWORK,
	};
	
	// configurable values
	public static EInstrumentationDataPours DEFAULT_POUR = EInstrumentationDataPours.CONSOLE;
	
	private static final PourFactory[] instances;
	
	static {
		// build the multiton instances
		instances = new PourFactory[EInstrumentationDataPours.values().length];
		instances[EInstrumentationDataPours.RAM.ordinal()]     = new PourFactory(EInstrumentationDataPours.RAM);
		instances[EInstrumentationDataPours.CONSOLE.ordinal()] = new PourFactory(EInstrumentationDataPours.CONSOLE);
	}
	
	private IInstrumentationPour ip;
	
	
	private PourFactory(EInstrumentationDataPours pour) {
		
		switch (pour) {
			case RAM:
				ip = new mutua.icc.instrumentation.pour.ram.InstrumentationPour();
				break;
			case CONSOLE:
				ip = new mutua.icc.instrumentation.pour.console.InstrumentationPour();
				break;
			case DATABASE:
			case ROLLING_FILE:
			case COMPRESSED_ROLLING_FILE:
			case NETWORK:
			default:
				throw new RuntimeException("Don't now how to build an instance of " + pour.name());
		}
		
	}

	public static IInstrumentationPour getInstrumentationPour() {
		return instances[EInstrumentationDataPours.CONSOLE.ordinal()].ip;
	}
	
}
