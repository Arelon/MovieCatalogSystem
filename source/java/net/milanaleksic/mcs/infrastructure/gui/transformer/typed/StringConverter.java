package net.milanaleksic.mcs.infrastructure.gui.transformer.typed;

import com.google.common.base.Strings;
import net.milanaleksic.mcs.infrastructure.gui.transformer.TransformerException;
import net.milanaleksic.mcs.infrastructure.messages.ResourceBundleSource;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.regex.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:23 PM
 */
public class StringConverter extends TypedConverter<String> {

    private static final Pattern resourceMessage = Pattern.compile("\\[(.*)\\]");

    @Autowired
    private ResourceBundleSource resourceBundleSource;

    @Override
    public String getValueFromJson(JsonNode node, Map<String, Object> mappedObjects) throws TransformerException {
        String fieldValue = node.asText();
        if (Strings.isNullOrEmpty(fieldValue))
            return fieldValue;

        // TODO: this should be done better with state machine instead of regex - to allow multiple replacements of different templates
        Matcher matcher = resourceMessage.matcher(fieldValue);
        if (matcher.find())
            return matcher.replaceAll(resourceBundleSource.getMessagesBundle().getString(matcher.group(1)));
        return fieldValue;
    }

}
