package net.milanaleksic.mcs.infrastructure.network;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * User: Milan Aleksic
 * Date: 3/1/12
 * Time: 12:53 PM
 */
public final class PersistentHttpContext {

    private final HttpClient httpClient;

    private final HttpContext httpContext;

    public PersistentHttpContext(HttpClient httpClient, HttpContext httpContext) {
        this.httpClient = httpClient;
        this.httpContext = httpContext;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }

    public HttpResponse execute(HttpUriRequest method) throws IOException {
        return httpClient.execute(method, httpContext);
    }
}
