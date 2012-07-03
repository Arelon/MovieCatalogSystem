package net.milanaleksic.mcs.domain.service;

import net.milanaleksic.mcs.domain.model.*;

import java.util.List;
import java.util.Set;

public interface FilmService {

    void updateFilmWithChanges(Film film);

    void updateFilmWithChanges(Film movieToBeUpdated, Zanr newZanr, Set<Medij> newMediums, Pozicija newPozicija, Iterable<Tag> selectedTags);

    List<Film> getListOfUnmatchedMovies();
}
