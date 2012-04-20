package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.eclipse.swt.graphics.*;

import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:19 PM
 */
public class ConvertorFactory {

    private final Map<Class<?>, Convertor> registeredConvertors;

    public static ConvertorFactory createFactory(ResourceBundle resourceBundle) {
        return new ConvertorFactory(resourceBundle);
    }

    public ConvertorFactory(ResourceBundle resourceBundle) {
        registeredConvertors = ImmutableMap.<Class<?>, Convertor>builder()
                .put(String.class, new StringConvertor(resourceBundle))
                .put(boolean.class, new BooleanConvertor())
                .put(int.class, new IntegerConvertor())
                .put(Point.class, new PointConvertor())
                .put(Color.class, new ColorConvertor())
                .put(Font.class, new FontConvertor())
                .build();
    }

    public Convertor getConvertor(final Transformer originator, final Class<?> argType) throws TransformerException {
        Convertor convertor = registeredConvertors.get(argType);
        if (convertor == null)
            return new ObjectConvertor(originator, argType);
        return convertor;
    }

    public Optional<Convertor> getExactTypeConvertor(final Class<?> type) throws TransformerException {
        Convertor convertor = registeredConvertors.get(type);
        if (convertor == null)
            return Optional.absent();
        return Optional.of(convertor);
    }

    public void cleanUp() {
        for (Convertor convertor : registeredConvertors.values()) {
            convertor.cleanUp();
        }
    }

}
