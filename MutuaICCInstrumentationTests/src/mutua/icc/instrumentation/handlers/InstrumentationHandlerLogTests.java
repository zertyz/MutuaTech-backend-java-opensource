package mutua.icc.instrumentation.handlers;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import static mutua.icc.instrumentation.TestInstrumentationMethods.*;
import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;

import org.junit.Test;

/** <pre>
 * InstrumentationHandlerLogTests.java
 * ===================================
 * (created by luiz, Apr 28, 2016)
 *
 * Tests the various log implementations of the Instrumentation Handler architecture.
 *
 * @version $Id$
 * @author luiz
 */

public class InstrumentationHandlerLogTests {
	
	public static void reportDebugEvent(IInstrumentationHandler logHandler, String message) {
		logHandler.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), debugEvent, messageProperty, message));
	}
	
	public static void reportCryptEvent(IInstrumentationHandler logHandler, String message) {
		logHandler.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), cryptEvent, messageProperty, message));
	}
	
	public static void reportInfoEvent(IInstrumentationHandler logHandler, String message) {
		logHandler.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), infoEvent, messageProperty, message));
	}
	
	public static void reportCriticalEvent(IInstrumentationHandler logHandler, String message) {
		logHandler.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), criticalEvent, messageProperty, message));
	}
	
	public static void reportErrorEvent(IInstrumentationHandler logHandler, String message) {
		logHandler.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), errorEvent, messageProperty, message));
	}
	
	
	//public static void 

	@Test
	public void testLogToAPrintStream() {
		
		ByteArrayOutputStream debugLogLevelBaos    = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream cryptLogLevelBaos    = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream infoLogLevelBaos     = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream criticalLogLevelBaos = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream errorLogLevelBaos    = new ByteArrayOutputStream(1024);
				
		IInstrumentationHandler debugLogLevel    = new InstrumentationHandlerLogPrintStream("LogHandlerDebugLevelTests",    debugLogLevelBaos,    ELogSeverity.DEBUG);
		IInstrumentationHandler cryptLogLevel    = new InstrumentationHandlerLogPrintStream("LogHandlerCryptLevelTests",    cryptLogLevelBaos,    ELogSeverity.CRYPT);
		IInstrumentationHandler infoLogLevel     = new InstrumentationHandlerLogPrintStream("LogHandlerInfoLevelTests",     infoLogLevelBaos,     ELogSeverity.INFO);
		IInstrumentationHandler criticalLogLevel = new InstrumentationHandlerLogPrintStream("LogHandlerCriticalLevelTests", criticalLogLevelBaos, ELogSeverity.CRITICAL);
		IInstrumentationHandler errorLogLevel    = new InstrumentationHandlerLogPrintStream("LogHandlerErrorLevelTests",    errorLogLevelBaos,    ELogSeverity.ERROR);
		
		String debugMessage    = "This is my debug message";
		String cryptMessage    = "This is a crypt message";
		String infoMessage     = "This is an info message";
		String criticalMessage = "This is a critical message";
		String errorMessage    = "This is an error message";
		
		String[]                   messages = {debugMessage,  cryptMessage,  infoMessage,  criticalMessage,  errorMessage};
		IInstrumentationHandler[] logLevels = {debugLogLevel, cryptLogLevel, infoLogLevel, criticalLogLevel, errorLogLevel};
		String[]             suffixMessages = {"a debug",     "a crypt",     "an info",    "a critical",     "an error"};
		
		for (int i=0; i<messages.length; i++) {
			String message = messages[i];
			IInstrumentationHandler logLevel = logLevels[i];
			String suffixMessage = suffixMessages[i];
			reportDebugEvent(logLevel,       message + " routed to "+suffixMessage+" level log event handler");
			reportCryptEvent(logLevel,       message + " routed to "+suffixMessage+" level log event handler");
			reportInfoEvent(logLevel,        message + " routed to "+suffixMessage+" level log event handler");
			reportCriticalEvent(logLevel,    message + " routed to "+suffixMessage+" level log event handler");
			reportErrorEvent(logLevel,       message + " routed to "+suffixMessage+" level log event handler");
		}

		Object[] output = {
			"debugLogLevelBaos",    debugLogLevelBaos,
			"cryptLogLevelBaos",    cryptLogLevelBaos,
			"infoLogLevelBaos",     infoLogLevelBaos,
			"criticalLogLevelBaos", criticalLogLevelBaos,
			"errorLogLevelBaos",    errorLogLevelBaos,
		};
		
		for (int i=0; i<output.length; i+=2) {
			String                  outputName =                (String)output[i];
			ByteArrayOutputStream outputStream = (ByteArrayOutputStream)output[i+1];
			System.out.println(outputName+":");
			System.out.println(outputStream.toString());
		}

	}
	
	@Test
	public void testRotatoryLogFile() {
		
		final boolean[] closeOldAndOpenNewPrintStreamCalled = {false};
		final boolean[] setNextRotationMillisCalled         = {false};

		// constructs a test InstrumentationHandler
		IInstrumentationHandler log = new InstrumentationHandlerLogRotatoryPlainFile("LogHandlerDebugLevelTests", "/tmp/logTest", ".log", ELogSeverity.DEBUG, InstrumentationHandlerLogRotatoryFile.DAILY_ROTATION_FREQUENCY) {
			
			@Override
			public void closeOldAndOpenNewPrintStream(String fsFilePath) {
				super.closeOldAndOpenNewPrintStream(fsFilePath);
				closeOldAndOpenNewPrintStreamCalled[0] = true;
			}

			@Override
			public void setNextRotationMillis() {
				// causes an instant file rotation, for test purposes only
				nextRorationMillis = System.currentTimeMillis();
				setNextRotationMillisCalled[0] = true;
			}
		};
		
		reportDebugEvent(log, "first event. Should already open a new file");
		assertTrue("close old and open new not called",   closeOldAndOpenNewPrintStreamCalled[0]);
		assertTrue("set next rotation millis not called", setNextRotationMillisCalled[0]);
		closeOldAndOpenNewPrintStreamCalled[0] = false;
		setNextRotationMillisCalled[0]         = false;
		reportDebugEvent(log, "second event. Should once more open a new file... which happens to be the same as the previous one, with this line appended");
		assertTrue("close old and open new not called",   closeOldAndOpenNewPrintStreamCalled[0]);
		assertTrue("set next rotation millis not called", setNextRotationMillisCalled[0]);
	}
	
	@Test
	public void testLogToAXZFile() {
		IInstrumentationHandler debugLogLevel = new InstrumentationHandlerLogRotatoryCompressedFile("LogHandlerDebugLevelTests", "/tmp/logTest", ".log.xz", ELogSeverity.DEBUG, InstrumentationHandlerLogRotatoryFile.DAILY_ROTATION_FREQUENCY);
		
		for (int i=0; i<10000; i++) {
			reportDebugEvent(debugLogLevel, "this is my thing for i #"+i+" at ms "+System.currentTimeMillis());
		}
		
		debugLogLevel.close();	// when using the 'Instrumentation' class, this method will be called automatically by the shutdownHook

		
	}

}