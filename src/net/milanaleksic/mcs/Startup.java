package net.milanaleksic.mcs;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.infrastructure.util.VersionInformation;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.nio.channels.FileLock;

public class Startup {

    private static final Logger log = Logger.getLogger(Startup.class);

    public static void main(String[] args) {
        ProgramArgsService.setProgramArgs(args);
        FileLock lock = null;
        try {
            lock = getSingletonApplicationFileLock();
            if (log.isInfoEnabled())
                log.info("Welcome to Movie Catalog System v" + VersionInformation.getVersion()+", booting application context..."); //NON-NLS
            ApplicationContext applicationContext = bootSpringContext();
            ApplicationManager applicationManager = ((ApplicationManager) applicationContext.getBean("applicationManager")); //NON-NLS
            applicationManager.entryPoint();
        } catch (Throwable t) {
            log.error("Application error!", t); //NON-NLS
        } finally {
            if (lock != null)
                closeSingletonApplicationLock(lock);
        }
    }

    private static ApplicationContext bootSpringContext() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-beans.xml"); //NON-NLS
        context.registerShutdownHook();
        if (log.isDebugEnabled())
            log.debug("Application context booted"); //NON-NLS
        return context;
    }

    private static FileLock getSingletonApplicationFileLock() {
        final String lockFileName = ".launcher"; //NON-NLS
        File locker = new File(lockFileName);
        FileLock lock;
        try {
            if (!locker.exists())
                throw new IllegalStateException("Nisam mogao da pristupim lock fajlu!");
            lock = new RandomAccessFile(lockFileName, "rw").getChannel().tryLock(); //NON-NLS
            if (lock == null) {
                throw new IllegalStateException("Program je vec pokrenut, ne mozete pokrenuti novu instancu");
            }
        } catch (IOException e) {
            throw new IllegalStateException("IO exception while trying to acquire lock");
        }
        return lock;
    }

    private static void closeSingletonApplicationLock(FileLock lock) {
        try {
            lock.channel().close();
        } catch (IOException e) {
            log.error(e);
        }
    }
}