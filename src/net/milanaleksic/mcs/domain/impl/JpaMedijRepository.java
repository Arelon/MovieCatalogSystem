package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.*;
import org.springframework.stereotype.Repository;
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

    @Inject private TipMedijaRepository tipMedijaRepository;

    @Override
    public Medij getCompleteMedij(Medij rawMedij) {
        rawMedij = entityManager.merge(rawMedij);
        rawMedij.getFilms().size();
        return rawMedij;
    }

    @Override
    @Transactional(readOnly = true)
    public int getNextMedijIndeks(String mediumTypeName) {
        TypedQuery<Integer> query = entityManager.createQuery(
                "select max(indeks)+1 from Medij m where m.tipMedija.naziv=:tipMedija", Integer.class);
        query.setParameter("tipMedija", mediumTypeName);
        Integer nextMedijIndeks = query.getSingleResult();
        if (nextMedijIndeks==null)
            return 1;
        else
            return nextMedijIndeks;
    }

    @Override
    public void saveMedij(int index, String mediumTypeName) {
        Medij medij = new Medij();
        medij.setFilms(null);
        medij.setIndeks(index);

        Pozicija defaultPozicija = pozicijaRepository.getDefaultPozicija();
        defaultPozicija.addMedij(medij);

        TipMedija tipMedija = tipMedijaRepository.getTipMedija(mediumTypeName);
        tipMedija.addMedij(medij);

        entityManager.persist(medij);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medij> getMedijs() {
        TypedQuery<Medij> query = entityManager.createQuery(
            "from Medij m order by m.tipMedija.naziv, indeks", Medij.class);
        query.setHint("org.hibernate.cacheable", true);
        return query.getResultList();
    }
}
