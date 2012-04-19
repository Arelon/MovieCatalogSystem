package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.layout.GridData;

import java.util.Map;
import java.util.regex.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:23 PM
 */
public class IntegerConvertor extends AbstractConvertor {

    private static final Pattern magicConstantsValue = Pattern.compile("\\{(.*)\\}");

    @SuppressWarnings({"HardCodedStringLiteral"})
    private static final Map<String, Integer> magicConstants = ImmutableMap.<String, Integer>builder()
            .put("grid.center", GridData.CENTER)
            .put("grid.begin", GridData.BEGINNING)
            .put("grid.end", GridData.END)
            .build();

    @Override
    protected Object getValueFromJson(JsonNode node) throws TransformerException {
        String input = node.asText();
        Matcher matcher = magicConstantsValue.matcher(input);
        if (matcher.matches()) {
            Integer value = magicConstants.get(matcher.group(1));
            if (value == null)
                throw new TransformerException("Magic constant does not exist - "+matcher.group(1));
            return value;
        }
        return node.asInt();
    }

}
