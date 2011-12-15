package net.milanaleksic.mcs;

import net.milanaleksic.mcs.application.ApplicationManager;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.nio.channels.FileLock;

public class Startup {

    private static final Logger log = Logger.getLogger(Startup.class);

    private static String[] programArgs;

    public static String[] getProgramArgs() {
        return programArgs;
    }

    public static void main(String[] args) {
        programArgs = args;
        FileLock lock = null;
        try {
            lock = getSingletonApplicationFileLock();
            if (log.isInfoEnabled())
                log.info("Welcome to Movie Catalog System v" + ApplicationManager.getVersion()+", booting application context...");
            ApplicationContext applicationContext = bootSpringContext();
            ApplicationManager applicationManager = ((ApplicationManager) applicationContext.getBean("applicationManager"));
            applicationManager.entryPoint();
        } catch (Throwable t) {
            log.error("Application error!", t);
        } finally {
            if (lock != null)
                closeSingletonApplicationLock(lock);
        }
    }

    private static ApplicationContext bootSpringContext() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-beans.xml");
        context.registerShutdownHook();
        if (log.isDebugEnabled())
            log.debug("Application context loaded");
        return context;
    }

    private static FileLock getSingletonApplicationFileLock() {
        final String lockFileName = ".launcher";
        File locker = new File(lockFileName);
        FileLock lock;
        try {
            if (!locker.exists())
                throw new IllegalStateException("Nisam mogao da pristupim lock fajlu!");
            lock = new RandomAccessFile(lockFileName, "rw").getChannel().tryLock();
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