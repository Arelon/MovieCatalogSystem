package net.milanaleksic.mcs.infrastructure.thumbnail.impl;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.gui.helper.ShowImageComposite;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import javax.annotation.Nullable;

/**
 * User: Milan Aleksic
 * Date: 3/17/12
 * Time: 1:58 PM
 */
class CompositeImageTargetWidget implements ImageTargetWidget {

    private final ShowImageComposite composite;
    private final Optional<String> imdbId;

    public CompositeImageTargetWidget(ShowImageComposite composite, @Nullable String imdbId) {
        this.composite = composite;
        this.imdbId = Optional.fromNullable(imdbId);
    }

    public Optional<String> getImdbId() {
        return imdbId;
    }

    @Override
    public void setImageFromExternalFile(String absoluteFileLocation) {
        composite.setImage(new Image(Display.getCurrent(), absoluteFileLocation));
    }

    @Override
    public void setImageFromResource(String imageResource) {
        SWTUtil.setImageOnTarget(composite, imageResource);
    }

    @Override
    public void safeSetImage(Optional<Image> image, String imdbId) {
        if (composite.isDisposed())
            return;
        if (!this.imdbId.isPresent() || !this.imdbId.get().equals(imdbId))
            return;
        composite.setImage(image.orNull());
    }

}
