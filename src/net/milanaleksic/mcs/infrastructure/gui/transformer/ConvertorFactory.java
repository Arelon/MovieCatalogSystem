package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:19 PM
 */
class ConvertorFactory implements LifecycleListener, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private Map<Class<?>, Convertor> registeredConvertors;

    public void setRegisteredConvertors(Map<Class<?>, Convertor> registeredConvertors) {
        this.registeredConvertors = registeredConvertors;
    }

    public Convertor getConvertor(final Transformer originator, final Class<?> argType, Map<String, Object> mappedObjects) throws TransformerException {
        Convertor convertor = registeredConvertors.get(argType);
        if (convertor == null)
            return new ObjectConvertor(originator, argType, mappedObjects, applicationContext);
        return convertor;
    }

    public Optional<Convertor> getExactTypeConvertor(final Class<?> type) throws TransformerException {
        Convertor convertor = registeredConvertors.get(type);
        if (convertor == null)
            return Optional.absent();
        return Optional.of(convertor);
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        for (Convertor convertor : registeredConvertors.values()) {
            convertor.cleanUp();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
