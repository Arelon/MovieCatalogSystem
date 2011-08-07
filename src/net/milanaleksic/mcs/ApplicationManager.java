package net.milanaleksic.mcs;

import net.milanaleksic.mcs.config.*;
import net.milanaleksic.mcs.event.LifecycleListener;
import net.milanaleksic.mcs.gui.MainForm;
import net.milanaleksic.mcs.gui.SplashScreenManager;
import org.eclipse.swt.widgets.Display;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author Milan 22 Sep 2007
 */
public class ApplicationManager {

    @Autowired private SplashScreenManager splashScreenManager;

    @Autowired private MainForm mainForm;

    private static ApplicationConfiguration applicationConfiguration;

    private Set<LifecycleListener> lifecycleListeners;

    private static final String version = "0.5";
    private UserConfiguration userConfiguration;

    public static void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
        ApplicationManager.applicationConfiguration = applicationConfiguration;
    }

    public static ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    public void setLifecycleListeners(Set<LifecycleListener> lifecycleListeners) {
        this.lifecycleListeners = lifecycleListeners;
    }

    public UserConfiguration getUserConfiguration() {
        return userConfiguration;
    }

	public static String getVersion() {
		return version;
	}

    public void setUserConfiguration(UserConfiguration userConfiguration) {
        this.userConfiguration = userConfiguration;
    }

    private void fireApplicationStarted() {
        for(LifecycleListener listener : lifecycleListeners) {
            listener.applicationStarted();
        }
    }

    private void fireApplicationShutdown() {
        for(LifecycleListener listener : lifecycleListeners) {
            listener.applicationShutdown();
        }
    }

    public void entryPoint() {
        fireApplicationStarted();
        try {
            mainGuiLoop();
        } finally {
            fireApplicationShutdown();
        }
    }

    private void mainGuiLoop() {
        Display.setAppName("Movie Catalog System - v" + ApplicationManager.getVersion());
        Display display = Display.getDefault();

        mainForm.showForm();

        splashScreenManager.closeSplashScreen();

        while (!mainForm.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        display.dispose();
    }


}