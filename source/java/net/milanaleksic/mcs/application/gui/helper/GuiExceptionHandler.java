package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.guitransformer.MethodEventListenerExceptionHandler;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.infrastructure.messages.ResourceBundleSource;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 6/28/12
 * Time: 2:46 PM
 */
public class GuiExceptionHandler implements MethodEventListenerExceptionHandler {

    private static final Logger log = Logger.getLogger(HandledListener.class);

    @Inject
    protected ResourceBundleSource resourceBundleSource;

    @Override
    public void handleException(Shell shell, Exception exc) {
        final ResourceBundle resourceBundle = resourceBundleSource.getMessagesBundle();
        if (exc instanceof ApplicationException) {
            log.error(exc.getMessage(), exc);
            MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
            box.setMessage(String.format(resourceBundle.getString("global.applicationErrorTemplate"), exc.getClass().getCanonicalName(), exc.getMessage()));
            box.setText(resourceBundle.getString("global.error"));
            box.open();
        } else {
            log.error("Unexpected error: " + exc.getMessage(), exc); //NON-NLS
            MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
            box.setMessage(String.format(resourceBundle.getString("global.unexpectedErrorTemplate"), exc.getClass().getCanonicalName(), exc.getMessage()));
            box.setText(resourceBundle.getString("global.unexpectedError"));
            box.open();
        }
    }
}
