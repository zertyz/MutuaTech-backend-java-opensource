package mutua.icc.instrumentation.handlers;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import mutua.icc.instrumentation.dto.InstrumentationEventDto;

/** <pre>
 * InstrumentationHandlerLogRotatoryFile.java
 * ==========================================
 * (created by luiz, Apr 20, 2016)
 *
 * Specialization of {@link InstrumentationHandlerLogPrintStream} to account for log file rotation, through one of
 * the file output classes
 *
 * @see InstrumentationHandlerLogRotatoryPlainFile
 * @see InstrumentationHandlerLogRotatoryCompressedFile
 * @version $Id$
 * @author luiz
*/

public abstract class InstrumentationHandlerLogRotatoryFile extends InstrumentationHandlerLogPrintStream {
	
	public static final int DAILY_ROTATION_FREQUENCY = 1;
	
	private final int    rotationFrequency;
	private final String fsFilePathPrefix;
	private final String fsFilePathSuffix;
	private       long   nextRorationMillis;

	public InstrumentationHandlerLogRotatoryFile(String applicationName, String fsFilePathPrefix, String fsFilePathSuffix, int minimumLogLevel, int rotationFrequency) {
		super(applicationName, System.out, minimumLogLevel);
		this.rotationFrequency = rotationFrequency;
		this.fsFilePathPrefix = fsFilePathPrefix;
		this.fsFilePathSuffix = fsFilePathSuffix;
		closeOldAndOpenNewPrintStream(getFSFilePath(System.currentTimeMillis()));
		setNextRotationMillis();
	}
	
	/** Called to set 'nextRotationMillis', which indicates the time in which the current PrintStream 'out' expires --
	 *  when calls to {@link #closeOldPrintStream()} and {@link #openNewPrintStream()} should be placed */
	public void setNextRotationMillis() {
		// find the milliseconds representing the next midnight
		Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        if (rotationFrequency == DAILY_ROTATION_FREQUENCY) {
        	cal.set(Calendar.HOUR_OF_DAY, 0);
        	cal.add(Calendar.DAY_OF_MONTH, 1);
        } else {
        	throw new RuntimeException("Unknown 'rotationFrequency' " + rotationFrequency);
        }
		nextRorationMillis = cal.getTimeInMillis()-1;
	}
	
	public String getFSFilePath(long currentTimeMillis) {
		String dateAndTimeFormat;
		if (rotationFrequency == DAILY_ROTATION_FREQUENCY) {
			dateAndTimeFormat = "yyyy-MM-dd";
		} else {
        	throw new RuntimeException("Unknown 'rotationFrequency' " + rotationFrequency);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(dateAndTimeFormat);
		FieldPosition    fp  = new FieldPosition(0);
		String dateAndTimeId = sdf.format(currentTimeMillis, new StringBuffer(), fp).toString();
		return fsFilePathPrefix + dateAndTimeId + fsFilePathSuffix;
	}
	
	/** This method should close the current 'out' and open a new PrintStream and will be called when rotating to a new log file */
	public abstract void closeOldAndOpenNewPrintStream(String fsFilePath);
	
	@Override
	public void onInstrumentationEvent(InstrumentationEventDto instrumentationEvent) {
		synchronized (logLine) {
			if (instrumentationEvent.currentTimeMillis > nextRorationMillis) {
				closeOldAndOpenNewPrintStream(getFSFilePath(instrumentationEvent.currentTimeMillis));
				setNextRotationMillis();
			}
			super.onInstrumentationEvent(instrumentationEvent);
		}
	}

}