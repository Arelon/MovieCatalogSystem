package net.milanaleksic.mcs.restore;

import net.milanaleksic.mcs.event.LifecycleListener;
import net.milanaleksic.mcs.gui.ClosingForm;
import net.milanaleksic.mcs.util.MCSProperties;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:11 PM
 */
public class RestoreManager implements LifecycleListener{

    @Override public void applicationStarted() {
    }

    @Override public void applicationShutdown() {
        if (MCSProperties.getDatabaseCreateRestore()) {
            new ClosingForm();
            new RestorePointCreator().createRestorePoint();
        }
    }
}
