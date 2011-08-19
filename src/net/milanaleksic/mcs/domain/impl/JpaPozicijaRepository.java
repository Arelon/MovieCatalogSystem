package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.*;
import net.milanaleksic.mcs.util.ApplicationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.Set;

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
        cq.orderBy(builder.asc(builder.lower(from.<String>get("pozicija"))));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public void addPozicija(String newPozicija) {
        Pozicija pozicija = new Pozicija();
        pozicija.setPozicija(newPozicija);
        entityManager.persist(pozicija);
    }

    @Override
    public void deletePozicija(String pozicija) throws ApplicationException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pozicija> cq = builder.createQuery(Pozicija.class);
        Root<Pozicija> from = cq.from(Pozicija.class);
        cq.where(builder.equal(from.<String>get("pozicija"), pozicija));
        Pozicija pozicijaToDelete = entityManager.createQuery(cq).getSingleResult();
        Set<Medij> medijs = pozicijaToDelete.getMedijs();
        if (medijs != null && medijs.size()>0)
            throw new ApplicationException("You can't delete this Position since there are "+medijs.size()+" movies in that position");
        entityManager.remove(pozicijaToDelete);
    }
}
