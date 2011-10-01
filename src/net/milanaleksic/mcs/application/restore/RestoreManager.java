package net.milanaleksic.mcs.application.restore;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.LifecycleListener;
import net.milanaleksic.mcs.application.gui.ClosingForm;
import net.milanaleksic.mcs.infrastructure.restore.RestorePointCreator;
import net.milanaleksic.mcs.infrastructure.restore.RestorePointRestorer;

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
        restorePointCreator.setDbVersion(applicationManager.getApplicationConfiguration().getDatabaseConfiguration().getDBVersion());
        restorePointRestorer.setDbVersion(applicationManager.getApplicationConfiguration().getDatabaseConfiguration().getDBVersion());
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
