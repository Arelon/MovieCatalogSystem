package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.gui.*;
import net.milanaleksic.mcs.application.gui.AbstractForm;
import net.milanaleksic.mcs.infrastructure.gui.transformer.Transformer;
import org.eclipse.swt.widgets.Display;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
        applicationManager.getUserConfiguration().setLocaleLanguage(args.length > 1 ? args[1] : "en"); //NON-NLS


        ClassPathXmlApplicationContext applicationContext = null;
        try {
            applicationContext = new ClassPathXmlApplicationContext("spring-beans.xml"); //NON-NLS
            applicationContext.registerShutdownHook();

            Class<?> clazz = Class.forName(clazzName);
            AbstractForm form = (AbstractForm) clazz.newInstance();
            if (form instanceof AbstractTransformedForm) {
                ((AbstractTransformedForm) form).setTransformer(applicationContext.getBean("transformer", Transformer.class));
            }
            form.setApplicationManager(applicationManager);
            form.setNoReadyEvent(true);
            form.open();
            while (!form.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            display.dispose();

        } finally {
            if (applicationContext != null)
                applicationContext.close();
        }
    }

}
