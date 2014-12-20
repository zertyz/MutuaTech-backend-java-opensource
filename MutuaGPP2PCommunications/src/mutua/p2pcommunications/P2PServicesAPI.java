package mutua.p2pcommunications;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** <pre>
 * P2PServicesAPI.java
 * ================
 * (created by luiz, Dec 20, 2014)
 *
 * TYPE HERE WHY THIS CLASS MUST EXIST AND GIVE AN IDEA HOW IT RELATES
 * TO THE REST OF THE PROJECT
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public abstract class P2PServicesAPI {

	public static String P2PServiceAndVersionId;
	
	private Object[] attemptToMatchAndCapture(String input, String regularExpression) {
        Pattern pattern = Pattern.compile(regularExpression, Pattern.DOTALL | Pattern.MULTILINE);
        Matcher m = pattern.matcher(input);
        while (m.find()) {
            Object[] captures = new String[m.groupCount()];
            if (m.groupCount() > 0) {
                for (int i=0; i<captures.length; i++) {
                    captures[i] = m.group(i+1);                // discard item 0 -- the matched string
                }
            }
            String matched = m.group();
            return captures;
        }
        return null;
	}
	
	public Method findAttendingMethod(String protocolMessage) {
		Class<? extends P2PServicesAPI> r = this.getClass();
		Method[] methods = r.getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(RecognizePattern.class)) {
				String recognizePattern = method.getAnnotation(RecognizePattern.class).value();
				if (attemptToMatchAndCapture(protocolMessage, recognizePattern) != null) {
					return method;
				}
			}
		}
		return null;
	}

	public Object[] parseProtocolMessageParametersForAttendingMethod(Method method, String protocolMessage) {
		String recognizePattern = method.getAnnotation(RecognizePattern.class).value();
		Object[] parameters = attemptToMatchAndCapture(protocolMessage, recognizePattern);
		return parameters;
	}
	

}
