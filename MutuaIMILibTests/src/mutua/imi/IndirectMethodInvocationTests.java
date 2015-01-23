package mutua.imi;

import java.lang.reflect.InvocationTargetException;

import mutua.imi.annotations.IndirectMethodId;
import mutua.imi.annotations.IndirectMethodIds;

import org.junit.Test;

import static org.junit.Assert.*;

/** <pre>
 * IndirectMethodInvocationTests.java
 * ==================================
 * (created by luiz, Jan 23, 2015)
 *
 * Test the indirect method calling library
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

enum Client1Methods {
	method1,
	method2,
	method3,
	method4,
}

class Client1 {

	// this method is found by name
	public String method1(String callerId) {
		return "This was method1, called by " + callerId;
	}

	// this method is found by the annotation
	@IndirectMethodId("method2")
	public String __method2(String callerId) {
		return "This was method2, called by " + callerId;
	}
	
	@IndirectMethodIds({"method3", "method4"})
	public String severalMethods(String callerId) {
		return "This serves methods 3 and 4 -- " + callerId;
	}
	
}

public class IndirectMethodInvocationTests {		

	@Test
	public void testSimpleCalling() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IndirectMethodNotFoundException {
		
		IndirectMethodInvoker<Client1Methods> client1Invoker = new IndirectMethodInvoker<Client1Methods>(new Client1(), Client1Methods.class, IndirectMethodId.class, IndirectMethodIds.class);
		
		IndirectMethodInvocationInfo<Client1Methods> method1Invocation = new IndirectMethodInvocationInfo<Client1Methods>(Client1Methods.method1, "hard coded invocation 1");
		IndirectMethodInvocationInfo<Client1Methods> method2Invocation = new IndirectMethodInvocationInfo<Client1Methods>(Client1Methods.method2, "hard coded invocation 2");
		IndirectMethodInvocationInfo<Client1Methods> method3Invocation = new IndirectMethodInvocationInfo<Client1Methods>(Client1Methods.method3, "hard coded invocation of method 3");
		IndirectMethodInvocationInfo<Client1Methods> method4Invocation = new IndirectMethodInvocationInfo<Client1Methods>(Client1Methods.method4, "hard coded invocation of method 4");
		
		String result1 = (String)client1Invoker.invokeMethod(method1Invocation);
		String result2 = (String)client1Invoker.invokeMethod(method2Invocation);
		String result3 = (String)client1Invoker.invokeMethod(method3Invocation);
		String result4 = (String)client1Invoker.invokeMethod(method4Invocation);
		
		assertEquals("Indirect Method Call 1 failed", "This was method1, called by hard coded invocation 1", result1);
		assertEquals("Indirect Method Call 2 failed", "This was method2, called by hard coded invocation 2", result2);
		assertEquals("Indirect Method Call 3 failed", "This serves methods 3 and 4 -- hard coded invocation of method 3", result3);
		assertEquals("Indirect Method Call 4 failed", "This serves methods 3 and 4 -- hard coded invocation of method 4", result4);

	}

}
