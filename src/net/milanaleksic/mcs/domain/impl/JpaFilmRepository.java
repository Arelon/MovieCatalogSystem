package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

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
    public Set<Film> getFilmByCriteria(int startFrom, int maxItems, Zanr zanrFilter, TipMedija tipMedijaFilter,
                                       Pozicija pozicijaFilter, String textFilter) {
        int fetchedCount;
        int totallyFetchedCount = 0;
        Set<Film> films = new HashSet<Film>();
        do {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Film> cq = builder.createQuery(Film.class);
            Root<Film> film = cq.from(Film.class);
            cq.select(film);
            Join<Film, Medij> medij = film.join("medijs", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<Predicate>();

            ParameterExpression<String> textFilterParameter = null;
            if (textFilter != null) {
                textFilterParameter = builder.parameter(String.class, "filter");
                predicates.add(builder.or(
                        builder.like(builder.lower(film.<String>get("nazivfilma")), textFilterParameter),
                        builder.like(builder.lower(film.<String>get("prevodnazivafilma")), textFilterParameter),
                        builder.like(builder.lower(film.<String>get("komentar")), textFilterParameter)
                ));
            }
            ParameterExpression<Zanr> zanrParameter = null;
            if (zanrFilter != null)
                predicates.add(builder.equal(film.<String>get("zanr"), zanrParameter = builder.parameter(Zanr.class, "zanr")));
            ParameterExpression<TipMedija> tipMedijaParameter = null;
            if (tipMedijaFilter != null)
                predicates.add(builder.equal(medij.<TipMedija>get("tipMedija"), tipMedijaParameter = builder.parameter(TipMedija.class, "tipMedija")));
            ParameterExpression<Pozicija> pozicijaParameter = null;
            if (pozicijaFilter != null)
                predicates.add(builder.equal(medij.<Pozicija>get("pozicija"), pozicijaParameter = builder.parameter(Pozicija.class, "pozicija")));

            if (predicates.size()==1)
                cq.where(predicates.get(0));
            else if (predicates.size()>1)
                cq.where(builder.and(predicates.toArray(new Predicate[1])));

            cq.orderBy(builder.asc(medij.<String>get("tipMedija").<String>get("naziv")),
                    builder.asc(medij.<String>get("indeks")),
                    builder.asc(film.<String>get("nazivfilma")));

            TypedQuery<Film> query = entityManager.createQuery(cq);
            if (textFilter != null)
                query.setParameter(textFilterParameter, textFilter);
            if (zanrFilter != null)
                query.setParameter(zanrParameter, zanrFilter);
            if (tipMedijaFilter != null)
                query.setParameter(tipMedijaParameter, tipMedijaFilter);
            if (pozicijaFilter != null)
                query.setParameter(pozicijaParameter, pozicijaFilter);
            if (maxItems > 0) {
                query.setFirstResult(startFrom + totallyFetchedCount);
                query.setMaxResults(maxItems);
            }

            List<Film> fetched = query.getResultList();
            fetchedCount = fetched.size();
            totallyFetchedCount += fetched.size();

            if (maxItems == 0 || maxItems>fetchedCount+films.size())
                films.addAll(fetched);
            else {
                for (Film filmItem : fetched) {
                    films.add(filmItem);
                    if (films.size()==maxItems)
                        break;
                }
            }
            log.info("films.size() = "+films.size()+", fetchedCount = "+fetchedCount+", maxItems = "+maxItems);
        }
        while (maxItems == 0 || (films.size()<maxItems && fetchedCount>0));

        for (Film film : films) {
            Hibernate.initialize(film.getMedijs());
        }

        return films;
    }

}
