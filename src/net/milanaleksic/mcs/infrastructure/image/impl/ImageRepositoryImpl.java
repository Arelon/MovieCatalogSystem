package net.milanaleksic.mcs.infrastructure.image.impl;

import com.google.common.base.Function;
import net.milanaleksic.mcs.infrastructure.image.ImageRepository;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
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
    public Image getImage(String imageName) {
        Lock readLock = lock.readLock();
        try {
            ImageData imageData = images.get(imageName);
            if (imageData == null) {
                final int holdCount = lock.getReadHoldCount();
                for (int i = 0; i < holdCount; i++) {
                    readLock.unlock();
                }
                Lock writeLock = lock.writeLock();
                writeLock.lock();
                try {
                    imageData = images.get(imageName);
                    if (imageData == null) {
                        Image image = new Image(Display.getDefault(), imageName);
                        images.put(imageName, image.getImageData());
                        return image;
                    }
                    return new Image(Display.getDefault(), imageData);
                } finally {
                    for (int i = 0; i < holdCount; i++) {
                        readLock.lock();
                    }
                    writeLock.unlock();
                }
            }
            return new Image(Display.getDefault(), imageData);
        } finally {
            if (lock.getReadHoldCount() > 0)
                readLock.unlock();
        }
    }

    @Override
    public Image getResourceImage(String imageResource) {
        Lock readLock = lock.readLock();
        try {
            ImageData imageData = images.get(imageResource);
            if (imageData == null) {
                final int holdCount = lock.getReadHoldCount();
                for (int i = 0; i < holdCount; i++) {
                    readLock.unlock();
                }
                Lock writeLock = lock.writeLock();
                writeLock.lock();
                try {
                    imageData = images.get(imageResource);
                    if (imageData == null) {
                        try {
                            Image image = StreamUtil.useClasspathResource(imageResource, new Function<InputStream, Image>() {
                                @Override
                                public Image apply(@Nullable InputStream inputStream) {
                                    return new Image(Display.getDefault(), inputStream);
                                }
                            });
                            images.put(imageResource, image.getImageData());
                            return image;
                        } catch (IOException e) {
                            logger.error("Error reading image details: " + imageResource);
                            return null;
                        }
                    }
                    return new Image(Display.getDefault(), imageData);
                } finally {
                    for (int i = 0; i < holdCount; i++) {
                        readLock.lock();
                    }
                    writeLock.unlock();
                }
            }
            return new Image(Display.getDefault(), imageData);
        } finally {
            if (lock.getReadHoldCount() > 0)
                readLock.unlock();
        }
    }

}
