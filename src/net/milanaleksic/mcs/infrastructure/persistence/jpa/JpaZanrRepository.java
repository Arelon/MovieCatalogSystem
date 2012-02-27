package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
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
    @Transactional(propagation=Propagation.SUPPORTS, readOnly = true)
    public List<Zanr> getZanrs() {
        return entityManager.createNamedQuery("getZanrsOrdered", Zanr.class).getResultList();
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
        TypedQuery<Long> query = entityManager.createNamedQuery("getCountOfFilmWithZanrByName", Long.class);
        query.setParameter("zanr", zanrToDelete);
        long count = query.getSingleResult();
        if (count > 0)
            throw new ApplicationException("You can't delete this Genre since "+count+" movies are referencing it");

        entityManager.remove(zanrToDelete);
    }

    @Override
    @Transactional(propagation= Propagation.SUPPORTS, readOnly = true)
    public Zanr getZanrByName(String genreName) {
        TypedQuery<Zanr> getZanrByName = entityManager.createNamedQuery("getZanrByName", Zanr.class);
        getZanrByName.setParameter("genreName", genreName);
        return getZanrByName.getSingleResult();
    }

    @Override
    public void updateZanr(Zanr zanr) {
        entityManager.merge(zanr);
    }
}
