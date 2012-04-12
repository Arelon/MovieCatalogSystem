package net.milanaleksic.mcs.infrastructure.image.impl;

import com.google.common.base.*;
import net.milanaleksic.mcs.infrastructure.image.ImageRepository;
import net.milanaleksic.mcs.infrastructure.util.RuntimeUtil;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.locks.*;

/**
 * User: Milan Aleksic
 * Date: 3/25/12
 * Time: 10:03 AM
 */
public class ImageRepositoryImpl implements ImageRepository {

    private static final Logger logger = Logger.getLogger(ImageRepositoryImpl.class);

    private HashMap<String, ImageData> images;

    private ReentrantReadWriteLock lock;

    public ImageRepositoryImpl() {
        images = new HashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public Image getImage(final String imageName) {
        Lock readLock = lock.readLock();
        try {
            Optional<ImageData> imageDataOptional = Optional.fromNullable(images.get(imageName));
            if (imageDataOptional.isPresent())
                return new Image(Display.getDefault(), imageDataOptional.get());
            return RuntimeUtil.promoteReadLockToWriteLockAndProcess(lock, new Supplier<Image>() {
                @Override
                public Image get() {
                    Optional<ImageData> imageDataOptional = Optional.fromNullable(images.get(imageName));
                    if (!imageDataOptional.isPresent()) {
                        ImageData data = cacheImageDataForImage(imageName);
                        return new Image(Display.getDefault(), data);
                    }
                    return new Image(Display.getDefault(), imageDataOptional.get());
                }
            });
        } catch (SWTException e) {
            logger.debug("SWT Exception: ", e); //NON-NLS
            return null;
        } finally {
            if (lock.getReadHoldCount() > 0)
                readLock.unlock();
        }
    }

    @Override
    public Image getResourceImage(final String imageResource) {
        Lock readLock = lock.readLock();
        try {
            Optional<ImageData> imageDataOptional = Optional.fromNullable(images.get(imageResource));
            if (imageDataOptional.isPresent())
                return new Image(Display.getDefault(), imageDataOptional.get());
            return RuntimeUtil.promoteReadLockToWriteLockAndProcess(lock, new Supplier<Image>() {
                @Override
                public Image get() {
                    Optional<ImageData> imageDataOptional = Optional.fromNullable(images.get(imageResource));
                    if (imageDataOptional.isPresent())
                        return new Image(Display.getDefault(), imageDataOptional.get());
                    Image image;
                    try {
                        image = StreamUtil.useClasspathResource(imageResource, new Function<InputStream, Image>() {
                            @Override
                            public Image apply(@Nullable InputStream inputStream) {
                                return new Image(Display.getDefault(), inputStream);
                            }
                        });
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Resource does not exist: "+imageResource);
                    }
                    images.put(imageResource, image.getImageData());
                    return image;
                }
            });
        } finally {
            if (lock.getReadHoldCount() > 0)
                readLock.unlock();
        }
    }

    @Override
    public ImageData cacheImageDataForImage(String absolutePath) {
        ImageLoader imageLoader = new ImageLoader();
        ImageData imageData = imageLoader.load(absolutePath)[0];
        images.put(absolutePath, imageData);
        return imageData;
    }

}
