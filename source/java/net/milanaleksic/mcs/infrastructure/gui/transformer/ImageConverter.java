package net.milanaleksic.mcs.infrastructure.gui.transformer;

import net.milanaleksic.mcs.infrastructure.image.ImageRepository;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.graphics.Image;

import javax.inject.Inject;
import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/21/12
 * Time: 7:30 PM
 */
public class ImageConverter extends TypedConverter<Image> {

    @Inject
    private ImageRepository imageRepository;

    @Override
    protected Image getValueFromJson(JsonNode node, Map<String, Object> mappedObjects) throws TransformerException {
        return imageRepository.getResourceImage(node.asText());
    }

}
