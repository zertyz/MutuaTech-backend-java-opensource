package adapters;

import org.h2.mvstore.MVStore;

/** MVStoreAdapter.java
 * ====================
 * (created by luiz, Aug 31, 2018)
 *
 * Single point to access H2's MVStore and provide an application-wide
 * configuration and instantiation point of the library.
 * 
 * NOTE: performance tips when working with MVStore:
 *  1) Serializing arrays of objects are faster than serializing java objects
 *  2) More information in 'H2MVStoreSpikes.java'
 * 
 * @author luiz
*/

public class MVStoreAdapter {
	
	public static final String MVStoreVersion = "1.4.197";	// as linked in the project's libraries
	
	// Mutua Configurable Class pattern
	///////////////////////////////////
	
	/** this class' singleton instance */
	private static MVStoreAdapter instance = null;
	
	public enum    EMVStoreCompressors {NONE, DEFAULT, HIGH};
	// MVStore mapped settings to be used for new instances
	private static EMVStoreCompressors compressor = EMVStoreCompressors.HIGH;
	private static int    cacheSize               = 256;
	private static int    cacheConcurrency        = 4;
	private static int    autoCommitBufferSize    = 4*1024;
	private static int    autoCommitDelay         = 15*1000;
	private static int    autoCompactFillRate     = 50;
	private static int    pageSplitSize           = 4*4096;
	/** The complete file path on which MVStore will persist the data */
	private static String FILE_NAME               = null;
	
	private MVStore store = null;
	
	/** method to be called when attempting to configure the singleton for new instances of 'MVStore'.
	 *  @param fileName see {@link #FILE_NAME} */
	public static void configureDefaultValuesForNewInstances(String fileName) {
		FILE_NAME = fileName;
		instance = null;
	}

	
	private MVStoreAdapter() {
		MVStore.Builder builder = new MVStore.Builder().fileName(FILE_NAME).
				                                        cacheSize(cacheSize).
				                                        cacheConcurrency(cacheConcurrency).
				                                        autoCommitBufferSize(autoCommitBufferSize).
				                                        autoCompactFillRate(autoCompactFillRate).
				                                        pageSplitSize(pageSplitSize);
		// compressor
		switch (compressor) {
			case DEFAULT:
				builder = builder.compress();
				break;
			case HIGH:
				builder = builder.compressHigh();
				break;
			default:
				break;
		}
		
		store = builder.open();
		
		store.setAutoCommitDelay(autoCommitDelay);
	}
	
	private static MVStoreAdapter getInstance() {
		if (instance == null) {
			instance = new MVStoreAdapter();
		}
		return instance;
	}

	
	// public methods
	/////////////////

	/** Callers are responsible for closing the store -- maybe MVStore has a shutdown hook that might close all opened stores... */
	public static MVStore getStore() {
		return getInstance().store;
	}

}
