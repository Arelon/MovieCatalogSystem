package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Optional;
import org.codehaus.jackson.JsonNode;

import java.util.regex.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:03 PM
 */
public class ObjectConvertor extends AbstractConvertor {

    private static final Pattern magicConstantsValue = Pattern.compile("\\((.*)\\)");

    private Transformer originator;
    private Class<?> argType;

    public ObjectConvertor(Transformer originator, Class<?> argType) {
        this.originator = originator;
        this.argType = argType;
    }

    @Override
    protected Object getValueFromJson(JsonNode node) throws TransformerException {
        if (node.isTextual()) {
            String originalValue = node.asText();
            Matcher matcher = magicConstantsValue.matcher(originalValue);
            if (matcher.matches()) {
                Optional<Object> mappedObject = originator.getMappedObject(matcher.group(1));
                if (!mappedObject.isPresent())
                    throw new TransformerException("Object does not exist - "+node.asText());
                return mappedObject.get();
            }
            else
                throw new TransformerException("Invalid syntax for magical value - " + originalValue);
        }
        return originator.createWidgetFromResource(argType, node);
    }

}
