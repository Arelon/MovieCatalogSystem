package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.util.ApplicationException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.util.ResourceBundle;

public abstract class HandledRunnable implements Runnable {

    private ResourceBundle bundle;

    private final Shell parent;

    private static final Logger logger = Logger.getLogger(HandledRunnable.class);

    public HandledRunnable(Shell parent, ResourceBundle bundle) {
        this.parent = parent;
        this.bundle = bundle;
    }

    public abstract void handledRun() throws ApplicationException;

    @Override
    public final void run() {
        try {
            handledRun();
        } catch (ApplicationException exc) {
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
