package net.milanaleksic.mcs.application.restore;

import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.LifeCycleListener;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.restore.RestorePointCreator;
import net.milanaleksic.mcs.infrastructure.restore.RestorePointRestorer;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:11 PM
 */
public class RestoreManager implements LifeCycleListener {

    @Inject private ProgramArgsService programArgsService;

    @Inject private RestorePointCreator restorePointCreator;

    @Inject private RestorePointRestorer restorePointRestorer;

    private ApplicationConfiguration configuration;

    @Override public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        this.configuration = configuration;
        if (programArgsService.getProgramArgs().isNoRestorationProcessing())
            return;
        restorePointRestorer.restoreDatabaseIfNeeded();
    }

    @Override public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        if (programArgsService.getProgramArgs().isNoRestorationProcessing())
            return;
        if (configuration.getDatabaseConfiguration().isDatabaseCreateRestore()) {
            restorePointCreator.createRestorePoint();
        }
    }

}
