package net.milanaleksic.mcs.infrastructure.gui.transformer;

import org.codehaus.jackson.JsonNode;

import java.lang.reflect.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:10 PM
 */
public interface Convertor {

    void invoke(Method method, Object targetObject, JsonNode value) throws TransformerException;

    void setField(Field field, Object targetObject, JsonNode value) throws TransformerException;

    void cleanUp() ;

}
