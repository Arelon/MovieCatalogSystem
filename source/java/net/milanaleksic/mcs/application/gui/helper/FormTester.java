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

        ClassPathXmlApplicationContext applicationContext = null;
        try {
            applicationContext = new ClassPathXmlApplicationContext("spring-beans.xml"); //NON-NLS
            applicationContext.registerShutdownHook();

            Class<?> clazz = Class.forName(clazzName);
            AbstractForm form = (AbstractForm) clazz.newInstance();
            if (form instanceof AbstractTransformedForm) {
                ((AbstractTransformedForm) form).setTransformer(applicationContext.getBean("transformer", Transformer.class));
            }
            form.setApplicationManager(applicationContext.getBean("applicationManager", ApplicationManager.class));
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
