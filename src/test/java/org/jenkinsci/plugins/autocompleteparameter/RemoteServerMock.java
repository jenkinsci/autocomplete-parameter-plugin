package org.jenkinsci.plugins.autocompleteparameter;

import hidden.jth.org.apache.http.HttpException;
import hidden.jth.org.apache.http.HttpRequest;
import hidden.jth.org.apache.http.HttpResponse;
import hidden.jth.org.apache.http.entity.StringEntity;
import hidden.jth.org.apache.http.impl.bootstrap.HttpServer;
import hidden.jth.org.apache.http.impl.bootstrap.ServerBootstrap;
import hidden.jth.org.apache.http.protocol.HttpContext;
import hidden.jth.org.apache.http.protocol.HttpRequestHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RemoteServerMock implements Closeable {

    private HttpServer server;

    public RemoteServerMock() {
        server = ServerBootstrap.bootstrap()
                .registerHandler("/rest/users", new HttpRequestHandler() {
                    @Override
                    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                        response.setEntity(new StringEntity("[{'name':'Frederic Chopin','email':'chopin@mail.com'}, {'name':'Wolfgang A. Mozart','email':'mozart@mail.com'}, {'name':'Ludwig Von Beethoven', 'email':'beethoven@mail.com'}]"));
                        response.setStatusCode(200);
                        if (StringUtils.contains(request.getRequestLine().getUri(), "slow=true")) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                // do nothing
                            }
                        }
                    }
                })
                .create();
    }

    public String getAddress() {
        return "http://localhost:" + server.getLocalPort();
    }

    public void start() throws IOException {
        server.start();
    }

    public void stop() throws InterruptedException {
        server.stop();
        server.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws IOException {
        try {
            stop();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
