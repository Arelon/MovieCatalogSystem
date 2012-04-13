package net.milanaleksic.mcs.infrastructure.tmdb;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageSearchResult;
import net.milanaleksic.mcs.infrastructure.tmdb.impl.AbstractServiceAwareRequest;

/**
 * User: Milan Aleksic
 * Date: 3/5/12
 * Time: 4:00 PM
 */
public class ImageSearch extends AbstractServiceAwareRequest {

    private final String imdbId;

    public ImageSearch(TmdbService tmdbService, String imdbId) {
        super(tmdbService);
        this.imdbId = imdbId;
    }

    @Override
    @SuppressWarnings({"HardCodedStringLiteral"})
    protected String getUrl() {
        return apiLocation + "Movie.getImages/en/json/" + getApiKey() + "/" + imdbId;
    }

    public Optional<ImageSearchResult[]> getSearchResult() throws TmdbException {
        return processRequest(ImageSearchResult[].class);
    }

}
