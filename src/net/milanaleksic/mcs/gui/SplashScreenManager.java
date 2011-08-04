package net.milanaleksic.mcs.gui;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.event.LifecycleListener;

import java.awt.*;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 11:41 PM
 */
public class SplashScreenManager implements LifecycleListener {

    private SplashScreen splashScreen;

    @Override public void applicationStarted() {
        splashScreen = refreshSplashScreen();
    }

    @Override public void applicationShutdown() {
    }

    private SplashScreen refreshSplashScreen() {
        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            Graphics2D canvas = splashScreen.createGraphics();
            canvas.setFont(new Font("Arial", Font.BOLD, 14));
            canvas.setColor(Color.DARK_GRAY);
            String text = "v" + ApplicationManager.getVersion();
            canvas.drawString(text,
                    splashScreen.getBounds().width - canvas.getFontMetrics().stringWidth(text) - 10,
                    splashScreen.getBounds().height - 10);
            splashScreen.update();
        }
        return splashScreen;
    }

    public void closeSplashScreen() {
        if (splashScreen != null)
            splashScreen.close();
    }
}
