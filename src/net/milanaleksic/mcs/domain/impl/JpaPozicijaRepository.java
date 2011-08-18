package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.Pozicija;
import net.milanaleksic.mcs.domain.PozicijaRepository;
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
public class JpaPozicijaRepository extends AbstractRepository implements PozicijaRepository {

    @Override
    public List<Pozicija> getPozicijas() {
        log.debug("PozicijaRepository::getPozicijas");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pozicija> cq = builder.createQuery(Pozicija.class);
        Root<Pozicija> from = cq.from(Pozicija.class);
        cq.orderBy(builder.asc(from.get("pozicija")));
        return entityManager.createQuery(cq).getResultList();
    }
}
