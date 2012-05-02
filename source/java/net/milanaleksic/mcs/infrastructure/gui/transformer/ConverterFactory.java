package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.*;

import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:19 PM
 */
class ConverterFactory implements LifecycleListener {

    private Map<Class<?>, Converter<?>> registeredConverters;

    public void setRegisteredConverters(Map<Class<?>, Converter<?>> registeredConverters) {
        this.registeredConverters = registeredConverters;
    }

    public Converter getConverter(final Class<?> argType) throws TransformerException {
        Converter converter = registeredConverters.get(argType);
        if (converter == null)
            return registeredConverters.get(Object.class);
        return converter;
    }

    @SuppressWarnings({"unchecked"})
    public <T> Optional<Converter<T>> getExactTypeConverter(final Class<T> type) throws TransformerException {
        Converter<T> converter = (Converter<T>) registeredConverters.get(type);
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
}
