package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.Zanr;
import net.milanaleksic.mcs.domain.ZanrRepository;
import net.milanaleksic.mcs.util.ApplicationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
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

    @Override
    public void deleteZanr(String zanr) throws ApplicationException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Zanr> cq = builder.createQuery(Zanr.class);
        Root<Zanr> from = cq.from(Zanr.class);
        cq.where(builder.equal(from.<String>get("zanr"), zanr));
        Zanr zanrToDelete = entityManager.createQuery(cq).getSingleResult();

        TypedQuery<Long> query = entityManager.createQuery("select count(*) from Film where zanr=:zanr", Long.class);
        query.setParameter("zanr", zanrToDelete);
        long count = query.getSingleResult();
        if (count > 0)
            throw new ApplicationException("You can't delete this Genre since there are "+count+" movies in that position");

        entityManager.remove(zanrToDelete);
    }
}
