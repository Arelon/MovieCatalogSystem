package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.Film;
import net.milanaleksic.mcs.domain.FilmRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.*;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 11:35 PM
 */
@Repository
@Transactional
public class JpaFilmRepository extends AbstractRepository implements FilmRepository {

    @Override
    public Film getFilm(int idfilm) {
        log.debug("FilmRepository::getFilm idFilm=" + idfilm);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Film> criteriaQuery = builder.createQuery(Film.class);
        Root<Film> from = criteriaQuery.from(Film.class);
        criteriaQuery.
                select(from).
                where(
                        builder.equal(from.get("idfilm"), idfilm)
                );
        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    @Override
    public void deleteFilm(int idfilm) {
        log.debug("FilmRepository::getFilm idFilm=" + idfilm);
        entityManager.remove(entityManager.getReference(Film.class, idfilm));
    }

}
