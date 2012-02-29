package net.milanaleksic.mcs.infrastructure.tmdb.impl;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.tmdb.*;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.ntlm.NTLMSchemeFactory;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.*;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.*;

import javax.inject.Inject;
import java.io.IOException;

/**
 * User: Milan Aleksic
 * Date: 10/7/11
 * Time: 9:29 PM
 */
public class TmdbServiceImpl implements TmdbService {

    @Inject
    private ApplicationManager applicationManager;

    private String apiKey;

    private HttpClient httpClient;
    private HttpContext requestContext;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Movie[] searchForMovies(String searchString) throws TmdbException {
        Movie[] ofTheJedi = new MovieSearch(this, searchString).getSearchResult();
        if (ofTheJedi == null)
            return new Movie[0];
        return ofTheJedi;
    }

    HttpClient getHttpClient() {
        return httpClient;
    }

    String getApiKey() {
        return apiKey;
    }

    @Override
    public void applicationStarted() {
        prepareCommunicationLevel();
    }

    private void prepareCommunicationLevel() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        UserConfiguration.ProxyConfiguration proxyConfiguration = applicationManager.getUserConfiguration().getProxyConfiguration();
        DefaultHttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(new BasicHttpParams(), registry), new BasicHttpParams());
        if (null != proxyConfiguration.getServer() && !proxyConfiguration.getServer().isEmpty()) {
            String server = proxyConfiguration.getServer();
            int port = proxyConfiguration.getPort() == 0 ? 80 : proxyConfiguration.getPort();
            final HttpHost hcProxyHost = new HttpHost(server, port, "http");
            if (null != proxyConfiguration.getUsername() && !proxyConfiguration.getUsername().isEmpty()
                    && null != proxyConfiguration.getPassword() && !proxyConfiguration.getPassword().isEmpty()) {
                Credentials credentials;
                httpClient.getAuthSchemes().register("NTLM", new NTLMSchemeFactory());
                if (proxyConfiguration.isNtlm()) {
                    credentials = new NTCredentials(proxyConfiguration.getUsername(), proxyConfiguration.getPassword(), "", "");
                } else
                    credentials = new UsernamePasswordCredentials(proxyConfiguration.getUsername(), proxyConfiguration.getPassword());
                httpClient.getCredentialsProvider().setCredentials(new AuthScope(server, port), credentials);
            }
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, hcProxyHost);
        }
        httpClient.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                long keepAlive = super.getKeepAliveDuration(response, context);
                if (keepAlive == -1) {
                    keepAlive = 60000;
                }
                return keepAlive;
            }
        });
        httpClient.getParams().setParameter("http.connection.timeout", 2000);

        this.httpClient = httpClient;
        requestContext = new BasicHttpContext();
    }

    @Override
    public void applicationShutdown() {
    }

    HttpResponse executeHttpMethod(HttpGet httpMethod) throws IOException {
        return httpClient.execute(httpMethod, requestContext);
    }
}
