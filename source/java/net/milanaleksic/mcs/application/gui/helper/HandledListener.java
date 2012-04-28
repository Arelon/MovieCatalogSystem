package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.gui.Form;
import net.milanaleksic.mcs.application.util.ApplicationException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * User: Milan Aleksic
 * Date: 4/23/12
 * Time: 8:54 AM
 */
public abstract class HandledListener implements Listener {

    private static final Logger log = Logger.getLogger(HandledListener.class);

    private Form form;

    public HandledListener(Form form) {
        this.form = form;
    }

    @Override
    public final void handleEvent(Event event) {
        try {
            safeHandleEvent(event);
        } catch (ApplicationException exc) {
            log.error(exc.getMessage(), exc);
            MessageBox box = new MessageBox(form.getShell(), SWT.ICON_ERROR);
            box.setMessage(String.format(form.getResourceBundle().getString("global.applicationErrorTemplate"), exc.getClass().getCanonicalName(), exc.getMessage()));
            box.setText(form.getResourceBundle().getString("global.error"));
            box.open();
        } catch (Throwable t) {
            log.error("Unexpected error: " + t.getMessage(), t); //NON-NLS
            MessageBox box = new MessageBox(form.getShell(), SWT.ICON_ERROR);
            box.setMessage(String.format(form.getResourceBundle().getString("global.unexpectedErrorTemplate"), t.getClass().getCanonicalName(), t.getMessage()));
            box.setText(form.getResourceBundle().getString("global.unexpectedError"));
            box.open();
        }
    }

    public abstract void safeHandleEvent(Event event) throws ApplicationException;
}
