package net.milanaleksic.mcs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

import net.milanaleksic.mcs.restore.RestorePointCreator;
import net.milanaleksic.mcs.util.Kernel;
import net.milanaleksic.mcs.util.MCSProperties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.widgets.Display;
import org.springframework.context.ApplicationContext;
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
		if (lock==null)
			return;

		// SPLASH SCREEN
		SplashScreen splashScreen = refreshSplashScreen();

		// SPRING
		ApplicationContext context = new FileSystemXmlApplicationContext("spring-beans.xml");
		setKernel ((Kernel) context.getBean("kernel"));

		// SWT
		Display.setAppName("Movie Catalog System - v"+Kernel.getVersion());
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
			String text = "v"+Kernel.getVersion();
			canvas.drawString(text, 
					splashScreen.getBounds().width-canvas.getFontMetrics().stringWidth(text)-10, 
					splashScreen.getBounds().height-10);
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
			lock = lockerStream.getChannel().tryLock(0,1,false);
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