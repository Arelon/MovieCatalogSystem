package net.milanaleksic.mcs.infrastructure.image.impl;

import com.google.common.base.*;
import com.google.common.cache.*;
import net.milanaleksic.mcs.infrastructure.image.ImageRepository;
import net.milanaleksic.mcs.infrastructure.util.*;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.springframework.beans.BeansException;
import org.springframework.context.*;
import org.springframework.jmx.export.annotation.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * User: Milan Aleksic
 * Date: 3/25/12
 * Time: 10:03 AM
 */
@ManagedResource(
        objectName = "net.milanaleksic.mcs:name=imageRepository",
        description = "Application image repository",
        currencyTimeLimit = -1
)
public class ImageRepositoryImpl implements ImageRepository, ApplicationContextAware {

    private static final Logger logger = Logger.getLogger(ImageRepositoryImpl.class);

    private Cache<String, ImageData> images;

    private ReentrantReadWriteLock lock;

    private int initialCapacity = 200;

    private long maximumSize = 1000;

    public ImageRepositoryImpl() {
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public Optional<Image> getImageByPath(final String imageName) {
        Lock readLock = lock.readLock();
        try {
            return Optional.of(new Image(Display.getDefault(), images.get(imageName, new Callable<ImageData>() {
                @Override
                public ImageData call() throws Exception {
                    return RuntimeUtil.promoteReadLockToWriteLockAndProcess(lock, new Supplier<ImageData>() {
                        @Override
                        public ImageData get() {
                            ImageData imageData = new ImageLoader().load(imageName)[0];
                            images.put(imageName, imageData);
                            return imageData;
                        }
                    });
                }
            })));
        } catch (SWTException e) {
            logger.debug("SWT Exception: ", e); //NON-NLS
            return Optional.absent();
        } catch (ExecutionException e) {
            logger.error("Execution exception", e); //NON-NLS
            return Optional.absent();
        } finally {
            if (lock.getReadHoldCount() > 0)
                readLock.unlock();
        }
    }

    @Override
    public Image getResourceImage(final String imageResource) {
        Lock readLock = lock.readLock();
        try {
            return new Image(Display.getDefault(), images.get(imageResource, new Callable<ImageData>() {
                @Override
                public ImageData call() throws Exception {
                    return RuntimeUtil.promoteReadLockToWriteLockAndProcess(lock, new Supplier<ImageData>() {
                        @Override
                        public ImageData get() {
                            try {
                                return StreamUtil.useClasspathResource(imageResource, new Function<InputStream, ImageData>() {
                                    @Override
                                    public ImageData apply(@Nullable InputStream inputStream) {
                                        ImageData imageData = new ImageLoader().load(inputStream)[0];
                                        images.put(imageResource, imageData);
                                        return imageData;
                                    }
                                });
                            } catch (IOException e) {
                                throw new IllegalArgumentException("Resource does not exist: " + imageResource);
                            }
                        }
                    });
                }
            }));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.getReadHoldCount() > 0)
                readLock.unlock();
        }
    }

    @Override
    @ManagedOperation(description = "Explicitly cache an image using absolute path")
    public boolean cacheExternalImageFile(String absolutePath) {
        long begin = images.stats().evictionCount();
        images.put(absolutePath, new ImageLoader().load(absolutePath)[0]);
        long end = images.stats().evictionCount();
        return end - begin > 0;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        images = CacheBuilder
                .newBuilder()
                .maximumSize(maximumSize)
                .initialCapacity(initialCapacity)
//            .removalListener(new RemovalListener<String, ImageData>() {
//                @Override
//                public void onRemoval(RemovalNotification<String, ImageData> removalNotification) {
//                    logger.debug("Removed from image cache: "+removalNotification.getKey()); //NON-NLS
//                }
//            })
                .build();
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    @ManagedAttribute(description = "Initial cache capacity")
    public int getInitialCapacity() {
        return initialCapacity;
    }

    @ManagedAttribute(description = "Maximum cache size")
    public long getMaximumSize() {
        return maximumSize;
    }

    @ManagedAttribute(description = "Approximate cache size")
    public long getCurrentSize() {
        return images.size();
    }

    @ManagedAttribute(description = "Cache hit rate")
    public double getHitRate() {
        return images.stats().hitRate();
    }

}
