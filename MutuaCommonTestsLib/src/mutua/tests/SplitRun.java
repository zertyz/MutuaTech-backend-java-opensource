package mutua.tests;

import java.util.ArrayList;

/** <pre>
 * SplitRun.java
 * =============
 * (created by luiz, Jul 25, 2015)
 *
 * Allows several threads to run, simultaneously, their tasks -- waiting for all of them
 * to finish. Made for profiling/tuning and reentrancy tests purposes.
 * 
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public abstract class SplitRun extends Thread {

	public static ArrayList<SplitRun> instances = new ArrayList<SplitRun>();
	
	public static void add(SplitRun instance) {
		instances.add(instance);
	}
	
	private static void reset() {
		instances.clear();
	}
	
	public static void runAndWaitForAll() throws InterruptedException {

		// run
		for (SplitRun instance : instances) {
			instance.start();
		}
		
		// wait
		for (SplitRun instance : instances) {
			synchronized (instance) {
				if (instance.running) {
					instance.wait();
				}
			}
		}
		
		// prepare for the next use
		reset();
	}
	
	private int arg;
	
	public SplitRun(int arg) {
		this.arg = arg;
	}

	/** This is the function that needs to be overridden to perform the work. When it returns, the work is considered done */
	public abstract void splitRun(int arg) throws Throwable;

	public boolean running = true;
	@Override
	public void run() {
		try {
			splitRun(arg);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		running = false;
		synchronized (this) {
			notify();
		}
	}
	
}
