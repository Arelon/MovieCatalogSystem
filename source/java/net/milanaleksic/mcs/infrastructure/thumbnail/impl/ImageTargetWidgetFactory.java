package net.milanaleksic.mcs.infrastructure.thumbnail.impl;

import net.milanaleksic.mcs.application.gui.helper.ShowImageComposite;
import org.eclipse.swt.widgets.TableItem;

/**
* User: Milan Aleksic
* Date: 3/17/12
* Time: 1:58 PM
*/
class ImageTargetWidgetFactory {

    public static ImageTargetWidget createCompositeImageTarget(ShowImageComposite composite, String imdbId) {
        return new CompositeImageTargetWidget(composite, imdbId);
    }
    public static ImageTargetWidget createTableItemImageTarget(TableItem tableItem) {
        return new TableItemImageTargetWidget(tableItem);
    }

}
