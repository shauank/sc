package com.sc.queue;

import java.util.concurrent.TimeUnit;

public interface SpecialQueue<K, E> {

	// Return the number of items within the queue.
    int size();         

    // Adds an item to the queue however if there already exists an entry with the mapping K -> E then it replaces the 
    // existing entry.  Priority within the queue must be maintained if there is an existing entry being updated.
    void offer(E e);

    // removes an entry in the queue mapped to the key K, return true if removed.
    boolean removeByKey(K key);

     // Removes the given entry, return true if removed.
    Boolean remove(E e);

    // Tries to take the next entry from the queue.  Will wait indefinitely until an item is available to be removed.
    E take() throws InterruptedException;

    // Attempts to take the next entry.  If there are no entries will return immediately with null.
    E tryTake();

    // Attempts to take the next item from the queue and will wait the given duration until one is available
    E poll(long timeout, TimeUnit unit) throws InterruptedException;
}
