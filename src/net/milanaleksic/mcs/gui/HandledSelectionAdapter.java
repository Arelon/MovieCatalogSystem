package net.milanaleksic.mcs.gui;

import net.milanaleksic.mcs.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * User: Milan Aleksic
 * Date: 8/19/11
 * Time: 10:04 PM
 */
public abstract class HandledSelectionAdapter extends org.eclipse.swt.events.SelectionAdapter {

    private Shell parent;

    public abstract void handledSelected(SelectionEvent event) throws ApplicationException;

    public HandledSelectionAdapter(Shell parent) {
        this.parent = parent;
    }

    @Override
    public final void widgetSelected(SelectionEvent event) {
        try {
            handledSelected(event);
        } catch(ApplicationException exc) {
            MessageBox box = new MessageBox(parent, SWT.ICON_ERROR);
            box.setMessage("Error: " + exc.getMessage());
            box.setText("Error");
            box.open();
        }
    }

}
