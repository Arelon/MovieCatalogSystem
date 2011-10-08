package net.milanaleksic.mcs.infrastructure.tmdb.bean;

/**
 * User: Milan Aleksic
 * Date: 10/8/11
 * Time: 12:18 AM
 */
public class Image {

    private String id;
    private String type;
    private String url;
    private String size;
    private int height;
    private int width;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
