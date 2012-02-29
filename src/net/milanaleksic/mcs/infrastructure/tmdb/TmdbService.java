package net.milanaleksic.mcs.infrastructure.tmdb;

import net.milanaleksic.mcs.application.IntegrationManager;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;

/**
 * User: Milan Aleksic
 * Date: 10/8/11
 * Time: 11:39 AM
 */
public interface TmdbService extends IntegrationManager {

    public void setApiKey(String apiKey) ;

    public Movie[] searchForMovies(String searchString) throws TmdbException ;

}
