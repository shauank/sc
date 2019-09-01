package com.sc.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sc.cache.function.CacheValueExtractor;

/**
 * @author shpatel
 *
 * @param <K> Type of Key
 * @param <V> Type of Value
 * 
 * This class provide blocking/non-blocking cache implementation based on key. 
 */
public class CacheImpl<K, V> implements Cache<K, V> {

	private Map<K, V> map = new ConcurrentHashMap<K, V>();
	private CacheValueExtractor<K, V> valueExtractor;
	public Map<K, Integer> counterMap;
	Logger logger = LoggerFactory.getLogger(CacheImpl.class);

	public CacheImpl(CacheValueExtractor<K, V> valueExtractor) {
		this.valueExtractor = valueExtractor;
		this.counterMap = new ConcurrentHashMap<K, Integer>();
	}

	/**
	 * @return value for given @param key
	 * Returns value for given key. This method gives thread safe operation for given key. If multiple threads calling this method then only one 
	 * will execute and remaining will wait for first to finish. However, if thread with different @param key comes execution then it perform 
	 * concurrent operation.
	 */
	public V get(K key) {
		logger.debug(String.format("Getting value for %s", key));
		try {
			V value = this.map.get(key);
			if (value == null) {
				synchronized (this) {
					//Added counterMap to implement blocking behavior for threads with similar but parallelism for other keys  
					Integer counter = this.counterMap.get(key);
					if(counter == null) {
						this.counterMap.put(key, 1);
					}else {
						this.wait();
					}
				}

				if (this.map.get(key) == null) {
					value = this.valueExtractor.getValue(key);
					this.map.put(key, value);
					this.counterMap.remove(key);
					synchronized (this) {
						this.notifyAll();
					}
					return value;
				} else {
					return this.map.get(key);
				}
			}
			return value;
		} catch (InterruptedException e) {
			logger.error("Error while reading value");
			return null;
		}
	}
}
