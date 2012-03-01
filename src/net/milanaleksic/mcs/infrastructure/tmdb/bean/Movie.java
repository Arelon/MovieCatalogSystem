package net.milanaleksic.mcs.infrastructure.tmdb.bean;

import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;
import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 10/8/11
 * Time: 12:08 AM
 */
public class Movie {

    private BigDecimal score;
    private int popularity;
    private boolean translated;
    private boolean adult;
    private String language;

    private String name;

    @JsonProperty("original_name")
    private String originalName;

    @JsonProperty("alternative_name")
    private String alternativeName;

    private String type;

    private int id;

    private int votes;

    @JsonProperty("imdb_id")
    private String imdbId;

    private String url;

    private BigDecimal rating;

    private String certification;

    private String overview;

    private String released;

    private int version;

    @JsonProperty("last_modified_at")
    private String lastModifiedAt;

    @JsonProperty("movie_type")
    private String movieType;

    private List<ImageInfo> posters;

    private List<ImageInfo> backdrops;

    public int getVotes() {
        return votes;
    }

    public String getMovieType() {
        return movieType;
    }

    public BigDecimal getScore() {
        return score;
    }

    public int getPopularity() {
        return popularity;
    }

    public boolean isTranslated() {
        return translated;
    }

    public boolean isAdult() {
        return adult;
    }

    public String getLanguage() {
        return language;
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getAlternativeName() {
        return alternativeName;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getImdbId() {
        return imdbId;
    }

    public String getUrl() {
        return url;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public String getCertification() {
        return certification;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleased() {
        return released;
    }

    public String getReleasedYear() {
        return released != null ? released.substring(0, 4) : "?";
    }

    public int getVersion() {
        return version;
    }

    public String getLastModifiedAt() {
        return lastModifiedAt;
    }

    public List<ImageInfo> getPosters() {
        return posters;
    }

    public List<ImageInfo> getBackdrops() {
        return backdrops;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        if (imdbId != null ? !imdbId.equals(movie.imdbId) : movie.imdbId != null) return false;
        if (originalName != null ? !originalName.equals(movie.originalName) : movie.originalName != null) return false;
        if (released != null ? !released.equals(movie.released) : movie.released != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = originalName != null ? originalName.hashCode() : 0;
        result = 31 * result + (imdbId != null ? imdbId.hashCode() : 0);
        result = 31 * result + (released != null ? released.hashCode() : 0);
        return result;
    }
}
