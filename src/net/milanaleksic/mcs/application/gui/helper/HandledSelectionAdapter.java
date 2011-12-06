package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.util.ApplicationException;
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
            box.setMessage(String.format("Error (%s): %s", exc.getClass().getCanonicalName(), exc.getMessage()));
            box.setText("Error");
            box.open();
        } catch (Throwable t) {
            MessageBox box = new MessageBox(parent, SWT.ICON_ERROR);
            box.setMessage(String.format("Unexpected Error (%s): %s", t.getClass().getCanonicalName(), t.getMessage()));
            box.setText("Unexpected Error");
            box.open();
        }
    }

}
