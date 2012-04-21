package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.milanaleksic.mcs.application.ApplicationManager;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 11:35 AM
 */
public class Transformer {

    @Inject
    private ApplicationManager applicationManager;

    static final String KEY_SPECIAL_TYPE = "_type"; //NON-NLS
    static final String KEY_SPECIAL_CHILDREN = "_children"; //NON-NLS
    static final String KEY_SPECIAL_NAME = "_name"; //NON-NLS
    static final String KEY_SPECIAL_STYLE = "_style"; //NON-NLS
    static final String KEY_SPECIAL_COMMENT = "__comment"; //NON-NLS

    private static final Set<String> SPECIAL_KEYS = ImmutableSet
            .<String>builder()
            .add(KEY_SPECIAL_TYPE)
            .add(KEY_SPECIAL_CHILDREN)
            .add(KEY_SPECIAL_NAME)
            .add(KEY_SPECIAL_STYLE)
            .add(KEY_SPECIAL_COMMENT)
            .build();

    private ObjectMapper mapper;

    @Inject
    private ConvertorFactory convertorFactory;

    public Transformer() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public TransformationContext createFormFromResource(String fullName) throws TransformerException {
        return fillForm(fullName, new Shell());
    }

    public TransformationContext fillForm(String fullName, Shell shell) throws TransformerException {
        Map<String, Object> mappedObjects = Maps.newHashMap();
        mappedObjects.put("bundle", applicationManager.getMessagesBundle());
        try (InputStream resourceAsStream = getClass().getResourceAsStream(fullName)) {
            deserializeObjectFromNode(mapper.readValue(resourceAsStream, JsonNode.class), shell, mappedObjects);
            return new TransformationContext(shell, mappedObjects);
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + fullName, e);
        }
    }

    private void deserializeObjectFromNode(JsonNode jsonNode, Object object, Map<String, Object> mappedObjects) throws TransformerException {
        transformNodeToProperties(jsonNode, object, mappedObjects);
        createChildrenIfTheyExist(jsonNode, object, mappedObjects);
        if (jsonNode.has(KEY_SPECIAL_NAME)) {
            String objectName = jsonNode.get(KEY_SPECIAL_NAME).asText();
            mappedObjects.put(objectName, object);
        }
    }

    private void createChildrenIfTheyExist(JsonNode parentNode, Object object, Map<String, Object> mappedObjects) throws TransformerException {
        if (!parentNode.has(KEY_SPECIAL_CHILDREN))
            return;
        if (!(object instanceof Composite))
            throw new IllegalStateException("Can not create children for parent which is not Composite");
        Composite compositeParent = (Composite) object;
        try {
            for (JsonNode node : mapper.readValue(parentNode.get(KEY_SPECIAL_CHILDREN), JsonNode[].class)) {
                Object childObject = createChildObject(compositeParent, node);
                deserializeObjectFromNode(node, childObject, mappedObjects);
            }
        } catch (IOException e) {
            throw new TransformerException("IO exception while trying to parse child nodes", e);
        }
    }

    void transformNodeToProperties(JsonNode jsonNode, Object object, Map<String, Object> mappedObjects) throws TransformerException {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.getFields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            transformSingleJsonNode(object, field, mappedObjects);
        }
    }

    private void transformSingleJsonNode(Object object, Map.Entry<String, JsonNode> field, Map<String, Object> mappedObjects) throws TransformerException {
        try {
            if (SPECIAL_KEYS.contains(field.getKey()))
                return;
            Optional<Method> method = getSetterByName(object, getSetterForField(field.getKey()));
            if (method.isPresent()) {
                Class<?> argType = method.get().getParameterTypes()[0];
                convertorFactory.getConvertor(this, argType, mappedObjects).invoke(method.get(), object, field.getValue());
            } else {
                Optional<Field> fieldByName = getFieldByName(object, field.getKey());
                if (fieldByName.isPresent()) {
                    Class<?> argType = fieldByName.get().getType();
                    convertorFactory.getConvertor(this, argType, mappedObjects).setField(fieldByName.get(), object, field.getValue());
                } else
                    throw new TransformerException("No setter nor field " + field.getKey() + " could be found in class " + object.getClass().getName() +"; context: "+field.getValue());
            }
        } catch (Throwable t) {
            throw new TransformerException("Transformation was not successful", t);
        }
    }

    private Optional<Field> getFieldByName(Object object, String fieldName) {
        for (Field field : object.getClass().getFields()) {
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

    private Object createChildObject(Composite parent, JsonNode childDefinition) throws TransformerException {
        try {
            if (!childDefinition.has(KEY_SPECIAL_TYPE))
                throw new IllegalArgumentException("Could not deduce the child type without explicit definition: "+childDefinition);
            Class<?> widgetClass = ObjectConvertor.deduceClassFromNode(childDefinition);
            Constructor<?> chosenConstructor = null;

            Constructor<?>[] constructors = widgetClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 2) {
                    if (Composite.class.isAssignableFrom(parameterTypes[0]) &&
                            parameterTypes[1].equals(int.class)) {
                        chosenConstructor = constructor;
                        break;
                    }
                }
            }

            if (chosenConstructor == null)
                throw new TransformerException("Could not find adequate constructor(? extends Composite, int) in class "
                        + widgetClass.getName());

            int style = SWT.NONE;
            if (childDefinition.has(KEY_SPECIAL_STYLE)) {
                JsonNode styleNode = childDefinition.get(KEY_SPECIAL_STYLE);
                AbstractConvertor exactTypeConvertor = (AbstractConvertor) convertorFactory.getExactTypeConvertor(int.class).get();
                style = (Integer) exactTypeConvertor.getValueFromJson(styleNode);
            }
            return chosenConstructor.newInstance(parent, style);
        } catch (ReflectiveOperationException e) {
            throw new TransformerException("Widget creation of class failed", e);
        }
    }

}
