package mutua.p2pcommunications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ArrayBlockingQueue;

import mutua.p2pcommunications.model.P2PServicesAPIMethodCallInfo;

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
	
	public String receivedTime;
	
	// TODO: os métodos que atendem a requests -- e têm expressão regular de reconhecimento -- podem ser sincronos ou assincronos,
	// sinalizado por anottations. Para o caso de ser assincrona, o método deve retornar um 'P2PServicesAPIMethodCallInfo' sinalizando
	// quem irá executar o trabalho e enviar uma resposta (este quem deve estar assinado com uma anottation que especifica isso).
	// para o caso de ser sincrono, basta responder uma string -- a anottation deve, de qualquer modo, especificar quem irá receber a resposta.
	
	@RecognizedBy("receiveWhatTimeIsItRequest")
	@P2PServicesAPIRequestingMethod
	public String askWhatTimeIsIt(Boolean ampm) {
		return "please share your RTC with me -- " + (ampm?"AM/PM, please":"24h, please");
	}
	
	// @ should recognize requests made by 'askWhatTimeIsIt'
	@RecognizePattern("please share your RTC with me -- ([^,]+), please")
	@P2PServicesAPIAsynchronousRequestReceiverMethod("informWhatTimeItIs")
	public P2PServicesAPIMethodCallInfo receiveWhatTimeIsItRequest(String desiredTimeFormat) throws P2PServicesUnrecognizedParameterFormatException, NoSuchMethodException {
		if ("24h".equals(desiredTimeFormat)) {
			return getAPIMethodInvocationInfo("informWhatTimeItIs", false);
		} else if ("AM/PM".equals(desiredTimeFormat)) {
			return getAPIMethodInvocationInfo("informWhatTimeItIs", true);
		} else {
			throw new P2PServicesUnrecognizedParameterFormatException("don't know how to respond to '"+desiredTimeFormat+"' time format request");
		}
	}
	
	@RecognizedBy("receiveWhatTimeItIsResponse")
	@P2PServicesAPIAsynchronousRequestAnsweringMethod("askWhatTimeIsIt")
	public String informWhatTimeItIs(Boolean ampm) {
		String answerPrefix = "Once you asked what time is it. Here is the answer: ";
		if (ampm) {
			return answerPrefix + "5:60pm";
		} else {
			return answerPrefix + "17:60";
		}
	}
	
	@RecognizePattern("Once you asked what time is it. Here is the answer: (\\d?\\d:\\d\\d[ap]?m?)")
	@P2PServicesAPIAnswerReceiverMethod("askWhatTimeIsIt")
	public boolean receiveWhatTimeItIsResponse(String receivedTime) {
		System.out.println("Peer said now it is "+receivedTime);
		this.receivedTime = receivedTime;
		return true;
	}

}

public class P2PServicesManagerTest {

	@Test
	public void customP2PServicesTest() throws P2PServicesUnrecognizedParameterFormatException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		CustomP2PServicesAPI api = new CustomP2PServicesAPI();
		String askWhatTimeIsItMessage = api.askWhatTimeIsIt(false);
		P2PServicesAPIMethodCallInfo methodCallInfo = api.parseProtocolMessageIntoMethodCall(askWhatTimeIsItMessage);
		assertEquals("'parseProtocolMessageIntoMethodCall' didn't work", "receiveWhatTimeIsItRequest", methodCallInfo.getMethod().getName());
		methodCallInfo = api.invokeAsynchronousRequestReceiverMethod(methodCallInfo);
		assertNotNull("Reflexive invokation of API method 'receiveWhatTimeIsItRequest' failed", methodCallInfo);
		assertEquals("'getAPIMethodInvocationInfo' didn't work", "informWhatTimeItIs", methodCallInfo.getMethod().getName());
		String answer = api.ivokeAsynchronousRequestAnsweringMethod(methodCallInfo);
		assertEquals("'ivokeAPIMethod' didn't work", api.informWhatTimeItIs(false), answer);
		methodCallInfo = api.parseProtocolMessageIntoMethodCall(answer);
		assertEquals("failed to complete the communication process test", "receiveWhatTimeItIsResponse", methodCallInfo.getMethod().getName());
		boolean ack = api.invokeAnswerReceiverMethod(methodCallInfo);
		assertTrue("The 'receiveWhatTimeItIsResponse' process promptly, not requiring another API method call, so it should return null", ack);
	}
	
	private void syncStreams(ByteArrayOutputStream out, byte[] inputContent, int[] inputContentCount) {
		byte[] outputContent = out.toByteArray();
System.out.println("Found on output stream: "+new String(outputContent));
		out.reset();
		System.arraycopy(outputContent, 0, inputContent, inputContentCount[0], outputContent.length);
		inputContentCount[0] += outputContent.length;
	}
	
	@Test
	public void customP2PServicesOverACommunicationChannelTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, IOException, InterruptedException {
		
		final byte[] peer1Buffer = new byte[40960];
		final byte[] peer2Buffer = new byte[40960];
		InputStream  peer1InputStream  = new InputStream() {
			int pos = 0;
			public int read() throws IOException {
				return peer1Buffer[pos++] & 0xff;
			}
			public int available() throws IOException {
				return 1;
			}
			
		};
		OutputStream peer1OutputStream = new OutputStream() {
			int pos = 0;
			public void write(int b) throws IOException {
				peer2Buffer[pos++] = (byte)(b & 0xff);
			}
			
		};
		InputStream  peer2InputStream  = new InputStream() {
			int pos = 0;
			public int read() throws IOException {
				return peer2Buffer[pos++] & 0xff;
			}
			public int available() throws IOException {
				return 1;
			}
			
		};
		OutputStream peer2OutputStream = new OutputStream() {
			int pos = 0;
			public void write(int b) throws IOException {
				peer1Buffer[pos++] = (byte)(b & 0xff);
			}
			
		};
				
		// puting the two to share the same channels
		CustomP2PServicesAPI servicesForPeer1 = new CustomP2PServicesAPI();
		CustomP2PServicesAPI servicesForPeer2 = new CustomP2PServicesAPI();
		P2PServicesNode peer1 = new P2PServicesNode(servicesForPeer1, peer1InputStream, peer1OutputStream);
		P2PServicesNode peer2 = new P2PServicesNode(servicesForPeer2, peer2InputStream, peer2OutputStream);

		peer1.executeAndSendLocalRequest("askWhatTimeIsIt", true);		// put a shit on the output stream
		peer2.getAndExecuteRemoteRequest();                       		// grab a shit from the input stream and leave an asynchronous shit on the pending execution queue
		peer2.executeAndSendNextAsynchronousRequestAnsweringMethod();	// execute the pending shit
		peer1.getAndExecuteRemoteRequest();								// get the pending shit execution output
	}
}
