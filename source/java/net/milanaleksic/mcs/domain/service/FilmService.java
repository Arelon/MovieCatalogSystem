package net.milanaleksic.mcs.domain.service;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.domain.model.*;

import java.util.List;
import java.util.Set;

public interface FilmService {

    void updateFilmWithChanges(Film film);

    Film updateFilmWithChanges(Film movieToBeUpdated, Zanr newZanr, Set<Medij> newMediums, Optional<Pozicija> newPozicija, Iterable<Tag> selectedTags);

    List<Film> getListOfUnmatchedMovies();
}
