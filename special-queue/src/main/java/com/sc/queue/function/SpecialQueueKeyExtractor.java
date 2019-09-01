package com.sc.queue.function;

/**
 * @author shpatel
 * 
 * This functional interface provision to define key for element
 */
@FunctionalInterface
public interface SpecialQueueKeyExtractor<K, E> {
	/**
	 * @param element
	 * @return Key
	 */
	K getKey(E element);

}
