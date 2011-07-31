package net.milanaleksic.mcs;

import java.awt.*;
import java.io.*;
import java.nio.channels.FileLock;

import net.milanaleksic.mcs.restore.RestorePointCreator;
import net.milanaleksic.mcs.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.widgets.Display;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.context.support.FileSystemXmlApplicationContext;


/**
 * @author Milan 22 Sep 2007
 */
public class Startup {

    private static Logger log = Logger.getLogger(Startup.class);  //  @jve:decl-index=0:

    private static Kernel kernel;

    public static Kernel getKernel() {
        return kernel;
    }

    public static void setKernel(Kernel dedicatedKernel) {
        kernel = dedicatedKernel;
    }

    public static void main(String[] args) {
        // LOG4J
        PropertyConfigurator.configure("log4j.properties");

        // SINGLETON APPLICATION
        FileLock lock = getSingletonApplicationFileLock();
        if (lock == null)
            return;

        // SPLASH SCREEN
        SplashScreen splashScreen = refreshSplashScreen();

        // SPRING
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("spring-beans.xml");
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
        File locker = new File(".lock");
        FileOutputStream lockerStream = null;
        FileLock lock = null;
        try {
            if (!locker.exists())
                locker.createNewFile();
            lockerStream = new FileOutputStream(locker);
            lock = lockerStream.getChannel().tryLock(0, 1, false);
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