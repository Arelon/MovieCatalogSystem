package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Strings;
import net.milanaleksic.mcs.application.ApplicationManager;
import org.codehaus.jackson.JsonNode;

import javax.inject.Inject;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:23 PM
 */
public class StringConverter extends TypedConverter<String> {

    @Inject
    private ApplicationManager applicationManager;

    private static final Pattern resourceMessage = Pattern.compile("\\[(.*)\\]");

    @Override
    protected String getValueFromJson(JsonNode node, Map<String, Object> mappedObjects) throws TransformerException {
        String fieldValue = node.asText();
        if (Strings.isNullOrEmpty(fieldValue))
            return fieldValue;

        // TODO: this should be done better with state machine instead of regex - to allow multiple replacements of different templates
        Matcher matcher = resourceMessage.matcher(fieldValue);
        if (matcher.find())
            return matcher.replaceAll(applicationManager.getMessagesBundle().getString(matcher.group(1)));
        return fieldValue;
    }

}
