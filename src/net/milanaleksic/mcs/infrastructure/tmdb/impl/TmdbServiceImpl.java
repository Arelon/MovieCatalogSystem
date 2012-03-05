package net.milanaleksic.mcs.infrastructure.tmdb.impl;

import net.milanaleksic.mcs.infrastructure.network.HttpClientFactoryService;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import net.milanaleksic.mcs.infrastructure.tmdb.*;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageSearchResult;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 10/7/11
 * Time: 9:29 PM
 */
public class TmdbServiceImpl implements TmdbService {

    @Inject private HttpClientFactoryService httpClientFactoryService;

    private String apiKey;

    private PersistentHttpContext persistentHttpContext;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Movie[] searchForMovies(String searchString) throws TmdbException {
        Movie[] ofTheJedi = new MovieSearch(this, searchString).getSearchResult();
        if (ofTheJedi == null)
            return new Movie[0];
        return ofTheJedi;
    }

    @Override
    public ImageSearchResult getImagesForMovie(String imdbId) throws TmdbException {
        return new ImageSearch(this, imdbId).getSearchResult();
    }

    PersistentHttpContext getPersistentHttpContext() {
        return persistentHttpContext;
    }

    String getApiKey() {
        return apiKey;
    }

    @Override
    public void applicationStarted() {
        persistentHttpContext = httpClientFactoryService.createPersistentHttpContext();
    }

    @Override
    public void applicationShutdown() { }

}
