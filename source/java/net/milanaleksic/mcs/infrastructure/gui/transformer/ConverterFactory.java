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
class ConverterFactory implements LifecycleListener, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private Map<Class<?>, Converter> registeredConverters;

    public void setRegisteredConverters(Map<Class<?>, Converter> registeredConverters) {
        this.registeredConverters = registeredConverters;
    }

    public Converter getConverter(final Transformer originator, final Class<?> argType, Map<String, Object> mappedObjects) throws TransformerException {
        Converter converter = registeredConverters.get(argType);
        if (converter == null)
            return new ObjectConverter(originator, argType, mappedObjects, applicationContext);
        return converter;
    }

    public Optional<Converter> getExactTypeConverter(final Class<?> type) throws TransformerException {
        Converter converter = registeredConverters.get(type);
        if (converter == null)
            return Optional.absent();
        return Optional.of(converter);
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        for (Converter converter : registeredConverters.values()) {
            converter.cleanUp();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
