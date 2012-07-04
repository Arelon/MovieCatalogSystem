package net.milanaleksic.mcs.infrastructure.persistence.jpa.service;

import com.google.common.base.Function;
import com.google.common.collect.*;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.FilmService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.persistence.TypedQuery;
import java.util.*;

@Transactional(readOnly = false)
@SuppressWarnings({"HardCodedStringLiteral"})
public class FilmServiceImpl extends AbstractService implements FilmService {

    @Override
    public void updateFilmWithChanges(Film movie) {
        entityManager.merge(movie);
    }

    @Override
    public Film updateFilmWithChanges(Film movieToBeUpdated, Zanr newZanr, Set<Medij> newMediums, Pozicija newPozicija, Iterable<Tag> selectedTags) {
        movieToBeUpdated = entityManager.merge(movieToBeUpdated);

        if (!newZanr.equals(movieToBeUpdated.getZanr())) {
            newZanr = entityManager.merge(newZanr);
            movieToBeUpdated.getZanr().removeFilm(movieToBeUpdated);
            newZanr.addFilm(movieToBeUpdated);
        }

        Set<Medij> raniji = Sets.newHashSet(movieToBeUpdated.getMedijs());

        if (!newMediums.isEmpty()) {
            newPozicija = entityManager.merge(newPozicija);
            for (Medij medij : newMediums) {
                medij = entityManager.merge(medij);
                if (raniji.contains(medij)) {
                    raniji.remove(medij);
                    if (!medij.getPozicija().equals(newPozicija)) {
                        medij.getPozicija().removeMedij(medij);
                        newPozicija.addMedij(medij);
                    }
                }
                else {
                    movieToBeUpdated.addMedij(medij);
                    newPozicija.addMedij(medij);
                }
            }
        }

        for (Medij medij : raniji) {
            if (log.isInfoEnabled())
                log.info("Removing medium from the list of mediums: "+medij.toString());
            movieToBeUpdated.removeMedij(medij);
        }

        movieToBeUpdated.refreshFilmLocation();

        movieToBeUpdated.setTags(Sets.newHashSet(Iterables.transform(selectedTags, new Function<Tag,Tag>() {
            @Override
            public Tag apply(@Nullable Tag input) {
                return entityManager.merge(input);
            }
        })));

        return movieToBeUpdated;
    }

    @Override
    public List<Film> getListOfUnmatchedMovies() {
        TypedQuery<Film> query = entityManager.createNamedQuery("getUnmatchedMovies", Film.class);
        return query.getResultList();
    }

}
