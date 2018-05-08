package adapters;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.*;

import java.sql.SQLException;

import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.InstrumentableProperty;
import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;
import mutua.icc.instrumentation.Instrumentation;
import mutua.serialization.SerializationRepository;
import mutua.serialization.SerializationRepository.EfficientTextualSerializationMethod;

/** <pre>
 * JDBCAdapterInstrumentationMethods.java
 * ======================================
 * (created by luiz, May 5, 2016)
 *
 * Helper Instrumentation class concentrating definitions & calls to all
 * instrumentation events used by this project
 *
 * @version $Id$
 * @author luiz
 */

public class JDBCAdapterInstrumentationMethods {

	// 'InstrumentableEvent's
	private static final InstrumentableEvent databaseAdministrationWarningEvent;
	private static final InstrumentableEvent databaseSQLEvent;
	private static final InstrumentableEvent databaseQueryStartEvent;
	private static final InstrumentableEvent databaseQueryFinishEvent;
	private static final InstrumentableEvent databaseRetryQueryEvent;
	
	// 'InstrumentableProperty'ies
	private static final InstrumentableProperty preparedSQLProperty;
	private static final InstrumentableProperty parametersAndValuesPairsProperty;
	
	public static class ParametersAndValuesPairsSerializer {
		// code based on 'AbstractPreparedProcedure#buildPreparedStatement'
		@EfficientTextualSerializationMethod
		public static void toString(Object _this, StringBuffer buffer) {
			if (_this instanceof Object[][]) {
				Object[][] batchParametersAndValuesPairs = (Object[][])(Object)_this;
				for (int i=0; i<batchParametersAndValuesPairs.length; i++) {
					if (i>0) {
						buffer.append(',');
					}
					buffer.append('{');
					toString(batchParametersAndValuesPairs[i], buffer);
					buffer.append('}');
				}
			} else if (_this instanceof Object[]) {
				Object[] parametersAndValuesPairs = (Object[])(Object)_this;
				for (int i=0; i<parametersAndValuesPairs.length; i+=2) {
					if (i>0) {
						buffer.append(',');
					}
					String parameterName  = ((IJDBCAdapterParameterDefinition)parametersAndValuesPairs[i]).getParameterName();
					buffer.append(parameterName).append('=');
					Object parameterValue = parametersAndValuesPairs[i+1];
					if (parameterValue instanceof String) {
						SerializationRepository.serialize(buffer.append('\''), (String)parameterValue).append('\'');
					} else {
						buffer.append(parameterValue);
					}
				}
			}
		}
	}
	
	static {
		preparedSQLProperty              = new InstrumentableProperty("preparedSQL", String.class);
		parametersAndValuesPairsProperty = new InstrumentableProperty("parameters",  ParametersAndValuesPairsSerializer.class);
		
		databaseAdministrationWarningEvent = new InstrumentableEvent("JDBCAdapter Database Administration warning",  ELogSeverity.INFO);
		databaseSQLEvent                   = new InstrumentableEvent("Database Query",                               ELogSeverity.DEBUG);
		databaseQueryStartEvent            = new InstrumentableEvent("queryStarted",   preparedSQLProperty);
		databaseQueryFinishEvent           = new InstrumentableEvent("queryFinished",  preparedSQLProperty);
		databaseRetryQueryEvent            = new InstrumentableEvent("Exception while executing query. Recheking connections and associated prepared statements -- after that, a retry will be transparently performed", ELogSeverity.ERROR, preparedSQLProperty, THROWABLE_PROPERTY);
	}

	public static void reportAdministrationWarningMessage(String message) {
		Instrumentation.justLog(databaseAdministrationWarningEvent, MSG_PROPERTY, message);
	}
	
	public static void reportRetryingQueryDueToException(Throwable t, AbstractPreparedProcedure absPP, Object... pvp) {
		Instrumentation.logAndCompute(databaseRetryQueryEvent, preparedSQLProperty, absPP.getPreparedProcedureSQL(), parametersAndValuesPairsProperty, pvp, THROWABLE_PROPERTY, t);
		if (t instanceof SQLException) {
			int c = 2;
			SQLException nextSQLException = ((SQLException)t).getNextException();
			while (nextSQLException != null) {
				Instrumentation.reportThrowable(nextSQLException, "neasted exception #"+(c++));
				nextSQLException = nextSQLException.getNextException();
			}
		}
	}
	
	public static void reportDatabaseSQL(AbstractPreparedProcedure absPP, Object... pvp) {
		Instrumentation.justLog(databaseSQLEvent, preparedSQLProperty, absPP.getPreparedProcedureSQL(), parametersAndValuesPairsProperty, pvp);
	}

	public static void reportDatabaseSQL(String query) {
		Instrumentation.justLog(databaseSQLEvent, preparedSQLProperty, query);
	}
}
