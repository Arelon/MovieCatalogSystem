package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import org.apache.log4j.Logger;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.SWT;
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
    private static final String KEY_SPECIAL_STYLE = "_style"; //NON-NLS

    private Map<String, Object> mappedObjects = Maps.newHashMap();

    private static final Set<String> SPECIAL_KEYS = ImmutableSet
            .<String>builder()
            .add(KEY_SPECIAL_TYPE)
            .add(KEY_SPECIAL_CHILDREN)
            .add(KEY_SPECIAL_NAME)
            .add(KEY_SPECIAL_STYLE)
            .build();

    private ObjectMapper mapper;
    private ConvertorFactory convertorFactory;

    public Transformer(ResourceBundle resourceBundle) {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        this.convertorFactory = ConvertorFactory.createFactory(resourceBundle);
    }

    public Shell createFormFromResource(String fullName) throws TransformerException {
        return fillForm(fullName, new Shell());
    }

    public Shell fillForm(String fullName, Shell shell) throws TransformerException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(fullName)) {
            deserializeObjectFromNode(mapper.readValue(resourceAsStream, JsonNode.class), shell);
            return shell;
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + fullName, e);
        } finally {
            convertorFactory.cleanUp();
        }
    }

    public <T> Optional<T> getMappedObject(String name) {
        Object object = mappedObjects.get(name);
        if (object == null)
            return Optional.absent();
        else
            //noinspection unchecked
            return Optional.of((T) object);
    }

    private void deserializeObjectFromNode(JsonNode jsonNode, Object object) throws TransformerException {
        transformNodeToProperties(jsonNode, object);
        createChildrenIfTheyExist(jsonNode, object);
        if (jsonNode.has(KEY_SPECIAL_NAME)) {
            String objectName = jsonNode.get(KEY_SPECIAL_NAME).asText();
            mappedObjects.put(objectName, object);
        }
    }

    private void createChildrenIfTheyExist(JsonNode parentNode, Object object) throws TransformerException {
        if (!parentNode.has(KEY_SPECIAL_CHILDREN))
            return;
        if (!(object instanceof Composite))
            throw new IllegalStateException("Can not create children for parent which is not Composite");
        Composite compositeParent = (Composite) object;
        try {
            for (JsonNode node : mapper.readValue(parentNode.get(KEY_SPECIAL_CHILDREN), JsonNode[].class)) {
                Object childObject = createChildObject(compositeParent, node);
                deserializeObjectFromNode(node, childObject);
            }
        } catch (IOException e) {
            throw new TransformerException("IO exception while trying to parse child nodes", e);
        }
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

    private Object createChildObject(Composite parent, JsonNode childDefinition) throws TransformerException {
        try {
            Class<?> widgetClass;
            if (!childDefinition.has(KEY_SPECIAL_TYPE))
                throw new IllegalArgumentException("Could not deduce the child type without explicit definition");
            widgetClass = Class.forName(childDefinition.get(KEY_SPECIAL_TYPE).asText());
            Constructor<?> constructor = widgetClass.getConstructor(Composite.class, int.class);

            int style = SWT.NONE;
            if (childDefinition.has(KEY_SPECIAL_STYLE)) {
                JsonNode styleNode = childDefinition.get(KEY_SPECIAL_STYLE);
                AbstractConvertor exactTypeConvertor = (AbstractConvertor) convertorFactory.getExactTypeConvertor(int.class).get();
                style = (Integer) exactTypeConvertor.getValueFromJson(styleNode);
            }
            return constructor.newInstance(parent, style);
        } catch (ReflectiveOperationException e) {
            throw new TransformerException("Widget creation of class failed", e);
        }
    }

}
