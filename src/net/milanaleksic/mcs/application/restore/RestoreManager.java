package net.milanaleksic.mcs.application.restore;

import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.application.gui.ClosingDialogForm;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.restore.RestorePointCreator;
import net.milanaleksic.mcs.infrastructure.restore.RestorePointRestorer;
import org.eclipse.swt.widgets.Display;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:11 PM
 */
public class RestoreManager implements LifecycleListener {

    @Inject private ProgramArgsService programArgsService;

    @Inject private RestorePointCreator restorePointCreator;

    @Inject private RestorePointRestorer restorePointRestorer;

    @Inject private ClosingDialogForm closingDialogForm;

    private ApplicationConfiguration configuration;

    @Override public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        this.configuration = configuration;
        if (programArgsService.getProgramArgs().isNoRestorationProcessing())
            return;
        restorePointCreator.setDbVersion(configuration.getDatabaseConfiguration().getDbVersion());
        restorePointRestorer.setDbVersion(configuration.getDatabaseConfiguration().getDbVersion());
        restorePointRestorer.restoreDatabaseIfNeeded();
    }

    @Override public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        if (programArgsService.getProgramArgs().isNoRestorationProcessing())
            return;
        if (configuration.getDatabaseConfiguration().isDatabaseCreateRestore()) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    closingDialogForm.open();
                }
            });
            restorePointCreator.createRestorePoint();
        }
    }

}
