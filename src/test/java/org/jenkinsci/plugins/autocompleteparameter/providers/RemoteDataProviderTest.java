package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import hidden.jth.org.apache.http.HttpException;
import hidden.jth.org.apache.http.HttpRequest;
import hidden.jth.org.apache.http.HttpResponse;
import hidden.jth.org.apache.http.entity.StringEntity;
import hidden.jth.org.apache.http.impl.bootstrap.HttpServer;
import hidden.jth.org.apache.http.impl.bootstrap.ServerBootstrap;
import hidden.jth.org.apache.http.protocol.HttpContext;
import hidden.jth.org.apache.http.protocol.HttpRequestHandler;
import net.sf.ezmorph.bean.MorphDynaBean;

public class RemoteDataProviderTest {
	@Test
	public void happyDay() throws IOException, InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpServer server = ServerBootstrap .bootstrap() .setListenerPort(11331)
						.registerHandler("/test", new HttpRequestHandler() {
							@Override
							public void handle(HttpRequest arg0, HttpResponse response, HttpContext arg2) throws HttpException, IOException {
								response.setEntity(new StringEntity("[{'name':'Eddard','house':'Stark'}, {'name':'Robert','house':'Baratheon'}]"));
								response.setStatusCode(200);
							}
						})
						.create();
					server.start();
					latch.countDown();
					server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		});
		t.start();
		latch.await();
		RemoteDataProvider subject = new RemoteDataProvider(true, "http://localhost:11331/test", null);
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
}