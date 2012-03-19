package net.milanaleksic.mcs.infrastructure.thumbnail.impl;

import org.eclipse.swt.graphics.Image;

/**
* User: Milan Aleksic
* Date: 3/17/12
* Time: 1:58 PM
*/
public interface ImageTargetWidget {

    public String getImdbId();

    void setImageFromExternalFile(String absoluteFileLocation);

    void setImageFromResource(String defaultImageResource);

    void safeSetImage(Image image, String imdbId);
}
