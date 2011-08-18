package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.MedijRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
@Repository
@Transactional
public class JpaMedijRepository extends AbstractRepository implements MedijRepository {

    @Override
    public int getNextMedijIndeks(String s) {
//				Query query = session.createQuery("select max(indeks)+1 from Medij m where m.tipMedija.naziv=:tipMedija");
//				String selected = rbCD.getSelection() ? "CD" : "DVD";
//				query.setString("tipMedija", selected);
//				if (query.list().get(0)==null)
//					return new Integer("1");
//				else
//					return new Integer(query.list().get(0).toString());
        throw new IllegalStateException("NYI");
    }
}
