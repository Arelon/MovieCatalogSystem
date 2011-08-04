package net.milanaleksic.mcs.util;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.event.LifecycleListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:29 PM
 */
public class StatisticsManager implements LifecycleListener {

    private static final Log log = LogFactory.getLog(StatisticsManager.class);

    @Autowired private ApplicationManager applicationManager;

    @Autowired private HibernateTemplate hibernateTemplate;

    @Override public void applicationStarted() {
        if (applicationManager.getProgramArgs().isCollectStatistics())
            hibernateTemplate.getSessionFactory().getStatistics().setStatisticsEnabled(true);
    }

    @Override public void applicationShutdown() {
        if (applicationManager.getProgramArgs().isCollectStatistics()) {
            log.info("Statistics information: "+ hibernateTemplate.getSessionFactory().getStatistics());
        }
    }
}
