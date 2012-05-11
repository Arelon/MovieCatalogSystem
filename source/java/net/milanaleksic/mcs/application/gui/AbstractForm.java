package net.milanaleksic.mcs.application.gui;

import com.google.common.base.*;
import net.milanaleksic.mcs.infrastructure.messages.ResourceBundleSource;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ResourceBundle;

public abstract class AbstractForm implements Form {

    protected final Logger logger = Logger.getLogger(this.getClass());

    @Inject
    protected ResourceBundleSource resourceBundleSource;

    protected Shell shell;
    protected Optional<Shell> parent = Optional.absent();

    private Optional<Function<AbstractForm, Void>> runWhenClosing = Optional.absent();

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
        open(null, (Function<AbstractForm, Void>)null);
    }

    public void open(Shell parent) {
        open(parent, (Function<AbstractForm, Void>)null);
    }

    public void open(@Nullable Shell parent, @Nullable final Runnable runWhenClosing) {
        open(parent, new Function<AbstractForm, Void>() {
            @Override
            public Void apply(@Nullable AbstractForm abstractForm) {
                if (runWhenClosing != null)
                    runWhenClosing.run();
                return null;
            }
        });
    }

    public void open(@Nullable Shell parent, @Nullable Function<AbstractForm, Void> runWhenClosing) {
        this.runWhenClosing = Optional.fromNullable(runWhenClosing);
        this.runnerWhenClosingShouldRun = false;
        prepareShell(parent);
        if (!shell.isDisposed())
            shell.open();
    }

    public void setResourceBundleSource(ResourceBundleSource resourceBundleSource) {
        this.resourceBundleSource = resourceBundleSource;
    }

    protected void prepareShell(Shell parent) {
        bundle = resourceBundleSource.getMessagesBundle();
        this.parent = Optional.fromNullable(parent);

        createShell();

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
                    if (!AbstractForm.this.runWhenClosing.isPresent())
                        throw new IllegalStateException("Value runnerWhenClosingShouldRun set to true, but no valid runner registered!");
                    AbstractForm.this.runWhenClosing.get().apply(AbstractForm.this);
                }
                shell.dispose();
            }
        });

        shell.pack();
        if (this.parent.isPresent()) {
            Shell parentShell = this.parent.get();
            shell.setLocation(new Point(
                    parentShell.getLocation().x + (parentShell.getSize().x - shell.getSize().x) / 2,
                    parentShell.getLocation().y + (parentShell.getSize().y - shell.getSize().y) / 2)
            );
        }
    }

    protected void createShell() {
        shell = new Shell(this.parent.orNull(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    public boolean isDisposed() {
        return shell == null || shell.isDisposed();
    }

    public void setNoReadyEvent(boolean noReadyEvent) {
        this.noReadyEvent = noReadyEvent;
    }

    @Override
    public ResourceBundle getResourceBundle() {
        return bundle;
    }

    @Override
    public Shell getShell() {
        return shell;
    }
}
