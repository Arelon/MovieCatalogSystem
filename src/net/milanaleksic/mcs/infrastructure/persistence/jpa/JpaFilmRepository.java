package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.domain.model.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
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
@Transactional()
public class JpaFilmRepository extends AbstractRepository implements FilmRepository {

    @Override
    public Film getCompleteFilm(Film rawFilm) {
        rawFilm = entityManager.merge(rawFilm);
        Hibernate.initialize(rawFilm.getMedijs());
        return rawFilm;
    }

    @Override
    public void deleteFilm(Film film) {
        film = entityManager.find(Film.class, film.getIdfilm());
        entityManager.remove(film);
    }

    @Override
    public void saveFilm(Film newFilm, Zanr zanr, List<Medij> medijs, Pozicija position) {
        zanr = entityManager.find(Zanr.class, zanr.getIdzanr());
        position = entityManager.find(Pozicija.class, position.getIdpozicija());
        zanr.addFilm(newFilm);
        for (Medij medij : medijs) {
            medij = entityManager.find(Medij.class, medij.getIdmedij());
            position.addMedij(medij);
            newFilm.addMedij(medij);
        }
        entityManager.persist(newFilm);
    }

    @Override
    @Transactional(propagation= Propagation.SUPPORTS, readOnly = true)
    public FilmsWithCount getFilmByCriteria(int startFrom, int maxItems, Zanr zanrFilter, TipMedija tipMedijaFilter,
                                       Pozicija pozicijaFilter, String textFilter) {
        return new FilmsWithCount(
                doGetFilmsByCriteria(startFrom, maxItems, zanrFilter, tipMedijaFilter, pozicijaFilter, textFilter),
                doGetFilmCountByCriteria(zanrFilter, tipMedijaFilter, pozicijaFilter, textFilter));
    }

    private long doGetFilmCountByCriteria(Zanr zanrFilter, TipMedija tipMedijaFilter, Pozicija pozicijaFilter, String textFilter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = builder.createQuery(Long.class);
        Root<Film> film = cq.from(Film.class);
        cq.select(builder.countDistinct(film));

        List<Predicate> predicates = new ArrayList<>();

        ParameterExpression<String> textFilterParameter = null;
        if (textFilter != null) {
            textFilterParameter = builder.parameter(String.class, "filter");
            predicates.add(builder.or(
                    builder.like(builder.lower(film.<String>get(Film_.nazivfilma)), textFilterParameter),
                    builder.like(builder.lower(film.<String>get(Film_.prevodnazivafilma)), textFilterParameter),
                    builder.like(builder.lower(film.<String>get(Film_.komentar)), textFilterParameter)
            ));
        }
        ParameterExpression<Zanr> zanrParameter = null;
        if (zanrFilter != null)
            predicates.add(builder.equal(film.<Zanr>get(Film_.zanr), zanrParameter = builder.parameter(Zanr.class, "zanr")));
        ParameterExpression<String> tipMedijaParameter = null;
        if (tipMedijaFilter != null)
            predicates.add(builder.like(film.<String>get(Film_.medijListAsString), tipMedijaParameter = builder.parameter(String.class, "medijListAsString")));
        ParameterExpression<String> pozicijaParameter = null;
        if (pozicijaFilter != null)
            predicates.add(builder.equal(film.<String>get(Film_.pozicija), pozicijaParameter = builder.parameter(String.class, "pozicija")));

        if (predicates.size()==1)
            cq.where(predicates.get(0));
        else if (predicates.size()>1) {
            Predicate[] predicateArray = new Predicate[predicates.size()];
            cq.where(builder.and(predicates.toArray(predicateArray)));
        }

        TypedQuery<Long> query = entityManager.createQuery(cq);
        if (textFilter != null)
            query.setParameter(textFilterParameter, textFilter);
        if (zanrFilter != null)
            query.setParameter(zanrParameter, zanrFilter);
        if (tipMedijaFilter != null)
            query.setParameter(tipMedijaParameter, '%'+tipMedijaFilter.getNaziv()+'%');
        if (pozicijaFilter != null)
            query.setParameter(pozicijaParameter, pozicijaFilter.getPozicija());

        query.setHint("org.hibernate.cacheable", true);

        return query.getSingleResult();
    }

    private List<Film> doGetFilmsByCriteria(int startFrom, int maxItems, Zanr zanrFilter, TipMedija tipMedijaFilter, Pozicija pozicijaFilter, String textFilter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Film> cq = builder.createQuery(Film.class);
        Root<Film> film = cq.from(Film.class);
        cq.select(film);

        List<Predicate> predicates = new ArrayList<>();

        ParameterExpression<String> textFilterParameter = null;
        if (textFilter != null) {
            textFilterParameter = builder.parameter(String.class, "filter");
            predicates.add(builder.or(
                    builder.like(builder.lower(film.<String>get(Film_.nazivfilma)), textFilterParameter),
                    builder.like(builder.lower(film.<String>get(Film_.prevodnazivafilma)), textFilterParameter),
                    builder.like(builder.lower(film.<String>get(Film_.komentar)), textFilterParameter)
            ));
        }
        ParameterExpression<Zanr> zanrParameter = null;
        if (zanrFilter != null)
            predicates.add(builder.equal(film.<Zanr>get(Film_.zanr), zanrParameter = builder.parameter(Zanr.class, "zanr")));
        ParameterExpression<String> tipMedijaParameter = null;
        if (tipMedijaFilter != null)
            predicates.add(builder.like(film.<String>get(Film_.medijListAsString), tipMedijaParameter = builder.parameter(String.class, "medijListAsString")));
        ParameterExpression<String> pozicijaParameter = null;
        if (pozicijaFilter != null)
            predicates.add(builder.equal(film.<String>get(Film_.pozicija), pozicijaParameter = builder.parameter(String.class, "pozicija")));

        if (predicates.size()==1)
            cq.where(predicates.get(0));
        else if (predicates.size()>1) {
            Predicate[] predicateArray = new Predicate[predicates.size()];
            cq.where(builder.and(predicates.toArray(predicateArray)));
        }

        cq.orderBy(builder.asc(film.<String>get(Film_.medijListAsString)),
                builder.asc(film.<String>get(Film_.nazivfilma)));

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
            query.setFirstResult(startFrom);
            query.setMaxResults(maxItems);
        }

        query.setHint("org.hibernate.cacheable", true);

        return query.getResultList();
    }

}
