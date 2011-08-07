package net.milanaleksic.mcs.restore;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.event.LifecycleListener;
import net.milanaleksic.mcs.gui.ClosingForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:11 PM
 */
public class RestoreManager implements LifecycleListener{

    @Autowired private ApplicationManager applicationManager;

    @Autowired private RestorePointService restorePointService;

    @Override public void applicationStarted() {
        if (!restorePointService.validateDatabase()) {
             restorePointService.restoreDatabase();
        }
    }

    @Override public void applicationShutdown() {
        if (ApplicationManager.getApplicationConfiguration().getDatabaseConfiguration().isDatabaseCreateRestore()) {
            new ClosingForm();
            restorePointService.createRestorePoint();
        }
    }
}
