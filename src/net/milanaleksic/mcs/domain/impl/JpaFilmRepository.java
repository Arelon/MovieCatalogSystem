package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 11:35 PM
 */
@Repository
@Transactional
public class JpaFilmRepository extends AbstractRepository implements FilmRepository {

    @Override
    public void deleteFilm(Film film) {
        entityManager.remove(film);
    }

    @Override
    public void saveFilm(Film newFilm) {
        entityManager.persist(newFilm);
    }

    @Override
    public List<Film> getFilmByCriteria(int startFrom, int maxItems, Zanr zanrFilter, TipMedija tipMedijaFilter,
                                        Pozicija pozicijaFilter, String textFilter) {
//        StringBuilder buff = new StringBuilder("select f from Film f where idfilm in (select f.idfilm from Film f, Medij m where f.idfilm in elements(m.films)");
//        buff.append(" order by m.tipMedija.naziv, m.indeks, f.nazivfilma)");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Film> cq = builder.createQuery(Film.class);
        Root<Film> film = cq.from(Film.class);
        cq.select(film)
                .distinct(true);
        Join<Film, Medij> medij = film.join("medijs", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (textFilter != null)
            predicates.add(builder.or(
                    builder.like(builder.lower(film.<String>get("nazivfilma")), textFilter),
                    builder.like(builder.lower(film.<String>get("prevodnazivafilma")), textFilter),
                    builder.like(builder.lower(film.<String>get("komentar")), textFilter)
            ));
        if (zanrFilter != null)
            predicates.add(builder.equal(film.<String>get("zanr"), zanrFilter));
        if (tipMedijaFilter != null)
            predicates.add(builder.equal(medij.<TipMedija>get("tipMedija"), tipMedijaFilter));
        if (pozicijaFilter != null)
            predicates.add(builder.equal(medij.<TipMedija>get("pozicija"), pozicijaFilter));

        if (predicates.size()==1)
            cq.where(predicates.get(0));
        else if (predicates.size()>1)
            cq.where(builder.and(predicates.toArray(new Predicate[0])));

        cq.orderBy(//builder.asc(medij.<String>get("tipMedija.naziv")),
                builder.asc(medij.<String>get("indeks")),
                builder.asc(film.<String>get("nazivfilma")));
        TypedQuery<Film> query = entityManager.createQuery(cq);
        if (maxItems > 0) {
            query.setFirstResult(startFrom);
            query.setMaxResults(maxItems);
        }
        return query.getResultList();
    }

}
