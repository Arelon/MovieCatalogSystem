package net.milanaleksic.mcs.application;

import net.milanaleksic.mcs.application.config.*;
import net.milanaleksic.mcs.application.gui.MainForm;
import net.milanaleksic.mcs.application.gui.helper.SplashScreenManager;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.util.*;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.inject.Inject;
import java.io.*;
import java.util.*;

public class ApplicationManager implements ApplicationContextAware {

    private static final Logger log = Logger.getLogger(ApplicationManager.class);

    public static final String LOG4J_XML = "log4j.xml"; //NON-NLS

    @Inject
    private SplashScreenManager splashScreenManager;

    @Inject
    private MainForm mainForm;

    @Inject
    private UserConfigurationManager userConfigurationManager;

    private ApplicationConfiguration applicationConfiguration;

    private UserConfiguration userConfiguration;

    private Set<LifecycleListener> lifecycleListeners = new HashSet<>();

    public ApplicationManager() {
        this(false);
    }

    public ApplicationManager(boolean explicitReadConfigurationsNow) {
        if (new File(LOG4J_XML).exists())
            DOMConfigurator.configure(LOG4J_XML);
        if (explicitReadConfigurationsNow)
            readConfigurationsWithExplicitReadConfiguration();
    }

    public void setLifecycleListeners(Set<LifecycleListener> lifecycleListeners) {
        this.lifecycleListeners = lifecycleListeners;
    }

    public UserConfiguration getUserConfiguration() {
        return userConfiguration;
    }

    private void fireApplicationStarted() {
        for (LifecycleListener listener : lifecycleListeners) {
            listener.applicationStarted(applicationConfiguration, userConfiguration);
        }
    }

    private void fireApplicationShutdown() {
        for (LifecycleListener listener : lifecycleListeners) {
            listener.applicationShutdown(applicationConfiguration, userConfiguration);
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
        Display.setAppName("Movie Catalog System - v" + VersionInformation.getVersion()); //NON-NLS
        Display display = Display.getDefault();
        try {
            mainForm.open();

            splashScreenManager.closeSplashScreen();

            while (!mainForm.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
        } catch (Exception e) {
            log.error("Exception experienced in GUI thread", e);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        readConfigurations();
    }

    private void readConfigurations() {
        applicationConfiguration = new ApplicationConfigurationManager().loadApplicationConfiguration();
        userConfiguration = userConfigurationManager.loadUserConfiguration();
    }

    private void readConfigurationsWithExplicitReadConfiguration() {
        applicationConfiguration = new ApplicationConfigurationManager().loadApplicationConfiguration();
        userConfiguration = new UserConfigurationManager().loadUserConfiguration();
    }
}