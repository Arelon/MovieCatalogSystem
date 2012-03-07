package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.util.VersionInformation;

import java.awt.*;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 11:41 PM
 */
public class SplashScreenManager implements LifecycleListener {

    private SplashScreen splashScreen;

    @Override public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        splashScreen = refreshSplashScreen();
    }

    @Override public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
    }

    private SplashScreen refreshSplashScreen() {
        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            Graphics2D canvas = splashScreen.createGraphics();
            canvas.setFont(new Font("Arial", Font.BOLD, 14));
            canvas.setColor(Color.DARK_GRAY);
            String text = "v" + VersionInformation.getVersion();
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
