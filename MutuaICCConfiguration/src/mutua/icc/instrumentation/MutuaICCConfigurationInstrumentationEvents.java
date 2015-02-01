package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.MutuaICCConfigurationInstrumentationProperties.*;
import static mutua.icc.instrumentation.DefaultInstrumentationProperties.*;

/** <pre>
 * MutuaICCConfigurationInstrumentationEvents.java
 * ===============================================
 * (created by luiz, Jan 29, 2015)
 *
 * Defines the available events that can participate on instrumentation logs
 *
 * @see MutuaICCConfigurationInstrumentationProperties
 * @version $Id$
 * @author luiz
 */

public enum MutuaICCConfigurationInstrumentationEvents implements IInstrumentableEvent {

	
	IE_LOADING_CONFIGURATION_FILE ("Loading configuration file", IP_FILE_NAME),
	
	IE_CONFIGURING_CLASS ("Initializing Configuration SET", IP_CLASS),

	// values
	/////////
	
	IE_CONFIGURING_STRING_PROPERTY       ("Configuring STRING property",       IP_CONFIGURATION_FIELD_NAME, IP_CONFIGURATION_STRING_FIELD_VALUE),
	IE_CONFIGURING_NUMBER_PROPERTY       ("Configuring NUMBER property",       IP_CONFIGURATION_FIELD_NAME, IP_CONFIGURATION_NUMBER_FIELD_VALUE),
	IE_CONFIGURING_STRING_ARRAY_PROPERTY ("Configuring STRING ARRAY property", IP_CONFIGURATION_FIELD_NAME, IP_CONFIGURATION_STRING_ARRAY_FIELD_VALUE),
	IE_CONFIGURING_ENUMERATION_PROPERTY  ("Configuring ENUMERATION property",  IP_CONFIGURATION_FIELD_NAME, IP_CONFIGURATION_ENUMERATION_FIELD_VALUE),
	
	
	// errors
	/////////
	
	IE_DESSERIALIZATION_ERROR ("Configuration desserialization ERROR", IP_ERROR_MSG),
	
	;
	
	
	private InstrumentableEvent instrumentableEvent;
	
	private MutuaICCConfigurationInstrumentationEvents(String name, IInstrumentableProperty property) {
		instrumentableEvent = new InstrumentableEvent(name, property);
	}
	
	private MutuaICCConfigurationInstrumentationEvents(String name, IInstrumentableProperty property1, IInstrumentableProperty property2) {
		instrumentableEvent = new InstrumentableEvent(name, property1, property2);
	}
	
	private MutuaICCConfigurationInstrumentationEvents(String name) {
		instrumentableEvent = new InstrumentableEvent(name);
	}

	@Override
	public InstrumentableEvent getInstrumentableEvent() {
		return instrumentableEvent;
	}

}
