package net.milanaleksic.mcs.infrastructure.thumbnail;

import net.milanaleksic.mcs.application.gui.helper.ShowImageComposite;
import net.milanaleksic.mcs.infrastructure.thumbnail.impl.ImageTargetWidget;
import org.eclipse.swt.widgets.TableItem;

public interface ThumbnailManager {

    void setThumbnailForItem(TableItem item) ;

    void setThumbnailForShowImageComposite(ShowImageComposite posterImage, String imdbId);

    void setThumbnailOnWidget(ImageTargetWidget imageTargetWidget);

}
