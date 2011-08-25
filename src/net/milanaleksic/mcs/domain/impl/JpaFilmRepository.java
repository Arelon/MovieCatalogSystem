package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.*;
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
    public Film getCompleteFilm(Film rawFilm) {
        rawFilm = entityManager.merge(rawFilm);
        rawFilm.getMedijs().size();
        return rawFilm;
    }

    @Override
    public void deleteFilm(Film film) {
        entityManager.remove(film);
    }

    @Override
    public void saveFilm(Film newFilm) {
        entityManager.merge(newFilm);
    }

    @Override
    public FilmsWithCount getFilmByCriteria(int startFrom, int maxItems, Zanr zanrFilter, TipMedija tipMedijaFilter,
                                       Pozicija pozicijaFilter, String textFilter) {
        int fetchedCount;
        int totallyFetchedCount = 0;
        Set<Film> films = new HashSet<Film>();
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        do {
            CriteriaQuery<Film> cq = builder.createQuery(Film.class);
            Root<Film> film = cq.from(Film.class);
            cq.select(film);

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
            ParameterExpression<String> tipMedijaParameter = null;
            if (tipMedijaFilter != null)
                predicates.add(builder.like(film.<String>get("medijListAsString"), tipMedijaParameter = builder.parameter(String.class, "medijListAsString")));
            ParameterExpression<String> pozicijaParameter = null;
            if (pozicijaFilter != null)
                predicates.add(builder.equal(film.<String>get("pozicija"), pozicijaParameter = builder.parameter(String.class, "pozicija")));

            if (predicates.size()==1)
                cq.where(predicates.get(0));
            else if (predicates.size()>1)
                cq.where(builder.and(predicates.toArray(new Predicate[1])));

            cq.orderBy(builder.asc(film.<String>get("medijListAsString")),
                    builder.asc(film.<String>get("nazivfilma")));

            TypedQuery<Film> query = entityManager.createQuery(cq);
            if (textFilter != null)
                query.setParameter(textFilterParameter, textFilter);
            if (zanrFilter != null)
                query.setParameter(zanrParameter, zanrFilter);
            if (tipMedijaFilter != null)
                query.setParameter(tipMedijaParameter, '%'+tipMedijaFilter.getNaziv()+'%');
            if (pozicijaFilter != null)
                query.setParameter(pozicijaParameter, pozicijaFilter.getPozicija());
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
        }
        while (maxItems != 0 && films.size()<maxItems && fetchedCount>0);



        CriteriaQuery<Long> cq = builder.createQuery(Long.class);
        Root<Film> film = cq.from(Film.class);
        cq.select(builder.countDistinct(film));

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
        ParameterExpression<String> tipMedijaParameter = null;
        if (tipMedijaFilter != null)
            predicates.add(builder.like(film.<String>get("medijListAsString"), tipMedijaParameter = builder.parameter(String.class, "medijListAsString")));
        ParameterExpression<String> pozicijaParameter = null;
        if (pozicijaFilter != null)
            predicates.add(builder.equal(film.<String>get("pozicija"), pozicijaParameter = builder.parameter(String.class, "pozicija")));

        if (predicates.size()==1)
            cq.where(predicates.get(0));
        else if (predicates.size()>1)
            cq.where(builder.and(predicates.toArray(new Predicate[1])));

        TypedQuery<Long> query = entityManager.createQuery(cq);
        if (textFilter != null)
            query.setParameter(textFilterParameter, textFilter);
        if (zanrFilter != null)
            query.setParameter(zanrParameter, zanrFilter);
        if (tipMedijaFilter != null)
            query.setParameter(tipMedijaParameter, '%'+tipMedijaFilter.getNaziv()+'%');
        if (pozicijaFilter != null)
            query.setParameter(pozicijaParameter, pozicijaFilter.getPozicija());

        Long count = query.getSingleResult();

        return new FilmsWithCount(films, count);
    }

}
