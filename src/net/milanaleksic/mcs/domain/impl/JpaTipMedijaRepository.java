package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.TipMedija;
import net.milanaleksic.mcs.domain.TipMedijaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
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
    public List<TipMedija> getTipMedijas() {
        log.debug("TipMedijaRepository::getTipMedijas");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TipMedija> cq = builder.createQuery(TipMedija.class);
        Root<TipMedija> from = cq.from(TipMedija.class);
        cq.orderBy(builder.asc(builder.lower(from.<String>get("naziv"))));
        return entityManager.createQuery(cq).getResultList();
    }
}
