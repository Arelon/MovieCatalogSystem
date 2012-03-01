package net.milanaleksic.mcs.infrastructure.util;

import com.google.common.base.Function;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * User: Milan Aleksic
 * Date: 3/1/12
 * Time: 9:44 AM
 */
public class SWTUtil {

    private static final Logger log = Logger.getLogger(SWTUtil.class);

    public static void setImageOnTarget(Decorations target, String resourceLocation) {
        Image image;
        try {
            image = StreamUtil.useClasspathResource(resourceLocation, new Function<InputStream, Image>() {
                @Override
                public Image apply(@Nullable InputStream inputStream) {
                    return new Image(Display.getCurrent(), inputStream);
                }
            });
//        NOTE: DO NOT DISPOSE THE IMAGE OBJECT, IT WILL BE DISPOSED BY THE WIDGET CODE
            target.setImage(image);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unexpected exception while working with the Image", e);
        }
    }

    public static void setImageOnTarget(ToolItem target, String resourceLocation) {
        Image image;
        try {
            image = StreamUtil.useClasspathResource(resourceLocation, new Function<InputStream, Image>() {
                @Override
                public Image apply(@Nullable InputStream inputStream) {
                    return new Image(Display.getCurrent(), inputStream);
                }
            });
//        NOTE: DO NOT DISPOSE THE IMAGE OBJECT, IT WILL BE DISPOSED BY THE WIDGET CODE
            target.setImage(image);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unexpected exception while working with the Image", e);
        }
    }

    public static void useImageAndThenDispose(String resourceLocation, Function<Image, Void> callback) {
        Image image = null;
        try {
            image = StreamUtil.useClasspathResource(resourceLocation, new Function<InputStream, Image>() {
                @Override
                public Image apply(@Nullable InputStream inputStream) {
                    return new Image(Display.getCurrent(), inputStream);
                }
            });
            callback.apply(image);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unexpected exception while working with the Image", e);
        } finally {
            if (image != null)
                image.dispose();
        }
    }

    public static void createImageFromUrl(final URI uri, PersistentHttpContext persistentHttpContext, Function<Image, Void> callback) {
        try {
            final long begin = System.currentTimeMillis();
            Image image = StreamUtil.useURIResource(uri, persistentHttpContext, new Function<InputStream, Image>() {
                @Override
                public Image apply(@Nullable InputStream inputStream) {
                    if (log.isDebugEnabled())
                        log.debug("Image "+uri+" downloaded in "+(System.currentTimeMillis()-begin)+"ms");
                    return new Image(Display.getCurrent(), inputStream);
                }
            });
            callback.apply(image);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unexpected exception while working with the Image", e);
        }
    }

}
