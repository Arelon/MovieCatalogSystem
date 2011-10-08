package net.milanaleksic.mcs.infrastructure.tmdb.impl;

import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbService;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.tmdb.request.MovieSearch;

/**
 * User: Milan Aleksic
 * Date: 10/7/11
 * Time: 9:29 PM
 */
public class TmdbServiceImpl implements TmdbService {

    private String apiKey;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Movie[] searchForMovies(String searchString) throws TmdbException {
        return new MovieSearch(apiKey, searchString).getSearchResult();
    }

}
