package net.milanaleksic.mcs.infrastructure.guitransformer.providers;

import net.milanaleksic.mcs.infrastructure.image.ImageRepository;
import org.eclipse.swt.graphics.Image;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 1:07 PM
 */
public class ImageProvider implements net.milanaleksic.guitransformer.providers.ImageProvider {

    @Inject
    private ImageRepository imageRepository;

    @Override
    public Image provideImageForName(String imageName) {
        return imageRepository.getResourceImage(imageName);
    }
}
