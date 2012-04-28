package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.*;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 11:41 PM
 */
public class SplashScreenManager implements LifecycleListener {

//    private Optional<SplashScreen> splashScreen = Optional.absent();

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
//        splashScreen = Optional.fromNullable(SplashScreen.getSplashScreen());
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
    }

    public void closeSplashScreen() {
//        if (splashScreen.isPresent())
//            splashScreen.get().close();
    }
}
