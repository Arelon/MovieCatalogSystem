package net.milanaleksic.mcs.application.gui.helper.event;

import org.eclipse.swt.internal.SWTEventListener;

public interface MovieSelectionListener extends SWTEventListener {

    /**
     * Sent when movie has been selected in the CoolMovieComposite component.
     * @param e an event containing information about the selection
     */
    public void movieSelected(MovieSelectionEvent e);

    /**
     * Sent when details for a movie have been requested in the CoolMovieComposite component.
     * @param e an event containing information about the selection
     */
    public void movieDetailsSelected(MovieSelectionEvent e);

}
