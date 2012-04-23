package net.milanaleksic.mcs.infrastructure.gui.transformer;

import net.milanaleksic.mcs.infrastructure.image.ImageRepository;
import org.codehaus.jackson.JsonNode;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 4/21/12
 * Time: 7:30 PM
 */
public class ImageConverter extends AbstractConverter {

    @Inject
    ImageRepository imageRepository;

    @Override
    protected Object getValueFromJson(JsonNode node) throws TransformerException {
        return imageRepository.getResourceImage(node.asText());
    }

}
