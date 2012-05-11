package net.milanaleksic.mcs.infrastructure.tmdb;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.*;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageSearchResult;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;

/**
 * User: Milan Aleksic
 * Date: 10/8/11
 * Time: 11:39 AM
 */
public interface TmdbService extends LifeCycleListener {

    public void setApiKey(String apiKey) ;

    public Optional<Movie[]> searchForMovies(String searchString) throws TmdbException ;

    public Optional<ImageSearchResult> getImagesForMovie(String imdbId) throws TmdbException;
}
