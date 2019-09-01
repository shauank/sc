package com.sc.queue;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sc.queue.function.SpecialQueueKeyExtractor;

/**
 * @author shpatel
 *
 * @param Type of Key
 * @param Type of Element
 * 
 * This class is implementation of SpecialQueue and provide Thread
 * safe operation. Additionally this class provide blocking and
 * non-blocking way of fetching data queue.
 * 
 * All elements stored into queue has associated key and same can
 * provided by use of functional interface SpecialQueueKeyExtractor.
 */
public class ConcurrentBlockingSpecialQueue<K, E> implements SpecialQueue<K, E> {

	private SpecialQueueKeyExtractor<K, E> keyExtractor;
	Map<K, E> map = null;
	Logger logger = LoggerFactory.getLogger(ConcurrentBlockingSpecialQueue.class);

	/**
	 * @param keyExtractor Functional interface provides a way of finding Key for
	 *                     all Elements.
	 */
	public ConcurrentBlockingSpecialQueue(SpecialQueueKeyExtractor<K, E> keyExtractor) {
		this.keyExtractor = keyExtractor;
		this.map = new ConcurrentHashMap<K, E>();
	}

	/**
	 * Returns size of queue
	 */
	public int size() {
		return this.map.size();
	}

	/**
	 * Adds @param element into queue. Also, notify threads waiting for values to be
	 * available in queue(For blocking operation)
	 */
	public void offer(E element) {
		this.map.put(this.keyExtractor.getKey(element), element);
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Delete element by @param key. 
	 * @return {@code true} if given element for @param key present and deleted 
	 * @return {@code true} if given element for @param key not present and not deleted
	 */
	public boolean removeByKey(K key) {
		return this.map.remove(key) != null;
	}

	/**
	 * Delete element by @param element. Returns true if deleted otherwise false
	 * @return {@code true} if given element is present and deleted 
	 * @return {@code true} if given element is present and not deleted
	 */
	public Boolean remove(E element) {
		return this.map.remove(this.keyExtractor.getKey(element)) != null;
	}

	/**
	 * This method provides non-blocking read operation from queue
	 * 
	 * @return first inserted element 
	 * @return null if queue is empty
	 */
	public E tryTake() {
		return this.returnFirstElement();
	}

	/**
	 * This method provide blocking read operation from queue. It will wait
	 * indefinitely time for element to available in queue
	 * 
	 * @return first inserted element
	 */
	public E take() throws InterruptedException {
		E element = this.returnFirstElement();
		if (element != null) {
			return element;
		} else {
			synchronized (this) {
				this.wait();
				return this.returnFirstElement();
			}
		}
	}

	/**
	 * Similar to {@link #take} but this does wait for @param timeout(in @param unit) for element to
	 * available in queue
	 * 
	 * @return first element if element available in queue within given time frame
	 * @return null if no element pushed by {@link #offer} in given time
	 *
	 */
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		logger.debug("Polling element for %s timout and %s timeunit", timeout, unit);
		E element = this.returnFirstElement();
		if (element != null) {
			return element;
		} else {
			synchronized (this) {
				this.wait(unit.toMillis(timeout));
				return this.returnFirstElement();
			}
		}
	}

	/**
	 * @return firstElement of queue
	 * @return null if no element present
	 */
	private E returnFirstElement() {
		Optional<K> firstElement = this.map.keySet().stream().findFirst();

		if (firstElement.isPresent()) {
			return this.map.get(firstElement.get());
		} else {
			logger.debug("Could not found any element");
			return null;
		}
	}
}
