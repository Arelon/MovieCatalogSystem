package net.milanaleksic.mcs.infrastructure.tmdb;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * User: Milan Aleksic
 * Date: 10/7/11
 * Time: 11:31 PM
 */
public abstract class AbstractRequest {

    protected final Logger logger = Logger.getLogger(this.getClass());

    protected static final String apiLocation = "http://api.themoviedb.org/2.1/"; //NON-NLS

    protected static final ObjectMapper mapper = new ObjectMapper();

    protected abstract String getUrl();

    @MethodTiming
    protected <T> Optional<T> processRequest(Class<T> clazz) throws TmdbException {
        String url = getUrl();
        String value;
        try {
            if (logger.isDebugEnabled())
                logger.debug("Creating TMDB request "+url); //NON-NLS
            HttpGet httpMethod = new HttpGet(url);
            HttpResponse response = executeHttpMethod(httpMethod);
            int statusLine = response.getStatusLine().getStatusCode();
            if (statusLine != 200)
                throw new TmdbException("Invalid response: " + statusLine);
            HttpEntity entity = response.getEntity();
            if (entity == null)
                return Optional.absent();
            value = EntityUtils.toString(entity);
            if ("[\"Nothing found.\"]".equals(value)) //NON-NLS
                return Optional.absent();
            return Optional.fromNullable(mapper.readValue(value, clazz));
        } catch (ClientProtocolException e) {
            throw new TmdbException("Client protocol exception occurred: ", e);
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.warn("IO Interrupted, returning null as result from TMDB API request"); //NON-NLS
            return Optional.absent();
        } catch (IOException e) {
            throw new TmdbException("IO exception occurred: ", e);
        } catch (Throwable t) {
            throw new TmdbException("Totally unexpected exception occurred: ", t);
        }
    }

    protected abstract HttpResponse executeHttpMethod(HttpGet httpMethod) throws IOException;

    protected Optional<JsonNode> processRequest() throws TmdbException {
        return processRequest(JsonNode.class);
    }

}
