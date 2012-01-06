package net.milanaleksic.mcs.application;

import net.milanaleksic.mcs.application.config.*;
import net.milanaleksic.mcs.application.gui.MainForm;
import net.milanaleksic.mcs.application.gui.helper.SplashScreenManager;
import net.milanaleksic.mcs.application.util.UTF8ResourceBundleControl;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.swt.widgets.Display;

import javax.inject.Inject;
import java.io.*;
import java.util.*;

public class ApplicationManager {

    @Inject private SplashScreenManager splashScreenManager;

    @Inject private MainForm mainForm;

    private final ApplicationConfiguration applicationConfiguration;

    private Set<LifecycleListener> lifecycleListeners = new HashSet<>();
    private static String version = null;
    private UserConfiguration userConfiguration;

    public ApplicationManager() {
        if (new File("log4j.xml").exists())
            DOMConfigurator.configure("log4j.xml");
        applicationConfiguration = new ApplicationConfigurationManager().loadApplicationConfiguration();
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    public void setLifecycleListeners(Set<LifecycleListener> lifecycleListeners) {
        this.lifecycleListeners = lifecycleListeners;
    }

    public UserConfiguration getUserConfiguration() {
        return userConfiguration;
    }

    public static synchronized String getVersion() {
        if (version==null) {
            Properties properties;
            try {
                properties = StreamUtil.fetchPropertiesFromClasspath("/net/milanaleksic/mcs/version.properties");
            } catch (IOException e) {
                throw new RuntimeException("Version properties files not found in resources");
            }
            version = properties.getProperty("src.version")+'.'+properties.getProperty("build.number");
        }
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
        Display.setAppName("Movie Catalog System - v" + getVersion());
        Display display = Display.getDefault();

        mainForm.open();

        splashScreenManager.closeSplashScreen();

        while (!mainForm.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        display.dispose();
    }

    private ResourceBundle messageBundle = null;
    public synchronized ResourceBundle getMessagesBundle() {
        if (messageBundle == null)
            messageBundle = ResourceBundle.getBundle("messages",
                    new Locale(getUserConfiguration().getLocaleLanguage()), new UTF8ResourceBundleControl());
        return messageBundle;
    }
}