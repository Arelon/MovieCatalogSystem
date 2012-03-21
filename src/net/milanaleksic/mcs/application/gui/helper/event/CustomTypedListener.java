package net.milanaleksic.mcs.application.gui.helper.event;

import net.milanaleksic.mcs.application.gui.helper.CoolMovieComposite;
import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TypedListener;

public class CustomTypedListener extends TypedListener {

    public CustomTypedListener(SWTEventListener listener) {
        super(listener);
    }

    @Override
    public void handleEvent(Event e) {
        switch (e.type) {
            case CoolMovieComposite.EventMovieSelected: {
                ((MovieSelectionListener) eventListener).movieSelected(new MovieSelectionEvent(e));
                return;
            }
            case CoolMovieComposite.EventMovieDetailsSelected: {
                ((MovieSelectionListener) eventListener).movieDetailsSelected(new MovieSelectionEvent(e));
                return;
            }
        }
        super.handleEvent(e);
    }
}
