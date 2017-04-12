package org.jenkinsci.plugins.autocompleteparameter.providers;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class SimpleTextProviderTest {
	@Test
	public void happyDay() {
		String autoCompleteData="a,b,c";
		SimpleTextProvider subject = new SimpleTextProvider(autoCompleteData);
		
		assertEquals("a,b,c", StringUtils.join(subject.getData(),","));
	}
}