package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.base.Optional;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;

import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 3/1/12
 * Time: 11:32 AM
 */
public class ShowImageComposite extends Composite implements PaintListener {

    private ResourceBundle bundle;

    private Optional<Image> image;
    private Optional<String> status;

    public ShowImageComposite(Composite parent, int style, ResourceBundle bundle) {
        super(parent, style);
        this.bundle = bundle;
        image = Optional.absent();
        status = Optional.of(bundle.getString("global.noImagePresent"));
        addPaintListener(this);
    }

    @Override
    public void paintControl(PaintEvent paintEvent) {
        GC gc = paintEvent.gc;
        if (!image.isPresent()) {
            paintMessageWhenNoImageExists(paintEvent);
            return;
        }
        Rectangle bounds = getDrawableBounds();
        Image image = this.image.get();
        gc.drawImage(image, (bounds.width-image.getBounds().width) / 2, (bounds.height-image.getBounds().height) / 2);
    }

    private Rectangle getDrawableBounds() {
        Rectangle ofTheJedi = this.getClientArea();
        ofTheJedi.width -= 1;
        ofTheJedi.height -= 1;
        return ofTheJedi;
    }

    private void paintMessageWhenNoImageExists(PaintEvent paintEvent) {
        GC gc = paintEvent.gc;
        Color color = new Color(getDisplay(), 0, 0, 0);
        gc.setForeground(color);
        Rectangle bounds = getDrawableBounds();
        gc.drawRectangle(bounds);
        color.dispose();

        Point point = gc.textExtent(status.get());
        gc.drawText(status.get(), (bounds.width - point.x) / 2, (bounds.height - point.y) / 2);
    }

    public void setImage(Optional<Image> image) {
        disposeImage();
        this.image = image;
        redraw();
    }

    private void disposeImage() {
        if (image.isPresent() && !image.get().isDisposed()) {
            image.get().dispose();
            image = Optional.absent();
        }
    }

    @Override
    public void dispose() {
        disposeImage();
        super.dispose();
    }

    public void setStatus(Optional<String> status) {
        this.status = status.or(Optional.of(bundle.getString("global.noImagePresent")));
        redraw();
    }
}
