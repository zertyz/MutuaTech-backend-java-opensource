package mutua.tests;

/** <pre>
 * DatabaseAlgorithmAnalysis.java
 * ==============================
 * (created by luiz, Jul 28, 2015)
 *
 * Measures the complexity of (database) algorithms.
 * 
 * As a reference of how to use this class, please see 'ISubscriptionDBPerformanceTests'
 * 
 * Algorithm analysis:
 * 
 * Inserts:
 * 
 * (^t)     n      t(1)
 * 108     100	   1.08
 * 112     100	   1.12
 * 5632    100	   56.32
 * 16772   100	   167.72
 * 410     100	   4.1
 * 560     100	   5.6
 *                 
 * 222     200     1.11
 * 223     200     1.115
 * 22408   200     112.04
 * 66937   200     334.685
 * 960     200     4.8
 * 1292	   200     6.46
 *                 
 * 1115    1000	   1.115
 * 1119    1000	   1.119
 * 55737   1000	   557.377
 * 167125  1000	   1671.255
 * 6659    1000	   6.659
 * 8023    1000	   8.023
 * 
 * Inserts of n elements (p threads):
 * O(1)      = t2(1)/t1(1) / 1 ~= 1
 * O(n)      = t2(1)/t1(1) / 3 ~= 1
 * O(log(n)) = t2(1)/t1(1) / log(n*3)/log(n) ~= 1 +/- 10%
 *
 * Updates:
 * (^t)    p         n      u       t
 * 112	1	1000	100	1.12
 * 111381	1	1000	100	1113.81
 * 782	1	1000	100	7.82
 * 
 * 56	2	1000	100	0.56
 * 55599	2	1000	100	555.99
 * 393	2	1000	100	3.93
 * 
 * 37	3	1000	100	0.37
 * 36590	3	1000	100	365.9
 * 259	3	1000	100	2.59
 * 
 * 29	4	1000	100	0.29
 * 27870	4	1000	100	278.7
 * 198	4	1000	100	1.98
 * 
 * 107	1	10000	100	1.07
 * 1113457	1	10000	100	11134.57
 * 1006	1	10000	100	10.06
 * 
 * 56	2	10000	100	0.56
 * 555999	2	10000	100	5559.99
 * 504	2	10000	100	5.04
 * 
 * 38	3	10000	100	0.38
 * 365792	3	10000	100	3657.92
 * 329	3	10000	100	3.29
 * 
 * 28	4	10000	100	0.28
 * 278431	4	10000	100	2784.31
 * 258	4	10000	100	2.58
 * 
 * 1336	1	100000	100	13.36
 * 
 * Updates:
 * O(1)     = t2 / t1                       ~= 1 for any p
 * O(n)     = t2 / t1  /  (n2 / n1)         ~= 1 for any p
 * O(log n) = t2 / t1  /  log(n2) / log(n1) ~= 1 for any p
 * 
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public abstract class DatabaseAlgorithmAnalysis {
	
	private final int numberOfThreads;
	
	private final boolean shouldPerformWarmUp;
	private final boolean shouldPerformInsertTests;
	private final boolean shouldPerformUpdateTests;
	private final boolean shouldPerformSelectTests;
	private final boolean shouldPerformDeleteTests;
	
	private final int inserts;
	private final int _perThreadInsertAttempts;
	private long insertStart1, insertEnd1, insertStart2, insertEnd2;
	
	private final int updates;
	private final int numberOfFirstPassUpdateElements;
	private final int numberOfSecondPassUpdateElements;
	private final int _perThreadUpdates;
	private long updateStart1, updateEnd1, updateStart2, updateEnd2;
	
	private final int selects;
	private final int numberOfFirstPassSelectElements;
	private final int numberOfSecondPassSelectElements;
	private final int _perThreadSelects;
	private long selectStart1, selectEnd1, selectStart2, selectEnd2;
	
	private final int deletes;
	private final int numberOfFirstPassDeleteElements;
	private final int numberOfSecondPassDeleteElements;
	private final int _perThreadDeletes;
	private long deleteStart1, deleteEnd1, deleteStart2, deleteEnd2;
	
	/** Runs a Insert, Update, Select algorithm analysis test, with the possibility of not running the Warmup phase */
	public DatabaseAlgorithmAnalysis(String testName, boolean performWarmUp, int numberOfThreads, int inserts, int updates, int selects) throws Throwable {

		this.numberOfThreads          = numberOfThreads;
		
		this.shouldPerformInsertTests = inserts != -1 ? true : false;
		this.shouldPerformWarmUp      = this.shouldPerformInsertTests ? performWarmUp : false;
		this.shouldPerformUpdateTests = updates != -1 ? true : false;
		this.shouldPerformSelectTests = selects != -1 ? true : false;
		this.shouldPerformDeleteTests = false;
		
		this.inserts                  = inserts;
		this._perThreadInsertAttempts = inserts / numberOfThreads;
		
		this.updates                          = updates;
		this.numberOfFirstPassUpdateElements  = inserts;
		this.numberOfSecondPassUpdateElements = inserts*2;
		this._perThreadUpdates                = updates / numberOfThreads; 
		
		this.selects                          = selects;
		this.numberOfFirstPassSelectElements  = inserts;
		this.numberOfSecondPassSelectElements = inserts*2;
		this._perThreadSelects                = selects / numberOfThreads;
		
		this.deletes                          = -1;
		this.numberOfFirstPassDeleteElements  = -1;
		this.numberOfSecondPassDeleteElements = -1;
		this._perThreadDeletes                = -1;
		
		System.err.print(testName + " Algorithm Analisys: "); System.err.flush();
		analyse();
	}
	
	/** Runs a Insert, Update, Select algorithm analysis test */
	public DatabaseAlgorithmAnalysis(String testName, int numberOfThreads, int inserts, int updates, int selects) throws Throwable {
		this(testName, true, numberOfThreads, inserts, updates, selects);
	}

	/** Runs a Insert & Select algorithm analysis test */
	public DatabaseAlgorithmAnalysis(String testName, int numberOfThreads, int inserts, int selects) throws Throwable {
		this(testName, numberOfThreads, inserts, -1, selects);
	}

	
	public abstract void resetTables() throws Throwable;

	public void insertLoopCode(int i)  throws Throwable {
		throw new RuntimeException("If you want your algorithm analysis test to test INSERTs, you must override 'insertLoopCode'");
	}

	public void updateLoopCode(int i)  throws Throwable {
		throw new RuntimeException("If you want your algorithm analysis test to test UPDATEs, you must override 'updateLoopCode'");
	}

	public void selectLoopCode(int i)  throws Throwable {
		throw new RuntimeException("If you want your algorithm analysis test to test SELECTs, you must override 'selectLoopCode'");
	}
	
	public void deleteLoopCode(int i)  throws Throwable {
		throw new RuntimeException("If you want your algorithm analysis test to test DELETEs, you must override 'deleteLoopCode'");
	}
	
	private void analyse() throws Throwable {

		// WARMUP
		if (shouldPerformWarmUp) {
			System.gc(); System.runFinalization(); System.gc();
			System.err.print("Warm Up; "); System.err.flush();
			resetTables();
			for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
				SplitRun.add(new SplitRun(threadNumber) {
					public void splitRun(int threadNumber) throws Throwable {
						for (int i=_perThreadInsertAttempts*threadNumber; i<_perThreadInsertAttempts*(threadNumber+1)/100; i++) {
							insertLoopCode(i);
						}
					}
				});
			}
			SplitRun.runAndWaitForAll();
		}
		
		resetTables();		
		System.err.print("First Pass ( "); System.err.flush();
		
		// INSERTS (first pass)
		if (shouldPerformInsertTests) {
			System.gc(); System.runFinalization(); System.gc();
			System.err.print("Insert "); System.err.flush();
			insertStart1 = System.currentTimeMillis();
			for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
				SplitRun.add(new SplitRun(threadNumber) {
					public void splitRun(int threadNumber) throws Throwable {
						for (int i=_perThreadInsertAttempts*threadNumber; i<_perThreadInsertAttempts*(threadNumber+1); i++) {
							insertLoopCode(i);
						}
					}
				});
			}
			SplitRun.runAndWaitForAll();
			insertEnd1 = System.currentTimeMillis();
		}
		
		// UPDATES (first pass)
		if (shouldPerformUpdateTests) {
			System.gc(); System.runFinalization(); System.gc();
			System.err.print("Update "); System.err.flush();
			updateStart1 = System.currentTimeMillis();
			for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
				SplitRun.add(new SplitRun(threadNumber) {
					public void splitRun(int threadNumber) throws Throwable {
						for (int i=_perThreadUpdates*threadNumber; i<_perThreadUpdates*(threadNumber+1); i++) {
							updateLoopCode(i);
						}
					}
				});
			}
			SplitRun.runAndWaitForAll();
			updateEnd1 = System.currentTimeMillis();
		}

		// SELECTS (first pass)
		if (shouldPerformSelectTests) {
			System.gc(); System.runFinalization(); System.gc();
			System.err.print("Select "); System.err.flush();
			selectStart1 = System.currentTimeMillis();
			for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
				SplitRun.add(new SplitRun(threadNumber) {
					public void splitRun(int threadNumber) throws Throwable {
						for (int i=_perThreadSelects*threadNumber; i<_perThreadSelects*(threadNumber+1); i++) {
							selectLoopCode(i);
						}
					}
				});
			}
			SplitRun.runAndWaitForAll();
			selectEnd1 = System.currentTimeMillis();
		}

		System.err.print("); Second Pass ( "); System.err.flush();
		
		// INSERTS (second pass)
		if (shouldPerformInsertTests) {
			System.gc(); System.runFinalization(); System.gc();
			System.err.print("Insert "); System.err.flush();
			insertStart2 = System.currentTimeMillis();
			for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
				SplitRun.add(new SplitRun(threadNumber) {
					public void splitRun(int threadNumber) throws Throwable {
						for (int i=_perThreadInsertAttempts*threadNumber; i<_perThreadInsertAttempts*(threadNumber+1); i++) {
							insertLoopCode(inserts+i);
						}
					}
				});
			}
			SplitRun.runAndWaitForAll();
			insertEnd2 = System.currentTimeMillis();
		}
				
		// UPDATES (second pass)
		if (shouldPerformUpdateTests) {
			System.gc(); System.runFinalization(); System.gc();
			System.err.print("Update "); System.err.flush();
			updateStart2 = System.currentTimeMillis();
			for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
				SplitRun.add(new SplitRun(threadNumber) {
					public void splitRun(int threadNumber) throws Throwable {
						for (int i=_perThreadUpdates*threadNumber; i<_perThreadUpdates*(threadNumber+1); i++) {
							updateLoopCode(inserts+i);
						}
					}
				});
			}
			SplitRun.runAndWaitForAll();
			updateEnd2 = System.currentTimeMillis();
		}

		// SELECTS (second pass)
		if (shouldPerformSelectTests) {
			System.gc(); System.runFinalization(); System.gc();
			System.err.print("Select "); System.err.flush();
			selectStart2 = System.currentTimeMillis();
			for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
				SplitRun.add(new SplitRun(threadNumber) {
					public void splitRun(int threadNumber) throws Throwable {
						for (int i=_perThreadSelects*threadNumber; i<_perThreadSelects*(threadNumber+1); i++) {
							selectLoopCode(inserts+i);
						}
					}
				});
			}
			SplitRun.runAndWaitForAll();
			selectEnd2 = System.currentTimeMillis();
		}

		System.err.println(")."); System.err.flush();
		
		if (shouldPerformInsertTests) {
			computeInsertAlgorithmAnalysis(insertStart1, insertEnd1, insertStart2, insertEnd2, inserts);
		}
		if (shouldPerformUpdateTests) {
			computeUpdateOrSelectAlgorithmAnalysis("Update", updateStart1, updateEnd1, updateStart2, updateEnd2, numberOfFirstPassUpdateElements, numberOfSecondPassUpdateElements, updates);
		}
		if (shouldPerformSelectTests) {
			computeUpdateOrSelectAlgorithmAnalysis("Select", selectStart1, selectEnd1, selectStart2, selectEnd2, numberOfFirstPassSelectElements, numberOfSecondPassSelectElements, selects);
		}
		
	}
	
	public enum EAlgorithmComplexity {BetterThanO1, O1, Ologn, BetweenOLogNAndOn, On, WorseThanOn;
		public String toString() {
			switch (this) {
				case BetterThanO1:
					return "Better than O(1) -- aren't the machines idle or is there too little RAM?";
				case O1:
					return "O(1)";
				case Ologn:
					return "O(log(n))";
				case BetweenOLogNAndOn:
					return "Worse than O(log(n)), but better than O(n)";
				case On:
					return "O(n)";
				case WorseThanOn:
					return "Worse than O(n)";
				default:
					return "unpredicted algorithm complexity -- update 'DatabaseAlgorithmAnalysis' source code to account for such case";
			}
		}
	};
	
	/** Performs the algorithm analysis for a massive database insert operation.
	 * To perform the analysis, two insertions of 'n' elements must be done on an empty database.
	 * start 1 & 2 and end 1 & 2 are measurement times, takem from System.currentTimeMillis().
	 * The returned algorithm complexity is an indication of the time taken to insert one more element
	 * on a table containing n elements, where O is the constant of proportionality -- 
	 * the average time to insert 1 element on an empty table */
	public static EAlgorithmComplexity computeInsertAlgorithmAnalysis(long start1, long end1, long start2, long end2, int n) {
		
		// the acceptable percent measurement error when computing complexity
		double percentError = 0.15;
		
		long deltaT1 = end1 - start1;
		long deltaT2 = end2 - start2;
		double t1 = ((double)deltaT1) / ((double)n);
		double t2 = ((double)deltaT2) / ((double)n);
		
		EAlgorithmComplexity complexity;
		
		// sanity check
		if (((t1/t2) - ((double)1)) >= percentError) {
			complexity = EAlgorithmComplexity.BetterThanO1;
		} else
		// test for O(1) -- t2/t1 ~= 1
		if ( Math.abs((t2/t1) - ((double)1)) < percentError ) {
			complexity = EAlgorithmComplexity.O1;
		} else		
		// test for O(log(n)) -- (t2/t1) / (log(n*3)/log(n)) ~= 1
		if ( Math.abs( ((t2/t1) / (Math.log(n*3)/Math.log(n))) - ((double)1) ) < percentError ) {
			complexity = EAlgorithmComplexity.Ologn;
		} else 
		// test for O(n) -- t2/t1 / 3 ~= 1
		if ( Math.abs( ((t2/t1)/3) - ((double)1)) < percentError ) {
			complexity = EAlgorithmComplexity.On;
		} else
		// test for worse than O(n)
		if ( ( ((t2/t1)/3) - ((double) 1) ) > percentError ) {
			complexity = EAlgorithmComplexity.WorseThanOn;
		} else {
			complexity = EAlgorithmComplexity.BetweenOLogNAndOn;
		}
		
		System.err.println("Insert algorithm analysis:");
		System.err.println("   (^t)\t\tn\tt(1)");
		System.err.println("1: "+deltaT1+(deltaT1>9999 ? "":"\t")+"\t"+n+"\t"+t1);
		System.err.println("2: "+deltaT2+(deltaT2>9999 ? "":"\t")+"\t"+n*2+"\t"+t2);
		System.err.println("--> "+complexity);
		
		return complexity;
	}
	
	/** Performs the algorithm analysis for a massive database update/select operation.
	 * To perform the analysis, two batch updates/selects of r rows must be done on a table at different times.
	 * On the first time, the database must have n1 rows and on the second time, n2 rows -- n2 must be at least twice n1.
	 * r should be so that end-start can be accurately measured.
	 * start 1 & 2 and end 1 & 2 are measurement times, taken from System.currentTimeMillis().
	 * The returned algorithm complexity is an indication of the time taken to update/select one element
	 * on a table containing n elements, where O is the constant of proportionality -- 
	 * the average time to update/select 1 element on an single row table */
	public static EAlgorithmComplexity computeUpdateOrSelectAlgorithmAnalysis(String operation, long start1, long end1, long start2, long end2, int n1, int n2, int r) {
		
		// the acceptable percent measurement error when computing complexity
		double percentError = 0.15;
		
		long deltaT1 = end1 - start1;
		long deltaT2 = end2 - start2;
		double t1 = ((double)deltaT1) / ((double)r);
		double t2 = ((double)deltaT2) / ((double)r);
		
		EAlgorithmComplexity complexity;
		
		// sanity check
		if (((t1/t2) - ((double)1)) >= percentError) {
			complexity = EAlgorithmComplexity.BetterThanO1;
		} else
		// test for O(1) -- t2/t1 ~= 1
		if ( Math.abs((t2/t1) - ((double)1)) < percentError ) {
			complexity = EAlgorithmComplexity.O1;
		} else		
		// test for O(log(n)) -- (t2/t1) / (log(n2)/log(n1)) ~= 1
		if ( Math.abs( ((t2/t1) / (Math.log(n2)/Math.log(n1))) - ((double)1) ) < percentError ) {
			complexity = EAlgorithmComplexity.Ologn;
		} else 
		// test for O(n) -- (t2/t1) / (n2/n1) ~= 1
		if ( Math.abs( ((t2/t1) / (n2/n1)) - ((double)1) ) < percentError ) {
			complexity = EAlgorithmComplexity.On;
		} else
		// test for worse than O(n)
		if ( ( ((t2/t1) / (n2/n1)) - ((double)1) ) > percentError ) {
			complexity = EAlgorithmComplexity.WorseThanOn;
		} else {
			complexity = EAlgorithmComplexity.BetweenOLogNAndOn;
		}
		
		System.err.println(operation + " algorithm analysis:");
		System.err.println("   (^t)\t\tn\tr\tt(1)");
		System.err.println("1: "+deltaT1+(deltaT1>9999 ? "":"\t")+"\t"+n1+"\t"+r+"\t"+t1);
		System.err.println("2: "+deltaT2+(deltaT2>9999 ? "":"\t")+"\t"+n2+"\t"+r+"\t"+t2);
		System.err.println("--> "+complexity);
		
		return complexity;
	}

}

//TEST METHODS
///////////////

//@Test
//public void testInsertion() throws InterruptedException {
//	
//	int inserts = 100;
//	int numberOfThreads = 1;
//	final int _perThreadAttempts = inserts / numberOfThreads;
//	long start1, end1, start2, end2;
//	final int[] count = {0};
//	
//	// O(1) inserts
//	///////////////
//	
//	start1 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadAttempts*_threadNumber; i<_perThreadAttempts*(_threadNumber+1); i++) {
//					try {sleep(1);} catch (Throwable t) {}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end1 = System.currentTimeMillis();
//	
//	start2 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadAttempts*_threadNumber; i<_perThreadAttempts*(_threadNumber+1); i++) {
//					try {sleep(1);} catch (Throwable t) {}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end2 = System.currentTimeMillis();
//	
//	SplitRun.computeInsertAlgorithmAnalysis(start1, end1, start2, end2, inserts);
//
//	// O(n) inserts
//	///////////////
//
//	count[0] = 0;
//	start1 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadAttempts*_threadNumber; i<_perThreadAttempts*(_threadNumber+1); i++) {
//					count[0]++;
//					for (int j=0; j<count[0]; j++) {
//						try {sleep(1);} catch (Throwable t) {}
//					}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end1 = System.currentTimeMillis();
//	
//	start2 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadAttempts*_threadNumber; i<_perThreadAttempts*(_threadNumber+1); i++) {
//					count[0]++;
//					for (int j=0; j<count[0]; j++) {
//						try {sleep(1);} catch (Throwable t) {}
//					}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end2 = System.currentTimeMillis();
//
//	SplitRun.computeInsertAlgorithmAnalysis(start1, end1, start2, end2, inserts);
//
//	// O(log(n)) inserts
//	////////////////////
//
//	count[0] = 0;
//	start1 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadAttempts*_threadNumber; i<_perThreadAttempts*(_threadNumber+1); i++) {
//					count[0]++;
//					int _count = (int)Math.round(Math.log(count[0]));
//					for (int j=0; j<_count; j++) {
//						try {sleep(1);} catch (Throwable t) {}
//					}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end1 = System.currentTimeMillis();
//	
//	start2 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadAttempts*_threadNumber; i<_perThreadAttempts*(_threadNumber+1); i++) {
//					count[0]++;
//					int _count = (int)Math.round(Math.log(count[0]));
//					for (int j=0; j<_count; j++) {
//						try {sleep(1);} catch (Throwable t) {}
//					}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end2 = System.currentTimeMillis();
//
//	SplitRun.computeInsertAlgorithmAnalysis(start1, end1, start2, end2, inserts);
//
//}
//
//@Test
//public void testUpdation() throws InterruptedException {
//	final int updates = 30;
//	final int numberOfFirstPassElements  = 500;
//	final int numberOfSecondPassElements = 2000;
//	final int numberOfThreads = 1;
//	final int _perThreadUpdates = updates / numberOfThreads;
//	long start1, end1, start2, end2;
//	
//	// O(1) updates
//	///////////////
//	
//	start1 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadUpdates*_threadNumber; i<_perThreadUpdates*(_threadNumber+1); i++) {
//					try {sleep(1);} catch (Throwable t) {}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end1 = System.currentTimeMillis();
//
//	start2 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadUpdates*_threadNumber; i<_perThreadUpdates*(_threadNumber+1); i++) {
//					try {sleep(1);} catch (Throwable t) {}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end2 = System.currentTimeMillis();
//
//	SplitRun.computeUpdateAlgorithmAnalysis(start1, end1, start2, end2, numberOfFirstPassElements, numberOfSecondPassElements, updates);
//
//	// O(n) updates
//	///////////////
//
//	start1 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadUpdates*_threadNumber; i<_perThreadUpdates*(_threadNumber+1); i++) {
//					for (int j=0; j<numberOfFirstPassElements; j++) {
//						try {sleep(1);} catch (Throwable t) {}
//					}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end1 = System.currentTimeMillis();
//
//	start2 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadUpdates*_threadNumber; i<_perThreadUpdates*(_threadNumber+1); i++) {
//					for (int j=0; j<numberOfSecondPassElements; j++) {
//						try {sleep(1);} catch (Throwable t) {}
//					}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end2 = System.currentTimeMillis();
//
//	SplitRun.computeUpdateAlgorithmAnalysis(start1, end1, start2, end2, numberOfFirstPassElements, numberOfSecondPassElements, updates);
//
//	// O(log(n)) updates
//	////////////////////
//
//	start1 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadUpdates*_threadNumber; i<_perThreadUpdates*(_threadNumber+1); i++) {
//					int _count = (int)Math.round(Math.log(numberOfFirstPassElements));
//					for (int j=0; j<_count; j++) {
//						try {sleep(2);} catch (Throwable t) {}
//					}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end1 = System.currentTimeMillis();
//
//	start2 = System.currentTimeMillis();
//	for (int threadNumber=0; threadNumber<numberOfThreads; threadNumber++) {
//		final int _threadNumber = threadNumber;
//		SplitRun.add(new SplitRun() {
//			public void splitRun() throws SQLException {
//				for (int i=_perThreadUpdates*_threadNumber; i<_perThreadUpdates*(_threadNumber+1); i++) {
//					int _count = (int)Math.round(Math.log(numberOfSecondPassElements));
//					for (int j=0; j<_count; j++) {
//						try {sleep(2);} catch (Throwable t) {}
//					}
//				}
//			}
//		});
//	}
//	SplitRun.runAndWaitForAll();
//	end2 = System.currentTimeMillis();
//
//	SplitRun.computeUpdateAlgorithmAnalysis(start1, end1, start2, end2, numberOfFirstPassElements, numberOfSecondPassElements, updates);
//}
