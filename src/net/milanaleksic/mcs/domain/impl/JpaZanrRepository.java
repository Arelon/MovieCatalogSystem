package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
@Repository
@Transactional
public class JpaZanrRepository extends AbstractRepository implements ZanrRepository {

    @Override
    public List<Zanr> getZanrs() {
        log.debug("ZanrRepository::getZanrs");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Zanr> cq = builder.createQuery(Zanr.class);
        Root<Zanr> from = cq.from(Zanr.class);
        cq.orderBy(builder.asc(builder.lower(from.<String>get("zanr"))));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public void addZanr(String newZanr) {
        Zanr zanr = new Zanr();
        zanr.setZanr(newZanr);
        entityManager.persist(zanr);
    }
}
