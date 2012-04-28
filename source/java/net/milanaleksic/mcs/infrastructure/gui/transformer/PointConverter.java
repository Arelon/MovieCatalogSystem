package net.milanaleksic.mcs.infrastructure.gui.transformer;

import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.graphics.Point;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 2:58 PM
 */
public class PointConverter extends AbstractConverter {

    @Override
    protected Object getValueFromJson(JsonNode value) {
        String[] nodeValue = value.asText().split(","); //NON-NLS
        int x = Integer.parseInt(nodeValue[0]);
        int y = Integer.parseInt(nodeValue[1]);
        return new Point(x, y);
    }
}
