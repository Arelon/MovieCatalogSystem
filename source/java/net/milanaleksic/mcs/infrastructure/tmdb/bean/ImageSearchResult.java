package net.milanaleksic.mcs.infrastructure.tmdb.bean;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class ImageSearchResult {

    private String id;

    @JsonProperty("name")
    private String movieName;

    private List<ImageInfo> posters;

    private List<ImageInfo> backdrops;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public List<ImageInfo> getPosters() {
        return posters;
    }

    public void setPosters(List<ImageInfo> posters) {
        this.posters = posters;
    }

    public List<ImageInfo> getBackdrops() {
        return backdrops;
    }

    public void setBackdrops(List<ImageInfo> backdrops) {
        this.backdrops = backdrops;
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    @Override
    public String toString() {
        return "ImageSearchResult{" +
                "id='" + id + '\'' +
                ", movieName='" + movieName + '\'' +
                ", posters=" + posters +
                ", backdrops=" + backdrops +
                '}';
    }
}
