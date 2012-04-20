package net.milanaleksic.mcs.infrastructure.gui.transformer;

import org.codehaus.jackson.JsonNode;

import java.lang.reflect.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:59 PM
 */
public abstract class AbstractConvertor implements Convertor {

    @Override
    public final void invoke(Method method, Object targetObject, JsonNode value) throws TransformerException {
        try {
            method.invoke(targetObject, getValueFromJson(value));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new TransformerException("Wrapped invoke failed: ", e);
        }
    }

    @Override
    public final void setField(Field field, Object targetObject, JsonNode value) throws TransformerException {
        try {
            field.set(targetObject, getValueFromJson(value));
        } catch (IllegalAccessException e) {
            throw new TransformerException("Wrapped setField failed: ", e);
        }
    }

    protected abstract Object getValueFromJson(JsonNode node) throws TransformerException;

    @Override
    public void cleanUp() {}

}
