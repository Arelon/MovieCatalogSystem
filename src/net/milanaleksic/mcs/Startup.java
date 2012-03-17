package net.milanaleksic.mcs;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.infrastructure.util.VersionInformation;
import net.milanaleksic.winlauncher.*;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;

public class Startup {

    private static final Logger log = Logger.getLogger(Startup.class);

    public static void main(String[] args) {
        ProgramArgsService.setProgramArgs(args);

        WinLauncherUtil.wrapSingletonApplicationLogic(
                new ApplicationLogic(
                        WinLauncherConfig.build().setExecutable("MCS.exe")
                ) {
                    public void run() {
                        if (log.isInfoEnabled())
                            log.info("Welcome to Movie Catalog System v" + VersionInformation.getVersion()+", booting application context..."); //NON-NLS
                        ApplicationContext applicationContext = bootSpringContext();
                        ApplicationManager applicationManager = ((ApplicationManager) applicationContext.getBean("applicationManager")); //NON-NLS
                        applicationManager.entryPoint();
                    }

                    public void couldNotRun(Exception e) {
                        JOptionPane.showMessageDialog(null, "Startup error - " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
        });
    }

    private static ApplicationContext bootSpringContext() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-beans.xml"); //NON-NLS
        context.registerShutdownHook();
        if (log.isDebugEnabled())
            log.debug("Application context booted"); //NON-NLS
        return context;
    }

}