package net.milanaleksic.mcs.application.util;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.LifecycleListener;
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

    @Inject private ApplicationManager applicationManager;

    @SuppressWarnings({"SpringJavaAutowiringInspection"})
    @Inject
    private SessionFactory sessionFactory;

    @Override public void applicationStarted() {
        if (applicationManager.getProgramArgs().isCollectStatistics()) {
            sessionFactory.getStatistics().setStatisticsEnabled(true);
        }
    }

    @Override public void applicationShutdown() {
        if (applicationManager.getProgramArgs().isCollectStatistics()) {
            if (log.isInfoEnabled())
                log.info("Statistics information: " + sessionFactory.getStatistics());
        }
    }

}
