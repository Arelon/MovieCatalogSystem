package net.milanaleksic.mcs.infrastructure;

import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 9:15 PM
 */
public interface LifecycleListener {

    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration);

    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration);

}
