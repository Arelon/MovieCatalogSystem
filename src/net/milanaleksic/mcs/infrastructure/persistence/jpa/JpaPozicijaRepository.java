package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Pozicija;
import net.milanaleksic.mcs.domain.model.PozicijaRepository;
import org.springframework.stereotype.Repository;
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
    @Transactional(readOnly = true)
    public List<Pozicija> getPozicijas() {
        return entityManager.createNamedQuery("getPozicijasOrdered", Pozicija.class).getResultList();
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
        return getByName(Pozicija.DEFAULT_POZICIJA_NAME);
    }

    @Override
    @Transactional(readOnly = true)
    public Pozicija getByName(String locationName) {
        TypedQuery<Pozicija> pozicijaByName = entityManager.createNamedQuery("getPozicijaByName", Pozicija.class);
        pozicijaByName.setParameter("locationName", locationName);
        return pozicijaByName.getSingleResult();
    }

}
