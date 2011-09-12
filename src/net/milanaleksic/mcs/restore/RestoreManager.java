package net.milanaleksic.mcs.restore;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.event.LifecycleListener;
import net.milanaleksic.mcs.gui.ClosingForm;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:11 PM
 */
public class RestoreManager implements LifecycleListener {

    @Inject private ApplicationManager applicationManager;

    @Inject private RestorePointCreator restorePointCreator;

    @Inject private RestorePointRestorer restorePointRestorer;

    @Override public void applicationStarted() {
        if (applicationManager.getProgramArgs().isNoRestorationProcessing())
            return;
        restorePointRestorer.restoreDatabaseIfNeeded();
    }

    @Override public void applicationShutdown() {
        if (applicationManager.getProgramArgs().isNoRestorationProcessing())
            return;
        if (applicationManager.getApplicationConfiguration().getDatabaseConfiguration().isDatabaseCreateRestore()) {
            new ClosingForm();
            restorePointCreator.createRestorePoint();
        }
    }

}
