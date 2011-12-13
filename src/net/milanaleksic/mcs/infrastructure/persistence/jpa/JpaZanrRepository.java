package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.domain.model.Zanr;
import net.milanaleksic.mcs.domain.model.ZanrRepository;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Zanr_;
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
    @Transactional(readOnly = true)
    public List<Zanr> getZanrs() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Zanr> cq = builder.createQuery(Zanr.class);
        Root<Zanr> from = cq.from(Zanr.class);
        cq.orderBy(builder.asc(builder.lower(from.<String>get(Zanr_.zanr))));
        TypedQuery<Zanr> query = entityManager.createQuery(cq);
        query.setHint("org.hibernate.cacheable", true);
        return query.getResultList();
    }

    @Override
    public void addZanr(String newZanr) {
        Zanr zanr = new Zanr();
        zanr.setZanr(newZanr);
        entityManager.persist(zanr);
    }

    @Override
    public void deleteZanrByName(String zanr) throws ApplicationException {
        Zanr zanrToDelete = getZanrByName(zanr);

        TypedQuery<Long> query = entityManager.createQuery("select count(*) from Film where zanr=:zanr", Long.class);
        query.setParameter("zanr", zanrToDelete);
        long count = query.getSingleResult();
        if (count > 0)
            throw new ApplicationException("You can't delete this Genre since "+count+" movies are referencing it");

        entityManager.remove(zanrToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public Zanr getZanrByName(String genreName) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Zanr> cq = builder.createQuery(Zanr.class);
        Root<Zanr> from = cq.from(Zanr.class);
        ParameterExpression<String> genreNameParameter = builder.parameter(String.class, "genreName");
        cq.where(builder.equal(from.<String>get(Zanr_.zanr), genreNameParameter));
        TypedQuery<Zanr> query = entityManager.createQuery(cq);
        query.setParameter(genreNameParameter, genreName);
        return query.getSingleResult();
    }
}
