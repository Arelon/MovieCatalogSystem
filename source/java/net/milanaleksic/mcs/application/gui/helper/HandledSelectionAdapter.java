package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.util.ApplicationException;
import org.apache.log4j.Logger;
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

    private static final Logger logger = Logger.getLogger(HandledSelectionAdapter.class);

    public abstract void handledSelected(SelectionEvent event) throws ApplicationException;

    public HandledSelectionAdapter(Shell parent, ResourceBundle bundle) {
        this.parent = parent;
        this.bundle = bundle;
    }

    @Override
    public final void widgetSelected(SelectionEvent event) {
        try {
            handledSelected(event);
        } catch(ApplicationException exc) {
            logger.error(exc.getMessage(), exc);
            MessageBox box = new MessageBox(parent, SWT.ICON_ERROR);
            box.setMessage(String.format(bundle.getString("global.applicationErrorTemplate"), exc.getClass().getCanonicalName(), exc.getMessage()));
            box.setText(bundle.getString("global.error"));
            box.open();
        } catch (Throwable t) {
            logger.error("Unexpected error: "+t.getMessage(), t); //NON-NLS
            MessageBox box = new MessageBox(parent, SWT.ICON_ERROR);
            box.setMessage(String.format(bundle.getString("global.unexpectedErrorTemplate"), t.getClass().getCanonicalName(), t.getMessage()));
            box.setText(bundle.getString("global.unexpectedError"));
            box.open();
        }
    }

}
