package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.apache.log4j.Logger;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 11:35 AM
 */
public class Transformer {

    private static final Logger log = Logger.getLogger(Transformer.class);

    private static final String KEY_SPECIAL_TYPE = "_type"; //NON-NLS
    private static final String KEY_SPECIAL_CHILDREN = "_children"; //NON-NLS
    private static final String KEY_SPECIAL_NAME = "_name"; //NON-NLS

    private static final Set<String> SPECIAL_KEYS = ImmutableSet
            .<String>builder()
            .add(KEY_SPECIAL_TYPE)
            .add(KEY_SPECIAL_CHILDREN)
            .add(KEY_SPECIAL_NAME)
            .build();

    private ObjectMapper mapper;
    private ConvertorFactory convertorFactory;

    public Transformer(ResourceBundle resourceBundle) {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        this.convertorFactory = ConvertorFactory.createFactory(resourceBundle);
    }

    public Shell createFormFromResource(String fullName) throws TransformerException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(fullName)) {
            JsonNode jsonNode = mapper.readValue(resourceAsStream, JsonNode.class);
            return transformNodeToForm(jsonNode);
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + fullName, e);
        }
    }

    private Shell transformNodeToForm(JsonNode jsonNode) throws TransformerException {
        Shell shell = new Shell();
        transformNodeToProperties(jsonNode, shell);
        return shell;
    }

    private void transformNodeToProperties(JsonNode jsonNode, Object object) throws TransformerException {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.getFields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            transformSingleJsonNode(object, field);
        }
    }

    private void transformSingleJsonNode(Object object, Map.Entry<String, JsonNode> field) throws TransformerException {
        try {
            if (SPECIAL_KEYS.contains(field.getKey()))
                return;
            Optional<Method> method = getSetterByName(object, getSetterForField(field.getKey()));
            if (method.isPresent()) {
                Class<?> argType = method.get().getParameterTypes()[0];
                convertorFactory.getConvertor(this, argType).invoke(method.get(), object, field.getValue());
            } else {
                Optional<Field> fieldByName = getFieldByName(object, field.getKey());
                if (fieldByName.isPresent()) {
                    Class<?> argType = fieldByName.get().getType();
                    convertorFactory.getConvertor(this, argType).setField(fieldByName.get(), object, field.getValue());
                } else
                    throw new TransformerException("No setter nor field " + field.getKey() + " could be found in class " + object.getClass());
            }
        } catch (Throwable t) {
            throw new TransformerException("Transformation was not successful", t);
        }
    }

    private Optional<Field> getFieldByName(Object object, String fieldName) {
        for (Field field: object.getClass().getFields()) {
            if (field.getName().equals(fieldName)) {
                return Optional.of(field);
            }
        }
        return Optional.absent();
    }

    private Optional<Method> getSetterByName(Object object, String setterName) {
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(setterName) && method.getParameterTypes().length == 1) {
                return Optional.of(method);
            }
        }
        return Optional.absent();
    }

    private String getSetterForField(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1); //NON-NLS
    }

    public Object createWidgetFromResource(Class<?> widgetClass, JsonNode value) throws TransformerException {
        //TODO: wrap creation for cases where there are no simple constructors
        try {
            Object widget;
            if (value.has(KEY_SPECIAL_TYPE))
                widget = Class.forName(value.get(KEY_SPECIAL_TYPE).asText()).newInstance();
            else
                widget = widgetClass.newInstance();
            transformNodeToProperties(value, widget);
            return widget;
        } catch (InstantiationException | TransformerException | IllegalAccessException | ClassNotFoundException e) {
            throw new TransformerException("Widget creation of class failed", e);
        }
    }

}
