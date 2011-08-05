package net.milanaleksic.mcs;

import net.milanaleksic.mcs.config.ApplicationConfiguration;
import net.milanaleksic.mcs.config.UserConfiguration;
import net.milanaleksic.mcs.event.LifecycleListener;
import net.milanaleksic.mcs.gui.MainForm;
import net.milanaleksic.mcs.gui.SplashScreenManager;
import net.milanaleksic.mcs.util.ProgramArgs;
import org.eclipse.swt.widgets.Display;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.*;

/**
 * @author Milan 22 Sep 2007
 */
public class ApplicationManager {

    @Autowired private SplashScreenManager splashScreenManager;

    private Set<LifecycleListener> lifecycleListeners;

    private static ApplicationConfiguration applicationConfiguration;

    public static void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
        ApplicationManager.applicationConfiguration = applicationConfiguration;
    }

    public static ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    public void setLifecycleListeners(Set<LifecycleListener> lifecycleListeners) {
        this.lifecycleListeners = lifecycleListeners;
    }

    private HibernateTemplate hibernateTemplate;
	private static final String version = "0.42";
    private ProgramArgs programArgs;
    private UserConfiguration userConfiguration;

    public UserConfiguration getUserConfiguration() {
        return userConfiguration;
    }

    public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public static String getVersion() {
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
        Display.setAppName("Movie Catalog System - v" + ApplicationManager.getVersion());
        Display display = Display.getDefault();
        MainForm form = new MainForm(this);
        splashScreenManager.closeSplashScreen();

        while (!form.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        display.dispose();
    }

}