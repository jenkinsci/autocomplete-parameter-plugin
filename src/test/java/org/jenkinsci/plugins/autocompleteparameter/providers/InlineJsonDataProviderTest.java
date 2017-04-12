package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import net.sf.ezmorph.bean.MorphDynaBean;

public class InlineJsonDataProviderTest {
	@Test
	public void happyDayWithArray() {
		String autoCompleteData="[{'name':'Eddard','house':'Stark'}, {'name':'Robert','house':'Baratheon'}]";
		InlineJsonDataProvider subject = new InlineJsonDataProvider(autoCompleteData);
		
		
		@SuppressWarnings("unchecked")
		Collection<MorphDynaBean> actual = (Collection<MorphDynaBean>) subject.getData();
		Iterator<MorphDynaBean> it = actual.iterator();
		
		MorphDynaBean actual1 = it.next();
		Assert.assertEquals("Eddard", actual1.get("name"));
		Assert.assertEquals("Stark", actual1.get("house"));
		
		MorphDynaBean actual2 = it.next();
		Assert.assertEquals("Robert", actual2.get("name"));
		Assert.assertEquals("Baratheon", actual2.get("house"));		
	}
	
	@Test
	public void happyDayWithObject() {
		String autoCompleteData="{'name':'Eddard','house':'Stark'}";
		InlineJsonDataProvider subject = new InlineJsonDataProvider(autoCompleteData);
		
		
		@SuppressWarnings("unchecked")
		Collection<Map<String, String>> actual = (Collection<Map<String, String>>) subject.getData();
		Iterator<Map<String, String>> it = actual.iterator();
		
		Map<String, String> actual1 = it.next();
		Assert.assertEquals("name", actual1.get("key"));
		Assert.assertEquals("Eddard", actual1.get("value"));
		
		Map<String, String> actual2 = it.next();
		Assert.assertEquals("house", actual2.get("key"));
		Assert.assertEquals("Stark", actual2.get("value"));		
	}
}