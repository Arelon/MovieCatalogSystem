package net.milanaleksic.mcs.infrastructure.gui.transformer;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

import java.util.Map;
import java.util.regex.*;

import static com.google.common.base.Preconditions.checkNotNull;

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

            .put("wrap", SWT.WRAP)
            .put("shadow_out", SWT.SHADOW_OUT)
            .put("horizontal", SWT.HORIZONTAL)
            .put("center", SWT.CENTER)
            .build();

    @Override
    protected Object getValueFromJson(JsonNode node) throws TransformerException {
        String input = node.asText();
        checkNotNull(input);
        String[] values = input.split("\\|");
        int ofTheJedi = 0;
        for (String value : values) {
            Matcher matcher = magicConstantsValue.matcher(value);
            if (matcher.matches()) {
                Integer matchedMagicConstantValue = magicConstants.get(matcher.group(1));
                if (matchedMagicConstantValue == null)
                    throw new TransformerException("Magic constant does not exist - "+matcher.group(1));
                ofTheJedi |= matchedMagicConstantValue;
            } else
                ofTheJedi |= Integer.parseInt(value);
        }
        return ofTheJedi;
    }

}
