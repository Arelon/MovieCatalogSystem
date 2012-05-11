package net.milanaleksic.mcs.infrastructure.messages;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.LifeCycleListener;
import net.milanaleksic.mcs.infrastructure.config.*;
import net.milanaleksic.mcs.infrastructure.util.UTF8ResourceBundleControl;

import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 5/11/12
 * Time: 1:48 PM
 */
public class ResourceBundleSource implements LifeCycleListener{

    private Optional<ResourceBundle> messageBundle = Optional.absent();

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        init(userConfiguration);
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
    }

    public ResourceBundle getMessagesBundle() {
        return messageBundle.get();
    }

    public void init(UserConfiguration userConfiguration) {
        messageBundle = Optional.of(ResourceBundle.getBundle("messages", //NON-NLS
                new Locale(userConfiguration.getLocaleLanguage()), new UTF8ResourceBundleControl()));
    }

}
