package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.Pozicija;
import net.milanaleksic.mcs.domain.PozicijaRepository;
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
public class JpaPozicijaRepository extends AbstractRepository implements PozicijaRepository {

    private final String DEFAULT_POZICIJA_NAME = "присутан";

    @Override
    public List<Pozicija> getPozicijas() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pozicija> cq = builder.createQuery(Pozicija.class);
        Root<Pozicija> from = cq.from(Pozicija.class);
        cq.orderBy(builder.asc(builder.lower(from.<String>get("pozicija"))));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public void addPozicija(String newPozicija) {
        Pozicija pozicija = new Pozicija();
        pozicija.setPozicija(newPozicija);
        entityManager.persist(pozicija);
    }

    @Override
    public void deletePozicija(String pozicija) throws ApplicationException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pozicija> cq = builder.createQuery(Pozicija.class);
        Root<Pozicija> from = cq.from(Pozicija.class);
        cq.where(builder.equal(from.<String>get("pozicija"), pozicija));
        Pozicija pozicijaToDelete = entityManager.createQuery(cq).getSingleResult();

        TypedQuery<Long> query = entityManager.createQuery("select count(*) from Medij where pozicija=:pozicija", Long.class);
        query.setParameter("pozicija", pozicijaToDelete);
        long count = query.getSingleResult();
        if (count > 0)
            throw new ApplicationException("You can't delete this Position since "+count+" mediums are referencing it");

        entityManager.remove(pozicijaToDelete);
    }

    @Override
    public Pozicija getDefaultPozicija() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pozicija> cq = builder.createQuery(Pozicija.class);
        Root<Pozicija> from = cq.from(Pozicija.class);
        cq.where(builder.equal(from.<String>get("pozicija"), DEFAULT_POZICIJA_NAME));
        return entityManager.createQuery(cq).getSingleResult();
    }
}
