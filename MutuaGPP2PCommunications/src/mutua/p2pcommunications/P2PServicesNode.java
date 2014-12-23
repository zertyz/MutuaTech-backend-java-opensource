package mutua.p2pcommunications;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mutua.p2pcommunications.model.P2PServicesAPIMethodCallInfo;

/** <pre>
 * P2PServicesNode.java
 * ====================
 * (created by luiz, Dec 22, 2014)
 *
 * Class to manage input and output for a service API instance
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class P2PServicesNode {

	private final P2PServicesAPI services;
	private final InputStream  inputStream;
	private final OutputStream outputStream;

	
	private void populate(byte[] binaryProtocolMessage) throws IOException {
		int offset = 0;
		int remainingLength = binaryProtocolMessage.length;
		while (true) {
			if (remainingLength == 0) {
				return ;
			}
			int bytesRead = inputStream.read(binaryProtocolMessage, offset, remainingLength);
			if (bytesRead > 0) {
				offset += bytesRead;
				remainingLength -= bytesRead;
			} else if (bytesRead == 0) {
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			} else {
				throw new RuntimeException("Premature end of input stream");
			}
		}
	}

	
	public P2PServicesNode(P2PServicesAPI services,	InputStream inputStream, OutputStream outputStream) {
		this.services    = services;
		this.inputStream  = inputStream;
		this.outputStream = outputStream;
	}

	public void executeAndSendLocalRequest(String servicesAPIMethodName, Object... parameters) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, IOException {
		P2PServicesAPIMethodCallInfo requestMethodCallInfo = services.getAPIMethodInvocationInfo(servicesAPIMethodName, parameters);
		String protocolMessage = services.invokeRequestMethod(requestMethodCallInfo);
		byte[] binaryProtocolMessage = protocolMessage.getBytes();
		DataOutputStream dout = new DataOutputStream(outputStream);
		dout.writeInt(binaryProtocolMessage.length);
		outputStream.write(binaryProtocolMessage);
		outputStream.flush();
System.out.println("wrote: "+binaryProtocolMessage.length+"|"+protocolMessage);
	}
	
	public boolean getAndExecuteRemoteRequest() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (inputStream.available() == 0) {
			return false;
		}
		DataInputStream din = new DataInputStream(inputStream);
		int protocolMessageLength = din.readInt();
		byte[] binaryProtocolMessage = new byte[protocolMessageLength];
		populate(binaryProtocolMessage);
		String protocolMessage = new String(binaryProtocolMessage);
System.out.println("read: "+protocolMessage.length()+"|"+protocolMessage);
		P2PServicesAPIMethodCallInfo methodCallInfo = services.parseProtocolMessageIntoMethodCall(protocolMessage);
		
		Method method = methodCallInfo.getMethod();
		if (method.isAnnotationPresent(P2PServicesAPIAsynchronousRequestReceiverMethod.class)) {
			P2PServicesAPIMethodCallInfo async = services.invokeAsynchronousRequestReceiverMethod(methodCallInfo);
			return services.scheduleAsynchronousRequestAnsweringMethodForExecution(async);
		} else if (method.isAnnotationPresent(P2PServicesAPIAnswerReceiverMethod.class)) {
			return services.invokeAnswerReceiverMethod(methodCallInfo);
		} else {
			//String protocolResponseMessage = services.ivokeSynchronousRequestAnsweringMethod(methodCallInfo);
			return false;
		}
	}
	
	public boolean executeAndSendNextAsynchronousRequestAnsweringMethod() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException {
		String protocolMessage = services.executeNextScheduledAsynchronousRequestAnsweringMethod();
		byte[] binaryProtocolMessage = protocolMessage.getBytes();
		DataOutputStream dout = new DataOutputStream(outputStream);
		dout.writeInt(binaryProtocolMessage.length);
		outputStream.write(binaryProtocolMessage);
		outputStream.flush();
System.out.println("wrote: "+binaryProtocolMessage.length+"|"+protocolMessage);
		return true;
	}

}
