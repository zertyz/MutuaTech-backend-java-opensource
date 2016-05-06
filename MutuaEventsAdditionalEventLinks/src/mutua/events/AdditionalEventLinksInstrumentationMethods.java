package mutua.events;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.*;

import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.InstrumentableProperty;
import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;

/** <pre>
 * AdditionalEventLinksInstrumentationMethods.java
 * ===============================================
 * (created by luiz, May 5, 2016)
 *
 * Helper Instrumentation class concentrating definitions & calls to all
 * instrumentation events used by this project
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class AdditionalEventLinksInstrumentationMethods {
	
	// 'InstrumentableEvent's
	private static final InstrumentableEvent databaseQueueInfoEvent;
	
	// 'InstrumentableProperty'ies
	private static final InstrumentableProperty queueTableNameProperty;
	
	static {
		queueTableNameProperty = new InstrumentableProperty("queueTableName", String.class);

		databaseQueueInfoEvent = new InstrumentableEvent("DatabaseQueueEventLink INFO",  ELogSeverity.INFO);
	}

	public static void reportDatabaseQueueInfo(String msg, String queueTableName) {
		Instrumentation.justLog(databaseQueueInfoEvent, queueTableNameProperty, queueTableName, MSG_PROPERTY, msg);
	}

}