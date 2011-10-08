package net.milanaleksic.mcs.infrastructure.tmdb.request;

import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * User: Milan Aleksic
 * Date: 10/7/11
 * Time: 11:31 PM
 */
public class MovieSearch extends AbstractRequest {

    private String searchFilter;

    public MovieSearch(String apiKey, String searchFilter) {
        super(apiKey);
        this.searchFilter = searchFilter;
    }

    @Override
    protected String getUrl() {
        try {
            return apiLocation + "Movie.search/en/json/" + apiKey + "/" +
                    URLEncoder.encode(searchFilter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is not supported in this system? WTF?");
        }
    }

    public Movie[] getSearchResult() throws TmdbException {
        return processRequest(Movie[].class);
    }
}
