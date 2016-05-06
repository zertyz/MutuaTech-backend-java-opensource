package mutua.icc.configuration;

import static org.junit.Assert.*;

import java.io.IOException;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.*;
import mutua.icc.configuration.annotations.ConfigurableElement;
import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;
import mutua.icc.instrumentation.handlers.IInstrumentationHandler;
import mutua.icc.instrumentation.handlers.InstrumentationHandlerLogConsole;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** <pre>
 * ConfigurationManagerTests.java
 * ==============================
 * (created by luiz, Jan 29, 2015)
 *
 * Tests the 'ConfigurationManager' behavior and features
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class ConfigurationManagerTests {
	
	
	private static String defaultValues;
	
	static {
		IInstrumentationHandler log = new InstrumentationHandlerLogConsole("MutuaICCConfigurationTests", ELogSeverity.DEBUG);
		Instrumentation.configureDefaultValuesForNewInstances(log, null, null);
	}


	@BeforeClass
	public static void saveDefaultValues() throws IllegalArgumentException, IllegalAccessException {
		ConfigurationManager cm = new ConfigurationManager(OneOfEachValueConfigurableClass.class);
		defaultValues = cm.serializeConfigurableClasses();
	}

	@Before
	public void resetDefaultValues() throws IllegalArgumentException, IllegalAccessException {
		ConfigurationManager cm = new ConfigurationManager(OneOfEachValueConfigurableClass.class);
		cm.deserializeConfigurableClasses(defaultValues);
	}

	private static void checkSerializationAndDeserialization(ConfigurationManager cm) throws IllegalArgumentException, IllegalAccessException {
		String serializedFields = cm.serializeConfigurableClasses();
		cm.deserializeConfigurableClasses(serializedFields);
		String reserializedFields = cm.serializeConfigurableClasses();
		assertEquals("Serialization/Deserialization of Configuration failed", serializedFields, reserializedFields);
	}

	/** Checks deserialization of the serialized values against current 'cm's class values */
	private static void checkDeserialization(ConfigurationManager cm, String serializedFields) throws IllegalArgumentException, IllegalAccessException {
		String originalSerialization = cm.serializeConfigurableClasses();
		cm.deserializeConfigurableClasses(serializedFields);
		String reserialization = cm.serializeConfigurableClasses();
		assertEquals("Deserialization of Configuration failed", originalSerialization, reserialization);
	}

	public void checkSaveAndLoadFromFile(ConfigurationManager cm) throws IllegalArgumentException, IllegalAccessException, IOException {
		cm.saveToFile("/tmp/config.tests");
	}
	
	
	@Test
	public void testOneOfEachValueSerializationAndDesserializationClass() throws IllegalArgumentException, IllegalAccessException, IOException {
		Instrumentation.startRequest(MSG_PROPERTY, "testSerializeConfigurationClass");
		ConfigurationManager cm = new ConfigurationManager(OneOfEachValueConfigurableClass.class);
		String serializedFields = cm.serializeConfigurableClasses();
		System.out.println(serializedFields);
		checkSerializationAndDeserialization(cm);
		checkSaveAndLoadFromFile(cm);
		Instrumentation.finishRequest();
	}
	
	@ConfigurableElement("This is the shitty method's javadoc to demonstrate that configurable elements may get their comments elsewhere")
	public static boolean shittyMethod() {
		return true;
	}
	
	@Test
	public void testConfigurationElementReference() throws IllegalArgumentException, IllegalAccessException, IOException {
		Instrumentation.startRequest(MSG_PROPERTY, "testConfigurationElementReference");
		ConfigurationManager cm = new ConfigurationManager(OneOfEachValueConfigurableClass.class);
		String serializedFields = cm.serializeConfigurableClasses();
		boolean isMethodRefenceDocumentationPresent  = serializedFields.matches("(?s).*shitty method.*");
		boolean isFieldReferenceDocumentationPresent = serializedFields.matches("(?s).*Now with enums.*");
		System.out.println(serializedFields);
		assertTrue("Documentation reference to a method didn't work",      isMethodRefenceDocumentationPresent);
		assertTrue("Documentation reference to a class field didn't work", isFieldReferenceDocumentationPresent);
		Instrumentation.finishRequest();
		shittyMethod();		// here just for you to hover and see the javadoc through annotations
	}
	
	@Test
	public void testCommentedProperties() throws IllegalArgumentException, IllegalAccessException, IOException {
		Instrumentation.startRequest(MSG_PROPERTY, "testCommentedProperties");
		ConfigurationManager cm = new ConfigurationManager(OneOfEachValueConfigurableClass.class);
		String serializedFields = cm.serializeConfigurableClasses();
		// all these should be ignored by the parser:
		String toBeIgnored = "#nicePlaces+=This one should not be loaded\n" +
		                     "#MyFirstChoice=TWO\n" +
		                     "#text=this is not the text i want...\n" +
		                     "#bool=false\n" +
		                     "#intNumber=-1\n" +
		                     "#longNumber=-1\n" +
		                     "#mySecondChoice=THREE\n" +
		                     "#myThirdChoice=ONE\n";
		serializedFields = "\n" + toBeIgnored + serializedFields + toBeIgnored;
		System.out.println(serializedFields);
		checkDeserialization(cm, serializedFields);
		checkSaveAndLoadFromFile(cm);
		Instrumentation.finishRequest();
	}
	
}

class OneOfEachValueConfigurableClass {
	
	public enum ESomeNumbers {ONE, TWO, @ConfigurableElement("May be too much")THREE};
	
	static {
		// fucking java... the right way to reference it in 1.6 is as described in 'myThirdChoice', with an '$'
		System.out.println(ESomeNumbers.ONE.getClass().getCanonicalName());
	}
	
	@ConfigurableElement("This is the dumb example. It is a freestyle String -- therefore, can have any value")
	public static String text = "this is a string";
	
	@ConfigurableElement(sameAsMethod="mutua.icc.configuration.ConfigurationManagerTests.shittyMethod")
	public static boolean bool  = true;
	
	@ConfigurableElement("This is an integer. Don't attempt to set any dotted values to it. Negatives are fine")
	public static int intNumber = 10;
	
	@ConfigurableElement("This is a long. Good to represent milliseconds")
	public static long longNumber = System.currentTimeMillis();
	
	@ConfigurableElement("For this one, we may have multiple entries... one per line.")
	public static String[] nicePlaces = {"Ilha Grande", "Ubatuba", "Maromba"};
	
	@ConfigurableElement("Now with enums")
	public static ESomeNumbers myFirstChoice = ESomeNumbers.ONE;
	
	@ConfigurableElement(sameAs="mutua.icc.configuration.OneOfEachValueConfigurableClass.myFirstChoice")
	public static ESomeNumbers mySecondChoice = ESomeNumbers.TWO;
	
	@ConfigurableElement(sameAs="mutua.icc.configuration.OneOfEachValueConfigurableClass$ESomeNumbers.THREE")
	public static ESomeNumbers myThirdChoice = ESomeNumbers.THREE;
	
}