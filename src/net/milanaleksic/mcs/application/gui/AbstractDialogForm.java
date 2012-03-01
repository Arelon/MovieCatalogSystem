package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.ApplicationManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ResourceBundle;

public abstract class AbstractDialogForm {

    protected final Logger logger = Logger.getLogger(this.getClass());

    @Inject
    protected ApplicationManager applicationManager;

    protected Shell shell = null;
    protected Shell parent = null;

    private Runnable runWhenClosing = null;

    protected ResourceBundle bundle;

    protected boolean runnerWhenClosingShouldRun;
    private boolean noReadyEvent = false;

    protected abstract void onShellCreated();

    /**
     * This method should be inherited in case you wish to handle moment after all UI creation has finished
     */
    protected void onShellReady() {
    }

    /**
     * This method should be inherited in case you wish to handle moment before shell gets disposed
     * @return true if shell should be allowed to close
     */
    protected boolean onShouldShellClose() {
        return true;
    }

    public void open() {
        open(null, null);
    }

    public void open(Shell parent) {
        open(parent, null);
    }

    public void open(@Nullable Shell parent, @Nullable Runnable runWhenClosing) {
        this.parent = parent;
        this.runWhenClosing = runWhenClosing;
        this.runnerWhenClosingShouldRun = false;
        bundle = applicationManager.getMessagesBundle();
        createShell(parent);
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                if (!onShouldShellClose()) {
                    e.doit = false;
                    return;
                }
                if (runnerWhenClosingShouldRun) {
                    if (AbstractDialogForm.this.runWhenClosing == null)
                        throw new IllegalStateException("Value runnerWhenClosingShouldRun set to true, but no valid runner registered!");
                    AbstractDialogForm.this.runWhenClosing.run();
                }
                shell.dispose();
            }
        });

        onShellCreated();

        if (!noReadyEvent)
            onShellReady();

        shell.pack();
        shell.open();
    }

    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    protected void createShell(Shell parent) {
        if (parent == null)
            shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        else {
            shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            shell.setLocation(new Point(parent.getLocation().x + Math.abs(parent.getSize().x - shell.getSize().x) / 2, parent.getLocation().y
                    + Math.abs(parent.getSize().y - shell.getSize().y) / 2));
        }
    }

    public boolean isDisposed() {
        return shell == null || shell.isDisposed();
    }

    public void setNoReadyEvent(boolean noReadyEvent) {
        this.noReadyEvent = noReadyEvent;
    }
}
