package net.milanaleksic.mcs.application.util;

import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:29 PM
 */
public class StatisticsManager implements LifecycleListener {

    private static final Log log = LogFactory.getLog(StatisticsManager.class);

    @Inject private ProgramArgsService programArgsService;

    @Inject
    @SuppressWarnings({"SpringJavaAutowiringInspection"})
    private SessionFactory sessionFactory;

    @Override public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        if (programArgsService.getProgramArgs().isCollectStatistics()) {
            sessionFactory.getStatistics().setStatisticsEnabled(true);
        }
    }

    @Override public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        if (programArgsService.getProgramArgs().isCollectStatistics()) {
            if (log.isInfoEnabled())
                log.info("Statistics information: " + sessionFactory.getStatistics()); //NON-NLS
        }
    }

}
