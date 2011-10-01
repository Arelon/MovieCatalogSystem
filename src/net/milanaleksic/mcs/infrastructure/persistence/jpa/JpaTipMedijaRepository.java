package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.domain.model.TipMedija;
import net.milanaleksic.mcs.domain.model.TipMedijaRepository;
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
public class JpaTipMedijaRepository extends AbstractRepository implements TipMedijaRepository {

    @Override
    @Transactional(readOnly = true)
    public List<TipMedija> getTipMedijas() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TipMedija> cq = builder.createQuery(TipMedija.class);
        Root<TipMedija> from = cq.from(TipMedija.class);
        cq.orderBy(builder.asc(builder.lower(from.<String>get("naziv"))));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipMedija getTipMedija(String mediumTypeName) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TipMedija> cq = builder.createQuery(TipMedija.class);
        Root<TipMedija> from = cq.from(TipMedija.class);
        ParameterExpression<String> mediumTypeNameParameter = builder.parameter(String.class, "mediumTypeName");
        cq.where(builder.equal(from.<String>get("naziv"), mediumTypeNameParameter));
        TypedQuery<TipMedija> query = entityManager.createQuery(cq);
        query.setParameter(mediumTypeNameParameter, mediumTypeName);
        return query.getSingleResult();
    }
}
