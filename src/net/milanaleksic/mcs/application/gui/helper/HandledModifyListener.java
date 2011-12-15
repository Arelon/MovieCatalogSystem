package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public abstract class HandledModifyListener implements ModifyListener {

    private final Shell parent;

    public abstract void handledModifyText() throws ApplicationException;

    public HandledModifyListener(Shell parent) {
        this.parent = parent;
    }

    @Override
    public final void modifyText(ModifyEvent event) {
        try {
            handledModifyText();
        } catch (ApplicationException exc) {
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
