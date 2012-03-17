package net.milanaleksic.mcs.infrastructure.thumbnail.impl;

import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

/**
 * User: Milan Aleksic
 * Date: 3/17/12
 * Time: 1:58 PM
 */
class TableItemImageTargetWidget implements ImageTargetWidget {

    private final TableItem tableItem;

    public TableItemImageTargetWidget(TableItem tableItem) {
        this.tableItem = tableItem;
    }

    public String getImdbId() {
        return getFilm().getImdbId();
    }

    private Film getFilm() {
        return ((Film) tableItem.getData());
    }

    @Override
    public void setImageFromExternalFile(String absoluteFileLocation) {
        tableItem.setImage(new Image(Display.getCurrent(), absoluteFileLocation));
    }

    @Override
    public void setImageFromResource(String imageResource) {
        SWTUtil.setImageOnTarget(tableItem, imageResource);
    }

    @Override
    public void safeSetImage(Image image, String imdbId) {
        if (tableItem.isDisposed())
            return;
        if (getFilm() == null)
            return;
        String targetImdbId = getImdbId();
        if (targetImdbId == null || !targetImdbId.equals(imdbId))
            return;
        tableItem.setImage(image);
    }

}
