package net.milanaleksic.mcs.application;

import net.milanaleksic.mcs.application.config.*;
import net.milanaleksic.mcs.application.gui.MainForm;
import net.milanaleksic.mcs.application.gui.helper.SplashScreenManager;
import net.milanaleksic.mcs.infrastructure.util.UTF8ResourceBundleControl;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.io.*;
import java.util.*;

public class ApplicationManager {

    private static final Logger log = Logger.getLogger(ApplicationManager.class);

    public static final String LOG4J_XML = "log4j.xml"; //NON-NLS

    @Inject
    private SplashScreenManager splashScreenManager;

    @Inject
    private MainForm mainForm;

    private final ApplicationConfiguration applicationConfiguration;

    private Set<LifecycleListener> lifecycleListeners = new HashSet<>();
    private static String version = null;
    private UserConfiguration userConfiguration;

    public ApplicationManager() {
        if (new File(LOG4J_XML).exists())
            DOMConfigurator.configure(LOG4J_XML);
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
        if (version == null) {
            Properties properties;
            try {
                properties = StreamUtil.fetchPropertiesFromClasspath("/net/milanaleksic/mcs/version.properties"); //NON-NLS
            } catch (IOException e) {
                throw new RuntimeException("Version properties files not found in resources");
            }
            version = properties.getProperty("src.version") + '.' + properties.getProperty("build.number");
        }
        return version;
    }

    public void setUserConfiguration(UserConfiguration userConfiguration) {
        this.userConfiguration = userConfiguration;
    }

    private void fireApplicationStarted() {
        for (LifecycleListener listener : lifecycleListeners) {
            listener.applicationStarted();
        }
    }

    private void fireApplicationShutdown() {
        for (LifecycleListener listener : lifecycleListeners) {
            listener.applicationShutdown();
        }
    }

    public void entryPoint() {
        setUncaughtExceptionHandler();
        fireApplicationStarted();
        try {
            mainGuiLoop();
        } catch (RuntimeException e) {
            String message = "Runtime exception caught in main GUI loop: " + e.getMessage(); //NON-NLS
            log.error(message, e);
            showTerribleErrorInGui(message);
        } finally {
            fireApplicationShutdown();
        }
    }

    public void setUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                String message = "Runtime exception caught in non-primary thread: " + e.getMessage(); //NON-NLS
                log.error(message, e);
                showTerribleErrorInGui(message);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    private void showTerribleErrorInGui(String message) {
        Shell activeShell = Display.getDefault().getActiveShell();
        if (activeShell == null)
            activeShell = new Shell();
        MessageBox messageBox = new MessageBox(activeShell, SWT.APPLICATION_MODAL | SWT.ERROR);
        messageBox.setMessage(message);
        messageBox.setText("Terrible, terrible error"); //NON-NLS
        messageBox.open();
    }

    private void mainGuiLoop() {
        Display.setAppName("Movie Catalog System - v" + getVersion()); //NON-NLS
        Display display = Display.getDefault();
        try {
            mainForm.open();

            splashScreenManager.closeSplashScreen();

            while (!mainForm.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
        } finally {
            display.dispose();
        }
    }

    private ResourceBundle messageBundle = null;

    public synchronized ResourceBundle getMessagesBundle() {
        if (messageBundle == null)
            messageBundle = ResourceBundle.getBundle("messages", //NON-NLS
                    new Locale(getUserConfiguration().getLocaleLanguage()), new UTF8ResourceBundleControl());
        return messageBundle;
    }
}