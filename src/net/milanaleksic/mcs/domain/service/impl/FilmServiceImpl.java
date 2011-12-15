package net.milanaleksic.mcs.domain.service.impl;

import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.AbstractService;
import net.milanaleksic.mcs.domain.service.FilmService;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Transactional(readOnly = false)
public class FilmServiceImpl extends AbstractService implements FilmService {

    @Override
    public void updateFilmWithChanges(Film movieToBeUpdated, Zanr newZanr, Pozicija newPozicija, Set<Medij> newMediums) {
        movieToBeUpdated = entityManager.merge(movieToBeUpdated);

        if (!newZanr.equals(movieToBeUpdated.getZanr())) {
            newZanr = entityManager.merge(newZanr);
            movieToBeUpdated.getZanr().removeFilm(movieToBeUpdated);
            newZanr.addFilm(movieToBeUpdated);
        }

        Set<Medij> raniji = new HashSet<Medij>();
        for (Medij medij : movieToBeUpdated.getMedijs())
            raniji.add(medij);

        if (!newMediums.isEmpty()) {
            newPozicija = entityManager.merge(newPozicija);
            for (Medij medij : newMediums) {
                medij = entityManager.merge(medij);
                if (raniji.contains(medij)) {
                    if (!medij.getPozicija().equals(newPozicija)) {
                        medij.getPozicija().removeMedij(medij);
                        newPozicija.addMedij(medij);
                    }
                    raniji.remove(medij);
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
            medij.removeFilm(movieToBeUpdated);
            movieToBeUpdated.removeMedij(medij);
        }
    }

}
