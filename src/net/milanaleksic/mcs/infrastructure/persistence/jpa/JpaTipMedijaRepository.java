package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.domain.model.TipMedija;
import net.milanaleksic.mcs.domain.model.TipMedijaRepository;
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
}
