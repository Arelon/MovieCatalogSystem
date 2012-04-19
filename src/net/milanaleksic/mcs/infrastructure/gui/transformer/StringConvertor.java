package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.base.Strings;
import org.codehaus.jackson.JsonNode;

import java.util.ResourceBundle;
import java.util.regex.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:23 PM
 */
public class StringConvertor extends AbstractConvertor {

    private static final Pattern resourceMessage = Pattern.compile("\\[(.*)\\]");
    private ResourceBundle resourceBundle;

    public StringConvertor(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    @Override
    protected Object getValueFromJson(JsonNode node) throws TransformerException {
        String fieldValue = node.asText();
        if (Strings.isNullOrEmpty(fieldValue))
            return fieldValue;

        Matcher matcher = resourceMessage.matcher(fieldValue);
        if (matcher.matches())
            fieldValue = resourceBundle.getString(matcher.group(1));
        return fieldValue;
    }
}
