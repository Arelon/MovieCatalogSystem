package net.milanaleksic.mcs.infrastructure.image;

import org.eclipse.swt.graphics.Image;

/**
 * User: Milan Aleksic
 * Date: 3/25/12
 * Time: 8:59 AM
 */
public interface ImageRepository {

    public Image getImage(String imageName);

    public Image getResourceImage(String imageResource);
}
