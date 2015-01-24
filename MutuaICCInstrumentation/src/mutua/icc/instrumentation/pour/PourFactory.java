package mutua.icc.instrumentation.pour;

import mutua.icc.instrumentation.IInstrumentableProperty;

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
	
	private static final PourFactory[] instances = new PourFactory[EInstrumentationDataPours.values().length];

	
	private IInstrumentationPour ip;
	
	
	private PourFactory(EInstrumentationDataPours pour, IInstrumentableProperty[] instrumentationProperties) {
		
		switch (pour) {
			case RAM:
				ip = new mutua.icc.instrumentation.pour.ram.InstrumentationPour();
				break;
			case CONSOLE:
				ip = new mutua.icc.instrumentation.pour.console.InstrumentationPour(instrumentationProperties);
				break;
			case DATABASE:
			case ROLLING_FILE:
			case COMPRESSED_ROLLING_FILE:
			case NETWORK:
			default:
				throw new RuntimeException("Don't now how to build an instance of " + pour.name());
		}
		
	}

	public static IInstrumentationPour getInstrumentationPour(EInstrumentationDataPours pour, IInstrumentableProperty[] instrumentationProperties) {
		if (instances[pour.ordinal()] != null) {
			return instances[pour.ordinal()].ip;
		} else {
			instances[pour.ordinal()] = new PourFactory(pour, instrumentationProperties);
			return getInstrumentationPour(pour, instrumentationProperties);
		}
	}

	public static IInstrumentationPour getInstrumentationPour(IInstrumentableProperty[] instrumentationProperties) {
		return getInstrumentationPour(DEFAULT_POUR, instrumentationProperties);
	}
	
}
