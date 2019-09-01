package com.sc.cache.function;

/**
 * @author shpatel
 * 
 * This functional interface provision to define key for element
 */
@FunctionalInterface
public interface CacheValueExtractor<K, E> {
	/**
	 * @param key
	 * @return element
	 */
	E getValue(K key);

}
