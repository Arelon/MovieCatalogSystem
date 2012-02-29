package net.milanaleksic.mcs.infrastructure.tmdb.impl;

import net.milanaleksic.mcs.infrastructure.tmdb.AbstractRequest;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

/**
 * User: Milan Aleksic
 * Date: 2/29/12
 * Time: 1:26 PM
 */
public abstract class AbstractServiceAwareRequest extends AbstractRequest {

    protected final TmdbService tmdbService;

    public AbstractServiceAwareRequest(TmdbService tmdbService) {
        super();
        this.tmdbService = tmdbService;
    }

    protected HttpResponse executeHttpMethod(HttpGet httpMethod) throws IOException {
        return ((TmdbServiceImpl)tmdbService).executeHttpMethod(httpMethod);
    }

    protected String getApiKey() {
        return ((TmdbServiceImpl)tmdbService).getApiKey();
    }
}
