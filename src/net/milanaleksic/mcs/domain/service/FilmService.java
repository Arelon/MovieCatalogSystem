package net.milanaleksic.mcs.domain.service;

import net.milanaleksic.mcs.domain.model.*;

import java.util.Set;

public interface FilmService {

    public void updateFilmWithChanges(Film movieToBeUpdated, Zanr newZanr, Pozicija newPozicija, Set<Medij> newMediums);

}
