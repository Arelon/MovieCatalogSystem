package net.milanaleksic.mcs.util;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.event.LifecycleListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:29 PM
 */
public class StatisticsManager implements LifecycleListener {

    private static final Log log = LogFactory.getLog(StatisticsManager.class);

    @Autowired private ApplicationManager applicationManager;

    @SuppressWarnings({"SpringJavaAutowiringInspection"})
    @Autowired
    private SessionFactory sessionFactory;

    @Override public void applicationStarted() {
        if (applicationManager.getProgramArgs().isCollectStatistics()) {
            sessionFactory.getStatistics().setStatisticsEnabled(true);
        }
    }

    @Override public void applicationShutdown() {
        if (applicationManager.getProgramArgs().isCollectStatistics()) {
            log.info("Statistics information: " + sessionFactory.getStatistics());
        }
    }

}
