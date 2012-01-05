package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 8/19/11
 * Time: 10:04 PM
 */
public abstract class HandledSelectionAdapter extends org.eclipse.swt.events.SelectionAdapter {

    private ResourceBundle bundle;

    private final Shell parent;

    public abstract void handledSelected() throws ApplicationException;

    public HandledSelectionAdapter(Shell parent, ResourceBundle bundle) {
        this.parent = parent;
        this.bundle = bundle;
    }

    @Override
    public final void widgetSelected(SelectionEvent event) {
        try {
            handledSelected();
        } catch(ApplicationException exc) {
            MessageBox box = new MessageBox(parent, SWT.ICON_ERROR);
            box.setMessage(String.format(bundle.getString("global.applicationError"), exc.getClass().getCanonicalName(), exc.getMessage()));
            box.setText("Error");
            box.open();
        } catch (Throwable t) {
            MessageBox box = new MessageBox(parent, SWT.ICON_ERROR);
            box.setMessage(String.format(bundle.getString("global.unexpectedError"), t.getClass().getCanonicalName(), t.getMessage()));
            box.setText("Unexpected Error");
            box.open();
        }
    }

}
