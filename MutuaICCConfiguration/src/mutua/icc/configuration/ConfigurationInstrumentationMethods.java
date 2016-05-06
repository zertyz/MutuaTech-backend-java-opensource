package mutua.icc.configuration;

import java.lang.reflect.Field;
import java.util.Arrays;

import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;
import mutua.icc.instrumentation.InstrumentableProperty;
import mutua.icc.instrumentation.Instrumentation;

/** <pre>
 * ConfigurationInstrumentationMethods.java
 * ========================================
 * (created by luiz, May 4, 2016)
 *
 * Helper Instrumentation class concentrating definitions & calls to all
 * instrumentation events used by this project
 * 
 * @version $Id$
 * @author luiz
 */

public class ConfigurationInstrumentationMethods {
	
	// 'InstrumentableEvent's
	private static final InstrumentableEvent loadConfigurationFileEvent;
	private static final InstrumentableEvent configureClassEvent;
	private static final InstrumentableEvent configureStringPropertyEvent;
	private static final InstrumentableEvent configureNumberPropertyEvent;
	private static final InstrumentableEvent configureBooleanPropertyEvent;
	private static final InstrumentableEvent configureStringArrayPropertyEvent;
	private static final InstrumentableEvent configureEnumerationPropertyEvent;
	private static final InstrumentableEvent deserializationErrorEvent;

	// 'InstrumentableProperty'ies
	private static final InstrumentableProperty fileNameProperty;
	private static final InstrumentableProperty classNameProperty;
	private static final InstrumentableProperty fieldNameProperty;
	private static final InstrumentableProperty stringFieldValueProperty;
	private static final InstrumentableProperty numberFieldValueProperty;
	private static final InstrumentableProperty booleanFieldValueProperty;
	private static final InstrumentableProperty stringArrayFieldValueProperty;
	private static final InstrumentableProperty enumerationFieldValueProperty;
	private static final InstrumentableProperty errorMessageProperty;
	
	static {
		fileNameProperty              = new InstrumentableProperty("file",      String.class);
		classNameProperty             = new InstrumentableProperty("class",     Class.class);
		fieldNameProperty             = new InstrumentableProperty("fieldName", String.class);
		stringFieldValueProperty      = new InstrumentableProperty("value",     String.class);
		numberFieldValueProperty      = new InstrumentableProperty("value",     Long.class);
		booleanFieldValueProperty     = new InstrumentableProperty("value",     Boolean.class);
		stringArrayFieldValueProperty = new InstrumentableProperty("values",    String[].class);
		enumerationFieldValueProperty = new InstrumentableProperty("value",     Enum.class);
		errorMessageProperty          = new InstrumentableProperty("errorMsg",  String.class);
		
		loadConfigurationFileEvent        = new InstrumentableEvent("Loading configuration file",           ELogSeverity.CRITICAL);
		configureClassEvent               = new InstrumentableEvent("Initializing Configuration SET",       ELogSeverity.CRITICAL);
		configureStringPropertyEvent      = new InstrumentableEvent("Configuring STRING field",             ELogSeverity.CRITICAL);
		configureNumberPropertyEvent      = new InstrumentableEvent("Configuring NUMBER field",             ELogSeverity.CRITICAL);
		configureBooleanPropertyEvent     = new InstrumentableEvent("Configuring BOOLEAN field",            ELogSeverity.CRITICAL);
		configureStringArrayPropertyEvent = new InstrumentableEvent("Configuring STRING ARRAY field",       ELogSeverity.CRITICAL);
		configureEnumerationPropertyEvent = new InstrumentableEvent("Configuring ENUMERATION field",        ELogSeverity.CRITICAL);
		deserializationErrorEvent         = new InstrumentableEvent("Configuration deserialization ERROR", ELogSeverity.ERROR);
	}
	
	public static void reportLoadConfigurationFile(String filePath) {
		Instrumentation.justLog(loadConfigurationFileEvent, fileNameProperty, filePath);
	}
	
	public static void reportConfigureClass(Class<?> configurableClass) {
		Instrumentation.justLog(configureClassEvent, classNameProperty, configurableClass);
	}
	
	public static void reportConfigureStringField(String fieldName, String fieldValue) {
		Instrumentation.justLog(configureStringPropertyEvent, fieldNameProperty, fieldName, stringFieldValueProperty, fieldValue);
	}

	public static void reportConfigureNumberField(String fieldName, Number fieldValue) {
		Instrumentation.justLog(configureNumberPropertyEvent, fieldNameProperty, fieldName, numberFieldValueProperty, fieldValue);
	}
	
	public static void reportConfigureBooleanField(String fieldName, boolean fieldValue) {
		Instrumentation.justLog(configureBooleanPropertyEvent, fieldNameProperty, fieldName, booleanFieldValueProperty, fieldValue);
	}
	
	public static void reportConfigureStringArrayField(String fieldName, String[] fieldValue) {
		Instrumentation.justLog(configureStringArrayPropertyEvent, fieldNameProperty, fieldName, stringArrayFieldValueProperty, fieldValue);
	}
	
	public static void reportConfigureEnumerationField(String fieldName, Object fieldValue) {
		Instrumentation.justLog(configureEnumerationPropertyEvent, fieldNameProperty, fieldName, enumerationFieldValueProperty, fieldValue);
	}
	
	public static void reportDeserializationError(String errorMsg) {
		Instrumentation.justLog(deserializationErrorEvent, errorMessageProperty, errorMsg);
	}
	
	public static void logFieldIsNotPresentOnConfiguration(String type, Field f) {
		try {
			reportDeserializationError(type+" property '"+f.getName()+"' is not present on configuration. Using default value of '"+f.get(null)+"'");
		} catch (Throwable t) {
			reportDeserializationError(type+" property '"+f.getName()+"' is neither present on configuration nor has a default value (or it is inaccessable)");
		}
	}
	
	public static void logScalarFieldDeclaredAsVector(Field f, String[] values) {
		reportDeserializationError("Scalar property '"+f.getName()+"' was wrongly declared as an array containing values "+Arrays.deepToString(values));
	}
	

}
