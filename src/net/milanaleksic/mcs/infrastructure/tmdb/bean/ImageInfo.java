package net.milanaleksic.mcs.infrastructure.tmdb.bean;

/**
 * User: Milan Aleksic
 * Date: 10/8/11
 * Time: 12:18 AM
 */
public class ImageInfo {

    private Image image;

    public Image getImage() {
        return image;
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    @Override
    public String toString() {
        return "ImageInfo{" +
                "image=" + image +
                '}';
    }
}
