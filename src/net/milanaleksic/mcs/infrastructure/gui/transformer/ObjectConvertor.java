package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:03 PM
 */
public class ObjectConvertor extends AbstractConvertor  {

    private static final Pattern magicConstantsValue = Pattern.compile("\\((.*)\\)");

    private static Map<String, Class<?>> knownClasses = ImmutableMap.of();

    static {
        try {
            knownClasses = ImmutableMap
                    .<String, Class<?>>builder()
                    .put("showImageComposite", Class.forName("net.milanaleksic.mcs.application.gui.helper.ShowImageComposite"))

                    .put("gridData", Class.forName("org.eclipse.swt.layout.GridData"))
                    .put("gridLayout", Class.forName("org.eclipse.swt.layout.GridLayout"))

                    .put("button", Class.forName("org.eclipse.swt.widgets.Button"))
                    .put("canvas", Class.forName("org.eclipse.swt.widgets.Canvas"))
                    .put("composite", Class.forName("org.eclipse.swt.widgets.Composite"))
                    .put("group", Class.forName("org.eclipse.swt.widgets.Group"))
                    .put("label", Class.forName("org.eclipse.swt.widgets.Label"))
                    .put("tabFolder", Class.forName("org.eclipse.swt.widgets.TabFolder"))
                    .put("tabItem", Class.forName("org.eclipse.swt.widgets.TabItem"))
                    .put("table", Class.forName("org.eclipse.swt.widgets.Table"))
                    .put("tableColumn", Class.forName("org.eclipse.swt.widgets.TableColumn"))
                    .put("link", Class.forName("org.eclipse.swt.widgets.Link"))
                    .put("list", Class.forName("org.eclipse.swt.widgets.List"))
                    .put("text", Class.forName("org.eclipse.swt.widgets.Text"))
                    .put("combo", Class.forName("org.eclipse.swt.widgets.Combo"))
                    .put("toolBar", Class.forName("org.eclipse.swt.widgets.ToolBar"))
                    .put("toolItem", Class.forName("org.eclipse.swt.widgets.ToolItem"))

                    .put("scrolledComposite", Class.forName("org.eclipse.swt.custom.ScrolledComposite"))
                    .build();
        } catch (ClassNotFoundException e) {
            Logger.getLogger(ObjectConvertor.class).error("At least one class was not found on classpath", e);
            e.printStackTrace();
        }
    }

    private final Transformer transformer;
    private final Class<?> argType;
    private final Map<String, Object> mappedObjects;
    private final ApplicationContext applicationContext;

    public ObjectConvertor(Transformer transformer, Class<?> argType, Map<String, Object> mappedObjects, ApplicationContext applicationContext) {
        this.transformer = transformer;
        this.argType = argType;
        this.mappedObjects = mappedObjects;
        this.applicationContext = applicationContext;
    }

    @Override
    protected Object getValueFromJson(JsonNode node) throws TransformerException {
        if (node.isTextual()) {
            String originalValue = node.asText();
            Matcher matcher = magicConstantsValue.matcher(originalValue);
            if (matcher.matches()) {
                Object mappedObject = mappedObjects.get(matcher.group(1));
                if (mappedObject == null)
                    mappedObject = applicationContext.getBean(matcher.group(1));
                if (mappedObject == null)
                    throw new TransformerException("Object does not exist - " + node.asText());
                return mappedObject;
            } else
                throw new TransformerException("Invalid syntax for magical value - " + originalValue);
        }
        return createWidgetFromResource(argType, node);
    }

    public Object createWidgetFromResource(Class<?> widgetClass, JsonNode value) throws TransformerException {
        try {
            Object widget;
            Class<?> deducedClass = deduceClassFromNode(value);
            if (deducedClass != null)
                widgetClass = deducedClass;
            widget = widgetClass.newInstance();
            transformer.transformNodeToProperties(value, widget, mappedObjects);
            return widget;
        } catch (InstantiationException | TransformerException | IllegalAccessException e) {
            throw new TransformerException("Widget creation of class " + widgetClass.getName() + " failed", e);
        }
    }

    public static Class<?> deduceClassFromNode(JsonNode value) throws TransformerException {
        if (value.has(Transformer.KEY_SPECIAL_TYPE)) {
            String classIdentifier = value.get(Transformer.KEY_SPECIAL_TYPE).asText();
            Class<?> aClass = knownClasses.get(classIdentifier);
            if (aClass != null)
                return aClass;
            else
                try {
                    return Class.forName(classIdentifier);
                } catch (ClassNotFoundException e) {
                    throw new TransformerException("Class was not found: "+classIdentifier, e);
                }
        }
        return null;
    }

}
