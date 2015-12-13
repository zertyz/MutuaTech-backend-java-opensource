package mutua.icc.configuration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** <pre>
 * ConfigurationParser.java
 * ========================
 * (created by luiz, Apr 15, 2015)
 *
 * Parses a configuration file, searching for \n or \r\n separated lines containing:
 *  "NAME=VALUE" and
 *  "NAME+=VALUE" relations.
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class ConfigurationParser {
	
	
	private Hashtable<String, ArrayList<String>> keyToValuesMap;	// := {key1={value1, value2, ...}, ...}
	private ArrayList<String> orderedKeys;	// so we can provide the keys in the order they appear on the configuration file
	private ArrayList<Object[]> errorLines;	// := {{line_number, line_contents}, ...}
	
	
	/** g'ol'gsub like function */
	private String[] attemptToMatchAndCapture(String input, String regularExpression) {
        Pattern pattern = Pattern.compile(regularExpression, Pattern.DOTALL | Pattern.MULTILINE);
        Matcher m = pattern.matcher(input);
        while (m.find()) {
            String[] captures = new String[m.groupCount()];
            if (m.groupCount() > 0) {
                for (int i=0; i<captures.length; i++) {
                    captures[i] = m.group(i+1);                // discard item 0 -- the matched string
                }
            }
            //String matched = m.group();
            return captures;
        }
        return null;
	}
	
	private void parse(String text) {
		String[] lines = text.split("\r?\n");
		for (int lineNumber=0; lineNumber<lines.length; lineNumber++) {
			String lineContents = lines[lineNumber];
			// is this a comment or empty line?
			if ((lineContents.length() == 0) ||
				lineContents.matches(" *#.*") || lineContents.matches(" *//.*")) {
				continue;
			}
			// ... is this an "add value" property?
			String[] addCaptures = attemptToMatchAndCapture(lineContents, "([^\\+]+)\\+=(.*)");
			if (addCaptures != null) {
				String key   = addCaptures[0];
				String value = addCaptures[1];
				ArrayList<String> values;
				if (keyToValuesMap.containsKey(key)) {
					values = keyToValuesMap.get(key);
				} else {
					values = new ArrayList<String>();
					keyToValuesMap.put(key, values);
					orderedKeys.add(key);
				}
				values.add(value);
				continue;
			}
			// ... is this is a "set value" property?
			String[] setCaptures = attemptToMatchAndCapture(lineContents, "([^=]+)=(.*)");
			if (setCaptures != null) {
				String key   = setCaptures[0];
				String value = setCaptures[1];
				ArrayList<String> values = new ArrayList<String>();
				values.add(value);
				keyToValuesMap.put(key, values);
				orderedKeys.add(key);
				continue;
			}
			// so this is an invalid line. report.
			errorLines.add(new Object[] {lineNumber, lineContents});
		}
	}

	/** instantiate this class and make it ready to provide information about the 'configurationContents' provided */
	public ConfigurationParser(String configurationContents) {
		keyToValuesMap = new Hashtable<String, ArrayList<String>>();
		orderedKeys    = new ArrayList<String>();
		errorLines     = new ArrayList<Object[]>();
		parse(configurationContents);
	}

	public String[] getKeys() {
		return orderedKeys.toArray(new String[] {});
	}
	
	public String[] getValues(String key) {
		if (keyToValuesMap.containsKey(key)) {
			return keyToValuesMap.get(key).toArray(new String[] {});
		} else {
			return null;
		}
	}
	
	public Object[][] getErrorLines() {
		return errorLines.toArray(new Object[][] {});
	}
	
}
