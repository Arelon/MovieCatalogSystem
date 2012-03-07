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

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String clazzName = args[0];
        Display display = new Display();

        ApplicationManager applicationManager = new ApplicationManager(true);
        applicationManager.getUserConfiguration().setLocaleLanguage(args.length>1?args[1]:"en");

        Class<?> clazz = Class.forName(clazzName);
        AbstractDialogForm form = (AbstractDialogForm) clazz.newInstance();
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
