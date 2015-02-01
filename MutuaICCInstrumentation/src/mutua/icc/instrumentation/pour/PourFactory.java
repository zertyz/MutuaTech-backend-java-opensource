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
		POSTGRESQL_DATABASE,
		ROLLING_FILE,
		COMPRESSED_ROLLING_FILE,
		NETWORK,
	};
	
	// configurable values
	public static EInstrumentationDataPours DEFAULT_POUR = EInstrumentationDataPours.CONSOLE;
	
	// the multiton instances
	private static final PourFactory[] instances = new PourFactory[EInstrumentationDataPours.values().length];

	
	private IInstrumentationPour ip;
	
	
	private PourFactory(EInstrumentationDataPours pourType, IInstrumentableProperty[] instrumentationProperties) {
		
		switch (pourType) {
			case RAM:
				ip = new mutua.icc.instrumentation.pour.ram.InstrumentationPour();
				break;
			case CONSOLE:
				ip = new mutua.icc.instrumentation.pour.console.InstrumentationPour(instrumentationProperties);
				break;
			case POSTGRESQL_DATABASE:
			case ROLLING_FILE:
			case COMPRESSED_ROLLING_FILE:
			case NETWORK:
			default:
				throw new RuntimeException("Don't now how to build an instance of " + pourType.name());
		}
		
	}

	public static IInstrumentationPour getInstrumentationPour(EInstrumentationDataPours pourType, IInstrumentableProperty[] instrumentationProperties) {
		if (instances[pourType.ordinal()] != null) {
			return instances[pourType.ordinal()].ip;
		} else {
			instances[pourType.ordinal()] = new PourFactory(pourType, instrumentationProperties);
			return getInstrumentationPour(pourType, instrumentationProperties);
		}
	}

	public static IInstrumentationPour getInstrumentationPour(IInstrumentableProperty[] instrumentationProperties) {
		return getInstrumentationPour(DEFAULT_POUR, instrumentationProperties);
	}
	
}
