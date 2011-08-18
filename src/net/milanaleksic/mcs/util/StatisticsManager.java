package net.milanaleksic.mcs.util;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.event.LifecycleListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.jpa.JpaTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:29 PM
 */
public class StatisticsManager implements LifecycleListener {

    private static final Log log = LogFactory.getLog(StatisticsManager.class);

    @Autowired private ApplicationManager applicationManager;

    @Override public void applicationStarted() {
//        if (applicationManager.getProgramArgs().isCollectStatistics()) {
//            ((HibernateSessionProxy)entityManager.getDelegate()).getSessionFactory().getStatistics().setStatisticsEnabled(true);
//        }
    }

    @Override public void applicationShutdown() {
//        if (applicationManager.getProgramArgs().isCollectStatistics()) {
//            log.info("Statistics information: "+ hibernateTemplate.getSessionFactory().getStatistics());
//        }
    }
}
