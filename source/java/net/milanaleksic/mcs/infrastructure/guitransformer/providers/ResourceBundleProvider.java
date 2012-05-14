package net.milanaleksic.mcs.infrastructure.guitransformer.providers;

import net.milanaleksic.mcs.infrastructure.messages.ResourceBundleSource;

import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 1:01 PM
 */
public class ResourceBundleProvider implements net.milanaleksic.guitransformer.providers.ResourceBundleProvider {

    @Inject
    private ResourceBundleSource resourceBundleSource;

    @Override
    public ResourceBundle getResourceBundle() {
        return resourceBundleSource.getMessagesBundle();
    }

}
