package net.milanaleksic.mcs.infrastructure.tmdb.request;

import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;

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
        return apiLocation + "Movie.search/en/json/" + apiKey + "/" + searchFilter;
    }

    public String getSearchResult() throws TmdbException {
        Movie[] movies = processRequest(Movie[].class);
        for (Movie movie : movies) {
            System.out.println("Movie found: " + movie.getName());
        }
        return ""+ Arrays.asList(movies);
    }
}
