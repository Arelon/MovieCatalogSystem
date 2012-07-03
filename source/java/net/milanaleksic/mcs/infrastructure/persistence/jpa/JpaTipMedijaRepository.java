package net.milanaleksic.mcs.infrastructure.persistence.jpa;

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
@SuppressWarnings({"HardCodedStringLiteral"})
public class JpaTipMedijaRepository extends AbstractRepository implements TipMedijaRepository {

    @Override
    @Transactional(propagation=Propagation.SUPPORTS, readOnly = true)
    public List<TipMedija> getTipMedijas() {
        return entityManager.createNamedQuery("getTipMedijaOrdered", TipMedija.class).getResultList();
    }

    @Override
    @Transactional(propagation= Propagation.SUPPORTS, readOnly = true)
    public TipMedija getTipMedija(String mediumTypeName) {
        TypedQuery<TipMedija> tipMedijaByName = entityManager.createNamedQuery("getTipMedijaByName", TipMedija.class);
        tipMedijaByName.setParameter("mediumTypeName", mediumTypeName);
        return tipMedijaByName.getSingleResult();
    }

    @Override
    public void deleteMediumTypeByName(String mediumTypeName) {
        TipMedija mediumTypeToDelete = getTipMedija(mediumTypeName);
        TypedQuery<Long> query = entityManager.createNamedQuery("getCountOfMediumsWithMediumTypeByName", Long.class);
        query.setParameter("tipMedija", mediumTypeToDelete);
        long count = query.getSingleResult();
        if (count > 0)
            throw new RuntimeException("You can't delete this medium type since "+count+" mediums are referencing it");
        entityManager.remove(mediumTypeToDelete);
    }

    @Override
    public TipMedija addTipMedija(String newMediumType) {
        TipMedija ofTheJedi = new TipMedija();
        ofTheJedi.setNaziv(newMediumType);
        entityManager.persist(ofTheJedi);
        return ofTheJedi;
    }

    @Override
    public void updateTipMedija(TipMedija tipMedija) {
        tipMedija = entityManager.merge(tipMedija);
        for (Medij medij : tipMedija.getMedijs()) {
            for (Film film : medij.getFilms()) {
                film.refreshMedijListAsString();
            }
        }
    }

}
