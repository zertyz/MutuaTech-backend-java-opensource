package mutua.icc.configuration;

import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Categories.ExcludeCategory;

import static org.junit.Assert.*;

/** <pre>
 * ConfigurationParserTests.java
 * =============================
 * (created by luiz, Apr 15, 2015)
 *
 * Assures the parser works with different kinds of values and in different scenarios (normal, error, corrupted, etc).
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class ConfigurationParserTests {
	
	
	private void checkConfigurationContents(String configurationContents, String[][] expectedStructure) {
		ConfigurationParser cp = new ConfigurationParser(configurationContents);
		String[] keys = cp.getKeys();
		String[][] observedStructure = new String[keys.length][];
		for (int keysIndex=0; keysIndex<keys.length; keysIndex++) {
			String key = keys[keysIndex];
			String[] values = cp.getValues(key);
			observedStructure[keysIndex] = new String[1 + values.length];
			observedStructure[keysIndex][0] = key;
			System.arraycopy(values, 0, observedStructure[keysIndex], 1, values.length);
		}
		System.out.println(Arrays.deepToString(observedStructure));
		assertArrayEquals("Parsed content is wrong", expectedStructure, observedStructure);
		assertEquals("Errors on the configuration were wrongly reported", 0, cp.getErrorLines().length);
	}


	@Test
	public void testNiceToParseConfiguration() {
		String configurationContents = "Name=Luiz\n"              +
		                               "Age=36\n"                 +
		                               "Nick=Dom\n"               +
		                               "Transports+=Bike\n"       +
		                               "Transports+=Motorbyke\n"  +
		                               "Transports+=Car\n"        ;
		String[][] expectedStructure = {
			{"Name",       "Luiz"},
			{"Age",        "36"},
			{"Nick",       "Dom"},
			{"Transports", "Bike", "Motorbyke", "Car"},
		};
		
		checkConfigurationContents(configurationContents, expectedStructure);
	}
	
	@Test
	public void testNiceOnWindowsConfiguration() {
		String configurationContents = "Name=Patrícia\r\n" +
                                       "Age=36\r\n"        +
                                       "Nick=Paty\r\n"     +
                                       "Transports+=\r\n"  ;
		String[][] expectedStructure = {
			{"Name",       "Patrícia"},
			{"Age",        "36"},
			{"Nick",       "Paty"},
			{"Transports", ""},
		};
	   
		checkConfigurationContents(configurationContents, expectedStructure);
	}
	
	@Test
	public void testConfigurationWithCommentsAndEmptyLines() {
		String configurationContents = "# just the first name will do\n"    +
		                               "Name=Luiz\n\n"                      +
		                               "// do not lie, mother fucker!\n"    +
		                               "Age=36\n"                           +
		                               "Nick=Dom\n\n"                       +
		                               "Transports+=Bike\n\n\n"             +
		                               "# isn't this one awayting a fix?\n" +
		                               "Transports+=Motorbyke\n\n\n"        +
		                               "Transports+=Car\n"                  ;
		String[][] expectedStructure = {
			{"Name",       "Luiz"},
			{"Age",        "36"},
			{"Nick",       "Dom"},
			{"Transports", "Bike", "Motorbyke", "Car"},
		};
		
		checkConfigurationContents(configurationContents, expectedStructure);
	}
	
	@Test
	public void testCommentedProperties() {
		String configurationContents = "Name=Luiz\n"              +
		                               "#Age=35\n"                 +
		                               "Age=36\n"                 +
		                               "#Age=37\n"                 +
		                               "Nick=Dom\n"               +
		                               "Transports+=Bike\n"       +
		                               "Transports+=Motorbyke\n"  +
		                               "Transports+=Car\n"        ;
		String[][] expectedStructure = {
			{"Name",       "Luiz"},
			{"Age",        "36"},
			{"Nick",       "Dom"},
			{"Transports", "Bike", "Motorbyke", "Car"},
		};
		
		checkConfigurationContents(configurationContents, expectedStructure);
	}
	
	@Test
	public void testMisformattedConfiguration() {
		String configurationContents = " Name=Luiz\n"              +
		                               "Age =36\n"                 +
		                               "Nick= Dom\n"               +
		                               "Transports += Bike\n"       +
		                               "Transports +=Motorbyke\n"  +
		                               "Transports+= Car\n"        ;
		String[][] expectedStructure = {
			{" Name",       "Luiz"},
			{"Age ",        "36"},
			{"Nick",        " Dom"},
			{"Transports ", " Bike", "Motorbyke"},
			{"Transports",  " Car"},
		};
		
		checkConfigurationContents(configurationContents, expectedStructure);
	}

	@Test
	public void testWrongEntries() {
		String configurationContents = "Name: Luiz\n"              +
		                               "Age is 36\n"                 +
		                               "Nick=>Dom\n"               +
		                               "Transports.Bike\n"       +
		                               "Transports=Motorbyke\n"  +
		                               "Transports(Car)\n"        ;

		Object[][] expectedErrorLines = {
			{0, "Name: Luiz"},	
			{1, "Age is 36"},
			{3, "Transports.Bike"},
			{5, "Transports(Car)"},
		};
		
		ConfigurationParser cp = new ConfigurationParser(configurationContents);

		Object[][] observedErrorLines = cp.getErrorLines();

		// dump to stdout, for debug
		for (Object[] errorLine : observedErrorLines) {
			int    errorLineNumber  = (Integer) errorLine[0];
			String errorLineContent = (String)  errorLine[1];
			System.out.println(errorLineNumber+": "+errorLineContent);
		}
		
		assertArrayEquals("Error lines reporting is wrong", expectedErrorLines, observedErrorLines);
	}
	
	@Test
	public void testGetInexistentProperty() {
		ConfigurationParser cp = new ConfigurationParser("");
		String[] inexistentValues = cp.getValues("inexistentProperty");
		assertNull("Inexistent properties should return null values", inexistentValues);
	}

}
