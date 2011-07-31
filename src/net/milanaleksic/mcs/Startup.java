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


/**
 * @author Milan 22 Sep 2007
 */
public class Startup {

    private static final Logger log = Logger.getLogger(Startup.class);  //  @jve:decl-index=0:

    private static Kernel kernel;

    public static Kernel getKernel() {
        return kernel;
    }

    private static void setKernel(Kernel dedicatedKernel) {
        kernel = dedicatedKernel;
    }

    public static void main(String[] args) {
        // LOADING LOG4J OVERRIDE FROM STARTUP DIR IF IT EXISTS
        if (new File("log4j.properties").exists())
            PropertyConfigurator.configure("log4j.properties");

        // SINGLETON APPLICATION
        FileLock lock = getSingletonApplicationFileLock();
        if (lock == null)
            return;

        // SPLASH SCREEN
        SplashScreen splashScreen = refreshSplashScreen();

        // SPRING
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-beans.xml");
        setKernel((Kernel) context.getBean("kernel"));
        context.registerShutdownHook();

        // ARGS4J
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
            return;
        }

        // START STATISTICS MONITORING
        if (programArgs.isCollectStatistics())
            getKernel().getHibernateTemplate().getSessionFactory().getStatistics().setStatisticsEnabled(true);

        // SWT
        Display.setAppName("Movie Catalog System - v" + Kernel.getVersion());
        Display display = Display.getDefault();
        MainForm form = new MainForm();

        // CLOSE SPLASH SCREEN
        if (splashScreen != null)
            splashScreen.close();

        // SHOWALL!!!!
        while (!form.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        // GENERATE RESTORE SQL
        log.info("Napustam program!");
        if (MCSProperties.getDatabaseCreateRestore()) {
            new ClosingForm();
            new RestorePointCreator().createRestorePoint();
        }

        // TERMINATE DISPLAY
        display.dispose();

        // SHOW STATISTICS
        if (programArgs.isCollectStatistics()) {
            log.info("Statistics information: "+getKernel().getHibernateTemplate().getSessionFactory().getStatistics());
        }

        // CLOSE LOCK
        try {
            lock.channel().close();
        } catch (IOException e) {
            log.error(e);
        }
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
        FileLock lock = null;
        try {
            if (!locker.exists())
                throw new IllegalStateException("Nisam mogao da pristupim lock fajlu!");
            lock = new RandomAccessFile(lockFileName, "rw").getChannel().tryLock();
            if (lock == null) {
                log.error("Program je vec pokrenut, ne mozete pokrenuti novu instancu");
                return null;
            }
        } catch (IOException e) {
            log.error(e);
        }
        return lock;
    }

}