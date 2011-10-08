package net.milanaleksic.mcs.infrastructure.tmdb.request;

import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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

    protected String apiKey;

    protected static HttpClient httpClient = new DefaultHttpClient();
    protected static ObjectMapper mapper = new ObjectMapper();

    protected abstract String getUrl();

    public AbstractRequest(String apiKey) {
        this.apiKey = apiKey;
    }

    @MethodTiming
    protected <T> T processRequest(Class<T> clazz) throws TmdbException {
        HttpGet httpMethod = new HttpGet(getUrl());
        try {
            HttpResponse response = httpClient.execute(httpMethod);
            int statusLine = response.getStatusLine().getStatusCode();
            if (statusLine != 200)
                throw new TmdbException("Invalid response: "+statusLine);
            HttpEntity entity = response.getEntity();
            if (entity == null)
                return null;
            String value = EntityUtils.toString(entity);
            return mapper.readValue(value, clazz);
        } catch (ClientProtocolException e) {
            throw new TmdbException("Client protocol exception occurred: ", e);
        } catch (IOException e) {
            throw new TmdbException("IO exception occurred: ", e);
        }
    }

    protected JsonNode processRequest() throws TmdbException {
         return processRequest(JsonNode.class);
    }

}
