package mutua.imi;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import mutua.imi.annotations.EClientMethods;
import mutua.imi.annotations.IndirectMethodId;
import mutua.imi.annotations.IndirectMethodIds;
import mutua.tests.SplitRun;

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

class SimpleClient {

	// although this method has the same name of 'EclientMethods.method1', it is found by the annotation property, which is immune to ProGuard
	@IndirectMethodId(EClientMethods.method1)
	public String method1(String callerId) {
		return "This was method1, called by " + callerId;
	}

	// this method is found by the annotation, regardless of it's name
	@IndirectMethodId(EClientMethods.method2)
	public String __method2(String callerId) {
		return "This was method2, called by " + callerId;
	}

	// this method will handle invocations of these two indirect methods
	@IndirectMethodIds({EClientMethods.method3, EClientMethods.method4})
	public String severalMethods(String callerId) {
		return "This serves methods 3 and 4 -- " + callerId;
	}
	
}

class PerformanceTestsClient {
	
	@IndirectMethodId(EClientMethods.method1)
	public void annotationValueMethod() {}

	@IndirectMethodIds({EClientMethods.method2, EClientMethods.method3, EClientMethods.method4})
	public void annotationArrayMethod() {}
}

class ReentrancyTestsClient {
	int numberOfMethod1Calls = 0;
	int numberOfMethod2Calls = 0;
	int numberOfMethod3Calls = 0;
	int numberOfMethod4Calls = 0;
	
	@IndirectMethodIds(EClientMethods.method1)
	public synchronized void m1() {
		numberOfMethod1Calls++;
	}
	
	@IndirectMethodIds(EClientMethods.method2)
	public synchronized void m2() {
		numberOfMethod2Calls++;
	}

	@IndirectMethodIds(EClientMethods.method3)
	public synchronized void m3() {
		numberOfMethod3Calls++;
	}

	@IndirectMethodIds(EClientMethods.method4)
	public synchronized void m4() {
		numberOfMethod4Calls++;
	}
}

public class IndirectMethodInvocationTests {		

	@Test
	public void testSimpleCalling() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IndirectMethodNotFoundException {
		
		IndirectMethodInvoker<EClientMethods> client1Invoker = new IndirectMethodInvoker<EClientMethods>(new SimpleClient(), EClientMethods.class, IndirectMethodId.class, IndirectMethodIds.class);
		
		IndirectMethodInvocationInfo<EClientMethods> method1Invocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method1, "hard coded invocation 1");
		IndirectMethodInvocationInfo<EClientMethods> method2Invocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method2, "hard coded invocation 2");
		IndirectMethodInvocationInfo<EClientMethods> method3Invocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method3, "hard coded invocation of method 3");
		IndirectMethodInvocationInfo<EClientMethods> method4Invocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method4, "hard coded invocation of method 4");
		
		String result1 = (String)client1Invoker.invokeMethod(method1Invocation);
		String result2 = (String)client1Invoker.invokeMethod(method2Invocation);
		String result3 = (String)client1Invoker.invokeMethod(method3Invocation);
		String result4 = (String)client1Invoker.invokeMethod(method4Invocation);
		
		assertEquals("Indirect Method Call 1 failed", "This was method1, called by hard coded invocation 1", result1);
		assertEquals("Indirect Method Call 2 failed", "This was method2, called by hard coded invocation 2", result2);
		assertEquals("Indirect Method Call 3 failed", "This serves methods 3 and 4 -- hard coded invocation of method 3", result3);
		assertEquals("Indirect Method Call 4 failed", "This serves methods 3 and 4 -- hard coded invocation of method 4", result4);

	}
	
	@Test
	// this test shows that annotating a method with a value or array does not impact on its performance, therefore,
	// user classes are advised to implements their annotations like 'IndirectMethodIds', which can be referenced by
	// either an array or value
	public void performanceMeasurements() throws IndirectMethodNotFoundException {
		System.out.println("Starting performance measurement for MutuaIMILib...");
		IndirectMethodInvoker<EClientMethods> performanceTestsInvoker = new IndirectMethodInvoker<EClientMethods>(new PerformanceTestsClient(), EClientMethods.class, IndirectMethodId.class, IndirectMethodIds.class);
		int samples = 100*1000*1000;
		
		IndirectMethodInvocationInfo<EClientMethods> annotationValueInvocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method1);
		IndirectMethodInvocationInfo<EClientMethods> annotationArrayInvocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method2);
		
		// annotation value measurements
		long start = System.currentTimeMillis();
		for (int i=0; i<samples; i++) {
			performanceTestsInvoker.invokeMethod(annotationValueInvocation);
		}
		long finish = System.currentTimeMillis();
		System.out.println("Annotation Value based invocation: "+samples+" times in "+(finish-start)+"ms: "+samples/(finish-start)+"/ms");

		// annotation array measurements
		start = System.currentTimeMillis();
		for (int i=0; i<samples; i++) {
			performanceTestsInvoker.invokeMethod(annotationArrayInvocation);
		}
		finish = System.currentTimeMillis();
		System.out.println("Annotation Array based invocation: "+samples+" times in "+(finish-start)+"ms: "+samples/(finish-start)+"/ms");
	}
	
	@Test
	public void reentrancyTest() throws IndirectMethodNotFoundException, InterruptedException {
		ReentrancyTestsClient client = new ReentrancyTestsClient();
		final IndirectMethodInvoker<EClientMethods> reentrancyTests = new IndirectMethodInvoker<EClientMethods>(client, EClientMethods.class, IndirectMethodIds.class);
		final IndirectMethodInvocationInfo<EClientMethods> method1Invocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method1);
		final IndirectMethodInvocationInfo<EClientMethods> method2Invocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method2);
		final IndirectMethodInvocationInfo<EClientMethods> method3Invocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method3);
		final IndirectMethodInvocationInfo<EClientMethods> method4Invocation = new IndirectMethodInvocationInfo<EClientMethods>(EClientMethods.method4);

		int numberOfThreads = 16;
		int numberOfLoops   = 1*1000*1000;
		for (int i=0; i<numberOfThreads; i++) {
			SplitRun.add(new SplitRun(numberOfLoops) {
				@Override
				public void splitRun(int numberOfLoops) throws Throwable {
					for (int i=0; i<numberOfLoops; i++) {
						reentrancyTests.invokeMethod(method1Invocation);
						reentrancyTests.invokeMethod(method2Invocation);
						reentrancyTests.invokeMethod(method3Invocation);
						reentrancyTests.invokeMethod(method4Invocation);
					}
				}
			});
		}
		SplitRun.runAndWaitForAll();
		
		assertEquals("Number of invocations to method 1 is wrong", numberOfLoops*numberOfThreads, client.numberOfMethod1Calls);
		assertEquals("Number of invocations to method 2 is wrong", numberOfLoops*numberOfThreads, client.numberOfMethod2Calls);
		assertEquals("Number of invocations to method 3 is wrong", numberOfLoops*numberOfThreads, client.numberOfMethod3Calls);
		assertEquals("Number of invocations to method 4 is wrong", numberOfLoops*numberOfThreads, client.numberOfMethod4Calls);
	}

}
