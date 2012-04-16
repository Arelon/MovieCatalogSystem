package net.milanaleksic.mcs.infrastructure.thumbnail.impl;

import com.google.common.base.Optional;
import org.eclipse.swt.graphics.Image;

/**
* User: Milan Aleksic
* Date: 3/17/12
* Time: 1:58 PM
*/
public interface ImageTargetWidget {

    public Optional<String> getImdbId();

    void safeSetImage(Optional<Image> image, String imdbId);

    void setImage(Image image);
}
