package net.milanaleksic.mcs.infrastructure.gui.transformer;

import org.codehaus.jackson.JsonNode;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:03 PM
 */
public class ObjectConvertor extends AbstractConvertor {

    private Transformer originator;
    private Class<?> argType;

    public ObjectConvertor(Transformer originator, Class<?> argType) {
        this.originator = originator;
        this.argType = argType;
    }

    @Override
    protected Object getValueFromJson(JsonNode node) throws TransformerException {
        return originator.createWidgetFromResource(argType, node);
    }

}
