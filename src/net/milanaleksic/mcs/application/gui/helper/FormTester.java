package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.UserConfigurationManager;
import net.milanaleksic.mcs.application.gui.AbstractDialogForm;
import org.eclipse.swt.widgets.Display;

/**
 * User: Milan Aleksic
 * Date: 2/28/12
 * Time: 1:22 PM
 */
public class FormTester {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String clazzName = args[0];
        Display display = new Display();

        ApplicationManager applicationManager = new ApplicationManager();
        UserConfigurationManager userConfigurationManager = new UserConfigurationManager();
        userConfigurationManager.setApplicationManager(applicationManager);
        userConfigurationManager.applicationStarted();

        Class<AbstractDialogForm> clazz = (Class<AbstractDialogForm>) Class.forName(clazzName);
        AbstractDialogForm form = clazz.newInstance();
        form.setApplicationManager(applicationManager);
        form.setNoReadyEvent(true);
        form.open();
        while (!form.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

}
