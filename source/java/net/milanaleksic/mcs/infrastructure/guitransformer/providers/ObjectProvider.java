package net.milanaleksic.mcs.infrastructure.guitransformer.providers;

import net.milanaleksic.guitransformer.TransformerException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.*;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 1:07 PM
 */
public class ObjectProvider implements net.milanaleksic.guitransformer.providers.ObjectProvider, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object provideObjectNamed(String beanName) {
        try {
            return applicationContext.getBean(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            throw new RuntimeException("Bean could not be found: "+beanName, e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
