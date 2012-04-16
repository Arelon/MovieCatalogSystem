package net.milanaleksic.mcs.infrastructure.image;

import com.google.common.base.Optional;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * User: Milan Aleksic
 * Date: 3/25/12
 * Time: 8:59 AM
 */
public interface ImageRepository {

    public Optional<Image> getImageByPath(String imageName);

    public Image getResourceImage(String imageResource);

    /**
     * Puts the image behind the path in the memory cache.
     * @param absolutePath physical path of image file for caching
     * @return true if at least one item was removed from cache during introduction of the requested item
     */
    public boolean cacheExternalImageFile(String absolutePath);

}
