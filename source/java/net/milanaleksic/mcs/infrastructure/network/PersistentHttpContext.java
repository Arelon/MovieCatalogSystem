package net.milanaleksic.mcs.infrastructure.network;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * User: Milan Aleksic
 * Date: 3/1/12
 * Time: 12:53 PM
 */
public final class PersistentHttpContext {

    private final HttpClient httpClient;

    private static ThreadLocal<HttpContext> httpContextThreadLocal = new ThreadLocal<HttpContext>() {
        @Override
        protected HttpContext initialValue() {
            return new BasicHttpContext();
        }
    };

    public PersistentHttpContext(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public HttpContext getHttpContext() {
        return httpContextThreadLocal.get();
    }

    public HttpResponse execute(HttpUriRequest method) throws IOException {
        return httpClient.execute(method, httpContextThreadLocal.get());
    }
}
