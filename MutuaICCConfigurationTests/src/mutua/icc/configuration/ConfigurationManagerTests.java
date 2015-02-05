package mutua.icc.configuration;

import static org.junit.Assert.assertEquals;

import java.io.FileOutputStream;
import java.io.IOException;

import mutua.icc.configuration.annotations.ConfigurableElement;
import mutua.icc.instrumentation.DefaultInstrumentationProperties;
import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.pour.PourFactory.EInstrumentationDataPours;

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
	
	private static Instrumentation<DefaultInstrumentationProperties, String> log = new Instrumentation<DefaultInstrumentationProperties, String>(
			"MutuaICCConfigurationTests", DefaultInstrumentationProperties.DIP_MSG, EInstrumentationDataPours.CONSOLE, null);
	
	
	private static void checkSerializationAndDesserialization(ConfigurationManager cm) throws IllegalArgumentException, IllegalAccessException {
		String serializedFields = cm.serializeConfigurableClasses();
		cm.deserializeConfigurableClasses(serializedFields);
		String reserializedFields = cm.serializeConfigurableClasses();
		assertEquals("Serialization/Desserialization of Configuration failed", serializedFields, reserializedFields);
	}

	public void checkSaveAndLoadFromFile(ConfigurationManager cm) throws IllegalArgumentException, IllegalAccessException, IOException {
		cm.saveToFile("/tmp/config.tests");
	}
	

	@Test
	public void testOneOfEachValueSerializationAndDesserializationClass() throws IllegalArgumentException, IllegalAccessException, IOException {
		log.reportRequestStart("testSerializeConfigurationClass");
		ConfigurationManager cm = new ConfigurationManager(log, OneOfEachValueConfigurableClass.class);
		String serializedFields = cm.serializeConfigurableClasses();
		System.out.println(serializedFields);
		checkSerializationAndDesserialization(cm);
		checkSaveAndLoadFromFile(cm);
		log.reportRequestFinish();
	}
	
	@Test
	public void testConfigurationElementReference() throws IllegalArgumentException, IllegalAccessException, IOException {
		log.reportRequestStart("testConfigurationElementReference");
		ConfigurationManager cm = new ConfigurationManager(log, OneOfEachValueConfigurableClass.class);
		String serializedFields = cm.serializeConfigurableClasses();
		System.out.println(serializedFields);
		checkSerializationAndDesserialization(cm);
		checkSaveAndLoadFromFile(cm);
		log.reportRequestFinish();
	}
	
}

class OneOfEachValueConfigurableClass {
	
	public enum ESomeNumbers {ONE, TWO, THREE};
	
	@ConfigurableElement("This is the dump example. It is a freestyle String -- therefore, can have any value")
	public static String text = "this is a string";
	
	@ConfigurableElement("This is an integer. Don't attempt to set any dotted values to it. Negatives are fine")
	public static int intNumber = 10;
	
	@ConfigurableElement("This is a long. Good to represent milliseconds")
	public static long longNumber = System.currentTimeMillis();
	
	@ConfigurableElement("For this one, we may have multiple entries... one per line.")
	public static String[] nicePlaces = {"Ilha Grande", "Ubatuba", "Maromba"};
	
	@ConfigurableElement("Now with enums")
	public static ESomeNumbers myChoice = ESomeNumbers.TWO;
	
	@ConfigurableElement(sameAs="mutua.icc.configuration.OneOfEachValueConfigurableClass.myChoice")
	public static ESomeNumbers myOtherChoice = ESomeNumbers.THREE;
	
}