package net.milanaleksic.mcs.infrastructure.tmdb;

/**
 * User: Milan Aleksic
 * Date: 10/8/11
 * Time: 11:39 AM
 */
public interface TmdbService {

    public void setApiKey(String apiKey) ;

    public String searchForMovies(String searchString) throws TmdbException ;

}
