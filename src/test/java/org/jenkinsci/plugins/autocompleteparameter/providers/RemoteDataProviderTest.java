package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hudson.util.FormValidation;
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RemoteDataProviderTest {
	@Test
	public void happyDay() throws IOException {
		HttpServer server = ServerBootstrap.bootstrap().setListenerPort(11331)
				.registerHandler("/test", new HttpRequestHandler() {
					@Override
					public void handle(HttpRequest arg0, HttpResponse response, HttpContext arg2) throws HttpException, IOException {
						response.setEntity(new StringEntity("[{'name':'Eddard','house':'Stark'}, {'name':'Robert','house':'Baratheon'}]"));
						response.setStatusCode(200);
					}
				})
				.create();
		server.start();
		try {
			RemoteDataProvider subject = new RemoteDataProvider(true, "http://localhost:11331/test", null, "");
			@SuppressWarnings("unchecked")
			Collection<MorphDynaBean> actual = (Collection<MorphDynaBean>) subject.getData();
			Iterator<MorphDynaBean> it = actual.iterator();

			MorphDynaBean actual1 = it.next();
			Assert.assertEquals("Eddard", actual1.get("name"));
			Assert.assertEquals("Stark", actual1.get("house"));

			MorphDynaBean actual2 = it.next();
			Assert.assertEquals("Robert", actual2.get("name"));
			Assert.assertEquals("Baratheon", actual2.get("house"));
		} finally {
			server.stop();
		}
	}

	@Test
	public void happyDayFilter() throws Exception {
		HttpServer server = ServerBootstrap.bootstrap().setListenerPort(11331)
				.registerHandler("/test/query=smth", new HttpRequestHandler() {
					@Override
					public void handle(HttpRequest arg0, HttpResponse response, HttpContext arg2) throws IOException {
						response.setEntity(new StringEntity("{'start': 1, 'entries': [{'name':'Eddard'," +
								"'house':'Stark'}, {'name':'Robert','house':'Baratheon'}]}"));
						response.setStatusCode(200);
					}
				})
				.create();
		server.start();
		try {
			RemoteDataProvider subject = new RemoteDataProvider(true, "http://localhost:11331/test/query=smth", null, "entries");
			@SuppressWarnings("unchecked")
			Collection<MorphDynaBean> actual = (Collection<MorphDynaBean>) subject.filter("smth");
			Iterator<MorphDynaBean> it = actual.iterator();

			MorphDynaBean actual1 = it.next();
			Assert.assertEquals("Eddard", actual1.get("name"));
			Assert.assertEquals("Stark", actual1.get("house"));

			MorphDynaBean actual2 = it.next();
			Assert.assertEquals("Robert", actual2.get("name"));
			Assert.assertEquals("Baratheon", actual2.get("house"));
		} finally {
			server.stop();
		}
	}

	@Test
	public void timeout() throws IOException {
		// given
		HttpServer server = ServerBootstrap .bootstrap() .setListenerPort(13311)
				.registerHandler("/test", new HttpRequestHandler() {
					@Override
					public void handle(HttpRequest arg0, HttpResponse response, HttpContext arg2) throws HttpException, IOException {
						try {
							Thread.sleep(4000);
						} catch (InterruptedException ex) {
							throw new IllegalStateException(ex);
						}
						response.setEntity(new StringEntity("[{'name':'Eddard','house':'Stark'}, {'name':'Robert','house':'Baratheon'}]"));
						response.setStatusCode(200);
					}
				})
				.create();
		server.start();
		try {
			RemoteDataProvider subject = new RemoteDataProvider(true, "http://localhost:13311/test", null, "");

			try {
				// when
				subject.getData(1, TimeUnit.SECONDS);
				fail();
			} catch (Exception ex) {
				// then
				assertThat(ex, instanceOf(TimeoutException.class));
			}
		} finally {
			server.stop();
		}
	}

	@Test
	public void urlValidation_empty() {
		RemoteDataProvider.DescriptorImpl subject = new RemoteDataProvider.DescriptorImpl();
		FormValidation response = subject.doCheckAutoCompleteUrl(true, "");
		assertThat(response.kind, is(FormValidation.Kind.ERROR));
	}

	@Test
	public void urlValidation_async_withoutQueryParameter() {
		RemoteDataProvider.DescriptorImpl subject = new RemoteDataProvider.DescriptorImpl();
		FormValidation response = subject.doCheckAutoCompleteUrl(false, "http://remote/rest");
		assertThat(response.kind, is(FormValidation.Kind.OK));
		assertThat(response.renderHtml(), containsString("${query}"));
	}

	@Test
	public void urlValidation_async_withQueryParameter() {
		RemoteDataProvider.DescriptorImpl subject = new RemoteDataProvider.DescriptorImpl();
		FormValidation response = subject.doCheckAutoCompleteUrl(false, "http://remote/rest?q=${query}");
		assertThat(response.kind, equalTo(FormValidation.Kind.OK));
		assertThat(response.renderHtml(), not(containsString("${query}")));
	}
}