package mutua.icc.instrumentation.pour;

import java.io.FileNotFoundException;
import java.util.Hashtable;

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
		ROTATING_FILE,
		COMPRESSED_ROTATING_FILE,
		NETWORK,
	};
	
	// the multiton instances
	private static final Hashtable<String, PourFactory> instances = new Hashtable<String, PourFactory>();

	
	private IInstrumentationPour ip;
	
	
	/** Creates a set of pours with the following 'descriptorReference' (a file name, a network connection, etc.) */
	private PourFactory(EInstrumentationDataPours pourType, IInstrumentableProperty[] instrumentationProperties, String descriptorReference) {
		
		switch (pourType) {
			case POSTGRESQL_DATABASE:
			case NETWORK:
			case COMPRESSED_ROTATING_FILE:
			case ROTATING_FILE:
				try {
					ip = new mutua.icc.instrumentation.pour.rotatingfile.InstrumentationPour(instrumentationProperties, descriptorReference);
					break;
				} catch (FileNotFoundException e) {
					System.out.println("mutua.Instrumentation: Error creating a ROTATING_FILE log pour for file '"+descriptorReference+"'. Falling back to 'CONSOLE'");
					e.printStackTrace();
				}
			case CONSOLE:
				ip = new mutua.icc.instrumentation.pour.console.InstrumentationPour(instrumentationProperties);
				break;
			case RAM:
				ip = new mutua.icc.instrumentation.pour.ram.InstrumentationPour();
				break;
			default:
				throw new RuntimeException("Don't now how to build an instance of " + pourType.name());
		}
		
	}

	/** Gets the instrumentation pours with the following 'descriptorReference' (a file name, a network connection, etc.) */
	public static IInstrumentationPour getInstrumentationPour(EInstrumentationDataPours pourType, String descriptorReference, IInstrumentableProperty[] instrumentationProperties) {
		String key = pourType.name() + ((descriptorReference!=null)?descriptorReference:"");
		if (!instances.containsKey(key)) {
			instances.put(key, new PourFactory(pourType, instrumentationProperties, descriptorReference));
		}
		return instances.get(key).ip;
	}

}
