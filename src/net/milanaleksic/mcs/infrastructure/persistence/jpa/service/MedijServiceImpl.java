package net.milanaleksic.mcs.infrastructure.persistence.jpa.service;

import net.milanaleksic.mcs.domain.model.Medij;
import net.milanaleksic.mcs.domain.service.MedijService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 2/28/12
 * Time: 12:21 PM
 */
@Transactional(readOnly = false)
public class MedijServiceImpl extends AbstractService implements MedijService {

    @Override
    @Transactional(propagation= Propagation.SUPPORTS, readOnly = true)
    public int getNextMedijIndeks(String mediumTypeName) {
        TypedQuery<Integer> query = entityManager.createNamedQuery("getNextMedijIndeks", Integer.class);
        query.setParameter("tipMedija", mediumTypeName);
        Integer nextMedijIndeks = query.getSingleResult();
        if (nextMedijIndeks==null)
            return 1;
        else
            return nextMedijIndeks;
    }

    @Override
    @Transactional(propagation= Propagation.SUPPORTS, readOnly = true)
    public List<Medij> getListOfUnusedMediums() {
        TypedQuery<Medij> query = entityManager.createNamedQuery("getUnusedMediums", Medij.class);
        return query.getResultList();
    }

}
