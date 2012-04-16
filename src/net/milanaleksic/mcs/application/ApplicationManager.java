package net.milanaleksic.mcs.application;

import com.google.common.base.Optional;
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
    private ApplicationConfigurationManager applicationConfigurationManager;

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
            readConfigurationsWithoutDI();
    }

    public void setLifecycleListeners(Set<LifecycleListener> lifecycleListeners) {
        this.lifecycleListeners = lifecycleListeners;
    }

    public UserConfiguration getUserConfiguration() {
        return userConfiguration;
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    private void fireApplicationStarted() {
        for (LifecycleListener listener : lifecycleListeners) {
            safeCallStartedOnListener(listener);
        }
    }

    private void fireApplicationShutdown() {
        for (LifecycleListener listener : lifecycleListeners) {
            safeCallShutdownOnListener(listener);
        }
    }

    private void safeCallStartedOnListener(LifecycleListener listener) {
        try {
            listener.applicationStarted(applicationConfiguration, userConfiguration);
        } catch (Exception e) {
            log.error("Application exception while calling startup callbacks", e); //NON-NLS
        }
    }

    private void safeCallShutdownOnListener(LifecycleListener listener) {
        try {
            listener.applicationShutdown(applicationConfiguration, userConfiguration);
        } catch (Exception e) {
            log.error("Application exception while calling shutdown callbacks", e); //NON-NLS
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
        if (!activeShell.isDisposed())
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
        } catch (Throwable t) {
            log.error("Unhandled GUI thread exception - "+t.getMessage(), t); //NON-NLS
        } finally {
            if (!display.isDisposed())
                display.dispose();
        }
    }

    private Optional<ResourceBundle> messageBundle = Optional.absent();

    public synchronized ResourceBundle getMessagesBundle() {
        if (!messageBundle.isPresent())
            messageBundle = Optional.of(ResourceBundle.getBundle("messages", //NON-NLS
                    new Locale(getUserConfiguration().getLocaleLanguage()), new UTF8ResourceBundleControl()));
        return messageBundle.get();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        readConfigurations();
    }

    private void readConfigurations() {
        applicationConfiguration = applicationConfigurationManager.loadApplicationConfiguration();
        userConfiguration = userConfigurationManager.loadUserConfiguration();
    }

    private void readConfigurationsWithoutDI() {
        applicationConfiguration = new ApplicationConfigurationManager().loadApplicationConfiguration();
        userConfiguration = new UserConfigurationManager().loadUserConfiguration();
    }
}