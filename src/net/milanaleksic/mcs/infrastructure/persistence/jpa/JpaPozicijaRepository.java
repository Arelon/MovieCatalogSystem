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
public class JpaPozicijaRepository extends AbstractRepository implements PozicijaRepository {

    @Override
    @Transactional(propagation=Propagation.SUPPORTS, readOnly = true)
    public List<Pozicija> getPozicijas() {
        return entityManager.createNamedQuery("getPozicijasOrdered", Pozicija.class).getResultList();
    }

    @Override
    public void addPozicija(Pozicija position) {
        if (position.isDefault())
            entityManager.createNamedQuery("removeDefaultFlagIfOneExists").executeUpdate();
        entityManager.persist(position);
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
    @Transactional(propagation=Propagation.SUPPORTS, readOnly = true)
    public Pozicija getDefaultPozicija() {
        TypedQuery<Pozicija> defaultPozicija = entityManager.createNamedQuery("getPozicijaDefault", Pozicija.class);
        defaultPozicija.setMaxResults(1);
        List<Pozicija> defaultPozicijaOrFirstOneInOrder = defaultPozicija.getResultList();
        return defaultPozicijaOrFirstOneInOrder.size() == 0 ? null : defaultPozicijaOrFirstOneInOrder.get(0);
    }

    @Override
    @Transactional(propagation=Propagation.SUPPORTS, readOnly = true)
    public Pozicija getByName(String locationName) {
        TypedQuery<Pozicija> pozicijaByName = entityManager.createNamedQuery("getPozicijaByName", Pozicija.class);
        pozicijaByName.setParameter("locationName", locationName);
        return pozicijaByName.getSingleResult();
    }

    @Override
    public void updatePozicija(Pozicija pozicija) {
        if (pozicija.isDefault())
            entityManager.createNamedQuery("removeDefaultFlagIfOneExists").executeUpdate();
        pozicija = entityManager.merge(pozicija);
        for (Medij medij : pozicija.getMedijs()) {
            for (Film film : medij.getFilms()) {
                film.refreshFilmLocation();
            }
        }
    }

}
