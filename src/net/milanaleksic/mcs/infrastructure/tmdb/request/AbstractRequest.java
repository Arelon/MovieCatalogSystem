package net.milanaleksic.mcs.infrastructure.tmdb.request;

import net.milanaleksic.mcs.application.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * User: Milan Aleksic
 * Date: 10/7/11
 * Time: 11:31 PM
 */
public abstract class AbstractRequest {

    protected static final String apiLocation = "http://api.themoviedb.org/2.1/";

    protected final Logger log = Logger.getLogger(this.getClass());

    protected String apiKey;

    protected HttpClient httpClient;

    protected static ObjectMapper mapper = new ObjectMapper();

    protected abstract String getUrl();

    public AbstractRequest(String apiKey, UserConfiguration.ProxyConfiguration proxyConfiguration) {
        this.apiKey = apiKey;
        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
        if (null != proxyConfiguration.getServer() && !proxyConfiguration.getServer().isEmpty()) {
            String server = proxyConfiguration.getServer();
            int port = proxyConfiguration.getPort() == 0 ? 80 : proxyConfiguration.getPort();
            final HttpHost hcProxyHost = new HttpHost(server, port, "http");
            if (null != proxyConfiguration.getUsername() && !proxyConfiguration.getUsername().isEmpty()
                    && null != proxyConfiguration.getPassword() && !proxyConfiguration.getPassword().isEmpty())
            ((DefaultHttpClient) httpClient).getCredentialsProvider().setCredentials(new AuthScope(server, port),
                         new UsernamePasswordCredentials(proxyConfiguration.getUsername(), proxyConfiguration.getPassword()));
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, hcProxyHost);
        }
        httpClient.getParams().setParameter("http.connection.timeout", 2000);
    }

    @MethodTiming
    protected <T> T processRequest(Class<T> clazz) throws TmdbException {
        String url = getUrl();
        String value = "";
        try {
            HttpGet httpMethod = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpMethod);
            int statusLine = response.getStatusLine().getStatusCode();
            if (statusLine != 200)
                throw new TmdbException("Invalid response: "+statusLine);
            HttpEntity entity = response.getEntity();
            if (entity == null)
                return null;
            value = EntityUtils.toString(entity);
            if ("[\"Nothing found.\"]".equals(value))
                return null;
            return mapper.readValue(value, clazz);
        } catch (ClientProtocolException e) {
            throw new TmdbException("Client protocol exception occurred: ", e);
        } catch (IOException e) {
            throw new TmdbException("IO exception occurred: ", e);
        } catch (Throwable t) {
            throw new TmdbException("Totally unexpected exception occurred: ", t);
        }
    }

    protected JsonNode processRequest() throws TmdbException {
         return processRequest(JsonNode.class);
    }

}
