package net.milanaleksic.mcs.application.gui.helper;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nullable;
import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 3/1/12
 * Time: 11:32 AM
 */
public class ShowImageComposite extends Composite implements PaintListener {

    private ResourceBundle bundle;

    private Image image;
    private String status;

    public ShowImageComposite(Composite parent, int style, ResourceBundle bundle) {
        super(parent, style);
        this.bundle = bundle;
        addPaintListener(this);
    }

    @Override
    public void paintControl(PaintEvent paintEvent) {
        GC gc = paintEvent.gc;
        if (image == null) {
            paintMessageWhenNoImageExists(paintEvent);
            return;
        }
        Rectangle bounds = getDrawableBounds();
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

        if (status == null)
            status = bundle.getString("global.noImagePresent");
        Point point = gc.textExtent(status);
        gc.drawText(status, (bounds.width - point.x) / 2, (bounds.height - point.y) / 2);
    }

    public void setImage(@Nullable Image image) {
        disposeImage();
        this.image = image;
        redraw();
    }

    private void disposeImage() {
        if (image != null && !image.isDisposed()) {
            image.dispose();
            image = null;
        }
    }

    @Override
    public void dispose() {
        disposeImage();
        super.dispose();
    }

    public void setStatus(@Nullable String status) {
        this.status = status;
        redraw();
    }
}
