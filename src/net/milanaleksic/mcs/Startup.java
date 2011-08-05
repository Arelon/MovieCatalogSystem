package net.milanaleksic.mcs;

import net.milanaleksic.mcs.config.ApplicationConfiguration;
import net.milanaleksic.mcs.config.ApplicationConfigurationManager;
import net.milanaleksic.mcs.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.nio.channels.FileLock;

public class Startup {

    private static final Logger log = Logger.getLogger(Startup.class);

    public static void main(String[] args) {
        loadLog4JOverride();
        FileLock lock = null;
        try {
            lock = getSingletonApplicationFileLock();

            ApplicationConfiguration applicationConfiguration = ApplicationConfigurationManager.loadApplicationConfiguration();
            ApplicationManager.setApplicationConfiguration(applicationConfiguration);

            ApplicationManager applicationManager = bootSpringForManager();

            ProgramArgs programArgs = getApplicationArgs(args);
            applicationManager.setProgramArgs(programArgs);

            applicationManager.entryPoint();

        } finally {
            if (lock != null)
               closeSingletonApplicationLock(lock);
        }
    }

    private static ProgramArgs getApplicationArgs(String[] args) {
        ProgramArgs programArgs = new ProgramArgs();
        CmdLineParser parser = new CmdLineParser(programArgs);
        try {
            parser.parseArgument(args);
            log.info("Program arguments: "+programArgs);
        } catch (CmdLineException e) {
            log.error("Command line arguments could not have been read", e);
            parser.printUsage(System.err);
            return null;
        }
        return programArgs;
    }

    private static ApplicationManager bootSpringForManager() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-beans.xml");
        ApplicationManager applicationManager = ((ApplicationManager) context.getBean("applicationManager"));
        context.registerShutdownHook();
        return applicationManager;
    }

    private static void loadLog4JOverride() {
        if (new File("log4j.properties").exists())
            PropertyConfigurator.configure("log4j.properties");
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