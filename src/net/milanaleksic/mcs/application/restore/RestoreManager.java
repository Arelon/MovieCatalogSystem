package net.milanaleksic.mcs.application.restore;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.LifecycleListener;
import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.application.gui.ClosingDialogForm;
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

    @Inject private ProgramArgsService programArgsService;

    @Inject private RestorePointCreator restorePointCreator;

    @Inject private RestorePointRestorer restorePointRestorer;

    @Inject private ClosingDialogForm closingDialogForm;

    @Override public void applicationStarted() {
        if (programArgsService.getProgramArgs().isNoRestorationProcessing())
            return;
        restorePointCreator.setDbVersion(applicationManager.getApplicationConfiguration().getDatabaseConfiguration().getDbVersion());
        restorePointRestorer.setDbVersion(applicationManager.getApplicationConfiguration().getDatabaseConfiguration().getDbVersion());
        restorePointRestorer.restoreDatabaseIfNeeded();
    }

    @Override public void applicationShutdown() {
        if (programArgsService.getProgramArgs().isNoRestorationProcessing())
            return;
        if (applicationManager.getApplicationConfiguration().getDatabaseConfiguration().isDatabaseCreateRestore()) {
            closingDialogForm.open();
            restorePointCreator.createRestorePoint();
        }
    }

}
