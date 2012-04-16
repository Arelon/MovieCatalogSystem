package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.milanaleksic.mcs.domain.model.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 11:35 PM
 */
@Repository
@Transactional
@SuppressWarnings({"HardCodedStringLiteral"})
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
    public FilmsWithCount getFilmByCriteria(int startFrom, int maxItems, Optional<Zanr> zanrFilter, Optional<TipMedija> tipMedijaFilter,
                                            Optional<Pozicija> pozicijaFilter, Optional<String> textFilter, SingularAttribute<Film,String> orderByAttribute, boolean ascending) {
        return new FilmsWithCount(
                doGetFilmsByCriteria(startFrom, maxItems, zanrFilter, tipMedijaFilter, pozicijaFilter,
                        textFilter, orderByAttribute, ascending),
                doGetFilmCountByCriteria(zanrFilter, tipMedijaFilter, pozicijaFilter, textFilter));
    }

    private long doGetFilmCountByCriteria(Optional<Zanr> zanrFilter, Optional<TipMedija> tipMedijaFilter, Optional<Pozicija> pozicijaFilter, Optional<String> textFilter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = builder.createQuery(Long.class);
        Root<Film> film = cq.from(Film.class);
        cq.select(builder.countDistinct(film));

        List<Predicate> predicates = Lists.newLinkedList();

        Optional<ParameterExpression<String>> textFilterParameter = Optional.absent();
        if (textFilter.isPresent()) {
            textFilterParameter = Optional.of(builder.parameter(String.class, "filter"));
            predicates.add(builder.or(
                    builder.like(builder.lower(film.<String>get(Film_.nazivfilma)), textFilterParameter.get()),
                    builder.like(builder.lower(film.<String>get(Film_.prevodnazivafilma)), textFilterParameter.get()),
                    builder.like(builder.lower(film.<String>get(Film_.komentar)), textFilterParameter.get())
            ));
        }
        Optional<ParameterExpression<Zanr>> zanrParameter = Optional.absent();
        if (zanrFilter.isPresent()) {
            zanrParameter = Optional.of(builder.parameter(Zanr.class, "zanr"));
            predicates.add(builder.equal(film.<Zanr>get(Film_.zanr), zanrParameter.get()));
        }
        Optional<ParameterExpression<String>> tipMedijaParameter = Optional.absent();
        if (tipMedijaFilter.isPresent()) {
            tipMedijaParameter = Optional.of(builder.parameter(String.class, "medijListAsString"));
            predicates.add(builder.like(film.<String>get(Film_.medijListAsString), tipMedijaParameter.get()));
        }
        Optional<ParameterExpression<String>> pozicijaParameter = Optional.absent();
        if (pozicijaFilter.isPresent()) {
            pozicijaParameter = Optional.of(builder.parameter(String.class, "pozicija"));
            predicates.add(builder.equal(film.<String>get(Film_.pozicija), pozicijaParameter));
        }

        if (predicates.size()==1)
            cq.where(predicates.get(0));
        else if (predicates.size()>1) {
            Predicate[] predicateArray = new Predicate[predicates.size()];
            cq.where(builder.and(predicates.toArray(predicateArray)));
        }

        TypedQuery<Long> query = entityManager.createQuery(cq);
        if (textFilter.isPresent())
            query.setParameter(textFilterParameter.get(), textFilter.get());
        if (zanrFilter.isPresent())
            query.setParameter(zanrParameter.get(), zanrFilter.get());
        if (tipMedijaFilter.isPresent())
            query.setParameter(tipMedijaParameter.get(), '%'+tipMedijaFilter.get().getNaziv()+'%');
        if (pozicijaFilter.isPresent())
            query.setParameter(pozicijaParameter.get(), pozicijaFilter.get().getPozicija());

        query.setHint(HIBERNATE_HINT_CACHEABLE, true);

        return query.getSingleResult();
    }

    private List<Film> doGetFilmsByCriteria(int startFrom, int maxItems, Optional<Zanr> zanrFilter, Optional<TipMedija> tipMedijaFilter, Optional<Pozicija> pozicijaFilter,
                                            Optional<String> textFilter, SingularAttribute<Film,String> orderByAttribute, boolean ascending) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Film> cq = builder.createQuery(Film.class);
        Root<Film> film = cq.from(Film.class);
        cq.select(film);

        List<Predicate> predicates = Lists.newLinkedList();

        Optional<ParameterExpression<String>> textFilterParameter = Optional.absent();
        if (textFilter.isPresent()) {
            textFilterParameter = Optional.of(builder.parameter(String.class, "filter"));
            predicates.add(builder.or(
                    builder.like(builder.lower(film.<String>get(Film_.nazivfilma)), textFilterParameter.get()),
                    builder.like(builder.lower(film.<String>get(Film_.prevodnazivafilma)), textFilterParameter.get()),
                    builder.like(builder.lower(film.<String>get(Film_.komentar)), textFilterParameter.get())
            ));
        }
        Optional<ParameterExpression<Zanr>> zanrParameter = Optional.absent();
        if (zanrFilter.isPresent()) {
            zanrParameter = Optional.of(builder.parameter(Zanr.class, "zanr"));
            predicates.add(builder.equal(film.<Zanr>get(Film_.zanr), zanrParameter.get()));
        }
        Optional<ParameterExpression<String>> tipMedijaParameter = Optional.absent();
        if (tipMedijaFilter.isPresent()) {
            tipMedijaParameter = Optional.of(builder.parameter(String.class, "medijListAsString"));
            predicates.add(builder.like(film.<String>get(Film_.medijListAsString), tipMedijaParameter.get()));
        }
        Optional<ParameterExpression<String>> pozicijaParameter = Optional.absent();
        if (pozicijaFilter.isPresent()) {
            pozicijaParameter = Optional.of(builder.parameter(String.class, "pozicija"));
            predicates.add(builder.equal(film.<String>get(Film_.pozicija), pozicijaParameter));
        }

        if (predicates.size()==1)
            cq.where(predicates.get(0));
        else if (predicates.size()>1) {
            Predicate[] predicateArray = new Predicate[predicates.size()];
            cq.where(builder.and(predicates.toArray(predicateArray)));
        }

        if (ascending) {
            cq.orderBy(builder.asc(film.<String>get(orderByAttribute)),
                builder.asc(film.<String>get(Film_.nazivfilma)));
        } else {
            cq.orderBy(builder.desc(film.<String>get(orderByAttribute)),
                builder.asc(film.<String>get(Film_.nazivfilma)));
        }

        TypedQuery<Film> query = entityManager.createQuery(cq);
        if (textFilter.isPresent())
            query.setParameter(textFilterParameter.get(), textFilter.get());
        if (zanrFilter.isPresent())
            query.setParameter(zanrParameter.get(), zanrFilter.get());
        if (tipMedijaFilter.isPresent())
            query.setParameter(tipMedijaParameter.get(), '%'+tipMedijaFilter.get().getNaziv()+'%');
        if (pozicijaFilter.isPresent())
            query.setParameter(pozicijaParameter.get(), pozicijaFilter.get().getPozicija());
        if (maxItems > 0) {
            query.setFirstResult(startFrom);
            query.setMaxResults(maxItems);
        }

        query.setHint(HIBERNATE_HINT_CACHEABLE, true);

        return query.getResultList();
    }

}
