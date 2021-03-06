package net.milanaleksic.mcs.infrastructure.tmdb.impl;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
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

    public Optional<Movie[]> searchForMovies(String searchString) throws TmdbException {
        return new MovieSearch(this, searchString).getSearchResult();
    }

    @Override
    public Optional<ImageSearchResult> getImagesForMovie(String imdbId) throws TmdbException {
        Optional<ImageSearchResult[]> ofTheJedi = new ImageSearch(this, imdbId).getSearchResult();
        if (!ofTheJedi.isPresent())
            return Optional.absent();
        if (ofTheJedi.get().length>1)
            throw new TmdbException("Non-unique search for Images on TMDB API");
        return Optional.fromNullable(ofTheJedi.get()[0]);
    }

    PersistentHttpContext getPersistentHttpContext() {
        return persistentHttpContext;
    }

    String getApiKey() {
        return apiKey;
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        persistentHttpContext = httpClientFactoryService.createPersistentHttpContext();
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) { }

}
