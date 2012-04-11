package net.milanaleksic.mcs.infrastructure.image;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * User: Milan Aleksic
 * Date: 3/25/12
 * Time: 8:59 AM
 */
public interface ImageRepository {

    public Image getImage(String imageName);

    public Image getResourceImage(String imageResource);

    public ImageData cacheImageDataForImage(String absolutePath);
}
