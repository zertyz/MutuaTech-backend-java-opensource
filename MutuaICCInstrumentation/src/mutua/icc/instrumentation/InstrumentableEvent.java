package mutua.icc.instrumentation;

import mutua.icc.instrumentation.handlers.InstrumentationHandlerLogPrintStream;

/** <pre>
 * InstrumentableEvent.java
 * ========================
 * (created by luiz, Jan 24, 2015)
 *
 * This class defines an "Instrumentable Event", along with information on how it should behave
 * when the event hits 
 *
 * @version $Id$
 * @author luiz
 */

public class InstrumentableEvent {
	
	public enum ELogSeverity {
		/** For events that will produce log messages presenting details which are only useful for developers -- to improve the software */
		DEBUG,
		/** For events that will produce log messages that are similar to {@link #DEBUG}, but which information could be exploited by
		 *  reverse engineers to overcome the license and/or author rights protections and, therefore, should be either presented cryptically or not presented at all (on production) */
		CRYPT,
		/** For events that will produce log messages presenting additional information that helps the reproduction of the execution scenarios -- either in normal or error circumstances */
		INFO,
		/** For events that will produce log messages presenting minimal information needed to reproduce any {@link #ERROR} scenarios */
		CRITICAL,
		/** For events that will produce log messages corresponding to any thrown 'Errors', 'Exceptions' and 'Throwable' objects */
		ERROR
	}
	
	public enum EComputability {
		/** For events that, when happen, mean an increase of '1' on the amount computed by the reports generation facility */
		HIT,
		/** For events that, when happen, mean an increase of 'n' on the amount computed by the reports generation facility. 'n' is specified as an {@link InstrumentableProperty} on the constructor */
		INCREMENTAL_VALUE,
		/** For events that, when happen, mean setting to 'n' the amount computed by the reports generation facility. 'n' is specified as an {@link InstrumentableProperty} on the constructor */
		ABSOLUTE_VALUE,
		/** For events that, when happen, mean adding 'n' to the series to be computed as an average by the reports generation facility. 'n' is specified as an {@link InstrumentableProperty} on the constructor */
		AVERAGE_VALUE,
		/** Instructs {@link InstrumentationHandlerComputable} (and subclasses) to completely ignore this 'InstrumentationEvent' */
		NOT_COMPUTABLE
	}
	
	public final String                   eventName;
	/** Specifies how this {@link InstrumentableEvent} is classified when handled by {@link InstrumentationHandlerLogPrintStream} or any of it's subclass */
	public final ELogSeverity             logSeverity;
	public final int                      logSeverityOrdinal;
	/** Specifies how this {@link InstrumentableEvent} is classified when handled by {@link InstrumentationHandlerComputable} or any of it's subclass */
	public final EComputability           computability;
	/** A list of String or Integer properties whose values will be used as indexes, when consolidating the data for a report generation --
	 *  for instance: on NEW_USER events, a property named 'areaCode' might be used to separate new users for each area code */
	public final InstrumentableProperty   computableValueProperty;
	public final InstrumentableProperty[] computableIndexProperties;
	
	/** Creates an 'InstrumentableEvent' meant to be used to log information */
	public InstrumentableEvent(String eventName, ELogSeverity logSeverity) {
		this(eventName, logSeverity, EComputability.NOT_COMPUTABLE, null, (InstrumentableProperty[])null);
	}
	
	/** Creates an 'InstrumentableEvent' meant to be used to compute {@link EComputability#HIT} information for later reports generation */
	public InstrumentableEvent(String eventName, InstrumentableProperty... computableIndexProperties) {
		this(eventName, ELogSeverity.DEBUG, EComputability.HIT, null, computableIndexProperties);
	}
	
	/** Mix between constructors {@link #InstrumentableEvent(String, ELogSeverity)} and {@link #InstrumentableEvent(String, InstrumentableProperty...)} */
	public InstrumentableEvent(String eventName, ELogSeverity logSeverity, InstrumentableProperty... computableIndexProperties) {
		this(eventName, logSeverity, EComputability.HIT, null, computableIndexProperties);
	}	

	/** Creates an 'InstrumentableEvent' meant to be used to compute INCREMENTAL, ABSOLUTE or AVERAGE {@link EComputability} information for later reports generation */
	public InstrumentableEvent(String eventName, EComputability computability, InstrumentableProperty computableValueProperty, InstrumentableProperty... computableIndexProperties) {
		this(eventName, ELogSeverity.DEBUG, computability, computableValueProperty, computableIndexProperties);
	}
	
	/** Mix between constructors {@link #InstrumentableEvent(String, ELogSeverity)} and {@link #InstrumentableEvent(String, EComputability, InstrumentableProperty, InstrumentableProperty...)} */
	public InstrumentableEvent(String eventName, ELogSeverity logSeverity, EComputability computability, InstrumentableProperty computableValueProperty, InstrumentableProperty... computableIndexProperties) {

		if ( ((computability == EComputability.ABSOLUTE_VALUE) || (computability == EComputability.INCREMENTAL_VALUE) || (computability == EComputability.AVERAGE_VALUE)) &&
			 (computableValueProperty.propertyType != Integer.class) ) {
			throw new RuntimeException("InstrumentableProperty '"+computableValueProperty.propertyName+"' should be numerical for ABSOLUTE, INCREMENTAL and AVERAGE computabilities");
		}

		this.eventName                 = eventName;
		this.logSeverity               = logSeverity;
		this.logSeverityOrdinal        = logSeverity.ordinal();
		this.computability             = computability;
		this.computableValueProperty   = computableValueProperty;
		this.computableIndexProperties = computableIndexProperties;
	}

}
