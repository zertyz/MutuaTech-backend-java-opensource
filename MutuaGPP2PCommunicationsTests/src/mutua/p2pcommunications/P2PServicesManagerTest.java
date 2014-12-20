package mutua.p2pcommunications;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;


/** <pre>
 * P2PServicesManagerTest.java
 * ===========================
 * (created by luiz, Dec 20, 2014)
 *
 * Tests if we can register custom P2P Services APIs and then invoke them
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

class CustomP2PServicesAPI extends P2PServicesAPI {
	
	public static final String P2PServiceAndVersionId = "CustomP2PServices for testing purposes, v. 1";
	
	@RecognizedBy("receiveWhatTimeIsItRequest")
	public String askWhatTimeIsIt(boolean ampm) {
		return "please share your RTC with me -- " + (ampm?"AM/PM, please":"24h, please");
	}
	
	// @ should recognize requests make by 'askWhatTimeIsIt'
	@RecognizePattern("please share your RTC with me -- ([^,]+), please")
	public boolean receiveWhatTimeIsItRequest(String desiredTimeFormat) throws P2PServicesUnrecognizedParameterFormatException {
		System.out.println("oh, yeah!");
		if ("24h".equals(desiredTimeFormat)) {
			// add to the send to the peer queue: "informWhatTimeItIs(false)"
		} else if ("AM/PM".equals(desiredTimeFormat)) {
			// add to the send to the peer queue: "informWhatTimeItIs(true)"
		} else {
			throw new P2PServicesUnrecognizedParameterFormatException("don't know how to respond to '"+desiredTimeFormat+"' time format request");
		}
		return true;
	}
	
	// @ should be recognized by 'receiveWhatTimeItIsResponse'
	public String informWhatTimeItIs(boolean ampm) {
		if (ampm) {
			return "5:60pm";
		} else {
			return "17:60";
		}
	}
	
	// @ should recognize what 'informWhatTimeItIs' generates
	// @ should recognize "(DD:DD{am|pm}?)" patterns
	public boolean receiveWhatTimeItIsResponse(String time) {
		System.out.println("Peer said now it is "+time);
		return true;
	}

}

public class P2PServicesManagerTest {

	@Test
	public void customP2PServicesTest() throws P2PServicesUnrecognizedParameterFormatException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		CustomP2PServicesAPI api = new CustomP2PServicesAPI();
		String askWhatTimeIsItMessage = api.askWhatTimeIsIt(false);
		Method m = api.findAttendingMethod(askWhatTimeIsItMessage);
		assertEquals("'findAttendingMethod' didn't work", "receiveWhatTimeIsItRequest", m.getName());
		Object[] parameters = api.parseProtocolMessageParametersForAttendingMethod(m, askWhatTimeIsItMessage);
		boolean receiveWhatTimeIsItRequestAck = (Boolean)m.invoke(api, parameters);
		assertTrue("Reflexive invokation of API method 'receiveWhatTimeIsItRequest' failed", receiveWhatTimeIsItRequestAck);
		
		// A fazer: criar DTO que representa a chamada ao método da API e resolver, numa tacada só o problema de ineficiência atual
		// e também a colocada da chamada numa fila.
		
//		assertTrue("Ask / be asked communication failed",        api.receiveWhatTimeIsItRequest(api.askWhatTimeIsIt(false)));
//		assertTrue("Inform / get informed communication failed", api.receiveWhatTimeItIsResponse(api.informWhatTimeItIs(false)));
		
		
//		Class<CustomP2PServicesAPI> r = CustomP2PServicesAPI.class;
//		Method[] ms = r.getMethods();
//		r.getMethod(name, parameterTypes);
//		ms[0].invoke(obj, args)(api, new Object[] {});
//		
//		api.inokeAPIMethod(r.getDeclaredMethod("receiveWhatTimeIsItRequest", String.class), api.askWhatTimeIsIt(false))

	}
}
