package net.milanaleksic.mcs.infrastructure.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.gui.helper.ShowImageComposite;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

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
            throw new IllegalArgumentException("Unexpected exception while working with the image", e);
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
            throw new IllegalArgumentException("Unexpected exception while working with the image", e);
        }
    }

    public static void setImageOnTarget(Item target, String resourceLocation) {
        Image image;
        try {
            image = StreamUtil.useClasspathResource(resourceLocation, new Function<InputStream, Image>() {
                @Override
                public Image apply(@Nullable InputStream inputStream) {
                    return new Image(Display.getCurrent(), inputStream);
                }
            });
//        NOTE: DO NOT DISPOSE THE IMAGE OBJECT, IT WILL BE DISPOSED BY THE WIDGET CODE
            if (target.isDisposed())
                return;
            target.setImage(image);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unexpected exception while working with the image", e);
        }
    }

    public static void setImageOnTarget(ShowImageComposite target, String resourceLocation) {
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
            throw new IllegalArgumentException("Unexpected exception while working with the image", e);
        }
    }

    public static void useImageAndThenDispose(String resourceLocation, Function<Image, Void> callback) {
        Optional<Image> image = Optional.absent();
        try {
            image = Optional.of(StreamUtil.useClasspathResource(resourceLocation, new Function<InputStream, Image>() {
                @Override
                public Image apply(@Nullable InputStream inputStream) {
                    return new Image(Display.getCurrent(), inputStream);
                }
            }));
            callback.apply(image.get());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unexpected exception while working with the image", e);
        } finally {
            if (image.isPresent())
                image.get().dispose();
        }
    }

    public static void createImageFromUrl(final URI uri, PersistentHttpContext persistentHttpContext, Function<Image, Void> callback) {
        try {
            final long begin = System.currentTimeMillis();
            Image image = StreamUtil.useURIResource(uri, persistentHttpContext, new Function<InputStream, Image>() {
                @Override
                public Image apply(@Nullable InputStream inputStream) {
                    if (log.isDebugEnabled())
                        log.debug("Image " + uri + " downloaded in " + (System.currentTimeMillis() - begin) + "ms"); //NON-NLS
                    return new Image(Display.getCurrent(), inputStream);
                }
            });
            callback.apply(image);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unexpected exception while working with the image", e);
        }
    }

    public static String getTextFrom(Scrollable source) {
        if (source instanceof Text)
            return ((Text) source).getText();
        else if (source instanceof Combo)
            return ((Combo) source).getText();
        else
            throw new IllegalStateException("Not able to read from SWT class " + source.getClass().getSimpleName());
    }

    public static void addImagePaintListener(final Control target, final String resource) {
        final AtomicReference<Optional<Image>> image = new AtomicReference<>(Optional.<Image>absent());
        try {
            image.set(Optional.of(StreamUtil.useClasspathResource(resource, new Function<InputStream, Image>() {
                @Override
                public Image apply(@Nullable InputStream inputStream) {
                    return new Image(Display.getCurrent(), inputStream);
                }
            })));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unexpected exception while working with the image", e);
        }
        target.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
                event.gc.drawImage(image.get().get(), 0, 0);
            }
        });
        target.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                Optional<Image> optionalImage = image.getAndSet(Optional.<Image>absent());
                if (optionalImage.isPresent())
                    optionalImage.get().dispose();
            }
        });
    }

    public static Image resize(Image image, int canvasWidth, int canvasHeight) {
        Image scaled = new Image(Display.getDefault(), canvasWidth, canvasHeight);
        GC gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        double canvasRatio = 1.0 * canvasWidth / canvasHeight;
        double imageRatio = 1.0 * image.getBounds().width / image.getBounds().height;
        int width, height;
        if (canvasRatio > imageRatio) {
            width = (int) Math.round(imageRatio * canvasHeight);
            height = canvasHeight;
        } else if (canvasRatio < imageRatio) {
            width = canvasWidth;
            height = (int) Math.round(width / imageRatio);
        } else {
            width = canvasWidth;
            height = canvasHeight;
        }
        gc.drawImage(image, 0, 0,
                image.getBounds().width, image.getBounds().height,
                (int) Math.round(1.0 * Math.abs(canvasWidth - width) / 2),
                (int) Math.round(1.0 * Math.abs(canvasHeight - height) / 2),
                width, height);
        gc.dispose();
        return scaled;
    }

    public static FontData getSystemFontData() {
        return Display.getDefault().getSystemFont().getFontData()[0];
    }

}
