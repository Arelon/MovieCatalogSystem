package net.milanaleksic.mcs.infrastructure.tmdb;

import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.tmdb.impl.AbstractServiceAwareRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * User: Milan Aleksic
 * Date: 10/7/11
 * Time: 11:31 PM
 */
public class MovieSearch extends AbstractServiceAwareRequest {

    private final String searchFilter;

    public MovieSearch(TmdbService tmdbService, String searchFilter) {
        super(tmdbService);
        this.searchFilter = searchFilter;
    }

    @Override
    @SuppressWarnings({"HardCodedStringLiteral"})
    protected String getUrl() {
        try {
            return apiLocation + "Movie.search/en/json/" + getApiKey() + "/" +
                    URLEncoder.encode(searchFilter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is not supported in this system? WTF?");
        }
    }

    public Movie[] getSearchResult() throws TmdbException {
        return processRequest(Movie[].class);
    }

}
