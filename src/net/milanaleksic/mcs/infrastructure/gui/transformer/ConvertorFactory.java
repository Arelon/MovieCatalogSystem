package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.collect.ImmutableMap;
import org.eclipse.swt.graphics.Point;

import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:19 PM
 */
public class ConvertorFactory {

    private final Map<Class<?>, Convertor> map;

    public static ConvertorFactory createFactory(ResourceBundle resourceBundle) {
        return new ConvertorFactory(resourceBundle);
    }

    public ConvertorFactory(ResourceBundle resourceBundle) {
        map = ImmutableMap.<Class<?>, Convertor>builder()
            .put(String.class, new StringConvertor(resourceBundle))
            .put(boolean.class, new BooleanConvertor())
            .put(int.class, new IntegerConvertor())
            .put(Point.class, new PointConvertor())
            .build();
    }

    public Convertor getConvertor(final Transformer originator, final Class<?> argType) throws TransformerException {
        Convertor convertor = map.get(argType);
        if (convertor == null)
            return new ObjectConvertor(originator, argType);
        return convertor;
    }

}
