package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.management.Query;
import javax.persistence.TypedQuery;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
@Repository
@Transactional
public class JpaMedijRepository extends AbstractRepository implements MedijRepository {

    @Autowired private PozicijaRepository pozicijaRepository;

    @Autowired private TipMedijaRepository tipMedijaRepository;

    @Override
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

        log.info("Adding new medium: indeksID=" + medij.getIndeks() +
                        ", pozicijaID=" + defaultPozicija.getIdpozicija() +
                        ", tipMedijaID=" + tipMedija.getIdtip());


        entityManager.persist(medij);
    }
}
