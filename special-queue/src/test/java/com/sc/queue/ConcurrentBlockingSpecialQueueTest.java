package com.sc.queue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sc.queue.function.SpecialQueueKeyExtractor;

/**
 * @author shpatel
 *
 */
public class ConcurrentBlockingSpecialQueueTest {

	private SpecialQueue<String, String> specialQueue;
	private SpecialQueueKeyExtractor<String, String> keyExtractor;

	@Before
	public void setUp() throws Exception {
		this.keyExtractor = (element) -> {
			return element;
		};

		this.specialQueue = new ConcurrentBlockingSpecialQueue<String, String>(keyExtractor);

		this.specialQueue.offer("E1");
		this.specialQueue.offer("E2");
		this.specialQueue.offer("E3");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSize() {
		assertThat(this.specialQueue.size(), is(3));
	}

	@Test
	public void testOffer() {
		this.specialQueue.offer("E4");
		assertThat(this.specialQueue.size(), is(4));
	}

	@Test
	public void testPriorityOfInsert() {
		this.specialQueue = new ConcurrentBlockingSpecialQueue<String, String>(keyExtractor);

		this.specialQueue.offer("E1");
		this.specialQueue.offer("E2");
		this.specialQueue.offer("E3");
		this.specialQueue.offer("E4");

		this.specialQueue.offer("E3");

		int count = 0;
		while (true) {
			String latestValue = this.specialQueue.tryTake();
			this.specialQueue.remove(latestValue);

			count++;
			if (latestValue.contentEquals("E3")) {
				break;
			}
		}

		assertThat(count, is(3));
		assertThat(count, not(5));
	}

	@Test
	public void testTryTake() {
		assertThat(this.specialQueue.size(), is(3));

		String element = this.specialQueue.tryTake();
		assertThat(element, is("E1"));

		assertThat(this.specialQueue.size(), is(3));
	}

	@Test
	public void testRemoveByKey() {
		this.keyExtractor = (element) -> {
			return element + "2";
		};

		this.specialQueue = new ConcurrentBlockingSpecialQueue<String, String>(keyExtractor);

		this.specialQueue.offer("E1");
		this.specialQueue.offer("E2");
		this.specialQueue.offer("E3");

		assertThat(this.specialQueue.size(), is(3));

		assertThat(this.specialQueue.removeByKey("E12"), is(true));
		assertThat(this.specialQueue.size(), is(2));

		assertThat(this.specialQueue.removeByKey("AbElem"), is(false));
		assertThat(this.specialQueue.size(), is(2));

		assertThat(this.specialQueue.removeByKey("E1"), is(false));
		assertThat(this.specialQueue.size(), is(2));
	}

	@Test
	public void testRemove() {
		assertThat(this.specialQueue.size(), is(3));

		assertThat(this.specialQueue.remove("E1"), is(true));
		assertThat(this.specialQueue.size(), is(2));

		assertThat(this.specialQueue.remove("AbElem"), is(false));
		assertThat(this.specialQueue.size(), is(2));
	}

	@Test
	public void testTake() throws InterruptedException {
		this.specialQueue.remove("E1");
		this.specialQueue.remove("E2");
		this.specialQueue.remove("E3");

		assertThat(this.specialQueue.size(), is(0));

		Runnable runnable = () -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			this.specialQueue.offer("E4");
		};
		Thread t = new Thread(runnable);
		t.start();

		assertThat(this.specialQueue.take(), is("E4"));
		assertThat(this.specialQueue.size(), is(1));
	}

	@Test
	public void testPoll() throws InterruptedException {
		this.specialQueue.remove("E1");
		this.specialQueue.remove("E2");
		this.specialQueue.remove("E3");

		assertThat(this.specialQueue.size(), is(0));

		Runnable runnable = () -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			this.specialQueue.offer("E4");
		};
		Thread t = new Thread(runnable);
		t.start();

		assertNull(this.specialQueue.poll(1, TimeUnit.SECONDS));
		assertThat(this.specialQueue.size(), is(0));

		assertThat(this.specialQueue.poll(5, TimeUnit.SECONDS), is("E4"));
		assertThat(this.specialQueue.size(), is(1));
	}
}
