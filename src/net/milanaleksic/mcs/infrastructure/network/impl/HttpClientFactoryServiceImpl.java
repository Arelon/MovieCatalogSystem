package net.milanaleksic.mcs.infrastructure.network.impl;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.network.HttpClientFactoryService;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import net.milanaleksic.mcs.infrastructure.util.ntlm.NTLMSchemeFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.*;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.*;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 3/1/12
 * Time: 12:39 PM
 */
public class HttpClientFactoryServiceImpl implements HttpClientFactoryService {

    @Inject private ApplicationManager applicationManager;

    @Override
    public PersistentHttpContext createPersistentHttpContext() {
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
        return new PersistentHttpContext(httpClient, new BasicHttpContext());
    }

}
