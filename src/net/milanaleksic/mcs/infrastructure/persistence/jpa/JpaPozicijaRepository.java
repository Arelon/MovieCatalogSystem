package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.domain.model.Pozicija;
import net.milanaleksic.mcs.domain.model.PozicijaRepository;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Pozicija_;
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

    @Override
    @Transactional(readOnly = true)
    public List<Pozicija> getPozicijas() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pozicija> cq = builder.createQuery(Pozicija.class);
        Root<Pozicija> from = cq.from(Pozicija.class);
        cq.orderBy(builder.asc(builder.lower(from.<String>get(Pozicija_.pozicija))));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public void addPozicija(String newPozicija) {
        Pozicija pozicija = new Pozicija();
        pozicija.setPozicija(newPozicija);
        entityManager.persist(pozicija);
    }

    @Override
    public void deletePozicijaByName(String pozicija) throws ApplicationException {
        Pozicija pozicijaToDelete = getByName(pozicija);
        TypedQuery<Long> query = entityManager.createNamedQuery("getCountOfMedijOnPozicijaByName", Long.class);
        query.setParameter("pozicija", pozicijaToDelete);
        long count = query.getSingleResult();
        if (count > 0)
            throw new ApplicationException("You can't delete this Position since "+count+" mediums are referencing it");

        entityManager.remove(pozicijaToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public Pozicija getDefaultPozicija() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pozicija> cq = builder.createQuery(Pozicija.class);
        Root<Pozicija> from = cq.from(Pozicija.class);
        cq.where(builder.equal(from.<String>get(Pozicija_.pozicija), Pozicija.DEFAULT_POZICIJA_NAME));
        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Pozicija getByName(String locationName) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pozicija> cq = builder.createQuery(Pozicija.class);
        Root<Pozicija> from = cq.from(Pozicija.class);
        ParameterExpression<String> locationNameParameter = builder.parameter(String.class, "locationName");
        cq.where(builder.equal(from.<String>get(Pozicija_.pozicija), locationNameParameter));
        TypedQuery<Pozicija> query = entityManager.createQuery(cq);
        query.setParameter(locationNameParameter, locationName);
        return query.getSingleResult();
    }

}
