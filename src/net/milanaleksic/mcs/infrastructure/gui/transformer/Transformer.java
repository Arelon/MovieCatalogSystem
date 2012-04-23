package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;

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
    private ConverterFactory converterFactory;

    public Transformer() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public TransformationContext fillManagedForm(Object formObject) throws TransformerException {
        return fillManagedForm(formObject, new Shell());
    }

    @MethodTiming(name = "GUI transformation")
    public TransformationContext fillManagedForm(Object formObject, Shell shell) throws TransformerException {
        String thisClassNameAsResourceLocation = formObject.getClass().getCanonicalName().replaceAll("\\.", "/");
        String formName = "/" + thisClassNameAsResourceLocation + ".gui"; //NON-NLS
        TransformationContext transformationContext = fillForm(formName, shell);
        embedComponents(formObject, transformationContext);
        embedEvents(formObject, transformationContext);
        return transformationContext;
    }

    private void embedComponents(Object targetObject, TransformationContext transformationContext) throws TransformerException {
        Field[] fields = targetObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            EmbeddedComponent annotation = field.getAnnotation(EmbeddedComponent.class);
            if (annotation == null)
                continue;
            String name = annotation.name();
            if (name.isEmpty())
                name = field.getName();
            boolean wasPublic = Modifier.isPublic(field.getModifiers());
            if (!wasPublic)
                field.setAccessible(true);
            Optional<Object> mappedObject = transformationContext.getMappedObject(name);
            if (!mappedObject.isPresent())
                throw new IllegalStateException("Field marked as embedded could not be found: " + targetObject.getClass().getName() + "." + field.getName());
            try {
                field.set(targetObject, mappedObject.get());
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new TransformerException("Error while embedding component field named " + field.getName(), e);
            } finally {
                if (!wasPublic)
                    field.setAccessible(false);
            }
        }
    }

    private void embedEvents(Object targetObject, TransformationContext transformationContext) throws TransformerException {
        Field[] fields = targetObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            List<EmbeddedEventListener> allListeners = Lists.newArrayList();
            EmbeddedEventListeners annotations = field.getAnnotation(EmbeddedEventListeners.class);
            if (annotations != null)
                allListeners.addAll(Arrays.asList(annotations.value()));
            else {
                EmbeddedEventListener annotation = field.getAnnotation(EmbeddedEventListener.class);
                if (annotation != null)
                    allListeners.add(annotation);
            }
            for (EmbeddedEventListener listenerAnnotation : allListeners) {
                String componentName = listenerAnnotation.component();
                Optional<Object> mappedObject = componentName.isEmpty()
                        ? Optional.<Object>of(transformationContext.getShell())
                        : transformationContext.getMappedObject(componentName);
                if (!mappedObject.isPresent())
                    throw new IllegalStateException("Event source could not be found in the GUI definition: " + targetObject.getClass().getName() + "." + field.getName());
                handleSingleEventDelegation(targetObject, field, listenerAnnotation.event(), (Widget) mappedObject.get());
            }
        }
    }

    private void handleSingleEventDelegation(Object targetObject, Field field, int event, Widget mappedObject) throws TransformerException {
        boolean wasPublic = Modifier.isPublic(field.getModifiers());
        if (!wasPublic)
            field.setAccessible(true);
        try {
            mappedObject.addListener(event, (Listener) field.get(targetObject));
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new TransformerException("Error while embedding component field named " + field.getName(), e);
        } finally {
            if (!wasPublic)
                field.setAccessible(false);
        }
    }

    TransformationContext createFormFromResource(String fullName) throws TransformerException {
        return fillForm(fullName, new Shell());
    }

    private TransformationContext fillForm(String fullName, Shell shell) throws TransformerException {
        Map<String, Object> mappedObjects = Maps.newHashMap();
        mappedObjects.put("bundle", applicationManager.getMessagesBundle()); //NON-NLS
        try (InputStream resourceAsStream = getClass().getResourceAsStream(fullName)) {
            deserializeObjectFromNode(mapper.readValue(resourceAsStream, JsonNode.class), shell, mappedObjects);
            return new TransformationContext(shell, mappedObjects);
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + fullName, e);
        }
    }

    private void deserializeObjectFromNode(JsonNode jsonNode, Object object, Map<String, Object> mappedObjects) throws TransformerException {
        if (jsonNode.has(KEY_SPECIAL_NAME)) {
            String objectName = jsonNode.get(KEY_SPECIAL_NAME).asText();
            mappedObjects.put(objectName, object);
        }
        transformNodeToProperties(jsonNode, object, mappedObjects);
    }

    private void transformChildren(JsonNode childrenNodes, Object parentWidget, Map<String, Object> mappedObjects) throws TransformerException {
        if (!(parentWidget instanceof Composite) && !(parentWidget instanceof Menu))
            throw new IllegalStateException("Can not create children for parent which is not Composite nor Menu (" + parentWidget.getClass().getName() + " in this case)");
        try {
            for (JsonNode node : mapper.readValue(childrenNodes, JsonNode[].class)) {
                Object childObject = createChildObject(parentWidget, node);
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
            if (field.getKey().equals(KEY_SPECIAL_CHILDREN))
                transformChildren(field.getValue(), object, mappedObjects);
            else
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
                converterFactory.getConverter(this, argType, mappedObjects).invoke(method.get(), object, field.getValue());
            } else {
                Optional<Field> fieldByName = getFieldByName(object, field.getKey());
                if (fieldByName.isPresent()) {
                    Class<?> argType = fieldByName.get().getType();
                    converterFactory.getConverter(this, argType, mappedObjects).setField(fieldByName.get(), object, field.getValue());
                } else
                    throw new TransformerException("No setter nor field " + field.getKey() + " could be found in class " + object.getClass().getName() + "; context: " + field.getValue());
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

    private Object createChildObject(Object parent, JsonNode childDefinition) throws TransformerException {
        try {
            if (!childDefinition.has(KEY_SPECIAL_TYPE))
                throw new IllegalArgumentException("Could not deduce the child type without explicit definition: " + childDefinition);
            Class<?> widgetClass = ObjectConverter.deduceClassFromNode(childDefinition);
            Constructor<?> chosenConstructor = null;

            Constructor<?>[] constructors = widgetClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 2) {
                    if ((Composite.class.isAssignableFrom(parameterTypes[0]) ||
                            Menu.class.isAssignableFrom(parameterTypes[0])) &&
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
                AbstractConverter exactTypeConverter = (AbstractConverter) converterFactory.getExactTypeConverter(int.class).get();
                style = (Integer) exactTypeConverter.getValueFromJson(styleNode);
            }
            return chosenConstructor.newInstance(parent, style);
        } catch (ReflectiveOperationException e) {
            throw new TransformerException("Widget creation of class failed", e);
        }
    }

}
