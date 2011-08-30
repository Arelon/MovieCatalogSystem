package net.milanaleksic.mcs;

import net.milanaleksic.mcs.config.*;
import net.milanaleksic.mcs.event.LifecycleListener;
import net.milanaleksic.mcs.gui.MainForm;
import net.milanaleksic.mcs.gui.SplashScreenManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.swt.widgets.Display;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * @author Milan 22 Sep 2007
 */
public class ApplicationManager {

    @Autowired private SplashScreenManager splashScreenManager;

    @Autowired private MainForm mainForm;

    private ApplicationConfiguration applicationConfiguration;

    private Set<LifecycleListener> lifecycleListeners;

    private static String version = null;
    private ProgramArgs programArgs;
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
            Properties properties = new Properties();
            try {
                properties.load(Startup.class.getResourceAsStream("version.properties"));
                version = properties.getProperty("src.version")+'.'+properties.getProperty("build.number");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return version;
    }

    public void setProgramArgs(ProgramArgs programArgs) {
        this.programArgs = programArgs;
    }

    public ProgramArgs getProgramArgs() {
        return programArgs;
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

        mainForm.showForm();

        splashScreenManager.closeSplashScreen();

        while (!mainForm.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        display.dispose();
    }
}