package net.milanaleksic.mcs.infrastructure.gui.transformer;

import org.codehaus.jackson.JsonNode;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 4:32 PM
 */
public class BooleanConverter extends AbstractConverter {

    @Override
    protected Object getValueFromJson(JsonNode node) {
        return node.asBoolean();
    }

}
