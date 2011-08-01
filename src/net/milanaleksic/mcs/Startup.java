package net.milanaleksic.mcs;

import net.milanaleksic.mcs.gui.ClosingForm;
import net.milanaleksic.mcs.gui.MainForm;
import net.milanaleksic.mcs.restore.RestorePointCreator;
import net.milanaleksic.mcs.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.widgets.Display;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.*;
import java.io.*;
import java.nio.channels.FileLock;

public class Startup {

    private static final Logger log = Logger.getLogger(Startup.class);

    private static Kernel kernel;

    public static Kernel getKernel() {
        return kernel;
    }

    private static void setKernel(Kernel dedicatedKernel) {
        kernel = dedicatedKernel;
    }

    public static void main(String[] args) {
        loadLog4JOverride();
        FileLock lock = getSingletonApplicationFileLock();
        applicationLogic(args);
        closeSingletonApplicationLock(lock);
    }

    private static void applicationLogic(String[] args) {
        SplashScreen splashScreen = refreshSplashScreen();
        loadSpring();
        ProgramArgs programArgs = getApplicationArgs(args);
        startStatisticsMonitoringIfRequired(programArgs);
        mainGuiLoop(splashScreen);
        showStatisticsInformationIfAvailable(programArgs);
    }

    private static void closeSingletonApplicationLock(FileLock lock) {
        try {
            lock.channel().close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static void showStatisticsInformationIfAvailable(ProgramArgs programArgs) {
        if (programArgs.isCollectStatistics()) {
            log.info("Statistics information: "+getKernel().getHibernateTemplate().getSessionFactory().getStatistics());
        }
    }

    private static void mainGuiLoop(SplashScreen splashScreen) {
        Display.setAppName("Movie Catalog System - v" + Kernel.getVersion());
        Display display = Display.getDefault();
        MainForm form = new MainForm();
        if (splashScreen != null)
            splashScreen.close();

        while (!form.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        leavingProgram();

        display.dispose();
    }

    private static void leavingProgram() {
        log.info("Napustam program!");
        if (MCSProperties.getDatabaseCreateRestore()) {
            new ClosingForm();
            new RestorePointCreator().createRestorePoint();
        }
    }

    private static void startStatisticsMonitoringIfRequired(ProgramArgs programArgs) {
        if (programArgs.isCollectStatistics())
            getKernel().getHibernateTemplate().getSessionFactory().getStatistics().setStatisticsEnabled(true);
    }

    private static ProgramArgs getApplicationArgs(String[] args) {
        ProgramArgs programArgs = new ProgramArgs();
        CmdLineParser parser = new CmdLineParser(programArgs);
        try {
            parser.parseArgument(args);
            kernel.setProgramArgs(programArgs);
            System.out.println("Program arguments: "+programArgs);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar myprogram.jar [options...] arguments...");
            parser.printUsage(System.err);
            return null;
        }
        return programArgs;
    }

    private static void loadSpring() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-beans.xml");
        setKernel((Kernel) context.getBean("kernel"));
        context.registerShutdownHook();
    }

    private static void loadLog4JOverride() {
        if (new File("log4j.properties").exists())
            PropertyConfigurator.configure("log4j.properties");
    }

    private static SplashScreen refreshSplashScreen() {
        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            Graphics2D canvas = splashScreen.createGraphics();
            canvas.setFont(new Font("Arial", Font.BOLD, 14));
            canvas.setColor(Color.DARK_GRAY);
            String text = "v" + Kernel.getVersion();
            canvas.drawString(text,
                    splashScreen.getBounds().width - canvas.getFontMetrics().stringWidth(text) - 10,
                    splashScreen.getBounds().height - 10);
            splashScreen.update();
        }
        return splashScreen;
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

}