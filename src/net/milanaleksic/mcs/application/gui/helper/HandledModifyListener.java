package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.util.ResourceBundle;

public abstract class HandledModifyListener implements ModifyListener {

    private ResourceBundle bundle;

    private final Shell parent;

    public abstract void handledModifyText() throws ApplicationException;

    public HandledModifyListener(Shell parent, ResourceBundle bundle) {
        this.parent = parent;
        this.bundle = bundle;
    }

    @Override
    public final void modifyText(ModifyEvent event) {
        try {
            handledModifyText();
        } catch (ApplicationException exc) {
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
