package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.domain.model.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
@Repository
@Transactional
public class JpaMedijRepository extends AbstractRepository implements MedijRepository {

    @Inject
    private PozicijaRepository pozicijaRepository;

    @Override
    public void saveMedij(int index, TipMedija tipMedija) {
        Medij medij = new Medij();
        medij.setIndeks(index);

        Optional<Pozicija> defaultPozicija = pozicijaRepository.getDefaultPozicija();
        if (defaultPozicija.isPresent())
            defaultPozicija.get().addMedij(medij);

        tipMedija = entityManager.merge(tipMedija);
        tipMedija.addMedij(medij);

        entityManager.persist(medij);
    }

    @Override
    @Transactional(propagation=Propagation.SUPPORTS, readOnly = true)
    public List<Medij> getMedijs() {
        TypedQuery<Medij> query = entityManager.createNamedQuery("getMedijsOrdered", Medij.class);
        return query.getResultList();
    }

    @Override
    public void deleteMediumType(Medij medij) {
        medij = entityManager.merge(medij);
        entityManager.remove(medij);
    }
}
