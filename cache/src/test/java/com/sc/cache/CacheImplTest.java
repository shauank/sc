package com.sc.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import com.sc.cache.function.CacheValueExtractor;

public class CacheImplTest {

	private CacheValueExtractor<String, String> valueExtractor;
	private CacheImpl<String, String> cacheImpl;

	@Before
	public void setUp() throws Exception {

		this.valueExtractor = (key) -> {
			return key;
		};

		this.cacheImpl = new CacheImpl<String, String>(this.valueExtractor);
	}

	@Test
	public void testGeteWithDifferentValueExtractor() {
		this.valueExtractor = (key) -> {
			return key + "1";
		};

		this.cacheImpl = new CacheImpl<String, String>(this.valueExtractor);

		assertThat(this.cacheImpl.get("E1"), is("E11"));
	}
	
	@Test
	public void testHappyPath() {
		assertThat(this.cacheImpl.get("E1"), is("E1"));
		assertThat(this.cacheImpl.get("E2"), is("E2"));
		assertThat(this.cacheImpl.get("E3"), is("E3"));
	}
	
	@Test
	public void testMultiValueExtractorCallsOnlyOnceThreadSafeOperation() {

		@SuppressWarnings("unchecked")
		CacheValueExtractor<String, String> mockCLass = mock(CacheValueExtractor.class);
		when(mockCLass.getValue("E1")).thenReturn("E1");
		when(mockCLass.getValue("E2")).thenReturn("E2");
		final CacheImpl<String, String> cacheImpl = new CacheImpl<String, String>(mockCLass);

		final CountDownLatch latch = new CountDownLatch(1);
		for (int i = 0; i < 50; i++) {
			Runnable runner = new Runnable() {
				public void run() {
					try {
						latch.await();
						String number = Thread.currentThread().getName();
						if(Integer.valueOf(number) /2 == 0) {
							cacheImpl.get("E1");
						}else {
							cacheImpl.get("E2");
						}
					} catch (InterruptedException ie) {
					}
				}
			};
			new Thread(runner, String.valueOf(i)).start();
		}
		latch.countDown();

		verify(mockCLass, times(1)).getValue("E1");
		verify(mockCLass, times(1)).getValue("E2");
	}

}
