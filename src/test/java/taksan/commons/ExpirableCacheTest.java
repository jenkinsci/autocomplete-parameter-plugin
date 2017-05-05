package taksan.commons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import taksan.commons.ExpirableCache;
import taksan.commons.ExpirableCacheRepopulator;

public class ExpirableCacheTest {
	@Test
	public void happyDay() throws InterruptedException {
		ExpirableCache<String> subject = new ExpirableCache<>(500);
		ExpirableCacheRepopulator<String> repopulate = new ExpirableCacheRepopulator<String>() {
			@Override
			public String call(String oldValue) {
				return "newvalue";
			}
		};
		assertEquals("newvalue", subject.get("foo", repopulate));
		
		Thread.sleep(600);
		subject.put("foo", "bar");
		assertEquals("bar", subject.get("foo", repopulate));
		
		Thread.sleep(300);
		assertEquals("bar", subject.get("foo", repopulate));
		
		subject.put("foo", "bar");
		Thread.sleep(300);
		assertEquals("bar", subject.get("foo", repopulate));
		
		Thread.sleep(300);
		assertEquals("newvalue", subject.get("foo", repopulate));
	}
}