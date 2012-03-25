package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Function;
import net.milanaleksic.mcs.application.ApplicationManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
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

    private Function<AbstractDialogForm, Void> runWhenClosing = null;

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
     *
     * @return true if shell should be allowed to close
     */
    protected boolean onShouldShellClose() {
        return true;
    }

    public void open() {
        open(null, (Function<AbstractDialogForm, Void>)null);
    }

    public void open(Shell parent) {
        open(parent, (Function<AbstractDialogForm, Void>)null);
    }

    public void open(@Nullable Shell parent, @Nullable final Runnable runWhenClosing) {
        open(parent, new Function<AbstractDialogForm, Void>() {
            @Override
            public Void apply(@Nullable AbstractDialogForm abstractDialogForm) {
                if (runWhenClosing != null)
                    runWhenClosing.run();
                return null;
            }
        });
    }

    public void open(@Nullable Shell parent, @Nullable Function<AbstractDialogForm, Void> runWhenClosing) {
        this.parent = parent;
        this.runWhenClosing = runWhenClosing;
        this.runnerWhenClosingShouldRun = false;
        bundle = applicationManager.getMessagesBundle();
        createShell(parent);
    }

    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    protected void createShell(Shell parent) {
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        onShellCreated();

        if (!noReadyEvent)
            onShellReady();

        if (shell.isDisposed())
            return;

        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                if (!onShouldShellClose()) {
                    e.doit = false;
                    return;
                }
                if (runnerWhenClosingShouldRun) {
                    if (AbstractDialogForm.this.runWhenClosing == null)
                        throw new IllegalStateException("Value runnerWhenClosingShouldRun set to true, but no valid runner registered!");
                    AbstractDialogForm.this.runWhenClosing.apply(AbstractDialogForm.this);
                }
                shell.dispose();
            }
        });

        shell.pack();
        if (parent != null) {
            shell.setLocation(new Point(
                    parent.getLocation().x + Math.abs(parent.getSize().x - shell.getSize().x) / 2,
                    parent.getLocation().y + Math.abs(parent.getSize().y - shell.getSize().y) / 2)
            );
        }
        shell.open();
    }

    public boolean isDisposed() {
        return shell == null || shell.isDisposed();
    }

    public void setNoReadyEvent(boolean noReadyEvent) {
        this.noReadyEvent = noReadyEvent;
    }
}
