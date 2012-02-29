package net.milanaleksic.mcs.infrastructure.tmdb;

import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
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

    protected static final ObjectMapper mapper = new ObjectMapper();

    protected abstract String getUrl();

    private static HttpContext context = new BasicHttpContext();

    @MethodTiming
    protected <T> T processRequest(Class<T> clazz) throws TmdbException {
        String url = getUrl();
        String value;
        try {
            HttpGet httpMethod = new HttpGet(url);
            HttpResponse response = executeHttpMethod(httpMethod);
            int statusLine = response.getStatusLine().getStatusCode();
            if (statusLine != 200)
                throw new TmdbException("Invalid response: " + statusLine);
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

    protected abstract HttpResponse executeHttpMethod(HttpGet httpMethod) throws IOException;

    protected JsonNode processRequest() throws TmdbException {
        return processRequest(JsonNode.class);
    }

}
