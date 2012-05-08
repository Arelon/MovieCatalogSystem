package net.milanaleksic.mcs;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.infrastructure.util.VersionInformation;
import net.milanaleksic.winlauncher.*;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;

public class Startup {

    private static final Logger log = Logger.getLogger(Startup.class);

    public static void main(String[] args) {
        ProgramArgsService.setProgramArgs(args);

        WinLauncherUtil.wrapSingletonApplicationLogic(new ApplicationLogic() {
            public void run() {
                if (log.isInfoEnabled())
                    log.info("Welcome to Movie Catalog System v" + VersionInformation.getVersion()); //NON-NLS
                ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-beans.xml"); //NON-NLS
                applicationContext.registerShutdownHook();
                if (log.isDebugEnabled())
                    log.debug("Application context booted"); //NON-NLS
                ApplicationManager applicationManager = ((ApplicationManager) applicationContext.getBean("applicationManager")); //NON-NLS
                applicationManager.entryPoint();
            }

            public void couldNotRun(Exception e) {
                log.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(null, "Application error - " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); //NON-NLS
            }
        });
    }

}